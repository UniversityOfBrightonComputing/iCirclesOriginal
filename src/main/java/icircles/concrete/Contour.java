package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import javafx.scene.shape.Shape;

/**
 * A concrete contour.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public abstract class Contour {

    /**
     * Abstract representation of this concrete contour.
     */
    private final AbstractCurve curve;

    public Contour(AbstractCurve curve) {
        this.curve = curve;
    }

    /**
     * @return abstract curve associated with this contour
     */
    public final AbstractCurve getCurve() {
        return curve;
    }

    public abstract Shape getShape();
}
