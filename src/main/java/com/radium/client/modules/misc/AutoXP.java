package com.radium.client.modules.misc;

import com.radium.client.client.RadiumClient;
import com.radium.client.events.event.ItemUseListener;
import com.radium.client.gui.settings.NumberSetting;
import com.radium.client.modules.Module;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public final class AutoXP extends Module implements ItemUseListener {
    private final NumberSetting delay = new NumberSetting("Delay", 0, 0, 20, 1);
    private final NumberSetting chance = new NumberSetting("Chance", 100, 0, 100, 1);

    private final Random random = new Random();
    private int clock = 0;

    public AutoXP() {
        super("Auto XP", "Automatically throws XP bottles for you", Category.MISC);
        addSettings(delay, chance);
    }

    @Override
    public void onEnable() {
        RadiumClient.getEventManager().add(ItemUseListener.class, this);
        clock = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        RadiumClient.getEventManager().remove(ItemUseListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (!isEnabled())
            return;
        if (mc.currentScreen != null)
            return;
        if (mc.player == null)
            return;

        boolean dontThrow = clock != 0;

        int randomInt = random.nextInt(100) + 1;

        if (mc.player.getMainHandStack().getItem() != Items.EXPERIENCE_BOTTLE)
            return;

        if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) != GLFW.GLFW_PRESS)
            return;

        if (dontThrow) {
            clock--;
            return;
        }

        if (randomInt <= chance.getValue().intValue()) {
            ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            if (result.isAccepted())
                mc.player.swingHand(Hand.MAIN_HAND);

            clock = delay.getValue().intValue();
        }
    }

    @Override
    public void onItemUse(ItemUseEvent event) {
        if (mc.player != null && mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
            event.cancel();
        }
    }
}
