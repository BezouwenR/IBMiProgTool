package copyfiles;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class is a dialog for entering a name - new directory name, etc.
 * 
 * @author Vladimír Župka 2016
 */
public class GetProgLanguage extends JDialog {
   Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
   String encoding = System.getProperty("file.encoding", "UTF-8");
   final String PROP_COMMENT = "Copy files between IBM i and PC, edit and compile.";

   Properties properties;

   Container cont;
   JPanel panel = new JPanel();
   GroupLayout layout = new GroupLayout(panel);
   JLabel titleLabel = new JLabel("Choose programming language");
   JComboBox<String> languageComboBox = new JComboBox<>();
   JButton cancel = new JButton("Cancel");
   JButton enter = new JButton("Enter");

   int windowWidth = 290;
   int windowHeight = 110;

   String progLanguage;

   /**
    * Constructor
    * 
    * @param windowTitle
    */
   public GetProgLanguage(String windowTitle) {
      super();
      super.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
      super.setTitle(windowTitle);
   }

   public void getProgLanguage(int currentX, int currentY) {
      
      properties = new Properties();
      try {
         BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
         properties.load(infile);
         infile.close();
         progLanguage = properties.getProperty("HIGHLIGHT_BLOCKS");
      } catch (Exception exc) {
         exc.printStackTrace();
      }

      cont = getContentPane();
      cont.add(panel);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);

      languageComboBox.setPreferredSize(new Dimension(170, 30));
      languageComboBox.setMaximumSize(new Dimension(170, 30));
      languageComboBox.setMinimumSize(new Dimension(170, 30));

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

      panel.setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(titleLabel)
            .addComponent(languageComboBox)
      );
      layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(titleLabel)
            .addComponent(languageComboBox)
      );

      // Select programming language from the list in combo box - listener
      languageComboBox.addItemListener(il -> {
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
         dispose();
      });

      // Add window listener to set properties before the dialog is disposed of.
      addWindowListener(new ThisWindowAdapter());

      setSize(windowWidth, windowHeight);
      setLocation(currentX + 460, currentY + 160);
      setVisible(true);
      pack();

   }

   /**
    * Window adapter
    */
   class ThisWindowAdapter extends WindowAdapter {
      @Override
      public void windowClosing(WindowEvent we) {
         try {
            BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
            // Save programming language into properties
            properties.setProperty("HIGHLIGHT_BLOCKS", progLanguage);
            properties.store(outfile, PROP_COMMENT);
            outfile.close();
         } catch (Exception exc) {
            exc.printStackTrace();
         }
         JDialog dialog = (JDialog) we.getSource();
         dialog.dispose();
      };
   }

}
