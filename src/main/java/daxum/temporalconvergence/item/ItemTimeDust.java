package daxum.temporalconvergence.item;

import daxum.temporalconvergence.block.BlockFluidTimeWater;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;

public class ItemTimeDust extends ItemBase {
	public ItemTimeDust() {
		super("time_dust");
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem item) {
		if (item.getEntityWorld().isRemote)
			return false;

		if (item.getEntityWorld().getBlockState(item.getPosition()) == Blocks.WATER.getDefaultState()) {
			item.getEntityWorld().setBlockState(item.getPosition(), ModBlocks.timeWater.getDefaultState());
			Block justPlaced = item.getEntityWorld().getBlockState(item.getPosition()).getBlock();

			((BlockFluidTimeWater)justPlaced).checkAllButUp(item.getEntityWorld(), item.getPosition());

			if (item.getEntityItem().getCount() == 1)
				item.setDead();
			else
				item.getEntityItem().shrink(1);
		}

		return false;
	}
}
