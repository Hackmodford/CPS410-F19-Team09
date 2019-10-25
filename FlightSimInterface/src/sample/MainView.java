package sample;

import com.sun.tools.doclets.internal.toolkit.builders.ConstantsSummaryBuilder;
import com.sun.tools.internal.jxc.ap.Const;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.awt.*;

public class MainView extends Application {

    /*
    //Values to display
    set pvalue (double p, double i, double d)
    current pvalue (double p, double i, double d)
    set rvalue (double p, double i, double d)
    current rvalue (double p, double i, double d)
    voltage [-10, 10]

    //values we want manipulate
    pvalue (double p, double i, double d) voltage
    rvalue (double p, double i, double d) voltage
    DAC (on four channels: Pitch - A, Roll - B, Height - C, Balance? - D)
            -pick a channel, and send following values (byte)
            -course gain
                • byte
            -fine gain
                • signed char -> 6-bit in B's program
                • -32 LSBs -> 31 LSBs
            -offset
                • -16 LSBs -> 15.875 LSBs

        CMD =
    X-- set pvalue
    X-- set rvalue
    Course gain

     */

    private MainController controller;
    private MainModel model;

    private char command;

    public TextField pField;
    public TextField iField;
    public TextField dField;
    private int[] pidVals = new int[3];

    public SplitMenuButton commandMenu;

    public Button btnSubmit;
    public Button btnStart;

    public Label errorLabel;



    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new MainController(this);
        model = new MainModel();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    private void initElementProperties(){
        btnSubmit.setDisable(true);
        errorLabel.setVisible(true);
    }


    public void sendCommand(){
        switch (command) {
            case Constants.PITCH:
                controller.sendCommand(buildPidCommand());
            case Constants.ROLL:
                controller.sendCommand(buildPidCommand());
        }
        System.out.println(String.format("pval = %d\n" +
                "ival = %d\n" +
                "dval = %d\n", pidVals[0], pidVals[1], pidVals[2]));
    }

    //Size of command is 4 bytes
    private double[] buildPidCommand(){
        return new double[]{command, pidVals[0], pidVals[0], pidVals[0]};
    }



    public void setPIDValsIfValid(){
        String[] pidFields = new String[]{pField.getText(), iField.getText(), dField.getText()};
        for (int i = 0; i < pidFields.length; i++){
            int tmp;
            try {
                tmp = Integer.parseInt(pidFields[i]);
            }catch (Exception e){
                showError("Fields must have a positive decimal.");
                return;
            }
            if (tmp < 0){
                showError("Fields must have a positive decimal.");
                return;
            }
            pidVals[i] = tmp;
        }
        errorLabel.setVisible(false);
        sendCommand();
    }

    private void showError(String error){
        errorLabel.setText(error);
        errorLabel.setVisible(true);
    }

    private void enableSubmitIfNeeded(){
        //btnSubmit.setDisable(!(pval >= 0 && ival >= 0 && dval >= 0));
    }

    // 'S'
    public void startSim(){
        controller.startSim();
    }

    // 'E'
    public void endSim(){
        controller.endSim();
    }

    // '0'
    public void emergencyStop(){
        controller.emergencyStop();
    }

    public void changeSetPoints(){
        command = Constants.MOVE;
    }

    public void setEditAsRoll(){
        showPIDFields();
        command = Constants.ROLL;
        commandMenu.setText("Roll");
    }

    public void setEditAsPitch(){
        showPIDFields();
        command = Constants.PITCH;
        commandMenu.setText("Pitch");
    }

    private void showPIDFields(){
        pField.setVisible(true);
        iField.setVisible(true);
        dField.setVisible(true);
    }
}
