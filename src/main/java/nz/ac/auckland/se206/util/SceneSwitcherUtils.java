package nz.ac.auckland.se206.util;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;

public class SceneSwitcherUtils extends SoundPlayer {
  /**
   * Switches scene by taking a mouse event and an fxml file and stage and loads fxml scene.
   * 
   * @param event the mouse event
   * @param fxmlFile the fxml file to load
   * @param stage the stage to load
   */
  public static void switchScene(MouseEvent event, String fxmlFile, Stage stage) {
    // Show a loading indicator or keep the current scene
    // Optionally, you can add a fade-out effect here

    // Create a Task to load the FXML in a background thread
    Task<Parent> loadSceneTask =
        new Task<Parent>() {
          @Override
          protected Parent call() throws Exception {
            FXMLLoader loader = new FXMLLoader(SceneSwitcherUtils.class.getResource(fxmlFile));
            Parent newScene = loader.load(); // Load the new scene
            return newScene;
          }
        };

    // On succeeded, update the UI on the JavaFX Application Thread
    loadSceneTask.setOnSucceeded(
        workerStateEvent -> {
          Parent newScene = loadSceneTask.getValue();
          Scene scene = new Scene(newScene);

          // Add the CSS stylesheet
          newScene
              .getStylesheets()
              .add(SceneSwitcherUtils.class.getResource("/css/styles.css").toExternalForm());

          // Set the new scene on the stage
          stage.setScene(scene);
          stage.show();
        });

    // On failed, handle the exception
    loadSceneTask.setOnFailed(
        workerStateEvent -> {
          Throwable exception = loadSceneTask.getException();
          exception.printStackTrace();
          // Optionally, handle the error on the UI thread
          Platform.runLater(
              () -> {
                // Show an error dialog or return to a safe state
                System.err.println("Failed to load the FXML file: " + fxmlFile);
              });
        });

    // Start the Task in a background thread
    Thread thread = new Thread(loadSceneTask);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Handles the clicking of the guess button based on the game state.
   * 
   * @param context takes the context of the game
   * @param event the mouse vent
   * @throws IOException
   */
  public static void handleGuessClick(GameStateContext context, MouseEvent event)
      throws IOException {
    // checks if the player is able to guess
    if (context.canGuess()) {
      TimerManager timerManager = TimerManager.getInstance();
      timerManager.startGuessingTimer();
      context.setState(context.getGuessingState());

      // switches to guessing scene if the player is able to guess
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      SceneSwitcherUtils.switchScene(event, "/fxml/guessing.fxml", stage);
    } else {
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  /**
   * Updates the progress bars of the clues and suspect interactions.
   * 
   * @param clueProgressBar progress bar for the clue progress
   * @param suspectsProgressBar the progress bar for the suspect progress
   * @param context the context of the game
   */
  public static void updateProgressBar(
      ImageView clueProgressBar, ImageView suspectsProgressBar, GameStateContext context) {
    // checks if the clue progress bar is empty or not
    if (clueProgressBar != null) {
      int cluesInteracted = context.getNumCluesInteracted();
      // updates progress bar image based on how many clues have been interacted with
      clueProgressBar.setImage(new Image("/images/layouts/bar" + cluesInteracted + ".png"));
    }

    // checks if the suspect interaction progress bar is empty or not
    if (suspectsProgressBar != null) {
      int suspectsInteracted = context.getNumSuspectsInteracted();
      // updates progress bar image based on how many suspects have been interacted with
      suspectsProgressBar.setImage(new Image("/images/layouts/bar" + suspectsInteracted + ".png"));
    }
  }
}
