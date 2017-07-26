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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBase extends Block {
	private final Map<IBlockState, AxisAlignedBB[]> stateBoxMap = new HashMap(8);
	private boolean hasTileEntity = false;

	public BlockBase(Material material, String registryName, float hardness, float resistance, Tool tool, MiningLevel level, SoundType sound) {
		super(material);
		setUnlocalizedName(registryName);
		setRegistryName(registryName);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(hardness);
		setResistance(resistance);
		setHarvestLevel(tool.name, level.level);
		setSoundType(sound);
	}

	public BlockBase(String registryName, BlockPresets preset) {
		this(preset.material, registryName, preset.hardness, preset.resistance, preset.tool, preset.level, preset.sound);
	}

	public BlockBase(String registryName, float hardness, float resistance, Tool tool, MiningLevel level) {
		this(Material.ROCK, registryName, hardness, resistance, tool, level, SoundType.STONE);
	}

	public BlockBase(String registryName) {
		this(registryName, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD);
	}

	protected void setHarvestTool(Tool tool) {
		setHarvestLevel(tool.name, getHarvestLevel(getDefaultState()));
	}

	protected void setMiningLevel(MiningLevel level) {
		setHarvestLevel(getHarvestTool(getDefaultState()), level.level);
	}

	protected void setHasTileEntity() {
		hasTileEntity = true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, getProperties());
	}

	//This is one of the stupidest things I've ever done. Also probably really inefficient.
	protected IProperty[] getProperties() {
		List<IProperty> properties = new ArrayList<>();

		for (Field field : getClass().getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
				try {
					Object o = field.get(null);

					if (o instanceof IProperty) {
						properties.add((IProperty) o);
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					TemporalConvergence.LOGGER.fatal("Reflective BlockState wizardry failed for {}!", getClass());

					throw new ReportedException(new CrashReport("Reflective BlockState wizardry failed for " + getClass() + "!", e));
				}
			}
		}

		//TODO: remove sorting in 1.13, it's only needed because of state -> meta
		properties.sort(new Comparator<IProperty>() {
			@Override
			public int compare(IProperty arg0, IProperty arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});

		return properties.toArray(new IProperty[0]);
	}

	protected void setStateDefaults(Object... objects) {
		IBlockState defaultState = blockState.getBaseState();

		for (int i = 0; i < objects.length; i += 2) {
			if (objects[i] instanceof IProperty && i + 1 < objects.length) {
				defaultState = setStateValue(defaultState, (IProperty)objects[i], objects[i + 1]);
			}
			else {
				TemporalConvergence.LOGGER.fatal("Malformed default list for {}", getClass());

				throw new IllegalArgumentException("Malformed default list for " + getClass());
			}
		}

		setDefaultState(defaultState);
	}

	private <T extends Comparable<T>> IBlockState setStateValue(IBlockState state, IProperty<T> property, Object value) {
		try {
			return state.withProperty(property, (T)value);
		}
		catch(ClassCastException e) {
			TemporalConvergence.LOGGER.fatal("Failed to assign property {} of {} to {}", property, getClass(), value);

			throw new ReportedException(new CrashReport("Failed to assign property " + property + " of " + getClass() + " to " + value, e));
		}
	}

	protected boolean isCube() {
		return true;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isCube();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return isCube();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isCube();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return isNormalCube(state, world, pos) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	protected AxisAlignedBB[] getNewBoundingBoxList(World world, BlockPos pos, IBlockState state) {
		return new AxisAlignedBB[] {state.getBoundingBox(world, pos)};
	}

	public final AxisAlignedBB[] getBoundingBoxList(World world, BlockPos pos, IBlockState state) {
		AxisAlignedBB[] aabbList = stateBoxMap.get(state);

		if (aabbList == null) {
			stateBoxMap.put(state, getNewBoundingBoxList(world, pos, state));
			aabbList = stateBoxMap.get(state);
		}

		return aabbList;
	}

	public AxisAlignedBB[] getSelectedBBList(World world, BlockPos pos, IBlockState state) {
		AxisAlignedBB[] oldList = getBoundingBoxList(world, pos, state);
		AxisAlignedBB[] aabbList = new AxisAlignedBB[oldList.length];

		for (int i = 0; i < aabbList.length; i++) {
			aabbList[i] = oldList[i].offset(pos);
		}

		return aabbList;
	}

	public boolean hasMultipleBoundingBoxes() {
		return false;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> aabbList, Entity entity, boolean actualState) {
		if (hasMultipleBoundingBoxes()) {
			for (AxisAlignedBB aabb : getBoundingBoxList(world, pos, state)) {
				addCollisionBoxToList(pos, entityBox, aabbList, aabb);
			}
		}
		else {
			addCollisionBoxToList(pos, entityBox, aabbList, state.getCollisionBoundingBox(world, pos));
		}
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		if (hasMultipleBoundingBoxes()) {
			RayTraceResult rtr = null;

			for (AxisAlignedBB aabb : getBoundingBoxList(world, pos, state)) {
				if (rtr == null) {
					rtr = rayTrace(pos, start, end, aabb);
				}
				else {
					break;
				}
			}

			return rtr;
		}
		else {
			return rayTrace(pos, start, end, state.getBoundingBox(world, pos));
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return hasTileEntity;
	}

	public enum BlockPresets {
		STONE(Material.ROCK, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD, SoundType.STONE),
		PLANT(Material.PLANTS, 0.0f, 0.0f, Tool.NONE, MiningLevel.HAND, SoundType.PLANT),
		UNBREAKABLE(Material.BARRIER, -1.0f, Float.MAX_VALUE, Tool.NONE, MiningLevel.HAND, SoundType.STONE),
		IRON(Material.IRON, 5.0f, 30.0f, Tool.PICKAXE, MiningLevel.STONE, SoundType.METAL),
		WOOD(Material.WOOD, 2.0f, 15.0f, Tool.AXE, MiningLevel.HAND, SoundType.WOOD),
		STONE_MACHINE(Material.ROCK, 5.0f, 30.0f, Tool.PICKAXE, MiningLevel.IRON, SoundType.STONE),
		WEAK_IRON(Material.IRON, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD, SoundType.METAL),
		GLASS(Material.GLASS, 0.3f, 1.5f, Tool.NONE, MiningLevel.HAND, SoundType.GLASS);

		private final Material material;
		private final float hardness;
		private final float resistance;
		private final Tool tool;
		private final MiningLevel level;
		private final SoundType sound;

		private BlockPresets(Material m, float h, float r, Tool t, MiningLevel l, SoundType s) {
			material = m;
			hardness = h;
			resistance = r;
			tool = t;
			level = l;
			sound = s;
		}
	}

	public enum Tool {
		NONE(""),
		PICKAXE("pickaxe"),
		AXE("axe"),
		SHOVEL("shovel");

		private final String name;

		private Tool(String n) {
			name = n;
		}
	}

	public enum MiningLevel {
		HAND(-1),
		WOOD(0),
		GOLD(0),
		STONE(1),
		IRON(2),
		DIAMOND(3);

		private final int level;

		private MiningLevel(int l) {
			level = l;
		}
	}

	//TODO: remove three methods below in 1.13

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return blockState.getValidStates().get(meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state.getPropertyKeys().isEmpty()) {
			return 0;
		}
		else {
			for (int i = 0; i < blockState.getValidStates().size() && i < 16; i++) {
				if (blockState.getValidStates().get(i).equals(state)) {
					return i;
				}
			}

			TemporalConvergence.LOGGER.error("Couldn't convert blockState {} to meta", state);

			return 0;
		}
	}

	//Bit of a hack to fix improper meta ordering
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (meta == 0) {
			return getDefaultState();
		}

		return getStateFromMeta(meta);
	}
}
