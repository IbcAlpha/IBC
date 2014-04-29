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

namespace IBControllerService
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        static void Main()
        {
            ServiceBase[] ServicesToRun;
            ServicesToRun = new ServiceBase[] { new IBControllerService() };
            ServiceBase.Run(ServicesToRun);
        }
    }
}