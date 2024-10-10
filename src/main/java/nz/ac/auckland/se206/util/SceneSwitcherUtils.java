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

public class SceneSwitcherUtils {

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

  public static void handleGuessClick(GameStateContext context, MouseEvent event)
      throws IOException {
    if (context.canGuess()) {
      TimerManager timerManager = TimerManager.getInstance();
      timerManager.startGuessingTimer();
      context.setState(context.getGuessingState());

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      SceneSwitcherUtils.switchScene(event, "/fxml/guessing.fxml", stage);
    } else {
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  public static void updateProgressBar(
      ImageView clueProgressBar, ImageView suspectsProgressBar, GameStateContext context) {
    if (clueProgressBar != null) {
      int cluesInteracted = context.getNumCluesInteracted();
      clueProgressBar.setImage(new Image("/images/layouts/bar" + cluesInteracted + ".png"));
    }

    if (suspectsProgressBar != null) {
      int suspectsInteracted = context.getNumSuspectsInteracted();
      suspectsProgressBar.setImage(new Image("/images/layouts/bar" + suspectsInteracted + ".png"));
    }
  }
}
