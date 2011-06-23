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

class Encryptor {

    /**
     * Encrypts an arbitrary string by adding (p+1) to each
     * character value at 0 based index p
     * and which is >= 32 and <= 127, wrapping
     * values 127+i to 31+i.  The benefit of this encryption
     * is that it can be done by hand, if needed, yet
     * the result can be put in a .ini file without making
     * it so easy to see the real password.
     */
    public static String encrypt(String in) {
        StringBuffer sb = new StringBuffer(in.length());

        for (int i = 0; i < in.length(); i++) {
            int code = in.charAt(i);
            code += i + 1;
            while (code > 127) {
                code = code - 127 + 31;
            }
            char c = (char) code;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * decrypts a string encrypted as in encrypt()
     */
    public static String decrypt(String in) {
        StringBuffer sb = new StringBuffer(in.length());

        for (int i = 0; i < in.length(); i++) {
            int code = in.charAt(i);
            if (code < 32 || code > 127) {
                sb.append((char) code);
                continue;
            }

            code -= (i + 1);
            while (code < 32) {
                code = code + 127 - 31;
            }
            char c = (char) code;
            sb.append(c);
        }
        return sb.toString();
    }
}
