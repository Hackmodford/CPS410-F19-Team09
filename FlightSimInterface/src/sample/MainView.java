package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class MainView extends Application {

    private MainController controller;
    private MainModel model;
    @FXML TextField pField;
    @FXML TextField iField;
    @FXML TextField dField;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new MainController(this);
        model = new MainModel();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public void updateValues(){
        
    }

    public void startSim(){
        controller.startSim();
    }

    public void endSim(){
        controller.endSim();
    }

    public void beginEditConfig(){
        controller.beginEditConfig();
    }
}
