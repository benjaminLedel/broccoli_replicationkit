package org.locus.utils;

import org.locus.main.Main;

public class Stem {

	private static ThreadLocal<PorterStemmer> threadLocal = new ThreadLocal<PorterStemmer>();

	public static PorterStemmer getInstance()
	{
		if(threadLocal.get() == null)
		{
			threadLocal.set(new PorterStemmer());
		}
		return threadLocal.get();
	}

//	private static StanfordStemmer sstemmer = new StanfordStemmer();

	public static String stem(String word) {
		getInstance().reset();
		getInstance().stem(word);
		return getInstance().toString();
	}


}
