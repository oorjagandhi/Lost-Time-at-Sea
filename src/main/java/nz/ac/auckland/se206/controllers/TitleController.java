package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TitleController {

  @FXML private AnchorPane titleScreen;

  @FXML
  public void onKeyPressed(KeyEvent event) {
    // Ensure titleScreen is not null
    if (titleScreen == null) {
      System.err.println("titleScreen is not initialized. Check fx:id.");
      return;
    }

    // Check if the Enter key is pressed
    if (event.getCode() == KeyCode.ENTER) {
      // Switch to the crime scene screen
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
        Parent crimeSceneRoot = loader.load();

        // Get the current stage
        Stage stage = (Stage) titleScreen.getScene().getWindow();

        // Create the new scene for the crime scene
        Scene newScene = new Scene(crimeSceneRoot);

        // Load and add the CSS stylesheet to the new scene
        newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set the new scene (crime scene)
        stage.setScene(newScene);
        stage.show();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
