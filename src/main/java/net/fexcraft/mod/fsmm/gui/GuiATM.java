package net.fexcraft.mod.fsmm.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketJsonObject;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiATM extends GuiScreen {
	
	private static ResourceLocation bg = new ResourceLocation("fsmm:textures/gui/GuiATM.png");
	private static int sx = 176;
	private static int sy = 152;
	private static ArrowButton l0, l1, r0, r1;
	private static final String pktlid = "fsmm_atm_gui";
	protected static String log = ">_";
	protected static long balance = -1l;
	private GuiTextField amount_field;
	private GuiTextField receiver_field;
	
	public GuiATM(boolean opentype){
		if(!opentype){
			sendJsonCommandPacket(JsonUtil.getJsonForPacket(pktlid), "get_balance", false);
		}
	}
	
	@Override
    public void drawScreen(int mx, int my, float f){		
		this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(bg);
        this.drawTexturedModalRect((this.width - sx) / 2, (this.height - sy) / 2, 0, 0, sx, sy);
        
        l0.drawButton(mc, mx, my, f); l1.drawButton(mc, mx, my, f);
        r0.drawButton(mc, mx, my, f); r1.drawButton(mc, mx, my, f);
        
        this.fontRenderer.drawString(Config.getWorthAsString(balance), ((this.width - sx) / 2) + 33, ((this.height - sy) / 2) + 66, 0x000000);
        this.fontRenderer.drawString(log, ((this.width - sx) / 2) + 33, ((this.height - sy) / 2) + 88, 0x000000);
        
        this.amount_field.drawTextBox();
        this.receiver_field.drawTextBox();
	}
	
	@Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.amount_field.mouseClicked(mouseX, mouseY, mouseButton);
        this.receiver_field.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	@Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        if (this.amount_field.textboxKeyTyped(typedChar, keyCode)){
        	return;
        }
        else if(this.receiver_field.textboxKeyTyped(typedChar, keyCode)){
        	return;
        }
        else{
            super.keyTyped(typedChar, keyCode);
        }
    }
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	public static class Receiver implements IPacketListener<PacketJsonObject> {

		@Override
		public String getId(){
			return pktlid;
		}

		@Override
		public void process(PacketJsonObject pkt, Object[] objs){
			if(Config.DEBUG){
				Print.log("PKT R - Client: " + pkt.obj.toString());
			}
			boolean reopen = false;
			if(pkt.obj.has("balance")){
				balance = pkt.obj.get("balance").getAsLong();
				reopen = true;
			}
			if(pkt.obj.has("log")){
				log = pkt.obj.get("log").getAsString();
				reopen = true;
			}
			if(reopen){
				EntityPlayer player = Minecraft.getMinecraft().player;
				player.openGui(FSMM.getInstance(), 1, player.world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
			}
		}
		
	}
	
	@Override
    public void initGui(){
		super.initGui();
        Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(l0 = new ArrowButton(0, ((this.width - sx) / 2) +  15, ((this.height - sy) / 2) + 12, EnumSide.RIGHT));
		this.buttonList.add(l1 = new ArrowButton(1, ((this.width - sx) / 2) +  15, ((this.height - sy) / 2) + 35, EnumSide.RIGHT));
		this.buttonList.add(r0 = new ArrowButton(2, ((this.width - sx) / 2) + 151, ((this.height - sy) / 2) + 12, EnumSide.LEFT));
		this.buttonList.add(r1 = new ArrowButton(3, ((this.width - sx) / 2) + 151, ((this.height - sy) / 2) + 35, EnumSide.LEFT));
		this.amount_field = new GuiTextField(0, this.fontRenderer, ((this.width - sx) / 2) + 36, ((this.height - sy) / 2) + 116, 103, 12);
        this.amount_field.setTextColor(-1);
        this.amount_field.setDisabledTextColour(-1);
        this.amount_field.setEnableBackgroundDrawing(false);
        this.amount_field.setMaxStringLength(32);
        this.receiver_field = new GuiTextField(0, this.fontRenderer, ((this.width - sx) / 2) + 36, ((this.height - sy) / 2) + 136, 103, 12);
        this.receiver_field.setTextColor(-1);
        this.receiver_field.setDisabledTextColour(-1);
        this.receiver_field.setEnableBackgroundDrawing(false);
        this.receiver_field.setMaxStringLength(32);
        this.receiver_field.setText(mc.player.getName());
	}
	
	@Override
    protected void actionPerformed(GuiButton button){
		JsonObject obj = JsonUtil.getJsonForPacket(pktlid);
		switch(button.id){
			case 0: //l0;
				obj.addProperty("amount", amount_field.getText());
				sendJsonCommandPacket(obj, "deposit", true);
				break;
			case 1: //l1;
				obj.addProperty("amount", amount_field.getText());
				obj.addProperty("receiver", receiver_field.getText());
				sendJsonCommandPacket(obj, "transfer", true);
				break;
			case 2: //r0;
				obj.addProperty("amount", amount_field.getText());
				sendJsonCommandPacket(obj, "withdraw", true);
				break;
			case 3: //r1;
				close();
				break;
			default: return;
		}
	}
	
	public static void sendJsonCommandPacket(JsonObject obj, String task, boolean reopen){
		obj.addProperty("sender", Minecraft.getMinecraft().player.getName());
		obj.addProperty("task", task);
		PacketHandler.getInstance().sendToServer(new PacketJsonObject(obj));
		if(reopen){
			close();
		}
		if(Config.DEBUG){
			Print.log("PKT S - Client: " + obj.toString());
		}
	}
	
	public static void close(){
		Minecraft.getMinecraft().displayGuiScreen(null);
        if (Minecraft.getMinecraft().currentScreen == null){
        	Minecraft.getMinecraft().setIngameFocus();
        }
	}
	
	public static class ArrowButton extends GuiButton {
		private static ResourceLocation texture = new ResourceLocation("fsmm:textures/gui/GuiATM.png");
		private EnumSide type;
		
		public ArrowButton(int buttonID, int x, int y, EnumSide type){
			super(buttonID, x, y, 10, 15, "");
			this.type = type;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
			if(this.visible){
				int ytp = type.getPosition();
				mc.getTextureManager().bindTexture(texture);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				if(this.enabled){
					if(!this.hovered){
						this.drawTexturedModalRect(this.x, this.y, 178, ytp, this.width, this.height);
					}
					else{
						this.drawTexturedModalRect(this.x, this.y, 190, ytp, this.width, this.height);
					}
				}
				else{
					this.drawTexturedModalRect(this.x, this.y, 202, ytp, this.width, this.height);
				}
			}
		}
	}
	
	public static enum EnumSide {
		RIGHT(2, "right"), LEFT(21, "left");

		public int pos;
		public String id;
		
		private EnumSide(int pos, String id){
			this.pos = pos;
			this.id = id;
		}
		
		public int getPosition(){
			return pos;
		}
		
		public String getId(){
			return id;
		}
	}
	
}