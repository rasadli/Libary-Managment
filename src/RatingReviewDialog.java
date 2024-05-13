package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.ResourceBundle;

public class RatingReviewDialog extends JDialog {
    private Map<Integer, Book> generalDatabase;
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;
    private JTable table;
    private String currentUsername;
    private ResourceBundle bundle;

    public RatingReviewDialog(Map<Integer, Book> generalDatabase,
            Map<String, Map<Integer, PersonalBook>> personalDatabase,
            JTable table, String currentUsername, ResourceBundle bundle) {
        this.bundle = bundle;
        this.generalDatabase = generalDatabase;
        this.personalDatabase = personalDatabase;
        this.table = table;
        this.currentUsername = currentUsername;
    }

    public void openRatingDialog(int row, int userBookID, DefaultTableModel model, Book book) {
        Object[] ratingOptions = { "1", "2", "3", "4", "5" };
    
        // Check if there's already a review for this book
        PersonalBook pb = personalDatabase.get(currentUsername).get(userBookID);
        int existingRating = (int) pb.getUserRating();
    
        // Show a JOptionPane with the rating options
        JComboBox<Object> ratingComboBox = new JComboBox<>(ratingOptions);
        if (existingRating != 0) {
            ratingComboBox.setSelectedItem(Integer.toString(existingRating));
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(bundle.getString("selectRatingLabel")), BorderLayout.WEST);
        panel.add(ratingComboBox, BorderLayout.CENTER);
    
        JButton removeButton = new JButton(bundle.getString("removeButton"));
        removeButton.addActionListener(e -> {
            this.dispose();
    
            if (userBookID > 0) {
                book.getRatings().removeUserRating(currentUsername);
                generalDatabase.put(userBookID, book);
                UpdateDialog.updateGeneralCSVFile(generalDatabase);
            }
            Map<Integer, PersonalBook> userPersonalDatabase = personalDatabase.get(currentUsername);
            PersonalBook personalBook = userPersonalDatabase.get(userBookID);
            personalBook.getBook().removeRating(currentUsername);
            personalBook.setUserRating(0.0);
            userPersonalDatabase.put(userBookID, personalBook);
    
            if (userBookID > 0) {
                UpdateDialog.updatePersonalDatabaseFromGeneral(userBookID, generalDatabase, personalDatabase);
            }
    
            UpdateDialog.updatePersonalCSVFile(personalDatabase);
            PersonalDatabase.showPersonalDatabase(personalDatabase, currentUsername, table, bundle);
        });
    
        panel.add(removeButton, BorderLayout.LINE_END);
    
        int option = JOptionPane.showConfirmDialog(this, panel, bundle.getString("addRating"),
                JOptionPane.OK_CANCEL_OPTION);
    
        // If the user selects a rating, update the table model
        if (option == JOptionPane.OK_OPTION) {
            Object selectedRating = ratingComboBox.getSelectedItem();
            double ratingValue = Double.parseDouble((String) selectedRating);
    
            table.getModel().setValueAt(selectedRating, row, table.getColumnCount() - 2);
            if (userBookID > 0) {
                book.getRatings().addUserRating(currentUsername, ratingValue);
                generalDatabase.put(userBookID, book);
                UpdateDialog.updateGeneralCSVFile(generalDatabase);
            }
    
            // Update the personalDatabase map with the selected rating
            Map<Integer, PersonalBook> userPersonalDatabase = personalDatabase.get(currentUsername);
            PersonalBook personalBook = userPersonalDatabase.get(userBookID);
    
            personalBook.getBook().addRating(currentUsername, ratingValue);
            personalBook.setUserRating(ratingValue);
            userPersonalDatabase.put(userBookID, personalBook); // Update the personal book in the map
    
            if (userBookID > 0) {
                UpdateDialog.updatePersonalDatabaseFromGeneral(userBookID, generalDatabase, personalDatabase);
            }
            UpdateDialog.updatePersonalCSVFile(personalDatabase);
            PersonalDatabase.showPersonalDatabase(personalDatabase, currentUsername, table, bundle);
        }
    }
    

    public void openReviewDialog(int row, int userBookID, DefaultTableModel model, Book book) {
        // Create a text field for the user to input their review
        JTextField reviewField = new JTextField(20);

        // Check if there's already a review for this book
        PersonalBook personalBook = personalDatabase.get(currentUsername).get(userBookID);
        String existingReview = personalBook.getUserReview();

        // If there's an existing review, set the text field to display it
        if (existingReview != null && !existingReview.isEmpty()
                && !existingReview.equals(bundle.getString("addRating"))) {
            reviewField.setText(existingReview);
        }
        if (existingReview.equals("Add Review")) {
            reviewField.setText("");
        }

        // Create a panel to hold the text field and the "Remove" button
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(reviewField, BorderLayout.CENTER);

        // Create a "Remove" button
        JButton removeButton = new JButton(bundle.getString("removeButton"));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Remove the review
                if (userBookID > 0) {
                    book.getReviews().removeUserReview(currentUsername);
                    generalDatabase.put(userBookID, book);
                    UpdateDialog.updateGeneralCSVFile(generalDatabase);
                }

                personalBook.getBook().removeReview(currentUsername);

                personalBook.setUserReview("Add Review");
                UpdateDialog.updatePersonalCSVFile(personalDatabase);

                if (userBookID > 0) {
                    UpdateDialog.updatePersonalDatabaseFromGeneral(userBookID, generalDatabase, personalDatabase);
                }
                // Close the dialog
                dispose();

                System.out.println(bundle);
                PersonalDatabase.showPersonalDatabase(personalDatabase, currentUsername, table, bundle);
            }
        });
        removeButton.setPreferredSize(new Dimension(80, 20));
        panel.add(removeButton, BorderLayout.LINE_END);

        // Show a JOptionPane with the panel containing the text field and the "Remove"
        // button
        int option = JOptionPane.showConfirmDialog(this, panel, bundle.getString("enterReviewDialogTitle"),
                JOptionPane.OK_CANCEL_OPTION);

        // If the user clicks Add and the review is not empty, retrieve the review text
        // and update the table model
        if (option == JOptionPane.OK_OPTION) {
            String reviewText = reviewField.getText().trim();

            // Trim to remove leading/trailing whitespaces
            if (!reviewText.isEmpty() && !reviewText.equals(bundle.getString("addReview"))) {
                // Update the table model with the review text for the specified row
                if (userBookID > 0) {
                    book.getReviews().addUserReview(currentUsername, reviewText);
                    UpdateDialog.updateGeneralCSVFile(generalDatabase);
                }
                // Update the personalDatabase map with the selected rating
                Map<Integer, PersonalBook> userPersonalDatabase = personalDatabase.get(currentUsername);
                personalBook.setUserReview(reviewText);
                personalBook.getBook().addReview(currentUsername, reviewText);
                userPersonalDatabase.put(userBookID, personalBook); // Update the personal book in the map

                if (userBookID > 0) {
                    UpdateDialog.updatePersonalDatabaseFromGeneral(userBookID, generalDatabase, personalDatabase);
                }
                UpdateDialog.updatePersonalCSVFile(personalDatabase);
                PersonalDatabase.showPersonalDatabase(personalDatabase, currentUsername, table, bundle);
            } else {
                // If the review is empty, display an error message
                JOptionPane.showMessageDialog(this, "Review cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Restore the default UIManager settings
        UIManager.put("OptionPane.noIcon", Boolean.FALSE);
    }
}
