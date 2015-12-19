package icircles.guiswing;

import icircles.abstractdescription.CurveLabel;
import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CirclesPanel extends JPanel {

    // TODO: why do we even need this?
    private static final Map<CurveLabel, Color> labelsToColours = new HashMap<>();

    public CirclesPanel(String desc, String failureMessage, ConcreteDiagram cd, int size,
            boolean useColors) 
    	{
        labelsToColours.put(CurveLabel.get("a"), new Color(0, 100, 0)); // dark green
        labelsToColours.put(CurveLabel.get("b"), Color.red);
        labelsToColours.put(CurveLabel.get("c"), Color.blue);
        labelsToColours.put(CurveLabel.get("d"), new Color(150, 50, 0));
        labelsToColours.put(CurveLabel.get("e"), new Color(0, 50, 150));
        labelsToColours.put(CurveLabel.get("f"), new Color(100, 0, 100));
        labelsToColours.put(CurveLabel.get("g"), new Color(0, 100, 0)); // dark green
        labelsToColours.put(CurveLabel.get("h"), Color.red);
        labelsToColours.put(CurveLabel.get("i"), Color.blue);
        labelsToColours.put(CurveLabel.get("j"), new Color(150, 50, 0));
        labelsToColours.put(CurveLabel.get("k"), new Color(0, 50, 150));
        labelsToColours.put(CurveLabel.get("l"), new Color(100, 0, 100));
        labelsToColours.put(CurveLabel.get("m"), new Color(0, 100, 0)); // dark green
        labelsToColours.put(CurveLabel.get("n"), Color.red);
        labelsToColours.put(CurveLabel.get("o"), Color.blue);
        labelsToColours.put(CurveLabel.get("p"), new Color(150, 50, 0));
        labelsToColours.put(CurveLabel.get("q"), new Color(0, 50, 150));
        labelsToColours.put(CurveLabel.get("r"), new Color(100, 0, 100));
        labelsToColours.put(CurveLabel.get("s"), new Color(0, 100, 0)); // dark green
        labelsToColours.put(CurveLabel.get("t"), Color.red);
        labelsToColours.put(CurveLabel.get("u"), Color.blue);
        labelsToColours.put(CurveLabel.get("v"), new Color(150, 50, 0));
        labelsToColours.put(CurveLabel.get("w"), new Color(0, 50, 150));
        labelsToColours.put(CurveLabel.get("x"), new Color(100, 0, 100));
        labelsToColours.put(CurveLabel.get("y"), new Color(0, 100, 0)); // dark green
        labelsToColours.put(CurveLabel.get("z"), Color.red);

        setLayout(new BorderLayout());
        JLabel jl = new JLabel(desc);
        Font f = new Font("Dialog", Font.PLAIN, 12);
        if (desc.length() > 24) {
            f = new Font("Dialog", Font.PLAIN, 8);
        } else if (desc.length() > 16) {
            f = new Font("Dialog", Font.PLAIN, 8);
        } else if (desc.length() > 14) {
            f = new Font("Dialog", Font.PLAIN, 10);
        }
        jl.setFont(f);
        jl.setHorizontalAlignment(JLabel.CENTER);
        add(jl, BorderLayout.NORTH);

        int padding = 5;
        DiagramPanel dp = new DiagramPanel(cd, failureMessage, useColors);

        if (cd == null) {
            dp.setPreferredSize(new Dimension(size + padding, size + padding));
            dp.setMinimumSize(new Dimension(size + padding, size + padding));
            dp.setMaximumSize(new Dimension(size + padding, size + padding));
        }

        JPanel containsDiag = new JPanel();
        containsDiag.setLayout(new FlowLayout());
        containsDiag.add(dp);

        add(containsDiag, BorderLayout.CENTER);
    }

    static class DiagramPanel extends JPanel {
        ConcreteDiagram diagram;
        String failureMessage;
        private boolean useColors;

        DiagramPanel(ConcreteDiagram diagram, String failureMessage, boolean useColors) {
            setBackground(Color.white);
            this.diagram = diagram;
            this.failureMessage = failureMessage;
            this.useColors = useColors;
            if (diagram != null) {
                setPreferredSize(new Dimension((int) (diagram.getBox().width) + 5,
                        (int) (diagram.getBox().height) + 5));
            }
        }

        public void paint(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (diagram == null) {
                this.setBackground(Color.red);
                super.paint(g);
                if (failureMessage != null) {
                    g.drawString(failureMessage, 0, (int) (this.getHeight() * 0.5));
                }
                return;
            }
            // draw the diagram
            super.paint(g);

            // draw shaded zones

//            g.setColor(Color.lightGray);
//            List<ConcreteZone> zones = diagram.getShadedZones();
//            for (ConcreteZone z : zones) {
//                ((Graphics2D) g).fill(z.getShape(diagram.getBox()));
//            }

            ((Graphics2D) g).setStroke(new BasicStroke(2));
            List<CircleContour> circles = diagram.getCircles();

            // draw curve contours

            for (CircleContour cc : circles) {

                if (useColors) {
                    Color col = labelsToColours.get(cc.ac.getLabel());
                    if (col == null) {
                        col = Color.black;
                    }
                    g.setColor(col);
                } else {
                    g.setColor(Color.black);
                }

                ((Graphics2D) g).draw(cc.getCircle());
            }

            // draw labels

            for (CircleContour cc : circles) {
            	if (cc.ac.getLabel() == null)
            		continue;

                if (useColors) {
                    Color col = labelsToColours.get(cc.ac.getLabel());
                    if (col == null) {
                        col = Color.black;
                    }
                    g.setColor(col);
                } else {
                    g.setColor(Color.black);
                }

                ((Graphics2D) g).drawString(cc.ac.getLabel().getLabel(),
                        (int) cc.getLabelXPosition(),
                        (int) cc.getLabelYPosition());
            }
        }
    }
}
