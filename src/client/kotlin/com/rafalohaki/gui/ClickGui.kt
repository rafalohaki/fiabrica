package com.rafalohaki.gui

import com.rafalohaki.module.*
import com.rafalohaki.module.modules.KillauraModule
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW

/**
 * Simple ClickGUI for module management
 * Right Shift key opens this screen
 */
class ClickGui : Screen(Text.literal("Fiabrica ClickGUI")) {
    
    private var selectedCategory = Category.COMBAT
    private var scrollOffset = 0
    private val moduleHeight = 25
    private val categoryButtonWidth = 80
    private val moduleButtonWidth = 120
    
    override fun init() {
        // Initialize screen
    }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        
        // Draw background
        context.fillGradient(0, 0, width, height, 0x80000000.toInt(), 0x80000000.toInt())
        
        // Draw title
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Fiabrica ClickGUI").formatted(Formatting.AQUA),
            width / 2,
            10,
            0xFFFFFF
        )
        
        // Draw category buttons
        drawCategoryButtons(context, mouseX, mouseY)
        
        // Draw modules for selected category
        drawModules(context, mouseX, mouseY)
        
        // Draw instructions
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("Click to toggle • ESC to close").formatted(Formatting.GRAY),
            10,
            height - 20,
            0xAAAAAA
        )
    }
    
    private fun drawCategoryButtons(context: DrawContext, mouseX: Int, mouseY: Int) {
        val categories = Category.values()
        var x = 10
        val y = 40
        
        for (category in categories) {
            val isSelected = category == selectedCategory
            val color = if (isSelected) 0xFF5555 else 0x555555
            val textColor = if (isSelected) Formatting.WHITE else Formatting.GRAY
            
            // Draw button background
            context.fill(x, y, x + categoryButtonWidth, y + 20, color)
            
            // Draw category name
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal(category.name).formatted(textColor),
                x + categoryButtonWidth / 2,
                y + 6,
                0xFFFFFF
            )
            
            x += categoryButtonWidth + 5
        }
    }
    
    private fun drawModules(context: DrawContext, mouseX: Int, mouseY: Int) {
        val modules = ModuleManager.getModulesByCategory(selectedCategory)
        val startX = 10
        val startY = 70
        val maxWidth = width - 20
        
        var y = startY - scrollOffset
        
        for (module in modules) {
            if (y < startY - moduleHeight) {
                y += moduleHeight + 5
                continue
            }
            if (y > height) break
            
            val moduleColor = if (module.enabled) 0xFF5555 else 0x555555
            val textColor = if (module.enabled) Formatting.GREEN else Formatting.RED
            
            // Module background
            context.fill(startX, y, startX + moduleButtonWidth, y + moduleHeight, moduleColor)
            
            // Module name
            context.drawTextWithShadow(
                textRenderer,
                Text.literal(module.name).formatted(textColor),
                startX + 5,
                y + 8,
                0xFFFFFF
            )
            
            // Module status
            val statusText = if (module.enabled) "ON" else "OFF"
            context.drawTextWithShadow(
                textRenderer,
                Text.literal(statusText).formatted(Formatting.WHITE),
                startX + moduleButtonWidth - 25,
                y + 8,
                0xFFFFFF
            )
            
            // Draw settings for enabled modules
            if (module.enabled && module is KillauraModule) {
                drawKillauraSettings(context, startX + moduleButtonWidth + 10, y, module)
            }
            
            y += moduleHeight + 5
        }
    }
    
    private fun drawKillauraSettings(context: DrawContext, x: Int, y: Int, module: KillauraModule) {
        val settingsWidth = 150
        
        // Settings background
        context.fill(x, y, x + settingsWidth, y + moduleHeight, 0x333333)
        
        // Range setting
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("Range: ${module.range}").formatted(Formatting.YELLOW),
            x + 5,
            y + 3,
            0xFFFFFF
        )
        
        // CPS setting
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("CPS: ${module.cps}").formatted(Formatting.YELLOW),
            x + 5,
            y + 13,
            0xFFFFFF
        )
    }
    
    override fun close() {
        super.close()
    }
    
    override fun shouldPause(): Boolean {
        return false
    }
}
