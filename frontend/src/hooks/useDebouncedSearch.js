import { useState, useEffect } from 'react';
import { movieApi } from '../services/api'; // Adjust path if needed

export function useDebouncedSearch(query, delay = 300) {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // If the query is too short, clear everything
    if (query.trim().length < 3) {
      setSuggestions([]);
      return;
    }

    let cancelled = false;
    setLoading(true);

    // Start a timer. It will only fetch if the user stops typing for 'delay' milliseconds.
    const timeoutId = setTimeout(() => {
      movieApi.search(query)
        .then(results => {
          if (!cancelled) {
            setSuggestions(results.slice(0, 5));
            setLoading(false);
          }
        })
        .catch(err => {
          console.error('Search error:', err);
          if (!cancelled) {
            setSuggestions([]);
            setLoading(false);
          }
        });
    }, delay);

    // If the user types again before the timer finishes, clear the timer
    return () => {
      cancelled = true;
      clearTimeout(timeoutId);
    };
  }, [query, delay]);

  return { suggestions, setSuggestions, loading };
}