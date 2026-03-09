package ui;

import javax.swing.*;
import java.awt.*;

public class ResultFrame extends JFrame {
    public ResultFrame(String message) {
        setTitle("Match Result");
        setSize(520, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);

        JButton ok = new JButton("Close");
        ok.addActionListener(e -> dispose());
        JPanel p = new JPanel();
        p.add(ok);
        add(p, BorderLayout.SOUTH);
        setVisible(true);
    }
}
