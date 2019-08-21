package com.example.bcosm.buckbeak;


//TODO GET STARTED BUTTON cu urmatorul windows sa ne alegem numele plantei ( recycle virw)



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Switch      enable_bt;
    ImageView   search_bt;

    TextView    display_switch_status;
    ListView    list_view;
    Button      view_data;

    Animation top,left;


    public BluetoothAdapter BA;
    protected Set<BluetoothDevice> pairedDevices;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Butoane
        enable_bt = findViewById(R.id.enable_bt);
        search_bt = findViewById(R.id.search_bt);

        list_view = findViewById(R.id.list_view);

        view_data = findViewById(R.id.view_data);
        display_switch_status = findViewById(R.id.display_switch_status);

        //Animatii
        top = AnimationUtils.loadAnimation(this,R.anim.top);
        left = AnimationUtils.loadAnimation(this,R.anim.left);

        search_bt.setAnimation(top);
        enable_bt.setAnimation(left);
        display_switch_status.setAnimation(left);




        BA = BluetoothAdapter.getDefaultAdapter();

        if( BA == null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else if(BA.isEnabled()) {
            enable_bt.setChecked(true);
        }



        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    display_switch_status.setText("DISCONNECTED");
                    BA.disable();
                   // Toast.makeText(MainActivity.this, "Turned OFF", Toast.LENGTH_SHORT).show();

                }else{
                    display_switch_status.setText("CONNECTED");
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn,0);
                    //Toast.makeText(MainActivity.this, "Turned ON", Toast.LENGTH_SHORT).show();

                }
            }
        });

        if(!enable_bt.isChecked()){
            display_switch_status.setText("DISCONNECTED");
        }else{
            display_switch_status.setText("CONNECTED");
        }

        search_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_view();
            }
        });


        view_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_view_data();
            }
        });
    }

//aici se termina onCreate

    private void list_view() {
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : pairedDevices){
            list.add(bt.getName());
            Toast.makeText(this, "Showing devices", Toast.LENGTH_SHORT).show();
            ArrayAdapter adapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1, list );
            list_view.setAdapter(adapter);
        }

    }

    public String getLocalBluetoothName(){
        if(BA == null){
            BA = BluetoothAdapter.getDefaultAdapter();
        }
        String name = BA.getName();
        if (name== null){
            name = BA.getAddress();
        }
        return name;
    }
    public void open_view_data(){
        Intent open_view_data = new Intent(this , Values.class);
        startActivity(open_view_data);
    }
}

