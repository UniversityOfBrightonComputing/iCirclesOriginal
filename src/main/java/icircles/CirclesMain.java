package icircles;

import icircles.guifx.CirclesApp;
import icircles.guiswing.CirclesFrame;
import javafx.application.Application;
import org.apache.logging.log4j.core.config.Configurator;

public class CirclesMain {

    public static void main(String args[]) {
        Configurator.initialize("default", CirclesMain.class.getResource("/icircles/log4j2.xml").toExternalForm());

        // TODO: command line parsing, no gui, gui swing, gui fx
        boolean guiFX = true;
        if (guiFX) {
            Application.launch(CirclesApp.class, args);
        } else {
            new CirclesFrame();
        }
    }
}
