import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { explainCode, isGeminiReady } from '../utils/gemini';

export default function CodeExplainerPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { code } = location.state || {};
  const [explanation, setExplanation] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!code) { navigate('/'); return; }
    if (!isGeminiReady()) { setError('Please set your Gemini API key first!'); setLoading(false); return; }

    const fetch = async () => {
      try {
        const result = await explainCode(code);
        setExplanation(result);
      } catch (err) { setError('Failed: ' + err.message); }
      setLoading(false);
    };
    fetch();
  }, [code, navigate]);

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p className="loading-text">Analyzing your code... 💻</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <button className="back-btn" onClick={() => navigate('/')}>← Back Home</button>
        <div className="info-page"><div className="info-card"><h1>⚠️</h1><p>{error}</p></div></div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <button className="back-btn" onClick={() => navigate('/')}>← Back Home</button>
      <div className="feature-page-content">
        <div className="page-header">
          <h1>💻 Code Explanation</h1>
          <p>Detailed analysis of your code</p>
        </div>
        <div className="code-explain-card">
          <h2 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: '16px', color: 'var(--primary)' }}>
            📝 Your Code
          </h2>
          <pre>{code}</pre>
          <h2 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: '16px', color: 'var(--accent)' }}>
            🔍 Explanation
          </h2>
          <div className="explanation">{explanation}</div>
        </div>
      </div>
    </div>
  );
}
