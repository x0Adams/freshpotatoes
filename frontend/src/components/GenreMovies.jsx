import { useState, useEffect } from 'react'
import { movieApi, genreApi } from '../services/api'
import MovieTrack, { MovieTrackSkeleton } from './MovieTrack'

function GenreMovies() {
  const [movies, setMovies] = useState([])
  const [genre, setGenre] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [fetchingMore, setFetchingMore] = useState(false)

  // We use a singleton-like fetch to avoid hammering the genre API if multiple components mount at once
  // but for simplicity here we just fetch normally.
  useEffect(() => {
    genreApi.getAll()
      .then(genres => {
        if (!genres || genres.length === 0) return;
        
        // Find genres that aren't already being displayed if we can, 
        // but since components don't know about each other, we'll just use random.
        // To truly avoid duplicates, we'd need to lift state up.
        const randomGenre = genres[Math.floor(Math.random() * genres.length)];
        setGenre(randomGenre.name);
        
        return fetchMoviesData(randomGenre.name, 0).then(selectedMovies => {
          setMovies(selectedMovies);
        });
      })
      .catch(err => console.error("Failed to fetch genre movies:", err))
      .finally(() => setLoading(false))
  }, [])

  const fetchMoviesData = async (genreName, pageNum) => {
    const data = await movieApi.getMoviesByGenre(genreName, pageNum, 20)
    const withPosters = data.filter(m => m.posterUrl);
    const withoutPosters = data.filter(m => !m.posterUrl);
    
    let selectedMovies = withPosters.slice(0, 20);
    
    if (selectedMovies.length < 20) {
      const needed = 20 - selectedMovies.length;
      const fillerMovies = withoutPosters.slice(0, needed);
      
      const fetchedFillerMovies = await Promise.all(
        fillerMovies.map(async m => {
          try {
            const fullMovie = await movieApi.getById(m.id);
            return { ...m, posterUrl: fullMovie.posterUrl };
          } catch {
            return m;
          }
        })
      );
      
      selectedMovies = [...selectedMovies, ...fetchedFillerMovies];
    }
    return selectedMovies;
  }

  const handleLoadMore = async () => {
    if (!genre) return;
    setFetchingMore(true);
    const nextPage = page + 1;
    try {
      const newMovies = await fetchMoviesData(genre, nextPage);
      const newUniqueMovies = newMovies.filter(nm => !movies.find(m => m.id === nm.id));
      setMovies(prev => [...prev, ...newUniqueMovies]);
      setPage(nextPage);
    } catch (err) {
      console.error("Failed to load more genre movies:", err);
    } finally {
      setFetchingMore(false);
    }
  }

  if (loading) return <MovieTrackSkeleton title="Loading Genre..." hideTopBorder={true} />

  if (movies.length === 0) return null

  return (
    <MovieTrack 
      title={`${genre} Movies`}
      movies={movies}
      fetchingMore={fetchingMore}
      onLoadMore={handleLoadMore}
      hideTopBorder={true}
    />
  )
}

/**
 * Enhanced version that can take an excluded list to avoid duplicates
 */
export function MultiGenreMovies() {
  const [genres, setGenres] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    genreApi.getAll()
      .then(data => {
        // Pick 3 unique random genres
        const shuffled = [...data].sort(() => 0.5 - Math.random());
        setGenres(shuffled.slice(0, 3));
      })
      .catch(err => console.error(err))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <>
      <MovieTrackSkeleton title="Loading..." hideTopBorder={true} />
      <MovieTrackSkeleton title="Loading..." hideTopBorder={true} />
      <MovieTrackSkeleton title="Loading..." hideTopBorder={true} />
    </>
  )

  return (
    <>
      {genres.map(g => (
        <GenreMoviesByKey key={g.id} genreName={g.name} />
      ))}
    </>
  )
}

function GenreMoviesByKey({ genreName }) {
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [fetchingMore, setFetchingMore] = useState(false)

  useEffect(() => {
    fetchMoviesData(genreName, 0)
      .then(data => setMovies(data))
      .finally(() => setLoading(false))
  }, [genreName])

  const fetchMoviesData = async (name, pageNum) => {
    const data = await movieApi.getMoviesByGenre(name, pageNum, 20)
    const withPosters = data.filter(m => m.posterUrl);
    const withoutPosters = data.filter(m => !m.posterUrl);
    let selectedMovies = withPosters.slice(0, 20);
    if (selectedMovies.length < 20) {
      const needed = 20 - selectedMovies.length;
      const fillerMovies = withoutPosters.slice(0, needed);
      const fetchedFillerMovies = await Promise.all(
        fillerMovies.map(async m => {
          try {
            const fullMovie = await movieApi.getById(m.id);
            return { ...m, posterUrl: fullMovie.posterUrl };
          } catch { return m; }
        })
      );
      selectedMovies = [...selectedMovies, ...fetchedFillerMovies];
    }
    return selectedMovies;
  }

  const handleLoadMore = async () => {
    setFetchingMore(true);
    const nextPage = page + 1;
    try {
      const newMovies = await fetchMoviesData(genreName, nextPage);
      const newUniqueMovies = newMovies.filter(nm => !movies.find(m => m.id === nm.id));
      setMovies(prev => [...prev, ...newUniqueMovies]);
      setPage(nextPage);
    } catch (err) {
      console.error(err);
    } finally {
      setFetchingMore(false);
    }
  }

  if (loading) return <MovieTrackSkeleton title={`${genreName} Movies`} hideTopBorder={true} />

  return (
    <MovieTrack 
      title={`${genreName} Movies`}
      movies={movies}
      fetchingMore={fetchingMore}
      onLoadMore={handleLoadMore}
      hideTopBorder={true}
    />
  )
}

export default GenreMovies