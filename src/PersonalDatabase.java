package src;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

public class PersonalDatabase extends JFrame implements CRUD {
    private Map<String, Map<Integer, PersonalBook>> personalDatabase;
    private JTable table;
    private JTextField searchField;
    private String currentUsername;
    private Map<Integer, Book> generalDatabase;
    private RatingReviewDialog ratingReviewDialog; // Add RatingReviewDialog instance
    private TableRowSorter<DefaultTableModel> sorter; // Table row sorter for sorting and filtering

    private ResourceBundle bundle;

    private Map<Integer, SortOrder> sortOrderMap = new HashMap<>();
    private List<Integer> sortSequence = new ArrayList<>();

    public PersonalDatabase(String username, Map<Integer, Book> generalDatabase, ResourceBundle bundle) {
        this.personalDatabase = CSVProcessor.personalDatabases;
        this.generalDatabase = generalDatabase;
        this.currentUsername = username;
        this.bundle = bundle;
        initializeUI();
        read();
        // Initialize RatingReviewDialog with the required parameters
        this.ratingReviewDialog = new RatingReviewDialog(generalDatabase, personalDatabase, table, currentUsername,
                bundle);
        setupTableSorting();

    }

    private void initializeUI() {
        setTitle(bundle.getString("personal_database_title"));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel();
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 7;
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
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase(); // Get the search query in lowercase
                System.out.println(query);
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

        JButton addButton = new JButton(bundle.getString("button.add"));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });

        JButton deleteButton = new JButton(bundle.getString("delete"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });

        JButton logoutButton = new JButton(bundle.getString("logout"));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the current window
                dispose();
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new LoginPage(bundle).setVisible(true); // Assuming LoginPage is the class for your login page
                    });
                });
            }
        });

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(homeButton);
        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(logoutButton);
        getContentPane().add(controlPanel, BorderLayout.NORTH);

        String[] columnNames = {
                bundle.getString("column.bookID"), bundle.getString("column.title"),
                bundle.getString("column.author"), bundle.getString("column.rating"),
                bundle.getString("column.reviews"), bundle.getString("column.status"),
                bundle.getString("column.timeSpent"), bundle.getString("column.startDate"),
                bundle.getString("column.endDate"), bundle.getString("column.userRating"),
                bundle.getString("column.userReview")
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

        // Build the list of sort keys in the correct order based on click sequence
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        for (int colIndex : sortSequence) { // Use the sequence list to maintain precedence
            if (sortOrderMap.containsKey(colIndex)) { // Ensure column is still in sortOrderMap
                sortKeys.add(new RowSorter.SortKey(colIndex, sortOrderMap.get(colIndex)));
            }
        }

        sorter.setSortKeys(sortKeys); // Apply the updated list of sort keys
    }

    private void handleTableMouseClicked(MouseEvent e, DefaultTableModel model) {
        int column = table.getColumnModel().getColumnIndexAtX(e.getX());
        int row = e.getY() / table.getRowHeight();

        // Check if the click is within valid row and column indices
        if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
            int idValue = (int) table.getValueAt(row, 0);
            Book book = generalDatabase.get(idValue);

            // Check if the click is on the "Reviews" column
            if (column == table.getColumnCount() - 1) {
                // Open modal dialog for adding review
                ReopenReviewDialog(row, idValue, (DefaultTableModel) table.getModel(), book);
            }
            // Check if the click is on the "Ratings" column
            else if (column == table.getColumnCount() - 2) {
                // Open modal dialog for adding rating
                openRatingDialog(row, idValue, (DefaultTableModel) table.getModel(), book);
            }
            // Check if the click is on the start date or end date column
            else if (column == START_DATE_COLUMN_INDEX || column == END_DATE_COLUMN_INDEX) {
                // Open date input dialog
                openDateInputDialog(row, column);
            }
        }

        // Check if the click is on the "Reviews" column
        if (column == 4) {
            String usersLine = (String) table.getValueAt(row, column);
            // Split the reviewers by comma and space
            String[] users = usersLine.split(", ");

            Rectangle cellRect = table.getCellRect(row, column, true);
            int clickX = e.getX() - cellRect.x;
            int currentPosition = 0;
            int bookID = (int) table.getValueAt(row, 0);
            for (String user : users) {
                int reviewerWidth = table.getFontMetrics(table.getFont()).stringWidth(user);
                if (clickX >= currentPosition && clickX <= currentPosition + reviewerWidth) {
                    // Display additional information about the reviewer
                    GeneralDatabase.displayNameInfo(user, bookID, personalDatabase);
                    break;
                }
                currentPosition += reviewerWidth + 2; // Account for the comma and space
            }
        }
    }

    private void openDateInputDialog(int row, int column) {
        int bookID = (int) table.getValueAt(row, 0);
        // Retrieve the PersonalBook object associated with the given bookID
        PersonalBook personalBook = personalDatabase.get(currentUsername).get(bookID);

        // Get the start date and end date from the PersonalBook object
        String startDate = personalBook.getStartDate();
        String endDate = personalBook.getEndDate();

        // Create a new instance of DateInputDialog with the retrieved values
        DateInputDialog dateInputDialog = new DateInputDialog(personalDatabase, currentUsername, bookID, table, this,startDate, endDate, bundle);
        dateInputDialog.open();
    }

    private void ReopenReviewDialog(int row, int idValue, DefaultTableModel model, Book book) {
        ratingReviewDialog.openReviewDialog(row, idValue, model, book); 
    }

    private void openRatingDialog(int row, int idValue, DefaultTableModel model, Book book) {
        ratingReviewDialog.openRatingDialog(row, idValue, model, book); 
    }

    private void addNewBookToPersonalDatabase() {
        int lastBookID = CSVProcessor.getLastBookID();
        AddDialog addDialog = new AddDialog(lastBookID + 1, currentUsername, personalDatabase, table,bundle);
        addDialog.setVisible(true);
    }

    public static void showPersonalDatabase(Map<String, Map<Integer, PersonalBook>> personalDatabase,
            String currentUsername, JTable table,ResourceBundle bundle) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        Map<Integer, PersonalBook> userPersonalDatabase = personalDatabase.get(currentUsername);
        if (userPersonalDatabase != null) {
            for (Map.Entry<Integer, PersonalBook> entry : userPersonalDatabase.entrySet()) {
                PersonalBook personalBook = entry.getValue();
                Book book = personalBook.getBook();
                String ratings = book.getRatings().getAverageRating() == 0.0 ? bundle.getString("book.noRating")
                        : book.calculateAverageRating() + " (" + book.getRatings().getRatingCount() + ")";

                String reviews = book.getReviews().toString().isEmpty() || book.getReviews().toString().equals("No Review") ? bundle.getString("book.noReview")
                        : book.getReviews().toTableString();

                double userRating = personalBook.getUserRating();
                String userReview = personalBook.getUserReview();
                String displayReview;
                displayReview = userReview == null || userReview.isEmpty() ? bundle.getString("addReview") : userReview;
                if (userReview.equals("Add Review")) {
                    displayReview = bundle.getString("addReview");
                }
                String displayRating = userRating == 0.0 ? bundle.getString("addRating") : String.format("%.2f", userRating);

                String status = "";
                switch (personalBook.getStatus()) {
                    case "Not Started":
                        status = bundle.getString("book.notStarted");
                        break;
                    case "Ongoing":
                        status = bundle.getString("book.Ongoing");
                        break;
                    case "Completed":
                        status = bundle.getString("book.Completed");
                        break;
                    default:
                        break;
                }

                model.addRow(new Object[] {
                        entry.getKey(),
                        personalBook.getBook().getTitle().equals("Unknown") ? bundle.getString("book.unknown") : personalBook.getBook().getTitle(),
                        personalBook.getBook().getAuthor().equals("Unknown") ? bundle.getString("book.unknown"): personalBook.getBook().getAuthor(),
                        ratings,
                        reviews,
                        status, // Status
                        personalBook.getTimeSpent(), // Time spent
                        personalBook.getStartDate(), // Start date
                        personalBook.getEndDate(), // End date
                        displayRating,
                        displayReview
                });

            }
        }
    }

    private void handleDeleteOperation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int userBookID = (int) table.getValueAt(selectedRow, 0);
            String title = (String) table.getValueAt(selectedRow, 1);

            CustomConfirmDialog confirmDialog = new CustomConfirmDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(table),
                    bundle.getString("delete.confirmTitle"),
                    bundle.getString("delete.confirmMessage") + " '" + title + "'?");
            confirmDialog.setVisible(true);

            if (confirmDialog.isConfirmed()) {
                // Remove the book from the personal database of the current user
                if (personalDatabase.get(currentUsername).containsKey(userBookID)) {
                    if (userBookID>0) {
                        Book book = generalDatabase.get(userBookID);
                        // Retrieve the review and rating from the book
                        Review reviews = book.getReviews();
                        Rating ratings = book.getRatings();
                        ratings.removeUserRating(currentUsername);
                        reviews.removeUserReview(currentUsername);
                    }


                    UpdateDialog.updateGeneralCSVFile(generalDatabase);
                    personalDatabase.get(currentUsername).remove(userBookID);
                    UpdateDialog.updatePersonalCSVFile(personalDatabase);
                    read();
                    JOptionPane.showMessageDialog(null,
                            bundle.getString("delete.successMessage"), "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            bundle.getString("delete.errorNotExistMessage"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, bundle.getString("delete.noBookSelectedMessage"),
                    bundle.getString("delete.noBookSelectedTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void create() {
        addNewBookToPersonalDatabase();
    }

    @Override
    public void read() {
        showPersonalDatabase(personalDatabase, currentUsername, table, bundle);
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

    // Constants for column indices
    private static final int START_DATE_COLUMN_INDEX = 7;
    private static final int END_DATE_COLUMN_INDEX = 8;

}
