package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.build.BuildViewManager
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.BuildProgressListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import javax.swing.SwingUtilities

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(project: Project) {

        private var failedBuildCount = 0
        private val failedBuildLabel = JBLabel("Failed builds: $failedBuildCount")

        init {
            val buildViewManager = project.getService(BuildViewManager::class.java)
            buildViewManager.addListener(object : BuildProgressListener {
                override fun onEvent(buildId: Any, event: BuildEvent) {
                    if (event is FinishBuildEvent && event.result is FailureResult) {
                        failedBuildCount++
                        SwingUtilities.invokeLater {
                            failedBuildLabel.text = "Failed builds: $failedBuildCount"
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
