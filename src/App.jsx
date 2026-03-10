import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Chatbot from './components/Chatbot';
import HomePage from './pages/HomePage';
import SummarizePage from './pages/SummarizePage';
import QuizPage from './pages/QuizPage';
import GenerateQuestionsPage from './pages/GenerateQuestionsPage';
import KannadaLearningPage from './pages/KannadaLearningPage';
import RealWorldPage from './pages/RealWorldPage';
import CodeExplainerPage from './pages/CodeExplainerPage';
import AboutUsPage from './pages/AboutUsPage';
import ContactUsPage from './pages/ContactUsPage';
import ProfilePage from './pages/ProfilePage';
import { startStudySession, endStudySession } from './utils/storage';
// Gemini auto-initializes with built-in API key on import
import './utils/gemini';

function App() {
  useEffect(() => {
    // Track study time
    startStudySession();
    const handleBeforeUnload = () => endStudySession();
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
      endStudySession();
    };
  }, []);

  return (
    <Router>
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/summarize" element={<SummarizePage />} />
          <Route path="/quiz" element={<QuizPage />} />
          <Route path="/generate-questions" element={<GenerateQuestionsPage />} />
          <Route path="/kannada-learning" element={<KannadaLearningPage />} />
          <Route path="/real-world" element={<RealWorldPage />} />
          <Route path="/code-explainer" element={<CodeExplainerPage />} />
          <Route path="/about" element={<AboutUsPage />} />
          <Route path="/contact" element={<ContactUsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Routes>
      </main>
      <Footer />
      <Chatbot />
    </Router>
  );
}

export default App;

