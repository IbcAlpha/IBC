// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

class IBControllerServer
        implements Runnable {

    private ServerSocket mSocket = null;
    private volatile boolean mQuitting = false;
    
    private final boolean isGateway;
    


    IBControllerServer(boolean isGateway) {
        this.isGateway = isGateway;
    }

    @Override public void run() {
        Thread.currentThread().setName("IBControllerServer");
        Utils.logToConsole("IBControllerServer is started.");

        if (! createSocket()) {
            return;
        }

        for (; !mQuitting;) {
            Socket socket = getClient();

            if (socket != null) MyCachedThreadPool.getInstance().execute(new CommandDispatcher(new CommandChannel(socket), isGateway));
        }

        try {
            mSocket.close();
        } catch (Exception e) {
        }

        Utils.logToConsole("IBControllerServer is shutdown");
    }

    public void shutdown() {
        mQuitting = true;
    }

    private boolean createSocket() {
        int port = Settings.settings().getInt("IbControllerPort", 7462);
        int backlog = 5;
        String bindaddr = null;
        try {
            bindaddr = Settings.settings().getString("IbBindAddress", "");
            if (bindaddr != null && bindaddr.length() > 0) {
                mSocket = new ServerSocket(port,
                                            backlog,
                                            InetAddress.getByName(bindaddr));
                Utils.logToConsole("IBControllerServer listening on address: " +
                                   bindaddr + " port: " +
                                   java.lang.String.valueOf(port));
            } else {
                mSocket = new ServerSocket(port, backlog);
                Utils.logToConsole("IBControllerServer listening on addresses: " +
                                   getAddresses() + "; port: " +
                                   java.lang.String.valueOf(port));
            }
        } catch (IOException e) {
            Utils.logError("exception:\n" + e.toString());
            Utils.logToConsole("IBControllerServer failed to create socket");
            mSocket = null;
            mQuitting = true;
            return false;
        }
        return true;
    }

    private Socket getClient() {
        Socket socket;
        try {
            socket = mSocket.accept();

            String allowedAddresses =
                    Settings.settings().getString("IbControlFrom", "");

            if (!socket.getInetAddress().getHostAddress().equals(mSocket.getInetAddress().getHostAddress()) &&
                    !socket.getInetAddress().getHostAddress().equals(InetAddress.getLoopbackAddress().getHostAddress()) &&
                    !allowedAddresses.contains(socket.getInetAddress().getHostAddress()) &&
                    !allowedAddresses.contains(socket.getInetAddress().getHostName())) {
                Utils.logToConsole("IBControllerServer denied access to: " +
                                    socket.getInetAddress().toString());
                socket.close();
                return null;
            }

            Utils.logToConsole("IBControllerServer accepted connection from: " + socket.getInetAddress().getHostAddress());
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getAddresses() {
        List<String> addressList = getAddressList();
        String s = addressList.isEmpty() ? "" : addressList.get(0);
        for (int i = 1; i < addressList.size(); i++) {
            s = s + "," + addressList.get(i);
        }
        return s;
    }
    
    private List<String> getAddressList() {
        List<String> addressList = new ArrayList<>(); 
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    addressList.add(address.getHostAddress());
                }
            }
        } catch (SocketException e) {
            Utils.logToConsole("SocketException occurred while enumerating network interfaces");
        }
        return addressList;
    }
}
