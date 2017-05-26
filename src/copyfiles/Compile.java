package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Job;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Compile source files or IFS files
 * 
 * @author Vladimír Župka 2017
 */
public class Compile extends JFrame {

   MainWindow mainWindow;

   int windowWidth = 900;
   int windowHeight = 500;

   final Color DIM_BLUE = new Color(50, 60, 160);
   final Color DIM_RED = new Color(190, 60, 50);
   final Color DIM_PINK = new Color(170, 58, 128);

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

   JButton cancelButton = new JButton("Cancel");

   JButton performButton = new JButton("Perform command");

   JButton jobLogButton = new JButton("Job log");

   JButton spooledFileButton = new JButton("Spooled files");
   WrkSplFCall wwsp;

   JButton editButton = new JButton("Edit");

   AS400 remoteServer;

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
   ArrayList<String> compileCommandsArrayList;
   String[] compileCommands;
   String compileCommand;
   JComboBox compileCommandsComboBox;

   JLabel commandLabel = new JLabel("Compile command:");

   JLabel commandTextLabel = new JLabel();

   JButton changeLibraryListButton;
   ChangeLibraryList chgLibList; // Object of this class is the window Change user library list.

   TreeMap<String, String> sourceFilesAndTypes = new TreeMap<>();
   TreeMap<String, ArrayList<String>> sourceTypesAndCommands = new TreeMap<>();

   String commandText;
   ActionListener commandsComboBoxListener;

   JLabel libraryPrefixLabel = new JLabel("Library prefix:");
   JTextField libraryPrefix;
   String libraryPrefixString;

   String libraries = "";
   // Path to UserLibraryList.lib file
   Path libraryListPath = Paths.get(System.getProperty("user.dir"), "workfiles",
         "UserLibraryList.lib");
   // Path to CurrentLibrary.lib file
   Path currentLibraryPath = Paths.get(System.getProperty("user.dir"), "workfiles",
         "CurrentLibrary.lib");

   String userLibraryListString;
   String currentLibrary;

   ArrayList<String> librariesArrayList;
   JComboBox librariesComboBox;
   JLabel librariesLabel = new JLabel("Compiled object       Library:");
   String libNamePar;
   ActionListener librariesComboBoxListener;

   JLabel objectNameLabel = new JLabel("Object:");
   JTextField objectNameFld;
   String objNamePar;

   String compileNotSupported;

   Properties properties;
   Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
   String encoding = System.getProperty("file.encoding", "UTF-8");

   int compileWindowX;
   int compileWindowY;

   String compileWindowXString;
   String compileWindowYString;

   String pathString;
   boolean ifs;

   /**
    * Constructor
    * 
    * @param mainWindow
    */
   Compile(AS400 remoteServer, MainWindow mainWindow, String pathString, boolean ifs) {
      this.mainWindow = mainWindow;
      this.remoteServer = remoteServer;
      this.pathString = pathString;
      this.ifs = ifs;

      globalPanel = new JPanel();
      titlePanel = new JPanel();
      commandSelectionPanel = new JPanel();
      parameterPanel = new JPanel();
      commandPanel = new JPanel();
      buttonPanel = new JPanel();

      cmdSelLayout = new GroupLayout(commandSelectionPanel);
      paramLayout = new GroupLayout(parameterPanel);

      titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
      titlePanel.setPreferredSize(new Dimension(windowWidth - 20, 70));
      titlePanel.setMinimumSize(new Dimension(windowWidth - 20, 70));

      commandSelectionPanel.setLayout(new BoxLayout(commandSelectionPanel, BoxLayout.LINE_AXIS));
      commandSelectionPanel.setPreferredSize(new Dimension(windowWidth - 20, 50));
      commandSelectionPanel.setMinimumSize(new Dimension(windowWidth - 20, 50));

      commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.LINE_AXIS));
      commandPanel.setPreferredSize(new Dimension(windowWidth - 20, 70));
      commandPanel.setMinimumSize(new Dimension(windowWidth - 20, 70));

      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
      buttonPanel.setPreferredSize(new Dimension(windowWidth - 20, 30));
      buttonPanel.setMinimumSize(new Dimension(windowWidth - 20, 30));

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
      String[] sourceFileTypes = { "C", "CBL", "CBLLE", "CLLE", "CLP", "CMD", "CPP", "DSPF", "LF",
            "PF", "PRTF", "RPG", "RPGLE", "SQLC", "SQLCPP", "SQLCBL", "SQLCBLLE", "SQLRPG",
            "SQLRPGLE", "TBL", };

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
      sourceTypeComboBox.setPreferredSize(new Dimension(100, 20));
      sourceTypeComboBox.setMinimumSize(new Dimension(100, 20));
      sourceTypeComboBox.setMaximumSize(new Dimension(100, 20));
      sourceTypeComboBox.setEditable(true);

      getAppProperties();

      getSourceType();

      // One to three compile commands may exist for a source type, 
      // e.g. CRTSQLRPGI: with object types *PGM, *MODULE, *SRVPGM.
      // They are being stored into the String array "compileCommands".
      compileCommands = getCompileCommands(sourceType);

      // Create compile commands combo box with preselected item
      compileCommandsComboBox = new JComboBox(compileCommands);
      compileCommandsComboBox.setPreferredSize(new Dimension(170, 20));
      compileCommandsComboBox.setMinimumSize(new Dimension(170, 20));
      compileCommandsComboBox.setMaximumSize(new Dimension(170, 20));
      compileCommandsComboBox.setEditable(true);
      // Set the first item of the list as selected
      compileCommandsComboBox.setSelectedItem(compileCommands[0]);

      pathLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
      pathLabel.setPreferredSize(new Dimension(windowWidth, 40));
      pathLabel.setMinimumSize(new Dimension(windowWidth, 40));
      pathLabel.setMaximumSize(new Dimension(windowWidth, 40));

      sourceTypeLabel = new JLabel("Source type:");
      compileCommandLabel = new JLabel("Compile command:");

      changeLibraryListButton = new JButton("Change library list");

      // Set and create the panel layout
      commandSelectionPanel.setLayout(cmdSelLayout);
      cmdSelLayout.setHorizontalGroup(cmdSelLayout.createSequentialGroup()
            .addGroup(cmdSelLayout.createSequentialGroup().addComponent(sourceTypeLabel)
                  .addComponent(sourceTypeComboBox).addComponent(compileCommandLabel)
                  .addComponent(compileCommandsComboBox).addComponent(changeLibraryListButton)));
      cmdSelLayout.setVerticalGroup(cmdSelLayout.createSequentialGroup()
            .addGroup(cmdSelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(sourceTypeLabel).addComponent(sourceTypeComboBox)
                  .addComponent(compileCommandLabel).addComponent(compileCommandsComboBox)
                  .addComponent(changeLibraryListButton)));

      libraryPrefix = new JTextField();
      libraryPrefix.setPreferredSize(new Dimension(100, 20));
      libraryPrefix.setMinimumSize(new Dimension(100, 20));
      libraryPrefix.setMaximumSize(new Dimension(100, 20));

      String[] selectedLibraries = getListOfLibraries(libraryPrefixString);
      // Source types combo box - fill with data
      librariesArrayList = new ArrayList<>();
      librariesArrayList.addAll(Arrays.asList(selectedLibraries));
      //Object[] strArr = librariesArrayList.toArray();
      librariesComboBox = new JComboBox(librariesArrayList.toArray());
      librariesComboBox.setPreferredSize(new Dimension(120, 20));
      librariesComboBox.setMinimumSize(new Dimension(120, 20));
      librariesComboBox.setMaximumSize(new Dimension(120, 20));
      librariesComboBox.setEditable(true);

      objectNameFld = new JTextField();
      objectNameFld.setPreferredSize(new Dimension(100, 20));
      objectNameFld.setMinimumSize(new Dimension(100, 20));
      objectNameFld.setMaximumSize(new Dimension(100, 20));

      getObjectNames();

      // Set and create the panel layout
      parameterPanel.setLayout(paramLayout);
      paramLayout.setHorizontalGroup(paramLayout.createSequentialGroup()
            .addGroup(paramLayout.createSequentialGroup().addComponent(librariesLabel)
                  .addComponent(librariesComboBox).addComponent(objectNameLabel)
                  .addComponent(objectNameFld).addComponent(libraryPrefixLabel)
                  .addComponent(libraryPrefix)));
      paramLayout.setVerticalGroup(paramLayout.createSequentialGroup()
            .addGroup(paramLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(libraryPrefixLabel).addComponent(libraryPrefix)
                  .addComponent(librariesLabel).addComponent(librariesComboBox)
                  .addComponent(objectNameLabel).addComponent(objectNameFld)
                  .addComponent(libraryPrefixLabel).addComponent(libraryPrefix)));

      commandPanel.add(commandTextLabel);

      buttonPanel.add(cancelButton);
      buttonPanel.add(performButton);
      buttonPanel.add(jobLogButton);
      buttonPanel.add(spooledFileButton);
      buttonPanel.add(editButton);

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

      globalPanelLayout = new GroupLayout(globalPanel);
      globalPanelLayout.setAutoCreateGaps(true);
      globalPanelLayout.setAutoCreateContainerGaps(true);

      // Set and create the global panel layout
      globalPanel.setLayout(globalPanelLayout);
      globalPanelLayout.setHorizontalGroup(globalPanelLayout.createSequentialGroup()
            .addGroup(globalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(titlePanel).addComponent(commandSelectionPanel)
                  .addComponent(parameterPanel).addComponent(commandPanel).addComponent(buttonPanel)
                  .addComponent(scrollMessagePane)));
      globalPanelLayout.setVerticalGroup(globalPanelLayout.createSequentialGroup()
            .addGroup(globalPanelLayout.createSequentialGroup().addComponent(titlePanel)
                  .addComponent(commandSelectionPanel).addComponent(parameterPanel)
                  .addComponent(commandPanel).addComponent(buttonPanel)
                  .addComponent(scrollMessagePane)));

      // Listeners for command selection panel
      // -------------------------------------
      //
      // Set listener for compile command combo box
      commandsComboBoxListener = new CommandsComboBoxListener();
      compileCommandsComboBox.addActionListener(commandsComboBoxListener);

      // Source type combo box listener
      sourceTypeComboBox.addActionListener(il -> {
         JComboBox<String> source = (JComboBox) il.getSource();
         String srcType = (String) source.getSelectedItem();
         // Get commands (one or more) corresponing to the source type
         compileCommands = getCompileCommands(srcType);
         compileCommandsComboBox.removeActionListener(commandsComboBoxListener);
         compileCommandsComboBox.removeAllItems();
         for (int idx = 0; idx < compileCommands.length; idx++) {
            compileCommandsComboBox.addItem(compileCommands[idx]);
         }
         compileCommandsComboBox.addActionListener(commandsComboBoxListener);
         compileCommandsComboBox.setSelectedItem(compileCommands[0]);
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
      //
      // Library prefix listener
      libraryPrefix.addActionListener(en -> {
         libraryPrefixString = libraryPrefix.getText().toUpperCase();
         libraryPrefix.setText(libraryPrefixString);
         String[] librariesArr = getListOfLibraries(libraryPrefixString);
         librariesComboBox.removeAllItems();
         for (int idx = 0; idx < librariesArr.length; idx++) {
            librariesComboBox.addItem(librariesArr[idx]);
         }
      });

      // Libraries combo box listener
      librariesComboBoxListener = new LibrariesComboBoxListener();
      librariesComboBox.addActionListener(librariesComboBoxListener);

      // Object name text field listener
      objectNameFld.addActionListener(en -> {
         objNamePar = objectNameFld.getText();
         compileCommand = (String) compileCommandsComboBox.getSelectedItem();
         commandText = buildCommand(compileCommand, libNamePar, objNamePar);
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

      // This listener does the same as the Cancel button
      this.addWindowListener(new WindowClosing());

      // Perform command button listener
      performButton.addActionListener(en -> {
         performCommand(commandText);
      });

      // Job log button listener
      jobLogButton.addActionListener(en -> {
         printJobLog();
      });

      // Spooled file button listener
      spooledFileButton.addActionListener(en -> {
         scrollMessagePane.getVerticalScrollBar()
               .addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
         //         if (wwsp == null) {
         String className = this.getClass().getSimpleName();
         // "true" stands for *CURRENT user
         wwsp = new WrkSplFCall(remoteServer, mainWindow, this.pathString, true, compileWindowX,
               compileWindowY, className);
         //         }
         wwsp.execute();
      });

      // Edit button listener
      editButton.addActionListener(en -> {
         scrollMessagePane.getVerticalScrollBar()
               .addAdjustmentListener(messageScrollPaneAdjustmentListenerMax);
         // Editing begins with display of the file (or member) which is edited by the user.
         // Then the new data is written back to the file (or member).
         // by the user pressing a button.
         if (pathString.startsWith("/QSYS.LIB")) {
            // Source member
            EditFile edtf = new EditFile(remoteServer, mainWindow, this.pathString,
                  "rewriteSourceMember");
            edtf.displaySourceMember(true);
         } else {
            // IFS file
            EditFile edtf = new EditFile(remoteServer, mainWindow, this.pathString,
                  "rewriteIfsFile");
            edtf.displayIfsFile(true);
         }
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
    * 
    * @param pathString
    * @param ifs
    */
   public void compile(String pathString, boolean ifs) {

      this.pathString = pathString;
      this.ifs = ifs;

      getAppProperties();

      getSourceType();

      getObjectNames();

      // Set the window visible again if it was closed (by click on close icon) or canceled by Cancel button.
      this.setVisible(true);
   }

   /**
    * Extract individual names (libraryName, fileName, memberName) from the
    * AS400 IFS path.
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
    * Get compile command for source type
    * 
    * @param sourceType
    * @return
    */
   protected String[] getCompileCommands(String sourceType) {
      ArrayList<String> commandNames;
      commandNames = sourceTypesAndCommands.get(sourceType);
      if (commandNames == null) {
         commandNames = new ArrayList<>();
         commandNames.add("CRTCLPGM");
      }
      String[] compileCommandNames = new String[commandNames.size()];
      compileCommandNames = (String[]) commandNames.toArray(compileCommandNames);
      return compileCommandNames;
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
         String[] strArr = compileCommand.split(" ");
         String compileCommandName = strArr[0];
         String objectType;
         if (strArr.length == 2) {
            objectType = strArr[1];
         } else {
            objectType = "";
         }
         commandText = compileCommandName + " OBJ(  " + libNamePar + "/" + objNamePar + "  ) ";
         if (ifs) {
            commandText += "SRCSTMF(  '" + pathString + "'  )";
         } else {
            commandText += "SRCFILE(  " + libraryName + "/" + fileName + "  ) ";
         }
         commandText += " OBJTYPE(  " + objectType + "  ) " // objectType: *MODULE, *PGM, *SRVPGM
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

      // System.out.println(compileCommandText);

      if (compileCommandText == null) {
         return;
      }
      try {
         // Create object for calling CL commands
         CommandCall cmdCall = new CommandCall(remoteServer);

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
         cmdCall.run(commandChgLiblText);

         // Run the compile command
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
    * Get list of all libraries whose names start with a prefix defined in the
    * input field
    * 
    * @param libraryPrefix
    */
   protected String[] getListOfLibraries(String libraryPrefix) {

      IFSFile ifsFile = new IFSFile(remoteServer, "/QSYS.LIB");
      if (ifsFile.getName().equals("QSYS.LIB")) {
         try {
            // Get list of subfiles/subdirectories
            IFSFile[] ifsFiles2 = ifsFile.listFiles();
            libraryNameVector.removeAllElements();
            libraryNameVector.addElement("*CURLIB");

            for (IFSFile ifsFileLevel2 : ifsFiles2) {
               if (ifsFileLevel2.toString().endsWith(".LIB")) {
                  // Select only libraries whose name starts with a string
                  if (ifsFileLevel2.getName().startsWith(libraryPrefix)) {
                     String libName = ifsFileLevel2.getName().substring(0,
                           ifsFileLevel2.getName().indexOf("."));
                     libraryNameVector.addElement(libName);
                  }
               }
            }
         } catch (Exception exc) {
            exc.printStackTrace();
         }
      }

      String[] strArr = new String[libraryNameVector.size()];
      strArr = libraryNameVector.toArray(strArr);
      return strArr;
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
    * Call the buildCommand() method and get the command text.
    */
   class CommandsComboBoxListener implements ActionListener {

      @Override
      public void actionPerformed(ActionEvent ae) {
         JComboBox source = (JComboBox) ae.getSource();
         compileCommand = (String) source.getSelectedItem();
         // Build command text given compile command name (CRT...)
         commandText = buildCommand(compileCommand, libNamePar, objNamePar);
         if (commandText == null) {
            commandTextLabel.setText(compileNotSupported);
            commandTextLabel.setForeground(DIM_RED);
         } else {
            commandTextLabel.setForeground(DIM_BLUE);
            commandTextLabel.setText(commandText);
         }
      }
   }

   /**
    * Call the buildCommand() method and get the command text.
    */
   class LibrariesComboBoxListener implements ActionListener {

      @Override
      public void actionPerformed(ActionEvent ae) {
         libNamePar = (String) librariesComboBox.getSelectedItem();
         compileCommand = (String) compileCommandsComboBox.getSelectedItem();
         commandText = buildCommand(compileCommand, libNamePar, objNamePar);
         if (commandText == null) {
            commandTextLabel.setText(compileNotSupported);
            commandTextLabel.setForeground(DIM_RED);
         } else {
            commandTextLabel.setForeground(DIM_BLUE);
            commandTextLabel.setText(commandText);
         }
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
    * 
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
      libraryPrefixString = properties.getProperty("LIBRARY_PREFIX");
      sourceType = properties.getProperty("SOURCE_TYPE");

      // Path label differs for IFS file and source member
      titlePanel.add(pathLabel);
      if (ifs) {
         pathLabel.setText("Compile IFS file  " + pathString);
      } else {
         extractNamesFromIfsPath(pathString);
         pathLabel.setText(
               "Compile source member  " + libraryName + "/" + fileName + "(" + memberName + ")");
      }
   }

   /**
    * 
    */
   protected void getSourceType() {
      // Obtain source type from IFS file or source file
      if (ifs) {
         // IFS file
         try {
            sourceType = pathString.substring(pathString.lastIndexOf(".") + 1).toUpperCase();
            sourceTypeComboBox.setSelectedItem(sourceType);
            libNamePar = "*CURLIB";

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
         extractNamesFromIfsPath(pathString);

         if (sourceType.equals("*DEFAULT")) {
            // Set source type according to the standard Source file (QRPGLESRC, ...)
            sourceType = getDefaultSourceType(fileName);
         }
         // Source type combo box - fill with source type
         sourceTypeComboBox.setSelectedItem(sourceType);

         // Object p for source members
         libNamePar = libraryName;
         objNamePar = memberName;
      }
   }

   /**
    * 
    */
   protected void getObjectNames() {

      libraryPrefixString = libraryPrefix.getText().toUpperCase();
      libraryPrefix.setText(libraryPrefixString);
      String[] librariesArr = getListOfLibraries(libraryPrefixString);
      librariesComboBox.removeAllItems();
      for (int idx = 0; idx < librariesArr.length; idx++) {
         librariesComboBox.addItem(librariesArr[idx]);
      }

      libraryPrefix.setText(libraryPrefixString);

      librariesComboBox.setSelectedItem(libNamePar);
      objectNameFld.setText(objNamePar);
   }
}
