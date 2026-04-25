import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

function AuthModal({ show, onHide }) {
  const [isLogin, setIsLogin] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const { showToast } = useToast();
  const [shouldRender, setRender] = useState(show);

  useEffect(() => {
    if (show) {
      setRender(true);
      document.body.classList.add('modal-open-lock');
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
    } else {
      document.body.classList.remove('modal-open-lock');
    }
    return () => {
      document.body.classList.remove('modal-open-lock');
    };
  }, [show]);

  const onAnimationEnd = () => {
    if (!show) setRender(false);
  };

  const [formData, setFormData] = useState({
    email: '',
    username: '',
    genderName: 'None', 
    age: '',
    password: '',
    confirmPassword: ''
  });

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
        showToast(`Welcome back, ${formData.username}!`);
      } else {
        await register({
          email: formData.email,
          username: formData.username,
          genderName: formData.genderName,
          age: parseInt(formData.age),
          password: formData.password
        });
        showToast("Account created successfully! Please sign in.");
        setIsLogin(true);
        return;
      }
      onHide();
    } catch (err) {
      setError(err.message || 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!shouldRender) return null;

  return (
    <div 
      className={`custom-modal-overlay ${show ? 'animate-fade-in' : 'animate-fade-out'}`} 
      onAnimationEnd={onAnimationEnd}
      onClick={onHide}
    >
      <div className="custom-modal-container" onClick={e => e.stopPropagation()}>
        <div className="custom-modal-glow" />
        
        <div className="custom-modal-content">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="custom-modal-title">
              {isLogin ? 'Welcome Back' : 'Create Account'}
            </h2>
            <button className="btn-close-custom" onClick={onHide}>
              <i className="bi bi-x-lg" />
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            {error && <div className="custom-modal-error mb-4">{error}</div>}
            
            {!isLogin && (
              <div className="custom-input-group mb-3">
                <label>Email Address</label>
                <input 
                  type="email" 
                  name="email" 
                  placeholder="name@example.com" 
                  required 
                  value={formData.email} 
                  onChange={handleChange} 
                />
              </div>
            )}

            <div className="custom-input-group mb-3">
              <label>Username</label>
              <input 
                type="text" 
                name="username" 
                placeholder="potatoFan" 
                required 
                value={formData.username} 
                onChange={handleChange} 
              />
            </div>

            {!isLogin && (
              <div className="row g-3 mb-3">
                <div className="col-7">
                  <div className="custom-input-group">
                    <label>Gender</label>
                    <select 
                      name="genderName" 
                      className="custom-select-minimal"
                      required 
                      value={formData.genderName} 
                      onChange={handleChange}
                    >
                      <option value="None" disabled>Select</option>
                      <option value="male">Male</option>
                      <option value="female">Female</option>
                      <option value="transfeminine">Transfeminine</option>
                      <option value="transmasculine">Transmasculine</option>
                    </select>
                  </div>
                </div>
                <div className="col-5">
                  <div className="custom-input-group">
                    <label>Age</label>
                    <input 
                      type="number" 
                      name="age" 
                      placeholder="21" 
                      required 
                      min="1" 
                      value={formData.age} 
                      onChange={handleChange} 
                    />
                  </div>
                </div>
              </div>
            )}

            <div className="custom-input-group mb-3">
              <label>Password</label>
              <input 
                type="password" 
                name="password" 
                placeholder="••••••••" 
                required 
                value={formData.password} 
                onChange={handleChange} 
              />
            </div>

            {!isLogin && (
              <div className="custom-input-group mb-4">
                <label>Confirm Password</label>
                <input 
                  type="password" 
                  name="confirmPassword" 
                  placeholder="••••••••" 
                  required 
                  value={formData.confirmPassword} 
                  onChange={handleChange} 
                />
              </div>
            )}

            <button type="submit" className="btn-fresh-primary w-100 mt-2" disabled={loading}>
              {loading ? (
                <><span className="spinner-border spinner-border-sm me-2" /> Working...</>
              ) : (isLogin ? 'Sign In' : 'Join Now')}
            </button>
          </form>

          <div className="text-center mt-4">
            <p className="text-secondary smaller">
              {isLogin ? "New to freshPotatoes? " : "Already have an account? "}
              <button 
                type="button" 
                className="btn btn-link text-warning p-0 align-baseline text-decoration-none fw-bold" 
                onClick={() => {
                  setIsLogin(!isLogin);
                  setError('');
                }}
              >
                {isLogin ? 'Create one' : 'Sign in'}
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuthModal;