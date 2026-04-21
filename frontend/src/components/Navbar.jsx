import { useState, useEffect , useRef } from 'react'
import { Link, useNavigate, useLocation  } from 'react-router-dom'
import { movieApi, staffApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import AuthModal from './AuthModal'

function Navbar() {
  const { user, logout } = useAuth()
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [searchMode, setSearchMode] = useState('movie') // 'movie' or 'actor'

  // scroll event listener
  const [scrolled, setScrolled] = useState(false)
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [open, setOpen] = useState(false)
  const wrapperRef = useRef(null)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 0)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // live suggestions
  useEffect(() => {
    const controller = new AbortController()
    
    if (query.trim().length >= 3) {
      const timeoutId = setTimeout(() => {
        const api = searchMode === 'movie' ? movieApi : staffApi
        api.search(query, controller.signal)
          .then(data => {
            setSuggestions(data.slice(0, 5))
            setOpen(true)
          })
          .catch(err => {
            if (err.name !== 'AbortError') console.error("Search failed:", err)
          })
      }, 300) // 300ms debounce
      
      return () => {
        clearTimeout(timeoutId)
        controller.abort()
      }
    } else {
      setSuggestions([])
      setOpen(false)
    }
  }, [query, searchMode])

  // close dropdown
  useEffect(() => {
    function handleClickOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleKeyDown(e) {
    if (e.key === 'Enter' && query.trim().length >= 3) {
      setOpen(false)
      navigate(`/search?q=${encodeURIComponent(query.trim())}&type=${searchMode}`)
    }
  }

  function handleSuggestionClick() {
    setQuery('')
    setOpen(false)
  }

  // Prefill search from URL
  useEffect(() => {
    const params = new URLSearchParams(location.search)
    const q = params.get('q') || ''
    const t = params.get('type') || 'movie'
    
    // Only update state if it actually differs to avoid extra renders
    if (q !== query) setQuery(q)
    if (t !== searchMode) setSearchMode(t)
  }, [location.search])

  // Update URL when search state changes (only when already on /search)
  useEffect(() => {
    if (location.pathname !== '/search') return

    const params = new URLSearchParams(location.search)
    const currentQ = params.get('q') || ''
    const currentT = params.get('type') || 'movie'

    const queryTrimmed = query.trim()
    
    // Only navigate if state is different from what's currently in the URL
    if (queryTrimmed !== currentQ || searchMode !== currentT) {
      const newParams = new URLSearchParams()
      if (queryTrimmed.length >= 3) newParams.set('q', queryTrimmed)
      newParams.set('type', searchMode)
      
      const newSearch = newParams.toString()
      const currentSearch = location.search.startsWith('?') ? location.search.substring(1) : location.search

      if (newSearch !== currentSearch) {
        navigate(`/search?${newSearch}`, { replace: true })
      }
    }
  }, [query, searchMode, location.pathname])

  return (
    <>
      <nav className={`navbar navbar-expand-lg fixed-top cv-nav ${scrolled ? 'cv-nav--scrolled' : ''}`}>
        <div className="container-fluid px-4">
          {/*namelink*/}
          <Link className="navbar-brand fw-bold fs-4" to="/">
            fresh<span className="text-warning">Potatoes</span>
          </Link>

          {/*search*/}
          <div className="d-none d-lg-flex mx-auto cv-search search-wrapper" ref={wrapperRef}>
            <div className="input-group w-100">
              <span className="input-group-text bg-transparent border-secondary">
                <i className="bi bi-search text-secondary" />
              </span>
              <input
                type="search"
                className="form-control border-secondary border-start-0 border-end-0 bg-transparent text-light"
                placeholder={searchMode === 'movie' ? "Search movies…" : "Search actors…"}
                value={query}
                onChange={e => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                onFocus={() => suggestions.length > 0 && setOpen(true)}
              />
              <button 
                className="btn btn-outline-secondary dropdown-toggle border-secondary" 
                type="button" 
                data-bs-toggle="dropdown" 
                aria-expanded="false"
              >
                {searchMode === 'movie' ? 'Movies' : 'Actors'}
              </button>
              <ul className="dropdown-menu dropdown-menu-end dropdown-menu-dark border-secondary">
                <li><button className="dropdown-item" onClick={() => setSearchMode('movie')}>Movies</button></li>
                <li><button className="dropdown-item" onClick={() => setSearchMode('actor')}>Actors</button></li>
              </ul>
            </div>

            {/*dropdown*/}
            {open && suggestions.length > 0 && (
              <div className="search-dropdown">
                {suggestions.map(item => (
                  <Link
                    key={item.id}
                    to={searchMode === 'movie' ? `/movie/${item.id}` : `/staff/${item.id}`}
                    className="search-dropdown-item"
                    onClick={handleSuggestionClick}
                  >
                    {searchMode === 'movie' ? (
                      <img
                        src={item.posterUrl || '/favicon.png'}
                        alt={item.title}
                        className="search-dropdown-thumb"
                      />
                    ) : (
                      <div className="search-dropdown-thumb d-flex align-items-center justify-content-center bg-secondary">
                        <i className="bi bi-person-fill text-dark" />
                      </div>
                    )}
                    <div className="search-dropdown-info">
                      <span className="search-dropdown-title">{searchMode === 'movie' ? item.title : item.name}</span>
                      <span className="search-dropdown-meta">
                        {searchMode === 'movie' 
                          ? `${item.genre} · ${item.year}` 
                          : `${item.gender || 'Person'} · ${item.birthCountry || 'Unknown'}`}
                      </span>
                    </div>
                  </Link>
                ))}
                <div className="search-dropdown-footer">
                  Press Enter to see all results
                </div>
              </div>
            )}
          </div>

          {/*auth*/}
          <div className="d-flex align-items-center gap-2">
            {/* mobile search icon */}
            <Link 
              to="/search" 
              className="btn btn-link text-light d-lg-none p-2"
              aria-label="Search"
            >
              <i className="bi bi-search fs-5" />
            </Link>

            {user ? (
              <div className="dropdown">
                <button 
                  className="btn btn-link text-warning text-decoration-none dropdown-toggle d-flex align-items-center gap-2" 
                  type="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false"
                >
                  <i className="bi bi-person-circle fs-5"></i>
                  <span className="d-none d-md-inline fw-bold">{user.username}</span>
                </button>
                <ul className="dropdown-menu dropdown-menu-end dropdown-menu-dark border-secondary">
                  <li>
                    <Link className="dropdown-item" to="/profile">
                      <i className="bi bi-person me-2"></i> Profile
                    </Link>
                  </li>
                  <li><hr className="dropdown-divider border-secondary" /></li>
                  <li>
                    <button className="dropdown-item text-danger" onClick={logout}>
                      <i className="bi bi-box-arrow-right me-2"></i> Logout
                    </button>
                  </li>
                </ul>
              </div>
            ) : (
              <button 
                className="btn btn-outline-warning btn-sm px-4 fw-bold" 
                onClick={() => setShowAuthModal(true)}
              >
                Log In
              </button>
            )}
          </div>
        </div>
      </nav>

      <AuthModal 
        show={showAuthModal} 
        onHide={() => setShowAuthModal(false)} 
      />
    </>
  )
}

export default Navbar