package nz.ac.auckland.se206.util;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility class for switching scenes in a JavaFX application. Used to switch the scene of a stage
 * with a fade transition.
 */
public class SceneSwitcher {

  /**
   * Switches the scene of the given stage to the new root.
   *
   * @param stage the stage to switch the scene of
   * @param newRoot the new root to switch to
   */
  public static void switchScene(Stage stage, Parent newRoot) {
    Scene currentScene = stage.getScene();

    if (currentScene == null) {
      // Set the new scene for the first time
      stage.setScene(new Scene(newRoot));
    } else {
      // Apply fade out
      FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
      fadeOut.setFromValue(1.0);
      fadeOut.setToValue(0.0);
      fadeOut.setOnFinished(
          event -> {
            // Set the new root and apply fade in
            currentScene.setRoot(newRoot);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
          });
      fadeOut.play();
    }
  }
}
