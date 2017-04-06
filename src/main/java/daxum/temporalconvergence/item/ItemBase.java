package daxum.temporalconvergence.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBase extends Item {
	public ItemBase (String name) {
		setUnlocalizedName(name);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setRegistryName(name);
		GameRegistry.register(this);
	}
}
