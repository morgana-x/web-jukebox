package net.morgana.signedjukebox;

import org.apache.commons.io.FileUtils;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongDownloader {

    static Path TempFolder;

    // Valid but needs to be converted
    static String[] ConvertToOgg = {"mp3", "flac"};

    // Valid extensions supported by pauls code  sound lib
    static String[] ValidExt = new String[]{"ogg", "mid", "mus", "wav"};

    public static String getValidFileExtension(String file)
    {
        var i = file.lastIndexOf(".");
        if (i == -1)
            return null;

        var ext = file.substring(i).toLowerCase();


        ext = ext.replace("/", "").replace("\\", "").replace(".", "");

        for (var s:ValidExt) {
            if (s.equals(ext))
                return ext;
        }

        for (var s:ConvertToOgg) {
            if (s.equals(ext))
                return ext;
        }

        return null;
    }

    public static boolean needsConversion(String file)
    {

        var ext = getValidFileExtension(file);

        for (var s:ConvertToOgg) {
            if (s.equals(ext))
                return true;
        }

        return false;
    }


    public static File Download(URL url) throws IOException, InterruptedException {
        var ext = getValidFileExtension(url.getFile());
        if (ext == null)
            return null;

        System.out.println("Ext: " + ext);

        if (TempFolder == null) {
            try {
                TempFolder = Files.createTempDirectory("b1.7.3.music");

                //https://stackoverflow.com/questions/15022219/does-files-createtempdirectory-remove-the-directory-after-jvm-exits-normally
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            FileUtils.deleteDirectory(TempFolder.toFile());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            catch (IOException e)
            {
                System.out.println(e);
                return null;
            }
        }

        var path = new File(TempFolder.toString() + "/song."+ext);

        return Download(url, path);
    }

    // Returns new path in-case conversion to ogg was required
    public static File Download(URL url, File path) throws IOException, InterruptedException {
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


        if (needsConversion(path.getName()))
        {
            Thread.sleep(1000);

            File newPath = new File(TempFolder.toString() + "/song.ogg");

            System.out.println("Converting " + path + " to " + newPath + "...");

            ProcessBuilder ffmpeg = new ProcessBuilder(
                    "ffmpeg", "-i", path.getAbsolutePath(), "-vn", "-y", newPath.getAbsolutePath()
            );

            ffmpeg.inheritIO();

            try {
                Process p = ffmpeg.start();

                int c = p.waitFor();
                if (c != 0) {
                    System.out.println("INSTALL FFMPEG if you want to use mp3, flac streams etc");
                    return null;
                }
                System.out.println("Converted to " + newPath + "!");
                return newPath;
            }
            catch (IOException e)
            {
                System.out.println(e);
                return null;
            }
            catch (Exception e)
            {
                System.out.println(e);
                return null;
            }
        }


        return path;
    }
}
