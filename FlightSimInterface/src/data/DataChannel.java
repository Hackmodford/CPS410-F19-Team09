package data;


import ui.MainViewController;
import util.Observer;

public class DataChannel extends Observer {

    private MotionControllerChannel mcChannel;
    private MainViewController viewController;
    private static DataChannel instance;

    private DataChannel(){
        mcChannel = MotionControllerChannel.getInstance();
        viewController = MainViewController.getInstance();
        mcChannel.setObserver(this);
        if (!mcChannel.isAlive()) mcChannel.start();
    }

    @Override
    public void update() {
        viewController.updateView(mcChannel.getDisplayValues());
    }

    public void sendCommand(double[] values, char cmd){
        mcChannel.sendCommand(values, cmd);
    }

    public void sendCommand(byte[] command) {
        mcChannel.sendCommand(command);
    }

    public void sendCommand(char c) {
        mcChannel.sendCommand(c);
    }

    public void relayConnectionLost(String s){
        viewController.showError(s);
    }

    public void relaySuccessToView(){
        viewController.showCommandSentMessage();
    }

    public static DataChannel getInstance(){
        if (instance == null) instance = new DataChannel();
        return instance;
    }

}
