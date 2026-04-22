import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useEffect } from 'react';
import PlaylistSection from '../components/PlaylistSection';
import testBg from '../assets/test_bg.jpg';

function UserPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // If someone tries to access /profile without being logged in, send them home
  useEffect(() => {
    if (!user) navigate('/');
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
            <span className="movie-genre-badge">User Profile</span>
            <span className="movie-genre-badge bg-warning text-dark">Member</span>
          </div>

          <h1 className="movie-title mb-4">{user.username || 'User Profile'}</h1>

          <div className="movie-meta-row flex-column align-items-start gap-3">
            <span className="fs-5">
              <i className="bi bi-envelope-fill text-warning me-3"></i>
              {user.email || 'No email provided'}
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
            <button className="btn btn-outline-danger btn-sm px-4 fw-bold" onClick={handleLogout}>
              <i className="bi bi-box-arrow-right me-2"></i> Logout
            </button>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        <PlaylistSection user={user} />
      </div>
    </div>
  );
}

export default UserPage;