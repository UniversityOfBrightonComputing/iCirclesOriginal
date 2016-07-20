package icircles.guifx;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.*;
import icircles.decomposition.DecomposerFactory;
import icircles.decomposition.DecompositionStrategyType;
import icircles.graph.EulerDualNode;
import icircles.graph.MED;
import icircles.recomposition.RecomposerFactory;
import icircles.recomposition.RecompositionStrategyType;
import icircles.util.ExampleData;
import icircles.util.ExampleDiagram;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class Controller {

    @FXML
    private FXRenderer renderer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Menu drTypes;
    @FXML
    private Menu menuDiagrams;

    @FXML
    private TextField fieldInput;

//    @FXML
//    private TextArea areaInfo;

    @FXML
    private CheckMenuItem cbBruteforce;
    @FXML
    private CheckMenuItem cbEulerDual;

    private Alert progressDialog = new Alert(Alert.AlertType.INFORMATION);

    private ToggleGroup diagramCreatorToggle = new ToggleGroup();
    private ToggleGroup decompositionToggle = new ToggleGroup();
    private ToggleGroup recompositionToggle = new ToggleGroup();

    private List<AbstractDescription> historyUndo = new ArrayList<>();
    private List<AbstractDescription> historyRedo = new ArrayList<>();
    private AbstractDescription currentDescription = AbstractDescription.from("");

    public void initialize() {
        //areaInfo.setVisible(false);
        renderer.setTranslateX(-1000);
        renderer.setTranslateY(-1000);


        RadioMenuItem item1 = new RadioMenuItem("Original Creator");
        RadioMenuItem item2 = new RadioMenuItem("TwoStep Creator");

        item1.setToggleGroup(diagramCreatorToggle);
        item2.setToggleGroup(diagramCreatorToggle);

        item2.setSelected(true);

        for (DecompositionStrategyType dType : DecompositionStrategyType.values()) {
            RadioMenuItem item = new RadioMenuItem(dType.getUiName());
            item.setToggleGroup(decompositionToggle);

            if (dType == DecompositionStrategyType.INNERMOST) {
                item.setSelected(true);
            }

            item.setUserData(dType);

            drTypes.getItems().add(item);
        }

        drTypes.getItems().add(new SeparatorMenuItem());

        for (RecompositionStrategyType rType : RecompositionStrategyType.values()) {
            RadioMenuItem item = new RadioMenuItem(rType.getUiName());
            item.setToggleGroup(recompositionToggle);
            item.setSelected(true);
            item.setUserData(rType);

            drTypes.getItems().add(item);
        }

        fieldInput.setOnAction(e -> {
            AbstractDescription ad = AbstractDescription.from(fieldInput.getText());
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

        initMenuDiagrams();

        progressDialog.setTitle("Working...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressDialog.getDialogPane().setContent(progressIndicator);
    }

    private void initMenuDiagrams() {
        MenuItem itemVenn = new MenuItem("Venn3");
        itemVenn.setOnAction(e -> visualize(AbstractDescription.from("a b c abc ab ac bc")));

        MenuItem itemExamples = new MenuItem("Examples");
        itemExamples.setOnAction(e -> {
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);

            ListView<ExampleDiagram> list = new ListView<>(FXCollections.observableArrayList(ExampleData.exampleDiagrams));

            ScrollPane scrollPane = new ScrollPane(list);

            dialog.getDialogPane().setContent(scrollPane);

            dialog.showAndWait().ifPresent(buttonType -> {
                ExampleDiagram diagram = list.getSelectionModel().getSelectedItem();
                if (diagram != null) {
                    visualize(AbstractDescription.from(diagram.description));
                }
            });
        });

        menuDiagrams.getItems().addAll(itemVenn, itemExamples);
    }

    @FXML
    private void new_() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("iCirclesFX");
            stage.show();
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void open() {
        // TODO: load data in via a dialog

        AbstractDescription ad = AbstractDescription.from("a b c ab bc abd bcd");
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
            fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void redo() {
        if (!historyRedo.isEmpty()) {
            AbstractDescription ad = historyRedo.remove(historyRedo.size() - 1);
            historyUndo.add(ad);

            fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About iCirclesFX");
        alert.setHeaderText(null);
        alert.setContentText("iCirclesFX is a set visualization library.");
        alert.show();
    }

    private void showError(Throwable e) {
        System.out.println("Caught error:\n");
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ooops");
        alert.setContentText(e.getMessage());
        alert.show();
    }

    private void visualize(AbstractDescription description) {
        progressDialog.show();

        fieldInput.setText(description.getInformalDescription());
        currentDescription = description;
        int size = (int) Math.min(renderer.getWidth(), renderer.getHeight());

        Task<ConcreteDiagram> task = new CreateDiagramTask(description, size);

        Thread t = new Thread(task, "Diagram Creation Thread");
        t.start();
    }

    /**
     * A task of creating a diagram and subsequently drawing it on the screen.
     */
    private class CreateDiagramTask extends Task<ConcreteDiagram> {

        private AbstractDescription description;
        private int size;

        private long generationTime;

        private HamiltonianDiagramCreator newCreator;

        public CreateDiagramTask(AbstractDescription description, int size) {
            this.description = description;
            this.size = size;

            renderer.setPrefSize(fieldSize, fieldSize);
            renderer.setCanvasSize(fieldSize, fieldSize);

            renderer.setScaleX(0.25);
            renderer.setScaleY(0.25);

            renderer.rootSceneGraph.getChildren().clear();
        }

        @Override
        protected ConcreteDiagram call() throws Exception {
            long startTime = System.nanoTime();

            DecompositionStrategyType dType = (DecompositionStrategyType) decompositionToggle.getSelectedToggle().getUserData();
            RecompositionStrategyType rType = (RecompositionStrategyType) recompositionToggle.getSelectedToggle().getUserData();

            ConcreteDiagram diagram;

            if (rType == RecompositionStrategyType.DOUBLY_PIERCED) {
                diagram = new DiagramCreator(DecomposerFactory.newDecomposer(dType),
                        RecomposerFactory.newRecomposer(rType)).createDiagram(description, size);
            } else {
                //diagram = new TwoStepDiagramCreator().createDiagram(description, size);

                newCreator = new HamiltonianDiagramCreator();

                newCreator.getCurveToContour().addListener((MapChangeListener<? super AbstractCurve, ? super Contour>) change -> {
                    if (change.wasAdded()) {
                        Contour c = change.getValueAdded();

                        Shape s = c.getShape();
                        s.setStrokeWidth(6);
                        s.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
                        s.setFill(null);

                        Text label = new Text(c.getCurve().getLabel());
                        label.setFont(Font.font(72));
                        label.setTranslateX(s.getLayoutBounds().getMaxX());
                        label.setTranslateY(s.getLayoutBounds().getMinY());

                        Platform.runLater(() -> renderer.rootSceneGraph.getChildren().addAll(s, label));

//                        labels.forEach(renderer.rootSceneGraph.getChildren()::add);
//
//                        List<Point2D> points = modifiedDual.getNodes().stream().map(EulerDualNode::getPoint).collect(Collectors.toList());
//
//                        points.forEach(p -> {
//                            Circle point = new Circle(p.getX(), p.getY(), 2.5, Color.RED);
//
//                            Text coord = new Text((int)p.getX() + "," + (int)p.getY());
//                            coord.setTranslateX(p.getX());
//                            coord.setTranslateY(p.getY() - 10);
//
//                            renderer.rootSceneGraph.getChildren().addAll(point, coord);
//                        });

//            modifiedDual.getEdges().forEach(e -> {
//                e.getCurve().setStroke(Color.RED);
//                renderer.rootSceneGraph.getChildren().addAll(e.getCurve());
//            });

                        //renderer.rootSceneGraph.getChildren().addAll(modifiedDual.getBoundingCircle());
                    }
                });


                diagram = newCreator.createDiagram(description, size);
            }

            generationTime = System.nanoTime() - startTime;

            return diagram;
        }

        private double fieldSize = 4000.0;

        @Override
        protected void succeeded() {
            ConcreteDiagram diagram = getValue();

            MED modifiedDual = newCreator.getModifiedDual();
            Collection<Contour> contours = newCreator.getCurveToContour().values();

//
//
//
//
//
//            List<Text> labels = new ArrayList<>();
//
//            renderer.rootSceneGraph.getChildren().addAll(contours.stream().map(c -> {
//                Shape s = c.getShape();
//                s.setStrokeWidth(2);
//                s.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
//                s.setFill(null);
//
//                Text label = new Text(c.getCurve().getLabel());
//                label.setTranslateX(s.getLayoutBounds().getMaxX());
//                label.setTranslateY(s.getLayoutBounds().getMinY());
//                labels.add(label);
//
//                return s;
//            }).collect(Collectors.toList()));
//
//            labels.forEach(renderer.rootSceneGraph.getChildren()::add);

            newCreator.getDebugPoints().forEach(p -> {
                Circle point = new Circle(p.getX(), p.getY(), 10, Color.LIGHTSKYBLUE);

                Text coord = new Text((int) p.getX() + "," + (int) p.getY());
                coord.setTranslateX(p.getX());
                coord.setTranslateY(p.getY() - 10);

                renderer.rootSceneGraph.getChildren().addAll(point, coord);
            });
//

            if (cbEulerDual.isSelected()) {

                modifiedDual.getNodes().stream().map(EulerDualNode::getPoint).forEach(p -> {
                    Circle point = new Circle(p.getX(), p.getY(), 10, Color.RED);

                    Text coord = new Text((int) p.getX() + "," + (int) p.getY());
                    coord.setTranslateX(p.getX());
                    coord.setTranslateY(p.getY() - 10);

                    renderer.rootSceneGraph.getChildren().addAll(point, coord);
                });

                modifiedDual.getEdges().forEach(e -> {
                    e.getCurve().setStroke(Color.RED);
                    e.getCurve().setStrokeWidth(6);
                    renderer.rootSceneGraph.getChildren().addAll(e.getCurve());
                });
            }


//
//            renderer.rootSceneGraph.getChildren().addAll(modifiedDual.getBoundingCircle());




//            try {
//                long startTime = System.nanoTime();
//
//                renderer.draw(diagram, cbEulerDual.isSelected());
//
//                // highlighting
//                renderer.getShadedZones().forEach(zone -> {
//                    zone.setOnMouseEntered(e -> {
//                        ((Shape) zone).setFill(Color.YELLOW);
//                        areaInfo.setText(((ConcreteZone) zone.getUserData()).toDebugString());
//                    });
//
//                    zone.setOnMouseExited(e -> {
//                        ((Shape) zone).setFill(Color.TRANSPARENT);
//                    });
//                });
//
//                long estimatedTime = System.nanoTime() - startTime;
//
//                System.out.printf("Diagram creation took: %.3f sec\n", generationTime / 1000000000.0);
//                System.out.printf("Diagram drawing took:  %.3f sec\n", estimatedTime / 1000000000.0);
//            } catch (Exception e) {
//                showError(e);
//            }

            progressDialog.hide();
        }

        @Override
        protected void failed() {
            progressDialog.hide();

            Throwable error = getException();
            if (error != null)
                showError(error);
            else
                showError(new RuntimeException("Unresolved error. Exception returned null"));

            succeeded();
        }
    }
}
