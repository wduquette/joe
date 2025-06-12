package com.wjduquette.joe.clark;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.Arrays;

import static com.wjduquette.joe.checker.Checker.check;

public class RLETest extends Ted {
    private static final int[] INPUT =
        {1, 1, 1, 2, 2, 3, 3, 3, 3, 3, 4, 5, 5, 5, 5, 7, 7, 7};

    @Test
    public void testEncodeDecode() {
        test("testEncodeDecode()");

        var encoded = RLE.encode(INPUT);
        println("INPUT.length   = " + INPUT.length);
        println("encoded.length = " + encoded.length);

        println("Encoded:");
        for (var i = 0; i < encoded.length; i += 2) {
            println("   " + encoded[i] + ", " + encoded[i+1]);
        }

        var decoded = RLE.decode(encoded);
        check(Arrays.equals(INPUT, decoded)).eq(true);
    }

    @Test
    public void testGet() {
        test("testGet()");

        var encoded = RLE.encode(INPUT);
        println("INPUT.length   = " + INPUT.length);
        println("encoded.length = " + encoded.length);

        for (var i = 0; i < INPUT.length; i++) {
            var value = RLE.get(encoded, i);
            System.out.printf("[%02d] in %d out %d\n",
                i, INPUT[i], value);

            check(value).eq(INPUT[i]);
        }
    }

    @Test
    public void testEncodedLength() {
        test("testEncodedLength()");

        var encoded = RLE.encode(INPUT);
        check(RLE.encodedLength(INPUT)).eq(encoded.length);
    }

    @Test
    public void testDecodedLength() {
        test("testDecodedLength()");

        var encoded = RLE.encode(INPUT);
        check(RLE.decodedLength(encoded)).eq(INPUT.length);
    }
}
