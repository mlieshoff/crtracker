package crtracker;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;
import javax.security.auth.login.LoginException;
import crtracker.api.ApiWrapper;
import crtracker.bot.DiscordApi;
import crtracker.challenge.ChallengeDefinition;
import crtracker.challenge.RunningChallenge;
import crtracker.persistency.model.DecimalMeasure;
import crtracker.persistency.model.MeasureId;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Config {

    public static final boolean TEST = false;

    @Getter
    private final Properties config;

    @Getter
    private final Properties credentials;

    private SessionFactory sessionFactory;

    public void init() {
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
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public Session createSession() {
        return sessionFactory.openSession();
    }

    public DiscordApi createBot() throws LoginException, InterruptedException {
        return new DiscordApi(config, credentials);
    }

    public ApiWrapper createApiWrapper() {
        return new ApiWrapper(
                config.getProperty("official.cr.api.url"),
                credentials.getProperty("official.cr.api.token"),
                config.getProperty("royal.cr.api.url"),
                credentials.getProperty("royal.cr.api.token")
        );
    }

}
