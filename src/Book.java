package src;

public class Book {
    private String title;
    private String author;
    private Rating ratings;
    private Review reviews;

    public Book(String title, String author, Rating ratings, Review reviews) {
        this.title = (title.isEmpty() ? "Unknown" : title);
        this.author = (author.isEmpty() ? "Unknown" : author);
        this.ratings = ratings;
        this.reviews = reviews;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Rating getRatings() {
        return ratings;
    }

    public Review getReviews() {
        return reviews;
    }

    public void addRating(String username, double rating) {
        ratings.addUserRating(username, rating);
    }

    public void removeRating(String username) {
        ratings.removeUserRating(username);
    }

    public void addReview(String username, String review) {
        reviews.addUserReview(username, review);
    }

    public void removeReview(String username) {
        reviews.removeUserReview(username);
    }

    public String calculateAverageRating() {
        return String.format("%.2f", ratings.getAverageRating());
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", ratings=" + ratings +
                ", reviews=" + reviews +
                '}';
    }

    public String toCSVString() {
        String titleStr = title.equals("Unknown") ? "" : title;
        String authorStr = author.equals("Unknown") ? "" : author;
        String reviewsStr = reviews.toString().isEmpty() ? "No Review" : reviews.toString();
        String ratingStr = ratings.getAverageRating() == 0.0 ? "No Rating" : ratings.toString();

        return String.format("%s,%s,\"%s\",\"%s\"", titleStr, authorStr, ratingStr, reviewsStr);
    }

    public static Book fromCSVString(String csvString, String userReviewsStr) {
        String[] parts = csvString.split(",");

        String title = parts[1].isEmpty() ? "Unknown" : parts[1].trim(); // Use index 0 for title
        String author = parts[2].isEmpty() ? "Unknown" : parts[2].trim(); // Use index 1 for author
        String ratingStr = parts[3].trim().replace("\"", "");
        String reviewsStr = userReviewsStr;
    
        Rating ratings = new Rating();
        if (!ratingStr.equals("No Rating")) {
            ratings = Rating.fromString(ratingStr);
        }
    
        Review reviews = new Review();
        if (!reviewsStr.equals("No Review")) {
            reviews = Review.fromString(reviewsStr);
        }
    
        return new Book(title, author, ratings, reviews);
    }
    

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRatings(Rating ratings) {
        this.ratings = ratings;
    }

    public void setReviews(Review reviews) {
        this.reviews = reviews;
    }
}
