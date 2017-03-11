package org.chatbot.abc;

import org.chatbot.domain.WordVector;
import org.springframework.stereotype.Service;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
public class WordVectorService {

	public WordVector parseToWordVector(String source){
		String chunks[] = source.split(" ");
		double vector[] = new double[chunks.length -1];

		for (int i = 0; i < vector.length; ++i){
			vector[i] = Double.parseDouble(chunks[i + 1]);
		}

		return new WordVector()
				.withWord(parseStringToSql(chunks[0]))
				.withVector(vector);
	}

	private String parseStringToSql(String str){
		str = str.trim();
		StringBuilder sb = new StringBuilder(str.length());

		for (int i = 0; i < str.length(); ++i){
			char ch = str.charAt(i);
			if(ch == '\\' || ch == '\'' || ch == '"' || ch == '/'){
				sb.append('\\');
			}
			sb.append(ch);
		}
		return sb.toString();
	}
}
