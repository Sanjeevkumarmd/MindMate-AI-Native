import { useRef } from 'react';
import { useNavigate } from 'react-router-dom';

export default function FeatureCard({ 
  image, 
  title, 
  description, 
  color, 
  textColor = 'white',
  inputType = 'file', // 'file' or 'text'
  inputPlaceholder,
  navigateTo,
  staggerClass = '',
  onFileSelect,
  onTextSubmit
}) {
  const fileInputRef = useRef(null);
  const navigate = useNavigate();
  const isDarkText = textColor === 'black' || textColor === '#000' || textColor === '#000000';

  const handleFileClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && onFileSelect) {
      onFileSelect(file);
    }
  };

  const handleTextKeyDown = (e) => {
    if (e.key === 'Enter' && e.target.value.trim()) {
      if (onTextSubmit) {
        onTextSubmit(e.target.value.trim());
      }
    }
  };

  const handleCardClick = (e) => {
    // Only trigger if it's a file input type and the click wasn't on an internal interactive element
    if (inputType === 'file' && !['INPUT', 'BUTTON'].includes(e.target.tagName)) {
      handleFileClick();
    }
  };

  return (
    <div 
      className={`feature-card ${staggerClass} ${inputType === 'file' ? 'clickable-card' : ''}`} 
      style={{ backgroundColor: color, color: textColor }}
      onClick={handleCardClick}
    >
      <div className="card-image-wrapper">
        <img src={image} alt={title} />
      </div>
      <div className="card-content">
        <h3>{title}</h3>
        <p>{description}</p>
        <div className="card-input-section">
          {inputType === 'file' ? (
            <>
              <input 
                type="file" 
                ref={fileInputRef}
                accept=".pdf,.png,.jpg,.jpeg"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <button 
                className={`file-upload-btn ${isDarkText ? 'dark-text' : ''}`}
                onClick={(e) => {
                  e.stopPropagation(); // Prevent double trigger
                  handleFileClick();
                }}
              >
                📝 Import PDF / Screenshot
              </button>
            </>
          ) : (
            <div className="code-input-wrapper">
              <input 
                type="text"
                placeholder={inputPlaceholder || 'Type here...'}
                onKeyDown={handleTextKeyDown}
                onClick={(e) => e.stopPropagation()}
                style={{ color: textColor }}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
