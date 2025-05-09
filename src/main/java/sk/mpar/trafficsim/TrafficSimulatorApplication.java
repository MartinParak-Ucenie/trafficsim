package sk.mpar.trafficsim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the traffic simulator.
 */
public class TrafficSimulatorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TrafficSimulatorApplication.class.getResource("traffic-simulator.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Traffic Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}