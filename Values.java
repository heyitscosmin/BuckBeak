package com.example.bcosm.buckbeak;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class Values extends AppCompatActivity {

    private static final String FILE_NAME = "logging.csv";

    TextView moist_textView, temp_textView , incomingmsg_textView, update_time;
    Animation bottom;
    LinearLayout moisture_box, temp_box;
    ProgressBar moist_progressbar, temp_progressbar;
    CardView graph_card;
    Button pump_button;


    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    String sensor0,sensor1, time_data;


    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_values);



        //Link the buttons and textViews to respective views
        temp_textView = findViewById(R.id.temp_textView);
        moist_textView = findViewById(R.id.moist_textView);
        incomingmsg_textView = findViewById(R.id.incomingmsg);
        moisture_box = findViewById(R.id.moisture_box);
        temp_box = findViewById(R.id.temp_box);
        update_time = findViewById(R.id.update_time);
        moist_progressbar = findViewById(R.id.moist_progressbar);
        temp_progressbar = findViewById(R.id.temp_progressbar);
        graph_card = findViewById(R.id.graph_card);
        pump_button = findViewById(R.id.pump_button);




        //Animations
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom);
        moisture_box.setAnimation(bottom);
        temp_box.setAnimation(bottom);

        find_weather();

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										    //daca mesajul este ceeea de dorim
                    String readMessage = (String) msg.obj;                              // msg.arg1 = biti de la arduino
                    recDataString.append(readMessage);      							//continua sa appendese stringul pana la ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determinam sfarsitul mesajului
                    if (endOfLineIndex > 0) {                                           // ne asiguram ca avem continut inainte de ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);// extragem string-ul
                        incomingmsg_textView.setText("" + dataInPrint);

                        if (recDataString.charAt(0) == '#')								//daca stringul incepe cu # stim ca e stringul transmis
                        {
                             sensor0 = recDataString.substring(1,3);                    //get sensor value from string between indices 1-5
                             sensor1 = recDataString.substring(4,6);


                                 int moist_val = Integer.parseInt(sensor0);
                                 int temp_val = Integer.parseInt(sensor1);

                                 moist_progressbar.setProgress(moist_val);
                                 temp_progressbar.setProgress(temp_val);

                                 moist_progressbar.setEnabled(false); //Disable changes on seekbar by client
                                 temp_progressbar.setEnabled(false); //Disable changes on seekbar by client

                                 showMoistStatus(moist_val);
                                 showTempStatus(temp_val);





                            	                                                        //update the textviews with sensor values
                        }


                        mConnectedThread.clock();                                       //timestamp


                        recDataString.setLength(0); 				                    //clear all string data
                        dataInPrint = " ";
                        mConnectedThread.saveData();
                    }

                }



            }


            private void showMoistStatus(int moist_val) {
                if( moist_val >= 1 && moist_val <=30 ) {
                    moist_textView.setText("Moisture is very good");
                }else if ( moist_val >= 30 && moist_val <= 50 ){
                    moist_textView.setText("Moisture is good");
                }else if(moist_val >=50){
                    moist_textView.setText("Moisture is bad");
                }
            }

            private void showTempStatus(int temp_val) {
                if( temp_val >= 12 && temp_val <=18 ) {
                    temp_textView.setText("Temp is cold");
                }else if ( temp_val >= 20 && temp_val <= 23 ){
                    temp_textView.setText("Temp is good");
                }else if(temp_val >=24){
                    temp_textView.setText("Temp is warm");
                }
            }


        };

        moisture_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("modal_value", sensor0);
                bundle.putString("modal_title", "Current soil moisture (%)");

                ModalBottom modalBottom = new ModalBottom();
                modalBottom.setArguments(bundle);

                modalBottom.show(getSupportFragmentManager(), "example dialog");
            }
        });

        temp_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("modal_value", sensor1);
                bundle.putString("modal_title", "Current soil temp (°C)");

                ModalBottom modalBottom = new ModalBottom();
                modalBottom.setArguments(bundle);

                modalBottom.show(getSupportFragmentManager(), "example dialog");
            }
        });

        graph_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_graph_card();
            }
        });

        pump_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectedThread.write("m");
            }
        });






        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }

    private void find_weather(){
        String url ="http://api.openweathermap.org/data/2.5/weather?q=brasov,romania&appid=aa2bdf91c192a8a71998cb4e1a439c73&units=Metric";
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = String.valueOf(main_object.getDouble("temp"));
                    String description = object.getString("description");
                    String city = response.getString("name");
                    System.out.println("prolaps2");
                    System.out.println("prolaps"+temp+description+city);

                }catch(JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);


    }

    private void open_graph_card() {
        Intent intent = new Intent(this, Chart.class);
        startActivity(intent);
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = "98:D3:31:FD:80:CB";

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            makeText(getBaseContext(), "Socket creation failed", LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                makeText(getBaseContext(), "Socket connection failed", LENGTH_LONG).show();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            makeText(getBaseContext(), "Socket closing failed", LENGTH_LONG).show();
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            makeText(getBaseContext(), "Device does not support bluetooth", LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                makeText(getBaseContext(), "Can not connect to board", LENGTH_LONG).show();
                finish();

            }
        }
        public void clock() {
            java.util.Date noteTS = Calendar.getInstance().getTime();

            String time; // 12:00
            time = "kk:mm:ss";
            String time2 = (String) DateFormat.format(time, noteTS);

            String date = "dd-MM-yyyy"; // 01 January 2013
            String date2 = (String) DateFormat.format(date, noteTS);

            time_data = date2 + "," + time2;

            update_time.setText(DateFormat.format(time_data, noteTS));




        }


        public void saveData(){
            //String title = "Date" + "," + "Moisture" + "Temp" ;
            String text = time_data + "," + sensor0 + "," + sensor1 + "," +"\n" ;
            FileOutputStream fos = null;


            try {
                fos = openFileOutput(FILE_NAME, MODE_APPEND);
                //fos.write(title.getBytes());
                fos.write(text.getBytes());

            }catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }



    }
}
