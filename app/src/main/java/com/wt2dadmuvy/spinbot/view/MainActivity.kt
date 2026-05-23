package com.wt2dadmuvy.spinbot.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wt2dadmuvy.spinbot.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }
}
