package icircles.recomposition;

import java.util.ArrayList;
import java.util.List;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecompositionStrategyDoublyPierced extends RecompositionStrategy {

    private static final Logger log = LogManager.getLogger(Recomposer.class);

    public List<Cluster> makeClusters(List<AbstractBasicRegion> zonesToSplit) {
        // Look for four-tuples of AbstractBasicRegions which differ by
        // two AbstractCurves - these four-tuples are potential double-clusters
        List<Cluster> result = new ArrayList<>();

        AbstractDualGraph adg = new AbstractDualGraph(zonesToSplit);

        log.trace("Zones to split: " + zonesToSplit);

        for (List<AbstractDualNode> nodes = adg.getFourTuple(); nodes != null; nodes = adg.getFourTuple()) {
            if (nodes.isEmpty()) {
                break;
            }

            Cluster c = new Cluster(nodes.get(0).abr,
                    nodes.get(1).abr,
                    nodes.get(2).abr,
                    nodes.get(3).abr);
            result.add(c);

            log.trace("Made cluster: " + c);
            log.trace("Graph before trimming for cluster: " + (adg.debug()));

            adg.remove(nodes.get(0));
            adg.remove(nodes.get(1));
            adg.remove(nodes.get(2));
            adg.remove(nodes.get(3));

            log.trace("Graph after trimming for cluster: " + adg.debug());
        }

        result.addAll(RecompositionStrategySinglyPierced.seekSinglePiercings(adg));

        return result;
    }
}
