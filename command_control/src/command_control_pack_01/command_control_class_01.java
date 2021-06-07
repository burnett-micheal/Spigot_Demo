package command_control_pack_01;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;



public class command_control_class_01 extends JavaPlugin implements Listener{

	public Location town;
	public ArrayList<Player> online_players = new ArrayList<Player>();
	public Integer max_pay_distance = 50;
	public Boolean allow = false;
	public ArrayList<Player> no_home = new ArrayList<Player>();
	public float t_cost = 0.05f;
	public HashMap<Player, Location> player_loc = new HashMap<Player, Location>(); 
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		//startup
		//reloads
		//this reloads
	}
	
	@Override
	public void onDisable() {
		//shutdown
		//reloads
		//this reloads
		
		for(int a = 0; a < online_players.size(); a++) {
			Player p = online_players.get(a);
			if(player_loc.containsKey(p)) {
				Location loc = player_loc.get(p);
				player_loc.remove(p, loc);
				p.teleport(loc);
				}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		double x = p.getLocation().getX();
		double y = p.getLocation().getY();
		double z = p.getLocation().getZ();
		p.sendMessage("You died at " + String.valueOf((int)Math.round(x)) + ", " + String.valueOf((int)Math.round(y)) + ", " + String.valueOf((int)Math.round(z)));
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		online_players.add(p);
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		
		if(player_loc.get(p) != null) {
		Location loc = player_loc.get(p);
		player_loc.remove(p, loc);
		p.teleport(loc);
		}
		
		online_players.remove(p);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String[] parts = event.getMessage().split(" ");
		
		if(parts[0].equalsIgnoreCase("/" + "home") || parts[0].equalsIgnoreCase("/" + "homes")) {			
			if(no_home.contains(p)) {
			event.setCancelled(true);
			p.sendMessage("Only one /home is allowed per day, try again tomorrow");
			} else {no_home.add(p);}
		}
		
		if(parts[0].equalsIgnoreCase("/" + "pay")) {
			double distance = p.getLocation().distance(Bukkit.getPlayer(parts[1]).getLocation());
			if(distance > max_pay_distance) {
				event.setCancelled(true);
				p.sendMessage("You are too far away, maximum pay distance is " + String.valueOf(max_pay_distance));
			}			
		}
		

		
	}
	
}
