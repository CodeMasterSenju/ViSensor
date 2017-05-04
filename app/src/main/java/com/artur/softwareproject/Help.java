package com.artur.softwareproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by artur_000 on 03.05.2017.
 */

public class Help extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)
        setContentView(R.layout.activity_help);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
