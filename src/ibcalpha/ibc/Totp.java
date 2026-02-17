// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2021 Richard L King (rlking@aultan.com)
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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Totp {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int CODE_MODULUS = 1_000_000; // 10^CODE_DIGITS

    static String generateCode(String base32Secret) {
        byte[] key = decodeBase32(base32Secret.toUpperCase().replace(" ", ""));
        long counter = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        byte[] hash = hmacSha1(key, counter);
        int code = truncate(hash) % CODE_MODULUS;
        return String.format("%0" + CODE_DIGITS + "d", code);
    }

    private static byte[] decodeBase32(String encoded) {
        encoded = encoded.replace("=", "");
        int bitLength = encoded.length() * 5;
        byte[] result = new byte[bitLength / 8];

        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (char c : encoded.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[index++] = (byte) (buffer >> bitsLeft);
            }
        }
        return result;
    }

    private static byte[] hmacSha1(byte[] key, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
            return mac.doFinal(counterBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP generation failed", e);
        }
    }

    private static int truncate(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        return ((hash[offset] & 0x7F) << 24)
             | ((hash[offset + 1] & 0xFF) << 16)
             | ((hash[offset + 2] & 0xFF) << 8)
             | (hash[offset + 3] & 0xFF);
    }
}
