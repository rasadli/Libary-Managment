package src;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddDialog extends JDialog {
    private Map<Integer, Book> generalDatabase;
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;

    private JTextField titleField;
    private JTextField authorField;
    private JButton addButton;
    private boolean isAdmin = false;
    private String currentUser;
    private ResourceBundle bundle;

    public AddDialog(int nextBookID, String username, Map<String, Map<Integer, PersonalBook>> personalDatabase,
            JTable table, ResourceBundle bundle) {
        this.bundle = bundle;
        this.currentUser = username;
        this.personalDatabase = personalDatabase;

        if (currentUser.equals("admin")) {
            isAdmin = true;
        }

        String addNewBookTitle = bundle.getString("title.addNewBook");

        setTitle(addNewBookTitle);
        setSize(300, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.generalDatabase = CSVProcessor.generalDatabase;
        initializeUI();
        addButtonListener(nextBookID, table);
    }

    private void initializeUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 10)); // Increased vertical gap for margin

        panel.setLayout(new GridLayout(3, 2));

        // Create and add the title label and text field
        panel.add(new JLabel(bundle.getString("label.title")));
        titleField = new JTextField();
        panel.add(titleField);

        // Create and add the author label and text field
        panel.add(new JLabel(bundle.getString("label.author")));
        authorField = new JTextField();
        panel.add(authorField);

        // Create and add the add button
        addButton = new JButton(bundle.getString("button.add"));
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(addButton);

        // Add some padding around the panel
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(panel);
    }

    public void addButtonListener(int nextBookID, JTable table) {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the title and author from the text fields
                String title = titleField.getText();
                String author = authorField.getText();

                // Create a new book
                Book newBook = new Book(title, author, new Rating(), new Review());

                if (isAdmin) {
                    generalDatabase.put(nextBookID, newBook);
                    UpdateDialog.updateGeneralCSVFile(generalDatabase);
                    GeneralDatabase.initializeGeneralDatabase();
                } else {
                    Map<Integer, PersonalBook> userPersonalDatabase = personalDatabase.get(currentUser);
                    if (userPersonalDatabase != null) {
                        int newBookId = nextBookID * -1; // Make it negative
                        while (userPersonalDatabase.containsKey(newBookId)) {
                            newBookId--;
                        }
                        userPersonalDatabase.put(newBookId, new PersonalBook(newBook));
                        GeneralDatabase.savePersonalDatabaseToCSV(currentUser);
                    } else {
                        // Create a new personal database for the user
                        userPersonalDatabase = new HashMap<>(); // Or any other suitable implementation of Map
                        personalDatabase.put(currentUser, userPersonalDatabase);

                        // Add the new book to the newly created personal database
                        userPersonalDatabase.put(nextBookID * -1, new PersonalBook(newBook)); // Make it negative
                        GeneralDatabase.savePersonalDatabaseToCSV(currentUser);
                    }
                }

                dispose();
                if (currentUser.equals("admin")) {
                    GeneralDatabase.initializeGeneralDatabase();
                }else{
                    PersonalDatabase.showPersonalDatabase(personalDatabase, currentUser, table, bundle);
                }
            }
        });
    }

}
