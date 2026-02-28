package com.example.csc325_firebase_webview_auth.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SignInController {

    private static final String FIREBASE_WEB_API_KEY = "AIzaSyClgM7fQSGH4Er9nJhI_-xWahcX0r00X4";

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void signIn() {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Enter email + password");
                return;
            }

            String endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                    + FIREBASE_WEB_API_KEY;

            String jsonBody = """
                    {"email":"%s","password":"%s","returnSecureToken":true}
                    """.formatted(escape(email), escape(password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Simple extraction (good enough for class projects)
                String body = response.body();
                String idToken = extractJsonValue(body, "idToken");

                App.signedInEmail = email;
                App.idToken = idToken;

                statusLabel.setText("✅ Signed in as: " + email);

                // go back to main screen
                App.setRoot("/files/AccessFBView.fxml");
            } else {
                statusLabel.setText("❌ Sign in failed: " + response.body());
            }

        } catch (Exception e) {
            statusLabel.setText("❌ Error signing in");
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() throws Exception {
        App.setRoot("/files/AccessFBView.fxml");
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }
}
