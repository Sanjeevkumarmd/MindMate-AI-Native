import { useState, useRef, useEffect } from 'react';
import { chatWithBot, isGeminiReady } from '../utils/gemini';

export default function Chatbot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      type: 'bot',
      text: "Hi👋, I am MindMate AI your study buddy😉\nHow can I help you?😊"
    }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [ttsEnabled, setTtsEnabled] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const speakText = (text) => {
    if (ttsEnabled && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 0.9;
      utterance.pitch = 1;
      window.speechSynthesis.speak(utterance);
    }
  };

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;
    
    const userMessage = input.trim();
    setInput('');
    setMessages(prev => [...prev, { type: 'user', text: userMessage }]);
    setIsLoading(true);

    try {
      if (!isGeminiReady()) {
        setMessages(prev => [...prev, { 
          type: 'bot', 
          text: "⚠️ Please set your Gemini API key first! Click the 🔑 icon in the top right to set it up." 
        }]);
        setIsLoading(false);
        return;
      }
      
      const response = await chatWithBot(userMessage);
      setMessages(prev => [...prev, { type: 'bot', text: response }]);
      speakText(response);
    } catch (error) {
      setMessages(prev => [...prev, { 
        type: 'bot', 
        text: "Sorry, I encountered an error. Please try again! 😅" 
      }]);
    }
    setIsLoading(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleSend();
  };

  return (
    <>
      {/* Floating trigger button */}
      <button 
        className="chatbot-trigger" 
        onClick={() => setIsOpen(!isOpen)}
        id="chatbot-trigger"
      >
        {isOpen ? (
          <span className="close-icon">×</span>
        ) : (
          <img src="/images/chatbot-icon.svg" alt="Chat" />
        )}
      </button>

      {/* Chat window */}
      {isOpen && (
        <div className="chatbot-window" id="chatbot-window">
          <div className="chatbot-header">
            <div className="chatbot-header-avatar">
              <img src="/images/chatbot-icon.svg" alt="MindMate" />
            </div>
            <div className="chatbot-header-info">
              <h3>MindMate AI</h3>
              <p>Your Study Buddy • Online</p>
            </div>
          </div>

          <div className="chatbot-messages">
            {messages.map((msg, idx) => (
              <div key={idx} className={`chat-message ${msg.type}`}>
                {msg.text}
              </div>
            ))}
            {isLoading && (
              <div className="chat-message bot">
                <span style={{ animation: 'pulse 1s ease infinite' }}>Thinking... 🤔</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-input-area">
            <button 
              className={`tts-btn ${ttsEnabled ? 'active' : ''}`}
              onClick={() => setTtsEnabled(!ttsEnabled)}
              title={ttsEnabled ? 'Disable text-to-speech' : 'Enable text-to-speech'}
            >
              {ttsEnabled ? '🔊' : '🔇'}
            </button>
            <input 
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask me anything..."
              disabled={isLoading}
            />
            <button className="chatbot-send-btn" onClick={handleSend} disabled={isLoading}>
              ➤
            </button>
          </div>
        </div>
      )}
    </>
  );
}
