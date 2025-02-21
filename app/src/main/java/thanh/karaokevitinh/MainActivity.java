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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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
                    String serverAddress = "192.168.0.172";
                    int port = 5000;
                    try (Socket socket = new Socket(serverAddress, port);
                         InputStream inputStream = socket.getInputStream();
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         OutputStream outputStream = socket.getOutputStream();
                         BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                        // Reassemble the string
                        ByteArrayOutputStream stringBuilder = new ByteArrayOutputStream();
                        byte[] lengthBytes = new byte[4];
                        int bytesRead;
                        while (true) {
                            // Read the length prefix (4 bytes)
                            int read = bufferedInputStream.read(lengthBytes);
                            if (read == -1) {
                                // end of stream.
                                break;
                            }

                            int chunkLength = byteArrayToInt(lengthBytes);
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
                        System.out.println("Received large string: " + receivedString);
                        //Send the response

                        bufferedWriter.write("data well received");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        socket.close();

                        // Update the UI (example)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update UI here (e.g., set text in a TextView)
                                // Do not try to make network request here.
                                Log.d(TAG, "Update UI " + receivedString);
                                textView.setText(receivedString);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
}