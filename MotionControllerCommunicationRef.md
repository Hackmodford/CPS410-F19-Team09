# Arduino Commands
Commands are sent as a byte array via UDP. The Arduino listens on IP 192.168.1.5 port 8888. The first byte will always be the command byte. All commands are little-endian unless specified otherwise.

### Commands

| Name             | Size (byte) | Command | Parameters                                   | Description |
| --------------   | ----------- | ------- | ----------------                                   | ----------- |
| Emergency Stop   | 1           | '0'     | N/A                                          | Flight Sim comes to a complete halt. |
| Start            | 1           | 'S'     | N/A                                          | Raises machine in air, then accepts setpoint commands to move the simulator. |
| Stop             | 1           | 'E'     | N/A                                          | Gently moves simulator to neutral position and lowers to ground. |
| Change Setpoints | 5           | 'M'     | 4 byte unsigned integer for pitch (big-endian)</br>4 byte unsigned integer for roll (big-endian) | |
| Set Pitch PID    | 24          | '1'     | 8-byte double for P</br> 8-byte double for I</br> 8-byte double for D | |
| Set Roll PID     | 24          | '2'     | 8-byte double for P</br> 8-byte double for I</br> 8-byte double for D  | |
| Set DAC Coarse Gain  | 3 | 'G' | 1 byte for "Channel"</br>1 byte for "Coarse Gain" | |
| Set DAC Fine Gain    | 3 | 'g' | 1 byte for "Channel"</br>1 byte for "Fine Gain" | |
| Set DAC Offset       | 3 | 'O' | 1 byte for "Channel"</br>1 byte for "Offset" | |

### Parameter Descriptions
| Name | Details         | Datatype |
|------|-----------------| -------- |
| Channel | The channel of the DAC. </br>0 = Pitch</br>1 = Roll</br>2 = TBD</br>3 = TBD</br>4 = All | byte |
| Coarse Gain | 0 = ±10V</br>1 = ±10.2564V</br>2 = ±10.5263 | byte |
| Fine Gain | [-32, 31] | 2's complement byte |
| Offset | DAC Offset | 2's complement byte |

# Arduino State Output

The arduino reports state information multiple times per second via UDP. It broadcasts to IP 192.168.1.6 with port 8888. The format of the byte array is specified below. The report is little-endian.

| Index | Size (byte) | Format  | Description                                             |
| ----- | ----------- | ------- | ------------------------------------------------------- |
| 0     | 2           | integer | The pitch setpoint. [0, 12600]  |
| 2     | 2           | integer | The pitch position. [0, 12600]  |
| 4     | 2           | integer | The roll setpoint. [0, 8640]    |
| 6     | 2           | integer | The roll position. [0, 8640]     |
| 8     | 2           | integer | The pitch voltage. Min Int value represents -10V. Max Int value represents +10V |
| 10    | 2           | integer | The roll voltage. Min Int value represents -10V. Max Int value represents +10V |
| 12    | 1           | byte    | The current state of the Arduino. (See state chart) |
| 13    | 8           | double  | Pitch PID Controller's P value |
| 21    | 8           | double  | Pitch PID Controller's I value |
| 29    | 8           | double  | Pitch PID Controller's D value |
| 37    | 8           | double  | Roll PID Controller's P value |
| 45    | 8           | double  | Roll PID Controller's I value |
| 53    | 8           | double  | Roll PID Controller's D value |
| 61    | 1           | byte    | Byte that has states of all input pins (See I/O Chart) |
| 62    | 1           | byte    | Byte that has states of all output pins (See I/O Chart) |

## State Chart
| Value | State          |
| ----- | -------------- |
| 0     | Stopped        |
| 1     | Starting       |
| 2     | Running        |
| 3     | Ending         |
| 4     | Manual         |
| 99    | Emergency Stop |


## I/O Chart
The arduino reports all input states in a single byte and all outputs in a single byte.
Specific bits within the byte are used to represent the state of the I/O. Indexes starting with LSB.

Input Chart

| Index | Input Name                   |
| ----- | ---------------------------- |
| 0     | Top Limit Switch A           |
| 1     | Top Limit Switch B           |
| 2     | Bottom Limit Switch          |
| 3     | Pitch Home Set Switch        |
| 4     | Canopy (Open/Closed)         |
| 5     | Stop/Seatbelts (Open/Closed) |

Output Chart

| Index | Input Name             |
| ----- | ---------------------- |
| 0     | Raise                  |
| 1     | Lower                  |
| 2     | Increase Counterweight |
| 3     | Decrease Counterweight |
| 4     | Pressure               |
| 5     | Pitch Disable          |
