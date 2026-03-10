import { Link, useLocation } from 'react-router-dom';

export default function Navbar() {
  const location = useLocation();

  return (
    <nav className="navbar" id="main-navbar">
      <div className="navbar-left">
        <Link to="/" className="navbar-logo">
          <img src="/images/logo.svg" alt="MindMate AI" />
          <span>MindMate AI</span>
        </Link>
        <div className="nav-links">
          <Link to="/about" className={`nav-link${location.pathname === '/about' ? ' active' : ''}`}>
            <span>About Us</span>
          </Link>
          <Link to="/contact" className={`nav-link${location.pathname === '/contact' ? ' active' : ''}`}>
            <span>Contact Us</span>
          </Link>
        </div>
      </div>
      <div className="navbar-right">
        <Link to="/profile" className="profile-btn" title="View Profile">
          🎓
        </Link>
      </div>
    </nav>
  );
}
