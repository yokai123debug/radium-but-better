package com.radium.client.modules.misc;

import com.radium.client.client.RadiumClient;
import com.radium.client.events.event.AttackListener;
import com.radium.client.events.event.BlockBreakingListener;
import com.radium.client.events.event.ItemUseListener;
import com.radium.client.gui.settings.BooleanSetting;
import com.radium.client.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

public final class Prevent extends Module implements ItemUseListener, AttackListener, BlockBreakingListener {
    private final BooleanSetting doubleGlowstone = new BooleanSetting("Double Glowstone", false);
    private final BooleanSetting glowstoneMisplace = new BooleanSetting("Glowstone Misplace", false);
    private final BooleanSetting anchorOnAnchor = new BooleanSetting("Anchor on Anchor", false);
    private final BooleanSetting obiPunch = new BooleanSetting("Obi Punch", false);
    private final BooleanSetting echestClick = new BooleanSetting("E-chest Click", false);

    public Prevent() {
        super("Prevent", "Prevents you from certain actions", Category.MISC);
        addSettings(doubleGlowstone, glowstoneMisplace, anchorOnAnchor, obiPunch, echestClick);
    }

    @Override
    public void onEnable() {
        RadiumClient.getEventManager().add(BlockBreakingListener.class, this);
        RadiumClient.getEventManager().add(AttackListener.class, this);
        RadiumClient.getEventManager().add(ItemUseListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        RadiumClient.getEventManager().remove(BlockBreakingListener.class, this);
        RadiumClient.getEventManager().remove(AttackListener.class, this);
        RadiumClient.getEventManager().remove(ItemUseListener.class, this);
        super.onDisable();
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (mc.player == null)
            return;
        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            if (isBlock(hit, Blocks.OBSIDIAN) && obiPunch.getValue()
                    && mc.player.isHolding(Items.END_CRYSTAL))
                event.cancel();
        }
    }

    @Override
    public void onBlockBreaking(BlockBreakingEvent event) {
        if (mc.player == null)
            return;
        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            if (isBlock(hit, Blocks.OBSIDIAN) && obiPunch.getValue()
                    && mc.player.isHolding(Items.END_CRYSTAL))
                event.cancel();
        }
    }

    @Override
    public void onItemUse(ItemUseEvent event) {
        if (mc.player == null)
            return;
        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            // Prevent double glowstone on charged anchor
            if (isAnchorCharged(hit) && doubleGlowstone.getValue()
                    && mc.player.isHolding(Items.GLOWSTONE))
                event.cancel();

            // Prevent glowstone misplace
            if (!isBlock(hit, Blocks.RESPAWN_ANCHOR) && glowstoneMisplace.getValue()
                    && mc.player.isHolding(Items.GLOWSTONE))
                event.cancel();

            // Prevent anchor on uncharged anchor
            if (isAnchorNotCharged(hit) && anchorOnAnchor.getValue()
                    && mc.player.isHolding(Items.RESPAWN_ANCHOR))
                event.cancel();

            // Prevent echest click with PvP items
            if (isBlock(hit, Blocks.ENDER_CHEST) && echestClick.getValue() &&
                    (mc.player.getMainHandStack().contains(DataComponentTypes.TOOL)
                            || mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
                            || mc.player.getMainHandStack().getItem() == Items.OBSIDIAN
                            || mc.player.getMainHandStack().getItem() == Items.RESPAWN_ANCHOR
                            || mc.player.getMainHandStack().getItem() == Items.GLOWSTONE))
                event.cancel();
        }
    }

    private boolean isBlock(BlockHitResult hit, net.minecraft.block.Block block) {
        return mc.world != null && mc.world.getBlockState(hit.getBlockPos()).isOf(block);
    }

    private boolean isAnchorCharged(BlockHitResult hit) {
        if (mc.world == null)
            return false;
        var state = mc.world.getBlockState(hit.getBlockPos());
        if (!state.isOf(Blocks.RESPAWN_ANCHOR))
            return false;
        return state.get(net.minecraft.block.RespawnAnchorBlock.CHARGES) > 0;
    }

    private boolean isAnchorNotCharged(BlockHitResult hit) {
        if (mc.world == null)
            return false;
        var state = mc.world.getBlockState(hit.getBlockPos());
        if (!state.isOf(Blocks.RESPAWN_ANCHOR))
            return false;
        return state.get(net.minecraft.block.RespawnAnchorBlock.CHARGES) == 0;
    }
}
