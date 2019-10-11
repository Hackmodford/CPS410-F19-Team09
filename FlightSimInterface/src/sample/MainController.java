package sample;


public class MainController extends CustomObserver {

    private MainModel model;
    private MainView view;

    public MainController(MainView view){
        model = new MainModel();
        model.addObserver(this);
    }

    @Override
    public void update() {
        view.updateValues(model.getValues());
    }


    public void startSim(){

    }

    public void endSim(){

    }

    public void beginEditConfig(){

    }

}
