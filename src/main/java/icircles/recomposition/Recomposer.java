package icircles.recomposition;

import icircles.decomposition.DecompositionStep;

import java.util.List;

public interface Recomposer {
    List<RecompositionStep> recompose(List<DecompositionStep> decompositionSteps);
}
