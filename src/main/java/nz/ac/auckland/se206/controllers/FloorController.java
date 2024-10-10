package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/** The FloorController class handles interactions iwth the floorboard clue. */
public class FloorController extends ClueSoundController {

  @FXML private ImageView screw1;
  @FXML private ImageView screw2;
  @FXML private ImageView screw3;
  @FXML private ImageView floorBoard;
  @FXML private ImageView currentScene;

  private List<ImageView> screws;
  private boolean allScrewsRemoved = false;

  /** Initializes the floor view. Sets up event handlers for screws and floorboard interactions. */
  @FXML
  public void initialize() {
    super.initialize(); // Call the parent initialize method

    // Add screws to a list for easy management
    screws = new ArrayList<>();
    screws.add(screw1);
    screws.add(screw2);
    screws.add(screw3);

    // Set up event handlers for screws
    for (ImageView screw : screws) {
      screw.setOnMouseClicked(this::handleScrewClick);
      screw.setOnMouseEntered(this::handleMouseEnterScrew);
      screw.setOnMouseExited(this::handleMouseExitScrew);
    }

    // Initially, disable floorboard interactions
    floorBoard.setDisable(true);
    floorBoard.setOnMouseEntered(this::handleMouseEnterFloorboard);
    floorBoard.setOnMouseExited(this::handleMouseExitFloorboard);
    floorBoard.setOnMousePressed(this::handleMousePressFloorboard);
    floorBoard.setOnMouseDragged(this::handleMouseDragFloorboard);

    updateProgressBar(); // Method from ClueSoundController

    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
    updateGuessButtonState();
  }

  /**
   * Handles the event when a screw is clicked. The screw will fade out and be removed from the
   * list.
   *
   * @param event the mouse event
   */
  private void handleScrewClick(MouseEvent event) {
    ImageView screw = (ImageView) event.getSource();
    fadeOutScrew(screw);
    screws.remove(screw);
    playSound("/sounds/screw.mp3");

    // Check if all screws are removed
    if (screws.isEmpty()) {
      allScrewsRemoved = true;
      floorBoard.setDisable(false); // Enable floorboard interactions
    }
  }

  /**
   * Fades out the screw when it is clicked.
   *
   * @param screw the screw to fade out
   */
  private void fadeOutScrew(ImageView screw) {
    FadeTransition fade = new FadeTransition(Duration.seconds(1), screw);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setOnFinished(e -> screw.setVisible(false)); // Hide the screw after fading
    fade.play();
  }

  /**
   * Highlight screw when the mouse cursor hovers over them.
   *
   * @param event the mouse event
   */
  private void handleMouseEnterScrew(MouseEvent event) {
    ImageView screw = (ImageView) event.getSource();
    screw.setCursor(Cursor.HAND);
    screw.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  /**
   * Remove screw highlight when mouse exits.
   *
   * @param event the mouse event
   */
  private void handleMouseExitScrew(MouseEvent event) {
    ImageView screw = (ImageView) event.getSource();
    screw.setCursor(Cursor.DEFAULT);
    screw.setStyle(""); // Reset to default style
  }

  /**
   * Highlight floorboard when mouse cursor hovers over it.
   *
   * @param event the mouse event
   */
  private void handleMouseEnterFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setCursor(Cursor.OPEN_HAND);
      floorBoard.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
    }
  }

  /**
   * Remove floorboard highlight when mouse exits.
   *
   * @param event the mouse event
   */
  private void handleMouseExitFloorboard(MouseEvent event) {
    floorBoard.setCursor(Cursor.DEFAULT);
    floorBoard.setStyle(""); // Reset to default style
  }

  /**
   * Handles the pressing of the floorboard clue.
   *
   * @param event the mouse event
   */
  private void handleMousePressFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setCursor(Cursor.CLOSED_HAND);
    }
  }

  /**
   * Handles the dragging of the floorboard clue.
   *
   * @param event the mouse event
   */
  private void handleMouseDragFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setLayoutX(event.getSceneX() - floorBoard.getFitWidth() / 2);
      floorBoard.setLayoutY(event.getSceneY() - floorBoard.getFitHeight() / 2);
    }
  }
}
