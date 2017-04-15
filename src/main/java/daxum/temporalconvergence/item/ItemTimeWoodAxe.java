package daxum.temporalconvergence.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTimeWoodAxe extends ItemAxe {
	public ItemTimeWoodAxe() {
		super(ModItems.TIMEWOOD, 6.0f, -3.2f);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("time_wood_axe");
		setRegistryName("time_wood_axe");
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!world.isRemote && stack.getItem() == this && world.getTotalWorldTime() % 5 == 0)
			stack.damageItem(1, (EntityLivingBase) entity);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase user) {
		return true;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean changed) {
		return changed || !(oldStack.getItem() == newStack.getItem());
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
}
