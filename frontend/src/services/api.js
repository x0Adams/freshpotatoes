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
  const posterPath = movie.posterPath || movie.poster_path || movie.posterUrl;
  return {
    id: movie.id,
    title: movie.name || movie.title,
    posterUrl: formatPosterUrl(posterPath),
    year: movie.releaseDate ? movie.releaseDate.substring(0, 4) : (movie.year || 'Unknown'),
    genre: movie.genres && movie.genres.length > 0 ? movie.genres[0].name : 'Movie'
  };
}

export const movieApi = {
  async search(query, signal) {
    const res = await fetch(`${BASE_URL}/movie/search/${encodeURIComponent(query)}`, { signal });
    if (!res.ok) throw new Error(`Search failed`);
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  // NEW: Fetch paginated movies
  async getMovies(page = 0, size = 30) {
    const res = await fetch(`${BASE_URL}/movie?page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch movies');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  // NEW: Fetch movies by genre
  async getMoviesByGenre(genre, page = 0, size = 30) {
    const res = await fetch(`${BASE_URL}/movie/search?title=a&genre=${encodeURIComponent(genre)}&page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch movies by genre');
    const data = await res.json();
    return data.map(formatBasicMovie);
  },

  // NEW: Fetch random movies
  async getRandomMovies() {
    const res = await fetch(`${BASE_URL}/movie/random`);
    if (!res.ok) throw new Error('Failed to fetch random movies');
    const data = await res.json();
    return data.map(formatBasicMovie);
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
      // Map nested arrays to flat string arrays
      genres: m.genres ? m.genres.map(g => g.name).sort() : [],
      actors: m.actors ? m.actors.map(a => a.name).sort() : [],
      directors: m.directors ? m.directors.map(d => d.name).sort() : [],
      description: m.description,
      country: m.productionCountries && m.productionCountries.length > 0 
        ? m.productionCountries.map(c => c.name).sort().join(', ') 
        : null,
      // Backend already provides the average on a 1-5 scale
      rating: m.rate ? Number(m.rate).toFixed(1) : null
    };
  },

  async rate(movieId, rating, token) {
    const res = await fetch(`${BASE_URL}/rate/secure/rate`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ movieId, rating: Number(rating) })
    });
    if (!res.ok) {
      const errText = await res.text().catch(() => 'Unknown error');
      console.error('Rating error from backend:', errText);
      throw new Error(errText || 'Failed to save rating');
    }
    return res.text();
  }
};

export const staffApi = {
  async search(name, signal) {
    // Backend search endpoint removed as per user instruction to not modify backend
    return [];
  },

  async getById(id) {
    const res = await fetch(`${BASE_URL}/staff/${id}`);
    if (!res.ok) throw new Error('Failed to fetch staff details');
    const s = await res.json();
    return {
      ...s,
      birthCountry: s.birthCountry ? s.birthCountry.name : null,
      gender: s.gender ? s.gender.name : null
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
    if (!res.ok) throw new Error('Could not fetch user profile');
    const data = await res.json();
    // Map backend JWT claims to our frontend-friendly user object
    return {
      id: data.uid,
      username: data.sub, // 'sub' is standard for username/subject in JWT
      email: data.email,
      age: data.age,
      genderName: data.gender,
      roles: data.authorities || []
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