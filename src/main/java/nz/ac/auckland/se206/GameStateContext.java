package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.states.GameOver;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.states.GameState;
import nz.ac.auckland.se206.states.Guessing;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * states and maintains game data such as the professions and rectangle IDs.
 */
public class GameStateContext {

  private final String rectIdToGuess = "rectSecurity";
  private final Map<String, String> rectanglesToProfession;
  private final GameStarted gameStartedState;
  private final Guessing guessingState;
  private final GameOver gameOverState;
  private GameState gameState;
  private boolean clueInteracted = false;
  private Set<String> suspectsInteracted;
  private boolean suspectInteracted = false;
  private boolean won = false;
  private Runnable updateGuessButtonStateCallback;

  /** Constructs a new GameStateContext and initializes the game states and professions. */
  public GameStateContext() {
    gameStartedState = new GameStarted(this);
    guessingState = new Guessing(this);
    gameOverState = new GameOver(this);

    gameState = gameStartedState; // Initial state
    rectanglesToProfession = new HashMap<>();
    rectanglesToProfession.put("rectSecurity", "Security");
    rectanglesToProfession.put("rectArtist", "Artist");
    rectanglesToProfession.put("rectCollector", "Collector");

    suspectsInteracted = new HashSet<>();
  }

  /**
   * Sets the current state of the game.
   *
   * @param state the new state to set
   */
  public void setState(GameState state) {
    this.gameState = state;
  }

  public GameState getState() {
    return this.gameState;
  }

  /**
   * Gets the initial game started state.
   *
   * @return the game started state
   */
  public GameState getGameStartedState() {
    return gameStartedState;
  }

  /**
   * Gets the guessing state.
   *
   * @return the guessing state
   */
  public GameState getGuessingState() {
    return guessingState;
  }

  /**
   * Gets the game over state.
   *
   * @return the game over state
   */
  public GameState getGameOverState() {
    return gameOverState;
  }

  /**
   * Gets the ID of the rectangle to be guessed.
   *
   * @return the rectangle ID to guess
   */
  public String getRectIdToGuess() {
    return rectIdToGuess;
  }

  /**
   * Gets the profession associated with a specific rectangle ID.
   *
   * @param rectangleId the rectangle ID
   * @return the profession associated with the rectangle ID
   */
  public String getProfession(String rectangleId) {
    return rectanglesToProfession.get(rectangleId);
  }

  /**
   * Handles the event when a rectangle is clicked.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    gameState.handleRectangleClick(event, rectangleId);
    updateGuessButtonState();
  }

  /**
   * Handles the event when the guess button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void handleGuessClick() throws IOException {
    gameState.handleGuessClick();
  }

  public void setClueInteracted(boolean interacted) {
    this.clueInteracted = interacted;
    updateGuessButtonState();
  }

  public void setSuspectInteracted(String suspectId) {
    suspectsInteracted.add(suspectId);
    updateGuessButtonState();
  }

  public boolean canGuess() {
    return true;
    // return clueInteracted && suspectsInteracted.size() >= 3;
  }

  public boolean isWon() {
    return won;
  }

  public void setWon(boolean won) {
    this.won = won;
  }

  public void setUpdateGuessButtonStateCallback(Runnable callback) {
    this.updateGuessButtonStateCallback = callback;
  }

  private void updateGuessButtonState() {
    if (updateGuessButtonStateCallback != null) {
      updateGuessButtonStateCallback.run();
    }
  }
}
