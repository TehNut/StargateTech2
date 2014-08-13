package lordfokas.stargatetech2.transport.util;

import lordfokas.stargatetech2.transport.packet.PacketActivateRings;
import lordfokas.stargatetech2.transport.tileentity.TileTransportRing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public class RingKeyHandler{
	private static final RingKeyHandler INSTANCE = new RingKeyHandler();
	private static final KeyBinding RING_UP = new KeyBinding("[SGTech2] Activate rings (Up)", 200, "SGTech2");
	private static final KeyBinding RING_DOWN = new KeyBinding("[SGTech2] Activate rings (Down)", 208, "SGTech2");
	
	private RingKeyHandler(){}
	
	public static void register(){
		ClientRegistry.registerKeyBinding(RING_DOWN);
		ClientRegistry.registerKeyBinding(RING_UP);
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}
	
	@SubscribeEvent
	public void keyUp(KeyInputEvent evt){
		if (!FMLClientHandler.instance().isGUIOpen(GuiScreen.class)){
			if(RING_UP.isPressed()){
				makePlayerTriggerRings(true);
			}else if(RING_DOWN.isPressed()){
				makePlayerTriggerRings(false);
			}
		}
	}
	
	private void makePlayerTriggerRings(boolean up){
		TileTransportRing rings = TileTransportRing.getRingsInRange(FMLClientHandler.instance().getClient().theWorld);
		if(rings != null && Minecraft.getMinecraft().currentScreen == null){
			PacketActivateRings packet = new PacketActivateRings();
			packet.x = rings.xCoord;
			packet.y = rings.yCoord;
			packet.z = rings.zCoord;
			packet.up = up;
			packet.sendToServer();
		}
	}
}