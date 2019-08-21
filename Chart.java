package com.example.bcosm.buckbeak;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;


public class Chart extends Activity {

    private static final String FILE_NAME = "logging.csv";


    TextView lista;
    //String[] country;

     ArrayList<String> categoryList = new ArrayList<String>();



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        lista = findViewById(R.id.lista);






        List<String[]> list = new ArrayList<String[]>();
        String next[] = {};
        FileInputStream fis = null;
        try {
              fis = openFileInput(FILE_NAME);
              InputStreamReader isr = new InputStreamReader(fis);



            CSVReader reader = new CSVReader(isr);
            for (;;) {
                next = reader.readNext();
                if (next != null) {
                    list.add(next);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(list.get(i)[j]).append("\n");
                lista.setText(sb.toString());
            }
        }

    }
}
