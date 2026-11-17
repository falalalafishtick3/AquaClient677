package com.aqua.client.gui;

import com.aqua.client.AquaClient;
import com.aqua.client.config.HighlightSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends Screen {
    private static final int CYAN = 0xFF20CBB8;
    private static final int PANEL = 0xE9101719;
    private static final String[] MODES = {"Outline", "Outline + Fill", "Filled"};
    private HighlightSettings selected;
    private String selectedName = "StorageFinder";

    public ClickGuiScreen() { super(Component.literal("Aqua Client")); selected = AquaClient.CONFIG.storage; }

    @Override
    protected void init() {
        clearWidgets();
        int panelW = 190;
        int gap = 12;
        int totalW = panelW * 3 + gap * 2;
        int left = (width - totalW) / 2;
        int top = Math.max(24, (height - 300) / 2);
        addRenderableWidget(Button.builder(Component.literal("RENDER"), b -> select("StorageFinder"))
                .bounds(left, top, panelW, 22).build());
        addRenderableWidget(Button.builder(Component.literal("VISUALS"), b -> select("SpawnerFinder"))
                .bounds(left + panelW + gap, top, panelW, 22).build());
        addRenderableWidget(Button.builder(Component.literal("CLIENT"), b -> {})
                .bounds(left + (panelW + gap) * 2, top, panelW, 22).build());

        int x = left;
        int y = top + 34;
        addButton(x, y, "StorageFinder", () -> select("StorageFinder"));
        addButton(x, y + 25, "SpawnerFinder", () -> select("SpawnerFinder"));

        int sx = left + panelW + gap;
        int sy = y;
        addButton(sx, sy, "Enabled: " + onOff(selected.enabled), () -> { selected.enabled = !selected.enabled; refresh(); });
        addButton(sx, sy + 27, "Mode: " + selected.mode, () -> { selected.mode = MODES[(indexOf(MODES, selected.mode) + 1) % MODES.length]; refresh(); });
        addButton(sx, sy + 54, "Outline opacity: " + pct(selected.outlineOpacity), () -> { selected.outlineOpacity = step(selected.outlineOpacity); refresh(); });
        addButton(sx, sy + 81, "Fill opacity: " + pct(selected.fillOpacity), () -> { selected.fillOpacity = step(selected.fillOpacity); refresh(); });
        addButton(sx, sy + 108, "Thickness: " + selected.thickness, () -> { selected.thickness = selected.thickness >= 5 ? 1 : selected.thickness + 1; refresh(); });
        addButton(sx, sy + 135, "Range: " + selected.range, () -> { selected.range = selected.range >= 128 ? 16 : selected.range + 16; refresh(); });
        addButton(sx, sy + 162, "Through walls: " + onOff(selected.throughWalls), () -> { selected.throughWalls = !selected.throughWalls; refresh(); });
        addButton(sx, sy + 189, "Tracers: " + onOff(selected.tracers), () -> { selected.tracers = !selected.tracers; refresh(); });
        addButton(sx, sy + 216, "Rainbow: " + onOff(selected.rainbow), () -> { selected.rainbow = !selected.rainbow; refresh(); });
        addButton(sx, sy + 243, "Pulse: " + onOff(selected.pulse), () -> { selected.pulse = !selected.pulse; refresh(); });
        addButton(left + (panelW + gap) * 2, y, "Colour: " + hex(selected.color), () -> { selected.color = nextColor(selected.color); refresh(); });
        addButton(left + (panelW + gap) * 2, y + 27, "Save settings", () -> AquaClient.saveConfig());
        addButton(left + (panelW + gap) * 2, y + 54, "Close", this::onClose);
    }

    private void addButton(int x, int y, String text, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> action.run()).bounds(x, y, 190, 22).build());
    }
    private void select(String name) {
        selectedName = name;
        selected = name.equals("StorageFinder") ? AquaClient.CONFIG.storage : AquaClient.CONFIG.spawner;
        init();
    }
    private void refresh() { init(); }
    private static int indexOf(String[] a, String s) { for (int i=0;i<a.length;i++) if (a[i].equals(s)) return i; return 0; }
    private static float step(float v) { return v >= 1.0f ? 0.0f : Math.min(1.0f, v + 0.1f); }
    private static String pct(float v) { return Math.round(v * 100) + "%"; }
    private static String onOff(boolean v) { return v ? "ON" : "OFF"; }
    private static String hex(int v) { return String.format("#%06X", v & 0xFFFFFF); }
    private static int nextColor(int c) { int[] colors={0x35D6C2,0x4CA3FF,0xB56CFF,0xFFB84D,0xFF5D78,0xFFFFFF}; for(int i=0;i<colors.length;i++) if(colors[i]==c) return colors[(i+1)%colors.length]; return colors[0]; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw a simple dim overlay instead of calling renderBackground().
        // On Minecraft 1.21.11 that helper can request the background blur a
        // second time in the same frame, causing "Can only blur once per frame".
        graphics.fill(0, 0, this.width, this.height, 0x99000000);
        int panelW = 190, gap = 12, totalW = panelW * 3 + gap * 2;
        int left = (width - totalW) / 2, top = Math.max(24, (height - 300) / 2);
        for (int i=0;i<3;i++) {
            int x=left+i*(panelW+gap);
            graphics.fill(x, top, x+panelW, top+290, PANEL);
            graphics.fill(x, top, x+panelW, top+22, CYAN);
        }
        graphics.drawCenteredString(font, "A Q U A   C L I E N T", width/2, top-18, 0xFFE8FFFF);
        graphics.drawString(font, "MODULES", left+10, top+64, 0xFFB7C8C8, false);
        graphics.drawString(font, "SETTINGS  /  " + selectedName, left+panelW+gap+10, top+64, 0xFFB7C8C8, false);
        graphics.drawString(font, "AQUA", left+(panelW+gap)*2+10, top+64, 0xFFB7C8C8, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { AquaClient.saveConfig(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
