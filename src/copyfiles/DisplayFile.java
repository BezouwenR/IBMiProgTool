package copyfiles;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.AS400Text;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import java.awt.Color;
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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * Display file - PC file, IFS file, Source Member, Spooled file.
 * 
 * @author Vladimír Župka, 2016
 */
public final class DisplayFile extends JFrame {

   public JTextArea textArea = new JTextArea();

   final Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.020f, 0.99f);
   final Color VERY_LIGHT_GREEN = Color.getHSBColor(0.52f, 0.020f, 0.99f);
   final Color VERY_LIGHT_PINK = Color.getHSBColor(0.025f, 0.008f, 0.99f);

   
   final Color WARNING_COLOR = new Color(255, 200, 200);
   final Color VERY_LIGHT_GRAY = Color.getHSBColor(0.50f, 0.01f, 0.90f); 

   Highlighter.HighlightPainter currentPainter = new DefaultHighlighter.DefaultHighlightPainter(
         Color.ORANGE);
   Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
         Color.YELLOW);

   JLabel characterSetLabel = new JLabel();

   JLabel fontSizeLabel = new JLabel("Font size:");
   JTextField fontSizeField = new JTextField();

   JLabel searchLabel = new JLabel("Search:");
   JTextField searchField = new JTextField();
   JLayer fieldLayer;

   JButton prevButton = new JButton("<"); // character "arrow up"
   JButton nextButton = new JButton(">"); // character "arrow down"

   JCheckBox checkCaseBox = new JCheckBox("Match case");

   PlaceholderLayerUI layerUI = new PlaceholderLayerUI();
   transient HighlightHandler highlightHandler = new HighlightHandler();
   int current; // current sequence number

   MainWindow mainWindow;
   static int windowWidth;
   static int windowHeight;
   int screenWidth;
   int screenHeight;
   int windowX;
   int windowY;

   Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
   String encoding = System.getProperty("file.encoding", "UTF-8");
   final String PROP_COMMENT = "Copy files between IBM i and PC, edit and compile.";
   Properties properties;
   String pcCharset;
   String ibmCcsid;
   int ibmCcsidInt;
   String fontSizeString;
   int fontSize;

   JPanel globalPanel;

   String row;
   boolean nodes = true;
   boolean noNodes = false;

   GroupLayout globalPanelLayout;
   JScrollPane scrollPane;
   
   final String NEW_LINE = "\n";

   /**
    * Constructor
    * 
    * @param mainWindow
    */
   @SuppressWarnings("OverridableMethodCallInConstructor")
   public DisplayFile(MainWindow mainWindow) {

      this.mainWindow = mainWindow;

      properties = new Properties();
      try {
         BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
         properties.load(infile);
         infile.close();
      } catch (Exception exc) {
         exc.printStackTrace();
      }

      pcCharset = properties.getProperty("PC_CHARSET");
      if (!pcCharset.equals("*DEFAULT")) {
         // Check if charset is valid
         try {
            Charset.forName(pcCharset);
         } catch (IllegalCharsetNameException | UnsupportedCharsetException charset) {
            // If pcCharset is invalid, take ISO-8859-1
            pcCharset = "ISO-8859-1";
         }
      }

      ibmCcsid = properties.getProperty("IBM_CCSID");
      if (!ibmCcsid.equals("*DEFAULT")) {
         try {
            ibmCcsidInt = Integer.parseInt(ibmCcsid);
         } catch (Exception exc) {
            exc.printStackTrace();
            ibmCcsid = "819";
            ibmCcsidInt = 819;
         }
      }

      fontSizeString = properties.getProperty("FONT_SIZE");
      try {
         fontSize = Integer.parseInt(fontSizeString);
      } catch (Exception exc) {
         exc.printStackTrace();
         fontSizeString = "12";
         fontSize = 12;
      }

      fontSizeField.setText(fontSizeString);
      fontSizeField.setPreferredSize(new Dimension(25, 20));
      fontSizeField.setMaximumSize(new Dimension(25, 20));

      // Adjust the text area
      textArea.setEditable(false);
      textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
      
      // Create scroll pane with the text area inside
      scrollPane = new JScrollPane(textArea);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      // Light sky blue
      scrollPane.setBackground(VERY_LIGHT_BLUE);
      textArea.setBackground(VERY_LIGHT_BLUE);

      Toolkit kit = Toolkit.getDefaultToolkit();
      Dimension screenSize = kit.getScreenSize();
      screenWidth = screenSize.width;
      screenHeight = screenSize.height;
      windowWidth = 980;
      windowHeight = screenHeight;

      windowX = screenWidth / 2 - windowWidth / 2;
      windowY = 10;

      prevButton.setPreferredSize(new Dimension(40, 20));
      prevButton.setMinimumSize(new Dimension(40, 20));
      prevButton.setMaximumSize(new Dimension(40, 20));
      prevButton.setActionCommand("prev");

      nextButton.setPreferredSize(new Dimension(40, 20));
      nextButton.setMinimumSize(new Dimension(40, 20));
      nextButton.setMaximumSize(new Dimension(40, 20));
      nextButton.setActionCommand("next");

      checkCaseBox.setHorizontalTextPosition(SwingConstants.LEFT);

      searchField.setPreferredSize(new Dimension(300, 20));
      searchField.setMaximumSize(new Dimension(300, 20));

      // Set document listener for the search field
      searchField.getDocument().addDocumentListener(highlightHandler);

      // Set action listener for buttons and check boxes
      Arrays.asList(nextButton, prevButton, checkCaseBox).stream()
            .map((abstractButton) -> {
               abstractButton.setFocusable(false);
               return abstractButton;
            }).forEachOrdered((abstractButton) -> {
               abstractButton.addActionListener(highlightHandler);
            });

      // Set a layer of counts that overlay the search field:
      // - the sequence number of just highlighted text found
      // - how many matches were found
      fieldLayer = new JLayer<>(searchField, layerUI);

      globalPanel = new JPanel();
      // Lay out components in globalPanel
      globalPanelLayout = new GroupLayout(globalPanel);
      globalPanelLayout.setAutoCreateGaps(true);
      globalPanelLayout.setAutoCreateContainerGaps(true);
      GroupLayout.SequentialGroup sg1 = globalPanelLayout.createSequentialGroup()
            .addComponent(characterSetLabel);

      GroupLayout.SequentialGroup sg2 = globalPanelLayout.createSequentialGroup()
            .addComponent(searchLabel).addComponent(fieldLayer).addComponent(prevButton)
            .addComponent(nextButton).addComponent(checkCaseBox).addComponent(fontSizeLabel)
            .addComponent(fontSizeField);

      globalPanelLayout.setHorizontalGroup(globalPanelLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(sg1).addGroup(sg2)
            .addGroup(globalPanelLayout.createSequentialGroup().addComponent(scrollPane)));

      GroupLayout.ParallelGroup pg1 = globalPanelLayout
            .createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(characterSetLabel);

      GroupLayout.ParallelGroup pg2 = globalPanelLayout
            .createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(searchLabel)
            .addComponent(fieldLayer).addComponent(prevButton).addComponent(nextButton)
            .addComponent(checkCaseBox).addComponent(fontSizeLabel).addComponent(fontSizeField);
      
      globalPanelLayout
            .setVerticalGroup(globalPanelLayout.createSequentialGroup().addGroup(pg1).addGroup(pg2)
                  .addGroup(globalPanelLayout.createParallelGroup().addComponent(scrollPane)));
      
      globalPanel.setLayout(globalPanelLayout);
      globalPanel.setBackground(VERY_LIGHT_GRAY);
      
      scrollPane.setPreferredSize(new Dimension(windowWidth, windowHeight - 140));
      scrollPane.setBorder(BorderFactory.createEmptyBorder());

      // Enable processing of function keyCtrl + Arrow UP = Find next hit upwards
      searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "arrowUp");
      searchField.getActionMap().put("arrowUp", new ArrowUp());

      // Enable processing of function key Ctrl + Arrow DOWN =  Find next hit downwards
      searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "arrowDown");
      searchField.getActionMap().put("arrowDown", new ArrowDown());

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

      Container cont = getContentPane();
      cont.add(globalPanel);

      // Display the window.
      setSize(windowWidth, windowHeight);
      setLocation(windowX, windowY);

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      pack();
      
      searchField.requestFocusInWindow(); 
   }

   /**
    * Display contents of the IFS file using its CCSID attribute
    * 
    * @param remoteServer
    * @param ifsFilePathString
    */
   protected void displayIfsFile(AS400 remoteServer, String ifsFilePathString) {

      this.setTitle("Display IFS file " + ifsFilePathString);

      // Contents of the file are always decoded according to its attributed CCSID.
      // Characters may be displayed incorrectly if the "IBMi CCSID" parameter
      // does not correspond to the file's attributed CCSID.
      // The user can correct the parameter "IBMi CCSID" and try again.
      try {
         IFSFile ifsFile = new IFSFile(remoteServer, ifsFilePathString);
         int attributeCCSID = ifsFile.getCCSID();

         characterSetLabel.setText("CCSID " + attributeCCSID + " was used for display.");

         byte[] inputBuffer = new byte[100000];
         byte[] workBuffer = new byte[100000];

         try (IFSFileInputStream inputStream = new IFSFileInputStream(remoteServer,
               ifsFilePathString)) {
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
            // Set scroll bar to top
            textArea.setCaretPosition(0);
            // Display the window.
            setVisible(true);
            row = "Info: IFS file  " + ifsFilePathString + "  has CCSID  " + attributeCCSID + ".";
            mainWindow.msgVector.add(row);
            mainWindow.reloadLeftSideAndShowMessages(nodes);
         }
      } catch (Exception exc) {
         exc.printStackTrace();
         row = "Error: " + exc.toString();
         mainWindow.msgVector.add(row);
         mainWindow.reloadLeftSideAndShowMessages(nodes);
      }
      // Remove message scroll listener (cancel scrolling to the last message)
      mainWindow.scrollMessagePane.getVerticalScrollBar()
            .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
   }

   /**
    * Display text area
    * 
    * @param aTextArea
    */
   protected void displayTextArea(JTextArea aTextArea, String ibmCcsid) {

      characterSetLabel.setText("CCSID " + ibmCcsid + " was used for display.");

      // Copy text area from parameter to instance text area
      textArea.append(aTextArea.getText());

      scrollPane.setBackground(VERY_LIGHT_GREEN);
      textArea.setBackground(VERY_LIGHT_GREEN);

      // Set scroll bar to top
      textArea.setCaretPosition(0);
      setLocation(windowX + 100, windowY);
      // Display the window.
      setVisible(true);
   }

   /**
    * Display PC file using the application parameter "pcCharset".
    * 
    * @param pcPathString
    */
   protected void displayPcFile(String pcPathString) {

      this.setTitle("Display PC file " + pcPathString);

      try {
         Path filePath = Paths.get(pcPathString);
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

         scrollPane.setBackground(VERY_LIGHT_PINK);
         textArea.setBackground(VERY_LIGHT_PINK);

         // Set scroll bar to top
         textArea.setCaretPosition(0);

         setLocation(windowX - 100, windowY);

         // Display the window.
         setVisible(true);
         row = "Info: PC file  " + pcPathString + "  is displayed using character set  " + pcCharset
               + "  from the application parameter.";
         mainWindow.msgVector.add(row);
         mainWindow.reloadLeftSideAndShowMessages(nodes);
         // Remove message scroll listener (cancel scrolling to the last
         // message)
         mainWindow.scrollMessagePane.getVerticalScrollBar()
               .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
      } catch (Exception exc) {
         exc.printStackTrace();
         row = "Error: Displaying PC file:  " + pcPathString
               + "  is not a text file or has an unsuitable character set.  -  " + exc.toString();
         mainWindow.msgVector.add(row);
         mainWindow.reloadLeftSideAndShowMessages(nodes); // do not add child nodes
         // Remove message scroll listener (cancel scrolling to the last message)
         mainWindow.scrollMessagePane.getVerticalScrollBar()
               .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
         return;
      }
   }

   /**
    * Display source member using its CCSID attribute; Only data part of the
    * source record is translated (to String - UTF-16).
    * 
    * @param remoteServer
    * @param as400PathString
    */
   protected void displaySourceMember(AS400 remoteServer, String as400PathString) {

      this.setTitle("Display member " + as400PathString);

      IFSFile ifsFile = new IFSFile(remoteServer, as400PathString);
      // Create an AS400FileRecordDescription object that represents the file
      AS400FileRecordDescription inRecDesc = new AS400FileRecordDescription(remoteServer,
            as400PathString);

      // Set editability
      textArea.setEditable(false);

      try {
         // Decide what CCSID is appropriate for displaying the member
         int ccsidAttribute = ifsFile.getCCSID();
         characterSetLabel.setText("CCSID " + ccsidAttribute + " was used for display.");

         // Get list of record formats of the database file
         RecordFormat[] format = inRecDesc.retrieveRecordFormat();
         // Create an AS400File object that represents the file
         SequentialFile as400seqFile = new SequentialFile(remoteServer, as400PathString);
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

            // Source record is composed of three source record fields: seq.
            // number, date, source data.
            DecimalFormat df1 = new DecimalFormat("0000.00");
            DecimalFormat df2 = new DecimalFormat("000000");

            // Sequence number - 6 bytes
            String seq = df1.format((Number) inRecord.getField("SRCSEQ"));
            String seq2 = seq.substring(0, 4) + seq.substring(5);
            textLine.append(seq2);
            // Date - 6 bytes
            String srcDat = df2.format((Number) inRecord.getField("SRCDAT"));
            // textLine.append(srcDat);
            textLine.append(srcDat);
            // Data from source record (the source line)
            byte[] bytes = inRecord.getFieldAsBytes("SRCDTA");

            // Create object for conversion from bytes to characters
            // Ignore "IBM i CCSID" parameter - display characters in the
            // member.
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
         // Set scroll bar to top
         textArea.setCaretPosition(0);

         // Display the window.
         setVisible(true);
         row = "Info: Source member  " + as400PathString + "  has CCSID  " + ccsidAttribute + ".";
         mainWindow.msgVector.add(row);
         mainWindow.reloadLeftSideAndShowMessages(nodes);
      } catch (Exception exc) {
         exc.printStackTrace();
         row = "Error: " + exc.toString();
         mainWindow.msgVector.add(row);
         mainWindow.reloadLeftSideAndShowMessages(nodes);
      }
      // Remove message scroll listener (cancel scrolling to the last message)
      mainWindow.scrollMessagePane.getVerticalScrollBar()
            .removeAdjustmentListener(mainWindow.messageScrollPaneAdjustmentListenerMax);
   }

   /**
    * 
    * @param textComponent
    * @param position
    * @throws BadLocationException
    */
   private static void scrollToCenter(JTextComponent textComponent, int position)
         throws BadLocationException {
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
    * 
    * @return
    */
   private Pattern getPattern() {
      String pattern = searchField.getText();

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
         int flags = checkCaseBox.isSelected() ? 0
               : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
         return Pattern.compile(pattern, flags);
      } catch (PatternSyntaxException ex) {
         searchField.setBackground(WARNING_COLOR);
         return null;
      }
   }

   /**
   
    */
   private void changeHighlight() {
      searchField.setBackground(Color.WHITE);
      Highlighter highlighter = textArea.getHighlighter();
      highlighter.removeAllHighlights();
      Document doc = textArea.getDocument();
      try {
         Pattern pattern = getPattern();
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
            current = -1;
            label.setOpaque(true);
         } else {
            current = (current + hits) % hits;
            label.setOpaque(false);
            Highlighter.Highlight hh = highlighter.getHighlights()[current];
            highlighter.removeHighlight(hh);
            highlighter.addHighlight(hh.getStartOffset(), hh.getEndOffset(), currentPainter);
            scrollToCenter(textArea, hh.getStartOffset());
         }
         label.setText(String.format("%02d / %02d%n", current + 1, hits));
      } catch (BadLocationException ex) {
         ex.printStackTrace();
      }
      searchField.repaint();
   }

   /**
   
    */
   class HighlightHandler implements DocumentListener, ActionListener {

      @Override
      public void changedUpdate(DocumentEvent de) {
         /* not needed */ }

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
            if ("prev".equals(cmd)) {
               current--;
            } else if ("next".equals(cmd)) {
               current++;
            }
         }
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
            // setForeground(UIManager.getColor("TextField.inactiveForeground"));

            // blue little saturated dim (gray)            
            setForeground(Color.getHSBColor(0.60f, 0.2f, 0.5f));
            // red little saturated bright            
            setBackground(Color.getHSBColor(0.00f, 0.2f, 0.98f));
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

   /**
    * Inner class for Ctrl + Arrow Up function key
    */
   class ArrowUp extends AbstractAction {

      @Override
      public void actionPerformed(ActionEvent de) {
         current--;
         changeHighlight();
      }
   }

   /**
    * Inner class for Ctrl + Arrow Down function key
    */
   class ArrowDown extends AbstractAction {

      @Override
      public void actionPerformed(ActionEvent de) {
         current++;
         changeHighlight();
      }
   }

}
