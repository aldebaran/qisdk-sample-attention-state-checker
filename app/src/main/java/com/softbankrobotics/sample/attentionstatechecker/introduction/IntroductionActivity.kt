package com.softbankrobotics.sample.attentionstatechecker.introduction

import android.os.Bundle
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.sample.attentionstatechecker.R

class IntroductionActivity : RobotActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
    }
}
