package icircles.recomposition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdual.AbstractDualEdge;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecompositionStrategySinglyPierced extends RecompositionStrategy {

    private static final Logger log = LogManager.getLogger(Recomposer.class);

    public List<Cluster> makeClusters(List<AbstractBasicRegion> zonesToSplit) {
        // Look for pairs of AbstractBasicRegions which differ by just a
        // single AbstractCurve - these pairs are potential double-clusters

        AbstractDualGraph adg = new AbstractDualGraph(zonesToSplit);
        return seekSinglePiercings(adg);
    }

    public static List<Cluster> seekSinglePiercings(AbstractDualGraph adg) {
        List<Cluster> result = new ArrayList<>();
        for (AbstractDualEdge e = adg.getLowDegreeEdge(); e != null; e = adg.getLowDegreeEdge()) {
            Cluster c = new Cluster(e.from.abr, e.to.abr);
            result.add(c);

            log.trace("Made single-pierced cluster: " + c);
            log.trace("Graph before trimming for cluster: " + adg.debug());

            adg.remove(e.from);
            adg.remove(e.to);

            log.trace("Graph after trimming for cluster: " + adg.debug());

        }
        //DEB.assertCondition(adg.getNumEdges() == 0, "non-empty adg edge set");
        result.addAll(seekNestedPiercings(adg));
        return result;
    }

    public static List<Cluster> seekNestedPiercings(AbstractDualGraph adg) {
        List<Cluster> result = new ArrayList<>();
        Iterator<AbstractDualNode> nIt = adg.getNodeIterator();
        while (nIt.hasNext()) {
            AbstractDualNode n = nIt.next();
            result.add(new Cluster(n.abr));

            log.trace("Adding nested cluster: " + n.abr);
        }
        return result;
    }
}
