package com.radium.client.modules.combat;

import com.radium.client.gui.settings.BooleanSetting;
import com.radium.client.gui.settings.NumberSetting;
import com.radium.client.modules.Module;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class TotemOffhand extends Module {
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0, 0, 5, 1);
    private final NumberSetting equipDelay = new NumberSetting("Equip Delay", 1, 1, 5, 1);
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", false);

    private int switchClock = 0;
    private int equipClock = 0;
    private int switchBackClock = 0;
    private int previousSlot = -1;
    private boolean sent = false;
    private boolean active = false;

    public TotemOffhand() {
        super("Totem Offhand", "Switches to totem slot and offhands a totem if you don't have one", Category.COMBAT);
        addSettings(switchDelay, equipDelay, switchBack);
    }

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
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

        if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
            active = true;

        if (active) {
            if (switchClock < switchDelay.getValue().intValue()) {
                switchClock++;
                return;
            }

            if (previousSlot == -1)
                previousSlot = mc.player.getInventory().selectedSlot;

            if (selectTotemFromHotbar()) {
                if (equipClock < equipDelay.getValue().intValue()) {
                    equipClock++;
                    return;
                }

                if (!sent) {
                    mc.getNetworkHandler().getConnection().send(
                            new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                                    BlockPos.ORIGIN, Direction.DOWN));
                    sent = true;
                    return;
                }
            }

            if (switchBackClock < switchDelay.getValue().intValue()) {
                switchBackClock++;
            } else {
                if (switchBack.getValue())
                    mc.player.getInventory().selectedSlot = previousSlot;

                reset();
            }
        }
    }

    private boolean selectTotemFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private void reset() {
        switchClock = 0;
        equipClock = 0;
        switchBackClock = 0;
        previousSlot = -1;
        sent = false;
        active = false;
    }
}
