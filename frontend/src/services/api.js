const BASE_URL = '/api'   // was 'http://localhost:8080/api'

/* export const movieApi = {
  async search(query, signal) {
    const res = await fetch(`${BASE_URL}/movie/search/${encodeURIComponent(query)}`, { signal })
    if (!res.ok) throw new Error(`Search failed: ${res.status}`)
    const data = await res.json()
    return data.map(movie => ({
      id: movie.id,
      title: movie.name,
      posterUrl: movie.posterPath || null,
      year: movie.year || null,
    }))
  },
} */
// Helper to format poster URLs
function formatPosterUrl(path) {
  if (!path || path === "Not Fetched") return null;
  if (path.startsWith('http')) return path;
  if (path.startsWith('//')) return `https:${path}`;
  return `https://${path}`;
}

// Helper to format basic movie data
function formatBasicMovie(movie) {
  if (!movie) return { id: 0, title: 'Unknown' };

  // 1. Poster Path extraction
  const posterPath = movie.posterPath ?? movie.poster_path ?? movie.posterUrl ?? movie.movie?.posterPath;
  
  // 2. ID extraction (Robust check for all DTO variations)
  let rawId = movie.movieId ?? movie.id ?? movie.movie_id ?? movie.movieID ?? movie.movie?.id ?? movie.movie?.movieId;
  
  // Fallback for raw values or strings
  if ((rawId === undefined || rawId === null) && (typeof movie === 'string' || typeof movie === 'number')) {
    rawId = movie;
  }

  const idNum = (rawId !== undefined && rawId !== null) ? Number(rawId) : 0;
  
  const movieObj = movie.movie || movie;
  const genres = movieObj.genres || [];

  return {
    id: idNum,
    title: movieObj.name ?? movieObj.title ?? 'Unknown Movie',
    posterUrl: formatPosterUrl(posterPath),
    year: (movieObj.releaseDate && typeof movieObj.releaseDate === 'string') 
      ? movieObj.releaseDate.substring(0, 4) 
      : (movieObj.year ?? 'Unknown'),
    genres: Array.isArray(genres) ? genres.map(g => {
      if (!g) return 'Movie';
      return typeof g === 'string' ? g : (g.name || 'Movie');
    }).sort() : [],
    genre: (Array.isArray(genres) && genres.length > 0)
      ? (typeof genres[0] === 'string' ? genres[0] : (genres[0]?.name || 'Movie'))
      : 'Movie'
  };
}

const isValidMovie = m => (m.name || m.title) && (m.name !== 'None' && m.title !== 'None');

export const movieApi = {
  async search(query, signal) {
    const res = await fetch(`${BASE_URL}/movie/search/${encodeURIComponent(query)}`, { signal });
    if (!res.ok) throw new Error(`Search failed`);
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async advancedSearch(params = {}, signal) {
    const { title, genre, staff, page = 0, size = 30 } = params;
    let url = `${BASE_URL}/movie/search?title=${encodeURIComponent(title || '')}&page=${page}&size=${size}`;
    if (genre) url += `&genre=${encodeURIComponent(genre)}`;
    if (staff) url += `&staff=${encodeURIComponent(staff)}`;

    const res = await fetch(url, { signal });
    if (!res.ok) {
      if (res.status === 404) return [];
      throw new Error(`Advanced search failed`);
    }
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  // NEW: Fetch paginated movies
  async getMovies(page = 0, size = 30) {
    const res = await fetch(`${BASE_URL}/movie?page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch movies');
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  // NEW: Fetch movies by genre using sequential random letter discovery
  async getMoviesByGenre(genre, page = 0, size = 30) {
    const alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('').sort(() => 0.5 - Math.random());
    const seenIds = new Set();
    const allResults = [];

    // On the first page, we hunt for at least 10 movies
    // If page > 0, we just do one fetch to keep pagination simple
    const maxAttempts = page === 0 ? 10 : 1;

    for (let i = 0; i < maxAttempts; i++) {
      const char = alphabet[i];
      try {
        const res = await fetch(`${BASE_URL}/movie/search?title=${char}&genre=${encodeURIComponent(genre)}&page=${page}&size=${size}`);
        if (res.ok) {
          const data = await res.json();
          if (Array.isArray(data)) {
            data.forEach(m => {
              if (isValidMovie(m)) {
                if (!seenIds.has(m.id)) {
                  seenIds.add(m.id);
                  allResults.push(m);
                }
              }
            });
          }
        }
      } catch (err) {
        console.error(`Genre hunt failed for letter ${char}:`, err);
      }

      // If we've reached our threshold of 20 movies, we can stop hunting
      if (allResults.length >= 20) break;
    }

    return allResults.map(formatBasicMovie);
  },

  // NEW: Fetch random movies
  async getRandomMovies() {
    const res = await fetch(`${BASE_URL}/movie/random`);
    if (!res.ok) throw new Error('Failed to fetch random movies');
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  // NEW: Fetch full movie details
  async getById(id) {
    const res = await fetch(`${BASE_URL}/movie/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch movie details: ${res.status}`);
    const m = await res.json();

    const posterPath = m.posterPath || m.poster_path || m.posterUrl;

    return {
      id: m.id,
      title: m.name || m.title,
      posterUrl: formatPosterUrl(posterPath),
      year: m.releaseDate ? m.releaseDate.substring(0, 4) : null,
      duration: m.duration,
      trailerUrl: m.trailer && m.trailer !== "Not Fetched" ? m.trailer : null,
      youtubeUrl: m.youtubeMovie && m.youtubeMovie !== "Not Fetched" ? `https://www.youtube.com/watch?v=${m.youtubeMovie}` : null,
      // Map nested arrays to flat string arrays or simple objects
      genres: m.genres ? m.genres.map(g => g.name).sort() : [],
      actors: m.actors ? m.actors.sort((a, b) => a.name.localeCompare(b.name)) : [],
      directors: m.directors ? m.directors.sort((a, b) => a.name.localeCompare(b.name)) : [],
      description: m.description,
      country: m.productionCountries && m.productionCountries.length > 0 
        ? m.productionCountries.map(c => c.name).sort().join(', ') 
        : null,
      // Backend already provides the average on a 1-5 scale
      rating: m.rate ? Number(m.rate).toFixed(1) : null
    };
  },

  async rate(movieId, rating, token) {
    const res = await fetch(`${BASE_URL}/rate/secure`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId, rate: Number(rating) })
    });
    if (!res.ok) {
      const errText = await res.text().catch(() => 'Unknown error');
      console.error('Rating error from backend:', errText);
      throw new Error(errText || 'Failed to save rating');
    }
    return res.text();
  },

  async getUserRating(userId, movieId) {
    const res = await fetch(`${BASE_URL}/rate?userid=${userId}`);
    if (!res.ok) return 0;
    const ratings = await res.json();
    const movieRating = ratings.find(r => r.movieId === Number(movieId));
    return movieRating ? movieRating.rating : 0;
  },

  async getRatingsByUser(userId) {
    const res = await fetch(`${BASE_URL}/rate?userid=${userId}`);
    if (!res.ok) throw new Error('Failed to fetch user ratings');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  async deleteRate(movieId, token) {
    const res = await fetch(`${BASE_URL}/rate/secure`, {
      method: 'DELETE',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId) })
    });
    if (!res.ok) {
      const errText = await res.text().catch(() => 'Unknown error');
      throw new Error(errText || 'Failed to delete rating');
    }
    return true;
  }
};

export const staffApi = {
  async search() {
    // Search by actor name is not supported by the current backend API.
    return [];
  },

  async getById(id) {
    const res = await fetch(`${BASE_URL}/staff/${id}`);
    if (!res.ok) throw new Error('Failed to fetch staff details');
    const s = await res.json();
    return {
      ...s,
      birthCountry: s.birthCountry ? s.birthCountry.name : null,
      gender: s.gender ? s.gender.name : null,
      playedMovies: s.playedMovies ? s.playedMovies.map(formatBasicMovie) : [],
      directedMovies: s.directedMovies ? s.directedMovies.map(formatBasicMovie) : []
    };
  }
};

export const authApi = {
  async login(credentials) {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData.message || errData.error || `Login failed: ${res.status}`);
    }
    return res.json();
  },

  async register(userData) {
    const res = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData.message || errData.error || `Registration failed: ${res.status}`);
    }
    // Backend returns a plain string "User is created", so we don't call .json()
    return res.text();
  },

  async logout(refreshToken) {
    await fetch(`${BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
  },

  async getMe(token) {
    const res = await fetch(`${BASE_URL}/auth/me`, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to fetch user data');
    const data = await res.json();
    // Map backend UserDto to our frontend-friendly user object
    return {
      id: data.id,
      username: data.username,
      email: data.email,
      age: data.age,
      genderName: data.gender,
      roles: [] // UserDto doesn't currently provide roles
    };
  },

  async getUserPublicById(userId) {
    const idNum = parseInt(userId);
    if (isNaN(idNum) || idNum === 0) throw new Error('Invalid User ID');

    const res = await fetch(`${BASE_URL}/auth/user/${idNum}`);
    if (!res.ok) {
      if (res.status === 404) {
         const errText = await res.text().catch(() => '');
         throw new Error(errText || 'User not found.');
      }
      if (res.status >= 500) {
         throw new Error('Server error: The backend failed to load this profile.');
      }
      throw new Error(`Profile unavailable (Status: ${res.status})`);
    }
    const data = await res.json();
    return {
      id: data.id,
      username: data.username,
      age: data.age,
      genderName: data.gender,
      playlists: Array.isArray(data.playlists) ? data.playlists.map(pl => ({
        ...pl,
        movies: Array.isArray(pl.movies) ? pl.movies.map(formatBasicMovie) : []
      })) : [],
      ratedMovies: Array.isArray(data.ratedMovies) ? data.ratedMovies.map(formatBasicMovie) : [],
      reviewedMovies: Array.isArray(data.reviewedMovies) ? data.reviewedMovies.map(formatBasicMovie) : []
    };
  }
  };

export const genreApi = {
  async getAll() {
    const res = await fetch(`${BASE_URL}/genre`);
    if (!res.ok) throw new Error('Failed to fetch genres');
    return res.json();
  }
};

export const playlistApi = {
  async getByOwner(ownerId, token) {
    const res = await fetch(`${BASE_URL}/playlist?ownerId=${ownerId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to fetch playlists');
    return res.json();
  },

  async getById(playlistId, token) {
    const res = await fetch(`${BASE_URL}/playlist/${playlistId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to fetch playlist details');
    const data = await res.json();
    return {
      ...data,
      movies: data.movies ? data.movies.map(formatBasicMovie) : []
    };
  },

  async create(name, isPublic, token) {
    const res = await fetch(`${BASE_URL}/playlist/secure`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ name, isPrivate: !isPublic })
    });
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData.message || errData.error || `Failed to create playlist: ${res.status}`);
    }
    return res.json();
  },

  async delete(playlistId, token) {
    const res = await fetch(`${BASE_URL}/playlist/secure/${playlistId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to delete playlist');
  },

  async rename(playlistId, name, token) {
    const res = await fetch(`${BASE_URL}/playlist/secure/${playlistId}/name`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ name })
    });
    if (!res.ok) throw new Error('Failed to rename playlist');
    return res.json();
  },

  async changeVisibility(playlistId, isPublic, token) {
    const res = await fetch(`${BASE_URL}/playlist/secure/${playlistId}/visibility`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ isPrivate: !isPublic })
    });
    if (!res.ok) throw new Error('Failed to change visibility');
    return res.json();
  },

  async addMovie(playlistId, movieId, token) {
    const res = await fetch(`${BASE_URL}/playlist/secure/${playlistId}/movies`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId) })
    });
    if (!res.ok) throw new Error('Failed to add movie');
    return res.json();
  },

  async removeMovie(playlistId, movieId, token) {
    const pId = Number(playlistId);
    const mId = Number(movieId);
    const url = `${BASE_URL}/playlist/secure/${pId}/movies/${mId}`;
    
    const res = await fetch(url, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) {
      const errBody = await res.text().catch(() => 'No error body');
      let message = errBody;
      try {
        const json = JSON.parse(errBody);
        message = json.message || message;
      } catch { /* not json */ }
      
      throw new Error(`Server Error (${res.status}): ${message}`);
    }

    try {
      const data = await res.json();
      return {
        ...data,
        movies: data.movies ? data.movies.map(formatBasicMovie) : []
      };
    } catch {
      return null;
    }
  }
};

export const reviewApi = {
  async getAllByMovie(movieId) {
    const res = await fetch(`${BASE_URL}/review/${movieId}`);
    if (!res.ok) throw new Error('Failed to fetch reviews');
    return res.json();
  },

  async getReviewsByUser(userId) {
    const res = await fetch(`${BASE_URL}/review?userid=${userId}`);
    if (!res.ok) throw new Error('Failed to fetch user reviews');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  async postReview(movieId, reviewText, token) {
    const res = await fetch(`${BASE_URL}/review/secure`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId), rate: reviewText })
    });
    if (!res.ok) {
      const errText = await res.text().catch(() => 'Unknown error');
      throw new Error(errText || 'Failed to save review');
    }
    return res.text();
  },

  async deleteReview(movieId, token) {
    const res = await fetch(`${BASE_URL}/review/secure`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId) })
    });
    if (!res.ok) {
      const errText = await res.text().catch(() => 'Unknown error');
      throw new Error(errText || 'Failed to delete review');
    }
    return true;
  }
};