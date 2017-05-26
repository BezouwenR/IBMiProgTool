package copyfiles;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 This class is a dialog for entering a name - new directory name, etc.

 @author Vladimír Župka 2016
 */
public class GetTextFromDialog extends JDialog {

    Container cont;
    JPanel panel = new JPanel();
//    JPanel titlePanel = new JPanel();
    JPanel dataPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    GroupLayout layout = new GroupLayout(panel);
    JLabel titleLabel = new JLabel();
    JLabel parentPathLabel = new JLabel();
    JLabel newNameLabel = new JLabel();
    JTextField textField = new JTextField();
    JButton cancel = new JButton("Cancel");
    JButton enter = new JButton("Enter");

    int windowWidth = 450;
    int windowHeight = 180;

    String returnedText;
    // "true" if the path leads to an IBM i library object to be converted to upper case
    boolean isIbmObject; 

    /**
     Constructor

     @param windowTitle
     */
    public GetTextFromDialog(String windowTitle) {
        super();
        super.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        super.setTitle(windowTitle);
    }

    public String getTextFromDialog(String parentTitle, String newNameTitle, String parentPathString, String fileName, 
            boolean isIbmObject, int currentX, int currentY) {
        this.isIbmObject = isIbmObject;
        cont = getContentPane();
        cont.add(panel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        textField.setPreferredSize(new Dimension(300, 20));
        textField.setMaximumSize(new Dimension(300, 20));
        textField.setText(fileName);

        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.LINE_AXIS));
        dataPanel.setPreferredSize(new Dimension(windowWidth - 20, 50));
        dataPanel.setMinimumSize(new Dimension(windowWidth - 20, 50));

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setPreferredSize(new Dimension(200, 30));
        buttonPanel.setMaximumSize(new Dimension(200, 30));
        buttonPanel.setMinimumSize(new Dimension(200, 30));

        titleLabel.setText(parentTitle + ": ");
        parentPathLabel.setText(parentPathString);

        newNameLabel.setText(newNameTitle + ": ");
        dataPanel.add(newNameLabel);
        dataPanel.add(textField);

        buttonPanel.add(cancel);
        buttonPanel.add(enter);

        panel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.
                                createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(titleLabel).
                                addComponent(parentPathLabel).
                                addComponent(dataPanel).
                                addComponent(buttonPanel)
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createSequentialGroup().
                                addComponent(titleLabel).
                                addComponent(parentPathLabel).
                                addComponent(dataPanel).
                                addComponent(buttonPanel)
                        )
        );

        enter.addActionListener(en -> {
            evaluateTextField();
            dispose();
        });

        textField.addActionListener(tf -> {
            evaluateTextField();
            dispose();
        });

        cancel.addActionListener(en -> {
            returnedText = null;
            dispose();
        });

        setSize(windowWidth, windowHeight);
        setLocation(currentX, currentY);
        setVisible(true);
        pack();

        return returnedText;
    }

    /**
     Special treatment of AS400 object names (Source files, members).
     */
    private void evaluateTextField() {
        if (isIbmObject) {
            returnedText = textField.getText().toUpperCase();
            if (returnedText.length() > 10) {
                returnedText = returnedText.substring(0, 10);
            }
        } else {
            returnedText = textField.getText();
        }
    }
}
