export default function ContactUsPage() {
  return (
    <div className="page-container info-page">
      <div className="info-card">
        <h1>Contact Us</h1>
        <p>
          We'd love to hear from you! Whether you have feedback, suggestions, or need help getting started 
          with MindMate AI, our team is here to help. Reach out to us through any of the channels below 
          and we'll get back to you as soon as possible.
        </p>
        <div className="contact-links">
          <a href="mailto:mindmateai@example.com" className="contact-link">
            📧 mindmateai@example.com
          </a>
          <a href="https://github.com" target="_blank" rel="noopener noreferrer" className="contact-link">
            🐙 GitHub Repository
          </a>
          <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" className="contact-link">
            💼 LinkedIn Page
          </a>
          <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" className="contact-link">
            🐦 Twitter / X
          </a>
        </div>
      </div>
    </div>
  );
}
