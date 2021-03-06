package com.ralitski.util.l;

import java.util.Arrays;

public class StringInputStream {

    private char[] buf;

    private int pos;

    public StringInputStream(String s) {
        buf = s.toCharArray();
    }

    public StringInputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                                               + size);
        }
        buf = new char[size];
    }
    
    //reading

    public synchronized char read() {
        return (pos < buf.length) ? (buf[pos++]) : '\u0000';
    }

    public synchronized int read(char b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= buf.length) {
            return -1;
        }

        int avail = available();
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        java.lang.System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }
    
    public synchronized String read(int len) {
        if (pos >= buf.length) {
            return null;
        } else if (len <= 0) {
            return null;
        }

        int avail = available();
        if (len > avail) {
            len = avail;
        }
        char[] copy = new char[len];
        java.lang.System.arraycopy(buf, pos, copy, 0, len);
        pos += len;
        return new String(copy);
    }

    public synchronized long skip(long n) {
        long k = buf.length - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }
    
    public synchronized long back(long n) {
        long k = pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos -= k;
        return k;
    }

    public synchronized int available() {
        return buf.length - pos;
    }
    
    //misc

    public synchronized char[] toCharArray() {
        return Arrays.copyOf(buf, buf.length);
    }
    
    public String toString() {
    	return new String(buf, 0, buf.length);
    }
}
