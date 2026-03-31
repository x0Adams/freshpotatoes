import { useState, useEffect , useRef } from 'react'
import { Link, useNavigate, useLocation  } from 'react-router-dom'
import { searchMovies } from '../data/mockMovies'

function Navbar({ onLoginClick }) {

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
    if (query.trim().length >= 3) {
      setSuggestions(searchMovies(query, 5))
      setOpen(true)
    } else {
      setSuggestions([])
      setOpen(false)
    }
  }, [query])

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
      navigate(`/search?q=${encodeURIComponent(query.trim())}`)
    }
  }

  function handleSuggestionClick() {
    setQuery('')
    setOpen(false)
  }

  // when on /search prefill
  useEffect(() => {
    const params = new URLSearchParams(location.search)
    const q = params.get('q') || ''
    setQuery(q)
  }, [location.search])

  // when on /search update
  useEffect(() => {
    if (location.pathname === '/search') {
      if (query.trim().length >= 3) {
        navigate(`/search?q=${encodeURIComponent(query.trim())}`, { replace: true })
      } else {
        navigate('/search', { replace: true })
      }
    }
  }, [query])

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
                className="form-control border-secondary border-start-0 bg-transparent"
                placeholder="Search movies…"
                value={query}
                onChange={e => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                onFocus={() => suggestions.length > 0 && setOpen(true)}
              />
            </div>

            {/*dropdown*/}
            {open && suggestions.length > 0 && (
              <div className="search-dropdown">
                {suggestions.map(movie => (
                  <Link
                    key={movie.id}
                    to={`/movie/${movie.id}`}
                    className="search-dropdown-item"
                    onClick={handleSuggestionClick}
                  >
                    <img
                      src={movie.posterUrl}
                      alt={movie.title}
                      className="search-dropdown-thumb"
                    />
                    <div className="search-dropdown-info">
                      <span className="search-dropdown-title">{movie.title}</span>
                      <span className="search-dropdown-meta">{movie.genre} · {movie.year}</span>
                    </div>
                  </Link>
                ))}
                <div className="search-dropdown-footer">
                  Press Enter to see all results
                </div>
              </div>
            )}
          </div>

          {/*login*/}
          <div className="d-flex align-items-center">
              <button 
                className="btn btn-outline-warning btn-sm px-3" 
                onClick={onLoginClick}>
                    Log In
              </button>
          </div>

        </div>
      </nav>
    </>
  )
}

export default Navbar