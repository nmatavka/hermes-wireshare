package org.limewire.ui.compose.integration

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

private val composeBackgroundExecutor: ExecutorService = Executors.newCachedThreadPool(
    ComposeBackgroundThreadFactory()
)

fun runOnComposeBackground(task: () -> Unit) {
    composeBackgroundExecutor.execute(task)
}

private class ComposeBackgroundThreadFactory : ThreadFactory {
    private val threadIds = AtomicInteger(1)

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, "compose-bg-${threadIds.getAndIncrement()}").apply {
            isDaemon = true
        }
    }
}
