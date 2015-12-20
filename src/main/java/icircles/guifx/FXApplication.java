package icircles.guifx;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXApplication extends Application {

    private static final Logger log = LogManager.getLogger(FXApplication.class);

//    private FXRenderer renderer = new FXRenderer();
//
//    private ChoiceBox<DecompositionType> decompBox;
//    private ChoiceBox<RecompositionType> recompBox;
//
//    BorderPane pane = new BorderPane();
//
//    private Parent createContent() {
//        pane.setPrefSize(800, 600);
//        pane.setCenter(renderer);
//        pane.setLeft(createContentLeft());
//
//        return pane;
//    }
//
//    private Parent createContentLeft() {
//        TextField input = new TextField();
//        input.setOnAction(e -> draw(input.getText()));
//
//        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
//            draw(input.getText());
//        });
//        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            draw(input.getText());
//        });
//
//        decompBox = new ChoiceBox<>(FXCollections.observableArrayList(DecompositionType.values()));
//        recompBox = new ChoiceBox<>(FXCollections.observableArrayList(RecompositionType.values()));
//
//        decompBox.getSelectionModel().selectLast();
//        recompBox.getSelectionModel().selectLast();
//
//        Button btnDrawVenn = new Button("VENN3");
//        btnDrawVenn.setOnAction(e -> drawVenn3());
//
//        VBox vbox = new VBox(50, input, decompBox, recompBox, btnDrawVenn);
//        vbox.setAlignment(Pos.CENTER);
//
//        return vbox;
//    }

//    private void draw(String description) {
//        try {
//            System.out.println(new AbstractDescription(description).toDebugString());
//
//            ConcreteDiagram diagram = new ConcreteDiagram(new AbstractDescription(description),
//                    Math.min((int)renderer.getWidth(), (int)renderer.getHeight()),
//                    decompBox.getValue(),
//                    recompBox.getValue());
//
//            System.out.println(diagram);
//
//            Map<AbstractCurve, List<CircleContour> > duplicates = diagram.findDuplicateContours();
//
//            log.trace("Duplicates: " + duplicates);
//            duplicates.values().forEach(contours -> {
//                for (CircleContour contour : contours) {
//                    log.trace("Contour " + contour + " is in " + diagram.getZonesContainingContour(contour));
//                }
//            });
//
//            renderer.draw(diagram);
//        } catch (CannotDrawException e) {
//            e.printStackTrace();
//        }
//    }

//    private void drawVenn3() {
//        draw("a b c abc ab ac bc");
//    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("iCircles FX");
        stage.show();
    }
}
