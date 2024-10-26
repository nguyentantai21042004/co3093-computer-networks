package Peer.src.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class represents a B-encoded value, which can be a byte array, a Number, a List, or a Map.
public class BEValue {
    // Stores the value, which can be of various types (byte array, String, Number, List, or Map).
    private final Object value;

    public BEValue(byte[] value) {
        this.value = value;
    }

    // Constructor for a B-encoded value as a UTF-8 encoded String.
    public BEValue(String value) throws UnsupportedEncodingException {
        this.value = value.getBytes(StandardCharsets.UTF_8);
    }

    // Constructor for a B-encoded value as a String with specified encoding.
    public BEValue(String value, String enc) throws UnsupportedEncodingException {
        this.value = value.getBytes(enc);
    }

    // Constructor for a B-encoded value as an integer.
    public BEValue(int value) {
        this.value = Integer.valueOf(value); // Use Integer.valueOf to create Integer object.
    }

    // Constructor for a B-encoded value as a long.
    public BEValue(long value) {
        this.value = Long.valueOf(value); // Use Long.valueOf to create Long object.
    }

    // Constructor for a B-encoded value as a generic Number.
    public BEValue(Number value) {
        this.value = value;
    }

    // Constructor for a B-encoded value as a list of BEValues.
    public BEValue(List<BEValue> value) {
        this.value = value;
    }

    // Constructor for a B-encoded value as a map of String keys and BEValue values.
    public BEValue(Map<String, BEValue> value) {
        this.value = value;
    }

    // Returns the stored value (byte array, Number, List, or Map).
    public Object getValue() {
        return this.value;
    }

    // Returns this BEValue as a String interpreted with UTF-8 encoding.
    public String getString() throws Exception {
        return this.getString("UTF-8");
    }

    // Returns this BEValue as a String interpreted with the specified encoding.
    public String getString(String encoding) throws Exception {
        try {
            return new String(this.getBytes(), encoding);
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new InternalError(uee.toString());
        }
    }

    // Returns this BEValue as a byte array.
    public byte[] getBytes() throws Exception {
        try {
            return (byte[]) this.value;
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        }
    }

    // Returns this BEValue as a Number.
    public Number getNumber() throws Exception {
        try {
            return (Number) this.value;
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        }
    }

    // Returns this BEValue as a short (converting from a Number).
    public short getShort() throws Exception {
        return this.getNumber().shortValue();
    }

    // Returns this BEValue as an int (converting from a Number).
    public int getInt() throws Exception {
        return this.getNumber().intValue();
    }

    // Returns this BEValue as a long (converting from a Number).
    public long getLong() throws Exception {
        return this.getNumber().longValue();
    }

    // Returns this BEValue as a list of BEValues.
    @SuppressWarnings("unchecked")
    public List<BEValue> getList() throws Exception {
        if (this.value instanceof ArrayList) {
            return (ArrayList<BEValue>) this.value;
        } else {
            throw new Exception("Expected List<BEValue>!");
        }
    }

    // Returns this BEValue as a map of String keys and BEValue values.
    @SuppressWarnings("unchecked")
    public Map<String, BEValue> getMap() throws Exception {
        if (this.value instanceof HashMap) {
            return (Map<String, BEValue>) this.value;
        } else {
            throw new Exception("Expected Map<String, BEValue>!");
        }
    }
}
