import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { movieApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import MoviePoster from '../components/MoviePoster'
import DiscoverMovies from '../components/DiscoverMovies'

//potato rating display
function PotatoRating({ rating, userRating, onRate, isSubmitting }) {
  const [hover, setHover] = useState(0)
  
  return (
    <div className="potato-rating-container">
      <div className="potato-rating">
        {[1, 2, 3, 4, 5].map(n => {
          const displayRating = hover || userRating || rating
          const full = displayRating >= n
          const half = !full && displayRating >= n - 0.5
          
          return (
            <span
              key={n}
              className={`potato ${full ? 'lit' : half ? 'half' : ''} ${onRate ? 'clickable' : ''} ${isSubmitting ? 'opacity-50' : ''}`}
              onMouseEnter={() => onRate && setHover(n)}
              onMouseLeave={() => onRate && setHover(0)}
              onClick={() => onRate && !isSubmitting && onRate(n)}
            >
              🥔
            </span>
          )
        })}
        <span className="potato-score">{userRating ? `Your rating: ${userRating}` : `${rating || 'No'} / 5`}</span>
      </div>
      {onRate && !userRating && <small className="text-secondary">Click a potato to rate!</small>}
    </div>
  )
}

function CommentsSection() {
  const [comments] = useState([]) // Placeholder as there is no comments API yet

  return (
    <div className="movie-section">
      <h2 className="movie-section-title">
        Comments <span>({comments.length})</span>
      </h2>

      <div className="comment-form opacity-50">
        <textarea
          className="form-control comment-input mb-2"
          rows={3}
          placeholder="Comments are coming soon!"
          disabled
        />
        <button className="btn btn-warning btn-sm fw-semibold px-4 text-dark" disabled>
          Post Comment
        </button>
      </div>
    </div>
  )
}

//main page
function MoviePage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [movie, setMovie] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [userRating, setUserRating] = useState(0)
  const [isSubmittingRating, setIsSubmittingRating] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError(null)
    movieApi.getById(id)
      .then(data => {
        setMovie(data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        // If it's a 404 or looks like a missing movie, we show the "No Info" state
        if (err.message.includes('404') || err.message.toLowerCase().includes('not found')) {
          setMovie({ id, title: 'Unknown Movie', genres: [], actors: [] })
        } else {
          setError("Movie not found or server error.")
        }
        setLoading(false)
      })
  }, [id])

  const handleRate = async (rating) => {
    if (!user) {
      alert("Please log in to rate movies!")
      return
    }
    
    setIsSubmittingRating(true)
    try {
      const token = localStorage.getItem('accessToken')
      await movieApi.rate(id, rating, token)
      setUserRating(rating)
      // Refresh movie data to see updated average
      const updatedMovie = await movieApi.getById(id)
      setMovie(updatedMovie)
    } catch (err) {
      alert(err.message || "Failed to submit rating")
    } finally {
      setIsSubmittingRating(false)
    }
  }

  if (loading) return (
    <div className="text-center py-5 mt-5">
      <div className="spinner-border text-warning" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
    </div>
  )

  if (error || !movie) return (
    <div className="text-center py-5 mt-5">
      <p className="text-secondary">{error || "Movie not found."}</p>
      <Link to="/" className="btn btn-outline-warning btn-sm mt-3">← Back to Home</Link>
    </div>
  )

  // Check if movie has "no information"
  const isPlaceholderDescription = !movie.description || movie.description === movie.title || movie.description === 'Not Fetched';
  const hasNoInfo = !movie.title || movie.title === 'Unknown' || (isPlaceholderDescription && !movie.posterUrl);

  if (hasNoInfo) {
    return (
      <div className="movie-page">
        <div className="container py-5 text-center">
          <div className="py-5 bg-dark border border-secondary rounded-4 opacity-75" style={{ marginTop: '24px' }}>
            <h1 className="display-1 mb-4">🥔</h1>
            <h2 className="fw-bold text-white mb-3">{movie.title || 'No Information Available'}</h2>
            <p className="text-secondary fs-5 mb-4 mx-auto" style={{ maxWidth: '600px' }}>
              We're sorry, but we don't have enough details about this movie in our database yet. 
              Our movie miners are working hard to gather posters, ratings, and descriptions!
            </p>
            <div className="d-flex justify-content-center gap-3">
              <Link to="/" className="btn btn-warning px-4 fw-semibold text-dark">
                Explore Other Movies
              </Link>
              <button onClick={() => window.location.reload()} className="btn btn-outline-secondary px-4">
                <i className="bi bi-arrow-clockwise me-1" /> Refresh
              </button>
            </div>
          </div>
        </div>
        
        <div className="mt-5">
          <DiscoverMovies />
        </div>
      </div>
    )
  }

  return (
    <div className="movie-page">

      {/* hero*/}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* poster */}
        <div className="movie-poster-frame">
          {movie.posterUrl ? (
            <img src={movie.posterUrl} alt="" aria-hidden className="movie-poster-glow" />
          ) : (
            <div className="movie-poster-glow bg-dark opacity-50" aria-hidden />
          )}
          <MoviePoster
            posterUrl={movie.posterUrl}
            title={movie.title}
            className="movie-poster-img"
          />
        </div>

        {/* info */}
        <div className="movie-info">

          <div className="movie-genres">
            {movie.genres && movie.genres.map(g => (
              <span key={g} className="movie-genre-badge">{g}</span>
            ))}
          </div>

          <h1 className="movie-title">{movie.title}</h1>

          <div className="movie-meta-row">
            <span><i className="bi bi-calendar3" /> {movie.year}</span>
            {movie.country && (
              <span><i className="bi bi-globe2" /> {movie.country}</span>
            )}
          </div>

          {/* potato rating */}
          <PotatoRating 
            rating={movie.rating} 
            userRating={userRating}
            onRate={user ? handleRate : null}
            isSubmitting={isSubmittingRating}
          />

          {/* Links */}
          <div className="movie-links">
            {movie.trailerUrl && (
              <a
                href={movie.trailerUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-warning btn-sm fw-semibold text-dark px-3" >
                <i className="bi bi-play-fill me-1" /> Watch Trailer
              </a>
            )}
            {movie.youtubeUrl && (
              <a
                href={movie.youtubeUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-outline-secondary btn-sm px-3">
                <i className="bi bi-youtube me-1" /> YouTube Page
              </a>
            )}
          </div>

          {/* credits */}
          <div className="movie-credits">
            {movie.directors && movie.directors.length > 0 && (
              <div className="movie-credit-group">
                <h6>Directed by</h6>
                <p>{movie.directors.join(', ')}</p>
              </div>
            )}
            {movie.actors && movie.actors.length > 0 && (
              <div className="movie-credit-group">
                <h6>Starring</h6>
                <p>{movie.actors.join(', ')}</p>
              </div>
            )}
          </div>

          {/* description */}
          {movie.description && (
            <p className="movie-description">{movie.description}</p>
          )}

        </div>
      </div>

      {/* recom */}
      <DiscoverMovies />

      {/*comments*/}
      <CommentsSection />

    </div>
  )
}

export default MoviePage