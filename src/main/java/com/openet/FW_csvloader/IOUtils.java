package components;
import java.io.*;

public class IOUtils {
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

   /* Represents the end-of-file (or stream). */
   public static final int EOF = -1;

   public static FileOutputStream openOutputStream(final File file) throws IOException {
       if (file.exists()) {
           if (file.isDirectory()) throw new IOException("File '" + file + "' exists but is a directory");
           if (file.canWrite() == false) throw new IOException("File '" + file + "' cannot be written to");
       } else {
           final File parent = file.getParentFile();
           if (parent != null) {
               if (!parent.mkdirs() && !parent.isDirectory())  throw new IOException("Directory '" + parent + "' could not be created");
           }
       }
       return new FileOutputStream(file);
   }

   public static int copy(final InputStream input, final OutputStream output) throws IOException {
       final long count = copyLarge(input, output);
       if (count > Integer.MAX_VALUE) return -1;
       return (int) count;
   }

   public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
           throws IOException {
       long count = 0;
       int n;
       while (EOF != (n = input.read(buffer))) {
           output.write(buffer, 0, n);
           count += n;
       }
       return count;
   }

   public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
       return copyLarge(input, output, new byte[bufferSize]);
   }

   public static long copyLarge(final InputStream input, final OutputStream output) throws IOException {
       return copy(input, output, DEFAULT_BUFFER_SIZE);
   }
}