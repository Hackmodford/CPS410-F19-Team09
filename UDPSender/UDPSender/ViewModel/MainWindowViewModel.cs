using System;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Reactive;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Text;
using System.Threading;
using ReactiveUI;

namespace UDPSender.ViewModel
{
    public class MainViewModel : ReactiveObject
    {
        const string manyZeros =
            "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

        double pitchAnglef = 0;
        byte[] pitchAngleb;

        double rollAnglef = 0;
        byte[] rollAngleb;

        string messageWithoutChecksum;
        string finalMessage;
        
        uint checksum;

        bool running = true;

        byte[] syncData = new byte[415];
        byte[] G2Data = new byte[415];
        
        IPEndPoint endPointConsoleRecive;
        UdpClient clientConsoleRecive;

        IPEndPoint endPointDispatchSend;
        UdpClient clientDispatchSend;

        IPEndPoint G2EndPoint;
        UdpClient G2Client;
        UdpClient G2Client2;
        IPEndPoint endPointDispatchRecive;
        
        Socket socketRecv;
        string srcIP = "127.0.0.1";
        int srcPort = 4123;
        IPAddress fromAddr;
        IPEndPoint srcPoint;
        EndPoint senderRemote;
        IPEndPoint endPoint1;

        string[] LogLines = new string[4];

        //View Model Code

        private IPAddress _consoleAddress;

        public IPAddress ConsoleAddress
        {
            
            get => _consoleAddress;
            set => this.RaiseAndSetIfChanged(ref _consoleAddress, value);
        }

        private int _consoleReceivePort;

        public int ConsoleReceivePort
        {
            get => _consoleReceivePort;
            set => this.RaiseAndSetIfChanged(ref _consoleReceivePort, value);
        }

        private int _consoleSendPort;

        public int ConsoleSendPort
        {
            get => _consoleSendPort;
            set => this.RaiseAndSetIfChanged(ref _consoleSendPort, value);
        }

        private int _dispatchSourcePort;

        public int DispatchSourcePort
        {
            get => _dispatchSourcePort;
            set => this.RaiseAndSetIfChanged(ref _dispatchSourcePort, value);
        }

        private int _dispatchDestPort;

        public int DispatchDestPort
        {
            get => _dispatchDestPort;
            set => this.RaiseAndSetIfChanged(ref _dispatchDestPort, value);
        }

        private int _g2SourcePort;

        public int G2SourcePort
        {
            get => _g2SourcePort;
            set => this.RaiseAndSetIfChanged(ref _g2SourcePort, value);
        }

        private int _g2DestPort;
        
        public int G2DestPort
        {
            get => _g2DestPort;
            set => this.RaiseAndSetIfChanged(ref _g2DestPort, value);
        }
        
        public BehaviorSubject<string> LogMessage = new BehaviorSubject<string>("");
        
        public ReactiveCommand<Unit, Unit> QuitCommand { get; }
        public ReactiveCommand<Unit, Unit> UpdateNetworkCommand { get; }
        //public ReactiveCommand<String, Unit> LogMessage { get; set; }

        public MainViewModel()
        {
            QuitCommand = ReactiveCommand.Create(() => { });
            UpdateNetworkCommand = ReactiveCommand.Create(UpdateNetwork);
            
            //Opens the config file to load the last know values, if there are none use default values
            if (File.Exists("config.txt"))
            {
                var lines = System.IO.File.ReadAllLines("config.txt");
                ConsoleAddress = IPAddress.Parse(lines[0]);
                ConsoleReceivePort = int.Parse(lines[1]);
                ConsoleSendPort = int.Parse(lines[2]);
                DispatchSourcePort = int.Parse(lines[3]);
                DispatchDestPort = int.Parse(lines[4]);
                G2SourcePort = int.Parse(lines[5]);
                G2DestPort = int.Parse(lines[6]);
            }
            else
            {
                ConsoleAddress = IPAddress.Parse("127.0.0.1");
                ConsoleReceivePort = 30100;
                ConsoleSendPort = 30120;
                DispatchSourcePort = 30420;
                DispatchDestPort = 30421;
                G2SourcePort = 30421;
                G2DestPort = 30421;
                UpdateNetwork();
            }
            
            endPoint1 = new IPEndPoint(IPAddress.Any, srcPort);
            
            endPointConsoleRecive = new IPEndPoint(IPAddress.Any, ConsoleReceivePort);
            clientConsoleRecive = new UdpClient(ConsoleReceivePort);

            endPointDispatchSend = new IPEndPoint(IPAddress.Parse("127.0.0.1"), DispatchDestPort);
            clientDispatchSend = new UdpClient(DispatchSourcePort);

            endPointDispatchRecive = new IPEndPoint(IPAddress.Loopback, DispatchSourcePort);

            G2EndPoint = new IPEndPoint(IPAddress.Parse("127.0.0.1"), G2SourcePort);
            G2Client = new UdpClient(G2SourcePort);

            clientConsoleRecive.Client.ReceiveTimeout = 1000;
            clientDispatchSend.Client.ReceiveTimeout = 1000;
            G2Client.Client.ReceiveTimeout = 1000;

            fromAddr = IPAddress.Parse(srcIP);
            srcPoint = new IPEndPoint(fromAddr, srcPort);
            senderRemote = (EndPoint)srcPoint;
            try
            {
                socketRecv = new Socket(endPoint1.Address.AddressFamily, SocketType.Dgram, ProtocolType.Udp)
                {
                    ReceiveBufferSize = 1
                };
            }
            catch (SocketException e)
            {
                LogMessage.OnNext(e.Message);
            }

            socketRecv.Bind(endPoint1);


            Thread syncPacketsThread = new Thread(SyncDis);
            syncPacketsThread.Start();

            Thread gameDataThread = new Thread(GameData);
            gameDataThread.Start();
        }


        private void SyncDis()
        {
            while (running)
            {
                if (ReciveFromConsole(endPointConsoleRecive, clientConsoleRecive, ref syncData))
                {
                    SendToDispatch(syncData, clientDispatchSend);

                    if (ReciveFromDispatch(endPointDispatchRecive, clientDispatchSend, ref syncData))
                    {
                        SendToConsole(syncData, clientConsoleRecive);
                    }
                }
            }
        }

        private void GameData()
        {
            LogLines[0] = "Did not get a packet";
            LogLines[1] = "Did Not get a packet";
            LogLines[2] = "";
            LogLines[3] = "";
            while (running)
            {
                if (ReciveFromG2(G2EndPoint, G2Client, ref G2Data))
                {
                    LogLines[0] = "got a packet";

                    //inject postions here

                    try
                    {
                        byte[] recvBuffer = new Byte[48];

                        socketRecv.ReceiveFrom(recvBuffer, ref senderRemote);
                        LogMessage.OnNext(Encoding.Default.GetString(recvBuffer));
                        string[] telemetry = Encoding.Default.GetString(recvBuffer).Split(',');
                        rollAnglef = Convert.ToDouble(telemetry[0]);
                        pitchAnglef = Convert.ToDouble(telemetry[1]);


                        rollAngleb = BitConverter.GetBytes(pitchAnglef);
                        pitchAngleb = BitConverter.GetBytes(rollAnglef);

                        string str = BitConverter.ToString(G2Data).Replace("-", "");

                        string head = "009901";
                        string head2 = "004d004400000008020201100400";
                        string mid = "02100400";

                        messageWithoutChecksum = head2 + BitConverter.ToString(rollAngleb).Replace("-", "") +
                                                 mid +
                                                 BitConverter.ToString(pitchAngleb).Replace("-", "") + manyZeros;

                        checksum = Calc_crc(StringToBinary(messageWithoutChecksum),
                            StringToBinary(messageWithoutChecksum).Length);

                        //This is garbage
                        finalMessage = head + messageWithoutChecksum +
                                       BitConverter.ToString(BitConverter.GetBytes((Int16) checksum)).Replace("-", "") +
                                       "00";


                        G2Data = StringToBinary(finalMessage);

                        SendG2(G2Data, G2Client);
                    }
                    catch (Exception e)
                    {
                        LogLines[3] = e.Message;

                        //rollAngleb = BitConverter.GetBytes(0.0);
                        //pitchAngleb = BitConverter.GetBytes(0.0);
                    }


                    LogLines[1] = "Sent a packet";
                }

                File.WriteAllLines("log.txt", LogLines);
            }
        }

        private byte[] StringToBinary(String textIn)
        {
            return Enumerable.Range(0, textIn.Length)
                .Where(x => x % 2 == 0)
                .Select(x => Convert.ToByte(textIn.Substring(x, 2), 16))
                .ToArray();
        }
        
        private void UpdateNetwork()
        {
            string[] lines = new string[7];

            lines[0] = ConsoleAddress.ToString();
            lines[1] = ConsoleReceivePort.ToString();
            lines[2] = ConsoleSendPort.ToString();
            lines[3] = DispatchSourcePort.ToString();
            lines[4] = DispatchDestPort.ToString();
            lines[5] = G2SourcePort.ToString();
            lines[6] = G2DestPort.ToString();

            File.Delete("config.txt");
            File.WriteAllLines("config.txt", lines);
        }

        private bool ReciveFromConsole(IPEndPoint endPoint, UdpClient client, ref byte[] data)
        {
            LogMessage.OnNext("Waiting for a packet from console");
            try
            {
                data = client.Receive(ref endPoint);
            }
            catch
            {
                return false;
            }


            return true;
        }

        private void SendToDispatch(byte[] data, UdpClient client)
        {
            LogMessage.OnNext("Got a packet from controller, Sending it to Dispatch");
            client.Send(data, data.Length, "127.0.0.1", DispatchDestPort);
        }

        private bool ReciveFromDispatch(IPEndPoint endPoint, UdpClient client, ref byte[] data)
        {
            LogMessage.OnNext("Waiting for a packet from Dispatch");
            try
            {
                data = client.Receive(ref endPoint);
            }
            catch
            {
                return false;
            }

            return true;
        }

        private void SendToConsole(byte[] data, UdpClient client)
        {
            LogMessage.OnNext("Got a packet from Dispatch, Sending it to Console");
            client.Send(data, data.Length, ConsoleAddress.ToString(), ConsoleSendPort);
        }

        private bool ReciveFromG2(IPEndPoint endPoint, UdpClient client, ref byte[] data)
        {
            try
            {
                data = client.Receive(ref endPoint);
            }
            catch (Exception e)
            {
                LogLines[2] = e.Message;

                return false;
            }

            return true;
        }

        private void SendG2(byte[] data, UdpClient client)
        {
            client.Send(data, data.Length, ConsoleAddress.ToString(), G2DestPort);
        }

        private uint Calc_crc(byte[] ptbuf, int num)
        {
            uint crc16 = 0xffff;
            uint temp;
            uint flag;

            for (int i = 0; i < num; i++)
            {
                temp = (uint) ptbuf[i]; // temp has the first byte 
                temp &= 0x00ff; // mask the MSB 
                crc16 = crc16 ^ temp; //crc16 XOR with temp 
                for (uint c = 0; c < 8; c++)
                {
                    flag = crc16 & 0x01; // LSBit di crc16 is mantained
                    crc16 = crc16 >> 1; // Lsbit di crc16 is lost 
                    if (flag != 0)
                        crc16 = crc16 ^ 0x0a001; // crc16 XOR with 0x0a001 
                }
            }

            //crc16 = (crc16 >> 8) | (crc16 << 8); // LSB is exchanged with MSB
            return (crc16);
        }
    }
}