package org.buglocator.bug;

import de.broccoli.context.BroccoliContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

public class SimilarityDistribution {
	private int fileCount = BroccoliContext.getInstance().getContextVar("FileCount");
	private String workDir = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator();


	/**
	 * ���׸���Ʈ ���� ���絵�� ���׸���Ʈ�� ���� ������ �� ���ϵ鿡�� �Ҵ�
	 * ���Ϻ��� ���絵 ������ ���.
	 * @throws Exception
	 * @throws IOException
	 */
	public void distribute() throws Exception, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.workDir + "BugSimilarity.txt"));
		String line = null;
		Hashtable<Integer, TreeSet<String>> fixedTable = getFixedTable();
		Hashtable<String, Integer> idTable = getFileIdTable();

		FileWriter writer = new FileWriter(this.workDir + "SimiScore.txt");
		FileWriter errorWriter = new FileWriter(this.workDir + "BugFixedFile-NoMatch.txt");
		int errorCount = 0;
		while ((line = reader.readLine()) != null) {
			float[] similarValues = new float[this.fileCount];
			String idStr = line.substring(0, line.indexOf(";"));
			String vectorStr = line.substring(line.indexOf(";") + 1).trim();
			Integer id = Integer.parseInt(idStr);
			String[] values = vectorStr.split(" ");

			//�� file�鿡 ���ؼ� ���絵 ����ġ�� �й� �� ����.
			for (String value : values) {
				String[] singleValues = value.split(":");
				if (singleValues.length != 2) continue; 
					
				Integer simBugId = Integer.parseInt(singleValues[0]);
				float sim = Float.parseFloat(singleValues[1]);
				TreeSet<String> fileSet = fixedTable.get(simBugId);
				if (fileSet == null) {
					System.out.println(simBugId);
					continue;
				}
				
				Iterator<String> fileSetIt = fileSet.iterator();
				int size = fileSet.size();
				float singleValue = sim / size;
				while (fileSetIt.hasNext()) {
					String name = fileSetIt.next();
					Integer fileId = idTable.get(name);
					if (fileId == null) {
						//System.err.println("Warning:: The Fixed File in bug is not in source code: "+name);
						errorWriter.write(name+"\n");
						errorCount++;
						continue;
					}
					similarValues[fileId.intValue()] += singleValue;
				}
			}

			String output = id + ";";
			for (int i = 0; i < this.fileCount; i++) {
				if (similarValues[i] != 0.0F) {
					output += i + ":" + similarValues[i] + " ";
				}
			}
			writer.write(output.trim() + BroccoliContext.getInstance().getLineSeparator());
			writer.flush();
		}
		reader.close();
		writer.close();
		
		errorWriter.close();
		if (errorCount>0)
			System.err.println("There are "+errorCount+" fixed file in bug without in source code. Check the BugFixedFile-NoMatch.txt.");
	}

	public Hashtable<Integer, TreeSet<String>> getFixedTable() throws IOException {
		Hashtable<Integer, TreeSet<String>> idTable = new Hashtable<Integer, TreeSet<String>>();

		BufferedReader reader = new BufferedReader(new FileReader(this.workDir + "FixLink.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer id = Integer.valueOf(Integer.parseInt(values[0]));
			String name = values[1].trim();
			if (!idTable.containsKey(id)) {
				idTable.put(id, new TreeSet<String>());
			}
			idTable.get(id).add(name);
		}
		reader.close();
		return idTable;
	}

	public Hashtable<String, Integer> getFileIdTable() throws IOException {
		Hashtable<String, Integer> idTable = new Hashtable<String, Integer>();

		BufferedReader reader = new BufferedReader(new FileReader(this.workDir + "ClassName.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer id = Integer.valueOf(Integer.parseInt(values[0]));
			String name = values[1].trim();
			idTable.put(name, id);
		}
		reader.close();
		return idTable;
	}
}