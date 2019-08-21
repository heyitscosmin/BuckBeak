package com.example.bcosm.buckbeak;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ModalBottom extends BottomSheetDialogFragment  {

    TextView modal_title, modal_value;

    public ModalBottom() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.modal_bottom, container, false);

        modal_title = v.findViewById(R.id.modal_title);
        modal_value = v.findViewById(R.id.modal_value);


        String title = getArguments().getString("modal_title");
        String value = getArguments().getString("modal_value");
        modal_title.setText(title);
        modal_value.setText(value);


        return v;
    }



}



