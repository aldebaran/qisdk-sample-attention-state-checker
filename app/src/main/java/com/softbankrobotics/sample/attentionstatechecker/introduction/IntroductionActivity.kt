package com.softbankrobotics.sample.attentionstatechecker.introduction

import android.os.Bundle
import android.view.View
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.sample.attentionstatechecker.R
import kotlinx.android.synthetic.main.activity_introduction.*

class IntroductionActivity : RobotActivity(), RobotLifecycleCallbacks {

    private var chat: Chat? = null
    private var qiChatbot: QiChatbot? = null
    private var startBookmark: Bookmark? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        startButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                goToBookmark(startBookmark)
            }
        }

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

        val bookmarks = topic.bookmarks
        val firstBookmark = bookmarks[FIRST_BOOKMARK]
        startBookmark = bookmarks[START_BOOKMARK]

        qiChatbot = QiChatbotBuilder.with(qiContext)
                            .withTopic(topic)
                            .build()

        qiChatbot?.addOnBookmarkReachedListener {
            when (it.name) {
                SECOND_BOOKMARK -> displayStartButton()
                START_BOOKMARK -> checkStartButton()
            }
        }

        chat = ChatBuilder.with(qiContext)
                    .withChatbot(qiChatbot)
                    .build()

        chat?.addOnStartedListener { goToBookmark(firstBookmark) }

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

    private fun checkStartButton() {
        runOnUiThread {
            startButton.apply {
                isChecked = true
            }
        }
    }

    private fun goToBookmark(bookmark: Bookmark?) {
        qiChatbot?.async()?.goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
    }

    private companion object {
        const val FIRST_BOOKMARK = "first"
        const val SECOND_BOOKMARK = "second"
        const val START_BOOKMARK = "start"
    }
}
