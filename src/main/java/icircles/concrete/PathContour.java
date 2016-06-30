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

    @Override
    public Shape getShape() {
        Shape shape = Shape.intersect(new Rectangle(5000, 5000), path);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.DARKBLUE);
        shape.setStrokeWidth(2);

        return shape;
    }
}
