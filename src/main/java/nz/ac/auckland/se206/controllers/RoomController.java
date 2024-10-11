package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.io.InputStream;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.RoomManager;
import nz.ac.auckland.se206.util.SceneSwitcher;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController extends SoundPlayer {

  private static GameStateContext context = GameStateContext.getInstance();
  private static boolean isFirstTimeInit = true;

  private static final String[] THINKING_IMAGES = {
    "/images/think.png", "/images/think-1.png", "/images/think-2.png", "/images/think-3.png"
  };

  private String currentSuspect;
  private int currentThinkingImageIndex = 0;
  private final RoomManager roomManager = RoomManager.getInstance();
  private Timeline thinkingTimeline;

  @FXML private AnchorPane room;

  @FXML private Button btnBack;
  @FXML private ImageView btnGuess;

  @FXML private VBox chatContainer;

  @FXML private ChatController chatController;

  @FXML private ImageView book;
  @FXML private ImageView floorBoardImageView;
  @FXML private ImageView paperImageView;
  @FXML private ImageView radioImageView;
  @FXML private ImageView suspectBartender;
  @FXML private ImageView suspectIcon;
  @FXML private ImageView suspectMaid;
  @FXML private ImageView suspectSailor;
  @FXML private ImageView thinkingBubble;
  @FXML private ImageView clueProgressBar;
  @FXML private ImageView suspectsProgressBar;
  @FXML private ImageView currentScene;

  @FXML private Pane popupContainer;

  @FXML private Rectangle rectSuspect;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() throws IOException {
    System.out.println("Initializing RoomController...");

    updateGuessButtonState();
    context.setUpdateGuessButtonStateCallback(this::updateGuessButtonAvailability);
    if (!roomManager.isUserWelcomed()) {
      playSound("/sounds/welcome.mp3");
    }
    roomManager.setUserWelcomed(true);

    // Initialize chat controller
    if (chatContainer != null) {
      // Load chat.fxml manually
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
      Node chatContent = loader.load();
      chatController = loader.getController();
      chatContainer.getChildren().add(chatContent);

      // Set up the listener for isLoading
      chatController
          .isLoadingProperty()
          .addListener(
              (obs, wasLoading, isNowLoading) -> {
                Platform.runLater(
                    () -> {
                      try {
                        Thread.sleep(300);
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                      updateSuspectIcon(isNowLoading);
                      if (isNowLoading) {
                        startThinkingAnimation();
                      } else {
                        stopThinkingAnimation();
                      }
                    });
              });
    }

    // Set up the timer for the room view
    TimerManager.getInstance()
        .setNoGuessTimeListener(
            () -> {
              // Handle the end of the initial timer phase
              Platform.runLater(
                  () -> {
                    try {
                      // Load the feedback-notime.fxml file
                      FXMLLoader loader =
                          new FXMLLoader(getClass().getResource("/fxml/feedback-notime.fxml"));
                      Parent root = loader.load();
                      Stage stage = (Stage) room.getScene().getWindow();
                      stage.setScene(new Scene(root));
                      stage.show();
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  });
            });

    // Start the timer
    TimerManager.getInstance().startTimer();
    // set the image of the progress bar

    // **Set the profession based on the current room or suspect**
    setProfessionForCurrentScene();

    // **Update suspect icon**
    updateSuspectIcon(false);

    // **Optionally set input focus to the chat input field**
    if (chatController != null) {
      chatController.setInputFocus();
    }

    // set the image of the progress bar
    updateProgressBar();

    // set the red border around the current scene to indicate the user's location
    if (room != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
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

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) throws ApiProxyException, IOException {
    System.out.println("Key " + event.getCode() + " released");
    if (event.getCode() == KeyCode.ENTER && chatController != null) {
      chatController.sendMessage();
    }
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) {
    // handles when a rectangle is clicked
    Node source = (Node) event.getSource();
    ImageView suspectImageView = getSuspectImageView(source);
    if (suspectImageView != null) {
      // Forward the click event to the suspect's ImageView
      suspectImageView.fireEvent(
          new MouseEvent(
              // information to be sent when rectangle is clicked
              MouseEvent.MOUSE_CLICKED,
              suspectImageView.getLayoutX(),
              suspectImageView.getLayoutY(),
              suspectImageView.getLayoutX(),
              suspectImageView.getLayoutY(),
              MouseButton.PRIMARY,
              1,
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              true,
              null));
    }
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
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
      // gets how many suspects nad clues have been interacted with
      int cluesInteractedWith = context.getNumCluesInteracted();
      int suspectsInteractedWith = context.getNumSuspectsInteracted();
      // plays a different sound depending on the amounts of interaction
      if ((cluesInteractedWith == 0) && (suspectsInteractedWith < 3)) {
        playSound("/sounds/suspect-and-clue.mp3");
      } else if (cluesInteractedWith == 0) {
        playSound("/sounds/clue.mp3");
      } else {
        playSound("/sounds/suspect.mp3");
      }
    }
  }

  @FXML
  private void handleMouseEnter(MouseEvent event) {
    Node source = (Node) event.getSource();
    source.getScene().setCursor(Cursor.HAND); // Set cursor to hand on hover
    ImageView suspectImageView = getSuspectImageView(source);
    if (suspectImageView != null) {
      suspectImageView.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
    }
  }

  @FXML
  private void handleMouseExit(MouseEvent event) {
    Node source = (Node) event.getSource();
    source.getScene().setCursor(Cursor.DEFAULT); // Reset cursor to default when not hovering
    ImageView suspectImageView = getSuspectImageView(source);
    if (suspectImageView != null) {
      suspectImageView.setStyle(""); // Remove the drop shadow effect
    }
  }

  private void startThinkingAnimation() {
    Platform.runLater(
        () -> {
          thinkingBubble.setVisible(true);
          // Set the first thinking image to be shown immediately
          thinkingBubble.setImage(new Image(getClass().getResourceAsStream(THINKING_IMAGES[0])));
        });

    // Set up the timeline for changing the thinking bubble images
    thinkingTimeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(0.5),
                event -> {
                  // Increment the index and loop back to 0 if at the end
                  currentThinkingImageIndex =
                      (currentThinkingImageIndex + 1) % THINKING_IMAGES.length;

                  InputStream imageStream =
                      getClass().getResourceAsStream(THINKING_IMAGES[currentThinkingImageIndex]);
                  if (imageStream != null) {
                    thinkingBubble.setImage(new Image(imageStream));
                  } else {
                    System.err.println(
                        "Error: Image not found at path: "
                            + THINKING_IMAGES[currentThinkingImageIndex]);
                  }
                }));

    thinkingTimeline.setCycleCount(Animation.INDEFINITE);
    thinkingTimeline.play();
  }

  private void stopThinkingAnimation() {
    if (thinkingTimeline != null) {
      thinkingTimeline.stop();
    }
    currentThinkingImageIndex = 0; // Reset index for next time
    thinkingBubble.setVisible(false);
  }

  private void updateGuessButtonState() {
    if (btnGuess != null) {
      if (context.canGuess()) {
        btnGuess.setImage(new Image("/images/layouts/enabled-button.png"));
      } else {
        btnGuess.setImage(new Image("/images/layouts/disabled-button.png"));
      }
    }
  }

  private void updateGuessButtonAvailability() {
    if (btnGuess != null) {
      btnGuess.setDisable(!context.canGuess());
    }
  }

  // Method to handle mouse entering rectMaid and highlighting suspectMaid
  @FXML
  private void handleMouseEnterrectSuspect(MouseEvent event) {
    if (suspectMaid != null) {
      suspectMaid.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }

    if (suspectBartender != null) {
      suspectBartender.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }

    if (suspectSailor != null) {
      suspectSailor.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }
  }

  // Method to handle mouse exiting rectMaid and removing highlight from suspectMaid
  @FXML
  private void handleMouseExitrectSuspect(MouseEvent event) {
    if (suspectMaid != null) {
      suspectMaid.setStyle(""); // Remove the drop shadow effect
    }

    if (suspectBartender != null) {
      suspectBartender.setStyle(""); // Remove the drop shadow effect
    }

    if (suspectSailor != null) {
      suspectSailor.setStyle(""); // Remove the drop shadow effect
    }
  }

  @FXML
  private void handleRadioClick(MouseEvent event) {
    context.setClueInteracted(true, "radio");
    System.out.println("Radio clicked, attempting to load radio.fxml...");
    try {
      // Load the FXML file for the radio scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/radio.fxml"));
      Parent radioScene = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the radio scene
      stage.setScene(new Scene(radioScene));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
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

  @FXML
  private void handleFloorBoardClick(MouseEvent event) {
    context.setClueInteracted(true, "floorBoard");
    System.out.println("Floor clicked, attempting to load floor.fxml...");
    try {
      // Load the FXML file for the floorboard scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/floor.fxml"));
      Parent floorScene = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the floorboard scene
      stage.setScene(new Scene(floorScene));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void switchScene(MouseEvent event, String fxmlFile) {
    // Show a loading indicator or keep the current scene
    Stage stage = (Stage) room.getScene().getWindow();

    // Start a background thread to load the FXML
    new Thread(
            () -> {
              try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent newSceneRoot = loader.load();

                // After loading is complete, update the UI on the JavaFX Application Thread
                Platform.runLater(
                    () -> {
                      Scene newScene = new Scene(newSceneRoot);
                      newSceneRoot
                          .getStylesheets()
                          .add(getClass().getResource("/css/styles.css").toExternalForm());
                      stage.setScene(newScene);
                    });
              } catch (IOException e) {
                e.printStackTrace();
                // Optionally, handle the error on the UI thread
                Platform.runLater(
                    () -> {
                      // Show an error dialog or return to a safe state
                    });
              }
            })
        .start();
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

  @FXML
  private void handlePaperClick(MouseEvent event) {
    context.setClueInteracted(true, "paper");
    System.out.println("Paper clicked, attempting to load paper.fxml...");
    try {
      // Load the FXML file for the paper scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paper.fxml"));
      Parent paperScene = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the paper scene
      stage.setScene(new Scene(paperScene));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private ImageView getSuspectImageView(Node source) {
    // Check which suspect is associated with the node
    if (source.getId().contains("Maid")) {
      return suspectMaid;
    } else if (source.getId().contains("Bartender")) {
      return suspectBartender;
    } else if (source.getId().contains("Sailor")) {
      return suspectSailor;
    }
    // Add more conditions for other suspects if any
    return null;
  }

  private void updateSuspectIcon(boolean isLoading) {
    String imagePath;
    if (isLoading) {
      switch (currentSuspect) {
        case "maid":
          imagePath = "/images/cleaner-closeup3.png"; // Maid's loading image
          break;
        case "bartender":
          imagePath = "/images/bartender-closeup2.png"; // Bartender's loading image
          break;
        case "sailor":
          imagePath = "/images/sailor_closeup.png"; // Sailor's loading image
          break;
        default:
          imagePath = "/images/loading.jpg"; // Default loading image if needed
          break;
      }
    } else {
      switch (currentSuspect) {
        case "maid":
          imagePath = "/images/cleaner-closeup2.png"; // Maid's icon image
          break;
        case "bartender":
          imagePath = "/images/bartender-closeup.png"; // Bartender's icon image
          break;
        case "sailor":
          imagePath = "/images/sailor_closeup1.png"; // Sailor's icon image
          break;
        default:
          imagePath = "/images/default-icon.png"; // Default icon
          break;
      }
    }
    // Ensure the image path is correct and the image exists
    InputStream imageStream = getClass().getResourceAsStream(imagePath);
    if (imageStream != null) {
      suspectIcon.setImage(new Image(imageStream));
    } else {
      System.err.println("Error: Image not found at path: " + imagePath);
    }
  }

  @FXML
  private void handleClueClick(MouseEvent event) {
    // Assuming this method is triggered by clicking on a clue
    context.setClueInteracted(true, null);
    updateGuessButtonAvailability();
    // Additional code for clue interaction...
  }

  private void setProfessionForCurrentScene() {
    // Determine the profession based on the room or suspect
    // For example, if this is the maid's room
    // You can use FXML IDs or other indicators to determine the suspect

    // Example:
    if (suspectMaid != null) {
      currentSuspect = "maid";
    } else if (suspectBartender != null) {
      currentSuspect = "bartender";
    } else if (suspectSailor != null) {
      currentSuspect = "sailor";
    } else {
      currentSuspect = "unknown";
    }

    if (chatController != null) {
      chatController.setProfession(currentSuspect);
    }
  }
}
