
import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { generateQuiz, isGeminiReady } from '../utils/gemini';
import { addQuizScore } from '../utils/storage';
import { motion, AnimatePresence } from 'framer-motion';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
import { Share } from '@capacitor/share';
import { Share2 } from 'lucide-react';

export default function QuizPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { extractedText, fileName } = location.state || {};
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [showResults, setShowResults] = useState(false);
  const [score, setScore] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!extractedText) {
      navigate('/');
      return;
    }
    if (!isGeminiReady()) {
      setError('Please set your Gemini API key first!');
      setLoading(false);
      return;
    }

    const fetchQuiz = async () => {
      try {
        const quiz = await generateQuiz(extractedText);
        setQuestions(quiz);
      } catch (err) {
        setError('Failed to generate quiz: ' + err.message);
      }
      setLoading(false);
    };
    fetchQuiz();
  }, [extractedText, navigate]);

  const triggerHaptic = async (style = ImpactStyle.Light) => {
    try { await Haptics.impact({ style }); } catch (e) {}
  };

  const handleAnswer = (questionIdx, optionIdx) => {
    if (showResults) return;
    triggerHaptic();
    setAnswers(prev => ({ ...prev, [questionIdx]: optionIdx }));
  };

  const handleSubmit = () => {
    triggerHaptic(ImpactStyle.Heavy);
    let correct = 0;
    questions.forEach((q, idx) => {
      if (answers[idx] === q.correctIndex) correct++;
    });
    setScore(correct);
    setShowResults(true);
    addQuizScore(correct, questions.length, fileName || 'Quiz');
  };

  const handleShareScore = async () => {
    try {
      await Share.share({
        title: 'MindMate AI Quiz Score',
        text: `I just scored ${score}/${questions.length} on my MindMate AI Quiz based on ${fileName}!\n\nCan you beat my score? Download MindMate AI to find out. 🚀`,
        dialogTitle: 'Share your Score',
      });
    } catch (error) {
      console.error('Error sharing:', error);
    }
  };

  const getOptionClass = (qIdx, oIdx) => {
    if (!showResults) {
      return answers[qIdx] === oIdx ? 'quiz-option selected' : 'quiz-option';
    }
    if (oIdx === questions[qIdx].correctIndex) return 'quiz-option correct';
    if (answers[qIdx] === oIdx && oIdx !== questions[qIdx].correctIndex) return 'quiz-option wrong';
    return 'quiz-option';
  };

  if (loading) {
    return (
      <motion.div 
        className="page-container"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
      >
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p className="loading-text">Generating your quiz... ⏱️</p>
        </div>
      </motion.div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <button className="back-btn" onClick={() => navigate('/')}>← Back Home</button>
        <div className="info-page">
          <div className="info-card">
            <h1>⚠️ Error</h1>
            <p>{error}</p>
          </div>
        </div>
      </div>
    );
  }

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: { staggerChildren: 0.15 }
    }
  };

  const cardVariants = {
    hidden: { opacity: 0, y: 30, scale: 0.95 },
    show: { opacity: 1, y: 0, scale: 1, transition: { type: "spring", stiffness: 200, damping: 20 } }
  };

  return (
    <div className="quiz-container">
      <div className="main-content-inner" style={{ maxWidth: '800px', margin: '0 auto', paddingTop: 'var(--navbar-height)' }}>
        <button className="back-btn" onClick={() => navigate('/')} style={{ marginTop: '20px' }}>← Back Home</button>
        
        <motion.div 
          className="page-header" 
          style={{ color: 'black' }}
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <h1>⏱️ Quick Quiz</h1>
          <p>Answer all 10 questions and check your score!</p>
        </motion.div>

        <motion.div 
          variants={containerVariants}
          initial="hidden"
          animate="show"
        >
          {questions.map((q, qIdx) => (
            <motion.div key={qIdx} className="quiz-question-card" variants={cardVariants}>
              <span className="question-number">Question {qIdx + 1}</span>
              <p className="question-text">{q.question}</p>
              <div className="quiz-options">
                {q.options.map((opt, oIdx) => (
                  <button
                    key={oIdx}
                    className={getOptionClass(qIdx, oIdx)}
                    onClick={() => handleAnswer(qIdx, oIdx)}
                  >
                    {String.fromCharCode(65 + oIdx)}. {opt}
                  </button>
                ))}
              </div>
            </motion.div>
          ))}
        </motion.div>

        {!showResults && questions.length > 0 && (
          <motion.div 
            style={{ textAlign: 'center', margin: '32px 0' }}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 1 }}
          >
            <button 
              className="btn-home"
              onClick={handleSubmit}
              disabled={Object.keys(answers).length < questions.length}
              style={{ 
                opacity: Object.keys(answers).length < questions.length ? 0.6 : 1,
                transform: Object.keys(answers).length < questions.length ? 'scale(0.95)' : 'scale(1)'
              }}
            >
              Submit Quiz 🎯
            </button>
            <p style={{ marginTop: '12px', color: '#666', fontSize: '0.85rem' }}>
              {Object.keys(answers).length}/{questions.length} answered
            </p>
          </motion.div>
        )}

        <AnimatePresence>
          {showResults && (
            <motion.div 
              className="quiz-result" 
              style={{ marginTop: '32px', position: 'relative' }}
              initial={{ opacity: 0, scale: 0.8, y: 50 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ type: 'spring', damping: 15, stiffness: 100 }}
            >
              <button 
                onClick={handleShareScore} 
                className="action-btn" 
                style={{ position: 'absolute', top: '24px', right: '24px', background: 'rgba(0,0,0,0.05)', color: 'var(--primary)' }}
                title="Share Score"
              >
                <Share2 size={24} />
              </button>
              <h2>Quiz Complete! 🏆</h2>
              <motion.div 
                className="score-circle"
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ type: 'spring', delay: 0.3 }}
              >
                <span>{score}/{questions.length}</span>
              </motion.div>
              <p className="result-message">
                {score >= 8 ? "Outstanding! You've mastered this topic! 🌟" :
                 score >= 6 ? "Great job! Keep up the good work! 👍" :
                 score >= 4 ? "Good effort! Review the topics you missed. 📚" :
                 "Keep learning! Practice makes perfect! 💪"}
              </p>
              <button className="btn-home" onClick={() => navigate('/')}>
                ← Back to Home
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

