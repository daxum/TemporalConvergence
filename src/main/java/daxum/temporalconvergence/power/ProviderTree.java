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
package daxum.temporalconvergence.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class ProviderTree implements INBTSerializable<NBTTagList> {
	public static final AxisAlignedBB SINGLE_BLOCK_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	private TreeNode root = new TreeNode(new AxisAlignedBB(-30000000, 0, -30000000, 30000000, 256, 30000000));

	public List<ProviderData> getIntersectingProviders(PowerType powerType, AxisAlignedBB receiverBox) {
		List<ProviderData> posList = new ArrayList();

		root.getIntersectingProviders(powerType, receiverBox, posList);

		return posList;
	}

	public void addProvider(BlockPos pos, PowerType powerType, AxisAlignedBB providerBox) {
		AxisAlignedBB blockBox = SINGLE_BLOCK_AABB.offset(pos);

		if (isAabbWithin(blockBox, providerBox)) {
			root.addProvider(pos, powerType, providerBox);
			root.recalculateMultipliers(providerBox, PowerTypeManager.getEffectedTypes(powerType));
		}
		else {
			TemporalConvergence.LOGGER.warn("Couldn't add provider at pos {} with {} because the position was not inside the bounding box", pos, providerBox);
		}
	}

	public void removeProvider(BlockPos pos, PowerType powerType) {
		root.removeProvider(pos, powerType, SINGLE_BLOCK_AABB.offset(pos));
	}

	public static class ProviderData {
		public final BlockPos pos;
		public final AxisAlignedBB range;
		public float multiplier;

		public ProviderData(BlockPos position, AxisAlignedBB aabb, float mult) {
			pos = position;
			range = aabb;
			multiplier = mult;
		}

		@Override
		public String toString() {
			return "Provider at " + pos + " with range " + range + " and multiplier " + multiplier;
		}
	}

	private boolean isAabbWithin(AxisAlignedBB inner, AxisAlignedBB outer) {
		return inner.minX >= outer.minX && inner.minY >= outer.minY && inner.minZ >= outer.minZ && inner.maxX <= outer.maxX && inner.maxY <= outer.maxY && inner.maxZ <= outer.maxZ;
	}

	private class TreeNode {
		private static final int MAX_LOAD = 20; //The maximum number of providers that can be placed in this node
		private static final int MIN_LENGTH = 16; //The minimum length of a side of the bounding box. If a side is below this, this node won't split in that direction

		private final AxisAlignedBB range;
		private TreeNode[] children = null;
		private Map<PowerType, List<ProviderData>> typeProviderMap = null;
		private int providers = 0;

		public TreeNode(AxisAlignedBB aabb) {
			range = aabb;
		}

		public void getIntersectingProviders(PowerType powerType, AxisAlignedBB aabb, List<ProviderData> posList) {
			if (typeProviderMap != null && typeProviderMap.get(powerType) != null) {
				List<ProviderData> providerList = typeProviderMap.get(powerType);

				for (int i = 0; i < providerList.size(); i++) {
					ProviderData data = providerList.get(i);

					if (data.range.intersects(aabb)) {
						posList.add(data);
					}
				}
			}

			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (children[i].range.intersects(aabb)) {
						children[i].getIntersectingProviders(powerType, aabb, posList);
					}
				}
			}
		}

		public void addProvider(BlockPos pos, PowerType powerType, AxisAlignedBB providerBox) {
			//Try adding to children, if any
			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (isAabbWithin(providerBox, children[i].range)) {
						children[i].addProvider(pos, powerType, providerBox);
						return;
					}
				}
			}

			//Couldn't add to children, so add to self
			PowerType[] types = PowerTypeManager.getEffectingTypes(powerType);
			float multiplier = types == null ? 1.0f : root.calculateMultiplier(powerType, providerBox, types, new int[types.length]);

			addProvider(powerType, new ProviderData(pos, providerBox, multiplier));

			//If too full, split tree
			if (shouldSplit()) {
				splitNode();
			}
		}

		public void removeProvider(BlockPos pos, PowerType powerType, AxisAlignedBB posBox) {
			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (isAabbWithin(posBox, children[i].range)) {
						children[i].removeProvider(pos, powerType, posBox);
						break;
					}
				}
			}

			if (typeProviderMap != null) {
				List<ProviderData> dataList = typeProviderMap.get(powerType);

				if (dataList != null) {
					for (int i = dataList.size() - 1; i >= 0; i--) {
						if (dataList.get(i).pos.equals(pos)) {
							ProviderData removed = dataList.get(i);
							dataList.remove(i);
							providers--;
							root.recalculateMultipliers(removed.range, PowerTypeManager.getEffectedTypes(powerType));
						}
					}
				}
			}
		}

		public void recalculateMultipliers(AxisAlignedBB changedBox, PowerType[] effectedTypes) {
			if (effectedTypes.length == 0) {
				return;
			}

			if (typeProviderMap != null) {
				for (int i = 0; i < effectedTypes.length; i++) {
					List<ProviderData> dataList = typeProviderMap.get(effectedTypes[i]);

					if (dataList != null) {
						for (int j = 0; j < dataList.size(); j++) {
							if (dataList.get(j).range.intersects(changedBox)) {
								PowerType[] types = PowerTypeManager.getEffectingTypes(effectedTypes[i]);
								int[] typeCounts = new int[types.length];

								dataList.get(j).multiplier = root.calculateMultiplier(effectedTypes[i], dataList.get(j).range, types, typeCounts);
							}
						}
					}
				}
			}

			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (changedBox.intersects(children[i].range)) {
						children[i].recalculateMultipliers(changedBox, effectedTypes);
					}
				}
			}
		}

		private float calculateMultiplier(PowerType powerType, AxisAlignedBB box, PowerType[] multiplyingTypes, int[] typeCounts) {
			float multiplier = 1.0f;

			if (typeProviderMap != null) {
				for (int i = 0; i < multiplyingTypes.length; i++) {
					List<ProviderData> dataList = typeProviderMap.get(multiplyingTypes[i]);

					if (dataList != null) {
						for (int j = 0; j < dataList.size(); j++) {
							if (dataList.get(j).range.intersects(box)) {
								multiplier *= (PowerTypeManager.getMultiplier(multiplyingTypes[i], powerType) - 1.0) * (1.0 / Math.pow(2.0, typeCounts[i])) + 1.0;
								typeCounts[i]++;
							}
						}
					}
				}
			}

			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (box.intersects(children[i].range)) {
						multiplier *= children[i].calculateMultiplier(powerType, box, multiplyingTypes, typeCounts);
					}
				}
			}

			return multiplier;
		}

		private boolean shouldSplit() {
			return isLeaf() && providers > MAX_LOAD && (canXSplit() || canYSplit() || canZSplit());
		}

		private boolean canXSplit() {
			return range.maxX - range.minX > MIN_LENGTH;
		}

		private boolean canYSplit() {
			return range.maxY - range.minY > MIN_LENGTH;
		}

		private boolean canZSplit() {
			return range.maxZ - range.minZ > MIN_LENGTH;
		}

		private void addProvider(PowerType powerType, ProviderData data) {
			if (typeProviderMap == null) {
				typeProviderMap = new HashMap<>();
			}

			List<ProviderData> providerList = typeProviderMap.get(powerType);

			if (providerList == null) {
				typeProviderMap.put(powerType, new ArrayList<>());
				providerList = typeProviderMap.get(powerType);
			}

			providerList.add(data);
			providers++;
		}

		private void splitNode() {
			//Create children
			int numChildren = 1;

			if (canXSplit()) {
				numChildren *= 2;
			}

			if (canYSplit()) {
				numChildren *= 2;
			}

			if (canZSplit()) {
				numChildren *= 2;
			}

			children = new TreeNode[numChildren];

			//Taken from AxisAlignedBB.getCenter() because client side only
			Vec3d center = new Vec3d(range.minX + (range.maxX - range.minX) * 0.5, range.minY + (range.maxY - range.minY) * 0.5, range.minZ + (range.maxZ - range.minZ) * 0.5);
			int index = 0;

			for (int i = 0; i <= (canXSplit() ? 1 : 0); i++) {
				for (int j = 0; j <= (canYSplit() ? 1 : 0); j++) {
					for (int k = 0; k <= (canZSplit() ? 1 : 0); k++) {
						double minX = canXSplit() && i == 1 ? center.x : range.minX;
						double minY = canYSplit() && j == 1 ? center.y : range.minY;
						double minZ = canZSplit() && k == 1 ? center.z : range.minZ;
						double maxX = canXSplit() && i == 0 ? center.x : range.maxX;
						double maxY = canYSplit() && j == 0 ? center.y : range.maxY;
						double maxZ = canZSplit() && k == 0 ? center.z : range.maxZ;

						children[index++] = new TreeNode(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
					}
				}
			}

			//Add providers to children
			for (Entry<PowerType, List<ProviderData>> entry : typeProviderMap.entrySet()) {
				List<ProviderData> providerList = entry.getValue();

				for (int i = providerList.size() - 1; i >= 0; i--) {
					for (int j = 0; j < children.length; j++) {
						ProviderData data = providerList.get(i);

						if (isAabbWithin(data.range, children[j].range)) {
							children[j].addProvider(entry.getKey(), data);
							providerList.remove(i);
							providers--;
							break;
						}
					}
				}
			}
		}

		private void addFromLoad(BlockPos pos, PowerType powerType, AxisAlignedBB providerBox, float multiplier) {
			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					if (isAabbWithin(providerBox, children[i].range)) {
						children[i].addFromLoad(pos, powerType, providerBox, multiplier);
						return;
					}
				}
			}

			addProvider(powerType, new ProviderData(pos, providerBox, multiplier));

			if (shouldSplit()) {
				splitNode();
			}
		}

		private boolean isLeaf() {
			return children == null;
		}

		@Override
		public String toString() {
			String output = "";

			output += "Range: " + range.toString() + "\n";
			output += "Providers: " + providers + "\n";

			if (typeProviderMap != null) {
				for (Map.Entry<PowerType, List<ProviderData>> entry : typeProviderMap.entrySet()) {
					if (entry.getValue() != null && !entry.getValue().isEmpty()) {
						output += "\t" + entry.getKey() + ":\n";

						for (ProviderData data : entry.getValue()) {
							output += "\t\t" + data + "\n";
						}
					}
					else {
						output += "\t" + entry.getKey() + ": present but empty\n";
					}
				}
			}

			if (isLeaf()) {
				output += "Children: none\n";
			}
			else {
				output += "Children:\n";
				for (TreeNode node : children) {
					output += "\t" + node.toString().replace("\n", "\n\t") + "\n";
				}
			}

			return output;
		}
	}

	@Override
	public NBTTagList serializeNBT() {
		NBTTagList list = new NBTTagList();

		addNodeToList(root, list);

		return list;
	}

	private void addNodeToList(TreeNode node, NBTTagList list) {
		if (node.typeProviderMap != null) {
			for (Map.Entry<PowerType, List<ProviderData>> entry : node.typeProviderMap.entrySet()) {
				if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					for (ProviderData data : entry.getValue()) {
						NBTTagCompound comp = new NBTTagCompound();
						comp.setString("type", entry.getKey().getName());
						comp.setLong("pos", data.pos.toLong());
						comp.setDouble("minX", data.range.minX);
						comp.setDouble("minY", data.range.minY);
						comp.setDouble("minZ", data.range.minZ);
						comp.setDouble("maxX", data.range.maxX);
						comp.setDouble("maxY", data.range.maxY);
						comp.setDouble("maxZ", data.range.maxZ);
						comp.setFloat("mult", data.multiplier);

						list.appendTag(comp);
					}
				}
			}
		}

		if (!node.isLeaf()) {
			for (int i = 0; i < node.children.length; i++) {
				addNodeToList(node.children[i], list);
			}
		}
	}

	@Override
	public void deserializeNBT(NBTTagList list) {
		root = new TreeNode(new AxisAlignedBB(-30000000, 0, -30000000, 30000000, 256, 30000000));

		for (NBTBase base : list) {
			if (base instanceof NBTTagCompound) {
				NBTTagCompound comp = (NBTTagCompound) base;

				if (comp.hasKey("type", Constants.NBT.TAG_STRING) && comp.hasKey("pos", Constants.NBT.TAG_LONG)
						&& comp.hasKey("minX", Constants.NBT.TAG_DOUBLE) && comp.hasKey("minY", Constants.NBT.TAG_DOUBLE)
						&& comp.hasKey("minZ", Constants.NBT.TAG_DOUBLE) && comp.hasKey("maxX", Constants.NBT.TAG_DOUBLE)
						&& comp.hasKey("maxY", Constants.NBT.TAG_DOUBLE) && comp.hasKey("maxZ", Constants.NBT.TAG_DOUBLE)) {
					AxisAlignedBB box = new AxisAlignedBB(comp.getDouble("minX"), comp.getDouble("minY"), comp.getDouble("minZ"), comp.getDouble("maxX"), comp.getDouble("maxY"), comp.getDouble("maxZ"));
					root.addFromLoad(BlockPos.fromLong(comp.getLong("pos")), PowerTypeManager.getType(comp.getString("type")), box, comp.getFloat("mult"));
				}
				else {
					TemporalConvergence.LOGGER.error("Error loading tag {} for ProviderTree", comp);
				}
			}
		}
	}

	@Override
	public String toString() {
		return root.toString();
	}

	public void log() {
		for (String toLog : toString().split("\n")) {
			TemporalConvergence.LOGGER.info(toLog);
		}
	}
}


/*
Debugging code - best ran statically from the main mod class:

ProviderTree tree = new ProviderTree(new PowerTreeData());

tree.addProvider(new BlockPos(1, 1, 1), "water", new AxisAlignedBB(0.0, 0.0, 0.0, 2.0, 2.0, 2.0), true);
tree.addProvider(new BlockPos(-2, 1, 1), "debug_base", new AxisAlignedBB(-2.0, 0.0, 0.0, 0.0, 2.0, 2.0), true);
tree.addProvider(new BlockPos(1, 1, -2), "air", new AxisAlignedBB(0.0, 0.0, -2.0, 2.0, 2.0, 0.0), true);
tree.addProvider(new BlockPos(-2, 1, -2), "earth", new AxisAlignedBB(-2.0, 0.0, -2.0, 0.0, 2.0, 0.0), true);
tree.addProvider(new BlockPos(0, 1, 0), "fire", new AxisAlignedBB(-1.0, 0.0, -1.0, 1.0, 2.0, 1.0), true);

tree.setActive(new BlockPos(-2, 1, -2), "earth", false);
tree.removeProvider(new BlockPos(1, 1, -2), "air");
tree.setActive(new BlockPos(-2, 1, -2), "earth", true);

tree.deserializeNBT(tree.serializeNBT());

tree.log();

TemporalConvergence.LOGGER.info(tree.getIntersectingProviders("water", new AxisAlignedBB(-2.0, 0.0, -2.0, 2.0, 2.0, 2.0)));
TemporalConvergence.LOGGER.info(tree.getIntersectingProviders("debug_base", new AxisAlignedBB(-2.0, 0.0, -2.0, 2.0, 2.0, 2.0)));
TemporalConvergence.LOGGER.info(tree.getIntersectingProviders("air", new AxisAlignedBB(-2.0, 0.0, -2.0, 2.0, 2.0, 2.0)));
TemporalConvergence.LOGGER.info(tree.getIntersectingProviders("earth", new AxisAlignedBB(-2.0, 0.0, -2.0, 2.0, 2.0, 2.0)));
TemporalConvergence.LOGGER.info(tree.getIntersectingProviders("fire", new AxisAlignedBB(-2.0, 0.0, -2.0, 2.0, 2.0, 2.0)));
 */