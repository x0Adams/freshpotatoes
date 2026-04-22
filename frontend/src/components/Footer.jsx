function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="mt-5 py-4 border-top border-secondary" style={{ backgroundColor: '#0a0a0f', position: 'relative', zIndex: 10 }}>
      <div className="container text-center">
        <div className="mb-3">
          <span className="fw-bold fs-5 text-light">
            fresh<span className="text-warning">Potatoes</span>
          </span>
        </div>
        
        <p className="text-secondary small mb-2">
          &copy; {currentYear} freshPotatoes. All rights reserved.
        </p>
        
        <p className="text-secondary small mb-0">
          Movie data and descriptions generously provided by{' '}
          <a 
            href="https://www.wikipedia.org/" 
            target="_blank" 
            rel="noopener noreferrer" 
            className="text-warning text-decoration-none"
          >
            Wikipedia
          </a>.
        </p>
      </div>
    </footer>
  );
}

export default Footer;