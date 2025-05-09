package sk.mpar.trafficsim.model;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages the traffic simulation.
 */
public class Simulation {
    // Simulation components
    private Road road;
    private List<Vehicle> vehicles;
    private Pane simulationPane;

    // Simulation parameters
    private double defaultAcceleration = 1.0;
    private boolean isRunning = false;
    private Random random = new Random();

    // Animation timer for the simulation loop
    private AnimationTimer animationTimer;

    // Time tracking for deltaTime calculation
    private long lastUpdateTime = 0;

    /**
     * Creates a new simulation.
     * 
     * @param road The road for the simulation
     * @param simulationPane The pane where the simulation will be rendered
     */
    public Simulation(Road road, Pane simulationPane) {
        this.road = road;
        this.simulationPane = simulationPane;
        this.vehicles = new ArrayList<>();

        // Add the road shape to the pane
        simulationPane.getChildren().add(road.getRoadShape());

        // Add the lane markings
        simulationPane.getChildren().addAll(
            road.getInnerCircle(),
            road.getMiddleCircle(),
            road.getOuterCircle()
        );

        // Add the obstacle
        simulationPane.getChildren().add(road.getObstacle());

        // Initialize the animation timer
        initializeAnimationTimer();
    }

    /**
     * Initializes the animation timer for the simulation loop.
     */
    private void initializeAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate delta time in seconds
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
                lastUpdateTime = now;

                // Update the simulation
                update(deltaTime);
            }
        };
    }

    /**
     * Updates the simulation.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    private void update(double deltaTime) {
        // Update each vehicle
        for (Vehicle vehicle : vehicles) {
            // Store the original position
            double originalX = vehicle.getX();
            double originalY = vehicle.getY();

            // Update the vehicle's position
            vehicle.update(deltaTime);

            // Check if the vehicle is still on the road
            if (!road.isOnRoad(vehicle.getX(), vehicle.getY())) {
                // If not, revert to the original position
                vehicle.setVelocity(0);
                // TODO: Implement better handling of vehicles going off-road
            }

            // Check for collision with the obstacle
            if (road.collidesWithObstacle(vehicle)) {
                // If colliding with the obstacle, revert to the original position
                vehicle.setVelocity(0);
                // TODO: Implement better handling of obstacle collisions
            }

            // Check if the vehicle is near the obstacle and needs to change lanes
            if (road.isNearObstacle(vehicle) && vehicle.getLane() == 0 && !vehicle.isChangingLane()) {
                // Try to change to the outer lane to avoid the obstacle
                boolean canChangeLane = true;

                // Check if there's a vehicle in the way
                for (Vehicle otherVehicle : vehicles) {
                    if (otherVehicle != vehicle && otherVehicle.getLane() == 1) {
                        // Calculate the angle between the vehicles
                        double angle1 = Math.atan2(vehicle.getY() - road.getCenterY(), vehicle.getX() - road.getCenterX());
                        double angle2 = Math.atan2(otherVehicle.getY() - road.getCenterY(), otherVehicle.getX() - road.getCenterX());

                        // Normalize angles to [0, 2*PI)
                        angle1 = (angle1 + 2 * Math.PI) % (2 * Math.PI);
                        angle2 = (angle2 + 2 * Math.PI) % (2 * Math.PI);

                        // Calculate the angle difference
                        double angleDiff = Math.abs(angle1 - angle2);
                        angleDiff = Math.min(angleDiff, 2 * Math.PI - angleDiff);

                        // If the vehicles are too close, don't change lanes
                        if (angleDiff < 0.3) { // Adjust this threshold as needed
                            canChangeLane = false;
                            break;
                        }
                    }
                }

                if (canChangeLane) {
                    vehicle.setChangingLane(true);
                    vehicle.changeLane(1, road.getInnerRadius(), road.getLaneWidth());
                } else {
                    // If can't change lane, slow down
                    vehicle.setAcceleration(-2.0);
                }
            }

            // If the vehicle has passed the obstacle, it can return to the inner lane
            if (vehicle.isChangingLane() && vehicle.getLane() == 1) {
                // Calculate the angle of the vehicle and the obstacle
                double vehicleAngle = Math.atan2(vehicle.getY() - road.getCenterY(), vehicle.getX() - road.getCenterX());
                double obstacleAngle = road.getObstacleAngle();

                // Normalize angles to [0, 2*PI)
                vehicleAngle = (vehicleAngle + 2 * Math.PI) % (2 * Math.PI);
                obstacleAngle = (obstacleAngle + 2 * Math.PI) % (2 * Math.PI);

                // Calculate the angle difference
                double angleDiff = Math.abs(vehicleAngle - obstacleAngle);
                angleDiff = Math.min(angleDiff, 2 * Math.PI - angleDiff);

                // If the vehicle has passed the obstacle, it can return to the inner lane
                if (angleDiff > 0.5) { // Adjust this threshold as needed
                    boolean canChangeLane = true;

                    // Check if there's a vehicle in the way
                    for (Vehicle otherVehicle : vehicles) {
                        if (otherVehicle != vehicle && otherVehicle.getLane() == 0) {
                            // Calculate the angle between the vehicles
                            double angle1 = Math.atan2(vehicle.getY() - road.getCenterY(), vehicle.getX() - road.getCenterX());
                            double angle2 = Math.atan2(otherVehicle.getY() - road.getCenterY(), otherVehicle.getX() - road.getCenterX());

                            // Normalize angles to [0, 2*PI)
                            angle1 = (angle1 + 2 * Math.PI) % (2 * Math.PI);
                            angle2 = (angle2 + 2 * Math.PI) % (2 * Math.PI);

                            // Calculate the angle difference
                            double vehicleAngleDiff = Math.abs(angle1 - angle2);
                            vehicleAngleDiff = Math.min(vehicleAngleDiff, 2 * Math.PI - vehicleAngleDiff);

                            // If the vehicles are too close, don't change lanes
                            if (vehicleAngleDiff < 0.3) { // Adjust this threshold as needed
                                canChangeLane = false;
                                break;
                            }
                        }
                    }

                    if (canChangeLane) {
                        vehicle.setChangingLane(false);
                        vehicle.changeLane(0, road.getInnerRadius(), road.getLaneWidth());
                    }
                }
            }

            // Check for vehicles ahead in the same lane
            Vehicle vehicleAhead = null;
            double minAngleDiff = Double.MAX_VALUE;

            for (Vehicle otherVehicle : vehicles) {
                if (vehicle != otherVehicle && otherVehicle.getLane() == vehicle.getLane()) {
                    // Calculate the angle between the vehicles
                    double angle1 = Math.atan2(vehicle.getY() - road.getCenterY(), vehicle.getX() - road.getCenterX());
                    double angle2 = Math.atan2(otherVehicle.getY() - road.getCenterY(), otherVehicle.getX() - road.getCenterX());

                    // Normalize angles to [0, 2*PI)
                    angle1 = (angle1 + 2 * Math.PI) % (2 * Math.PI);
                    angle2 = (angle2 + 2 * Math.PI) % (2 * Math.PI);

                    // Calculate the angle difference
                    double angleDiff = (angle2 - angle1 + 2 * Math.PI) % (2 * Math.PI);

                    // Check if the other vehicle is ahead (within a small angle)
                    if (angleDiff > 0 && angleDiff < 0.3) { // Adjust this threshold as needed
                        if (angleDiff < minAngleDiff) {
                            minAngleDiff = angleDiff;
                            vehicleAhead = otherVehicle;
                        }
                    }
                }
            }

            // If there's a vehicle ahead, try to change lanes or slow down
            if (vehicleAhead != null && !vehicle.isChangingLane()) {
                // Determine the target lane (opposite of current lane)
                int targetLane = (vehicle.getLane() == 0) ? 1 : 0;

                // Check if it's safe to change to the target lane
                boolean canChangeLane = true;

                for (Vehicle otherVehicle : vehicles) {
                    if (otherVehicle != vehicle && otherVehicle.getLane() == targetLane) {
                        // Calculate the angle between the vehicles
                        double angle1 = Math.atan2(vehicle.getY() - road.getCenterY(), vehicle.getX() - road.getCenterX());
                        double angle2 = Math.atan2(otherVehicle.getY() - road.getCenterY(), otherVehicle.getX() - road.getCenterX());

                        // Normalize angles to [0, 2*PI)
                        angle1 = (angle1 + 2 * Math.PI) % (2 * Math.PI);
                        angle2 = (angle2 + 2 * Math.PI) % (2 * Math.PI);

                        // Calculate the angle difference
                        double angleDiff = Math.abs(angle1 - angle2);
                        angleDiff = Math.min(angleDiff, 2 * Math.PI - angleDiff);

                        // If the vehicles are too close, don't change lanes
                        if (angleDiff < 0.3) { // Adjust this threshold as needed
                            canChangeLane = false;
                            break;
                        }
                    }
                }

                if (canChangeLane) {
                    // Change to the target lane
                    vehicle.setChangingLane(true);
                    vehicle.changeLane(targetLane, road.getInnerRadius(), road.getLaneWidth());
                } else {
                    // If can't change lane, slow down
                    vehicle.setAcceleration(-2.0);
                }
            }

            // Check for collisions with other vehicles
            for (Vehicle otherVehicle : vehicles) {
                if (vehicle != otherVehicle && vehicle.collidesWith(otherVehicle)) {
                    // If colliding with another vehicle, revert to the original position
                    vehicle.setVelocity(0);
                    // TODO: Implement better handling of vehicle collisions
                }
            }
        }
    }

    /**
     * Adds a personal car to the simulation at a random position.
     * 
     * @return The added car
     */
    public PersonalCar addPersonalCar() {
        return addPersonalCar(PersonalCar.DEFAULT_COLOR);
    }

    /**
     * Adds a personal car with the specified color to the simulation at a specific position.
     * 
     * @param color The color of the car
     * @param x The x coordinate where to add the car
     * @param y The y coordinate where to add the car
     * @return The added car, or null if a car could not be added at that position
     */
    public PersonalCar addPersonalCar(Color color, double x, double y) {
        // Calculate the distance from the center
        double distance = Math.sqrt(Math.pow(x - road.getCenterX(), 2) + Math.pow(y - road.getCenterY(), 2));

        // Determine the lane based on the distance
        int lane;
        if (distance < road.getInnerRadius() + road.getLaneWidth()) {
            lane = 0; // Inner lane
        } else {
            lane = 1; // Outer lane
        }

        // Create a car at the specified position
        PersonalCar car = new PersonalCar(x, y, lane, road.getCenterX(), road.getCenterY(), 
                                         road.getInnerRadius(), road.getLaneWidth());

        // Set the color if different from default
        if (!color.equals(PersonalCar.DEFAULT_COLOR)) {
            car.setColor(color);
        }

        // Check if the car collides with the obstacle
        if (road.collidesWithObstacle(car)) {
            return null; // Can't add car at this position
        }

        // Check if the car collides with any existing vehicle
        for (Vehicle vehicle : vehicles) {
            if (car.collidesWith(vehicle)) {
                return null; // Can't add car at this position
            }
        }

        // If we get here, the car doesn't collide with anything
        // Set initial acceleration
        car.setAcceleration(defaultAcceleration);

        // Add the car to the list of vehicles
        vehicles.add(car);

        // Add the car's shape to the pane
        simulationPane.getChildren().add(car.getShape());

        return car;
    }

    /**
     * Adds a personal car with the specified color to the simulation at a random position.
     * 
     * @param color The color of the car
     * @return The added car, or null if a car could not be added
     */
    public PersonalCar addPersonalCar(Color color) {
        // Try multiple positions to find one without collisions
        for (int attempts = 0; attempts < 50; attempts++) {
            // Generate a random angle
            double angle = random.nextDouble() * 2 * Math.PI;

            // Randomly choose a lane (0 for inner, 1 for outer)
            int lane = random.nextInt(2);

            // Calculate the radius of the lane
            double laneRadius = road.getInnerRadius() + (lane * road.getLaneWidth()) + (road.getLaneWidth() / 2);

            // Calculate the position based on the angle and lane radius
            double x = road.getCenterX() + laneRadius * Math.cos(angle);
            double y = road.getCenterY() + laneRadius * Math.sin(angle);

            // Create a temporary car to check for collisions
            PersonalCar car = new PersonalCar(x, y, lane, road.getCenterX(), road.getCenterY(), 
                                             road.getInnerRadius(), road.getLaneWidth());

            // Set the color if different from default
            if (!color.equals(PersonalCar.DEFAULT_COLOR)) {
                car.setColor(color);
            }

            // Check if the car collides with the obstacle
            if (road.collidesWithObstacle(car)) {
                continue; // Try another position
            }

            // Check if the car collides with any existing vehicle
            boolean collides = false;
            for (Vehicle vehicle : vehicles) {
                if (car.collidesWith(vehicle)) {
                    collides = true;
                    break;
                }
            }

            if (collides) {
                continue; // Try another position
            }

            // If we get here, the car doesn't collide with anything
            // Set initial acceleration
            car.setAcceleration(defaultAcceleration);

            // Add the car to the list of vehicles
            vehicles.add(car);

            // Add the car's shape to the pane
            simulationPane.getChildren().add(car.getShape());

            return car;
        }

        // If we couldn't find a valid position after multiple attempts, return null
        return null;
    }

    /**
     * Adds multiple personal cars to the simulation at random positions.
     * 
     * @param count The number of cars to add
     */
    public void addPersonalCars(int count) {
        addPersonalCars(count, PersonalCar.DEFAULT_COLOR);
    }

    /**
     * Adds multiple personal cars with the specified color to the simulation at random positions.
     * 
     * @param count The number of cars to add
     * @param color The color of the cars
     */
    public void addPersonalCars(int count, Color color) {
        for (int i = 0; i < count; i++) {
            addPersonalCar(color);
        }
    }

    /**
     * Adds a truck to the simulation at a random position.
     * 
     * @return The added truck
     */
    public Truck addTruck() {
        return addTruck(Truck.DEFAULT_COLOR);
    }

    /**
     * Adds a truck with the specified color to the simulation at a specific position.
     * 
     * @param color The color of the truck
     * @param x The x coordinate where to add the truck
     * @param y The y coordinate where to add the truck
     * @return The added truck, or null if a truck could not be added at that position
     */
    public Truck addTruck(Color color, double x, double y) {
        // Calculate the distance from the center
        double distance = Math.sqrt(Math.pow(x - road.getCenterX(), 2) + Math.pow(y - road.getCenterY(), 2));

        // Determine the lane based on the distance
        int lane;
        if (distance < road.getInnerRadius() + road.getLaneWidth()) {
            lane = 0; // Inner lane
        } else {
            lane = 1; // Outer lane
        }

        // Create a truck at the specified position
        Truck truck = new Truck(x, y, lane, road.getCenterX(), road.getCenterY(), 
                               road.getInnerRadius(), road.getLaneWidth());

        // Set the color if different from default
        if (!color.equals(Truck.DEFAULT_COLOR)) {
            truck.setColor(color);
        }

        // Check if the truck collides with the obstacle
        if (road.collidesWithObstacle(truck)) {
            return null; // Can't add truck at this position
        }

        // Check if the truck collides with any existing vehicle
        for (Vehicle vehicle : vehicles) {
            if (truck.collidesWith(vehicle)) {
                return null; // Can't add truck at this position
            }
        }

        // If we get here, the truck doesn't collide with anything
        // Set initial acceleration
        truck.setAcceleration(defaultAcceleration);

        // Add the truck to the list of vehicles
        vehicles.add(truck);

        // Add the truck's shape to the pane
        simulationPane.getChildren().add(truck.getShape());

        return truck;
    }

    /**
     * Adds a truck with the specified color to the simulation at a random position.
     * 
     * @param color The color of the truck
     * @return The added truck, or null if a truck could not be added
     */
    public Truck addTruck(Color color) {
        // Try multiple positions to find one without collisions
        for (int attempts = 0; attempts < 50; attempts++) {
            // Generate a random angle
            double angle = random.nextDouble() * 2 * Math.PI;

            // Randomly choose a lane (0 for inner, 1 for outer)
            int lane = random.nextInt(2);

            // Calculate the radius of the lane
            double laneRadius = road.getInnerRadius() + (lane * road.getLaneWidth()) + (road.getLaneWidth() / 2);

            // Calculate the position based on the angle and lane radius
            double x = road.getCenterX() + laneRadius * Math.cos(angle);
            double y = road.getCenterY() + laneRadius * Math.sin(angle);

            // Create a temporary truck to check for collisions
            Truck truck = new Truck(x, y, lane, road.getCenterX(), road.getCenterY(), 
                                   road.getInnerRadius(), road.getLaneWidth());

            // Set the color if different from default
            if (!color.equals(Truck.DEFAULT_COLOR)) {
                truck.setColor(color);
            }

            // Check if the truck collides with the obstacle
            if (road.collidesWithObstacle(truck)) {
                continue; // Try another position
            }

            // Check if the truck collides with any existing vehicle
            boolean collides = false;
            for (Vehicle vehicle : vehicles) {
                if (truck.collidesWith(vehicle)) {
                    collides = true;
                    break;
                }
            }

            if (collides) {
                continue; // Try another position
            }

            // If we get here, the truck doesn't collide with anything
            // Set initial acceleration
            truck.setAcceleration(defaultAcceleration);

            // Add the truck to the list of vehicles
            vehicles.add(truck);

            // Add the truck's shape to the pane
            simulationPane.getChildren().add(truck.getShape());

            return truck;
        }

        // If we couldn't find a valid position after multiple attempts, return null
        return null;
    }

    /**
     * Adds multiple trucks to the simulation at random positions.
     * 
     * @param count The number of trucks to add
     */
    public void addTrucks(int count) {
        addTrucks(count, Truck.DEFAULT_COLOR);
    }

    /**
     * Adds multiple trucks with the specified color to the simulation at random positions.
     * 
     * @param count The number of trucks to add
     * @param color The color of the trucks
     */
    public void addTrucks(int count, Color color) {
        for (int i = 0; i < count; i++) {
            addTruck(color);
        }
    }

    /**
     * Removes a vehicle from the simulation.
     * 
     * @param vehicle The vehicle to remove
     */
    public void removeVehicle(Vehicle vehicle) {
        // Remove the vehicle's shape from the pane
        simulationPane.getChildren().remove(vehicle.getShape());

        // Remove the vehicle from the list
        vehicles.remove(vehicle);
    }

    /**
     * Removes a random vehicle from the simulation.
     * 
     * @return true if a vehicle was removed, false if there were no vehicles to remove
     */
    public boolean removeRandomVehicle() {
        if (vehicles.isEmpty()) {
            return false;
        }

        // Select a random vehicle
        int index = random.nextInt(vehicles.size());
        Vehicle vehicle = vehicles.get(index);

        // Remove the vehicle
        removeVehicle(vehicle);

        return true;
    }

    /**
     * Starts the simulation.
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            lastUpdateTime = 0;
            animationTimer.start();
        }
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            animationTimer.stop();
        }
    }

    /**
     * Sets the default acceleration for new vehicles.
     * 
     * @param acceleration The new default acceleration
     */
    public void setDefaultAcceleration(double acceleration) {
        this.defaultAcceleration = acceleration;

        // Update acceleration for all existing vehicles
        for (Vehicle vehicle : vehicles) {
            vehicle.setAcceleration(acceleration);
        }
    }

    /**
     * Sets the maximum velocity for all vehicles.
     * 
     * @param maxVelocity The new maximum velocity
     */
    public void setMaxVelocity(double maxVelocity) {
        for (Vehicle vehicle : vehicles) {
            vehicle.setMaxVelocity(maxVelocity);
        }
    }

    /**
     * Sets the maximum velocity for all personal cars.
     * 
     * @param maxVelocity The new maximum velocity for personal cars
     */
    public void setPersonalCarMaxVelocity(double maxVelocity) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle instanceof PersonalCar) {
                vehicle.setMaxVelocity(maxVelocity);
            }
        }
    }

    /**
     * Sets the maximum velocity for all trucks.
     * 
     * @param maxVelocity The new maximum velocity for trucks
     */
    public void setTruckMaxVelocity(double maxVelocity) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle instanceof Truck) {
                vehicle.setMaxVelocity(maxVelocity);
            }
        }
    }

    /**
     * Gets the number of vehicles in the simulation.
     * 
     * @return The number of vehicles
     */
    public int getVehicleCount() {
        return vehicles.size();
    }

    /**
     * Checks if the simulation is running.
     * 
     * @return true if the simulation is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets all vehicles of the specified type.
     * 
     * @param <T> The type of vehicle to get
     * @param vehicleClass The class of the vehicle type
     * @return A list of vehicles of the specified type
     */
    public <T extends Vehicle> List<T> getVehicles(Class<T> vehicleClass) {
        List<T> result = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicleClass.isInstance(vehicle)) {
                result.add(vehicleClass.cast(vehicle));
            }
        }
        return result;
    }

    /**
     * Finds a vehicle at the specified position.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The vehicle at the specified position, or null if there is no vehicle at that position
     */
    public Vehicle getVehicleAt(double x, double y) {
        for (Vehicle vehicle : vehicles) {
            // Check if the point (x, y) is inside the vehicle's shape
            if (vehicle.getShape().contains(x - vehicle.getShape().getX(), y - vehicle.getShape().getY())) {
                return vehicle;
            }
        }
        return null;
    }
}
