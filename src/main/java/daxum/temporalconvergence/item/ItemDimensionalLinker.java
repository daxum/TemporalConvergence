package daxum.temporalconvergence.item;

import java.util.List;

import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDimensionalLinker extends ItemBase {
	public ItemDimensionalLinker() {
		super("dimensional_linker");
		addPropertyOverride(new ResourceLocation("bound"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				return isBound(stack) ? 1.0f : 0.0f;
			}
		});
		addPropertyOverride(new ResourceLocation("empty"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				if (isBound(stack)) {
					return stack.getTagCompound().getBoolean("empty") ? 1.0f : 0.0f;
				}

				return 0.0f;
			}
		});
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!world.isRemote && isBound(stack)) {
			PowerDimension pd = PowerDimension.get(world, stack.getTagCompound().getInteger("dimid"));

			if (pd != null) {
				stack.getTagCompound().setString("debug", pd.toString());
			}
			else {
				stack.getTagCompound().setString("debug", "Empty dimension");
				stack.getTagCompound().setBoolean("empty", true);
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (stack == ItemStack.EMPTY || stack.getItem() != this || world.isRemote)
			return ActionResult.newResult(EnumActionResult.PASS, stack);

		//Clear if sneaking
		if (player.isSneaking() && isBound(stack)) {
			stack.getTagCompound().removeTag("dimid");
			return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
		}

		//Bind to other
		ItemStack other = hand == EnumHand.MAIN_HAND ? player.getHeldItem(EnumHand.OFF_HAND) : player.getHeldItem(EnumHand.MAIN_HAND);
		if (other != ItemStack.EMPTY && isBound(other)) {
			int id = other.getTagCompound().getInteger("dimid");

			if (stack.hasTagCompound()) {
				if (!stack.getTagCompound().hasKey("dimid")) {
					stack.getTagCompound().setInteger("dimid", id);
					return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
				}
			} else {
				NBTTagCompound comp = new NBTTagCompound();
				comp.setInteger("dimid", id);
				stack.setTagCompound(comp);
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	public boolean isBound(ItemStack stack) {
		return stack.getItem() == this && stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid")) {
			tooltip.add("Set to dimension #" + stack.getTagCompound().getInteger("dimid"));
			if (stack.getTagCompound().hasKey("debug"))
				tooltip.add(stack.getTagCompound().getString("debug"));
		}
		else {
			tooltip.add("No dimension set");
		}
	}
}
