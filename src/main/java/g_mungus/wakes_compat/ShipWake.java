package g_mungus.wakes_compat;

import com.goby56.wakes.WakesClient;
import com.goby56.wakes.duck.ProducesWake;
import com.goby56.wakes.simulation.WakeHandler;
import com.goby56.wakes.simulation.WakeNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.LevelYRange;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static g_mungus.wakes_compat.Util.approximateDirection;
import static g_mungus.wakes_compat.Util.getYaw;

public class ShipWake {

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
            AABBic saabb = ship.getShipAABB();
            if (prevPos != null) {
                assert saabb != null;
                float xwidth = saabb.maxX() - saabb.minX();
                float zwidth = saabb.maxZ() - saabb.minZ();
                float width = Math.min(xwidth, zwidth);

                var7 = WakeNode.Factory.thickNodeTrail(prevPos.x, prevPos.z, (aabb.maxX() + aabb.minX())/2, (aabb.maxZ() + aabb.minZ())/2, height, (float) WakesClient.CONFIG_INSTANCE.initialStrength, velocity, width).iterator();

                while(var7.hasNext()) {
                    node = (WakeNode)var7.next();
                    wakeHandler.insert(node);
                }

            }
        }
    }

    public static void checkShipSize(Ship s) {

        World world = MinecraftClient.getInstance().world;

        Vector3dc horizontalVelocity = new Vector3d(s.getVelocity().x(), 0, s.getVelocity().z());

        if (horizontalVelocity.length() < 0.05) return;


        double velAngle = horizontalVelocity.angleSigned(new Vector3d(0, 0, -1), new Vector3d(0, 1, 0));


        double shipAngle = getYaw(s.getTransform().getShipToWorldRotation());



        Direction direction = approximateDirection(Math.toDegrees(velAngle - shipAngle));

        Vec3d shipPos = Util.getCentre(s.getWorldAABB());

        Double yLevelShip = VectorConversionsMCKt.toJOML(new Vec3d(shipPos.x, 62.9, shipPos.z)).mulPosition(s.getWorldToShip()).y;

        int blockYLevelShip = yLevelShip.intValue();


        Vector3i minWorldPos = new Vector3i();
        Vector3i maxWorldPos = new Vector3i();

        // Rounding minY down to the nearest multiple of 16
        int minY = (blockYLevelShip / 16) * 16;

        // Rounding maxY up to the nearest multiple of 16, then subtracting 1 to get congruent to 15
        int maxY = ((blockYLevelShip / 16) * 16) + 15;


        s.getActiveChunksSet().getMinMaxWorldPos(minWorldPos, maxWorldPos, new LevelYRange(minY, maxY));

        float width = 0;
        Vec3d offset;

        //floodfill stuff


        offset = Vec3d.ZERO;

        ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setWidth(width);
        ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setOffset(offset);


        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.of(String.valueOf(minWorldPos) + String.valueOf(maxWorldPos)));

    }


    // Helper method to calculate ship's width and offset
    private static void calculateShipWidthAndOffset(World world, Vector3i minWorldPos, Vector3i maxWorldPos,
                                                    int blockYLevelShip, Direction direction, Ship s) {
        // Axis variables to abstract x-axis or z-axis iterations
        boolean isZAxis = (direction == Direction.NORTH || direction == Direction.SOUTH);

        int primaryMin = isZAxis ? minWorldPos.z() : minWorldPos.x();  // Min for the primary iteration axis
        int primaryMax = isZAxis ? maxWorldPos.z() : maxWorldPos.x();  // Max for the primary iteration axis
        int secondaryMin = isZAxis ? minWorldPos.x() : minWorldPos.z();  // Min for the secondary axis
        int secondaryMax = isZAxis ? maxWorldPos.x() : maxWorldPos.z();  // Max for the secondary axis
        boolean isNegativeDirection = (direction == Direction.NORTH || direction == Direction.WEST); // Reverse iteration?

        // Variables to track the row index and bounds
        boolean foundFirstNonAirBlock = false;
        int rowsChecked = 0;

        RowWithBlocks widestRow = null;
        Vec3d boundsCenter = new Vec3d((minWorldPos.x() + maxWorldPos.x()) / 2.0, blockYLevelShip,
                (minWorldPos.z() + maxWorldPos.z()) / 2.0);

        // Iterate based on primary axis (either x or z depending on direction)
        for (int primary = isNegativeDirection ? primaryMax : primaryMin;
             isNegativeDirection ? primary >= primaryMin : primary <= primaryMax;
             primary += isNegativeDirection ? -1 : 1) {

            List<BlockPos> blockPositions = new ArrayList<>();
            int minInRow = secondaryMax;
            int maxInRow = secondaryMin;
            boolean foundNonAirBlockInRow = false;

            // Iterate over the secondary axis (either z or x depending on direction)
            for (int secondary = secondaryMin; secondary <= secondaryMax; secondary++) {
                int x = isZAxis ? secondary : primary;  // Set x for BlockPos
                int z = isZAxis ? primary : secondary;  // Set z for BlockPos

                if (!world.getBlockState(new BlockPos(x, blockYLevelShip, z)).isAir()) {
                    foundNonAirBlockInRow = true;
                    blockPositions.add(new BlockPos(x, blockYLevelShip, z));
                    minInRow = Math.min(minInRow, secondary);
                    maxInRow = Math.max(maxInRow, secondary);
                }
            }

            if (foundNonAirBlockInRow) {
                if (!foundFirstNonAirBlock) {
                    foundFirstNonAirBlock = true;
                }
                rowsChecked++;

                // Create a new RowWithBlocks instance for this row
                RowWithBlocks currentRow = new RowWithBlocks(rowsChecked, blockPositions, minInRow, maxInRow, Util.getCentre((AABBdc) s.getShipAABB()));

                // Update widestRow if this row is wider
                if (widestRow == null || currentRow.getWidth() > widestRow.getWidth()) {
                    widestRow = currentRow;
                }

                // Stop after 5 rows checked
                if (rowsChecked >= 5) break;
            }
        }

        // Use the widest row to calculate the final width and offset
        if (widestRow != null) {
            float width = widestRow.getWidth();
            Vec3d offset = widestRow.getOffset();

            ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setWidth(width);
            ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setOffset(offset);

            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Text.of("Width: " + width + ", Offset: " + offset));
        } else {
            ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setWidth(0);
            ((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$setOffset(Vec3d.ZERO);
        }
    }




    private static class RowWithBlocks {
        int index;
        List<BlockPos> blockPositions;
        int width;
        Vec3d offset;

        public RowWithBlocks(int index, List<BlockPos> blockPositions, int minPos, int maxPos, Vec3d boundsCenter) {
            this.index = index;
            this.blockPositions = blockPositions;
            this.width = maxPos - minPos;
            // Offset is calculated as the center between min and max non-air blocks
            this.offset = new Vec3d((minPos + maxPos) / 2.0, boundsCenter.y, boundsCenter.z);
        }

        public int getWidth() {
            return this.width;
        }

        public Vec3d getOffset() {
            return this.offset;
        }
    }
}
