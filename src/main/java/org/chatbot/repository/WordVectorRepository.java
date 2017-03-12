package org.chatbot.repository;

import org.chatbot.domain.WordVector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by DzianisH on 11.03.2017.
 */
public interface WordVectorRepository extends JpaRepository<WordVector, Long>{

	List<WordVector> findAllByWord(String word);

//	@Query(" CREATE INDEX word_index ON word_vector (word)")
//	public void createIndex();
}
