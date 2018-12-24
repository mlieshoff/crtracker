package crtracker.job;

public interface Job {

    long getTimeout();

    boolean mustRun();

    void run() throws Exception;
}
