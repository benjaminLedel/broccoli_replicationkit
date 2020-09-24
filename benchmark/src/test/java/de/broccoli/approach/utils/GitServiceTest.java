package de.broccoli.approach.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.io.File;

public class GitServiceTest {

    @Test
    public void testCheckOut() throws GitAPIException {
        File checkOut = new File("repospace/test");
        deleteFolder(checkOut);
        checkOut.mkdirs();
        File orginal = new File("C:/Users/blede/Documents/smartshark/broccoli_dataimporter/example/AspectJ/gitrepo/.git");
        Git git = Git.cloneRepository()
                .setURI(orginal.toURI().toString())
                .setDirectory(checkOut)
                .call();
        git.checkout().setCreateBranch(true)
                .setName("newbranch")
                .setStartPoint("cef7d981ed3df324e37a20442f6bd0d1bdda3def")
                .call();

    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
