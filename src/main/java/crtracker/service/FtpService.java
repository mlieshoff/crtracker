package crtracker.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.mili.utils.Lambda;
import org.mili.utils.sql.service.Service;
import org.mili.utils.sql.service.ServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpService extends Service {

    public void upload(final String host, final int port, final String username, final String password, String filename, String content) throws ServiceException {
        doInService(new Lambda<Void>() {
            public Void exec(Object... params) throws Exception {
                FTPClient ftpClient = new FTPClient();
                try {
                    ftpClient.connect(host, port);
                    ftpClient.login(username, password);
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
                    boolean done = ftpClient.storeFile(filename, inputStream);
                    inputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (ftpClient.isConnected()) {
                            ftpClient.logout();
                            ftpClient.disconnect();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

}
