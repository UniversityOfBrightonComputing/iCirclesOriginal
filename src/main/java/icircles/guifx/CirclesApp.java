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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class CirclesApp extends Application {

    private GraphicsContext g;
    private Pane root;
    private Pane shadedZonesRoot;

    private Parent createContent() {
        BorderPane pane = new BorderPane();
        pane.setPrefSize(800, 600);

        root = new Pane();
        root.setPrefSize(800, 500);

        shadedZonesRoot = new Pane();
        shadedZonesRoot.setPrefSize(800, 500);

        Canvas canvas = new Canvas(800, 600);
        g = canvas.getGraphicsContext2D();
        root.getChildren().addAll(shadedZonesRoot, canvas);
        pane.setCenter(root);

        TextField input = new TextField();
        input.setOnAction(e -> draw(input.getText()));
        pane.setTop(input);

        Button btnDrawVenn = new Button("VENN3");
        btnDrawVenn.setOnAction(e -> drawVenn3());
        pane.setBottom(btnDrawVenn);

        return pane;
    }

    private void draw(String description) {
        try {
            ConcreteDiagram diagram = ConcreteDiagram.makeConcreteDiagram(DecompositionType.PIERCED_FIRST,
                    RecompositionType.DOUBLY_PIERCED, AbstractDescription.makeForTesting(description), 550);
            draw(diagram);
        } catch (CannotDrawException e) {
            e.printStackTrace();
        }
    }

    private void draw(ConcreteDiagram diagram) {
        g.clearRect(0, 0, 800, 600);

        // draw shaded zones
        g.setFill(Color.LIGHTGREY);
        shadedZonesRoot.getChildren().clear();
        List<ConcreteZone> zones = diagram.getShadedZones();
        for (ConcreteZone zone : zones) {
            Shape area = zone.getShapeFX(diagram.getBox());
            area.setFill(Color.LIGHTGREY);

            shadedZonesRoot.getChildren().addAll(area);
        }

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

            g.fillText(cc.ac.getLabel().getLabel(), cc.getLabelXPosition(), cc.getLabelYPosition());
        }
    }

    private void drawVenn3() {
        draw("a b c abc ab ac bc");
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle("iCircles FX");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
