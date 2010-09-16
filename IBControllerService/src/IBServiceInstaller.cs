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

using System.ComponentModel;
using System.ServiceProcess;
using System.Configuration.Install;

namespace IBControllerService
{
    [RunInstaller(true)]
    public partial class IBServiceInstaller : Installer
    {
        /// <summary>
        /// Installs the IBController service as the LocalSystem account with automatic startup.
        /// </summary>
        public IBServiceInstaller()
        {
            ServiceProcessInstaller process = new ServiceProcessInstaller();
            process.Account = ServiceAccount.LocalSystem;
            process.Username = null;
            process.Password = null;
            this.Installers.Add(process);

            ServiceInstaller service = new ServiceInstaller();
            service.ServiceName = IBControllerService.SvcName;
            service.DisplayName = IBControllerService.DisplayName;
            service.Description = IBControllerService.Description;
            service.StartType = ServiceStartMode.Automatic;
            this.Installers.Add(service);
        }
    }
}
