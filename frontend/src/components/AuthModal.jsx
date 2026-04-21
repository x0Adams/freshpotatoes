import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

function AuthModal({ show, onHide }) {
  const [isLogin, setIsLogin] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    username: '',
    genderName: 'None', 
    age: '',
    password: '',
    confirmPassword: ''
  });

  // Clear state when modal closes/opens
  useEffect(() => {
    if (show) {
      setError('');
      setLoading(false);
      setFormData({
        email: '',
        username: '',
        genderName: 'None', 
        age: '',
        password: '',
        confirmPassword: ''
      });
    }
  }, [show]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!isLogin && formData.password !== formData.confirmPassword) {
      setError("Passwords do not match!");
      return;
    }

    setLoading(true);

    try {
      if (isLogin) {
        await login(formData.username, formData.password);
      } else {
        await register({
          email: formData.email,
          username: formData.username,
          genderName: formData.genderName,
          age: parseInt(formData.age),
          password: formData.password
        });
      }
      onHide(); // Close modal on success
    } catch (err) {
      setError(err.message || 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!show) return null;

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.8)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content bg-dark text-light border-secondary">
          <div className="modal-header border-secondary">
            <h5 className="modal-title text-light fw-bold">
              {isLogin ? (
                <>Login to fresh<span className="text-warning">Potatoes</span></>
              ) : (
                'Create Account'
              )}
            </h5>
            <button type="button" className="btn-close btn-close-white" onClick={onHide}></button>
          </div>
          <div className="modal-body">
            {error && <div className="alert alert-danger py-2">{error}</div>}
            
            <form onSubmit={handleSubmit}>
              {!isLogin && (
                <div className="mb-3">
                  <input 
                    type="email" 
                    name="email" 
                    className="form-control bg-transparent text-light border-secondary" 
                    placeholder="Email Address" 
                    required 
                    value={formData.email} 
                    onChange={handleChange} 
                  />
                </div>
              )}

              <div className="mb-3">
                <input 
                  type="text" 
                  name="username" 
                  className="form-control bg-transparent text-light border-secondary" 
                  placeholder="Username" 
                  required 
                  value={formData.username} 
                  onChange={handleChange} 
                />
              </div>

              {!isLogin && (
                <div className="d-flex gap-2 mb-3">
                  <select 
                    name="genderName" 
                    className="form-select bg-transparent text-light border-secondary" 
                    required 
                    value={formData.genderName} 
                    onChange={handleChange}
                  >
                    <option value="None" disabled>Select Gender</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="transfeminine">Transfeminine</option>
                    <option value="transmasculine">Transmasculine</option>
                  </select>

                  <input 
                    type="number" 
                    name="age" 
                    className="form-control bg-transparent text-light border-secondary" 
                    placeholder="Age" 
                    required 
                    min="1" 
                    value={formData.age} 
                    onChange={handleChange} 
                  />
                </div>
              )}

              <div className="mb-3">
                <input 
                  type="password" 
                  name="password" 
                  className="form-control bg-transparent text-light border-secondary" 
                  placeholder="Password" 
                  required 
                  value={formData.password} 
                  onChange={handleChange} 
                />
              </div>

              {!isLogin && (
                <div className="mb-4">
                  <input 
                    type="password" 
                    name="confirmPassword" 
                    className="form-control bg-transparent text-light border-secondary" 
                    placeholder="Confirm Password" 
                    required 
                    value={formData.confirmPassword} 
                    onChange={handleChange} 
                  />
                </div>
              )}

              <button type="submit" className="btn btn-warning w-100 fw-bold mt-2" disabled={loading}>
                {loading ? 'Processing...' : (isLogin ? 'Log In' : 'Sign Up')}
              </button>
            </form>
          </div>
          <div className="modal-footer border-secondary justify-content-center">
            <p className="text-secondary m-0">
              {isLogin ? "Don't have an account? " : "Already have an account? "}
              <button 
                type="button" 
                className="btn btn-link text-warning p-0 align-baseline text-decoration-none fw-bold" 
                onClick={() => {
                  setIsLogin(!isLogin);
                  setError('');
                }}
              >
                {isLogin ? 'Sign Up' : 'Log In'}
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuthModal;