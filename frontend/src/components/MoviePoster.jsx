import { useState } from 'react';

function MoviePoster({ posterUrl, title, className, style }) {
  const [hasError, setHasError] = useState(false);

  if (posterUrl && !hasError) {
    return (
      <img
        src={posterUrl}
        alt={title}
        className={className}
        style={style}
        onError={() => setHasError(true)}
      />
    )
  }

  return (
    <div className={`movie-poster-fallback ${className || ''}`} style={style}>
      <i className="bi bi-film fallback-film-icon" aria-hidden="true" />
      <div className="fallback-title-wrap">
        <span className="fallback-brand">freshPotatoes</span>
        <span className="fallback-title">{title}</span>
      </div>
    </div>
  )
}

export default MoviePoster