package icircles;

import icircles.decomposition.DecompositionStrategyType;
import icircles.recomposition.RecompositionType;

public class TestDatum {

    public String description;
    public DecompositionStrategyType decomp_strategy;
    public RecompositionType recomp_strategy;
    public double expectedChecksum;

    public TestDatum(String string,
                     DecompositionStrategyType decomp_strategy,
                     RecompositionType recomp_strategy,
                     double checksum) {
        description = string;
        this.decomp_strategy = decomp_strategy;
        this.recomp_strategy = recomp_strategy;
        expectedChecksum = checksum;
    }
}
