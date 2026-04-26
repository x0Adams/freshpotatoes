import { useState, useEffect, useRef, useCallback } from 'react'
import { movieApi, genreApi } from '../services/api'
import MovieTrack, { MovieTrackSkeleton } from './MovieTrack'

/**
 * Single Genre Track Component
 */
function GenreMoviesByKey({ genreName, initialMovies }) {
  const [movies, setMovies] = useState(initialMovies || [])
  const [loading, setLoading] = useState(!initialMovies)
  const [page, setPage] = useState(0)
  const [fetchingMore, setFetchingMore] = useState(false)

  const displayTitle = genreName.toLowerCase().includes('film') 
    ? genreName 
    : `${genreName} movies`;

  useEffect(() => {
    if (!initialMovies) {
      setLoading(true)
      movieApi.getMoviesByGenre(genreName, 0, 20)
        .then(data => setMovies(data))
        .finally(() => setLoading(false))
    }
  }, [genreName, initialMovies])

  const handleLoadMore = async () => {
    setFetchingMore(true);
    const nextPage = page + 1;
    try {
      const newMovies = await movieApi.getMoviesByGenre(genreName, nextPage, 20);
      const newUniqueMovies = newMovies.filter(nm => !movies.find(m => m.id === nm.id));
      setMovies(prev => [...prev, ...newUniqueMovies]);
      setPage(nextPage);
    } catch (err) {
      console.error(err);
    } finally {
      setFetchingMore(false);
    }
  }

  if (loading) return <MovieTrackSkeleton title={displayTitle} hideTopBorder={true} />

  return (
    <MovieTrack 
      title={displayTitle}
      movies={movies}
      fetchingMore={fetchingMore}
      onLoadMore={handleLoadMore}
      hideTopBorder={true}
    />
  )
}

/**
 * Container that progressively loads shuffled genres as the user scrolls
 */
export function MultiGenreMovies() {
  const [allShuffledGenres, setAllShuffledGenres] = useState([])
  const [visibleGenres, setVisibleGenres] = useState([])
  const [loading, setLoading] = useState(true)
  const [currentIndex, setCurrentIndex] = useState(0)
  const [isloadingNext, setIsLoadingNext] = useState(false)
  
  // Use a ref to track index to avoid stale closures in the observer/async calls
  const indexRef = useRef(0)

  const observer = useRef()
  const lastElementRef = useCallback(node => {
    if (loading || isloadingNext) return
    if (observer.current) observer.current.disconnect()
    
    observer.current = new IntersectionObserver(entries => {
      // Trigger if sentinel is visible AND we have more genres to try
      if (entries[0].isIntersecting && indexRef.current < allShuffledGenres.length) {
        loadNextValidGenre()
      }
    }, { threshold: 0.1 })
    
    if (node) observer.current.observe(node)
  }, [loading, isloadingNext, allShuffledGenres])

  // 1. Initial Load: Fetch and Shuffle All Genres
  useEffect(() => {
    async function init() {
      try {
        const all = await genreApi.getAll()
        const filtered = all.filter(g => g.name && !g.name.includes('/') && !g.name.includes('\\'))
        const shuffled = [...filtered].sort(() => 0.5 - Math.random())
        setAllShuffledGenres(shuffled)
        setLoading(false)
      } catch (err) {
        console.error("Failed to init MultiGenreMovies:", err)
        setLoading(false)
      }
    }
    init()
  }, [])

  // 2. Load the next genre that actually has movies
  const loadNextValidGenre = async () => {
    // Basic guards
    if (isloadingNext || indexRef.current >= allShuffledGenres.length) return
    
    setIsLoadingNext(true)
    let found = false
    const currentPool = allShuffledGenres

    try {
      // Loop until we find a genre with at least 4 movies or run out of genres
      while (indexRef.current < currentPool.length && !found) {
        const genreToLoad = currentPool[indexRef.current]
        
        // IMPORTANT: Increment BEFORE the async call to prevent other 
        // observer triggers from picking up the same index
        indexRef.current += 1
        setCurrentIndex(indexRef.current)

        try {
          const movies = await movieApi.getMoviesByGenre(genreToLoad.name, 0, 15)
          
          if (movies && movies.length >= 4) {
            setVisibleGenres(prev => {
              // Final safety check: don't add if already there
              if (prev.some(g => g.name === genreToLoad.name)) return prev
              return [...prev, { ...genreToLoad, initialMovies: movies }]
            })
            found = true
          }
        } catch (err) {
          // Log only real errors, not 404s (which just mean empty genre)
          if (!err.message?.includes('404')) {
            console.debug(`Error fetching genre ${genreToLoad.name}:`, err)
          }
        }
      }
    } finally {
      setIsLoadingNext(false)
    }
  }

  // 3. Trigger first genre load once pool is ready
  useEffect(() => {
    if (!loading && allShuffledGenres.length > 0 && visibleGenres.length === 0) {
      loadNextValidGenre()
    }
  }, [loading, allShuffledGenres])

  if (loading) return (
    <div className="container py-5">
      <MovieTrackSkeleton title="Finding fresh genres..." hideTopBorder={true} />
    </div>
  )

  return (
    <>
      {visibleGenres.map((g) => (
        <GenreMoviesByKey 
          key={g.name} 
          genreName={g.name} 
          initialMovies={g.initialMovies} 
        />
      ))}
      
      {/* Sentinel element for infinite scroll */}
      <div ref={lastElementRef} className="d-flex justify-content-center align-items-center" style={{ minHeight: '150px', margin: '2rem 0' }}>
        {isloadingNext && (
          <div className="spinner-border text-warning" role="status" style={{ opacity: 0.6 }}>
            <span className="visually-hidden">Loading next genre...</span>
          </div>
        )}
      </div>
    </>
  )
}

function GenreMovies() {
    // This is the default export, but we mostly use MultiGenreMovies
    return <MultiGenreMovies />;
}

export default GenreMovies;