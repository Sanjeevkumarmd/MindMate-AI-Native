const STORAGE_KEYS = {
  API_KEY: 'mindmate_gemini_api_key',
  QUIZ_SCORES: 'mindmate_quiz_scores',
  STUDY_TIME: 'mindmate_study_time',
  SESSION_START: 'mindmate_session_start',
};

// API Key
export function getApiKey() {
  return localStorage.getItem(STORAGE_KEYS.API_KEY);
}

export function setApiKey(key) {
  localStorage.setItem(STORAGE_KEYS.API_KEY, key);
}

// Quiz Scores
export function getQuizScores() {
  const scores = localStorage.getItem(STORAGE_KEYS.QUIZ_SCORES);
  return scores ? JSON.parse(scores) : [];
}

export function addQuizScore(score, total, topic) {
  const scores = getQuizScores();
  scores.push({
    score,
    total,
    topic: topic || 'General Quiz',
    date: new Date().toISOString(),
  });
  localStorage.setItem(STORAGE_KEYS.QUIZ_SCORES, JSON.stringify(scores));
}

// Study Time Tracking
export function startStudySession() {
  localStorage.setItem(STORAGE_KEYS.SESSION_START, Date.now().toString());
}

export function endStudySession() {
  const start = localStorage.getItem(STORAGE_KEYS.SESSION_START);
  if (start) {
    const elapsed = Date.now() - parseInt(start);
    const totalTime = getStudyTime();
    localStorage.setItem(STORAGE_KEYS.STUDY_TIME, (totalTime + elapsed).toString());
    localStorage.removeItem(STORAGE_KEYS.SESSION_START);
  }
}

export function getStudyTime() {
  const stored = localStorage.getItem(STORAGE_KEYS.STUDY_TIME);
  const base = stored ? parseInt(stored) : 0;
  
  // Add current session time if active
  const sessionStart = localStorage.getItem(STORAGE_KEYS.SESSION_START);
  if (sessionStart) {
    return base + (Date.now() - parseInt(sessionStart));
  }
  return base;
}

export function formatStudyTime(ms) {
  const totalMinutes = Math.floor(ms / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  return `${minutes}m`;
}
