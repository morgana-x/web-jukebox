package net.morgana.signedjukebox;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.util.math.Vec3i;

public class SignUtil {

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


    static String getSignText(SignBlockEntity sign) {
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

    public static String getMultiSignText(World world, int x, int y, int z, boolean leftOnly)
    {
        SignBlockEntity signEntity = (SignBlockEntity)world.getBlockEntity(x, y, z);

        Vec3i dir = SignUtil.getDir(signEntity);

        Vec3i right = new Vec3i(dir.getZ(), 0, -dir.getX());



        Vec3i leftMost = new Vec3i(x, y, z);
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
        for (int i = 0; i < (leftOnly ? 3 : 7); i++)
        {
            if (!(world.getBlockEntity(nextSign.getX(), nextSign.getY(), nextSign.getZ()) instanceof SignBlockEntity sign && dir.equals(getDir(sign))))
                break;

            System.out.println("Right " + i);

            totalSignText += getSignText(sign);

            nextSign = nextSign.add(right.multiply(-1));
        }

        return totalSignText;
    }
}
