import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { movieApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import AuthModal from './AuthModal'

function Navbar() {
  const { user, logout } = useAuth()
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  
  const navigate = useNavigate()
  const location = useLocation()
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [open, setOpen] = useState(false)
  const wrapperRef = useRef(null)

  // 1. Handle scroll effect
  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 0)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // 2. Sync State FROM URL (Initial load and Back/Forward)
  useEffect(() => {
    const params = new URLSearchParams(location.search)
    setQuery(params.get('q') || '')
  }, [location.search])

  // 3. Live Suggestions
  useEffect(() => {
    const controller = new AbortController()
    if (query.trim().length >= 3) {
      const timeoutId = setTimeout(() => {
        movieApi.search(query, controller.signal)
          .then(data => {
            setSuggestions(data.slice(0, 5))
            setOpen(true)
          })
          .catch(err => {
            if (err.name !== 'AbortError') console.error("Search failed:", err)
          })
      }, 300)
      return () => {
        clearTimeout(timeoutId)
        controller.abort()
      }
    } else {
      setSuggestions([])
      setOpen(false)
    }
  }, [query])

  // 4. Click outside to close
  useEffect(() => {
    function handleClick(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  function handleSearchSubmit(e) {
    if (e) e.preventDefault()
    if (query.trim().length >= 3) {
      setOpen(false)
      navigate(`/search?q=${encodeURIComponent(query.trim())}`)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') {
      handleSearchSubmit()
    }
  }

  return (
    <>
      <nav className={`navbar navbar-expand-lg fixed-top cv-nav ${scrolled ? 'cv-nav--scrolled' : ''}`}>
        <div className="container-fluid px-4">
          <Link className="navbar-brand fw-bold fs-4" to="/">
            fresh<span className="text-warning">Potatoes</span>
          </Link>

          {/*search*/}
          <form className="d-none d-lg-flex mx-auto cv-search search-wrapper" ref={wrapperRef} onSubmit={handleSearchSubmit}>
            <div className="input-group w-100">
              <span className="input-group-text bg-transparent border-secondary">
                <i className="bi bi-search text-secondary" />
              </span>
              <input
                type="search"
                className="form-control border-secondary bg-transparent text-light"
                placeholder="Search movies…"
                value={query}
                onChange={e => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                onFocus={() => suggestions.length > 0 && setOpen(true)}
              />
            </div>

            {open && suggestions.length > 0 && (
              <div className="search-dropdown">
                {suggestions.map(item => (
                  <Link
                    key={item.id}
                    to={`/movie/${item.id}`}
                    className="search-dropdown-item"
                    onClick={() => setOpen(false)}
                  >
                    <img src={item.posterUrl || '/favicon.png'} alt="" className="search-dropdown-thumb" />
                    <div className="search-dropdown-info">
                      <span className="search-dropdown-title">{item.title}</span>
                      <span className="search-dropdown-meta">{`${item.genre} · ${item.year}`}</span>
                    </div>
                  </Link>
                ))}
                <div className="search-dropdown-footer">Press Enter for all results</div>
              </div>
            )}
          </form>

          <div className="d-flex align-items-center gap-2">
            <Link to="/search" className="btn btn-link text-light d-lg-none p-2"><i className="bi bi-search fs-5" /></Link>
            {user ? (
              <div className="dropdown">
                <button className="btn btn-link text-warning text-decoration-none dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                  <i className="bi bi-person-circle fs-5"></i>
                  <span className="fw-bold text-truncate" style={{ maxWidth: '100px' }}>{user.username}</span>
                </button>
                <ul className="dropdown-menu dropdown-menu-end dropdown-menu-dark border-secondary">
                  <li><Link className="dropdown-item" to="/profile"><i className="bi bi-person me-2"></i> Profile</Link></li>
                  <li><hr className="dropdown-divider border-secondary" /></li>
                  <li><button className="dropdown-item text-danger" onClick={logout}><i className="bi bi-box-arrow-right me-2"></i> Logout</button></li>
                </ul>
              </div>
            ) : (
              <button className="btn btn-warning btn-sm px-4 fw-bold text-dark" onClick={() => setShowAuthModal(true)}>Log In</button>
            )}
          </div>
        </div>
      </nav>
      <AuthModal show={showAuthModal} onHide={() => setShowAuthModal(false)} />
    </>
  )
}

export default Navbar