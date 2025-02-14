package com.wjduquette.joe.bert;

/**
 * Run-Length Encoding.
 */
public class RLE {
    private RLE() {} // not instantiable

    //-------------------------------------------------------------------------
    // Static Methods

    /**
     * Computes the length of the encoded version of the input array.
     * @param input The input
     * @return The length.
     */
    public static int encodedLength(int[] input) {
        int length = 0;
        for (int i = 0; i < input.length; i++) {
            while (i < input.length - 1 && input[i] == input[i + 1]) {
                i++;
            }
            length += 2;
        }

        return length;
    }

    /**
     * Encodes the input as an RLE array.
     * @param input The input array
     * @return The encoded array.
     */
    public static int[] encode(int[] input) {
        var encoded = new int[encodedLength(input)];

        int j = 0;
        for (int i = 0; i < input.length; i++) {
            var count = 1;
            var value = input[i];
            while (i < input.length - 1 && input[i] == input[i + 1]) {
                count++;
                i++;
            }
            encoded[j++] = value;
            encoded[j++] = count;
        }

        return encoded;
    }

    /**
     * Decodes a run-length encoded array of integers.
     * @param encoded The encoded array
     * @return The decoded array.
     */
    public static int[] decode(int[] encoded) {
        var decoded = new int[decodedLength(encoded)];

        var j = 0;
        for (int i = 0; i < encoded.length; i += 2) {
            var value = encoded[i];
            var count = encoded[i + 1];
            while (count > 0) {
                decoded[j++] = value;
                count--;
            }
        }

        return decoded;
    }

    /**
     * Computes the decoded length of an encoded array.
     * @param encoded The encoded array.
     * @return The length.
     */
    public static int decodedLength(int[] encoded) {
        var total = 0;
        for (int i = 0; i < encoded.length; i += 2) {
            total += encoded[i + 1];
        }

        return total;
    }

    /**
     * Given an RLE-encoded array, returns the index-th item in the
     * decoded array.  Returns the first value if the index is
     * less than 0 and the last value if the index is greater than or
     * equal to the decoded length.
     * @param encoded The RLE-encoded array
     * @param index The input array index
     * @return The value
     */
    public static int get(int[] encoded, int index) {
        if (encoded.length == 0) {
            throw new IllegalArgumentException("Encoded array is empty.");
        }

        var total = 0;

        if (index < 0) {
            return encoded[0];
        }

        for (int i = 0; i < encoded.length; i += 2) {
            var value = encoded[i];
            total += encoded[i + 1];
            if (index < total) {
                return value;
            }
        }

        return encoded[encoded.length - 2];
    }
}
