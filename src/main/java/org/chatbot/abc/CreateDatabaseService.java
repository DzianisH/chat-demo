package org.chatbot.abc;

import org.chatbot.domain.WordVector;
import org.chatbot.repository.WordVectorRepository;
import org.chatbot.sevice.WordVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
public class CreateDatabaseService {
	@Autowired
	private WordVectorService service;
	@Autowired
	private WordVectorRepository repository;

	@Value("${chatdemo.dictionary-path}")
	private String dictionaryPath;

	final private int wordVectorsChunkSize = 32_768;
	final private int maxWordLength = 32;
	final private List<WordVector> wordVectors = new LinkedList<>();
	private ProcessETAPrinter etaPrinter;

	public void createDatabase() {
		Path path = Paths.get(dictionaryPath);

		try {
			etaPrinter = new ProcessETAPrinter(400);
			createDatabase(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		etaPrinter.printResultProgress();

		System.out.println("\n\nPlease, execute manually:\n\tcreate INDEX word_index ON word_vector (word);\n\n");

		try {
			System.out.println("Sleeping for 20 seconds.");
			Thread.sleep(20 * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void createDatabase(Path path) throws IOException {
		// add key string;
		repository.saveAndFlush(new WordVector()
				.withId(1L)// 0
				.withWord("<END>")
				.withVector(new double[100]));

		BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
		reader.lines()
				.map(service::parseToWordVector)
				.filter(wv -> wv.getWord().length() < maxWordLength)
				.forEach(this::puckAndSaveToRepo);

		if (wordVectors.size() > 0) {
			repository.save(wordVectors);
			etaPrinter.printProgress(wordVectors.size());
		}

		repository.flush();
	}

	private void puckAndSaveToRepo(WordVector wordVector) {
		wordVectors.add(wordVector);
		if (wordVectors.size() >= wordVectorsChunkSize) {
			repository.save(wordVectors);
			etaPrinter.printProgress(wordVectors.size());
			wordVectors.clear();
		}
	}
}
