package com.example.rec_ab;

import android.content.Intent;
import android.os.Bundle;
import java.io.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public String TAG = "mnoram";
    private String macAddress = "98:DA:50:02:ED:D3";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public int red, green, blue;
    private TextView txtRed, txtGreen, txtBlue;
    private SeekBar RED, GREEN, BLUE;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;
    InputStream inputStream;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;});
        //------------------------------------------------------------------------------------
        txtRed = findViewById(R.id.txtRed);
        txtGreen = findViewById(R.id.txtGreen);
        txtBlue = findViewById(R.id.txtBlue);

        RED = findViewById(R.id.RED);
        GREEN = findViewById(R.id.GREEN);
        BLUE = findViewById(R.id.BLUE);

        textView=findViewById(R.id.setTextId);
//===================================================================








//===========================================================

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT > 31) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
                Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            System.out.println("Name->" + device.getName() + "    " + "MAC->" + device.getAddress());
            if (device.getName().equals("BABLA_HC_05")) {
                macAddress = device.getAddress();
                System.out.println("MAC - > " + macAddress);
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        // Connecting to bluetooth device in a separate thread;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT > 31) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        Toast.makeText(MainActivity.this, "Please restart the app", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream=bluetoothSocket.getInputStream();
                    //============================================


                    //================================================================================
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                    try {
//                        String line;
//                        while((line = reader.readLine()) != null) {
//                            // Process the received string value
//                            // For example, you can display it on the UI, log it, or perform any other action
////                            Log.d("hc05", line);
//                            textView.setText(textView.getText().toString()+"\n"+line);
//                            if(line.equals("1_11")){
//                                Intent intent=new Intent(MainActivity.this,videoRecorderActivity.class);
//                                intent.putExtra("1_1","1_1");
//                                startActivity(intent);
//                                textView.setText("got 1-1");
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    final int n=50;
                    final String[] lines = new String[n]; // Circular buffer to store the last five lines
                    final int[] currentIndex = {0}; // Index to keep track of the current position in the circular buffer

                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final String receivedLine = line;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the current index and store the received line
                                    lines[currentIndex[0]] = receivedLine;
                                    currentIndex[0] = (currentIndex[0] + 1) % n;

                                    // Construct text for textView
                                    StringBuilder builder = new StringBuilder();
                                    for (int i = n-1; i >= 0; i--) {
                                        int index = (currentIndex[0] + i) % n;
                                        if (lines[index] != null) {
                                            builder.append("-->  "+lines[index]).append(new StringBuilder().append("\n"));
                                        }
                                    }

                                    textView.setText(builder.toString().trim());

                                    // Check if the received line is "1_11"
                                    if (receivedLine.equals("1_11")) {
                                        Intent intent = new Intent(MainActivity.this, videoRecorderActivity.class);
                                        intent.putExtra("1_1", "1_1");
                                        startActivity(intent);
                                        textView.setText("got 1-1");
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //==============================================================================

                    Log.d("Message", "Connected to HC-05");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Bluetooth successfully connected", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                catch (IOException e) {
                    Log.d("Message5", "Turn on bluetooth and restart the app");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Turn on bluetooth and restart the app", Toast.LENGTH_SHORT).show();
                        }
                    });
                    throw new RuntimeException(e);
                }
            }
        }).start();



        RED.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red = (int) (((float) 200 / 100) * i);
                txtRed.setText(Integer.toString(red));
                sendCommand(1, red);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        GREEN.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                green = (int) (((float) 200 / 100) * i);
                txtGreen.setText(Integer.toString(green));
                sendCommand(2, green);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        BLUE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blue = (int) (((float) 200 / 100) * i);
                txtBlue.setText(Integer.toString(blue));
                sendCommand(3, blue);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
//=========on create end===============

    private void sendCommand(int LED_index, int value) {
        if (outputStream == null) {
            Log.d(TAG, "Output stream error");
            Toast.makeText(this, "Output stream error", Toast.LENGTH_SHORT).show();
            // return;
        }
        try {
            String command = "";
            if (LED_index == 1) {
                // if function is being used by RED LED
                command = "R_" + Integer.toString(value);
            }
            if (LED_index == 2) {
                // if function is being used by GREEN LED
                command = "G_" + Integer.toString(value);
            }
            if (LED_index == 3) {
                // if function is being used by BLUE LED
                command = "B_" + Integer.toString(value);
            }
            command = command + '\n';
            outputStream.write(command.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Connection closed");
                Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.d(TAG, "Error while closing the connection");
                Toast.makeText(this, "Error on closing connection", Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(this, "Closing App", Toast.LENGTH_SHORT).show();
    }

    public void goNextActivity(View view) {
        Intent intent=new Intent(this,videoRecorderActivity.class);
        startActivity(intent);
    }
}