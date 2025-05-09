package sk.mpar.trafficsim;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import sk.mpar.trafficsim.model.PersonalCar;
import sk.mpar.trafficsim.model.Road;
import sk.mpar.trafficsim.model.Simulation;
import sk.mpar.trafficsim.model.Truck;
import sk.mpar.trafficsim.model.Vehicle;

/**
 * Controller for the traffic simulator UI.
 */
public class TrafficSimulatorController {
    // UI components
    @FXML
    private Pane simulationPane;

    @FXML
    private Button startStopButton;

    @FXML
    private Button addCarButton;

    @FXML
    private Button addTruckButton;

    @FXML
    private Button removeVehicleButton;

    @FXML
    private Slider accelerationSlider;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label vehicleCountLabel;

    @FXML
    private Spinner<Integer> carCountSpinner;

    @FXML
    private Spinner<Integer> truckCountSpinner;

    @FXML
    private ColorPicker carColorPicker;

    @FXML
    private ColorPicker truckColorPicker;

    @FXML
    private Button colorAllCarsButton;

    @FXML
    private Button colorAllTrucksButton;

    @FXML
    private Button setCarDimensionsButton;

    @FXML
    private Button setTruckDimensionsButton;

    // Simulation components
    private Road road;
    private Simulation simulation;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Create initial road and simulation with default values
        final double initialCenterX = 400; // Default width / 2
        final double initialCenterY = 300; // Default height / 2
        final double initialInnerRadius = 150;
        final double laneWidth = 50;
        final double obstacleAngle = Math.PI / 2; // Obstacle at the top

        road = new Road(initialCenterX, initialCenterY, initialInnerRadius, laneWidth, obstacleAngle);

        // Create the simulation
        simulation = new Simulation(road, simulationPane);

        // We need to wait until the pane is laid out to get its actual size
        // and update the road and simulation if necessary
        simulationPane.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            // Only update if the size has changed significantly
            if (Math.abs(newValue.getWidth() - 2 * initialCenterX) > 50 || Math.abs(newValue.getHeight() - 2 * initialCenterY) > 50) {
                updateRoadAndSimulation(newValue.getWidth(), newValue.getHeight(), laneWidth, obstacleAngle);
            }
        });

        // Initialize UI components
        startStopButton.setText("Start");
        vehicleCountLabel.setText("Vehicles: 0");

        // Set up the acceleration slider
        accelerationSlider.setMin(0.0);
        accelerationSlider.setMax(50.0);
        accelerationSlider.setValue(1.0);
        accelerationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setDefaultAcceleration(newValue.doubleValue());
        });

        // Set up the speed slider
        speedSlider.setMin(0.0);
        speedSlider.setMax(50.0);
        speedSlider.setValue(1.0);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setMaxVelocity(newValue.doubleValue());
        });

        // Set up the car count spinner
        carCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Set up the truck count spinner
        truckCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Set up the car color picker
        carColorPicker.setValue(PersonalCar.DEFAULT_COLOR);

        // Set up the truck color picker
        truckColorPicker.setValue(Truck.DEFAULT_COLOR);

        // Set up mouse click handling for the simulation pane
        simulationPane.setOnMouseClicked(this::handleMouseClick);

        // We need to wait until the scene is set before adding the key event filter
        simulationPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Set up key event handling for the entire scene
                newValue.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });
    }

    /**
     * Handles key press events.
     * 
     * @param event The key event
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            toggleSimulation();
        } else if (event.getCode() == KeyCode.C) {
            addCar();
        } else if (event.getCode() == KeyCode.T) {
            addTruck();
        } else if (event.getCode() == KeyCode.R) {
            removeVehicle();
        } else if (event.getCode() == KeyCode.UP) {
            // Increase speed
            double currentSpeed = speedSlider.getValue();
            speedSlider.setValue(Math.min(currentSpeed + 0.5, speedSlider.getMax()));
        } else if (event.getCode() == KeyCode.DOWN) {
            // Decrease speed
            double currentSpeed = speedSlider.getValue();
            speedSlider.setValue(Math.max(currentSpeed - 0.5, speedSlider.getMin()));
        }
    }

    /**
     * Handles mouse click events on the simulation pane.
     * 
     * @param event The mouse event
     */
    private void handleMouseClick(MouseEvent event) {
        // Get the click coordinates
        double x = event.getX();
        double y = event.getY();

        // Check if the click is on a vehicle
        Vehicle clickedVehicle = simulation.getVehicleAt(x, y);

        if (clickedVehicle != null) {
            // If right-click on a vehicle, remove it
            if (event.isSecondaryButtonDown()) {
                simulation.removeVehicle(clickedVehicle);
                updateVehicleCount();
            } else {
                // If left-click on a vehicle, show properties dialog
                showVehiclePropertiesDialog(clickedVehicle);
            }
        } else if (road.isOnRoad(x, y)) {
            // If click is on the road but not on a vehicle
            // If right-click, do nothing
            if (!event.isSecondaryButtonDown()) {
                // If left-click, add a vehicle at the click position
                // If shift is pressed, add a truck, otherwise add a car
                if (event.isShiftDown()) {
                    // Add a truck with the selected color at the click position
                    Color color = truckColorPicker.getValue();
                    Truck truck = simulation.addTruck(color, x, y);
                    if (truck != null) {
                        updateVehicleCount();
                    }
                } else {
                    // Add a car with the selected color at the click position
                    Color color = carColorPicker.getValue();
                    PersonalCar car = simulation.addPersonalCar(color, x, y);
                    if (car != null) {
                        updateVehicleCount();
                    }
                }
            }
        }
    }

    /**
     * Toggles the simulation between running and stopped.
     */
    @FXML
    private void toggleSimulation() {
        if (simulation.isRunning()) {
            simulation.stop();
            startStopButton.setText("Start");
        } else {
            simulation.start();
            startStopButton.setText("Stop");
        }
    }

    /**
     * Adds personal cars to the simulation.
     */
    @FXML
    private void addCar() {
        int count = carCountSpinner.getValue();
        Color color = carColorPicker.getValue();
        simulation.addPersonalCars(count, color);
        updateVehicleCount();
    }

    /**
     * Adds trucks to the simulation.
     */
    @FXML
    private void addTruck() {
        int count = truckCountSpinner.getValue();
        Color color = truckColorPicker.getValue();
        simulation.addTrucks(count, color);
        updateVehicleCount();
    }

    /**
     * Changes the color of all personal cars in the simulation.
     */
    @FXML
    private void colorAllCars() {
        Color color = carColorPicker.getValue();
        for (PersonalCar car : simulation.getVehicles(PersonalCar.class)) {
            car.setColor(color);
        }
    }

    /**
     * Changes the color of all trucks in the simulation.
     */
    @FXML
    private void colorAllTrucks() {
        Color color = truckColorPicker.getValue();
        for (Truck truck : simulation.getVehicles(Truck.class)) {
            truck.setColor(color);
        }
    }

    /**
     * Shows a dialog to set dimensions for all personal cars.
     */
    @FXML
    private void setAllCarDimensions() {
        // Create the dialog
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Set Car Dimensions");
        dialog.setHeaderText("Set dimensions for all personal cars");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the grid pane for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create the form fields
        Spinner<Double> widthSpinner = new Spinner<>();
        widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(30, 100, PersonalCar.WIDTH, 1));
        widthSpinner.setEditable(true);

        Spinner<Double> heightSpinner = new Spinner<>();
        heightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(15, 50, PersonalCar.HEIGHT, 1));
        heightSpinner.setEditable(true);

        // Add the form fields to the grid
        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthSpinner, 1, 0);

        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(widthSpinner.getValue(), heightSpinner.getValue());
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(dimensions -> {
            double width = dimensions.getKey();
            double height = dimensions.getValue();

            // Update all personal cars
            for (PersonalCar car : simulation.getVehicles(PersonalCar.class)) {
                car.getShape().setWidth(width);
                car.getShape().setHeight(height);
                car.getShape().setX(car.getX() - width / 2);
                car.getShape().setY(car.getY() - height / 2);
            }
        });
    }

    /**
     * Shows a dialog to set dimensions for all trucks.
     */
    @FXML
    private void setAllTruckDimensions() {
        // Create the dialog
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Set Truck Dimensions");
        dialog.setHeaderText("Set dimensions for all trucks");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the grid pane for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create the form fields
        Spinner<Double> widthSpinner = new Spinner<>();
        widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(60, 200, Truck.WIDTH, 1));
        widthSpinner.setEditable(true);

        Spinner<Double> heightSpinner = new Spinner<>();
        heightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(20, 80, Truck.HEIGHT, 1));
        heightSpinner.setEditable(true);

        // Add the form fields to the grid
        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthSpinner, 1, 0);

        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(widthSpinner.getValue(), heightSpinner.getValue());
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(dimensions -> {
            double width = dimensions.getKey();
            double height = dimensions.getValue();

            // Update all trucks
            for (Truck truck : simulation.getVehicles(Truck.class)) {
                truck.getShape().setWidth(width);
                truck.getShape().setHeight(height);
                truck.getShape().setX(truck.getX() - width / 2);
                truck.getShape().setY(truck.getY() - height / 2);
            }
        });
    }

    /**
     * Removes a random vehicle from the simulation.
     */
    @FXML
    private void removeVehicle() {
        if (simulation.removeRandomVehicle()) {
            updateVehicleCount();
        }
    }

    /**
     * Updates the vehicle count label.
     */
    private void updateVehicleCount() {
        vehicleCountLabel.setText("Vehicles: " + simulation.getVehicleCount());
    }

    /**
     * Shows a dialog with the properties of a vehicle and allows the user to modify them.
     * 
     * @param vehicle The vehicle to show properties for
     */
    private void showVehiclePropertiesDialog(Vehicle vehicle) {
        // Create the dialog
        Dialog<Pair<ButtonType, Vehicle>> dialog = new Dialog<>();
        dialog.setTitle("Vehicle Properties");
        dialog.setHeaderText("Edit vehicle properties");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the grid pane for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create the form fields
        ColorPicker colorPicker = new ColorPicker(vehicle.getColor());

        Spinner<Double> velocitySpinner = new Spinner<>();
        velocitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, vehicle.getVelocity(), 0.1));
        velocitySpinner.setEditable(true);

        Spinner<Double> accelerationSpinner = new Spinner<>();
        accelerationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-5, 5, vehicle.getAcceleration(), 0.1));
        accelerationSpinner.setEditable(true);

        Spinner<Double> maxVelocitySpinner = new Spinner<>();
        maxVelocitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, vehicle.getMaxVelocity(), 0.1));
        maxVelocitySpinner.setEditable(true);

        Spinner<Integer> laneSpinner = new Spinner<>();
        laneSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1, vehicle.getLane()));

        // Add fields for dimensions if it's a personal car or truck
        final Spinner<Double> widthSpinner;
        final Spinner<Double> heightSpinner;

        if (vehicle instanceof PersonalCar) {
            Spinner<Double> tempWidthSpinner = new Spinner<>();
            tempWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(30, 100, vehicle.getShape().getWidth(), 1));
            tempWidthSpinner.setEditable(true);
            widthSpinner = tempWidthSpinner;

            Spinner<Double> tempHeightSpinner = new Spinner<>();
            tempHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(15, 50, vehicle.getShape().getHeight(), 1));
            tempHeightSpinner.setEditable(true);
            heightSpinner = tempHeightSpinner;
        } else if (vehicle instanceof Truck) {
            Spinner<Double> tempWidthSpinner = new Spinner<>();
            tempWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(60, 200, vehicle.getShape().getWidth(), 1));
            tempWidthSpinner.setEditable(true);
            widthSpinner = tempWidthSpinner;

            Spinner<Double> tempHeightSpinner = new Spinner<>();
            tempHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(20, 80, vehicle.getShape().getHeight(), 1));
            tempHeightSpinner.setEditable(true);
            heightSpinner = tempHeightSpinner;
        } else {
            widthSpinner = null;
            heightSpinner = null;
        }

        // Add the form fields to the grid
        grid.add(new Label("Color:"), 0, 0);
        grid.add(colorPicker, 1, 0);

        grid.add(new Label("Velocity:"), 0, 1);
        grid.add(velocitySpinner, 1, 1);

        grid.add(new Label("Acceleration:"), 0, 2);
        grid.add(accelerationSpinner, 1, 2);

        grid.add(new Label("Max Velocity:"), 0, 3);
        grid.add(maxVelocitySpinner, 1, 3);

        grid.add(new Label("Lane:"), 0, 4);
        grid.add(laneSpinner, 1, 4);

        if (widthSpinner != null && heightSpinner != null) {
            grid.add(new Label("Width:"), 0, 5);
            grid.add(widthSpinner, 1, 5);

            grid.add(new Label("Height:"), 0, 6);
            grid.add(heightSpinner, 1, 6);
        }

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update the vehicle properties
                vehicle.setColor(colorPicker.getValue());
                vehicle.setVelocity(velocitySpinner.getValue());
                vehicle.setAcceleration(accelerationSpinner.getValue());
                vehicle.setMaxVelocity(maxVelocitySpinner.getValue());

                // Update the lane if it changed
                int newLane = laneSpinner.getValue();
                if (newLane != vehicle.getLane()) {
                    vehicle.changeLane(newLane, road.getInnerRadius(), road.getLaneWidth());
                }

                // Update the dimensions if applicable
                if (widthSpinner != null && heightSpinner != null) {
                    double newWidth = widthSpinner.getValue();
                    double newHeight = heightSpinner.getValue();

                    // Resize the shape
                    vehicle.getShape().setWidth(newWidth);
                    vehicle.getShape().setHeight(newHeight);

                    // Update the position to keep the vehicle centered
                    vehicle.getShape().setX(vehicle.getX() - newWidth / 2);
                    vehicle.getShape().setY(vehicle.getY() - newHeight / 2);
                }

                return new Pair<>(saveButtonType, vehicle);
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait();
    }

    /**
     * Updates the road and simulation with new dimensions.
     * 
     * @param width The new width of the simulation pane
     * @param height The new height of the simulation pane
     * @param laneWidth The width of each lane
     * @param obstacleAngle The angle where the obstacle is located
     */
    private void updateRoadAndSimulation(double width, double height, double laneWidth, double obstacleAngle) {
        // Remove existing road and simulation elements from the pane
        simulationPane.getChildren().clear();

        // Calculate new road parameters
        double centerX = width / 2;
        double centerY = height / 2;
        double innerRadius = Math.min(centerX, centerY) * 0.5; // Make sure the road fits in the pane

        // Create a new road and simulation
        road = new Road(centerX, centerY, innerRadius, laneWidth, obstacleAngle);
        simulation = new Simulation(road, simulationPane);

        // Update UI to reflect the new simulation
        updateVehicleCount();
    }
}
