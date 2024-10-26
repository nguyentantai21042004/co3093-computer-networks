package Peer.src.utils;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// This class decodes B-encoded data from an InputStream.
public class BDecoder {
    // The InputStream to BDecode.
    private final InputStream in;

    // Holds the last read indicator (used to identify type in B-encoding).
    private int indicator = 0;

    // Constructor for the BDecoder, initializing the InputStream.
    public BDecoder(InputStream in) {
        this.in = in;
    }

    // Decodes a B-encoded input stream and returns the decoded BEValue.
    public static BEValue bdecode(InputStream in) throws Exception {
        return new BDecoder(in).bdecode();
    }

    // Decodes a B-encoded byte buffer and returns the decoded BEValue.
    public static BEValue bdecode(ByteBuffer data) throws Exception {
        return BDecoder.bdecode(new ByteArrayInputStream(data.array()));
    }

    // Returns the next indicator from the InputStream or -1 if the end of the stream is reached.
    private int getNextIndicator() throws IOException {
        if (this.indicator == 0) {
            this.indicator = in.read();  // Read the next byte from the InputStream
        }
        return this.indicator;
    }

    // Decodes the next B-encoded value from the stream.
    public BEValue bdecode() throws Exception {
        if (this.getNextIndicator() == -1)
            return null;  // End of stream, return null

        if (this.indicator >= '0' && this.indicator <= '9')
            return this.bdecodeBytes();  // Decode as a byte array
        else if (this.indicator == 'i')
            return this.bdecodeNumber();  // Decode as a number
        else if (this.indicator == 'l')
            return this.bdecodeList();  // Decode as a list
        else if (this.indicator == 'd')
            return this.bdecodeMap();  // Decode as a map (dictionary)
        else
            throw new Exception("Unknown indicator '" + this.indicator + "'");
    }

    // Decodes the next B-encoded value from the stream as a byte array.
    public BEValue bdecodeBytes() throws Exception {
        int c = this.getNextIndicator();  // Get the first digit of the length
        int num = c - '0';  // Convert char to int
        if (num < 0 || num > 9)
            throw new Exception("Number expected, not '" + (char) c + "'");
        this.indicator = 0;  // Reset the indicator

        // Read the rest of the length
        c = this.read();
        int i = c - '0';
        while (i >= 0 && i <= 9) {
            num = num * 10 + i;  // Build the full length
            c = this.read();
            i = c - '0';
        }

        if (c != ':') {
            throw new Exception("Colon expected, not '" + (char) c + "'");
        }

        return new BEValue(read(num));  // Return the decoded byte array
    }

    // Decodes the next B-encoded value from the stream as a number.
    public BEValue bdecodeNumber() throws Exception {
        int c = this.getNextIndicator();
        if (c != 'i') {
            throw new Exception("Expected 'i', not '" + (char) c + "'");
        }
        this.indicator = 0;

        c = this.read();  // Read the first character of the number
        if (c == '0') {
            c = this.read();
            if (c == 'e')
                return new BEValue(BigInteger.ZERO);  // Special case for 0
            else
                throw new Exception("'e' expected after zero, not '" + (char) c + "'");
        }

        char[] chars = new char[256];  // Support up to 255-character big integers
        int off = 0;

        if (c == '-') {  // Handle negative numbers
            c = this.read();
            if (c == '0')
                throw new Exception("Negative zero not allowed");
            chars[off++] = '-';
        }

        if (c < '1' || c > '9')  // Check for valid start of integer
            throw new Exception("Invalid Integer start '" + (char) c + "'");
        chars[off++] = (char) c;

        c = this.read();
        int i = c - '0';
        while (i >= 0 && i <= 9) {  // Read the rest of the integer
            chars[off++] = (char) c;
            c = this.read();
            i = c - '0';
        }

        if (c != 'e')  // Integer should end with 'e'
            throw new Exception("Integer should end with 'e'");

        String s = new String(chars, 0, off);  // Convert char array to string
        return new BEValue(new BigInteger(s));  // Return the decoded number
    }

    // Decodes the next B-encoded value from the stream as a list.
    public BEValue bdecodeList() throws Exception {
        int c = this.getNextIndicator();
        if (c != 'l') {
            throw new Exception("Expected 'l', not '" + (char) c + "'");
        }
        this.indicator = 0;

        List<BEValue> result = new ArrayList<>();
        c = this.getNextIndicator();
        while (c != 'e') {  // Decode each element in the list until 'e'
            result.add(this.bdecode());
            c = this.getNextIndicator();
        }
        this.indicator = 0;

        return new BEValue(result);  // Return the decoded list
    }

    // Decodes the next B-encoded value from the stream as a map (dictionary).
    public BEValue bdecodeMap() throws Exception {
        int c = this.getNextIndicator();
        if (c != 'd') {
            throw new Exception("Expected 'd', not '" + (char) c + "'");
        }
        this.indicator = 0;

        Map<String, BEValue> result = new HashMap<>();
        c = this.getNextIndicator();
        while (c != 'e') {  // Decode each key-value pair in the map until 'e'
            String key = this.bdecode().getString();  // Keys are always strings
            BEValue value = this.bdecode();
            result.put(key, value);
            c = this.getNextIndicator();
        }
        this.indicator = 0;

        return new BEValue(result);  // Return the decoded map
    }

    // Reads the next byte from the InputStream.
    private int read() throws IOException {
        int c = this.in.read();
        if (c == -1)
            throw new EOFException();  // End of stream
        return c;
    }

    // Reads the specified number of bytes from the InputStream.
    private byte[] read(int length) throws IOException {
        byte[] result = new byte[length];
        int read = 0;
        while (read < length) {
            int i = this.in.read(result, read, length - read);
            if (i == -1)
                throw new EOFException();  // End of stream
            read += i;
        }
        return result;  // Return the byte array
    }
}
