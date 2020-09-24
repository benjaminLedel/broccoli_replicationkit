package org.locus.utils;

import de.broccoli.context.BroccoliContext;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChangeLocator {


	private static ThreadLocal<ChangeLocator> threadLocal = new ThreadLocal<ChangeLocator>();

	public static ChangeLocator getInstance()
	{
		if(threadLocal.get() == null)
		{
			threadLocal.set(new ChangeLocator());
		}
		return threadLocal.get();
	}


	public HashMap<String,String> shortChangeMap = null;

	public HashMap<String,String> getShortChangeMap() {
		if (shortChangeMap == null || shortChangeMap.size() == 0) {
			shortChangeMap = readShortChangeMap();
		}
		return shortChangeMap;
	}

	public HashMap<String,Long> getChangeTime() throws ParseException {
		HashMap<String,Long> changeTime = new HashMap<String,Long>();
		List<String> lines = FileToLines.fileToLines(BroccoliContext.getInstance().getWorkDir() + File.separator + "logOneline.txt");
		for (String line : lines) {
			String[] split = line.split("\t");

			Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH).parse(split[2]);
			changeTime.put(split[0], date.getTime());
		}
		return changeTime;
	}

	public HashMap<String,String> readShortChangeMap() {
		HashMap<String,String> changeMap = new HashMap<String,String>();
		List<String> lines = FileToLines.fileToLines(BroccoliContext.getInstance().getWorkDir() + File.separator + "logOneline.txt");
		for (String line : lines) {
			String[] split = line.split("\t");
			changeMap.put(split[0].substring(0, 7), split[0]);
		}
		return changeMap;
	}

	public void clear()
	{
		if(shortChangeMap != null)
			shortChangeMap.clear();
	}
}
