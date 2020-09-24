package org.locus.preprocess;

import de.broccoli.dataimporter.models.Bug;
import org.locus.utils.*;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ExtractCommits {
	public  String loc = de.broccoli.context.BroccoliContext.getInstance().getWorkDir() ;
	public  String repo = de.broccoli.context.BroccoliContext.getInstance().getRepoDir();
	public  HashSet<String> concernedCommits;
	public  HashMap<String,String> changeMap;
	public  HashMap<String,Long> changeTime;
	public  void indexHunks() throws Exception {
		getCommitsOneLine();
		loadCommits();
		extractHunks();
	}

	public  void getCommitsOneLine() throws Exception{
		String logFile = loc + File.separator + "logOneline.txt";
		File file = new File(logFile);
		if (!file.exists()) {
			String content = GitHelp.getAllCommitOneLine(repo);
			// List<String> lines = FileToLines.fileToLines(repo + BroccoliContext.getInstance().getSeparator() + "log.txt");
			WriteLinesToFile.writeToFiles(content, logFile);
		}
	}

	public  void loadCommits() throws ParseException {
		String commitFile = "";
		if (org.locus.main.Main.getInstance().settings.containsKey("concernedCommit"))
			commitFile = org.locus.main.Main.getInstance().settings.get("concernedCommit");
		List<String> lines = null;
		changeMap = ChangeLocator.getInstance().getShortChangeMap();
		if (commitFile.equals("")) {
			changeTime = ChangeLocator.getInstance().getChangeTime();
			List<Bug> bugs = ReadBugsFromXML.getFixedBugsFromXML(org.locus.main.Main.getInstance().settings.get("bugReport"));
			HashMap<String,HashSet<String>> bugConcernedCommits = new HashMap<String, HashSet<String>>();
			System.out.println("Change map " + changeMap.size());
			for (String change : changeTime.keySet()) {

				for (Bug bug : bugs) {
					if (!bugConcernedCommits.containsKey(bug.getBugId()))
						bugConcernedCommits.put(bug.getBugId(), new HashSet<String>());
					if (changeTime.get(change) < bug.getOpenDate().getTime())
						bugConcernedCommits.get(bug.getBugId()).add(change);
				}
			}
			concernedCommits = new HashSet<String>();
			lines = new ArrayList<String>();
			for (Bug bug : bugs) {
				lines.add(bug.getBugId() + "\t" + bugConcernedCommits.get(bug.getBugId()).toString());
				concernedCommits.addAll(bugConcernedCommits.get(bug.getBugId()));
			}
			System.out.println("Concerned Commits " + concernedCommits.size());
			WriteLinesToFile.writeLinesToFile(lines, loc + File.separator + "concernedCommits.txt");

		} else {
			lines = FileToLines.fileToLines(commitFile);
			System.out.println(commitFile);
			concernedCommits = new HashSet<String>();
			for (String line : lines) {
	            System.out.println(line);
				concernedCommits.add(line.split("\t")[0].trim());
			}
		}
	}

	public  void extractHunks() throws Exception {
		System.out.print("Extracting Commits");
		String revisionLoc = loc + File.separator + "revisions";
		if (org.locus.main.Main.getInstance().settings.containsKey("revisionsLoc"))
			revisionLoc = org.locus.main.Main.getInstance().settings.get("revisionsLoc");
		File file = new File(revisionLoc);
		if (!file.exists())
			file.mkdirs();

		int count = 0;
		int percent = 0;
		int max = concernedCommits.size();
		for (String hash : concernedCommits) {
			count++;
			if (!changeMap.containsKey(hash)) continue;


			String fullHash = changeMap.get(hash);

			File parentPath = new File(revisionLoc + File.separator + fullHash.substring(0,2)+ File.separator +fullHash.substring(2,4));
            //file = new File(revisionLoc + File.separator + fullHash);
			if (!parentPath.exists())
				parentPath.mkdirs();

			String commitFile = parentPath.getAbsolutePath() + File.separator + fullHash + ".txt";
			file = new File(commitFile);
			if (!file.exists()) {
				String content = GitHelp.gitShow(hash, repo);
				WriteLinesToFile.writeToFiles(content, commitFile);
			}

			int newpercent = (int)((count*100) / (double)max);
			if (newpercent > percent){
				if (newpercent != 0 && newpercent != 100 && newpercent%10==0)
					System.out.print(",");
				else if(newpercent%2==0)
					System.out.print(".");
				percent = newpercent;
			}
		}
		System.out.println("Done.");
	}

}
