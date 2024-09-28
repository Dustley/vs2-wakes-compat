package g_mungus.wakes_compat;

import com.goby56.wakes.duck.ProducesWake;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ships.ShipObjectClient;
import org.valkyrienskies.eureka.EurekaMod;
import org.valkyrienskies.eureka.fabric.services.EurekaPlatformHelperFabric;
import org.valkyrienskies.eureka.services.EurekaPlatformHelper;
import org.valkyrienskies.eureka.ship.EurekaShipControl;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

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
		Direction direction;

		Vector3dc horizontalVelocity = new Vector3d(s.getVelocity().x(), 0, s.getVelocity().z());

		if (horizontalVelocity.length() < 0.2) return -1;


		double velAngle = horizontalVelocity.angleSigned(new Vector3d(0, 0, -1), new Vector3d(0, 1, 0));


		double shipAngle = getYaw(s.getTransform().getShipToWorldRotation());


        try {
            direction = approximateDirection(Math.toDegrees(velAngle - shipAngle));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.of(String.valueOf(direction)));

		return 0;
	}

	public static double getYaw(Quaterniondc quaternion) {
		// Extract the components of the quaternion
		double w = quaternion.w();
		double x = quaternion.x();
		double y = quaternion.y();
		double z = quaternion.z();

		// Calculate yaw using atan2
		return -Math.atan2(2.0 * (w * y + x * z), 1.0 - 2.0 * (y * y + z * z));
	}

	public static Direction approximateDirection (Double degrees) throws Exception {
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
			throw new Exception("Bruh");
		}
	}

}