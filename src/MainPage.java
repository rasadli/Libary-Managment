package src;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class MainPage extends JFrame {
    private JLabel welcomeLabel;
    @SuppressWarnings("unused")
    private ResourceBundle bundle;

    public MainPage(String username,ResourceBundle bundle) {

        this.bundle = bundle;

        setTitle(bundle.getString("mainPage.title"));
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        welcomeLabel = new JLabel(bundle.getString("welcome") + " " + username + "!");
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1)); // Two rows, one column

        JPanel upperButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton generalDatabaseButton = new JButton(bundle.getString("generalDatabase"));
        upperButtonPanel.add(generalDatabaseButton);

        if (username.equals("admin")) {
            JButton usersButton = new JButton(bundle.getString("users"));
            upperButtonPanel.add(usersButton);
            usersButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        new UserPanel(username,bundle).setVisible(true); // Pass username to GeneralDatabase
                        dispose(); // Close the MainPage
                    });
                }
            });
        } else {
            JButton personalDatabaseButton = new JButton(bundle.getString("personalDatabase"));
            upperButtonPanel.add(personalDatabaseButton);
            personalDatabaseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    EventQueue.invokeLater(() -> {
                        SwingUtilities.invokeLater(() -> {
                            // Create instance of GeneralDatabase
                            GeneralDatabase generalDatabase = new GeneralDatabase(username, bundle);
                            // Hide GeneralDatabase
                            generalDatabase.setVisible(false);
                            // Pass username and generalDatabase to PersonalDatabase and make it visible
                            new PersonalDatabase(username, generalDatabase.getGeneralDatabase(),bundle).setVisible(true);
                            dispose(); // Close the MainPage
                        });
                    });
                }
            });
        }

        JPanel lowerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton logoutButton = new JButton(bundle.getString("logout"));
        lowerButtonPanel.add(logoutButton);

        buttonPanel.add(upperButtonPanel);
        buttonPanel.add(lowerButtonPanel);
        add(buttonPanel, BorderLayout.CENTER);

        // Action listener for General Database button
        generalDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new GeneralDatabase(username,bundle).setVisible(true); // Pass username to GeneralDatabase
                        dispose(); // Close the MainPage
                    });
                });
            }
        });

        // Action listener for Logout button
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the current window
                dispose();

                // Open the login page window
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new LoginPage(bundle).setVisible(true); // Assuming LoginPage is the class for your login page
                    });
                });
            }
        });
    }
}
