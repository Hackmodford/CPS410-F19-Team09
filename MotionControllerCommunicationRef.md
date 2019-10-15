Commands are sent via UDP. The format is a byte array.

The first byte will always be the command byte. The following bytes are parameters.

| Name             | Size (byte) | Command | Parameters                                   | Description |
| --------------   | ----------- | ------- | ----------                                   | ----------- |
| Emergency Stop   | 1           | '0'     | N/A                                          | Flight Sim comes to a complete halt. |
| Start            | 1           | 'S'     | N/A                                          | Raises machine in air, then accepts setpoint commands to move the simulator. |
| Stop             | 1           | 'E'     | N/A                                          | Gently moves simulator to neutral position and lowers to ground. |
| Change Setpoints | 5           | 'M'     | 8-bit unsigned integer for pitch</br>8-bit unsigned integer for roll | |
| Set Pitch PID    | 24          | '1'     | 8-byte double for P</br> 8-byte double for I</br> 8-byte double for D | |
| Set Roll PID     | 24          | '2'     | 8-byte double for P</br> 8-byte double for I</br> 8-byte double for D  | |
| Set DAC Coarse Gain  | 3 | 'G' | 1 byte for "Channel"</br>1 byte for "Coarse Gain" | |
| Set DAC Fine Gain    | 3 | 'g' | 1 byte for "Channel"</br>1 byte for "Fine Gain" | |
| Set DAC Offset       | 3 | 'O' | 1 byte for "Channel"</br>1 byte for "Offset" | |
