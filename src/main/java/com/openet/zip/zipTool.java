package zip;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.concurrent.locks.ReentrantLock;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
public class zipTool {
    private static class SingletonHolder {
        static zipTool instance = new zipTool();
    }
    public static zipTool getInstance() {
        return SingletonHolder.instance;
    }
    private static final ReentrantLock lock = new ReentrantLock();

    public static byte[] compress(byte[] data) throws IOException {
        lock.lock();
        try {
            Deflater deflater = new Deflater();
            deflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();
            //System.out.println("Original: " + data.length / 1024 + " Kb");
            //System.out.println("Compressed: " + output.length / 1024 + " Kb");
            return output;
        } finally {
            lock.unlock();
        }
    }
    public static byte[] appendBeginByte(byte[] data) throws IOException, DataFormatException {
        lock.lock();
        try {
            //System.out.println("appendBeginByte IN: " + data.length);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            outputStream.write(0xa4);
            outputStream.write(data, 0, data.length);
            outputStream.close();
            byte[] output = outputStream.toByteArray();
            output[2] = 0x16;
            //System.out.println("appendBeginByteOUT: " + output.length);
            return output;
        } finally {
            lock.unlock();
        }
    }
    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        lock.lock();
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            outputStream.write(0xa4);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();
            return output;
        } finally {
            lock.unlock();
        }
    }
    public void initialize() {
    }
    public static byte[] asHex(byte[] bufbyte) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        String buf = new String(bufbyte);
        for (int i = 0; i < buf.length(); i+=2) {
            String string = null;
            if (i < buf.length() && (i+2 ) < buf.length()) string = new String(buf.substring(i, i+2));
            else break;
            //System.out.println(string + " = " + Integer.parseInt(string, 16));
            if (i != 2) dos.writeByte(Integer.parseInt(string, 16));
            else dos.writeByte(156);
        }
        dos.flush();
        return bos.toByteArray();
    }

    public static String asString(byte[] bufbyte) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte b : bufbyte) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
