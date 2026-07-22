package net.morgana.signedjukebox;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

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

                if (world.getBlockEntity(px, y, pz) instanceof SignBlockEntity)
                    return new BlockPos(px, y, pz);

            }
        return null;
    }


    static String getSignText(SignBlockEntity sign) {
        StringBuilder result = new StringBuilder();

        for (var t: sign.texts)
            result.append(t);

        return result.toString();
    }

    static Vec3i getDir(SignBlockEntity sign)
    {
        return switch (sign.getPushedBlockData()) {
            case 2 -> new Vec3i(0, 0, 1);
            case 4 -> new Vec3i(1, 0, 0);
            case 5 -> new Vec3i(-1, 0, 0);
            default -> new Vec3i(0, 0, -1);
        };
    }


    static final int MaxCombinedSigns = 8;

    public static String getMultiSignText(World world, int x, int y, int z, boolean leftOnly)
    {
        SignBlockEntity signEntity = (SignBlockEntity)world.getBlockEntity(x, y, z);

        Vec3i dir = SignUtil.getDir(signEntity);

        Vec3i right = new Vec3i(dir.z, 0, -dir.x);

        Vec3i leftMost = new Vec3i(x, y, z);

        int numSignsLeft;

        for (numSignsLeft = 0; numSignsLeft < MaxCombinedSigns; numSignsLeft++)
        {
            System.out.println("Left " + numSignsLeft);

            Vec3i next = new Vec3i(leftMost.x + right.x, leftMost.y + right.y, leftMost.z + right.z);

            if (!(world.getBlockEntity(next.x, next.y, next.z) instanceof SignBlockEntity sign &&dir.equals(getDir(sign))))
                break;
            leftMost = next;
        }

        StringBuilder totalSignText = new StringBuilder();

        Vec3i nextSign = leftMost;
        for (int i = 0; i < (leftOnly ? numSignsLeft : (numSignsLeft) + MaxCombinedSigns + 1); i++)
        {
            if (!(world.getBlockEntity(nextSign.x, nextSign.y, nextSign.z) instanceof SignBlockEntity sign && dir.equals(getDir(sign))))
                break;

            System.out.println("Right " + i);

            totalSignText.append(getSignText(sign));

            nextSign = new Vec3i(nextSign.x - right.x, nextSign.y - right.y, nextSign.z - right.z );
        }

        return totalSignText.toString();
    }
}
