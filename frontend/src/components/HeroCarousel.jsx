import { Link } from 'react-router-dom'
import { useState, useEffect, useRef } from 'react'
import { movieApi } from '../services/api'
import MoviePoster from './MoviePoster'
import testBg from '../assets/test_bg.jpg'
import * as bootstrap from 'bootstrap'

function HeroSlide({ movie, isActive, isFirstLoad }) {
  return (
    <div className={`carousel-item h-100 ${isActive ? 'active' : ''} ${isFirstLoad ? 'hero-first-load' : ''}`}>
      <div className="hero-slide">
        <div className="container-fluid h-100">
          <div className="row h-100 align-items-center">

            {/* poster-frame */}
            <div className="col-12 col-md-5 hero-poster-wrap">
              <div className="poster-frame">

                {/* background cust */}
                <img
                  src={movie.posterUrl || testBg}
                  alt=""
                  aria-hidden="true"
                  className={`poster-glow ${!movie.posterUrl ? 'opacity-50' : ''}`}
                />

                {/* poster */}
                <MoviePoster
                  posterUrl={movie.posterUrl}
                  title={movie.title}
                  className="hero-poster"
                />

              </div>
            </div>

            {/* describtion */}
            <div className="col-12 col-md-7 hero-info">
              <div className="movie-genres mb-2">
                {movie.genres && movie.genres.map(g => (
                  <span key={g} className="movie-genre-badge">{g}</span>
                ))}
              </div>
              <h1 className="hero-title">{movie.title}</h1>
              <p className="hero-year">{movie.year}</p>
              <Link to={`/movie/${movie.id}`}
                className="btn-fresh-primary mt-2">
                View Movie <i className="bi bi-arrow-right ms-2" />
              </Link>
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}

function HeroSkeleton() {
  return (
    <div className="hero-carousel d-flex justify-content-center align-items-center bg-dark">
      <div className="hero-slide w-100">
        <div className="container-fluid h-100">
          <div className="row h-100 align-items-center">

            {/* poster-frame skeleton */}
            <div className="col-12 col-md-5 hero-poster-wrap">
              <div className="poster-frame">
                <div className="hero-skeleton-poster skeleton-pulse"></div>
              </div>
            </div>

            {/* description skeleton */}
            <div className="col-12 col-md-7 hero-info">
              <div className="hero-skeleton-text skeleton-pulse" style={{ width: '20%' }}></div>
              <div className="hero-skeleton-title skeleton-pulse"></div>
              <div className="hero-skeleton-text skeleton-pulse" style={{ width: '15%', marginBottom: '2.2rem' }}></div>
              <div className="hero-skeleton-btn skeleton-pulse"></div>
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}

function HeroCarousel() {
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(true)
  const [isFirstLoad, setIsFirstLoad] = useState(true)
  const [isSliding, setIsSliding] = useState(false)
  const carouselRef = useRef(null)

  useEffect(() => {
    movieApi.getMovies(0, 10)
      .then(data => setMovies(data))
      .catch(err => console.error("Failed to fetch carousel movies:", err))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!loading && movies.length > 0 && carouselRef.current) {
      const carousel = new bootstrap.Carousel(carouselRef.current, {
        interval: 8000,
        pause: false
      })

      const handleSlide = () => {
        setIsFirstLoad(false)
        setIsSliding(true)
      }
      const handleSlid = () => setIsSliding(false)

      carouselRef.current.addEventListener('slide.bs.carousel', handleSlide)
      carouselRef.current.addEventListener('slid.bs.carousel', handleSlid)

      carousel.cycle()

      return () => {
        if (carouselRef.current) {
          carouselRef.current.removeEventListener('slide.bs.carousel', handleSlide)
          carouselRef.current.removeEventListener('slid.bs.carousel', handleSlid)
        }
        carousel.dispose()
      }
    }
  }, [loading, movies])

  if (loading) return <HeroSkeleton />

  if (movies.length === 0) return null

  const handlePrev = () => {
    if (isSliding) return
    const inst = bootstrap.Carousel.getInstance(carouselRef.current)
    inst?.prev()
  }

  const handleNext = () => {
    if (isSliding) return
    const inst = bootstrap.Carousel.getInstance(carouselRef.current)
    inst?.next()
  }

  return (
    <div className="hero-carousel">
      <div
        id="heroCarousel"
        ref={carouselRef}
        className="carousel slide h-100 slide"
        data-bs-interval="8000"
        data-bs-pause="false">
        <div className="carousel-indicators">
          {movies.map((_, i) => (
            <button key={i}
              type="button"
              data-bs-target="#heroCarousel"
              data-bs-slide-to={i}
              className={i === 0 ? 'active' : ''}
              aria-label={`Slide ${i + 1}`}
              disabled={isSliding}
            />
          ))}
        </div>

        <div className="carousel-inner h-100">
          {movies.map((movie, i) => (
            <HeroSlide 
              key={`${movie.id}-${i}`} 
              movie={movie} 
              isActive={i === 0} 
              isFirstLoad={i === 0 && isFirstLoad} 
            />
          ))}
        </div>

        <button 
          className="carousel-control-prev" 
          type="button" 
          onClick={handlePrev}
          style={{ cursor: isSliding ? 'default' : 'pointer', pointerEvents: isSliding ? 'none' : 'auto' }}
        >
          <span className="carousel-control-prev-icon" />
        </button>
        <button 
          className="carousel-control-next" 
          type="button" 
          onClick={handleNext}
          style={{ cursor: isSliding ? 'default' : 'pointer', pointerEvents: isSliding ? 'none' : 'auto' }}
        >
          <span className="carousel-control-next-icon" />
        </button>
      </div>
    </div>
  )
}

export default HeroCarousel