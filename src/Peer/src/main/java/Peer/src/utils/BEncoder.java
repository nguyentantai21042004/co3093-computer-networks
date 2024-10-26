package Peer.src.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;


// This class handles B-encoding of various data types (String, byte[], Number, List, Map).
public class BEncoder {
    // Encodes a generic object (String, byte[], Number, List, or Map) in B-encoding format.
    @SuppressWarnings("unchecked")
    public static void bencode(Object o, OutputStream out) throws IOException, IllegalArgumentException {
        if (o instanceof BEValue) {
            o = ((BEValue) o).getValue();
        }

        if (o instanceof String) {
            bencode((String) o, out);
        } else if (o instanceof byte[]) {
            bencode((byte[]) o, out);
        } else if (o instanceof Number) {
            bencode((Number) o, out);
        } else if (o instanceof List) {
            bencode((List<BEValue>) o, out);
        } else if (o instanceof Map) {
            bencode((Map<String, BEValue>) o, out);
        } else {
            throw new IllegalArgumentException("Cannot bencode: " + o.getClass());
        }
    }

    // Encodes a String in B-encoding format.
    public static void bencode(String s, OutputStream out) throws IOException {
        byte[] bs = s.getBytes(StandardCharsets.UTF_8);
        bencode(bs, out);
    }

    // Encodes a Number in B-encoding format.
    public static void bencode(Number n, OutputStream out) throws IOException {
        out.write('i');  // Write 'i' for integer start in B-encoding
        String s = n.toString();
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.write('e');  // Write 'e' for integer end in B-encoding
    }

    // Encodes a List of BEValues in B-encoding format.
    public static void bencode(List<BEValue> l, OutputStream out) throws IOException {
        out.write('l');  // Write 'l' for list start in B-encoding
        for (BEValue value : l) {
            bencode(value, out);  // Recursively encode each element in the list
        }
        out.write('e');  // Write 'e' for list end in B-encoding
    }

    // Encodes a byte array in B-encoding format.
    public static void bencode(byte[] bs, OutputStream out) throws IOException {
        String l = Integer.toString(bs.length);  // Get the length of the byte array
        out.write(l.getBytes(StandardCharsets.UTF_8));  // Write the length of the byte array
        out.write(':');  // Write ':' separator between length and content
        out.write(bs);  // Write the actual byte array content
    }

    // Encodes a Map (with String keys and BEValue values) in B-encoding format.
    public static void bencode(Map<String, BEValue> m, OutputStream out) throws IOException {
        out.write('d');  // Write 'd' for dictionary start in B-encoding

        // Sort the keys of the map before encoding to ensure lexicographical order.
        Set<String> s = m.keySet();
        List<String> l = new ArrayList<>(s);
        Collections.sort(l);

        for (String key : l) {
            Object value = m.get(key);  // Get the value corresponding to each key
            bencode(key, out);  // Encode the key
            bencode(value, out);  // Encode the value
        }

        out.write('e');  // Write 'e' for dictionary end in B-encoding
    }

    // Encodes a Map (with String keys and BEValue values) and returns the result as a ByteBuffer.
    public static ByteBuffer bencode(Map<String, BEValue> m) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();  // Use ByteArrayOutputStream to accumulate the output
        BEncoder.bencode(m, bas);  // Encode the map using B-encoding
        bas.close();  // Close the ByteArrayOutputStream
        return ByteBuffer.wrap(bas.toByteArray());  // Return the byte array wrapped in a ByteBuffer
    }
}
