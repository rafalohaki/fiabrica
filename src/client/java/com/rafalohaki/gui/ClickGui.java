package com.rafalohaki.gui;

import com.rafalohaki.module.*;
import com.rafalohaki.module.modules.KillauraModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * Simple ClickGUI for module management
 * Right Shift key opens this screen
 */
public class ClickGui extends Screen {
    private Category selectedCategory = Category.COMBAT;
    private int scrollOffset = 0;
    private final int moduleHeight = 25;
    private final int categoryButtonWidth = 80;
    private final int moduleButtonWidth = 120;
    
    public ClickGui() {
        super(Text.literal("Fiabrica ClickGUI"));
    }
    
    @Override
    protected void init() {
        // Initialize screen
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw background
        context.fillGradient(0, 0, this.width, this.height, 0x80000000, 0x80000000);
        
        // Draw title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Fiabrica ClickGUI").formatted(Formatting.AQUA),
            this.width / 2,
            10,
            0xFFFFFF
        );
        
        // Draw category buttons
        drawCategoryButtons(context, mouseX, mouseY);
        
        // Draw modules for selected category
        drawModules(context, mouseX, mouseY);
        
        // Draw instructions
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Click to toggle • ESC to close").formatted(Formatting.GRAY),
            10,
            this.height - 20,
            0xAAAAAA
        );
    }
    
    private void drawCategoryButtons(DrawContext context, int mouseX, int mouseY) {
        Category[] categories = Category.values();
        int x = 10;
        int y = 40;
        
        for (Category category : categories) {
            boolean isSelected = category == selectedCategory;
            int color = isSelected ? 0xFF5555 : 0x555555;
            Formatting textColor = isSelected ? Formatting.WHITE : Formatting.GRAY;
            
            // Draw button background
            context.fill(x, y, x + categoryButtonWidth, y + 20, color);
            
            // Draw category name
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(category.name()).formatted(textColor),
                x + categoryButtonWidth / 2,
                y + 6,
                0xFFFFFF
            );
            
            x += categoryButtonWidth + 5;
        }
    }
    
    private void drawModules(DrawContext context, int mouseX, int mouseY) {
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int startX = 10;
        int startY = 70;
        int maxWidth = this.width - 20;
        
        int y = startY - scrollOffset;
        
        for (com.rafalohaki.module.Module module : modules) {
            if (y < startY - moduleHeight) {
                y += moduleHeight + 5;
                continue;
            }
            if (y > this.height) break;
            
            int moduleColor = module.isEnabled() ? 0xFF5555 : 0x555555;
            Formatting textColor = module.isEnabled() ? Formatting.GREEN : Formatting.RED;
            
            // Module background
            context.fill(startX, y, startX + moduleButtonWidth, y + moduleHeight, moduleColor);
            
            // Module name
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(module.getName()).formatted(textColor),
                startX + 5,
                y + 8,
                0xFFFFFF
            );
            
            // Module status
            String statusText = module.isEnabled() ? "ON" : "OFF";
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(statusText).formatted(Formatting.WHITE),
                startX + moduleButtonWidth - 25,
                y + 8,
                0xFFFFFF
            );
            
            // Draw settings for enabled modules
            if (module.isEnabled() && module instanceof KillauraModule) {
                drawKillauraSettings(context, startX + moduleButtonWidth + 10, y, (KillauraModule) module);
            }
            
            y += moduleHeight + 5;
        }
    }
    
    private void drawKillauraSettings(DrawContext context, int x, int y, KillauraModule module) {
        int settingsWidth = 150;
        
        // Settings background
        context.fill(x, y, x + settingsWidth, y + moduleHeight, 0x333333);
        
        // Range setting
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Range: " + module.getRange()).formatted(Formatting.YELLOW),
            x + 5,
            y + 3,
            0xFFFFFF
        );
        
        // CPS setting
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("CPS: " + module.getCps()).formatted(Formatting.YELLOW),
            x + 5,
            y + 13,
            0xFFFFFF
        );
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // Check category button clicks
            Category[] categories = Category.values();
            int x = 10;
            int y = 40;
            
            for (Category category : categories) {
                if (mouseX >= x && mouseX <= x + categoryButtonWidth &&
                    mouseY >= y && mouseY <= y + 20) {
                    selectedCategory = category;
                    return true;
                }
                x += categoryButtonWidth + 5;
            }
            
            // Check module button clicks
            var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
            int startX = 10;
            int startY = 70;
            int moduleY = startY - scrollOffset;
            
            for (com.rafalohaki.module.Module module : modules) {
                if (mouseX >= startX && mouseX <= startX + moduleButtonWidth &&
                    mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                    module.toggle();
                    return true;
                }
                moduleY += moduleHeight + 5;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Handle scrolling
        scrollOffset += verticalAmount * 10;
        
        // Limit scroll offset
        var modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int maxScroll = Math.max(0, modules.size() * (moduleHeight + 5) - (this.height - 70));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        return true;
    }
}