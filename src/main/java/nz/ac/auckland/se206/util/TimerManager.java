package nz.ac.auckland.se206.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * The TimerManager class is responsible for managing the countdown timer in the game. This includes
 * two phases: the initial timer phase and the guessing phase.
 */
public class TimerManager {

  // Singleton instance of TimerManager
  private static TimerManager instance;

  /**
   * Retrieves the singleton instance of TimerManager.
   *
   * @return The single instance of TimerManager.
   */
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
  private Runnable guessTimeEndListener; // Listener for when guess time ends

  private boolean isGuessTime = false;
  private boolean canGuess = true;

  /** Private constructor to initialize the timer to 5 minutes. */
  private TimerManager() {
    this.time = 291; // initial time of 5 minutes
  }

  /**
   * Configures the timer by creating a new timeline. The timeline is set to decrement the time
   * every second and notify any tick listeners.
   */
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

  /**
   * Updates the timer by decrementing the current time. If the time reaches zero, the timer either
   * switches to the guess time phase or ends the guessing time.
   */
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

  /**
   * Switches the timer to the guessing phase. The timer is reset to the guessing time, and the
   * guessing phase begins. The guessingStartListener is triggered to notify any listeners.
   */
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

  /**
   * Handles the end of the guessing phase by stopping the timeline. The guessTimeEndListener is
   * triggered to notify that the guessing time is over.
   */
  private void onFinishGuessTimer() {
    timeline.stop();
    if (guessTimeEndListener != null) {
      guessTimeEndListener.run();
    }
  }

  /** Starts the timer and begins counting down from the initial time. */
  public void startTimer() {
    configureTimer();
    timeline.playFromStart();
  }

  /** Stops the timer if it is running. */
  public void stopTimer() {
    if (timeline != null) {
      timeline.stop();
    }
  }

  /**
   * Returns the current time left on the timer.
   *
   * @return The remaining time in seconds.
   */
  public int getTime() {
    return time;
  }

  /**
   * Checks whether the timer is in the guessing phase.
   *
   * @return True if the timer is in the guessing phase, false otherwise.
   */
  public boolean isGuessTime() {
    return isGuessTime;
  }

  /**
   * Checks whether the timer is currently running.
   *
   * @return True if the timer is running, false if it is stopped.
   */
  public boolean isTimerRunning() {
    return timeline != null && timeline.getStatus() == Timeline.Status.RUNNING;
  }

  /**
   * Sets a listener to be notified on each tick (every second).
   *
   * @param listener The Runnable to be executed on each tick.
   */
  public void setTickListener(Runnable listener) {
    this.tickListener = listener;
  }

  /**
   * Sets a listener to be notified when the guessing phase starts.
   *
   * @param guessingStartListener The Runnable to be executed when guessing starts.
   */
  public void setGuessingStartListener(Runnable guessingStartListener) {
    this.guessingStartListener = guessingStartListener;
  }

  /**
   * Sets a listener to be notified when the guessing phase ends.
   *
   * @param guessTimeEndListener The Runnable to be executed when guessing ends.
   */
  public void setGuessTimeEndListener(Runnable guessTimeEndListener) {
    this.guessTimeEndListener = guessTimeEndListener;
  }

  /**
   * Sets whether the player is allowed to make a guess.
   *
   * @param canGuess True if the player can guess, false otherwise.
   */
  public void setCanGuess(boolean canGuess) {
    this.canGuess = canGuess;
  }

  /**
   * Checks whether the player can make a guess.
   *
   * @return True if the player can guess, false otherwise.
   */
  public boolean isCanGuess() {
    return canGuess;
  }

  /**
   * Resets the timer to the initial 5-minute duration and resets the guessing phase state. The
   * timer is stopped after resetting.
   */
  public void resetTimer() {
    stopTimer();
    this.time = 300; // 5 min timer
    this.isGuessTime = false;
    this.canGuess = true;
    configureTimer(); // Reconfigure the timer
  }

  /**
   * Starts the guessing timer, switching to the guessing phase if it has not already started. The
   * guessing phase is configured and begins immediately.
   */
  public void startGuessingTimer() {
    System.out.println("Starting guessing timer");
    if (!isGuessTime) { // Check if the guessing time hasn't already started
      switchToGuessTime(); // Use the existing method that configures and starts the guessing timer
    }
  }
}
