package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

public class GeneralDatabase extends JFrame implements CRUD {
    private static Map<Integer, Book> generalDatabase;
    private static JTable table;
    private JTextField searchField;
    private static String currentUsername;
    private static Map<String, Map<Integer, PersonalBook>> PersonalDatabase;
    private TableRowSorter<DefaultTableModel> sorter; // Table row sorter for sorting and filtering
    private static ResourceBundle bundle;

    private Map<Integer, SortOrder> sortOrderMap = new HashMap<>();
    private List<Integer> sortSequence = new ArrayList<>();

    public GeneralDatabase(String username, ResourceBundle bundle) {
        GeneralDatabase.bundle = bundle;
        GeneralDatabase.currentUsername = username;
        CSVProcessor.readPersonalDataFromCSV();
        GeneralDatabase.PersonalDatabase = CSVProcessor.personalDatabases;
        initializeUI();
        read();
        setupTableSorting();
    }

    public Map<Integer, Book> getGeneralDatabase() {
        return GeneralDatabase.generalDatabase;
    }

    private void initializeUI() {
        setTitle(bundle.getString("generalDatabase"));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model); // Initialize TableRowSorter
        table.setRowSorter(sorter); // Set TableRowSorter to JTable
        table.getTableHeader().setReorderingAllowed(false); // Disable column reordering
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel(bundle.getString("searchLabel"));
        searchField = new JTextField(20);
        JButton homeButton = new JButton(bundle.getString("homeButton"));
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new MainPage(currentUsername, bundle).setVisible(true);
                    });
                });
            }
        });

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase(); // Get the search query in lowercase
                RowFilter<DefaultTableModel, Integer> rowFilter = new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int i = 0; i < entry.getModel().getColumnCount(); i++) {
                            String cellValue = entry.getStringValue(i).toLowerCase(); // Convert cell value to lowercase
                            if (cellValue.contains(query)) {
                                return true; // Include if any column matches the query
                            }
                        }
                        return false; // Exclude otherwise
                    }
                };

                sorter.setRowFilter(rowFilter); // Apply row filter to the sorter
            }
        });

        controlPanel.add(homeButton);

        JButton addButton;
        if (currentUsername.equals("admin")) {
            addButton = new JButton(bundle.getString("title.addNewBook"));
        } else {
            addButton = new JButton(bundle.getString("button.addToPersonalDatabase"));
        }
        controlPanel.add(addButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });

        JButton editButton;
        if (currentUsername.equals("admin")) {
            String editButtonText = bundle.getString("editButton.text");
            editButton = new JButton(editButtonText);

            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        int bookID = (int) table.getValueAt(selectedRow, 0);
                        Book book = generalDatabase.get(bookID);
                        UpdateDialog updateDialog = new UpdateDialog(book, generalDatabase, PersonalDatabase, bookID,
                                model, bundle);
                        updateDialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, bundle.getString("noBookSelectedTitle"), "Erorr",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            controlPanel.add(editButton);
        }

        JButton deleteButton;
        if (currentUsername.equals("admin")) {
            String deleteButtonText = bundle.getString("deleteButton.text");
            deleteButton = new JButton(deleteButtonText);

            controlPanel.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    delete();
                }
            });
        }

        // Add the logout button to the control panel
        JButton logoutButton = new JButton(bundle.getString("logout"));
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

        JLabel welcomeLabel = new JLabel(bundle.getString("welcome") + " " + currentUsername + "!");
        controlPanel.add(welcomeLabel, BorderLayout.SOUTH);

        String[] columnNames = {
                bundle.getString("column.bookID"),
                bundle.getString("column.title"),
                bundle.getString("column.author"),
                bundle.getString("column.rating"),
                bundle.getString("column.reviews"),
        };

        for (String columnName : columnNames) {
            model.addColumn(columnName);
        }

        TableColumn bookIDColumn = table.getColumnModel().getColumn(0);
        bookIDColumn.setMinWidth(0);
        bookIDColumn.setMaxWidth(0);
        bookIDColumn.setPreferredWidth(0);
        bookIDColumn.setWidth(0);

        table.setRowHeight(50);

        update(model);

    }

    private void handleTableMouseClicked(MouseEvent e, DefaultTableModel model) {
        int row = table.rowAtPoint(e.getPoint());
        int column = table.getColumnModel().getColumnIndex(bundle.getString("column.reviews"));

        if (column == table.columnAtPoint(e.getPoint())) {
            String usersLine = (String) model.getValueAt(row, column);
            String[] users = usersLine.split(", ");

            Rectangle cellRect = table.getCellRect(row, column, true);
            int clickX = e.getX() - cellRect.x;
            int currentPosition = 0;
            int bookID = (int) model.getValueAt(row, 0);
            for (String user : users) {
                int reviewerWidth = table.getFontMetrics(table.getFont()).stringWidth(user);
                if (clickX >= currentPosition && clickX <= currentPosition + reviewerWidth) {
                    displayNameInfo(user, bookID, PersonalDatabase);
                    break;
                }
                currentPosition += reviewerWidth + 2; // Account for the comma and space
            }
        }
    }

    private void setupTableSorting() {
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                toggleSortOrder(column);
            }
        });
    }

    private void toggleSortOrder(int column) {
        SortOrder currentOrder = sortOrderMap.getOrDefault(column, SortOrder.UNSORTED);

        // Toggle sorting order
        switch (currentOrder) {
            case UNSORTED:
                sortOrderMap.put(column, SortOrder.ASCENDING);
                sortSequence.add(column); // Keep track of order of clicks
                break;
            case ASCENDING:
                sortOrderMap.put(column, SortOrder.DESCENDING);
                break;
            case DESCENDING:
                sortOrderMap.remove(column);
                sortSequence.remove((Integer) column); // Remove from sequence if removed from sorting
                break;
        }

        // Build the list of sort keys using the stream API
        List<RowSorter.SortKey> sortKeys = sortSequence.stream()
                .filter(sortOrderMap::containsKey) 
                .map(colIndex -> new RowSorter.SortKey(colIndex, sortOrderMap.get(colIndex)))
                .collect(Collectors.toList()); 

        sorter.setSortKeys(sortKeys); 
    }

    public static void initializeGeneralDatabase() {
        File generalDatabaseFile = new File("data/general_database.csv");
        if (!generalDatabaseFile.exists()) {
            CSVProcessor.processCSV("data/brodsky.csv", table, bundle);
        }
        CSVProcessor.readDataFromCSV();

        GeneralDatabase.generalDatabase = CSVProcessor.generalDatabase;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Map.Entry<Integer, Book> entry : generalDatabase.entrySet()) {
            Book book = entry.getValue();
            String rating = book.getRatings().getAverageRating() == 0.0 ? bundle.getString("book.noRating")
                    : book.calculateAverageRating() + " (" + book.getRatings().getRatingCount() + ")";
            String reviews = book.getReviews().toString().isEmpty() ? bundle.getString("book.noReview")
                    : book.getReviews().toTableString();
            model.addRow(new Object[] { entry.getKey(), book.getTitle(), book.getAuthor(), rating, reviews });
        }
    }

    public static void displayNameInfo(String name, int bookID,
            Map<String, Map<Integer, PersonalBook>> personalDatabase) {
        Map<Integer, PersonalBook> userPersonalBooks = personalDatabase.get(name);

        if (userPersonalBooks != null && userPersonalBooks.containsKey(bookID)) {
            PersonalBook personalBook = userPersonalBooks.get(bookID);

            String info = bundle.getString("book.title") + " " + personalBook.getBook().getTitle() + "\n"
                    + bundle.getString("book.author") + " " + personalBook.getBook().getAuthor() + "\n"
                    + bundle.getString("book.username") + " " + name + "\n"
                    + bundle.getString("book.rating") + " " + personalBook.getUserReview() + "\n"
                    + bundle.getString("book.reviews") + " " + personalBook.getUserRating() + "\n";

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setText(info);
            textArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font
            textArea.setLineWrap(true); // Enable line wrapping
            textArea.setWrapStyleWord(true); // Wrap at word boundaries
            textArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10), // Add padding
                    BorderFactory.createLineBorder(Color.GRAY))); // Add border

            // Optionally, you can set the background and foreground color of the text area
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);

            JButton closeButton = new JButton(bundle.getString("closeButton"));
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(closeButton);

            if (currentUsername.equals("admin")) {
                JButton removeReviewButton = new JButton(bundle.getString("removeButton"));
                removeReviewButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Book book = generalDatabase.get(bookID);
                        // Retrieve the review and rating from the book
                        Review reviews = book.getReviews();

                        // Remove the current user's review
                        reviews.removeUserReview(name);

                        // Update the general database CSV file
                        UpdateDialog.updateGeneralCSVFile(generalDatabase);

                        // Remove the review from the personal database of the current user
                        personalDatabase.get(name).get(bookID).setUserReview(bundle.getString("addReview"));

                        // Save the updated personal database to CSV
                        UpdateDialog.updatePersonalDatabaseFromGeneral(bookID, generalDatabase, personalDatabase);
                        UpdateDialog.updatePersonalCSVFile(personalDatabase);

                        initializeGeneralDatabase();

                        JOptionPane.showMessageDialog(
                                null,
                                bundle.getString("reviewRemovedMessage"),
                                bundle.getString("reviewRemovedTitle"),
                                JOptionPane.INFORMATION_MESSAGE);
                        SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
                    }
                });
                buttonPanel.add(removeReviewButton);
            }

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(textArea, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            JDialog dialog = new JDialog();
            dialog.setTitle(bundle.getString("personalBookInfo"));
            dialog.setModal(true);
            dialog.getContentPane().add(panel);
            dialog.getContentPane().setPreferredSize(new Dimension(320, 200));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, bundle.getString("noInfoAvailable"), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddOperation() {
        if (currentUsername.equals("admin")) {
            int lastBookID = CSVProcessor.getLastBookID();
            AddDialog addDialog = new AddDialog(lastBookID + 1, currentUsername, PersonalDatabase, table, bundle);
            addDialog.setVisible(true);
        } else {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int bookID = (int) table.getValueAt(selectedRow, 0);
                if (PersonalDatabase.get(currentUsername) != null
                        && PersonalDatabase.get(currentUsername).containsKey(bookID)) {
                    JOptionPane.showMessageDialog(null, bundle.getString("duplicateBookMessage"), "Duplicate Book",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Book book = generalDatabase.get(bookID);

                // Construct the confirmation message with the book title and author
                String title = bundle.getString("label.title") + book.getTitle() + "\n";
                String author = bundle.getString("label.author") + book.getAuthor() + "\n";
                String confirmationMessage = bundle.getString("confirmationMessage") + "\n\n" + title + author;

                // Create the custom confirmation dialog
                CustomConfirmDialog confirmDialog = new CustomConfirmDialog(this, "Confirmation", confirmationMessage);
                confirmDialog.setVisible(true);

                // Check if the user confirmed
                if (confirmDialog.isConfirmed()) {
                    addBookToPersonalDatabase(currentUsername, bookID, book);
                }
            } else {
                JOptionPane.showMessageDialog(null, bundle.getString("noBookSelectedMessage"),
                        bundle.getString("noBookSelectedTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void addBookToPersonalDatabase(String username, int userBookId, Book book) {
        if (!PersonalDatabase.containsKey(username)) {
            PersonalDatabase.put(username, new HashMap<>());
        }
        PersonalDatabase.get(username).put(userBookId, new PersonalBook(book));

        savePersonalDatabaseToCSV(username);

        String successMessage = bundle.getString("success.addBookToPersonalDatabase");
        JOptionPane.showMessageDialog(null, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void savePersonalDatabaseToCSV(String username) {
        Map<Integer, PersonalBook> personalDatabase = PersonalDatabase.get(username);
        File csvFile = new File("data/personal_databases.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            Set<String> existingEntries = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingEntries.add(line);
                }
            }

            for (Map.Entry<Integer, PersonalBook> entry : personalDatabase.entrySet()) {
                int userBookId = entry.getKey();
                PersonalBook personalBook = entry.getValue();
                String newEntry = userBookId + "," + username + "," + personalBook.toCSVString();
                String entryPrefix = userBookId + "," + username + ",";

                if (!existingEntries.stream().anyMatch(suffix -> suffix.startsWith(entryPrefix))) {
                    writer.write(newEntry + "\n");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteOperation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int bookID = (int) table.getValueAt(selectedRow, 0);
            String confirmationMessage = bundle.getString("confirmation.deleteBook");
            int option = JOptionPane.showConfirmDialog(null, confirmationMessage, "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                generalDatabase.remove(bookID);
                for (Map.Entry<String, Map<Integer, PersonalBook>> userEntry : PersonalDatabase.entrySet()) {
                    Map<Integer, PersonalBook> userDatabase = userEntry.getValue();
                    userDatabase.entrySet().removeIf(entry -> entry.getKey().equals(bookID));
                }
                UpdateDialog.updateGeneralCSVFile(generalDatabase);
                UpdateDialog.updatePersonalCSVFile(PersonalDatabase);
                initializeGeneralDatabase();
                String successMessage = bundle.getString("success.deleteBook");
                JOptionPane.showMessageDialog(null, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);

            }
        } else {
            String warningMessage = bundle.getString("warning.noBookSelected");
            JOptionPane.showMessageDialog(null, warningMessage, "No Book Selected", JOptionPane.WARNING_MESSAGE);

        }
    }

    @Override
    public void create() {
        handleAddOperation();
    }

    @Override
    public void read() {
        initializeGeneralDatabase();
    }

    @Override
    public void update(DefaultTableModel model) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableMouseClicked(e, model);
            }
        });

    }

    @Override
    public void delete() {
        handleDeleteOperation();
    }

}
