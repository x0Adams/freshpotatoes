import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { movieApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useSearch } from '../context/SearchContext'
import AuthModal from './AuthModal'

function Navbar() {
  const { user, logout, showAuthModal, setShowAuthModal } = useAuth()
  const { liveQuery, setLiveQuery } = useSearch()
  const [scrolled, setScrolled] = useState(false)
  
  const navigate = useNavigate()
  const location = useLocation()
  const [suggestions, setSuggestions] = useState([])
  const [open, setOpen] = useState(false)
  const wrapperRef = useRef(null)

  // 1. Handle scroll effect
  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 0)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // 2. Sync State FROM URL (Handled by SearchContext)

  // 3. Live Suggestions
  useEffect(() => {
    const controller = new AbortController()
    if (liveQuery.trim().length >= 3) {
      const timeoutId = setTimeout(() => {
        movieApi.search(liveQuery, controller.signal)
          .then(data => {
            if (!controller.signal.aborted) {
              setSuggestions(data.slice(0, 5))
              setOpen(true)
            }
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
      // Avoid extra renders if suggestions are already empty
      setSuggestions(prev => prev.length > 0 ? [] : prev)
      setOpen(false)
    }
  }, [liveQuery])

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
    if (liveQuery.trim().length >= 3) {
      setOpen(false)
      
      // If we are already on the search page, preserve current filters
      if (location.pathname === '/search') {
        const params = new URLSearchParams(location.search)
        params.set('q', liveQuery.trim())
        navigate(`/search?${params.toString()}`)
      } else {
        navigate(`/search?q=${encodeURIComponent(liveQuery.trim())}`)
      }
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
                value={liveQuery}
                onChange={e => setLiveQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                onFocus={() => suggestions.length > 0 && setOpen(true)}
              />
            </div>

            {location.pathname !== '/search' && open && suggestions.length > 0 && (
              <div className="search-dropdown animate-fade-in">
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
            <Link to="/search" className="btn btn-link text-light d-lg-none p-2">
              <i className="bi bi-search fs-5" />
            </Link>
            {user ? (
              <div className="dropdown">
                <button 
                  className="btn btn-nav-user dropdown-toggle" 
                  type="button" 
                  data-bs-toggle="dropdown"
                >
                  <i className="bi bi-person-circle fs-5" />
                  <span className="text-truncate d-none d-sm-inline" style={{ maxWidth: '100px' }}>{user.username}</span>
                </button>
                <ul className="dropdown-menu dropdown-menu-end">
                  {user.isAdmin && (
                    <>
                      <li>
                        <Link className="dropdown-item text-warning" to="/admin">
                          <i className="bi bi-shield-lock-fill" /> Control Center
                        </Link>
                      </li>
                      <li><hr className="dropdown-divider border-opacity-25" /></li>
                    </>
                  )}
                  <li>
                    <Link className="dropdown-item" to="/profile">
                      <i className="bi bi-person-badge" /> Profile
                    </Link>
                  </li>
                  <li><hr className="dropdown-divider border-opacity-25" /></li>
                  <li>
                    <button className="dropdown-item text-danger" onClick={logout}>
                      <i className="bi bi-box-arrow-right" /> Logout
                    </button>
                  </li>
                </ul>
              </div>
            ) : (
              <button 
                className="btn btn-nav-login" 
                onClick={() => setShowAuthModal(true)}
              >
                Log In
              </button>
            )}
          </div>
        </div>
      </nav>
      <AuthModal show={showAuthModal} onHide={() => setShowAuthModal(false)} />
    </>
  )
}

export default Navbar