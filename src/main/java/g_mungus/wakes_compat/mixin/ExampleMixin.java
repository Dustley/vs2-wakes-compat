package g_mungus.wakes_compat.mixin;

import com.goby56.wakes.duck.ProducesWake;
import com.goby56.wakes.particle.custom.SplashPlaneParticle;
import g_mungus.wakes_compat.Util;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ships.ShipObjectClient;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(ShipObjectClient.class)
public abstract class ExampleMixin implements ProducesWake {

	private Vec3d prevPosOnSurface;

	@Shadow public abstract Vector3dc getVelocity();

	@Override
	public boolean onWaterSurface() {
		return true;
	}

	@Override
	public float producingHeight() {
		return 62.9f;
	}

	@Override
	public Vec3d getPrevPos() {
		return this.prevPosOnSurface == null ? null : new Vec3d(this.prevPosOnSurface.x, this.prevPosOnSurface.y, this.prevPosOnSurface.z);
	}

	@Unique
	public Vec3d getPos() {
		return Util.getCentre(((Ship)(Object)this).getWorldAABB());
	}

	@Override
	public void setPrevPos(Vec3d pos) {
		this.prevPosOnSurface = pos;
	}


	@Override
	public Vec3d getNumericalVelocity() {
		return VectorConversionsMCKt.toMinecraft(((Ship)(Object)this).getVelocity());
	}

	@Override
	public double getHorizontalVelocity() {
		Vector3dc velocityVector =((Ship)(Object)this).getVelocity();
		Vector3dc horizontalVelocityVector = new Vector3d(velocityVector.x(), 0, velocityVector.z());

		return horizontalVelocityVector.length();
	}

	@Override
	public double getVerticalVelocity() {
		return ((Ship)(Object)this).getVelocity().y();
	}

	@Override
	public void setSplashPlane(SplashPlaneParticle splashPlaneParticle) {

	}
}