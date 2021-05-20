package com.decagon.android.sq007

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var openMap: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openMap = findViewById(R.id.buttonOpenMap)
        openMap.setOnClickListener {
            val mapIntent = Intent(this, TrackerActivity::class.java)
            startActivity(mapIntent)
        }
    }

}