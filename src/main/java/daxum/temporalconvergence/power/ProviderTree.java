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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ProviderTree {
	private final TreeNode root = new TreeNode(new AxisAlignedBB(-30000000, 0, -30000000, 30000000, 256, 30000000));

	public List<BlockPos> getIntersectingProviders(String powerType, AxisAlignedBB receiverBox) {
		List<BlockPos> posList = new ArrayList();

		root.getIntersectingProviders(powerType, receiverBox, posList);

		return posList;
	}

	public void addProvider(BlockPos pos, String powerType, AxisAlignedBB providerBox) {
		root.addProvider(pos, powerType, providerBox);
	}

	public void removeProvider(BlockPos pos, String powerType) {
		root.removeProvider(pos, powerType);
	}

	private class TreeNode {
		private static final int MAX_LOAD = 20; //The maximum number of providers that can be placed in this node
		private static final int MIN_LENGTH = 16; //The minimum length of a side of the bounding box. If a side is below this, this node won't split in that direction

		private final AxisAlignedBB range;
		private TreeNode[] children = null;
		private Map<String, List<ProviderData>> typeProviderMap = null;
		private int providers = 0;

		public TreeNode(AxisAlignedBB aabb) {
			range = aabb;
		}

		public void getIntersectingProviders(String powerType, AxisAlignedBB aabb, List<BlockPos> posList) {
			if (typeProviderMap != null && typeProviderMap.get(powerType) != null) {
				List<ProviderData> providerList = typeProviderMap.get(powerType);

				for (int i = 0; i < providerList.size(); i++) {
					ProviderData data = providerList.get(i);

					if (data.range.intersects(aabb)) {
						posList.add(data.pos);
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

		public void addProvider(BlockPos pos, String powerType, AxisAlignedBB providerBox) {
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
			addProvider(powerType, new ProviderData(pos, providerBox));

			//If too full, split tree
			if (shouldSplit()) {
				splitNode();
			}
		}

		public void removeProvider(BlockPos pos, String powerType) {
			if (!isLeaf()) {
				for (int i = 0; i < children.length; i++) {
					children[i].removeProvider(pos, powerType);
				}
			}

			if (typeProviderMap != null) {
				List<ProviderData> dataList = typeProviderMap.get(powerType);

				if (dataList != null) {
					for (int i = dataList.size() - 1; i >= 0; i--) {
						if (dataList.get(i).pos.equals(pos)) {
							dataList.remove(i);
							providers--;
						}
					}
				}
			}
		}

		private boolean isAabbWithin(AxisAlignedBB inner, AxisAlignedBB outer) {
			return inner.minX > outer.minX && inner.minY > outer.minY && inner.minZ > outer.minZ && inner.maxX < outer.maxX && inner.maxY < outer.maxY && inner.maxZ < outer.maxZ;
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

		private void addProvider(String powerType, ProviderData data) {
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
			for (Entry<String, List<ProviderData>> entry : typeProviderMap.entrySet()) {
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

		private boolean isLeaf() {
			return children == null;
		}

		public class ProviderData {
			public final BlockPos pos;
			public final AxisAlignedBB range;

			public ProviderData(BlockPos position, AxisAlignedBB aabb) {
				pos = position;
				range = aabb;
			}
		}

		@Override
		public String toString() {
			String output = "";

			output += "Range: " + range.toString() + "\n";
			output += "Providers: " + providers + "\n";

			if (typeProviderMap != null) {
				for (Map.Entry<String, List<ProviderData>> entry : typeProviderMap.entrySet()) {
					if (entry.getValue() != null && !entry.getValue().isEmpty()) {
						output += "\t" + entry.getKey() + ":\n";

						for (ProviderData data : entry.getValue()) {
							output += "\t\tProvider at " + data.pos.toString() + " with range " + data.range.toString() + "\n";
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
	public String toString() {
		return root.toString();
	}

	public void log() {
		for (String toLog : toString().split("\n")) {
			TemporalConvergence.LOGGER.info(toLog);
		}
	}
}
