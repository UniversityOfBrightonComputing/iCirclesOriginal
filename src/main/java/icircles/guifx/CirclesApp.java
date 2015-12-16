package icircles.guifx;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.concrete.DiagramCreator;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.decomposition.DecompositionStrategy;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionStep;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class CirclesApp extends Application {

    private GraphicsContext g;

    private Parent createContent() {
        BorderPane pane = new BorderPane();
        pane.setPrefSize(800, 600);

        Canvas canvas = new Canvas(800, 600);
        g = canvas.getGraphicsContext2D();
        pane.setCenter(canvas);

        TextField input = new TextField();
        pane.setTop(input);

        Button btnDrawVenn = new Button("VENN3");
        btnDrawVenn.setOnAction(e -> drawVenn3());
        pane.setBottom(btnDrawVenn);

        return pane;
    }

    private void drawVenn3() {
        ConcreteDiagram diagram = null;
        String failureMessage = null;
        try {
            Decomposer d = new Decomposer(DecompositionType.PIERCED_FIRST);
            List<DecompositionStep> d_steps = d.decompose(AbstractDescription.makeForTesting("a b c abc ab ac bc"));

            Recomposer r = new Recomposer(RecompositionType.DOUBLY_PIERCED);
            List<RecompositionStep> r_steps = r.recompose(d_steps);

            DiagramCreator dc = new DiagramCreator(d_steps, r_steps, 400);
            diagram = dc.createDiagram(400);
        } catch (CannotDrawException x) {
            failureMessage = x.message;
            System.out.println(failureMessage);
            return;
        }

        /////////////////////////// DRAW





        // draw shaded zones

//        g.setFill(Color.LIGHTGREY);
//        List<ConcreteZone> zones = diagram.getShadedZones();
//        for (ConcreteZone z : zones) {
//            g.fi
//
//            (g).fill(z.getShape(diagram.getBox()));
//        }
//
//        ((Graphics2D) g).setStroke(new BasicStroke(2));
        List<CircleContour> circles = diagram.getCircles();

        // draw curve contours
        g.setFill(Color.BLACK);
        g.setStroke(Color.BLUE);

        for (CircleContour cc : circles) {
            Ellipse2D.Double circle = cc.getCircle();

            g.strokeOval(circle.getX(), circle.getY(), circle.getWidth(), circle.getHeight());
        }

        // draw labels

        for (CircleContour cc : circles) {
            if (cc.ac.getLabel() == null)
                continue;

            g.fillText(cc.ac.getLabel().getLabel(),
                    (int) cc.getLabelXPosition(),
                    (int) cc.getLabelYPosition());
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
