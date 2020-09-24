package org.bluir.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;


public class Evaluation {
	private final String outputFilePath = BroccoliContext.getInstance().getOutputFile();
	private final String lineSparator = BroccoliContext.getInstance().getLineSeparator();
	private final String indriQueryResult = BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator() + "indriQueryResult";
	private String recommendedPath =  BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + "BLUIR" + BroccoliContext.getInstance().getSeparator()+ "recommended" +BroccoliContext.getInstance().getSeparator();

	private Hashtable<String, TreeSet<String>> fixedTable;
	private Hashtable<String, Integer> idTable;
	private Hashtable<Integer, String> nameTable;

	public Evaluation()
	{
		fixedTable = getFixedFileTable();

		idTable = new Hashtable<String, Integer>();
		nameTable = new Hashtable<Integer, String>();
	}

	/**
	 * query ��������� �ε��Ͽ� ����� ����
	 * @return
	 * @throws IOException
	 */
	public boolean evaluate() throws IOException
	{
		//������� �ε�
		Hashtable<Integer, Hashtable<Integer, Rank>> results = getResultTable();

		//������� �غ�
		FileWriter outputWriter = new FileWriter(this.outputFilePath);
		File resultDir = new File(recommendedPath);
		if (!resultDir.exists())
			resultDir.mkdirs();

		//�� ���׸���Ʈ�� ���ؼ�,....
		Set<Integer> bugIDS = results.keySet();
		for (Integer bugID : bugIDS)
		{
			//��õ��� ���� �ε�
			Hashtable<Integer, Rank> recommends = results.get(bugID);

			ArrayList<Rank> recommendsList = new ArrayList<Rank>(recommends.values());
			recommendsList.sort((Rank o1, Rank o2)->o1.rank-o2.rank);	// order of rank in ASC

			//��õ��� ���
			FileWriter writer = new FileWriter(recommendedPath + bugID + ".txt");
			for (Rank rank : recommendsList) {
				if(nameTable.containsKey(rank.fileID)) {
					writer.write(rank.rank  + "\t" +rank.score + "\t" + nameTable.get(rank.fileID) + this.lineSparator);
				}
			}
			writer.close();

			//���������� �����ϴ��� Ȯ��.
			TreeSet<String> fileSet = fixedTable.get(String.valueOf(bugID));
			for(String fileName : fileSet)
			{
				if (!idTable.containsKey(fileName)) continue;
				int fileID = idTable.get(fileName);

				if (!recommends.containsKey(fileID)) continue;
				Rank rank = recommends.get(fileID);


				outputWriter.write(bugID + "\t" + fileName + "\t" + rank.rank + "\t" + rank.score + this.lineSparator);
				outputWriter.flush();
			}
		}
		outputWriter.close();

		return true;
	}


	/**
	 * Indri���� ��õ�� ����� �ε�.
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private Hashtable<Integer, Hashtable<Integer, Rank>> getResultTable() throws NumberFormatException, IOException {
		String line = null;
		int fileIndex = 0;

		Hashtable<Integer, Hashtable<Integer, Rank>> table = new Hashtable<Integer, Hashtable<Integer, Rank>>();

		long count=0;
		BufferedReader reader = new BufferedReader(new FileReader(this.indriQueryResult));
		while ((line = reader.readLine()) != null) {
			count++;
			if (line.matches("[0-9]+ Q0 [$a-zA-Z./]+.*")==false) {
				System.err.println("Line-"+count+": "+line);
				continue;
			}

			//75739 Q0 org.eclipse.swt.ole.win32.Variant.java 1 0.930746 indri
			String[] values = line.split(" ");
			String filename = values[2].trim();

			//find File ID
			int fid = 0;
			if (!idTable.containsKey(filename)){
				fid = fileIndex++;
				idTable.put(filename, fid);
				nameTable.put(fid, filename);
			}
			else
				fid = idTable.get(filename);

			Rank item = new Rank();
			item.bugID = Integer.parseInt(values[0]);
			item.fileID = fid;
			item.rank = Integer.parseInt(values[3])-1;
			item.score = Double.parseDouble(values[4]);

			if (!table.containsKey(item.bugID)){
				table.put(item.bugID, new Hashtable<Integer, Rank>());
			}
			table.get(item.bugID).put(item.fileID, item);
		}
		reader.close();

		return table;

	}

	/**
	 * ������ ��������(XML)���� fixed File list������ ����
	 * XML������ �������� ���׸���Ʈ�� �ϳ��� ������ ������ ����.
	 * @return
	 */
	private Hashtable<String, TreeSet<String>> getFixedFileTable() {

		Hashtable<String, TreeSet<String>> fixTable = new Hashtable<String, TreeSet<String>>();

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		try {

			for (int i = 0; i < BroccoliContext.getInstance().getBugList().size(); i++) {
				Bug bugNode = BroccoliContext.getInstance().getBugList().get(i);

				//get bugID
				String bugID = bugNode.getBugId();

				for (String fixName : bugNode.getSet()) {
					//append fixTable
					if (!fixTable.containsKey(bugID))
						fixTable.put(bugID, new TreeSet<String>());
					fixTable.get(bugID).add(fixName);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return fixTable;
	}

}
