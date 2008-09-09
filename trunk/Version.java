/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import javax.swing.*;

/**
 *
 * @author ehoward
 */
public class Version{

    String fileContents;
    JTextArea versionWindow;
    JFrame versionFrame;

    public Version() {
        versionWindow = new JTextArea();
        File inputFile = new File(".");
        try {
            inputFile = new File(System.getProperty("user.dir") + "\\UpdateInfo.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileContents = getContents(inputFile);
        versionWindow.setText(fileContents);
        versionFrame = new JFrame();
        versionFrame.add(versionWindow);
        versionFrame.pack();
        versionFrame.setLocationRelativeTo(null);
        versionFrame.setTitle("About");
        versionWindow.setEditable(false);
        versionFrame.setVisible(true);
    }

    static public String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuilder contents = new StringBuilder();

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null; //not declared within while loop
        /*
                 * readLine is a bit quirky :
                 * it returns the content of a line MINUS the newline.
                 * it returns null only for the END of the stream.
                 * it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }
}
