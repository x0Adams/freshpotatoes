import { useParams, Link, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { authApi, movieApi, reviewApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import PlaylistSection from '../components/PlaylistSection'
import MovieTrack from '../components/MovieTrack'
import testBg from '../assets/test_bg.jpg'

function PublicProfile() {
  const { id } = useParams()
  const { user: currentUser } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [ratedMovies, setRatedMovies] = useState([])
  const [reviewedMovies, setReviewedMovies] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    // If viewing own profile via ID, just redirect to /profile for the full private experience
    if (currentUser && Number(id) === currentUser.id) {
      navigate('/profile')
      return
    }

    setLoading(true)
    setError(null)
    
    // 1. Fetch Basic Profile
    authApi.getUserPublicById(id)
      .then(userData => {
        setProfile(userData)
        setLoading(false)
        
        // 2. Fetch Interactions in background (don't block the UI)
        movieApi.getRatingsByUser(id)
          .then(data => setRatedMovies(data))
          .catch(err => console.warn("Could not fetch public ratings:", err))

        reviewApi.getReviewsByUser(id)
          .then(data => setReviewedMovies(data))
          .catch(err => console.warn("Could not fetch public reviews:", err))
      })
      .catch(err => {
        console.error("Public profile fetch error:", err)
        setError(err.message || "User not found.")
        setLoading(false)
      })
  }, [id, currentUser, navigate])

  if (loading) return (
    <div className="text-center py-5 mt-5">
      <div className="spinner-border text-warning" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
    </div>
  )

  if (error || !profile) return (
    <div className="movie-page d-flex align-items-center justify-content-center">
      <div className="text-center py-5">
        <div className="mb-4 opacity-25" style={{ fontSize: '5rem' }}>
          <i className="bi bi-person-x" />
        </div>
        <h3 className="text-light fw-bold">{error || "User not found"}</h3>
        <p className="text-secondary mb-4">This profile may be private or does not exist.</p>
        <Link to="/" className="btn-fresh-secondary">
          <i className="bi bi-arrow-left me-2" /> Back to Discovery
        </Link>
      </div>
    </div>
  )

  return (
    <div className="movie-page">
      {/* profile hero */}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* profile picture frame */}
        <div className="movie-poster-frame">
          <img src={testBg} alt="" aria-hidden className="movie-poster-glow opacity-50" />
          <div className="movie-poster-img bg-dark d-flex align-items-center justify-content-center border border-secondary" style={{ height: '100%', borderRadius: '12px' }}>
            <i className="bi bi-person-fill text-warning" style={{ fontSize: '8rem' }}></i>
          </div>
        </div>

        {/* info */}
        <div className="movie-info">
          <div className="movie-genres">
            <span className="movie-genre-badge">User Profile</span>
            <span className="movie-genre-badge bg-warning text-dark">Member</span>
          </div>

          <h1 className="movie-title mb-4">{profile.username}</h1>

          <div className="movie-meta-row profile-meta-list gap-3">
            <span className="fs-5">
              <i className="bi bi-calendar-event text-warning me-3"></i>
              Age: {profile.age || 'Unknown'}
            </span>
            <span className="fs-5">
              <i className="bi bi-gender-ambiguous text-warning me-3"></i>
              Gender: {profile.genderName || 'Unknown'}
            </span>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        {/* Interaction Tracks */}
        {ratedMovies.length > 0 && (
          <div className="mb-5">
             <MovieTrack title={`Highly Rated by ${profile.username}`} movies={ratedMovies} hideTopBorder={true} />
          </div>
        )}

        {reviewedMovies.length > 0 && (
          <div className="mb-5">
             <MovieTrack title={`Reviewed by ${profile.username}`} movies={reviewedMovies} hideTopBorder={true} />
          </div>
        )}

        {/* Public Playlists */}
        <PlaylistSection 
          user={profile} 
          initialPlaylists={profile.playlists} 
          readOnly={true} 
        />
      </div>
    </div>
  )
}

export default PublicProfile;