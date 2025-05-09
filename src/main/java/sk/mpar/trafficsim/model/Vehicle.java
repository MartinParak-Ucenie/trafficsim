package sk.mpar.trafficsim.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Abstract class representing a vehicle in the traffic simulation.
 */
public abstract class Vehicle {
    // Position
    protected double x;
    protected double y;

    // Velocity and acceleration
    protected double velocity;
    protected double acceleration;
    protected double maxVelocity;

    // Speed multiplier to make vehicles move faster
    protected static final double SPEED_MULTIPLIER = 3.0;

    // Lane information
    protected int lane; // 0 for inner lane, 1 for outer lane
    protected boolean isChangingLane;

    // Visual representation
    protected Rectangle shape;

    /**
     * Creates a new vehicle.
     * 
     * @param x Initial x position
     * @param y Initial y position
     * @param lane Initial lane (0 for inner, 1 for outer)
     * @param color Color of the vehicle
     * @param width Width of the vehicle
     * @param height Height of the vehicle
     */
    public Vehicle(double x, double y, int lane, Color color, double width, double height) {
        this.x = x;
        this.y = y;
        this.lane = lane;
        this.velocity = 0;
        this.acceleration = 0;
        this.maxVelocity = 5;
        this.isChangingLane = false;

        // Create visual representation
        this.shape = new Rectangle(width, height);
        this.shape.setFill(color);
        updateShapePosition();
    }

    /**
     * Updates the vehicle's position based on its velocity and acceleration.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(double deltaTime) {
        // Update velocity based on acceleration
        velocity += acceleration * deltaTime;

        // Clamp velocity between 0 and maxVelocity
        velocity = Math.max(0, Math.min(velocity, maxVelocity));

        // Update position based on velocity
        // This will be implemented by subclasses to handle the circular movement
        updatePosition(deltaTime);

        // Update the visual representation
        updateShapePosition();
    }

    /**
     * Updates the position of the vehicle based on its velocity.
     * This method should be implemented by subclasses to handle the specific movement pattern.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    protected abstract void updatePosition(double deltaTime);

    /**
     * Updates the position of the shape to match the vehicle's position.
     */
    protected void updateShapePosition() {
        // Center the shape on the vehicle's position
        shape.setX(x - shape.getWidth() / 2);
        shape.setY(y - shape.getHeight() / 2);
    }

    /**
     * Checks if this vehicle collides with another vehicle.
     * 
     * @param other The other vehicle to check collision with
     * @return true if the vehicles collide, false otherwise
     */
    public boolean collidesWith(Vehicle other) {
        return shape.getBoundsInParent().intersects(other.shape.getBoundsInParent());
    }

    /**
     * Checks if this vehicle collides with a rectangle (obstacle).
     * 
     * @param obstacle The obstacle to check collision with
     * @return true if the vehicle collides with the obstacle, false otherwise
     */
    public boolean collidesWithObstacle(Rectangle obstacle) {
        return shape.getBoundsInParent().intersects(obstacle.getBoundsInParent());
    }

    // Getters and setters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = Math.max(0, Math.min(velocity, maxVelocity));
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public boolean isChangingLane() {
        return isChangingLane;
    }

    public void setChangingLane(boolean changingLane) {
        isChangingLane = changingLane;
    }

    /**
     * Updates the lane of the vehicle and adjusts its position accordingly.
     * This method should be implemented by subclasses to handle the specific lane changing behavior.
     * 
     * @param newLane The new lane (0 for inner, 1 for outer)
     * @param innerRadius Radius of the inner lane
     * @param laneWidth Width of each lane
     */
    public abstract void changeLane(int newLane, double innerRadius, double laneWidth);

    public Rectangle getShape() {
        return shape;
    }

    /**
     * Sets the color of the vehicle.
     * 
     * @param color The new color
     */
    public void setColor(Color color) {
        shape.setFill(color);
    }

    /**
     * Gets the current color of the vehicle.
     * 
     * @return The current color
     */
    public Color getColor() {
        return (Color) shape.getFill();
    }
}
