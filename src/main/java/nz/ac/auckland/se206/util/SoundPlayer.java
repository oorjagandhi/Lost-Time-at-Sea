package nz.ac.auckland.se206.util;

import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundPlayer {
  private MediaPlayer mediaPlayer;

  public void playSound(String filePath) {
    // runs playing an audio file as a background task
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            // attempts to find the audio file and if found will play it
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
    // starts background thread
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }
}
