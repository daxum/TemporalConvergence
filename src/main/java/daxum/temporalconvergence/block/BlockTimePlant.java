package daxum.temporalconvergence.block;

import java.util.Random;

import daxum.temporalconvergence.entity.EntityTimePixie;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Currently not shearable due to exploit.
public class BlockTimePlant extends BlockBase implements IPlantable {
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.9375, 0.9375);

	public BlockTimePlant() {
		super(Material.PLANTS, "time_plant", 0.0f, 0.0f);
		setTickRandomly(true);
		setLightLevel(0.4f);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		IBlockState base = world.getBlockState(pos.down());
		return super.canPlaceBlockAt(world, pos)
				&& base.getBlock().canSustainPlant(base, world, pos.down(), EnumFacing.UP, this);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (world.getGameRules().getBoolean("doMobSpawning")) {
			int amountSpawned = world.countEntities(EntityTimePixie.class);

			if (rand.nextInt(amountSpawned / 10 + 4) == 0 && amountSpawned <= 50) {
				EntityTimePixie toSpawn = new EntityTimePixie(world);

				toSpawn.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						MathHelper.wrapDegrees(rand.nextFloat() * 360.0F), 0.0F);
				toSpawn.rotationYawHead = toSpawn.rotationYaw;
				toSpawn.renderYawOffset = toSpawn.rotationYaw;
				toSpawn.onInitialSpawn(world.getDifficultyForLocation(pos), null);

				world.spawnEntity(toSpawn);
			}
		}
	}

	@Override
	// pos is the location of the block whose neighbor changed, changed is what
	// was there before, changedPos is the position of the block that changed
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		if (changedPos.equals(pos.down()) && !world.getBlockState(changedPos).getBlock()
				.canSustainPlant(world.getBlockState(changedPos), world, pos.down(), EnumFacing.UP, this)) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public SoundType getSoundType() {
		return SoundType.PLANT;

	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
