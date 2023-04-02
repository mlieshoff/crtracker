package crtracker.plugins.environment;

import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.messaging.AlertPluginEvent;

@Service
public class EnvironmentCheckPlugin extends AbstractPlugin {

  private static final long ONE_MEGABYTE_IN_BYTES = 1024 << 10;

  @Scheduled(initialDelay = 60000, fixedDelay = 60000 * 15)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) throws Exception {
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
    eventBus.fire(new AlertPluginEvent(s.toString()));
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
    try (Scanner scanner = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    }
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
    // what the hell?
    while (getGcCount() == before) {
      //
    }
    return getCurrentlyAllocatedMemory();
  }

  static long getCurrentlyAllocatedMemory() {
    Runtime runtime = Runtime.getRuntime();
    return (runtime.totalMemory() - runtime.freeMemory()) / (ONE_MEGABYTE_IN_BYTES);
  }

}
