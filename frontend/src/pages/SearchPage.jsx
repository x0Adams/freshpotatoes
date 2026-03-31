import { useSearchParams } from 'react-router-dom'
import { Link } from 'react-router-dom'
import { searchMovies } from '../data/mockMovies'

function SearchPage() {
  const [searchParams] = useSearchParams()
  const query   = searchParams.get('q') || ''
  const results = searchMovies(query, 100)

  return (
    <div className="search-page">
    <div style={{ height: '64px' }} />
      <div className="search-page-header">
        <h1 className="search-page-title">
          {query.trim().length >= 3
            ? <>Results for <span>"{query}"</span></>
            : 'Search'}
        </h1>
        {query.trim().length >= 3 && (
          <p className="search-page-count">
            {results.length} result{results.length !== 1 ? 's' : ''} found
          </p>
        )}
      </div>

      {query.trim().length < 3 ? (
        <div className="search-page-empty">
          <i className="bi bi-search" />
          <p>Type at least 3 characters in the search bar above</p>
        </div>
      ) : results.length === 0 ? (
        <div className="search-page-empty">
          <i className="bi bi-film" />
          <p>No movies found for "{query}"</p>
        </div>
      ) : (
        <div className="search-page-grid">
          {results.map(movie => (
            <Link
              key={movie.id}
              to={`/movie/${movie.id}`}
              className="coming-card"
            >
              <img
                src={movie.posterUrl}
                alt={movie.title}
                className="coming-card-img"
              />
              <div className="coming-card-overlay">
                <p className="coming-card-genre">{movie.genre}</p>
                <p className="coming-card-title">{movie.title}</p>
                <p className="coming-card-date">
                  <i className="bi bi-calendar3" />
                  {movie.year}
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