package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SceneSwitcher;
import nz.ac.auckland.se206.util.TimerManager;

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
  @FXML private ImageView btnGuess;
  @FXML private ImageView suspectsProgressBar;
  @FXML private ImageView clueProgressBar;
  @FXML private ImageView currentScene;
  @FXML private AnchorPane room;

  private static GameStateContext context = GameStateContext.getInstance();

  @FXML
  private void initialize() {
    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
    if (increaseFrequency != null && decreaseFrequency != null && play != null) {
      increaseFrequency.setCursor(Cursor.HAND);
      decreaseFrequency.setCursor(Cursor.HAND);
      play.setCursor(Cursor.HAND);
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

    updateProgressBar();
    updateGuessButtonState();
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
  private void onBackButtonAction(MouseEvent event) {
    // stop the audio
    stopAudio();

    // Load the crime scene FXML

    switchScene(event, "/fxml/crime-scene.fxml");
  }

  @FXML
  private void handleMouseEnterClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView

    // check if the player is allowed to guess now
    if (!context.canGuess() && source.equals(btnGuess)) {
      return;
    }
    source.setCursor(Cursor.HAND); // Change cursor to hand to indicate interactivity
    source.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  @FXML
  private void handleMouseExitClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }

  private void updateProgressBar() {
    if (clueProgressBar != null) {
      int cluesInteracted = context.getNumCluesInteracted();
      clueProgressBar.setImage(new Image("/images/layouts/bar" + cluesInteracted + ".png"));
    }

    if (suspectsProgressBar != null) {
      int suspectsInteracted = context.getNumSuspectsInteracted();
      suspectsProgressBar.setImage(new Image("/images/layouts/bar" + suspectsInteracted + ".png"));
    }
  }

  @FXML
  private void handleGuessClick(MouseEvent event) throws IOException {

    if (context.canGuess()) {
      TimerManager timerManager = TimerManager.getInstance();
      timerManager.startGuessingTimer();
      context.setState(context.getGuessingState());
      System.out.println("Transitioning to guessing state. Ready to make a guess.");

      // Load the guessing screen
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/guessing.fxml"));
      Parent root = loader.load();

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      SceneSwitcher.switchScene(stage, root);
    } else {
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  private void updateGuessButtonState() {
    if (btnGuess != null) {
      if (context.canGuess()) {
        btnGuess.setImage(new Image("/images/layouts/enabled-button.png"));
      } else {
        btnGuess.setImage(new Image("/images/layouts/disabled-button.png"));
      }
    }
  }

  @FXML
  private void onSwitchToMaidRoom(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("maid");
    switchScene(event, "/fxml/maid-room.fxml");
  }

  @FXML
  private void onSwitchToBar(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("bartender");
    switchScene(event, "/fxml/bar-room.fxml");
  }

  @FXML
  private void onSwitchToDeck(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("sailor");
    switchScene(event, "/fxml/deck.fxml");
  }

  private void switchScene(MouseEvent event, String fxmlFile) {
    try {
      // Use non-static FXMLLoader to load the FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Parent newScene = loader.load(); // Load the new scene

      // Get the stage from the current event
      Stage stage = (Stage) room.getScene().getWindow();
      Scene scene = new Scene(newScene);

      newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the new scene
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace(); // Handle IOException
    }
  }
}
