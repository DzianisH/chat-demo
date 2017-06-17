package org.chatbot.sevice;

import org.chatbot.domain.WordVector;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WordVectorCacheService {
	private final static int MAX_CACHE_SIZE = 3000;
	private final Map<String, Accuracies> cache = new ConcurrentHashMap<>();

	@PostConstruct
	private void init(){
		Thread gcThread = new Thread(new GC());
		gcThread.setDaemon(true);
		gcThread.start();
	}

	public List<WordVector> findAllByWord(String word) {
		Accuracies accuracies = cache.get(word.toLowerCase());
		if(accuracies == null){
			return null;
		}
		accuracies.accuracies++;
		return accuracies.vectors;
	}

	public void	putPair(String word, List<WordVector> vectors){
		word = word.toLowerCase();
		cache.put(word, new Accuracies(vectors, word));
	}

	private static class Accuracies implements Comparable<Accuracies> {
		private int accuracies;
		private List<WordVector> vectors;
		private String word;

		private Accuracies(List<WordVector> w2vs, String word){
			vectors = w2vs;
			this.word = word;
		}

		@Override
		public int compareTo(Accuracies other) {
			return this.accuracies - other.accuracies;
		}
	}

	private class GC implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException ignored) {}
				doGC();
			}
		}

		private void doGC(){
			int nToRemove = cache.size() - MAX_CACHE_SIZE;
			if(nToRemove > 0){
				PriorityQueue<Accuracies> heap = new PriorityQueue<>(nToRemove);
				for (Accuracies acc : cache.values()){
					heap.add(acc);
				}
				for(int i = 0; i < nToRemove; ++i){
					Accuracies rareUsed = heap.poll();
					cache.remove(rareUsed.word);
				}
			}
		}
	}
}
