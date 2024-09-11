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
  private int guessTime = 10;

  private Runnable tickListener;
  private Runnable guessingStartListener;

  private boolean isGuessTime = false;
  private boolean canGuess = true;

  private TimerManager() {
    this.time = 120; // initial time of 2 minutes
  }

  public void configureTimer() {
    if (timeline != null) {
      timeline.stop();
    }
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
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

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

  private void switchToGuessTime() {
    time = guessTime;
    isGuessTime = true;
    configureTimer();
    timeline.playFromStart();
    if (guessingStartListener != null) {
      guessingStartListener.run();
    }
  }

  private void onFinishGuessTimer() {
    timeline.stop();
    // Implement what happens when the guess timer finishes
  }

  public void startTimer() {
    configureTimer();
    timeline.playFromStart();
  }

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

  public void setCanGuess(boolean canGuess) {
    this.canGuess = canGuess;
  }

  public boolean isCanGuess() {
    return canGuess;
  }
}
