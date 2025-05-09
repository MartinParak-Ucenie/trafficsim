package sk.mpar.trafficsim.model;

import javafx.scene.paint.Color;

/**
 * Represents a personal car in the traffic simulation.
 * Personal cars are smaller than trucks and have a different color.
 */
public class PersonalCar extends Vehicle {
    // Constants for personal car dimensions
    public static final double WIDTH = 60;  // Longer side in direction of travel
    public static final double HEIGHT = 30; // Shorter side perpendicular to travel
    public static final Color DEFAULT_COLOR = Color.BLUE;

    // Center of the circular road
    private double centerX;
    private double centerY;
    // Radius of the lane (distance from center to middle of lane)
    private double laneRadius;
    // Current angle in radians
    private double angle;

    /**
     * Creates a new personal car.
     * 
     * @param x Initial x position
     * @param y Initial y position
     * @param lane Initial lane (0 for inner, 1 for outer)
     * @param centerX X coordinate of the center of the circular road
     * @param centerY Y coordinate of the center of the circular road
     * @param innerRadius Radius of the inner lane
     * @param laneWidth Width of each lane
     */
    public PersonalCar(double x, double y, int lane, double centerX, double centerY, 
                       double innerRadius, double laneWidth) {
        super(x, y, lane, DEFAULT_COLOR, WIDTH, HEIGHT);

        this.centerX = centerX;
        this.centerY = centerY;

        // Calculate the radius of the current lane
        this.laneRadius = innerRadius + (lane * laneWidth) + (laneWidth / 2);

        // Calculate initial angle based on position
        this.angle = Math.atan2(y - centerY, x - centerX);

        // Set a reasonable max velocity for personal cars
        this.maxVelocity = 3.0;

        // Rotate the car to face the direction of travel
        double rotationAngle = Math.toDegrees(angle) + 90; // +90 because cars move perpendicular to the radius
        shape.setRotate(rotationAngle);
    }

    @Override
    protected void updatePosition(double deltaTime) {
        // Update angle based on velocity
        // The angle change depends on the velocity and the radius of the lane
        // Smaller radius means the car needs to turn more to travel the same distance
        // Apply speed multiplier to make cars move faster
        angle += (velocity * deltaTime * SPEED_MULTIPLIER) / laneRadius;

        // Keep angle between 0 and 2*PI
        angle = angle % (2 * Math.PI);

        // Update position based on angle and lane radius
        x = centerX + laneRadius * Math.cos(angle);
        y = centerY + laneRadius * Math.sin(angle);

        // Rotate the car to face the direction of travel
        double rotationAngle = Math.toDegrees(angle) + 90; // +90 because cars move perpendicular to the radius
        shape.setRotate(rotationAngle);
    }

    /**
     * Updates the lane of the car and adjusts its radius accordingly.
     * 
     * @param newLane The new lane (0 for inner, 1 for outer)
     * @param innerRadius Radius of the inner lane
     * @param laneWidth Width of each lane
     */
    public void changeLane(int newLane, double innerRadius, double laneWidth) {
        this.lane = newLane;
        this.laneRadius = innerRadius + (lane * laneWidth) + (laneWidth / 2);
    }
}
