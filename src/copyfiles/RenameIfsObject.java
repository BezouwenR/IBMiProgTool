package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;

/**

 @author Vladimír Župka, 2017
 */
public class RenameIfsObject {

    AS400 remoteServer;
    MainWindow mainWindow;
    String row;

    int currentX;
    int currentY;

    /**
     Constructor.

     @param mainWindow
     @param pcFileSep
     */
    RenameIfsObject(AS400 remoteServer, MainWindow mainWindow, int currentX, int currentY) {
        this.remoteServer = remoteServer;
        this.mainWindow = mainWindow;
        this.currentX = currentX;
        this.currentY = currentY;
    }

    /**
     Rename IFS file.

     @param oldPathString
     */
    protected void renameIfsObject(String oldPathString) {
        GetTextFromDialog getText = new GetTextFromDialog("");
        String oldFilePrefix = oldPathString.substring(0, oldPathString.lastIndexOf("/"));
        String oldFileName = oldPathString.substring(oldPathString.lastIndexOf("/") + 1);
        
        // "false" stands for not changing result to upper case
        String newFileName = getText.getTextFromDialog("Parent directory",
                "File name:", oldFilePrefix + "/", oldFileName, false, currentX, currentY);

        if (newFileName == null) {
           return;
        }
        if (newFileName.isEmpty()) {
           newFileName = oldFileName;
        }

        if (oldPathString.startsWith("/QSYS.LIB")) {
            // Object name in library cannot be longer than 10 characters
            String nameWithoutSuffix = newFileName.substring(0, newFileName.lastIndexOf("."));
            String suffix = newFileName.substring(newFileName.lastIndexOf("."));
            if (nameWithoutSuffix.length() > 10) {
                nameWithoutSuffix = nameWithoutSuffix.substring(0, 10);
            }
            newFileName = nameWithoutSuffix + suffix;
            newFileName = newFileName.toUpperCase();
        }

        String renamedPathString = oldFilePrefix + "/" + newFileName;
        try {
            // Create IFSFile objects
            // Old IFS file
            IFSFile oldIfsFile = new IFSFile(remoteServer, oldPathString);
            // New IFS file
            IFSFile newIfsFile = new IFSFile(remoteServer, renamedPathString);
            // Rename the old to new IFS file
            boolean renamed = oldIfsFile.renameTo(newIfsFile);
            // If not renamed for any error, send message
            if (!renamed) {
                row = "Error: Renaming IFS file  " + oldPathString + "  to  " + renamedPathString + "  failed.";
                mainWindow.msgVector.add(row);
                // "false" stands for "no update of tree node"
                mainWindow.reloadRightSideAndShowMessages();
                return;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            row = "Error: Renaming IFS file  -  " + exc.toString();
            mainWindow.msgVector.add(row);
                // "false" stands for "no update of tree node"
                mainWindow.reloadRightSideAndShowMessages();
        }

        // Change left node (source node selected by mouse click)
        mainWindow.rightNode.setUserObject(newFileName);
        mainWindow.leftTreeModel.nodeChanged(mainWindow.rightNode);
        // Send completion message
        row = "Comp: IFS file  " + oldPathString + "  was renamed to  " + renamedPathString + ".";
        mainWindow.msgVector.add(row);
                mainWindow.reloadRightSideAndShowMessages();
    }
}
