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
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_introduction.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class IntroductionActivity : RobotActivity(), RobotLifecycleCallbacks {

    private var chat: Chat? = null
    private var qiChatbot: QiChatbot? = null
    private var startBookmark: Bookmark? = null
    private var secondBookmark: Bookmark? = null

    private var timerDisposable: Disposable? = null
    private val shouldRepeatWithTimer = AtomicBoolean(true)


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
        shouldRepeatWithTimer.set(true)

        val topic = TopicBuilder.with(qiContext)
                        .withResource(R.raw.introduction)
                        .build()

        val bookmarks = topic.bookmarks
        val firstBookmark = bookmarks[FIRST_BOOKMARK]
        secondBookmark = bookmarks[SECOND_BOOKMARK]
        startBookmark = bookmarks[START_BOOKMARK]

        qiChatbot = QiChatbotBuilder.with(qiContext)
                            .withTopic(topic)
                            .build()

        qiChatbot?.addOnBookmarkReachedListener {
            when (it.name) {
                SECOND_BOOKMARK -> displayStartButton()
                START_BOOKMARK -> checkStartButton()
                START_TIMER_BOOKMARK -> if (shouldRepeatWithTimer.getAndSet(false)) startTimer()
                STOP_TIMER_BOOKMARK -> stopTimer()
            }
        }

        chat = ChatBuilder.with(qiContext)
                    .withChatbot(qiChatbot)
                    .build()

        chat?.addOnStartedListener { goToBookmark(firstBookmark) }

        chat?.async()?.run()
    }

    override fun onRobotFocusLost() {
        stopTimer()

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

    private fun startTimer() {
        timerDisposable = Single.timer(5, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    stopTimer()
                    goToBookmark(secondBookmark)
                }
    }

    private fun stopTimer() {
        timerDisposable?.takeUnless { it.isDisposed }?.dispose()
    }

    private companion object {
        const val FIRST_BOOKMARK = "first"
        const val SECOND_BOOKMARK = "second"
        const val START_BOOKMARK = "start"
        const val START_TIMER_BOOKMARK = "start_timer"
        const val STOP_TIMER_BOOKMARK = "stop_timer"
    }
}
