package nz.ac.auckland.se206.states;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * The Guessing state of the game. Handles the logic for when the player is making a guess about the
 * profession of the characters in the game.
 */
public class Guessing implements GameState {

  private final GameStateContext context;

  private MediaPlayer mediaPlayer;
  private final TimerManager timerManager = TimerManager.getInstance();

  /**
   * Constructs a new Guessing state with the given game state context.
   *
   * @param context the context of the game state
   */
  public Guessing(GameStateContext context) {
    this.context = context;
  }

  /**
   * Handles the event when a rectangle is clicked. Checks if the clicked rectangle is a customer
   * and updates the game state accordingly.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    // Check if the clicked rectangle is the correct one
    if (rectangleId.equals(context.getRectIdToGuess())) {
      timerManager.stopTimer();
      playSound("/sounds/correct.mp3");
      // Transition to game over state
      context.setState(context.getGameOverState());
      System.out.println("Correct! You guessed the thief");
      // Load the game over screen
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/you_win.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);
      App.getStage().setScene(scene);
      App.getStage().show();
    } else {
      playSound("/sounds/wrong.mp3");
      // Transition to game over state
      context.setState(context.getGameOverState());
      // Load the game over screen
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/you_lose.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);
      App.getStage().setScene(scene);
      App.getStage().show();
    }
  }

  /**
   * Handles the event when the guess button is clicked. Since the player has already guessed, it
   * notifies the player.
   *
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleGuessClick() throws IOException {
    context.setState(context.getGuessingState());
    context.handleGuessClick();
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
