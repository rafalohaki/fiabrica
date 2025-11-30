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
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
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
    
    // Modern color palette with better contrast and visual hierarchy
    private static final int COLOR_BACKGROUND = 0xDD000000;      // Pure black background
    private static final int COLOR_PANEL = 0xEE1A1A1E;           // Dark panel
    private static final int COLOR_ACCENT = 0xFF3B82F6;          // Modern blue accent
    private static final int COLOR_ENABLED = 0xFF22C55E;         // Green for enabled
    private static final int COLOR_DISABLED = 0xFF475569;        // Dark gray for disabled
    private static final int COLOR_HOVER = 0xFF64748B;           // Medium gray for hover
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;     // Pure white text
    private static final int COLOR_TEXT_SECONDARY = 0xFF94A3B8;  // Muted secondary text
    private static final int COLOR_BORDER = 0xFF334155;         // Subtle border
    private static final int COLOR_CATEGORY_BG = 0xFF1E293B;     // Category button background
    private static final int COLOR_SETTINGS_BG = 0xDD0F172A;     // Settings panel background
    
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
        
        // Register scroll event using Fabric Screen Events API
        ScreenMouseEvents.allowMouseScroll(this).register((screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            // Handle scroll - negative = scroll down, positive = scroll up
            scrollOffset -= (int)(verticalAmount * 20);
            clampScrollOffset();
            return true; // Allow default handling too
        });
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
        panelX = 20;
        panelY = 30;
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
                animationProgress = Math.min(1.0f, animationProgress + delta * 4.0f);
            }
            lastAnimationTime = currentTime;
        }
    }
    
    private void drawRoundedPanel(DrawContext context, int x, int y, int width, int height, int color) {
        // Simple rounded corners using multiple rectangles
        int cornerSize = 8;
        
        // Main body (center area)
        context.fill(x + cornerSize, y + cornerSize, x + width - cornerSize, y + height - cornerSize, color);
        
        // Side strips
        context.fill(x + cornerSize, y, x + width - cornerSize, y + cornerSize, color);
        context.fill(x + cornerSize, y + height - cornerSize, x + width - cornerSize, y + height, color);
        context.fill(x, y + cornerSize, x + cornerSize, y + height - cornerSize, color);
        context.fill(x + width - cornerSize, y + cornerSize, x + width, y + height - cornerSize, color);
        
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
        int buttonHeight = 26;
        
        for (Category category : categories) {
            boolean isSelected = category == selectedCategory;
            boolean isHovered = mouseX >= x && mouseX <= x + categoryButtonWidth &&
                               mouseY >= startY && mouseY <= startY + buttonHeight;
            
            int bgColor = isSelected ? COLOR_ENABLED : (isHovered ? COLOR_HOVER : COLOR_CATEGORY_BG);
            
            // Draw modern category button with gradient effect
            drawRoundedPanel(context, x, startY, categoryButtonWidth, buttonHeight, bgColor);
            
            // Add accent bar for selected category
            if (isSelected) {
                context.fill(x, startY + buttonHeight - 2, x + categoryButtonWidth, startY + buttonHeight, COLOR_ACCENT);
            }
            
            // Add subtle shadow for hover
            if (isHovered && !isSelected) {
                context.fill(x + 1, startY + buttonHeight, x + categoryButtonWidth - 1, startY + buttonHeight + 1, 0x22000000);
            }
            
            // Modern category icons with emojis
            String categoryIcon = getCategoryModernIcon(category);
            String categoryText = categoryIcon + " " + category.name();
            Formatting textFormat = isSelected ? Formatting.BOLD : Formatting.RESET;
            int textColor = isSelected ? 0x000000 : COLOR_TEXT_PRIMARY;
            
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(categoryText).formatted(textFormat),
                x + categoryButtonWidth / 2,
                startY + 9,
                textColor
            );
            
            x += categoryButtonWidth + 10;
        }
    }
    
    private String getCategoryModernIcon(Category category) {
        return switch (category) {
            case COMBAT -> "⚔️";
            case MOVEMENT -> "🏃";
            case RENDER -> "🎨";
            case PLAYER -> "👤";
            case WORLD -> "🌍";
            case MISC -> "⚙️";
        };
    }
    
    private void drawRoundedModule(DrawContext context, int x, int y, int width, int height, int color, boolean isEnabled) {
        // Modern rounded module with subtle effects
        int cornerSize = 6;
        
        // Main body
        context.fill(x + cornerSize, y + cornerSize, x + width - cornerSize, y + height - cornerSize, color);
        
        // Rounded corners
        context.fill(x + cornerSize, y, x + width - cornerSize, y + cornerSize, color);
        context.fill(x + cornerSize, y + height - cornerSize, x + width - cornerSize, y + height, color);
        context.fill(x, y + cornerSize, x + cornerSize, y + height - cornerSize, color);
        context.fill(x + width - cornerSize, y + cornerSize, x + width, y + height - cornerSize, color);
        
        // Corner pieces for smooth rounding
        context.fill(x + cornerSize, y + cornerSize, x + cornerSize * 2, y + cornerSize * 2, color);
        context.fill(x + width - cornerSize * 2, y + cornerSize, x + width - cornerSize, y + cornerSize * 2, color);
        context.fill(x + cornerSize, y + height - cornerSize * 2, x + cornerSize * 2, y + height - cornerSize, color);
        context.fill(x + width - cornerSize * 2, y + height - cornerSize * 2, x + width - cornerSize, y + height - cornerSize, color);
        
        // Add subtle border for enabled modules
        if (isEnabled) {
            context.fill(x, y, x + width, y + 1, COLOR_ACCENT);
            context.fill(x, y + height - 1, x + width, y + height, COLOR_ACCENT);
            context.fill(x, y, x + 1, y + height, COLOR_ACCENT);
            context.fill(x + width - 1, y, x + width, y + height, COLOR_ACCENT);
        }
    }
    
    private void drawModules(DrawContext context, int mouseX, int mouseY, int startX, int startY, int areaWidth, int areaHeight) {
        moduleStartX = startX;
        moduleStartY = startY;
        moduleAreaWidth = areaWidth;
        moduleAreaHeight = areaHeight;
        
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        
        // Draw "No modules" message if empty with modern styling
        if (modules.isEmpty()) {
            // Draw centered icon
            String icon = "⚠";
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(icon).formatted(Formatting.YELLOW),
                startX + areaWidth / 2,
                startY + 15,
                COLOR_ACCENT
            );
            
            // Draw message below icon
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("No modules in this category").formatted(Formatting.GRAY),
                startX + areaWidth / 2,
                startY + 35,
                COLOR_TEXT_SECONDARY
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
            
            // Module background with rounded corners effect
            drawRoundedModule(context, startX, y, moduleButtonWidth, moduleHeight, bgColor, module.isEnabled());
            
            // Module name with better formatting
            int textColor = module.isEnabled() ? 0x000000 : COLOR_TEXT_PRIMARY;
            Formatting nameFormat = module.isEnabled() ? Formatting.BOLD : Formatting.RESET;
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(module.getName()).formatted(nameFormat),
                startX + 10,
                y + 6,
                textColor
            );
            
            // Module description with truncation for long text
            String description = module.getDescription();
            if (this.textRenderer.getWidth(description) > moduleButtonWidth - 40) {
                description = this.textRenderer.trimToWidth(description, moduleButtonWidth - 45) + "...";
            }
            context.drawText(
                this.textRenderer,
                Text.literal(description).formatted(Formatting.GRAY),
                startX + 10,
                y + 16,
                COLOR_TEXT_SECONDARY,
                false
            );
            
            // Modern status indicator with animation effect
            String statusText = module.isEnabled() ? "●" : "○";
            int statusColor = module.isEnabled() ? COLOR_ENABLED : COLOR_DISABLED;
            // Add subtle glow effect for enabled modules
            if (module.isEnabled()) {
                context.fill(startX + moduleButtonWidth - 18, y + 8, startX + moduleButtonWidth - 12, y + 14, 0x4400FF00);
            }
            context.drawText(
                this.textRenderer,
                Text.literal(statusText),
                startX + moduleButtonWidth - 15,
                y + 10,
                statusColor,
                false
            );
            
            // Draw settings panel for enabled modules with settings
            if (module.isEnabled()) {
                drawModuleSettings(context, startX + moduleButtonWidth + 10, y, module);
            }
            
            y += moduleHeight + moduleSpacing;
        }
    }
    
    private void drawModuleSettings(DrawContext context, int x, int y, com.rafalohaki.module.Module module) {
        int settingsWidth = 180;
        int settingsHeight = 40;
        
        // Draw settings panel with rounded corners
        drawRoundedPanel(context, x, y, settingsWidth, settingsHeight, COLOR_SETTINGS_BG);
        
        // Add border highlight
        context.fill(x, y, x + settingsWidth, y + 1, COLOR_ACCENT);
        
        if (module instanceof KillauraModule ka) {
            // Draw settings icon
            context.drawText(
                this.textRenderer,
                Text.literal("⚙").formatted(Formatting.GRAY),
                x + 5, y + 3,
                COLOR_ACCENT,
                false
            );
            
            // Range setting with icon
            context.drawText(
                this.textRenderer,
                Text.literal("📏 ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", ka.getRange())).formatted(Formatting.YELLOW)),
                x + 20, y + 4,
                COLOR_TEXT_PRIMARY,
                false
            );
            
            // CPS setting with icon
            context.drawText(
                this.textRenderer,
                Text.literal("⚡ ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", ka.getCps())).formatted(Formatting.YELLOW)),
                x + 20, y + 14,
                COLOR_TEXT_PRIMARY,
                false
            );
            
            // Players Only setting with icon and color coding
            Formatting playersFormat = ka.isPlayersOnly() ? Formatting.GREEN : Formatting.RED;
            context.drawText(
                this.textRenderer,
                Text.literal("👥 ").formatted(Formatting.GRAY)
                    .append(Text.literal(ka.isPlayersOnly() ? "Yes" : "No").formatted(playersFormat)),
                x + 20, y + 24,
                COLOR_TEXT_PRIMARY,
                false
            );
                
        } else if (module instanceof FlyModule fly) {
            // Draw settings icon
            context.drawText(
                this.textRenderer,
                Text.literal("⚙").formatted(Formatting.GRAY),
                x + 5, y + 3,
                COLOR_ACCENT,
                false
            );
            
            // Speed setting with icon
            context.drawText(
                this.textRenderer,
                Text.literal("🚀 ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", fly.getSpeed())).formatted(Formatting.YELLOW)),
                x + 20, y + 10,
                COLOR_TEXT_PRIMARY,
                false
            );
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
     * Handle scroll input from GLFW
     */
    private void checkScrollInput() {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Scroll handling via tick - simplified approach
        // For full scroll support, consider using Fabric Screen Events API
    }
    
    /**
     * Clamp scroll offset to valid range
     */
    private void clampScrollOffset() {
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int totalHeight = modules.size() * (moduleHeight + 4);
        int maxScroll = Math.max(0, totalHeight - moduleAreaHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }
}