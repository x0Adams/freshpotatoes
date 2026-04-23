import { createContext, useState, useContext, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check if we are already logged in when the app loads
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      authApi.getMe(token)
        .then(userData => setUser(userData))
        .catch(() => {
          // If token is invalid/expired, clear it
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (username, password) => {
    const data = await authApi.login({ username, password });
    const token = data.jwtToken;
    localStorage.setItem('accessToken', token);
    localStorage.setItem('refreshToken', data.refreshToken);
    
    // Fetch user details immediately after login
    const userData = await authApi.getMe(token);
    setUser(userData);
  };

  const register = async (userData) => {
    await authApi.register(userData);
    // Automatically log them in after successful registration
    await login(userData.username, userData.password);
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try { await authApi.logout(refreshToken); } catch (e) { console.error(e); }
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);