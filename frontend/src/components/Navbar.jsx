import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'

function Navbar({ onLoginClick }) {

  // scroll event listener
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 0)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <>
      <nav className={`navbar navbar-expand-lg fixed-top cv-nav ${scrolled ? 'cv-nav--scrolled' : ''}`}>
        <div className="container-fluid px-4">

          <Link className="navbar-brand fw-bold fs-4" to="/">
            fresh<span className="text-warning">Potatoes</span>
          </Link>

          <form className="d-none d-lg-flex mx-auto cv-search" role="search">
            <div className="input-group">
              <span className="input-group-text bg-transparent border-secondary">
                <i className="bi bi-search text-secondary" />
              </span>
              <input
                type="search"
                className="form-control border-secondary border-start-0 bg-transparent"
                placeholder="Search movies..." />
            </div>
          </form>

          <div className="d-flex align-items-center">
              <button 
                className="btn btn-outline-warning btn-sm px-3" 
                onClick={onLoginClick}>
                    Log In
              </button>
          </div>

        </div>
      </nav>
    </>
  )
}

export default Navbar