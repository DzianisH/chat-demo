package org.chatbot.abc;

import org.chatbot.domain.WordVector;
import org.chatbot.sevice.WordVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.chatbot.sevice.WordVectorService.END;


/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
public class CreateDatasetService {

	@Autowired
	private WordVectorService service;

	@Value("${chatdemo.movie-lines}")
	private String movieLinesPath;

	@Value("${chatdemo.movie-conversations}")
	private String moviceConversationsPath;

	private final int INPUT_HEIGHT = 32;

	final private static String DELIMITER = " +++$+++ ";

	private ProcessETAPrinter etaPrinter;

	public void createDataSet() {
		Path conversations = Paths.get(moviceConversationsPath);
		Path lines = Paths.get(movieLinesPath);
		try {
			etaPrinter = new ProcessETAPrinter(138_134);
			createDataSet(conversations, lines, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDataSet(Path conversationsPath, Path movieLinesPath, File inputs, File labels) throws IOException {
		Map<Integer, String> linesMap = Files.lines(movieLinesPath)
				.map(Phrase::new)
				.collect(toMap(Phrase::getLine, Phrase::getText));

		final int[] i = {0};
		Files.lines(conversationsPath)
				.flatMap(line -> extractPairs(line, linesMap).stream())
				.map(this::createTrainData)
				.forEach(k -> {
					i[0]++;
					if((i[0] & 511) == 0){
						etaPrinter.printProgress(512);
					}
				});
		etaPrinter.printResultProgress();
	}

	private LabeledTrainData createTrainData(QAPair pair) {
		return new LabeledTrainData()
				.withInput(createInputVectors(pair.getQuestion()))
				.withLabels(createOutputVectors(pair.getAnswer()));
	}

	private List<WordVector> createOutputVectors(String pair) {
		List<WordVector> list = convertToWordVectors(pair).stream()
				.filter(service::isNotEnd)
				.collect(toList());
		list.add(END);
		return list;
	}

	private List<WordVector> createInputVectors(String question) {
		List<WordVector> vectors = convertToWordVectors(question);

		int len = INPUT_HEIGHT - 1;
		int length = len < vectors.size() ? len : vectors.size();

		List<WordVector> inputs = new ArrayList<>(vectors.subList(0, length));
		List<WordVector> tail = new ArrayList<>(vectors.subList(length, vectors.size()));

		while (inputs.size() < len) {
			inputs.add(END);
		}

		// reduce final tail to 1 word
		WordVector finalWord = tail.stream()
				.reduce(END, WordVector::plus);


		inputs.add(finalWord);

		return inputs;
	}

	private List<WordVector> convertToWordVectors(String answer) {
		return stream(answer.split("[ ]+"))
				.filter(word -> !word.isEmpty())
				.map(service::getWordVectorByWord)
				.collect(toList());
	}

	private void tokenizeSentences(QAPair qaPair) {
		qaPair.answer = tokenizeSentence(qaPair.answer);
		qaPair.question = tokenizeSentence(qaPair.question);
	}

	private String tokenizeSentence(String sentence) {
		return stream(sentence.split("[ ]+"))
				.filter(word -> !word.isEmpty())
				.flatMap(word -> service.createTokens(word).stream())
				.collect(joining(" "));
	}

	private List<QAPair> extractPairs(String conversation, Map<Integer, String> linesMap) {
		int index = conversation.lastIndexOf(DELIMITER);
		conversation = conversation.substring(index + DELIMITER.length());
		conversation = conversation.replaceAll("[L'\\[\\],]+", "");

		String[] senseLines = conversation.split("\\s");

		// take only even number of sentences;
		int conversationLength = senseLines.length & ~1;

		List<QAPair> pairs = new ArrayList<>();
		for (int i = 0; i < conversationLength; i += 2) {
			QAPair pair = new QAPair();
			pair.question = linesMap.get(Integer.valueOf(senseLines[i]));
			pair.answer = linesMap.get(Integer.valueOf(senseLines[i + 1]));
			tokenizeSentences(pair);
			pairs.add(pair);
		}
		return pairs;
	}

	static class QAPair {
		String question, answer;

		public String getQuestion() {
			return question;
		}

		public String getAnswer() {
			return answer;
		}

		@Override
		public String toString() {
			return "[ " + question + " ] -> [ " + answer + " ];";
		}
	}

	static class Phrase {
		Integer line;
		String text;

		Phrase(String str) {
			int i = str.lastIndexOf(DELIMITER);
			text = str.substring(i + DELIMITER.length());

			str = str.substring(0, i);
			str = str.split("\\s")[0].substring(1);
			line = Integer.parseInt(str);
		}

		Integer getLine() {
			return line;
		}

		String getText() {
			return text;
		}
	}
}
