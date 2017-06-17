/***************************************************************************
 * Temporal Convergence
 * Copyright (C) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 **************************************************************************/
package daxum.temporalconvergence.tileentity;

import daxum.temporalconvergence.block.BlockBrazier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileBrazier extends TileEntityBase implements ITickable {
	private static final int MAX_BURN_TIME = 2400;

	private int burnTime = 0;
	private int flameRed = 255;
	private int flameGreen = 255;
	private int flameBlue = 255;

	@Override
	public void update() {
		IBlockState state = world.getBlockState(pos);

		if (!world.isRemote && state.getValue(BlockBrazier.BURNING)) {
			if (BlockBrazier.hasDust(state)) {
				if (burnTime > 0) {
					burnTime--;
					markDirty();
				}
				else {
					world.setBlockState(pos, BlockBrazier.getLowerDustState(state));

					if (BlockBrazier.isEmpty(world.getBlockState(pos))) {
						BlockBrazier.putOutBrazier(world, pos, world.getBlockState(pos));
					}
					else {
						burnTime = MAX_BURN_TIME;
						markDirty();
					}
				}
			}
		}
	}

	public void startBurning() {
		burnTime = MAX_BURN_TIME;
		markDirty();
	}

	public void resetBurnTime() {
		burnTime = 0;
		markDirty();
	}

	public void addColor(ItemStack dye) {
		if (dye.getItem() == Items.DYE) {
			int dyeColor = getDyeColor(dye.getMetadata());
			int dyeRed = dyeColor >> 16 & 255;
			int dyeGreen = dyeColor >> 8 & 255;
			int dyeBlue = dyeColor & 255;

			int averageRed = (dyeRed + flameRed) / 2;
			int averageGreen = (dyeGreen + flameGreen) / 2;
			int averageBlue = (dyeBlue + flameBlue) / 2;

			int lightestDyeComponent = Math.max(dyeRed, Math.max(dyeGreen, dyeBlue));
			int lightestFlameComponent = Math.max(flameRed, Math.max(flameGreen, flameBlue));

			float averageLightness = (lightestDyeComponent + lightestFlameComponent) / 2.0f;
			float lightestColorValue = Math.max(averageRed, Math.max(averageGreen, averageBlue));
			float lightnessPercent = averageLightness / lightestColorValue;

			flameRed = (int) (averageRed * lightnessPercent);
			flameGreen = (int) (averageGreen * lightnessPercent);
			flameBlue = (int) (averageBlue * lightnessPercent);
			markDirty();
		}
	}

	private int getDyeColor(int metadata) {
		float[] dyeColor = EntitySheep/*WHY?!*/.getDyeRgb(EnumDyeColor.byMetadata(metadata));
		return MathHelper.rgb(dyeColor[0], dyeColor[1], dyeColor[2]);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("burnTime", Constants.NBT.TAG_INT)) {
			burnTime = comp.getInteger("burnTime");
		}

		if (comp.hasKey("color", Constants.NBT.TAG_INT)) {
			int color = comp.getInteger("color");

			flameRed = color >> 16 & 255;
			flameGreen = color >> 8 & 255;
			flameBlue = color & 255;
		}

		super.readFromNBT(comp);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("burnTime", burnTime);
		comp.setInteger("color", flameRed & 255 << 16 | flameGreen & 255 << 8 | flameBlue & 255);

		return super.writeToNBT(comp);
	}
}
