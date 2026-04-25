import { createContext, useState, useContext, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(() => !!localStorage.getItem('accessToken'));
  const [showAuthModal, setShowAuthModal] = useState(false);

  // Check if we are already logged in when the app loads
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const refresh = localStorage.getItem('refreshToken');
    
    // No tokens? Start as guest immediately, no server call needed.
    if (!token && !refresh) {
      setLoading(false);
      return;
    }

    const initAuth = async () => {
      try {
        // If we have a token, getMe will use smartFetch which handles auto-refresh
        const userData = await authApi.getMe(token);
        setUser(userData);
      } catch (err) {
        console.warn("Initial session check failed, starting as guest.");
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
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
    <AuthContext.Provider value={{ user, login, register, logout, loading, showAuthModal, setShowAuthModal }}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);