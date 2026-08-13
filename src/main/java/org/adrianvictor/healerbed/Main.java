package org.adrianvictor.healerbed;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "healerbed";
    public static final String VERSION = "1.0";
    private final Set<UUID> sleepingPlayers = new HashSet<UUID>();
    private MinecraftServer server;
    private static Main INSTANCE;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	Config.load(event.getSuggestedConfigurationFile());
    	INSTANCE = this;
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	server = event.getServer();
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
    	EntityPlayer player = event.getEntityPlayer();
    	
    	if (player.worldObj.isRemote || Config.requireSleepUntilMorning) return;
    	
    	player.heal(Config.healAmount);
		player.getFoodStats().addStats(Config.feedAmount, Config.saturationAmount);
    }
    
    void onPlayerWoken(EntityPlayer player) {
    	if (!Config.requireSleepUntilMorning) return;
    	
    	player.heal(Config.healAmount);
		player.getFoodStats().addStats(Config.feedAmount, Config.saturationAmount);
    }
}