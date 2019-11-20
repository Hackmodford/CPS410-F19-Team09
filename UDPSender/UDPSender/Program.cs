using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net.Sockets;
using System.Net;
using System.IO;
using System.Timers;
using System.Threading;
using Terminal.Gui;
using UDPSender.View;

namespace UDPSender
{
    class Program
    {
        static void Main()
        {
            Application.Init();
            Application.Run(new MainWindow());
        }
    }
}