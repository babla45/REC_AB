package com.example.rec_ab;

import android.content.Intent;
import android.os.Bundle;
import java.io.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private int flag=0;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;
    InputStream inputStream=null;
    TextView textView;
    Button goNextButton, activateButton;
    Boolean activated=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //------------------------------------------------------------------------------------
        txtRed = findViewById(R.id.txtRed);
        txtGreen = findViewById(R.id.txtGreen);
        txtBlue = findViewById(R.id.txtBlue);

        RED = findViewById(R.id.RED);
        GREEN = findViewById(R.id.GREEN);
        BLUE = findViewById(R.id.BLUE);

        textView=findViewById(R.id.setTextId);
        goNextButton = findViewById(R.id.goNextButtonId);
        activateButton=findViewById(R.id.activateButtonId);
//===================================================================


        goNextButton.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this, videoRecorderActivity.class);
            startActivity(intent);
            finish();
        });

        activateButton.setOnClickListener(v -> {
            if(activateButton.getText().equals("Activate"))
            {
                activated=true;
                activateButton.setText("Activated");
                activateButton.setTextColor(0xFF00FF00);
            }
            else {
                activated=false;
                activateButton.setText("Activate");
                activateButton.setTextColor(0xFF000000);
            }

        });



//===========================================================

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT > 31) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
                Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        try{

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

        }
        catch (Exception e)
        {
            Toast.makeText(this, "exception caught", Toast.LENGTH_SHORT).show();
        }
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

                    // Add the code for reading from inputStream and processing it here

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error connecting to Bluetooth device: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error connecting to Bluetooth device", Toast.LENGTH_SHORT).show();
                }


                //================================================================================
                if(inputStream!=null)
                {
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
                                        if (lines[index] != null)
                                        {
                                            //when motion is detected
                                            if (receivedLine.equals("==============="))
                                            {
                                                builder.append(lines[index]).append(new StringBuilder().append("\n"));


                                                if(flag==0 && activateButton.getText().equals("Activated"))
                                                {
                                                    flag+=1;
                                                    Toast.makeText(MainActivity.this, "clicked "+Integer.toString(flag), Toast.LENGTH_SHORT).show();
                                                    if(activated==true){
                                                        Toast.makeText(MainActivity.this, "activated", Toast.LENGTH_SHORT).show();
                                                        goNextButton.performClick();
                                                    }

                                                }

                                                //else flag=1;
                                            }
                                            else builder.append("-->  "+lines[index]).append(new StringBuilder().append("\n"));
                                        }
                                    }

                                    textView.setText(builder.toString().trim());
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

            }
        }).start();



        RED.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red = (int) (((float) 200 / 100) * i);
                txtRed.setText(Integer.toString(red));
                if(inputStream!=null)
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
                if(inputStream!=null)
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
                if(inputStream!=null)
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
    int flag2=1;
    private void sendCommand(int LED_index, int value) {
        if (outputStream == null) {
            Log.d(TAG, "Output stream error");
            if(flag2==1) {
                Toast.makeText(this, "Output stream error", Toast.LENGTH_SHORT).show();
                flag2=0;
            }
            return;
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
    protected void onPause() {
        super.onPause();
        Toast.makeText(MainActivity.this, "on pause called", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flag=0;
        Toast.makeText(MainActivity.this, "on stop called", Toast.LENGTH_SHORT).show();
        ///new
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
        Toast.makeText(this, "On Destroy MainActivity", Toast.LENGTH_SHORT).show();
    }

}