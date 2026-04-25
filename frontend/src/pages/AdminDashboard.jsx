import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { movieApi, genreApi } from '../services/api'
import MoviePoster from '../components/MoviePoster'
import ModifyMovieModal from '../components/ModifyMovieModal'
import ConfirmModal from '../components/ConfirmModal'
import testBg from '../assets/test_bg.jpg'

function AdminDashboard() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  
  // Genres state
  const [availableGenres, setAvailableGenres] = useState([])
  
  // Movies state
  const [movies, setMovies] = useState([])
  const [moviePage, setMoviePage] = useState(0)
  const [loadingMovies, setLoadingMovies] = useState(false)
  
  // Modify/Delete state
  const [selectedEditMovie, setSelectedEditMovie] = useState(null)
  const [showModifyModal, setShowModifyModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [movieToDelete, setMovieToDelete] = useState(null)
  
  // Advanced Search state
  const [searchParams, setSearchParams] = useState({
    title: '',
    genre: '',
    staff: ''
  })

  useEffect(() => {
    if (!user || !user.isAdmin) {
      navigate('/')
      return
    }

    // Fetch genres for the picker
    genreApi.getAll()
      .then(data => {
        const sorted = [...data].sort((a, b) => a.name.localeCompare(b.name));
        setAvailableGenres(sorted);
      })
      .catch(err => console.error("Failed to fetch genres:", err))
  }, [user, navigate])

  useEffect(() => {
    fetchMovies(0)
  }, [])

  const fetchMovies = async (page) => {
    setLoadingMovies(true)
    try {
      let data;
      const { title, genre, staff } = searchParams;
      
      if (title.trim() || genre.trim() || staff.trim()) {
        data = await movieApi.advancedSearch({ 
            title: title.trim(), 
            genre: genre.trim(), 
            staff: staff.trim(), 
            page, 
            size: 20 
        })
      } else {
        data = await movieApi.getMovies(page, 20)
      }
      
      setMovies(data)
      setMoviePage(page)
    } catch (err) {
      console.error(err)
      setMovies([])
    } finally {
      setLoadingMovies(false)
    }
  }

  const handleSearchChange = (e) => {
    setSearchParams({ ...searchParams, [e.target.name]: e.target.value })
  }

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    fetchMovies(0)
  }

  const handleResetSearch = () => {
    const empty = { title: '', genre: '', staff: '' }
    setSearchParams(empty)
    fetchMovies(0)
  }

  const handleDeleteClick = (movieId) => {
    setMovieToDelete(movieId)
    setShowDeleteModal(true)
  }

  const handleConfirmDelete = async () => {
    if (!movieToDelete) return
    const token = localStorage.getItem('accessToken')
    try {
      await movieApi.adminDeleteMovie(movieToDelete, token)
      setMovies(movies.filter(m => m.id !== movieToDelete))
      showToast("Movie deleted successfully.")
    } catch (err) {
      showToast(err.message, "error")
    } finally {
      setMovieToDelete(null)
    }
  }

  const handleModifyClick = (movie) => {
    setSelectedEditMovie(movie)
    setShowModifyModal(true)
  }

  const handleModifySuccess = () => {
    fetchMovies(moviePage)
    showToast("Movie updated successfully.")
  }

  if (!user || !user.isAdmin) return null

  return (
    <div className="movie-page">
      <div className="movie-hero" style={{ minHeight: '350px', padding: '8rem 5% 3rem', overflow: 'hidden' }}>
        <div className="movie-hero-bg" />

        <div className="movie-info position-relative">
          {/* subtle colorful glow behind text */}
          <img 
            src={testBg} 
            alt="" 
            aria-hidden 
            className="position-absolute start-0 top-50 translate-middle-y opacity-25" 
            style={{ width: '600px', height: '400px', filter: 'blur(100px)', zIndex: -1, pointerEvents: 'none' }} 
          />

          <div className="movie-genres">
             <span className="movie-genre-badge bg-danger text-white border-0">Administrator</span>
          </div>
          <h1 className="movie-title">Control Center</h1>
          <p className="text-secondary fs-5">Manage movies across the platform.</p>
        </div>
      </div>

      <div className="container pb-5">
         <div className="animate-fade-in mt-4">
              {/* Advanced Search Form */}
              <div className="admin-panel-modern">
                 <h5>Filter Database</h5>
                 <form onSubmit={handleSearchSubmit} className="row g-3">
                    <div className="col-md-4">
                       <label className="text-secondary smallest uppercase fw-bold mb-2">Movie Title</label>
                       <input 
                         type="text" 
                         name="title"
                         className="form-control bg-transparent text-light border-secondary" 
                         placeholder="e.g. Interstellar"
                         value={searchParams.title}
                         onChange={handleSearchChange}
                       />
                    </div>
                    <div className="col-md-3">
                       <label className="text-secondary smallest uppercase fw-bold mb-2">Genre</label>
                       <select 
                         name="genre"
                         className="custom-select-minimal"
                         value={searchParams.genre}
                         onChange={handleSearchChange}
                       >
                          <option value="">All Genres</option>
                          {availableGenres.map(g => (
                            <option key={g.id || g.name} value={g.name}>{g.name}</option>
                          ))}
                       </select>
                    </div>
                    <div className="col-md-3">
                       <label className="text-secondary smallest uppercase fw-bold mb-2">Staff Name</label>
                       <input 
                         type="text" 
                         name="staff"
                         className="form-control bg-transparent text-light border-secondary" 
                         placeholder="e.g. Nolan"
                         value={searchParams.staff}
                         onChange={handleSearchChange}
                       />
                    </div>
                    <div className="col-md-2 d-flex align-items-end gap-2">
                       <button type="submit" className="btn-fresh-primary w-100" style={{ height: '48px' }}>Search</button>
                       <button type="button" className="btn-fresh-secondary" style={{ height: '48px', width: '48px', padding: 0 }} onClick={handleResetSearch} title="Reset Filters">
                          <i className="bi bi-arrow-counterclockwise" />
                       </button>
                    </div>
                 </form>
              </div>

              <div className="d-flex justify-content-between align-items-center mb-4">
                 <h3 className="text-light fw-bold m-0">All Movies</h3>
                 <div className="d-flex gap-2">
                    <button className="btn-fresh-secondary btn-sm" onClick={() => fetchMovies(moviePage - 1)} disabled={moviePage === 0 || loadingMovies}>Prev</button>
                    <span className="text-secondary align-self-center">
                       {loadingMovies ? <span className="spinner-border spinner-border-sm me-2" /> : `Page ${moviePage + 1}`}
                    </span>
                    <button className="btn-fresh-secondary btn-sm" onClick={() => fetchMovies(moviePage + 1)} disabled={loadingMovies}>Next</button>
                 </div>
              </div>

              <div className="table-responsive bg-dark rounded border border-secondary border-opacity-25 p-3">
                 <table className="table table-dark table-hover m-0 align-middle">
                    <thead>
                       <tr className="text-secondary smaller uppercase tracking-widest">
                          <th>Movie</th>
                          <th>ID</th>
                          <th>Year</th>
                          <th className="text-end">Actions</th>
                       </tr>
                    </thead>
                    <tbody>
                       {movies.map(movie => (
                         <tr key={movie.id}>
                            <td>
                               <div className="d-flex align-items-center gap-3">
                                  <div className="d-flex align-items-center justify-content-center bg-dark rounded border border-secondary border-opacity-25" style={{ width: '40px', height: '60px', overflow: 'hidden' }}>
                                     {movie.posterUrl && !movie.posterUrl.includes('Not%20Fetched') && !movie.posterUrl.includes('Not Fetched') ? (
                                        <img src={movie.posterUrl} alt="" className="img-fluid" style={{ objectFit: 'cover', height: '100%' }} />
                                     ) : (
                                        <i className="bi bi-film text-warning opacity-75 fs-5" />
                                     )}
                                  </div>
                                  <Link to={`/movie/${movie.id}`} className="text-light fw-bold text-decoration-none hover-warning">
                                     {movie.title}
                                  </Link>
                               </div>
                            </td>
                            <td className="text-secondary smaller font-monospace">{movie.id}</td>
                            <td className="text-secondary">{movie.year}</td>
                            <td className="text-end">
                               <div className="d-flex justify-content-end gap-2">
                                  <button 
                                    className="btn-fresh-secondary p-2" 
                                    style={{ width: '36px', height: '36px', borderRadius: '50%' }}
                                    onClick={() => handleModifyClick(movie)}
                                    title="Modify Movie"
                                  >
                                     <i className="bi bi-pencil-square" />
                                  </button>
                                  <button 
                                    className="btn-fresh-danger p-2" 
                                    style={{ width: '36px', height: '36px', borderRadius: '50%' }}
                                    onClick={() => handleDeleteClick(movie.id)}
                                  >
                                     <i className="bi bi-trash3" />
                                  </button>
                               </div>
                            </td>
                         </tr>
                       ))}
                    </tbody>
                 </table>
              </div>
           </div>
      </div>

      <ModifyMovieModal 
        show={showModifyModal}
        onHide={() => setShowModifyModal(false)}
        movie={selectedEditMovie}
        onSuccess={handleModifySuccess}
      />

      <ConfirmModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmDelete}
        title="Delete Movie"
        message="Are you sure you want to delete this movie? This action is permanent and cannot be undone."
        confirmText="Delete"
        isDanger={true}
      />

    </div>
  )
}

export default AdminDashboard