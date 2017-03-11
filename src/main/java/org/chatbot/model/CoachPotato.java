package org.chatbot.model;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

/**
 * Created by DzianisH on 25.02.2017.
 */
public class CoachPotato {
	private DataSetIterator trainDataset, validationDataset;
	private double bestCvAccuracy = -1, bestTrainAccuracy;
	private MultiLayerNetwork bestNetwork;
	private int epohs, numOfClasses;

	public MultiLayerNetwork fit(MultiLayerNetwork model){
		trainDataset.reset();
		int epoch = 0;
		printStats(model, epoch);
		while (++epoch < epohs){
			trainDataset.reset();
			model.fit(trainDataset);
			printStats(model, epoch);
		}
		return bestNetwork;
	}

	private void printStats(MultiLayerNetwork model, int epoh){
		Evaluation trainEvaluation = getEvaluation(model, trainDataset);
		Evaluation cvEvaluation = getEvaluation(model, validationDataset);

		String message = "\n";
		if (cvEvaluation.accuracy() >= bestCvAccuracy){
			bestCvAccuracy = cvEvaluation.accuracy();
			bestTrainAccuracy = trainEvaluation.accuracy();
			bestNetwork = model.clone();
			message = "\n\t---currently best net!";
		}

		String stats = new StringBuilder()
				.append("\n\n==================================================\nEpoch:")
				.append(epoh)
				.append("\n--------------------------------------------------\n")

				.append("Train:\n")
				.append("\tAccuracy: \t").append(trainEvaluation.accuracy())
				.append("\n\tPrecision: \t").append(trainEvaluation.precision())
				.append("\n\tRecall: \t").append(trainEvaluation.recall())
				.append("\n\tF1: \t\t").append(trainEvaluation.f1())

				.append("\nCV:\n")
				.append("\tAccuracy: \t").append(cvEvaluation.accuracy())
				.append("\n\tPrecision: \t").append(cvEvaluation.precision())
				.append("\n\tRecall: \t").append(cvEvaluation.recall())
				.append("\n\tF1: \t\t").append(cvEvaluation.f1())

				.append("\n\nAccuracy ratio: \t").append(trainEvaluation.accuracy() / cvEvaluation.accuracy())
				.append("\nF1 ratio: \t\t\t").append(trainEvaluation.f1() / cvEvaluation.f1())
				.append(message)
				.append("\n==================================================")
				.toString();

		System.out.println(stats);
	}

	private Evaluation getEvaluation(MultiLayerNetwork model, DataSetIterator ds){
		ds.reset();
		Evaluation evaluation = model.evaluate(ds);
		ds.reset();
		return evaluation;
	}


	private void persistModel(MultiLayerNetwork network, String prefix) throws IOException {
		int accuracy = (int) (100*bestCvAccuracy + 0.5);
		Formatter formatter = new Formatter();
		String modelFileName = formatter.format("%sdigits-recognizer-%04d.chatbot", prefix, accuracy)
				.toString();
		System.out.println("Persisting " + modelFileName);
		File modelFile = new File(modelFileName);
		modelFile.getParentFile().mkdirs();

		ModelSerializer.writeModel(network, modelFile, true);
	}

	private void persistModel(MultiLayerNetwork network) throws IOException {
		persistModel(network, "");
	}

	public DataSetIterator getTrainDataset() {
		return trainDataset;
	}

	public CoachPotato withTrainDataset(DataSetIterator trainDataset) {
		this.trainDataset = trainDataset;
		return this;
	}

	public DataSetIterator getValidationDataset() {
		return validationDataset;
	}

	public CoachPotato withValidationDataset(DataSetIterator validationDataset) {
		this.validationDataset = validationDataset;
		return this;
	}

	public double getBestCvAccuracy() {
		return bestCvAccuracy;
	}

	public void setBestCvAccuracy(double bestCvAccuracy) {
		this.bestCvAccuracy = bestCvAccuracy;
	}

	public MultiLayerNetwork getBestNetwork() {
		return bestNetwork;
	}

	public void setBestNetwork(MultiLayerNetwork bestNetwork) {
		this.bestNetwork = bestNetwork;
	}

	public double getBestTrainAccuracy() {
		return bestTrainAccuracy;
	}

	public double getBestTestToCvAccuracyRation(){
		return bestCvAccuracy / bestTrainAccuracy;
	}

	public void setBestTrainAccuracy(double bestTrainAccuracy) {
		this.bestTrainAccuracy = bestTrainAccuracy;
	}

	public int getEpohs() {
		return epohs;
	}

	public CoachPotato withEpohs(int epohs) {
		this.epohs = epohs;
		return this;
	}

	public int getNumOfClasses() {
		return numOfClasses;
	}

	public CoachPotato withNumOfClasses(int numOfClasses) {
		this.numOfClasses = numOfClasses;
		return this;
	}
}
