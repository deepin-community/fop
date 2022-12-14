/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: HexEncoderTestCase.java 1827168 2018-03-19 08:49:57Z ssteiner $ */

package org.apache.fop.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for the conversion of characters into hex-encoded strings.
 */
public class HexEncoderTestCase {

    /**
     * Tests that characters are properly encoded into hex strings.
     */
    @Test
    public void testEncodeChar() {
        char[] digits = new char[] {'0', '0', '0', '0'};
        for (int c = 0; c <= 0xFFFF; c++) {
            assertEquals(new String(digits), HexEncoder.encode((char) c));
            increment(digits);
        }
    }


    /**
     * Tests that codepoints are properly encoded into hex strings.
     */
    @Test
    public void testEncodeCodepoints() {
        char[] digits = new char[] {'0', '1', '0', '0', '0', '0'};
        for (int c = 0x10000; c <= 0x1FFFF; c++) {
            assertEquals(new String(digits), HexEncoder.encode(c));
            increment(digits);
        }
    }

    private static void increment(char[] digits) {
        int d = digits.length;
        do {
            d--;
            digits[d] = successor(digits[d]);
        } while (digits[d] == '0' && d > 0);
    }

    private static char successor(char d) {
        if (d == '9') {
            return 'A';
        } else if (d == 'F') {
            return '0';
        } else {
            return (char) (d + 1);
        }
    }

}
