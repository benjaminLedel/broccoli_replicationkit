package org.amalgam.common;

public class Property {


	private static Property p = null;

	public static void createInstance(String projectStr,
                                      String sourceCodeDir,
                                      String gitRepo,
                                      String workDir,
                                      float alpha,
                                      String outputFile)
	{
			p = new Property(projectStr, sourceCodeDir, gitRepo, workDir, alpha, outputFile);
	}

	public static Property getInstance() {
		return p;
	}

	private Property(String ProjectStr, String sourceCodeDir, String gitRepo, String workDir, float alpha, String outputFile) {
		this.ProjectName = ProjectStr;
		this.SourceCodeDir = sourceCodeDir;
		this.SourceCodeRepo= gitRepo;
		this.WorkDir = workDir;
		this.Alpha = alpha;
		this.OutputFile = outputFile;

		this.Separator = System.getProperty("file.separator");
        this.LineSeparator = System.getProperty("line.separator");

	}


	public  String Separator;
	public  String LineSeparator;


	public  String SourceCodeDir;
	public  String SourceCodeRepo;
	public  String WorkDir;
	public  String OutputFile;
	public float Alpha;
	public  String ProjectName;


}
