package nz.ac.auckland.se206.controllers;

import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class RadioSceneController {
  private boolean isPlayingaudio = false;
  private int frequency = 1;
  private MediaPlayer mediaPlayer;
  private Task<Void> backgroundTask;
  private int totalAudio = 5;

  /**
   * the play button is clicked if the audio is playing, pause it. if the audio is paused, play it
   *
   * @param event
   */
  @FXML
  private void onPlay(ActionEvent event) {
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
  private void onDecreaseFrequency(ActionEvent event) {
    System.out.println("Decrease Frequency button clicked");
    stopAudio();
    frequency--;
    if (frequency < 1) {
      frequency = totalAudio;
    }
    playAudio();
  }

  /**
   * the increase frequency button is clicked change to the right audio
   *
   * @param event
   */
  @FXML
  private void onIncreaseFrequency(ActionEvent event) {
    System.out.println("Increase Frequency button clicked");
    stopAudio();
    frequency++;
    if (frequency > totalAudio) {
      frequency = 1;
    }
    playAudio();
  }

  private void stopAudio() {
    if (backgroundTask == null) {
      return;
    }

    isPlayingaudio = false;
    backgroundTask.cancel();
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
}
