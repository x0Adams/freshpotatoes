import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { movieApi, reviewApi, playlistApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import MoviePoster from '../components/MoviePoster'
import DiscoverMovies from '../components/DiscoverMovies'
import TrailerModal from '../components/TrailerModal'
import CreatePlaylistModal from '../components/CreatePlaylistModal'
import testBg from '../assets/test_bg.jpg'
import potatoIcon from '../assets/potato.png'

//potato rating display
function PotatoRating({ rating, userRating, onRate, onDelete, isSubmitting }) {
  const [hover, setHover] = useState(0)
  
  return (
    <div className="potato-rating-container d-flex align-items-center flex-wrap gap-3">
      <div className="potato-rating">
        {[1, 2, 3, 4, 5].map(n => {
          const displayRating = hover || userRating || rating
          const isLit = displayRating >= n
          const isHalf = !isLit && displayRating >= n - 0.5
          
          return (
            <span
              key={n}
              className={`potato-wrapper ${onRate ? 'clickable' : ''} ${isSubmitting ? 'opacity-50' : ''}`}
              onMouseEnter={() => onRate && setHover(n)}
              onMouseLeave={() => onRate && setHover(0)}
              onClick={() => onRate && !isSubmitting && onRate(n)}
            >
              <img 
                src={potatoIcon} 
                alt="potato" 
                className={`potato-img ${isLit ? 'lit' : isHalf ? 'half' : 'dim'}`} 
              />
            </span>
          )
        })}
      </div>

      <div className="d-flex align-items-center gap-3">
        <div className="d-flex flex-column" style={{ minWidth: '100px' }}>
           <span className="potato-score fw-bold m-0">Avg: {rating || '0'} / 5</span>
           {userRating > 0 && (
             <span className="text-warning small" style={{ fontSize: '0.75rem' }}>Your vote: {userRating}</span>
           )}
        </div>
        
        {userRating > 0 && onDelete && (
          <button 
            className="btn btn-outline-danger btn-sm border-0 p-1 lh-1" 
            onClick={onDelete}
            disabled={isSubmitting}
            title="Remove my rating"
          >
            <i className="bi bi-trash3 fs-6" />
          </button>
        )}

        {onRate && !userRating && !hover && <small className="text-secondary">Rate this movie!</small>}
      </div>
    </div>
  )
}

function CommentsSection({ reviews, user, onPost, onDelete, isPosting, loading }) {
  const [text, setText] = useState('')
  const userReview = user ? reviews.find(r => r.userId === user.id) : null

  // Pre-fill text when userReview is found or changed
  useEffect(() => {
    if (userReview) {
      setText(userReview.rating)
    } else {
      setText('')
    }
  }, [userReview])
  
  const handleSubmit = (e) => {
    e.preventDefault()
    if (!text.trim()) return
    onPost(text)
  }

  return (
    <div className="movie-section">
      <h2 className="movie-section-title">
        Reviews <span>({reviews.length})</span>
      </h2>

      {user && (
        <form onSubmit={handleSubmit} className="comment-form mb-5">
          <textarea
            className="form-control comment-input mb-2"
            rows={3}
            placeholder={userReview ? "Update your review..." : "What did you think of the movie?"}
            value={text}
            onChange={(e) => setText(e.target.value)}
            disabled={isPosting}
          />
          <div className="d-flex gap-2">
            <button 
              type="submit" 
              className="btn-fresh-primary px-4 py-2"
              disabled={isPosting || !text.trim() || (userReview && text === userReview.rating)}
            >
              {isPosting ? 'Saving...' : userReview ? 'Update Review' : 'Post Review'}
            </button>
            {userReview && (
              <button 
                type="button" 
                className="btn-fresh-danger px-4 py-2"
                onClick={onDelete}
                disabled={isPosting}
              >
                Delete Review
              </button>
            )}
          </div>
        </form>
      )}

      {!user && (
        <div className="alert alert-dark border-secondary text-secondary small mb-5">
          Please log in to leave a review.
        </div>
      )}

      {loading ? (
        <div className="text-center py-4">
          <div className="spinner-border text-warning spinner-border-sm" />
        </div>
      ) : (
        <div className="reviews-list">
          {reviews.length === 0 ? (
            <p className="text-secondary italic">No reviews yet. Be the first to share your thoughts!</p>
          ) : (
            reviews.map((rev, idx) => (
              <div key={idx} className="review-item mb-4 pb-4 border-bottom border-secondary border-opacity-25">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <div className="d-flex align-items-center gap-2">
                    <div className="bg-secondary rounded-circle d-flex align-items-center justify-content-center" style={{ width: '32px', height: '32px' }}>
                      <i className="bi bi-person-fill text-dark" />
                    </div>
                    {/* Robust ID extraction for user profile link */}
                    <Link 
                      to={`/user/${rev.userId ?? rev.userid ?? rev.user_id ?? rev.user?.id ?? 0}`} 
                      className="fw-bold text-light text-decoration-none hover-warning"
                    >
                      {rev.username}
                    </Link>
                    {user && (rev.userId === user.id || rev.userid === user.id) && (
                      <span className="badge bg-warning text-dark smaller">You</span>
                    )}
                  </div>
                </div>
                <p className="comment-text">{rev.rating}</p>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  )
}

//main page
function MoviePage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [movie, setMovie] = useState(null)
  const [reviews, setReviews] = useState([])
  const [playlists, setPlaylists] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingReviews, setLoadingReviews] = useState(false)
  const [error, setError] = useState(null)
  const [userRating, setUserRating] = useState(0)
  const [isSubmittingRating, setIsSubmittingRating] = useState(false)
  const [isPostingReview, setIsPostingReview] = useState(false)
  const [isAddingToPlaylist, setIsAddingToPlaylist] = useState(false)
  const [showTrailer, setShowTrailer] = useState(false)
  const [showCreateModal, setShowCreateModal] = useState(false)

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

  useEffect(() => {
    if (id) {
      setLoadingReviews(true)
      reviewApi.getAllByMovie(id)
        .then(data => setReviews(data))
        .finally(() => setLoadingReviews(false))
    }
  }, [id])

  useEffect(() => {
    if (user && id) {
      movieApi.getUserRating(user.id, id)
        .then(val => setUserRating(val))
        .catch(() => setUserRating(0))
    } else {
      setUserRating(0)
    }
  }, [user, id])

  useEffect(() => {
    if (user) {
      const token = localStorage.getItem('accessToken')
      playlistApi.getByOwner(user.id, token)
        .then(data => setPlaylists(data))
        .catch(() => setPlaylists([]))
    } else {
      setPlaylists([])
    }
  }, [user])

  const handleRate = async (val) => {
    if (!user) {
      alert("Please log in to rate movies!")
      return
    }
    
    setIsSubmittingRating(true)
    try {
      const token = localStorage.getItem('accessToken')
      await movieApi.rate(id, val, token)
      setUserRating(val)
      // Refresh movie data to see updated average
      const updatedMovie = await movieApi.getById(id)
      setMovie(updatedMovie)
    } catch (err) {
      alert(err.message || "Failed to save rating")
    } finally {
      setIsSubmittingRating(false)
    }
  }

  const handlePostReview = async (text) => {
    if (!user) {
      alert("Please log in to post reviews!")
      return
    }
    
    setIsPostingReview(true)
    try {
      const token = localStorage.getItem('accessToken')
      await reviewApi.postReview(id, text, token)
      // Refresh reviews
      const updatedReviews = await reviewApi.getAllByMovie(id)
      setReviews(updatedReviews)
    } catch (err) {
      alert(err.message || "Failed to post review")
    } finally {
      setIsPostingReview(false)
    }
  }

  const handleDeleteReview = async () => {
    if (!user) return
    if (!window.confirm("Are you sure you want to delete your review?")) return

    setIsPostingReview(true)
    try {
      const token = localStorage.getItem('accessToken')
      await reviewApi.deleteReview(id, token)
      // Refresh reviews
      const updatedReviews = await reviewApi.getAllByMovie(id)
      setReviews(updatedReviews)
    } catch (err) {
      alert(err.message || "Failed to delete review")
    } finally {
      setIsPostingReview(false)
    }
  }

  const handleDeleteRate = async () => {
    if (!user) return
    
    setIsSubmittingRating(true)
    try {
      const token = localStorage.getItem('accessToken')
      await movieApi.deleteRate(id, token)
      setUserRating(0)
      // Refresh movie data to see updated average
      const updatedMovie = await movieApi.getById(id)
      setMovie(updatedMovie)
    } catch (err) {
      alert(err.message || "Failed to delete rating")
    } finally {
      setIsSubmittingRating(false)
    }
  }

  const handleAddToPlaylist = async (playlistId) => {
    if (!user) return
    setIsAddingToPlaylist(true)
    try {
      const token = localStorage.getItem('accessToken')
      await playlistApi.addMovie(playlistId, id, token)
      alert("Movie added to playlist!")
    } catch (err) {
      alert(err.message || "Failed to add movie")
    } finally {
      setIsAddingToPlaylist(false)
    }
  }

  const handleCreatePlaylistSuccess = async (newPlaylist) => {
    // 1. Refresh playlist list
    const token = localStorage.getItem('accessToken')
    try {
      const updatedList = await playlistApi.getByOwner(user.id, token)
      setPlaylists(updatedList)
      
      // 2. Automatically add current movie to the new playlist
      if (newPlaylist && newPlaylist.id) {
        await handleAddToPlaylist(newPlaylist.id)
      }
    } catch (err) {
      console.error(err)
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

  const isPlaceholderDescription = !movie.description || movie.description === movie.title || movie.description === 'Not Fetched';

  return (
    <div className="movie-page">

      {/* hero*/}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* poster */}
        <div className="movie-poster-frame">
          <img 
            src={movie.posterUrl || testBg} 
            alt="" 
            aria-hidden 
            className={`movie-poster-glow ${!movie.posterUrl ? 'opacity-50' : ''}`} 
          />
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
            {movie.duration > 0 && (
              <span><i className="bi bi-clock" /> {Math.floor(movie.duration / 60)}h {movie.duration % 60}m</span>
            )}
            {movie.country && (
              <span><i className="bi bi-globe2" /> {movie.country}</span>
            )}
          </div>

          {/* potato rating */}
          <PotatoRating 
            rating={movie.rating} 
            userRating={userRating}
            onRate={user ? handleRate : null}
            onDelete={user ? handleDeleteRate : null}
            isSubmitting={isSubmittingRating}
          />

          <div className="movie-links d-flex align-items-center gap-3 mt-4">
            {movie.trailerUrl && (
              <button 
                className="btn-fresh-primary"
                onClick={() => setShowTrailer(true)}
              >
                <i className="bi bi-play-fill me-2 fs-5" /> Watch Trailer
              </button>
            )}

            {user && (
              <div className="dropdown">
                <button 
                  className="btn-fresh-secondary dropdown-toggle" 
                  type="button" 
                  data-bs-toggle="dropdown"
                  disabled={isAddingToPlaylist}
                >
                  <i className="bi bi-plus-lg me-2" /> Add to Playlist
                </button>
                <ul className="dropdown-menu shadow-lg">
                  {playlists.length === 0 ? (
                    <li className="px-3 py-2 text-secondary smaller italic">No playlists yet</li>
                  ) : (
                    playlists.map(pl => (
                      <li key={pl.id}>
                        <button className="dropdown-item" onClick={() => handleAddToPlaylist(pl.id)}>
                          <i className={`bi bi-${pl.isPrivate ? 'lock-fill' : 'collection-play-fill'}`} />
                          {pl.name}
                        </button>
                      </li>
                    ))
                  )}
                  <li><hr className="dropdown-divider border-opacity-25" /></li>
                  <li>
                    <button className="dropdown-item text-warning" onClick={() => setShowCreateModal(true)}>
                       <i className="bi bi-plus-circle-fill" /> Create New Playlist
                    </button>
                  </li>
                  <li>
                    <Link className="dropdown-item text-secondary smaller" to="/profile">
                       <i className="bi bi-gear-fill" /> Manage All Playlists
                    </Link>
                  </li>
                </ul>
              </div>
            )}
          </div>

          {/* credits */}
          <div className="movie-credits">
            {movie.directors && movie.directors.length > 0 && (
              <div className="movie-credit-group">
                <h6>Directed by</h6>
                <div className="d-flex flex-wrap gap-2">
                  {movie.directors.map((d, index) => (
                    <span key={d.id}>
                      <Link to={`/staff/${d.id}`} className="text-warning text-decoration-none hover-underline">
                        {d.name}
                      </Link>
                      {index < movie.directors.length - 1 && <span className="text-secondary">,</span>}
                    </span>
                  ))}
                </div>
              </div>
            )}
            {movie.actors && movie.actors.length > 0 && (
              <div className="movie-credit-group">
                <h6>Starring</h6>
                <div className="d-flex flex-wrap gap-2">
                  {movie.actors.map((a, index) => (
                    <span key={a.id}>
                      <Link to={`/staff/${a.id}`} className="text-warning text-decoration-none hover-underline">
                        {a.name}
                      </Link>
                      {index < movie.actors.length - 1 && <span className="text-secondary">,</span>}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* description */}
          {!isPlaceholderDescription && (
            <p className="movie-description">{movie.description}</p>
          )}

        </div>
      </div>

      {/* recom */}
      <DiscoverMovies />

      {/*comments*/}
      <CommentsSection 
        reviews={reviews}
        user={user}
        onPost={handlePostReview}
        onDelete={handleDeleteReview}
        isPosting={isPostingReview}
        loading={loadingReviews}
      />

      <TrailerModal 
        show={showTrailer} 
        onHide={() => setShowTrailer(false)} 
        videoId={movie.trailerUrl} 
        title={movie.title} 
      />

      <CreatePlaylistModal 
        show={showCreateModal}
        onHide={() => setShowCreateModal(false)}
        onSuccess={handleCreatePlaylistSuccess}
      />

    </div>
  )
}

export default MoviePage