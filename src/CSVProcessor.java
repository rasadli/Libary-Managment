package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class CSVProcessor {

    public static Map<String, Map<Integer, PersonalBook>> personalDatabases = new HashMap<>();
    public static Map<Integer, Book> generalDatabase = new HashMap<>();
    public static List<String> userDatabase = new ArrayList<>();

    public CSVProcessor() {
        readDataFromCSV();
        readPersonalDataFromCSV();
        readUserDataFromCSV();
    }

    public static final String USER_DATABASE_FILE = "data/user_database.csv";

    public static void readUserDataFromCSV() {
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(USER_DATABASE_FILE))) {
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);
                String username = data[1].trim();
                userDatabase.add(username);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String GENERAL_DATABASE_FILE = "data/general_database.csv";

    public static void readDataFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(GENERAL_DATABASE_FILE)))) {
            String line;
            boolean isFirstLine = true; // Flag to skip the header line
            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                processCSVLine(line, generalDatabase);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static void processCSVLine(String line, Map<Integer, Book> generalDatabase) {
        int bookID = Integer.parseInt(line.split(",")[0]);

        String userReviews = extractUserData(line);

        String lineWithoutExtractedParts = line.replace(userReviews, "");
        Book book = Book.fromCSVString(lineWithoutExtractedParts, userReviews);
        generalDatabase.put(bookID, book);

    }

    public static int getLastBookID() {
        return generalDatabase.keySet().stream()
                .max(Integer::compareTo) // Find the maximum book ID
                .orElse(0); // If there are no keys, return 0
    }

    public static final String PERSONAL_DATABASE_FILE = "data/personal_databases.csv";

    public static void readPersonalDataFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(PERSONAL_DATABASE_FILE)))) {
            String line;
            boolean isFirstLine = true; // Flag to skip the header line
            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                processPersonalCSVLine(line, personalDatabases);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static void processPersonalCSVLine(String line, Map<String, Map<Integer, PersonalBook>> personalDatabases) {

        String userReviews = extractUserData(line);

        String userReview = extractUserReview(line);

        String lineWithoutExtractedParts = line.replace(userReviews, "").replace(userReview, "");

        int userBookID = Integer.parseInt(lineWithoutExtractedParts.split(",")[0]);
        String username = lineWithoutExtractedParts.split(",")[1];

        PersonalBook personalBook = PersonalBook.fromCSVString(lineWithoutExtractedParts, userReviews, userReview);

        personalDatabases.computeIfAbsent(username, k -> new HashMap<>()).put(userBookID, personalBook);

    }

    private static String extractUserData(String line) {
        int startIndexOfFirstDelimiter = line.indexOf("{|^$^|");
        int endIndexOfFirstDelimiter = line.indexOf("|^$^|}");

        String userReviews;
        if (startIndexOfFirstDelimiter != -1 && endIndexOfFirstDelimiter != -1) {
            userReviews = line.substring(startIndexOfFirstDelimiter, endIndexOfFirstDelimiter + "|^$^|}".length());
        } else {
            userReviews = "";
        }
        return userReviews;
    }

    private static String extractUserReview(String line) {
        int startIndexOfSecondDelimiter = line.indexOf("{<|$|>");
        int endIndexOfSecondDelimiter = line.indexOf("<|$|>}");

        String userReview;
        if (startIndexOfSecondDelimiter != -1 && endIndexOfSecondDelimiter != -1) {
            userReview = line.substring(startIndexOfSecondDelimiter, endIndexOfSecondDelimiter + "<|$|>}".length());
        } else {
            userReview = "";
        }
        return userReview;
    }

    public static void processCSV(String filename, JTable table,ResourceBundle bundle) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        boolean isFirstLine = true;
        String outputFilename = "data/general_database.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(filename));
                BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilename))) {
            bw.write("BookID,Title,Author,Rating,Review\n");
            int bookID = 1;
            String line;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length == 1) {
                    String title = parts[0].trim();
                    model.addRow(
                        new Object[] { 
                            bookID, 
                            bundle.getString("book.unknown"), // Title (if unknown)
                            bundle.getString("book.unknown"), // Author (if unknown)
                            bundle.getString("book.noRating"), // Rating (if not available)
                            bundle.getString("book.noReview")  // Review (if not available)
                        });
                    bw.write(String.format("%d,%s,Unknown,No Rating,No Review\n", bookID, removeQuotationMarks(title)));
                    bookID++;
                } else if (parts[0].isEmpty()) {
                    String author = parts[1].trim();
                    model.addRow(new Object[] { bookID, bundle.getString("book.unknown"), author, bundle.getString("book.noRating"), bundle.getString("book.noReview") });
                    bw.write(String.format("%d,Unknown,%s,No Rating,No Review\n", bookID, author));
                    bookID++;
                } else {
                    String author = parts[parts.length - 1].trim();
                    if (author.isEmpty()) {
                        author = bundle.getString("book.unknown");
                    }
                    for (int i = 0; i < parts.length - 1; i++) {
                        String title = parts[i].trim();
                        if (title.isEmpty()) {
                            continue;
                        }
                        model.addRow(
                                new Object[] { bookID, removeQuotationMarks(title), author, bundle.getString("book.noRating"), bundle.getString("book.noReview")  });
                        bw.write(String.format("%d,%s,%s,No Rating,No Review\n", bookID, removeQuotationMarks(title),
                                author));
                        bookID++;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String removeQuotationMarks(String input) {
        return input.replaceAll("\"", "");
    }
}
