package sample;


public class MainController extends CustomObserver {

    private ControllerChannel channel;
    private MainView view;

    public MainController(MainView view){
        channel = new ControllerChannel(this);
        channel.addObserver(this);
        this.view = view;
    }

    @Override
    public void update() {
        //view.submitNewConfig(channel.getValues());
    }

    public void sendCommand(double[] command){

    }

    public void sendCommand(byte[] command){

    }


    public void startSim(){
        channel.sendCommand(Constants.START);
    }

    public void endSim(){
        channel.sendCommand(Constants.END);
    }

    public void emergencyStop(){
        channel.sendCommand(Constants.EMERGENCY_STOP);
    }

    public void submitNewValues(){

    }

}
