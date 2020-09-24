/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */

package org.blia;

import de.broccoli.context.BroccoliContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class Property {

	public static String ASPECTJ = "aspectj";
	public static String ECLIPSE = "eclipse";
	public static String SWT = "swt";
	public static String ZXING = "zxing";

	public static Property p = null;

	public static int THREAD_COUNT = 0; //Integer.parseInt(Property.readProperty("THREAD_COUNT"));
	public static String WORK_DIR = ""; //Property.readProperty("WORK_DIR");
	public static String OUTPUT_FILE ="";// Property.readProperty("OUTPUT_FILE");

	/**
	 * Property Entry :: Input setteing file
	 * @return
	 * @throws Exception
	 */
	public static Property loadInstance() throws Exception {
		p = new Property(); //Do nothing
		return p;
	}

	public static Property getInstance() {
		return p;
	}

	public static void clear() {
		p = null;
	}


	/************************************************************************8
	 * Normal method Area
	 * @throws ParseException
	 */

	public String sourceCodeDir;
	public String[] sourceCodeDirList;

	public String separator = System.getProperty("file.separator");
	public String lineSeparator = System.getProperty("line.separator");

	public int fileCount;
	public int wordCount;
	public int bugReportCount;
	public int bugTermCount;
	public double alpha;
	public double beta;
	public String productName;
	public int pastDays;
	public Calendar since = null;
	public Calendar until = null;
	public String repoDir;
	public double candidateLimitRate = 1.0;


	private Property() throws ParseException {
		// settings = new Properties();

		//Read target prduct property
		String targetProduct = BroccoliContext.getInstance().getProjectName().toLowerCase(); //settings.getProperty("TARGET_PRODUCT");
		//targetProduct = targetProduct.toUpperCase();

		this.productName = BroccoliContext.getInstance().getProjectName().toLowerCase().replace("smartshark_","").substring(0,Integer.min(BroccoliContext.getInstance().getProjectName().toLowerCase().length(),30)); //settings.getProperty("PRODUCT");
		this.sourceCodeDir = BroccoliContext.getInstance().getSourceCodeDir(); //settings.getProperty("SOURCE_DIR");
		this.alpha = BroccoliContext.getInstance().getAlpha(); //Double.parseDouble(settings.getProperty("ALPHA"));
		this.beta = 0.2; // BroccoliContext.getInstance().getContextVar("beta"); // Double.parseDouble(settings.getProperty("BETA"));
		this.pastDays = BroccoliContext.getInstance().getContextVar("past_days"); // Integer.parseInt(settings.getProperty("PAST_DAYS"));
		this.repoDir =  BroccoliContext.getInstance().getRepoDir() + BroccoliContext.getInstance().getSeparator() + ".git"; //settings.getProperty("REPO_DIR");
		this.candidateLimitRate = 0.1;  //Double.parseDouble(settings.getProperty("CANDIDATE_LIMIT_RATE"));

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date sinceDate = dateFormat.parse("1990-04-01");
		this.since = new GregorianCalendar();
		this.since.setTime(sinceDate);
		Date untilDate = dateFormat.parse("2016-11-30");
		this.until = new GregorianCalendar();
		this.until.setTime(untilDate);

		this.sourceCodeDirList = new String[1];
		this.sourceCodeDirList[0] = this.sourceCodeDir;

		this.sourceCodeDir = this.sourceCodeDir.replace("\\", "/");
		this.sourceCodeDir = this.sourceCodeDir.replace("//", "/");
		this.repoDir = this.repoDir.replace("\\", "/");
		this.repoDir = this.repoDir.replace("//", "/");

		if (!this.sourceCodeDir.endsWith("/"))
			this.sourceCodeDir = this.sourceCodeDir + "/";
		//common variable load
		String originalWorkingPath = BroccoliContext.getInstance().getWorkDir();
		originalWorkingPath = originalWorkingPath.replace("\\", "/");
		originalWorkingPath = originalWorkingPath.replace("//", "/");
		if (!originalWorkingPath.endsWith("/"))
			originalWorkingPath += "/";

		THREAD_COUNT = 10;  //Integer.parseInt(settings.getProperty("THREAD_COUNT"));
		WORK_DIR =  originalWorkingPath;
		OUTPUT_FILE = BroccoliContext.getInstance().getOutputFile();
		//Property.readProperty("OUTPUT_FILE") + "_"+ targetProduct + ".txt";

		//post processing
		OUTPUT_FILE = OUTPUT_FILE.replace("//", "/");
		WORK_DIR = WORK_DIR.replace("//", "/");
		this.sourceCodeDir = this.sourceCodeDir.replace("//", "/");
		this.repoDir = this.repoDir.replace("//", "/");


	}

	/**
	 * ������ �Ӽ� ���
	 */
	public void printValues() {
		System.out.printf("WORK_DIR: %s\n", Property.WORK_DIR);
		System.out.printf("THREAD_COUNT: %d\n", Property.THREAD_COUNT);
		System.out.printf("OUTPUT_FILE: %s\n\n", Property.OUTPUT_FILE);
		System.out.printf("Product name: %s\n", this.productName);
		System.out.printf("Source code dir: %s\n", this.sourceCodeDir);
		System.out.printf("Alpha: %f\n", this.alpha);
		System.out.printf("Beta: %f\n", this.beta);
		System.out.printf("Past days: %s\n", this.pastDays);
		System.out.printf("Repo dir: %s\n", this.repoDir);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		System.out.printf("Since: %s\n", dateFormat.format(this.since.getTime()));
		System.out.printf("Until: %s\n", dateFormat.format(this.until.getTime()));
		System.out.printf("candidateLimitRate: %f\n\n", this.candidateLimitRate);
	}


}
