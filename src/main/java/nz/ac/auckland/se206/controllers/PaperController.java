package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/** The PaperController class handles the interactions with the paper clue scene. */
public class PaperController extends ClueSoundController {

  @FXML private ImageView paperImageView;

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
    super.initialize(); // Call the parent initialize method
    setupPaperImages(); // Initialize event handlers and images
    updateGuessButtonState();
    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
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
      paperImageView.setOnMouseClicked(this::handlePaperClick);

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
        // Mark that the clue has been interacted with
        context.setClueInteracted(true, "paper");
        updateProgressBar();
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
    ImageView paper = (ImageView) event.getSource();
    paper.setCursor(Cursor.DEFAULT);
    paper.setStyle(""); // Reset to default style
  }
}
