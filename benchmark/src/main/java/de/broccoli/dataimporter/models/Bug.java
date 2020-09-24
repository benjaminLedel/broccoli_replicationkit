package de.broccoli.dataimporter.models;

import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public class Bug {

	String bugId;

	Date openDate;
	Date fixDate;
	List<String> versions;
	List<List<Document>> cachedFileIndex;
	String bugSummary;
	String bugDescription;
	TreeSet<String> set = new TreeSet<String>();

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public Date getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Date openDate) {
		this.openDate = openDate;
	}

	public Date getFixDate() {
		return fixDate;
	}

	public void setFixDate(Date fixDate) {
		this.fixDate = fixDate;
	}

	public String getBugSummary() {
		return bugSummary;
	}

	public void setBugSummary(String bugSummary) {
		this.bugSummary = bugSummary;
	}

	public String getBugDescription() {
		return bugDescription;
	}

	public void setBugDescription(String bugDescription) {
		this.bugDescription = bugDescription;
	}

	public TreeSet<String> getSet() {
		return set;
	}

	public void addFixedFile(String fileName) {
		this.set.add(fileName);
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public List<List<Document>> getCachedFileIndex() {
		return cachedFileIndex;
	}

	public void setCachedFileIndex(List<List<Document>> cachedFileIndex) {
		this.cachedFileIndex = cachedFileIndex;
	}
}
