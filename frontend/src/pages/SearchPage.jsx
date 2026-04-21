import { useSearchParams } from 'react-router-dom'
import { Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { movieApi, staffApi } from '../services/api'
import MoviePoster from '../components/MoviePoster'

function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryParam = searchParams.get('q') || ''
  const typeParam = searchParams.get('type') || 'movie'
  
  const [localQuery, setLocalQuery] = useState(queryParam)
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLocalQuery(queryParam)
    if (queryParam.trim().length >= 3) {
      setLoading(true)
      setError(null)
      const api = typeParam === 'movie' ? movieApi : staffApi
      api.search(queryParam)
        .then(data => setResults(data))
        .catch(err => setError(err.message))
        .finally(() => setLoading(false))
    } else {
      setResults([])
    }
  }, [queryParam, typeParam])

  const handleSearch = (e) => {
    e.preventDefault()
    if (localQuery.trim().length >= 3) {
      setSearchParams({ q: localQuery.trim(), type: typeParam })
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
        
        <form onSubmit={handleSearch} className="search-page-input-wrap mt-4">
          <div className="input-group">
            <input 
              type="text" 
              className="form-control bg-dark text-white border-secondary"
              placeholder={`Search ${typeParam === 'movie' ? 'movies' : 'actors'}...`}
              value={localQuery}
              onChange={(e) => setLocalQuery(e.target.value)}
            />
            <button className="btn btn-warning text-dark fw-bold px-4" type="submit">
              Search
            </button>
          </div>
          <div className="mt-2 d-flex gap-3">
             <div className="form-check">
                <input className="form-check-input" type="radio" name="searchType" id="typeMovie" 
                  checked={typeParam === 'movie'} onChange={() => setSearchParams({ q: localQuery, type: 'movie' })} />
                <label className="form-check-label text-secondary small" htmlFor="typeMovie">Movies</label>
             </div>
             <div className="form-check">
                <input className="form-check-input" type="radio" name="searchType" id="typeActor" 
                  checked={typeParam === 'actor'} onChange={() => setSearchParams({ q: localQuery, type: 'actor' })} />
                <label className="form-check-label text-secondary small" htmlFor="typeActor">Actors</label>
             </div>
          </div>
        </form>

        {!loading && queryParam.trim().length >= 3 && (
          <p className="search-page-count">
            {results.length} result{results.length !== 1 ? 's' : ''} found in {typeParam === 'movie' ? 'Movies' : 'Actors'}
          </p>
        )}
      </div>

      {queryParam.trim().length < 3 ? (
        <div className="search-page-empty">
          <i className="bi bi-search" />
          <p>Type at least 3 characters in the search bar above</p>
        </div>
      ) : loading ? (
        <div className="search-page-empty">
          <div className="spinner-border text-warning" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : error ? (
        <div className="search-page-empty">
          <i className="bi bi-exclamation-triangle text-danger" />
          <p>Error: {error}</p>
        </div>
      ) : results.length === 0 ? (
        <div className="search-page-empty">
          <i className={typeParam === 'movie' ? "bi bi-film" : "bi bi-person-x"} />
          <p>No {typeParam === 'movie' ? 'movies' : 'actors'} found for "{queryParam}"</p>
        </div>
      ) : (
        <div className="search-page-grid">
          {results.map(item => (
            <Link
              key={item.id}
              to={typeParam === 'movie' ? `/movie/${item.id}` : `/staff/${item.id}`}
              className="coming-card"
            >
              {typeParam === 'movie' ? (
                <MoviePoster
                  posterUrl={item.posterUrl}
                  title={item.title}
                  className="coming-card-img"
                />
              ) : (
                <div className="coming-card-img d-flex align-items-center justify-content-center bg-secondary text-dark" style={{ fontSize: '4rem' }}>
                  <i className="bi bi-person-fill" />
                </div>
              )}
              <div className="coming-card-overlay">
                <p className="coming-card-genre">
                  {typeParam === 'movie' 
                    ? (Array.isArray(item.genre) ? item.genre[0] : item.genre)
                    : (item.gender || 'Person')}
                </p>
                <p className="coming-card-title">{typeParam === 'movie' ? item.title : item.name}</p>
                <p className="coming-card-date">
                  <i className={typeParam === 'movie' ? "bi bi-calendar3" : "bi bi-geo-alt"} />
                  {typeParam === 'movie' ? item.year : (item.birthCountry || 'Unknown')}
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