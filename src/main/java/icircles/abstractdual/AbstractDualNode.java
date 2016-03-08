package icircles.abstractdual;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.ArrayList;
import java.util.List;

public class AbstractDualNode {

    private final AbstractBasicRegion zone;

    AbstractDualNode(AbstractBasicRegion zone) {
        this.zone = zone;
    }

    public AbstractBasicRegion getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return "Node[" + zone.toString() + "]";
    }
}
