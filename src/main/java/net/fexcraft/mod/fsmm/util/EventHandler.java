package net.fexcraft.mod.fsmm.util;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.MoneyCapability;
import net.fexcraft.mod.fsmm.api.MoneyItem;
import net.fexcraft.mod.fsmm.impl.cap.MoneyCapabilityUtil;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventHandler {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(UpdateHandler.Status != null){
        	event.player.sendMessage(new TextComponentString(Formatter.format(UpdateHandler.Status)));
    	}
		Print.debug("Loading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = AccountManager.INSTANCE.getAccount("player", event.player.getGameProfile().getId().toString(), true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = AccountManager.INSTANCE.getAccount("player", event.player.getGameProfile().getId().toString());
		AccountManager.INSTANCE.unloadAccount(account);
    }
    
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Unload event){
    	AccountManager.INSTANCE.saveAll();
    }
    
    @Mod.EventHandler
    public static void onShutdown(FMLServerStoppingEvent event){
    	AccountManager.INSTANCE.saveAll();
    }
    
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP){ return; }
    	if(event.getItemStack().hasCapability(MoneyCapabilityUtil.CAPABILITY, null)){
    		event.getToolTip().add(Formatter.format("&9[&8FSMM&9]&3 Worth: &7" + Config.getWorthAsString(event.getItemStack().getCapability(MoneyCapabilityUtil.CAPABILITY, null).getWorth())));
    		if(event.getItemStack().getCount() > 1){
    			event.getToolTip().add(Formatter.format("&9[&8FSMM&9]&3 Stack: &7" + Config.getWorthAsString(event.getItemStack().getCapability(MoneyCapabilityUtil.CAPABILITY, null).getWorth() * event.getItemStack().getCount())));
    		}
    	}
    }
    
    @SubscribeEvent
    public void onAttackCapabilities(AttachCapabilitiesEvent<ItemStack> event){
    	if(event.getObject().getItem() instanceof MoneyItem || Config.containsAsExternalItemStack(event.getObject())){
    		event.addCapability(MoneyCapability.REGISTRY_NAME, new MoneyCapabilityUtil(event.getObject()));
    	}
    }
    
}