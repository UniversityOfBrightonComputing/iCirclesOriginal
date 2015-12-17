package icircles.gui;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.DiagramCreator;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionStep;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

public class CirclesFrame extends JFrame {

    final InputPanel inputPanel = new InputPanel();
    final ResultPanel resultPanel = new ResultPanel();
    final SettingsPanel settingsPanel = new SettingsPanel();
    private int SIZE = 200;
    boolean useColors = true;

    CirclesFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        populateFrame();
        pack();
        setVisible(true);

        //drawTest(63);
        getContentPane().addComponentListener(new ComponentListener() {

            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
                respondToResize();
            }

            public void componentShown(ComponentEvent e) {
            }
        });
        respondToResize();
    }

    void respondToResize() {
        SIZE = Math.min(getContentPane().getHeight() - settingsPanel.getPanel().getHeight()
                - inputPanel.getPanel().getHeight(), // allow for buttons etc
                getContentPane().getWidth()) - 30;

        System.out.println("new size is " + getContentPane().getHeight() + "," + getContentPane().getWidth());
        System.out.println("SIZE is " + SIZE);
        redraw();
    }

    void draw(String s) {
        inputPanel.setInput(s);
        redraw();
    }

    void populateFrame() {
        getContentPane().add(inputPanel.getPanel(), BorderLayout.NORTH);
        getContentPane().add(resultPanel.getPanel(), BorderLayout.CENTER);
        getContentPane().add(settingsPanel.getPanel(), BorderLayout.SOUTH);
        draw("");
    }

//    void drawTest(int test_num) {
//        settingsPanel.setDecompStrategy(TestData.test_data[test_num - 1].decomp_strategy);
//        settingsPanel.setRecompStrategy(TestData.test_data[test_num - 1].recomp_strategy);
//        draw(TestData.test_data[test_num - 1].description);
//    }

    private void goDraw(String description, DecompositionType decomp_strategy, RecompositionType recomp_strategy) {
        ConcreteDiagram cd = null;
        String failureMessage = null;
        try {
            Decomposer d = new Decomposer(decomp_strategy);
            List<DecompositionStep> d_steps = d.decompose(AbstractDescription.makeForTesting(description));

            Recomposer r = new Recomposer(recomp_strategy);
            List<RecompositionStep> r_steps = r.recompose(d_steps);

            DiagramCreator dc = new DiagramCreator(d_steps, r_steps);
            cd = dc.createDiagram(SIZE);
        } catch (CannotDrawException x) {
            failureMessage = x.message;
        }

        resultPanel.show(description, failureMessage, cd, SIZE, useColors);
    }

    void redraw() {
        goDraw(inputPanel.getCurrentDescription(),
                settingsPanel.getDecompStrategy(),
                settingsPanel.getRecompStrategy());
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

        ResultPanel() {
            p.setLayout(new BorderLayout());
        }

        JPanel getPanel() {
            return p;
        }

        void show(String description,
                String failureMessage,
                ConcreteDiagram cd,
                int SIZE,
                boolean useColors) {
            JPanel jp = new CirclesPanel(description, failureMessage, cd, SIZE, useColors);
            p.removeAll();
            p.invalidate();
            p.add(jp);
            p.revalidate();
            getContentPane().repaint();
        }
    }

    class SettingsPanel {

        private int test_num = 0;
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

            final JCheckBox jcb = new JCheckBox("Show contours with colours", true);
            jcb.addActionListener(e -> {
                useColors = jcb.isSelected();
                redraw();
            });
            topPanel.add(jcb);

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

            final JLabel testLabel = new JLabel("draw test index:");
            examplePanel.add(testLabel);
            InputMap im = testJTF.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = testJTF.getActionMap();
            im.put(KeyStroke.getKeyStroke("ENTER"), ENTER_ACTION);
            //am.put(ENTER_ACTION, new TestListener());
            examplePanel.add(testJTF);

            JButton next = new JButton("draw next test case");
//            next.addActionListener(e -> {
//                if (testJTF.getText().length() == 0) {
//                    test_num = 2;
//                } else {
//                    test_num += 3;
//                    if (test_num > TestData.test_data.length - 1) {
//                        test_num = test_num % 3;
//                    }
//                }
//                testJTF.setText("" + (test_num + 1));
//                drawTest(test_num + 1);
//            });
            examplePanel.add(next);

            JButton prev = new JButton("draw previous test case");
//            prev.addActionListener(e -> {
//                if (testJTF.getText().length() == 0) {
//                    test_num = getBiggestTestNum(TestData.test_data.length, 3);
//                } else {
//                    test_num -= 3;
//                    if (test_num <= 0) {
//                        test_num = getBiggestTestNum(TestData.test_data.length, test_num);
//                    }
//                }
//                testJTF.setText("" + (test_num + 1));
//                drawTest(test_num + 1);
//            });
            examplePanel.add(prev);
        }

        int getBiggestTestNum(int length_test_data_array, int n) {
            while (n < 0) {
                n += 3;
            }
            // seek maximal i with
            // i < length_test_data_array
            // and i % 3 == match_by_nine % 3
            int result = length_test_data_array - 1;
            while (result % 3 != n % 3) // can't be bothered to work out arithmetic...
            {
                result--;
            }
            return result;
        }

        void setDecompStrategy(DecompositionType type) {
            int i = 0;
            for (DecompositionType t : DecompositionType.values()) {
                if (t == type) {
                    decompList.setSelectedIndex(i);
                    break;
                }
                i++;
            }
        }

        DecompositionType getDecompStrategy() {
            return DecompositionType.values()[decompList.getSelectedIndex()];
        }

        void setRecompStrategy(RecompositionType type) {
            int i = 0;
            for (RecompositionType t : RecompositionType.values()) {
                if (t == type) {
                    recompList.setSelectedIndex(i);
                    break;
                }
                i++;
            }
        }

        RecompositionType getRecompStrategy() {
            return RecompositionType.values()[recompList.getSelectedIndex()];
        }

        JPanel getPanel() {
            return p;
        }

//        class TestListener extends AbstractAction {
//
//            public void actionPerformed(ActionEvent ev) {
//                try {
//                    int i = Integer.parseInt(testJTF.getText());
//                    if (i < 1 || i > TestData.test_data.length) {
//                        JOptionPane.showMessageDialog(null, "test number should be between 1 and " + TestData.test_data.length);
//                        return;
//                    }
//                    test_num = i - 1;
//                    drawTest(test_num + 1);
//                } catch (NumberFormatException x) {
//                    JOptionPane.showMessageDialog(null, "type an integer between 1 and " + TestData.test_data.length);
//                }
//            }
//        }
    }
}
