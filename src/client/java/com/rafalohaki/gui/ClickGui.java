package com.rafalohaki.gui;

import com.rafalohaki.module.Category;
import com.rafalohaki.module.ModuleManager;
import com.rafalohaki.module.modules.FlyModule;
import com.rafalohaki.module.modules.KillauraModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modern ClickGUI for module management.
 * Opens with Right Shift or J key.
 * 
 * Features:
 * - Category-based module organization
 * - Mouse scroll support
 * - Module settings display
 */
public class ClickGui extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("fiabrica");
    
    private Category selectedCategory = Category.COMBAT;
    private int scrollOffset = 0;
    private final int moduleHeight = 28;
    private final int categoryButtonWidth = 85;
    private final int moduleButtonWidth = 140;
    
    // Colors - improved contrast and modern palette
    private static final int COLOR_BACKGROUND = 0xDD0a0a0f;
    private static final int COLOR_PANEL = 0xEE1e1e2e;
    private static final int COLOR_ACCENT = 0xFF2563eb;
    private static final int COLOR_ENABLED = 0xFF10b981;
    private static final int COLOR_DISABLED = 0xFF374151;
    private static final int COLOR_HOVER = 0xFF4b5563;
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SECONDARY = 0xFF9ca3af;
    private static final int COLOR_BORDER = 0xFF475569;
    
    // Input state
    private boolean wasMousePressed = false;
    private double lastScrollY = 0;
    
    // Animation state
    private float animationProgress = 0f;
    private long lastAnimationTime = System.currentTimeMillis();
    
    public ClickGui() {
        super(Text.literal("Fiabrica ClickGUI"));
        LOGGER.info("ClickGui created");
    }
    
    @Override
    protected void init() {
        LOGGER.info("ClickGui init - Screen size: {}x{}", this.width, this.height);
        scrollOffset = 0;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animation
        updateAnimation(delta);
        
        // Handle mouse input
        MinecraftClient mc = MinecraftClient.getInstance();
        long windowHandle = mc.getWindow().getHandle();
        boolean isMousePressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        
        // Detect mouse click (press -> release)
        if (wasMousePressed && !isMousePressed) {
            handleClick(mouseX, mouseY);
        }
        wasMousePressed = isMousePressed;
        
        // Handle escape key
        if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            this.close();
            return;
        }
        
        // Render background (darkened game view)
        super.render(context, mouseX, mouseY, delta);
        
        // Draw semi-transparent overlay with animation
        float bgAlpha = Math.min(1.0f, animationProgress * 2.0f);
        int animatedBg = (int)(bgAlpha * 255) << 24 | (COLOR_BACKGROUND & 0x00FFFFFF);
        context.fill(0, 0, this.width, this.height, animatedBg);
        
        // Draw main panel with rounded corners effect
        int panelX = 20;
        int panelY = 30;
        int panelWidth = this.width - 40;
        int panelHeight = this.height - 60;
        
        drawRoundedPanel(context, panelX, panelY, panelWidth, panelHeight, COLOR_PANEL);
        
        // Draw title with gradient effect
        String title = "✦ FIABRICA ✦";
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(title).formatted(Formatting.AQUA, Formatting.BOLD),
            this.width / 2,
            panelY + 8,
            COLOR_TEXT_PRIMARY
        );
        
        // Draw separator line with gradient
        context.fillGradient(panelX + 10, panelY + 25, panelX + panelWidth - 10, panelY + 26, COLOR_ACCENT, COLOR_BORDER);
        
        // Draw category buttons
        drawCategoryButtons(context, mouseX, mouseY, panelX + 10, panelY + 35);
        
        // Draw modules for selected category
        drawModules(context, mouseX, mouseY, panelX + 10, panelY + 70, panelWidth - 20, panelHeight - 100);
        
        // Draw instructions with better formatting
        String instructions = "Click to toggle • Scroll to navigate • ESC to close";
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(instructions).formatted(Formatting.GRAY),
            panelX + 10,
            panelY + panelHeight - 18,
            COLOR_TEXT_SECONDARY
        );
        
        // Draw module count
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        String countText = String.format("%d modules in %s", modules.size(), selectedCategory.name());
        int countWidth = this.textRenderer.getWidth(countText);
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(countText).formatted(Formatting.DARK_GRAY),
            panelX + panelWidth - countWidth - 10,
            panelY + panelHeight - 18,
            COLOR_TEXT_SECONDARY
        );
    }
    
    // Instance variables for click detection (store panel position)
    private int panelX = 20;
    private int panelY = 30;
    private int categoryStartX, categoryStartY;
    private int moduleStartX, moduleStartY, moduleAreaWidth, moduleAreaHeight;
    
    private void updateAnimation(float delta) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastAnimationTime;
        
        if (deltaTime > 16) { // Cap at ~60 FPS
            if (animationProgress < 1.0f) {
                animationProgress = Math.min(1.0f, animationProgress + delta * 0.1f);
            }
            lastAnimationTime = currentTime;
        }
    }
    
    private void drawRoundedPanel(DrawContext context, int x, int y, int width, int height, int color) {
        // Simple rounded corners using multiple rectangles
        int cornerSize = 8;
        
        // Main body
        context.fill(x + cornerSize, y, x + width - cornerSize, y + height, color);
        context.fill(x, y + cornerSize, x + width, y + height - cornerSize, color);
        
        // Corner pieces (rounded effect)
        context.fill(x + cornerSize, y + cornerSize, x + cornerSize * 2, y + cornerSize * 2, color);
        context.fill(x + width - cornerSize * 2, y + cornerSize, x + width - cornerSize, y + cornerSize * 2, color);
        context.fill(x + cornerSize, y + height - cornerSize * 2, x + cornerSize * 2, y + height - cornerSize, color);
        context.fill(x + width - cornerSize * 2, y + height - cornerSize * 2, x + width - cornerSize, y + height - cornerSize, color);
        
        // Border
        context.fill(x, y, x + width, y + 1, COLOR_BORDER);
        context.fill(x, y + height - 1, x + width, y + height, COLOR_BORDER);
        context.fill(x, y, x + 1, y + height, COLOR_BORDER);
        context.fill(x + width - 1, y, x + width, y + height, COLOR_BORDER);
    }
    
    private void drawCategoryButtons(DrawContext context, int mouseX, int mouseY, int startX, int startY) {
        categoryStartX = startX;
        categoryStartY = startY;
        
        Category[] categories = Category.values();
        int x = startX;
        int buttonHeight = 24;
        int cornerSize = 6;
        
        for (Category category : categories) {
            boolean isSelected = category == selectedCategory;
            boolean isHovered = mouseX >= x && mouseX <= x + categoryButtonWidth &&
                               mouseY >= startY && mouseY <= startY + buttonHeight;
            
            int bgColor = isSelected ? COLOR_ENABLED : (isHovered ? COLOR_HOVER : COLOR_DISABLED);
            
            // Draw rounded button background
            drawRoundedPanel(context, x, startY, categoryButtonWidth, buttonHeight, bgColor);
            
            // Add shadow effect for selected/hovered
            if (isSelected || isHovered) {
                context.fill(x + 2, startY + buttonHeight, x + categoryButtonWidth - 2, startY + buttonHeight + 2, 0x44000000);
            }
            
            // Category name with icons
            String categoryText = getCategoryIcon(category) + " " + category.name();
            Formatting textFormat = isSelected ? Formatting.BLACK : Formatting.WHITE;
            int textColor = isSelected ? 0x000000 : COLOR_TEXT_PRIMARY;
            
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(categoryText).formatted(textFormat),
                x + categoryButtonWidth / 2,
                startY + 8,
                textColor
            );
            
            x += categoryButtonWidth + 8;
        }
    }
    
    private String getCategoryIcon(Category category) {
        return switch (category) {
            case COMBAT -> "⚔️";
            case MOVEMENT -> "👟";
            case RENDER -> "👁️";
            case PLAYER -> "👤";
            case WORLD -> "🌍";
            case MISC -> "⚙️";
        };
    }
    
    private void drawModules(DrawContext context, int mouseX, int mouseY, int startX, int startY, int areaWidth, int areaHeight) {
        moduleStartX = startX;
        moduleStartY = startY;
        moduleAreaWidth = areaWidth;
        moduleAreaHeight = areaHeight;
        
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        
        // Draw "No modules" message if empty
        if (modules.isEmpty()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("No modules in this category").formatted(Formatting.GRAY),
                startX + areaWidth / 2,
                startY + 20,
                0x888888
            );
            return;
        }
        
        int y = startY - scrollOffset;
        int moduleSpacing = 4;
        
        for (com.rafalohaki.module.Module module : modules) {
            // Skip if above visible area
            if (y + moduleHeight < startY) {
                y += moduleHeight + moduleSpacing;
                continue;
            }
            // Stop if below visible area
            if (y > startY + areaHeight) break;
            
            boolean isHovered = mouseX >= startX && mouseX <= startX + moduleButtonWidth &&
                               mouseY >= y && mouseY <= y + moduleHeight &&
                               y >= startY && y + moduleHeight <= startY + areaHeight;
            
            int bgColor = module.isEnabled() ? COLOR_ENABLED : (isHovered ? COLOR_HOVER : COLOR_DISABLED);
            
            // Module background with slight transparency
            context.fill(startX, y, startX + moduleButtonWidth, y + moduleHeight, bgColor);
            
            // Module name
            int textColor = module.isEnabled() ? 0x000000 : 0xFFFFFF;
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(module.getName()),
                startX + 8,
                y + 6,
                textColor
            );
            
            // Module description (smaller, below name)
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(module.getDescription()).formatted(Formatting.GRAY),
                startX + 8,
                y + 16,
                0x888888
            );
            
            // Status indicator
            String statusText = module.isEnabled() ? "●" : "○";
            int statusColor = module.isEnabled() ? 0x00FF00 : 0xFF5555;
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(statusText),
                startX + moduleButtonWidth - 15,
                y + 10,
                statusColor
            );
            
            // Draw settings panel for enabled modules with settings
            if (module.isEnabled()) {
                drawModuleSettings(context, startX + moduleButtonWidth + 10, y, module);
            }
            
            y += moduleHeight + moduleSpacing;
        }
    }
    
    private void drawModuleSettings(DrawContext context, int x, int y, com.rafalohaki.module.Module module) {
        int settingsWidth = 160;
        
        if (module instanceof KillauraModule ka) {
            context.fill(x, y, x + settingsWidth, y + moduleHeight + 10, 0xDD2d2d44);
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal("Range: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", ka.getRange())).formatted(Formatting.YELLOW)),
                x + 5, y + 4, 0xFFFFFF);
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal("CPS: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", ka.getCps())).formatted(Formatting.YELLOW)),
                x + 5, y + 14, 0xFFFFFF);
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal("Players Only: ").formatted(Formatting.GRAY)
                    .append(Text.literal(ka.isPlayersOnly() ? "Yes" : "No").formatted(ka.isPlayersOnly() ? Formatting.GREEN : Formatting.RED)),
                x + 5, y + 24, 0xFFFFFF);
                
        } else if (module instanceof FlyModule fly) {
            context.fill(x, y, x + settingsWidth, y + 20, 0xDD2d2d44);
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal("Speed: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", fly.getSpeed())).formatted(Formatting.YELLOW)),
                x + 5, y + 6, 0xFFFFFF);
        }
    }
    
    /**
     * Close GUI with animation
     */
    @Override
    public void close() {
        LOGGER.info("ClickGui closed");
        super.close();
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    /**
     * Handle mouse clicks - called from render based on mouse state
     */
    private void handleClick(int mouseX, int mouseY) {
        // Check category button clicks
        Category[] categories = Category.values();
        int x = categoryStartX;
        int buttonHeight = 24; // Updated to match drawCategoryButtons
        
        for (Category category : categories) {
            if (mouseX >= x && mouseX <= x + categoryButtonWidth &&
                mouseY >= categoryStartY && mouseY <= categoryStartY + buttonHeight) {
                selectedCategory = category;
                scrollOffset = 0;
                LOGGER.debug("Selected category: {}", category);
                return;
            }
            x += categoryButtonWidth + 8; // Updated to match drawCategoryButtons
        }
        
        // Check module button clicks
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int moduleY = moduleStartY - scrollOffset;
        int moduleSpacing = 4;
        
        for (com.rafalohaki.module.Module module : modules) {
            if (moduleY + moduleHeight < moduleStartY) {
                moduleY += moduleHeight + moduleSpacing;
                continue;
            }
            if (moduleY > moduleStartY + moduleAreaHeight) break;
            
            if (mouseX >= moduleStartX && mouseX <= moduleStartX + moduleButtonWidth &&
                mouseY >= moduleY && mouseY <= moduleY + moduleHeight &&
                moduleY >= moduleStartY) {
                module.toggle();
                LOGGER.info("Toggled module: {} -> {}", module.getName(), module.isEnabled());
                return;
            }
            moduleY += moduleHeight + moduleSpacing;
        }
    }
    
    /**
     * Handle scroll - called from render
     */
    private void handleScroll(double scrollDelta) {
        scrollOffset -= (int)(scrollDelta * 20);
        
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int totalHeight = modules.size() * (moduleHeight + 4);
        int maxScroll = Math.max(0, totalHeight - moduleAreaHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }
}