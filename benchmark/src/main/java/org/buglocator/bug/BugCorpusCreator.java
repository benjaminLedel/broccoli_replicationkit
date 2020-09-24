package org.buglocator.bug;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.buglocator.utils.Splitter;
import org.buglocator.utils.Stem;
import org.buglocator.utils.Stopword;

import java.io.*;
import java.util.ArrayList;
//import java.util.Comparator;

public class BugCorpusCreator {

	private final String workDir = BroccoliContext.getInstance().getWorkDir();
	private final String pathSeperator = BroccoliContext.getInstance().getSeparator();
	private final String lineSeperator = BroccoliContext.getInstance().getLineSeparator();


//	class BugComparator implements Comparator<Bug>{
//		@Override
//		public int compare(Bug a, Bug b) {
//			if(a.getOpenDate().after(b.getOpenDate()))
//				return 1;
//			else if(a.getOpenDate().before(b.getOpenDate()))
//				return -1;
//			else
//				return 0;
//
//		}
//	}
	/**
	 * �����Լ�
	 * @throws IOException
	 */
	public void create() throws IOException {
		//Create Temp Directory
		String dirPath = workDir + this.pathSeperator + "BugCorpus" + this.pathSeperator;
		File dirObj = new File(dirPath);
		if (!dirObj.exists())
			dirObj.mkdirs();

		//Create Corpus and Sort
		ArrayList<Bug> list = BroccoliContext.getInstance().getBugList();
		//list.sort(new BugComparator());		// fixed date

		//Corpus Store
		BroccoliContext.getInstance().setContextVar( "BugReportCount", list.size());
		for (Bug bug : list) {
			writeCorpus(bug, dirPath);
		}

		//summarize corpus information.
		FileWriter writer = new FileWriter(this.workDir + this.pathSeperator + "SortedId.txt");
		FileWriter writerFix = new FileWriter(this.workDir + this.pathSeperator + "FixLink.txt");

		for (Bug bug : list) {
			//XML�� bug����Ʈ�� fixed_date�� ���ĵǾ��־ ���� ����
			writer.write(bug.getBugId() + "\t" + bug.getFixDate() + this.lineSeperator);
			writer.flush();

			for (String fixName : bug.getSet()) {
				writerFix.write(bug.getBugId() + "\t" + fixName + this.lineSeperator);
				writerFix.flush();
			}
		}
		writer.close();
		writerFix.close();
	}

	/**
	 * ���� corpus�� ���Ͽ� ���
	 *
	 * @param bug
	 * @param storeDir
	 * @throws IOException
	 */
	private void writeCorpus(Bug bug, String storeDir) throws IOException {

		//split words from bug content (summary + description)
		String content = bug.getBugSummary() + " " + bug.getBugDescription();
		String[] splitWords = Splitter.splitNatureLanguage(content);

		// concatenate words in bug
		StringBuffer corpus = new StringBuffer();
		for (String word : splitWords) {
			Stem stem = new Stem();
			word = stem.stem(word.toLowerCase());
			if (!Stopword.isEnglishStopword(word)) {
				corpus.append(word + " ");
			}
		}

		//save corpus.
		FileWriter writer = new FileWriter(storeDir + bug.getBugId() + ".txt");
		writer.write(corpus.toString().trim());
		writer.flush();
		writer.close();

	}

}
