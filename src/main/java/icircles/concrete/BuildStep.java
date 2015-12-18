package icircles.concrete;

import java.util.ArrayList;
import java.util.List;

import icircles.recomposition.RecompositionData;

public class BuildStep {

    public List<RecompositionData> recomp_data = new ArrayList<>();
    public BuildStep next = null;

    BuildStep(RecompositionData rd) {
        recomp_data.add(rd);
    }
}
