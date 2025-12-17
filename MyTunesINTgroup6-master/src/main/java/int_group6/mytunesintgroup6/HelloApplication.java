package int_group6.mytunesintgroup6;

import int_group6.mytunesintgroup6.bll.MyTunesManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        // Samu: Initialize the Business Logic Layer and pass it to the controller.
        // This keeps the controllers focused on UI responsibilities.
        MyTunesManager manager = new MyTunesManager();
        if (fxmlLoader.getController() instanceof int_group6.mytunesintgroup6.gui.HelloController controller) {
            controller.setManager(manager);
        }

        // Let the FXML drive sizing (it already defines prefWidth/prefHeight).
        Scene scene = new Scene(root);
        stage.setTitle("Group 6 INT Media Player");
        stage.setScene(scene);
        stage.show();
    }
}
