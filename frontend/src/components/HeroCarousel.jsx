import { Link } from 'react-router-dom'
import { MOCK_MOVIES } from '../data/mockMovies'

const CAROUSEL_MOVIES = MOCK_MOVIES.filter(m => m.id <= 10)

function HeroSlide({ movie, isActive }) {
  return (
    <div className={`carousel-item h-100 ${isActive ? 'active' : ''}`}>
      <div className="hero-slide">
        <div className="container-fluid h-100">
          <div className="row h-100 align-items-center">

            {/* poster-frame */}
            <div className="col-12 col-md-5 hero-poster-wrap">
              <div className="poster-frame">

                {/* background cust */}
                <img
                  src={movie.posterUrl}
                  alt=""
                  aria-hidden="true"
                  className="poster-glow"
                />

                {/* poster */}
                <img
                  src={movie.posterUrl}
                  alt={movie.title}
                  className="hero-poster"
                />

              </div>
            </div>

            {/* describtion */}
            <div className="col-12 col-md-7 hero-info">
              <p className="hero-genre">{movie.genre}</p>
              <h1 className="hero-title">{movie.title}</h1>
              <p className="hero-year">{movie.year}</p>
              <Link to={`/movie/${movie.id}`}
                className="btn btn-warning fw-semibold px-4 py-2 text-dark">
                View Movie →
              </Link>
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}

function HeroCarousel() {
  return (
    <div className="hero-carousel">
      <div
        id="heroCarousel"
        className="carousel slide h-100"
        data-bs-ride="carousel"
        data-bs-interval="6000">
        <div className="carousel-indicators">
          {CAROUSEL_MOVIES.map((_, i) => (
            <button key={i}
              type="button"
              data-bs-target="#heroCarousel"
              data-bs-slide-to={i}
              className={i === 0 ? 'active' : ''}
              aria-label={`Slide ${i + 1}`}
            />
          ))}
        </div>

        <div className="carousel-inner h-100">
          {CAROUSEL_MOVIES.map((movie, i) => (
            <HeroSlide key={movie.id} movie={movie} isActive={i === 0} />
          ))}
        </div>

        <button className="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
          <span className="carousel-control-prev-icon" />
        </button>
        <button className="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
          <span className="carousel-control-next-icon" />
        </button>
      </div>
    </div>
  )
}

export default HeroCarousel