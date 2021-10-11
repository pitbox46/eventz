package github.pitbox46.eventz.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import github.pitbox46.eventz.network.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderMixin {
    @Shadow
    @Final
    private static ResourceLocation FORCEFIELD_TEXTURES;
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    protected abstract void addVertex(BufferBuilder bufferIn, double camX, double camY, double camZ, double xIn, int yIn, double zIn, float texU, float texV);

    @Inject(at = @At(value = "HEAD"), method = "renderWorldBorder")
    private void onRenderWorldBorder(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        if (ClientProxy.getEventBoundary() == null) return;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        WorldBorder worldborder = ClientProxy.getEventBoundary();
        double d0 = this.mc.gameSettings.renderDistanceChunks * 16;
        if (!(activeRenderInfoIn.getProjectedView().x < worldborder.maxX() - d0) || !(activeRenderInfoIn.getProjectedView().x > worldborder.minX() + d0) || !(activeRenderInfoIn.getProjectedView().z < worldborder.maxZ() - d0) || !(activeRenderInfoIn.getProjectedView().z > worldborder.minZ() + d0)) {
            double d1 = 1.0D - worldborder.getClosestDistance(activeRenderInfoIn.getProjectedView().x, activeRenderInfoIn.getProjectedView().z) / d0;
            d1 = Math.pow(d1, 4.0D);
            double d2 = activeRenderInfoIn.getProjectedView().x;
            double d3 = activeRenderInfoIn.getProjectedView().y;
            double d4 = activeRenderInfoIn.getProjectedView().z;
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.textureManager.bindTexture(FORCEFIELD_TEXTURES);
            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            RenderSystem.pushMatrix();
            int i = worldborder.getStatus().getColor();
            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            RenderSystem.color4f(f, f1, f2, (float) d1);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            float f3 = (float) (Util.milliTime() % 3000L) / 3000.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 128.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            double d5 = Math.max(MathHelper.floor(d4 - d0), worldborder.minZ());
            double d6 = Math.min(MathHelper.ceil(d4 + d0), worldborder.maxZ());
            if (d2 > worldborder.maxX() - d0) {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F) {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float) d8 * 0.5F;
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.maxX(), 256, d7, f3 + f7, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.maxX(), 256, d7 + d8, f3 + f8 + f7, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.maxX(), 0, d7 + d8, f3 + f8 + f7, f3 + 128.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.maxX(), 0, d7, f3 + f7, f3 + 128.0F);
                    ++d7;
                }
            }

            if (d2 < worldborder.minX() + d0) {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F) {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float) d12 * 0.5F;
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.minX(), 256, d9, f3 + f9, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.minX(), 256, d9 + d12, f3 + f12 + f9, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.minX(), 0, d9 + d12, f3 + f12 + f9, f3 + 128.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, worldborder.minX(), 0, d9, f3 + f9, f3 + 128.0F);
                    ++d9;
                }
            }

            d5 = Math.max(MathHelper.floor(d2 - d0), worldborder.minX());
            d6 = Math.min(MathHelper.ceil(d2 + d0), worldborder.maxX());
            if (d4 > worldborder.maxZ() - d0) {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F) {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float) d13 * 0.5F;
                    this.addVertex(bufferbuilder, d2, d3, d4, d10, 256, worldborder.maxZ(), f3 + f10, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d10 + d13, 256, worldborder.maxZ(), f3 + f13 + f10, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d10 + d13, 0, worldborder.maxZ(), f3 + f13 + f10, f3 + 128.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d10, 0, worldborder.maxZ(), f3 + f10, f3 + 128.0F);
                    ++d10;
                }
            }

            if (d4 < worldborder.minZ() + d0) {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F) {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float) d14 * 0.5F;
                    this.addVertex(bufferbuilder, d2, d3, d4, d11, 256, worldborder.minZ(), f3 + f11, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 256, worldborder.minZ(), f3 + f14 + f11, f3 + 0.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 0, worldborder.minZ(), f3 + f14 + f11, f3 + 128.0F);
                    this.addVertex(bufferbuilder, d2, d3, d4, d11, 0, worldborder.minZ(), f3 + f11, f3 + 128.0F);
                    ++d11;
                }
            }

            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.enableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
        }
    }
}
