import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { kannadaExplain, chatWithBot, isGeminiReady } from '../utils/gemini';

export default function KannadaLearningPage() {
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
        const result = await kannadaExplain(extractedText);
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
      const response = await chatWithBot(`Context: This is about a Kannada learning session. The content being studied is about: ${fileName}. Question: ${msg}`);
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
          <p className="loading-text">ಕನ್ನಡದಲ್ಲಿ ವಿವರಿಸುತ್ತಿದೆ... 📖</p>
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
          <h1>📖 ಕನ್ನಡ ಕಲಿಕೆ</h1>
          <p>Kannada Learning Mode — {fileName}</p>
        </div>
        <div className="feature-page-card" style={{ background: '#ffdbbb', color: '#333' }}>
          <h2>📚 ಕನ್ನಡ ವಿವರಣೆ</h2>
          <div className="content-text">{explanation}</div>
        </div>

        <div className="mini-chatbot">
          <h3>💬 Clarify Your Doubts</h3>
          <div className="mini-chat-messages">
            {chatMessages.length === 0 && (
              <p style={{ color: '#94a3b8', textAlign: 'center', padding: '20px' }}>
                Ask any follow-up questions about the explanation above! 🤔
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
              placeholder="Ask a follow-up question..."
              disabled={chatLoading}
            />
            <button onClick={handleChatSend} disabled={chatLoading}>Send</button>
          </div>
        </div>
      </div>
    </div>
  );
}
