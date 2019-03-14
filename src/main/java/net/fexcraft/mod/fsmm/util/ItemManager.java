package net.fexcraft.mod.fsmm.util;

import java.util.List;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.api.Money;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemManager {
	
	public static long countInInventory(ICommandSender sender){
		return sender instanceof EntityPlayer ? countInInventory((EntityPlayer)sender) : -1;
	}
	
	public static long countInInventory(EntityPlayer player){
		long value = 0L;
		for(ItemStack s : player.inventory.mainInventory){
			if(s!=null && FSMM.CURRENCY.containsKey(s.getItem().delegate.name().toLowerCase())){
				value+=FSMM.CURRENCY.get(s.getItem().delegate.name().toLowerCase()).getWorth()* s.stackSize;
				Print.debug(s.getItem().delegate.name(), FSMM.CURRENCY.get(s.getItem().delegate.name()).getWorth() +"");
			}
		}
		return value;
	}
	
	public static boolean hasSpace(EntityPlayer player, boolean countMoneyItemAsSpace, Money money){
		int i = 0;
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack == null){
				i++;
				break;
			} else if(stack.stackSize<stack.getMaxStackSize() && FSMM.CURRENCY_ITEMS.containsKey(money)){
				i++;
				break;
			}
		}
		return i != 0;
	}
	
	public static long addToInventory(EntityPlayer player, long amount){
		return setInInventory(player, (amount += countInInventory(player)) > Long.MAX_VALUE ? Long.MAX_VALUE : amount);
	}

	public static long removeFromInventory(EntityPlayer player, long amount){
		long old = countInInventory(player);
		old -= amount; if(old < 0){ amount += old; old = 0; }
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			if(player.inventory.mainInventory[i] != null && FSMM.CURRENCY.containsKey(player.inventory.mainInventory[i].getItem().delegate.name().toLowerCase())){
				player.inventory.setInventorySlotContents(i, null);
			}
		}
		setInInventory(player, old);
		return amount;
	}
	
	public static long setInInventory(EntityPlayer player, long amount){
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			if(player.inventory.mainInventory[i] != null && FSMM.CURRENCY.containsKey(player.inventory.mainInventory[i].getItem().delegate.name().toLowerCase())){
				player.inventory.setInventorySlotContents(i, null);
			}
		}
		for(Money m : FSMM.getSortedMoneyList()){
			//Print.debug(m.getWorth()+"", m.getRegistryName().toString());
			while(amount - m.getWorth() >= 0){
				ItemStack stack = Money.getItemStack(m);
				if(hasSpace(player, true, m)){
					Print.debug("adding money?");
					player.inventory.addItemStackToInventory(stack);
				}
				else{
					player.getEntityWorld().spawnEntityInWorld(new EntityItem(player.getEntityWorld(), player.posX, player.posY, player.posZ, stack));
				}
				amount -= m.getWorth();
			}
		}
		if(amount > 0){
			Print.chat(player, Config.getWorthAsString(amount, true, true) + " couldn't be added to inventory because no matching items were found.");
		}
		return amount;
	}

}