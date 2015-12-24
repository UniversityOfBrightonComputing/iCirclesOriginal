package icircles.guifx;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.decomposition.DecompositionStrategyType;
import icircles.recomposition.RecompositionType;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class Controller {

    @FXML
    private FXRenderer renderer;

    @FXML
    private Menu drTypes;
    @FXML
    private Menu menuDiagrams;

    @FXML
    private TextField fieldInput;

    @FXML
    private TextArea areaInfo;

    private ToggleGroup decompositionToggle = new ToggleGroup();
    private ToggleGroup recompositionToggle = new ToggleGroup();

    private List<AbstractDescription> historyUndo = new ArrayList<>();
    private List<AbstractDescription> historyRedo = new ArrayList<>();
    private AbstractDescription currentDescription = new AbstractDescription("");

    public void initialize() {
        for (DecompositionStrategyType dType : DecompositionStrategyType.values()) {
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

        fieldInput.setOnAction(e -> {
            AbstractDescription ad = new AbstractDescription(fieldInput.getText());
            historyUndo.add(ad);
            visualize(ad);
        });

        fieldInput.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undo();
            }
        });

        historyUndo.add(currentDescription);

        decompositionToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null)
                visualize(currentDescription);
        });
        recompositionToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null)
                visualize(currentDescription);
        });

        MenuItem itemVenn = new MenuItem("Venn3");
        itemVenn.setOnAction(e -> visualize(new AbstractDescription("a b c abc ab ac bc")));
        menuDiagrams.getItems().addAll(itemVenn);
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
            showError(e);
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
        showError(new UnsupportedOperationException("Not yet implemented!"));
    }

    @FXML
    private void saveAs() {
        ExtensionFilter filterPNG = new ExtensionFilter("PNG image", "*.png");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As...");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName("diagram");
        fileChooser.getExtensionFilters().addAll(filterPNG);
        fileChooser.setSelectedExtensionFilter(filterPNG);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            WritableImage fxImage = renderer.snapshot(null, null);
            BufferedImage img = SwingFXUtils.fromFXImage(fxImage, null);

            try {
                ImageIO.write(img, "png", file);
            } catch (IOException e) {
                showError(e);
            }
        }
    }

    @FXML
    private void quit() {
        System.exit(0);
    }

    @FXML
    private void undo() {
        if (historyUndo.size() > 1) {
            historyRedo.add(historyUndo.remove(historyUndo.size() - 1));

            AbstractDescription ad = historyUndo.get(historyUndo.size() - 1);
            //fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void redo() {
        if (!historyRedo.isEmpty()) {
            AbstractDescription ad = historyRedo.remove(historyRedo.size() - 1);
            historyUndo.add(ad);

            //fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About iCircles");
        alert.setHeaderText(null);
        alert.setContentText("iCircles is a set visualization library.");
        alert.show();
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ooops");
        alert.setContentText(e.getMessage());
        alert.show();
    }

    private void visualize(AbstractDescription description) {
        fieldInput.setText(description.getInformalDescription());
        currentDescription = description;
        int size = (int) Math.min(renderer.getWidth(), renderer.getHeight());

        DecompositionStrategyType dType = (DecompositionStrategyType) decompositionToggle.getSelectedToggle().getUserData();
        RecompositionType rType = (RecompositionType) recompositionToggle.getSelectedToggle().getUserData();

        try {
            ConcreteDiagram diagram = new ConcreteDiagram(description, size, dType, rType);
            renderer.draw(diagram);

            // highlighting
            renderer.getShadedZones().forEach(zone -> {
                zone.setOnMouseEntered(e -> {
                    ((Shape)zone).setFill(Color.YELLOW);
                    areaInfo.setText(((ConcreteZone)zone.getUserData()).toDebugString());
                });

                zone.setOnMouseExited(e -> {
                    ((Shape)zone).setFill(Color.TRANSPARENT);
                });
            });
        } catch (Exception e) {
            showError(e);
        }
    }
}
