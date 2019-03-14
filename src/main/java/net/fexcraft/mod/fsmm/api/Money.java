package net.fexcraft.mod.fsmm.api;

import net.fexcraft.mod.fsmm.FSMM;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface Money{

	public long getWorth();
	
	public static ItemStack getItemStack(Money m){return new ItemStack(FSMM.CURRENCY_ITEMS.get(m),1).copy();}

	public ResourceLocation getRegistryName();

	public Money setRegistryName(ResourceLocation name);
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth(ItemStack stack);
		
	}

}
