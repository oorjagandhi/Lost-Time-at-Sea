package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CutsceneController {

  @FXML private MediaView mediaView;
  @FXML private AnchorPane cutscenePane;

  private MediaPlayer mediaPlayer;

  @FXML
  public void initialize() {
    // Do nothing here since mediaPlayer will be set via setMediaPlayer()
  }

  public void setMediaPlayer(MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
    mediaView.setMediaPlayer(mediaPlayer);

    // Play the video
    mediaPlayer.play();

    // Set an event listener for when the video finishes playing
    mediaPlayer.setOnEndOfMedia(
        () -> {
          // Release MediaPlayer resources
          mediaPlayer.stop();
          mediaPlayer.dispose();

          // Fade out and then load the game scene
          fadeOutAndLoadGameScene();
        });
  }

  private void fadeOutAndLoadGameScene() {
    // Create a black overlay that covers the entire cutscenePane
    Pane blackOverlay = new Pane();
    blackOverlay.setStyle("-fx-background-color: black;");
    blackOverlay.setOpacity(0.0);
    blackOverlay.prefWidthProperty().bind(cutscenePane.widthProperty());
    blackOverlay.prefHeightProperty().bind(cutscenePane.heightProperty());
    cutscenePane.getChildren().add(blackOverlay);
    blackOverlay.toFront();

    // Create a FadeTransition to fade in the black overlay
    FadeTransition fadeInBlackOverlay = new FadeTransition(Duration.seconds(1), blackOverlay);
    fadeInBlackOverlay.setFromValue(0.0);
    fadeInBlackOverlay.setToValue(1.0);
    fadeInBlackOverlay.setOnFinished(
        event -> {
          // After fade-in, load the game scene
          loadGameScene();
        });
    fadeInBlackOverlay.play();
  }

  private void loadGameScene() {
    // Load the game scene here
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent gameRoot = loader.load();

      // Set initial opacity to 0
      gameRoot.setOpacity(0.0);

      // Create the scene with black background
      Scene gameScene = new Scene(gameRoot, Color.BLACK);
      gameScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Get the current stage
      Stage stage = (Stage) cutscenePane.getScene().getWindow();

      // Set the new scene
      stage.setScene(gameScene);

      // Perform fade-in effect on the game scene
      FadeTransition fadeInGame = new FadeTransition(Duration.seconds(1), gameRoot);
      fadeInGame.setFromValue(0.0);
      fadeInGame.setToValue(1.0);
      fadeInGame.setOnFinished(
          event -> {
            // Start the game timer or any initial logic here
            startGameTimer();
          });
      fadeInGame.play();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void startGameTimer() {
    // Implement your game timer logic here
    System.out.println("Game timer started!");
  }

  public void fadeInCutscene() {
    // Set initial opacity to 0
    cutscenePane.setOpacity(0.0);

    // Create a FadeTransition for the cutscene pane
    FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), cutscenePane);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);
    fadeIn.play();
  }
}
