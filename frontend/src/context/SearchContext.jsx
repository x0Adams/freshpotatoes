import React, { createContext, useContext, useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const SearchContext = createContext(null);

export function SearchProvider({ children }) {
  const location = useLocation();
  const [liveQuery, setLiveQuery] = useState(() => {
    return new URLSearchParams(window.location.search).get('q') || '';
  });

  // Sync with URL changes (e.g. back/forward button or navigation)
  useEffect(() => {
    const q = new URLSearchParams(location.search).get('q') || '';
    setLiveQuery(q);
  }, [location.search]);

  return (
    <SearchContext.Provider value={{ liveQuery, setLiveQuery }}>
      {children}
    </SearchContext.Provider>
  );
}

export const useSearch = () => {
  const context = useContext(SearchContext);
  if (!context) {
    throw new Error('useSearch must be used within a SearchProvider');
  }
  return context;
};