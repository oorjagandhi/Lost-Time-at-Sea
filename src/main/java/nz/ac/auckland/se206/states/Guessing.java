package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * The Guessing state of the game. Handles the logic for when the player is making a guess about the
 * profession of the characters in the game.
 */
public class Guessing extends SoundPlayer implements GameState {

  private final GameStateContext context;

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
      playSound("/sounds/correct.mp3");
      // Transition to game over state with a win
      context.setState(context.getGameOverState());
      context.setWon(true);
      System.out.println("Correct! You guessed the thief");
    } else {
      playSound("/sounds/wrong.mp3");
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
}
