package com.yorhp.luckmoney.util.threadpool;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 *
 * @author dhht
 */
public class AppExecutors {

    private final ExecutorService diskIO;
    private final ExecutorService networkIO;
    private final Executor mainThread;
    private final ScheduledExecutorService mExecutorService;

    private AppExecutors() {
        diskIO = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("DiskIoThreadExecutor"));

        networkIO = new ThreadPoolExecutor(
                3,
                3,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("NetworkIOExecutor"));
        mainThread = new MainThreadExecutor();
        mExecutorService = new ScheduledThreadPoolExecutor(1, new MyThreadFactory("ScheduledExecutorService"));
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public ExecutorService networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public ScheduledExecutorService scheduledExecutorService() {
        return mExecutorService;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }


    public static AppExecutors getInstance() {
        return Holder.sAppExecutors;
    }

    static class Holder {
        static AppExecutors sAppExecutors = new AppExecutors();
    }

}
