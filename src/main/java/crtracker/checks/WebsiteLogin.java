package crtracker.checks;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.mili.utils.crawler.Crawler2;
import org.mili.utils.crawler.GetRequest;
import org.mili.utils.crawler.Response;
import org.mili.utils.crawler.StringPostRequest;
import org.mili.utils.sql.service.ServiceFactory;
import org.mili.utils.text.TextProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import crtracker.Config;
import crtracker.job.AbstractJob;
import crtracker.service.MessageService;

public class WebsiteLogin extends AbstractJob {

  private final Config config;

  private final MessageService messageService = ServiceFactory.getService(MessageService.class);

  private final Crawler2 crawler2;

  private final String loginUrl;

  public WebsiteLogin(Config config) {
    this.config = config;
    loginUrl = config.getConfig().getProperty("website.provider.url") + "login";
    crawler2 = new Crawler2();
  }

  @Override
  protected void runIntern() throws Exception {
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
              .param("user_session[username]", config.getCredentials().getProperty("website.provider.username"))
              .param("user_session[password]", config.getCredentials().getProperty("website.provider.password"))
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
      messageService.sendAlert(config, "Successful login to lima-city.");
    } else {
      messageService.sendAlert(config, "Problems while login to lima-city:\n" + problems);
    }
  }

  private String getMetaContent(String metaName, String content) {
    AtomicReference<String> value = new AtomicReference<>();
    TextProcessing.builder().one("name=\"" + metaName + "\" content=\"", "\"", s -> value.set(s)).build()
        .start(content);
    return value.get();
  }

  @Override
  public long getTimeout() {
    return 60000 * 60 * (int) (Math.random() + 1 * 24);
  }

}
