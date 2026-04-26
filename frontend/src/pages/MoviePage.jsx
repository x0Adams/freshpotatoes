import { useParams, Link, useNavigate } from 'react-router-dom'
import { useState, useEffect, useRef } from 'react'
import { movieApi, reviewApi, playlistApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import MoviePoster from '../components/MoviePoster'
import DiscoverMovies from '../components/DiscoverMovies'
import TrailerModal from '../components/TrailerModal'
import CreatePlaylistModal from '../components/CreatePlaylistModal'
import ModifyMovieModal from '../components/ModifyMovieModal'
import ConfirmModal from '../components/ConfirmModal'
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
            className="btn-fresh-danger p-0"
            style={{ width: '32px', height: '32px', borderRadius: '50%' }}
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

function CommentsSection({ reviews, user, onPost, onDelete, onAdminDelete, isPosting, loading }) {
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

                  {user?.isAdmin && (
                     <button 
                       className="btn-fresh-danger p-0"
                       style={{ width: '32px', height: '32px', borderRadius: '50%' }}
                       onClick={() => onAdminDelete(rev.userId || rev.userid, rev.username)}
                       title="Admin: Delete Review"
                     >
                        <i className="bi bi-shield-x" />
                     </button>
                  )}
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
  const { showToast } = useToast()
  const navigate = useNavigate()
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
  const [showModifyModal, setShowModifyModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [showReviewDeleteModal, setShowReviewDeleteModal] = useState(false)
  const [reviewDeletionInfo, setReviewDeletionInfo] = useState(null)
  const [showAllActors, setShowAllActors] = useState(false)
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const dropdownRef = useRef(null)

  useEffect(() => {
    function handleClick(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

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
      showToast("Please log in to rate movies!", "error")
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
      showToast("Rating saved!")
    } catch (err) {
      showToast(err.message || "Failed to save rating", "error")
    } finally {
      setIsSubmittingRating(false)
    }
  }

  const handlePostReview = async (text) => {
    if (!user) {
      showToast("Please log in to post reviews!", "error")
      return
    }
    
    setIsPostingReview(true)
    try {
      const token = localStorage.getItem('accessToken')
      await reviewApi.postReview(id, text, token)
      // Refresh reviews
      const updatedReviews = await reviewApi.getAllByMovie(id)
      setReviews(updatedReviews)
      showToast("Review posted successfully!")
    } catch (err) {
      showToast(err.message || "Failed to post review", "error")
    } finally {
      setIsPostingReview(false)
    }
  }

  const handleDeleteReview = () => {
    if (!user) return
    setReviewDeletionInfo({ type: 'self' })
    setShowReviewDeleteModal(true)
  }

  const handleAdminDeleteReview = (userId, username) => {
    if (!user?.isAdmin) return
    setReviewDeletionInfo({ type: 'admin', userId, username })
    setShowReviewDeleteModal(true)
  }

  const handleConfirmReviewDelete = async () => {
    if (!reviewDeletionInfo) return

    setIsPostingReview(true)
    try {
      const token = localStorage.getItem('accessToken')
      if (reviewDeletionInfo.type === 'self') {
        await reviewApi.deleteReview(id, token)
        showToast("Review deleted")
      } else {
        await reviewApi.adminDeleteReview(reviewDeletionInfo.userId, id, token)
        showToast(`Deleted ${reviewDeletionInfo.username}'s review`)
      }
      // Refresh reviews
      const updatedReviews = await reviewApi.getAllByMovie(id)
      setReviews(updatedReviews)
    } catch (err) {
      showToast(err.message || "Failed to delete review", "error")
    } finally {
      setIsPostingReview(false)
      setReviewDeletionInfo(null)
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
      showToast("Rating removed")
    } catch (err) {
      showToast(err.message || "Failed to delete rating", "error")
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
      showToast("Movie added to playlist!")
    } catch (err) {
      showToast(err.message || "Failed to add movie", "error")
    } finally {
      setIsAddingToPlaylist(false)
    }
  }

  const handleAdminDeleteMovie = () => {
    if (!user?.isAdmin) return
    setShowDeleteModal(true)
  }

  const handleConfirmDelete = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      await movieApi.adminDeleteMovie(id, token)
      showToast("Movie deleted successfully.")
      navigate('/')
    } catch (err) {
      showToast(err.message || "Failed to delete movie as admin", "error")
    }
  }

  const handleModifySuccess = async () => {
    try {
      const updatedMovie = await movieApi.getById(id)
      setMovie(updatedMovie)
      showToast("Movie updated successfully.")
    } catch (err) {
      console.error(err)
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

  const handlePickDifferent = async () => {
    try {
      const randoms = await movieApi.getRandomMovies();
      if (randoms.length > 0) {
        navigate(`/movie/${randoms[0].id}`);
      } else {
        navigate('/');
      }
    } catch {
      navigate('/');
    }
  }

  if (error || !movie) return (
    <div className="movie-page">
       <div className="movie-hero" style={{ minHeight: '60vh' }}>
          <div className="movie-hero-bg" />
          <div className="container text-center animate-fade-in" style={{ zIndex: 1 }}>
             <i className="bi bi-exclamation-octagon text-danger mb-4" style={{ fontSize: '5rem' }} />
             <h2 className="text-light fw-black uppercase tracking-widest mb-3">Cinematic Glitch</h2>
             <p className="text-secondary fs-5 mb-5 mx-auto" style={{ maxWidth: '600px' }}>
                {error || "We encountered a mysterious issue while loading this movie. It might be lost in our cinematic archives."}
             </p>
             <div className="d-flex justify-content-center gap-3">
                <Link to="/" className="btn-fresh-secondary">← Back to Home</Link>
                <button onClick={handlePickDifferent} className="btn-fresh-primary">
                   Try a Different Surprise <i className="bi bi-dice-5 ms-2" />
                </button>
             </div>
          </div>
       </div>
    </div>
  )

  return (
    <div className="movie-page">

      {/* hero*/}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* poster */}
        <div className="movie-poster-frame animate-hero-reveal">
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
        <div className="movie-info animate-hero-reveal" style={{ animationDelay: '0.2s' }}>

          <div className="movie-genres">
            {movie.genres && movie.genres.map(g => (
              <Link 
                key={g} 
                to={`/genre/${encodeURIComponent(g)}`} 
                className="movie-genre-badge clickable text-decoration-none"
              >
                {g}
              </Link>
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

            {movie.youtubeMovie && (
              <a 
                href={movie.youtubeMovie.startsWith('http') ? movie.youtubeMovie : `https://www.youtube.com/watch?v=${movie.youtubeMovie}`}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-fresh-secondary text-decoration-none"
              >
                <i className="bi bi-youtube me-2 fs-5" /> Watch on YouTube
              </a>
            )}

            {user && (
              <div className="dropdown" ref={dropdownRef}>
                <button
                  className={`btn-fresh-secondary ${isDropdownOpen ? 'show' : ''}`}
                  type="button"
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  aria-expanded={isDropdownOpen}
                  disabled={isAddingToPlaylist}
                >
                  <i className="bi bi-plus-lg me-2" /> Add to Playlist
                </button>
                <ul className={`dropdown-menu playlist-dropdown shadow-lg ${isDropdownOpen ? 'show' : ''}`}>
                  {playlists.length === 0 ? (
                    <li className="px-3 py-2 text-secondary smaller italic">No playlists yet</li>
                  ) : (
                    playlists.map(pl => (
                      <li key={pl.id}>
                        <button className="dropdown-item" onClick={() => { handleAddToPlaylist(pl.id); setIsDropdownOpen(false); }}>
                          <i className={`bi bi-${pl.isPrivate ? 'lock-fill' : 'collection-play-fill'}`} />
                          <span className="playlist-name-truncate">{pl.name}</span>
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

            {user?.isAdmin && (
              <div className="d-flex gap-2">
                <button 
                  className="btn-fresh-secondary"
                  onClick={() => setShowModifyModal(true)}
                >
                  <i className="bi bi-pencil-square me-2" /> Modify Movie
                </button>
                <button 
                  className="btn-fresh-danger"
                  onClick={handleAdminDeleteMovie}
                >
                  <i className="bi bi-shield-lock-fill me-2" /> Delete Movie
                </button>
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
                  {(showAllActors ? movie.actors : movie.actors.slice(0, 10)).map((a, index) => (
                    <span key={a.id}>
                      <Link to={`/staff/${a.id}`} className="text-warning text-decoration-none hover-underline">
                        {a.name}
                      </Link>
                      {index < (showAllActors ? movie.actors.length : Math.min(movie.actors.length, 10)) - 1 && <span className="text-secondary">,</span>}
                    </span>
                  ))}
                  {movie.actors.length > 10 && (
                    <button 
                      className="toggle-credits-btn" 
                      onClick={() => setShowAllActors(!showAllActors)}
                    >
                      {showAllActors ? 'Show Less' : `+${movie.actors.length - 10} More`}
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
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
        onAdminDelete={handleAdminDeleteReview}
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

      <ModifyMovieModal
        show={showModifyModal}
        onHide={() => setShowModifyModal(false)}
        movie={movie}
        onSuccess={handleModifySuccess}
      />

      <ConfirmModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmDelete}
        title="Delete Movie"
        message={`Are you sure you want to delete "${movie?.title}"? This action is permanent and cannot be undone.`}
        confirmText="Delete"
        isDanger={true}
      />

      <ConfirmModal
        show={showReviewDeleteModal}
        onHide={() => setShowReviewDeleteModal(false)}
        onConfirm={handleConfirmReviewDelete}
        title={reviewDeletionInfo?.type === 'admin' ? "Admin: Delete Review" : "Delete Review"}
        message={reviewDeletionInfo?.type === 'admin' 
          ? `Are you sure you want to delete ${reviewDeletionInfo.username}'s review? This action is permanent.`
          : "Are you sure you want to delete your review? This action cannot be undone."
        }
        confirmText="Delete"
        isDanger={true}
      />

    </div>
  )
}

export default MoviePage