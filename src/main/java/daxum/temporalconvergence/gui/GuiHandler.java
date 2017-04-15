package daxum.temporalconvergence.gui;

import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	public static final int TIME_CHEST_GUI = 0;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch(id) {
		case TIME_CHEST_GUI: return new ContainerTimeChest(player.inventory, (TileTimeChest)world.getTileEntity(new BlockPos(x, y, z)));
		default: return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch(id) {
		case TIME_CHEST_GUI: return new GuiTimeChest(player.inventory, (TileTimeChest)world.getTileEntity(new BlockPos(x, y, z)));
		default: return null;
		}
	}
}
