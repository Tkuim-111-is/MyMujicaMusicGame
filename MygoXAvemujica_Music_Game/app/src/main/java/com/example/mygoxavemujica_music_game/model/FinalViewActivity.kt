package com.example.mygoxavemujica_music_game.model

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mygoxavemujica_music_game.R


class FinalViewActivity : AppCompatActivity() {
    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var textView9: TextView
    private lateinit var textView15: TextView
    private lateinit var textView16: TextView
    private lateinit var textView17: TextView
    private lateinit var textView18: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_view)  // 這邊對應你的 XML 檔名
        textView1 = findViewById(R.id.textView1)
        textView2=findViewById(R.id.textView2)
        textView3=findViewById(R.id.textView3)
        textView9=findViewById(R.id.textView9)
        textView15=findViewById(R.id.textView15)
        textView16=findViewById(R.id.textView16)
        textView17=findViewById(R.id.textView17)
        textView18=findViewById(R.id.textView18)
    }
    public fun setText(content1: String, content2: String, content3: String,content9:String,content15:String,content16:String,content17:String,content18:String) {
        textView1.text = content1
        textView2.text = content2
        textView3.text = content3
        textView9.text = content9
        textView15.text = content15
        textView16.text = content16
        textView17.text = content17
        textView18.text = content18
    }
}