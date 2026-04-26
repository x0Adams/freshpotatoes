import { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { movieApi } from '../services/api';

function ModifyMovieModal({ show, onHide, movie, onSuccess }) {
  const [formData, setFormData] = useState({
    name: '',
    posterPath: '',
    duration: 0,
    releaseDate: '',
    wikipediaTitle: '',
    youtubeMovie: '',
    trailer: ''
  });
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState('');
  const [shouldRender, setRender] = useState(show);

  useEffect(() => {
    if (show) {
      setRender(true);
      document.body.classList.add('modal-open-lock');
      if (movie) {
        // Fetch full details to pre-fill the form accurately
        movieApi.getById(movie.id)
          .then(fullMovie => {
            setFormData({
              name: fullMovie.title || '',
              posterPath: fullMovie.posterUrl ? fullMovie.posterUrl.replace('https://', '') : '',
              duration: fullMovie.duration || 0,
              releaseDate: fullMovie.releaseDate || '',
              wikipediaTitle: fullMovie.description || '', // Mapping description back to wiki title as per our getById logic
              youtubeMovie: fullMovie.youtubeMovie || '',
              trailer: fullMovie.trailerUrl || ''
            });
          })
          .catch(err => {
            console.error("Failed to fetch full movie details for editing:", err);
            // Fallback to basic data if full fetch fails
            setFormData({
              name: movie.title || '',
              posterPath: movie.posterUrl ? movie.posterUrl.replace('https://', '') : '',
              duration: 0,
              releaseDate: movie.year ? `${movie.year}-01-01` : '',
              wikipediaTitle: '',
              youtubeMovie: '',
              trailer: ''
            });
          });
      }
      setError('');
    } else {
      document.body.classList.remove('modal-open-lock');
    }
    return () => {
      document.body.classList.remove('modal-open-lock');
    };
  }, [show, movie]);

  const onAnimationEnd = () => {
    if (!show) setRender(false);
  };

  if (!shouldRender) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'duration' ? parseInt(value) || 0 : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsProcessing(true);
    setError('');
    const token = localStorage.getItem('accessToken');

    try {
      await movieApi.adminModifyMovie(movie.id, formData, token);
      if (onSuccess) onSuccess();
      onHide();
    } catch (err) {
      setError(err.message || 'Failed to update movie');
    } finally {
      setIsProcessing(false);
    }
  };

  return createPortal(
    <div 
      className={`custom-modal-overlay ${show ? 'animate-fade-in' : 'animate-fade-out'}`} 
      onAnimationEnd={onAnimationEnd}
      onClick={onHide}
    >
      <div className="custom-modal-container" style={{ maxWidth: '600px' }} onClick={e => e.stopPropagation()}>
        <div className="custom-modal-glow" />
        
        <div className="custom-modal-content">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="custom-modal-title">Modify Movie</h2>
            <button className="btn-close-custom" onClick={onHide}>
              <i className="bi bi-x-lg" />
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            {error && <div className="custom-modal-error mb-3">{error}</div>}
            
            <div className="row g-3">
              <div className="col-md-12">
                <div className="custom-input-group">
                  <label>Movie Title</label>
                  <input type="text" name="name" value={formData.name} onChange={handleChange} required />
                </div>
              </div>
              
              <div className="col-md-12">
                <div className="custom-input-group">
                  <label>Poster Path (URL without https://)</label>
                  <input type="text" name="posterPath" value={formData.posterPath} onChange={handleChange} required />
                </div>
              </div>

              <div className="col-md-6">
                <div className="custom-input-group">
                  <label>Duration (minutes)</label>
                  <input type="number" name="duration" value={formData.duration} onChange={handleChange} required />
                </div>
              </div>

              <div className="col-md-6">
                <div className="custom-input-group">
                  <label>Release Date</label>
                  <input type="date" name="releaseDate" value={formData.releaseDate} onChange={handleChange} required />
                </div>
              </div>

              <div className="col-md-12">
                <div className="custom-input-group">
                  <label>Wikipedia Title</label>
                  <input type="text" name="wikipediaTitle" value={formData.wikipediaTitle} onChange={handleChange} required />
                </div>
              </div>

              <div className="col-md-6">
                <div className="custom-input-group">
                  <label>YouTube ID</label>
                  <input type="text" name="youtubeMovie" value={formData.youtubeMovie} onChange={handleChange} required />
                </div>
              </div>

              <div className="col-md-6">
                <div className="custom-input-group">
                  <label>Trailer URL</label>
                  <input type="text" name="trailer" value={formData.trailer} onChange={handleChange} required />
                </div>
              </div>
            </div>

            <div className="d-flex gap-3 mt-4">
              <button type="button" className="btn-fresh-secondary w-50" onClick={onHide} disabled={isProcessing}>
                Cancel
              </button>
              <button type="submit" className="btn-fresh-primary w-50" disabled={isProcessing}>
                {isProcessing ? 'Updating...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>,
    document.body
  );
}

export default ModifyMovieModal;