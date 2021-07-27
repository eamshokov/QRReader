package com.taeprog.qrreaderkit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout

class PreviewView(context: Context, attributes:AttributeSet): ConstraintLayout(context, attributes) {
    var flashlightTurnedOn:Boolean = false
        set(value) {
            field = value
        }

    var flashlightToggle:()->Unit = {}
        public set(value) {
            field = value
        }


    private val layout = inflate(context, R.layout.preview_view, this)
    internal val preview:PreviewView = layout.findViewById(R.id.camera_preview)
    internal val targetView: ImageView = layout.findViewById(R.id.target_view)
    private val flashlightIcon: ImageView = layout.findViewById(R.id.flash_light_image)
    //private val flashlightText: TextView = layout.findViewById(R.id.flash_state_text)
    init{
        flashlightIcon.setOnClickListener {
            flashlightTurnedOn = !flashlightTurnedOn
            if(flashlightTurnedOn){
                flashlightIcon.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }else{
                flashlightIcon.setImageResource(R.drawable.ic_baseline_flash_off_24)
            }
            flashlightToggle()
        }
    }

}