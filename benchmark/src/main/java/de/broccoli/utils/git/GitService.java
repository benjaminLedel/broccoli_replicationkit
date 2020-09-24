package de.broccoli.utils.git;

import de.broccoli.context.BroccoliContext;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class GitService {

    Logger logger = LoggerFactory.getLogger(GitService.class);

    public GitService() {
    }

    public Repository cloneIfNotExists(String projectPath, String cloneUrl) throws Exception {
        File folder = new File(projectPath);
        Repository repository;
        if (folder.exists()) {
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = ((RepositoryBuilder)((RepositoryBuilder)((RepositoryBuilder)builder.setGitDir(new File(folder, ".git"))).readEnvironment()).findGitDir()).build();
        } else {
            this.logger.info("Cloning {} ...", cloneUrl);
            Git git = Git.cloneRepository().setDirectory(folder).setURI(cloneUrl).setCloneAllBranches(true).call();
            repository = git.getRepository();
        }

        return repository;
    }

    public Repository openRepository(String repositoryPath) throws Exception {
        File folder = new File(repositoryPath);
        if (folder.exists()) {
            RepositoryBuilder builder = new RepositoryBuilder();
            Repository repository = ((RepositoryBuilder)((RepositoryBuilder)((RepositoryBuilder)builder.setGitDir(new File(folder, ".git"))).readEnvironment()).findGitDir()).build();
            return repository;
        } else {
            throw new FileNotFoundException(repositoryPath);
        }
    }

    public void checkout(String uri,File targetFolder, String commitId) throws Exception {
        this.logger.info("Checking out {} {} ...", uri, commitId);
        // currenty we are using a cache here!
        if(targetFolder.exists())
            return;
        deleteFolder(targetFolder);
        targetFolder.mkdirs();
        File orginal = new File(BroccoliContext.getInstance().getRepoDir());
        Git git = Git.cloneRepository()
                .setURI(orginal.toURI().toString())
                .setDirectory(targetFolder)
                .call();
        git.checkout().setCreateBranch(true)
                .setName("newbranch")
                .setStartPoint(commitId)
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
