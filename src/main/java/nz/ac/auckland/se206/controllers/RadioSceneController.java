package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class RadioSceneController {
  private boolean isPlayingaudio = false;
  private int frequency = 1;
  private MediaPlayer mediaPlayer;
  private Task<Void> backgroundTask;
  private int totalAudios = 5;

  @FXML private ImageView frequencyImage;
  @FXML private ImageView increaseFrequency;
  @FXML private ImageView decreaseFrequency;
  @FXML private ImageView play;

  @FXML
  private void initialize() {
    if (increaseFrequency != null && decreaseFrequency != null && play != null) {
      increaseFrequency.setCursor(javafx.scene.Cursor.HAND);
      decreaseFrequency.setCursor(javafx.scene.Cursor.HAND);
      play.setCursor(javafx.scene.Cursor.HAND);
    }
    increaseFrequency.setOnMouseClicked(event -> handleIncreaseClick(event));
    decreaseFrequency.setOnMouseClicked(event -> handleDecreaseClick(event));

    // deal with the hovering effect
    play.setOnMouseClicked(event -> handlePlayClick(event));
    increaseFrequency.setOnMouseEntered(this::handleMouseEnter);
    decreaseFrequency.setOnMouseEntered(this::handleMouseEnter);
    play.setOnMouseEntered(this::handleMouseEnter);
    increaseFrequency.setOnMouseExited(this::handleMouseExit);
    decreaseFrequency.setOnMouseExited(this::handleMouseExit);
    play.setOnMouseExited(this::handleMouseExit);
  }

  /**
   * the play button is clicked if the audio is playing, pause it. if the audio is paused, play it
   *
   * @param event
   */
  @FXML
  private void handlePlayClick(MouseEvent event) {
    System.out.println("Play button clicked");
    if (isPlayingaudio) {
      // pause the audio
      isPlayingaudio = false;
      stopAudio();
    } else {
      // play the audio
      isPlayingaudio = true;
      playAudio();
    }
  }

  /**
   * the decrease frequency button is clicked change to the left audio
   *
   * @param event
   */
  @FXML
  private void handleDecreaseClick(MouseEvent event) {
    System.out.println("Decrease Frequency button clicked");
    stopAudio();
    // if the frequency is smaller than 1, change to the last audio
    frequency--;
    if (frequency < 1) {
      frequency = totalAudios;
    }
    // change the image
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));

    // play the audio
    playAudio();
  }

  /**
   * the increase frequency button is clicked change to the right audio
   *
   * @param event
   */
  @FXML
  private void handleIncreaseClick(MouseEvent event) {
    System.out.println("Increase Frequency button clicked");
    stopAudio();
    frequency++;
    // if the frequency is larger than the total number of audios, change to the first audio
    if (frequency > totalAudios) {
      frequency = 1;
    }
    // change the image
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));
    playAudio();
  }

  private void stopAudio() {
    if (backgroundTask == null) {
      return;
    }

    isPlayingaudio = false;
    backgroundTask.cancel();
  }

  private void handleMouseEnter(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.HAND);
    imageView.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  private void handleMouseExit(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.DEFAULT);
    imageView.setStyle("");
  }

  /**
   * to play the audio with a input frequency, mp3s are all named as radio1.mp3, radio2.mp3,
   * radio3.mp3, the frequency is the number from 1 to 3 playing the audio could be stopped by the
   * stopAudio
   */
  private void playAudio() {
    String filePath = "/sounds/radio" + frequency + ".mp3";
    backgroundTask =
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
                  mediaPlayer = new MediaPlayer(media);

                  isPlayingaudio = true;
                  // Add a listener to the MediaPlayer status property
                  mediaPlayer
                      .statusProperty()
                      .addListener(
                          (obs, oldStatus, newStatus) -> {
                            if (newStatus == MediaPlayer.Status.STOPPED
                                || newStatus == MediaPlayer.Status.PAUSED) {
                              // Media stopped or paused, cancel the task
                              cancel();
                            }
                          });

                  // Set onEndOfMedia to update isPlayingaudio to false when media ends
                  mediaPlayer.setOnEndOfMedia(
                      () -> {
                        isPlayingaudio = false;
                        System.out.println("Audio finished playing");
                      });

                  mediaPlayer.play();
                });

            // Continuously check if the task is cancelled
            while (!isCancelled()) {
              try {
                Thread.sleep(100); // Sleep to avoid busy-waiting
              } catch (InterruptedException e) {
                if (isCancelled()) {
                  break;
                }
              }
            }

            // Stop the audio if the task is cancelled
            Platform.runLater(
                () -> {
                  if (mediaPlayer != null
                      && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.stop();
                    isPlayingaudio = false;
                  }
                });

            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }

  @FXML
  private void onBackButtonAction(ActionEvent event) {
    // stop the audio
    stopAudio();

    // Load the crime scene FXML
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent root = loader.load();
      Node source = (Node) event.getSource();
      javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
      // Add the stylesheet
      root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      // Auto-generated catch block
      e.printStackTrace();
    }
  }
}
