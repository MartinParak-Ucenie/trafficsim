package sk.mpar.trafficsim.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.layout.Pane;
import javafx.collections.ObservableList;

/**
 * Represents the circular two-lane road with an obstacle.
 */
public class Road {
    // Road parameters
    private double centerX;
    private double centerY;
    private double innerRadius;
    private double laneWidth;
    private double outerRadius;

    // Visual representation
    private Circle innerCircle;
    private Circle outerCircle;
    private Circle middleCircle; // Circle for lane divider
    private Shape roadShape; // The actual road shape (ring)

    // Obstacle
    private Rectangle obstacle;
    private double obstacleAngle; // Angle in radians where the obstacle is located

    /**
     * Creates a new circular road with two lanes and an obstacle.
     * 
     * @param centerX X coordinate of the center of the road
     * @param centerY Y coordinate of the center of the road
     * @param innerRadius Radius of the inner edge of the road
     * @param laneWidth Width of each lane
     * @param obstacleAngle Angle in radians where the obstacle is located
     */
    public Road(double centerX, double centerY, double innerRadius, double laneWidth, double obstacleAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.innerRadius = innerRadius;
        this.laneWidth = laneWidth;
        this.outerRadius = innerRadius + 2 * laneWidth; // Two lanes
        this.obstacleAngle = obstacleAngle;

        // Create the visual representation of the road
        createRoadShape();

        // Create the obstacle
        createObstacle();
    }

    /**
     * Creates the visual representation of the road.
     */
    private void createRoadShape() {
        // Create inner and outer circles
        innerCircle = new Circle(centerX, centerY, innerRadius);
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(2);
        innerCircle.setFill(Color.TRANSPARENT);

        // Create middle circle (lane divider)
        double middleRadius = innerRadius + laneWidth;
        middleCircle = new Circle(centerX, centerY, middleRadius);
        middleCircle.setStroke(Color.WHITE);
        middleCircle.setStrokeWidth(2);
        middleCircle.getStrokeDashArray().addAll(10.0, 10.0); // Dashed line
        middleCircle.setFill(Color.TRANSPARENT);

        // Create outer circle
        outerCircle = new Circle(centerX, centerY, outerRadius);
        outerCircle.setStroke(Color.WHITE);
        outerCircle.setStrokeWidth(2);
        outerCircle.setFill(Color.TRANSPARENT);

        // Create the road shape as a ring (outer circle minus inner circle)
        roadShape = Shape.subtract(outerCircle, innerCircle);
        roadShape.setFill(Color.DARKGRAY);
    }

    /**
     * Creates the obstacle on the road.
     */
    private void createObstacle() {
        // Calculate the position of the obstacle based on the angle
        double obstacleX = centerX + (innerRadius + laneWidth / 2) * Math.cos(obstacleAngle);
        double obstacleY = centerY + (innerRadius + laneWidth / 2) * Math.sin(obstacleAngle);

        // Create the obstacle as a rectangle
        obstacle = new Rectangle(obstacleX - 15, obstacleY - 15, 30, 30);
        obstacle.setFill(Color.ORANGE);
        obstacle.setRotate(Math.toDegrees(obstacleAngle) + 45); // Rotate to align with the road
    }

    /**
     * Checks if a point is on the road.
     * 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @return true if the point is on the road, false otherwise
     */
    public boolean isOnRoad(double x, double y) {
        // Calculate the distance from the center
        double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        // Check if the distance is between the inner and outer radius
        return distance >= innerRadius && distance <= outerRadius;
    }

    /**
     * Checks if a vehicle is near the obstacle.
     * 
     * @param vehicle The vehicle to check
     * @return true if the vehicle is near the obstacle, false otherwise
     */
    public boolean isNearObstacle(Vehicle vehicle) {
        // Calculate the angle of the vehicle
        double vehicleAngle = Math.atan2(vehicle.getY() - centerY, vehicle.getX() - centerX);

        // Normalize angles to [0, 2*PI)
        vehicleAngle = (vehicleAngle + 2 * Math.PI) % (2 * Math.PI);
        double normalizedObstacleAngle = (obstacleAngle + 2 * Math.PI) % (2 * Math.PI);

        // Check if the vehicle is in the inner lane (lane 0)
        if (vehicle.getLane() == 0) {
            // Check if the vehicle is near the obstacle angle
            double angleDifference = Math.abs(vehicleAngle - normalizedObstacleAngle);
            angleDifference = Math.min(angleDifference, 2 * Math.PI - angleDifference);

            // Consider the vehicle near the obstacle if it's within a certain angle range
            // and in the same lane as the obstacle
            return angleDifference < 0.2; // Adjust this threshold as needed
        }

        return false;
    }

    /**
     * Checks if a vehicle collides with the obstacle.
     * 
     * @param vehicle The vehicle to check
     * @return true if the vehicle collides with the obstacle, false otherwise
     */
    public boolean collidesWithObstacle(Vehicle vehicle) {
        return vehicle.collidesWithObstacle(obstacle);
    }

    // Getters

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public double getLaneWidth() {
        return laneWidth;
    }

    public double getOuterRadius() {
        return outerRadius;
    }

    public Shape getRoadShape() {
        return roadShape;
    }

    public Rectangle getObstacle() {
        return obstacle;
    }

    public double getObstacleAngle() {
        return obstacleAngle;
    }

    public Circle getInnerCircle() {
        return innerCircle;
    }

    public Circle getMiddleCircle() {
        return middleCircle;
    }

    public Circle getOuterCircle() {
        return outerCircle;
    }
}
