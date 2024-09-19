// FeedbackController.java
package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class FeedbackController {

  @FXML private Text responseText;

  // Method to update the text
  public void updateResponseText(String text) {
    System.out.println("Updating response text to: " + text);
    responseText.setText(text);
  }
}
