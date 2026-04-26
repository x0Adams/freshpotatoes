import { useParams, Link } from 'react-router-dom';
import { useState, useEffect, useCallback, useRef } from 'react';
import { movieApi } from '../services/api';
import MoviePoster from '../components/MoviePoster';
import testBg from '../assets/test_bg.jpg';

function GenrePage() {
  const { genreName } = useParams();
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchingMore, setFetchingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState(null);
  const PAGE_SIZE = 20;

  const observer = useRef();
  const lastElementRef = useCallback(node => {
    if (loading || fetchingMore) return;
    if (observer.current) observer.current.disconnect();
    
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        setPage(prev => prev + 1);
      }
    });
    
    if (node) observer.current.observe(node);
  }, [loading, fetchingMore, hasMore]);

  useEffect(() => {
    setMovies([]);
    setPage(0);
    setHasMore(true);
  }, [genreName]);

  useEffect(() => {
    const fetchGenreMovies = async () => {
      const isInitial = page === 0;
      if (isInitial) setLoading(true);
      else setFetchingMore(true);
      
      setError(null);

      try {
        const data = await movieApi.getMoviesByGenre(genreName, page, PAGE_SIZE);
        
        if (data.length < PAGE_SIZE) {
          setHasMore(false);
        }

        setMovies(prev => isInitial ? data : [...prev, ...data]);
      } catch (err) {
        console.error("Failed to fetch genre movies:", err);
        setError("We couldn't load the movies for this genre.");
      } finally {
        setLoading(false);
        setFetchingMore(false);
      }
    };

    fetchGenreMovies();
  }, [genreName, page]);

  return (
    <div className="movie-page">
      {/* Hero Section */}
      <div className="movie-hero" style={{ minHeight: '400px', display: 'flex', alignItems: 'center', overflow: 'hidden' }}>
        <div className="movie-hero-bg" />
        
        <div className="container position-relative animate-hero-reveal" style={{ zIndex: 1 }}>
          {/* subtle colorful glow behind text */}
          <img 
            src={testBg} 
            alt="" 
            aria-hidden 
            className="position-absolute start-0 top-50 translate-middle-y opacity-25" 
            style={{ width: '800px', height: '500px', filter: 'blur(120px)', zIndex: -1, pointerEvents: 'none' }} 
          />

          <div className="movie-genres mb-3">
             <span className="movie-genre-badge">Exploring Genre</span>
          </div>
          <h1 className="movie-title mb-0" style={{ fontSize: 'clamp(2.5rem, 8vw, 5rem)' }}>
            {genreName}
          </h1>
          <p className="text-secondary opacity-50 uppercase smallest tracking-widest mt-3">
             {movies.length > 0 ? `Discovering ${movies.length}${hasMore ? '+' : ''} cinematic gems` : 'Loading collection...'}
          </p>
        </div>
      </div>

      {/* Movies Grid */}
      <div className="container pb-5 animate-fade-in">
        {loading && page === 0 ? (
          <div className="d-flex justify-content-center py-5">
            <div className="spinner-border text-warning" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : error ? (
          <div className="search-page-empty py-5">
            <i className="bi bi-exclamation-triangle text-danger" />
            <h3>{error}</h3>
            <Link to="/" className="btn-fresh-secondary mt-3">Back to Home</Link>
          </div>
        ) : movies.length === 0 ? (
          <div className="search-page-empty py-5">
            <i className="bi bi-film" />
            <h3>No movies found in this genre.</h3>
            <Link to="/" className="btn-fresh-secondary mt-3">Back to Home</Link>
          </div>
        ) : (
          <div className="search-page-grid mt-4">
            {movies.map((movie, index) => {
              const isLast = movies.length === index + 1;
              return (
                <Link 
                  to={`/movie/${movie.id}`} 
                  key={`${movie.id}-${index}`} 
                  className="coming-card"
                  ref={isLast ? lastElementRef : null}
                >
                  <MoviePoster
                    posterUrl={movie.posterUrl}
                    title={movie.title}
                    className="coming-card-img"
                  />
                  <div className="coming-card-overlay">
                    <div className="coming-card-genres">
                      {movie.genres.slice(0, 2).map(g => (
                        <p key={g} className="coming-card-genre">{g}</p>
                      ))}
                    </div>
                    <p className="coming-card-title text-truncate w-100">{movie.title}</p>
                    <p className="coming-card-date">
                      <i className="bi bi-calendar3" /> {movie.year}
                    </p>
                  </div>
                </Link>
              );
            })}
          </div>
        )}

        {fetchingMore && (
          <div className="d-flex justify-content-center py-4">
            <div className="spinner-border spinner-border-sm text-warning" />
          </div>
        )}
      </div>
    </div>
  );
}

export default GenrePage;