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
    fetchMoviesData(startPage)
      .then(selectedMovies => setMovies(selectedMovies))
      .catch(err => console.error("Failed to fetch discover movies:", err))
      .finally(() => setLoading(false))
  }, [])

  const fetchMoviesData = async (pageNum) => {
    const data = await movieApi.getMovies(pageNum, 20)
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
    setFetchingMore(true);
    const nextPage = page + 1;
    try {
      const newMovies = await fetchMoviesData(nextPage);
      const newUniqueMovies = newMovies.filter(nm => !movies.find(m => m.id === nm.id));
      setMovies(prev => [...prev, ...newUniqueMovies]);
      setPage(nextPage);
    } catch (err) {
      console.error("Failed to load more discover movies:", err);
    } finally {
      setFetchingMore(false);
    }
  }

  if (loading) return <MovieTrackSkeleton title="Discover Movies" />

  if (movies.length === 0) return null

  return (
    <MovieTrack 
      title="Discover Movies"
      movies={movies}
      fetchingMore={fetchingMore}
      onLoadMore={handleLoadMore}
    />
  )
}

export default DiscoverMovies