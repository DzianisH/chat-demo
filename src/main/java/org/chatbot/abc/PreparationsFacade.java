package org.chatbot.abc;

import org.chatbot.domain.WordVector;
import org.chatbot.repository.WordVectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
//@Scope("prototype")
public class PreparationsFacade {
	private final WordVectorService service;
	private final WordVectorRepository repository;

	@Value("${chatdemom.dictionary-path}")
	private String dictionaryPath;

	final private int wordVectorsChunkSize = 32_768;
	final private int maxWordLength = 32;
	final private List<WordVector> wordVectors = new LinkedList<>();

	private long processedItems = 0;
	private long startTime;

	@Autowired
	public PreparationsFacade(WordVectorService wordVectorService, WordVectorRepository repository) {
		this.service = wordVectorService;
		this.repository = repository;
	}

	@PostConstruct
	public void performPreparations() {
		Path path = Paths.get(dictionaryPath);

		try {
			startTime = System.currentTimeMillis();
			performPreparations(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double duration = (System.currentTimeMillis() - startTime) / 1000.0;
		System.out.println("Process items: " + processedItems);
		System.out.println("It took: " + duration + " seconds");

		System.out.println("\n\nPlease, execute manually:\n\tcreate INDEX word_index ON word_vector (word);\n\n");
	}

	private void performPreparations(Path path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
		reader.lines()
				.map(service::parseToWordVector)
				.filter(wv -> wv.getWord().length() < maxWordLength)
				.forEach(this::puckAndSaveToRepo);

		if(wordVectors.size() > 0) {
			repository.save(wordVectors);
			processedItems += wordVectors.size();
		}
		repository.flush();
	}

	private void puckAndSaveToRepo(WordVector wordVector) {
		wordVectors.add(wordVector);
		if(wordVectors.size() >= wordVectorsChunkSize){
			repository.save(wordVectors);
			printProgress(wordVectors.size());
			wordVectors.clear();
		}
	}

	private void printProgress(long processedItems){
		this.processedItems += processedItems;
		// total number of words: 400 000
		double progress = this.processedItems / 400.0;
		long duration = System.currentTimeMillis() - startTime;
		double eta = ((duration / progress) * (1000.0 - progress)) / 1000.0;
		double speed = progress * 1000.0 / duration;

		System.out.print( new StringBuilder()
				.append("Progress: \t")
				.append(progress)
				.append(" ‰\n")
				.append("ETA:      \t")
				.append(eta)
				.append(" seconds\n")
				.append("Speed:    \t")
				.append(speed)
				.append(" ‰/second\n\n")
		);
	}
}
