package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TitleController {

  @FXML private AnchorPane titleScreen; // Make sure this matches fx:id in the FXML

  private MediaPlayer preloadedMediaPlayer;

  @FXML
  public void initialize() {
    // Preload the video to avoid delay
    try {
      String mediaPath = getClass().getResource("/videos/cutscene.mp4").toExternalForm();
      Media media = new Media(mediaPath);
      preloadedMediaPlayer = new MediaPlayer(media);
    } catch (NullPointerException e) {
      System.err.println("Error: Video file not found at the specified location.");
      e.printStackTrace();
    }
  }

  @FXML
  public void onKeyPressed(KeyEvent event) {
    // Ensure titleScreen is not null
    if (titleScreen == null) {
      System.err.println("titleScreen is not initialized. Check fx:id.");
      return;
    }

    // Check if the Enter key is pressed
    if (event.getCode() == KeyCode.ENTER) {
      // Create a black Pane that covers the entire title screen
      Pane blackOverlay = new Pane();
      blackOverlay.setStyle("-fx-background-color: black;");
      blackOverlay.setOpacity(0);
      blackOverlay.setPrefSize(titleScreen.getWidth(), titleScreen.getHeight());

      // Add the black overlay to the titleScreen
      titleScreen.getChildren().add(blackOverlay);

      // Bring the black overlay to the front
      blackOverlay.toFront();

      // Create a FadeTransition to fade in the black overlay
      FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), blackOverlay);
      fadeIn.setFromValue(0.0);
      fadeIn.setToValue(1.0);
      fadeIn.setOnFinished(
          e -> {
            // Switch to the cutscene animation screen
            try {
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cutscene.fxml"));
              Parent cutsceneRoot = loader.load();

              // Get the current stage
              Stage stage = (Stage) titleScreen.getScene().getWindow();

              // Create the new scene for the cutscene
              Scene cutsceneScene = new Scene(cutsceneRoot, Color.BLACK);
              cutsceneScene
                  .getStylesheets()
                  .add(getClass().getResource("/css/styles.css").toExternalForm());

              // Set the new scene (cutscene)
              stage.setScene(cutsceneScene);

              // Access the CutsceneController and set the preloaded MediaPlayer
              CutsceneController cutsceneController = loader.getController();
              cutsceneController.setMediaPlayer(preloadedMediaPlayer);

              // Perform fade-in effect on the cutscene
              cutsceneController.fadeInCutscene();

              stage.show();

            } catch (IOException ex) {
              ex.printStackTrace();
            }
          });
      fadeIn.play();
    }
  }
}
