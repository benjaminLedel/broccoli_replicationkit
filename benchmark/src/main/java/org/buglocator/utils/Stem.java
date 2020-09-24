package org.buglocator.utils;

public class Stem {
	private PorterStemmer stemmer = new PorterStemmer();

	public String stem(String word) {
		stemmer.reset();
		stemmer.stem(word);
		return stemmer.toString();
	}
}

