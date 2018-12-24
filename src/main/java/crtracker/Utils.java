package crtracker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Utils {

    public static void loadSystemProperties(File file) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            properties.load(fileInputStream);
        }
        System.getProperties().putAll(properties);
    }

    public static String loadCipher(String cipherFilename) throws IOException {
        return FileUtils.readFileToString(new File(cipherFilename));
    }

    public static void loadProperties(Properties properties, String propertiesFilename) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFilename)) {
            properties.load(fileInputStream);
        }
    }

    public static void loadCredentials(Properties credentialsProperties, String cipher, String credentialsFilename)
            throws Exception {
        byte[] encryptedFile = FileUtils.readFileToByteArray(new File(credentialsFilename));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        decryptOrEncrypt(cipher, Cipher.DECRYPT_MODE, new ByteArrayInputStream(encryptedFile), byteArrayOutputStream);
        String decryptedFile = new String(byteArrayOutputStream.toByteArray());
        credentialsProperties.load(new ByteArrayInputStream(decryptedFile.getBytes()));
    }

    public static void saveCredentials(Properties credentialsProperties, String cipher, String credentialsFilename)
            throws Exception {
        StringWriter stringWriter = new StringWriter();
        credentialsProperties.store(stringWriter, null);
        String decryptedFile = stringWriter.toString();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        decryptOrEncrypt(cipher, Cipher.ENCRYPT_MODE, new ByteArrayInputStream(decryptedFile.getBytes()), byteArrayOutputStream);
        FileUtils.writeByteArrayToFile(new File(credentialsFilename), byteArrayOutputStream.toByteArray());
    }

    private static void decryptOrEncrypt(String key, int mode, InputStream inputStream, OutputStream outputStream) throws Exception {
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, desKey);
        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(Cipher.ENCRYPT_MODE, desKey);
            copy(new CipherInputStream(inputStream, cipher), outputStream);
        } else if (mode == Cipher.DECRYPT_MODE) {
            cipher.init(Cipher.DECRYPT_MODE, desKey);
            copy(inputStream, new CipherOutputStream(outputStream, cipher));
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[64];
        int size;
        while ((size = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, size);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public static void main(String[] args) throws Exception {
        String key = FileUtils.readFileToString(new File("../server_credentials/conf/crtracker/key"));
        Properties credentials = new Properties();
        loadProperties(credentials, "../server_credentials/conf/crtracker/credentials.properties");
        saveCredentials(credentials, key, "../server_credentials/conf/crtracker/encrypted/credentials.properties");
        credentials.clear();
        loadProperties(credentials, "../server_credentials/conf/crtracker/test_credentials.properties");
        saveCredentials(credentials, key, "../server_credentials/conf/crtracker/encrypted/test_credentials.properties");
    }

    public static Pair<DateTime, DateTime> getCalendarWeekFromTo(Date date) {
        return getCalendarWeekFromTo(new DateTime(date));
    }

    public static Pair<DateTime, DateTime> getCalendarWeekFromTo(DateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek();
        DateTime weekBegin = dateTime.minusDays(dayOfWeek - 1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        DateTime weekEnd = dateTime.plusDays(7 - dayOfWeek).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        return new ImmutablePair<>(weekBegin, weekEnd);
    }

    public static boolean isInRange(DateTime now, Pair<DateTime, DateTime> startAndEnd) {
        return isInRange(now, startAndEnd.getLeft(), startAndEnd.getRight());
    }

    public static boolean isInRange(DateTime now, DateTime start, DateTime end) {
        return now.isAfter(start) && now.isBefore(end);
    }

}
