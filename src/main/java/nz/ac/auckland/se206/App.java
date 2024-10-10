package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.util.SoundPlayer;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {

  private static Scene scene;
  private static Stage stage;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    Parent root = loadFxml(fxml);
    scene.setRoot(root);
    root.requestFocus(); // Set focus to the new root for keyboard input
  }

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file
   * @throws IOException if the FXML file is not found
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * Opens the chat view and sets the profession in the chat controller.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String profession) throws IOException {
    long startTime = System.nanoTime();
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            Platform.runLater(
                () -> {
                  try {
                    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/chat.fxml"));
                    Parent root = loader.load();

                    ChatController chatController = loader.getController();
                    chatController.setProfession(profession);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1_000_000; // convert to milliseconds
                    System.out.println("openChat took: " + duration + " ms");
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                });

            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }

  /**
   * Returns the primary stage of the application.
   *
   * @return the primary stage of the application
   */
  public static Stage getStage() {
    return stage;
  }

  private SoundPlayer soundPlayer = new SoundPlayer();

  /**
   * This method is invoked when the application starts. It loads and shows the "title" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/title.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    // Play background music if necessary
    soundPlayer.playBackgroundTracks();

    // Load fonts for the application
    Font.loadFont(getClass().getResourceAsStream("/fonts/timer-text.ttf"), 24);
    Font.loadFont(getClass().getResourceAsStream("/fonts/MonoSpaceTypewriter.ttf"), 24);
    Font.loadFont(getClass().getResourceAsStream("/fonts/albertusnova_bold.otf"), 24);
    Font.loadFont(getClass().getResourceAsStream("/fonts/main-title.ttf"), 24);

    // Load the title scene
    Parent root = loadFxml("crime-scene");
    scene = new Scene(root);
    stage.setScene(scene);
    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    stage.setTitle("Lost Time at Sea"); // Set the window title
    stage.show();
    root.requestFocus(); // Request focus so key events can be processed

    App.stage = stage;
  }
}
