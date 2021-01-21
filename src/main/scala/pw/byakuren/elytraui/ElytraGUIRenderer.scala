package pw.byakuren.elytraui

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.client.gui.{AbstractGui, FontRenderer}
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.{LogManager, Logger}

import java.awt.Color

class ElytraGUIRenderer {

  private val renderColor: Int = Color.ORANGE.getRGB
  private val LOGGER: Logger = LogManager.getLogger

  private val mc: Minecraft = Minecraft.getInstance()

  private val pitchMax = 10
  private val pitchMin = -10

  @SubscribeEvent
  def renderHUD(event: RenderGameOverlayEvent.Post): Unit = {
    if (!mc.player.isElytraFlying) return
    val fR: FontRenderer = mc.fontRenderer
    val mStack = event.getMatrixStack
    val mainWindow = event.getWindow

    val effectiveGuiScale = math.max(1, mc.gameSettings.guiScale)
    val width = mainWindow.getWidth / effectiveGuiScale
    val height = mainWindow.getHeight / effectiveGuiScale

    val textInitialOffset = (height*0.20).toInt
    val textLineOffset = 0.01*effectiveGuiScale

    val pitch = mc.player.getPitch(event.getPartialTicks)
    //todo add fade in
    AbstractGui.drawCenteredString(mStack, fR, f"Pitch: $pitch%.1f", width / 2,
      (height / 2) + (height * textLineOffset).toInt+textInitialOffset, renderColor)
    AbstractGui.drawCenteredString(mStack, fR, f"${calculatePlayerVelocity(mc.player)}%.2fm/s", width / 2,
      (height / 2) + (height * textLineOffset * 2).toInt+textInitialOffset, renderColor)

    val lineThickness = 3
    val lineXOffset = 0.08

    val leftS = (width / 2) - (width * lineXOffset).toInt
    val rightS = (width / 2) + (width * lineXOffset).toInt
    val barB = (height / 2) - (width * 0.10).toInt
    val barT = (height / 2) + (width * 0.10).toInt
    AbstractGui.fill(mStack, leftS, barB, leftS + lineThickness, barT, renderColor)
    AbstractGui.fill(mStack, rightS, barB, rightS + lineThickness, barT, renderColor)

    val reticleStr = pitch match {
      case x if x > pitchMax => "^"
      case x if x < pitchMin => "v"
      case _ => "o"
    }

    var reticlePos = barB+(calculateReticlePos(pitch)*(barT-barB)).toInt
    reticlePos = math.max(barB, reticlePos)
    reticlePos = math.min(reticlePos, barT)

    AbstractGui.drawCenteredString(mStack, fR, reticleStr, width / 2, reticlePos-5, renderColor)

  }

  private def calculatePlayerVelocity(player: ClientPlayerEntity): Double = {
    math.sqrt(
      math.pow(player.getPosX - player.prevPosX, 2) +
        math.pow(player.getPosY - player.prevPosY, 2) +
        math.pow(player.getPosZ - player.prevPosZ, 2)
    )*20
  }

  /**
   *
   * @param player
   * @return A multiple to multiply the pos by. 0.5 is center, 1 is max, 0 is min
   */
  private def calculateReticlePos(pitch: Double): Double = {
    normalize(pitchMin, pitchMax, pitch)
  }

  private def normalize(min: Double, max: Double, value: Double): Double = {
    1 - ((value-min)/(max-min))
  }

  private def calculateGroundPos(pitch: Double): Double = {
    normalize(-90, 90, pitch)
  }
}
