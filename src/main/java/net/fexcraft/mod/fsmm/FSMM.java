package net.fexcraft.mod.fsmm;
 
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.RegistryDelegate;
import cpw.mods.fml.server.FMLServerHandler;
import net.fexcraft.mod.fsmm.impl.GenericMoney;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.api.PlayerCapability;
import net.fexcraft.mod.fsmm.api.WorldCapability;
import net.fexcraft.mod.fsmm.gui.GuiHandler;
import net.fexcraft.mod.fsmm.gui.Processor;
import net.fexcraft.mod.fsmm.impl.cap.MoneyCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.PlayerCapabilityUtil;
import net.fexcraft.mod.fsmm.impl.cap.WorldCapabilityUtil;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.EventHandler;
import net.fexcraft.mod.fsmm.util.Command;
import net.fexcraft.mod.fsmm.util.UpdateHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = FSMM.MODID, name = "Fex's Small Money Mod", version = FSMM.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*",
		 guiFactory = "net.fexcraft.mod.fsmm.util.GuiFactory")
public class FSMM {

	public static RegistryDelegate<Money> CURRENCY;
	public static final String MODID = "fsmm";
	public static final String VERSION = "@VERSION@";

    @Mod.Instance(MODID)
    private static FSMM INSTANCE;
    public static DataManager CACHE;
    //public static final Logger LOGGER = Log.getCustomLogger("fsmm", "transfers", "FSMM", null);

	//todo:player and world capability can get removed, as the same data can be taken directly from DataManager

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		CURRENCY = new RegistryDelegate.Delegate<Money>(new GenericMoney(new ResourceLocation("fsmm:money")), Money.class);
		//
		FCLRegistry.newAutoRegistry("fsmm");
		Config.initialize(event);
		CACHE = new DataManager(event.getModConfigurationDirectory());
	}
	
	public static CreativeTabs tabFSMM = new CreativeTabs("tabFSMM") {
	    @Override
	    public Item getTabIconItem() {
	    	return new ItemStack(FCLRegistry.getBlock("fsmm:atm").getItem());
	    }
	};
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartedEvent event){
		((ServerCommandManager)MinecraftServer.getServer().getCommandManager()).registerCommand(new Command());
	}
	
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		DataManager.saveAll();
	}
	
	@Mod.EventHandler
    public void init(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		UpdateHandler.initialize();
		MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.JSON, Side.CLIENT, new net.fexcraft.mod.fsmm.gui.AutomatedTellerMashineGui.Receiver());
    	}
    	PacketHandler.registerListener(PacketHandlerType.JSON, Side.SERVER, new Processor());
    	CACHE.schedule();
    }
    
    public static FSMM getInstance(){
    	return INSTANCE;
    }
	
	@SuppressWarnings("deprecation")
	public static List<Money> getSortedMoneyList(){
		return CURRENCY.getValues().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() < o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}
	
}
