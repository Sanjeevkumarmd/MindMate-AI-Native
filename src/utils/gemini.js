import { GoogleGenerativeAI } from '@google/generative-ai';

// ===== Built-in API Key =====
const GEMINI_API_KEY = 'AIzaSyBWuU51ncl8Coj8MAl3PMy21r47eFYjNpc';

let genAI = null;
let model = null;

// ===== Rate Limiter =====
const RATE_LIMIT = {
  maxRequests: 10,        // max requests per window
  windowMs: 60 * 1000,    // 1 minute window
  maxInputChars: 8000,    // max input chars per request (truncate beyond this)
  requests: [],            // timestamps of recent requests
};

function checkRateLimit() {
  const now = Date.now();
  // Remove requests older than the window
  RATE_LIMIT.requests = RATE_LIMIT.requests.filter(
    (t) => now - t < RATE_LIMIT.windowMs
  );
  if (RATE_LIMIT.requests.length >= RATE_LIMIT.maxRequests) {
    const waitSec = Math.ceil(
      (RATE_LIMIT.windowMs - (now - RATE_LIMIT.requests[0])) / 1000
    );
    throw new Error(
      `Rate limit reached! Please wait ${waitSec}s before trying again. (Max ${RATE_LIMIT.maxRequests} requests/min)`
    );
  }
  RATE_LIMIT.requests.push(now);
}

function truncateInput(text) {
  if (text && text.length > RATE_LIMIT.maxInputChars) {
    return text.slice(0, RATE_LIMIT.maxInputChars) + '\n\n[...content truncated for efficiency]';
  }
  return text;
}

// ===== Init =====
export function initGemini(apiKey) {
  const key = apiKey || GEMINI_API_KEY;
  genAI = new GoogleGenerativeAI(key);
  model = genAI.getGenerativeModel({ model: 'gemini-2.0-flash' });
}

export function isGeminiReady() {
  return model !== null;
}

// Auto-initialize on import
initGemini(GEMINI_API_KEY);

export async function summarizeText(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are an expert academic tutor. Summarize the following academic content clearly and concisely for engineering students. Make it easy to understand while covering all key concepts.

Content:
${safeText}

Provide a well-structured summary with bullet points and headings where appropriate.`;
  const result = await model.generateContent(prompt);
  return result.response.text();
}

export async function summarizeInKannada(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are an expert academic tutor who is fluent in Kannada. Summarize the following academic content in Kannada language (ಕನ್ನಡ). Make it easy to understand for engineering students while covering all key concepts. Use Kannada script throughout.

Content:
${safeText}

Provide a well-structured summary in Kannada with bullet points and headings where appropriate.`;
  const result = await model.generateContent(prompt);
  return result.response.text();
}

export async function generateQuiz(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are an expert quiz master for engineering students. Based on the following academic content, generate exactly 10 multiple-choice questions. Each question should have 4 options with exactly 1 correct answer.

Content:
${safeText}

Return ONLY a valid JSON array with this exact format (no markdown, no code blocks, just raw JSON):
[
  {
    "question": "Question text here?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctIndex": 0
  }
]

The correctIndex is the 0-based index of the correct option. Make questions that test understanding, not just memorization.`;
  const result = await model.generateContent(prompt);
  let responseText = result.response.text().trim();
  // Remove markdown code blocks if present
  responseText = responseText.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim();
  return JSON.parse(responseText);
}

export async function generateQuestions(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are an expert academic tutor. Based on the following content, generate 10 to 15 important questions that are likely to be asked in exams. These should be a mix of short answer, long answer, and conceptual questions.

Content:
${safeText}

Return ONLY a valid JSON array of strings, each string being one question (no markdown, no code blocks, just raw JSON):
["Question 1?", "Question 2?", ...]`;
  const result = await model.generateContent(prompt);
  let responseText = result.response.text().trim();
  responseText = responseText.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim();
  return JSON.parse(responseText);
}

export async function kannadaExplain(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are a patient and expert tutor who explains concepts in Kannada (ಕನ್ನಡ). Explain the following academic content in Kannada in a very detailed and easy-to-understand manner for engineering students. Use simple Kannada with technical terms transliterated where needed. Include examples.

Content:
${safeText}

Provide a thorough explanation in Kannada (ಕನ್ನಡ ಭಾಷೆಯಲ್ಲಿ).`;
  const result = await model.generateContent(prompt);
  return result.response.text();
}

export async function realWorldExample(text) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeText = truncateInput(text);
  const prompt = `You are an expert teacher who relates academic concepts to real-world examples. For the following academic content, provide clear real-world examples and analogies that make the concepts easy to understand.

Content:
${safeText}

For each major concept:
1. State the concept
2. Provide a clear real-world analogy or example
3. Explain how the analogy maps to the technical concept

Make it engaging and relatable for engineering students.`;
  const result = await model.generateContent(prompt);
  return result.response.text();
}

export async function explainCode(code) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  const safeCode = truncateInput(code);
  const prompt = `You are an expert programming tutor. Explain the following code in detail, line by line. Cover:
1. What the code does overall
2. What each important line/block does
3. Time and space complexity if applicable
4. Any potential issues or improvements

Code:
${safeCode}

Provide a clear, structured explanation suitable for engineering students.`;
  const result = await model.generateContent(prompt);
  return result.response.text();
}

let chatHistory = [];

export async function chatWithBot(message) {
  if (!model) throw new Error('Gemini not initialized');
  checkRateLimit();
  
  chatHistory.push({ role: 'user', content: message });
  
  const conversationContext = chatHistory
    .slice(-10)
    .map(m => `${m.role === 'user' ? 'Student' : 'MindMate AI'}: ${m.content}`)
    .join('\n');

  const prompt = `You are MindMate AI, a friendly and knowledgeable AI study buddy for engineering students. You help with academic topics, explain concepts clearly, and provide examples. Be conversational, encouraging, and use emojis occasionally.

Conversation so far:
${conversationContext}

Respond to the student's latest message helpfully and concisely.`;

  const result = await model.generateContent(prompt);
  const response = result.response.text();
  chatHistory.push({ role: 'bot', content: response });
  return response;
}

export function resetChat() {
  chatHistory = [];
}
