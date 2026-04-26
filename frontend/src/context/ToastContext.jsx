import React, { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';

const ToastContext = createContext(null);

function ToastItem({ toast, onRemove }) {
  const [exiting, setExiting] = useState(false);
  const timerRef = useRef(null);

  const startTimer = useCallback(() => {
    // Clear any existing timer first
    if (timerRef.current) clearTimeout(timerRef.current);
    
    timerRef.current = setTimeout(() => {
      setExiting(true);
      // Wait for exit animation to complete before removing from state
      setTimeout(() => {
        onRemove(toast.id);
      }, 500);
    }, 5000);
  }, [toast.id, onRemove]);

  const pauseTimer = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  useEffect(() => {
    startTimer();
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [startTimer]);

  return (
    <div 
      className={`toast-item ${toast.type} ${exiting ? 'animate-toast-out' : 'animate-toast-in'}`}
      onMouseEnter={pauseTimer}
      onMouseLeave={startTimer}
    >
      <div className="toast-icon">
        <i className={`bi bi-${toast.type === 'success' ? 'check-circle-fill' : 'exclamation-triangle-fill'}`} />
      </div>
      <div className="toast-content">{toast.message}</div>
    </div>
  );
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, type = 'success') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="toast-container">
        {toasts.map(toast => (
          <ToastItem 
            key={toast.id} 
            toast={toast} 
            onRemove={removeToast} 
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
