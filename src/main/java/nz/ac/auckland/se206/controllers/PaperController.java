package nz.ac.auckland.se206.controllers;

import java.io.IOException;
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
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SceneSwitcher;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * The PaperController class handles the interactions with the paper clue scene. This includes
 * managing paper images that change upon clicks, controlling hover effects, and switching between
 * different scenes. It also updates the progress bar and manages the guessing state.
 */
public class PaperController extends SoundPlayer {

  private static GameStateContext context = GameStateContext.getInstance();

  @FXML private ImageView paperImageView;
  @FXML private ImageView btnGuess;
  @FXML private ImageView suspectsProgressBar;
  @FXML private ImageView clueProgressBar;
  @FXML private ImageView currentScene;

  @FXML private AnchorPane room;

  private int clickCount = 0; // To track the number of clicks
  private final String[] paperImages = {
    "/images/clues/paper.png",
    "/images/clues/paper2.png",
    "/images/clues/paper3.png",
    "/images/clues/paper5.png"
  };

  /** Initializes the scene and sets up paper images, event handlers, and progress bar. */
  @FXML
  public void initialize() {
    setupPaperImages(); // Ensure this is called to initialize event handlers and images
    updateProgressBar();
    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
    updateGuessButtonState();
  }

  /**
   * Sets up the initial paper image and click handlers for cycling through paper images. Also
   * handles mouse hover effects.
   */
  public void setupPaperImages() {
    if (paperImageView != null) {
      // Set the initial image
      paperImageView.setImage(new Image(getClass().getResourceAsStream(paperImages[0])));
      // Set up click event handler
      paperImageView.setOnMouseClicked(event -> handlePaperClick(event));

      // Set up hover effects
      paperImageView.setOnMouseEntered(this::handleMouseEnterPaper);
      paperImageView.setOnMouseExited(this::handleMouseExitPaper);
    }
  }

  /**
   * Handles the paper click event. Cycles through the paper images and plays sound effects. Removes
   * hover effects after the final image is displayed.
   *
   * @param event The mouse event associated with the paper click.
   */
  @FXML
  private void handlePaperClick(MouseEvent event) {
    // Cycle through the images
    clickCount++;
    if (clickCount < paperImages.length) {
      playSound("/sounds/paper.mp3");
      paperImageView.setImage(new Image(getClass().getResourceAsStream(paperImages[clickCount])));

      // If the final image is reached, remove the highlight effect
      if (clickCount == paperImages.length - 1) {
        paperImageView.setStyle(""); // Reset style to remove highlight
        paperImageView.setCursor(Cursor.DEFAULT); // Reset cursor
      }
    }
  }

  /**
   * Updates the state of the guess button, enabling or disabling it based on the player's progress.
   */
  private void updateGuessButtonState() {
    // Check if the player can guess
    if (btnGuess != null) {
      if (context.canGuess()) {
        btnGuess.setImage(new Image("/images/layouts/enabled-button.png"));
      } else {
        btnGuess.setImage(new Image("/images/layouts/disabled-button.png"));
      }
    }
  }

  /**
   * Handles the mouse entering the paper image, applying a hover effect and changing the cursor to
   * a hand.
   *
   * @param event The mouse event associated with entering the paper image area.
   */
  private void handleMouseEnterPaper(MouseEvent event) {
    if (clickCount < paperImages.length - 1) {
      ImageView paper = (ImageView) event.getSource();
      paper.setCursor(Cursor.HAND); // Change cursor to hand
      paper.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }
  }

  /**
   * Handles the mouse exiting the paper image, resetting the cursor and removing the hover effect.
   *
   * @param event The mouse event associated with exiting the paper image area.
   */
  private void handleMouseExitPaper(MouseEvent event) {
    // Only remove hover effect if the final image is not displayed
    ImageView paper = (ImageView) event.getSource();
    paper.setCursor(Cursor.DEFAULT);
    paper.setStyle(""); // Reset to default style
  }

  /**
   * Switches back to the crime scene when the back button is clicked.
   *
   * @param event The mouse event associated with the back button click.
   */
  @FXML
  private void onBackButtonAction(MouseEvent event) {
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  /**
   * Applies a hover effect to a clue when the mouse enters, and sets the cursor to a hand. If the
   * player is not allowed to guess, the effect is not applied to the guess button.
   *
   * @param event The mouse event associated with entering the clue image area.
   */
  @FXML
  private void handleMouseEnterClue(MouseEvent event) {
    // Check if the player is allowed to guess now
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView

    // check if the player is allowed to guess now
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
  private void handleMouseExitClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }

  /** Updates the progress bar to reflect the player's interactions with clues and suspects. */
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
   * Handles the guess button click, transitioning the game to the guessing state if the player is
   * allowed to guess. Otherwise, displays a message indicating more interactions are required.
   *
   * @param event The mouse event associated with the guess button click.
   * @throws IOException If an error occurs while loading the guessing scene.
   */
  @FXML
  private void handleGuessClick(MouseEvent event) throws IOException {
    // Check if the player can guess
    if (context.canGuess()) {
      // Start the guessing timer
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
      // Display a message indicating more interactions are required
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  /**
   * Switches the scene back to the crime scene.
   *
   * @param event The mouse event associated with switching back to the crime scene.
   */
  @FXML
  private void onSwitchToCrimeScene(MouseEvent event) {
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  /**
   * Switches the scene to the maid's room when the maid suspect is interacted with.
   *
   * @param event The mouse event associated with switching to the maid's room.
   */
  @FXML
  private void onSwitchToMaidRoom(MouseEvent event) {
    context.setSuspectInteracted("maid");
    switchScene(event, "/fxml/maid-room.fxml");
  }

  /**
   * Switches the scene to the bartender's bar when the bartender suspect is interacted with.
   *
   * @param event The mouse event associated with switching to the bartender's bar.
   */
  @FXML
  private void onSwitchToBar(MouseEvent event) {
    context.setSuspectInteracted("bartender");
    switchScene(event, "/fxml/bar-room.fxml");
  }

  /**
   * Switches the scene to the first mate's deck when the sailor suspect is interacted with.
   *
   * @param event The mouse event associated with switching to the deck.
   */
  @FXML
  private void onSwitchToDeck(MouseEvent event) {
    context.setSuspectInteracted("sailor");
    switchScene(event, "/fxml/deck.fxml");
  }

  /**
   * Switches the current scene to the specified FXML file.
   *
   * @param event The mouse event associated with the scene switch.
   * @param fxmlFile The path to the FXML file to switch to.
   */
  private void switchScene(MouseEvent event, String fxmlFile) {
    try {
      // Use non-static FXMLLoader to load the FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Parent newScene = loader.load(); // Load the new scene

      Stage stage = (Stage) room.getScene().getWindow();
      Scene scene = new Scene(newScene);

      // Add the CSS stylesheet
      newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the new scene
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace(); // Handle IOException
    }
  }
}
