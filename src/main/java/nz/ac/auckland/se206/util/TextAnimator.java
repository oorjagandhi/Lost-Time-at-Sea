package nz.ac.auckland.se206.util;

import java.util.Random;

/**
 * The TextAnimator class is responsible for animating text in the game. This includes writing text
 * to the text field character by character with a delay between each character.
 */
public class TextAnimator implements Runnable {

  private String text;
  private int animationTime;
  private TextOutput textOutput;
  private Random random = new Random();

  /**
   * Constructs a new TextAnimator with the given text, animation time, and text field.
   *
   * @param text the text to animate
   * @param animationTime the time to wait between each character
   * @param textField the text field to write the text to
   */
  public TextAnimator(String text, int animationTime, TextOutput textField) {
    this.text = text;
    this.animationTime = animationTime;
    this.textOutput = textField;
  }

  @Override
  // This method is called when the thread is started
  public void run() {

    try {
      // Loop through the text and write it to the text field
      for (int i = 0; i <= text.length(); i++) {
        String textAtThisPoint = text.substring(0, i);

        textOutput.writeText(textAtThisPoint);
        Thread.sleep(animationTime + random.nextInt(150));
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
