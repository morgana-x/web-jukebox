package net.morgana.signedjukebox;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SignPlayer {

    static Path TempFolder;

    public static boolean DoCustomJukebox(World world, int x, int y, int z)
    {
        if (!(world.getBlockEntity(x, y, z) instanceof JukeboxBlockEntity))
            return false;

        URL url = null;
        for (int bx = -1; bx < 2; bx++)
            for (int by = -1; by < 2; by++)
                for (int bz = -1; bz < 2; bz++)
                {
                    if (!(world.getBlockEntity(x + bx, y + by, z + bz) instanceof SignBlockEntity sign))
                        continue;

                    String tempurl = "";
                    for (var t: sign.texts) {
                        tempurl += t;
                    }
                    System.out.println("Trying to apply " + tempurl);
                    try
                    {
                        if (!tempurl.contains("://"))
                            tempurl = "http://" + tempurl;
                        url = new URL(tempurl);
                        bx = 2;
                        by = 2;
                        break;
                    }
                    catch (MalformedURLException e)
                    {
                        System.out.println(e.toString());
                    }
                }

        if (url == null)
            return false;

        System.out.println("Trying to download " + url);

        downloadAndPlaySong(world, x, y, z, url);

        return true;
    }

    static String[] ValidExt = new String[]{"mp3", "ogg", "mid", "mus"};
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

        System.out.println(ext);

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
                System.out.println(e.toString());
                return;
            }
        }

        try {

            var path = new File(TempFolder.toString() + "/song."+ext);

            FileUtils.copyURLToFile(url, path);

            world.playStreaming("music://" + path.toString(), x, y, z);
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
    }
}
