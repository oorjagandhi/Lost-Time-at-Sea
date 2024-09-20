package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;

public class GuessingController {

  private String selectedSuspect;

  @FXML private Label whyLabel;

  @FXML private TextArea explanationTextArea;

  @FXML private VBox explanationBox;

  @FXML private ImageView suspectMaid;

  @FXML private ImageView suspectBartender;

  @FXML private ImageView suspectSailor;

  @FXML private Button submitGuessButton;

  @FXML
  private void initialize() throws IOException {
    // Set up hover effect for suspects
    setupHoverEffect(suspectMaid);
    setupHoverEffect(suspectBartender);
    setupHoverEffect(suspectSailor);

    bindSubmitButtonVisibility();
  }

  // Bind the visibility of the submit button to the explanation text area
  private void bindSubmitButtonVisibility() {
    explanationTextArea
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              updateSubmitButtonVisibility();
            });
  }

  // Update the visibility of the submit button based on the explanation text area
  private void updateSubmitButtonVisibility() {
    boolean isInputValid =
        selectedSuspect != null && !explanationTextArea.getText().trim().isEmpty();
    submitGuessButton.setVisible(isInputValid);
    submitGuessButton.setManaged(isInputValid);
  }

  private void setupHoverEffect(ImageView suspect) {
    // Apply hover effect
    suspect.setOnMouseEntered(
        event -> {
          // Only apply the yellow shadow if it's not the selected suspect
          if (!suspect.getId().equals(selectedSuspect)) {
            suspect.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
          }
        });

    // Remove hover effect when not hovering
    suspect.setOnMouseExited(
        event -> {
          // Only remove the hover effect if it's not the selected suspect
          if (!suspect.getId().equals(selectedSuspect)) {
            suspect.setStyle("");
          }
        });
  }

  @FXML
  private void handleSuspectClick(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();

    // Clear the red outline from the previously selected suspect, if any
    if (selectedSuspect != null) {
      // Find the previously selected suspect by its ID and remove the red outline
      Node previouslySelected = explanationBox.getScene().lookup("#" + selectedSuspect);
      if (previouslySelected != null) {
        previouslySelected.setStyle(""); // Remove the red outline
      }
    }

    // Set the red outline for the newly selected suspect
    source.setStyle("-fx-effect: dropshadow(gaussian, red, 20, 0.8, 0, 0);");
    selectedSuspect = source.getId(); // Update the selected suspect ID

    // Update other UI elements based on the selected suspect
    updateWhyLabel();
    fadeInExplanationBox();
    updateSubmitButtonVisibility();
  }

  private void updateWhyLabel() {
    if (selectedSuspect != null) {
      // Set the label text based on the selected suspect
      String suspectName = selectedSuspect.substring(7); // Assuming ID starts with 'suspect'
      whyLabel.setText("Why is " + suspectName + " the thief?");
    }
  }

  private void fadeInExplanationBox() {
    explanationBox.setVisible(true);
    explanationBox.setManaged(true);

    // Create a fade transition
    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), explanationBox);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);
    fadeIn.play();
  }

  @FXML
  private void submitGuess() {
    if (selectedSuspect != null && !explanationTextArea.getText().trim().isEmpty()) {
      try {

        // Call handleRectangleClick to make a guess
        GameStateContext context = GameStateContext.getInstance();
        context.handleRectangleClick(null, selectedSuspect);

        // Get the user's explanation
        String userExplanation = explanationTextArea.getText().trim();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feedback.fxml"));
        Parent root = loader.load();
        FeedbackController feedbackController = loader.getController();
        // Evaluate the explanation using OpenAI
        String responseContent = evaluateExplanation(selectedSuspect, userExplanation);
        Platform.runLater(
            () -> {
              feedbackController.updateResponseText(responseContent);
              feedbackController.updateStatus(context.isWon());
            });
        // Load the game over screen for losing
        Scene scene = new Scene(root);
        App.getStage().setScene(scene);
        App.getStage().show();

      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // Show an error message if no suspect is selected or explanation is missing
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("Incomplete Submission");
      alert.setHeaderText(null);
      alert.setContentText("Please select a suspect and provide an explanation.");
      alert.showAndWait();
    }
  }

  // Evaluate the player's explanation using OpenAI's chat completion API
  private String evaluateExplanation(String selectedSuspect, String userExplanation) {
    System.out.println("Selected Suspect: " + selectedSuspect);
    // Prepare the prompt
    String prompt =
        "You are an AI assistant in a mystery game where the player must identify the thief who"
            + " stole the captain's watch on a ship. Your task is to analyze the player's"
            + " explanation of who they believe the thief is and why. Based on their explanation,"
            + " provide feedback according to the following guidelines.\n"
            + "Game Context:\n"
            + "Thief: The bartender is the actual thief.\n"
            + "Suspects: The bartender, the first mate, and the maid.\n"
            + "Clues:\n"
            + "Crumpled paper of speech (Clue 1):\n"
            + "Found in the crime scene.\n"
            + "Contains a speech draft by the first mate.\n"
            + "Indicates the first mate wants to replace the captain.\n"
            + "Radio Recording (Clue 2):\n"
            + "Features a female voice talking to her mother about needing money for her sick"
            + " younger brother.\n"
            + "Suggests a motive related to financial need.\n"
            + "Voice could belong to the bartender or the maid.\n"
            + "Floor board (Clue 3):\n"
            + "A loose floorboard in the captain's cabin.\n"
            + "Contains a missing earring and cleaning supplies.\n"
            + "The bartender mentions she lost an earring.\n"
            + "Links the bartender to the crime scene.\n"
            + "Cleaning Supplies (Clue 4):\n"
            + "Also found under the loose floorboard in the captain's cabin.\n"
            + "The maid mentions that some of her cleaning supplies are missing.\n"
            + "This is a red herring intended to mislead the player into suspecting the maid.\n"
            + "The bartender took the cleaning supplies to clean up after the robbery and"
            + " inadvertently left them under the floorboard.\n"
            + "Additional Details:\n"
            + "Bartender's Motive:\n"
            + "Needs money for her sick younger brother's medical treatment.\n"
            + "Admits her brother is ill when asked.\n"
            + "May have taken the maid's cleaning supplies to cover her tracks after stealing the"
            + " watch.\n"
            + "First Mate's Motive:\n"
            + "Dislikes the captain and believes she could lead better.\n"
            + "Plans to overthrow the captain.\n"
            + "Reveals her dissatisfaction when questioned.\n"
            + "Maid's Motive:\n"
            + "Feels mistreated by other crewmates who treat her poorly.\n"
            + "Wants to leave the ship due to the mistreatment.\n"
            + "Mentions that her cleaning supplies are missing, suggesting someone may have taken"
            + " them.\n"
            + "The presence of her cleaning supplies under the floorboard serves as a red"
            + " herring.\n"
            + "Your Task:\n"
            + "Analyze the Player's Explanation:\n"
            + "Determine if the player correctly identified the thief (the bartender).\n"
            + "Assess whether their explanation correctly references the relevant clues.\n"
            + "Provide Feedback Based on Three Possible Outcomes:\n"
            + "Outcome 1: Incorrect Guess or No Guess\n"
            + "Inform the player that their guess is incorrect.\n"
            + "Tell them to try again, and to chat to suspects more and take a closer look at the"
            + " clues.\n"
            + "Do not mention the actual thief, and do not mention specific clues.\n"
            + "Outcome 2: Correct Guess with Incorrect/Incomplete Explanation\n"
            + "Acknowledge that they guessed the correct thief.\n"
            + "Point out that their explanation is missing key details or contains inaccuracies.\n"
            + "Encourage them by mentioning the relevant clues they should consider.\n"
            + "Outcome 3: Correct Guess with Correct Explanation\n"
            + "Praise the player for correctly identifying the thief.\n"
            + "Confirm that their reasoning is accurate and well-supported by the clues.\n"
            + "Encourage them to continue their good investigative work.\n"
            + "Formatting and Tone:\n"
            + "Address the player directly in a friendly and encouraging manner.\n"
            + "Keep the feedback concise and focused on the player's explanation.\n"
            + "Do not reveal unnecessary game details or additional spoilers.\n"
            + "Instructions for Generating the Response:\n"
            + "Step 1: Identify which outcome applies based on the player's explanation.\n"
            + "Step 2: Craft a response according to the guidelines for that outcome.\n"
            + "Step 3: Use the following structure in your response:\n"
            + "For Outcome 1:\n"
            + "You guessed incorrect.\n"
            + "Encourage them to try again.\n"
            + "For Outcome 2:\n"
            + "Acknowledge the correct guess.\n"
            + "Point out inaccuracies or missing details in their explanation.\n"
            + "Correct their explanation by mentioning the relevant clues.\n"
            + "Avoid saying the motive explicitly.\n"
            + "For Outcome 3:\n"
            + "Congratulate the player.\n"
            + "Affirm the correctness of their explanation.\n"
            + "Encourage them to keep up the good work.\n"
            + "The player guessed that the thief is the "
            + selectedSuspect
            + "Their explanation is: \""
            + userExplanation
            + "\"";

    // Call OpenAI API to evaluate the explanation
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      ChatCompletionRequest chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.1)
              .setTopP(0.3)
              .setMaxTokens(150);

      // Add the prompt and user explanation to the request
      ChatMessage systemMessage = new ChatMessage("system", prompt);
      chatCompletionRequest.addMessage(systemMessage);

      // Execute the request
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      ChatMessage responseMessage = result.getChatMessage();

      // Process the response
      String responseContent = responseMessage.getContent();

      // Print the response
      System.out.println("Response: " + responseContent);
      return responseContent;

    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
    return prompt;
  }
}
