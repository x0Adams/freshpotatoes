import { Link } from 'react-router-dom'

const MOCK_MOVIES = [
  { id: 1,  title: "Dune: Part Two",        year: 2024, genre: "Sci-Fi",    posterUrl: "https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg" },
  { id: 2,  title: "The Brutalist",         year: 2024, genre: "Drama",     posterUrl: "https://upload.wikimedia.org/wikipedia/en/7/7c/TheBrutalist2024.png" },
  { id: 3,  title: "Nosferatu",             year: 2024, genre: "Horror",    posterUrl: "https://bendblockbuster.com/wp-content/uploads/2025/02/nosferatu-cover.jpg" },
  { id: 4,  title: "Anora",                 year: 2024, genre: "Romance",   posterUrl: "https://upload.wikimedia.org/wikipedia/en/2/2b/Anora_%282024_film%29_poster.jpg" },
  { id: 5,  title: "Conclave",              year: 2024, genre: "Thriller",  posterUrl: "https://m.media-amazon.com/images/M/MV5BYjgxMDI5NmMtNTU3OS00ZDQxLTgxZmEtNzY1ZTBmMDY4NDRkXkEyXkFqcGc@._V1_.jpg" },
  { id: 6,  title: "The Substance",         year: 2024, genre: "Horror",    posterUrl: "https://www.thrillandkill.com/wordpress/wp-content/uploads/2024/09/the-substance_cover.jpg" },
  { id: 7,  title: "A Complete Unknown",    year: 2024, genre: "Biography", posterUrl: "https://m.media-amazon.com/images/M/MV5BYTA2NTA5NDYtMzlkOC00MTQxLWI0NDQtMzk2M2YzMGE4MTkxXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg" },
  { id: 8,  title: "Gladiator II",          year: 2024, genre: "Action",    posterUrl: "https://m.media-amazon.com/images/M/MV5BMWYzZTM5ZGQtOGE5My00NmM2LWFlMDEtMGNjYjdmOWM1MzA1XkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg" },
  { id: 9,  title: "Alien: Romulus",        year: 2024, genre: "Sci-Fi",    posterUrl: "https://i.redd.it/alien-romulus-standard-cover-art-v0-41e4o2e8i6rd1.jpg?width=853&format=pjpg&auto=webp&s=368cdcbf1c7e2a92a8a691bc0ad2a0869cd540e9" },
  { id: 10, title: "Longlegs",              year: 2024, genre: "Thriller",  posterUrl: "https://m.media-amazon.com/images/M/MV5BMmJkNGNiNjgtMzFlYy00ZDI5LWI2YzktZGVjYjI5MjQyMGU3XkEyXkFqcGc@._V1_.jpg" },
]

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
          {MOCK_MOVIES.map((_, i) => (
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
          {MOCK_MOVIES.map((movie, i) => (
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