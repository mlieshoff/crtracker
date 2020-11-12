package crtracker.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Service;

import java.util.Properties;
import crtracker.util.CipherUtil;
import crtracker.integration.ApiWrapper;
import crtracker.persistency.model.DecimalMeasure;
import crtracker.persistency.model.MeasureId;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.plugins.challenge.ChallengeDefinition;
import crtracker.plugins.challenge.RunningChallenge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService {

  @Getter
  public static final boolean TEST = Boolean.getBoolean("test");

  @Getter
  private final Properties config = new Properties();

  @Getter
  private final Properties credentials = new Properties();

  private String cipher;

  private SessionFactory sessionFactory;

  public void initialize(String cipherFilename, String propertiesFilename, String credentialsFilename)
      throws Exception {
    log.info("initialize...");

    cipher = CipherUtil.loadCipher(cipherFilename);
    CipherUtil.loadProperties(config, propertiesFilename);
    CipherUtil.loadCredentials(credentials, cipher, credentialsFilename);

    System.setProperty("org.mili.database.host", config.getProperty("database.host"));
    System.setProperty("org.mili.database.port", config.getProperty("database.port"));
    System.setProperty("org.mili.database.username", credentials.getProperty("database.username"));
    System.setProperty("org.mili.database.password", credentials.getProperty("database.password"));
    System.setProperty("org.mili.database.url", config.getProperty("database.url"));

    Configuration configuration = new AnnotationConfiguration();
    configuration.addAnnotatedClass(NumberMeasure.class);
    configuration.addAnnotatedClass(DecimalMeasure.class);
    configuration.addAnnotatedClass(StringMeasure.class);
    configuration.addAnnotatedClass(TextMeasure.class);
    configuration.addAnnotatedClass(MeasureId.class);
    configuration.addAnnotatedClass(ChallengeDefinition.class);
    configuration.addAnnotatedClass(RunningChallenge.class);
    configuration.setProperty("hibernate.connection.driver_class", config.getProperty("database.driver"));
    configuration.setProperty("hibernate.connection.url", config.getProperty("database.url"));
    configuration.setProperty("hibernate.connection.dialect", config.getProperty("database.dialect"));
    configuration.setProperty("hibernate.connection.username", credentials.getProperty("database.username"));
    configuration.setProperty("hibernate.connection.password", credentials.getProperty("database.password"));
    configuration.setProperty("hibernate.show_sql", config.getProperty("database.show.sql"));

    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties()).build();

    sessionFactory = configuration.buildSessionFactory(serviceRegistry);

    log.info("initialized.");
  }

  public ApiWrapper createApiWrapper() {
    return new ApiWrapper(
        config.getProperty("proxy.cr.api.url"),
        credentials.getProperty("proxy.cr.api.token"),
        config.getProperty("official.cr.api.url"),
        credentials.getProperty("official.cr.api.token")
    );
  }

  public String getClanTag() {
    return getConfig().getProperty("crtracker.clan.tag");
  }

  public Session createSession() {
    return sessionFactory.openSession();
  }

}
