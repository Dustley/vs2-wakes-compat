package g_mungus.wakes_compat;

import com.goby56.wakes.WakesClient;
import com.goby56.wakes.duck.ProducesWake;
import com.goby56.wakes.simulation.WakeHandler;
import com.goby56.wakes.simulation.WakeNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Iterator;

public class Util {

    public static void placeWakeTrail(Ship ship) {
        WakeHandler wakeHandler = WakeHandler.getInstance();
        if (wakeHandler != null) {
            ProducesWake producer = (ProducesWake)ship;
            double velocity = producer.getHorizontalVelocity();
            float height = producer.producingHeight();
            Iterator var7;
            WakeNode node;

            Vec3d prevPos = producer.getPrevPos();

            AABBdc aabb = ship.getWorldAABB();
            if (prevPos != null) {
                var7 = WakeNode.Factory.thickNodeTrail(prevPos.x, prevPos.z, (aabb.maxX() + aabb.minX())/2, (aabb.maxX() + aabb.minX())/2, height, (float)WakesClient.CONFIG_INSTANCE.initialStrength, velocity, 3f).iterator();

                while(var7.hasNext()) {
                    node = (WakeNode)var7.next();
                    wakeHandler.insert(node);
                }

            }
        }
    }

    public static Vec3d getCentre (AABBdc aabb) {
        double centreX = (aabb.maxX() + aabb.minX())/2;
        double centreZ = (aabb.maxZ() + aabb.minZ())/2;
        return new Vec3d(centreX, aabb.minY(), centreZ);
    }
}
