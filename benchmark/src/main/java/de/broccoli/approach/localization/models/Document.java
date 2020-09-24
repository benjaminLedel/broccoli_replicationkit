package de.broccoli.approach.localization.models;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Document {

    private String javaName;
    private String root;
    private String path;
    private List<String> content;
    private String contentString;

    // Internal
    private CompilationUnit compilationUnit;

    public String getPath() {
        return path;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getContentList() {
        return content;
    }

    public String getContent() {
        return contentString;
    }

    public void setContent(List<String> content) {
        this.content = content;
        this.contentString = String.join("\n", content);
        try {
            compilationUnit = StaticJavaParser.parse(contentString);
        } catch (ParseProblemException e)
        {
            // currentyl do nothing, is a bit sad, but we do not lose information
        }
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    // Util functions section

    public String getFileName(boolean withEnding) {
        String shortpath = path;
        if(path.contains("\\"))
        {
            shortpath = shortpath.substring(path.lastIndexOf("\\") +1);
        }
        if(withEnding)
        {
            if(shortpath.contains("."))
            {
                shortpath = shortpath.substring(0,shortpath.lastIndexOf("."));
            }
            return shortpath;
        }
        return shortpath;
    }

    public String getProjectPath()
    {
        String projectPath = path.replace(root, "");
        if(projectPath.startsWith("\\"))
        {
            projectPath = projectPath.substring(1);
        }
        projectPath = projectPath.replace("\\", "/");
        return projectPath;
    }

    public List<String> getJavaDoc()
    {
        if(compilationUnit != null)
        {
           return compilationUnit.getComments().stream().map(Comment::getContent).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return path.equals(document.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
