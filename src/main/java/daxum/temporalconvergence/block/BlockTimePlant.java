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
package daxum.temporalconvergence.block;

import java.util.Random;

import daxum.temporalconvergence.tileentity.TileTimePlant;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTimePlant extends BlockBase implements IPlantable, IGrowable {
	public static final PropertyBool WITHERED = PropertyBool.create("withered");
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.9375, 0.9375);

	public BlockTimePlant() {
		super("time_plant", BlockPresets.PLANT);
		setStateDefaults(new Default(WITHERED, true));
		setLightLevel(0.4f);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		IBlockState base = world.getBlockState(pos.down());
		return super.canPlaceBlockAt(world, pos) && base.getBlock().canSustainPlant(base, world, pos.down(), EnumFacing.UP, this);
	}

	@Override
	// pos is the location of the block whose neighbor changed, changed is what
	// was there before, changedPos is the position of the block that changed
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		if (changedPos.equals(pos.down()) && !world.getBlockState(changedPos).getBlock().canSustainPlant(world.getBlockState(changedPos), world, pos.down(), EnumFacing.UP, this)) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player != null && player.getHeldItem(hand).getItem() instanceof ItemShears) {
			if (world.getTileEntity(pos) instanceof TileTimePlant) {
				ItemStack toDrop = ((TileTimePlant)world.getTileEntity(pos)).getShearedItem(world.getWorldTime());

				if (!toDrop.isEmpty()) {
					if (!world.isRemote) {
						world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, toDrop));
						player.getHeldItem(hand).damageItem(1, player);
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(WITHERED, (meta & 1) == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(WITHERED) ? 1: 0;
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
	protected boolean isCube() {
		return false;
	}

	@Override
	public SoundType getSoundType() {
		return SoundType.PLANT;

	}

	public MapColor getMapColor() {
		return MapColor.FOLIAGE;
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

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return !state.getValue(WITHERED);
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		return !state.getValue(WITHERED);
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TileTimePlant) {
			((TileTimePlant) world.getTileEntity(pos)).onGrowthAccelerated(world.getWorldTime());
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileTimePlant();
	}
}
