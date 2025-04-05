package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.BuildProgressListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities

class BuildFailureTracker(
    private val project: Project,
    private val label: JBLabel
) {
    private val failedBuildTimestamps = CopyOnWriteArrayList<Long>()

    init {
        val buildViewManager = project.getService(BuildViewManager::class.java)

        // Timer pentru actualizare UI și curățare builduri vechi
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val now = System.currentTimeMillis()
                val oneMinuteAgo = now - 60_000
                failedBuildTimestamps.removeIf { it < oneMinuteAgo }

                SwingUtilities.invokeLater {
                    label.text = "Failed builds (last 1 min): ${failedBuildTimestamps.size}"
                }
            }
        }, 0, 5_000)

        buildViewManager.addListener(object : BuildProgressListener {
            override fun onEvent(buildId: Any, event: BuildEvent) {
                if (event is FinishBuildEvent && event.result is FailureResult) {
                    val now = System.currentTimeMillis()
                    failedBuildTimestamps.add(now)

                    val oneMinuteAgo = now - 60_000
                    failedBuildTimestamps.removeIf { it < oneMinuteAgo }

                    SwingUtilities.invokeLater {
                        val recentFails = failedBuildTimestamps.size
                        label.text = "Failed builds (last 1 min): $recentFails"

                        if (recentFails >= 10) {
                            Messages.showInfoMessage(
                                project,
                                "You've had $recentFails failed builds in the last minute.\nTake a breather! Stretch, relax, or get some ☕️",
                                "Time for a Break?"
                            )
                            failedBuildTimestamps.clear()
                            label.text = "Failed builds (last 1 min): 0"
                        }
                    }
                }
            }
        }, project)
    }
}
