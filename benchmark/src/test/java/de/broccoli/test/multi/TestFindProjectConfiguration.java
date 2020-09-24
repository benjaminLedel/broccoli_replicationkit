package de.broccoli.test.multi;

import de.broccoli.utils.ProjectConfiguration;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TestFindProjectConfiguration {

    @Test
    public void findProjectConfiguration()
    {
        assertNotNull(getConfigurationList());
    }

    public static List<ProjectConfiguration> getConfigurationList() {
        ArrayList<ProjectConfiguration> configurations = new ArrayList<>();
        String basePath = "F:\\Master\\smartshark_timeaware";
        basePath = "C:\\Users\\blede\\Documents\\smartshark\\data";
        boolean smartShark = false;
        File baseFolder = new File(basePath);
        for (File folder1 : baseFolder.listFiles())
        {
            if(folder1.isDirectory())
            {
                String firstName = folder1.getName();
                for (File folder2 : folder1.listFiles())
                {
                    String secondName = folder2.getName();
                    // safety check
                    File bugrepo = new File(folder2.getAbsolutePath() + "\\bugrepo");
                    File gitrepo = new File(folder2.getAbsolutePath() + "\\gitrepo\\");
                    if(smartShark) {
                        gitrepo = new File(folder2.getAbsolutePath() + "\\gitrepo\\" + secondName);
                    }
                    File sources = new File(folder2.getAbsolutePath() + "\\sources");

                    if(!bugrepo.exists() || !gitrepo.exists() || !sources.exists())
                    {
                        System.out.println("Error!");
                        System.exit(1);
                    }

                    File normalRepo = new File(bugrepo.getAbsolutePath() + "\\repository");
                    if(normalRepo.exists())
                    {
                        for(File normalRepos : normalRepo.listFiles())
                        {
                            if (normalRepos.getName().contains(".xml")) {
                                File sourceFolder = new File(sources +"\\" + normalRepos.getName().replace(".xml",""));
                                if(!sourceFolder.exists()) {
                                    System.out.println("error!");
                                    return null;
                                }
                                configurations.add(new ProjectConfiguration(firstName + "_" + secondName, normalRepos.getName().replace(".xml","") ,normalRepos, gitrepo, sourceFolder));
                            }
                        }
                    } else {
                        File repoBug = new File(bugrepo.getAbsolutePath() + "\\repository.xml");
                        if (!repoBug.exists()) {
                            System.out.println("exi");
                        }
                        for (File sourceFolder : sources.listFiles()) {
                            if (sourceFolder.isDirectory()) {
                                configurations.add(new ProjectConfiguration(firstName + "_" + secondName, sourceFolder.getName(), repoBug, gitrepo, sourceFolder));
                            }
                        }
                    }
                }
            }
        }
        for (ProjectConfiguration config: configurations) {
            System.out.println(config);
        }
        return configurations;
    }
}
