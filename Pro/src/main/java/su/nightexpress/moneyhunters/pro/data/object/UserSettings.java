package su.nightexpress.moneyhunters.pro.data.object;

public class UserSettings {

    private boolean soundPickupEnabled;

    public UserSettings() {
        this(true);
    }

    public UserSettings(boolean soundPickupEnabled) {
        this.setSoundPickupEnabled(soundPickupEnabled);
    }

    public boolean isSoundPickupEnabled() {
        return soundPickupEnabled;
    }

    public void setSoundPickupEnabled(boolean soundPickupEnabled) {
        this.soundPickupEnabled = soundPickupEnabled;
    }
}
