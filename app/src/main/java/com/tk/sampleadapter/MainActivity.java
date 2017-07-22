package com.tk.sampleadapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void single(View v) {
        startActivity(new Intent(this, SingleActivity.class));
    }

    public void multi(View v) {
        startActivity(new Intent(this, MultiActivity.class));
    }

    public void multi_bind(View v) {
        startActivity(new Intent(this, MultiBindActivity.class));
    }
    public void nested(View v) {
        startActivity(new Intent(this, NestedActivity.class));
    }
}
