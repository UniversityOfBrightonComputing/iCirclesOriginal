package icircles.guiswing;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.ConcreteDiagram;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public final class CirclesFrame extends JFrame {

    private static final Logger log = LogManager.getLogger(CirclesFrame.class);

    private final InputPanel inputPanel = new InputPanel();
    private final ResultPanel resultPanel = new ResultPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private int size = 200;

    public CirclesFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        populateFrame();
        pack();
        setVisible(true);

        getContentPane().addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent e) {}
            @Override
            public void componentMoved(ComponentEvent e) {}
            @Override
            public void componentResized(ComponentEvent e) {
                resize();
            }
            @Override
            public void componentShown(ComponentEvent e) {}
        });
        resize();
    }

    private void populateFrame() {
        getContentPane().add(inputPanel.getPanel(), BorderLayout.NORTH);
        getContentPane().add(resultPanel.getPanel(), BorderLayout.CENTER);
        getContentPane().add(settingsPanel.getPanel(), BorderLayout.SOUTH);
        draw("");
    }

    private void resize() {
        size = Math.min(getContentPane().getHeight() - settingsPanel.getPanel().getHeight()
                - inputPanel.getPanel().getHeight(), // allow for buttons etc
                getContentPane().getWidth()) - 30;

        log.trace("Content size: " + getContentPane().getWidth() + "," + getContentPane().getHeight());
        log.trace("Diagram size: " + size);
        redraw();
    }

    private void draw(String s) {
        inputPanel.setInput(s);
        redraw();
    }

    private void redraw() {
        goDraw(inputPanel.getCurrentDescription(),
                settingsPanel.getDecompStrategy(),
                settingsPanel.getRecompStrategy());
    }

    private void goDraw(String description, DecompositionType decompositionType, RecompositionType recompositionType) {
        try {
            ConcreteDiagram diagram = new ConcreteDiagram(new AbstractDescription(description),
                    size, decompositionType, recompositionType);
            resultPanel.show(diagram);
        } catch (CannotDrawException e) {
            resultPanel.showError(e.getMessage());
        }
    }

    class InputPanel {

        final JTextField inputJTF = new JTextField();
        final JPanel p = new JPanel();
        final static String ESCAPE_ACTION = "cancel-typing";
        final static String ENTER_ACTION = "go-draw";

        InputPanel() {
            p.setLayout(new BorderLayout());
            p.add(inputJTF);

            InputMap im = inputJTF.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = inputJTF.getActionMap();
            im.put(KeyStroke.getKeyStroke("ESCAPE"), ESCAPE_ACTION);
            am.put(ESCAPE_ACTION, new EscapeAction());

            im.put(KeyStroke.getKeyStroke("ENTER"), ENTER_ACTION);
            am.put(ENTER_ACTION, new RedrawListener());
        }

        String getCurrentDescription() {
            return inputJTF.getText();
        }

        void setInput(String s) {
            inputJTF.setText(s);
        }

        void clear() {
            inputJTF.setText("");
        }

        JPanel getPanel() {
            return p;
        }
    }

    class EscapeAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            inputPanel.clear();
        }
    }

    class RedrawListener extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            redraw();
        }
    }

    class ResultPanel {

        JPanel p = new JPanel();

        private SwingRenderer renderer = new SwingRenderer();

        ResultPanel() {
            p.setLayout(new BorderLayout());
            p.add(renderer, BorderLayout.CENTER);
        }

        JPanel getPanel() {
            return p;
        }

        void show(ConcreteDiagram diagram) {
            renderer.draw(diagram);
        }

        void showError(String errorMessage) {
            log.error("Cannot draw diagram: " + errorMessage);
        }
    }

    class SettingsPanel {
        String[] decompStrings = new String[] {"TYPE1", "TYPE2", "TYPE3", "TYPE4"};
        String[] recompStrings = new String[] {"TYPE1", "TYPE2", "TYPE3"};
        final JComboBox decompList = new JComboBox(decompStrings);
        final JComboBox recompList = new JComboBox(recompStrings);
        final JTextField testJTF = new JTextField("");
        final static String ENTER_ACTION = "go-draw-test";
        final JPanel p = new JPanel();

        SettingsPanel() {
            //Construct a GridLayout with 1 columns and an unspecified number of rows.
            //p.setLayout(new GridLayout(0,1));
            p.setLayout(new BorderLayout());

            JPanel topPanel = new JPanel();
            topPanel.setBorder(BorderFactory.createTitledBorder("settings"));
            topPanel.setLayout(new GridLayout(0, 1));
            p.add(topPanel, BorderLayout.NORTH);

            decompList.setSelectedIndex(decompStrings.length - 1);
            decompList.addActionListener(new RedrawListener());
            topPanel.add(decompList);

            recompList.setSelectedIndex(recompStrings.length - 1);
            recompList.addActionListener(new RedrawListener());
            topPanel.add(recompList);

            JPanel examplePanel = new JPanel();
            examplePanel.setBorder(BorderFactory.createTitledBorder("examples"));
            examplePanel.setLayout(new GridLayout(0, 1));
            p.add(examplePanel, BorderLayout.CENTER);

            JButton v3 = new JButton("draw Venn 3");
            v3.addActionListener(e -> draw("a b c ab ac bc abc"));
            examplePanel.add(v3);

//            final JLabel testLabel = new JLabel("draw test index:");
//            examplePanel.add(testLabel);
//            InputMap im = testJTF.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//            ActionMap am = testJTF.getActionMap();
//            im.put(KeyStroke.getKeyStroke("ENTER"), ENTER_ACTION);
//            examplePanel.add(testJTF);
//
//            JButton next = new JButton("draw next test case");
//            examplePanel.add(next);
//
//            JButton prev = new JButton("draw previous test case");
//            examplePanel.add(prev);
        }

        DecompositionType getDecompStrategy() {
            return DecompositionType.values()[decompList.getSelectedIndex()];
        }

        RecompositionType getRecompStrategy() {
            return RecompositionType.values()[recompList.getSelectedIndex()];
        }

        JPanel getPanel() {
            return p;
        }
    }
}
