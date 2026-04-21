import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';

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
    <div className="container" style={{ minHeight: '100vh', paddingTop: '120px' }}>
      <div className="card bg-dark border-secondary p-5 mx-auto" style={{ maxWidth: '600px' }}>
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h1 className="text-warning fw-bold m-0">
            <i className="bi bi-person-circle me-3"></i>
            {user.username || 'User Profile'}
          </h1>
          <button className="btn btn-outline-danger" onClick={handleLogout}>
            <i className="bi bi-box-arrow-right me-2"></i> Logout
          </button>
        </div>

        <hr className="border-secondary mb-4" />

        <div className="text-light">
          {/* Note: Update these fields based on what your /me endpoint actually returns */}
          <p><strong>Email:</strong> {user.email || 'Hidden'}</p>
          <p><strong>Age:</strong> {user.age || 'Unknown'}</p>
          <p><strong>Gender:</strong> {user.genderName || 'Unknown'}</p>
        </div>
      </div>
    </div>
  );
}

export default UserPage;