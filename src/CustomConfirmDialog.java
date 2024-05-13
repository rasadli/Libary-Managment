package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomConfirmDialog extends JDialog {
    private boolean confirmed;

    public CustomConfirmDialog(JFrame parent, String title, String message) {
        super(parent, title, true);
        setLayout(new BorderLayout());

        // Create a JTextArea to display the message
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT); // Center-align the text
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton yesButton = new JButton("Yes");
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose(); // Close the dialog
            }
        });
        buttonPanel.add(yesButton);

        JButton noButton = new JButton("No");
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose(); // Close the dialog
            }
        });
        buttonPanel.add(noButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 200); // Adjust the size as needed
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

