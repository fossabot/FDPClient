/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.shader.FramebufferShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "StorageESP", description = "Allows you to see chests, dispensers, etc. through walls.", category = ModuleCategory.RENDER)
public class StorageESP extends Module {
    private final ListValue modeValue = new ListValue("Mode", new String[]{"Box", "OtherBox", "Outline", "ShaderOutline", "ShaderGlow", "2D", "WireFrame"}, "Outline");

    private final FloatValue outlineWidth = new FloatValue("Outline-Width", 3F, 0.5F, 5F);
    private final BoolValue chestValue = new BoolValue("Chest", true);
    private final BoolValue enderChestValue = new BoolValue("EnderChest", true);
    private final BoolValue furnaceValue = new BoolValue("Furnace", true);
    private final BoolValue dispenserValue = new BoolValue("Dispenser", true);
    private final BoolValue hopperValue = new BoolValue("Hopper", true);

    private Color getColor(TileEntity tileEntity){
        Color color = null;

        if (chestValue.get() && tileEntity instanceof TileEntityChest && !ChestAura.INSTANCE.getClickedBlocks().contains(tileEntity.getPos()))
            color = new Color(0, 66, 255);

        if (enderChestValue.get() && tileEntity instanceof TileEntityEnderChest && !ChestAura.INSTANCE.getClickedBlocks().contains(tileEntity.getPos()))
            color = Color.MAGENTA;

        if (furnaceValue.get() && tileEntity instanceof TileEntityFurnace)
            color = Color.BLACK;

        if (dispenserValue.get() && tileEntity instanceof TileEntityDispenser)
            color = Color.BLACK;

        if (hopperValue.get() && tileEntity instanceof TileEntityHopper)
            color = Color.GRAY;

        return color;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        try {
            final String mode = modeValue.get();

            float gamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 100000.0F;

            for (final TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                Color color=getColor(tileEntity);

                if (color == null)
                    continue;

                switch (mode.toLowerCase()) {
                    case "otherbox":
                    case "box":
                        RenderUtils.drawBlockBox(tileEntity.getPos(), color, !mode.equalsIgnoreCase("otherbox"),true, outlineWidth.get());
                        break;
                    case "2d":
                        RenderUtils.draw2D(tileEntity.getPos(), color.getRGB(), Color.BLACK.getRGB());
                        break;
                    case "outline":
                        RenderUtils.drawBlockBox(tileEntity.getPos(), color, true,false, outlineWidth.get());
                        break;
                    case "wireframe":
                        glPushMatrix();
                        glPushAttrib(GL_ALL_ATTRIB_BITS);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                        glDisable(GL_TEXTURE_2D);
                        glDisable(GL_LIGHTING);
                        glDisable(GL_DEPTH_TEST);
                        glEnable(GL_LINE_SMOOTH);
                        glEnable(GL_BLEND);
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        RenderUtils.glColor(color);
                        glLineWidth(1.5F);
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.getPartialTicks(), -1);
                        glPopAttrib();
                        glPopMatrix();
                        break;
                }
            }
            RenderUtils.glColor(new Color(255, 255, 255, 255));
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception ignored) {
        }
    }

    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        final String mode = modeValue.get();
        final RenderManager renderManager = mc.getRenderManager();
        final float partialTicks=event.getPartialTicks();

        final FramebufferShader shader = mode.equalsIgnoreCase("shaderoutline")
                ? OutlineShader.OUTLINE_SHADER : mode.equalsIgnoreCase("shaderglow")
                ? GlowShader.GLOW_SHADER : null;

        if (shader == null) return;

        try {
            Map<Color, ArrayList<TileEntity>> entityMap=new HashMap<>();
            //search
            for (final TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                Color color=getColor(tileEntity);

                if (color == null)
                    continue;

                if(!entityMap.containsKey(color)){
                    entityMap.put(color,new ArrayList<>());
                }
                entityMap.get(color).add(tileEntity);
            }
            //draw
            for(Map.Entry<Color, ArrayList<TileEntity>> entry:entityMap.entrySet()){
                shader.startDraw(partialTicks);

                for(TileEntity tileEntity:entry.getValue()){
                    TileEntityRendererDispatcher.instance.renderTileEntityAt(
                            tileEntity,
                            tileEntity.getPos().getX() - renderManager.renderPosX,
                            tileEntity.getPos().getY() - renderManager.renderPosY,
                            tileEntity.getPos().getZ() - renderManager.renderPosZ,
                            partialTicks
                    );
                }

                shader.stopDraw(entry.getKey(), mode.equalsIgnoreCase("shaderglow") ? 2.5F : 1.5F, 1F);
            }
        } catch (final Exception ex) {
            ClientUtils.getLogger().error("An error occurred while rendering all storages for shader esp", ex);
        }
    }
}

