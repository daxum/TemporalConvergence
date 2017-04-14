package daxum.temporalconvergence.item;

import net.minecraft.item.Item;

public class ItemBase extends Item {
	public ItemBase (String name) {
		setUnlocalizedName(name);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setRegistryName(name);
	}
}
