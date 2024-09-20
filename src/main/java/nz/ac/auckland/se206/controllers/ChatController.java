package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;

  private BooleanProperty isLoading = new SimpleBooleanProperty(false);

  private ChatCompletionRequest chatCompletionRequest;
  private String profession;

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
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.1)
              .setTopP(0.3)
              .setMaxTokens(60);
      runGpt(new ChatMessage("system", getSystemPrompt()));
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    String displayRole;
    if ("user".equals(msg.getRole())) {
      displayRole = "You";
    } else if ("assistant".equals(msg.getRole())) {
      displayRole = capitalize(profession);
    } else {
      displayRole = msg.getRole();
    }
    txtaChat.appendText(displayRole + ": " + msg.getContent() + "\n\n");
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
    txtaChat.clear();
  }

  public void sendMessage() throws ApiProxyException, IOException {
    onSendMessage(null);
  }

  public void setInputFocus() {
    txtInput.requestFocus();
  }
}
