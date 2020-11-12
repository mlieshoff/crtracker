package crtracker.service;

import org.mili.utils.Lambda;
import org.mili.utils.sql.service.ServiceException;

public class BaseService {

  public <T> T doInService(Lambda<T> lambda, Object... objects) throws ServiceException {
    T result;
    try {
      result = lambda.exec(objects);
    } catch (Exception var9) {
      throw new ServiceException(var9);
    }
    return result;
  }

}
