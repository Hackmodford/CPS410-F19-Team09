package util;

public class Constants {
    public static final int CHANNEL_A = 0;
    public static final int CHANNEL_B = 1;
    public static final int CHANNEL_C = 2;
    public static final int CHANNEL_D = 3;
    public static final int CHANNEL_ALL = 3;

    //Course Gain = 'G'
    public static final int LOW = 0;
    public static final int MEDIUM = 1;
    public static final int HIGH = 2;

    // Fine Gain 'g'
    // {g, CHANNEL_A, 5}
    //                ^
    // in range [-32, 31]

    //Offset 'O'
    //signed char


    //states
    public static final byte STATE_STOPPED = 0;
    public static final byte STATE_STARTING = 1;
    public static final byte STATE_RUNNING = 2;
    public static final byte STATE_ENDING = 3;
    public static final byte STATE_EMERGENCY_STOP = 99;
    public static final byte STATE_MANUAL = 4;

    //commands
    public static final char START = 'S';
    public static final char END = 'E';
    public static final char EMERGENCY_STOP = '0';
    public static final char MOVE = 'M'; //16 bit pitch, 16 bit roll (split to chars)
    public static final char PITCH = '1'; //3 doubles (P, I, D)
    public static final char ROLL = '2'; //3 doubles (P, I, D)

    public static final char DAC_CG = 'G';
    public static final char DAC_FG = 'g';
    public static final char DAC_OFFSET = 'O';






}
