package src;

import java.util.HashMap;
import java.util.Map;

public class Review {
    private Map<String, String> userReviews;

    public Review() {
        this.userReviews = new HashMap<>();
    }

    public Map<String, String> getUserReviews() {
        return userReviews;
    }

    public void addUserReview(String username, String review) {
        // System.out.println("usernamee : "+username);
        // System.out.println("reviewww : " + review);
        userReviews.put(username, review);
    }

    public void removeUserReview(String username) {
        userReviews.remove(username);
    }

    public String toTableString() {
        // System.out.println("Table String: " + userReviews);
        if (userReviews.isEmpty()) {
            return "No Review";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : userReviews.entrySet()) {
            sb.append(entry.getKey()).append(", ");
        }
        // Remove the trailing comma and space
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    @Override
    public String toString() {
        if (userReviews.isEmpty()) {
            return "No Review";
        }
    
        StringBuilder sb = new StringBuilder();
        sb.append("{|^$^|");
        for (Map.Entry<String, String> entry : userReviews.entrySet()) {
            // System.out.println("Rkey "+ entry.getKey());
            // System.out.println("Rkey "+ entry.getValue());

            sb.append("<").append(entry.getKey()).append("/").append(entry.getValue()).append(">_");
        }
        // Remove the trailing '_' character
        if (sb.length() > 5) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("|^$^|}");
        // System.out.println("reviewssss: " + sb);
        return sb.toString();
    }
    
    public static Review fromString(String reviewString) {
        Review review = new Review();
        if (!reviewString.isEmpty() && !reviewString.equals("No Review")) {
            // Remove the leading and trailing delimiters
            reviewString = reviewString.substring(6, reviewString.length() - 6);
            // System.out.println("R string "+reviewString);
            String[] parts = reviewString.split("_");
            for (String part : parts) {
                // System.out.println("p "+part);
                // Check if part contains a "/"
   
    
                if (part.contains("/")) {
                    String[] pair = part.substring(1, part.length() - 1).split("/");
                    if (pair.length == 2) { // Ensure there are two elements after splitting
                        // System.out.println("p "+pair[0]);

                        review.addUserReview(pair[0], pair[1]);
                        // System.out.println("r"+review);

                    }
                }
            }
        }
        return review;
    }
    
    

}
