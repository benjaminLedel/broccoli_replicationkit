package org.amalgam;

import org.amalgam.analysis.BugReportParser;
import org.amalgam.analysis.CodeRepository;
import org.amalgam.analysis.SimiLoader;
import org.amalgam.analysis.VersionHistoryCalculator;
import org.amalgam.common.Weights;
import org.amalgam.evaluate.Evaluation;
import org.amalgam.models.Bug;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

public class Core {

	private final int days_back = 15;

	/**
	 * Constructor
	 */
	public Core(){
		// TODO Auto-generated method stub
		//String projectRoot = "E:\\research\\buglocatortool_data\\ZXing-1.6_result";
		//String repo = "E:\\research\\buglocatortool_data\\zxing_repo";

		//String projectRoot = "data\\swt-3.1_result";
		//String repo = "data\\swt_repo";

		//String projectRoot = "E:\\research\\buglocatortool_data\\aspectj_result";
		//String repo = "E:\\research\\buglocatortool_data\\aspectj_repo";

		//String projectRoot = "E:\\research\\buglocatortool_data\\eclipse-3.1_result";
		//String repo = "E:\\research\\buglocatortool_data\\eclipse_repo";

	}

	public void process() throws IOException, ParseException {

		System.out.print("Load commit information from git....");
		CodeRepository codeRepo = new CodeRepository();
		codeRepo.loadCommits();
		System.out.println("Done.");


		//���׸���Ʈ ���� �ε� (BugID, commitDate, FixedFiles)
		System.out.print("Load bug report information from bug repository....");
		HashMap<String, Bug> bugObjs = BugReportParser.loadBugReports();
		System.out.println("Done.");


		//git �� ������ �̿��ؼ� commit date update
		System.out.print("update commit date in bug information ....");
		codeRepo.getCommitDateOfBugObj(bugObjs);
		System.out.println("Done.");

		//BLUiR ����
		System.out.println("copy BLUiR result....");
		if (!SimiLoader.create()){
			System.out.println("Error!!");
			return ;
		}
		System.out.println("Done.");

		//BLUiR ��� �ε�
		System.out.print("Load BLUiR results....");
		if (!SimiLoader.load(bugObjs)){
			System.out.println("Error!!");
			return ;
		}
		System.out.println("Done.");


		//historical score ���.
		System.out.print("calculate historical score ....");
		VersionHistoryCalculator historyCalc = new VersionHistoryCalculator(codeRepo.loadFileCommitHistory());
		for (Bug bug : bugObjs.values()) {
			bug.historicalScores = historyCalc.computeBugSuspeciousScore(bug,  days_back, 0); // ����n���� �������� ����. (�ʱⰪ 25����)
		}
		historyCalc.storeScores(bugObjs);
		System.out.println("Done.");


		//setting weights
		HashMap<String, Double> weights = new HashMap<String, Double>();
		weights.put(Weights.BugSimilarityName, 0.5);
		weights.put(Weights.HistoricalScoreName, 0.2);

		// evaluation
		System.out.println("evauate AmaLgam....\n");
		Evaluation ev = new Evaluation();
		ev.run(bugObjs, weights);
		System.out.println("\nDone.");

		System.out.println("\nFinally Done.!!");
	}


}
