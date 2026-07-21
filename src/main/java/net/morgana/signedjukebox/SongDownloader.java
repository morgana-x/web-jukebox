package net.morgana.signedjukebox;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SongDownloader {

    public static void Download(URL url, File path) throws IOException {
        URL foundUrl = url;

        HttpURLConnection connection;
        for (; ; ) {
            connection = (HttpURLConnection) foundUrl.openConnection();
            connection.setInstanceFollowRedirects(false);
            String redirectLocation = connection.getHeaderField("Location");
            if (redirectLocation == null) break;
            foundUrl = new URL(redirectLocation);
        }

        FileUtils.copyURLToFile(foundUrl, path);
    }
}
