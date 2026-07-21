package net.morgana.signedjukebox;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.util.math.Vec3i;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SignPlayer {

    static Path TempFolder;

    static BlockPos getAttachedSign(World world, int x, int y, int z) {
        for (int bx = -1; bx < 2; bx++)
                for (int bz = -1; bz < 2; bz++) {
                    int t = Math.abs(bx) + Math.abs(bz);
                    if (t == 0)
                        continue;
                    if (t == 2)
                        continue;

                    int px = x + bx;
                    int pz = z + bz;

                    if (world.getBlockEntity(px, y, pz) instanceof SignBlockEntity sign)
                        return new BlockPos(px, y, pz);

                }
        return null;
    }


    static String getTotalText(SignBlockEntity sign) {
        String result = "";

        for (var t: sign.texts)
            result += t;
        return result;
    }

    static Vec3i getDir(SignBlockEntity sign)
    {
        switch(sign.getPushedBlockData())
        {
            case 2:
                return new Vec3i(0, 0, 1);
            case 4:
                return new Vec3i(1, 0, 0);
            case 5:
                return new Vec3i(-1,0,0);
            default:
                return new Vec3i(0, 0, -1);
        }
    }

    public static boolean DoCustomJukebox(World world, int x, int y, int z)
    {
        if (!(world.getBlockEntity(x, y, z) instanceof JukeboxBlockEntity))
            return false;


        BlockPos signPos = getAttachedSign(world, x, y, z);
        if (signPos == null)
            return false;

        SignBlockEntity signEntity = (SignBlockEntity)world.getBlockEntity(signPos.x, signPos.y, signPos.z);

        Vec3i dir = getDir(signEntity);
        Vec3i right = new Vec3i(dir.getZ(), 0, -dir.getX());

        System.out.println(dir);

        Vec3i leftMost = new Vec3i(signPos.x, signPos.y, signPos.z);
        for (int i = 0; i < 3; i++)
        {
            System.out.println("Left " + i);

            Vec3i next = leftMost.add(right);
            if (!(world.getBlockEntity(next.getX(), next.getY(), next.getZ()) instanceof SignBlockEntity sign &&dir.equals(getDir(sign))))
                break;
            leftMost = next;
        }

        String totalSignText = "";

        Vec3i nextSign = leftMost;
        for (int i = 0; i < 7; i++)
        {
            if (!(world.getBlockEntity(nextSign.getX(), nextSign.getY(), nextSign.getZ()) instanceof SignBlockEntity sign && dir.equals(getDir(sign))))
                break;

            System.out.println("Right " + i);

            totalSignText += getTotalText(sign);

            nextSign = nextSign.add(right.multiply(-1));

        }

        URL url = null;

        System.out.println("Trying to apply " + totalSignText);
        try
        {
            if (!totalSignText.contains("://"))
                totalSignText = "http://" + totalSignText;
            url = new URL(totalSignText);
        }
        catch (MalformedURLException e)
        {
            System.out.println(e.toString());
            return false;
        }

        System.out.println("Trying to download " + url);

        downloadAndPlaySong(world, x, y, z, url);

        return true;
    }

    static String[] ValidExt = new String[]{"ogg", "mid", "mus"};
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
                System.out.println(e.toString());
                return;
            }
        }


        var path = new File(TempFolder.toString() + "/song."+ext);

        Thread thread = new Thread() {
            public void run() {
                // https://stackoverflow.com/questions/69861993/java-asynchronously-wait-x-seconds
                try {
                    SongDownloader.Download(url, path);

                    Thread.sleep(1000);

                    world.playStreaming("music://" + path.toString(), x, y, z);
                }
                catch (InterruptedException e) {
                    // See https://www.javaspecialists.eu/archive/Issue056-Shutting-down-Threads-Cleanly.html
                    Thread.currentThread().interrupt();
                }
                catch (IOException e)
                {
                    System.out.println(e);
                }

            }
        };
        thread.start();

    }
}
