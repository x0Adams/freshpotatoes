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
      <i className="bi bi-film" />
      <span>{title}</span>
    </div>
  )
}

export default MoviePoster