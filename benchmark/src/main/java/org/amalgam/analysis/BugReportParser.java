package org.amalgam.analysis;

import de.broccoli.context.BroccoliContext;
import org.amalgam.common.Utils;
import org.amalgam.models.Bug;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

public class BugReportParser {

	public static Date makeTime(String time){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		Date date = null;
		try{
			date = formatter.parse(time);
		}
		catch(Exception e){
			long ltime = Long.parseLong(time);
			date = new Date(ltime);
		}

	    return date;
	}

	/**
	 * bug report�� ����� XML���Ͽ��� FixedFile ��� ��������.
	 * ���׸���Ʈ ���� FixedFile�� ����
	 * @return
	 */
	public static HashMap<String, Bug> loadFixedFileFromXML(HashMap<String, Bug> bugObjs) {
		ArrayList<de.broccoli.dataimporter.models.Bug> bugs = BroccoliContext.getInstance().getBugList();
		for (de.broccoli.dataimporter.models.Bug bug:bugs) {
			String bugID = bug.getBugId();
			TreeSet<String> fixedFiles = bug.getSet();
			for (String filePath: fixedFiles) {

				if (filePath.endsWith(".java")) {
					String fixedFile = Utils.getUniqueClassName(filePath);
					if (bugObjs.containsKey(bugID)) {
						bugObjs.get(bugID).addLink(fixedFile);
					} else {
						Bug b = new Bug(bugID);
						b.commitDate = bug.getFixDate();
						b.addLink(fixedFile);
						bugObjs.put(bugID, b);
					}

				}
			}
		}

		return bugObjs;
//		try {
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Document document = db.parse(new File(bugPATH));
//			NodeList nodeList = document.getElementsByTagName("bug");
//
//			//for each bug
//			for (int x = 0, size = nodeList.getLength(); x < size; x++)
//			{
//				Element node = (Element) nodeList.item(x);
//				String bugID = node.getAttributes().getNamedItem("id").getNodeValue();
//				String bugFixeDate_str = node.getAttributes().getNamedItem("fixdate").getNodeValue();
//				Date bugFixedDate = makeTime(bugFixeDate_str);
////				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
////				Date bugFixedDate = formatter.parse(bugFixeDate_str);
//				bugObjs.get(bugID).commitDate = bugFixedDate;
//
//				// get fixed location
//				NodeList fixedFiles = node.getElementsByTagName("fixedFiles");
//
//				for (int j = 0; j < fixedFiles.getLength(); j++)
//				{
//					NodeList fixFiles = ((Element) fixedFiles.item(j)).getElementsByTagName("file");
//
//					for (int i = 0; i < fixFiles.getLength(); i++)
//					{
//						Element el = (Element) fixFiles.item(i);
//						if (el == null) continue;
//
//						String filePath = el.getTextContent();
//
//						if (filePath.endsWith(".java")) {
//							String fixedFile = Utils.getUniqueClassName(filePath);
//							if(bugObjs.containsKey(bugID)){
//								bugObjs.get(bugID).addLink(fixedFile);
//							}else{
//								Bug b = new Bug(bugID);
//								b.addLink(fixedFile);
//								bugObjs.put(bugID, b);
//							}
//						}//if (filePath.endsWith(".java"))
//
//					} //for i
//				}//for j // each fixedfiles
//
//			}// for each bug
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return bugObjs;
	}

	/**
	 *
	 */
	public static HashMap<String, Bug> loadBugReports() {
		HashMap<String, Bug> bugObjs = new HashMap<String, Bug>();
		ArrayList<de.broccoli.dataimporter.models.Bug> bugs = BroccoliContext.getInstance().getBugList();
		for (de.broccoli.dataimporter.models.Bug bug:bugs) {
			String bugID = bug.getBugId();

			Bug b = new Bug(bugID);
			b.commitDate = bug.getFixDate();
			// no description or summary ?

			TreeSet<String> fixedFiles = bug.getSet();
			for (String filePath: fixedFiles) {
				if (filePath.endsWith(".java")) {
					String fixedFile = Utils.getUniqueClassName(filePath);
					b.addLink(fixedFile);
				}
			}
			bugObjs.put(bugID, b);
		}

		return bugObjs;
//		try {
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Document document = db.parse(new File(bugPATH));
//			NodeList nodeList = document.getElementsByTagName("bug");
//
//			//for each bug
//			for (int x = 0, size = nodeList.getLength(); x < size; x++)
//			{
//				Element node = (Element) nodeList.item(x);
//				String bugID = node.getAttributes().getNamedItem("id").getNodeValue();
//				String bugFixeDate_str = node.getAttributes().getNamedItem("fixdate").getNodeValue();
//
////				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
////				Date bugFixedDate = formatter.parse(bugFixeDate_str);
//				Date bugFixedDate = makeTime(bugFixeDate_str);
//
//
//				Bug item = new Bug(bugID);
//				item.commitDate = bugFixedDate;
//
//				// get fixed location
//				NodeList fixedFiles = node.getElementsByTagName("fixedFiles");
//
//				for (int j = 0; j < fixedFiles.getLength(); j++)
//				{
//					NodeList fixFiles = ((Element) fixedFiles.item(j)).getElementsByTagName("file");
//
//					for (int i = 0; i < fixFiles.getLength(); i++)
//					{
//						Element el = (Element) fixFiles.item(i);
//						if (el == null) continue;
//
//						String filePath = el.getTextContent();
//
//						if (filePath.endsWith(".java")) {
//							String fixedFile = Utils.getUniqueClassName(filePath);
//							item.addLink(fixedFile);
//						}//if (filePath.endsWith(".java"))
//
//					} //for i
//				}//for j // each fixedfiles
//
//				bugObjs.put(bugID, item);
//			}// for each bug
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return bugObjs;
	}


}
