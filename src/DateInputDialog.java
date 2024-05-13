package src;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

public class DateInputDialog {
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;
    private String username;
    private JFrame parentFrame; // Reference to the parent JFrame to center the dialog
    private JTable table;
    private int bookID;
    private String StartDate;
    private String EndDate;
    private ResourceBundle bundle;

    public DateInputDialog(Map<String, Map<Integer, PersonalBook>> personalDatabase, String username, int bookID,
            JTable table, JFrame parentFrame, String startDate, String endDate, ResourceBundle bundle) {
        this.personalDatabase = personalDatabase;
        this.username = username;
        this.bookID = bookID;
        this.parentFrame = parentFrame;
        this.table = table;
        this.StartDate = startDate;
        this.EndDate = endDate;
        this.bundle = bundle;
    }

    public void open() {
        // Create a new JDialog
        JDialog dialog = new JDialog(parentFrame, bundle.getString("enterDatesTitle"), true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(3, 2));
        dialog.setLocationRelativeTo(parentFrame); // Center the dialog relative to the parent frame

        // Create text fields for start date and end date
        JTextField startDateField = new JTextField(StartDate); // Pre-fill with start date
        JTextField endDateField = new JTextField(EndDate != null && !EndDate.isEmpty() ? EndDate : "");

        // Add labels and text fields to the dialog
        dialog.add(new JLabel(bundle.getString("startDateLabel")));
        dialog.add(startDateField);
        dialog.add(new JLabel(bundle.getString("endDateLabel")));
        dialog.add(endDateField);

        JButton okButton = new JButton(bundle.getString("okButton"));
        JButton cancelButton = new JButton(bundle.getString("cancelButton"));

        dialog.add(okButton);
        dialog.add(cancelButton);

        // ActionListener for OK button
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String startDateText = startDateField.getText();
                String endDateText = endDateField.getText();
                // Validate date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
                dateFormat.setLenient(false);
                try {
                    // Parse start date
                    Date startDate = dateFormat.parse(startDateText);

                    // Parse end date if not empty
                    Date endDate = null;
                    if (!endDateText.isEmpty()) {
                        endDate = dateFormat.parse(endDateText);
                        // Validate end date not before start date
                        if (endDate.before(startDate)) {
                            JOptionPane.showMessageDialog(dialog, bundle.getString("endDateBeforeStartDateError"), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return; // Exit method without updating PersonalBook
                        }
                    }

                    // Update PersonalBook object
                    PersonalBook personalBook = personalDatabase.get(username).get(bookID);

                    personalBook.setStartDate(startDateText);
                    personalBook.setEndDate(endDateText);
                    personalBook.calculateTimeSpent();

                    System.out.println(personalBook);
                    UpdateDialog.updatePersonalCSVFile(personalDatabase);
                    PersonalDatabase.showPersonalDatabase(personalDatabase, username, table, bundle);
                    // Close dialog
                    dialog.dispose();
                } catch (ParseException ex) {
                    // Show error message for invalid date format
                    JOptionPane.showMessageDialog(dialog, bundle.getString("invalidDateFormat"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ActionListener for Cancel button
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close dialog without making any changes
                dialog.dispose();
            }
        });

        // Set dialog visibility to true
        dialog.setVisible(true);
    }

}
