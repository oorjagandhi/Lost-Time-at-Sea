package nz.ac.auckland.se206.util;

import java.io.IOException;
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
    try {
      FXMLLoader loader = new FXMLLoader(SceneSwitcherUtils.class.getResource(fxmlFile));
      Parent newScene = loader.load(); // Load the new scene

      Scene scene = new Scene(newScene);

      // Add the CSS stylesheet
      newScene
          .getStylesheets()
          .add(SceneSwitcherUtils.class.getResource("/css/styles.css").toExternalForm());

      // Set the new scene on the stage
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void handleGuessClick(GameStateContext context, MouseEvent event)
      throws IOException {
    if (context.canGuess()) {
      TimerManager timerManager = TimerManager.getInstance();
      timerManager.startGuessingTimer();
      context.setState(context.getGuessingState());

      FXMLLoader loader = new FXMLLoader(context.getClass().getResource("/fxml/guessing.fxml"));
      Parent root = loader.load();

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
