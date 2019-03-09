package com.therandomlabs.randomportals.client;

import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.block.BlockLateralNetherPortal;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = RandomPortals.MOD_ID)
public final class RPOPortalRenderer {
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static BlockPos pos;
	private static Block block;

	private static float minU;
	private static float minV;
	private static float maxU;
	private static float maxV;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if(RPOConfig.NetherPortals.enabled &&
				event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
			final float timeInPortal = mc.player.prevTimeInPortal +
					(mc.player.timeInPortal - mc.player.prevTimeInPortal) * event.getPartialTicks();

			if(timeInPortal > 0.0F) {
				render(timeInPortal, event.getResolution());
			}

			event.setCanceled(true);
		}
	}

	public static void render(float timeInPortal, ScaledResolution resolution) {
		if(timeInPortal < 1.0F) {
			timeInPortal *= timeInPortal * timeInPortal * 0.8F;
			timeInPortal += 0.2F;
		}

		GlStateManager.disableAlpha();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);

		GlStateManager.tryBlendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO
		);
		GlStateManager.color(1.0F, 1.0F, 1.0F, timeInPortal);

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		final int width = resolution.getScaledWidth();
		final int height = resolution.getScaledHeight();

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.0, height, -90.0).tex(minU, maxV).endVertex();
		buffer.pos(width, height, -90.0).tex(maxU, maxV).endVertex();
		buffer.pos(width, 0.0, -90.0).tex(maxU, minV).endVertex();
		buffer.pos(0.0, 0.0, -90.0).tex(minU, minV).endVertex();

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void resetSprite(BlockPos pos, Block block) {
		if(RPOPortalRenderer.pos == null || pos.distanceSq(RPOPortalRenderer.pos) > 2.0 ||
				isPlayerCloser(pos) ||
				(pos.equals(RPOPortalRenderer.pos) && block != RPOPortalRenderer.block)) {
			//pos is a PooledMutableBlockPos
			RPOPortalRenderer.pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
			RPOPortalRenderer.block = block;

			final IBlockState stateToRender;

			if(block instanceof BlockNetherPortal) {
				if(block instanceof BlockLateralNetherPortal) {
					stateToRender = block.getDefaultState();
				} else {
					//If the portal is vertical, get the portal at eye-level
					final Block block2 = mc.world.getBlockState(pos.up()).getBlock();

					if(block2 instanceof BlockNetherPortal) {
						stateToRender = block2.getDefaultState();
					} else {
						stateToRender = block.getDefaultState();
					}
				}
			} else {
				stateToRender = Blocks.PORTAL.getDefaultState();
			}

			final TextureAtlasSprite sprite =
					mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(stateToRender);

			minU = sprite.getMinU();
			minV = sprite.getMinV();
			maxU = sprite.getMaxU();
			maxV = sprite.getMaxV();
		}
	}

	private static boolean isPlayerCloser(BlockPos pos) {
		final BlockPos playerPos = mc.player.getPosition();
		return pos.distanceSq(playerPos) < RPOPortalRenderer.pos.distanceSq(playerPos);
	}
}
