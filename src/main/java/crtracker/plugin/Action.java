package crtracker.plugin;

import org.hibernate.Session;

@FunctionalInterface
interface Action {

  void execute(Session session) throws Exception;

}
