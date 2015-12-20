package icircles;

import icircles.gui.CirclesGUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class CirclesMain {

    public static void main(String args[]) {
        Configurator.initialize("default", CirclesMain.class.getResource("/icircles/log4j2.xml").toExternalForm());

        Logger log = LogManager.getLogger(CirclesMain.class);

        try {
            if (args.length == 0) {
                log.info("Launching FX GUI");
                CirclesGUI gui = (CirclesGUI) Class.forName("icircles.guifx.FXCirclesGUI").newInstance();
                gui.launchGUI(args);
            }
        } catch (Exception e) {
            log.error("Failed to launch FX GUI: " + e.getMessage());

            try {
                log.info("Launching Swing GUI");
                CirclesGUI gui = (CirclesGUI) Class.forName("icircles.guiswing.SwingCirclesGUI").newInstance();
                gui.launchGUI(args);
            } catch (Exception ex) {
                log.error("Failed to launch Swing GUI: " + ex.getMessage());
            }
        }

    }
}
