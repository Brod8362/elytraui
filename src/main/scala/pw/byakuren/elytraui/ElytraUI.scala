package pw.byakuren.elytraui

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.{LogManager, Logger}

@Mod("elytraui") class ElytraUI() { // Register the setup method for modloading

  // Register ourselves for server and other game events we are interested in
  MinecraftForge.EVENT_BUS.register(new ElytraGUIRenderer)

  val LOGGER: Logger = LogManager.getLogger

}