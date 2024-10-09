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

/**
 * The RadioSceneController class manages the radio interaction within the game. It handles audio
 * playback through changing frequencies, user interaction via buttons, and transitions to different
 * scenes such as suspect rooms or the guessing state.
 */
public class RadioSceneController {

  private static GameStateContext context = GameStateContext.getInstance();

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

  /**
   * Initializes the controller. Sets up hover effects, click handlers, and updates the progress bar
   * and guess button state.
   */
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

    // Set up hovering effects for buttons
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
   * Handles the play button click. Toggles audio playback. If audio is playing, it pauses it; if
   * paused, it resumes the audio.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handlePlayClick(MouseEvent event) {
    System.out.println("Play button clicked");
    if (isPlayingaudio) {
      isPlayingaudio = false;
      stopAudio();
    } else {
      isPlayingaudio = true;
      playAudio();
    }
  }

  /**
   * Handles the decrease frequency button click. Changes the frequency to the previous audio file
   * and plays it. If the frequency is less than 1, it wraps around to the last available audio.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handleDecreaseClick(MouseEvent event) {
    System.out.println("Decrease Frequency button clicked");
    stopAudio();
    frequency--;
    if (frequency < 1) {
      frequency = totalAudios;
    }
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));
    playAudio();
  }

  /**
   * Handles the increase frequency button click. Changes the frequency to the next audio file and
   * plays it. If the frequency exceeds the total number of available audios, it wraps around to the
   * first one.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handleIncreaseClick(MouseEvent event) {
    System.out.println("Increase Frequency button clicked");
    stopAudio();
    frequency++;
    if (frequency > totalAudios) {
      frequency = 1;
    }
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));
    playAudio();
  }

  /** Stops the currently playing audio by canceling the background task that plays the audio. */
  private void stopAudio() {
    if (backgroundTask == null) {
      return;
    }

    isPlayingaudio = false;
    backgroundTask.cancel();
  }

  /**
   * Handles mouse entering a button, changing the cursor to a hand and applying a drop shadow
   * effect to the button.
   *
   * @param event The mouse event associated with entering the button area.
   */
  private void handleMouseEnter(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.HAND);
    imageView.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  /**
   * Handles mouse exiting a button, resetting the cursor to default and removing the drop shadow
   * effect.
   *
   * @param event The mouse event associated with exiting the button area.
   */
  private void handleMouseExit(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.DEFAULT);
    imageView.setStyle("");
  }

  /**
   * Plays the audio corresponding to the current frequency. Audio files are named as radio1.mp3,
   * radio2.mp3, etc. This method plays the audio in the background and can be interrupted by
   * calling stopAudio().
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

                  // Listener to stop playing when audio ends
                  mediaPlayer
                      .statusProperty()
                      .addListener(
                          (obs, oldStatus, newStatus) -> {
                            if (newStatus == MediaPlayer.Status.STOPPED
                                || newStatus == MediaPlayer.Status.PAUSED) {
                              cancel(); // Cancel task when media stops/pauses
                            }
                          });

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

  /**
   * Handles the back button click. Stops any playing audio and switches the scene back to the crime
   * scene.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void onBackButtonAction(MouseEvent event) {
    stopAudio();
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  /**
   * Applies a hover effect to the image of a clue or guess button when the mouse enters. If the
   * player is not allowed to guess yet, the guess button hover effect is not applied.
   *
   * @param event The mouse event associated with entering the clue or guess button area.
   */
  @FXML
  private void handleMouseEnterClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();
    if (!context.canGuess() && source.equals(btnGuess)) {
      return;
    }
    source.setCursor(Cursor.HAND);
    source.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  /**
   * Removes the hover effect from the image of a clue or guess button when the mouse exits.
   *
   * @param event The mouse event associated with exiting the clue or guess button area.
   */
  @FXML
  private void handleMouseExitClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();
    source.setCursor(Cursor.DEFAULT);
    source.setStyle("");
  }

  /**
   * Updates the progress bar to reflect the number of clues and suspects the player has interacted
   * with.
   */
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

  /**
   * Handles the guess button click. If the player is allowed to guess, the game transitions to the
   * guessing state. Otherwise, a message is shown that the player needs to interact with both a
   * clue and a suspect.
   *
   * @param event The mouse event associated with the guess button click.
   * @throws IOException If an error occurs while loading the guessing scene.
   */
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

  /**
   * Updates the state of the guess button, enabling or disabling it based on the player's progress.
   */
  private void updateGuessButtonState() {
    if (btnGuess != null) {
      if (context.canGuess()) {
        btnGuess.setImage(new Image("/images/layouts/enabled-button.png"));
      } else {
        btnGuess.setImage(new Image("/images/layouts/disabled-button.png"));
      }
    }
  }

  /**
   * Switches the scene to the maid's room when the maid suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the maid suspect.
   */
  @FXML
  private void onSwitchToMaidRoom(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("maid");
    switchScene(event, "/fxml/maid-room.fxml");
  }

  /**
   * Switches the scene to the bartender's bar when the bartender suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the bartender suspect.
   */
  @FXML
  private void onSwitchToBar(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("bartender");
    switchScene(event, "/fxml/bar-room.fxml");
  }

  /**
   * Switches the scene to the first mate's deck when the first mate suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the first mate suspect.
   */
  @FXML
  private void onSwitchToDeck(MouseEvent event) {
    stopAudio();
    context.setSuspectInteracted("sailor");
    switchScene(event, "/fxml/deck.fxml");
  }

  /**
   * Switches the current scene to the specified FXML file.
   *
   * @param event The mouse event associated with switching the scene.
   * @param fxmlFile The path to the FXML file to switch to.
   */
  private void switchScene(MouseEvent event, String fxmlFile) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Parent newScene = loader.load();

      Stage stage = (Stage) room.getScene().getWindow();
      Scene scene = new Scene(newScene);

      newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the new scene
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
