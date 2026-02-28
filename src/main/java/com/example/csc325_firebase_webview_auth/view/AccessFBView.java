package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AccessFBView {

    @FXML
    private TextField nameField;

    @FXML
    private TextField majorField;

    @FXML
    private TextField ageField;

    @FXML
    private Button writeButton;

    @FXML
    private Button readButton;

    @FXML
    private Button switchroot;

    @FXML
    private TableView<Person> personTable;

    @FXML private TableColumn<Person, String> nameCol;

    @FXML private TableColumn<Person, String> majorCol;

    @FXML private TableColumn<Person, Integer> ageCol;

    @FXML
    private TextArea outputField;

    private boolean key;

    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();

    private Person person;

    public ObservableList<Person> getListOfUsers() {

        return listOfUsers;
    }

    public void initialize() {

        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        personTable.setItems(listOfUsers);

        if (App.signedInEmail != null) {
            outputField.setText("✅ Signed in as: " + App.signedInEmail + "\n");
        } else {
            outputField.setText("Not signed in.\n");
        }
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

        @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

            @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

     @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    @FXML
    private void uploadPicture() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Picture");
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            File file = chooser.showOpenDialog(nameField.getScene().getWindow());
            if (file == null) return;

            String safeUser = (App.signedInEmail != null) ? App.signedInEmail.replace("@", "_at_").replace(".", "_") : "guest";

            String ext = getFileExtension(file.getName());
            String objectName = "profilePictures/" + safeUser + "/" + UUID.randomUUID() + "." + ext;

            Bucket bucket = StorageClient.getInstance().bucket();
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            try (FileInputStream fis = new FileInputStream(file)) {
                Blob blob = bucket.create(objectName, fis, contentType);
                System.out.println("✅ Uploaded to: gs://" + bucket.getName() + "/" + objectName);

                outputField.setText("✅ Uploaded profile picture!\nPath: " + objectName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            outputField.setText("❌ Upload failed: " + e.getMessage());
        }
    }

    private static String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "png";
        return filename.substring(dot + 1).toLowerCase();
    }

    public void addData() {
        String name = nameField.getText();
        String major = majorField.getText();
        int age = Integer.parseInt(ageField.getText());

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", name);
        data.put("Major", major);
        data.put("Age", age);
        docRef.set(data);

        Person newPerson = new Person(name, major, age);
        listOfUsers.add(newPerson);
    }

        public boolean readFirebase()
         {
             key = false;
             listOfUsers.clear();
             outputField.clear();

        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        List<QueryDocumentSnapshot> documents;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents) {
                    Person p = new Person(
                            String.valueOf(document.getData().get("Name")),
                            String.valueOf(document.getData().get("Major")),
                            Integer.parseInt(String.valueOf(document.getData().get("Age")))
                    );
                    listOfUsers.add(p);
                }
            }
            else
            {
               System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
             ex.printStackTrace();
        }
        return key;
    }

        public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
        } catch (Exception e) {
        }
    }

    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("user@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;

        } catch (FirebaseAuthException ex) {
            return false;
        }
    }

    @FXML
    private void openRegister() throws IOException {
        App.setRoot("/files/RegisterView.fxml");
    }

    @FXML
    private void openSignIn() throws IOException {
        App.setRoot("/files/SignInView.fxml");
    }

    @FXML
    private void closeApp() {
        javafx.application.Platform.exit();
    }
    @FXML
    private void clearOutput() {
        outputField.clear();
        listOfUsers.clear();
    }

    @FXML
    private void deleteSelected() {
        if (personTable == null) return;

        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        personTable.getItems().remove(selected);
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("CSC325 Firebase App");
        alert.setContentText("Firestore + Auth + JavaFX\nMade by: Daniel Gron");
        alert.showAndWait();
    }
}
