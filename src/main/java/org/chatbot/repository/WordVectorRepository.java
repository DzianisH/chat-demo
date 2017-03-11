package org.chatbot.repository;

import org.chatbot.domain.WordVector;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by DzianisH on 11.03.2017.
 */
public interface WordVectorRepository extends JpaRepository<WordVector, Long>{
}
