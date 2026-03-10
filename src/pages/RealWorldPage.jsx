import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { realWorldExample, chatWithBot, isGeminiReady } from '../utils/gemini';

export default function RealWorldPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { extractedText, fileName } = location.state || {};
  const [explanation, setExplanation] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  const chatEndRef = useRef(null);

  useEffect(() => {
    if (!extractedText) { navigate('/'); return; }
    if (!isGeminiReady()) { setError('Please set your Gemini API key first!'); setLoading(false); return; }

    const fetch = async () => {
      try {
        const result = await realWorldExample(extractedText);
        setExplanation(result);
      } catch (err) { setError('Failed: ' + err.message); }
      setLoading(false);
    };
    fetch();
  }, [extractedText, navigate]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const handleChatSend = async () => {
    if (!chatInput.trim() || chatLoading) return;
    const msg = chatInput.trim();
    setChatInput('');
    setChatMessages(prev => [...prev, { type: 'user', text: msg }]);
    setChatLoading(true);
    try {
      const response = await chatWithBot(`Context: Real-world examples session about: ${fileName}. Question: ${msg}`);
      setChatMessages(prev => [...prev, { type: 'bot', text: response }]);
    } catch {
      setChatMessages(prev => [...prev, { type: 'bot', text: 'Sorry, please try again!' }]);
    }
    setChatLoading(false);
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p className="loading-text">Finding real-world examples... 🌍</p>
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
          <h1>🌍 Real-World Examples</h1>
          <p>Relating concepts to real life — {fileName}</p>
        </div>
        <div className="feature-page-card" style={{ background: '#ffb6c1', color: '#333' }}>
          <h2>🔗 Real-World Mapping</h2>
          <div className="content-text">{explanation}</div>
        </div>

        <div className="mini-chatbot">
          <h3>💬 Need More Examples?</h3>
          <div className="mini-chat-messages">
            {chatMessages.length === 0 && (
              <p style={{ color: '#94a3b8', textAlign: 'center', padding: '20px' }}>
                Ask for more real-world examples or clarifications! 🤔
              </p>
            )}
            {chatMessages.map((msg, idx) => (
              <div key={idx} className={`chat-message ${msg.type}`}>{msg.text}</div>
            ))}
            {chatLoading && <div className="chat-message bot">Thinking... 🤔</div>}
            <div ref={chatEndRef} />
          </div>
          <div className="mini-chat-input-row">
            <input
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleChatSend()}
              placeholder="Ask for more examples..."
              disabled={chatLoading}
            />
            <button onClick={handleChatSend} disabled={chatLoading}>Send</button>
          </div>
        </div>
      </div>
    </div>
  );
}
