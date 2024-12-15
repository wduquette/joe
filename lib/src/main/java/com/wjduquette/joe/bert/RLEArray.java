package com.wjduquette.joe.bert;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Given an array of integers, run-length-encode it for space.
 */
class RLEArray {
    //-------------------------------------------------------------------------
    // Instance variables

    // Length of the input array.
    private final int inputLength;
    private final int[] encoded;

    //-------------------------------------------------------------------------
    // Constructor

    RLEArray(int[] input) {
        this.inputLength = input.length;

        // FIRST, handle the empty case.
        if (inputLength == 0) {
            this.encoded = new int[0];
            return;
        }

        var temp = new ArrayList<Integer>();

        for (int i = 0; i < inputLength; i++) {
            var count = 1;
            var value = input[i];
            while (i < inputLength - 1 && input[i] == input[i + 1]) {
                count++;
                i++;
            }
            temp.add(value);
            temp.add(count);
        }

        this.encoded = new int[temp.size()];
        for (var i = 0; i < temp.size(); i++) {
            encoded[i] = temp.get(i);
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    /**
     * Returns the length of the array.
     * @return The length
     */
    int length() {
        return inputLength;
    }

    /**
     * Gets the ith item from the original array.
     * @param index The index.
     * @return The item
     */
    int get(int index) {
        if (index < 0 || index >= inputLength) {
            throw new ArrayIndexOutOfBoundsException("Index out of range.");
        }

        var total = 0;
        for (int i = 0; i < encoded.length; i += 2) {
            var value = encoded[i];
            total += encoded[i + 1];
            if (index < total) {
                return value;
            }
        }

        throw new IllegalStateException(
            "Failed to find value for index " + index);
    }

    int[] encoded() {
        return Arrays.copyOf(encoded, encoded.length);
    }
}
