package tintor.apps.rts.core;

public class Unit extends BaseUnit {
	public static final double MaxMiningDistance = 0.5;
	public static final double MaxDropOffDistance = 0.5;
	public static final int DroneMineralCapacity = 8;
	public static int DroneMininingTicks = 20;

	public enum Type {
		Sheep, Mineral, Base, Drone, Marine
	}

	public static final int BadId = 0;

	public final int id;
	public Type type;
	public int player;
	public int targetId;

	// Used by Drone and Mineral types
	public short minerals;
	public short miningTicks;

	// Used by offensive units
	public int attackRange;
	public int attackDamage;
	public int attackCooldownTicks;

	// Used by destructible units
	public int hitPoints;
	public int maxHitPoints;

	Unit(final int id) {
		this.id = id;
	}

	boolean isDead() {
		return type == Unit.Type.Mineral && minerals == 0 || player > 0 && hitPoints == 0;
	}

	@Override
	void update(final World world) {
		if (targetId != BadId) {
			final Unit target = world.units.get(targetId);
			if (target == null) {
				// if unit doesn't exists anymore
				targetId = BadId;
			} else {
				ActOnTarget(world, target);
			}
		}

		if (targetId == BadId) {
			miningTicks = 0;
		}

		super.update(world);
	}

	private void ActOnTarget(final World world, final Unit target) {
		assert target != null;
		if (type == Type.Drone && target.type == Type.Mineral) {
			if (distance(target) > MaxMiningDistance) {
				// move closer to mineral
				destination = target.position;
			} else if (target.minerals > 0 && minerals < DroneMineralCapacity) {
				miningTicks++;
				if (miningTicks >= DroneMininingTicks) {
					target.minerals--;
					minerals++;
					miningTicks = 0;
				}
			} else {
				// mining complete
				targetId = 0;
				destination = null;
			}
		} else if (type == Type.Drone && target.type == Type.Base) {
			if (distance(target) > MaxDropOffDistance) {
				// move closer to the base
				destination = target.position;
			} else {
				// drop minerals at the base
				world.minerals[player] += minerals;
				minerals = 0;
				targetId = 0;
				destination = null;
			}
		} else {
			destination = target.position;
			//final Vector2 dir = AntiGravity.direction(world, this, target.position, target);
			//destination = position.add(maxVelocity * 2 / dir.length(), dir);
		}
	}
}