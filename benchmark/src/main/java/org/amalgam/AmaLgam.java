package org.amalgam;

import de.broccoli.BLAlgorithm;
import de.broccoli.context.BroccoliContext;
import org.amalgam.common.Property;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class AmaLgam implements BLAlgorithm {

	public AmaLgam()
	{
		// nothing, just for the interface
	}

	public void run() {

		try {

			boolean isLegal = parseArgs();
			if (!isLegal)
				throw null;

		} catch (Exception ex) {
			showHelp();
			return;
		}


		Core core = new Core();
		try {
			core.process();
		} catch (IOException | ParseException e) {
			System.err.println("Error occured in working");
			e.printStackTrace();
		}
	}


	/**
	 *
	 */
	private static void showHelp() {
		String usage = "Usage:java -jar AmaLgam.jar [-options] \r\n"
				+ "where options must include:\r\n"
				+ "-b\tindicates the bug information file\r\n"
				+ "-s\tindicates the source code directory\r\n"
				+ "-g\tindicates the source code repository\r\n"
				+ "-a\tindicates the alpha value for combining vsmScore and simiScore\r\n"
				+ "-w\tindicates the working directory\r\n"
				+ "-n\tindicates the working name (this uses for result file name.)\r\n"
				+ "  \tOn the below of the {working directory}\r\n"
				+ "  \tThis program will make temp directory : AmaLgam_{working name}\\\r\n"
				+ "  \t                and final result file : AmaLgam_{working name}_output.txt";


		System.out.println(usage);
	}

	/**
	 * �Է� �Ķ���͸� �Ľ��Ͽ� Property ��ü ����
	 * @return
	 */
	public static boolean parseArgs() {
		int i = 0;
		String sourceCodeDir = "";
		String alphaStr = "";
		float alpha = 0.3f;
		String outputFile = "";
		String workingPath = "";
		String projectStr = "";
		String gitRepo = "";

		sourceCodeDir = BroccoliContext.getInstance().getSourceCodeDir();
		alpha = BroccoliContext.getInstance().getAlpha();
		workingPath = BroccoliContext.getInstance().getWorkDir();
		projectStr = BroccoliContext.getInstance().getProjectName();
		gitRepo = BroccoliContext.getInstance().getRepoDir();
//
//		while (i < args.length - 1) {
//			} else if (args[i].equals("-s")) {
//				i++;
//				sourceCodeDir = args[i];
//			sourceCodeDir = sourceCodeDir.replace("\\", "/");
//				sourceCodeDir = sourceCodeDir.replace("//", "/");
//			} else if (args[i].equals("-a")) {
//				i++;
//				alphaStr = args[i];
//			} else if (args[i].equals("-w")) {
//				i++;
//				workingPath = args[i];
//				workingPath = workingPath.replace("\\", "/");
//				workingPath = workingPath.replace("//", "/");
//			} else if (args[i].equals("-n")) {
//				i++;
//				projectStr = args[i];
//			} else if (args[i].equals("-g")) {
//				i++;
//				gitRepo = args[i];
//			}
//			i++;
//		}

		boolean isLegal = true;

		if ((sourceCodeDir.equals("")) || (sourceCodeDir == null)) {
			isLegal = false;
			if (!sourceCodeDir.endsWith(BroccoliContext.getInstance().getSeparator())) sourceCodeDir += BroccoliContext.getInstance().getSeparator();
			System.out.println("you must indicate the source code directory");
		}
		if ((!alphaStr.equals("")) && (alphaStr != null)) {
			try {
				alpha = Float.parseFloat(alphaStr);
			} catch (Exception ex) {
				isLegal = false;
				System.out.println("-a argument is ilegal,it must be a float value");
			}
		}
		if (workingPath.equals("") || workingPath == null) {
			isLegal = false;
			System.out.println("you must indicate the working directory (temp directory)");
		}
		if (projectStr.equals("") || projectStr == null) {
			isLegal = false;
			System.out.println("you must indicate the working name (for result file or directory)");
		}

		if (!(gitRepo.equals("") || gitRepo == null)) {
			if (!sourceCodeDir.endsWith(BroccoliContext.getInstance().getSeparator())) sourceCodeDir += BroccoliContext.getInstance().getSeparator();
		}
		else {
			gitRepo = sourceCodeDir;
		}


		//File System check (minimum 2GB)
		File file = new File(System.getProperty("user.dir"));
		if (file.getFreeSpace() / 1024 / 1024 / 1024 < 2) {
			System.out.println("Not enough free disk space, please ensure your current disk space are bigger than 2G.");
			isLegal = false;
		}


		//Check this state.
		if (!isLegal) {
			return isLegal;
		}

		// prepare working directory and create properties.
		// make workingPath
		if (workingPath.endsWith(BroccoliContext.getInstance().getSeparator()) == false) workingPath += BroccoliContext.getInstance().getSeparator();
		workingPath += "AmaLgam" + BroccoliContext.getInstance().getSeparator();

		//make outputFile path.
		File dir = new File(workingPath);
		if (!dir.exists())
			dir.mkdirs();
		outputFile = BroccoliContext.getInstance().getOutputFile();

		Property.createInstance(projectStr, sourceCodeDir, gitRepo, workingPath, alpha, outputFile);

		return isLegal;
	}


}
