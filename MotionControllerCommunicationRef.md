# Arduino Commands
Commands are sent as a byte array via UDP.

Arduino listens on IP 192.168.1.5 port 8888.

The first byte will always be the command byte.

### Commands

| Name             | Size (byte) | Command | Parameters                                   | Description |
| --------------   | ----------- | ------- | ----------------                                   | ----------- |
| Emergency Stop   | 1           | '0'     | N/A                                          | Flight Sim comes to a complete halt. |
| Start            | 1           | 'S'     | N/A                                          | Raises machine in air, then accepts setpoint commands to move the simulator. |
| Stop             | 1           | 'E'     | N/A                                          | Gently moves simulator to neutral position and lowers to ground. |
| Change Setpoints | 5           | 'M'     | 4 byte unsigned integer for pitch</br>4 byte unsigned integer for roll | |
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
