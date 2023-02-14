package crtracker.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.mili.utils.Lambda;
import org.mili.utils.sql.service.ServiceException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FtpService extends BaseService {

  public void upload(String host, int port, String username, String password, String filename, String content)
      throws ServiceException {
    doInService((Lambda<Void>) params -> {
      uploadInternally(host, port, username, password, content, filename);
      return null;
    });
  }

  private void uploadInternally(String host, int port, String username, String password, String content,
                                String filename) {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(host, port);
      ftpClient.login(username, password);
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      InputStream inputStream = new ByteArrayInputStream(content.getBytes(UTF_8));
      boolean done = ftpClient.storeFile(filename, inputStream);
      log.debug("FTP upload of {} to {} result was: {}", filename, host, done);
      inputStream.close();
    } catch (IOException ex) {
      log.warn("FTP upload failed", ex);
    } finally {
      try {
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
      } catch (IOException ex) {
        log.warn("Closing FTP client failed", ex);
      }
    }
  }

}
