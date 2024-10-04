package g_mungus.wakes_compat;

import com.goby56.wakes.duck.ProducesWake;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.LevelYRange;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.List;

import static g_mungus.wakes_compat.ShipWake.checkShipSize;
import static g_mungus.wakes_compat.Util.approximateDirection;
import static g_mungus.wakes_compat.Util.getYaw;

public class VSWakesCompat implements ClientModInitializer {
	public static final String MOD_ID = "vs-wakes-compat";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int SHIPS_PER_CYCLE = 60;
	private int shipSizeUpdaterCooldown = 0;

	private static double seaLevel = 62.9;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onClientJoin(client));

		LOGGER.info("Hello Fabric world!");
	}

	private void onClientJoin(MinecraftClient client) {
		if (seaLevel != 63) {
			assert client.world != null;
			seaLevel = client.world.getSeaLevel() - 0.1;
		}
	}


	private void onClientTick() {
        if (MinecraftClient.getInstance().player == null) return;

        World world = MinecraftClient.getInstance().player.getWorld();

		// Update wake scale
		List<Ship> ships = VSGameUtilsKt.getAllShips(world).stream().toList();

		if(!ships.isEmpty()) {

			// Determine the number of ships to process per cycle
			int shipsThisCycle = (int) Math.ceil((double) ships.size() / SHIPS_PER_CYCLE);
			int startIndex = shipSizeUpdaterCooldown * shipsThisCycle;
			int endIndex = Math.min(startIndex + shipsThisCycle, ships.size());

			LOGGER.info("Start at: {} End at: {} Ships: {} Tick: {}", startIndex, endIndex, ships.size(), shipSizeUpdaterCooldown);

			for (int i = startIndex; i < endIndex - 1; i++) {
				Ship s = ships.get(i);
				if (s != null) checkShipSize(s);
			}

			// Place wake
			for (Ship s : VSGameUtilsKt.getAllShips(world)) {
				ShipWake.placeWakeTrail(s);
				((ProducesWake) s).setPrevPos(((DynamicWakeSize) s).vs_wakes_compat_template_1_20_1$getPos());
			}

			if (shipSizeUpdaterCooldown == SHIPS_PER_CYCLE - 1) {
				shipSizeUpdaterCooldown = 0;
			} else {
				shipSizeUpdaterCooldown++;
			}
		}
	}


	public static double getSeaLevel() {
		return seaLevel;
	}
}