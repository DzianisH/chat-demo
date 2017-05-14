package org.chatbot.sevice;

import org.chatbot.domain.WordVector;
import org.chatbot.repository.WordVectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Created by DzianisH on 12.03.2017.
 */
@Service
public class WordVectorService {
	@Autowired
	private WordVectorRepository repository;

	public static WordVector END;
	public static WordVector NOT;

	@PostConstruct
	public void init() {
		END = getWordVectorByWord("<END>");
		if (END == null) {
			END = new WordVector()
					.withId(1L)
					.withWord("<END>")
					.withVector(new double[100]);
		}
		NOT = getWordVectorByWord("not");
	}

	public WordVector getWordVectorByWord(String word) {
		WordVector vector = getWordVectorByWord111(word);
		if (vector == END) {
			System.out.println("Can't find word: " + word);
		}
		return vector;
	}

	private WordVector getWordVectorByWord111(String word) {
		List<WordVector> vectors = repository.findAllByWord(word);
		if (vectors.size() == 0) {
			return convertToWordVectorFromSubWord(word);
		}

		WordVector vector = vectors.stream()
				.filter(wv -> word.equals(wv.getWord()))
				.findAny()
				.orElse(null);
		if (vector == null) {
			vector = vectors.stream()
					.filter(wv -> word.toLowerCase().equals(wv.getWord().toLowerCase()))
					.findAny()
					.orElse(null);
		}
		if (vector == null) {
			vector = vectors.get(0);
		}

		return vector;
	}


	public WordVector parseToWordVector(String source) {
		String chunks[] = source.split(" ");
		double vector[] = new double[chunks.length - 1];

		for (int i = 0; i < vector.length; ++i) {
			vector[i] = Double.parseDouble(chunks[i + 1]);
		}

		return new WordVector()
				.withWord(chunks[0])
				.withVector(vector);
	}

	public List<String> createTokens(String word) {
		if (word.length() < 2) {
			return singletonList(word);
		}
		final String punctuations = "!?,.\"':-/`\\@#$%^;&*(){}[]<>+~—|";
		String tail = String.valueOf(word.charAt(word.length() - 1));
		if (punctuations.contains(tail)) {
			List<String> tokens = new ArrayList<>();
			tokens.addAll(createTokens(word.substring(0, word.length() - 1)));
			tokens.add(tail);
			return tokens;
		}
		String prefix = String.valueOf(word.charAt(0));
		if (punctuations.contains(prefix)) {
			List<String> tokens = new ArrayList<>();
			tokens.add(prefix);
			tokens.addAll(createTokens(word.substring(1)));
			return tokens;
		}


		String wordRoot = word.substring(0, word.length() - 2);

		if (word.equalsIgnoreCase("can't")) {
			return asList("can", "not");
		} else if (word.endsWith("'s")) {
			return asList(wordRoot, "is");
		} else if (word.endsWith("'d")) {
			return asList(wordRoot, "would");
		} else if (word.endsWith("'m")) {
			return asList(wordRoot, "am");
		}

		if (word.length() < 4) {
			return singletonList(word);
		}

		wordRoot = word.substring(0, word.length() - 3);

		if (word.endsWith("'re")) {
			return asList(wordRoot, "are");
		} else if (word.endsWith("n't")) {
			return asList(wordRoot, "not");
		} else if (word.endsWith("'ve")) {
			return asList(wordRoot, "have");
		} else if (word.endsWith("'ll")) {
			return asList(wordRoot, "will");
		}
		return singletonList(word);
	}

	private WordVector convertToWordVectorFromSubWord(String word) {
		int i = word.lastIndexOf("n't");
		if (i != -1) {
			String prefix = word.substring(0, i + 1);
			WordVector vector = getWordVectorByWord111(prefix + "ot");// append not
			if (vector == END) {
				vector = getWordVectorByWord111(prefix + "not");// for double n, like cannot
			}
			if (vector == END) {
				vector = getWordVectorByWord111(prefix.substring(0, prefix.length() - 1));
				if (vector == END) {
					vector = getWordVectorByWord111(prefix);// for double n, like cannot
				}
				vector = vector.plus(NOT);
			}
			return vector;
		}

		i = word.lastIndexOf("'");
		if (i != -1) {
			return getWordVectorByWord111(word.substring(0, i));
		}

		return END;
	}

	public boolean isEnd(WordVector wordVector) {
		return END.equals(wordVector);
	}

	public boolean isNotEnd(WordVector wordVector) {
		return !END.equals(wordVector);
	}
}
