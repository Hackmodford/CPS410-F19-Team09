package sample;

import java.util.ArrayList;

public class MainModel {
    private ArrayList<CustomObserver> observers = new ArrayList<>();
    private MCListener listener;
    private int[] values;

    public MainModel(){
        listener = new MCListener(this);

    }

    public void setValues(int[] data){
        this.values = data;
        notifyObservers();
    }

    public int[] getValues(){
        return values;
    }

    public void addObserver(CustomObserver observer){
        observers.add(observer);
    }

    public void notifyObservers(){
        for(CustomObserver o : observers){
            o.update();
        }
    }
}
