package nz.ac.auckland.se206.util;

/**
 * The RoomManager class is responsible for managing the state of the room. This includes keeping
 * track of whether the user has been welcomed to the room.
 */
public class RoomManager {
  private static RoomManager instance;

  /**
   * Returns the singleton instance of the RoomManager.
   *
   * @return the RoomManager instance
   */
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
