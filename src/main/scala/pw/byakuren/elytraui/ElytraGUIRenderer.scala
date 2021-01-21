package pw.byakuren.elytraui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{AbstractGui, FontRenderer}
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.{LogManager, Logger}

import java.awt.Color

class ElytraGUIRenderer {

  private val renderColor: Int = Color.ORANGE.getRGB
  private val LOGGER: Logger = LogManager.getLogger

  private val mc: Minecraft = Minecraft.getInstance()

  @SubscribeEvent
  def renderHUD(event: RenderGameOverlayEvent.Post): Unit = {
    val fR: FontRenderer = mc.fontRenderer
    val mStack = event.getMatrixStack
    if (mc.currentScreen == null) return
    val width = mc.currentScreen.width
    val height = mc.currentScreen.height

    AbstractGui.fill(mStack, 0, 0, 50, 10, renderColor)
    val pitch = mc.player.getPitch(event.getPartialTicks)
    AbstractGui.drawCenteredString(mStack, fR, f"Pitch: $pitch%.1f", width/2, (height/2)+(height*0.10).toInt, renderColor)
    AbstractGui.drawCenteredString(mStack, fR, "o", width/2, (height/2)-5, renderColor)


    val lineThickness = 3
    val lineXOffset = 0.08

    val leftS = (width/2)-(width*lineXOffset).toInt
    val rightS = (width/2)+(width*lineXOffset).toInt
    val barB = (height/2)-(width*0.10).toInt
    val barT = (height/2)+(width*0.10).toInt
    AbstractGui.fill(mStack, leftS, barB, leftS+lineThickness, barT, renderColor)
    AbstractGui.fill(mStack, rightS, barB, rightS+lineThickness, barT, renderColor)

  }


}
