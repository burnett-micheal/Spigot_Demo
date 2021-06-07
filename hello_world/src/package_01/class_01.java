package package_01;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class class_01 extends JavaPlugin implements Listener{

	public ArrayList<Player> online_players = new ArrayList<Player>();
	public Integer max_pay_distance = 50;
	public Boolean allow = false;
	public ArrayList<Player> no_home = new ArrayList<Player>();
	
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
	}
	
	@EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		online_players.add(p);
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		online_players.remove(p);
	}
	
	@EventHandler
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
		
		p.sendMessage(parts[0] + parts[1]);
		
		if(parts[0].equalsIgnoreCase("/" + "t") && parts[1].equalsIgnoreCase("spawn")) {
			
			p.sendMessage("t spawn detected");
			
	        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	            public void run() {
	            	p.sendMessage("2 seconds have passed");
	            }
	        }, 40);
	        
			}			
		}
	}
