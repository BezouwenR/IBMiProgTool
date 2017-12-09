package copyfiles;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * This class enables finding and replacing text in the editor.
 *
 * @author Vladimír Župka 2016
 */
public class FindWindow extends JFrame {

    static final Color DIM_BLUE = Color.getHSBColor(0.60f, 0.2f, 0.5f); // blue little saturated dim (gray)
    static final Color DIM_RED = Color.getHSBColor(0.00f, 0.2f, 0.98f); // red little
    static final Color WARNING_COLOR = new Color(255, 200, 200);

    Path prevIconPath = Paths.get(System.getProperty("user.dir"), "workfiles", "prev.png");
    Path nextIconPath = Paths.get(System.getProperty("user.dir"), "workfiles", "next.png");
    Path matchCaseIconPathDark = Paths.get(System.getProperty("user.dir"), "workfiles", "matchCase1.png");
    Path matchCaseIconPathDim = Paths.get(System.getProperty("user.dir"), "workfiles", "matchCase2.png");

    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");

    Container cont;
    JLabel findLabel = new JLabel("Find what:");
    JTextField findField = new JTextField();
    JLayer fieldLayer;

    PlaceholderLayerUI layerUI = new PlaceholderLayerUI();

    JLabel replaceLabel = new JLabel("Replace with:");
    JTextField replaceField = new JTextField();
    JButton cancelButton = new JButton("Cancel");
    JButton replaceButton = new JButton("Replace");
    JButton replaceFindButton = new JButton("Replace+Find");
    JButton replaceAllButton = new JButton("Replace All");

    boolean wasReplace = false;

    JPanel colPanel1;
    JPanel colPanel2;
    JPanel colPanel21;
    JPanel colPanel22;
    JPanel rowPanel2;
    JPanel rowPanel3;
    JPanel panel;

    int windowWidth = 385;
    int windowHeight = 130;

    EditFile editFile;

    // Icon "Aa" will toggle dim or dark when clicked
    ImageIcon matchCaseIconDark = new ImageIcon(matchCaseIconPathDark.toString());
    ImageIcon matchCaseIconDim = new ImageIcon(matchCaseIconPathDim.toString());
    // Icon "Left arrow" 
    ImageIcon prevImageIcon = new ImageIcon(prevIconPath.toString());
    // Icon "Right arrow"
    ImageIcon nextImageIcon = new ImageIcon(nextIconPath.toString());

    JButton prevButton = new JButton(prevImageIcon);
    JButton nextButton = new JButton(nextImageIcon);
    JToggleButton matchCaseButton = new JToggleButton();

    String pathString;

    /**
     * Constructor
     *
     * @param editFile - object of the editor
     * @param pathString
     */
    public FindWindow(EditFile editFile, String pathString) {

        super.setTitle(pathString.substring(pathString.lastIndexOf("/") + 1));

        this.editFile = editFile;
        this.pathString = pathString;

        // Create an icon (left arrow),
        // then create the button and set the icon to it
        prevButton.setToolTipText("Previous match. Also Ctrl+⬆ (Cmd+⬆ in macOS).");
        prevButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        prevButton.setContentAreaFilled(false);
        prevButton.setPreferredSize(new Dimension(25, 20));
        //prevButton.setMinimumSize(new Dimension(20, 20));
        //prevButton.setMaximumSize(new Dimension(20, 20));

        // Create an icon (right arrow),
        // then create the button and set the icon to it
        nextButton.setToolTipText("Next match. Also Ctrl+⬇ (Cmd+⬇ in macOS).");
        nextButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        nextButton.setContentAreaFilled(false);
        nextButton.setPreferredSize(new Dimension(25, 20));

        matchCaseButton.setIcon(matchCaseIconDim);
        matchCaseButton.setSelectedIcon(matchCaseIconDim);
        matchCaseButton.setToolTipText("Case insensitive. Toggle Match case.");
        matchCaseButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        matchCaseButton.setContentAreaFilled(false);
        matchCaseButton.setPreferredSize(new Dimension(25, 20));

        // This window will be allways on top.
        setAlwaysOnTop(true);

        findField.requestFocus();
        findField.setPreferredSize(new Dimension(200, 20));
        findField.setMaximumSize(new Dimension(200, 20));
        // Set document listener for the search field
        findField.getDocument().addDocumentListener(editFile.highlightHandler);
        findField.setToolTipText("Enter text to find.");

        // Set a layer of counts that overlay the find field:
        // - the sequence number of just highlighted text found
        // - how many matches were found
        fieldLayer = new JLayer<>(findField, layerUI);

        replaceField.setPreferredSize(new Dimension(200, 20));
        replaceField.setMaximumSize(new Dimension(200, 20));
        replaceField.setToolTipText("Enter replacement text.");

        colPanel1 = new JPanel();
        colPanel1.setLayout(new BoxLayout(colPanel1, BoxLayout.Y_AXIS));
        colPanel1.add(findLabel);
        colPanel1.add(Box.createVerticalStrut(5));
        colPanel1.add(replaceLabel);

        colPanel2 = new JPanel();
        colPanel2.setLayout(new BoxLayout(colPanel2, BoxLayout.Y_AXIS));

        colPanel21 = new JPanel();
        colPanel22 = new JPanel();
        colPanel21.setLayout(new BoxLayout(colPanel21, BoxLayout.X_AXIS));

        colPanel21.add(fieldLayer);
        colPanel21.add(prevButton);
        colPanel21.add(nextButton);
        colPanel21.add(Box.createHorizontalStrut(5));
        //colPanel21.add(matchCaseButton);
        colPanel21.add(Box.createHorizontalGlue());
        colPanel22.setLayout(new BoxLayout(colPanel22, BoxLayout.X_AXIS));
        colPanel22.add(replaceField);
        colPanel22.add(Box.createHorizontalStrut(12));
        colPanel22.add(matchCaseButton);
        colPanel22.add(Box.createHorizontalGlue());

        colPanel2.add(Box.createVerticalGlue());
        colPanel2.add(colPanel21);
        colPanel2.add(Box.createVerticalStrut(5));
        colPanel2.add(colPanel22);
        colPanel2.add(Box.createVerticalGlue());

        rowPanel2 = new JPanel();
        rowPanel2.setLayout(new BoxLayout(rowPanel2, BoxLayout.X_AXIS));
        rowPanel2.add(colPanel1);
        rowPanel2.add(colPanel2);
        rowPanel2.add(Box.createHorizontalGlue());

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());
        panel.add(rowPanel2);

        rowPanel3 = new JPanel();
        rowPanel3.setLayout(new BoxLayout(rowPanel3, BoxLayout.X_AXIS));

        cancelButton.setPreferredSize(new Dimension(70, 20));
        replaceButton.setPreferredSize(new Dimension(70, 20));
        replaceFindButton.setPreferredSize(new Dimension(100, 20));
        replaceAllButton.setPreferredSize(new Dimension(100, 20));

        rowPanel3.add(cancelButton);
        rowPanel3.add(Box.createHorizontalStrut(25));
        rowPanel3.add(replaceButton);
        rowPanel3.add(replaceFindButton);
        rowPanel3.add(replaceAllButton);
        rowPanel3.add(Box.createHorizontalGlue());

        panel.add(Box.createVerticalStrut(5));
        panel.add(rowPanel3);
        panel.add(Box.createVerticalGlue());

        add(panel);

        // "Previous" button listener
        // --------------------------
        prevButton.addActionListener(ae -> {
            if (!editFile.lowerHalfActive) {
                if (!wasReplace) {
                    editFile.currentPos--; // Find previous match
                }
                editFile.changeHighlight();
            } else {
                if (!wasReplace) {
                    editFile.currentPos2--; // Find previous match
                }
                editFile.changeHighlight2();
            }
            wasReplace = false; // Set off Replace flag
        });

        // "Next" button listener
        // ----------------------
        nextButton.addActionListener(ae -> {
            if (!editFile.lowerHalfActive) {
                editFile.currentPos++; // Find next match
                editFile.changeHighlight();
            } else {
                editFile.currentPos2++; // Find next match
                editFile.changeHighlight2();
            }
            wasReplace = false; // Set off Replace flag
        });

        // "Match case" button listener
        // ----------------------------
        matchCaseButton.addActionListener(ae -> {
            if (matchCaseButton.getSelectedIcon().equals(matchCaseIconDark)) {
                matchCaseButton.setSelectedIcon(matchCaseIconDim);
                matchCaseButton.setToolTipText("Case insensitive. Toggle Match case.");
            } else {
                matchCaseButton.setSelectedIcon(matchCaseIconDark);
                matchCaseButton.setToolTipText("Match case. Toggle Case insensitive.");
            }
            editFile.currentPos = 0;
            editFile.changeHighlight();
            editFile.currentPos2 = 0;
            editFile.changeHighlight2();
        });

        // "Cancel" button listener
        // ----------------------
        cancelButton.addActionListener(ae -> {
            // Clear input fields
            findField.setText("");
            replaceField.setText("");
            // Set off flags controlling replacing
            wasReplace = false; // Set off replace flag
            // Clear all highlights
            editFile.changeHighlight();
            if (editFile.textArea2 != null) {
                editFile.changeHighlight2();
            }
            setVisible(false);
        });

        // "Replace" button listener
        // ------------------------------
        replaceButton.addActionListener(ae -> {
            if (editFile.currentPos > -1) {
                if (!editFile.lowerHalfActive) {
                    if (!wasReplace) {
                        editFile.textArea.replaceRange(replaceField.getText(), editFile.startOffset, editFile.endOffset);
                        editFile.currentPos--; // Find previous match
                    }
                } else {
                    if (!wasReplace) {
                        editFile.textArea2.replaceRange(replaceField.getText(), editFile.startOffset2, editFile.endOffset2);
                        editFile.currentPos2--; // Find previous match
                    }
                }
            }
            wasReplace = true; // Set on replace flag - note that single replace occurred
        });

        // "Replace+Find" button listener
        // ------------------------------
        replaceFindButton.addActionListener(ae -> {
            if (editFile.currentPos > -1) {
                if (!editFile.lowerHalfActive) {
                    if (!wasReplace) {
                        editFile.textArea.replaceRange(replaceField.getText(), editFile.startOffset, editFile.endOffset);
                        if (replaceField.getText().toUpperCase().contains(findField.getText().toUpperCase())) {
                            editFile.currentPos++;
                        }
                        editFile.changeHighlight();
                    }
                } else {
                    if (!wasReplace) {
                        editFile.textArea2.replaceRange(replaceField.getText(), editFile.startOffset2, editFile.endOffset2);
                        if (replaceField.getText().toUpperCase().contains(findField.getText().toUpperCase())) {
                            editFile.currentPos++;
                        }
                        editFile.changeHighlight2();
                    }
                }
            }
        });

        // "Replace All" button listener
        replaceAllButton.addActionListener(ae -> {
            String replacement = replaceField.getText();
            ArrayList<String> arrListPattern = new ArrayList<>();
            ArrayList<Integer> arrListStart = new ArrayList<>();
            ArrayList<Integer> arrListEnd = new ArrayList<>();
            Highlighter highlighter = editFile.textArea.getHighlighter();
            highlighter.removeAllHighlights();
            try {
                String text = editFile.textArea.getText();
                String pattern = findField.getText();
                int patternLen = pattern.length();
                int start = 1;
                int end = 0;
                while (start > 0 && end <= text.length()) {
                    start = text.toUpperCase().indexOf(findField.getText().toUpperCase(), end);
                    if (start < 0) {
                        break;
                    }
                    end = text.toUpperCase().indexOf(findField.getText().toUpperCase(), start) + patternLen;
                    // Fill array lists with: found text, start position, end position
                    arrListPattern.add(editFile.textArea.getText(start, end - start));
                    arrListStart.add(start);
                    arrListEnd.add(end);
                    highlighter.addHighlight(start, end, editFile.highlightPainter);
                    start = end;
                }
                int hits = arrListPattern.size();
                // Replace texts in intervals found by the replacement (= pattern)
                int idx = 0;
                for (idx = hits - 1; idx >= 0; --idx) {
                    editFile.textArea.replaceRange(replacement, arrListStart.get(idx), arrListEnd.get(idx));
                }
                editFile.textArea.setCaretPosition(arrListEnd.get(hits - 1));
            } catch (BadLocationException exc) {
                exc.printStackTrace();
            }
        });

        // Set input maps and actions for Ctrl + Arrow UP and Ctrl + Arrow DOWN on different buttons and text areas.
        ArrowUp arrowUp = new ArrowUp();
        ArrowDown arrowDown = new ArrowDown();
        Arrays.asList(prevButton, nextButton, findField, replaceField, replaceButton, replaceFindButton,
                editFile.textArea, editFile.textArea2).stream().map((object) -> {
                    return object;
                }).forEachOrdered((object) -> {
            // Enable processing of function key Ctrl + Arrow UP = Find next hit upwards
            object.getInputMap(JComponent.WHEN_FOCUSED)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "arrowUp");
            object.getActionMap().put("arrowUp", arrowUp);

            // Enable processing of function key Ctrl + Arrow DOWN = Find next hit downwards
            object.getInputMap(JComponent.WHEN_FOCUSED)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "arrowDown");
            object.getActionMap().put("arrowDown", arrowDown);
        });

        // Set document listener for the find field
        findField.getDocument().addDocumentListener(editFile.highlightHandler);

    }

    /**
     * Conclude creating the window when the constructor has already been performed.
     * 
     * @param searchedText
     */
    public void createWindow(String searchedText) {

        // Make window dimensions fixed.
        setResizable(false);

        // Register WindowListener for disposal of the window
        addWindowListener(new FindWindowAdapter());

        // Conclude showing window
        setSize(windowWidth, windowHeight);
        setLocation(editFile.windowX + 670, editFile.windowY);
        setVisible(true);
        // pack();

        // Set text selected from the text area (primary or secondary) into findField
        findField.setText(searchedText);
    }


    /**
     * Set indicator N/M that overlays the search field and is right adjusted N - the sequence number of the text that is
     * just highlighted, M - how many matches were found.
     */
    class PlaceholderLayerUI extends LayerUI<JTextComponent> {

        public final JLabel hint = new JLabel() {

            @Override
            public void updateUI() {
                super.updateUI();
                // setForeground(UIManager.getColor("TextField.inactiveForeground"));
                // The following foreground color is almost the same as "TextField.inactiveForeground"
                setForeground(DIM_BLUE); // blue little saturated dim - gray
                setBackground(DIM_RED); // red little saturated - bright
            }
        };

        @Override
        public void paint(Graphics g, JComponent component) {
            super.paint(g, component);
            if (component instanceof JLayer) {
                JLayer jlayer = (JLayer) component;
                JTextComponent textComponent = (JTextComponent) jlayer.getView();
                if (!textComponent.getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(hint.getForeground());
                    Insets insets = textComponent.getInsets();
                    Dimension dimension = hint.getPreferredSize();
                    int x = textComponent.getWidth() - insets.right - dimension.width - 2;
                    int y = (textComponent.getHeight() - dimension.height) / 2;
                    g2.translate(x, y);
                    SwingUtilities.paintComponent(g2, hint, textComponent, 0, 0, dimension.width, dimension.height);
                    g2.dispose();
                }
            }
        }
    }

    /**
     * Produce a Pattern object for matching in "changeHighlighter" methods (in EditFile class).
     * @return
     */
    protected Pattern getPattern() {
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
            int flags = matchCaseButton.getSelectedIcon().equals(matchCaseIconDark) ? 0
                    : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException ex) {
            findField.setBackground(WARNING_COLOR);
            return null;
        }
    }

    /**
     * Inner class for Ctrl + Arrow Up function key.
     */
    class ArrowUp extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (!editFile.lowerHalfActive) {
                editFile.currentPos--;
                editFile.changeHighlight();
            } else {
                editFile.currentPos2--;
                editFile.changeHighlight2();
            }
        }
    }

    /**
     * Inner class for Ctrl + Arrow Down function key.
     */
    class ArrowDown extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (!editFile.lowerHalfActive) {
                editFile.currentPos++;
                editFile.changeHighlight();
            } else {
                editFile.currentPos2++;
                editFile.changeHighlight2();
            }
        }
    }

    /**
     * Window adapter clears text in findField and closes the window.
     */
    class FindWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent we) {
            JFrame jFrame = (JFrame) we.getSource();
            // Clear input fields
            findField.setText("");
            replaceField.setText("");
            // Set off flags controlling replacing
            wasReplace = false; // Set off replace flag
            // Clear all highlights
            editFile.changeHighlight();
            if (editFile.textArea2 != null) {
                editFile.changeHighlight2();
            }
            jFrame.setVisible(false);
        }
    }
}
