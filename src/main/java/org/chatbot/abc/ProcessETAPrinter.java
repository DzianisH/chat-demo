package org.chatbot.abc;

import static java.lang.System.currentTimeMillis;

public class ProcessETAPrinter {

	private long processedItems;
	private long startTime;
	private final double totalElements;

	public ProcessETAPrinter(double totalElements){
		startTime = currentTimeMillis();
		this.totalElements = totalElements;
	}

	public void printProgress(long processedItems) {
		this.processedItems += processedItems;
		double progress = (this.processedItems / totalElements) * 1000;
		long duration = currentTimeMillis() - startTime;
		double eta = ((duration / progress) * (1000.0 - progress)) / 1000.;
		double speed = progress * 1000.0 / duration;

		System.out.print(new StringBuilder()
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

	public void printResultProgress(){
		double duration = (System.currentTimeMillis() - startTime) / 1000.0;
		System.out.println("Process items: " + processedItems);
		System.out.println("It took: " + duration + " seconds");
	}
}
