package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.BuildProgressListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(project: Project) {

        private val failedBuildTimestamps = CopyOnWriteArrayList<Long>()
        private val failedBuildLabel = JBLabel("Failed builds (last 1 min): 0")

        init {
            val buildViewManager = project.getService(BuildViewManager::class.java)

            // Timer pentru curățare periodică și actualizare UI
            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val now = System.currentTimeMillis()
                    val oneMinuteAgo = now - 60_000
                    failedBuildTimestamps.removeIf { it < oneMinuteAgo }

                    SwingUtilities.invokeLater {
                        failedBuildLabel.text = "Failed builds (last 1 min): ${failedBuildTimestamps.size}"
                    }
                }
            }, 0, 5_000) // rulează la fiecare 5 secunde

            buildViewManager.addListener(object : BuildProgressListener {
                override fun onEvent(buildId: Any, event: BuildEvent) {
                    if (event is FinishBuildEvent && event.result is FailureResult) {
                        val now = System.currentTimeMillis()
                        failedBuildTimestamps.add(now)

                        // curățăm imediat după adăugare
                        val oneMinuteAgo = now - 60_000
                        failedBuildTimestamps.removeIf { it < oneMinuteAgo }

                        SwingUtilities.invokeLater {
                            val recentFails = failedBuildTimestamps.size
                            failedBuildLabel.text = "Failed builds (last 1 min): $recentFails"

                            if (recentFails >= 10) {
                                Messages.showInfoMessage(
                                    project,
                                    "You've had $recentFails failed builds in the last minute.\nTake a breather! Stretch, relax, or get some ☕️",
                                    "Time for a Break?"
                                )
                                failedBuildTimestamps.clear()
                                failedBuildLabel.text = "Failed builds (last 1 min): 0"
                            }
                        }
                    }
                }
            }, project)
        }

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            add(failedBuildLabel)
        }
    }
}
