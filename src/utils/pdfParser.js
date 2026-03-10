import * as pdfjsLib from 'pdfjs-dist';

// Set worker source
pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.mjs`;

export async function extractTextFromPDF(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = async (event) => {
      try {
        const typedArray = new Uint8Array(event.target.result);
        const pdf = await pdfjsLib.getDocument({ data: typedArray }).promise;
        let fullText = '';
        
        for (let i = 1; i <= pdf.numPages; i++) {
          const page = await pdf.getPage(i);
          const textContent = await page.getTextContent();
          const pageText = textContent.items.map(item => item.str).join(' ');
          fullText += pageText + '\n\n';
        }
        
        resolve(fullText.trim());
      } catch (error) {
        reject(new Error('Failed to extract text from PDF: ' + error.message));
      }
    };
    reader.onerror = () => reject(new Error('Failed to read file'));
    reader.readAsArrayBuffer(file);
  });
}

export async function extractTextFromImage(file) {
  // For image files, we'll convert to base64 and return a description
  // In a real app, you'd use OCR. For now, return the filename info.
  return new Promise((resolve) => {
    resolve(`[Image uploaded: ${file.name}] - Please describe the content you'd like analyzed from this image.`);
  });
}

export function getFileText(file) {
  if (file.type === 'application/pdf') {
    return extractTextFromPDF(file);
  } else if (file.type.startsWith('image/')) {
    return extractTextFromImage(file);
  } else {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target.result);
      reader.onerror = () => reject(new Error('Failed to read file'));
      reader.readAsText(file);
    });
  }
}
