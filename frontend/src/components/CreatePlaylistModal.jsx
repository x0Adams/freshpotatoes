import { useState, useEffect } from 'react';
import { playlistApi } from '../services/api';
import { useToast } from '../context/ToastContext';

function CreatePlaylistModal({ show, onHide, onSuccess }) {
  const { showToast } = useToast();
  const [name, setName] = useState('');
  const [isPublic, setIsPublic] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState('');
  const [shouldRender, setRender] = useState(show);

  useEffect(() => {
    if (show) {
      setRender(true);
      document.body.classList.add('modal-open-lock');
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

  if (!shouldRender) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || isProcessing) return;

    setIsProcessing(true);
    setError('');
    const token = localStorage.getItem('accessToken');

    try {
      const newPlaylist = await playlistApi.create(name.trim(), isPublic, token);
      setName('');
      setIsPublic(false);
      showToast("Collection created!");
      if (onSuccess) onSuccess(newPlaylist);
      onHide();
    } catch (err) {
      setError(err.message || 'Failed to create playlist');
    } finally {
      setIsProcessing(false);
    }
  };

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
            <h2 className="custom-modal-title">Create Collection</h2>
            <button className="btn-close-custom" onClick={onHide}>
              <i className="bi bi-x-lg" />
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            {error && <div className="custom-modal-error mb-3">{error}</div>}
            
            <div className="custom-input-group mb-4">
              <label>Collection Name</label>
              <input
                type="text"
                placeholder="e.g. Midnight Thrillers"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoFocus
                disabled={isProcessing}
              />
            </div>

            <div className="custom-checkbox-row mb-5">
              <div className="d-flex align-items-center gap-3">
                 <div className="custom-switch">
                    <input
                      type="checkbox"
                      id="modalIsPublic"
                      checked={isPublic}
                      onChange={(e) => setIsPublic(e.target.checked)}
                      disabled={isProcessing}
                    />
                    <label htmlFor="modalIsPublic" />
                 </div>
                 <div>
                    <div className="text-light fw-bold smaller uppercase tracking-widest">Public Access</div>
                    <div className="text-secondary smallest">Allow others to see this collection on your profile</div>
                 </div>
              </div>
            </div>

            <div className="d-flex gap-3 mt-2">
              <button 
                type="button" 
                className="btn-fresh-secondary w-50" 
                onClick={onHide} 
                disabled={isProcessing}
              >
                Dismiss
              </button>
              <button 
                type="submit" 
                className="btn-fresh-primary w-50" 
                disabled={isProcessing || !name.trim()}
              >
                {isProcessing ? (
                  <><span className="spinner-border spinner-border-sm me-2" /> Creating...</>
                ) : 'Create Now'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreatePlaylistModal;