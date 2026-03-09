package main;

import javax.swing.SwingUtilities;
import ui.OpeningScreen;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OpeningScreen frame = new OpeningScreen();
            frame.setVisible(true);
        });
    }
}
