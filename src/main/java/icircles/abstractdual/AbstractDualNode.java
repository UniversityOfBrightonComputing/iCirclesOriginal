package icircles.abstractdual;

import java.util.ArrayList;

import icircles.abstractdescription.AbstractBasicRegion;

public class AbstractDualNode {

    public AbstractBasicRegion abr;
    ArrayList<AbstractDualEdge> incidentEdges;

    AbstractDualNode(AbstractBasicRegion abr) {
        incidentEdges = new ArrayList<AbstractDualEdge>();
        this.abr = abr;
    }

    int degree() {
        return incidentEdges.size();
    }

    void removeEdge(AbstractDualEdge e) {
        incidentEdges.remove(e);
    }
}
