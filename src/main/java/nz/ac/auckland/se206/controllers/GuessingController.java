package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
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

  @FXML private ImageView suspectMaid;

  @FXML private ImageView suspectBartender;

  @FXML private ImageView suspectSailor;

  public void setContext(GameStateContext context) {
    this.context = context;
  }

  @FXML
  private void initialize() {
    // Set up hover effect for suspects
    setupHoverEffect(suspectMaid);
    setupHoverEffect(suspectBartender);
    setupHoverEffect(suspectSailor);
  }

  private void setupHoverEffect(ImageView suspect) {
    // Apply hover effect
    suspect.setOnMouseEntered(
        event -> {
          suspect.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
        });

    // Remove hover effect when not hovering
    suspect.setOnMouseExited(
        event -> {
          // Only remove hover effect if it's not the selected suspect
          if (!suspect.getId().equals(selectedSuspect)) {
            suspect.setStyle("");
          }
        });
  }

  @FXML
  private void handleSuspectClick(MouseEvent event) {
    ImageView source = (ImageView) event.getSource();
    // Remove the red glow from previously selected suspect if any
    if (selectedSuspect != null) {
      Node previouslySelected = suspectMaid.getScene().lookup("#" + selectedSuspect);
      if (previouslySelected != null) {
        previouslySelected.setStyle("");
      }
    }
    // Apply red glow to the selected suspect
    source.setStyle("-fx-effect: dropshadow(gaussian, red, 20, 0.8, 0, 0);");
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
