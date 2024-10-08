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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SceneSwitcher;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

public class FloorController extends SoundPlayer {

  @FXML private ImageView screw1;
  @FXML private ImageView screw2;
  @FXML private ImageView screw3;
  @FXML private ImageView floorBoard;
  @FXML private ImageView btnGuess;
  @FXML private ImageView suspectsProgressBar;
  @FXML private ImageView clueProgressBar;

  private static GameStateContext context = GameStateContext.getInstance();

  @FXML private AnchorPane room;
  private List<ImageView> screws;
  private boolean allScrewsRemoved = false;

  @FXML
  public void initialize() {
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

    updateProgressBar();
  }

  // Handle screw click to fade it away
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

  // Apply fade-out transition to the screw
  private void fadeOutScrew(ImageView screw) {
    FadeTransition fade = new FadeTransition(Duration.seconds(1), screw);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setOnFinished(e -> screw.setVisible(false)); // Hide the screw after fading
    fade.play();
  }

  // Highlight screw on hover
  private void handleMouseEnterScrew(MouseEvent event) {
    ImageView screw = (ImageView) event.getSource();
    screw.setCursor(Cursor.HAND);
    screw.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  // Remove screw highlight when mouse exits
  private void handleMouseExitScrew(MouseEvent event) {
    ImageView screw = (ImageView) event.getSource();
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
      // Load the FXML file for the crimescene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent roomContent = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();

      roomContent.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the scene to the crimescene
      stage.setScene(new javafx.scene.Scene(roomContent));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleMouseEnterClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView

    // check if the player is allowed to guess now
    if (!context.canGuess() && source.equals(btnGuess)) {
      return;
    }
    source.setCursor(Cursor.HAND); // Change cursor to hand to indicate interactivity
    source.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  @FXML
  private void handleMouseExitClue(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }

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

  @FXML
  private void handleGuessClick(MouseEvent event) throws IOException {

    if (context.canGuess()) {
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
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  @FXML
  private void onSwitchToCrimeScene(MouseEvent event) {
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  @FXML
  private void onSwitchToMaidRoom(MouseEvent event) {
    context.setSuspectInteracted("maid");
    switchScene(event, "/fxml/maid-room.fxml");
  }

  @FXML
  private void onSwitchToBar(MouseEvent event) {
    context.setSuspectInteracted("bartender");
    switchScene(event, "/fxml/bar-room.fxml");
  }

  @FXML
  private void onSwitchToDeck(MouseEvent event) {
    context.setSuspectInteracted("sailor");
    switchScene(event, "/fxml/deck.fxml");
  }

  private void switchScene(MouseEvent event, String fxmlFile) {
    try {
      // Use non-static FXMLLoader to load the FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Parent newScene = loader.load(); // Load the new scene

      // Get the stage from the current event
      Stage stage = (Stage) room.getScene().getWindow();
      Scene scene = new Scene(newScene);

      newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Set the new scene
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace(); // Handle IOException
    }
  }
}
