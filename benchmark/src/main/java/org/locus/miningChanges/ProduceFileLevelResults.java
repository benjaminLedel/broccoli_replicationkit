package org.locus.miningChanges;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.locus.generics.Commit;
import org.locus.generics.Pair;
import org.locus.main.Main;
import org.locus.utils.FileToLines;
import org.locus.utils.GitHelp;
import org.locus.utils.ReadBugsFromXML;
import org.locus.utils.WriteLinesToFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProduceFileLevelResults {
	public String loc = de.broccoli.context.BroccoliContext.getInstance().getWorkDir() ;
	private HashMap<String, HashSet<Integer>> bugRelatedFiles;
	public List<Bug> bugs;
	public HashMap<String, HashMap<String,Double>> hunkResults;
	public HashMap<String, Boolean> isCommitFix;
	public HashMap<String, HashMap<Integer,Double>> bugFixSuspicious;
	public List<String> hunkIndex;
	public HashMap<Integer, HashSet<String>> sourceCommits;
	public HashMap<String,Long> commitTime;
	public HashMap<Integer,String> sourceFileIndex;
	public long startTime = Long.MAX_VALUE;

	public boolean loadOracles() {
		String filename = loc + File.separator + "bugSourceIndex.txt";

		File file = new File(filename);
		if (!file.exists()) {
			System.err.println("could not find file level oracles");
			return false;
		}
		List<String> lines = FileToLines.fileToLines(filename);
		int index = 0;
		bugs = ReadBugsFromXML.getFixedBugsFromXML(org.locus.main.Main.getInstance().settings.get("bugReport"));
		bugRelatedFiles = new HashMap<String,HashSet<Integer>>();
		while (index < lines.size()) {
//			System.out.println(lines.get(index));
			String[] splits = lines.get(index).split("\t");
			String bid = splits[0];
			HashSet<Integer> revisions = new HashSet<Integer>();
			for (int i = 1; i < splits.length; i++)
				revisions.add(Integer.parseInt(splits[i]));
			bugRelatedFiles.put(bid, revisions);
			index++;
		}

		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
		commitTime = new HashMap<String, Long>();
		filename = loc + File.separator + "logOneline.txt";
		lines = FileToLines.fileToLines(filename);
		try {
			for (String line : lines) {
				String[] splits = line.split("\t");
				Date date = formatter.parse(splits[2]);
				commitTime.put(splits[0], date.getTime());
				if (date.getTime() < startTime)
					startTime = date.getTime();
			}
		} catch (Exception e) {
			System.out.println("Parsing time error");
			e.printStackTrace();
		}


		// load sourceFileIndex
	    sourceFileIndex = new HashMap<>();
	    lines.clear();
        lines = FileToLines.fileToLines(loc + File.separator + "sourceFileIndex.txt");
        int count = 0;
        for (String line : lines) {
            sourceFileIndex.put(count++, line);
        }

		return true;
	}

	public void loadResults() {
		String resultFile = loc + File.separator + "results_file" + ".txt";
		File file = new File(resultFile);
		if (!file.exists()) {
			ObtainVSMScore ovs = new ObtainVSMScore();
			hunkResults = ovs.obtainSimilarity(false);
		} else {
			System.out.println("Results of file level exists, read from results_file.txt");
			hunkResults = new HashMap<>();
			List<String> lines = FileToLines.fileToLines(resultFile);
			for (String line : lines) {
				String[] splits = line.split("\t");
				String sid = splits[0];
				hunkResults.put(sid, new HashMap<>());
				for (int i = 1; i < splits.length; i++)
					hunkResults.get(sid).put(splits[i].split(":")[0], Double.parseDouble(splits[i].split(":")[1]));
			}
		}

	}

	public void loadFileSuspiciousScore() throws Exception {
		String commitFix = loc + File.separator + "isCommitFix.txt";
		File file = new File(commitFix);
		isCommitFix = new HashMap<String,Boolean>();
		List<String> lines = null;
		if (!file.exists()) {
			String logFullDescription = loc + File.separator + "logFullDescription.txt";
			String content = GitHelp.getAllCommitWithFullDescription(BroccoliContext.getInstance().getRepoDir());
			WriteLinesToFile.writeToFiles(content, logFullDescription);
			List<Commit> commits = GitHelp.readFromTextGIT(logFullDescription);
			lines = new ArrayList<String>();
			for (int i = 0; i < commits.size(); i++) {
				String hash = commits.get(i).changeSet;
				String description = commits.get(i).description;
				description = description.toLowerCase();
				if (description.contains("bug") || description.contains("patch") ||
						description.contains("fix") || description.contains("issue")) {
//				if (description.contains("fix") || description.contains("bug")) {
					isCommitFix.put(hash.substring(0, 9), true);
					lines.add(hash + "\t1");
				} else {
					isCommitFix.put(hash.substring(0, 9), false);
					lines.add(hash + "\t0");
				}
			}
			WriteLinesToFile.writeLinesToFile(lines, commitFix);
		} else {
			lines = FileToLines.fileToLines(commitFix);
			for (String line : lines) {
				String[] split = line.split("\t");
				isCommitFix.put(split[0].substring(0,9), split[1].equals("1"));
			}
		}

		String filename = loc + File.separator + "hunkIndex.txt";
		hunkIndex = FileToLines.fileToLines(filename);


		filename = loc + File.separator + "sourceHunkLink.txt";
		lines = FileToLines.fileToLines(filename);

		sourceCommits = new HashMap<Integer,HashSet<String>>();
		for (String line : lines) {
			String[] tmp = line.split("\t");
			int sid = Integer.parseInt(tmp[0]);
			for (int i = 1; i < tmp.length; i++) {
				int hid = Integer.parseInt(tmp[i]);
				String hunk = hunkIndex.get(hid);
				String commit = hunk.split("@")[0];
				if (!sourceCommits.containsKey(sid)) sourceCommits.put(sid, new HashSet<String>());
				sourceCommits.get(sid).add(commit);
			}
		}

		bugFixSuspicious = new HashMap<String, HashMap<Integer, Double>>();
		for (Bug bug : bugs) {
			long time = bug.getOpenDate().getTime();
			bugFixSuspicious.put(bug.getBugId(), new HashMap<Integer,Double>());
			for (int sid : sourceCommits.keySet()) {
				HashSet<String> commits = sourceCommits.get(sid);
				double score = 0;
				for (String commit : commits) {
					if (time > commitTime.get(commit)) {
						if (!isCommitFix.containsKey(commit))
							continue;
						if (isCommitFix.get(commit)) {
							double norm = (commitTime.get(commit) - startTime) * 1.0 / (time - startTime);
							double sus = 1.0 / (1 + Math.exp(-12 * norm + 12));
//							System.out.println(sid + "\t" + norm + "\t" + sus);
							score += sus;
						}
					}
				}
				bugFixSuspicious.get(bug.getBugId()).put(sid, score);
			}
		}

	}

	public void integrateResults() {
		String recommendedPath = de.broccoli.context.BroccoliContext.getInstance().getWorkDir()  + File.separator + "recommended" + File.separator;
		File dir = new File(recommendedPath);
		if (!dir.exists())
			dir.mkdirs();

		List<List<Integer>> ranks = new ArrayList<List<Integer>>();
		List<String> resultsLines = new ArrayList<String>();
		List<String> rawRanks = new ArrayList<String>();

		double belta1 = Main.getInstance().belta1;
		if (loc.toLowerCase().contains("zxing"))
			belta1 = 0.05;

		for (Bug bug : bugs) {
			String bid = bug.getBugId();
			if (!hunkResults.containsKey(bid)) continue;

			HashMap<String, Double> results = hunkResults.get(bid);
			HashMap<Integer,Double> finalResults = new HashMap<Integer, Double>();

			// normalize bug fix results
			HashMap<Integer, Double> fixSuspicious = bugFixSuspicious.get(bid);
			double max = 0;
			for (int sid : fixSuspicious.keySet())
				if (max < fixSuspicious.get(sid))
					max = fixSuspicious.get(sid);
			if(max != 0) {
				for (int sid : fixSuspicious.keySet())
					fixSuspicious.put(sid, fixSuspicious.get(sid) / max);
			}
			//calculate finalResults
			for (String change : results.keySet()) {

				int sid = Integer.parseInt(change);

				if (bugFixSuspicious.get(bug.getBugId()).containsKey(sid)) {
					Double wert1 = results.get(change);
					Double wert2 = bugFixSuspicious.get(bid).get(sid);
					finalResults.put(sid, wert1 + belta1 * wert2);
				}
				else
					finalResults.put(sid, results.get(change));
			}

			List<Pair<Integer, Double>> finalRanks = new ArrayList<Pair<Integer,Double>>();
			for (int sid : finalResults.keySet()) {
				finalRanks.add(new Pair<Integer,Double>(sid, finalResults.get(sid)));
			}

			Collections.sort(finalRanks, Collections.reverseOrder());
			List<Integer> rank = new ArrayList<Integer>();
			HashSet<Integer> ansFileIndices = bugRelatedFiles.get(bid);
			List<String> fullRanks = new ArrayList<String>();

			for (int r = 0; r < finalRanks.size(); r++) {
				int sid = finalRanks.get(r).getKey();
				double score = finalRanks.get(r).getValue();
				String filename = sourceFileIndex.get(sid);

				// ��ü ��ŷ���� ����
				fullRanks.add(r + "\t" + score + "\t" + filename);

				// answer�� ���� ����� ����.
				if (!ansFileIndices.contains(sid)) continue;
				rawRanks.add(bid + "\t" +filename + "\t" + r + "\t" + score );
				rank.add(r);
			}
			ranks.add(rank);
			//System.out.println(bid + "\t" + rank.toString());

			String bugresultsPath = recommendedPath + bid + ".txt";
			WriteLinesToFile.writeLinesToFile(fullRanks, bugresultsPath);
		}
		WriteLinesToFile.writeLinesToFile(rawRanks, org.locus.main.Main.getInstance().settings.get("outputFile"));



		//make result file data
		int N = 10;
		double[] topN = EvaluationMetric.topN(ranks, N);
		double map = EvaluationMetric.MAP(ranks);
		double mrr = EvaluationMetric.MRR(ranks);
		resultsLines.add("MAP:\t" + map);
		resultsLines.add("MRR:\t" + mrr);
		for (int i = 0; i < N; i++) {
			resultsLines.add("top@" + (i + 1) + ":\t" + topN[i]);
		}

		WriteLinesToFile.writeLinesToFile(resultsLines, loc + File.separator + "fileLevelResults.txt");

		//Show Display
		System.out.println("MAP:" + map + "\tMRR:" + mrr);
		System.out.print("Top@");
		for (int i = 0; i < N; i++) {
			System.out.print("\t"+Integer.toString(i+1));
		}
		System.out.println("");
		for (int i = 0; i < N; i++) {
			System.out.print("\t"+topN[i]);
		}
		System.out.println("");
	}

	public void getFinalResults() throws Exception {
		if (loadOracles()) {
			System.out.println("Finish loading files");
			loadResults();
			System.out.println("Finish calculating suspicious score");
			loadFileSuspiciousScore();
			integrateResults();
		}
	}
}