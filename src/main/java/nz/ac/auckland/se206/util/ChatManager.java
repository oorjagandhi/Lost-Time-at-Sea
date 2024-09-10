package nz.ac.auckland.se206.util;

import java.util.HashSet;
import java.util.Set;

public class ChatManager {

  private static ChatManager instance;

  public static ChatManager getInstance() {
    if (instance == null) {
      instance = new ChatManager();
    }
    return instance;
  }

  private final Set<String> professionsWelcomed = new HashSet<>();

  private ChatManager() {}

  public void addProfession(String profession) {
    professionsWelcomed.add(profession);
  }

  public boolean hasProfession(String profession) {
    return professionsWelcomed.contains(profession);
  }
}
