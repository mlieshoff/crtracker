package crtracker.plugins.website;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.hibernate.Session;
import org.mili.utils.crawler.Crawler2;
import org.mili.utils.crawler.GetRequest;
import org.mili.utils.crawler.Response;
import org.mili.utils.crawler.StringPostRequest;
import org.mili.utils.text.TextProcessing;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.messaging.AlertPluginEvent;

@Service
public class WebsiteLoginCheckPlugin extends AbstractPlugin {

  @Scheduled(initialDelay = 60000, fixedDelayString = "#{new Double((T(java.lang.Math).random() + 1) * 24).intValue() * (60000 * 60)}")
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) {
    String loginUrl = configurationService.getConfig().getProperty("website.provider.url") + "login";
    Crawler2 crawler2 = new Crawler2();
    List<String> problems = new ArrayList<>();
    Response response = crawler2.get(GetRequest.builder().uri(loginUrl).build());
    if (response.isError()) {
      problems.add("Error while get login page: " + response.getStatusCode());
    } else {
      String content = response.getString("UTF-8");
      String csrfParam = getMetaContent("csrf-param", content);
      if (isBlank(csrfParam)) {
        problems.add("'csrf-param' could not found!");
      } else {
        String csrfContent = getMetaContent("csrf-token", content);
        if (isBlank(csrfContent)) {
          problems.add("'csrf-token' could not found!");
        } else {
          response = crawler2.post(StringPostRequest.builder().uri(loginUrl)
              .param("utf8", "✓")
              .param(csrfParam, csrfContent)
              .param("user_session[username]",
                  configurationService.getCredentials().getProperty("website.provider.username"))
              .param("user_session[password]",
                  configurationService.getCredentials().getProperty("website.provider.password"))
              .param("user_session[remember_me]", "0")
              .param("commit", "Login")
              .header("content-type", "application/x-www-form-urlencoded")
              .build());
          if (response.isError()) {
            problems.add("Error while doing login: " + response.getStatusCode());
          }
        }
      }
    }
    if (problems.isEmpty()) {
      eventBus.fire(new AlertPluginEvent("Successful login to lima-city."));
    } else {
      eventBus.fire(new AlertPluginEvent("Problems while login to lima-city:\n" + problems));
    }
  }

  private String getMetaContent(String metaName, String content) {
    AtomicReference<String> value = new AtomicReference<>();
    TextProcessing.builder().one("name=\"" + metaName + "\" content=\"", "\"", value::set).build()
        .start(content);
    return value.get();
  }

}
