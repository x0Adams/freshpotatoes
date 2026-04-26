const BASE_URL = '/api';

function formatPosterUrl(path) {
  if (!path || path === 'Not Fetched' || path === 'Not%20Fetched') return null;
  if (path.startsWith('http')) return path;
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

// Helper to decode JWT without a library
function decodeToken(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (_) {
    return null;
  }
}

// Helper to check if a token is expired locally
function isTokenExpired(token) {
  if (!token) return true;
  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return true;
  // Check if expiration is in the past (with a 5s buffer)
  return (decoded.exp * 1000) < (Date.now() + 5000);
}

/**
 * SMART FETCH WRAPPER
 * Handles automatic token refresh on 401 Unauthorized
 */
async function smartFetch(url, options = {}) {
  const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/register');
  
  // If it's a secure request, check token before sending
  const token = localStorage.getItem('accessToken');
  if (!isAuthRequest && options.headers?.Authorization && isTokenExpired(token)) {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        const refreshRes = await fetch(`${BASE_URL}/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });

        if (refreshRes.ok) {
          const tokens = await refreshRes.json();
          localStorage.setItem('accessToken', tokens.jwtToken);
          localStorage.setItem('refreshToken', tokens.refreshToken);

          if (options.headers) {
            options.headers['Authorization'] = `Bearer ${tokens.jwtToken}`;
          }
        } else {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          return refreshRes; // Return the 401 from refresh
        }
      } catch (err) {
        console.error("Auto-refresh failed during pre-check:", err);
      }
    }
  }

  let res = await fetch(url, options);

  // If unauthorized, try to refresh ONLY IF we have a refresh token
  if (res.status === 401 && !isAuthRequest) {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        const refreshRes = await fetch(`${BASE_URL}/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });

        if (refreshRes.ok) {
          const tokens = await refreshRes.json();
          localStorage.setItem('accessToken', tokens.jwtToken);
          localStorage.setItem('refreshToken', tokens.refreshToken);

          const newOptions = { ...options };
          const newHeaders = { ...(newOptions.headers || {}) };
          newHeaders['Authorization'] = `Bearer ${tokens.jwtToken}`;
          newOptions.headers = newHeaders;
          
          return fetch(url, newOptions);
        } else {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      } catch (err) {
        console.error("Auto-refresh failed:", err);
      }
    }
  }

  return res;
}

export const movieApi = {
  async search(query, signal) {
    const res = await smartFetch(`${BASE_URL}/movie/search/${encodeURIComponent(query)}`, { signal });
    if (!res.ok) throw new Error(`Search failed`);
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async advancedSearch(params = {}, signal) {
    const { title, genre, staff, page = 0, size = 30 } = params;
    let url = `${BASE_URL}/movie/search?title=${encodeURIComponent(title || '')}&page=${page}&size=${size}`;
    if (genre) url += `&genre=${encodeURIComponent(genre)}`;
    if (staff) url += `&staff=${encodeURIComponent(staff)}`;

    const res = await smartFetch(url, { signal });
    if (!res.ok) {
      if (res.status === 404) return [];
      throw new Error(`Advanced search failed`);
    }
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async getMovies(page = 0, size = 30) {
    const res = await smartFetch(`${BASE_URL}/movie?page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch movies');
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async getMoviesByGenre(genre, page = 0, size = 30) {
    const res = await smartFetch(`${BASE_URL}/movie/genre/${encodeURIComponent(genre)}?page=${page}&size=${size}`);
    if (!res.ok) {
      if (res.status === 404) return [];
      throw new Error(`Failed to fetch movies for genre: ${genre}`);
    }
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async getRandomMovies() {
    const res = await smartFetch(`${BASE_URL}/movie/random`);
    if (!res.ok) throw new Error('Failed to fetch random movies');
    const data = await res.json();
    return data.filter(isValidMovie).map(formatBasicMovie);
  },

  async getById(id) {
    const res = await smartFetch(`${BASE_URL}/movie/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch movie details: ${res.status}`);
    const m = await res.json();
    return {
      id: m.id,
      title: m.name || m.title,
      posterUrl: formatPosterUrl(m.posterPath),
      releaseDate: m.releaseDate,
      year: m.releaseDate ? m.releaseDate.substring(0, 4) : null,
      duration: m.duration,
      trailerUrl: m.trailer && m.trailer !== "Not Fetched" ? m.trailer : null,
      youtubeMovie: m.youtubeMovie && m.youtubeMovie !== "Not Fetched" ? m.youtubeMovie : null,
      description: m.wikipediaTitle || m.name,
      rating: m.rate ? Number(m.rate).toFixed(1) : null,
      genres: m.genres ? m.genres.map(g => g.name) : [],
      actors: m.actors ? m.actors.map(a => ({ id: a.id, name: a.name })) : [],
      directors: m.directors ? m.directors.map(d => ({ id: d.id, name: d.name })) : [],
      country: m.productionCountries ? m.productionCountries.map(c => c.name).join(', ') : null
    };
  },

  async rate(movieId, rating, token) {
    const res = await smartFetch(`${BASE_URL}/rate/secure`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId), rate: Number(rating) })
    });
    if (!res.ok) throw new Error('Failed to save rating');
    return true;
  },

  async getUserRating(userId, movieId) {
    const res = await smartFetch(`${BASE_URL}/rate?userid=${userId}`);
    if (!res.ok) return 0;
    const ratings = await res.json();
    const movieRating = ratings.find(r => r.movieId === Number(movieId));
    return movieRating ? movieRating.rating : 0;
  },

  async getRatingsByUser(userId) {
    const res = await smartFetch(`${BASE_URL}/rate?userid=${userId}`);
    if (!res.ok) throw new Error('Failed to fetch user ratings');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  async deleteRate(movieId, token) {
    const res = await smartFetch(`${BASE_URL}/rate/secure`, {
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
  },

  // Admin methods
  async adminDeleteMovie(movieId, token) {
    const res = await smartFetch(`${BASE_URL}/movie/admin/${movieId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to delete movie as admin');
    return true;
  },

  async adminModifyMovie(movieId, movieData, token) {
    const res = await smartFetch(`${BASE_URL}/movie/admin/${movieId}`, {
      method: 'PATCH',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(movieData)
    });
    if (!res.ok) throw new Error('Failed to modify movie as admin');
    return res.json();
  }
};

export const staffApi = {
  async getById(id) {
    const res = await smartFetch(`${BASE_URL}/staff/${id}`);
    if (!res.ok) throw new Error('Staff not found');
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
    return res.text();
  },

  async logout(refreshToken) {
    await fetch(`${BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
  },

  async refresh(refreshToken) {
    const res = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });

    if (!res.ok) {
      throw new Error('Refresh failed');
    }
    return res.json();
  },


   async getMe(token) {
     const res = await smartFetch(`${BASE_URL}/auth/me`, {
       method: 'GET',
       cache: 'no-store',
       headers: { 'Authorization': `Bearer ${token}` }
     });
     if (!res.ok) throw new Error('Failed to fetch user profile');
     const data = await res.json();

     // Always decode the freshest token from localStorage, 
     // as smartFetch might have silently refreshed it.
     const currentToken = localStorage.getItem('accessToken');
     const decoded = decodeToken(currentToken);
     const authorities = decoded?.authorities || [];

     return {
       id: data.id,
       username: data.username,
       email: data.email,
       age: data.age,
       genderName: data.gender,
       roles: authorities,
       isAdmin: authorities.includes('ROLE_ADMIN') || authorities.includes('ADMIN'),
       playlists: Array.isArray(data.playlists) ? data.playlists.map(pl => ({
         ...pl,
         movies: Array.isArray(pl.movies) ? pl.movies.map(formatBasicMovie) : []
       })) : [],
       ratedMovies: Array.isArray(data.ratedMovies) ? data.ratedMovies.map(formatBasicMovie) : [],
       reviewedMovies: Array.isArray(data.reviewedMovies) ? data.reviewedMovies.map(formatBasicMovie) : []
     };
   },

   async getUserPublicById(userId) {
     const idNum = parseInt(userId);
     if (isNaN(idNum) || idNum === 0) throw new Error('Invalid User ID');

     const res = await smartFetch(`${BASE_URL}/auth/user/${idNum}`);
     if (!res.ok) {
       if (res.status === 404) {
          const errText = await res.text().catch(() => '');
          throw new Error(errText || `User not found (ID: ${idNum})`);
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
    const res = await smartFetch(`${BASE_URL}/genre`);
    if (!res.ok) throw new Error('Failed to fetch genres');
    return res.json();
  }
};

export const recommendationApi = {
  async getForMe(token) {
    const res = await smartFetch(`${BASE_URL}/recommend/secure/me`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) {
      if (res.status === 404) return []; // No interactions yet
      throw new Error('Recommendations unavailable');
    }
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  async getForUserAdmin(userId, token) {
    const res = await smartFetch(`${BASE_URL}/recommend/admin/${userId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to fetch recommendations for user');
    const data = await res.json();
    return data.map(formatBasicMovie);
  }
};

export const playlistApi = {
  async getByOwner(ownerId, token) {
    const res = await smartFetch(`${BASE_URL}/playlist?ownerId=${ownerId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to fetch playlists');
    return res.json();
  },

  async getById(playlistId, token) {
    const res = await smartFetch(`${BASE_URL}/playlist/${playlistId}`, {
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
    const res = await smartFetch(`${BASE_URL}/playlist/secure`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ name, isPrivate: !isPublic })
    });
    if (!res.ok) throw new Error('Failed to create playlist');
    return res.json();
  },

  async delete(playlistId, token) {
    const res = await smartFetch(`${BASE_URL}/playlist/secure/${playlistId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to delete playlist');
    return true;
  },

  async addMovie(playlistId, movieId, token) {
    const res = await smartFetch(`${BASE_URL}/playlist/secure/${playlistId}/movies`, {
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

  async removeMovie(pId, mId, token) {
    const url = `${BASE_URL}/playlist/secure/${pId}/movies/${mId}`;
    const res = await smartFetch(url, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to remove movie');
    return res.json();
  },

  // Admin methods
  async adminDeletePlaylist(playlistId, token) {
    const res = await smartFetch(`${BASE_URL}/playlist/admin/${playlistId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Failed to delete playlist as admin');
    return true;
  }
};

export const reviewApi = {
  async getAllByMovie(movieId) {
    const res = await smartFetch(`${BASE_URL}/review/${movieId}`);
    if (!res.ok) throw new Error('Failed to fetch reviews');
    return res.json();
  },

  async getReviewsByUser(userId) {
    const res = await smartFetch(`${BASE_URL}/review?userid=${userId}`);
    if (!res.ok) throw new Error('Failed to fetch user reviews');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  async postReview(movieId, reviewText, token) {
    const res = await smartFetch(`${BASE_URL}/review/secure`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId), rate: reviewText })
    });
    if (!res.ok) throw new Error('Failed to post review');
    return true;
  },

  async deleteReview(movieId, token) {
    const res = await smartFetch(`${BASE_URL}/review/secure`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId: Number(movieId) })
    });
    if (!res.ok) throw new Error('Failed to delete review');
    return true;
  },

  async adminDeleteReview(userId, movieId, token) {
    const res = await smartFetch(`${BASE_URL}/review/admin`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ userId: Number(userId), movieId: Number(movieId) })
    });
    if (!res.ok) throw new Error('Failed to delete review as admin');
    return true;
  }
};