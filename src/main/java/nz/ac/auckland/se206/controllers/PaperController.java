package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class PaperController {

  @FXML private ImageView paperImageView;
  private MediaPlayer mediaPlayer;

  private int clickCount = 0; // To track the number of clicks
  private final String[] paperImages = {
    "/images/crumple-1.png",
    "/images/crumple-2.png",
    "/images/crumple-3.png",
    "/images/crumple-4.png"
  };

  // Method to set up the initial image and click handler
  public void setupPaperImages() {
    if (paperImageView != null) {
      // Set the initial image
      paperImageView.setImage(new Image(getClass().getResourceAsStream(paperImages[0])));
      // Set up click event handler
      paperImageView.setOnMouseClicked(event -> handlePaperClick(event));
    }
  }

  @FXML
  private void handlePaperClick(MouseEvent event) {
    // Cycle through the images
    clickCount++;
    if (clickCount < paperImages.length) {
      playSound("/sounds/paper.mp3");
      paperImageView.setImage(new Image(getClass().getResourceAsStream(paperImages[clickCount])));
    }
  }

  @FXML
  private void onBackButtonAction(ActionEvent event) {
    try {
      // Load the FXML file for the room
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent roomContent = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();

      roomContent.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the scene to the room
      stage.setScene(new javafx.scene.Scene(roomContent));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void playSound(String filePath) {
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            URL resource = getClass().getResource(filePath);
            if (resource == null) {
              Platform.runLater(() -> System.out.println("File not found: " + filePath));
              return null;
            }
            Media media = new Media(resource.toString());
            Platform.runLater(
                () -> {
                  if (mediaPlayer != null) {
                    mediaPlayer.stop();
                  }
                  mediaPlayer = new MediaPlayer(media);
                  mediaPlayer.play();
                });
            return null;
          }
        };
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }
}
