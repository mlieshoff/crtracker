package crtracker.checks;

import org.mili.utils.sql.service.ServiceFactory;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import crtracker.Config;
import crtracker.service.MessageService;
import crtracker.job.AbstractJob;

public class EnvironmentCheck extends AbstractJob {

    private final Config config;

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    public EnvironmentCheck(Config config) {
        this.config = config;
    }

    @Override
    protected void runIntern() throws Exception {
        String diskSpace = execCmd("df -h");
        String memory = execCmd("free");
        String job = execCmd("ps aux | grep java");
        String javaMemory = getJavaMemory();
        StringBuilder s = new StringBuilder();
        s.append("* Disk space:\n");
        s.append(diskSpace);
        s.append("\n");
        s.append("* Memory:\n");
        s.append(memory);
        s.append("\n");
        s.append("* Job:\n");
        s.append(job);
        s.append("* Java:\n");
        s.append(javaMemory);
        s.append("\n");
        messageService.sendAlert(config, s.toString());
    }

    private static String getJavaMemory() {
        StringBuilder s = new StringBuilder();
        s.append("        GC count        : " + getGcCount());
        s.append("\n");
        s.append("Allocated Memory (in MB): " + getCurrentlyAllocatedMemory());
        s.append("\n");
        s.append("     Used Memory (in MB): " + getReallyUsedMemory());
        return s.toString();
    }

    public static String execCmd(String cmd) throws IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public long getTimeout() {
        return 60000 * 15;
    }

    static long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    static long getReallyUsedMemory() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before);
        return getCurrentlyAllocatedMemory();
    }

    static long getCurrentlyAllocatedMemory() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}
