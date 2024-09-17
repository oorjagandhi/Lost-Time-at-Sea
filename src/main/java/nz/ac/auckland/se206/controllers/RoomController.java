package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.RoomManager;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  private static boolean isFirstTimeInit = true;
  private static GameStateContext context = new GameStateContext();
  private String currentSuspect;

  @FXML private Rectangle floorBoard;

  @FXML private Rectangle rectSecurity;
  @FXML private Rectangle rectArtist;
  @FXML private Rectangle rectCollector;

  @FXML private ImageView suspectBartender;

  @FXML private AnchorPane room;

  @FXML private Rectangle rectSuspect;
  @FXML private ImageView suspectMaid;
  @FXML private ImageView suspectSailor;

  @FXML private Button btnGuess;
  @FXML private Button btnBack;

  @FXML private ImageView book;
  @FXML private ImageView suspectIcon;
  @FXML private ImageView paperImageView;

  @FXML private Pane popupContainer;
  @FXML private VBox chatContainer;
  @FXML private ChatController chatController;

  private final TimerManager timerManager = TimerManager.getInstance();
  private final RoomManager roomManager = RoomManager.getInstance();

  private MediaPlayer mediaPlayer;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() throws IOException {
    System.out.println("Initializing RoomController...");

    timerManager.setGuessingStartListener(this::guessingStartListener);

    updateGuessButtonState();
    context.setUpdateGuessButtonStateCallback(this::updateGuessButtonAvailability);
    if (!roomManager.isUserWelcomed()) {
      // playSound("/sounds/welcome.mp3");
    }
    roomManager.setUserWelcomed(true);

    if (floorBoard != null) {
      floorBoard.setOnMouseEntered(this::handleMouseEnterFloorBoard);
      floorBoard.setOnMouseExited(this::handleMouseExitFloorBoard);
    }

    if (paperImageView != null) {
      paperImageView.setOnMouseEntered(this::handleMouseEnterpaperImageView);
      paperImageView.setOnMouseExited(this::handleMouseExitpaperImageView);
    }

    if (rectSuspect != null) {
      rectSuspect.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> handleRectangleHover());
      rectSuspect.addEventFilter(MouseEvent.MOUSE_EXITED, event -> handleRectangleHoverExit());
      rectSuspect.addEventFilter(
          MouseEvent.MOUSE_CLICKED,
          event -> {
            // Pass click event to the underlying ImageView (suspectBartender in this case)
            if (suspectBartender != null) {
              suspectBartender.fireEvent(event);
            }
            if (suspectMaid != null) {
              suspectMaid.fireEvent(event);
            }
            if (suspectSailor != null) {
              suspectSailor.fireEvent(event);
            }
          });
    }

    System.out.println("suspectMaid: " + suspectMaid);
    System.out.println("suspectBartender: " + suspectBartender);
    System.out.println("suspectSailor: " + suspectSailor);

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
                Platform.runLater(() -> updateSuspectIcon(isNowLoading));
              });
    }
  }

  private void guessingStartListener() {
    // Ensure UI updates are performed on the JavaFX application thread
    Platform.runLater(
        () -> {
          timerManager.setCanGuess(context.canGuess());
          context.setState(context.getGuessingState());
        });
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

  public void handleSuspectClick(MouseEvent event) {
    System.out.println("Suspect clicked");
    Node source = (Node) event.getSource();
    String suspectId = source.getId(); // e.g., "suspectMaid" or "suspectBartender"
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);

    // Handle the suspect interaction
    switch (suspectId) {
      case "suspectMaid":
        currentSuspect = "maid";
        showChat("maid");
        break;
      case "suspectBartender":
        currentSuspect = "bartender";
        showChat("bartender");
        break;
      case "suspectSailor":
        currentSuspect = "sailor";
        showChat("sailor");
        break;
      default:
        System.out.println("Unknown suspect ID: " + suspectId);
    }
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    // Check if the player is ready to guess
    if (context.canGuess()) {
      // Set the game state to guessing if the player has interacted with the required elements
      context.setState(context.getGuessingState());
      System.out.println("Transitioning to guessing state. Ready to make a guess.");
    } else {
      // Inform the player that they need to interact with both a clue and a suspect
      System.out.println("You must interact with both a clue and a suspect before you can guess.");
    }
  }

  @FXML
  private void handleSecurityClick(MouseEvent event) throws IOException {
    // handles opening the chat for the security and updates suspect interaction
    chatController.clearChat();
    context.setSuspectInteracted(true);
    updateGuessButtonAvailability();
    // Open chat with the security guard
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "rectSecurity");
    } else {
      showChat("security");
      System.out.println("security");
    }
  }

  @FXML
  private void handleCollectorClick(MouseEvent event) throws IOException {
    // handles opening the chat for the collector and updates suspect itneraction
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "rectCollector");
    } else {
      showChat("collector");
      System.out.println("collector");
    }
  }

  @FXML
  private void handleArtistClick(MouseEvent event) throws IOException {
    // handles opening the cha tfor the artist and updates suspect interaction
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "rectArtist");
    } else {
      showChat("artist");
      System.out.println("artist");
    }
  }

  @FXML
  private void handleBartenderClick(MouseEvent event) throws IOException {
    // handles opening the chat for the bartender and updates suspect interaction
    System.out.println("PLEASEPLAESEPLAESE");
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "suspectBartender");
    } else {
      showChat("bartender");
      System.out.println("bartender");
    }
  }

  @FXML
  private void handleMaidClick(MouseEvent event) throws IOException {
    // handles opening the chat for the maid and updates suspect interaction
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "suspectMaid");
    } else {
      showChat("maid");
      System.out.println("maid");
    }
  }

  @FXML
  private void handleSailorClick(MouseEvent event) throws IOException {
    // handles opening the chat for the sailor and updates suspect interaction
    chatController.clearChat();
    updateGuessButtonAvailability();
    context.setSuspectInteracted(true);
    if (context.getState().equals(context.getGuessingState())) {
      context.handleRectangleClick(event, "suspectMaid");
    } else {
      showChat("maid");
      System.out.println("maid");
    }
  }

  private void handleRectangleHover() {
    rectSuspect.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  private void handleRectangleHoverExit() {
    rectSuspect.setStyle(""); // Remove the drop shadow effect
  }

  @FXML
  private void handleMouseEnter(MouseEvent event) {
    Node source = (Node) event.getSource();
    ImageView suspectImageView = getSuspectImageView(source);
    if (suspectImageView != null) {
      suspectImageView.setStyle(
          "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
    }
  }

  @FXML
  private void handleMouseExit(MouseEvent event) {
    Node source = (Node) event.getSource();
    ImageView suspectImageView = getSuspectImageView(source);
    if (suspectImageView != null) {
      suspectImageView.setStyle(""); // Remove the drop shadow effect
    }
  }

  @FXML
  private void handlePhoneClick(MouseEvent event) {
    if (chatContainer != null) {
      chatContainer.setVisible(false);
    }

    context.setClueInteracted(true);
    try {
      // Load the FXML file for the pop-up
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/phoneZoom.fxml"));
      Parent popupContent = loader.load();

      // Clear existing content and add new pop-up content
      popupContainer.getChildren().clear();
      popupContainer.getChildren().add(popupContent);

      // Make the pop-up visible
      popupContainer.setVisible(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void showChat(String profession) {
    currentSuspect = profession;
    if (chatController != null) {
      chatController.setProfession(profession);
      chatContainer.setVisible(true);
      suspectIcon.setVisible(true);
      // Set the suspect icon to the appropriate image
      updateSuspectIcon(false);
    } else {
      System.out.println("Chat controller is null");
    }
  }

  private void updateGuessButtonState() {
    if (btnGuess != null) {
      btnGuess.setDisable(!context.canGuess());
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

  private void playSound(String filePath) {
    // runs finding a particular audiofile as a background task
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            // checks if a given audio file exists and if it does,plays it
            URL resource = getClass().getResource(filePath);
            if (resource == null) {
              Platform.runLater(() -> System.out.println("File not found: " + filePath));
              return null;
            }
            // if audio file is found, plays and then stops once it is finished
            Media media = new Media(resource.toString());
            Platform.runLater(
                () -> {
                  if (mediaPlayer != null) {
                    mediaPlayer.stop();
                  }
                  mediaPlayer = new MediaPlayer(media);
                  mediaPlayer.play();
                });
            return null;
          }
        };
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }

  @FXML
  private void onChangeToRadioScene(ActionEvent event) {
    try {
      // Load the FXML file for the radio scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/radio.fxml"));
      Parent radioContent = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the radio scene
      stage.setScene(new Scene(radioContent));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void switchToCrimeScene(ActionEvent event) {
    switchScene(event, "/fxml/crime-scene.fxml");
  }

  @FXML
  private void switchToMaidRoom(ActionEvent event) {
    switchScene(event, "/fxml/maid-room.fxml");
  }

  @FXML
  private void switchToBar(ActionEvent event) {
    switchScene(event, "/fxml/bar-room.fxml");
  }

  @FXML
  private void switchToDeck(ActionEvent event) {
    switchScene(event, "/fxml/deck.fxml");
  }

  // Method to handle mouse entering the floorBoard
  private void handleMouseEnterFloorBoard(MouseEvent event) {
    floorBoard.setCursor(Cursor.HAND); // Change cursor to hand
    floorBoard.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  // Method to handle mouse exiting the floorBoard
  private void handleMouseExitFloorBoard(MouseEvent event) {
    floorBoard.setCursor(Cursor.DEFAULT); // Reset cursor
    floorBoard.setStyle(""); // Remove the drop shadow effect
  }

  @FXML
  private void handleFloorBoardClick(MouseEvent event) {
    System.out.println("Floor clicked, attempting to load floor.fxml...");
    try {
      // Load the FXML file for the radio scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/floor.fxml"));
      Parent floorScene = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the radio scene
      stage.setScene(new Scene(floorScene));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void switchScene(ActionEvent event, String fxmlFile) {
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

  // Method to handle mouse entering the paper
  private void handleMouseEnterpaperImageView(MouseEvent event) {
    paperImageView.setCursor(Cursor.HAND); // Change cursor to hand
    paperImageView.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  // Method to handle mouse exiting the paper
  private void handleMouseExitpaperImageView(MouseEvent event) {
    paperImageView.setCursor(Cursor.DEFAULT); // Reset cursor
    paperImageView.setStyle(""); // Remove the drop shadow effect
  }

  @FXML
  private void handlePaperClick(MouseEvent event) {
    System.out.println("Paper clicked, attempting to load paper.fxml...");
    try {
      // Load the FXML file for the radio scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paper.fxml"));
      Parent paperScene = loader.load();

      // Get the current stage
      Node source = (Node) event.getSource();
      Stage stage = (Stage) source.getScene().getWindow();

      // Set the scene to the radio scene
      stage.setScene(new javafx.scene.Scene(paperScene));
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
      // Use the original image depending on the current suspect
      switch (currentSuspect) {
        case "maid":
          imagePath = "/images/cleaner-closeup2.png"; // Replace with the maid's icon image
          break;
        case "bartender":
          imagePath = "/images/bartender-closeup.png"; // Replace with the bartender's icon image
          break;
        case "sailor":
          imagePath = "/images/sailor_closeup1.png"; // Replace with the sailor's icon image
          break;
        default:
          // Default image if current suspect is not set
          imagePath = "/images/default-icon.png"; // Replace with a default icon if needed
          break;
      }
    }
    suspectIcon.setImage(new Image(getClass().getResourceAsStream(imagePath)));
  }
}
