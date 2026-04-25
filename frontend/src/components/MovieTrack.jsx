import { useRef } from 'react'
import { Link } from 'react-router-dom'
import MoviePoster from './MoviePoster'

export function MovieTrackSkeleton({ title = "Loading...", hideTopBorder = false }) {
  const dummyCards = Array.from({ length: 6 })
  
  return (
    <section className={`coming-section ${hideTopBorder ? 'no-top-border' : ''}`}>
      <h2 className="coming-heading">{title}</h2>
      <div className="coming-track-wrap">
        <div className="coming-track" style={{ overflow: 'hidden' }}>
          {dummyCards.map((_, i) => (
            <div key={i} className="coming-card">
              <div className="discover-skeleton-card skeleton-pulse"></div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

export default function MovieTrack({ title, movies, fetchingMore, onLoadMore, hideTopBorder = false }) {
  const trackRef = useRef(null)

  async function scrollBy(direction) {
    const track = trackRef.current
    if (!track) return
    const cards = track.querySelectorAll('.coming-card')
    if (cards.length === 0) return
    const cardWidth = cards[0].offsetWidth + 16 // 16 = gap

    if (direction === 1 && !fetchingMore && onLoadMore) {
      const maxScrollLeft = track.scrollWidth - track.clientWidth;
      if (track.scrollLeft >= maxScrollLeft - (cardWidth * 2)) {
        onLoadMore();
      }
    }

    setTimeout(() => {
      track.scrollBy({ left: direction * cardWidth * 2, behavior: 'smooth' })
    }, 10);
  }

  if (movies.length === 0) return null

  const sectionClass = hideTopBorder ? "genre-section" : "coming-section"

  return (
    <section className={sectionClass}>
      <h2 className="coming-heading">{title}</h2>
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
          {movies.map((movie, idx) => (
            <Link
              key={`${movie.id}-${idx}`}
              to={`/movie/${movie.id}`}
              className="coming-card"
            >
              <MoviePoster
                posterUrl={movie.posterUrl}
                title={movie.title}
                className="coming-card-img"
              />
              <div className="coming-card-overlay">
                <div className="coming-card-genres">
                  {movie.genres?.slice(0, 3).map(g => (
                    <p key={g} className="coming-card-genre">{g}</p>
                  ))}
                </div>
                <p className="coming-card-title text-truncate w-100">{movie.title}</p>
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
          disabled={fetchingMore}
        >
          {fetchingMore ? (
            <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          ) : (
            <i className="bi bi-chevron-right" />
          )}
        </button>
      </div>
    </section>
  )
}