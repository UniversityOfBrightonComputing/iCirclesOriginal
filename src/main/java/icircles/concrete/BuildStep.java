package icircles.concrete;

import java.util.ArrayList;
import java.util.List;

import icircles.recomposition.RecompData;

public class BuildStep {

    public List<RecompData> recomp_data = new ArrayList<>();
    public BuildStep next = null;

    BuildStep(RecompData rd) {
        recomp_data.add(rd);
    }
}
