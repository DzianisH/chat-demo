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

	public void createDataset(){
		Path conversations = Paths.get(moviceConversationsPath);
		Path lines = Paths.get(movieLinesPath);
		try {
			createDataset(conversations, lines, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDataset(Path conversationsPath, Path movieLinesPath, File inputs, File labels) throws IOException{
		Map<Integer, String> linesMap = Files.lines(movieLinesPath)
				.map(Phrase::new)
				.collect(Collectors.toMap(Phrase::getLine, Phrase::getText));

		Files.lines(conversationsPath)
				.flatMap(line -> extractPairs(line, linesMap).stream())
				.peek(this::tokenizeSentences)
				.map(this::createTrainData)
				.forEach(x -> {});

	}

	private LabeledTrainData createTrainData(QaPair pair){
		return new LabeledTrainData()
				.withInput(createInputVector(pair.getQuestion()))
				.withLabels(convertToWordVectors(pair.getAnswer()));
	}

	private List<WordVector> createInputVector(String question) {
		List<WordVector> vectors = convertToWordVectors(question);

		int len = INPUT_HEIGHT - 1;
		int length = len < vectors.size() ? len : vectors.size();

		List<WordVector> inputs = new ArrayList<>(vectors.subList(0, length));
		List<WordVector> tail = new ArrayList<>(vectors.subList(length, vectors.size()));

		while (inputs.size() < len){
			inputs.add(END);
		}

		// reduce final tail to 1 word
		WordVector finalWord = tail.stream()
				.reduce(END, WordVector::plus);


		inputs.add(finalWord);

		return inputs;
	}

	private List<WordVector> convertToWordVectors(String answer) {
		return Arrays.stream(answer.split("[ ]+"))
				.filter(word -> !word.isEmpty())
				.map(service::getWordVectorByWord)
				.collect(Collectors.toList());
	}

	private void tokenizeSentences(QaPair qaPair) {
		qaPair.answer = tokenizeSentence(qaPair.answer);
		qaPair.question = tokenizeSentence(qaPair.question);
	}

	private String tokenizeSentence(String sentence){
		StringBuilder tokenized = new StringBuilder();

		Arrays.stream(sentence.split("[ ]+"))
				.filter(word -> !word.isEmpty())
				.forEach(word -> {
					service.createTokens(word)
							.forEach(token -> {
								tokenized.append(token).append(" ");
							});
				});

		return tokenized.toString();
	}

	private List<QaPair> extractPairs(String conversation, Map<Integer, String> linesMap){
		int index = conversation.lastIndexOf(DELIMITER);
		conversation = conversation.substring(index + DELIMITER.length());
		conversation = conversation.replaceAll("[L'\\[\\],]+", "");

		String[] senseLines = conversation.split(" ");

		// take only even number of sentences;
		int conversationLength = senseLines.length & ~1;

		List<QaPair> pairs = new ArrayList<>();
		for (int i = 0; i < conversationLength; i += 2){
			QaPair pair = new QaPair();
			pair.question = linesMap.get(Integer.valueOf(senseLines[i]));
			pair.answer = linesMap.get(Integer.valueOf(senseLines[i + 1]));
			pairs.add(pair);
		}
		return pairs;
	}

	static class QaPair {
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

		Phrase(String str){
			int i = str.lastIndexOf(DELIMITER);
			text = str.substring(i + DELIMITER.length());

			str = str.substring(0, i);
			str = str.split(" ")[0].substring(1);
			line = Integer.parseInt(str);
		}

		Integer getLine(){
			return line;
		}

		String getText(){
			return text;
		}
	}
}
