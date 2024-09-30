package g_mungus.wakes_compat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;

public class Util {

    public static Vec3d getCentre (AABBdc aabb) {
        double centreX = (aabb.maxX() + aabb.minX())/2;
        double centreZ = (aabb.maxZ() + aabb.minZ())/2;
        return new Vec3d(centreX, aabb.minY(), centreZ);
    }

    public static Vector3d getCentre (AABBic aabb) {
        double centreX = (double) (aabb.maxX() + aabb.minX()) /2;
        double centreZ = (double) (aabb.maxZ() + aabb.minZ()) /2;
        return new Vector3d(centreX, aabb.minY(), centreZ);
    }


    public static double getYaw(Quaterniondc quaternion) {
        double w = quaternion.w();
        double x = quaternion.x();
        double y = quaternion.y();
        double z = quaternion.z();

        return -Math.atan2(2.0 * (w * y + x * z), 1.0 - 2.0 * (y * y + z * z));
    }

    public static Direction approximateDirection (Double degrees) {
        double y = degrees + 45d + 720d;
        double reduced = y % 360d;

        if (reduced >= 0 && reduced < 90) {
            return Direction.NORTH;
        } else if (reduced >= 90 && reduced < 180) {
            return Direction.EAST;
        } else if (reduced >= 180 && reduced < 270) {
            return Direction.SOUTH;
        } else if (reduced >= 270 && reduced < 360) {
            return Direction.WEST;
        } else {
            System.out.println("something went wrong");
            return Direction.NORTH;
        }
    }

    public static Vector3d averageVec(Vec3d vec1, Vec3d vec2) {
        double avgX = (vec1.getX() + vec2.getX()) / 2d;
        double avgY = (vec1.getY() + vec2.getY()) / 2d;
        double avgZ = (vec1.getZ() + vec2.getZ()) / 2d;

        return new Vector3d(avgX, avgY, avgZ);
    }
}
