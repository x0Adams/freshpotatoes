import { useRef } from 'react'
import { Link } from 'react-router-dom'

const MOCK_UPCOMING = [
  { id: 101, title: "Captain America: Brave New World",          genre: "Action",    year: 2025, posterUrl: "https://picsum.photos/seed/movie101/400/600" },
  { id: 102, title: "Snow White",                                genre: "Fantasy",   year: 2025, posterUrl: "https://picsum.photos/seed/movie102/400/600" },
  { id: 103, title: "Thunderbolts*",                             genre: "Action",    year: 2025, posterUrl: "https://picsum.photos/seed/movie103/400/600" },
  { id: 104, title: "Mission: Impossible – The Final Reckoning", genre: "Thriller",  year: 2025, posterUrl: "https://picsum.photos/seed/movie104/400/600" },
  { id: 105, title: "Lilo & Stitch",                             genre: "Family",    year: 2025, posterUrl: "https://picsum.photos/seed/movie105/400/600" },
  { id: 106, title: "The Fantastic Four: First Steps",           genre: "Sci-Fi",    year: 2025, posterUrl: "https://picsum.photos/seed/movie106/400/600" },
  { id: 107, title: "Jurassic World Rebirth",                    genre: "Adventure", year: 2025, posterUrl: "https://picsum.photos/seed/movie107/400/600" },
  { id: 108, title: "Superman",                                  genre: "Action",    year: 2025, posterUrl: "https://picsum.photos/seed/movie108/400/600" },
  { id: 109, title: "How to Train Your Dragon",                  genre: "Animation", year: 2025, posterUrl: "https://picsum.photos/seed/movie109/400/600" },
  { id: 110, title: "28 Years Later",                            genre: "Horror",    year: 2025, posterUrl: "https://picsum.photos/seed/movie110/400/600" },
  { id: 111, title: "Materialists",                              genre: "Romance",   year: 2025, posterUrl: "https://picsum.photos/seed/movie111/400/600" },
  { id: 112, title: "Sinners",                                   genre: "Horror",    year: 2025, posterUrl: "https://picsum.photos/seed/movie112/400/600" },
  { id: 113, title: "Final Destination: Bloodlines",             genre: "Horror",    year: 2025, posterUrl: "https://picsum.photos/seed/movie113/400/600" },
  { id: 114, title: "Avatar: Fire and Ash",                      genre: "Sci-Fi",    year: 2025, posterUrl: "https://picsum.photos/seed/movie114/400/600" },
  { id: 115, title: "Karate Kid: Legends",                       genre: "Action",    year: 2025, posterUrl: "https://picsum.photos/seed/movie115/400/600" },
  { id: 116, title: "Predator: Badlands",                        genre: "Sci-Fi",    year: 2025, posterUrl: "https://picsum.photos/seed/movie116/400/600" },
  { id: 117, title: "The Phoenician Scheme",                     genre: "Comedy",    year: 2025, posterUrl: "https://picsum.photos/seed/movie117/400/600" },
  { id: 118, title: "Ballerina",                                 genre: "Action",    year: 2025, posterUrl: "https://picsum.photos/seed/movie118/400/600" },
  { id: 119, title: "Nonnas",                                    genre: "Comedy",    year: 2025, posterUrl: "https://picsum.photos/seed/movie119/400/600" },
  { id: 120, title: "The Life of Chuck",                         genre: "Drama",     year: 2025, posterUrl: "https://picsum.photos/seed/movie120/400/600" },
]

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
          {MOCK_UPCOMING.map(movie => (
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