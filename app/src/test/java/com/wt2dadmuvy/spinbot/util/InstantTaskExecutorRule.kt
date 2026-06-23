package com.wt2dadmuvy.spinbot.util

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Hace que LiveData ejecute sus observers de forma síncrona en tests.
 * Equivalente a androidx.arch.core:core-testing sin necesidad de descargar la librería,
 * ya que ArchTaskExecutor viene incluida como dependencia de lifecycle-livedata-ktx.
 */
class InstantTaskExecutorRule : TestWatcher() {

    override fun starting(description: Description) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
    }

    override fun finished(description: Description) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
