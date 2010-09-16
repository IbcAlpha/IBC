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

using System.Diagnostics;

namespace IBControllerService
{
    public class Logger
    {
        EventLog log;

        public Logger()
        {
            if (!System.Diagnostics.EventLog.SourceExists(IBControllerService.SvcName))
            {
                System.Diagnostics.EventLog.CreateEventSource(IBControllerService.DisplayName, IBControllerService.DisplayName);
            }
            log = new EventLog();
            log.Source = IBControllerService.DisplayName;
            log.Log = IBControllerService.DisplayName;
        }

        // returns a reference to a singleton logger
        public static Logger Instance
        {
            get
            {
                return Nested.instance;
            }
        }

        class Nested
        {
            // Singleton implementation based on http://www.yoda.arachsys.com/csharp/singleton.html
            // Explicit static constructor to tell C# compiler not to mark type as beforefieldinit
            static Nested()
            {
            }

            internal static readonly Logger instance = new Logger();
        }


        /// <summary>
        /// Writes an entry to the "IBController" event log.
        /// </summary>
        /// <param name="message">Text to write to the event log. Can contain {0},{1},etc.</param>
        /// <param name="args">Arguments to include in message.</param>
        public void Write(string message, params object[] args)
        {
            log.WriteEntry(string.Format(message, args), EventLogEntryType.Information);
        }


        /// <summary>
        /// Writes an error entry to the "IBController" event log.
        /// </summary>
        /// <param name="message">Text to write to the event log. Can contain {0},{1},etc.</param>
        /// <param name="args">Arguments to include in message.</param>
        public void WriteError(string message, params object[] args)
        {
            log.WriteEntry(string.Format(message, args), EventLogEntryType.Error);
        }
    }
}
