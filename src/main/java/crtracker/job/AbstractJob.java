package crtracker.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractJob implements Job {

    private long lastExecutionTime;

    @Override
    public void run() throws Exception {
        if (mustRun()) {
            try {
                runIntern();
            } catch (Exception e) {
                throw e;
            } finally {
                lastExecutionTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public boolean mustRun() {
        return System.currentTimeMillis() > (lastExecutionTime + getTimeout());
    }

    protected abstract void runIntern() throws Exception;

}
