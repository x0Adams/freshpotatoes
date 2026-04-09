import { useParams, Link } from 'react-router-dom'
import { useState } from 'react'
import { MOCK_MOVIES } from '../data/mockMovies'

//potato rating display
function PotatoRating({ rating }) {
  return (
    <div className="potato-rating">
      {[1, 2, 3, 4, 5].map(n => {
        const full = rating >= n
        const half = !full && rating >= n - 0.5
        return (
          <span
            key={n}
            className={`potato ${full ? 'lit' : half ? 'half' : ''}`}
          >
            🥔
          </span>
        )
      })}
      <span className="potato-score">{rating} / 5</span>
    </div>
  )
}

//recommendation strip design
function RecommendationStrip({ currentMovie }) {
  const recs = MOCK_MOVIES
    .filter(m =>
      m.id !== currentMovie.id &&
      Array.isArray(m.genre) &&
      m.genre.some(g => currentMovie.genre.includes(g))
    )
    .slice(0, 10)

  if (recs.length === 0) return null

  return (
    <div className="movie-section">
      <h2 className="movie-section-title">
        More like this — <span>{currentMovie.genre[0]}</span>
      </h2>
      <div className="coming-track" style={{ overflowX: 'auto' }}>
        {recs.map(movie => (
          <Link key={movie.id} to={`/movie/${movie.id}`} className="coming-card">
            <img src={movie.posterUrl} alt={movie.title} className="coming-card-img" />
            <div className="coming-card-overlay">
              <p className="coming-card-genre">
                {Array.isArray(movie.genre) ? movie.genre[0] : movie.genre}
              </p>
              <p className="coming-card-title">{movie.title}</p>
              <p className="coming-card-date">
                <i className="bi bi-calendar3" /> {movie.year}
              </p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}

//comment
const MOCK_COMMENTS = [
  { id: 1, author: "MovieBuff42",   date: "2024-03-12", text: "One of the best films of the year. Absolutely stunning." },
  { id: 2, author: "CinemaLover",   date: "2024-03-15", text: "The cinematography alone is worth the price of admission." },
  { id: 3, author: "PotatoCritic",  date: "2024-03-18", text: "A slow burn but deeply rewarding. Not for everyone." },
]

function CommentsSection() {
  const [comments, setComments] = useState(MOCK_COMMENTS)
  const [text, setText]         = useState('')

  function handleSubmit() {
    if (!text.trim()) return
    setComments(prev => [
      ...prev,
      {
        id: Date.now(),
        author: "Guest",
        date: new Date().toISOString().slice(0, 10),
        text: text.trim(),
      }
    ])
    setText('')
  }

  return (
    <div className="movie-section">
      <h2 className="movie-section-title">
        Comments <span>({comments.length})</span>
      </h2>

      {/* form */}
      <div className="comment-form">
        <textarea
          className="form-control comment-input mb-2"
          rows={3}
          placeholder="Leave a comment… (logged in users only in the future)"
          value={text}
          onChange={e => setText(e.target.value)}
        />
        <button
          className="btn btn-warning btn-sm fw-semibold px-4 text-dark"
          onClick={handleSubmit}
          disabled={!text.trim()}
        >
          Post Comment
        </button>
      </div>

      {/* list */}
      {comments.map(c => (
        <div key={c.id} className="comment-card">
          <div className="comment-author">{c.author}</div>
          <div className="comment-date">{c.date}</div>
          <p className="comment-text">{c.text}</p>
        </div>
      ))}
    </div>
  )
}

//main page
function MoviePage() {
  const { id }  = useParams()
  const movie   = MOCK_MOVIES.find(m => m.id === parseInt(id))

  if (!movie) return (
    <div className="text-center py-5 mt-5">
      <p className="text-secondary">Movie not found.</p>
      <Link to="/" className="btn btn-outline-warning btn-sm mt-3">← Back to Home</Link>
    </div>
  )

  // only fully detailed movies
  const hasDetails = !!movie.directors

  return (
    <div className="movie-page">

      {/* hero*/}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* poster */}
        <div className="movie-poster-frame">
          <img src={movie.posterUrl} alt="" aria-hidden className="movie-poster-glow" />
          <img src={movie.posterUrl} alt={movie.title} className="movie-poster-img" />
        </div>

        {/* info */}
        <div className="movie-info">

          <div className="movie-genres">
            {(Array.isArray(movie.genre) ? movie.genre : [movie.genre]).map(g => (
              <span key={g} className="movie-genre-badge">{g}</span>
            ))}
          </div>

          <h1 className="movie-title">{movie.title}</h1>

          <div className="movie-meta-row">
            <span><i className="bi bi-calendar3" /> {movie.year}</span>
            {movie.country && (
              <span><i className="bi bi-globe2" /> {movie.country}</span>
            )}
          </div>

          {/* potato rating */}
          {movie.rating && <PotatoRating rating={movie.rating} />}

          {/* Links */}
          {hasDetails && (
            <div className="movie-links">
              {movie.trailerUrl && (
                <a
                  href={movie.trailerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-warning btn-sm fw-semibold text-dark px-3" >
                  <i className="bi bi-play-fill me-1" /> Watch Trailer
                </a>
              )}
              {movie.youtubeUrl && (
                <a
                  href={movie.youtubeUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-outline-secondary btn-sm px-3">
                  <i className="bi bi-youtube me-1" /> YouTube Page
                </a>
              )}
            </div>
          )}

          {/* credits */}
          {hasDetails && (
            <div className="movie-credits">
              <div className="movie-credit-group">
                <h6>Directed by</h6>
                <p>{movie.directors.join(', ')}</p>
              </div>
              <div className="movie-credit-group">
                <h6>Starring</h6>
                <p>{movie.actors.join(', ')}</p>
              </div>
            </div>
          )}

          {/* description */}
          {movie.description && (
            <p className="movie-description">{movie.description}</p>
          )}

        </div>
      </div>

      {/* recom */}
      {hasDetails && <RecommendationStrip currentMovie={movie} />}

      {/*comments*/}
      {hasDetails && <CommentsSection />}

    </div>
  )
}

export default MoviePage