package com.example.android.myinventory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class EditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu option from the res/menu/editor_menu.xml file.
        //This adds menu items to the top of the bar
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }
}
