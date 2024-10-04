package nz.ac.auckland.se206.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class TimerManager {
  private static TimerManager instance;

  public static TimerManager getInstance() {
    if (instance == null) {
      instance = new TimerManager();
    }
    return instance;
  }

  private Timeline timeline;
  private int time;
  private int guessTime = 60; // 1 min to guess

  private Runnable tickListener;
  private Runnable guessingStartListener;
  private Runnable guessTimeEndListener; // Add this line

  private boolean isGuessTime = false;
  private boolean canGuess = true;

  private TimerManager() {
    this.time = 300; // initial time of 5 minutes
  }

  // Method to configure the timer
  public void configureTimer() {
    // Stop the timer if it is running
    if (timeline != null) {
      timeline.stop();
    }
    // Create a new timeline
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  updateTimer();
                  if (tickListener != null) {
                    tickListener.run();
                  }
                }));
    // Set the cycle count to indefinite
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

  // Method to update the timer
  private void updateTimer() {
    time--;
    if (time < 0) {
      if (!isGuessTime) {
        switchToGuessTime();
      } else {
        onFinishGuessTimer();
      }
    }
  }

  // Method to switch to guess time
  private void switchToGuessTime() {
    time = guessTime;
    isGuessTime = true;
    configureTimer();
    timeline.playFromStart();
    // Notify the guessing start listener
    if (guessingStartListener != null) {
      guessingStartListener.run();
    }
  }

  // Method to handle the end of the guess time
  private void onFinishGuessTimer() {
    timeline.stop();
    if (guessTimeEndListener != null) {
      guessTimeEndListener.run();
    }
  }

  // Method to start the timer
  public void startTimer() {
    configureTimer();
    timeline.playFromStart();
  }

  // Method to stop the timer
  public void stopTimer() {
    if (timeline != null) {
      timeline.stop();
    }
  }

  public int getTime() {
    return time;
  }

  public boolean isGuessTime() {
    return isGuessTime;
  }

  public boolean isTimerRunning() {
    return timeline != null && timeline.getStatus() == Timeline.Status.RUNNING;
  }

  public void setTickListener(Runnable listener) {
    this.tickListener = listener;
  }

  public void setGuessingStartListener(Runnable guessingStartListener) {
    this.guessingStartListener = guessingStartListener;
  }

  public void setGuessTimeEndListener(Runnable guessTimeEndListener) {
    this.guessTimeEndListener = guessTimeEndListener;
  }

  public void setCanGuess(boolean canGuess) {
    this.canGuess = canGuess;
  }

  public boolean isCanGuess() {
    return canGuess;
  }

  public void resetTimer() {
    stopTimer();
    this.time = 300; // 5 min timer
    this.isGuessTime = false;
    this.canGuess = true;
    configureTimer(); // Reconfigure the timer
  }

  public void startGuessingTimer() {
    System.out.println("Starting guessing timer");
    if (!isGuessTime) { // Check if the guessing time hasn't already started
      switchToGuessTime(); // Use the existing method that configures and starts the guessing timer
    }
  }
}
