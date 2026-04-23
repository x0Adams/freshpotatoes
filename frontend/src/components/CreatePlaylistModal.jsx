import { useState } from 'react';
import { playlistApi } from '../services/api';

function CreatePlaylistModal({ show, onHide, onSuccess }) {
  const [name, setName] = useState('');
  const [isPublic, setIsPublic] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState('');

  if (!show) return null;

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
      if (onSuccess) onSuccess(newPlaylist);
      onHide();
    } catch (err) {
      setError(err.message || 'Failed to create playlist');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.8)', zIndex: 1070 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content bg-dark border-secondary shadow-lg">
          <div className="modal-header border-secondary">
            <h5 className="modal-title text-warning fw-bold">Create New Playlist</h5>
            <button type="button" className="btn-close btn-close-white" onClick={onHide} aria-label="Close"></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body py-4">
              {error && <div className="alert alert-danger small py-2">{error}</div>}
              
              <div className="mb-4">
                <label className="text-secondary small mb-2 fw-semibold uppercase tracking-wider">Playlist Name</label>
                <input
                  type="text"
                  className="form-control bg-transparent text-light border-secondary py-2"
                  placeholder="e.g. My Favorite Sci-Fi"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  autoFocus
                  disabled={isProcessing}
                />
              </div>

              <div className="form-check form-switch mb-2">
                <input
                  className="form-check-input"
                  type="checkbox"
                  id="modalIsPublic"
                  checked={isPublic}
                  onChange={(e) => setIsPublic(e.target.checked)}
                  disabled={isProcessing}
                />
                <label className="form-check-label text-light small ms-2" htmlFor="modalIsPublic">
                  Make this playlist public
                </label>
              </div>
              <p className="text-secondary smaller mb-0">Public playlists can be seen by other users on your public profile.</p>
            </div>
            <div className="modal-footer border-secondary">
              <button type="button" className="btn btn-outline-secondary btn-sm px-4" onClick={onHide} disabled={isProcessing}>
                Cancel
              </button>
              <button type="submit" className="btn btn-warning btn-sm px-4 fw-bold text-dark" disabled={isProcessing || !name.trim()}>
                {isProcessing ? (
                  <><span className="spinner-border spinner-border-sm me-2" /> Creating...</>
                ) : 'Create Playlist'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreatePlaylistModal;