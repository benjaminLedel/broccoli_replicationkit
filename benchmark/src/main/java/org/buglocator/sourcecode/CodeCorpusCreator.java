package org.buglocator.sourcecode;

import de.broccoli.context.BroccoliContext;
import org.buglocator.sourcecode.ast.Corpus;
import org.buglocator.sourcecode.ast.FileDetector;
import org.buglocator.sourcecode.ast.FileParser;
import org.buglocator.utils.Stem;
import org.buglocator.utils.Stopword;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.TreeSet;

public class CodeCorpusCreator
{
	private String workDir;
	private String codePath;
	private String pathSeparator;
	private String lineSeparator;
	private String projectName;

	public CodeCorpusCreator() throws IOException, ParseException
	{
		workDir = BroccoliContext.getInstance().getWorkDir();
		codePath = BroccoliContext.getInstance().getSourceCodeDir();
		pathSeparator = BroccoliContext.getInstance().getSeparator();
		lineSeparator = BroccoliContext.getInstance().getLineSeparator();
		projectName = BroccoliContext.getInstance().getProjectName();
	}

	/**
	 * ���� �Լ�.
	 * @throws Exception
	 */
	public void create() throws Exception
	{
		int count = 0;
		TreeSet<String> nameSet = new TreeSet<String>();

		//File listing
		FileDetector detector = new FileDetector("java"); // java file Filter
		File[] files = detector.detect(codePath);

		//preparing output File.
		FileWriter writeCorpus = new FileWriter(workDir + pathSeparator + "CodeCorpus.txt");
		FileWriter writer = new FileWriter(workDir + pathSeparator + "ClassName.txt");

		//make corpus each file
		for (File file: files) {
			Corpus corpus = this.create(file);	//Corpus ����.
			if (corpus == null)	continue;

			//file filtering  (�ߺ�����)
			String FullClassName = corpus.getJavaFileFullClassName();
			if (projectName.startsWith("ASPECTJ")){
				FullClassName = file.getPath().substring(codePath.length()); //��θ��� ���� �ν�.
				FullClassName = FullClassName.replace("\\", "/");
				if (FullClassName.startsWith("/"))
					FullClassName = FullClassName.substring(1); //��θ��� ���� �ν�.

			}
			if (nameSet.contains(FullClassName)) continue;


			//Write File.
			if (!FullClassName.endsWith(".java"))	FullClassName +=  ".java";
			writer.write(count + "\t" + FullClassName + this.lineSeparator);
			writeCorpus.write(FullClassName + "\t" + corpus.getContent() + this.lineSeparator);
			writer.flush();
			writeCorpus.flush();

			//Update Filter
			nameSet.add(FullClassName); //corpus.getJavaFileFullClassName());
			count++;
		}
		BroccoliContext.getInstance().setContextVar("FileCount", count);
		writeCorpus.close();
		writer.close();

	}

	/**
	 * �� ���Ͽ� ���ؼ� corpus�� ����
	 * @param file
	 * @return
	 */
	public Corpus create(File file) {
		FileParser parser = new FileParser(file);

		//������ ��Ű�� ���� ���
		String fileName = parser.getPackageName();
		if (fileName.trim().equals("")) {
			fileName = file.getName();
		} else {
			fileName = fileName + "." + file.getName();
		}
		fileName = fileName.substring(0, fileName.lastIndexOf("."));

		//content�� �и��Ͽ� stemming, removing stopwords ����
		String[] content = parser.getContent();
		StringBuffer contentBuf = new StringBuffer();
		for (String word : content) {	//camel case �и� tokenize�� content����.
			Stem stem = new Stem();
			String stemWord = stem.stem(word.toLowerCase());
			if ((!Stopword.isKeyword(word)) && (!Stopword.isEnglishStopword(word)))
			{
				contentBuf.append(stemWord);
				contentBuf.append(" ");
			}
		}
		String sourceCodeContent = contentBuf.toString();

		//Ŭ������, �޼ҵ�� ���ؼ� ������ corpus�� �ѹ� �� ����.
		String[] classNameAndMethodName = parser.getClassNameAndMethodName();
		StringBuffer nameBuf = new StringBuffer();

		for (String word: classNameAndMethodName) {
			Stem stem = new Stem();
			String stemWord = stem.stem(word.toLowerCase());
			nameBuf.append(stemWord);
			nameBuf.append(" ");
		}
		String names = nameBuf.toString();

		//corpus��ü ����.
		Corpus corpus = new Corpus();
		corpus.setJavaFilePath(file.getAbsolutePath());
		corpus.setJavaFileFullClassName(fileName);
		corpus.setContent(sourceCodeContent + " " + names);	//content���� �� corpus�� ����.
		return corpus;
	}
}
