package copyfiles;

import com.ibm.as400.access.AS400;
import java.awt.Cursor;
import javax.swing.SwingWorker;

/**
 Create a window for reading and displaying spooled files from output queues (*OUTQ).

 @author Vladimír Župka, 2016
 */
public class WrkSplFCall extends SwingWorker<String, String> {

    AS400 remoteServer;
    MainWindow mainWindow;
    String rightPathString;
    boolean currentUser;
    int compileWindowX;
    int compileWindowY;
    String className;
    
    /**
     Constructor.

     @param remoteServer
     @param mainWindow
     @param rightPathString
     @param currentUser
     */
    WrkSplFCall(AS400 remoteServer, MainWindow mainWindow, String rightPathString,
            boolean currentUser, int compileWindowX, int compileWindowY, String className) {
        this.remoteServer = remoteServer;
        this.mainWindow = mainWindow;
        this.rightPathString = rightPathString;
        this.currentUser = currentUser;
        this.compileWindowX = compileWindowX;
        this.compileWindowY = compileWindowY;
        this.className = className;
    }

    /**
     Perform method createSpoolWindow(), it runs as a SwingWorker background task.

     @return
     */
    @Override
    public String doInBackground() {
        createSpoolWindow(rightPathString);
        return "";
    }

    /**
     Concludes the SwingWorker background task; it is not needed here.
     */
    @Override
    public void done() {
    }

    /**
     Create window to work with spooled files in a table.

     @param rightPathString
     */
    protected void createSpoolWindow(String rightPathString) {
        // Change cursor to wait cursor
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        WrkSplF csw = new WrkSplF(remoteServer, mainWindow, rightPathString, currentUser, compileWindowX, compileWindowY, className);
        csw.createSpoolWindow(currentUser);
        // Change cursor to default
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Remove message scroll listener (cancel scrolling to the last message)
        mainWindow.scrollMessagePane.getVerticalScrollBar().
                removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
    }
}
