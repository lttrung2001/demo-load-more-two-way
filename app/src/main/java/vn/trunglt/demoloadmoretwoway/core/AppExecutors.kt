package vn.trunglt.demoloadmoretwoway.core

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(
    private val diskIO: Executor,
    private val networkIO: Executor,
    private val mainThread: Executor
) {

    companion object {

        private lateinit var instance: AppExecutors

        fun getInstance(): AppExecutors {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = AppExecutors()
                }
                return instance
            }
        }
    }

    constructor() : this(
        Executors.newSingleThreadExecutor(),
        Executors.newFixedThreadPool(3),
        MainThreadExecutor()
    )


    fun diskIO(): Executor {
        return diskIO
    }

    fun networkIO(): Executor {
        return networkIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
