package kits_pack;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class kits_class extends JavaPlugin implements Listener{

	public ArrayList<Player> once = new ArrayList<Player>();
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		
		int loop = 1;
		
		for(int i = 0; i < loop; i++) {
			if(getConfig().getString("saved_players." + i) != null) {
				Player p = Bukkit.getPlayer(getConfig().getString("saved_players." + i));
				once.add(p);
				loop += 1;
			} else {loop = 0;}
		}
	}
	
	@Override
	public void onDisable() {
		for(int a = 0; a < once.size(); a++) {
			Player p = once.get(a);
			this.getConfig().set("saved_players." + a, p.getName());
		}
		
		this.saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		
		if(label.equalsIgnoreCase("once") && !once.contains(p)) {
			once.add(p);
			p.getInventory().addItem(new ItemStack(Material.COOKED_PORKCHOP, 16));
			p.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE, 1));
			p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
		}
		
		return false;		
	}
	
}
