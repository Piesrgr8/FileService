package jared.ballstate.edu.server;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedEchoServer {

    static final int PORT = 1978;
    private static Socket sc;

    public static void main(String args[]) {
    	// newSingleThreadExecutor creates a thread pool with a single thread.
        // If multiple threads are needed, use:
        //   newFixedThreadPool(numThreads)
        ExecutorService p = Executors.newSingleThreadExecutor();
        p.submit(new EchoThread(sc));
        System.out.println("Main thread submitted the task.");
    }
}
