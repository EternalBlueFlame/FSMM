package net.fexcraft.mod.fsmm.api;

import com.google.gson.JsonObject;

import java.time.LocalDate;

/**
 * Internal Usage Class, do not bother with.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public abstract class Removable {
	
	private boolean temporary;
	private long last_access;
	
	/** Time of when this Account/Bank was last accessed, used for removing temporary loaded account/bankss. */
	public long lastAccessed(){
		return temporary ? last_access : -1;
	}
	
	/** Selfexplaining. */
	public long updateLastAccess(){
		return last_access = temporary ? LocalDate.now().getDayOfMonth() : -1;
	}
	
	@SuppressWarnings("unchecked")
	/** Set this instance as "temporary loaded", as such, to be removed next check for inactive accounts/banks.*/
	public <T extends Removable> T setTemporary(boolean value){
		this.temporary = value;
		this.updateLastAccess();
		return (T)this;
	}
	
	public boolean isTemporary(){
		return temporary;
	}
	
	public abstract JsonObject toJson();
	
}