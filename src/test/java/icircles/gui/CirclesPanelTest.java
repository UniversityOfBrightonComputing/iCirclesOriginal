package icircles.gui;

import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.ConcreteDiagram;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;
import icircles.util.DEB;

import javax.swing.*;
import java.awt.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class CirclesPanelTest {
    /**
     * This can be used to obtain a drawing of an abstract diagram.
     * @param ad the description to be drawn
     * @param size the size of the drawing panel
     * @return circles panel
     */
    public static CirclesPanel makeCirclesPanel(AbstractDescription ad,
                                                String diagText,
                                                int size)
    {
        String failuremessage = "no failure";
        ConcreteDiagram cd = null;
        try
        {
            cd = ConcreteDiagram.makeConcreteDiagram(DecompositionType.PIERCED_FIRST, RecompositionType.DOUBLY_PIERCED,
                    ad, size);
        }
        catch(CannotDrawException ex)
        {
            failuremessage = ex.message;
        }

        CirclesPanel cp = new CirclesPanel(diagText, failuremessage, cd, size,
                true); // do use colors

        return cp;
    }

    public static void main(String[] args) {
        // See the implementation of makeForTesting to see how to make an
        // AbstractDescription from scratch.
        AbstractDescription ad = AbstractDescription.makeForTesting(
                //"qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al m mn nc bc bco bo boj bp bop cq cqb rs ra s t");
                "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al m mn nc bc bco bo boj bp bop cq cqb rs ra s");
        //"a ab b c");

        DEB.level = 3; // generates intermediate frames

        int size = 600;

        CirclesPanel cp = makeCirclesPanel(ad, "a sample diagram", size);

        JFrame viewingFrame = new JFrame("frame to hold a CirclesPanel");
        JScrollPane scrollPane = new JScrollPane(cp);
        viewingFrame.getContentPane().setPreferredSize(new Dimension(Math.min(size,  800), Math.min(size,  800)));
        viewingFrame.getContentPane().add(scrollPane);
        viewingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        viewingFrame.pack();
        viewingFrame.setVisible(true);
    }
}
