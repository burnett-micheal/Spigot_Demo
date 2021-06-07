package pack_01;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class class_01 extends JavaPlugin implements Listener{

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {

	}
	
	@EventHandler
	public void command_event(PlayerCommandPreprocessEvent e, String s) {
		
		Player player = e.getPlayer();
		
		if(s.equalsIgnoreCase("home")) {
			player.sendMessage("home command sent");
		} else {player.sendMessage("home command not sent");}
	}
	
}
