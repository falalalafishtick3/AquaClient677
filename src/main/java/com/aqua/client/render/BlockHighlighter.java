package com.aqua.client.render;

import com.aqua.client.AquaClient;
import com.aqua.client.config.HighlightSettings;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BlockHighlighter {

    private static final Map<BlockPos, Boolean> STORAGE = new LinkedHashMap<>();
    private static final Map<BlockPos, Boolean> SPAWNERS = new LinkedHashMap<>();

    private static int scanDelay;

    private BlockHighlighter() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlighter::render);
    }

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            STORAGE.clear();
            SPAWNERS.clear();
            return;
        }

        if (++scanDelay < 20) return;
        scanDelay = 0;

        STORAGE.clear();
        SPAWNERS.clear();

        int radius = Math.max(
                AquaClient.CONFIG.storage.range,
                AquaClient.CONFIG.spawner.range
        );

        radius = Math.min(radius, 64);

        BlockPos origin = client.player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minY = Math.max(client.level.getMinY(), origin.getY() - 24);
        int maxY = Math.min(client.level.getMaxY(), origin.getY() + 24);

        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {

                int dx = x - origin.getX();
                int dz = z - origin.getZ();

                if (dx * dx + dz * dz > radius * radius) continue;

                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);

                    Block block = client.level.getBlockState(pos).getBlock();

                    if (isStorage(block)) {
                        STORAGE.put(pos.immutable(), Boolean.TRUE);
                    }

                    if (block == Blocks.SPAWNER) {
                        SPAWNERS.put(pos.immutable(), Boolean.TRUE);
                    }
                }
            }
        }
    }

    private static boolean isStorage(Block block) {
        return block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST
                || block == Blocks.ENDER_CHEST
                || block == Blocks.DROPPER
                || block == Blocks.DISPENSER
                || block == Blocks.BARREL
                || block instanceof ShulkerBoxBlock;
    }

    private static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        renderSet(STORAGE, AquaClient.CONFIG.storage);
        renderSet(SPAWNERS, AquaClient.CONFIG.spawner);
    }

    private static void renderSet(
            Map<BlockPos, Boolean> blocks,
            HighlightSettings settings
    ) {
        if (!settings.enabled || blocks.isEmpty()) return;

        // Randarea contururilor va fi adaugata ulterior.
    }

    public static int getStorageCount() {
        return STORAGE.size();
    }

    public static int getSpawnerCount() {
        return SPAWNERS.size();
    }
}
