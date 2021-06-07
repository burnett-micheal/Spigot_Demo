package time_pack;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class time_class extends JavaPlugin implements Listener{

	public ArrayList<Player> counting = new ArrayList<Player>();
	public ArrayList<Player> not_counting = new ArrayList<Player>();
	public boolean timer_going = false;
	
	
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
	}
	
	public void onDisable() {
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(timer_going == false) {timer(); timer_going = true;}		
		not_counting.add(p);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		not_counting.remove(p);
		counting.remove(p);
	}
	
	public void timer() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {          	
            	update_player_data();
            }
        }, 5990);
	}
	
	public void update_player_data() {		
		
		for(int a = 0; a < counting.size(); a++) {
			Player p = counting.get(a);
			if(getConfig().getString("player_time." + p.getName()) != null) {
				int time = Integer.parseInt(getConfig().getString("player_time." + p.getName()));
				if(time == 1200) {give_ranks(p);}
				if(time == 3000) {give_ranks(p);}
				if(time == 6000) {give_ranks(p);}
				getConfig().set(p.getName(), null);
				this.getConfig().set("player_time." + p.getName(), time + 5);
			} else {
				this.getConfig().set("player_time." + p.getName(), 5);
				}
			
	    	this.saveConfig();
		}
		
		for(int a = 0; a < not_counting.size(); a++) {			
			counting.add(not_counting.get(a));
			not_counting.remove(a);
		}
		
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {          	
            	timer();
            }
        }, 10);
	}
	
	public void give_ranks(Player p) {
		p.sendMessage("You've been promoted!");
		getServer().dispatchCommand(getServer().getConsoleSender(), "pex promote " + p.getName() + " player" );
	}
	
	
	
}
