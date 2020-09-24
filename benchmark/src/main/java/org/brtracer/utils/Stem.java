package org.brtracer.utils;

import de.broccoli.context.BroccoliContext;

public class Stem {
	private static ThreadLocal<PorterStemmer> threadLocal = new ThreadLocal<PorterStemmer>();

	public static String stem(String word) {
		PorterStemmer stemmer = getInstance();
		stemmer.reset();
		stemmer.stem(word);
		return stemmer.toString();
	}

	public static PorterStemmer getInstance()
	{
		if(threadLocal.get() == null)
		{
			threadLocal.set(new PorterStemmer());
		}
		return threadLocal.get();
	}
}

