package org.chatbot.abc;

import org.chatbot.domain.WordVector;

import java.util.List;

/**
 * Created by DzianisH on 12.03.2017.
 */
public class LabeledTrainData {
	private List<WordVector> input;
	private List<WordVector> labels;

	public List<WordVector> getInput() {
		return input;
	}

	public void setInput(List<WordVector> input) {
		this.input = input;
	}

	public List<WordVector> getLabels() {
		return labels;
	}

	public void setLabels(List<WordVector> labels) {
		this.labels = labels;
	}

	public LabeledTrainData withInput(List<WordVector> input){
		this.input = input;
		return this;
	}

	public LabeledTrainData withLabels(List<WordVector> labels){
		this.labels = labels;
		return this;
	}
}
