import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { movieApi } from '../services/api'
import { useToast } from '../context/ToastContext'

function SurpriseMeSection() {
  const [isRolling, setIsRolling] = useState(false)
  const { showToast } = useToast()
  const navigate = useNavigate()

  const handleSurprise = async () => {
    if (isRolling) return
    setIsRolling(true)
    
    try {
      // 1. Get a pool of random movies
      const randomMovies = await movieApi.getRandomMovies()
      
      if (!randomMovies || randomMovies.length === 0) {
        throw new Error("No movies found")
      }

      // 2. Try to find a loadable movie from the pool
      // We try up to 5 different movies from the pool to find one that doesn't 500
      let picked = null;
      let attempts = 0;
      const shuffled = [...randomMovies].sort(() => 0.5 - Math.random());

      while (attempts < Math.min(shuffled.length, 5)) {
        const candidate = shuffled[attempts];
        try {
          // Pre-flight check: can we actually load this movie's details?
          await movieApi.getById(candidate.id);
          picked = candidate; // If this line is reached, the movie is valid
          break;
        } catch (err) {
          console.warn(`Movie ${candidate.id} failed pre-flight, trying next...`);
          attempts++;
        }
      }

      if (picked) {
        // 3. Cinematic transition to the valid movie
        setTimeout(() => {
          navigate(`/movie/${picked.id}`)
          showToast(`Surprise! How about "${picked.title}"?`)
          setIsRolling(false)
        }, 800)
      } else {
        throw new Error("Could not find a valid movie in this batch")
      }

    } catch (err) {
      console.error("Surprise Me failed:", err)
      showToast("The freshPotatoes engine is recalibrating. Try again!", "error")
      setIsRolling(false)
    }
  }

  return (
    <div className="container mt-5 mb-2">
      <div 
        className="admin-panel-modern p-4 d-flex align-items-center justify-content-between animate-fade-in"
        style={{ 
          background: 'linear-gradient(90deg, rgba(255,193,7,0.05) 0%, rgba(13,13,18,0) 100%)',
          borderLeft: '4px solid #ffc107'
        }}
      >
        <div className="d-flex align-items-center gap-4">
           <div className={`surprise-icon-box ${isRolling ? 'rolling' : ''}`} style={{ fontSize: '2.5rem' }}>
              <i className="bi bi-dice-5 text-warning" />
           </div>
           <div>
              <h4 className="text-light fw-black uppercase tracking-widest mb-1">Feeling Lucky?</h4>
              <p className="text-secondary small m-0">Let the freshPotatoes engine pick a random masterpiece for you.</p>
           </div>
        </div>

        <button 
          className={`btn-fresh-primary px-5 ${isRolling ? 'disabled' : ''}`}
          onClick={handleSurprise}
          disabled={isRolling}
          style={{ height: '54px' }}
        >
          {isRolling ? (
            <><span className="spinner-border spinner-border-sm me-2" /> Picking...</>
          ) : (
            <>Surprise Me <i className="bi bi-stars ms-2" /></>
          )}
        </button>
      </div>
    </div>
  )
}

export default SurpriseMeSection