
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FeatureCard from '../components/FeatureCard';
import { getFileText } from '../utils/pdfParser';
import { motion, AnimatePresence } from 'framer-motion';
import { Haptics, ImpactStyle } from '@capacitor/haptics';

export default function HomePage() {
  const navigate = useNavigate();
  const [loadingCard, setLoadingCard] = useState(null);

  const triggerHaptic = async () => {
    try {
      await Haptics.impact({ style: ImpactStyle.Light });
    } catch (e) {
      // Ignore if not on native device
    }
  };

  const handleFileSelect = async (file, feature) => {
    await triggerHaptic();
    setLoadingCard(feature);
    try {
      const text = await getFileText(file);
      navigate(`/${feature}`, { state: { extractedText: text, fileName: file.name } });
    } catch (error) {
      alert('Error processing file: ' + error.message);
    }
    setLoadingCard(null);
  };

  const handleCodeSubmit = (code) => {
    triggerHaptic();
    navigate('/code-explainer', { state: { code } });
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, scale: 0.8, y: 20 },
    show: { opacity: 1, scale: 1, y: 0, transition: { type: "spring", stiffness: 200, damping: 20 } }
  };

  return (
    <div className="page-container">
      {/* Hero Section */}
      <motion.section 
        className="hero-section"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
      >
        <h1>Your AI-Powered Study Buddy 🚀</h1>
        <p>
          Master engineering concepts with AI assistance. Summarize notes, take quizzes, 
          generate questions, and learn in Kannada — all powered by Google Gemini AI.
        </p>
      </motion.section>

      {/* Feature Cards */}
      <motion.div 
        className="cards-grid"
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/summary-icon.svg"
            title="Summarize"
            description="Upload your PDF notes and get instant, clear summaries in both English and Kannada to speed up your revision."
            color="#90D5FF"
            textColor="white"
            onFileSelect={(file) => handleFileSelect(file, 'summarize')}
          />
        </motion.div>
        
        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/quiz-icon.svg"
            title="Quick Quiz"
            description="Test your knowledge with AI-generated quizzes based on your study material. Get instant feedback on your answers."
            color="#FFF44F"
            textColor="black"
            onFileSelect={(file) => handleFileSelect(file, 'quiz')}
          />
        </motion.div>

        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/logo.svg"
            title="Generate Questions"
            description="Get a list of 10-15 important exam-worthy questions generated from your uploaded notes for focused preparation."
            color="#ff2c2c"
            textColor="white"
            onFileSelect={(file) => handleFileSelect(file, 'generate-questions')}
          />
        </motion.div>
      </motion.div>

      {/* Feature Cards - Row 2 */}
      <motion.div 
        className="cards-grid" 
        style={{ marginTop: '28px' }}
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/kannada-icon.png"
            title="Kannada Learning Mode"
            description="Understand complex topics explained in Kannada with a mini chatbot for follow-up doubts and clarifications."
            color="#ffdbbb"
            textColor="#333"
            onFileSelect={(file) => handleFileSelect(file, 'kannada-learning')}
          />
        </motion.div>

        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/realworld-icon.png"
            title="Real-World Example"
            description="Map academic concepts to real-world examples and analogies for deeper understanding and better retention."
            color="#ffb6c1"
            textColor="#333"
            onFileSelect={(file) => handleFileSelect(file, 'real-world')}
          />
        </motion.div>

        <motion.div variants={itemVariants}>
          <FeatureCard
            image="/images/code-icon.png"
            title="Codium"
            description="Paste any code snippet and get a detailed line-by-line explanation with complexity analysis and best practices."
            color="#88e788"
            textColor="#333"
            inputType="text"
            inputPlaceholder="Have any doubt in coding?"
            onTextSubmit={handleCodeSubmit}
          />
        </motion.div>
      </motion.div>

      <AnimatePresence>
        {loadingCard && (
          <motion.div 
            className="modal-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div 
              className="loading-container" 
              style={{ background: 'white', padding: '40px', borderRadius: '20px' }}
              initial={{ scale: 0.8 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.8 }}
            >
              <div className="loading-spinner"></div>
              <p className="loading-text">Processing your file... 🚀</p>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

