package icircles.guifx;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.ConcreteDiagram;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class Controller {

    @FXML
    private FXRenderer renderer;

    @FXML
    private Menu drTypes;

    private ToggleGroup decompositionToggle = new ToggleGroup();
    private ToggleGroup recompositionToggle = new ToggleGroup();

    public void initialize() {
        for (DecompositionType dType : DecompositionType.values()) {
            RadioMenuItem item = new RadioMenuItem(dType.getUiName());
            item.setToggleGroup(decompositionToggle);
            item.setSelected(true);
            item.setUserData(dType);

            drTypes.getItems().add(item);
        }

        drTypes.getItems().add(new SeparatorMenuItem());

        for (RecompositionType rType : RecompositionType.values()) {
            RadioMenuItem item = new RadioMenuItem(rType.getUiName());
            item.setToggleGroup(recompositionToggle);
            item.setSelected(true);
            item.setUserData(rType);

            drTypes.getItems().add(item);
        }
    }

    @FXML
    private void new_() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("iCircles FX");
            stage.show();
        } catch (Exception e) {
            // TODO: handle
            e.printStackTrace();
        }
    }

    @FXML
    private void open() {
        // TODO: load data in via a dialog

        AbstractDescription ad = new AbstractDescription("a b c ab ac bc abc");
        visualize(ad);
    }

    @FXML
    private void save() {
        // TODO: save whatever we are working on now
    }

    @FXML
    private void saveAs() {
        // TODO: allow user to choose save format
    }

    @FXML
    private void quit() {
        System.exit(0);
    }

    @FXML
    private void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About iCircles");
        alert.setHeaderText(null);
        alert.setContentText("iCircles is a set visualization library.");
        alert.show();
    }

    private void visualize(AbstractDescription description) {
        int size = (int) Math.min(renderer.getWidth(), renderer.getHeight());

        DecompositionType dType = (DecompositionType) decompositionToggle.getSelectedToggle().getUserData();
        RecompositionType rType = (RecompositionType) recompositionToggle.getSelectedToggle().getUserData();

        try {
            ConcreteDiagram diagram = new ConcreteDiagram(description, size, dType, rType);
            renderer.draw(diagram);
        } catch (CannotDrawException e) {
            // TODO: show it to user *nicely*
            e.printStackTrace();
        }
    }
}
