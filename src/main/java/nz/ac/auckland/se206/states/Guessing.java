package nz.ac.auckland.se206.states;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    timerManager.stopTimer();
    // Check if the clicked rectangle is the bartender
    if (rectangleId.equals("suspectBartender")) {
      // Transition to game over state with a win
      context.setState(context.getGameOverState());
      context.setWon(true);
      System.out.println("Correct! You guessed the thief");
    } else {
      // Transition to game over state with a loss
      context.setState(context.getGameOverState());
      context.setWon(false);
      System.out.println("Incorrect! You guessed the wrong suspect");
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

  // Play the sound file at the given file path
  private void playSound(String filePath) {
    // Create a background task to play the sound file
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            URL resource = getClass().getResource(filePath);
            if (resource == null) {
              Platform.runLater(() -> System.out.println("File not found: " + filePath));
              return null;
            }
            // Play the sound file
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
    // Start the background task
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }
}
