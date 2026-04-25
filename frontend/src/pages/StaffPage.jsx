import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { staffApi } from '../services/api'
import MoviePoster from '../components/MoviePoster'
import testBg from '../assets/test_bg.jpg'

function StaffPage() {
  const { id } = useParams()
  const [staff, setStaff] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false;
    setLoading(true)
    setError(null)
    staffApi.getById(id)
      .then(data => {
        if (!cancelled) {
          setStaff(data)
          setLoading(false)
        }
      })
      .catch(err => {
        console.error(err)
        if (!cancelled) {
          setError("Staff member not found or server error.")
          setLoading(false)
        }
      })
    return () => { cancelled = true; }
  }, [id])

  if (loading) return (
    <div className="text-center py-5 mt-5">
      <div className="spinner-border text-warning" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
    </div>
  )

  if (error || !staff) return (
    <div className="text-center py-5 mt-5">
      <p className="text-secondary">{error || "Staff not found."}</p>
      <Link to="/" className="btn-fresh-secondary mt-3">
        <i className="bi bi-arrow-left me-2" /> Back to Home
      </Link>
    </div>
  )

  const actingMovies = (staff.playedMovies || []).map(m => ({ ...m, role: 'Actor' }));
  const directingMovies = (staff.directedMovies || []).map(m => ({ ...m, role: 'Director' }));
  
  // deduplicate for the "All" view but preserve role info
  const allMovies = [...actingMovies, ...directingMovies];
  const uniqueMovies = Array.from(new Map(allMovies.map(m => [m.id, m])).values())
    .sort((a, b) => (b.year || 0) - (a.year || 0));

  const years = uniqueMovies.map(m => parseInt(m.year)).filter(y => !isNaN(y));
  const careerSpan = years.length > 0 ? `${Math.min(...years)} — ${Math.max(...years)}` : null;

  return (
    <div className="movie-page">
      {/* staff hero */}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* avatar frame */}
        <div className="movie-poster-frame">
          <img src={testBg} alt="" aria-hidden className="movie-poster-glow opacity-50" />
          <div className="movie-poster-img bg-dark d-flex align-items-center justify-content-center border border-secondary" style={{ height: '100%', borderRadius: '12px' }}>
            <i className="bi bi-person-fill text-warning" style={{ fontSize: '8rem' }}></i>
          </div>
        </div>

        {/* info */}
        <div className="movie-info">
          <div className="movie-genres">
            <span className="movie-genre-badge">Cast & Crew</span>
            {directingMovies.length > 0 && <span className="movie-genre-badge bg-warning text-dark">Director</span>}
            {actingMovies.length > 0 && <span className="movie-genre-badge bg-warning text-dark">Actor</span>}
          </div>

          <h1 className="movie-title mb-4">{staff.name}</h1>

          <div className="movie-meta-row profile-meta-list gap-3">
            <span className="fs-5">
              <i className="bi bi-gender-ambiguous text-warning me-3"></i>
              {staff.gender || 'Unknown'}
            </span>
            {staff.birthDay && (
              <span className="fs-5">
                <i className="bi bi-calendar3 text-warning me-3"></i>
                Born: {staff.birthDay}
              </span>
            )}
            {careerSpan && (
              <span className="fs-5">
                <i className="bi bi-briefcase text-warning me-3"></i>
                Career: {careerSpan}
              </span>
            )}
            {staff.birthCountry && (
              <span className="fs-5">
                <i className="bi bi-geo-alt text-warning me-3"></i>
                {staff.birthCountry}
              </span>
            )}
          </div>
        </div>
      </div>

      <div className="container pb-5">
        {/* Filmography Section */}
        <div className="movie-section-header d-flex justify-content-between align-items-center mb-5 border-bottom border-secondary border-opacity-25 pb-3">
          <h2 className="text-light fw-bold m-0">Filmography</h2>
          <div className="d-flex gap-3">
             <div className="text-center">
                <div className="text-warning fw-bold fs-4">{uniqueMovies.length}</div>
                <div className="text-secondary smaller uppercase fw-bold tracking-wider">Total Titles</div>
             </div>
          </div>
        </div>

        {directingMovies.length > 0 && (
          <div className="mb-5">
            <h4 className="text-warning mb-4 d-flex align-items-center gap-2">
              <i className="bi bi-megaphone-fill" /> Directing
            </h4>
            <div className="search-page-grid">
              {directingMovies.sort((a,b) => b.year - a.year).map(movie => (
                <StaffMovieCard key={`dir-${movie.id}`} movie={movie} />
              ))}
            </div>
          </div>
        )}

        {actingMovies.length > 0 && (
          <div className="mb-5">
            <h4 className="text-warning mb-4 d-flex align-items-center gap-2">
              <i className="bi bi-person-video2" /> Acting
            </h4>
            <div className="search-page-grid">
              {actingMovies.sort((a,b) => b.year - a.year).map(movie => (
                <StaffMovieCard key={`act-${movie.id}`} movie={movie} />
              ))}
            </div>
          </div>
        )}

        {uniqueMovies.length === 0 && (
          <div className="text-center py-5">
            <i className="bi bi-film text-secondary fs-1 mb-3 opacity-25 d-block"></i>
            <p className="text-secondary italic">No filmography found for this person.</p>
          </div>
        )}
      </div>
    </div>
  )
}

function StaffMovieCard({ movie }) {
  return (
    <Link to={`/movie/${movie.id}`} className="coming-card">
      <MoviePoster
        posterUrl={movie.posterUrl}
        title={movie.title}
        className="coming-card-img"
      />
      <div className="coming-card-overlay">
        <div className="coming-card-genres">
          {movie.genres?.slice(0, 3).map(g => (
            <p key={g} className="coming-card-genre">{g}</p>
          ))}
        </div>
        <p className="coming-card-title">{movie.title}</p>
        <p className="coming-card-date">
          <i className="bi bi-calendar3" />
          {movie.year}
        </p>
      </div>
    </Link>
  )
}

export default StaffPage