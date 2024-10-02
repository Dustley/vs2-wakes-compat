package g_mungus.wakes_compat.mixin;

import com.goby56.wakes.duck.ProducesWake;
import com.goby56.wakes.particle.custom.SplashPlaneParticle;
import g_mungus.wakes_compat.DynamicWakeSize;
import g_mungus.wakes_compat.Util;
import g_mungus.wakes_compat.VSWakesCompat;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ships.ShipObjectClient;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(value = ShipObjectClient.class, remap = false)
public abstract class ShipObjectClientMixin implements ProducesWake, DynamicWakeSize {

	@Unique
	private Vec3d prevPosOnSurface;

	@Unique
	private float wakeWidth = 0;

	@Unique
	private Vec3d offset = Vec3d.ZERO;

	@Shadow public abstract Vector3dc getVelocity();

	@Override
	public boolean onWaterSurface() {
		return true;
	}

	@Override
	public float producingHeight() {
		return (float) VSWakesCompat.getSeaLevel();
	}

	@Override
	public Vec3d getPrevPos() {
		return this.prevPosOnSurface == null ? null : new Vec3d(this.prevPosOnSurface.x, this.prevPosOnSurface.y, this.prevPosOnSurface.z);
	}

	@Override
	public float vs_wakes_compat_template_1_20_1$getWidth() {
		return this.wakeWidth;
	}

	@Override
	public void vs_wakes_compat_template_1_20_1$setWidth(float width) {
		this.wakeWidth = width;
	}

	@Override
	public Vec3d vs_wakes_compat_template_1_20_1$getPos() {

		return Util.getCentre(((Ship)(Object)this).getWorldAABB()).add(offset);
	}

	@Override
    public void vs_wakes_compat_template_1_20_1$setOffset(Vector3d vec) {


		Ship ship = (Ship)(Object)this;
		Quaterniondc mat = ship.getTransform().getShipToWorldRotation();


		this.offset = VectorConversionsMCKt.toMinecraft(vec.rotate(mat));
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