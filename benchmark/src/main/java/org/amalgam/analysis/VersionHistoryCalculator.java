package org.amalgam.analysis;

import de.broccoli.approach.localization.approaches.versionHistory.VersionHistoryContainer;
import de.broccoli.approach.localization.models.Document;
import org.amalgam.common.Property;
import org.amalgam.common.Utils;
import org.amalgam.models.Bug;
import org.amalgam.models.CommitItem;
import org.amalgam.models.FileObjs;

import javax.print.Doc;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;


/**
 * ���� ��Ʈ�� �ý��ۿ� �����ϴ� ��� ���ϵ鿡 ���ؼ� ���� �����丮�� �ε��Ͽ� �����丮 ���ھ� ���.
 * �����ڿ��� Ŀ�������� �ε��ϰ�, �Է¹��� ���׸���Ʈ�� ���ؼ� historical score�� ���.
 * @author Zeck
 *
 */
public class VersionHistoryCalculator {

	private HashMap<Integer, HashSet<CommitItem>> fileHistories;	//all fixed File in Source code Repository,  commit dates.

	public VersionHistoryCalculator(HashMap<Integer, HashSet<CommitItem>> _fileHistories) throws IOException, ParseException {
		this.fileHistories = _fileHistories;
	}

	/**
	 * ������ ���׸���Ʈ�� ���ؼ� Version History Suspecious Scoure�� ���.
	 * @param bug
	 * @param days_back
	 * @param topk
	 * @return
	 */
	public HashMap<Integer, Double> computeBugSuspeciousScore(Bug bug, int days_back, int topk) {

		//get Start Date
		Date commitDate = bug.commitDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(commitDate);
		calendar.add(Calendar.DAY_OF_YEAR, -days_back);
		Date startDate = calendar.getTime();

		long rangeTimeInMin = (commitDate.getTime() - startDate.getTime()) / (1000 * 60);



		//���� �����丮�� �����ϴ� ��� ���Ͽ� ���ؼ�,
		HashMap<Integer, Double> fileHistoryScores = new HashMap<Integer, Double>();
		for (Integer fileID : this.fileHistories.keySet()) {

			//�ش� ���׸���Ʈ�� ��� ���Ͽ� ���Ե��� ������ out
			//if (!bug.containResultFile(fileID))	continue;

			//�Է¹��� ���׸���Ʈ���� history score ���.
			double score = 0;
			for (CommitItem commit : fileHistories.get(fileID))
			{
				// CommitDate(bug)-15 < CommitDate(file) < CommitDate(bug)
				Date fileDate = commit.commitDate;
				if (!(fileDate.before(commitDate) && !fileDate.before(startDate))) continue;

				double normalized_t = (double) ((fileDate.getTime() - startDate.getTime()) / (1000 * 60))	/ (double) rangeTimeInMin;
				score += 1 / (1 + Math.exp(-12 * normalized_t + 12));

			}
			if (score > 0) {
				fileHistoryScores.put(fileID, score);
			}
		}
		// sort and get topk result
		if (topk != 0)
			return getTopk(fileHistoryScores, topk);

		return fileHistoryScores;
	}

	/**
	 * ������ ���ϵ鿡 ���ؼ� TopN���� ������ ��õ
	 * @param fileHistoryScores
	 * @param topk
	 * @return
	 */
	private HashMap<Integer, Double> getTopk(HashMap<Integer, Double> fileHistoryScores, int topk) {
		HashMap<Integer, Double> topKResult = new HashMap<Integer, Double>();

//		ArrayList<Entry<String,Double>> results = new ArrayList<Entry<String,Double>>();
//		for (Entry<String,Double> scores : fileHistoryScores.entrySet()) {
//			results.add(scores);
//		}
		ArrayList<Entry<Integer, Double>> results = new ArrayList<Entry<Integer, Double>>(fileHistoryScores.entrySet());
		results.sort(new EntryComparator());

		for (int i = 0; i < topk && i < results.size(); i++) {
			topKResult.put(results.get(i).getKey(), results.get(i).getValue());
		}

		return topKResult;
	}

	/**
	 * Compare Function
	 * @param a
	 * @param b
	 * @return
	 */
	class EntryComparator implements Comparator<Entry<Integer, Double>> {
	    @Override
	    public int compare(Entry<Integer, Double> a, Entry<Integer, Double> b) {
	        return  b.getValue().compareTo(a.getValue());
	    }
	}

	/**
	 * Historical Score ��� ����� ����
	 * @param bugObjs
	 * @throws IOException
	 */
	public void storeScores(HashMap<String, Bug> bugObjs) throws IOException
	{
		String historicalSocre = Property.getInstance().WorkDir + Property.getInstance().Separator + "Historical_Score.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(historicalSocre));
		for (Bug bug : bugObjs.values()) {

			for (Integer fid : bug.historicalScores.keySet()){

				String filename = FileObjs.get(fid);
				Double score = bug.historicalScores.get(fid);
				if (score==null) continue;

				//output
				bw.write(bug.ID + " " + filename + " null " + score);
				bw.newLine();
			}
		}
		bw.close();
	}

	public Map<String, List<VersionHistoryContainer>> storeScoresBroccoli(HashMap<String, Bug> bugObjs, List<Document> documents) throws IOException
	{
		Map<String, Document> documentMap = new HashMap<>();
		for (Document d : documents)
		{
			documentMap.put( Utils.getUniqueClassName(d.getProjectPath()), d);
		}
		Map<String, List<VersionHistoryContainer>> map = new HashMap<>();
		for (Bug bug : bugObjs.values()) {

			for (Integer fid : bug.historicalScores.keySet()){

				String filename = FileObjs.get(fid);
				Double score = bug.historicalScores.get(fid);
				if (score==null) continue;

				//output
				if(documentMap.get(filename) == null)
				{
					continue;
				}
				if(!map.containsKey(bug.ID))
				{
					map.put(bug.ID, new ArrayList<>());
				}
				map.get(bug.ID).add(new VersionHistoryContainer(documentMap.get(filename), score));
			}
		}
		return map;
	}

}
