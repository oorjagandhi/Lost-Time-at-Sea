package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SceneSwitcherUtils;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * The ClueSoundController class is responsible for handling the sound and visual effects for the
 * clue and suspect interactions in the game.
 */
public class ClueSoundController extends SoundPlayer {

  protected GameStateContext context;

  @FXML protected ImageView clueProgressBar, suspectsProgressBar, btnGuess;
  @FXML protected AnchorPane room;
  @FXML protected ImageView currentScene;

  /** Initializes the controller. */
  @FXML
  public void initialize() {
    this.context = GameStateContext.getInstance();
    updateProgressBar();
    updateGuessButtonState();
  }

  /**
   * Handles the mouse entering a clue area and applies a hover effect.
   *
   * @param event The mouse event associated with entering the clue image area.
   */
  @FXML
  public void handleMouseEnterClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();

    // Check if the player is allowed to guess now
    if (!context.canGuess() && source.equals(btnGuess)) {
      return;
    }
    source.setCursor(Cursor.HAND); // Change cursor to hand to indicate interactivity
    source.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  /**
   * Removes the hover effect from a clue when the mouse exits.
   *
   * @param event The mouse event associated with exiting the clue image area.
   */
  @FXML
  public void handleMouseExitClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }

  /** Updates the progress bars for clues and suspects. */
  public void updateProgressBar() {
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
   * Handles the guess button click, transitioning the game to the guessing state if the player is
   * allowed to guess.
   *
   * @param event The mouse event associated with the guess button click.
   * @throws IOException If an error occurs while loading the guessing scene.
   */
  @FXML
  public void handleGuessClick(MouseEvent event) throws IOException {
    if (context.canGuess()) {
      TimerManager timerManager = TimerManager.getInstance();
      timerManager.startGuessingTimer();
      context.setState(context.getGuessingState());

      // Load the guessing screen
      SceneSwitcherUtils.switchScene(
          event, "/fxml/guessing.fxml", (Stage) ((Node) event.getSource()).getScene().getWindow());
    } else {
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  /**
   * Switches the scene to the specified FXML file.
   *
   * @param event The mouse event associated with the scene switch.
   * @param fxmlFile The path to the FXML file to switch to.
   */
  public void switchScene(MouseEvent event, String fxmlFile) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    SceneSwitcherUtils.switchScene(event, fxmlFile, stage);
  }

  /**
   * Updates the state of the guess button, enabling or disabling it based on the player's progress.
   */
  protected void updateGuessButtonState() {
    if (btnGuess != null) {
      if (context.canGuess()) {
        btnGuess.setImage(new Image("/images/layouts/enabled-button.png"));
      } else {
        btnGuess.setImage(new Image("/images/layouts/disabled-button.png"));
      }
    }
  }

  /**
   * Handles the back button action, switching back to the crime scene.
   *
   * @param event The mouse event associated with the back button click.
   */
  @FXML
  public void onBackButtonAction(MouseEvent event) {
    preBackAction();
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  /** Performs any necessary actions before switching back to the crime scene. */
  protected void preBackAction() {
    // Default implementation does nothing.
  }

  protected void preSwitchAction() {
    // Default implementation does nothing.
  }

  /**
   * Switches the scene to the maid's room when the maid suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the maid suspect.
   */
  @FXML
  public void onSwitchToMaidRoom(MouseEvent event) {
    preSwitchAction(); // Call any subclass-specific actions
    context.setSuspectInteracted("maid");
    switchScene(event, "/fxml/maid-room.fxml");
  }

  /**
   * Switches the scene to the bartender's bar when the bartender suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the bartender suspect.
   */
  @FXML
  public void onSwitchToBar(MouseEvent event) {
    preSwitchAction(); // Call any subclass-specific actions
    context.setSuspectInteracted("bartender");
    switchScene(event, "/fxml/bar-room.fxml");
  }

  /**
   * Switches the scene to the first mate's deck when the first mate suspect is interacted with.
   *
   * @param event The mouse event associated with interacting with the first mate suspect.
   */
  @FXML
  public void onSwitchToDeck(MouseEvent event) {
    preSwitchAction(); // Call any subclass-specific actions
    context.setSuspectInteracted("sailor");
    switchScene(event, "/fxml/deck.fxml");
  }
}
