package org.locus.main;

import de.broccoli.context.BroccoliContext;

import java.util.HashMap;

public class Main {

	private static ThreadLocal<Main> threadLocal = new ThreadLocal<Main>();

	public static Main getInstance()
	{
		if(threadLocal.get() == null)
		{
			threadLocal.set(new Main());
		}
		return threadLocal.get();
	}


	public HashMap<String,String> settings;
	public String task = "all";
	public String repoDir = "";
	public String workingLoc = "";
	public String bugReport = "";
	public String changeOracle = "";
	public String sourceDir = "";
	public String projectName = "";
	public String versionName = "";
	public double lambda = 5;
	public double belta1 = 0.1;
	public double belta2 = 0.2;

	public boolean loadConfigure() throws Exception {
		settings = new HashMap<String,String>();


		projectName = BroccoliContext.getInstance().getProjectName();
		versionName = "";
		repoDir = BroccoliContext.getInstance().getSourceCodeDir();
		workingLoc = BroccoliContext.getInstance().getWorkDir();
		sourceDir = BroccoliContext.getInstance().getSourceCodeDir();

		//setting change
		String outputFile = BroccoliContext.getInstance().getOutputFile();

		settings.put("outputFile", outputFile);

		if (task.equals("") || repoDir.equals("") || workingLoc.equals("")
				|| bugReport.equals("") || changeOracle.equals("") || projectName.equals("") || versionName.equals("")) {
			System.out.println("Warning Required Configuration");
			// return false;
		}
		return true;
	}

	public void main(String[] args) throws Exception {
		boolean flag = false;
		if (args.length > 0) {
		//	flag = loadConfigure();
		} else {
			System.out.println("Using default configuration file");
		//	flag = loadConfigure("./config.txt");
		}

		if (flag == true){
			System.out.println("working with " + projectName + " / " + versionName);
			Core.startTask();
		}
		else
			System.err.println("Error!, stop program..");

	}
}
