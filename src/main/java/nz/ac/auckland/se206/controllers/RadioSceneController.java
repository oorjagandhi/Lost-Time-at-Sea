package nz.ac.auckland.se206.controllers;

import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class RadioSceneController {
  boolean Playingaudio = false;
  int frequency = 1;
  MediaPlayer mediaPlayer;
  Task<Void> backgroundTask;

  /**
   * the play button is clicked if the audio is playing, pause it. if the audio is paused, play it
   *
   * @param event
   */
  @FXML
  private void onPlay(ActionEvent event) {
    System.out.println("Play button clicked");
    if (Playingaudio) {
      // pause the audio
      Playingaudio = false;
      stopAudio();
    } else {
      // play the audio
      Playingaudio = true;
      playSound("/sounds/welcome.mp3");
    }
  }

  /**
   * the decrease frequency button is clicked change to the left audio
   *
   * @param event
   */
  @FXML
  private void onDecreaseFrequency(ActionEvent event) {
    System.out.println("Decrease Frequency button clicked");
    frequency--;
  }

  /**
   * the increase frequency button is clicked change to the right audio
   *
   * @param event
   */
  @FXML
  private void onIncreaseFrequency(ActionEvent event) {
    System.out.println("Increase Frequency button clicked");
  }

  private void stopAudio() {
    backgroundTask.cancel();
  }

  /**
   * to play the audio with a input frequency, mp3s are all named as radio1.mp3, radio2.mp3,
   * radio3.mp3, radio4.mp3, radio5.mp3 the frequency is from 1 to 5 the audio3 is the clue audio
   * playing the audio could be stopped by the stopAudio
   */
  private void playSound(String filePath) {
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

                  // Add a listener to the MediaPlayer status property
                  mediaPlayer
                      .statusProperty()
                      .addListener(
                          (obs, oldStatus, newStatus) -> {
                            if (newStatus == MediaPlayer.Status.STOPPED
                                || newStatus == MediaPlayer.Status.PAUSED) {
                              // The media has stopped, so we can cancel the task
                              cancel();
                            }
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
                  }
                });

            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }
}
