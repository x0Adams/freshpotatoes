import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { staffApi } from '../services/api'

function StaffPage() {
  const { id } = useParams()
  const [staff, setStaff] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    staffApi.getById(id)
      .then(data => {
        setStaff(data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError("Staff member not found or server error.")
        setLoading(false)
      })
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
      <Link to="/" className="btn btn-outline-warning btn-sm mt-3">← Back to Home</Link>
    </div>
  )

  return (
    <div className="container" style={{ minHeight: '100vh', paddingTop: '120px' }}>
      <div className="row">
        <div className="col-md-4 text-center mb-4">
          <div className="bg-secondary rounded-circle d-flex align-items-center justify-content-center mx-auto mb-4" style={{ width: '200px', height: '200px', fontSize: '6rem' }}>
            <i className="bi bi-person-fill text-dark"></i>
          </div>
          <h1 className="text-warning fw-bold">{staff.name}</h1>
          <div className="text-light opacity-75">
            <p className="mb-1"><i className="bi bi-gender-ambiguous me-2"></i>{staff.gender || 'Unknown'}</p>
            {staff.birthDay && <p className="mb-1"><i className="bi bi-calendar3 me-2"></i>Born: {staff.birthDay}</p>}
            {staff.birthCountry && <p className="mb-1"><i className="bi bi-geo-alt me-2"></i>{staff.birthCountry}</p>}
          </div>
        </div>
        
        <div className="col-md-8">
          {staff.playedMovies && staff.playedMovies.length > 0 && (
            <div className="mb-5">
              <h3 className="text-light border-bottom border-secondary pb-2 mb-4">Actor</h3>
              <div className="row g-3">
                {staff.playedMovies.map(movie => (
                  <div key={movie.id} className="col-6 col-sm-4 col-lg-3">
                    <Link to={`/movie/${movie.id}`} className="text-decoration-none">
                      <div className="card bg-dark border-secondary h-100 coming-card">
                        <img src={movie.posterUrl || '/favicon.png'} className="card-img-top" alt={movie.title} />
                        <div className="card-body p-2">
                          <p className="card-text text-light small fw-bold mb-0 text-truncate">{movie.title}</p>
                          <p className="card-text text-secondary smaller mb-0">{movie.year}</p>
                        </div>
                      </div>
                    </Link>
                  </div>
                ))}
              </div>
            </div>
          )}

          {staff.directedMovies && staff.directedMovies.length > 0 && (
            <div>
              <h3 className="text-light border-bottom border-secondary pb-2 mb-4">Director</h3>
              <div className="row g-3">
                {staff.directedMovies.map(movie => (
                  <div key={movie.id} className="col-6 col-sm-4 col-lg-3">
                    <Link to={`/movie/${movie.id}`} className="text-decoration-none">
                      <div className="card bg-dark border-secondary h-100 coming-card">
                        <img src={movie.posterUrl || '/favicon.png'} className="card-img-top" alt={movie.title} />
                        <div className="card-body p-2">
                          <p className="card-text text-light small fw-bold mb-0 text-truncate">{movie.title}</p>
                          <p className="card-text text-secondary smaller mb-0">{movie.year}</p>
                        </div>
                      </div>
                    </Link>
                  </div>
                ))}
              </div>
            </div>
          )}
          
          {!staff.playedMovies?.length && !staff.directedMovies?.length && (
            <p className="text-secondary italic">No filmography found for this person.</p>
          )}
        </div>
      </div>
    </div>
  )
}

export default StaffPage