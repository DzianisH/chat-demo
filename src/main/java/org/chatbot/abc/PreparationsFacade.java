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
import java.util.LinkedList;
import java.util.List;

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

	private int wordVectorsChunkSize = 16384;
	private int maxWordLength = 32;
	private List<WordVector> wordVectors = new LinkedList<>();

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
		System.out.println("It took: " + duration);
	}

	private void performPreparations(Path path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
		final long[] x = {0};
		reader.lines()
				.map(service::parseToWordVector)
				.peek(w -> x[0]++)
				.filter(wv -> wv.getWord().length() < maxWordLength)
				.forEach(this::puckAndSaveToRepo);

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
		// total number of words: 2179072 (2196017)
		double progress = this.processedItems / 2179.072;
		long duration = System.currentTimeMillis() - startTime;
		double eta = ((duration / progress) * (1000.0 - progress)) / 1000.0;
		double speed = progress * 1000.0 / duration;

		System.out.print( new StringBuilder()
				.append("Progress: ")
				.append(progress)
				.append(" ‰\n")
				.append("ETA: ")
				.append(eta)
				.append(" seconds\n")
				.append("Speed ")
				.append(speed)
				.append(" ‰/second\n\n")
		);
	}
}
