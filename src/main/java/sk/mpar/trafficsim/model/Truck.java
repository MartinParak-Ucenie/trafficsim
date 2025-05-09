package sk.mpar.trafficsim.model;

import javafx.scene.paint.Color;

/**
 * Represents a truck in the traffic simulation.
 * Trucks are larger than personal cars and have a different color.
 */
public class Truck extends Vehicle {
    // Constants for truck dimensions
    public static final double WIDTH = 120;  // Longer side in direction of travel
    public static final double HEIGHT = 40;  // Shorter side perpendicular to travel
    public static final Color DEFAULT_COLOR = Color.RED;

    // Center of the circular road
    private double centerX;
    private double centerY;
    // Radius of the lane (distance from center to middle of lane)
    private double laneRadius;
    // Current angle in radians
    private double angle;

    /**
     * Creates a new truck.
     * 
     * @param x Initial x position
     * @param y Initial y position
     * @param lane Initial lane (0 for inner, 1 for outer)
     * @param centerX X coordinate of the center of the circular road
     * @param centerY Y coordinate of the center of the circular road
     * @param innerRadius Radius of the inner lane
     * @param laneWidth Width of each lane
     */
    public Truck(double x, double y, int lane, double centerX, double centerY, 
                double innerRadius, double laneWidth) {
        super(x, y, lane, DEFAULT_COLOR, WIDTH, HEIGHT);

        this.centerX = centerX;
        this.centerY = centerY;

        // Calculate the radius of the current lane
        this.laneRadius = innerRadius + (lane * laneWidth) + (laneWidth / 2);

        // Calculate initial angle based on position
        this.angle = Math.atan2(y - centerY, x - centerX);

        // Set a reasonable max velocity for trucks (slower than personal cars)
        this.maxVelocity = 2.0;

        // Rotate the truck to face the direction of travel
        double rotationAngle = Math.toDegrees(angle) + 90; // +90 because trucks move perpendicular to the radius
        shape.setRotate(rotationAngle);
    }

    @Override
    protected void updatePosition(double deltaTime) {
        // Update angle based on velocity
        // The angle change depends on the velocity and the radius of the lane
        // Smaller radius means the truck needs to turn more to travel the same distance
        // Apply speed multiplier to make trucks move faster
        angle += (velocity * deltaTime * SPEED_MULTIPLIER) / laneRadius;

        // Keep angle between 0 and 2*PI
        angle = angle % (2 * Math.PI);

        // Update position based on angle and lane radius
        x = centerX + laneRadius * Math.cos(angle);
        y = centerY + laneRadius * Math.sin(angle);

        // Rotate the truck to face the direction of travel
        double rotationAngle = Math.toDegrees(angle) + 90; // +90 because trucks move perpendicular to the radius
        shape.setRotate(rotationAngle);
    }

    /**
     * Updates the lane of the truck and adjusts its radius accordingly.
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
