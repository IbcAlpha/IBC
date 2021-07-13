// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2018 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBC is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBC.  If not, see <http://www.gnu.org/licenses/>.

package ibcalpha.ibc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static ibcalpha.ibc.IbcTws.checkArguments;

public class IbcStop {
    public static void main(String[] args) throws Exception {
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        }
        checkArguments(args);
        setupDefaultEnvironment(args);
        sendStopCommand();
    }

    static void setupDefaultEnvironment(final String[] args) {
        IbcTws.printVersionInfo();
        Settings.initialise(new DefaultSettings(args));
    }

    static void sendStopCommand() throws IOException {
        final int port = Settings.settings().getInt("CommandServerPort", 0);
        if (port == 0) {
            Utils.logError("CommandServer is not started because the port is not configured");
            return;
        }
        InetSocketAddress address = new InetSocketAddress(getInetAddress(Settings.settings().getString("BindAddress", "")), port);
        Utils.logToConsole("Connecting to CommandServer: " + address);
        try (Socket socket = new Socket()) {
            socket.connect(address);
            Utils.logToConsole("Connected to CommandServer: " + socket.getLocalSocketAddress() + "=>" + socket.getRemoteSocketAddress());
            try (OutputStream outputStream = socket.getOutputStream()) {
                outputStream.write("STOP\n".getBytes(StandardCharsets.US_ASCII));
            }
        }
        Utils.logToConsole("STOP command is sent successfully");
    }

    static InetAddress getInetAddress(String bindaddr) {
        InetAddress result = InetAddress.getLoopbackAddress();
        if (!bindaddr.isEmpty()) {
            try {
                InetAddress candidate = InetAddress.getByName(bindaddr);
                if (!candidate.isAnyLocalAddress()) {
                    result = candidate;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
