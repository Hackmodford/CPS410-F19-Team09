package sample;

import com.sun.tools.internal.jxc.ap.Const;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainViewController extends Application {

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

    @FXML public TextField field1;
    @FXML public TextField field2;
    @FXML public TextField field3;

    @FXML public MenuItem cmdSetPitchPid;
    @FXML public MenuItem cmdSetRollPid;
    @FXML public MenuItem cmdSetPoints;
    @FXML public MenuItem cmdDacCourseGain;
    @FXML public MenuItem cmdDacFineGain;
    @FXML public MenuItem cmdDacOffset;

    @FXML public MenuButton commandMenu;

    @FXML public Button btnSubmit;
    @FXML public Button btnStart;

    @FXML public Label errorLabel;
    @FXML public Label statusLabel;
    @FXML public Label txtSetPointPitch;
    @FXML public Label txtSetPointRoll;
    @FXML public Label txtVoltagePitch;
    @FXML public Label txtVoltageRoll;

    @FXML public ImageView imgDialPitch;
    @FXML public ImageView imgDialRoll;

    private TextField[] fields;

    private DataChannel channel;
    private MainModel model;

    private char command;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    @FXML
    public void initialize(){
        initElementsIfNeeded();
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public void initElementsIfNeeded(){
        if (channel == null) channel = new DataChannel(this);
        if (model == null) model = new MainModel();
        if (fields == null) fields = new TextField[]{field1, field2, field3};
    }

    //byte legend
//0 Pitch Setpoint
//1 Pitch Value
//2 Roll Setpoint
//3 Roll Value
//4 Pitch PWM
//5 Roll PWM
//6 State
//7 kP Pitch
//8 kI Pitch
//9 kD Pitch
//10 kP Roll
//11 kI Roll
//12 kD Roll
    public void updateValues(double[] values){
        Double pSetPoint =  values[0],
                pValue =  values[1],
                rSetPoint = values[2],
                rValue = values[3],
                pPwm = values[4],
                rPwm = values[5],
                state = values[6],
                kpPitch = values[7],
                kiPitch = values[8],
                kdPitch = values[9],
                kpRoll = values[10],
                kiRoll = values[11],
                kdRoll = values[11];

        Platform.runLater(() -> {

            //update set-points
            txtSetPointPitch.setText(pSetPoint.toString());
            txtSetPointRoll.setText(rSetPoint.toString());

            //update dials
            imgDialPitch.setRotate(pValue);
            imgDialRoll.setRotate(rValue);

            //update state of simulator
            switch (state.intValue()){
                case 0:
                    statusLabel.setText("Stopped");
                    break;
                case 1:
                    statusLabel.setText("Starting");
                    break;
                case 2:
                    statusLabel.setText("Running");
                    break;
                case 3:
                    statusLabel.setText("Ending");
                    break;
                case 4:
                    statusLabel.setText("Manual");
                    break;
                case 99:
                    statusLabel.setText("Emergency Stop");
                    break;
            }
        });
    }

    public void sendCommand(){

        //validate input for all visible text fields based on the current command
        if (inputsInvalid()) return;

        if (command == Constants.PITCH || command == Constants.ROLL){
            channel.sendCommand(buildPidCmd(), command);
        } else if (command == Constants.MOVE) {
            channel.sendCommand(buildSetPointsCmd());
        } else if (command == Constants.DAC_CG
                || command == Constants.DAC_FG
                || command == Constants.DAC_OFFSET) {
            channel.sendCommand(buildDacCmd());
        } else showError("Failed command type has not been set.");

    }

    private boolean inputsInvalid(){
        for (TextField tf : fields){
            if (!tf.isVisible()) continue;
            if (!inputIsValidFor(tf)){
                showError();
                return true;
            }
        }
        return false;
    }

    private boolean inputIsValidFor(TextField tf){

        double value;
        try {
            value = Double.parseDouble(tf.getText());
        } catch (Exception e){
            return false;
        }
        if (command == Constants.ROLL || command == Constants.PITCH) {
            return value >= 0.0;
        } else if (command == Constants.MOVE) {
            return value <= 360 && value >= 0;
        } else if (command == Constants.DAC_OFFSET
                || command == Constants.DAC_CG
                || command == Constants.DAC_FG){
            return value > -128 && value < 127;
        } else {
            return false;
        }
    }

    //Size of command is 4 bytes
    private double[] buildPidCmd(){
        double[] d =  new double[]{
                Double.parseDouble(field1.getText()),
                Double.parseDouble(field2.getText()),
                Double.parseDouble(field3.getText())};
        System.out.println(String.format("P: %s, I: %s, D: %s", d[0], d[1], d[2]));
        return d;
    }

    private byte[] buildSetPointsCmd(){
        int pitchDegree = Integer.parseInt(field1.getText());
        int rollDegree = Integer.parseInt(field2.getText());
        long pitchMappedVal = Utils.map(pitchDegree, 0, 360, 0, 4294967295L);
        long rollMappedVal = Utils.map(rollDegree, 0, 360, 0, 4294967295L);

        byte[] bytes = new byte[9];
        bytes[0] = (byte)command;
        byte[] a1 =
                ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(pitchMappedVal).array();
        byte[] a2 =
                ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(rollMappedVal).array();
        System.arraycopy(a1, 0, bytes, 1, 4);
        System.arraycopy(a2, 0, bytes, 5, 4);
//        for (int i = 0; i < a1.length; i++){
//            bytes[i+1] = a1[i];
//        }
//        for (int i = 0; i < a2.length; i++){
//            bytes[i+1+a1.length] = a2[i];
//        }
        return bytes;
    }

    private byte[] buildDacCmd(){
        return new byte[]{ (byte) command,
                Byte.parseByte(field1.getText()),
                Byte.parseByte(field2.getText())};
    }

    public void showCommandSentMessage(){
        errorLabel.setText("Command sent.");
        errorLabel.setTextFill(Paint.valueOf("black"));
        errorLabel.setVisible(true);
    }

    private void showError(){
        String error;

        //set error message based on what command is currently set
        if (command == Constants.ROLL || command == Constants.PITCH) {
            error = "Fields must have a positive 64-bit decimal.";
        } else if (command == Constants.MOVE) {
            error = "Fields require a positive integer <= 255";
        } else if (command == Constants.DAC_OFFSET
                || command == Constants.DAC_CG
                || command == Constants.DAC_FG){
            error = "Field 1: requires an integer in range [0, 4]\n" +
                    "Field 2: requires an integer in range [-128, 127]";
        } else {
            //should be unreachable
            error = "What happened?";
        }

        errorLabel.setText(error);
        errorLabel.setTextFill(Paint.valueOf("red"));
        errorLabel.setVisible(true);
    }

    public void showError(String error) {
        errorLabel.setText(error);
        errorLabel.setTextFill(Paint.valueOf("red"));
        errorLabel.setVisible(true);
    }

    // 'S'
    public void startSim(){
        channel.sendCommand(Constants.START);
    }

    // 'E'
    public void endSim(){
        channel.sendCommand(Constants.END);
    }

    // '0'
    public void emergencyStop(){
        channel.sendCommand(Constants.EMERGENCY_STOP);
    }

    public void setCmdRoll(){
        command = Constants.ROLL;
        setFieldProperties("P-value", "I-value", "D-value");
        commandMenu.setText(cmdSetRollPid.getText());
    }

    public void setCmdPitch(){
        command = Constants.PITCH;
        setFieldProperties("P-value", "I-value", "D-value");
        commandMenu.setText(cmdSetPitchPid.getText());
    }


    public void setCmdSetPoints(){
        command = Constants.MOVE;
        setFieldProperties("Pitch", "Roll");
        commandMenu.setText(cmdSetPoints.getText());
    }

    public void setCmdDacCourseGain(){
        command = Constants.DAC_CG;
        setFieldProperties("Channel: 8-bit", "Course Gain: 8-bit");
        commandMenu.setText(cmdDacCourseGain.getText());
    }

    public void setCmdDacFineGain(){
        command = Constants.DAC_FG;
        setFieldProperties("Channel: 8-bit", "Fine Gain: 8-bit");
        commandMenu.setText(cmdDacFineGain.getText());
    }

    public void setCmdDacOffset(){
        command = Constants.DAC_OFFSET;
        setFieldProperties("Channel: 8-bit", "Offset: 8-bit");
        commandMenu.setText(cmdDacOffset.getText());
    }

    private void setFieldProperties(String prompt1, String prompt2){
        errorLabel.setVisible(false);
        field1.setPromptText(prompt1);
        field2.setPromptText(prompt2);
        field3.setPromptText("");
        field1.setVisible(true);
        field2.setVisible(true);
        field3.setVisible(false);
        btnSubmit.setVisible(true);
    }

    private void setFieldProperties(String prompt1, String prompt2, String prompt3){
        errorLabel.setVisible(false);
        field1.setPromptText(prompt1);
        field2.setPromptText(prompt2);
        field3.setPromptText(prompt3);
        field1.setVisible(true);
        field2.setVisible(true);
        field3.setVisible(true);
        btnSubmit.setVisible(true);
    }
}
