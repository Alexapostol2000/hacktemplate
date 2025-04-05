package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.BuildProgressListener
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities

class BuildFailureTracker(
    project: Project,
    private val failedBuildLabel: JBLabel,
    private val onHighFailureRate: (Int) -> Unit
) {
    private val failedBuildTimestamps = CopyOnWriteArrayList<Long>()
    private val buildViewManager = project.getService(BuildViewManager::class.java)

    init {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val now = System.currentTimeMillis()
                val oneMinuteAgo = now - 60_000
                failedBuildTimestamps.removeIf { it < oneMinuteAgo }

                SwingUtilities.invokeLater {
                    failedBuildLabel.text = "Failed builds (last 1 min): ${failedBuildTimestamps.size}"
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
                        failedBuildLabel.text = "Failed builds (last 1 min): $recentFails"

                        if (recentFails >= 10) {
                            onHighFailureRate(recentFails)
                        }
                    }
                }
            }
        }, project)
    }
}