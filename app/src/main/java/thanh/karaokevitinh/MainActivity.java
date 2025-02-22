package thanh.karaokevitinh;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SERVER_IP = "192.168.0.172";
    private static final int SERVER_PORT = 5000;
    private static final String SAVE_PATH = "assets/downloaded_file.zip";
    //private Executor executor;
    private ExecutorService executor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textView = findViewById(R.id.textView);

        /*String serverAddress = "192.168.0.172";
        int port = 5000; // Change this to match your server's port
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            // Sending a message to the server
            out.println("Hello, Server!");
            // Reading response from the server
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        executor = Executors.newSingleThreadExecutor();

        /*executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String serverAddress = "192.168.0.172";
                    int port = 5000;
                    try (Socket socket = new Socket(serverAddress, port);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        // Sending a message to the server
                        out.println("Hello, Server!");

                        // Reading response from the server
                        String response = in.readLine();
                        System.out.println("Server response: " + response);

                        // Update the UI (example)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update UI here (e.g., set text in a TextView)
                                // Do not try to make network request here.
                                Log.d(TAG, "Update UI " + response);
                                textView.setText(response);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //HandleJSON(textView);
                    //HandleFile01();
                    //Non-static method 'execute(Params...)'  cannot be referenced from a static context
                    //FileClient.execute();
                    //FileClient fileClient = new FileClient();
                    //fileClient.execute();
                    String serverAddress = "192.168.0.172"; // Or the server's IP
                    int port = 5000;
                    try (Socket socket = new Socket(serverAddress, port);
                         InputStream inputStream = socket.getInputStream();
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         OutputStream outputStream = socket.getOutputStream();
                         PrintWriter writer = new PrintWriter(outputStream, true)) {
                        // Read from server
                        StringBuilder stringBuilder = new StringBuilder();
                        int chunkSize;
                        while ((chunkSize = readInt(bufferedInputStream)) != -1) {
                            if (chunkSize == -2) {
                                System.out.println("File not exists");
                                Log.d(TAG, "File not exists");
                                return;
                            }
                            byte[] chunk = new byte[chunkSize];
                            int bytesRead = bufferedInputStream.read(chunk);
                            stringBuilder.append(new String(chunk, 0, bytesRead, StandardCharsets.UTF_8));
                            Log.d(TAG, stringBuilder.toString());
                            textView.setText(stringBuilder.toString());
                        }
                        System.out.println("File received");
                        Log.d(TAG, "File received");
                        //send response
                        writer.println("OK");
                        writer.flush();
                        // Print the received data (for verification)
                        // System.out.println("Received data:\n" + stringBuilder.toString());
                    } catch (IOException e) {
                        //e.printStackTrace();
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // Helper function to convert byte array (4 bytes) to int
    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    public void HandleJSON(TextView textView) {
        String serverAddress = "192.168.0.172";
        int port = 5000;
        //int readTimeoutMillis = 60000; // 60 seconds
        try (Socket socket = new Socket(serverAddress, port)) {
            //socket.setSoTimeout(readTimeoutMillis); // Set the read timeout
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            // Reassemble the string
            ByteArrayOutputStream stringBuilder = new ByteArrayOutputStream();
            byte[] lengthBytes = new byte[4];
            int bytesRead;
            while (true) {
                // Read the length prefix (4 bytes)
                int read = bufferedInputStream.read(lengthBytes);
                Log.d(TAG, "read " + read);
                if (read == -1) {
                    // end of stream.
                    break;
                }
                int chunkLength = byteArrayToInt(lengthBytes);
                Log.d(TAG, "chunkLength " + chunkLength);
                if (chunkLength == -1) {
                    break;
                }
                byte[] chunk = new byte[chunkLength];
                bytesRead = bufferedInputStream.read(chunk, 0, chunkLength);
                if (bytesRead != chunkLength) {
                    // Handle error
                    System.err.println("Error reading chunk");
                    break;
                }
                stringBuilder.write(chunk);
            }
            String receivedString = new String(stringBuilder.toByteArray(), StandardCharsets.UTF_8);
            //System.out.println("Received large string: " + receivedString);
            //Send the response

            bufferedWriter.write("data well received");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            //socket.close();

            // Update the UI (example)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update UI here (e.g., set text in a TextView)
                    // Do not try to make network request here.
                    //Log.d(TAG, "Update UI " + receivedString);
                    textView.setText(receivedString);
                }
            });
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    public static void HandleFile() {

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
             FileOutputStream fos = new FileOutputStream(SAVE_PATH)) {

            // Read file size
            long fileSize = dis.readLong();
            System.out.println("Receiving file of size: " + fileSize + " bytes");

            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                // Optional: Show progress
                System.out.printf("Progress: %.2f%%\r", (totalRead * 100.0 / fileSize));
            }

            System.out.println("\nFile received successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void HandleFile01() {
        String serverAddress = "192.168.0.172"; // Or the server's IP
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port);
             InputStream inputStream = socket.getInputStream();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true)) {

            // Read from server
            StringBuilder stringBuilder = new StringBuilder();
            int chunkSize;
            while ((chunkSize = readInt(bufferedInputStream)) != -1) {
                if (chunkSize == -2) {
                    System.out.println("File not exists");
                    return;
                }
                byte[] chunk = new byte[chunkSize];
                int bytesRead = bufferedInputStream.read(chunk);
                stringBuilder.append(new String(chunk, 0, bytesRead, StandardCharsets.UTF_8));
            }
            System.out.println("File received");

            //send response
            writer.println("OK");
            writer.flush();

            // Print the received data (for verification)
            // System.out.println("Received data:\n" + stringBuilder.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int readInt(BufferedInputStream in) throws IOException {
        byte[] lengthBytes = new byte[4];
        int bytesRead = in.read(lengthBytes);
        if (bytesRead != 4) {
            return -1;
        }
        return byteArrayToInt(lengthBytes);
    }
}