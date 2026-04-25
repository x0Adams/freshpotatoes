import { useState, useEffect } from 'react'
import { movieApi } from '../services/api'
import MovieTrack, { MovieTrackSkeleton } from './MovieTrack'

function DiscoverMovies() {
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [fetchingMore, setFetchingMore] = useState(false)

  useEffect(() => {
    const startPage = Math.floor(Math.random() * 20)
    setPage(startPage)
    movieApi.getMovies(startPage, 20)
      .then(data => setMovies(data))
      .catch(err => console.error("Failed to fetch discover movies:", err))
      .finally(() => setLoading(false))
  }, [])

  const handleLoadMore = async () => {
    setFetchingMore(true);
    const nextPage = page + 1;
    try {
      const newMovies = await movieApi.getMovies(nextPage, 20);
      const newUniqueMovies = newMovies.filter(nm => !movies.find(m => m.id === nm.id));
      setMovies(prev => [...prev, ...newUniqueMovies]);
      setPage(nextPage);
    } catch (err) {
      console.error("Failed to load more discover movies:", err);
    } finally {
      setFetchingMore(false);
    }
  }

  if (loading) return <MovieTrackSkeleton title="Discover movies" />

  if (movies.length === 0) return null

  return (
    <MovieTrack 
      title="Discover movies"
      movies={movies}
      fetchingMore={fetchingMore}
      onLoadMore={handleLoadMore}
    />
  )
}

export default DiscoverMovies