package org.chatbot.model;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.IOException;

/**
 * Created by DzianisH on 01.02.2017.
 */
public class Runner {
	public static void main(String[] args) throws IOException, InterruptedException {
		Runner runner = new Runner();
		int epoch = 51;
		try {
			epoch = Integer.parseInt(System.getProperty("epoch"));
		} catch (RuntimeException re){
		}
		runner.run(epoch);
	}

	private void run(int epoch) throws IOException, InterruptedException {
		System.out.println("Initialisation");
		DataSetIterator trainDataset = createDatasetIterator("train.csv");
		DataSetIterator validationDataset = createDatasetIterator("cv.csv");

		CoachPotato coachPotato = new CoachPotato()
				.withEpohs(epoch)
				.withNumOfClasses(new ModelFactory().getNumOfClasses())
				.withTrainDataset(trainDataset)
				.withValidationDataset(validationDataset);
		MultiLayerConfiguration conf = new ModelFactory().createConfiguration();

		System.out.println(conf.toJson());
		MultiLayerNetwork network = new MultiLayerNetwork(conf);

		System.out.println("Initializing network");
//		network.setListeners(new ScoreIterationListener(1));
		network.init();

		System.out.println("Printing size of every layer");
		printTotalParamsInfo(network);

		trainDataset.reset();


		System.out.println("Training network\n");
		coachPotato.fit(network);
		System.out.println("\nModel trained.");

		System.out.println(
				"\nBest Train accuracy: " + coachPotato.getBestTrainAccuracy() + "%\n"
						+ "Best CV accuracy: " + coachPotato.getBestCvAccuracy() + "%\n"
						+ "Best Train/CV ratio: " + coachPotato.getBestTestToCvAccuracyRation()
		);
	}

	private void printTotalParamsInfo(MultiLayerNetwork network) {
		int totatlParams = 0;
		for(String name : network.getLayerNames()){
			Layer layer = network.getLayer(name);
			int params = layer.numParams();
			System.out.println("\t" + name + " contain " + params + " params");
			totatlParams += params;
		}
		System.out.println("Total number of parameters: " + totatlParams);
	}

	private DataSetIterator createDatasetIterator(String from) throws IOException, InterruptedException {
		return null;
//		RecordReader rr = new CSVRecordReader();
//		rr.initialize(new FileSplit(new ClassPathResource(from).getFile()));
//
//		return new RecordReaderDataSetIterator(rr,5000, 3072, 10);
	}
}
