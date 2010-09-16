// This file is part of the "IBControllerService".
// Copyright (C) 2010 Shane Cusson (shane.cusson@vaultic.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBControllerService is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBControllerService is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

using System.ServiceProcess;
using System.Threading;
using System.Diagnostics;
using System.Text;
using System.Net.Sockets;
using IBControllerService.Properties;
using System;

namespace IBControllerService
{
    public partial class IBControllerService : ServiceBase
    {
        public IBControllerService()
        {
            this.ServiceName = SvcName;

            this.CanHandlePowerEvent = true;
            this.CanHandleSessionChangeEvent = false;
            this.CanPauseAndContinue = false;
            this.CanShutdown = true;
            this.CanStop = true;
        }

        public static string SvcName { get { return "IBControllerSvc"; } }
        public static string DisplayName { get { return "IBController Service"; } }
        public static string Description { get { return "Starts Interactive Broker's TWS trading application. Listens on a TCP port for commands."; } }

        protected override void OnStart(string[] args)
        {
            Logger.Instance.Write("Starting IBController.");
            Thread t = new Thread(new ThreadStart(this.StartIBController));
            t.Start();
        }

        protected override void OnStop()
        {
            Logger.Instance.Write("Shutting down IBController.");
            StopIBController();
        }

        private static void StopIBController()
        {
            // Stop IBController if it's still running. IBController listens on a port
            // for three commands: STOP, EXIT, and ENABLEAPI. Here we send a STOP, then
            // an EXIT to cleanly shutdown TWS.
            try
            {
                byte[] stop = Encoding.Default.GetBytes("STOP\n");
                byte[] exit = Encoding.Default.GetBytes("EXIT\n");

                // connect to the IBController socket
                TcpClient tcp = new TcpClient(Settings.Default.IBControllerIp, Settings.Default.IBControllerPort);

                // send the "STOP" byte array to the IBController socket
                tcp.GetStream().Write(stop, 0, stop.Length);

                // send the "EXIT" command
                tcp.GetStream().Write(exit, 0, exit.Length);
            }
            catch (SocketException sockEx)
            {
                StringBuilder builder = new StringBuilder();
                builder.AppendFormat("IBController socket exeception: {0}\n", sockEx.Message);
                builder.AppendLine("Check the IP setting in IBController.ini,");
                builder.AppendLine("ensure it matches the setting in IBControllerService.config.");
                Logger.Instance.WriteError(builder.ToString());

                if (sockEx.InnerException != null)
                    Logger.Instance.WriteError("Inner Exception: {0}", sockEx.InnerException.Message);
            }
            catch (Exception ex)
            {
                Logger.Instance.WriteError(ex.Message);
            }
        }

        private void StartIBController()
        {
            // do ibcontroller startup
            Process.Start(Settings.Default.IBControllerPath);
        }
    }
}