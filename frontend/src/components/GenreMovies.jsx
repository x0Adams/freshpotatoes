import { useState, useEffect } from 'react'
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

  const displayTitle = genreName.toLowerCase().includes('film') 
    ? genreName 
    : `${genreName} movies`;

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
 * Container that finds 3 valid genres (>= 5 movies each)
 */
export function MultiGenreMovies() {
  const [validGenres, setValidGenres] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function findValidGenres() {
      try {
        const allGenres = await genreApi.getAll();
        const shuffled = [...allGenres].sort(() => 0.5 - Math.random());
        const selected = [];
        
        // We iterate through shuffled genres until we find 3 that have at least 5 movies
        for (const g of shuffled) {
          const movies = await movieApi.getMoviesByGenre(g.name, 0, 20);
          if (movies.length >= 5) {
            selected.push({ ...g, initialMovies: movies });
          }
          if (selected.length >= 3) break;
        }
        setValidGenres(selected);
      } catch (err) {
        console.error("Failed to find valid genres:", err);
      } finally {
        setLoading(false);
      }
    }
    findValidGenres();
  }, [])

  if (loading) return (
    <>
      <MovieTrackSkeleton title="Finding fresh genres..." hideTopBorder={true} />
      <MovieTrackSkeleton title="Finding fresh genres..." hideTopBorder={true} />
      <MovieTrackSkeleton title="Finding fresh genres..." hideTopBorder={true} />
    </>
  )

  return (
    <>
      {validGenres.map(g => (
        <GenreMoviesByKey key={g.id} genreName={g.name} initialMovies={g.initialMovies} />
      ))}
    </>
  )
}

function GenreMovies() {
    // This is the default export, but we mostly use MultiGenreMovies
    return <MultiGenreMovies />;
}

export default GenreMovies;