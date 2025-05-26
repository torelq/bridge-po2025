package tcs.bridge.server;

public class Server {

    // Should be executed in a dedicated thread
    private void run() {
        //
    }

    public Thread runInNewThread() {
        Thread thread = new Thread(this::run);
        thread.start();
        return thread;
    }
}
