package Peer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BEValue {
    /**
     * The B-encoded value can be a byte array, a Number, a List or a Map.
     * Lists and Maps contains BEValues too.
     */
    private final Object value;

    public BEValue(byte[] value) {
        this.value = value;
    }

    public BEValue(String value) throws UnsupportedEncodingException {
        this.value = value.getBytes(StandardCharsets.UTF_8);
    }

    public BEValue(String value, String enc)
            throws UnsupportedEncodingException {
        this.value = value.getBytes(enc);
    }

    public BEValue(int value) {
        this.value = Integer.valueOf(value); // Sử dụng Integer.valueOf thay vì new Integer()
    }

    public BEValue(long value) {
        this.value = Long.valueOf(value); // Sử dụng Long.valueOf thay vì new Long()
    }

    public BEValue(Number value) {
        this.value = value;
    }

    public BEValue(List<BEValue> value) {
        this.value = value;
    }

    public BEValue(Map<String, BEValue> value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    /**
     * Returns this BEValue as a String, interpreted as UTF-8.
     *
     * @throws Exception If the value is not a byte[].
     */
    public String getString() throws Exception {
        return this.getString("UTF-8");
    }

    /**
     * Returns this BEValue as a String, interpreted with the specified
     * encoding.
     *
     * @param encoding The encoding to interpret the bytes as when converting
     *                 them into a {@link String}.
     * @throws Exception If the value is not a byte[].
     */
    public String getString(String encoding) throws Exception {
        try {
            return new String(this.getBytes(), encoding);
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new InternalError(uee.toString());
        }
    }

    /**
     * Returns this BEValue as a byte[].
     *
     * @throws Exception If the value is not a byte[].
     */
    public byte[] getBytes() throws Exception {
        try {
            return (byte[]) this.value;
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        }
    }

    /**
     * Returns this BEValue as a Number.
     *
     * @throws Exception If the value is not a {@link Number}.
     */
    public Number getNumber() throws Exception {
        try {
            return (Number) this.value;
        } catch (ClassCastException cce) {
            throw new Exception(cce.toString());
        }
    }

    /**
     * Returns this BEValue as short.
     *
     * @throws Exception If the value is not a {@link Number}.
     */
    public short getShort() throws Exception {
        return this.getNumber().shortValue();
    }

    /**
     * Returns this BEValue as int.
     *
     * @throws Exception If the value is not a {@link Number}.
     */
    public int getInt() throws Exception {
        return this.getNumber().intValue();
    }

    /**
     * Returns this BEValue as long.
     *
     * @throws Exception If the value is not a {@link Number}.
     */
    public long getLong() throws Exception {
        return this.getNumber().longValue();
    }

    /**
     * Returns this BEValue as a List of BEValues.
     *
     * @throws Exception If the value is not an
     *                                   {@link ArrayList}.
     */
    @SuppressWarnings("unchecked")
    public List<BEValue> getList() throws Exception {
        if (this.value instanceof ArrayList) {
            return (ArrayList<BEValue>) this.value;
        } else {
            throw new Exception("Excepted List<BEvalue> !");
        }
    }

    /**
     * Returns this BEValue as a Map of String keys and BEValue values.
     *
     * @throws Exception If the value is not a {@link HashMap}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, BEValue> getMap() throws Exception {
        if (this.value instanceof HashMap) {
            return (Map<String, BEValue>) this.value;
        } else {
            throw new Exception("Expected Map<String, BEValue> !");
        }
    }
}
