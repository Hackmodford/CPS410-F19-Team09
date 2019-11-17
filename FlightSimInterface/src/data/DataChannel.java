package data;


import ui.MainViewController;
import util.CustomObserver;

public class DataChannel extends CustomObserver {

    private MotionControllerChannel mcChannel;
    private MainViewController viewController;

    public DataChannel(MainViewController viewController){
        mcChannel = new MotionControllerChannel(this);
        mcChannel.setObserver(this);
        this.viewController = viewController;
        mcChannel.start();
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

    public void relayErrorToView(String s){
        viewController.showError(s);
    }

    public void relaySuccessToView(){
        viewController.showCommandSentMessage();
    }

}
