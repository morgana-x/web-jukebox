package net.morgana.signedjukebox;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class SignPlayer {

    static Path TempFolder;



    public static boolean DoCustomJukebox(World world, int x, int y, int z)
    {
        if (!(world.getBlockEntity(x, y, z) instanceof JukeboxBlockEntity))
            return false;


        BlockPos signPos = SignUtil.getAttachedSign(world, x, y, z);
        if (signPos == null)
            return false;

        String totalSignText = SignUtil.getMultiSignText(world, signPos.x, signPos.y, signPos.z, false);

        URL url;

        System.out.println("Trying to apply " + totalSignText);
        try
        {
            if (!totalSignText.contains("://"))
                totalSignText = "http://" + totalSignText;
            url = new URL(totalSignText);
        }
        catch (MalformedURLException e)
        {
            System.out.println(e);
            return false;
        }

        System.out.println("Trying to download " + url);

        downloadAndPlaySong(world, x, y, z, url);

        return true;
    }

    static String[] ValidExt = new String[]{"ogg", "mid", "mus", "wav"};
    private static String getValidFileExtension(URL url)
    {
        String file = url.getFile();

        System.out.println(file);

        if (!file.contains("."))
            return null;

        var i = file.lastIndexOf(".");
        if (i == -1)
            return null;

        var ext = file.substring(i).toLowerCase();


        ext = ext.replace("/", "").replace("\\", "").replace(".", "");

        for (var s:ValidExt) {
            if (s.equals(ext))
                return ext;
        }

        return null;
    }


    private static void downloadAndPlaySong(World world, int x, int y, int z, URL url) {

        var ext = getValidFileExtension(url);
        if (ext == null)
            return;

        System.out.println("Ext: " + ext);

        if (TempFolder == null) {
            try {
                TempFolder = Files.createTempDirectory("b1.7.3.music");
            }
            catch (IOException e)
            {
                System.out.println(e);
                return;
            }
        }


        var path = new File(TempFolder.toString() + "/song."+ext);

        Thread thread = new Thread( () ->{
                // https://stackoverflow.com/questions/69861993/java-asynchronously-wait-x-seconds
                try {
                    SongDownloader.Download(url, path);

                    Thread.sleep(1000);

                    String title = URLDecoder.decode(new File(url.toString()).getName(), Charset.defaultCharset())
                            .replace("/", "").
                            replace("\\", "")
                                    .replace("." + ext, "");

                    world.playStreaming("music://" + path + "title://" + title, x, y, z);
                }
                catch (InterruptedException e) {
                    // See https://www.javaspecialists.eu/archive/Issue056-Shutting-down-Threads-Cleanly.html
                    Thread.currentThread().interrupt();
                }
                catch (IOException e)
                {
                    System.out.println(e);
                }
        });

        thread.start();

    }
}
