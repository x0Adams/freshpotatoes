import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { MOCK_MOVIES } from '../data/mockMovies'

const UPCOMING_MOVIES = MOCK_MOVIES.filter(m => m.id >= 101)

function ComingAttractions() {
  const trackRef = useRef(null)

  function scrollBy(direction) {
    const track = trackRef.current
    const cardWidth = track.querySelector('.coming-card').offsetWidth + 16 // 16 = gap
    track.scrollBy({ left: direction * cardWidth * 2, behavior: 'smooth' })
  }

  return (
    <section className="coming-section">

      <h2 className="coming-heading">Coming Attractions</h2>

      <div className="coming-track-wrap">

        {/*prev*/}
        <button
          className="coming-nav-btn prev"
          onClick={() => scrollBy(-1)}
          aria-label="Scroll left"
        >
          <i className="bi bi-chevron-left" />
        </button>

        {/*track*/}
        <div className="coming-track" ref={trackRef}>
          {UPCOMING_MOVIES.map(movie => (
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
                <p className="coming-card-genre">{Array.isArray(movie.genre) ? movie.genre[0] : movie.genre}</p>
                <p className="coming-card-title">{movie.title}</p>
                <p className="coming-card-date">
                  <i className="bi bi-calendar3" />
                  {movie.year}
                </p>
              </div>
            </Link>
          ))}
        </div>

        {/*next*/}
        <button
          className="coming-nav-btn next"
          onClick={() => scrollBy(1)}
          aria-label="Scroll right"
        >
          <i className="bi bi-chevron-right" />
        </button>

      </div>
    </section>
  )
}

export default ComingAttractions