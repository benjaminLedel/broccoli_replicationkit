package de.broccoli.approach.localization.util;


import de.broccoli.approach.localization.api.Approach;
import de.broccoli.approach.localization.approaches.*;

import java.util.ArrayList;
import java.util.List;

public class ApproachFactory {

    public static List<Approach> getApproaches()
    {
        List<Approach> approaches = new ArrayList<>();
        // register approaches, maybe later this could be done via config
       approaches.add(new FileNameMatchingApproach());
        // approaches.add(new Doc2VecApproach());
        approaches.add(new JavaDocAndMethodsApproach());
        approaches.add(new SearchApproach());
        approaches.add(new LOCApproach());
      approaches.add(new SimilarReportsApproach());
        approaches.add(new VersionHistoryApproach());
        approaches.add(new BRTracerApproach());

        return approaches;
    }
}
