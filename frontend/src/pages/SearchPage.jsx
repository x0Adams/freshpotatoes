import { useSearchParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { movieApi } from '../services/api'
import MoviePoster from '../components/MoviePoster'

function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryParam = searchParams.get('q') || ''
  
  const [localQuery, setLocalQuery] = useState(queryParam)
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // 1. URL -> State (Synchronize when navigating back/forward)
  useEffect(() => {
    setLocalQuery(queryParam)
  }, [queryParam])

  // 2. State -> Fetching (The core search logic)
  useEffect(() => {
    const query = queryParam.trim()
    if (query.length >= 3) {
      setLoading(true)
      setError(null)
      movieApi.advancedSearch({ title: query, size: 30 })
        .then(data => {
          setResults(data)
          setLoading(false)
        })
        .catch(err => {
          setError(err.message)
          setLoading(false)
        })
    } else {
      setResults([])
      setLoading(false)
    }
  }, [queryParam])

  const handleLocalSearchSubmit = (e) => {
    if (e) e.preventDefault()
    const trimmed = localQuery.trim()
    if (trimmed.length >= 3) {
      setSearchParams({ q: trimmed })
    } else if (trimmed === '') {
      setSearchParams({})
    }
  }

  return (
    <div className="search-page">
      <div style={{ height: '64px' }} />
      <div className="search-page-header">
        <h1 className="search-page-title">
          {queryParam.trim().length >= 3
            ? <>Results for <span>"{queryParam}"</span></>
            : `Search freshPotatoes`}
        </h1>
        
        <form onSubmit={handleLocalSearchSubmit} className="search-page-input-wrap mt-4 d-lg-none">
          <div className="input-group">
            <input 
              type="text" 
              className="form-control bg-dark text-white border-secondary"
              placeholder="Search movies..."
              value={localQuery}
              onChange={(e) => setLocalQuery(e.target.value)}
            />
            <button className="btn btn-warning text-dark fw-bold px-4" type="submit">
              Search
            </button>
          </div>
        </form>

        {!loading && queryParam.trim().length >= 3 && (
          <p className="search-page-count">
            {results.length} result{results.length !== 1 ? 's' : ''} found
          </p>
        )}
      </div>

      {loading ? (
        <div className="search-page-empty">
          <div className="spinner-border text-warning" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-3 text-secondary">Finding movies...</p>
        </div>
      ) : queryParam.trim().length < 3 ? (
        <div className="search-page-empty">
          <i className="bi bi-search text-secondary opacity-25" />
          <p>Type at least 3 characters to start searching</p>
        </div>
      ) : error ? (
        <div className="search-page-empty">
          <i className="bi bi-exclamation-triangle text-danger" />
          <p>Error: {error}</p>
        </div>
      ) : results.length === 0 ? (
        <div className="search-page-empty">
          <i className="bi bi-film text-secondary opacity-25" />
          <p>No movies found for "{queryParam}"</p>
        </div>
      ) : (
        <div className="search-page-grid">
          {results.map((item, idx) => (
            <Link
              key={`${item.id}-${idx}`}
              to={`/movie/${item.id}`}
              className="coming-card"
            >
              <MoviePoster
                posterUrl={item.posterUrl}
                title={item.title}
                className="coming-card-img"
              />
              <div className="coming-card-overlay">
                <p className="coming-card-genre">
                  {(Array.isArray(item.genre) ? item.genre[0] : item.genre)}
                </p>
                <p className="coming-card-title">{item.title}</p>
                <p className="coming-card-date">
                  <i className="bi bi-calendar3" />
                  {item.year}
                </p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default SearchPage