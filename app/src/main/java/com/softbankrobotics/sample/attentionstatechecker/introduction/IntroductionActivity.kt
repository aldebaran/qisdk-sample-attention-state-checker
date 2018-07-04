package com.softbankrobotics.sample.attentionstatechecker.introduction

import android.os.Bundle
import android.view.View
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionImportance
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionValidity
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.sample.attentionstatechecker.R
import kotlinx.android.synthetic.main.activity_introduction.*

class IntroductionActivity : RobotActivity(), RobotLifecycleCallbacks {

    private var chat: Chat? = null
    private var qiChatbot: QiChatbot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        QiSDK.register(this, this)
    }

    override fun onResume() {
        super.onResume()
        hideStartButton()
    }

    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        val topic = TopicBuilder.with(qiContext)
                        .withResource(R.raw.introduction)
                        .build()

        val firstBookmark = topic.bookmarks["first"]

        qiChatbot = QiChatbotBuilder.with(qiContext)
                            .withTopic(topic)
                            .build()

        qiChatbot?.addOnBookmarkReachedListener {
            if (it.name == "second") {
                displayStartButton()
            }
        }

        chat = ChatBuilder.with(qiContext)
                    .withChatbot(qiChatbot)
                    .build()

        chat?.addOnStartedListener { qiChatbot?.goToBookmark(firstBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE) }

        chat?.async()?.run()
    }

    override fun onRobotFocusLost() {
        qiChatbot?.removeAllOnBookmarkReachedListeners()
        chat?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String?) {
        // Not used.
    }

    private fun displayStartButton() {
        runOnUiThread {
            startButton.apply {
                visibility = View.VISIBLE
            }
        }
    }

    private fun hideStartButton() {
        runOnUiThread {
            startButton.apply {
                isChecked = false
                visibility = View.INVISIBLE
            }
        }
    }
}
