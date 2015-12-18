package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.List;
import java.util.stream.Collectors;

public class RecompositionStrategyNested extends RecompositionStrategy {

    public List<Cluster> makeClusters(List<AbstractBasicRegion> zones_to_split) {
        return zones_to_split.stream()
                .map(Cluster::new)
                .collect(Collectors.toList());
    }
}
