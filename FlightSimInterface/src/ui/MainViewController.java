package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import util.Constants;
import data.DataChannel;

import java.nio.ByteBuffer;

import static util.Utils.map;

public class MainViewController extends Application {

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
    @FXML public Label errorLabel;
    @FXML public Label statusLabel;
    @FXML public Label txtSetPointPitch;
    @FXML public Label txtSetPointRoll;
    @FXML public Label txtVoltagePitch;
    @FXML public Label txtVoltageRoll;
    @FXML public ImageView imgDialPitch;
    @FXML public ImageView imgDialRoll;
    @FXML public ImageView inTopLeft;
    @FXML public ImageView inTopRight;
    @FXML public ImageView inBottom;
    @FXML public ImageView inPitchHome;
    @FXML public ImageView inCanopy;
    @FXML public ImageView inStop;
    @FXML public ImageView outRaise;
    @FXML public ImageView outLower;
    @FXML public ImageView outCWPlus;
    @FXML public ImageView outCWMinus;
    @FXML public ImageView outPressure;
    @FXML public ImageView outPitchDisable;

    private final String layoutFile = "main_view.fxml";
    private final String switchOn = "assets/switch_on.png";
    private final String switchOff = "assets/switch_off.png";

    private static MainViewController instance;

    private TextField[] cmdFields;
    private DataChannel channel;
    private double[] pidValues;

    private char currentCommand;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(layoutFile));
        primaryStage.setScene(new Scene(root, 1000, 500));
        primaryStage.show();
        primaryStage.setOnCloseRequest(o -> System.exit(0));
    }

    @FXML
    public void initialize(){
        instance = this;
        channel = DataChannel.getInstance();
        cmdFields = new TextField[]{field1, field2, field3};
    }

    /**
     * Given an array of double values, we assign each value to update the view
     * in some way. Either updating a text, or rotating the dials. The indexes
     * match as follows:
     *
     * 0: Pitch Setpoint      8: kI Pitch
     * 1: Pitch Value         9: kD Pitch
     * 2: Roll Setpoint      10: kP Roll
     * 3: Roll Value         11: kI Roll
     * 4: Pitch PWM          12: kD Roll
     * 5: Roll PWM           13: inputs
     * 6: State              14: outputs
     * 7: kP Pitch
     *
     * This method is called by a background thread, so all UI updates are
     * called using Platform.runLater(), which runs it on the UIThread when
     * available.
     *
     * @param values array of doubles, used to update the UI
     */
    public void updateView(double[] values){
        double  pSetPoint =     values[0],
                pValue =        values[1],
                rSetPoint =     values[2],
                rValue =        values[3],
                pVoltage =      values[4],
                rVoltage =      values[5],
                state =         values[6];

        pidValues = new double[]{
                values[7], values[8], values[9],
                values[10], values[11], values[12]};

        boolean[] ins = extractStatus((byte)values[13]);
        boolean[] outs = extractStatus((byte)values[14]);

        Platform.runLater(() -> {

            //update set-points
            txtSetPointPitch.setText(Double.toString(pSetPoint));
            txtSetPointRoll.setText(Double.toString(rSetPoint));

            //update dials
            imgDialPitch.setRotate(pValue);
            imgDialRoll.setRotate(rValue);

            //update voltages
            txtVoltagePitch.setText(Double.toString(pVoltage));
            txtVoltageRoll.setText(Double.toString(rVoltage));

            setSwitchesStates(ins, outs);
            setSimulatorState((int)state);
        });
    }

    /**
     * Given two boolean arrays, we set the image for all states of switches
     * based on whether they are on or off.
     *
     * @param ins array of booleans representing the input switches
     * @param outs array of booleans representing the output switches
     */
    private void setSwitchesStates(boolean[] ins, boolean[] outs){
        inTopLeft.setImage(         new Image(ins[0] ? switchOn : switchOff));
        inTopRight.setImage(        new Image(ins[1] ? switchOn : switchOff));
        inBottom.setImage(          new Image(ins[2] ? switchOn : switchOff));
        inPitchHome.setImage(       new Image(ins[3] ? switchOn : switchOff));
        inCanopy.setImage(          new Image(ins[4] ? switchOn : switchOff));
        inStop.setImage(            new Image(ins[5] ? switchOn : switchOff));

        outRaise.setImage(          new Image(outs[0] ? switchOn : switchOff));
        outLower.setImage(          new Image(outs[1] ? switchOn : switchOff));
        outCWPlus.setImage(         new Image(outs[2] ? switchOn : switchOff));
        outCWMinus.setImage(        new Image(outs[3] ? switchOn : switchOff));
        outPressure.setImage(       new Image(outs[4] ? switchOn : switchOff));
        outPitchDisable.setImage(   new Image(outs[5] ? switchOn : switchOff));
    }

    /**
     * Given an integer value that represents the state of the simulator, the
     * statusLabel is updated accordingly.
     * @param state int value that represents the state fo the simulator
     */
    private void setSimulatorState(int state) {
        switch (state){
            case Constants.STATE_STOPPED:
                statusLabel.setText("Stopped");
                break;
            case Constants.STATE_STARTING:
                statusLabel.setText("Starting");
                break;
            case Constants.STATE_RUNNING:
                statusLabel.setText("Running");
                break;
            case Constants.STATE_ENDING:
                statusLabel.setText("Ending");
                break;
            case Constants.STATE_MANUAL:
                statusLabel.setText("Manual");
                break;
            case Constants.STATE_EMERGENCY_STOP:
                statusLabel.setText("Emergency Stop");
                break;
        }
    }

    /**
     * Given a byte, bits 0 -> 5 are supposed to represent the state (on/off)
     * of a switch on the simulator. Each bit is extracted and converted to
     * boolean and put in an array of booleans.
     *
     * @param bits byte to represent switch states
     * @return array of booleans matching the extracted 1s and 0s
     */
    private boolean[] extractStatus(byte bits){
        return new boolean[]{
                (bits & 1) == 1,
                ((bits >> 1) & 1) == 1,
                ((bits >> 2) & 1) == 1,
                ((bits >> 3) & 1) == 1,
                ((bits >> 4) & 1) == 1,
                ((bits >> 5) & 1) == 1
        };
    }

    /**
     * Based on what command is trying to be sent from the command board,
     * inputs are validated and a command is sent by calling channel.sendCommand().
     * Each command type has a corresponding helper method to build the command
     * accordingly.
     */
    public void sendCommand(){

        //validate input for all visible text cmdFields based on the current currentCommand
        if (inputsInvalid()) return;

        if (currentCommand == Constants.PITCH || currentCommand == Constants.ROLL){
            channel.sendCommand(buildPidCmd(), currentCommand);
        } else if (currentCommand == Constants.MOVE) {
            channel.sendCommand(buildSetPointsCmd());
        } else if (currentCommand == Constants.DAC_CG
                || currentCommand == Constants.DAC_FG
                || currentCommand == Constants.DAC_OFFSET) {
            channel.sendCommand(buildDacCmd());
        } else showError("Command type has not been set.");

    }

    /**
     * All visible TextFields in the Command Board are analyzed to see if they
     * contain valid input for the current command, using inputIsValidFor(TextField).
     * @return true if input valid, false otherwise
     */
    private boolean inputsInvalid(){
        for (TextField tf : cmdFields){
            if (!tf.isVisible()) continue;
            if (!inputIsValidFor(tf)){
                showError();
                return true;
            }
        }
        return false;
    }


    /**
     * Given a TextField, if the value within it is parsable to double,
     * we check if it's in the correct range compared to the currentCommand.
     *
     * @param tf TextField we are checking
     * @return true if input is valid, false otherwise
     */
    private boolean inputIsValidFor(TextField tf){
        double value;

        //check if text is parsable
        try {
            value = Double.parseDouble(tf.getText());
        } catch (Exception e){
            return false;
        }

        //Based on the command, we assure the value is within the corresponding range
        if (currentCommand == Constants.PITCH){
            return value <= 12600 && value >= 0;
        } else if (currentCommand == Constants.ROLL) {
            return value <= 8640 && value >= 0;
        } else if (currentCommand == Constants.MOVE) {
            return value <= 360 && value >= 0;
        } else if (currentCommand == Constants.DAC_OFFSET
                || currentCommand == Constants.DAC_CG
                || currentCommand == Constants.DAC_FG){
            return value > -128 && value < 127;
        } else {
            return false; //should be unreachable
        }
    }

    /**
     * Builds double array from values in TextFields. It is assumed
     * that the fields have already passed a validity check.
     *
     * @return values in fields in a double array of length 3
     */
    private double[] buildPidCmd(){
        double[] d =  new double[]{
                Double.parseDouble(field1.getText()),
                Double.parseDouble(field2.getText()),
                Double.parseDouble(field3.getText())};
        return d;
    }

    /**
     * Builds a command consisting of:
     *      [0]: char currentCommand
     *      [1..4]: pitch degree mapped to byte[]
     *      [5..8]: roll degree mapped to byte[]
     * To match set-point command.
     * @return constructed byte[] command
     */
    private byte[] buildSetPointsCmd(){
        int pitchDegree = Integer.parseInt(field1.getText());
        int rollDegree = Integer.parseInt(field2.getText());
        long pitchMappedVal = map(pitchDegree, 0, 360, 0, 4294967295L);
        long rollMappedVal = map(rollDegree, 0, 360, 0, 4294967295L);
        byte[] bytes = new byte[9];

        bytes[0] = (byte) currentCommand;

        //Not using Little Endian in order to mimic behavior of SimTools.
        byte[] a1 = ByteBuffer.allocate(8).putLong(pitchMappedVal).array();
        byte[] a2 = ByteBuffer.allocate(8).putLong(rollMappedVal).array();
        System.arraycopy(a1, 4, bytes, 1, 4);
        System.arraycopy(a2, 4, bytes, 5, 4);

        return bytes;
    }

    /**
     * Builds a command consisting of:
     *      [char currentCommand]
     *      [byte channel]
     *      [byte value]
     * To match any DAC-related command.
     *
     * @return the constructed byte[] command
     */
    private byte[] buildDacCmd(){
        return new byte[]{ (byte) currentCommand,
                Byte.parseByte(field1.getText()),
                Byte.parseByte(field2.getText())};
    }

    /**
     * Using the same view as an error, "Command sent" is displayed
     * to show that a command was correctly sent. Shows as black text
     * to distinguish from an error.
     */
    public void showCommandSentMessage(){
        errorLabel.setText("Command sent.");
        errorLabel.setTextFill(Paint.valueOf("black"));
        errorLabel.setVisible(true);
    }

    /**
     * Based on the current command, decides which message we want to show as
     * an error and shows the error view with the text shown as a red color.
     */
    private void showError(){
        String error;

        //set error message based on what currentCommand is currently set
        if (currentCommand == Constants.PITCH) {
            error = "Fields must have value in range [0, 12600].";
        } else if (currentCommand == Constants.ROLL) {
            error = "Fields must have value in range [0, 8640]";
        } else if (currentCommand == Constants.MOVE) {
            error = "Fields require a positive integer in range [0, 360]";
        } else if (currentCommand == Constants.DAC_OFFSET
                || currentCommand == Constants.DAC_CG
                || currentCommand == Constants.DAC_FG){
            error = "Field 1: requires an integer in range [0, 4]\n" +
                    "Field 2: requires an integer in range [-128, 127]";
        } else {
            error = "Reached unreachable error...";
        }

        errorLabel.setText(error);
        errorLabel.setTextFill(Paint.valueOf("red"));
        errorLabel.setVisible(true);
    }

    /**
     * Displays the error view with the given text. Sets it's text
     * color to red.
     *
     * @param error string we want to display as an error
     */
    public void showError(String error) {
        errorLabel.setText(error);
        errorLabel.setTextFill(Paint.valueOf("red"));
        errorLabel.setVisible(true);
    }

    /**
     * Sends a direct command to the motion controller as a
     * single char. 'S' in this case to represent START
     */
    public void startSim(){
        channel.sendCommand(Constants.START);
    }

    /**
     * Sends a direct command to the motion controller as a
     * single char. 'E' in this case to represent END
     */
    public void endSim(){
        channel.sendCommand(Constants.END);
    }

    /**
     * Sends a direct command to the motion controller as a
     * single char. '0' in this case to represent E-STOP
     */
    public void emergencyStop(){
        channel.sendCommand(Constants.EMERGENCY_STOP);
    }

    /**
     * Sets the current command as Roll PID, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdPitch(){
        currentCommand = Constants.PITCH;
        setFieldProperties("P-value", "I-value", "D-value");
        if (pidValues != null) setFieldTexts(pidValues[0], pidValues[1], pidValues[2]);
        commandMenu.setText(cmdSetPitchPid.getText());
    }

    /**
     * Sets the current command as Roll PID, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdRoll(){
        currentCommand = Constants.ROLL;
        setFieldProperties("P-value", "I-value", "D-value");
        if (pidValues != null) setFieldTexts(pidValues[3], pidValues[4], pidValues[5]);
        commandMenu.setText(cmdSetRollPid.getText());
    }

    /**
     * Sets the current command as Pitch and Roll Set-points, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdSetPoints(){
        currentCommand = Constants.MOVE;
        setFieldProperties("Pitch", "Roll");
        commandMenu.setText(cmdSetPoints.getText());
    }

    /**
     * Sets the current command as DAC Course Gain, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdDacCourseGain(){
        currentCommand = Constants.DAC_CG;
        setFieldProperties("Channel: 8-bit", "Course Gain: 8-bit");
        commandMenu.setText(cmdDacCourseGain.getText());
    }

    /**
     * Sets the current command as DAC Fine Gain, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdDacFineGain(){
        currentCommand = Constants.DAC_FG;
        setFieldProperties("Channel: 8-bit", "Fine Gain: 8-bit");
        commandMenu.setText(cmdDacFineGain.getText());
    }

    /**
     * Sets the current command as DAC offset, updates TextFields accordingly
     * with appropriate hint texts.
     */
    public void setCmdDacOffset(){
        currentCommand = Constants.DAC_OFFSET;
        setFieldProperties("Channel: 8-bit", "Offset: 8-bit");
        commandMenu.setText(cmdDacOffset.getText());
    }

    /**
     * Upon selection of a PID command for pitch or roll, the current
     * values are placed in the TextFields because those values aren't
     * available in the view
     *
     * @param v1 p-value
     * @param v2 i-value
     * @param v3 d-value
     */
    private void setFieldTexts(double v1, double v2, double v3){
        field1.setText(Double.toString(v1));
        field2.setText(Double.toString(v2));
        field3.setText(Double.toString(v3));
    }

    /**
     * Sets the hint text for field1 and field2 in the command board, hiding
     * field3 because a hint wasn't passed in for it. Error label is hidden
     * and the submit button becomes visible.
     *
     * @param prompt1 hint text for field1
     * @param prompt2 hint text for field2
     */
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

    /**
     * Sets hint text for all fields in the command board and makes them visible.
     * Error label is hidden and the submit button becomes visible.
     *
     * @param prompt1 hint text for field1
     * @param prompt2 hint text for field2
     * @param prompt3 hint text for field3
     */
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

    /**
     * Singleton method
     *
     * @return reference to the instance of the view
     */
    public static MainViewController getInstance(){
        return instance; //this will never be null
    }
}
