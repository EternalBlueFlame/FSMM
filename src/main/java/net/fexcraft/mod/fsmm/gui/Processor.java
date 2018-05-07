package net.fexcraft.mod.fsmm.gui;

import java.io.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketJsonObject;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.entity.player.EntityPlayerMP;

public class Processor implements IPacketListener<PacketJsonObject> {

	@Override
	public String getId(){
		return "fsmm:atm_gui";
	}

	@Override
	public void process(PacketJsonObject pkt, Object[] objs){
		Print.debug(pkt.obj);
		if(pkt.obj.has("request")){
			EntityPlayerMP player = (EntityPlayerMP)objs[0];
			Account playeracc = AccountManager.INSTANCE.getAccount("player", player.getGameProfile().getId().toString(), true);
			JsonObject reply = new JsonObject();
			switch(pkt.obj.get("request").getAsString()){
				case "main_data":{
					reply.addProperty("bank_id", playeracc.getBankId().toString());
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("bank_name", bank == null ? "Invalid Null Bank" : bank.getName());
					break;
				}
				case "show_balance":{
					reply.addProperty("balance", playeracc.getBalance());
					break;
				}
				case "deposit_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("success", bank.processDeposit(player, playeracc, input));
					break;
				}
				case "withdraw_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					reply.addProperty("success", bank.processWithdraw(player, playeracc, input));
					break;
				}
				case "account_types":{
					JsonArray types = new JsonArray();
					for(File fl : AccountManager.ACCOUNT_SAVE_DIRECTORY.listFiles()){
						if(fl.isDirectory() && !fl.isHidden()){
							types.add(fl.getName());
						}
					}
					if(types.size() == 0){
						types.add("nothing found");
					}
					reply.add("types", types);
					break;
				}
				case "accounts_of_type":{
					File file = new File(AccountManager.ACCOUNT_SAVE_DIRECTORY, pkt.obj.get("type").getAsString() + "/");
					JsonArray accounts = new JsonArray();
					if(file.exists() && file.isDirectory()){
						for(File fl : file.listFiles()){
							if(!fl.isDirectory() && !fl.isHidden() && fl.getName().endsWith(".json")){
								accounts.add(fl.getName().substring(0, fl.getName().length() - 5));
							}
						}
					}
					else{
						accounts.add("type not found");
					}
					if(accounts.size() == 0){
						accounts.add("nothing found");
					}
					reply.add("accounts", accounts);
					break;
				}
				case "transfer_result":{
					long input = pkt.obj.get("input").getAsLong();
					if(input <= 0){ return; }
					Bank bank = AccountManager.INSTANCE.getBank(playeracc.getBankId());
					String[] str = pkt.obj.get("receiver").getAsString().split(":");
					Account receiver = AccountManager.INSTANCE.getAccount(str[0], str[1], true);
					if(receiver == null){
						Print.chat(player, "Error loading Receiver account.");
						return;
					}
					reply.addProperty("success", bank.processTransfer(player, playeracc, input, receiver));
					reply.addProperty("receiver", receiver.getAsResourceLocation().toString());
					break;
				}
			}
			reply.addProperty("payload", pkt.obj.get("request").getAsString());
			reply.addProperty("target_listener", "fsmm:atm_gui");
			PacketHandler.getInstance().sendTo(new PacketJsonObject(reply), player);
		}
	}
	
}