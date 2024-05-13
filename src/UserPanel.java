package src;

import javax.swing.*;
import javax.swing.table.*;


import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UserPanel extends JFrame {
    private static List<String> userDatabase = new ArrayList<>();
    private JTable table;
    private JTextField searchField;
    private String currentUsername;
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;
    private Map<Integer, Book> generalDatabase;
    private ResourceBundle bundle;

    public UserPanel(String username, ResourceBundle bundle) {
        this.bundle = bundle;
        this.currentUsername = username;
        CSVProcessor.readDataFromCSV();
        CSVProcessor.readPersonalDataFromCSV();
        CSVProcessor.readUserDataFromCSV(); // Ensure this method is called
        userDatabase = CSVProcessor.userDatabase;
        this.personalDatabase = CSVProcessor.personalDatabases;
        generalDatabase = CSVProcessor.generalDatabase;
        initializeUI();
        initializeUserDatabase();
    }

    private void initializeUI() {
        setTitle(bundle.getString("userPanel.title"));
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel(bundle.getString("searchLabel"));
        searchField = new JTextField(20);
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);

        JButton homeButton = new JButton(bundle.getString("homeButton"));
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new MainPage(currentUsername,bundle).setVisible(true);
                    });
                });
            }
        });
        controlPanel.add(homeButton);

        JButton removeButton = new JButton(bundle.getString("removeButton"));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String removedUsername = (String) table.getValueAt(selectedRow, 0);

                    // Remove from general database
                    for (Map.Entry<Integer, Book> entry : generalDatabase.entrySet()) {
                        int bookID = entry.getKey();
                        Book book = entry.getValue();
                        if (book.getReviews().getUserReviews().containsKey(removedUsername)) {
                            book.getReviews().removeUserReview(removedUsername);
                        }
                        if (book.getRatings().getUserRatings().containsKey(removedUsername)) {
                            book.getRatings().removeUserRating(removedUsername);
                        }
                        // Update the general database CSV file
                        UpdateDialog.updateGeneralCSVFile(generalDatabase);

                        // Remove from user database
                        userDatabase.remove(removedUsername);
                        // Remove from personal database
                        if (personalDatabase.containsKey(removedUsername)) {
                            personalDatabase.remove(removedUsername);
                        }

                        UpdateDialog.updatePersonalDatabaseFromGeneral(bookID, generalDatabase, personalDatabase);

                        // Save the updated personal database to CSV
                        UpdateDialog.updatePersonalCSVFile(personalDatabase);

                    }

                    initializeUserDatabase();

                    // Remove the user from the file
                    removeUserFromFile(removedUsername);
                } else {
                    JOptionPane.showMessageDialog(null, bundle.getString("selectRowError"), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        controlPanel.add(removeButton); // Add remove button

        JButton logoutButton = new JButton(bundle.getString("logoutButton"));
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
        controlPanel.add(logoutButton);

        getContentPane().add(controlPanel, BorderLayout.NORTH);
    }

    private void removeUserFromFile(String removedUsername) {
        userDatabase.remove(removedUsername);

        final File usersFile = new File("data/user_database.csv");
        List<String> updatedUsersList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!parts[1].equals(removedUsername)) {
                    updatedUsersList.add(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile))) {
            for (String username : updatedUsersList) {
                writer.write(username + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUserDatabase() {
        String[] columnNames = { bundle.getString("userPanel.usernameColumn") };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };
        table.setModel(model); // Set the table model

        for (String username : UserPanel.userDatabase) {
            model.addRow(new Object[] { username });
        }
    }
}
