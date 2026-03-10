import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getQuizScores, getStudyTime, formatStudyTime } from '../utils/storage';

export default function ProfilePage() {
  const navigate = useNavigate();
  const [scores, setScores] = useState([]);
  const [studyTime, setStudyTime] = useState(0);

  useEffect(() => {
    setScores(getQuizScores());
    setStudyTime(getStudyTime());

    // Update study time every minute
    const interval = setInterval(() => {
      setStudyTime(getStudyTime());
    }, 60000);
    return () => clearInterval(interval);
  }, []);

  const averageScore = scores.length > 0
    ? Math.round(scores.reduce((sum, s) => sum + (s.score / s.total) * 100, 0) / scores.length)
    : 0;

  const totalQuizzes = scores.length;
  const bestScore = scores.length > 0
    ? Math.max(...scores.map(s => Math.round((s.score / s.total) * 100)))
    : 0;

  const getScoreClass = (pct) => {
    if (pct >= 70) return 'high';
    if (pct >= 40) return 'mid';
    return 'low';
  };

  return (
    <div className="page-container">
      <button className="back-btn" onClick={() => navigate('/')}>← Back Home</button>
      <div className="profile-page">
        <div className="profile-header">
          <div className="profile-avatar">🎓</div>
          <h1 style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.8rem' }}>
            Student Profile
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>Track your learning progress</p>
        </div>

        <div className="profile-stats">
          <div className="stat-card stagger-1">
            <div className="stat-value">{formatStudyTime(studyTime)}</div>
            <div className="stat-label">⏱️ Total Study Time</div>
          </div>
          <div className="stat-card stagger-2">
            <div className="stat-value">{totalQuizzes}</div>
            <div className="stat-label">📝 Quizzes Taken</div>
          </div>
          <div className="stat-card stagger-3">
            <div className="stat-value">{averageScore}%</div>
            <div className="stat-label">📊 Average Score</div>
          </div>
          <div className="stat-card stagger-4">
            <div className="stat-value">{bestScore}%</div>
            <div className="stat-label">🏆 Best Score</div>
          </div>
        </div>

        <div className="quiz-history">
          <h3>📋 Quiz History</h3>
          {scores.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>
              No quizzes taken yet. Start learning! 🚀
            </p>
          ) : (
            scores.slice().reverse().map((s, idx) => {
              const pct = Math.round((s.score / s.total) * 100);
              return (
                <div key={idx} className="quiz-history-item">
                  <div>
                    <div style={{ fontWeight: 600 }}>{s.topic}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(s.date).toLocaleDateString()}
                    </div>
                  </div>
                  <span className={`quiz-score ${getScoreClass(pct)}`}>
                    {s.score}/{s.total} ({pct}%)
                  </span>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
