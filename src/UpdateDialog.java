package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class UpdateDialog extends JDialog {
    private static JTextField titleField;
    private static JTextField authorField;
    private static JTextField reviewField;
    private static JTextField ratingField;
    private JButton updateButton;

    private static int bookID;
    private Book book;
    private Map<Integer, Book> generalDatabase;
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;
    private DefaultTableModel tableModel;
    private ResourceBundle bundle;

    public UpdateDialog(Book book, Map<Integer, Book> generalDatabase, Map<String, Map<Integer, PersonalBook>> personalDatabase, int bookID, DefaultTableModel tableModel,ResourceBundle bundle) {
        UpdateDialog.bookID = bookID;
        this.bundle = bundle;
        this.book = book;
        this.generalDatabase = generalDatabase;
        this.personalDatabase = personalDatabase;
        this.tableModel = tableModel;
        setTitle(bundle.getString("updateBookInfoTitle"));
        setSize(400, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldsPanel.add(new JLabel(bundle.getString("label.title")));
        titleField = new JTextField(book.getTitle());
        fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel(bundle.getString("book.author")));
        authorField = new JTextField(book.getAuthor());
        fieldsPanel.add(authorField);
        // Hidden fields for original review and rating
        reviewField = new JTextField(book.getReviews().toString());
        reviewField.setVisible(false);
        fieldsPanel.add(reviewField);
        ratingField = new JTextField(book.calculateAverageRating());
        ratingField.setVisible(false);
        fieldsPanel.add(ratingField);
    
        panel.add(fieldsPanel, BorderLayout.CENTER);
    
        updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update the book information in the map
                updateBookInformation();
    
                // Update the CSV file
                updateGeneralCSVFile(generalDatabase);
    
                // Update the table data
                updateTableData();
    
                updatePersonalDatabase();
    
                // Close the dialog
                dispose();
            }
        });
        panel.add(updateButton, BorderLayout.SOUTH);
    
        // Add an empty border with a specified width and height to the panel
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        // Create a new container to hold the panel
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
    
        // Add the panel to the container and center it
        container.add(panel, BorderLayout.CENTER);
    }

    private void updateBookInformation() {
        // Update book information based on the input fields
        book.setTitle(titleField.getText());
        book.setAuthor(authorField.getText());

        // Update the book's review
        String reviewText = reviewField.getText().trim();
        Review review = new Review();
        if (!reviewText.isEmpty() && !reviewText.equalsIgnoreCase("No Review")) {
            // If review is not empty and not "No Review", add each review individually
            String[] reviewLines = reviewText.split("\n");
            for (String line : reviewLines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    review.addUserReview(parts[0].trim(), parts[1].trim());
                }
            }
        }
        book.setReviews(review);

        // Update the book's rating
        String ratingText = ratingField.getText().trim();
        Rating rating = new Rating();
        if (!ratingText.isEmpty() && !ratingText.equalsIgnoreCase("No Rating")) {
            // If rating is not empty and not "No Rating", add the rating
            double ratingValue = Double.parseDouble(ratingText);
            rating.addUserRating("default", ratingValue); // Using a default username for now
        }
        book.setRatings(rating);

        // Update the book in the generalDatabase map
        generalDatabase.put(bookID, book);
    }

    public static void updateGeneralCSVFile(Map<Integer, Book> generalDatabase) {
        // Write the updated book information to the CSV file
        File csvFile = new File("data/general_database.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("BookID,Title,Author,Review,Rating\n");
            for (Map.Entry<Integer, Book> entry : generalDatabase.entrySet()) {
                int bookID = entry.getKey();
                Book b = entry.getValue();
                writer.write(bookID + "," + b.toCSVString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTableData() {
        // Clear the table
        tableModel.setRowCount(0);

        for (Map.Entry<Integer, Book> entry : generalDatabase.entrySet()) {
            Book book = entry.getValue();
            String rating = String.valueOf(book.getRatings().getAverageRating());
            tableModel.addRow(new Object[] { entry.getKey(), book.getTitle(), book.getAuthor(),
                    book.getReviews().toString(), rating });
        }
    }

    public void updatePersonalDatabase() {
        // Iterate over all users' personal databases
        for (Map.Entry<String, Map<Integer, PersonalBook>> userEntry : personalDatabase.entrySet()) {
            Map<Integer, PersonalBook> userPersonalBooks = userEntry.getValue();

            if (userPersonalBooks.isEmpty()) {
                continue; // If the user has no books, move to the next user
            }

            if (!userPersonalBooks.containsKey(bookID)) {
                continue; // If the bookID does not exist for the user, move to the next user
            }

            PersonalBook personalBookToUpdate = userPersonalBooks.get(bookID);

            personalBookToUpdate.getBook().setTitle(titleField.getText());
            personalBookToUpdate.getBook().setAuthor(authorField.getText());


            // Update the personalBookToUpdate in the user's personal database
            userPersonalBooks.put(bookID, personalBookToUpdate);

            // Update the CSV file for this user
            updatePersonalCSVFile(personalDatabase);
        }

    }

    public static void updatePersonalCSVFile(Map<String, Map<Integer, PersonalBook>> personalDatabase) {
        File csvFile = new File("data/personal_databases.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            // Write the CSV header
            writer.write("BookID,Username,Title,Author,Rating,Review,Status,TimeSpent,StartDate,EndDate,UserRating,UserReview\n");

            // Iterate over each entry in the personal database
            for (Map.Entry<String, Map<Integer, PersonalBook>> userEntry : personalDatabase.entrySet()) {
                String username = userEntry.getKey();
                Map<Integer, PersonalBook> userPersonalBooks = userEntry.getValue();

                // Iterate over each PersonalBook entry in the user's personal database
                for (Map.Entry<Integer, PersonalBook> entry : userPersonalBooks.entrySet()) {
                    int bookID = entry.getKey();
                    PersonalBook personalBook = entry.getValue();
                    // Write the CSV representation of the PersonalBook
                    writer.write(bookID + "," + username + "," + personalBook.toCSVString() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
    // New method to update personal database from the general database
    public static void updatePersonalDatabaseFromGeneral(int deletedUserBookID,Map<Integer, Book> generalDatabase,Map<String, Map<Integer, PersonalBook>> personalDatabase) {
        // Iterate over all entries in personalDatabase
        for (Map.Entry<String, Map<Integer, PersonalBook>> userEntry : personalDatabase.entrySet()) {
            Map<Integer, PersonalBook> userPersonalBooks = userEntry.getValue();

            // Check if the user's personal database contains the book shared with the deleted user
            if (userPersonalBooks.containsKey(deletedUserBookID)) {
                // Get the personal book shared with the deleted user
                PersonalBook personalBook = userPersonalBooks.get(deletedUserBookID);

                // Get the corresponding book from generalDatabase
                Book generalBook = generalDatabase.get(deletedUserBookID);
                if (generalBook != null) {
                    // Update the personal book with the latest ratings and reviews from the general database
                    personalBook.getBook().setRatings(generalBook.getRatings());
                    personalBook.getBook().setReviews(generalBook.getReviews());

                    // Update the personal book in the user's personal database
                    userPersonalBooks.put(deletedUserBookID, personalBook);
                }
            }
        }
    }


}
