package com.radium.client.gui.settings;
// radium client

/**
 * A button setting that executes an action when clicked.
 * The value is always false and clicking triggers the action.
 */
public class ButtonSetting extends Setting<Boolean> {
    private final Runnable action;

    public ButtonSetting(String name, Runnable action) {
        super(name, false);
        this.action = action;
    }

    /**
     * Execute the button action
     */
    public void click() {
        if (action != null) {
            action.run();
        }
    }

    public Runnable getAction() {
        return action;
    }
}
