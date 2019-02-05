package de.my;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Get the data from an URL pointing to an image and store it into a file. This is mainly copied from stackoverflow:
 * 
 * @author Oliver Meyer
 * @author swapnil gandhi
 *
 */
public class SaveURLToFile {
    /**
     * Retrieve data from a given URL and store it in a file. This does does not do any error handling, but just throws.
     * The idea is, to retrieve a picture from an URL.
     * 
     * @param imageUrl
     *            URL for the image.
     * @param destinationFile
     *            windows file name to store the image in
     * @throws IOException
     *             if something goes wrong, either in getting or storing the data.
     */
    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

}
