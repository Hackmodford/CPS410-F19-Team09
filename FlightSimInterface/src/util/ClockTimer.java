package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.tools.internal.jxc.ap.Const;
import javafx.beans.property.SimpleStringProperty;
import ui.MainViewController;
import util.Constants;


public class ClockTimer {

    private SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss:S");
    private TimerTask task;
    private SimpleStringProperty retTimer;
    private long time;
    private Timer xTimer = new Timer("Metronome", true);
    private String[] splitString;
    boolean running = false;
    private MainViewController view;

        public ClockTimer() {
            retTimer = new SimpleStringProperty("00:00:00");
            view = MainViewController.getInstance();
        }

        //Threads a timer to run until running is false
        public void startTimer(final long time) {
            this.time = time;
            running = true;
            task = new TimerTask() {

                @Override
                public void run() {
                    if (!running) {
                        try {
                            task.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        updateTime();
                    }
                }
            };
            xTimer.scheduleAtFixedRate(task, 10, 10);
    }

    public synchronized void stopTimer() {
        running = false;
    }

    public synchronized void updateTime() {
        this.time = this.time + 10;
        splitString = timeFormat.format(new Date(this.time)).split(":");
        retTimer.set(splitString[0] + ":" + splitString[1] + ":" + (splitString[2].length() == 1 ? "0" + splitString[2] : splitString[2].substring(0, 2)));
        view.updateTimer(this.retTimer.toString().substring(23,28), time < Constants.MAX_TIME);
    }

    public synchronized void moveToTime(long time) {
        stopTimer();
        this.time = time;
        splitString = timeFormat.format(new Date(time)).split(":");
        retTimer.set(splitString[0] + ":" + splitString[1] + ":" + (splitString[2].length() == 1 ? "0" + splitString[2] : splitString[2].substring(0, 2)));
    }


}
