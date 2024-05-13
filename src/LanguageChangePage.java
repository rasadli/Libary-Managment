package src;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageChangePage extends JFrame {
    private JComboBox<String> languageSelector;

    public LanguageChangePage() {
        setTitle("Change Language");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel);

        JLabel titleLabel = new JLabel("Select Language:");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        languageSelector = new JComboBox<>(new String[]{"English", "Azərbaycan"});
        languageSelector.setSelectedIndex(0);
        languageSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(languageSelector, BorderLayout.CENTER);

        JButton changeButton = new JButton("Change Language");
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeLanguage();
            }
        });
        changeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(changeButton, BorderLayout.SOUTH);
    }

    private void changeLanguage() {
        String selectedLanguage = (String) languageSelector.getSelectedItem();
        Locale locale;
        if (selectedLanguage.equals("Azərbaycan")) {
            locale = new Locale("az");
        } else {
            locale = new Locale("en");
        }
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        
        // Reload the login page with the updated resource bundle
        new LoginPage(bundle).setVisible(true);

        // Close the language change page
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LanguageChangePage().setVisible(true);
        });
    }
}
