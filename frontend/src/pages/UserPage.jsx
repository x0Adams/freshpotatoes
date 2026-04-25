import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import PlaylistSection from '../components/PlaylistSection';
import MovieTrack from '../components/MovieTrack';
import { movieApi, reviewApi } from '../services/api';
import testBg from '../assets/test_bg.jpg';

function UserPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [ratedMovies, setRatedMovies] = useState([]);
  const [reviewedMovies, setReviewedMovies] = useState([]);
  const [loadingContent, setLoadingContent] = useState(true);

  // If someone tries to access /profile without being logged in, send them home
  useEffect(() => {
    if (!user) {
      navigate('/');
      return;
    }

    const fetchData = async () => {
      setLoadingContent(true);
      try {
        const [ratings, reviews] = await Promise.all([
          movieApi.getRatingsByUser(user.id),
          reviewApi.getReviewsByUser(user.id)
        ]);
        setRatedMovies(ratings);
        setReviewedMovies(reviews);
      } catch (err) {
        console.error("Failed to fetch profile content:", err);
      } finally {
        setLoadingContent(false);
      }
    };

    fetchData();
  }, [user, navigate]);

  if (!user) return null;

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <div className="movie-page">
      {/* profile hero */}
      <div className="movie-hero">
        <div className="movie-hero-bg" />

        {/* profile picture frame */}
        <div className="movie-poster-frame">
          <img src={testBg} alt="" aria-hidden className="movie-poster-glow opacity-50" />
          <div className="movie-poster-img bg-dark d-flex align-items-center justify-content-center border border-secondary" style={{ height: '100%', borderRadius: '12px' }}>
            <i className="bi bi-person-fill text-warning" style={{ fontSize: '8rem' }}></i>
          </div>
        </div>

        {/* info */}
        <div className="movie-info">
          <div className="movie-genres">
            <span className="movie-genre-badge">Your Profile</span>
            <span className="movie-genre-badge bg-warning text-dark">Member</span>
          </div>

          <h1 className="movie-title mb-4">{user.username || 'User Profile'}</h1>

          <div className="movie-meta-row profile-meta-list gap-3">
            <span className="fs-5">
              <i className="bi bi-envelope-fill text-warning me-3"></i>
              <span className="text-break">{user.email || 'No email provided'}</span>
            </span>
            <span className="fs-5">
              <i className="bi bi-calendar-event text-warning me-3"></i>
              Age: {user.age || 'Unknown'}
            </span>
            <span className="fs-5">
              <i className="bi bi-gender-ambiguous text-warning me-3"></i>
              Gender: {user.genderName || 'Unknown'}
            </span>
          </div>

          <div className="movie-links mt-5">
            <button className="btn-fresh-danger px-4" onClick={handleLogout}>
              <i className="bi bi-box-arrow-right me-2"></i> Logout
            </button>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        
        {/* Playlists */}
        <PlaylistSection user={user} />

        {/* User Content Tracks */}
        {ratedMovies.length > 0 && (
          <div className="mt-4 pt-3 border-top border-secondary border-opacity-10">
             <MovieTrack title="movies you rated" movies={ratedMovies} hideTopBorder={true} />
          </div>
        )}

        {reviewedMovies.length > 0 && (
          <div className="mt-4 pt-3 border-top border-secondary border-opacity-10">
             <MovieTrack title="movies you reviewed" movies={reviewedMovies} hideTopBorder={true} />
          </div>
        )}

        {!loadingContent && ratedMovies.length === 0 && reviewedMovies.length === 0 && (
           <div className="text-center py-5 opacity-50">
              <i className="bi bi-stars fs-1 mb-3 d-block text-warning" />
              <p>Start rating and reviewing movies to see your history here!</p>
           </div>
        )}
      </div>
    </div>
  );
}

export default UserPage;