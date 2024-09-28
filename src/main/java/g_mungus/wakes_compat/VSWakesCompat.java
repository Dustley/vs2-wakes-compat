package g_mungus.wakes_compat;

import com.goby56.wakes.duck.ProducesWake;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.LevelYRange;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import static g_mungus.wakes_compat.Util.approximateDirection;
import static g_mungus.wakes_compat.Util.getYaw;

public class VSWakesCompat implements ClientModInitializer {
	public static final String MOD_ID = "vs-wakes-compat";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int shipSizeUpdaterCooldown = 0;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());

		LOGGER.info("Hello Fabric world!");
	}



	private void onClientTick() {
        if (MinecraftClient.getInstance().player == null) return;

        World world = MinecraftClient.getInstance().player.getWorld();

		VSGameUtilsKt.getAllShips(world).forEach(s -> {
			if (s != null) {

				if (shipSizeUpdaterCooldown == 0) {
					checkShipSize(s);
				}


				Util.placeWakeTrail(s);
				((ProducesWake)s).setPrevPos(Util.getCentre(s.getWorldAABB()));
			}


		});

		if (shipSizeUpdaterCooldown == 9) {
			shipSizeUpdaterCooldown = 0;
		} else {
			shipSizeUpdaterCooldown++;
		}
	}

	private int checkShipSize(Ship s) {

		World world = MinecraftClient.getInstance().world;

		Vector3dc horizontalVelocity = new Vector3d(s.getVelocity().x(), 0, s.getVelocity().z());

		if (horizontalVelocity.length() < 0.2) return -1;


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



        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.of(String.valueOf(minWorldPos) + String.valueOf(maxWorldPos)));

		return 0;
	}



}