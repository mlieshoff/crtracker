package crtracker;

import org.apache.commons.io.FileUtils;
import org.mili.utils.sql.service.MigrationService;
import org.mili.utils.sql.service.ServiceException;
import org.mili.utils.sql.service.ServiceFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import crtracker.checks.DataImporter;
import crtracker.checks.DatabaseCheck;
import crtracker.checks.EnvironmentCheck;
import crtracker.checks.WebsiteGenerator;
import crtracker.job.Job;
import crtracker.migration.DuplicateRemover;
import crtracker.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CrTracker {

    private final String cipherFilename;
    private final String propertiesFilename;
    private final String credentialsFilename;
    private final String statusFilename;

    private final Properties properties = new Properties();
    private final Properties credentials = new Properties();

    private String cipher;

    private File statusFile;

    private Config config;

    private volatile boolean running;

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    public static void main(String[] args) {
        String cipherFilename = args[0];
        String propertiesFilename = args[1];
        String credentialsFilename = args[2];
        String statusFilename = args[3];
        CrTracker crTracker = new CrTracker(cipherFilename, propertiesFilename, credentialsFilename, statusFilename);
        try {
            crTracker.run();
        } catch (InterruptedException e) {
            //
        } catch (Exception e) {
            crTracker.messageService.sendAlert(crTracker.config, "Error while running: " + e.getMessage());
            log.error("Error while running", e);
        }
        System.exit(0);
    }

    private void run() throws Exception {
        log.info("boot up, load properties...");
        cipher = Utils.loadCipher(cipherFilename);
        Utils.loadProperties(properties, propertiesFilename);
        Utils.loadCredentials(credentials, cipher, credentialsFilename);
        statusFile = new File(statusFilename);
        assertIsNotRunning();
        setStatusRunning();
        running = true;
        log.info("started.");

        config = new Config(properties, credentials);
        config.init();

        messageService.sendAlert(config, "CrTracker started...");
        migrate();
        List<Job> jobs = new ArrayList<>();
        jobs.add(new EnvironmentCheck(config));
        jobs.add(new DataImporter(config));
        jobs.add(new WebsiteGenerator(config));
        jobs.add(new DatabaseCheck(config));
//        jobs.add(new ChallengeJob(config));
        while(running) {
            Thread.sleep(5000);
            for (Job job : jobs) {
                job.run();
            }
            stopIfNeeded();
        }
        setStatusStop();
        log.info("stopped.");
        messageService.sendAlert(config, "CrTracker stopped.");
    }

    private void migrate() throws ServiceException {
        ServiceFactory.getService(MigrationService.class).migrate(false, CrTracker.class);
//        new DuplicateRemover().run(config, config.getConfig().getProperty("crtracker.clan.tag"));
    }

    private void assertIsNotRunning() throws IOException {
        if (statusFile.exists()) {
            String status = FileUtils.readFileToString(statusFile);
            if ("RUNNING".equals(status) && !Config.TEST) {
                throw new IllegalStateException("crtracker is already running!");
            }
        }
    }

    private void setStatusRunning() throws IOException {
        FileUtils.write(statusFile, "RUNNING");
    }

    private void stopIfNeeded() {
        if (!statusFile.exists()) {
            running = false;
        }
    }

    private void setStatusStop() throws IOException {
        FileUtils.write(statusFile, "STOPPED");
    }

}
