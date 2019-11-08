package sample;


public class DataChannel extends CustomObserver {

    private MotionControllerChannel mcChannel;
    private MainViewController view;

    public DataChannel(MainViewController view){
        mcChannel = new MotionControllerChannel(this);
        mcChannel.setObserver(this);
        this.view = view;
    }

    @Override
    public void update() {
        view.updateValues(mcChannel.getDisplayValues());
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
        view.showError(s);
    }

    public void relaySuccessToView(){
        view.showCommandSentMessage();
    }

}
