package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatController {

  private static final int MAX_MESSAGES = 3;

  private BooleanProperty isLoading = new SimpleBooleanProperty(false);

  private ChatCompletionRequest chatCompletionRequest;
  private String profession;

  @FXML private Button btnSend;
  @FXML private VBox chatBox;
  @FXML private TextField txtInput;

  /**
   * Initializes the chat view.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    // Any required initialization code can be placed here
  }

  public BooleanProperty isLoadingProperty() {
    return isLoading;
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt() {
    return PromptEngineering.getPrompt(profession);
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setProfession(String profession) {
    this.profession = profession;
    chatBox.getChildren().clear(); // Clear chat when profession changes
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.3)
              .setMaxTokens(40);
      // Initialize conversation with a system prompt
      chatCompletionRequest.addMessage(new ChatMessage("system", getSystemPrompt()));

      // Send initial assistant message
      sendInitialAssistantMessage();

    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat box.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    String displayRole;
    Color titleColor;
    double maxWidth = 400;

    if ("user".equals(msg.getRole())) {
      displayRole = "You";
      titleColor = Color.LIGHTGREEN;
    } else if ("assistant".equals(msg.getRole())) {
      displayRole = capitalize(profession);
      titleColor = Color.LIGHTBLUE;

      // Set the max width based on the current suspect (profession)
      switch (profession.toLowerCase()) {
        case "maid":
          maxWidth = 420; // Max width for Maid's responses
          break;
        case "bartender":
          maxWidth = 400; // Max width for Bartender's responses
          break;
        case "sailor":
          maxWidth = 420; // Max width for Sailor's responses
          break;
        default:
          maxWidth = 400; // Default max width for unknown professions
          break;
      }
    } else {
      displayRole = capitalize(msg.getRole());
      titleColor = Color.WHITE; // default color
    }

    // Create HBox for the message content
    HBox messageBox = new HBox();
    messageBox.setStyle("-fx-background-color: black; -fx-padding: 5;");
    messageBox.setSpacing(5); // Add spacing between title and content

    // Create Labels for the title and content
    Label titleLabel = new Label(displayRole + ": ");
    titleLabel.setTextFill(titleColor);
    titleLabel.setStyle("-fx-font-weight: bold;");

    Label contentLabel = new Label(msg.getContent());
    contentLabel.setTextFill(Color.WHITE);
    contentLabel.setWrapText(true);
    contentLabel.setMaxWidth(maxWidth); // Set the max width dynamically based on the suspect

    // Add the labels to the messageBox
    messageBox.getChildren().addAll(titleLabel, contentLabel);

    // Create a container to align the messageBox to left or right
    HBox messageContainer = new HBox();
    if ("user".equals(msg.getRole())) {
      messageContainer.setAlignment(Pos.CENTER_RIGHT);
    } else {
      messageContainer.setAlignment(Pos.CENTER_LEFT);
    }
    messageContainer.getChildren().add(messageBox);

    // Add the messageContainer to the chatBox
    Platform.runLater(
        () -> {
          chatBox.getChildren().add(messageContainer);
          System.out.println("Message added. Total messages: " + chatBox.getChildren().size());
          manageChatBoxOverflow();
        });
  }

  private void manageChatBoxOverflow() {
    // Remove oldest messages if the total exceeds MAX_MESSAGES
    while (chatBox.getChildren().size() > MAX_MESSAGES) {
      chatBox.getChildren().remove(0); // Remove the oldest message
      System.out.println("Removed oldest message. Total messages: " + chatBox.getChildren().size());
    }
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    isLoading.set(true);
    long startTime = System.nanoTime();
    final ChatMessage[] resultHolder = new ChatMessage[1];
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              chatCompletionRequest.addMessage(msg);
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              ChatMessage responseMessage = result.getChatMessage();
              // Schedule the update on the JavaFX Application Thread
              Platform.runLater(
                  () -> {
                    chatCompletionRequest.addMessage(responseMessage);
                    appendChatMessage(responseMessage);
                  });
              resultHolder[0] = responseMessage;
              long endTime = System.nanoTime();
              long duration = (endTime - startTime) / 1_000_000;
              System.out.println("runGpt took: " + duration + " ms");
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              // Ensure isLoading is set to false after the call
              Platform.runLater(() -> isLoading.set(false)); // API call ended
            }
            return null;
          }
        };
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
    return resultHolder[0];
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }

  public void clearChat() {
    chatBox.getChildren().clear();
  }

  public void sendMessage() throws ApiProxyException, IOException {
    onSendMessage(null);
  }

  public void setInputFocus() {
    txtInput.requestFocus();
  }

  /** Sends the initial assistant message to start the conversation. */
  public void sendInitialAssistantMessage() {
    isLoading.set(true);
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              // Since the conversation only has the system prompt, we can execute the chat
              // completion request
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              ChatMessage responseMessage = result.getChatMessage();
              Platform.runLater(
                  () -> {
                    chatCompletionRequest.addMessage(responseMessage);
                    appendChatMessage(responseMessage);
                  });
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              Platform.runLater(() -> isLoading.set(false));
            }
            return null;
          }
        };
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }
}
