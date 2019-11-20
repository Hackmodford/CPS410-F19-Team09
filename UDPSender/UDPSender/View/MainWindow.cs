using System;
using System.Net;
using System.Reactive.Concurrency;
using System.Reactive.Disposables;
using System.Reactive.Linq;
using NStack;
using ReactiveUI;
using Terminal.Gui;
using UDPSender.ViewModel;

namespace UDPSender.View
{
    public class MainWindow : Window, IViewFor<MainViewModel>
    {
        public MainViewModel ViewModel { get; set; }
        object IViewFor.ViewModel
        {
            get => ViewModel;
            set => ViewModel = (MainViewModel) value;
        }

        private TextField textFieldConsoleIp;
        
        public MainWindow() : base("UDPSender")
        {

            const int marginL = 3;
            const int marginLText = 25;
            const int widthTextBox = 20;
            
            ViewModel = new MainViewModel();
            
            // By using Dim.Fill(), it will automatically resize without manual intervention
            Width = Dim.Fill();
            Height = Dim.Fill();
            X = 0;
            Y = 0;
            
            var labelConsoleIp = new Label("Console IP: ")
            {
                X = marginL,
                Y = 1
            };
            textFieldConsoleIp = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelConsoleIp),
                Width = widthTextBox
            };
            
            var labelConsoleReceivePort = new Label("Console Receive Port: ")
            {
                X = marginL,
                Y = 3
            };
            var textFieldConsolePort = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelConsoleReceivePort),
                Width = widthTextBox
            };
            var labelConsoleSendPort = new Label("Console Send Port: ")
            {
                X = marginL,
                Y = Pos.Bottom(labelConsoleReceivePort)
            };
            var textFieldConsoleSendPort = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelConsoleSendPort),
                Width = widthTextBox
            };
            
            var labelDispatchPortSource = new Label("Dispatch Port Source: ")
            {
                X = marginL,
                Y = 6
            };
            var textFieldDispatchPortSource = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelDispatchPortSource),
                Width = widthTextBox
            };
            var labelDispatchPortDest = new Label("Dispatch Port Dest: ")
            {
                X = marginL,
                Y = Pos.Bottom(labelDispatchPortSource)
            };
            var textFieldDispatchPortDest = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelDispatchPortDest),
                Width = widthTextBox
            };
            
            var labelG2Source = new Label("G2 Source: ")
            {
                X = marginL,
                Y = 9
            };
            var textFieldG2Source = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelG2Source),
                Width = widthTextBox
            };
            var labelG2Dest = new Label("G2 Dest: ")
            {
                X = marginL,
                Y = Pos.Bottom(labelG2Source)
            };
            var textFieldG2Dest = new TextField("")
            {
                X = marginLText,
                Y = Pos.Top(labelG2Dest),
                Width = widthTextBox
            };
            
            var buttonUpdateNetwork = new Button("Update Network")
            {
                X = marginL,
                Y = 12,
                Clicked = () =>
                {
                    try
                    {
                        ViewModel.ConsoleAddress = IPAddress.Parse(textFieldConsoleIp.Text.ToString());

                        ViewModel.ConsoleReceivePort = int.Parse(textFieldConsolePort.Text.ToString());
                        ViewModel.ConsoleSendPort = int.Parse(textFieldConsoleSendPort.Text.ToString());

                        ViewModel.DispatchSourcePort = int.Parse(textFieldDispatchPortSource.Text.ToString());
                        ViewModel.DispatchDestPort = int.Parse(textFieldDispatchPortDest.Text.ToString());

                        ViewModel.G2SourcePort = int.Parse(textFieldG2Source.Text.ToString());
                        ViewModel.G2DestPort = int.Parse(textFieldG2Dest.Text.ToString());

                        ViewModel.UpdateNetworkCommand.Execute().Subscribe();
                    }
                    catch(Exception e)
                    {
                        Console.WriteLine(e.ToString());
                    }
                }
            };
            var buttonQuit = new Button("Quit")
            {
                X = Pos.Right(buttonUpdateNetwork) + 4,
                Y = 12,
                Clicked = () =>
                {
                    Running = false;
                    Console.Clear();
                    Environment.Exit(0);
                }
            };
            
            var textBox = new Label("")
            {
                X = marginL,
                Y = 14,
                Width = 40,
                Height = 5
            };
            
            ViewModel.LogMessage.Subscribe(s => { textBox.Text = textBox.Text + DateTime.Now.ToString("HH:mm:ss.ff ") + s + "\n"; });
            
            this.WhenAnyValue(v => v.ViewModel.ConsoleAddress)
                .Select(address => address.ToString())
                .Subscribe(s => textFieldConsoleIp.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.ConsoleReceivePort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldConsolePort.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.ConsoleSendPort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldConsoleSendPort.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.DispatchSourcePort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldDispatchPortSource.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.DispatchDestPort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldDispatchPortDest.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.G2SourcePort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldG2Source.Text = s);
            
            this.WhenAnyValue(v => v.ViewModel.G2DestPort)
                .Select(port => port.ToString())
                .Subscribe(s => textFieldG2Dest.Text = s);
            
            Add(
                labelConsoleIp,
                textFieldConsoleIp,
                labelConsoleReceivePort,
                textFieldConsolePort,
                labelConsoleSendPort,
                textFieldConsoleSendPort,
                labelDispatchPortSource,
                textFieldDispatchPortSource,
                labelDispatchPortDest,
                textFieldDispatchPortDest,
                labelG2Source,
                textFieldG2Source,
                labelG2Dest,
                textFieldG2Dest,
                buttonUpdateNetwork,
                buttonQuit,
                textBox
            );
        }
    }
}