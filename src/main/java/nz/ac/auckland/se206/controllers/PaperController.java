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

public class PaperController extends SoundPlayer {

  @FXML private ImageView paperImageView;
  @FXML private ImageView btnGuess;
  @FXML private ImageView suspectsProgressBar;
  @FXML private ImageView clueProgressBar;
  @FXML private ImageView currentScene;

  private static GameStateContext context = GameStateContext.getInstance();

  @FXML private AnchorPane room;

  private int clickCount = 0; // To track the number of clicks
  private final String[] paperImages = {
    "/images/clues/paper.png",
    "/images/clues/paper2.png",
    "/images/clues/paper3.png",
    "/images/clues/paper5.png"
  };

  @FXML
  public void initialize() {
    setupPaperImages(); // Ensure this is called to initialize event handlers and images
    updateProgressBar();
    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
  }

  // Method to set up the initial image and click handler
  public void setupPaperImages() {
    if (paperImageView != null) {
      // Set the initial image
      paperImageView.setImage(new Image(getClass().getResourceAsStream(paperImages[0])));
      // Set up click event handler
      paperImageView.setOnMouseClicked(event -> handlePaperClick(event));

      paperImageView.setOnMouseEntered(this::handleMouseEnterPaper);
      paperImageView.setOnMouseExited(this::handleMouseExitPaper);
    }
  }

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

  private void handleMouseEnterPaper(MouseEvent event) {
    if (clickCount < paperImages.length - 1) {
      ImageView paper = (ImageView) event.getSource();
      paper.setCursor(Cursor.HAND); // Change cursor to hand
      paper.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }
  }

  private void handleMouseExitPaper(MouseEvent event) {
    ImageView paper = (ImageView) event.getSource();
    paper.setCursor(Cursor.DEFAULT);
    paper.setStyle(""); // Reset to default style
  }

  @FXML
  private void onBackButtonAction(MouseEvent event) {

    switchScene(event, "/fxml/crime-scene.fxml");
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
