package src;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    @SuppressWarnings("unused")
    private ResourceBundle bundle;

    public LoginPage(ResourceBundle bundle) {
        this.bundle = bundle;

        setTitle(bundle.getString("login.title"));
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(bundle.getString("login.welcome"));
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        JPanel formPanel = new JPanel(new GridLayout(2, 1));
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel usernameLabel = new JLabel(bundle.getString("username"));
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel passwordLabel = new JLabel(bundle.getString("password"));
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        formPanel.add(usernamePanel);
        formPanel.add(passwordPanel);

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        JLabel signUpLabel = new JLabel(bundle.getString("login.signUp"), SwingConstants.CENTER);
        signUpLabel.setForeground(Color.BLUE);
        JButton loginButton = new JButton(bundle.getString("login.button.login"));

        signUpLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        signUpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        JFrame signUpFrame = new SignUpPage(LoginPage.this,bundle);
                        signUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        signUpFrame.setVisible(true);
                    });

                });
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, bundle.getString("login.fillInputs"));
                    return;
                }

                if (username.equals("admin") && password.equals("admin")) {
                    JOptionPane.showMessageDialog(null, bundle.getString("login.adminLoginSuccessful"));
                    dispose();
                    openMainPage(username); // Pass username to MainPage

                } else if (checkUsernameAndPassword(username, password)) {
                    JOptionPane.showMessageDialog(null, bundle.getString("login.loginSuccessful"));
                    dispose();
                    openMainPage(username); // Pass username to MainPage
                } else {
                    JOptionPane.showMessageDialog(null, bundle.getString("login.invalidUsernameOrPassword"));
                }
            }

            private void openMainPage(String username) {

                EventQueue.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        new MainPage(username,bundle).setVisible(true); // Pass username to MainPage
                    });
                });
            }
        });

        JPanel buttonCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonCenterPanel.add(loginButton);
        buttonsPanel.add(signUpLabel, BorderLayout.NORTH);
        buttonsPanel.add(buttonCenterPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private boolean checkUsernameAndPassword(String username, String password) {
        String csvFile = "data/user_database.csv";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] userData = line.split(",");
                String existingUsername = userData[1].trim();
                String existingPassword = userData[2].trim();
                if (username.equals(existingUsername) && password.equals(existingPassword)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

