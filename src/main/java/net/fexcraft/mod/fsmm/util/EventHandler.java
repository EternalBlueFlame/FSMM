package net.fexcraft.mod.fsmm.util;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Money;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.Iterator;

public class EventHandler {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(UpdateHandler.Status != null){
        	event.player.addChatComponentMessage(new ChatComponentText(Formatter.format(UpdateHandler.Status)));
    	}
    	if(event.player.worldObj.isRemote){ return; }
		Print.debug("Loading account of " + event.player.getDisplayName() + " || " + event.player.getGameProfile().getId().toString());
    	Account account = DataManager.getAccount("player:" + event.player.getGameProfile().getId().toString(), false, true);
    	if(Config.NOTIFY_BALANCE_ON_JOIN && account!=null){
    		Print.chat(event.player, "&m&3Balance &r&7(in bank)&0: &a" + Config.getWorthAsString(account.getBalance()));
    		Print.chat(event.player, "&m&3Balance &r&7(in Inv0)&0: &a" + Config.getWorthAsString(ItemManager.countInInventory(event.player)));
    	} else if (Config.NOTIFY_BALANCE_ON_JOIN){
    		Print.chat(event.player, "&m&3Balance could not be loaded");
		}
    	if(account.lastAccessed() >= 0){ account.setTemporary(false); }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		Print.debug("Unloading account of " + event.player.getDisplayName() + " || " + event.player.getGameProfile().getId().toString());
		DataManager.unloadAccount("player", event.player.getGameProfile().getId().toString());
    }
    
    @SideOnly(Side.CLIENT) @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
    	if(!Config.SHOW_ITEM_WORTH_IN_TOOLTIP){ return; }
		if(event.itemStack!=null && FSMM.CURRENCY.containsKey(event.itemStack.getItem().delegate.name().toLowerCase())){
			event.toolTip.add(
					Formatter.format("&9"+Config.getWorthAsString(FSMM.CURRENCY.get(event.itemStack.getItem().delegate.name().toLowerCase()).getWorth()* event.itemStack.stackSize))
			);
		}
    }
    
}