package com.wjduquette.joe.bert;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

public class RLEArrayTest extends Ted {
    @Test
    public void testEncodeDecode() {
        test("testEncodeDecode()");
        int[] input = {1, 1, 1, 2, 2, 3, 3, 3, 3, 3, 4, 5, 5, 5, 5, 7, 7, 7};

        var rle = new RLEArray(input);
        var encoded = rle.encoded();
        println("input.length   = " + input.length);
        println("encoded.length = " + encoded.length);

        println("Encoded:");
        for (var i = 0; i < encoded.length; i += 2) {
            println("   " + encoded[i] + ", " + encoded[i+1]);
        }

        check(rle.length()).eq(input.length);

        for (var i = 0; i < input.length; i++) {
            System.out.printf("[%02d] in %d out %d\n",
                i, input[i], rle.get(i));

            check(rle.get(i)).eq(input[i]);
        }

        checkThrow(() -> rle.get(-1))
            .containsString("Index out of range");
        checkThrow(() -> rle.get(input.length))
            .containsString("Index out of range");
    }
}
