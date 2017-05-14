package org.chatbot.abc;

import com.google.common.base.Joiner;
import org.chatbot.domain.WordVector;

import java.util.List;
import java.util.stream.Collectors;

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

	public LabeledTrainData withInput(List<WordVector> input) {
		this.input = input;
		return this;
	}

	public LabeledTrainData withLabels(List<WordVector> labels) {
		this.labels = labels;
		return this;
	}

	@Override
	public String toString() {
		List<String> inputWords = input.stream()
				.map(WordVector::getWord)
				.collect(Collectors.toList());
		List<String> outputWords = labels.stream()
				.map(WordVector::getWord)
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		Joiner joiner = Joiner.on(' ');

		sb = joiner.appendTo(sb, inputWords).append(" -> ");
		return joiner.appendTo(sb, outputWords).toString() + " \n\n";
	}
}
