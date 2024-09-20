package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.se206.util.SoundPlayer;

public class FloorController extends SoundPlayer {

  @FXML private Circle screw1;
  @FXML private Circle screw2;
  @FXML private Circle screw3;
  @FXML private ImageView floorBoard;

  private List<Circle> screws;
  private boolean allScrewsRemoved = false;

  @FXML
  public void initialize() {
    // Add screws to a list for easy management
    screws = new ArrayList<>();
    screws.add(screw1);
    screws.add(screw2);
    screws.add(screw3);

    // Set up event handlers for screws
    for (Circle screw : screws) {
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
  }

  // Handle screw click to fade it away
  private void handleScrewClick(MouseEvent event) {
    Circle screw = (Circle) event.getSource();
    fadeOutScrew(screw);
    screws.remove(screw);
    playSound("/sounds/screw.mp3");

    // Check if all screws are removed
    if (screws.isEmpty()) {
      allScrewsRemoved = true;
      floorBoard.setDisable(false); // Enable floorboard interactions
    }
  }

  // Apply fade-out transition to the screw
  private void fadeOutScrew(Circle screw) {
    FadeTransition fade = new FadeTransition(Duration.seconds(1), screw);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setOnFinished(e -> screw.setVisible(false)); // Hide the screw after fading
    fade.play();
  }

  // Highlight screw on hover
  private void handleMouseEnterScrew(MouseEvent event) {
    Circle screw = (Circle) event.getSource();
    screw.setCursor(Cursor.HAND);
    screw.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  // Remove screw highlight when mouse exits
  private void handleMouseExitScrew(MouseEvent event) {
    Circle screw = (Circle) event.getSource();
    screw.setCursor(Cursor.DEFAULT);
    screw.setStyle(""); // Reset to default style
  }

  // Highlight floorboard on hover after screws are removed
  private void handleMouseEnterFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setCursor(Cursor.OPEN_HAND);
      floorBoard.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
    }
  }

  // Remove floorboard highlight when mouse exits
  private void handleMouseExitFloorboard(MouseEvent event) {
    floorBoard.setCursor(Cursor.DEFAULT);
    floorBoard.setStyle(""); // Reset to default style
  }

  // Allow floorboard to be dragged
  private void handleMousePressFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setCursor(Cursor.CLOSED_HAND);
    }
  }

  // Handle floorboard dragging
  private void handleMouseDragFloorboard(MouseEvent event) {
    if (allScrewsRemoved) {
      floorBoard.setLayoutX(event.getSceneX() - floorBoard.getFitWidth() / 2);
      floorBoard.setLayoutY(event.getSceneY() - floorBoard.getFitHeight() / 2);
    }
  }

  @FXML
  private void onBackButtonAction(ActionEvent event) {
    try {
      // Load the FXML file for the room
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent roomContent = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();

      roomContent.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the scene to the room
      stage.setScene(new javafx.scene.Scene(roomContent));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
