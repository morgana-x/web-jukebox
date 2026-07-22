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


    private static String stripExtension(String name)
    {
        if (!name.contains("."))
            return name;

        return name.substring(0, name.lastIndexOf("."));
    }

    private static void downloadAndPlaySong(World world, int x, int y, int z, URL url) {

        Thread thread = new Thread( () ->{
                // https://stackoverflow.com/questions/69861993/java-asynchronously-wait-x-seconds
                try {
                    File newPath = SongDownloader.Download(url);

                    if (newPath == null)
                        return;

                    Thread.sleep(1000);

                    String title = stripExtension(URLDecoder.decode(new File(url.toString()).getName(), Charset.defaultCharset())
                            .replace("/", "").
                            replace("\\", ""));

                    world.playStreaming("music://" + newPath + "title://" + title, x, y, z);
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
