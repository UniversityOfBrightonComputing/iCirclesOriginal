package icircles.util;

import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.guiswing.CirclesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DEB {

    public static int level = 0; // DO NOT CHANGE HERE - change in code with main e.g. test harness
    
    private static ArrayList<CirclesPanel> filmStripShots = new ArrayList<CirclesPanel>();

    // this has been refactored from DiagramCreator, it seems to have been used for
    // showing individual frames
//    private void DEB_show_frame(int deb_level, int debug_frame_index, int size) {
//        // build a ConcreteDiagram for the current collection of circles
//        ArrayList<ConcreteZone> shadedZones = new ArrayList<ConcreteZone>();
//
//        ArrayList<CircleContour> circles_copy = new ArrayList<CircleContour>();
//        for(CircleContour c : circles) {
//            circles_copy.add(new CircleContour(c));
//        }
//        CircleContour.fitCirclesToSize(circles_copy, size);
//        ConcreteDiagram cd = new ConcreteDiagram(new icircles.geometry.Rectangle(0, 0, size, size),
//                circles_copy, shadedZones);
//        CirclesPanel cp = new CirclesPanel("toDebugString frame "+debug_frame_index, "no failure",
//                cd, size, true);
//        //DEB.addFilmStripShot(cp);
//    }

    public static void addFilmStripShot(CirclesPanel cp)
    {
    	filmStripShots.add(cp);
    }
    public static void showFilmStrip() {
    	if(filmStripShots.size()==0)
    		return;
    	
    	JFrame viewingFrame = new JFrame("frame to hold a CirclesPanel");
    	JScrollPane scrollPane = new JScrollPane();
    	JPanel filmPanel = new JPanel();
    	for(CirclesPanel cp : filmStripShots)
    	{
    		filmPanel.add(cp);
    	}
    	scrollPane.add(filmPanel);
    			
    	viewingFrame.getContentPane().setPreferredSize(new Dimension(200, 200));
    	viewingFrame.getContentPane().add(scrollPane);
    	viewingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	viewingFrame.pack();
    	viewingFrame.setVisible(true);    	
    }

    public void clearFilmStrip()
    {
    	filmStripShots.clear();
    }
    
    public static void assertCondition(boolean condition, String messageIfFail) {
        if (!condition) {
            System.out.println("!! assert failure !! " + messageIfFail);
            throw new Error();
        }
    }

    public static void out(int this_level, String message) {
        if (this_level <= level) {
            System.out.println(message);
        }
    }

    public static void show(int deb_level, Shape s, String desc) {
    	if (deb_level > DEB.level)
    		return;
    	
        JFrame jf = new JFrame(desc);
        jf.getContentPane().add(new ShapePanel(s));
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //jf.setSize(s.getBounds().width, s.getBounds().height);
        jf.setBounds(0, 0, 800, 800);
        jf.setVisible(true);
    }
}

class ShapePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    Shape m_s;

    ShapePanel(Shape s) {
        super();
        m_s = s;
        this.setBounds(s.getBounds());
        this.setMinimumSize(new Dimension(s.getBounds().width, s.getBounds().height));
    }

    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D) g).fill(m_s);
    }
}
