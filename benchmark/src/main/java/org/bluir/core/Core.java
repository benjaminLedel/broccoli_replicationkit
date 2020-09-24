package org.bluir.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import de.broccoli.BLAlgorithm;
import de.broccoli.context.BroccoliContext;
import de.broccoli.utils.indri.IndriUtil;
import org.bluir.evaluation.Evaluation;
import org.bluir.extraction.FactExtractor;
import org.bluir.extraction.QueryExtractor;
import org.eclipse.core.runtime.CoreException;

public class Core implements BLAlgorithm {
	private final String indriBinPath =  BroccoliContext.getInstance().getIndriPath();

	private final String docsLocation = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator() + "docs";
	private final String indexLocation = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" +  BroccoliContext.getInstance().getSeparator() + "index";
	private final String queryFilePath = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator() + "query";
	private final String parameterFilePath = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator() + "parameter.xml";
	private final String indriQueryResult = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator() +"indriQueryResult";
	private final String workDir = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR";
	private int topN = BroccoliContext.getInstance().getTopN();

	public void run() {
		File workspace = new File(workDir);
		workspace.mkdirs();
		if (!createQueryIndex())
			return;
		if (!createDocs())
			return;
		if (!index())
			return;
		if (!retrieve())
			return;
		if (!evaluation())
			return;

		System.out.println("finished");
	}

	boolean createQueryIndex() {
			System.out.print("create query...");
		try {
			int repoSize = QueryExtractor.extractSumDesField(queryFilePath);
			System.out.println(repoSize + " created successfully :-)");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	boolean createDocs() {
		try {
			System.out.print("create docs...");

			if (!FactExtractor.extractEclipseFacts(BroccoliContext.getInstance().getSourceCodeDir(), docsLocation))
				return false;

			int fileCount = BroccoliContext.getInstance().getContextVar("fileCount");
			System.out.println(fileCount + " file processed!");
			topN = fileCount;

		} catch (IOException | CoreException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.err.println("Error occurs when we're creating docs folder!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	boolean index() {
		try {
			System.out.print("Create indexes....");

			// index Dir ���� ����.
			File indexDir = new File(indexLocation);
			if (!indexDir.exists())
				if (!indexDir.mkdirs())
					throw new Exception();

			IndriUtil.createParametersFile(parameterFilePath,indexLocation,docsLocation);

			// program command
			String command = indriBinPath + "IndriBuildIndex\" " + parameterFilePath;

			// execute command
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.workDir + BroccoliContext.getInstance().getSeparator() + "IndexLog.txt"));

			Process p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				bw.write(line + "\n");
			}
			p.waitFor();
			bw.close();

			// executeIndexCommand(command);
			System.out.println("successfully Done!");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error occurs while we're working with file IO");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error occurs while we're working with process");
			System.err.println("Stopping Execution....");
			return false;
		}
		return true;
	}

	boolean retrieve() {
		System.out.print("Retrieval is in progress...");

		BufferedWriter bw = null;
		try {

			String command = indriBinPath + "IndriRunQuery\" " + queryFilePath + " -count=" + topN
					+ " -index=" + indexLocation + " -trecFormat=true -rule=method:tfidf,k1:1.0,b:0.3";

			bw = new BufferedWriter(new FileWriter(this.indriQueryResult));

			Process p = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
			p.waitFor();

			System.out.println("Done!");

		} catch (IOException e1) {
			System.out.println("Problems with result file io");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Problems in closing results file after writing.");
				return false;
			}

		}
		return true;
	}

	boolean evaluation() {
		try {
			System.out.print("Evaluating....");

			new Evaluation().evaluate();

			System.out.println("Done!");

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
