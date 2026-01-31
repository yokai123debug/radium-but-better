package com.radium.client.modules.combat;

import com.radium.client.client.RadiumClient;
import com.radium.client.events.event.AttackListener;
import com.radium.client.gui.settings.BooleanSetting;
import com.radium.client.gui.settings.NumberSetting;
import com.radium.client.modules.Module;
import com.radium.client.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class ShieldDisabler extends Module implements AttackListener {
    private final NumberSetting hitDelay = new NumberSetting("Hit Delay", 0, 0, 20, 1);
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0, 0, 20, 1);
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true);
    private final BooleanSetting stun = new BooleanSetting("Stun", false);
    private final BooleanSetting requireHoldAxe = new BooleanSetting("Hold Axe", false);

    private int previousSlot = -1;
    private int hitClock = 0;
    private int switchClock = 0;

    public ShieldDisabler() {
        super("Shield Disabler", "Automatically disables your opponents shield", Category.COMBAT);
        addSettings(switchDelay, hitDelay, switchBack, stun, requireHoldAxe);
    }

    @Override
    public void onEnable() {
        RadiumClient.getEventManager().add(AttackListener.class, this);
        hitClock = hitDelay.getValue().intValue();
        switchClock = switchDelay.getValue().intValue();
        previousSlot = -1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        RadiumClient.getEventManager().remove(AttackListener.class, this);
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

        if (requireHoldAxe.getValue() && !(mc.player.getMainHandStack().getItem() instanceof AxeItem))
            return;

        if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();

            if (mc.player.isUsingItem())
                return;

            if (entity instanceof PlayerEntity player) {
                if (CombatUtils.isShieldFacingAway(player))
                    return;

                if (player.isHolding(Items.SHIELD) && player.isBlocking()) {
                    if (switchClock > 0) {
                        if (previousSlot == -1)
                            previousSlot = mc.player.getInventory().selectedSlot;

                        switchClock--;
                        return;
                    }

                    if (selectAxe()) {
                        if (hitClock > 0) {
                            hitClock--;
                        } else {
                            hitEntity(player);

                            if (stun.getValue()) {
                                hitEntity(player);
                            }

                            hitClock = hitDelay.getValue().intValue();
                            switchClock = switchDelay.getValue().intValue();
                        }
                    }
                } else if (previousSlot != -1) {
                    if (switchBack.getValue())
                        mc.player.getInventory().selectedSlot = previousSlot;

                    previousSlot = -1;
                }
            }
        }
    }

    private boolean selectAxe() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private void hitEntity(PlayerEntity player) {
        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
            event.cancel();
    }
}
