import React from 'react';

function TrailerModal({ show, onHide, videoId, title }) {
  if (!show || !videoId) return null;

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.9)', zIndex: 1060 }}>
      <div className="modal-dialog modal-dialog-centered modal-xl">
        <div className="modal-content bg-black border-0 shadow-lg">
          <div className="modal-header border-0 pb-0">
            <h5 className="modal-title text-light fw-bold">{title} - Official Trailer</h5>
            <button type="button" className="btn-close btn-close-white" onClick={onHide} aria-label="Close"></button>
          </div>
          <div className="modal-body p-0">
            <div className="ratio ratio-16x9">
              <iframe
                src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                title={`${title} Trailer`}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              ></iframe>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TrailerModal;