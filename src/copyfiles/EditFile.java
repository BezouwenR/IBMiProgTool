package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.CommandCall;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Display and edit file - PC file, IFS file, Source Member.
 *
 * @author Vladimír Župka, 2016
 */
public final class EditFile extends JFrame {

    JTextArea textArea;
    JTextComponent editor;

    final Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.020f, 0.99f);
    final Color VERY_LIGHT_GREEN = Color.getHSBColor(0.52f, 0.020f, 0.99f);
    final Color VERY_LIGHT_PINK = Color.getHSBColor(0.025f, 0.008f, 0.99f);


    final Color WARNING_COLOR = new Color(255, 200, 200);
    final Color DIM_BLUE = Color.getHSBColor(0.60f, 0.2f, 0.5f); // blue little saturated dim (gray)
    final Color DIM_RED = Color.getHSBColor(0.00f, 0.2f, 0.98f); // red little saturated bright
    final Color VERY_LIGHT_GRAY = Color.getHSBColor(0.50f, 0.01f, 0.90f);

    HighlightPainter currentPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
    HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    Highlighter blockHighlighter;

    final Color BLUE_DARKER = Color.getHSBColor(0.60f, 0.20f, 0.95f);
    HighlightPainter blockBlueDarker = new DefaultHighlighter.DefaultHighlightPainter(BLUE_DARKER);
    final Color BLUE_LIGHTER = Color.getHSBColor(0.60f, 0.15f, 0.998f);
    HighlightPainter blockBlueLighter = new DefaultHighlighter.DefaultHighlightPainter(BLUE_LIGHTER);

    final Color GREEN_DARKER = Color.getHSBColor(0.35f, 0.15f, 0.90f);
    HighlightPainter blockGreenDarker = new DefaultHighlighter.DefaultHighlightPainter(GREEN_DARKER);
    final Color GREEN_LIGHTER = Color.getHSBColor(0.35f, 0.10f, 0.98f);
    HighlightPainter blockGreenLighter = new DefaultHighlighter.DefaultHighlightPainter(GREEN_LIGHTER);

    final Color RED_DARKER = Color.getHSBColor(0.95f, 0.12f, 0.92f);
    HighlightPainter blockRedDarker = new DefaultHighlighter.DefaultHighlightPainter(RED_DARKER);
    final Color RED_LIGHTER = Color.getHSBColor(0.95f, 0.09f, 0.98f);
    HighlightPainter blockRedLighter = new DefaultHighlighter.DefaultHighlightPainter(RED_LIGHTER);

    final Color YELLOW_DARKER = Color.getHSBColor(0.20f, 0.15f, 0.90f);
    HighlightPainter blockYellowDarker = new DefaultHighlighter.DefaultHighlightPainter(YELLOW_DARKER);
    final Color YELLOW_LIGHTER = Color.getHSBColor(0.20f, 0.15f, 0.96f);
    HighlightPainter blockYellowLighter = new DefaultHighlighter.DefaultHighlightPainter(YELLOW_LIGHTER);

    final Color BROWN_DARKER = Color.getHSBColor(0.13f, 0.15f, 0.86f);
    HighlightPainter blockBrownDarker = new DefaultHighlighter.DefaultHighlightPainter(BROWN_DARKER);
    final Color BROWN_LIGHTER = Color.getHSBColor(0.13f, 0.15f, 0.92f);
    HighlightPainter blockBrownLighter = new DefaultHighlighter.DefaultHighlightPainter(BROWN_LIGHTER);

    final Color GRAY_DARKER = Color.getHSBColor(0.25f, 0.015f, 0.82f);
    HighlightPainter blockGrayDarker = new DefaultHighlighter.DefaultHighlightPainter(GRAY_DARKER);
    final Color GRAY_LIGHTER = Color.getHSBColor(0.25f, 0.015f, 0.88f);
    HighlightPainter blockGrayLighter = new DefaultHighlighter.DefaultHighlightPainter(GRAY_LIGHTER);

    final Color CURLY_BRACKETS_DARKER = Color.getHSBColor(0.25f, 0.020f, 0.75f);
    HighlightPainter curlyBracketsDarker = new DefaultHighlighter.DefaultHighlightPainter(CURLY_BRACKETS_DARKER);
    final Color CURLY_BRACKETS_LIGHTER = Color.getHSBColor(0.25f, 0.020f, 0.86f);
    HighlightPainter curlyBracketsLighter = new DefaultHighlighter.DefaultHighlightPainter(CURLY_BRACKETS_LIGHTER);

    // Block painter
    HighlightPainter blockPainter;

    String progLanguage; // Programming language to highlight (RPG **FREE, ..., Any language)

    /**
     * Listener for edits on the current document.
     */
    protected UndoableEditListener undoHandler = new UndoHandler();
    /** UndoManager that we add edits to. */
    protected UndoManager undo = new UndoManager();

    // --- action implementations -----------------------------------
    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();

    CompileListener compileListener;

    JButton saveButton = new JButton("Save");

    JButton compileButton = new JButton("Compile");
    Compile compile; // Compile class object variable

    JButton undoButton = new JButton("Undo");
    JButton redoButton = new JButton("Redo");

    JLabel characterSetLabel = new JLabel();

    JLabel fontSizeLabel = new JLabel("Font size:");
    JTextField fontSizeField = new JTextField();

    JButton caretButton = new JButton();

    JLabel findLabel = new JLabel("Find what:    ");
    JTextField findField = new JTextField();
    JLayer fieldLayer;

    JButton prevButton = new JButton("<"); // character "arrow up"   
    JButton nextButton = new JButton(">"); // character "arrow down"

    JButton highlightBlocksButton = new JButton("Highlight blocks");
    JLabel highlightBlocksLabel = new JLabel("Highlight blocks:");

    JComboBox<String> languageComboBox = new JComboBox<>();

    JLabel replaceLabel = new JLabel("Replace with:");
    JTextField replaceField = new JTextField();
    JButton replaceButton = new JButton("Replace");
    JButton replaceFindButton = new JButton("Replace/Find");
    JButton replaceAllButton = new JButton("Replace all");

    JLabel shiftLabel = new JLabel("Shift selected:");
    JButton leftButton = new JButton("Left");
    JButton rightButton = new JButton("Right");

    PlaceholderLayerUI layerUI = new PlaceholderLayerUI();
    HighlightHandler highlightHandler = new HighlightHandler();
    int currentPos; // current position in the text area
    int startOffset; // start offset of found text 
    int endOffset; // end offset of found text

    static int windowWidth;
    static int windowHeight;
    int screenWidth;
    int screenHeight;
    int windowX;
    int windowY;

    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    BufferedReader infile;
    final String PROP_COMMENT = "Copy files between IBM i and PC, edit and compile.";
    final String SHORT_CARET = "Short caret";
    final String LONG_CARET = "Long caret";
    final int TAB_SIZE = 4;
    final String NEW_LINE = "\n";
    String encoding = System.getProperty("file.encoding", "UTF-8");
    Properties properties;
    String overWriteFile;
    String pcCharset;
    String ibmCcsid;
    int attributeCCSID;
    String fontSizeString;
    int fontSize;
    String caretShape;

    String msgText;
    String qsyslib;
    String libraryName;
    String fileName;
    String memberName;

    String textLine;
    List<String> list;

    String row;
    boolean nodes = true;
    boolean noNodes = false;

    JPanel globalPanel = new JPanel();
    GroupLayout globalPanelLayout;
    JScrollPane scrollPane;

    // Constructor parameters
    AS400 remoteServer;
    MainWindow mainWindow;
    String filePathString;
    SequentialFile outSeqFile;
    String methodName;
    String sourceType;

    // Highlighting blocks of paired statements (if, dow, etc.)
    ArrayList<String> stmtsBeg = new ArrayList<>();
    ArrayList<String> stmtsEnd = new ArrayList<>();

    int caretPosition;
    String selectedText;
    int selectionStart;
    String shiftedText;

    /**
     * Constructor
     *
     * @param remoteServer
     * @param mainWindow
     * @param filePathString
     * @param methodName
     */
    public EditFile(AS400 remoteServer, MainWindow mainWindow, String filePathString,
            String methodName) {
        this.remoteServer = remoteServer;
        this.mainWindow = mainWindow;
        this.filePathString = filePathString;
        this.methodName = methodName;
        createWindow();
    }

    /**
     * Create window
     */
    protected void createWindow() {
        properties = new Properties();
        try {
            infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
            overWriteFile = properties.getProperty("OVERWRITE_FILE");
            pcCharset = properties.getProperty("PC_CHARSET");
            caretShape = properties.getProperty("CARET");
            fontSizeString = properties.getProperty("FONT_SIZE");
            progLanguage = properties.getProperty("HIGHLIGHT_BLOCKS");

            try {
                fontSize = Integer.parseInt(fontSizeString);
            } catch (Exception exc) {
                exc.printStackTrace();
                fontSizeString = "12";
                fontSize = 12;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        textArea.setTabSize(TAB_SIZE);
        // Set caret position (and scroll bar) to top
        textArea.setCaretPosition(0);

        // create the embedded JTextComponent
        editor = textArea;
        editor.setDragEnabled(true);

        // Create a scroll pane
        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Light sky blue
        scrollPane.setBackground(VERY_LIGHT_BLUE);
        textArea.setBackground(VERY_LIGHT_BLUE);

        caretButton.setText(caretShape);
        if (caretShape.equals(LONG_CARET)) {
            // Set custom caret shape - long vertical gray line with a short red pointer
            textArea.setCaret(new CustomCaret());
        } else {
            textArea.setCaret(new BasicTextUI.BasicCaret());
        }

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        windowWidth = 850;
        windowHeight = screenHeight;

        windowX = screenWidth / 2 - windowWidth / 2;
        windowY = 10;

        // Now the scroll pane may be sized because window height is defined
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(windowWidth, windowHeight - 205));


        saveButton.setPreferredSize(new Dimension(60, 20));
        saveButton.setMinimumSize(new Dimension(60, 20));
        saveButton.setMaximumSize(new Dimension(60, 20));

        undoButton.setPreferredSize(new Dimension(60, 20));
        undoButton.setMinimumSize(new Dimension(60, 20));
        undoButton.setMaximumSize(new Dimension(60, 20));

        redoButton.setPreferredSize(new Dimension(60, 20));
        redoButton.setMinimumSize(new Dimension(60, 20));
        redoButton.setMaximumSize(new Dimension(60, 20));

        compileButton.setPreferredSize(new Dimension(80, 20));
        compileButton.setMinimumSize(new Dimension(80, 20));
        compileButton.setMaximumSize(new Dimension(80, 20));
        compileButton.setForeground(Color.BLUE);

        prevButton.setPreferredSize(new Dimension(60, 20));
        prevButton.setMinimumSize(new Dimension(60, 20));
        prevButton.setMaximumSize(new Dimension(60, 20));
        prevButton.setActionCommand("prev");

        nextButton.setPreferredSize(new Dimension(60, 20));
        nextButton.setMinimumSize(new Dimension(60, 20));
        nextButton.setMaximumSize(new Dimension(60, 20));
        nextButton.setActionCommand("next");

        caretButton.setPreferredSize(new Dimension(90, 20));
        caretButton.setMinimumSize(new Dimension(90, 20));
        caretButton.setMaximumSize(new Dimension(90, 20));

        highlightBlocksButton.setPreferredSize(new Dimension(130, 20));
        highlightBlocksButton.setMinimumSize(new Dimension(130, 20));
        highlightBlocksButton.setMaximumSize(new Dimension(130, 20));


        languageComboBox.setPreferredSize(new Dimension(130, 20));
        languageComboBox.setMaximumSize(new Dimension(130, 20));
        languageComboBox.setMinimumSize(new Dimension(130, 20));

        languageComboBox.addItem("*NONE");
        languageComboBox.addItem("*ALL");
        languageComboBox.addItem("RPG **FREE");
        languageComboBox.addItem("RPG /FREE");
        languageComboBox.addItem("RPG IV fixed");
        languageComboBox.addItem("RPG III");
        languageComboBox.addItem("COBOL");
        languageComboBox.addItem("CL");
        languageComboBox.addItem("C");
        languageComboBox.addItem("C++");

        languageComboBox.setSelectedItem(progLanguage);

        replaceButton.setPreferredSize(new Dimension(70, 20));
        replaceButton.setMinimumSize(new Dimension(70, 20));
        replaceButton.setMaximumSize(new Dimension(70, 20));

        replaceFindButton.setPreferredSize(new Dimension(100, 20));
        replaceFindButton.setMinimumSize(new Dimension(100, 20));
        replaceFindButton.setMaximumSize(new Dimension(100, 20));

        replaceAllButton.setPreferredSize(new Dimension(90, 20));
        replaceAllButton.setMinimumSize(new Dimension(90, 20));
        replaceAllButton.setMaximumSize(new Dimension(90, 20));

        leftButton.setPreferredSize(new Dimension(60, 20));
        leftButton.setMinimumSize(new Dimension(60, 20));
        leftButton.setMaximumSize(new Dimension(60, 20));

        rightButton.setPreferredSize(new Dimension(60, 20));
        rightButton.setMinimumSize(new Dimension(60, 20));
        rightButton.setMaximumSize(new Dimension(60, 20));

        fontSizeField.setText(fontSizeString);
        fontSizeField.setPreferredSize(new Dimension(25, 20));
        fontSizeField.setMaximumSize(new Dimension(25, 20));

        findField.setPreferredSize(new Dimension(200, 20));
        findField.setMaximumSize(new Dimension(200, 20));
        // Set document listener for the search field
        findField.getDocument().addDocumentListener(highlightHandler);

        replaceField.setPreferredSize(new Dimension(200, 20));
        replaceField.setMaximumSize(new Dimension(200, 20));

        // Set a layer of counts that overlay the search field:
        // - the sequence number of just highlighted text found
        // - how many matches were found
        fieldLayer = new JLayer<>(findField, layerUI);

        JPanel rowPanel1 = new JPanel();
        JPanel rowPanel2 = new JPanel();

        JPanel colPanel1 = new JPanel();
        JPanel colPanel2 = new JPanel();

        GroupLayout colPanel1Layout = new GroupLayout(colPanel1);
        SequentialGroup col1sg = colPanel1Layout.createSequentialGroup()
                .addComponent(findLabel)
                .addComponent(replaceLabel);
        ParallelGroup col1pg = colPanel1Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(findLabel)
                .addComponent(replaceLabel);
        colPanel1Layout.setHorizontalGroup(colPanel1Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(col1pg));
        colPanel1Layout.setVerticalGroup(colPanel1Layout.createSequentialGroup()
                .addGroup(col1sg));
        colPanel1.setLayout(colPanel1Layout);

        colPanel2.setLayout(new BoxLayout(colPanel2, BoxLayout.Y_AXIS));

        JPanel colPanel21 = new JPanel();
        JPanel colPanel22 = new JPanel();

        colPanel21.setLayout(new BoxLayout(colPanel21, BoxLayout.X_AXIS));
        colPanel21.add(fieldLayer);
        colPanel21.add(prevButton);
        colPanel21.add(nextButton);
        colPanel21.add(Box.createHorizontalStrut(5));
        colPanel21.add(fontSizeLabel);
        colPanel21.add(fontSizeField);
        colPanel21.add(caretButton);
        colPanel21.add(Box.createHorizontalStrut(5));
        colPanel21.add(highlightBlocksLabel);
        colPanel21.add(languageComboBox);
        colPanel21.add(Box.createHorizontalGlue());

        colPanel22.setLayout(new BoxLayout(colPanel22, BoxLayout.X_AXIS));
        colPanel22.add(replaceField);
        colPanel22.add(replaceButton);
        colPanel22.add(replaceFindButton);
        colPanel22.add(replaceAllButton);
        colPanel22.add(Box.createHorizontalGlue());

        colPanel2.add(colPanel21);
        colPanel2.add(colPanel22);

        GroupLayout rowPanel1Layout = new GroupLayout(rowPanel1);
        SequentialGroup sg1 = rowPanel1Layout.createSequentialGroup()
                .addComponent(saveButton)
                .addGap(5)
                .addComponent(compileButton)
                .addGap(5)
                .addComponent(undoButton)
                .addComponent(redoButton)
                .addGap(5)
                .addComponent(shiftLabel)
                .addComponent(leftButton)
                .addComponent(rightButton)
                .addGap(20)
                .addComponent(characterSetLabel);
        ParallelGroup pg1 = rowPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(saveButton)
                .addGap(5)
                .addComponent(compileButton)
                .addGap(5)
                .addComponent(undoButton)
                .addComponent(redoButton)
                .addGap(5)
                .addComponent(shiftLabel)
                .addComponent(leftButton)
                .addComponent(rightButton)
                .addGap(20)
                .addComponent(characterSetLabel);

        rowPanel1Layout.setHorizontalGroup(rowPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(sg1));
        rowPanel1Layout.setVerticalGroup(rowPanel1Layout.createSequentialGroup()
                .addGroup(pg1));
        rowPanel1.setLayout(rowPanel1Layout);

        GroupLayout rowPanel2Layout = new GroupLayout(rowPanel2);
        SequentialGroup sg2 = rowPanel2Layout.createSequentialGroup()
                .addComponent(colPanel1)
                .addComponent(colPanel2);
        ParallelGroup pg2 = rowPanel2Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(colPanel1)
                .addComponent(colPanel2);

        rowPanel2Layout.setHorizontalGroup(rowPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(sg2)
        );
        rowPanel2Layout.setVerticalGroup(rowPanel2Layout.createSequentialGroup()
                .addGroup(pg2)
        );
        rowPanel2.setLayout(rowPanel2Layout);

        // Lay out components in globalPanel
        globalPanelLayout = new GroupLayout(globalPanel);
        globalPanelLayout.setAutoCreateGaps(false);
        globalPanelLayout.setAutoCreateContainerGaps(true);

        globalPanelLayout.setHorizontalGroup(globalPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(rowPanel1)
                .addComponent(rowPanel2)
                .addGroup(globalPanelLayout.createSequentialGroup().addComponent(scrollPane)));

        globalPanelLayout.setVerticalGroup(globalPanelLayout.createSequentialGroup()
                .addComponent(rowPanel1)
                .addComponent(rowPanel2)
                .addGroup(globalPanelLayout.createParallelGroup().addComponent(scrollPane)));

        globalPanel.setLayout(globalPanelLayout);

        // Save button listener
        saveButton.addActionListener(ae -> {
            // Replace TAB characters with TAB_SIZE spaces in the text area
            replaceTabsWithSpaces();
            // Rewrite file or member
            rewriteFile();
        });

        // Enable ESCAPE key to escape from editing
        // ----------------------------------------
        globalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        globalPanel.getActionMap().put("escape", new Escape());

        // Register Compile button listener
        compileListener = new CompileListener();
        compileButton.addActionListener(compileListener);

        // Set action listener for buttons and check boxes
        Arrays.asList(nextButton, prevButton, replaceButton, replaceFindButton).stream().map((abstractButton) -> {
            abstractButton.setFocusable(false);
            return abstractButton;
        }).forEachOrdered((abstractButton) -> {
            abstractButton.addActionListener(highlightHandler);
        });

        // "Highlight blocks" button listener
        highlightBlocksButton.addActionListener(al -> {
            int currentCaretPos = textArea.getCaretPosition();
            // Show dialog to select programming language for block highlighting
            GetProgLanguage getPl = new GetProgLanguage("Programming language to highlight");
            getPl.getProgLanguage(windowX, windowY);
            try {
                infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
                properties.load(infile);
                infile.close();
                progLanguage = properties.getProperty("HIGHLIGHT_BLOCKS");
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            // Prepare text area with highlighting blocks and show
            prepareEditingAndShow();
            textArea.requestFocusInWindow();
            textArea.setCaretPosition(currentCaretPos);
        });

        // Select programming language from the list in combo box - listener
        languageComboBox.addItemListener(il -> {
            int currentCaretPos = textArea.getCaretPosition();
            JComboBox<String> source = (JComboBox) il.getSource();
            progLanguage = (String) source.getSelectedItem();
            try {
                BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
                // Save programming language into properties
                properties.setProperty("HIGHLIGHT_BLOCKS", progLanguage);
                properties.store(outfile, PROP_COMMENT);
                outfile.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            // Prepare text area with highlighting blocks and show
            prepareEditingAndShow();
            textArea.requestFocusInWindow();
            textArea.setCaretPosition(currentCaretPos);
        });

        // "Replace button" listener
        replaceButton.setActionCommand("replace");

        // "Replace/Find" button listener
        replaceFindButton.setActionCommand("replaceFind");

        // "Font size" field listener
        fontSizeField.addActionListener(al -> {
            fontSizeString = fontSizeField.getText();
            try {
                fontSize = Integer.parseInt(fontSizeString);
            } catch (Exception exc) {
                exc.printStackTrace();
                fontSizeString = "12";
                fontSize = 12;
            }
            fontSizeField.setText(fontSizeString);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
            repaint();
            try {
                BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
                // Save font size into properties
                properties.setProperty("FONT_SIZE", fontSizeString);
                properties.store(outfile, PROP_COMMENT);
                outfile.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }

        });

        // Caret button listener
        caretButton.addActionListener(ae -> {
            try {
                infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
                properties.load(infile);
                infile.close();
                caretShape = properties.getProperty("CARET");
                if (caretButton.getText().equals("Long caret")) {
                    caretButton.setText("Short caret");
                    textArea.setCaret(new BasicTextUI.BasicCaret());
                } else {
                    caretButton.setText("Long caret");
                    // Set custom caret shape - long vertical gray line with a short red pointer
                    textArea.setCaret(new CustomCaret());
                }
                BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
                // Save caret shape into properties
                properties.setProperty("CARET", caretButton.getText());
                properties.store(outfile, PROP_COMMENT);
                outfile.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        });

        // "Replace all" button listener
        replaceAllButton.addActionListener(ae -> {
            Document doc = textArea.getDocument();
            String replacement = replaceField.getText();
            ArrayList<String> arrListPattern = new ArrayList<>();
            ArrayList<Integer> arrListStart = new ArrayList<>();
            ArrayList<Integer> arrListEnd = new ArrayList<>();
            try {
                Pattern pattern = getPattern();
                if (pattern == null) {
                    return;
                }
                if (Objects.nonNull(pattern)) {
                    Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
                    int pos = 0;
                    while (matcher.find(pos)) {
                        int start = matcher.start();
                        int end = matcher.end();
                        // Fill array lists with: found text, start position, end position
                        arrListPattern.add(textArea.getText(start, end - start));
                        arrListStart.add(start);
                        arrListEnd.add(end);
                        pos = end;
                    }
                }
                int hits = arrListPattern.size();
                // Replace texts in intervals found by the replacement (= pattern) 
                for (int idx = hits - 1; idx >= 0; --idx) {
                    textArea.replaceRange(replacement, arrListStart.get(idx), arrListEnd.get(idx));
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });

        // Left shift button listener
        leftButton.addActionListener(ae -> {
            textArea.requestFocusInWindow();
            shiftLeft();
            if (!progLanguage.equals("*NONE")) {
                highlightBlocks(progLanguage);
            }
        });

        // Right shift button listener
        rightButton.addActionListener(ae -> {
            textArea.requestFocusInWindow();
            shiftRight();
            if (!progLanguage.equals("*NONE")) {
                highlightBlocks(progLanguage);
            }
        });

        // Enable processing of function key Ctrl + S = Save member
        globalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()), "save");
        globalPanel.getActionMap().put("save", new SaveAction());

        // Enable processing of function key Ctrl + Arrow UP = Find next hit upwards
        findField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()), "arrowUp");
        findField.getActionMap().put("arrowUp", new ArrowUp());

        // Enable processing of function key Ctrl + Arrow DOWN =  Find next hit downwards
        findField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()), "arrowDown");
        findField.getActionMap().put("arrowDown", new ArrowDown());

        // Enable processing of function Enter key - refresh "Find what"
        findField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "enter");
        findField.getActionMap().put("enter", new EnterKey());

        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("TAB"), "tab");
        textArea.getActionMap().put("tab", new TabListener());

        // Enable processing of function key Ctrl + Arrow Left = Shift lines left
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()), "shiftLeft");
        textArea.getActionMap().put("shiftLeft", new ArrowLeft());

        // Enable processing of function key Ctrl + Arrow Left = Shift lines left
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()), "shiftRight");
        textArea.getActionMap().put("shiftRight", new ArrowRight());

        Container cont = getContentPane();
        cont.add(globalPanel);

        // Display the window.
        setSize(windowWidth, windowHeight);
        setLocation(windowX, windowY);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        // Set caret to the beginning of the text area
        textArea.requestFocusInWindow();
    }

    /**
     * Display contents of the IFS file using its CCSID attribute
     *
     * @param edit
     */
    protected void displayIfsFile(boolean edit) {
        this.setTitle("Edit IFS file " + filePathString);
        caretPosition = textArea.getCaretPosition();

        // Contents of the file are always decoded according to its attributed CCSID.
        // Characters may be displayed incorrectly if the "IBMi CCSID" parameter
        // does not correspond to the file's attributed CCSID.
        // Correct the parameter "IBMi CCSID".
        try {
            IFSFile ifsFile = new IFSFile(remoteServer, filePathString);

            attributeCCSID = ifsFile.getCCSID();
            //System.out.println("IFS CCSID: " + CCSID);

            characterSetLabel.setText("CCSID " + attributeCCSID + " was used for display.");

            byte[] inputBuffer = new byte[100000];
            byte[] workBuffer = new byte[100000];
            textArea.setText("");

            try (IFSFileInputStream inputStream = new IFSFileInputStream(remoteServer, filePathString)) {
                int bytesRead = inputStream.read(inputBuffer);
                while (bytesRead != -1) {
                    for (int idx = 0; idx < bytesRead; idx++) {
                        // Copy input byte to output byte
                        workBuffer[idx] = inputBuffer[idx];
                    }
                    // Copy the printable part of the work array
                    // to a new buffer that will be written out.
                    byte[] bufferToWrite = new byte[bytesRead];
                    // Copy bytes from the work buffer to the new buffer
                    for (int indx = 0; indx < bytesRead; indx++) {
                        bufferToWrite[indx] = workBuffer[indx];
                    }
                    // Create object for conversion from bytes to characters
                    AS400Text textConverter = new AS400Text(bytesRead, attributeCCSID, remoteServer);
                    // Convert byte array buffer to text line
                    String textLine = (String) textConverter.toObject(bufferToWrite);
                    // Append the line to text area
                    textArea.append(textLine + NEW_LINE);

                    // Read next input buffer
                    bytesRead = inputStream.read(inputBuffer);
                }

                // Prepare editing and make editor visible
                prepareEditingAndShow();

                row = "Info: IFS file  " + filePathString + "  has CCSID  " + attributeCCSID + ".";
                mainWindow.msgVector.add(row);
                mainWindow.reloadLeftSideAndShowMessages(nodes);
            }
        } catch (Exception exc) {
            row = "Error: " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes);
        }
        // Remove message scroll listener (cancel scrolling to the last message)
        mainWindow.scrollMessagePane.getVerticalScrollBar().removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
    }

    /**
     * Display text area
     *
     * @param aTextArea
     */
    protected void displayTextArea(JTextArea aTextArea) {
        // Copy text area from parameter to instance text area
        textArea.append(aTextArea.getText());
        // Set scroll bar to top
        textArea.setCaretPosition(0);
        setLocation(windowX, windowY);
        // Display the window.
        setVisible(true);
    }

    /**
     * Display PC file using the application parameter "pcCharset".
     *
     * @param edit
     */
    protected void displayPcFile(boolean edit) {

        this.setTitle("Edit PC file " + filePathString);

        // Disable Compile button - compilation is impossible in PC
        compileButton.removeActionListener(compileListener);

        // Set editability     
        textArea.setEditable(edit);
        caretPosition = textArea.getCaretPosition();
        try {
            Path filePath = Paths.get(filePathString);
            if (Files.exists(filePath)) {
                if (pcCharset.equals("*DEFAULT")) {
                    // Set ISO-8859-1 as a default
                    pcCharset = "ISO-8859-1";
                }
                characterSetLabel.setText(pcCharset + " character set was used for display.");
                // Use PC charset parameter for conversion
                List<String> list = Files.readAllLines(filePath, Charset.forName(pcCharset));
                if (list != null) {
                    // Concatenate all text lines from the list obtained from the file
                    String text = list.stream().reduce("", (a, b) -> a + b + NEW_LINE);
                    textArea.setText(text);
                }
            }
            if (list != null) {
                // Concatenate all text lines from the list obtained from the file
                String text = list.stream().reduce("", (a, b) -> a + b + NEW_LINE);
                textArea.setText(text);
            }

            scrollPane.setBackground(VERY_LIGHT_PINK);
            textArea.setBackground(VERY_LIGHT_PINK);

            // Prepare editing and make editor visible
            prepareEditingAndShow();

            row = "Info: PC file  " + filePathString + "  is displayed using character set  "
                    + pcCharset + "  from the application parameter.";
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes);
            // Remove message scroll listener (cancel scrolling to the last message)
            mainWindow.scrollMessagePane.getVerticalScrollBar().removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
        } catch (Exception exc) {
            exc.printStackTrace();
            //System.out.println(exc.toString());
            row = "Error: File  " + filePathString
                    + "  is not a text file or has an unsuitable character set.  -  " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes); // do not add child nodes
            // Remove message scroll listener (cancel scrolling to the last message)
            mainWindow.scrollMessagePane.getVerticalScrollBar().removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
        }
    }

    /**
     * Display source member using its CCSID attribute; Only data part of the
     * source record is translated (to String - UTF-16).
     *
     * @param edit
     */
    @SuppressWarnings("UseSpecificCatch")
    protected void displaySourceMember(boolean edit) {

        this.setTitle("Edit member " + filePathString);
        caretPosition = textArea.getCaretPosition();

        IFSFile ifsFile = new IFSFile(remoteServer, filePathString);
        // Create an AS400FileRecordDescription object that represents the file
        AS400FileRecordDescription inRecDesc = new AS400FileRecordDescription(remoteServer, filePathString);

        // Set editability     
        textArea.setEditable(edit);
        textArea.setText("");
        try {
            int ccsidAttribute = ifsFile.getCCSID();
            characterSetLabel.setText("CCSID " + ccsidAttribute + " was used for display.");

            // Get list of record formats of the database file
            RecordFormat[] format = inRecDesc.retrieveRecordFormat();
            // Create an AS400File object that represents the file
            SequentialFile as400seqFile = new SequentialFile(remoteServer, filePathString);
            // Set the record format (the only one)
            as400seqFile.setRecordFormat(format[0]);

            // Open the source physical file member as a sequential file
            as400seqFile.open(AS400File.READ_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

            // Read the first source member record
            Record inRecord = as400seqFile.readNext();

            // Write source records to the PC output text file.
            // --------------------
            while (inRecord != null) {
                StringBuilder textLine = new StringBuilder();

                // Prefix is not displayed because it must not be edited!!!
                // Source record is composed of three source record fields: seq.
                // number, date, source data. 
                //-- DecimalFormat df1 = new DecimalFormat("0000.00"); 
                //-- DecimalFormat df2 = new DecimalFormat("000000");
                // Sequence number - 6 bytes String seq = df1.format((Number)
                //--inRecord.getField("SRCSEQ")); 
                // String seq2 = seq.substring(0, 4) + seq.substring(5); 
                // Date - 6 bytes 
                //--String srcDat = df2.format((Number) inRecord.getField("SRCDAT"));

                // Data from source record (the source line)
                byte[] bytes = inRecord.getFieldAsBytes("SRCDTA");

                // Create object for conversion from bytes to characters
                // Ignore "IBM i CCSID" parameter - display characters in the member.
                AS400Text textConverter = new AS400Text(bytes.length, remoteServer);
                // Convert byte array buffer to text line (String - UTF-16)
                String translatedData = (String) textConverter.toObject(bytes);

                // Append translated data to text line 
                textLine.append(translatedData).append(NEW_LINE);

                // Append text line to text area
                textArea.append(textLine.toString());

                // Read next source member record
                inRecord = as400seqFile.readNext();
            }

            // Close the file
            as400seqFile.close();

            // Prepare editing and make editor visible
            prepareEditingAndShow();

            row = "Info: Source member  " + filePathString + "  has CCSID  " + ccsidAttribute + ".";
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes);
        } catch (Exception exc) {
            exc.printStackTrace();
            row = "Error: " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes);
        }
        // Remove message scroll listener (cancel scrolling to the last message)
        mainWindow.scrollMessagePane.getVerticalScrollBar().removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
    }

    /**
     * Prepare text area and editor and set location and visibility of the
     * window.
     */
    private void prepareEditingAndShow() {

        // Replace TAB characters with TAB_SIZE spaces in the text area
        String text = textArea.getText();
        text = text.replace("\t", fixedLengthSpaces(TAB_SIZE));
        textArea.setText(text);

        // Set scroll bar to top
        textArea.setCaretPosition(0);
        // Listener for undoable edits
        editor.getDocument().addUndoableEditListener(undoHandler);
        // Undo button listener
        undoButton.addActionListener(new UndoAction());
        // Redo button listener
        redoButton.addActionListener(new RedoAction());

        // Get a highlighter for the text area
        blockHighlighter = textArea.getHighlighter();
        // Remove all preceding highlights
        blockHighlighter.removeAllHighlights();
        // Hightlight only if the option is not *NONE
        if (!progLanguage.equals("*NONE")) {
            highlightBlocks(progLanguage);
        }

        try {
            BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
            // Save programming language into properties
            properties.setProperty("HIGHLIGHT_BLOCKS", progLanguage);
            properties.store(outfile, PROP_COMMENT);
            outfile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // Show the window on the specified position
        setLocation(windowX - 100, windowY);
        setVisible(true);
    }

    /**
     * Highlight compound statements (blocks) in a simplified parsing
     */
    private void highlightBlocks(String progLanguage) {
        stmtsBeg.clear();
        stmtsEnd.clear();

        switch (progLanguage) {
            case "*ALL": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("DCL-DS");
                stmtsBeg.add("DCL-PR");
                stmtsBeg.add("DCL-PI");
                stmtsBeg.add("DCL-PROC");
                stmtsBeg.add("BEGSR");
                stmtsBeg.add("SECTION");
                // Loops
                stmtsBeg.add("DOW");
                stmtsBeg.add("DOW(");
                stmtsBeg.add("DOU");
                stmtsBeg.add("DOU(");
                stmtsBeg.add("DOUNTIL");
                stmtsBeg.add("DO");
                stmtsBeg.add("WHILE");
                stmtsBeg.add("DOWHILE");
                stmtsBeg.add("UNTIL");
                stmtsBeg.add("FOR");
                stmtsBeg.add("FOR(");
                stmtsBeg.add("DOFOR");
                stmtsBeg.add("PERFORM");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("IF(");
                stmtsBeg.add("ELSEIF");
                stmtsBeg.add("ELSEIF(");
                stmtsBeg.add("THEN");
                stmtsBeg.add("ELSE");
                stmtsBeg.add("SELECT");
                stmtsBeg.add("SWITCH");
                stmtsBeg.add("CASE");
                stmtsBeg.add("DEFAULT");
                stmtsBeg.add("WHEN");
                stmtsBeg.add("WHEN(");
                stmtsBeg.add("OTHER");
                stmtsBeg.add("OTHERWISE");
                stmtsBeg.add("EVALUATE");
                // Monitors
                stmtsBeg.add("MONITOR");
                stmtsBeg.add("ON-ERROR");
                stmtsBeg.add("MONMSG");
                stmtsBeg.add("TRY");
                stmtsBeg.add("CATCH");
                // C - style
                stmtsBeg.add("{");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("END-DS");
                stmtsEnd.add("END-PR");
                stmtsEnd.add("END-PI");
                stmtsEnd.add("END-PROC");
                stmtsEnd.add("ENDSR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("ENDFOR");
                stmtsEnd.add("END-PERFORM");
                // Conditions
                stmtsEnd.add("ENDIF");
                stmtsEnd.add("END-IF");
                stmtsEnd.add("ENDSL");
                stmtsEnd.add("ENDSELECT");
                stmtsEnd.add("END-EVALUATE");
                // Monitor
                stmtsEnd.add("ENDMON");
                // C - style
                stmtsEnd.add("}");
                break;
            } // End of case *ALL

            case "RPG **FREE": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("DCL-DS");
                stmtsBeg.add("DCL-PR");
                stmtsBeg.add("DCL-PI");
                stmtsBeg.add("DCL-PROC");
                stmtsBeg.add("BEGSR");
                // Loops
                stmtsBeg.add("DOW");
                stmtsBeg.add("DOW(");
                stmtsBeg.add("DOU");
                stmtsBeg.add("DOU(");
                stmtsBeg.add("FOR");
                stmtsBeg.add("FOR(");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("IF(");
                stmtsBeg.add("ELSEIF");
                stmtsBeg.add("ELSEIF(");
                stmtsBeg.add("SELECT");
                stmtsBeg.add("WHEN");
                stmtsBeg.add("WHEN(");
                stmtsBeg.add("OTHER");
                // Monitors
                stmtsBeg.add("MONITOR");
                stmtsBeg.add("ON-ERROR");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("END-DS");
                stmtsEnd.add("END-PR");
                stmtsEnd.add("END-PI");
                stmtsEnd.add("END-PROC");
                stmtsEnd.add("ENDSR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("ENDFOR");
                // Conditions
                stmtsEnd.add("ENDIF");
                stmtsEnd.add("ENDSL");
                // Monitor
                stmtsEnd.add("ENDMON");
                break;
            } // End of case RPG **FREE

            case "RPG /FREE": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("DCL-DS");
                stmtsBeg.add("DCL-PR");
                stmtsBeg.add("DCL-PI");
                stmtsBeg.add("DCL-PROC");
                stmtsBeg.add("BEGSR");
                // Loops
                stmtsBeg.add("DOW");
                stmtsBeg.add("DOW(");
                stmtsBeg.add("DOU");
                stmtsBeg.add("DOU(");
                stmtsBeg.add("FOR");
                stmtsBeg.add("FOR(");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("IF(");
                stmtsBeg.add("ELSEIF");
                stmtsBeg.add("ELSEIF(");
                stmtsBeg.add("SELECT");
                stmtsBeg.add("WHEN");
                stmtsBeg.add("WHEN(");
                stmtsBeg.add("OTHER");
                // Monitors
                stmtsBeg.add("MONITOR");
                stmtsBeg.add("ON-ERROR");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("END-DS");
                stmtsEnd.add("END-PR");
                stmtsEnd.add("END-PI");
                stmtsEnd.add("END-PROC");
                stmtsEnd.add("ENDSR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("ENDFOR");
                // Conditions
                stmtsEnd.add("ENDIF");
                stmtsEnd.add("ENDSL");
                // Monitor
                stmtsEnd.add("ENDMON");
                break;
            } // End of case RPG /FREE

            case "RPG IV fixed": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("BEGSR");
                // Loops
                stmtsBeg.add("DO");
                stmtsBeg.add("DOW");
                stmtsBeg.add("DOW(");
                stmtsBeg.add("DOU");
                stmtsBeg.add("DOU(");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("IF(");
                stmtsBeg.add("ELSEIF");
                stmtsBeg.add("SELECT");
                stmtsBeg.add("WHEN");
                stmtsBeg.add("WHEN(");
                stmtsBeg.add("OTHER");
                // Monitors
                stmtsBeg.add("MONITOR");
                stmtsBeg.add("ON-ERROR");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("ENDSR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("END  ");
                // Conditions
                stmtsEnd.add("ENDIF");
                stmtsEnd.add("ENDSL");
                // Monitor
                stmtsEnd.add("ENDMON");
                break;
            } // End of case RPG IV fixed

            case "RPG III": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("BEGSR");
                // Loops
                stmtsBeg.add("DO");
                stmtsBeg.add("DOW");
                stmtsBeg.add("DOU");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("SELEC");
                stmtsBeg.add("CAS");
                stmtsBeg.add("WH");
                stmtsBeg.add("OTHER");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("ENDSR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("END  ");
                // Conditions
                stmtsEnd.add("ENDIF");
                stmtsEnd.add("ENDSL");
                stmtsEnd.add("ENDCS");
                break;
            } // End of case RPG III

            case "CL": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("SUBR");
                // Loops
                stmtsBeg.add("DOUNTIL");
                stmtsBeg.add("DOWHILE");
                stmtsBeg.add("DOFOR");
                stmtsBeg.add("DO");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("THEN");
                stmtsBeg.add("ELSE");
                stmtsBeg.add("SELECT");
                stmtsBeg.add("WHEN");
                stmtsBeg.add("OTHERWISE");
                // Monitors         
                stmtsBeg.add("MONMSG");

                // Ends of block statements

                // Declarations
                stmtsEnd.add("ENDSUBR");
                // Loops
                stmtsEnd.add("ENDDO");
                stmtsEnd.add("ENDSELECT");
                break;
            } // End of case CL

            case "COBOL": {
                // Beginnings of block statements

                // Declarations
                stmtsBeg.add("SECTION");
                // Loops
                stmtsBeg.add("PERFORM");
                stmtsBeg.add("UNTIL");
                // Conditions
                stmtsBeg.add("IF ");
                stmtsBeg.add("THEN");
                stmtsBeg.add("ELSE");
                stmtsBeg.add("EVALUATE");
                stmtsBeg.add("WHEN");

                // Ends of block statements

                // Loops
                stmtsEnd.add("END-PERFORM");
                // Conditions
                stmtsEnd.add("END-IF");
                stmtsEnd.add("END-EVALUATE");
                break;
            } // End of case COBOL

            case "C": {
                // Beginnings of block statements

                // Loops
                stmtsBeg.add("WHILE");
                stmtsBeg.add("FOR");
                stmtsBeg.add("DO");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("ELSE");
                stmtsBeg.add("SWITCH");
                stmtsBeg.add("CASE");
                stmtsBeg.add("DEFAULT");
                stmtsBeg.add("{");

                // Endings of block statements

                stmtsEnd.add("}");
                stmtsEnd.add("ENDIF");
                break;
            } // End of case C

            case "C++": {
                // Beginnings of block statements

                // Loops
                stmtsBeg.add("WHILE");
                stmtsBeg.add("FOR");
                stmtsBeg.add("DO");
                // Conditions
                stmtsBeg.add("IF");
                stmtsBeg.add("ELSE");
                stmtsBeg.add("SWITCH");
                stmtsBeg.add("CASE");
                stmtsBeg.add("DEFAULT");
                // Monitors         
                stmtsBeg.add("TRY");
                stmtsBeg.add("CATCH");
                stmtsBeg.add("{");

                // Endings of block statements

                stmtsEnd.add("}");
                stmtsEnd.add("ENDIF");
                break;
            } // End of case C++

        } // End of switch

        // Find and highlight beginning block statements
        stmtsBeg.forEach(stmtBeg -> {
            highlightBlockStmt(stmtBeg, true); // true is tested as beg
        });

        // Find and highlight ending block statements
        stmtsEnd.forEach(stmtEnd -> {
            highlightBlockStmt(stmtEnd, false); // false is tested as !beg
        });

    }

    /**
     * Highlight block statements
     *
     * @param blockStmt
     * @param beg
     */
    private void highlightBlockStmt(String blockStmt, boolean beg) {

        // Beginnings of block statements - colors
        if (beg && (blockStmt.equals("DCL-DS"))) {
            // DCL-DS in RPG **FREE
            blockPainter = blockBrownLighter;
        } else if (beg && blockStmt.equals("DCL-PR")) {
            // DCL-PR in RPG **FREE
            blockPainter = blockBrownLighter;
        } else if (beg && blockStmt.equals("DCL-PI")) {
            // DCL-PI in RPG **FREE
            blockPainter = blockBrownLighter;
        } else if (beg && blockStmt.equals("DOW")) {
            // DOW in RPG
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOW(")) {
            // DOW( in RPG **FREE
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOU")) {
            // DOU in RPG
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOU(")) {
            // DOU( in RPG **FREE 
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOUNTIL")) {
            // DOUNTIL in CL
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOWHILE")) {
            // DOWHILE in CL
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DO")) {
            // DO in CL, C, C++, older RPG
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("WHILE")) {
            // WHILE in C, C++
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("UNTIL")) {
            // UNTIL in C, C++, COBOL
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("FOR")) {
            // FOR in RPG, C, C++
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("FOR(")) {
            // FOR( in RPG **FREE
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("DOFOR")) {
            // DOFOR in CL
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("PERFORM")) {
            // PERFORM in COBOL
            blockPainter = blockBlueLighter;
        } else if (beg && blockStmt.equals("IF")) {
            // IF in RPG, CL, C, C++
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("IF(")) {
            // IF( in RPG **FREE
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("IF ")) {
            // IF in COBOL (with a space at the end)
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("ELSEIF")) {
            // ELSEIF in RPG
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("THEN")) {
            // THEN in COBOL
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("ELSE")) {
            // ELSE in RPG, CL, COBOL, C, C++
            blockPainter = blockGreenLighter;
        } else if (beg && blockStmt.equals("SELECT")) {
            // SELECT in RPG IV
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("SELEC")) {
            // SELEC in RPG III
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("WHEN")) {
            // WHEN in RPG, COBOL
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("WHEN(")) {
            // WHEN( in RPG
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("WH")) {
            // WH in RPG III (as WHEQ, WHLE, ...)
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("OTHER")) {
            // OTHER in RPG
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("SWITCH")) {
            // switch in C or C++
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("CASE")) {
            // case in switch in C or C++
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("EVALUATE")) {
            // EVALUATE in COBOL
            blockPainter = blockYellowLighter;
        } else if (beg && blockStmt.equals("MONITOR")) {
            // MONITOR in RPG IV
            blockPainter = blockRedLighter;
        } else if (beg && blockStmt.equals("ON-ERROR")) {
            // ON-ERROR in RPG IV
            blockPainter = blockRedLighter;
        } else if (beg && blockStmt.equals("TRY")) {
            // try in C++
            blockPainter = blockRedLighter;
        } else if (beg && blockStmt.equals("CATCH")) {
            // catch in C++
            blockPainter = blockRedLighter;
        } else if (beg && blockStmt.equals("DCL-PROC")) {
            // DCL-PROC in RPG **FREE
            blockPainter = blockGrayLighter;
        } else if (beg && blockStmt.equals("BEGSR")) {
            // BEGSR in RPG
            blockPainter = blockGrayLighter;
        } else if (beg && blockStmt.equals("SECTION")) {
            // SECTION in COBOL
            blockPainter = blockGrayLighter;
        } else if (beg && blockStmt.equals("{")) {
            // { in C, C++
            blockPainter = curlyBracketsLighter;

            // Ends of block statements - colors
        } else if (!beg && blockStmt.equals("END-DS")) {
            // END-DS in RPG **FREE
            blockPainter = blockBrownDarker;
        } else if (!beg && blockStmt.equals("END-PR")) {
            // END-PR in RPG **FREE
            blockPainter = blockBrownDarker;
        } else if (!beg && blockStmt.equals("END-PI")) {
            // END-PI in RPG **FREE
            blockPainter = blockBrownDarker;
        } else if (!beg && blockStmt.equals("ENDDO")) {
            // ENDDO in RPG, CL
            blockPainter = blockBlueDarker;
        } else if (!beg && blockStmt.equals("END  ")) {
            // END in RPGIII
            blockPainter = blockBlueDarker;
        } else if (!beg && blockStmt.equals("ENDFOR")) {
            // ENDFOR in RPG, CL
            blockPainter = blockBlueDarker;
        } else if (!beg && blockStmt.equals("END-PERFORM")) {
            // END-PERFORM in COBOL
            blockPainter = blockBlueDarker;
        } else if (!beg && blockStmt.equals("ENDIF")) {
            // ENDIF in RPG and C, C++ (in #endif)
            blockPainter = blockGreenDarker;
        } else if (!beg && blockStmt.equals("END-IF")) {
            // END-IF in COBOL
            blockPainter = blockGreenDarker;
        } else if (!beg && blockStmt.equals("ENDSL")) {
            // ENDSL in RPG
            blockPainter = blockYellowDarker;
        } else if (!beg && blockStmt.equals("ENDSELECT")) {
            // ENDSELECT in CL
            blockPainter = blockYellowDarker;
        } else if (!beg && blockStmt.equals("DEFAULT")) {
            // default in C, C++
            blockPainter = blockYellowDarker;
        } else if (!beg && blockStmt.equals("END-EVALUATE")) {
            // END-EVALUATE in COBOL
            blockPainter = blockYellowDarker;
        } else if (!beg && blockStmt.equals("ENDMON")) {
            // ENDMON in CL
            blockPainter = blockRedDarker;
        } else if (!beg && blockStmt.equals("ENDSR")) {
            // ENDSR in RPG
            blockPainter = blockGrayDarker;
        } else if (!beg && blockStmt.equals("END-PROC")) {
            // END-PROC in RPG **FREE
            blockPainter = blockGrayDarker;
        } else if (!beg && blockStmt.equals("}")) {
            // } in C, C++
            blockPainter = curlyBracketsDarker;
        }

        // Inspect each line separately for ONE occurrence of the block statement. 
        // Highlight only the block statement that is outside of a comment, if it is not too complex.

        // C and C++ are inspected NEITHER for comments NOR for block end statements (i.e. curly brackets).
        // It would be unacceptably complex.

        // The *ALL option highlights all occurrences in all languages.
        String text = textArea.getText().toUpperCase();
        int startOfLine = 0;
        int endOfLine = 0;
        try {
            endOfLine = text.indexOf(NEW_LINE, startOfLine);
            while (startOfLine > -1 && startOfLine < text.length()) {

                if (endOfLine - startOfLine > 0) {

                    int startOfBlockStmt = text.indexOf(blockStmt, startOfLine);
                    int endOfBlockStmt = startOfBlockStmt + blockStmt.length();

                    if (startOfBlockStmt >= startOfLine && startOfBlockStmt <= endOfLine - blockStmt.length()) {
                        switch (progLanguage) {

                            case "*ALL": {
                                blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                break;
                            }

                            case "RPG **FREE": {
                                // Before block statement: All spaces or empty
                                // After block statement: A space or semicolon or new line
                                if ((text.substring(startOfLine, startOfBlockStmt).equals(fixedLengthSpaces(startOfBlockStmt - startOfLine))
                                        || text.substring(startOfLine, startOfBlockStmt).isEmpty())
                                        && (text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(" ")
                                        || text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(";")
                                        || text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(NEW_LINE))) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case RPG **FREE

                            case "RPG /FREE": {
                                // Before block statement: at least 7 spaces
                                // After block statement: A space or new line or semicolon
                                // No asterisk comment (* in column 7)
                                if ((text.substring(startOfLine + 7, startOfBlockStmt)
                                        .equals(fixedLengthSpaces(startOfBlockStmt - (startOfLine + 7)))
                                        || text.substring(startOfLine, startOfBlockStmt).isEmpty())
                                        && (text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(" ")
                                        || text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(NEW_LINE)
                                        || text.substring(endOfBlockStmt, endOfBlockStmt + 1).equals(";"))
                                        && !text.substring(startOfLine + 6, startOfLine + 7).equals("*")) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case RPG /FREE

                            case "RPG IV fixed": {
                                // C in column 6 and no asterisk comment (* in column 7) and block statement in column 26 (Opcode)
                                if (text.substring(startOfLine + 5, startOfLine + 6).equals("C")
                                        && !text.substring(startOfLine + 6, startOfLine + 7).equals("*")
                                        && startOfBlockStmt - startOfLine == 25) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case RPG IV fixed

                            case "RPG III": {
                                // C in column 6 and no asterisk comment (* in column 7) and block statement in column 28 (Opcode)
                                if (text.substring(startOfLine + 5, startOfLine + 6).equals("C")
                                        && !text.substring(startOfLine + 6, startOfLine + 7).equals("*")
                                        && startOfBlockStmt - startOfLine == 27) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case RPG RPG III

                            case "CL": {
                                String line = text.substring(startOfLine, endOfLine);
                                int commentLeftPos = line.indexOf("/*");
                                int commentRightPos = line.indexOf("*/");
                                // One comment exists in the line and the block statement is outside
                                // (We do not assume that there are more comments in the line.)
                                if (commentRightPos > 4 && commentLeftPos < commentRightPos
                                        && (endOfBlockStmt <= startOfLine + commentLeftPos
                                        || startOfBlockStmt >= startOfLine + commentRightPos + "*/".length())) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                } // Highlight block statement if there is no comment in line 
                                else if (commentLeftPos == -1) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case CL

                            case "COBOL": {
                                // No asterisk or slash comment (* or / in column 7) and the block statement in columns 12 to 72
                                if (!text.substring(startOfLine + 6, startOfLine + 7).equals("*")
                                        && !text.substring(startOfLine + 6, startOfLine + 7).equals("/")
                                        && startOfBlockStmt - startOfLine >= 11
                                        && endOfBlockStmt - startOfLine <= 72) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case COBOL

                            case "C": {
                                String line = text.substring(startOfLine, endOfLine);
                                int doubleSlashPos = line.indexOf("//");
                                int commentLeftPos = line.indexOf("/*");
                                int commentRightPos = line.indexOf("*/");
                                // One comment exists in the line and the block statement is outside
                                // (We do not assume that there are more comments in the line.)
                                if (commentRightPos > 4 && commentLeftPos < commentRightPos && commentLeftPos > 0
                                        && (endOfBlockStmt <= startOfLine + commentLeftPos
                                        || startOfBlockStmt >= startOfLine + commentRightPos + "*/".length())) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                } else if (doubleSlashPos > -1) {
                                    if (endOfBlockStmt <= startOfLine + doubleSlashPos) {
                                        blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                    }
                                } // Highlight block statement if there is no comment in line 
                                else if (commentLeftPos == -1) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case C

                            case "C++": {
                                String line = text.substring(startOfLine, endOfLine);
                                int doubleSlashPos = line.indexOf("//");
                                int commentLeftPos = line.indexOf("/*");
                                int commentRightPos = line.indexOf("*/");
                                // One comment exists in the line and the block statement is outside
                                // (We do not assume that there are more comments in the line.)
                                if (commentRightPos > 4 && commentLeftPos < commentRightPos && commentLeftPos > 0
                                        && (endOfBlockStmt <= startOfLine + commentLeftPos
                                        || startOfBlockStmt >= startOfLine + commentRightPos + "*/".length())) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                } else if (doubleSlashPos > -1) {
                                    if (endOfBlockStmt <= startOfLine + doubleSlashPos) {
                                        blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                    }
                                } // Highlight block statement if there is no comment in line 
                                else if (commentLeftPos == -1) {
                                    blockHighlighter.addHighlight(startOfBlockStmt, endOfBlockStmt, blockPainter);
                                }
                                break;
                            } // End of case C++

                        } // End of switch
                    }
                }
                startOfLine = text.indexOf(NEW_LINE, startOfLine) + NEW_LINE.length();
                endOfLine = text.indexOf(NEW_LINE, startOfLine);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Construct Pattern object and compile it.
     *
     * @return
     */
    private Pattern getPattern() {
        String pattern = findField.getText();

        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            return null;
        }
        try {
            pattern = String.format(pattern);
            // Allow backslash, asterisk, plus, question mark etc.
            // The backslash must be tested first!!!
            pattern = pattern.replace("\\", "\\\\");
            pattern = pattern.replace("*", "\\*");
            pattern = pattern.replace("+", "\\+");
            pattern = pattern.replace("?", "\\?");
            pattern = pattern.replace("$", "\\$");
            pattern = pattern.replace(".", "\\.");
            pattern = pattern.replace("[", "\\[");
            pattern = pattern.replace("^", "\\^");
            pattern = pattern.replace("_", "\\_");
            pattern = pattern.replace("|", "\\|");
            pattern = pattern.replace("{", "\\{");
            pattern = pattern.replace("(", "\\(");
            pattern = pattern.replace(")", "\\)");
            pattern = pattern.replace("`", "\\`");

            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            findField.setBackground(WARNING_COLOR);
            return null;
        }
    }

    /**
     * Find all matches and highlight it YELLOW (highlightPainter). Then
     * hihglight the match ORANGE (currentPainter) on the current position.
     * Current positions begin with -1 and are incremented or decremented by 1
     * using buttons or keys.
     */
    private void changeHighlight() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();

        findField.setBackground(Color.WHITE);
        Document doc = textArea.getDocument();
        try {
            Pattern pattern = getPattern();
            //System.out.println("patternOk: " + pattern);
            if (pattern == null) {
                return;
            }
            if (Objects.nonNull(pattern)) {
                Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
                int pos = 0;
                while (matcher.find(pos)) {
                    int start = matcher.start();
                    int end = matcher.end();
                    highlighter.addHighlight(start, end, highlightPainter);
                    pos = end;
                }
            }
            JLabel label = layerUI.hint;
            Highlighter.Highlight[] array = highlighter.getHighlights();
            int hits = array.length;
            if (hits == 0) {
                currentPos = -1;
                label.setOpaque(true);
            } else {
                currentPos = (currentPos + hits) % hits;
                label.setOpaque(false);
                Highlighter.Highlight hh = highlighter.getHighlights()[currentPos];
                highlighter.removeHighlight(hh);
                highlighter.addHighlight(hh.getStartOffset(), hh.getEndOffset(), currentPainter);
                // Remember offsets of the found text for possible later replacing
                startOffset = hh.getStartOffset();
                endOffset = hh.getEndOffset();
                scrollToCenter(textArea, startOffset);
            }
            label.setText(String.format("%02d / %02d%n", currentPos + 1, hits));
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        findField.repaint();
    }

    /**
     * Set the currently highlighted row to be seen (in the visible part of the
     * text area).
     *
     * @param textComponent
     * @param position
     * @throws BadLocationException
     */
    private static void scrollToCenter(JTextComponent textComponent, int position) throws BadLocationException {
        Rectangle rectangle = textComponent.modelToView(position);
        Container container = SwingUtilities.getAncestorOfClass(JViewport.class, textComponent);
        if (Objects.nonNull(rectangle) && container instanceof JViewport) {
            rectangle.x = (int) (rectangle.x - container.getWidth() * 0.5);
            rectangle.width = container.getWidth();
            rectangle.height = (int) (container.getHeight() * 0.5);
            textComponent.scrollRectToVisible(rectangle);
        }
    }

    /**
     * Replace all TAB characters with a number of spaces
     */
    private void replaceTabsWithSpaces() {
        String text = textArea.getText();
        text = text.replace("\t", fixedLengthSpaces(TAB_SIZE));
        textArea.setText(text);
    }

    /**
     * Create String of spaces with a given length
     *
     * @param length
     * @return
     */
    private String fixedLengthSpaces(int length) {
        char[] spaces = new char[length];
        for (int idx = 0; idx < length; idx++) {
            spaces[idx] = ' ';
        }
        return String.copyValueOf(spaces);
    }

    /**
     * Button listener for buttons Find (<,>) and Replace and Replace/Find. Note:
     * "ReplaceAll" button has different action listener.
     */
    class HighlightHandler implements DocumentListener, ActionListener {

        @Override
        public void changedUpdate(DocumentEvent de) {
            /* not needed */
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            changeHighlight();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            changeHighlight();
        }

        @Override
        public void actionPerformed(ActionEvent de) {
            Object obj = de.getSource();
            if (obj instanceof AbstractButton) {
                String cmd = ((AbstractButton) obj).getActionCommand();
                if (cmd.equals("prev")) {
                    currentPos--;
                } else if (cmd.equals("next")) {
                    currentPos++;
                } else if (cmd.equals("replace")) {
                    if (currentPos > -1) {
                        textArea.replaceRange(replaceField.getText(), startOffset, endOffset);
                        return; // Do not find next match
                    }
                } else if (cmd.equals("replaceFind")) {
                    if (currentPos > -1) {
                        textArea.replaceRange(replaceField.getText(), startOffset, endOffset);
                    }
                }
            }
            // Find next match
            changeHighlight();
        }
    }

    /**
     * Set indicator N/M that overlays the search field and is right adjusted N -
     * the sequence number of the text that is just highlighted, M - how many
     * matches were found.
     */
    class PlaceholderLayerUI extends LayerUI<JTextComponent> {

        public final JLabel hint = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                //setForeground(UIManager.getColor("TextField.inactiveForeground"));
                // The following foreground color is almost the same as "TextField.inactiveForeground"
                setForeground(DIM_BLUE); // blue little saturated dim (gray)
                setBackground(DIM_RED); // red little saturated bright
            }
        };

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (c instanceof JLayer) {
                JLayer jlayer = (JLayer) c;
                JTextComponent tc = (JTextComponent) jlayer.getView();
                if (!tc.getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(hint.getForeground());
                    Insets i = tc.getInsets();
                    Dimension d = hint.getPreferredSize();
                    int x = tc.getWidth() - i.right - d.width - 2;
                    int y = (tc.getHeight() - d.height) / 2;
                    g2.translate(x, y);
                    SwingUtilities.paintComponent(g2, hint, tc, 0, 0, d.width, d.height);
                    g2.dispose();
                }
            }
        }
    }

    class UndoHandler implements UndoableEditListener {

        /**
         * Messaged when the Document has created an edit, the edit is added to
         * "undo", an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent uee) {
            undo.addEdit(uee.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class UndoAction extends AbstractAction {

        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                // Logger.getLogger(UndoAction.class.getName()).log(Level.SEVERE, "Unable to undo", ex);
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent ae) {
            try {
                undo.redo();
            } catch (CannotRedoException cre) {
                // Logger.getLogger(RedoAction.class.getName()).log(Level.SEVERE, "Unable to redo", cre);
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
     *
     */
    protected void rewriteFile() {
        try {
            BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        pcCharset = properties.getProperty("PC_CHARSET");
        if (pcCharset.equals("*DEFAULT")) {
            pcCharset = "ISO-8859-1";
        }

        overWriteFile = properties.getProperty("OVERWRITE_FILE");

        if (overWriteFile.equals("Y")) {
            if (methodName.equals("rewriteIfsFile")) {
                rewriteIfsFile();
                displayIfsFile(true);
            } else if (methodName.equals("rewriteSourceMember")) {
                // Save edited data from text area back to the member
                rewriteSourceMember();
                displaySourceMember(true);
            } else if (methodName.equals("rewritePcFile")) {
                // Save edited data from text area back to the member
                rewritePcFile();
                displayPcFile(true);
            }
        } else {
            row = "Error: IFS file  " + filePathString
                    + "  cannot be rewtitten. Overwriting files is not allowed.";
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
        }
    }

    /**
     * Rewrite IFS file with edited text area.
     *
     * @return
     */
    protected String rewriteIfsFile() {
        try {
            IFSFileOutputStream outStream = new IFSFileOutputStream(remoteServer, filePathString);

            String textAreaString = textArea.getText();
            byte[] byteArray;
            AS400Text textConverter = new AS400Text(textAreaString.length(), attributeCCSID, remoteServer);
            byteArray = textConverter.toBytes(textAreaString);
            // Write text from  the text area to the file
            outStream.write(byteArray);
            // Close file
            outStream.close();

            row = "Comp: IFS file  " + filePathString + "  was saved.";
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
            return "";

        } catch (Exception exc) {
            exc.printStackTrace();
            row = "Error: " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
            return "ERROR";
        }
    }

    /**
     * Rewrite PC file with edited text area.
     *
     * @return
     */
    protected String rewritePcFile() {

        Path filePath = Paths.get(filePathString);

        try {
            // Open output text file
            BufferedWriter outputFile = Files.newBufferedWriter(filePath, Charset
                    .forName(pcCharset), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            // Write contents of data area to the file
            outputFile.write(textArea.getText());
            // Close file
            outputFile.close();

            row = "Comp: PC file  " + filePathString + "  was saved with charset  " + pcCharset + ".";
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
            return "";

        } catch (Exception exc) {
            exc.printStackTrace();
            row = "Error: " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
            return "ERROR";
        }
    }

    /**
     * Rewrite source member with edited text area.
     *
     * @return
     */
    protected String rewriteSourceMember() {

        // Extract individual names (libraryName, fileName, memberName) from the AS400 IFS path
        extractNamesFromIfsPath(filePathString);

        // Path to the output source member
        String outMemberPath = "/QSYS.LIB/" + libraryName + ".LIB/" + fileName + ".FILE" + "/"
                + memberName + ".MBR";
        String clrPfmCommand;

        // Enable calling CL commands
        CommandCall cmdCall = new CommandCall(remoteServer);

        msgText = "";

        try {

            // If overwrite is not allowed - return
            // ------------------------------------
            if (!properties.getProperty("OVERWRITE_FILE").equals("Y")) {
                row = "Info: Member  " + libraryName + "/" + fileName + "(" + memberName
                        + ")   cannot be overwtitten. " + " Overwriting files is not allowed.";
                mainWindow.msgVector.add(row);
                mainWindow.reloadRightSideAndShowMessages();
                return "ERROR";
            }

            // Overwrite is allowed
            // --------------------
            String[] lines = textArea.getText().split("\n");

            // Obtain output database file record description
            AS400FileRecordDescription outRecDesc = new AS400FileRecordDescription(remoteServer, outMemberPath);
            // Retrieve record format from the record description
            RecordFormat[] format = outRecDesc.retrieveRecordFormat();
            // Obtain output record object
            Record outRecord = new Record(format[0]);
            if (lines.length > 0) {

                // Create the member (SequentialFile object)
                outSeqFile = new SequentialFile(remoteServer, outMemberPath);

                // Clear physical file member
                clrPfmCommand = "CLRPFM FILE(" + libraryName + "/" + fileName + ") MBR(" + memberName + ")";
                cmdCall.run(clrPfmCommand);

                // Set the record format (the only one)
                outSeqFile.setRecordFormat(format[0]);

                try {
                    outSeqFile.open();
                } catch (com.ibm.as400.access.AS400Exception as400exc) {

                    // Add new member if open could not be performed (when the member does not exist)
                    // (the second parameter is a text description)
                    // The new member inherits the CCSID from its parent Source physical file
                    outSeqFile.addPhysicalFileMember(memberName, "Source member " + memberName);
                    // Open the new member
                    outSeqFile.open(AS400File.WRITE_ONLY, 100000, AS400File.COMMIT_LOCK_LEVEL_NONE);
                }

                // Member records contain sequence and data fields
                // -----------------------------------------------
                // Get lengths of three fields of the source record
                int lenSEQ = format[0].getFieldDescription("SRCSEQ").getLength();
                int lenDAT = format[0].getFieldDescription("SRCDAT").getLength();
                int lenDTA = format[0].getFieldDescription("SRCDTA").getLength();

                BigDecimal seqNumber = new BigDecimal("0000.00");
                BigDecimal increment = new BigDecimal("0001.00");

                String dataLine;
                // Process all lines
                for (int idx = 0; idx < lines.length; idx++) {
                    //System.out.println("0'" + lines[idx] + "'");
                    if (lines[idx].equals("\n")) {

                        //System.out.println("1'" + lines[idx] + "'");
                        dataLine = " ";
                    } else {

                        //System.out.println("2'" + lines[idx] + "'");
                        dataLine = lines[idx].replace("\r", "");
                    }

                    seqNumber = seqNumber.add(increment);
                    // Insert sequential number into the source record (zoned decimal, 2 d.p.)
                    outRecord.setField("SRCSEQ", seqNumber);

                    // Get actual date and transform it to YYMMDD
                    LocalDate date = LocalDate.now();
                    int intYear = date.getYear();
                    int intMonth = date.getMonthValue();
                    int intDay = date.getDayOfMonth();
                    String strYear = String.valueOf(intYear);
                    String strMonth = String.valueOf(intMonth);
                    if (intMonth < 10) {
                        strMonth = "0" + strMonth;
                    }
                    String strDay = String.valueOf(intDay);
                    if (intDay < 10) {
                        strDay = "0" + strDay;
                    }
                    // Insert today's date YYMMDD into the source record (zoned decimal, 0 d.p.)
                    outRecord.setField("SRCDAT", new BigDecimal(strYear.substring(2) + strMonth + strDay));

                    // Adjust data line obtained from 
                    int dataLength;
                    if (dataLine.length() >= lenDTA) {
                        // Shorten the data line to fit the data field in the record
                        dataLength = lenDTA;
                    } else {
                        // Pad the data line with spaces to fit the data field in the record
                        char[] chpad = new char[lenDTA - dataLine.length()];
                        for (int jdx = 0; jdx < chpad.length; jdx++) {
                            chpad[jdx] = ' '; // pad the data line with spaces
                            //System.out.println("chpad["+jdx+"]: " + chpad[jdx]);
                        }
                        dataLine = dataLine + String.valueOf(chpad);
                        dataLength = lenDTA;
                    }
                    //System.out.println("3'" + dataLine + "'");
                    //System.out.println("dataLength: " + dataLength);
                    //System.out.println(dataLine.substring(lenSEQ + lenDAT, lenSEQ + lenDAT + dataLength));

                    // Insert data into the source record
                    //                    outRecord.setField("SRCDTA", dataLine.substring(lenSEQ + lenDAT, lenSEQ + lenDAT + dataLength));
                    outRecord.setField("SRCDTA", dataLine.substring(0, dataLength));

                    try {
                        // Write source record
                        outSeqFile.write(outRecord);

                    } catch (Exception exc) {
                        exc.printStackTrace();
                        row = "Error: 1 Data cannot be written to the source member  " + libraryName + "/"
                                + fileName + "(" + memberName + ")  -  " + exc.toString();
                        mainWindow.msgVector.add(row);
                        mainWindow.reloadRightSideAndShowMessages();
                        msgText = "ERROR";
                        break;
                    }
                }
                // Close file
                outSeqFile.close();

                row = "Comp: Source member  " + libraryName + "/" + fileName + "(" + memberName
                        + ")  was saved.";
                mainWindow.msgVector.add(row);
                mainWindow.reloadRightSideAndShowMessages();
                return "";
            }
        } catch (Exception exc) {
            try {
                outSeqFile.close();
            } catch (Exception exce) {
                exce.printStackTrace();
            }
            exc.printStackTrace();
            row = "Error: 3 Data cannot be written to the source member  " + libraryName + "/"
                    + fileName + "(" + memberName + ")  -  " + exc.toString();
            mainWindow.msgVector.add(row);
            mainWindow.reloadRightSideAndShowMessages();
            return "ERROR"; // Must not continue in order not to lock an object
        }
        return "";
    }

    /**
     * Inner class for Ctrl + S (Save) function key
     */
    class SaveAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            // Save edited data from text area back to the member
            rewriteFile();
        }
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
            libraryName = as400PathString.substring(as400PathString.indexOf("/QSYS.LIB/")
                    + 10, as400PathString.lastIndexOf(".LIB"));
            if (as400PathString.length() > qsyslib.length() + libraryName.length() + 5) {
                fileName = as400PathString.substring(qsyslib.length() + libraryName.length()
                        + 5, as400PathString.lastIndexOf(".FILE"));
                if (as400PathString.length() > qsyslib.length() + libraryName.length() + 5
                        + fileName.length() + 6) {
                    memberName = as400PathString.substring(qsyslib.length() + libraryName.length() + 5
                            + fileName.length() + 6, as400PathString.lastIndexOf(".MBR"));
                }
            }
        }
    }

    /**
     * Inner class for Compile button
     */
    class CompileListener extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (filePathString.startsWith("/QSYS.LIB")) {
                if (mainWindow.compile == null) {
                    mainWindow.compile = new Compile(remoteServer, mainWindow, filePathString, false);
                }
                // "false" means NOT IFS file (it is a source member)
                mainWindow.compile.compile(filePathString, false);

            } else {
                if (mainWindow.compile == null) {
                    mainWindow.compile = new Compile(remoteServer, mainWindow, filePathString, true);
                }
                // "true" means IFS file
                mainWindow.compile.compile(filePathString, true);
            }
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
     * Inner class for Ctrl + Arrow Up function key
     */
    class ArrowUp extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            currentPos--;
            changeHighlight();
        }
    }

    /**
     * Inner class for Ctrl + Arrow Down function key
     */
    class ArrowDown extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            currentPos++;
            changeHighlight();
        }
    }

    /**
     * Inner class for Ctrl + Arrow Left function key
     */
    class ArrowLeft extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            shiftLeft();
            if (!progLanguage.equals("*NONE")) {
                highlightBlocks(progLanguage);
            }
        }
    }

    /**
     *
     */
    protected void shiftLeft() {
        selectedText = textArea.getSelectedText();
        selectionStart = textArea.getSelectionStart();
        int numberOfLines = 0;
        if (selectedText != null) {
            String[] strArr = selectedText.split("\n");
            int minPos = 10000;
            if (strArr.length > 0) {
                for (int idx = 0; idx < strArr.length; idx++) {
                    if (!strArr[idx].isEmpty()) {
                        int position = 0;
                        //minPos = strArr[idx].length();
                        for (position = 0; position < strArr[idx].length(); position++) {
                            if (!strArr[idx].isEmpty()) {
                                if (strArr[idx].charAt(position) != ' ') {
                                    if (position < minPos) {
                                        minPos = position;
                                    }
                                }
                            }
                        }
                    }
                }
                int numberOfEmptyLines = 0;
                shiftedText = "";
                if (minPos > 0) {
                    for (numberOfLines = 0; numberOfLines < strArr.length; numberOfLines++) {
                        if (!strArr[numberOfLines].isEmpty()) {
                            strArr[numberOfLines] = strArr[numberOfLines].substring(1);
                            shiftedText += strArr[numberOfLines] + "\n";
                        } else {
                            numberOfEmptyLines += 2;
                            shiftedText += " \n"; // 2 characters added
                        }
                    }
                    if (!selectedText.endsWith("\n")) {
                        shiftedText = shiftedText.substring(0, shiftedText.length() - 1);
                    }
                    textArea.replaceSelection(shiftedText);
                }
                if (!progLanguage.equals("*NONE")) {
                    highlightBlocks(progLanguage);
                }
                // Select shifted text
                textArea.select(selectionStart, selectionStart + shiftedText.length());
            }
        }
    }



    /**
     * Inner class for Ctrl + Arrow Left function key
     */
    class ArrowRight extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            shiftRight();
            textArea.requestFocusInWindow();
        }
    }

    /**
     *
     */
    protected void shiftRight() {
        selectedText = textArea.getSelectedText();
        selectionStart = textArea.getSelectionStart();
        int lineNbr = 0;
        char[] charArr = new char[1];
        Arrays.fill(charArr, ' ');

        if (selectedText != null) {
            String[] strArr = selectedText.split("\n");
            String[] lines = new String[strArr.length];
            shiftedText = "";
            for (lineNbr = 0; lineNbr < strArr.length; lineNbr++) {
                lines[lineNbr] = String.valueOf(charArr) + strArr[lineNbr].substring(0, strArr[lineNbr].length());
                shiftedText += lines[lineNbr] + "\n";
            }
            if (!selectedText.endsWith("\n")) {
                shiftedText = shiftedText.substring(0, shiftedText.length() - 1);
            }
            textArea.replaceSelection(shiftedText);
            if (!progLanguage.equals("*NONE")) {
                highlightBlocks(progLanguage);
            }
            // Select shifted text
            textArea.select(selectionStart, selectionStart + shiftedText.length());
        }
    }

    /**
     * Inner class for Enter key
     */
    class EnterKey extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            changeHighlight();
        }
    }

    /**
     * Inner class for Tab function key. Inserts TAB_SIZE spaces in caret
     * position
     */
    class TabListener extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            textArea.insert(fixedLengthSpaces(TAB_SIZE), textArea.getCaretPosition());
        }
    }

    /**
     * Implements custom caret as a long vertical line with a short red line
     * pointer
     */
    public class CustomCaret extends DefaultCaret {

        int oldDot;

        protected void damage(Rectangle r) {
            // give values to x,y,width,height (inherited from java.awt.Rectangle)
            x = r.x;
            y = 0; // upper edge of the vertical line is at the upper edge of the text area
            height = textArea.getHeight();
            width = 2;

            repaint(); // calls getComponent().repaint(x, y, width, height)
        }

        public void paint(Graphics g) {

            JTextComponent component = getComponent();
            int dot = getDot();
            Rectangle verticalLine = null;
            try {
                verticalLine = component.modelToView(dot);
            } catch (BadLocationException e) {
                return;
            }
            if (isVisible()) {
                // The long vertical line will be light gray
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(verticalLine.x, 0, width, textArea.getHeight());
                // The short line segment of the caret in the y position will be red
                g.setColor(Color.RED);
                g.fillRect(verticalLine.x, verticalLine.y, width, fontSize);
            }
        }
    }
}
