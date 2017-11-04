/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package copyfiles;

import com.ibm.as400.access.IFSFile;
import java.awt.Cursor;
import java.nio.charset.Charset;
import java.nio.file.Files;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author vzupka
 */
public class AddAS400Nodes extends SwingWorker<String, String> {

    IFSFile ifsFileParam;
    DefaultMutableTreeNode nodeParam;
    MainWindow mainWindow;

    IFSFile[] ifsFiles;
    IFSFile ifsFile;

    String libraryName;
    String fileName;
    String memberName;

    String libraryField;
    String fileField;
    String memberField;

    IFSFile[] libraries;
    IFSFile[] files;
    IFSFile[] members;

    String libraryPattern;
    String filePattern;
    String memberPattern;
    String libraryWildCard;
    String fileWildCard;
    String memberWildCard;

    AddAS400Nodes(IFSFile ifsFileParam, DefaultMutableTreeNode nodeParam, MainWindow mainWindow) {
        this.ifsFileParam = ifsFileParam;
        this.nodeParam = nodeParam;
        this.mainWindow = mainWindow;
    }

    /**
     * Perform method parallelAddAS400Nodes(), it runs as a SwingWorker background task.
     */
    @Override
    public String doInBackground() {
        parallelAddAS400Nodes();
        return "";
    }

    /**
     * Concludes the SwingWorker background task; it is not needed here.
     */
    @Override
    public void done() {
    }

    protected void parallelAddAS400Nodes() {

        libraryPattern = mainWindow.libraryPatternTextField.getText();
        filePattern = mainWindow.filePatternTextField.getText();
        memberPattern = mainWindow.memberPatternTextField.getText();

        libraryField = libraryPattern;
        fileField = filePattern;
        memberField = memberPattern;

        if (libraryField.isEmpty()) {
            libraryPattern = "*";
        }
        if (fileField.isEmpty()) {
            filePattern = "*";
        }
        if (memberField.isEmpty()) {
            memberPattern = "*";
        }

        libraryWildCard = libraryPattern.replace("*", ".*");
        libraryWildCard = libraryWildCard.replace("?", ".");
        fileWildCard = filePattern.replace("*", ".*");
        fileWildCard = fileWildCard.replace("?", ".");
        memberWildCard = memberPattern.replace("*", ".*");
        memberWildCard = memberWildCard.replace("?", ".");

        DefaultMutableTreeNode nodeLevel2 = null;
        DefaultMutableTreeNode nodeLevel3 = null;
        DefaultMutableTreeNode nodeLevel4 = null;
        DefaultMutableTreeNode nodeLevel5 = null;

        // Set wait-cursor (rotating wheel?)
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWindow.scrollMessagePane.getVerticalScrollBar().addAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);

        // First, remove all children in order to create all new child nodes
        nodeParam.removeAllChildren();

        try {
            // IFSFile list of directories/files
            ifsFiles = ifsFileParam.listFiles();

            // For each directory add a new node - IFS directory or file
            for (IFSFile ifsFile : ifsFiles) {
                try {
                    // Get parent file
                    IFSFile parent = ifsFile.getParentFile();

                    // Non-library system - all IFS objects
                    // ------------------
                    // IFS path does not start with /QSYS.LIB and it is not QSYS.LIB (without a leading slash)
                    if (!ifsFile.toString().toUpperCase().startsWith("/QSYS.LIB")
                            && !ifsFile.toString().toUpperCase().equals("QSYS.LIB")) {

                        // Level 1
                        nodeLevel2 = new DefaultMutableTreeNode(ifsFile.getName());
                        nodeParam.add(nodeLevel2);

                        // Level 2
                        IFSFile[] ifsFiles2;
                        nodeLevel2.removeAllChildren();
                        if (ifsFile.isDirectory()) {
                            try {
                                // Get list of sub-files/sub-directories.
                                // Some objects may be secured (e. g /QDLS directory).
                                ifsFiles2 = ifsFile.listFiles();
                                for (IFSFile ifsFileLevel2 : ifsFiles2) {
                                    nodeLevel3 = new DefaultMutableTreeNode(ifsFileLevel2.getName());
                                    nodeLevel2.add(nodeLevel3);

                                    //// No other levels are necessary
                                    //// -----------------------------
                                    if (false) {
                                        // Level 3
                                        IFSFile[] ifsFiles3;
                                        nodeLevel3.removeAllChildren();
                                        // Get list of sub-files/sub-directories.
                                        if (ifsFileLevel2.isDirectory()) {
                                            ifsFiles3 = ifsFileLevel2.listFiles();
                                            for (IFSFile ifsFileLevel3 : ifsFiles3) {
                                                nodeLevel4 = new DefaultMutableTreeNode(ifsFileLevel3.getName());
                                                nodeLevel3.add(nodeLevel4);

                                                // Level 4
                                                IFSFile[] ifsFiles4;
                                                nodeLevel4.removeAllChildren();

                                                if (ifsFileLevel3.isDirectory()) {
                                                    ifsFiles4 = ifsFileLevel3.listFiles();
                                                    for (IFSFile ifsFileLevel4 : ifsFiles4) {
                                                        nodeLevel5 = new DefaultMutableTreeNode(ifsFileLevel4.getName());
                                                        nodeLevel4.add(nodeLevel5);
                                                    }
                                                }
                                            }
                                        }
                                        //// ----------------
                                        //// Other levels end
                                    }
                                }
                            } catch (Exception exc) {
                                System.out.println(exc.getLocalizedMessage());
                                exc.printStackTrace();
                                mainWindow.row = "Info: Object  " + ifsFile.toString() + "  -  " + exc.toString();
                                mainWindow.msgVector.add(mainWindow.row);
                                mainWindow.showMessages(mainWindow.noNodes);
                                continue;
                            }
                        }
                    } // End of IFS objects

                    // System library QSYS
                    // -------------------
                    // System library is processed individually in order to select libraries
                    // starting with a prefix and add them as secondary nodes
                    if (ifsFile.toString().toUpperCase().equals("/QSYS.LIB")) {
                        // Add QSYS library
                        nodeLevel2 = new DefaultMutableTreeNode(ifsFile.getName());
                        nodeParam.add(nodeLevel2);

                        nodeLevel2.removeAllChildren();

                        // Get list of libraries - Level 2
                        // Select all libraries conforming to the library pattern
                        IFSFile[] ifsFiles2 = ifsFile.listFiles(libraryPattern + ".LIB");
                        for (IFSFile ifsFileLevel2 : ifsFiles2) {
                            nodeLevel3 = new DefaultMutableTreeNode(ifsFileLevel2.getName());
                            nodeLevel2.add(nodeLevel3);
                        }
                    } //
                    //
                    // Libraries (other than QSYS)
                    // ---------------------------
                    // Secondary nodes of Source Physical Files (level3) are added.
                    else if (ifsFile.toString().toUpperCase().startsWith("/QSYS.LIB/")
                            && ifsFile.toString().toUpperCase().endsWith(".LIB")) {
                        libraryName = ifsFile.getName();
                        String bareLibraryName = libraryName.substring(0, ifsFile.getName().lastIndexOf("."));
                        // Select libraries on file pattern and member pattern
                        if (bareLibraryName.matches(libraryWildCard)) {
                            // Select Source Physical Files depending on member pattern
                            files = ifsFile.listFiles(filePattern + ".FILE");
                            // Add empty library
                            if (files.length == 0) {
                                mainWindow.row = "Info: Library 0  " + libraryName + "  added.";
                                mainWindow.msgVector.add(mainWindow.row);
                                mainWindow.showMessages(mainWindow.noNodes);
                                nodeLevel2 = new DefaultMutableTreeNode(libraryName);
                                nodeParam.add(nodeLevel2);
                            }
                            boolean libraryAdded = false; // Note Library not yet added
                            labelFile:
                            for (IFSFile file : files) {
                                fileName = file.getName();
                                // Member pattern is all inclusive
                                if (memberPattern.equals("*")) {
                                    // If library not yet added - add it
                                    if (!libraryAdded) {
                                        mainWindow.row = "Info: Library 1  " + libraryName + "  added.";
                                        mainWindow.msgVector.add(mainWindow.row);
                                        mainWindow.showMessages(mainWindow.noNodes);
                                        nodeLevel2 = new DefaultMutableTreeNode(libraryName);
                                        nodeParam.add(nodeLevel2);
                                        nodeLevel2.removeAllChildren();
                                        libraryAdded = true; // Note library added for next loop
                                    }
                                    // Add the file
                                    //if (file.isSourcePhysicalFile()) {
                                    mainWindow.row = "Info: File 1  " + file.toString() + "  added.";
                                    mainWindow.msgVector.add(mainWindow.row);
                                    mainWindow.showMessages(mainWindow.noNodes);
                                    nodeLevel3 = new DefaultMutableTreeNode(fileName);
                                    nodeLevel2.add(nodeLevel3);
                                    //}
                                } else {
                                    // Member pattern is specific 
                                    if (file.isSourcePhysicalFile()) {
                                        members = file.listFiles(memberPattern + ".MBR");
                                        for (IFSFile member : members) {
                                            String bareMemberName = member.getName().substring(0, member.getName().lastIndexOf("."));
                                            if (bareMemberName.matches(memberWildCard)) {
                                                if (!libraryAdded) {
                                                    // If the library was not added - add it
                                                    mainWindow.row = "Info: Library 2  " + libraryName + "  added.";
                                                    mainWindow.msgVector.add(mainWindow.row);
                                                    mainWindow.showMessages(mainWindow.noNodes);
                                                    nodeLevel2 = new DefaultMutableTreeNode(libraryName);
                                                    nodeParam.add(nodeLevel2);
                                                    nodeLevel2.removeAllChildren();
                                                    libraryAdded = true; // Note library added for next loop
                                                }
                                                // Add the Source Physical File
                                                //if (file.isSourcePhysicalFile()) {
                                                mainWindow.row = "Info: File 2  " + file.toString() + "  added.";
                                                mainWindow.msgVector.add(mainWindow.row);
                                                mainWindow.showMessages(mainWindow.noNodes);
                                                nodeLevel3 = new DefaultMutableTreeNode(fileName);
                                                nodeLevel2.add(nodeLevel3);
                                                continue labelFile; // Get next file to check
                                                //}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Source Physical Files
                    // ---------------------
                    if (ifsFile.toString().contains(".LIB") && ifsFile.toString().contains(".FILE")
                            && ifsFile.isSourcePhysicalFile()) {
                        fileName = ifsFile.getName();
                        String bareFileName = fileName.substring(0, ifsFile.getName().lastIndexOf("."));
                        if (bareFileName.matches(fileWildCard)) {
                            // Add the Source Physical File
                            nodeLevel2 = new DefaultMutableTreeNode(fileName);
                            nodeParam.add(nodeLevel2);

                            nodeLevel2.removeAllChildren();
                            // Select members
                            IFSFile[] members = ifsFile.listFiles(memberPattern + ".MBR");
                            for (IFSFile member : members) {
                                // Select the member
                                nodeLevel3 = new DefaultMutableTreeNode(member.getName());
                                nodeLevel2.add(nodeLevel3);
                            }
                        }
                    }

                    // Source Members (their parents are Source Physical Files)
                    // --------------
                    if (ifsFile.toString().contains(".LIB") && ifsFile.toString().contains(".FILE")
                            && parent.isSourcePhysicalFile() && ifsFile.toString().endsWith(".MBR")) {
                        String bareMemberName = ifsFile.getName().substring(0, ifsFile.getName().lastIndexOf("."));
                        if (bareMemberName.matches(memberWildCard)) {
                            // Add the member
                            nodeLevel2 = new DefaultMutableTreeNode(ifsFile.getName());
                            nodeParam.add(nodeLevel2);
                        }
                    }

                    // Save Files - files with subtype SAVF
                    // ----------
                    if (ifsFile.toString().endsWith(".FILE") && ifsFile.getSubtype().equals("SAVF")) {
                        fileName = ifsFile.getName();
                        String bareFileName = fileName.substring(0, fileName.lastIndexOf("."));
                        // Save file has suffix .SAVF in the visible tree structure:
                        String saveFileName = bareFileName + ".SAVF";
                        mainWindow.row = "Info: Save file  " + saveFileName + " added.";
                        mainWindow.msgVector.add(mainWindow.row);
                        mainWindow.showMessages(mainWindow.noNodes);
                        nodeLevel2 = new DefaultMutableTreeNode(saveFileName);
                        nodeParam.add(nodeLevel2);
                    }

                    // Output queues
                    // -------------
                    if (ifsFile.toString().contains(".LIB") && ifsFile.toString().contains(".OUTQ")) {
                        nodeLevel2 = new DefaultMutableTreeNode(ifsFile.getName());
                        nodeParam.add(nodeLevel2);
                    }

                } catch (Exception exc) {
                    exc.printStackTrace();
                    mainWindow.row = "Info: Object  " + ifsFile.toString() + "  -  " + exc.toString();
                    mainWindow.msgVector.add(mainWindow.row);
                    mainWindow.showMessages(mainWindow.noNodes);
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            mainWindow.row = "Info: Object  " + ifsFile.toString() + "  -  " + exc.toString();
            mainWindow.msgVector.add(mainWindow.row);
            mainWindow.showMessages(mainWindow.noNodes);
        }

        // Change cursor to default
        //mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Remove setting last element of messages
        //mainWindow.scrollMessagePane.getVerticalScrollBar()
        //        .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);

        expandNode();
        refreshWindow();
    }

    /**
     *
     */
    protected void expandNode() {

        // Remove setting last element of messages
        mainWindow.scrollMessagePane.getVerticalScrollBar()
                .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);

        // Disable the tree expansion listener before expanding the tree.
        // -----------------------------------
        mainWindow.rightTree.removeTreeExpansionListener(mainWindow.rightTreeExpansionListener);

        mainWindow.rightTree.expandRow(0);

        // Activate the tree expansion listener to enable actions after expansion of the tree.
        // ------------------------------------
        mainWindow.rightTree.addTreeExpansionListener(mainWindow.rightTreeExpansionListener);

    }

    protected void refreshWindow() {

        // Change cursor to default
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Place the right tree in scroll pane
        mainWindow.scrollPaneRight.setViewportView(mainWindow.rightTree);
        mainWindow.splitPaneInner.setRightComponent(mainWindow.panelRight);

        // Set actual user name to the text field
        mainWindow.userNameTextField.setText(mainWindow.remoteServer.getUserId());
        mainWindow.properties.setProperty("USERNAME", mainWindow.remoteServer.getUserId());

        // Create the updated text file in directory "paramfiles"
        try {
            mainWindow.outfile = Files.newBufferedWriter(mainWindow.parPath, Charset.forName(mainWindow.encoding));
            mainWindow.properties.store(mainWindow.outfile, mainWindow.PROP_COMMENT);
            mainWindow.outfile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // Note that the structure of the node (children) changed
        mainWindow.rightTreeModel.nodeStructureChanged(mainWindow.rightNode);
        // Reload that node only (the other nodes remain unchanged)
        mainWindow.rightTreeModel.reload(mainWindow.rightNode);

        // Change cursor to default
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Remove setting last element of messages
        mainWindow.scrollMessagePane.getVerticalScrollBar()
                .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);

    }
}
