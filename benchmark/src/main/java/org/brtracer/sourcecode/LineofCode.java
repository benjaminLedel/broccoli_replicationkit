package org.brtracer.sourcecode;

import de.broccoli.context.BroccoliContext;
import org.brtracer.sourcecode.ast.FileDetector;
import org.brtracer.sourcecode.ast.FileParser;

import java.io.*;

public class LineofCode {
	private String fileName;
	private Integer loc;

	public void beginCount() throws Exception {
		FileDetector detector = new FileDetector("java");
		File[] files = detector.detect(BroccoliContext.getInstance().getSourceCodeDir());
		FileWriter writer = new FileWriter(
				BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "LOC.txt");

		for (File file : files) {
			loc = count(file);
			if (fileName.endsWith(".java")) {
				writer.write(fileName + "\t" + loc + BroccoliContext.getInstance().getLineSeparator());
			} else {
				writer.write(fileName + ".java" + "\t" + loc + BroccoliContext.getInstance().getLineSeparator());
			}
			writer.flush();
		}
		writer.close();
	}

	public Integer count(File file) throws IOException {
		FileParser parser = new FileParser(file);

		fileName = parser.getPackageName();
		if (fileName.trim().equals("")) {
			fileName = file.getName();
		} else {
			fileName += "." + file.getName();
		}

		/* modification for AspectJ */
		if (BroccoliContext.getInstance().getProjectName().startsWith("ASPECTJ")) {
			fileName = file.getPath();
			fileName = fileName.substring(BroccoliContext.getInstance().getSourceCodeDir().length());
		}
		/* ************************** */

		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Integer LoC = 0;
		String tmp;
		while (true) {
			tmp = reader.readLine();
			if (tmp == null)
				break;
			LoC++;
		}
		reader.close();
		return LoC;
	}
}
