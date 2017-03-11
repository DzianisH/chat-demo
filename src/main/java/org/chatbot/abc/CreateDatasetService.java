package org.chatbot.abc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
public class CreateDatasetService {

	@Value("${chatdemom.dialogs-path}")
	private String dialogsPath;

	@Value("${chatdemom.dataset-path}")
	private String datasetPath;

	public void createDataset(){
		Path path = Paths.get(dialogsPath);

		try {
			createDataset(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDataset(Path dialogs) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(datasetPath));

		final boolean[] endOfPhrase = {false};
		Files.lines(dialogs)
				.forEach(line -> {
					try {
						out.write(line);
						if (endOfPhrase[0]) {
							out.write(" <END>\n");
						} else{
							out.write(" <END> ");
						}
						endOfPhrase[0] = !endOfPhrase[0];
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}
}
