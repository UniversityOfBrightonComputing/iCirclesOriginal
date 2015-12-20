package icircles.guiswing;

import icircles.gui.CirclesGUI;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class SwingCirclesGUI implements CirclesGUI {
    @Override
    public void launchGUI(String[] args) {
        new SwingApplication();
    }
}
