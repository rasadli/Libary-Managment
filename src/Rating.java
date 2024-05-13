package src
;
import java.util.HashMap;
import java.util.Map;

public class Rating {
    private Map<String, Double> userRatings;
    private double averageRating;
    private int ratingCount;

    public Rating() {
        this.userRatings = new HashMap<>();
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    public Map<String, Double> getUserRatings() {
        return userRatings;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void addUserRating(String username, double rating) {
        if (userRatings.containsKey(username)) {
            // If the user already has a rating, update it
            userRatings.put(username, rating);
        } else {
            // If the user doesn't have a rating, add a new one
            userRatings.put(username, rating);
            ratingCount++; // Increment the rating count
            // System.out.println("Ratings in " + userRatings);
            // System.out.println("Ratings in " + rating);
        }
        recalculateAverageRating();

    }

    public void removeUserRating(String username) {
        userRatings.remove(username);
        recalculateAverageRating();
    }

    private void recalculateAverageRating() {
        double sum = 0.0;
        for (double rating : userRatings.values()) {
            sum += rating;
        }
        ratingCount = userRatings.size();
        averageRating = ratingCount > 0 ? sum / ratingCount : 0.0;
    }

    @Override
    public String toString() {
        if (userRatings.isEmpty()) {
            return "No Rating";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Double> entry : userRatings.entrySet()) {
            sb.append("<").append(entry.getKey()).append("/").append(entry.getValue()).append(">_");
        }
        // Remove the trailing '_' character
        sb.setLength(sb.length() - 1);
        sb.append("||(").append(averageRating).append("/").append(ratingCount).append(")}");
        return sb.toString();
    }

    public static Rating fromString(String ratingString) {
        String cleanedString = ratingString.substring(ratingString.indexOf("{") + 1, ratingString.lastIndexOf("}"));
    
        // Split by "||"
        String[] parts = cleanedString.split("\\|\\|");
    
        // Extract user ratings and average rating
        String userRatingsString = parts[0];
        String averageRatingString = parts[1];
    
        // Remove parentheses from average rating string
        String[] averagePart = averageRatingString.substring(1, averageRatingString.length() - 1).split("/");
    
        // Parse average rating and rating count
        double averageRating = Double.parseDouble(averagePart[0]);
        int ratingCount = Integer.parseInt(averagePart[1]);
    
        // Parse user ratings
        String[] userRatingsPart = userRatingsString.split("_");
        Rating rating = new Rating();
    
        for (String userRating : userRatingsPart) {
            String[] userRatingParts = userRating.substring(1, userRating.length() - 1).split("/");
            String username = userRatingParts[0];
            double ratingValue = Double.parseDouble(userRatingParts[1]);
            rating.addUserRating(username, ratingValue);
        }
    
        rating.averageRating = averageRating;
        rating.ratingCount = ratingCount;
    
        // System.out.println(rating);
        return rating;
    }
    
}
