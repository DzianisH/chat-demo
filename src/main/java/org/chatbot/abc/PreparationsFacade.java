package org.chatbot.abc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by DzianisH on 11.03.2017.
 */
@Service
public class PreparationsFacade {

	@Autowired
	private CreateDatasetService datasetService;
	@Autowired
	private CreateDatabaseService databaseService;

	@PostConstruct
	public void preformPreparations() {
		System.out.println("Create database...");
		databaseService.createDatabase();

		System.out.println("Create dataset...");
		datasetService.createDataset();
	}
}
