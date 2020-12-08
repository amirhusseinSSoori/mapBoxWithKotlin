package com.amirHusseinSoori.mapboxwithkotlin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class Dialog_message extends Dialog{
    Button Btn_yes,Btn_no;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_gps_provider);
        Btn_yes=findViewById(R.id.Btn_yes);
        Btn_no=findViewById(R.id.Btn_no);
        Btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dismiss();
            }
        });
        Btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public Dialog_message(Context context) {
        super(context);
        this.context=context;
    }



}
