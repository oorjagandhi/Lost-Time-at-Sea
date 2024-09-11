package nz.ac.auckland.se206.util;

public class RoomManager {
  private static RoomManager instance;

  public static RoomManager getInstance() {
    if (instance == null) {
      instance = new RoomManager();
    }
    return instance;
  }

  private boolean userWelcomed;

  private RoomManager() {}

  public boolean isUserWelcomed() {
    return userWelcomed;
  }

  public void setUserWelcomed(boolean userWelcomed) {
    this.userWelcomed = userWelcomed;
  }
}
