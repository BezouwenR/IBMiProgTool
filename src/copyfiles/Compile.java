package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.SpooledFile;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * Compile source files or IFS files
 *
 * @author Vladimír Župka 2017
 */
public class Compile extends JFrame {

    MainWindow mainWindow;

    int windowWidth = 850;
    int windowHeight = 500;

    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);
    final Color DIM_PINK = new Color(170, 58, 128);

    // Path to UserLibraryList.lib file
    Path libraryListPath = Paths.get(System.getProperty("user.dir"), "workfiles","UserLibraryList.lib");
    // Path to CurrentLibrary.lib file
    Path currentLibraryPath = Paths.get(System.getProperty("user.dir"), "workfiles", "CurrentLibrary.lib");
    // Path to SourceFileAttributes.lib file
    Path compileCommandsPath = Paths.get(System.getProperty("user.dir"), "workfiles", "SourceFileAttributes.lib");
    // Path to SrcAttrib.png file
    Path saveSrcAttribIconPathDark = Paths.get(System.getProperty("user.dir"), "icons", "saveSrcAttrib.png");
    // Path to noSrcAttrib.png file
    Path noSrcAttribIconPathDim = Paths.get(System.getProperty("user.dir"), "icons", "noSrcAttrib.png");
    // Path to clear SrcAttrib.png file
    Path clearSrcAttribIconPath = Paths.get(System.getProperty("user.dir"), "icons", "clearSrcAttrib.png");

    Container cont;
    JPanel globalPanel;
    JPanel titlePanel;
    JPanel commandSelectionPanel;
    JPanel parameterPanel;
    JPanel commandPanel;
    JPanel buttonPanel;

    JList<String> messageList;
    JScrollPane scrollMessagePane = new JScrollPane(messageList);

    Vector<String> libraryNameVector = new Vector<>();

    Vector<String> msgVector = new Vector<>();
    String msgText;
    String row;
    MessageScrollPaneAdjustmentListenerMax messageScrollPaneAdjustmentListenerMax;

    GroupLayout globalPanelLayout;
    GroupLayout cmdSelLayout;
    GroupLayout paramLayout;

    JLabel pathLabel = new JLabel();

    WrkSplFCall wwsp;

    AS400 remoteServer;
    String ibmCcsid;
    int ibmCcsidInt;

    String host;
    String qsyslib;
    String libraryName;
    String fileName;
    String memberName;

    JLabel sourceTypeLabel;
    ArrayList<String> sourceTypes;
    String sourceType;
    JComboBox sourceTypeComboBox;

    JLabel compileCommandLabel;
    ArrayList<String> compileCommands;
    TreeMap<String, String> compileCommandsMap = new TreeMap<>();
    String compileCommandName;
    String commandObjectType;

    JComboBox compileCommandsComboBox;

    JLabel commandLabel = new JLabel("Compile command:");

    JLabel commandTextLabel = new JLabel();

    String sourceAttributes; // Custom input compile parameters
    // Icon Aa will be dimmed or dark when clicked
    ImageIcon saveSrcAttribIconDark = new ImageIcon(saveSrcAttribIconPathDark.toString());
    ImageIcon noSrcAttribIconDim = new ImageIcon(noSrcAttribIconPathDim.toString());
    ImageIcon clearSrcAttribIcon = new ImageIcon(clearSrcAttribIconPath.toString());
    JToggleButton sourceAttributesButton = new JToggleButton();
    JButton clearSrcAttribButton = new JButton();
    JButton changeLibraryListButton;
    ChangeLibraryList chgLibList; // Object of this class is the window Change user library list.

    TreeMap<String, String> sourceFilesAndTypes = new TreeMap<>();
    TreeMap<String, ArrayList<String>> sourceTypesAndCommands = new TreeMap<>();

    String[] commandNameArray;
    String commandText;
    ActionListener commandsComboBoxListener;
    ActionListener sourceTypeComboBoxListener;

    JLabel libraryPatternLabel = new JLabel("Library pattern:");
    JTextField libraryPatternTextField;
    String libraryPattern;
    String libraryField;
    String libraryWildCard;

    String libraries = "";

    String userLibraryListString;
    String currentLibrary;

    ArrayList<String> librariesArrayList;
    JComboBox librariesComboBox;
    JLabel librariesLabel = new JLabel("Compiled object       Library:");
    String libNamePar;

    JLabel objectNameLabel = new JLabel("Object:");
    JTextField objectNameFld;
    String objNamePar;

    JButton cancelButton = new JButton("Cancel");
    JButton performButton = new JButton("Perform command");
    JButton lastSplfButton = new JButton("Last spooled file");
    JButton spooledFileButton = new JButton("Spooled files");
    JButton editButton = new JButton("Edit");
    JButton jobLogButton = new JButton("Job log");
    JButton clearButton = new JButton("Clear messages");

    String compileNotSupported;

    Properties properties;
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    final String PROP_COMMENT = "Copy files between IBM i and PC, edit and compile.";
    String encoding = System.getProperty("file.encoding", "UTF-8");
    String userName;
    String sourceTypePar;

    int compileWindowX;
    int compileWindowY;

    String compileWindowXString;
    String compileWindowYString;

    String pathString;
    boolean ifs;

    /**
     * Constructor
     *
     * @param remoteServer
     * @param mainWindow
     * @param pathString
     * @param ifs
     */
    public Compile(AS400 remoteServer, MainWindow mainWindow, String pathString, boolean ifs) {
        this.mainWindow = mainWindow;
        this.remoteServer = remoteServer;
        this.pathString = pathString;
        this.ifs = ifs;

        try {
            if (!Files.exists(compileCommandsPath)) {
                Files.createFile(compileCommandsPath);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        globalPanel = new JPanel();
        titlePanel = new JPanel();
        commandSelectionPanel = new JPanel();
        parameterPanel = new JPanel();
        commandPanel = new JPanel();
        buttonPanel = new JPanel();

        cmdSelLayout = new GroupLayout(commandSelectionPanel);
        paramLayout = new GroupLayout(parameterPanel);

        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
        titlePanel.setPreferredSize(new Dimension(windowWidth, 70));
        titlePanel.setMinimumSize(new Dimension(windowWidth, 70));

        commandSelectionPanel.setLayout(new BoxLayout(commandSelectionPanel, BoxLayout.LINE_AXIS));
        commandSelectionPanel.setPreferredSize(new Dimension(windowWidth, 50));
        commandSelectionPanel.setMinimumSize(new Dimension(windowWidth, 50));

        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.LINE_AXIS));
        commandPanel.setPreferredSize(new Dimension(windowWidth, 70));
        commandPanel.setMinimumSize(new Dimension(windowWidth, 70));

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setPreferredSize(new Dimension(windowWidth, 30));
        buttonPanel.setMinimumSize(new Dimension(windowWidth, 30));

        // Table of Standard Source Physical File Names (keys) and default Source Types (values)
        sourceFilesAndTypes.put("QCLSRC", "CLLE");
        sourceFilesAndTypes.put("QDDSSRC", "DSPF");
        sourceFilesAndTypes.put("QRPGLESRC", "RPGLE");
        sourceFilesAndTypes.put("QRPGSRC", "RPG");
        sourceFilesAndTypes.put("QCBLLESRC", "CBLLE");
        sourceFilesAndTypes.put("QCBLSRC", "CBL");
        sourceFilesAndTypes.put("QCMDSRC", "CMD");
        sourceFilesAndTypes.put("QCSRC", "C");
        sourceFilesAndTypes.put("QTBLSRC", "TBL");

        // Source types
        String[] sourceFileTypes = {"C", "CBL", "CBLLE", "CLLE", "CLP", "CMD", "CPP", "DSPF", "LF",
            "PF", "PRTF", "RPG", "RPGLE", "SQLC", "SQLCPP", "SQLCBL", "SQLCBLLE", "SQLRPG",
            "SQLRPGLE", "TBL",};

        // Command lists for source types
        ArrayList<String> PF = new ArrayList<>();
        ArrayList<String> LF = new ArrayList<>();
        ArrayList<String> DSPF = new ArrayList<>();
        ArrayList<String> PRTF = new ArrayList<>();
        ArrayList<String> CLLE = new ArrayList<>();
        ArrayList<String> CLP = new ArrayList<>();
        ArrayList<String> RPGLE = new ArrayList<>();
        ArrayList<String> RPG = new ArrayList<>();
        ArrayList<String> CBLLE = new ArrayList<>();
        ArrayList<String> CBL = new ArrayList<>();
        ArrayList<String> CMD = new ArrayList<>();
        ArrayList<String> SQLRPGLE = new ArrayList<>();
        ArrayList<String> SQLRPG = new ArrayList<>();
        ArrayList<String> SQLCBLLE = new ArrayList<>();
        ArrayList<String> SQLCBL = new ArrayList<>();
        ArrayList<String> C = new ArrayList<>();
        ArrayList<String> CPP = new ArrayList<>();
        ArrayList<String> SQLC = new ArrayList<>();
        ArrayList<String> SQLCPP = new ArrayList<>();
        ArrayList<String> TBL = new ArrayList<>();

        // Compile commands
        PF.add("CRTPF");
        LF.add("CRTLF");
        DSPF.add("CRTDSPF");
        PRTF.add("CRTPRTF");
        CLLE.add("CRTBNDCL");
        CLLE.add("CRTCLMOD");
        CLP.add("CRTCLPGM");
        RPGLE.add("CRTBNDRPG");
        RPGLE.add("CRTRPGMOD");
        RPG.add("CRTRPGPGM");
        CBLLE.add("CRTBNDCBL");
        CBLLE.add("CRTCBLMOD");
        CBL.add("CRTCBLPGM");
        CMD.add("CRTCMD");
        C.add("CRTBNDC");
        C.add("CRTCMOD");
        CPP.add("CRTBNDCPP");
        CPP.add("CRTCPPMOD");
        SQLRPGLE.add("CRTSQLRPGI *PGM");
        SQLRPGLE.add("CRTSQLRPGI *SRVPGM");
        SQLRPGLE.add("CRTSQLRPGI *MODULE");
        SQLRPG.add("CRTSQLRPG");
        SQLCBLLE.add("CRTSQLCBLI *PGM");
        SQLCBLLE.add("CRTSQLCBLI *SRVPGM");
        SQLCBLLE.add("CRTSQLCBLI *MODULE");
        SQLCBL.add("CRTSQLCBL");
        SQLC.add("CRTSQLCI *MODULE");
        SQLC.add("CRTSQLCI *PGM");
        SQLC.add("CRTSQLCI *SRVPGM");
        SQLCPP.add("CRTSQLCPPI");
        TBL.add("CRTTBL");

        // Table of Source Types (keys) and Compile Commands array lists (values)
        sourceTypesAndCommands.put("CBLLE", CBLLE);
        sourceTypesAndCommands.put("CLLE", CLLE);
        sourceTypesAndCommands.put("CLP", CLP);
        sourceTypesAndCommands.put("CMD", CMD);
        sourceTypesAndCommands.put("C", C);
        sourceTypesAndCommands.put("CPP", CPP);
        sourceTypesAndCommands.put("PF", PF);
        sourceTypesAndCommands.put("LF", LF);
        sourceTypesAndCommands.put("DSPF", DSPF);
        sourceTypesAndCommands.put("PRTF", PRTF);
        sourceTypesAndCommands.put("CBL", CBL);
        sourceTypesAndCommands.put("RPGLE", RPGLE);
        sourceTypesAndCommands.put("RPG", RPG);
        sourceTypesAndCommands.put("SQLC", SQLC);
        sourceTypesAndCommands.put("SQLCPP", SQLCPP);
        sourceTypesAndCommands.put("SQLCBL", SQLCBL);
        sourceTypesAndCommands.put("SQLCBLLE", SQLCBLLE);
        sourceTypesAndCommands.put("SQLRPG", SQLRPG);
        sourceTypesAndCommands.put("SQLRPGLE", SQLRPGLE);
        sourceTypesAndCommands.put("TBL", TBL);

        // Source types combo box - fill with data
        sourceTypes = new ArrayList<>();
        sourceTypes.addAll(Arrays.asList(sourceFileTypes));

        sourceTypeComboBox = new JComboBox(sourceTypes.toArray());
        sourceTypeComboBox.setToolTipText("Select from possible source types or enter one.");
        sourceTypeComboBox.setPreferredSize(new Dimension(100, 20));
        sourceTypeComboBox.setMinimumSize(new Dimension(100, 20));
        sourceTypeComboBox.setMaximumSize(new Dimension(100, 20));
        sourceTypeComboBox.setEditable(true);
        
        // Get application parameters from "Parameters.txt" file.
        getAppProperties();

        // Create compile commands combo box with preselected item
        compileCommandsComboBox = new JComboBox();
        compileCommandsComboBox.setToolTipText("Select from possible compile commands.");
        compileCommandsComboBox.setPreferredSize(new Dimension(170, 20));
        compileCommandsComboBox.setMinimumSize(new Dimension(170, 20));
        compileCommandsComboBox.setMaximumSize(new Dimension(170, 20));
        compileCommandsComboBox.setEditable(true);

        pathLabel.setFont(pathLabel.getFont().deriveFont(Font.BOLD, 20));
        pathLabel.setPreferredSize(new Dimension(windowWidth, 40));
        pathLabel.setMinimumSize(new Dimension(windowWidth, 40));
        pathLabel.setMaximumSize(new Dimension(windowWidth, 40));

        sourceTypeLabel = new JLabel("Source type:");
        compileCommandLabel = new JLabel("Compile command:");

        sourceAttributes = properties.getProperty("SOURCE_ATTRIBUTES");
        if (sourceAttributes.equals("SAVE_SOURCE_ATTRIBUTES")) {
            sourceAttributesButton.setIcon(saveSrcAttribIconDark);
            sourceAttributesButton.setSelectedIcon(saveSrcAttribIconDark);
            sourceAttributesButton.setToolTipText("Save source attributes. Toggle Do not save source attributes.");
        } else {
            sourceAttributesButton.setIcon(noSrcAttribIconDim);
            sourceAttributesButton.setSelectedIcon(noSrcAttribIconDim);
            sourceAttributesButton.setToolTipText("Do not save source attributes. Toggle Save source attributes.");
        }
        sourceAttributesButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sourceAttributesButton.setContentAreaFilled(true);
        sourceAttributesButton.setPreferredSize(new Dimension(25, 20));

        clearSrcAttribButton.setIcon(clearSrcAttribIcon);
        clearSrcAttribButton.setToolTipText("Clear source attributes.");
        clearSrcAttribButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        clearSrcAttribButton.setContentAreaFilled(true);
        clearSrcAttribButton.setPreferredSize(new Dimension(25, 20));

        changeLibraryListButton = new JButton("Library list");
        changeLibraryListButton.setToolTipText("Set user library list and current library.");
        // Set and create the panel layout
        commandSelectionPanel.setLayout(cmdSelLayout);
        cmdSelLayout.setHorizontalGroup(cmdSelLayout.createSequentialGroup()
                .addGroup(cmdSelLayout.createSequentialGroup()
                        .addComponent(sourceTypeLabel)
                        .addComponent(sourceTypeComboBox)
                        .addGap(5)
                        .addComponent(compileCommandLabel)
                        .addComponent(compileCommandsComboBox)
                        .addGap(5)                        
                        .addComponent(sourceAttributesButton)
                        .addComponent(clearSrcAttribButton)
                        .addGap(10)
                        .addComponent(changeLibraryListButton)));
        cmdSelLayout.setVerticalGroup(cmdSelLayout.createSequentialGroup()
                .addGroup(cmdSelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(sourceTypeLabel)
                        .addComponent(sourceTypeComboBox)
                        .addGap(5)
                        .addComponent(compileCommandLabel)
                        .addComponent(compileCommandsComboBox)
                        .addGap(5)
                        .addComponent(sourceAttributesButton)
                        .addComponent(clearSrcAttribButton)
                        .addGap(10)
                        .addComponent(changeLibraryListButton)));

        libraryPatternTextField = new JTextField();
        libraryPatternTextField.setToolTipText("Library search pattern. Can use * and ? wild cards.");
        libraryPattern = ((String) properties.get("LIBRARY_PATTERN")).toUpperCase();
        libraryPatternTextField.setText(libraryPattern);
        libraryPatternTextField.setPreferredSize(new Dimension(100, 20));
        libraryPatternTextField.setMinimumSize(new Dimension(100, 20));
        libraryPatternTextField.setMaximumSize(new Dimension(100, 20));

        String[] selectedLibraries = getListOfLibraries(libraryPattern);

        // Source types combo box - fill with data
        librariesArrayList = new ArrayList<>();
        librariesArrayList.addAll(Arrays.asList(selectedLibraries));
        librariesComboBox = new JComboBox(librariesArrayList.toArray());
        librariesComboBox.setToolTipText("Select library name of the compiled object.");
        librariesComboBox.setPreferredSize(new Dimension(120, 20));
        librariesComboBox.setMinimumSize(new Dimension(120, 20));
        librariesComboBox.setMaximumSize(new Dimension(120, 20));
        librariesComboBox.setEditable(true);

        objectNameFld = new JTextField();
        objectNameFld.setToolTipText("Enter name of compiled object.");
        objectNameFld.setPreferredSize(new Dimension(100, 20));
        objectNameFld.setMinimumSize(new Dimension(100, 20));
        objectNameFld.setMaximumSize(new Dimension(100, 20));

        cancelButton.setToolTipText("Return to previous window.");
        performButton.setToolTipText("Start compilation.");
        lastSplfButton.setToolTipText("Display the last spooled file for the current user.");
        spooledFileButton.setToolTipText("Display list of spooled files for the current user.");
        editButton.setToolTipText("Edit the file to be compiled.");
        jobLogButton.setToolTipText("Print actual contents of job log.");
        clearButton.setToolTipText("Delete all messages from the message area.");
        
        // Set result object library and object name.
        getObjectNames();

        // Set and create the panel layout
        parameterPanel.setLayout(paramLayout);
        paramLayout.setHorizontalGroup(paramLayout.createSequentialGroup()
                .addGroup(paramLayout.createSequentialGroup()
                        .addComponent(librariesLabel)
                        .addComponent(librariesComboBox)
                        .addGap(5)
                        .addComponent(objectNameLabel)
                        .addComponent(objectNameFld)
                        .addGap(5)
                        .addComponent(libraryPatternLabel)
                        .addComponent(libraryPatternTextField)));
        paramLayout.setVerticalGroup(paramLayout.createSequentialGroup()
                .addGroup(paramLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(libraryPatternLabel)
                        .addComponent(libraryPatternTextField)
                        .addComponent(librariesLabel)
                        .addComponent(librariesComboBox)
                        .addGap(5)
                        .addComponent(objectNameLabel)
                        .addComponent(objectNameFld)
                        .addGap(5)
                        .addComponent(libraryPatternLabel)
                        .addComponent(libraryPatternTextField)));

        commandPanel.add(commandTextLabel);

        buttonPanel.add(cancelButton);
        buttonPanel.add(performButton);
        buttonPanel.add(lastSplfButton);
        buttonPanel.add(spooledFileButton);
        buttonPanel.add(editButton);
        buttonPanel.add(jobLogButton);
        buttonPanel.add(clearButton);

        // Scroll pane for message list
        scrollMessagePane.setBorder(BorderFactory.createEmptyBorder());

        // List of messages for placint into message scroll pane
        messageList = new JList<>();

        // Background color of message list
        messageList.setSelectionBackground(Color.WHITE);

        // Decision what color the message will get
        messageList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value.toString().contains("*COMPLETION")) {
                    this.setForeground(DIM_BLUE);
                } else if (value.toString().contains("*ESCAPE")) {
                    this.setForeground(DIM_RED);
                } else if (value.toString().contains("*INFORMATIONAL")) {
                    this.setForeground(Color.BLACK);
                } else if (value.toString().contains("*NOTIFY")) {
                    this.setForeground(DIM_PINK);
                } else {
                    this.setForeground(Color.GRAY);
                }
                return component;
            }
        });
        
        // Build list of messages
        buildMessageList();

        // Make the message table visible in the message scroll pane
        scrollMessagePane.setViewportView(messageList);
        // Create scroll pane adjustment listener
        messageScrollPaneAdjustmentListenerMax = new MessageScrollPaneAdjustmentListenerMax();

        createGlobalPanelLayout();

        // Listeners for command selection panel
        // -------------------------------------
        //

        // Source type combo box
        sourceTypeComboBoxListener = new SourceTypeComboBoxListener();
        // Compile command combo box
        commandsComboBoxListener = new CommandsComboBoxListener();

        // "Custom defaults" button listener
        sourceAttributesButton.addActionListener(ae -> {
            if (sourceAttributesButton.getSelectedIcon().equals(saveSrcAttribIconDark)) {
                sourceAttributesButton.setSelectedIcon(noSrcAttribIconDim);
                sourceAttributesButton.setToolTipText("Do not save source attributes. Toggle Save source attributes.");
                sourceAttributes = "NO_SOURCE_ATTRIBUTES";
            } else {
                sourceAttributesButton.setSelectedIcon(saveSrcAttribIconDark);
                sourceAttributesButton.setToolTipText("Save source attributes. Toggle Do not save source attributes.");
                sourceAttributes = "SAVE_SOURCE_ATTRIBUTES";
            }
            try {
                BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
                // Save custom defaults mode into properties
                properties.setProperty("SOURCE_ATTRIBUTES", sourceAttributes);
                properties.store(outfile, PROP_COMMENT);
                outfile.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        });

        // Clear custom defaults button listener
        clearSrcAttribButton.addActionListener(ae -> {
            try {
                // Empty the custom defaults file
                Files.write(compileCommandsPath, new ArrayList(), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception exc) {
                exc.printStackTrace();
                row = "Error: Clearing of file  " + compileCommandsPath + "  failed.";
                mainWindow.msgVector.add(row);
                mainWindow.showMessages();
            }
            row = "Comp:  Custom defaults of compile commands were cleared. (File  " + compileCommandsPath + "  is empty).";
            mainWindow.msgVector.add(row);
            mainWindow.showMessages();
        });

        // Change library list button listener
        changeLibraryListButton.addActionListener(en -> {
            compileWindowX = this.getX();
            compileWindowY = this.getY();
            // Call window to chang library list
            chgLibList = new ChangeLibraryList(remoteServer, compileWindowX, compileWindowY);
        });

        // Listeners for parameter panel
        // -----------------------------

        // Library pattern listener
        libraryPatternTextField.addActionListener(en -> {
            libraryPattern = libraryPatternTextField.getText().toUpperCase();
            libraryPatternTextField.setText(libraryPattern);
            String[] librariesArr = getListOfLibraries(libraryPattern);
            librariesComboBox.removeAllItems();
            for (int idx = 0; idx < librariesArr.length; idx++) {
                librariesComboBox.addItem(librariesArr[idx]);
            }
        });

        // Object name text field listener
        objectNameFld.addActionListener(en -> {
            objNamePar = objectNameFld.getText();
            compileCommandName = (String) compileCommandsComboBox.getSelectedItem();
            commandText = buildCommand(compileCommandName, libNamePar, objNamePar);
            if (commandText == null) {
                commandTextLabel.setText(compileNotSupported);
                commandTextLabel.setForeground(DIM_RED);
            } else {
                commandTextLabel.setForeground(DIM_BLUE);
                commandTextLabel.setText(commandText);
            }
        });

        // Listeners for button panel
        // --------------------------
        //
        // Cancel button listener
        cancelButton.addActionListener(en -> {
            // Get current window coordinates
            compileWindowX = this.getX();
            compileWindowY = this.getY();
            // Save the coordinates for future display
            saveWindowCoordinates(compileWindowX, compileWindowY);
            // Make the window invisible.
            this.setVisible(false);
        });


        // Enable ESCAPE key to escape from display
        // ----------------------------------------
        globalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        globalPanel.getActionMap().put("escape", new Escape());

        // This listener does the same as the Cancel button
        addWindowListener(new WindowClosing());

        // Perform command button listener
        performButton.addActionListener(en -> {
            performCommand(commandText);
        });

        // Job log button listener
        jobLogButton.addActionListener(en -> {
            printJobLog();
        });

        // Last spooled file button listener
        lastSplfButton.addActionListener(en -> {
            scrollMessagePane.getVerticalScrollBar()
                    .addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
            extractNamesFromIfsPath(pathString);
            String className = this.getClass().getSimpleName();
            // "true" stands for *CURRENT user
            WrkSplF wrkSplf = new WrkSplF(remoteServer, mainWindow, this.pathString, true, compileWindowX, compileWindowY, className);
            SpooledFile splf;
            if (!ifs) {
                splf = wrkSplf.selectSpooledFiles(memberName, "", "", "", userName, "", "", ""); // Set member name
            } else {
                splf = wrkSplf.selectSpooledFiles(pathString, "", "", "", userName, "", "", ""); // Set IFS path string
            }
            String spoolTextAreaString = wrkSplf.convertSpooledFile(splf);
            JTextArea textArea = new JTextArea();
            DisplayFile dspf = new DisplayFile(textArea, mainWindow);
            dspf.displayTextArea(spoolTextAreaString, ibmCcsid);
        });

        // Spooled file button listener
        spooledFileButton.addActionListener(en -> {
            scrollMessagePane.getVerticalScrollBar().addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
            String className = this.getClass().getSimpleName();
            // "true" stands for *CURRENT user
            wwsp = new WrkSplFCall(remoteServer, mainWindow, this.pathString, true, compileWindowX, compileWindowY, className);
            wwsp.execute(); // Run in parallel SwingWorker
        });

        // Edit button listener
        editButton.addActionListener(en -> {
            scrollMessagePane.getVerticalScrollBar()
                    .addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
            // Editing begins with displaying of the file (or member) which is edited by the user.
            // Then the new data is written back to the file (or member) by the user pressing a button.
            if (this.pathString.startsWith("/QSYS.LIB")) {
                // Source member
                JTextArea textArea = new JTextArea();
                JTextArea textArea2 = new JTextArea();
                EditFile edtf = new EditFile(remoteServer, mainWindow, textArea, textArea2, this.pathString, "rewriteSourceMember");
                edtf.displaySourceMember();
            } else {
                // IFS file
                JTextArea textArea = new JTextArea();
                JTextArea textArea2 = new JTextArea();
                EditFile edtf = new EditFile(remoteServer, mainWindow, textArea, textArea2, this.pathString, "rewriteIfsFile");
                edtf.displayIfsFile();
            }
        });

        // Clear messages button listener
        clearButton.addActionListener(en -> {
            msgVector.clear();
            globalPanel.removeAll();
            messageList.removeAll();
            scrollMessagePane = new JScrollPane(messageList);
            scrollMessagePane.setBorder(BorderFactory.createEmptyBorder());
            createGlobalPanelLayout();
        });

        cont = getContentPane();

        cont.add(globalPanel);

        // Make the window visible
        // -----------------------
        setSize(windowWidth, windowHeight);

        setLocation(compileWindowX, compileWindowY);
        // No necessity to setting the window visible in constructor.
        //setVisible(true);
        //pack();
    }

    /**
     * This method gets application parameters from "Parameters.txt" file,
     * updates custom default parameters for source file
     * and concludes window creation (show).
     *
     * @param pathString
     * @param ifs
     */

    public void compile(String pathString, boolean ifs) {
        super.setTitle("Compile  '" + pathString + "'");

        this.pathString = pathString;
        this.ifs = ifs;

        // Disable combo boxes listeners because writing in them must not invoke their listeners.
        sourceTypeComboBox.removeActionListener(sourceTypeComboBoxListener);
        compileCommandsComboBox.removeActionListener(commandsComboBoxListener);

        // Get application parameters from "Parameters.txt" file.
        getAppProperties();

        // Decide if source file attributes modified by the user are to be saved or not.
        if (sourceAttributes.equals("SAVE_SOURCE_ATTRIBUTES")) {
            // Save and update modified attributes in "SourceFileAttributes.lib" for the source file to be compiled.
            String pair = updateModifiedAttributes("compile");
            sourceType = pair.substring(0, pair.indexOf(","));
            sourceTypeComboBox.setSelectedItem(sourceType);
            compileCommandName = pair.substring(pair.indexOf(",") + 1);
        } else {
            // The attributes of the source file are not saved.
            sourceType = setDefaultSourceType();
            setCommandNames(sourceType);
            ArrayList<String> commandNames = getSourceFileAttributes(sourceType);
            // Get default (first) command name
            compileCommandName = commandNames.get(0);
            System.out.println("srcType ORIG: " + sourceType);
            setCommandNames(sourceType);
            compileCommandsComboBox.setSelectedItem(compileCommandName);
        }

        // Set result object library and object name.
        getObjectNames();

        // Build complete command text when source type and command name is updated.
        commandText = buildCommand(compileCommandName, libNamePar, objNamePar);
        if (commandText == null) {
            commandTextLabel.setText(compileNotSupported);
            commandTextLabel.setForeground(DIM_RED);
        } else {
            commandTextLabel.setForeground(DIM_BLUE);
            commandTextLabel.setText(commandText);
        }

        // Enable combo boxes listeners
        // ----------------------------
        // Enable compile commands combo box listener
        compileCommandsComboBox.addActionListener(commandsComboBoxListener);
        // Enable source type combo box listener  - sets also compileCommandsComboBox.
        sourceTypeComboBox.addActionListener(sourceTypeComboBoxListener);

        // Set the window visible again if it was closed (by click on close icon) or canceled by Cancel button.
        setVisible(true);
        pack();
    }

    /**
     * Extract individual names (libraryName, fileName, memberName) from the AS400 IFS path.
     *
     * @param as400PathString
     */
    protected void extractNamesFromIfsPath(String as400PathString) {

        qsyslib = "/QSYS.LIB/";
        if (as400PathString.startsWith(qsyslib) && as400PathString.length() > qsyslib.length()) {
            libraryName = as400PathString.substring(as400PathString.indexOf("/QSYS.LIB/") + 10,
                    as400PathString.lastIndexOf(".LIB"));
            if (as400PathString.length() > qsyslib.length() + libraryName.length() + 5) {
                fileName = as400PathString.substring(qsyslib.length() + libraryName.length() + 5,
                        as400PathString.lastIndexOf(".FILE"));
                if (as400PathString.length() > qsyslib.length() + libraryName.length() + 5
                        + fileName.length() + 6) {
                    memberName = as400PathString.substring(
                            qsyslib.length() + libraryName.length() + 5 + fileName.length() + 6,
                            as400PathString.lastIndexOf(".MBR"));
                }
            }
        }
    }

    /**
     * Update user modified attributes in "SourceFileAttributes.lib" for the source file to be compiled,
     * The attributes are sourceType and compileCommand called here a "pair",
     * The item in the SourceFileAttributes.lib file consists of three elements separated by a comma:
     * <pathString>,<sourceType>,<compileCommand>,
     * A map is constructed for these items where pathString is a key and the "pair" is a value.
     */
    protected String updateModifiedAttributes(String whenCalled) {
        // Remember the source type and compile command name in the file "SourceFileAttributes.lib"
        compileCommandsMap = new TreeMap<>();
        String pair = null;
        String srcType = "";
        String cmdName = "";
        try {
            List<String> items = Files.readAllLines(compileCommandsPath);
            if (!items.isEmpty()) {
                // Read all pairs (<file-path>, <source-type, command-name>) from the file and write them in a map.
                for (String item : items) {
                    System.out.println("ITEM U0:       " + item);
                    String filePath = item.substring(0, item.indexOf(","));
                    pair = item.substring(item.indexOf(",") + 1);
                    System.out.println("pair U0: " + pair);
                    compileCommandsMap.put(filePath, pair); // Adds or updates the pair.
                    System.out.println("compileCommandsMap U0: " + compileCommandsMap);
                }
            }
            if (compileCommandsMap.containsKey(pathString)) {
                System.out.println("ITEM FOUND:   " + pathString + "," + compileCommandsMap.get(pathString));
                // Item for pathString was found
                pair = compileCommandsMap.get(pathString);
                if (whenCalled.equals("compile")) {
                    System.out.println("  *****" + whenCalled + "*****");
                    srcType = pair.substring(0, pair.indexOf(","));
                    cmdName = pair.substring(pair.indexOf(",") + 1);
                    pair = srcType + "," + cmdName;
                    System.out.println("srcType U1: " + srcType);
                    compileCommandsMap.put(pathString, pair);
                    compileCommandsComboBox.setSelectedItem(cmdName);
                } else if (whenCalled.equals("sourceType")) {
                    System.out.println("  *****" + whenCalled + "*****");
                    srcType = (String) sourceTypeComboBox.getSelectedItem();
                    setCommandNames(srcType);
                    cmdName = compileCommands.get(0);
                    pair = srcType + "," + cmdName;
                    System.out.println("srcType U2: " + srcType);
                    compileCommandsMap.put(pathString, pair);
                    compileCommandsComboBox.setSelectedItem(cmdName);
                } else if (whenCalled.equals("commands")) {
                    System.out.println("  *****" + whenCalled + "*****");
                    srcType = pair.substring(0, pair.indexOf(","));
                    cmdName = compileCommandName;
                    pair = srcType + "," + cmdName;
                    System.out.println("srcType U3: " + srcType);
                    System.out.println("cmdName U3: " + cmdName);
                    System.out.println("pair U3: " + pair);
                    compileCommandsMap.put(pathString, pair);
                    compileCommandsComboBox.setSelectedItem(cmdName);
                }
            } else {
                // Item for pathString was NOT found
                System.out.println("  *****" + whenCalled + "*****");
                System.out.println("ITEM NOT Found: " + pathString + "," + compileCommandsMap.get(pathString));
                srcType = setDefaultSourceType();
                ArrayList<String> commandNames = getSourceFileAttributes(srcType);
                // Get default (first) command name
                cmdName = commandNames.get(0);
                System.out.println("srcType UNF: " + srcType);
                setCommandNames(srcType);
                //cmdName = (String) compileCommandsComboBox.getSelectedItem();
                System.out.println("cmdName UNF: " + cmdName);
                pair = srcType + "," + cmdName;
                System.out.println("pair UNF: " + pair);

                compileCommandsMap.put(pathString, pair);
                compileCommandsComboBox.setSelectedItem(cmdName);
            }

            // Write contents of the map to array list.
            ArrayList<String> compileCommandsArr = new ArrayList<>();
            String pair2;
            if (!compileCommandsMap.isEmpty()) {
                String filePath = compileCommandsMap.firstKey();
                while (filePath != null) {
                    System.out.println("compileCommandsMap UK: " + compileCommandsMap);
                    System.out.println("filePath UK: " + filePath);
                    pair2 = compileCommandsMap.get(filePath);
                    System.out.println("pair UK: " + pair2);
                    compileCommandsArr.add(filePath + "," + pair2);
                    filePath = compileCommandsMap.higherKey(filePath);
                }
            }
            // Write array list to the file.
            Files.write(compileCommandsPath, compileCommandsArr);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        System.out.println("pair U4: " + pair);
        return pair;
    }

    /**
     * Get default source type for standard source physical file name (QCLSRC,
     * QRPGLESRC, ...)
     *
     * @param sourceFileName
     * @return
     */
    protected String getDefaultSourceType(String sourceFileName) {
        String sourceType;
        sourceType = sourceFilesAndTypes.get(sourceFileName);
        if (sourceType == null) {
            sourceType = "TXT";
        }
        return sourceType;
    }

    /**
     *
     * @param sourceType
     */
    protected void setCommandNames(String sourceType) {
        // Default values of command names for given sourceType.
        // Set predefined commands (one or more) corresponing to the source type to the command combo box.
        System.out.println("compileCommandsComboBox.getItemCount() N: " + compileCommandsComboBox.getItemCount());
        compileCommands = getSourceFileAttributes(sourceType);
        compileCommandsComboBox.removeAllItems();
        for (int idx = 0; idx < compileCommands.size(); idx++) {
            System.out.println("setCmdNames N: " + compileCommands.get(idx));
            compileCommandsComboBox.addItem(compileCommands.get(idx));
        }
        compileCommandsComboBox.setSelectedItem(compileCommands.get(0));
    }

    /**
     * Get compile commands array for a source type.
     *
     * @param sourceType
     * @return
     */
    protected ArrayList<String> getSourceFileAttributes(String sourceType) {
        ArrayList<String> commandNamesArrayList;
        commandNamesArrayList = sourceTypesAndCommands.get(sourceType);
        System.out.println("getSourceFileAttributes arrList: " + commandNamesArrayList);
        if (commandNamesArrayList == null) {
            commandNamesArrayList = new ArrayList<>();
            commandNamesArrayList.add("CRTCLPGM");
        }
//        String[] compileCommandNames = new String[commandNamesArrayList.size()];
//        compileCommandNames = (String[]) commandNamesArrayList.toArray(compileCommandNames);
        return commandNamesArrayList;
    }


    /**
     * Build text of compile command given command name and parameters
     *
     * @param compileCommand
     * @param libNamePar
     * @param objNamePar
     * @return
     */
    protected String buildCommand(String compileCommand, String libNamePar, String objNamePar) {

        switch (compileCommand) {
            // CLLE, CLP 
            case "CRTBNDCL": // CLLE 
            case "CRTCLPGM": // CLP
            {
                commandText = compileCommand + " PGM(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OUTPUT(  *PRINT  ) DBGVIEW(  *ALL  )";
                break;
            }
            case "CRTCLMOD": // CLLE
            {
                commandText = compileCommand + " MODULE(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OUTPUT(  *PRINT  ) DBGVIEW(  *ALL  )";
                break;
            }

            // OPM RPG
            case "CRTRPGPGM": {
                commandText = compileCommand + " PGM(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OPTION(  *SOURCE  )";
                break;

            }
            // OPM Cobol
            case "CRTCBLPGM": {
                commandText = compileCommand + " PGM(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OPTION(  *SOURCE *PRINT )";
                break;
            }
            // ILE languages without SQL
            case "CRTBNDRPG":
            case "CRTBNDCBL":
            case "CRTBNDC":
            case "CRTBNDCPP": {
                commandText = compileCommand + " PGM(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    commandText += "SRCSTMF( '" + pathString + "'  )";
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " DBGVIEW(  *ALL  ) OUTPUT(  *PRINT  )";
                if (compileCommand.equals("CRTBNDC") || compileCommand.equals("CRTBNDCPP")) {
                    commandText += " OPTION(  *SHOWINC  )";
                }
                break;
            }
            // Compiling ILE languages to modules
            case "CRTRPGMOD":
            case "CRTCBLMOD":
            case "CRTCMOD":
            case "CRTCPPMOD": {
                commandText = compileCommand + " MODULE(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    commandText += "SRCSTMF(  '" + pathString + "'  )";
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " DBGVIEW(  *ALL  ) OUTPUT(  *PRINT  )";
                if (compileCommand.equals("CRTCMOD") || compileCommand.equals("CRTCPPMOD")) {
                    commandText += " OPTION(  *SHOWINC  )";
                }
                break;
            }
            // SQL versions of ILE languages
            case "CRTSQLRPGI *PGM":
            case "CRTSQLRPGI *SRVPGM":
            case "CRTSQLRPGI *MODULE":
            case "CRTSQLCBLI *PGM":
            case "CRTSQLCBLI *SRVPGM":
            case "CRTSQLCBLI *MODULE":
            case "CRTSQLCI *MODULE":
            case "CRTSQLCI *PGM":
            case "CRTSQLCI *SRVPGM": {
                commandNameArray = compileCommand.split(" ");
                compileCommandName = commandNameArray[0];
                if (commandNameArray.length == 2) {
                    commandObjectType = commandNameArray[1];
                } else {
                    commandObjectType = "";
                }
                commandText = compileCommandName + " OBJ(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    commandText += "SRCSTMF(  '" + pathString + "'  )";
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OBJTYPE(  " + commandObjectType + "  ) " // objectType: *MODULE, *PGM, *SRVPGM
                        + " OUTPUT( *PRINT  ) DBGVIEW(*SOURCE)";
                break;
            }
            // C++ creates ony *MODULE
            case "CRTSQLCPPI": {
                commandText = compileCommand + " OBJ(  " + libNamePar + "/" + objNamePar + "  ) ";
                if (ifs) {
                    commandText += "SRCSTMF(  '" + pathString + "'  )";
                } else {
                    commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
                }
                commandText += " OUTPUT(  *PRINT  ) DBGVIEW(  *SOURCE  )";
                break;
            }
            // SQL versions of OPM languages. No possibility to choose object type
            case "CRTSQLRPG":
            case "CRTSQLCBL": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " PGM(  " + libNamePar + "/" + objNamePar + "  ) "
                            + "SRCFILE(  " + libraryName + "/" + fileName + "  ) OUTPUT(  *PRINT  )";
                }
                break;
            }
            // Physical file
            case "CRTPF": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " FILE(  " + libNamePar + "/" + objNamePar + "  ) "
                            + "SRCFILE(  " + libraryName + "/" + fileName + "  ) "
                            + "FILETYPE(  *DATA  ) MBR(  *FILE  ) OPTION(  *LIST  )";
                }
                break;
            }
            // Logical file
            case "CRTLF": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " FILE(  " + libNamePar + "/" + objNamePar + "  ) "
                            + "SRCFILE(  " + libraryName + "/" + fileName
                            + "  ) FILETYPE(  *DATA  ) MBR(  *FILE  ) DTAMBRS(  *ALL  ) OPTION(  *LIST  )";
                }
                break;
            }
            // Display file
            case "CRTDSPF": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " FILE(  " + libNamePar + "/" + objNamePar + "  ) "
                            + "SRCFILE(  " + libraryName + "/" + fileName
                            + "  ) DEV(  *REQUESTER  ) OPTION(  *LIST  )";
                }
                break;
            }
            // Printer file
            case "CRTPRTF": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " FILE(  " + libNamePar + "/" + objNamePar + "  ) "
                            + "SRCFILE(" + libraryName + "/" + fileName + "  ) OPTION(  *LIST  )";
                }
                break;
            }
            // Command
            case "CRTCMD": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " CMD(  " + libNamePar + "/" + objNamePar
                            + "  ) PGM(  *LIBL/" + memberName + "  ) " + "SRCFILE(  " + libraryName + "/"
                            + fileName + "  )";
                }
                break;
            }
            // Table
            case "CRTTBL": {
                if (ifs) {
                    // IFS not available.
                    return null;
                } else {
                    commandText = compileCommand + " TBL(  " + libNamePar + "/" + objNamePar
                            + "  ) PGM(*LIBL/" + memberName + "  ) " + "SRCFILE(  " + libraryName + "/"
                            + fileName + "  )";
                }
                break;
            }
            default: {
            }

        } // end of switch

        return commandText;
    }

    /**
     *
     * @param compileCommandText
     */
    protected void performCommand(String compileCommandText) {

        if (compileCommandText == null) {
            return;
        }

        // Create object for calling CL commands
        CommandCall cmdCall = new CommandCall(remoteServer);
        try {
            // Get current server job
            Job currentJob = new Job();
            currentJob = cmdCall.getServerJob();

            /*
          * System.out.println(currentJob.getUser());
          * System.out.println(currentJob.getName());
          * System.out.println(currentJob.getNumber());
          * System.out.println(currentJob.getLoggingLevel());
          * System.out.println(currentJob.getLoggingCLPrograms());
          * System.out.println(currentJob.getLoggingSeverity());
          * System.out.println(currentJob.getLoggingText());
             */
            // Set job attributes
            currentJob.setLoggingLevel(4);
            currentJob.setLoggingCLPrograms("*YES");
            currentJob.setLoggingSeverity(0);
            currentJob.setLoggingText(Job.LOGGING_TEXT_SECLVL);
            /*
          * System.out.println(currentJob.getUser());
          * System.out.println(currentJob.getName());
          * System.out.println(currentJob.getNumber());
          * System.out.println(currentJob.getLoggingLevel());
          * System.out.println(currentJob.getLoggingCLPrograms());
          * System.out.println(currentJob.getLoggingSeverity());
          * System.out.println(currentJob.getLoggingText());
             */

            // Get library list from the file "UserLibraryList.lib"
            String liblParameter = "";
            List<String> items = Files.readAllLines(libraryListPath);
            if (!items.isEmpty()) {
                items.get(0);
                String[] userUserLibraryList = items.get(0).split(",");
                for (int idx = 1; idx < userUserLibraryList.length; idx++) {
                    liblParameter += userUserLibraryList[idx].trim() + " ";
                }
            }
            if (liblParameter.isEmpty()) {
                liblParameter = "*NONE";
            }

            // Get current library for the file "CurrentLibrary.lib"
            String curlibParameter = "";
            List<String> curlib = Files.readAllLines(currentLibraryPath);
            if (!curlib.isEmpty()) {
                // The only item is the current library name or *CRTDFT
                curlibParameter += curlib.get(0);
            }
            // Build command CHGLIBL
            String commandChgLiblText = "CHGLIBL LIBL(" + liblParameter + ") CURLIB(" + curlibParameter + ")";

            // Perform the GHGLIBL command
            //             -------
            cmdCall.run(commandChgLiblText);

            // Perform the compile command
            // -------------------
            cmdCall.run(compileCommandText);

            // Get messages from the command if any
            AS400Message[] messagelist = cmdCall.getMessageList();
            String[] strArr = new String[messagelist.length];
            // Print all messages
            String type = "";
            int msgType;
            for (int idx = 0; idx < messagelist.length; idx++) {
                msgType = messagelist[idx].getType();
                switch (msgType) {
                    case AS400Message.ESCAPE: {
                        type = "*ESCAPE";
                    }
                    case AS400Message.DIAGNOSTIC: {
                        type = "*DIAGNOSTIC";
                    }
                    case AS400Message.COMPLETION: {
                        type = "*COMPLETION";
                    }
                    case AS400Message.NOTIFY: {
                        type = "*NOTIFY";
                    }
                    case AS400Message.INFORMATIONAL: {
                        type = "*INFORMATIONAL";
                    }
                }
                strArr[idx] = messagelist[idx].getID() + " " + type + ": " + messagelist[idx].getText();
                row = strArr[idx];
                msgVector.add(row);
                if (!messagelist[idx].getHelp().isEmpty()) {
                    strArr[idx] = "       " + messagelist[idx].getHelp();
                    row = strArr[idx];
                    msgVector.add(row);
                }
            }
            reloadMessages();

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     *
     */
    protected void printJobLog() {
        try {
            // Create object for calling CL commands
            CommandCall cmdCall = new CommandCall(remoteServer);
            // Build command DSPJOBLOG so that the job log is printed after the current job ends
            String commandDspJobLog = "DSPJOBLOG JOB(*) OUTPUT(*PRINT) MSGF(*MSG) DUPJOBOPT(*MSG)";
            // Perform the DSPJOBLOG command
            cmdCall.run(commandDspJobLog);
        } catch (Exception exc) {
            exc.printStackTrace();
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
                // Get list of selected libraries
                IFSFile[] ifsFiles = ifsFile.listFiles(libraryPattern + ".LIB");
                libraryNameVector.removeAllElements();
                // Add the selected library names to the vector
                //libraryNameVector.addElement(libraryName);
                for (IFSFile ifsFileLevel2 : ifsFiles) {
                    String bareLibraryName = ifsFileLevel2.getName().substring(0, ifsFileLevel2.getName().indexOf("."));
                    //System.out.println("bareLibraryName: " + bareLibraryName);
                    libraryNameVector.addElement(bareLibraryName);
                }
                // Add "current library" at the end of the vector
                libraryNameVector.addElement("*CURLIB");
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        //System.out.println("libraryNameVector: " + libraryNameVector);

        String[] libraryArray = new String[libraryNameVector.size()];
        libraryArray = libraryNameVector.toArray(libraryArray);
        return libraryArray;
    }



    /**
     * Reload messages
     */
    protected void reloadMessages() {
        scrollMessagePane.getVerticalScrollBar()
                .addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);

        buildMessageList();

        scrollMessagePane.getVerticalScrollBar()
                .removeAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
    }

    /**
     * Build message list.
     */
    protected void buildMessageList() {

        // Fill message list with elements of array list
        messageList.setListData(msgVector);

        // Make the message table visible in the message scroll pane
        scrollMessagePane.setViewportView(messageList);
    }

    /**
     *
     */
    protected void createGlobalPanelLayout() {

        globalPanelLayout = new GroupLayout(globalPanel);
        globalPanelLayout.setAutoCreateGaps(false);
        globalPanelLayout.setAutoCreateContainerGaps(false);

        // Set and create the global panel layout
        globalPanel.setLayout(globalPanelLayout);
        globalPanelLayout.setHorizontalGroup(globalPanelLayout.createSequentialGroup()
                .addGroup(globalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(titlePanel)
                        .addComponent(commandSelectionPanel)
                        .addComponent(parameterPanel)
                        .addComponent(commandPanel)
                        .addComponent(buttonPanel)
                        .addComponent(scrollMessagePane)));
        globalPanelLayout.setVerticalGroup(globalPanelLayout.createSequentialGroup()
                .addGroup(globalPanelLayout.createSequentialGroup()
                        .addComponent(titlePanel)
                        .addComponent(commandSelectionPanel)
                        .addComponent(parameterPanel)
                        .addComponent(commandPanel)
                        .addComponent(buttonPanel)
                        .addComponent(scrollMessagePane)));
        globalPanel.setBorder(BorderFactory.createLineBorder(globalPanel.getBackground(), 5));
    }

    /**
     *
     */
    protected void saveWindowCoordinates(int windowX, int windowY) {
        properties.setProperty("COMPILE_WINDOW_X", String.valueOf(windowX));
        properties.setProperty("COMPILE_WINDOW_Y", String.valueOf(windowY));

        // Create the updated text file in directory "paramfiles"
        final String PROP_COMMENT = "Copy files between IBM i and PC, edit and compile.";
        try {
            BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
            properties.store(outfile, PROP_COMMENT);
            outfile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     *
     */
    class SourceTypeComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            // Disable combo boxes because writing in them must not invoke their listeners.
            sourceTypeComboBox.removeActionListener(sourceTypeComboBoxListener);
            compileCommandsComboBox.removeActionListener(commandsComboBoxListener);

            String sourceType = (String) sourceTypeComboBox.getSelectedItem();
            System.out.println("sourceType S        : " + sourceType);

            if (sourceTypes.indexOf(sourceType) == -1) {
                sourceType = getDefaultSourceType(pathString);
                sourceTypeComboBox.setSelectedItem(sourceType);

                sourceTypeComboBox.setBackground(DIM_RED);
            }

            setCommandNames(sourceType);
            ArrayList<String> commandNames = getSourceFileAttributes(sourceType);
            System.out.println("commandNames S: " + commandNames);
            compileCommandName = commandNames.get(0);
            compileCommandsComboBox.setSelectedIndex(0);

            // Build command text given compile command name (CRT...)
            commandText = buildCommand(compileCommandName, libNamePar, objNamePar);
            if (commandText == null) {
                commandTextLabel.setText(compileNotSupported);
                commandTextLabel.setForeground(DIM_RED);
            } else {
                commandTextLabel.setForeground(DIM_BLUE);
                commandTextLabel.setText(commandText);
            }

            if (sourceAttributes.equals("SAVE_SOURCE_ATTRIBUTES")) {
                updateModifiedAttributes("sourceType");
            }

            // Set result object library and object name.
            getObjectNames();

            // Enable compile commands combo box listener
            compileCommandsComboBox.addActionListener(commandsComboBoxListener);
            // Enable source type combo box listener  - sets also compileCommandsComboBox.
            sourceTypeComboBox.addActionListener(sourceTypeComboBoxListener);
        }
    }

    /**
     * Call the buildCommand() method and get the command text.
     */
    class CommandsComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            // Disable combo boxes because writing in them must not invoke their listeners.
            sourceTypeComboBox.removeActionListener(sourceTypeComboBoxListener);
            compileCommandsComboBox.removeActionListener(commandsComboBoxListener);
            compileCommandName = (String) compileCommandsComboBox.getSelectedItem();
            sourceType = (String) sourceTypeComboBox.getSelectedItem();
            System.out.println("sourceType A        : " + sourceType);
            System.out.println("compileCommandName A: " + compileCommandName);

            if (sourceAttributes.equals("SAVE_SOURCE_ATTRIBUTES")) {
                updateModifiedAttributes("commands");
            }

            // Set result object library and object name.
            getObjectNames();

            // Build command text given compile command name (CRT...)
            commandText = buildCommand(compileCommandName, libNamePar, objNamePar);
            if (commandText == null) {
                commandTextLabel.setText(compileNotSupported);
                commandTextLabel.setForeground(DIM_RED);
            } else {
                commandTextLabel.setForeground(DIM_BLUE);
                commandTextLabel.setText(commandText);
            }
            // Enable compile commands combo box listener
            compileCommandsComboBox.addActionListener(commandsComboBoxListener);
            // Enable source type combo box listener  - sets also compileCommandsComboBox.
            sourceTypeComboBox.addActionListener(sourceTypeComboBoxListener);

        }
    }

    /**
     * Adjustment listener for MESSAGE scroll pane.
     */
    class MessageScrollPaneAdjustmentListenerMax implements AdjustmentListener {

        @Override
        public void adjustmentValueChanged(AdjustmentEvent ae) {
            // Set scroll pane to the bottom - the last element
            ae.getAdjustable().setValue(ae.getAdjustable().getMaximum());
        }
    }


    /**
     * Inner class for Escape function key
     */
    class Escape extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent de) {
            dispose();
        }
    }

    /**
     * Dispose of the main class window and the child window.
     */
    class WindowClosing extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent we) {
            JFrame jFrame = (JFrame) we.getSource();
            // Get current window coordinates
            int windowX = we.getWindow().getX();
            int windowY = we.getWindow().getY();
            // Save the coordinates for future display
            saveWindowCoordinates(windowX, windowY);
            // Make the window invisible
            jFrame.setVisible(false);
        }
    }

    /**
     * Get application parameters from "Parameters.txt" file.
     */
    protected void getAppProperties() {
        // Read application parameters
        properties = new Properties();
        try {
            BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        compileWindowXString = properties.getProperty("COMPILE_WINDOW_X");
        compileWindowYString = properties.getProperty("COMPILE_WINDOW_Y");
        compileWindowX = new Integer(compileWindowXString);
        compileWindowY = new Integer(compileWindowYString);
        libraryPattern = properties.getProperty("LIBRARY_PATTERN");
        sourceTypePar = properties.getProperty("SOURCE_TYPE");
        ibmCcsid = properties.getProperty("IBM_CCSID");
        try {
            ibmCcsidInt = Integer.parseInt(ibmCcsid);
        } catch (Exception exc) {
            // If ibmCcsid is not numeric, take 65535
            exc.printStackTrace();
            ibmCcsid = "65535";
            ibmCcsidInt = 65535;
        }
        userName = properties.getProperty("USERNAME");
        // Path label differs for IFS file and Source member
        titlePanel.add(pathLabel);
        if (ifs) {
            pathLabel.setText("Compile IFS file  " + pathString);
        } else {
            extractNamesFromIfsPath(pathString);
            pathLabel.setText("Compile source member  " + libraryName + "/" + fileName + "(" + memberName + ")");
        }
    }

    /**
     *
     */
    protected String setDefaultSourceType() {
        // Obtain source type from IFS file or source file
        if (ifs) {
            // IFS file
            sourceTypePar = pathString.substring(pathString.lastIndexOf(".") + 1).toUpperCase();
            sourceTypeComboBox.setSelectedItem(sourceTypePar);
        } else {
            // Source file 
            extractNamesFromIfsPath(pathString);
            if (sourceTypePar.equals("*DEFAULT")) {
                // Set source type according to the standard Source file (QRPGLESRC, ...)
                sourceTypePar = getDefaultSourceType(fileName);
            }
            // Source type combo box - fill with source type
            sourceTypeComboBox.setSelectedItem(sourceTypePar);
        }
        return sourceTypePar;
    }

    /**
     * Set result object library and object name.
     */
    protected void getObjectNames() {
        if (ifs) {
            // IFS file
            try {
                String[] librariesArr = getListOfLibraries(libraryPattern);
                librariesComboBox.removeAllItems();
                for (int idx = 0; idx < librariesArr.length; idx++) {
                    librariesComboBox.addItem(librariesArr[idx]);
                }
                libNamePar = (String) librariesComboBox.getSelectedItem();

                // Derive object name (or member name) from the IFS path string
                String fname = pathString.substring(pathString.lastIndexOf("/") + 1);
                // Take max 10 characters
                if (fname.length() >= 10) {
                    objNamePar = fname.substring(0, 10).toUpperCase();
                } else {
                    objNamePar = fname;
                }
                // Remove ending dot if any
                if (objNamePar.indexOf(".") > 0) {
                    objNamePar = objNamePar.substring(0, objNamePar.indexOf("."));
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } else {
            // Source file
            libNamePar = libraryName;
            objNamePar = memberName;
        }
        libraryPatternTextField.setText(libraryPattern);
        librariesComboBox.setSelectedItem(libNamePar);
        objectNameFld.setText(objNamePar);
    }
}
