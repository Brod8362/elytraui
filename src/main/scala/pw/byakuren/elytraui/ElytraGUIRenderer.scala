package pw.byakuren.elytraui

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.client.gui.{AbstractGui, FontRenderer}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.{LogManager, Logger}

import java.awt.Color

class ElytraGUIRenderer {

  private val renderColor: Int = Color.ORANGE.getRGB
  private val arrowColor: Int = Color.RED.getRGB
  private val goodColor: Int = Color.GREEN.getRGB
  private val LOGGER: Logger = LogManager.getLogger

  private val mc: Minecraft = Minecraft.getInstance()

  private val pitchMax = 10
  private val pitchMin = -10

  private var flightTime = 0

  @SubscribeEvent
  def renderHUD(event: RenderGameOverlayEvent.Post): Unit = {
    if (!mc.player.isElytraFlying) {
      flightTime=0
      return
    }
    flightTime+=1
    if (flightTime < 40) return //only show after 2 seconds

    val fR: FontRenderer = mc.fontRenderer
    implicit val mStack: MatrixStack = event.getMatrixStack
    val mainWindow = event.getWindow

    val effectiveGuiScale = math.max(1, mc.gameSettings.guiScale) //todo get actual effective gui scale
    val width = mainWindow.getWidth / effectiveGuiScale
    val height = mainWindow.getHeight / effectiveGuiScale

    val textInitialOffset = (height * 0.20).toInt
    val textLineOffset = 0.01 * effectiveGuiScale

    val pitch = mc.player.getPitch(event.getPartialTicks)
    val velocity = calculatePlayerVelocity(mc.player)
    //todo add fade in
    AbstractGui.drawCenteredString(mStack, fR, f"Pitch: $pitch%.1f", width / 2,
      (height / 2) + (height * textLineOffset).toInt + textInitialOffset, renderColor)
    AbstractGui.drawCenteredString(mStack, fR, f"$velocity%.2fm/s", width / 2,
      (height / 2) + (height * textLineOffset * 2).toInt + textInitialOffset, renderColor)

    val lineThickness = 3
    val lineXOffset = 0.08

    val leftS = (width / 2) - (width * lineXOffset).toInt
    val rightS = (width / 2) + (width * lineXOffset).toInt
    val barB = (height / 2) - (width * 0.10).toInt
    val barT = (height / 2) + (width * 0.10).toInt
    //Render left bar
    AbstractGui.fill(mStack, leftS, barB, leftS + lineThickness, barT, renderColor)
    //Render right bar
    AbstractGui.fill(mStack, rightS, barB, rightS + lineThickness, barT, renderColor)
    //Draw alt text under left bar
    AbstractGui.drawCenteredString(mStack, fR, "ALT", leftS, (barT + textLineOffset).toInt+1, renderColor)

    val velTextOffset = 7
    val hashOffset = 3
    //Draw bottom hash on right bar
    drawBarHash(rightS+hashOffset, barB, renderColor)
    AbstractGui.drawString(mStack, fR, "0", rightS+velTextOffset, barT-4, renderColor)
    //Draw top hash on right bar
    drawBarHash(rightS+hashOffset, barT, renderColor)
    AbstractGui.drawString(mStack, fR, "60", rightS+velTextOffset, barB, renderColor)
    //Draw center hash on right bar
    val hash30pos = barB+(barT-barB)/2.toInt
    AbstractGui.drawString(mStack, fR, "30", rightS+velTextOffset, hash30pos-4, renderColor)
    drawBarHash(rightS+hashOffset, hash30pos, renderColor)
    //Draw vel text under right bar
    AbstractGui.drawCenteredString(mStack, fR, "VEL", rightS, (barT + textLineOffset).toInt+1, renderColor)


    val reticleOffset = width*0.015
    var reticlePos = barB + (calculateReticlePos(pitch) * (barT - barB)).toInt
    reticlePos = math.max(barB, reticlePos)
    reticlePos = math.min(reticlePos, barT)

    val reticleColor = pitch match {
      case x if x < 0.5 && x > -0.5 => goodColor
      case _ => renderColor
    }
    //draw reticle
    drawRightFacingArrow((width/2)-reticleOffset.toInt, reticlePos, reticleColor, 3)
    drawLeftFacingArrow((width/2)+reticleOffset.toInt-1, reticlePos, reticleColor, 3)

    //Draw height arrow for alt
    val highest = highestBlockAtLocation(mc.player.world, mc.player.getPosX.toInt, mc.player.getPosZ.toInt)
    val groundPos = barB + (calculateAltitudePos(highest) * (barT - barB)).toInt
    drawRightFacingArrow(leftS, groundPos, renderColor,3)

    val altPos = barB + (calculateAltitudePos(mc.player.getPosY) * (barT - barB)).toInt
    if (mc.player.getPosY>255) {
      //draw small arrow at top indicating out of world
      drawRightFacingArrow(leftS+1, barB, arrowColor, 3)
    } else {
      drawRightFacingArrow(leftS+1, altPos, arrowColor)
    }

    val velPos = barB + (calculateVelocityPos(velocity) * (barT-barB)).toInt
    if (velocity > 60) {
      drawLeftFacingArrow(rightS+2, barB, arrowColor, 3)
    } else {
      drawLeftFacingArrow(rightS+2, velPos, arrowColor)
    }
  }

  /*
  Misc helper functions begin here
   */

  private def calculatePlayerVelocity(player: ClientPlayerEntity): Double = {
    math.sqrt(
      math.pow(player.getPosX - player.prevPosX, 2) +
        math.pow(player.getPosY - player.prevPosY, 2) +
        math.pow(player.getPosZ - player.prevPosZ, 2)
    ) * 20
  }

  private def calculateReticlePos(pitch: Double): Double = {
    normalize(pitchMin, pitchMax, pitch)
  }

  private def normalize(min: Double, max: Double, value: Double): Double = {
    1 - ((value - min) / (max - min))
  }

  private def calculateGroundMarkerPos(pitch: Double): Double = {
    normalize(-90, 90, pitch)
  }

  private def calculateAltitudePos(y: Double): Double = {
    normalize(0, 256, y)
  }

  private def calculateVelocityPos(vel: Double): Double = {
    normalize(0, 60, vel)
  }

  private def drawRightFacingArrow(x: Int, y: Int, color: Int, size: Int = 5)(implicit mStack: MatrixStack): Unit = {
    //conveniently controls both x and y
    for (offset <- 0 to size) {
      AbstractGui.fill(mStack, x - offset, y - offset, x - offset - 1, y + offset, color)
    }
  }

  private def drawLeftFacingArrow(x: Int, y: Int, color: Int, size: Int = 5)(implicit mStack: MatrixStack): Unit = {
    for (offset <- 0 to size) {
      AbstractGui.fill(mStack, x + offset, y - offset, x + offset + 1, y + offset, color)
    }
  }

  private def highestBlockAtLocation(world: World, x: Int, z: Int): Int = {
    val chunk = world.getChunkAt(new BlockPos(x, 10, z))
    chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z)
  }

  private def drawBarHash(x: Int, y: Int, color: Int)(implicit mStack: MatrixStack): Unit = {
    AbstractGui.fill(mStack, x-2, y-1, x+2, y+1, color)
  }
}
