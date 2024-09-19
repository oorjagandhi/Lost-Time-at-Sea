package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.se206.GameStateContext;

public class GuessingController {

  private GameStateContext context;
  private String selectedSuspect;

  @FXML private Label whyLabel;

  @FXML private TextArea explanationTextArea;

  @FXML private VBox explanationBox;

  public void setContext(GameStateContext context) {
    this.context = context;
  }

  @FXML
  private void handleSuspectClick(MouseEvent event) {
    Node source = (Node) event.getSource();
    selectedSuspect = source.getId(); // Get the ID of the clicked suspect
    updateWhyLabel(); // Update the label with the selected suspect's name
    fadeInExplanationBox(); // Show the explanation text box with fade-in effect
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
  public void handleSubmitGuess() {
    if (selectedSuspect != null && !explanationTextArea.getText().trim().isEmpty()) {
      try {
        // Call handleRectangleClick to make a guess
        context.handleRectangleClick(null, selectedSuspect);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // Show some error message or handle the case where no suspect is selected or explanation is
      // missing
      System.out.println("Please select a suspect and provide an explanation.");
    }
  }
}
