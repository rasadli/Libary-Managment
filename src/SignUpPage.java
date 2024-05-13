package src;

import javax.swing.*;

import src.CustomExceptions.UsernameUnavailableException;
import src.CustomExceptions.WeakPasswordException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.UUID;

public class SignUpPage extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;
    private JPasswordField reEnterPasswordField;
    private ResourceBundle bundle;

    public SignUpPage(JFrame loginPageFrame, ResourceBundle bundle) {
        this.bundle=bundle;

        setTitle(bundle.getString("signup.title"));
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(bundle.getString("signup.title"));
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel nameLabel = new JLabel(bundle.getString("signup.username"));
        nameField = new JTextField(20);
        JLabel passwordLabel = new JLabel(bundle.getString("signup.password"));
        passwordField = new JPasswordField(20);
        JLabel reEnterPasswordLabel = new JLabel(bundle.getString("signup.reEnterPassword"));
        reEnterPasswordField = new JPasswordField(20);

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(reEnterPasswordLabel);
        inputPanel.add(reEnterPasswordField);

        mainPanel.add(inputPanel);
        JLabel haveAccountLabel = new JLabel(bundle.getString("haveAccountLabel"));
        haveAccountLabel.setForeground(Color.BLUE);
        haveAccountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        haveAccountLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent event) {
                dispose();
                loginPageFrame.setVisible(true);
            }
        });

        JButton signUpButton = new JButton(bundle.getString("signup.button.signup"));
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(haveAccountLabel);
        mainPanel.add(signUpButton);

        signUpButton.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            String reEnteredPassword = new String(reEnterPasswordField.getPassword());

            try {
                checkPasswordsMatch(password, reEnteredPassword);
                checkPasswordStrength(password);
                checkUsernameAvailability(name);

                // Generate unique UserID
                String userID = UUID.randomUUID().toString(); // Generates a unique random UUID
                String userData = userID + "," + name + "," + password;

                try (PrintWriter printWriter = new PrintWriter(new FileWriter("data/user_database.csv", true))) {
                    printWriter.println(userData);
                    JOptionPane.showMessageDialog(this, bundle.getString("signup.successMessage"), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    LoginPage loginPage = new LoginPage(bundle);
                    loginPage.setVisible(true);
                } catch (IOException ex) {
                    throw new RuntimeException("Error writing to user database", ex);
                }
            } catch (UsernameUnavailableException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (WeakPasswordException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Weak Password", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        add(mainPanel);
        pack();
    }

    private void checkPasswordsMatch(String password, String reEnteredPassword) throws RuntimeException {
        if (!password.equals(reEnteredPassword)) {
            throw new RuntimeException(bundle.getString("signup.error.passwordMismatch"));
        }
    }

    private void checkPasswordStrength(String password) throws WeakPasswordException {
        String strengthMessage = getPasswordStrengthMessage(password);
        if (!strengthMessage.equals("strong")) {
            throw new WeakPasswordException(bundle.getString("signup.error.weakPassword") + " " + strengthMessage);
        }
    }

    private void checkUsernameAvailability(String username) throws UsernameUnavailableException {
        if (!isUsernameAvailable(username)) {
            throw new UsernameUnavailableException(bundle.getString("signup.error.usernameTaken"));
        }
    }

    private String getPasswordStrengthMessage(String password) {
        StringBuilder message = new StringBuilder("strong");

        // Password should be at least 8 characters long
        if (password.length() < 8) {
            message = new StringBuilder(bundle.getString("signup.error.passwordTooShort") + "; ");
        }

        // Password should contain at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            message = new StringBuilder(bundle.getString("signup.error.missingUppercaseLetter") + "; ");
        }

        // Password should contain at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            message = new StringBuilder(bundle.getString("signup.error.missingLowercaseLetter") + "; ");
        }

        // Password should contain at least one digit
        if (!password.matches(".*\\d.*")) {
            message = new StringBuilder(bundle.getString("signup.error.missingDigit") + "; ");
        }

        // Remove the trailing semicolon and space
        if (!message.toString().equals("strong")) {
            message.delete(message.length() - 2, message.length());
        }

        return message.toString();
    }

    private boolean isUsernameAvailable(String username) {
        File file = new File("data/user_database.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(username + ",")) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
