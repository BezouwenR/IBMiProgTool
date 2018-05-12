package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Job;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

/**
 * Select table libraries to a (reduced) resulting library list
 * and save it to the .lib file
 *
 * @author vzupka
 *
 */
public class ChangeLibraryList extends JFrame {

    protected static final long serialVersionUID = 1L;

    AS400 remoteServer;

    Properties properties;
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding", "UTF-8");

    String libraries = "";


    // Path to UserLibraryList.lib file
    Path libraryListPath = Paths.get(System.getProperty("user.dir"), "workfiles", "UserLibraryList.lib");
    // Path to CurrentLibrary.lib file
    Path currentLibraryPath = Paths.get(System.getProperty("user.dir"), "workfiles", "CurrentLibrary.lib");

    // Empty left vector for list of library names
    Vector<String> vectorLeft = new Vector<>();
    ListLeftRightTransfHdlr listLeftRightTransfHdlr = new ListLeftRightTransfHdlr();

    String userLibraryListString = ""; // Empty library list to be returned
    String currentLibrary;
    // Lists containing the vectors
    JList<String> listLeft = new JList<>(vectorLeft);
    JList<String> listRight;
    DefaultListModel<String> listRightModel = new DefaultListModel<>();
    // ListRightRightTransfHdlr listRightRightTransfHdlr = new ListRightRightTransfHdlr(); // Does not work properly.

    int selectedIndexRight;

    int scrollPaneWidth = 150;
    int scrollPaneHeight = 120;

    JScrollPane scrollPaneLeft;
    JScrollPane scrollPaneRight;

    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JPanel globalPanel = new JPanel();

    JLabel title = new JLabel("Change library list");
    JLabel prompt1 = new JLabel("Build user library list on the right.");
    JLabel currentLibraryLabel = new JLabel("Current library:");
    JLabel libraryPatternLabel = new JLabel("Library pattern:");

    JComboBox<String> curentLibraryComboBox;
    String[] librariesForCurlib;

    JTextField libraryPatternTextField;
    String libraryPattern;
    String libraryField;
    String libraryWildCard;

    JButton copyButton = new JButton("Copy ➔");
    JButton removeButton = new JButton("Remove");
    JButton clearButton = new JButton("Clear");
    JButton saveButton = new JButton("Save & Return");

    GroupLayout layout = new GroupLayout(globalPanel);

    /**
     * Constructor.
     */
    ChangeLibraryList(AS400 remoteServer, int compileWindowX, int compileWindowY) {
        this.remoteServer = remoteServer;
        // Read file Parameters.text to get property LIBRARY_PREFIX
        properties = new Properties();
        try {
            BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // Start window construction
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20));
        prompt1.setForeground(new Color(50, 60, 160)); // Dim blue
        libraryPatternLabel.setForeground(new Color(50, 60, 160)); // Dim blue
        currentLibraryLabel.setForeground(new Color(50, 60, 160)); // Dim blue

        libraryPatternTextField = new JTextField();
        libraryPatternTextField.setToolTipText("Library search pattern. Can use * and ? wild cards.");
        libraryPattern = ((String) properties.get("LIBRARY_PATTERN")).toUpperCase();
        libraryPatternTextField.setText(libraryPattern);
        libraryPatternTextField.setPreferredSize(new Dimension(100, 20));
        libraryPatternTextField.setMinimumSize(new Dimension(100, 20));
        libraryPatternTextField.setMaximumSize(new Dimension(100, 20));

        curentLibraryComboBox = new JComboBox<>();
        curentLibraryComboBox.setToolTipText("Select a library or *CRTDFT from the list of possible entries.");
        curentLibraryComboBox.setPreferredSize(new Dimension(120, 20));
        curentLibraryComboBox.setMinimumSize(new Dimension(120, 20));
        curentLibraryComboBox.setMaximumSize(new Dimension(120, 20));
        curentLibraryComboBox.setEditable(true);
        curentLibraryComboBox.setSelectedItem("*CRTDFT");

        copyButton.setToolTipText("Copy selected libraries from left to right. Also by drag and drop.");
        removeButton.setToolTipText("Remove selected libraries from the right side.");
        clearButton.setToolTipText("Remove all libraries from the right side.");
        saveButton.setToolTipText("Create user library list and current library. Return to the previous window.");

        // Retrieve library names beginning with a prefix    
        librariesForCurlib = getListOfLibraries(libraryPattern);

        // Fill combo box list with library names preceded by *CRTDFT
        curentLibraryComboBox.removeAllItems();
        // Fill the left list with *CRTDFT
        curentLibraryComboBox.addItem("*CRTDFT");
        // Add library names from the complete or selected list
        for (int idx = 1; idx < librariesForCurlib.length + 1; idx++) {
            curentLibraryComboBox.addItem(librariesForCurlib[idx - 1]);
        }

        // Fill the Right list with library names
        // and Current library combo box input field
        try {
            if (!Files.exists(libraryListPath)) {
                Files.createFile(libraryListPath);
            }
            // Fill the Right list with library names from the "UserLibraryList.lib" file.
            List<String> items = Files.readAllLines(libraryListPath);
            if (!items.isEmpty()) {
                items.get(0);
                String[] userUserLibraryList = items.get(0).split(",");
                for (int idx = 1; idx < userUserLibraryList.length; idx++) {
                    listRightModel.addElement(userUserLibraryList[idx].trim());
                }
            }
            if (!Files.exists(currentLibraryPath)) {
                Files.createFile(currentLibraryPath);
            }
            //
            // Set combo box input field from the "CurrentLibrary.lib" file.
            List<String> curlib = Files.readAllLines(currentLibraryPath);
            if (!curlib.isEmpty()) {
                // The only item is the current library name or *CRTDFT
                curentLibraryComboBox.setSelectedItem(curlib.get(0));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Create right list (user library list) using the DefaultListModel 
        listRight = new JList(listRightModel);
        listRight.setDragEnabled(true);
//        listRight.setTransferHandler(listRightRightTransfHdlr); // Does not work properly.
        listRight.setDropMode(DropMode.INSERT);
//        listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // This is unnecessary. Multiple interval selection is default.

        scrollPaneLeft = new JScrollPane(listLeft);
        scrollPaneLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPaneLeft.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setBackground(scrollPaneLeft.getBackground());
        scrollPaneLeft.setBorder(BorderFactory.createEmptyBorder());

        scrollPaneRight = new JScrollPane(listRight);
        scrollPaneRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPaneRight.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setBackground(scrollPaneLeft.getBackground());
        scrollPaneRight.setBorder(BorderFactory.createEmptyBorder());

        // Group layout of components
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(title)
                        .addComponent(prompt1)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(libraryPatternLabel)
                                .addComponent(libraryPatternTextField)
                                .addComponent(currentLibraryLabel)
                                .addComponent(curentLibraryComboBox)
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(scrollPaneLeft)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(copyButton)
                                        .addComponent(removeButton)
                                        .addComponent(clearButton)
                                        .addComponent(saveButton)
                                )
                                .addComponent(scrollPaneRight)
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(title)
                        .addComponent(prompt1)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(libraryPatternLabel)
                                .addComponent(libraryPatternTextField)
                                .addComponent(currentLibraryLabel)
                                .addComponent(curentLibraryComboBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(scrollPaneLeft)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(copyButton)
                                        .addComponent(removeButton)
                                        .addComponent(clearButton)
                                        .addComponent(saveButton))
                                .addComponent(curentLibraryComboBox)
                                .addComponent(scrollPaneRight)
                        )
        );

        // Library pattern text field listener
        // -----------------------------------
        libraryPatternTextField.addActionListener(ae -> {
            // Translate text to upper case letters (object names are always in upper case)
            libraryPattern = libraryPatternTextField.getText().toUpperCase();
            libraryPatternTextField.setText(libraryPattern);
            // Get list of all libraries whose names start with this string
            // getListOfLibraries(libraryPatternString);

            // Retrieve library names conforming to the pattern
            // for  both left list and Current library combo box  
            librariesForCurlib = getListOfLibraries(libraryPattern);

            // Fill combo box list with library names preceded by *CRTDFT
            curentLibraryComboBox.removeAllItems();
            // Fill the left list with *CRTDFT
            curentLibraryComboBox.addItem("*CRTDFT");
            // Add library names from the complete or selected list
            for (int idx = 1; idx < librariesForCurlib.length + 1; idx++) {
                curentLibraryComboBox.addItem(librariesForCurlib[idx - 1]);
            }
        });

        // Left list listener
        // ------------------
        listLeft.addListSelectionListener(lse -> {
            listRight.setTransferHandler(listLeftRightTransfHdlr);
        });

        // Right list listener
        // ------------------
        listRight.addListSelectionListener(lse -> {
            //listRight.setTransferHandler(listRightRightTransfHdlr); // Does not work properly
        });

        // Copy button listener
        // --------------------
        copyButton.addActionListener(ae -> {
            copyLeftToRight(null);
        });

        // Clear button activity
        // ---------------------
        clearButton.addActionListener(a -> {
            listRightModel.removeAllElements();
        });

        // Save & Return button listener
        // -----------------------------
        saveButton.addActionListener(a -> {
            // 
            for (int idx = 0; idx < listRightModel.size(); idx++) {
                libraries += listRightModel.get(idx) + " ";
            }

            CommandCall cmdCall = new CommandCall(remoteServer);
            String commandText = "CHGLIBL LIBL(" + libraries + ") CURLIB("
                    + (String) curentLibraryComboBox.getSelectedItem() + ")";

            try {
                cmdCall.run(commandText);
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }

            // Build library list as a string (comma separated library names)
            userLibraryListString = "";
            for (int idx = 0; idx < listRightModel.size(); idx++) {
                userLibraryListString += ", ";
                userLibraryListString += listRightModel.get(idx);
            }
            currentLibrary = (String) curentLibraryComboBox.getSelectedItem();
            // Write User library list ane Current library to corresponding .lib files
            try {
                // User library list
                ArrayList<String> userUserLibraryListArr = new ArrayList<>();
                userUserLibraryListArr.add(userLibraryListString);
                // Rewrite the existing file or create and write a new file.
                Files.write(libraryListPath, userUserLibraryListArr);
                // Current library
                ArrayList<String> currentLibraryArr = new ArrayList<>();
                currentLibraryArr.add((String) curentLibraryComboBox.getSelectedItem());
                // Rewrite the existing file or create and write a new file.
                Files.write(currentLibraryPath, currentLibraryArr);
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
            dispose();
        });

        // Remove button listener
        // ----------------------
        removeButton.addActionListener(a -> {
            List<String> itemsRight = listRight.getSelectedValuesList();
            if (!itemsRight.isEmpty() && !listRightModel.isEmpty()) {
                int firstIndex = listRightModel.indexOf(itemsRight.get(0));
                int lastIndex = listRightModel.indexOf(itemsRight.get(itemsRight.size() - 1));
                for (int idx = lastIndex; idx >= firstIndex; idx--) {
                    listRightModel.remove(idx);
                    lastIndex = listRightModel.lastIndexOf(itemsRight.get(itemsRight.size() - 1));
                }
            }
        });

        // Complete window construction
        globalPanel.setLayout(layout);
        globalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Container cont = getContentPane();
        cont.add(globalPanel);

        // Make window visible 
        setSize(500, 350);
        setLocation(compileWindowX + 440, compileWindowY + 40);
        setVisible(true);
        // Set default behavior on closing the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    /**
     *
     * @return
     */
    protected String getUserLibraryList() {
        return userLibraryListString;
    }

    /**
     *
     * @return
     */
    protected String getCurrentLibrary() {
        return currentLibrary;
    }

    /**
     *
     * @return
     */
    protected String[] getUserLibraryList2() {

        CommandCall cmdCall = new CommandCall(remoteServer);
        Job currentJob = new Job();
        try {
            currentJob = cmdCall.getServerJob();
            currentJob.getUser();
            currentJob.getName();
            currentJob.getNumber();

            String[] libList = currentJob.getUserLibraryList();
            return libList;

        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * Get list of all libraries whose names conform to the pattern defined in the input field
     *
     * @param libraryPattern
     * @return
     */
    protected String[] getListOfLibraries(String libraryPattern) {

        libraryField = libraryPattern;
        if (libraryField.isEmpty()) {
            libraryPattern = "*";
        }
        libraryWildCard = libraryPattern.replace("*", ".*");
        libraryWildCard = libraryWildCard.replace("?", ".");

        IFSFile ifsFile = new IFSFile(remoteServer, "/QSYS.LIB");
        if (ifsFile.getName().equals("QSYS.LIB")) {
            try {
                // Get list of subfiles/subdirectories
                IFSFile[] ifsFiles2 = ifsFile.listFiles();
                vectorLeft.removeAllElements();
                for (IFSFile ifsFileLevel2 : ifsFiles2) {
                    if (ifsFileLevel2.toString().endsWith(".LIB")) {
                        String bareLibraryName = ifsFileLevel2.getName().substring(0, ifsFileLevel2.getName().indexOf("."));
                        if (bareLibraryName.matches(libraryWildCard)) {
                            vectorLeft.addElement(bareLibraryName);
                        }
                    }
                }
                // Fill the left list with library names
                listLeft.setListData(vectorLeft);
                // Set the left list as enabled for dragging from.
                listLeft.setDragEnabled(true);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        String[] strArr = new String[vectorLeft.size()];
        strArr = vectorLeft.toArray(strArr);
        return strArr;
    }

    /**
     * @param index
     */
    protected void copyLeftToRight(Integer index) {
        // Copy selected items from the left box to the right box
        List<String> itemsLeft = listLeft.getSelectedValuesList();
        if (!listRightModel.isEmpty()) {
            // Add left list after non-empty right list 
            int lastRightIndex = listRightModel.size() - 1;
            for (int idx = itemsLeft.size() - 1; idx >= 0; idx--) {
                boolean foundInRight = false;
                // Find out if the left item matches any right item
                for (int jdx = 0; jdx < lastRightIndex + 1; jdx++) {
                    if (itemsLeft.get(idx).equals(listRightModel.get(jdx))) {
                        foundInRight = true;
                    }
                }
                // If the left item does not match any item in the right box vector
                // add the item at the end of the vector items in the right box.
                if (!foundInRight) {
                    if (index == null) {
                        listRightModel.addElement(itemsLeft.get(idx));
                    } else {
                        listRightModel.insertElementAt(itemsLeft.get(idx), index);
                    }
                }
            }
        } else { // 
            // Add (put) left list in the empty right list
            for (int idx = 0; idx < itemsLeft.size(); idx++) {
                listRightModel.add(idx, itemsLeft.get(idx));
            }
        }
        // Clear selection in the left list
        listLeft.clearSelection();
        repaint();
    }

    /**
     * @param insertIndex
     */
    protected void copyRightToRight(Integer insertIndex) {
        // Copy selected items from the right list to the (same) right list
        int minIndex = listRight.getMinSelectionIndex();
        int maxIndex = listRight.getMaxSelectionIndex();
        String itemRightSelected;

        if (!listRightModel.isEmpty()) {
            for (int idx = minIndex; idx <= maxIndex; idx++) {
                itemRightSelected = (String) listRightModel.getElementAt(idx);
                // Insert selected items on the insert index in the right list 
                if (insertIndex == null) {
                    listRightModel.addElement(itemRightSelected);
                } else {
                    //listRightModel.removeElementAt(idx);

                    listRightModel.add(idx, itemRightSelected);
                }
            }
            
        minIndex = listRight.getMinSelectionIndex();
        maxIndex = listRight.getMaxSelectionIndex();
            
System.out.println("minIndex   : "+minIndex);
System.out.println("maxIndex   : "+maxIndex);
System.out.println("insertIndex: "+insertIndex);
            //if (insertIndex > maxIndex) {
                //minIndex++;
                for (int idx = maxIndex; idx >= minIndex; idx--) {
                //for (int idx = minIndex; idx <= maxIndex; idx++) {
System.out.println("idx : "+idx);
                    listRightModel.remove(idx);
                }

            //}

        }
    }

    /**
     *
     */
    class ListLeftRightTransfHdlr extends TransferHandler {

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            // Check for String flavor
            if (!info.isDrop()) {
                return false;
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection("");
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            int index = dl.getIndex();
            boolean insert = dl.isInsert();
            // Perform the actual import.  
            if (insert) {
                copyLeftToRight(index);
            }
            return true;

        }
    }

    /**
     * Drag and drop does not work properly in the same list!
     */
    /*
    class ListRightRightTransfHdlr extends TransferHandler {

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            // Check for drop
            if (!info.isDrop()) {
                return false;
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection("");
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            int index = dl.getIndex();
            boolean insert = dl.isInsert();
            // Perform the actual import.  
            if (insert) {
                copyRightToRight(index);
            }
            return true;
        }
    }
    */

}
