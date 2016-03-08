package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PathContour extends Contour {

    private final Path path;

    public PathContour(AbstractCurve curve, Path path) {
        super(curve);
        this.path = path;
        path.getElements().addAll(new ClosePath());
        //path.setFillRule(FillRule.EVEN_ODD);
        path.setFill(Color.TRANSPARENT);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public Shape getShape() {
        //return Shape.intersect(new Rectangle(1000, 1000), path);

        return path;
    }
}
