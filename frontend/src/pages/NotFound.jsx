import { Link, useNavigate } from 'react-router-dom';

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="movie-page">
      <div className="movie-hero" style={{ minHeight: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="movie-hero-bg" />
        <div className="container text-center animate-hero-reveal" style={{ zIndex: 1 }}>
          <i className="bi bi-camera-reels text-warning mb-4" style={{ fontSize: '6rem', opacity: 0.8 }} />
          <h1 className="text-light fw-black uppercase tracking-widest mb-3" style={{ fontSize: 'clamp(3rem, 8vw, 5rem)' }}>
            404
          </h1>
          <h2 className="text-light fw-bold mb-4">Lost in the Archives</h2>
          <p className="text-secondary fs-5 mb-5 mx-auto" style={{ maxWidth: '600px', lineHeight: 1.6 }}>
            The page or movie you're looking for seems to have been cut from the final edit. Let's get you back to the main feature.
          </p>
          <div className="d-flex justify-content-center gap-3 flex-wrap">
            <button onClick={() => navigate(-1)} className="btn-fresh-secondary px-4 py-2">
              <i className="bi bi-arrow-left me-2" /> Go Back
            </button>
            <Link to="/" className="btn-fresh-primary px-4 py-2">
              Back to Home <i className="bi bi-house-door ms-2" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default NotFound;