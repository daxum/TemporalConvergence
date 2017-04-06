package daxum.temporalconvergence.block;

import daxum.temporalconvergence.world.DimensionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class BlockTestTeleporter extends BlockBase {
	public BlockTestTeleporter() {
		super("test_teleporter");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && player != null) {
			if (player.dimension == 0)
				DimensionHandler.changePlayerDim(player, DimensionHandler.EARLY_FUTURE);
			else if (player.dimension == DimensionHandler.EARLY_FUTURE.getId())
				DimensionHandler.changePlayerDim(player, DimensionType.OVERWORLD);
		}

		return true;
	}
}
