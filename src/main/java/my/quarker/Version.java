package my.quarker;

import java.io.*;
import javax.swing.*;

public class Version {

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
        StringBuilder contents = new StringBuilder();

        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null;
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
