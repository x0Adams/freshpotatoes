import React, { useEffect, useState } from 'react';

function TrailerModal({ show, onHide, videoId, title }) {
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

  if (!shouldRender || !videoId) return null;

  return (
    <div 
      className={`custom-modal-overlay ${show ? 'animate-fade-in' : 'animate-fade-out'}`} 
      onAnimationEnd={onAnimationEnd}
      onClick={onHide}
    >
      <div className="custom-modal-container" style={{ maxWidth: '1100px', width: '95%' }} onClick={e => e.stopPropagation()}>
        <div className="custom-modal-glow" />
        
        <div className="custom-modal-content p-0 overflow-hidden d-flex flex-column">
          <div className="d-flex justify-content-between align-items-center px-4 py-3 border-bottom border-secondary border-opacity-10 bg-black">
            <h2 className="custom-modal-title fs-6 text-truncate pe-4" style={{ opacity: 0.8 }}>{title} - Official Trailer</h2>
            <button className="btn-close-custom" onClick={onHide} style={{ padding: '4px' }}>
              <i className="bi bi-x-lg" />
            </button>
          </div>

          <div className="flex-grow-1 bg-black">
            <div className="ratio ratio-16x9">
              <iframe
                src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                title={`${title} Trailer`}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
                style={{ border: 'none' }}
              ></iframe>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TrailerModal;