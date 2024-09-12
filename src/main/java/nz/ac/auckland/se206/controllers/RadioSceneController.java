package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class RadioSceneController {
  boolean Playingaudio = false;

  /**
   * the play button is clicked if the audio is playing, pause it if the audio is paused, play it
   *
   * @param event
   */
  @FXML
  private void onPlay(ActionEvent event) {
    System.out.println("Play button clicked");
  }

  /**
   * the decrease frequency button is clicked change to the left audio
   *
   * @param event
   */
  @FXML
  private void onDecreaseFrequency(ActionEvent event) {
    System.out.println("Decrease Frequency button clicked");
  }

  /**
   * the increase frequency button is clicked change to the right audio
   *
   * @param event
   */
  @FXML
  private void onIncreaseFrequency(ActionEvent event) {
    System.out.println("Increase Frequency button clicked");
  }
}
