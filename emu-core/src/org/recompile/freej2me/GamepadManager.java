package org.recompile.freej2me;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component;
import net.java.games.input.Rumbler; // Added for potential future use

import java.util.ArrayList;
import java.util.List;

public class GamepadManager {

    private Controller[] controllers;
    private Controller selectedGamepad;
    private ArrayList<Controller> gamepads;

    public GamepadManager() {
        // Placeholder for JInput initialization
        initializeJInput();
    }

    private void initializeJInput() {
        try {
            // For older JInput versions, this might be needed:
            // ControllerEnvironment env = ControllerEnvironment.createDefaultEnvironment();

            // For JInput 2.0.X, direct enumeration is typical
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            controllers = env.getControllers();
            gamepads = new ArrayList<>();
            for (Controller controller : controllers) {
                if (controller.getType() == Controller.Type.GAMEPAD ||
                    controller.getType() == Controller.Type.STICK ||
                    controller.getType() == Controller.Type.JOYSTICK) { // JOYSTICK for wider compatibility
                    gamepads.add(controller);
                }
            }
            if (!gamepads.isEmpty()) {
                selectedGamepad = gamepads.get(0); // Default to the first detected gamepad
            }
        } catch (Exception e) {
            // Use Mobile.log if available and appropriate, otherwise System.err
            // Mobile.log(Mobile.LOG_ERROR, "GamepadManager: Error initializing JInput: " + e.getMessage());
            System.err.println("GamepadManager: Error initializing JInput: " + e.getMessage());
            e.printStackTrace();
            controllers = new Controller[0]; // Ensure controllers is not null
            gamepads = new ArrayList<>(); // Ensure gamepads is not null
        }
    }

    public List<Controller> getDetectedGamepads() {
        return gamepads;
    }

    public String[] getGamepadNames() {
        if (gamepads == null) {
            return new String[0];
        }
        String[] names = new String[gamepads.size()];
        for (int i = 0; i < gamepads.size(); i++) {
            names[i] = gamepads.get(i).getName();
        }
        return names;
    }

    public void selectGamepad(int index) {
        if (gamepads != null && index >= 0 && index < gamepads.size()) {
            selectedGamepad = gamepads.get(index);
            // Mobile.log(Mobile.LOG_INFO, "GamepadManager: Selected gamepad: " + selectedGamepad.getName());
            System.out.println("GamepadManager: Selected gamepad: " + selectedGamepad.getName());
        } else {
            // Mobile.log(Mobile.LOG_WARNING, "GamepadManager: Invalid gamepad index: " + index);
            System.err.println("GamepadManager: Invalid gamepad index: " + index);
        }
    }

    public Controller getSelectedGamepad() {
        return selectedGamepad;
    }

    public void pollSelectedGamepad() {
        if (selectedGamepad == null) {
            return;
        }

        // Poll the controller for updates
        selectedGamepad.poll();

        // Example: Get all components and their values
        // Component[] components = selectedGamepad.getComponents();
        // for (Component component : components) {
        //     if (component.getPollData() != 0.0f) { // Or some other threshold for analog
        //         System.out.println("Component: " + component.getName() +
        //                            ", ID: " + component.getIdentifier() +
        //                            ", Value: " + component.getPollData());
        //     }
        // }
    }

    // Placeholder for a method to get specific button/axis states
    // This will be expanded later to map to AWT KeyEvents or J2ME actions
    public float getComponentValue(Component.Identifier identifier) {
        if (selectedGamepad == null) {
            return 0.0f;
        }
        selectedGamepad.poll(); // Ensure data is current
        Component component = selectedGamepad.getComponent(identifier);
        if (component != null) {
            return component.getPollData();
        }
        return 0.0f;
    }

    // Example for rumbler, if the gamepad supports it
    public void rumble(float intensity) {
        if (selectedGamepad == null) return;
        Rumbler[] rumblers = selectedGamepad.getRumblers();
        if (rumblers.length > 0) {
            for (Rumbler rumbler : rumblers) {
                rumbler.rumble(intensity);
            }
        }
    }
}
