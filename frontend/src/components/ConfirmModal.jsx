import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

function ConfirmModal({ show, onHide, onConfirm, title, message, confirmText = "Confirm", isDanger = false }) {
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

  return createPortal(
    <div 
      className={`custom-modal-overlay ${show ? 'animate-fade-in' : 'animate-fade-out'}`} 
      onAnimationEnd={onAnimationEnd}
      onClick={onHide}
    >
      <div className="custom-modal-container" style={{ maxWidth: '400px' }} onClick={e => e.stopPropagation()}>
        <div className="custom-modal-glow" />
        
        <div className="custom-modal-content text-center py-5">
          <div className="mb-4">
             <i className={`bi bi-${isDanger ? 'exclamation-triangle-fill text-danger' : 'info-circle-fill text-warning'}`} style={{ fontSize: '3.5rem' }} />
          </div>
          
          <h2 className="custom-modal-title mb-3">{title}</h2>
          <p className="text-secondary mb-5 px-3">{message}</p>

          <div className="d-flex gap-3 mt-2 px-3">
            <button 
              type="button" 
              className="btn-fresh-secondary w-50" 
              onClick={onHide}
            >
              Cancel
            </button>
            <button 
              type="button" 
              className={isDanger ? "btn-fresh-danger w-50" : "btn-fresh-primary w-50"}
              onClick={() => {
                onConfirm();
                onHide();
              }}
            >
              {confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}

export default ConfirmModal;