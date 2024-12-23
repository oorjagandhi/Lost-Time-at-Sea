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

  private static GameStateContext gameStateContext;

  /**
   * Gets the singleton instance of the GameStateContext.
   *
   * @return the GameStateContext instance.
   */
  public static GameStateContext getInstance() {
    if (gameStateContext == null) {
      gameStateContext = new GameStateContext();
    }
    return gameStateContext;
  }

  private final GameOver gameOverState;
  private final GameStarted gameStartedState;
  private final Guessing guessingState;
  private final Map<String, String> rectanglesToProfession;
  private final String rectIdToGuess = "rectSecurity";

  private boolean clueInteracted = false;
  private boolean won = false;
  private int cluesInteracted = 0;
  private Set<Object> cluesInteractedSet = new HashSet<>();
  private Set<String> suspectsInteracted;
  private GameState gameState;
  private Runnable updateGuessButtonStateCallback;

  /** Constructs a new GameStateContext and initializes the game states and professions. */
  private GameStateContext() {
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

  /**
   * Handles the event when the play again button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void setClueInteracted(boolean interacted, String clueId) {
    this.clueInteracted = interacted;
    // If the clue was interacted with, add it to the set
    if (interacted && clueId != null) {
      cluesInteractedSet.add(clueId);
      cluesInteracted = cluesInteractedSet.size();
    } else if (!interacted) {
      cluesInteracted = 0;
    } else {
      cluesInteracted = cluesInteractedSet.size();
    }
    System.out.println("Clues interacted: " + cluesInteracted);
    // Update the state of the guess button
    updateGuessButtonState();
  }

  public void clearCluesInteracted() {
    this.clueInteracted = false;
    cluesInteractedSet.clear();
  }

  public void setSuspectInteracted(String suspectId) {
    suspectsInteracted.add(suspectId);
    updateGuessButtonState();
  }

  public void clearSuspects() {
    suspectsInteracted.clear();
    updateGuessButtonState();
  }

  public boolean canGuess() {
    return clueInteracted && suspectsInteracted.size() >= 3;
  }

  public int getNumSuspectsInteracted() {
    return suspectsInteracted.size();
  }

  public int getNumCluesInteracted() {
    return cluesInteracted;
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
