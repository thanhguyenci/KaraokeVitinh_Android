package thanh.karaokevitinh;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.*;
import java.net.Socket;

public class FileClient extends AsyncTask<Void, Integer, String> {
    private static final String SERVER_IP = "192.168.0.172"; // Change to server IP
    private static final int SERVER_PORT = 5000;
    private static final String SAVE_FILE_NAME = "downloaded_file.mp4";

    @Override
    protected String doInBackground(Void... voids) {
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_FILE_NAME);

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Read file size from server
            long fileSize = dis.readLong();
            Log.d("FileClient", "Receiving file of size: " + fileSize + " bytes");

            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                // Update progress (optional)
                publishProgress((int) ((totalRead * 100) / fileSize));
            }

            Log.d("FileClient", "File received successfully: " + outputFile.getAbsolutePath());
            return "Download complete!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Download failed!";
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.d("FileClient", "Progress: " + values[0] + "%");
    }
}
