package nz.ac.auckland.se206.util;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneSwitcher {

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
