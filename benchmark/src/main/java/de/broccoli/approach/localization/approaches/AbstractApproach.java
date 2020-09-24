package de.broccoli.approach.localization.approaches;


import de.broccoli.approach.localization.api.Approach;

import java.util.Collections;
import java.util.List;

public abstract class AbstractApproach implements Approach {

    @Override
    public List<String> getApproachLabels() {
        return Collections.singletonList(getApproachName());
    }

    public void shutdown()
    {
        //
    }
}
