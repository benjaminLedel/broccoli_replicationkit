package de.broccoli.dataimporter.xml;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class XMLDataImporter implements DataImporter {

    private String projectName;
    private String sourceLocation;
    private String repoLocation;
    private String bugLocation;

    public XMLDataImporter(String bugLocation, String sourceLocation, String repoLocation, String projectName)
    {
        this.bugLocation = bugLocation;
        this.sourceLocation = sourceLocation;
        this.repoLocation = repoLocation;
        this.projectName = projectName;
        createContext();
    }

    private void createContext() {
        BroccoliContext context = BroccoliContext.getInstance();
        context.setProjectName(projectName);
        context.setSourceCodeDir(sourceLocation);
        context.setRepoDir(repoLocation);
        context.createBasicContext(this);
    }

    @Override
    public List<Bug> getBugs() {
        return parseXML();
    }

    @Override
    public void clearData() {
        try {
            FileUtils.deleteDirectory(new File(BroccoliContext.getInstance().getWorkDir()));
            // bitte nicht
            // FileUtils.deleteDirectory(new File(BroccoliContext.getInstance().getSourceCodeDir()));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private ArrayList<Bug> parseXML() {
        ArrayList<Bug> list = new ArrayList<Bug>();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
            InputStream is = new FileInputStream(bugLocation);//Property.getInstance().BugFilePath);
            Document doc = domBuilder.parse(is);
            Element root = doc.getDocumentElement();
            NodeList bugRepository = root.getChildNodes();
            if (bugRepository == null)
                return list;

            for (int i = 0; i < bugRepository.getLength(); i++) {
                Node bugNode = bugRepository.item(i);
                if (bugNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                String bugId = bugNode.getAttributes().getNamedItem("id").getNodeValue();
                String openDate = bugNode.getAttributes().getNamedItem("opendate").getNodeValue();
                String fixDate = bugNode.getAttributes().getNamedItem("fixdate").getNodeValue();

                Bug bug = new Bug();
                if(bugNode.getAttributes().getNamedItem("fixversion") != null) {
                    String fixVersion = bugNode.getAttributes().getNamedItem("fixversion").getNodeValue();
                    fixVersion = fixVersion.replace("'", "").replace("[", "").replace("]", "");
                    List<String> versions = Arrays.asList(fixVersion.split(","));
                    versions = versions.stream().map(s -> s.replaceAll("[^\\d+(\\.\\d+)*]","")).collect(Collectors.toList());
                    bug.setVersions(versions);
                }
                bug.setBugId(bugId);
                bug.setOpenDate(makeTime(openDate));
                bug.setFixDate(makeTime(fixDate));

                for (Node node = bugNode.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    if (node.getNodeName().equals("buginformation")) {
                        NodeList _l = node.getChildNodes();
                        for (int j = 0; j < _l.getLength(); j++) {
                            Node _n = _l.item(j);
                            if (_n.getNodeName().equals("summary")) {
                                String summary = _n.getTextContent();
                                bug.setBugSummary(summary);
                            }

                            if (_n.getNodeName().equals("description")) {
                                String description = _n.getTextContent();
                                bug.setBugDescription(description);
                            }
                        }
                    }
                    if (node.getNodeName().equals("fixedFiles")) {
                        NodeList _l = node.getChildNodes();
                        for (int j = 0; j < _l.getLength(); j++) {
                            Node _n = _l.item(j);
                            if (_n.getNodeName().equals("file")) {
                                String fileName = _n.getTextContent();
                                // no inner classes
                                if(!fileName.contains("$")) {
                                    bug.addFixedFile(fileName );
                                }
                            }
                        }
                    }
                }
                if (bug.getSet().size() > 0) {
                    list.add(bug);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }


    private Date makeTime(String time){
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
}
