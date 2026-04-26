import { useState, useEffect } from 'react'
import { recommendationApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import MovieTrack, { MovieTrackSkeleton } from './MovieTrack'

function RecommendationsTrack({ title = "For You", hideIfEmpty = true }) {
  const { user } = useAuth()
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!user) return

    setLoading(true)
    const token = localStorage.getItem('accessToken')
    
    recommendationApi.getForMe(token)
      .then(data => {
        setMovies(data)
        setLoading(false)
      })
      .catch(err => {
        console.error("Recommendations fetch failed:", err)
        setError(err.message)
        setLoading(false)
      })
  }, [user])

  if (!user) return null
  if (loading) return <MovieTrackSkeleton title={title} hideTopBorder={true} />
  if (error) return null // Silently hide on error to not ruin experience
  if (hideIfEmpty && movies.length === 0) return null

  return (
    <MovieTrack 
      title={title}
      movies={movies}
      hideTopBorder={true}
    />
  )
}

export default RecommendationsTrack