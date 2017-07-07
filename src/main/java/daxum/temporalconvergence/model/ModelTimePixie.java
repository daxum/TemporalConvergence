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
package daxum.temporalconvergence.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelTimePixie extends ModelBase {
	public static final float WING_DISTANCE = 0.25f; //Distance from body
	public static final float WING_SEPERATION = 0.02f; //Distance between two wings on the same side (y axis)
	public static final float WING_HEIGHT_CHANGE = 0.05f; //How high the wings are pushed up on the body
	public static final float WING_Z = -0.12f; //How far the wings are pushed towards the back of the body

	private final ModelRenderer body;
	private final ModelRenderer bodyOuter;
	private final WingModel wing1;
	private final WingModel wing2;
	private final WingModel wing3;
	private final WingModel wing4;

	//X - pitch, Y - yaw, Z - roll
	public ModelTimePixie() {
		textureWidth = 32;
		textureHeight = 32;

		body = new ModelRenderer(this, 0, 12);
		body.addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6);
		body.setRotationPoint(0.0f, 0.0f, 0.0f);

		bodyOuter = new ModelRenderer(this, 0, 0);
		bodyOuter.addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6);
		bodyOuter.setRotationPoint(0.0f, 0.0f, 0.0f);

		wing1 = new WingModel(this);
		wing1.offsetX = WING_DISTANCE;
		wing1.offsetY = WING_SEPERATION + WING_HEIGHT_CHANGE;
		wing1.offsetZ = WING_Z;

		wing2 = new WingModel(this);
		wing2.offsetX = WING_DISTANCE;
		wing2.offsetY = -WING_SEPERATION + WING_HEIGHT_CHANGE;
		wing2.offsetZ = WING_Z;

		wing3 = new WingModel(this);
		wing3.offsetX = -WING_DISTANCE;
		wing3.offsetY = WING_SEPERATION + WING_HEIGHT_CHANGE;
		wing3.offsetZ = WING_Z;
		wing3.rotateAngleZ = 180.0f;

		wing4 = new WingModel(this);
		wing4.offsetX = -WING_DISTANCE;
		wing4.offsetY = -WING_SEPERATION + WING_HEIGHT_CHANGE;
		wing4.offsetZ = WING_Z;
		wing4.rotateAngleZ = 180.0f;

		body.addChild(wing1);
		body.addChild(wing2);
		body.addChild(wing3);
		body.addChild(wing4);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f); //I don't know why this was upside-down, but I'm not putting up with it anymore.
		GlStateManager.translate(0.0f, -1.0f, 0.0f);	 //The above also makes things rotate counterclockwise, AS THEY SHOULD.

		body.render(scale);
		bodyOuter.render(scale * (4.0f / 3.0f));

		GlStateManager.popMatrix();

		GlStateManager.disableBlend();
		GlStateManager.disableNormalize();
		GlStateManager.enableLighting();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale, Entity entity) {
		float wingRot = 45.0f * MathHelper.cos(age / 2.0f);

		wing1.rotateAngleZ = wingRot;
		wing2.rotateAngleZ = -wingRot;
		wing3.rotateAngleZ = 180.0f - wingRot;
		wing4.rotateAngleZ = 180.0f + wingRot;
	}

	//I'm just hijacking this so I can use addChild() to get rotations
	//I've also changed rotation angles to be in degrees to save unnecessary conversions
	private static class WingModel extends ModelRenderer {
		protected static boolean compiled; //Stupid private. At least I can make them static now.
		protected static int displayList;

		public WingModel(ModelBase model) {
			super(model);
		}

		//Next three functions have to be overridden so it uses local compiled and display list
		@Override
		@SideOnly(Side.CLIENT)
		public void render(float scale) {
			if (!isHidden && showModel) { //What's the difference between these two things?
				if (!compiled)
					compileDisplayList(scale);

				GlStateManager.translate(offsetX, offsetY, offsetZ);

				if (rotateAngleX == 0.0f && rotateAngleY == 0.0f && rotateAngleZ == 0.0f) {
					if (rotationPointX == 0.0f && rotationPointY == 0.0f && rotationPointZ == 0.0f) {
						GlStateManager.callList(displayList);
					}
					else {
						GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
						GlStateManager.callList(displayList);

						GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
					}
				}
				else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

					if (rotateAngleZ != 0.0f)
						GlStateManager.rotate(rotateAngleZ, 0.0f, 0.0f, 1.0f);
					if (rotateAngleY != 0.0f)
						GlStateManager.rotate(rotateAngleY, 0.0f, 1.0f, 0.0f);
					if (rotateAngleX != 0.0f)
						GlStateManager.rotate(rotateAngleX, 1.0f, 0.0f, 0.0f);

					GlStateManager.callList(displayList);
					GlStateManager.popMatrix();
				}

				GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void renderWithRotation(float scale) {
			if (!isHidden && showModel) {
				if (!compiled)
					compileDisplayList(scale);

				GlStateManager.pushMatrix();
				GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

				if (rotateAngleZ != 0.0f)
					GlStateManager.rotate(rotateAngleZ, 0.0f, 0.0f, 1.0f);
				if (rotateAngleY != 0.0f)
					GlStateManager.rotate(rotateAngleY, 0.0f, 1.0f, 0.0f);
				if (rotateAngleX != 0.0f)
					GlStateManager.rotate(rotateAngleX, 1.0f, 0.0f, 0.0f);

				GlStateManager.callList(displayList);
				GlStateManager.popMatrix();
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void postRender(float scale) {
			if (!isHidden && showModel) {
				if (!compiled)
					compileDisplayList(scale);

				if (rotateAngleX == 0.0f && rotateAngleY == 0.0f && rotateAngleZ == 0.0f) {
					if (rotationPointX != 0.0f || rotationPointY != 0.0f || rotationPointZ != 0.0f)
						GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
				}
				else {
					GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

					if (rotateAngleZ != 0.0f)
						GlStateManager.rotate(rotateAngleZ, 0.0f, 0.0f, 1.0f);
					if (rotateAngleY != 0.0f)
						GlStateManager.rotate(rotateAngleY, 0.0f, 1.0f, 0.0f);
					if (rotateAngleX != 0.0f)
						GlStateManager.rotate(rotateAngleX, 1.0f, 0.0f, 0.0f);
				}
			}
		}

		@SideOnly(Side.CLIENT)
		protected void compileDisplayList(float scale) { //There's no real point to making this protected, but it makes me feel better...
			displayList = GLAllocation.generateDisplayLists(1);
			GlStateManager.glNewList(displayList, GL11.GL_COMPILE);
			BufferBuilder vb = Tessellator.getInstance().getBuffer();

			double width = 19 * scale;
			double height = 4.3 * scale;

			vb.begin(7, DefaultVertexFormats.POSITION_TEX);
			vb.pos(0, -0.01, height).tex(0.0, 0.78125).endVertex();
			vb.pos(width, -0.01, height).tex(0.8125, 0.78125).endVertex();
			vb.pos(width, -0.01, 0.0).tex(0.8125, 1.0).endVertex();
			vb.pos(0, -0.01, 0.0).tex(0.0, 1.0).endVertex();

			Tessellator.getInstance().draw();

			GlStateManager.glEndList();
			compiled = true;
		}

		//Don't use these
		@Override
		public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
			return this;
		}

		@Override
		public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
			return this;
		}

		@Override
		public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
			return this;
		}

		@Override
		public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {}

		@Override
		public void addChild(ModelRenderer ignored) {}
	}
}
