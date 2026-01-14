import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
		String text = "";
		while (!in.isEmpty()) {
			text += in.readAll();
		}
		for (int i = 0; i < text.length() - windowLength; i++) {
			String window = text.substring(i, i + windowLength);
			char nextChar = text.charAt(i + windowLength);
			if (!CharDataMap.containsKey(window)) {
				CharDataMap.put(window, new List());
			}
			CharDataMap.get(window).update(nextChar);
		}
		for (String window : CharDataMap.keySet()) {
			calculateProbabilities(CharDataMap.get(window));
		}
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		int total = 0;
		ListIterator iter = probs.listIterator(0);
		while (iter.hasNext()) {
			CharData cd = iter.next();
			total += cd.count;
		}
		iter = probs.listIterator(0);
		double cumulative = 0.0;
		while (iter.hasNext()) {
			CharData cd = iter.next();
			cd.p = (double) cd.count / total;
			cumulative += cd.p; 
			cd.cp = cumulative;
		}
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
		ListIterator iter = probs.listIterator(0);
		while (iter.hasNext()) {
			CharData cd = iter.next();
			if (r <= cd.cp) {
				return cd.chr;
			}
		}
		return probs.get(0).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
			return initialText;
		}
		String result = initialText;
		String window = initialText.substring(initialText.length() - windowLength);
		for (int i = 0; i < textLength; i++) {
			if (!CharDataMap.containsKey(window)) {
				return result;
			}
			List probs = CharDataMap.get(window);
			char nextChar = getRandomChar(probs);
			result += nextChar;
			window = window.substring(1) + nextChar;
		}
		return result;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
