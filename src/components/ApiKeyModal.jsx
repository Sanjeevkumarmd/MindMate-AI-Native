import { useState, useEffect } from 'react';
import { getApiKey, setApiKey } from '../utils/storage';
import { initGemini } from '../utils/gemini';

export default function ApiKeyModal({ onClose }) {
  const [key, setKey] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const existingKey = getApiKey();
    if (existingKey) {
      setKey(existingKey);
    }
  }, []);

  const handleSubmit = () => {
    if (!key.trim()) {
      setError('Please enter your API key');
      return;
    }
    setApiKey(key.trim());
    initGemini(key.trim());
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-content">
        <h2>🔑 Gemini API Key</h2>
        <p>Enter your Google Gemini API key to unlock AI features. You can get one free from <a href="https://aistudio.google.com/apikey" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary)' }}>Google AI Studio</a>.</p>
        <input 
          type="password"
          value={key}
          onChange={(e) => { setKey(e.target.value); setError(''); }}
          placeholder="Enter your Gemini API key..."
          onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
        />
        {error && <p style={{ color: '#ef4444', fontSize: '0.85rem', marginBottom: '12px' }}>{error}</p>}
        <button className="btn-primary" onClick={handleSubmit}>
          Save & Continue
        </button>
      </div>
    </div>
  );
}
