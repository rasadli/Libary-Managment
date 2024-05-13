    package src;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Objects;
    import java.util.concurrent.TimeUnit;

    public class PersonalBook {
        private Book book;
        private String status;
        private long timeSpent;
        private String startDate;
        private String endDate;
        private double userRating;
        private String userReview;

        public PersonalBook(Book book) {
            this.book = Objects.requireNonNull(book, "Book cannot be null");
            this.status = "Not Started";
            this.timeSpent = 0;
            this.startDate = "";
            this.endDate = "";
            this.userReview = "Add Review";
        }

        public Book getBook() {
            return book;
        }

        public void setBook(Book book) {
            this.book = Objects.requireNonNull(book, "Book cannot be null");
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTimeSpent() {
            return timeSpent == 0 ? "" : String.valueOf(timeSpent);
        }

        public void setTimeSpent(int timeSpent) {
            if (timeSpent < 0) {
                throw new IllegalArgumentException("Time spent cannot be less than zero.");
            }
            this.timeSpent = timeSpent;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public double getUserRating() {
            return userRating;
        }

        public void setUserRating(double userRating) {
            this.userRating = userRating;
        }

        public String getUserReview() {
            return (userReview == null) ? "Add Review" : userReview; // Return "Add Review" if the review is null
        }

        public void setUserReview(String userReview) {
            this.userReview = userReview;
        }

        // Add a method to calculate time spent in PersonalBook class
        public void calculateTimeSpent() {
            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                    Date start = sdf.parse(startDate);
                    Date end = sdf.parse(endDate);
                    long diffInMillies = Math.abs(end.getTime() - start.getTime());
                    long diffInDays = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    timeSpent = diffInDays;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
            if (startDate.isEmpty()) {
                status = "Not Started";
            } else {
                status = "Ongoing";
            }
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
            if (endDate.isEmpty() && !startDate.isEmpty()) {
                status = "Ongoing";
                timeSpent = 0; // Reset time spent if end date is deleted
            } else if (!endDate.isEmpty() && !startDate.isEmpty()) {
                status = "Completed";
                calculateTimeSpent(); // Calculate time spent if end date is entered
            }
        }

        @Override
        public String toString() {
            return "PersonalBook{" +
                    "book=" + book +
                    ", status='" + status + '\'' +
                    ", timeSpent=" + timeSpent +
                    ", startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    ", userRating=" + userRating +
                    ", userReview='" + userReview + '\'' +
                    '}';
        }

        public String toStringUserReview() {
            return "{<|$|>" + userReview + "<|$|>}";
        }

        public String toCSVString() {
            // Convert attributes to CSV format
            String startDateStr = startDate != null ? startDate.toString() : "";
            return String.format("%s,%s,%d,%s,%s,%.2f,\"%s\"", book.toCSVString(), status, timeSpent, startDateStr, endDate,
                    userRating, toStringUserReview());
        }

        public static PersonalBook fromCSVString(String csvString, String userReviewsString, String userReviewString) {
            String[] parts = csvString.split(",");

            // Extract the required parts
            String extractedPart = parts[0] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "\"";
            // System.out.println("personal excrated: " + extractedPart);
            Book book = Book.fromCSVString(extractedPart, userReviewsString);

            String status = parts[6].trim();
            int timeSpent = Integer.parseInt(parts[7].trim());
            String startDate = parts[8].trim();
            String endDate = parts[9].trim();
            double userRating = Double.parseDouble(parts[10].trim());
            String userReview = userReviewString.replace("{<|$|>", "").replace("<|$|>}", "");

            PersonalBook personalBook = new PersonalBook(book);
            personalBook.setStatus(status);
            personalBook.setTimeSpent(timeSpent);
            personalBook.setStartDate(startDate);
            personalBook.setEndDate(endDate);
            personalBook.setUserRating(userRating);
            personalBook.setUserReview(userReview);

            return personalBook;
        }

    }