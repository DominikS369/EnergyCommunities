package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class GuiApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                GuiApplication.class.getResource("/gui/main-view.fxml"));
        stage.setTitle("Energy Communities - Intermediate");
        stage.setScene(new Scene(root, 720, 520));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
