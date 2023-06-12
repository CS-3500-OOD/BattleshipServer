package server;


import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class ServerProperties {

  public static Preferences getPreferences() {
    Preferences preferences = Preferences.userRoot().node(ServerProperties.class.getName());

    if (!preferencesLoaded(preferences)) {
      Server.logger.error("Server preferences not found, loading default settings.");
      setDefaults(preferences);
    }

    return preferences;
  }


  private static boolean preferencesLoaded(Preferences preferences) {
    try {
      if (preferences.keys().length > 0) {
        return true;
      }
    } catch (BackingStoreException ignored) {

    }
    return false;
  }


  public static void setDefaults(Preferences preferences) {
    preferences.put("server_name", "SERVER_AGENT");
    preferences.putInt("default_port", 35001);
    preferences.putBoolean("server_debug", false);
    preferences.putBoolean("game_specific_debug", false);
    preferences.putBoolean("socket_communication_debug", false);
    preferences.putInt("client_game_max_response_seconds", 2);
    preferences.putInt("max_games_in_parallel", 2);
    preferences.putInt("max_signup_response_seconds", 2);
    preferences.putInt("max_player_signup_in_parallel", 4);
    syncPreferences(preferences);
  }

  public static void printPreferences(Preferences preferences) {
    try {
      StringBuilder prefs = new StringBuilder("Preferences: \n");
      for (String key : preferences.keys()) {
        prefs.append("   " + key + ": " + preferences.get(key, "UNK") + "\n");
      }
      Server.logger.info(prefs);
    } catch (BackingStoreException ignored) {
    }
  }

  public static void syncPreferences(Preferences preferences) {
    try {
      preferences.sync();
    } catch (BackingStoreException ignored) {

    }
  }
}
