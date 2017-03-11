package org.chatbot.domain;

import javax.annotation.MatchesPattern;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Entity
public class WordVector {
	@Id @GeneratedValue
	@Column(nullable = false, unique = true)
	private Long id;

	@Column(length = 31, nullable = false)
	private String word;

	@Column(length = 300, nullable = false)
	private double vector[];


	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public double[] getVector() {
		return vector;
	}

	public void setVector(double[] vector) {
		this.vector = vector;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WordVector withId(Long id){
		setId(id);
		return this;
	}

	public WordVector withWord(String word){
		setWord(word);
		return this;
	}

	public WordVector withVector(double[] vector){
		setVector(vector);
		return this;
	}

	public String toString(){
		return id + " " + word + " " + vector.length;
	}
}
