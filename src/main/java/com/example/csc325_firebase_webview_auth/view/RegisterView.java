package com.example.csc325_firebase_webview_auth.view;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterView {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void register() {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Enter email + password");
                return;
            }
            if (password.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters");
                return;
            }

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = App.fauth.createUser(request);
            statusLabel.setText("✅ User created: " + userRecord.getUid());

        } catch (FirebaseAuthException e) {
            statusLabel.setText("❌ " + e.getAuthErrorCode() + ": " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("❌ Registration failed");
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() throws Exception {
        App.setRoot("/files/AccessFBView.fxml");
    }
}