package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "KeyBindManager", description = "Opens the KeyBindManager.", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RMENU, canEnable = false)
public class KeyBindManager extends Module {
    @Override
    public void onEnable() {
        mc.displayGuiScreen(LiquidBounce.keyBindMgr);
    }
}
