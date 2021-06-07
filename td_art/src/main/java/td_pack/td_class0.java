package td_pack;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import net.md_5.bungee.api.ChatColor;

public class td_class0 implements CommandExecutor, Listener{
	
	public Integer b_cost = 25;
	
	static td_class plugin;
	public td_class0(td_class instance) {
		plugin = instance;
	}
	
	public void dte(DeleteTownEvent e) {
		Bukkit.broadcastMessage("Town Deleted");
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
		
    	if(label.equalsIgnoreCase("testing")) {Bukkit.broadcastMessage("MULTIPLE CLASSES COMMANDS NOW WORK");}
    	Player p = (Player) sender;
    	if(label.equalsIgnoreCase("bounty")) {
    		
    		if(args[0].equalsIgnoreCase("list")) {
    			
    			if(args.length == 1) {
    				Set<String> a = plugin.getConfig().getConfigurationSection("bounty").getKeys(false);
        			
        			for(String b : a) {
    				Set<String> c = plugin.getConfig().getConfigurationSection("bounty." + b).getKeys(false);
    				Integer total = 0;
    				
        			for(String d : c) {
        				String e = plugin.getConfig().getString("bounty." + b + "." + d + "." + "R");
        				total += Integer.valueOf(e);
        			}
    				p.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.YELLOW + b + ChatColor.GREEN + "Bounty Reward:" + ChatColor.GREEN + total.toString());
    			}
    			} else {
    				Boolean is_pl = false;
        			OfflinePlayer[] op = Bukkit.getOfflinePlayers();
        			Bukkit.broadcastMessage(String.valueOf(op.length));
    				Bukkit.broadcastMessage(op[0].getName());
        			
    				for(int a = 0; a < op.length; a++) {
        				Bukkit.broadcastMessage(op[a].getName());
        				if(op[a].getName().equalsIgnoreCase(args[1])) {is_pl = true;}
        			}
    				
    				if(is_pl) {
    					
        				Set<String> a = plugin.getConfig().getConfigurationSection("bounty." + args[1]).getKeys(false);
        				
            			for(String d : a) {
            				String h = d;
            				String r = plugin.getConfig().getString("bounty." + args[1] + "." + d + "." + "R");
            				String t = plugin.getConfig().getString("bounty." + args[1] + "." + d + "." + "T");
            				p.sendMessage(ChatColor.YELLOW + "Holder: " + h + " " + "Town: " + t + " " + ChatColor.GREEN + "Reward: " + r);
            			}
    				}
    				
    				try {
						Town t = plugin.td().getTown(args[1]);
						
	    				Set<String> a = plugin.getConfig().getConfigurationSection("bounty").getKeys(false);
	        			
	        			for(String b : a) {
	    				Set<String> c = plugin.getConfig().getConfigurationSection("bounty." + b).getKeys(false);
	    				
	        			for(String d : c) {
	        				String T = plugin.getConfig().getString("bounty." + b + "." + d + "." + "T");
	        				String R = plugin.getConfig().getString("bounty." + b + "." + d + "." + "R");
	        				String Ta = b;
	        				String H = d;
	        				if(T.equalsIgnoreCase(args[1])) {p.sendMessage(ChatColor.YELLOW + "Holder: " + H + " Target: " + Ta + " Town: " + T + ChatColor.GREEN + " Reward:" + R);}
	        			}
	    			}
					} catch (NotRegisteredException e) {}
    				}
    			}
    		
    		
    		
    		if(args[0].equalsIgnoreCase("add") && args.length == 3) {
        		if(plugin.get_town(plugin.get_resident(p)) == null) {p.sendMessage(ChatColor.YELLOW + "You Must Be The Resident Of A Town To Use That Command!");return true;}
        		Resident r = plugin.get_resident(p);
    			Boolean is_num = false;
    			Boolean is_pl = false;
    			try {
    				  Integer num = Integer.parseInt(args[2]);
    				  is_num = true;
    				} catch (NumberFormatException ex){}
    			
    			OfflinePlayer[] op = Bukkit.getOfflinePlayers();
    			Bukkit.broadcastMessage(String.valueOf(op.length));
				Bukkit.broadcastMessage(op[0].getName());
    			
				for(int a = 0; a < op.length; a++) {
    				Bukkit.broadcastMessage(op[a].getName());
    				if(op[a].getName().equalsIgnoreCase(args[1])) {is_pl = true;}
    			}
    			
    			try {
					if(is_pl && is_num && p_town(p) != null && r.getAccount().getHoldingBalance() > b_cost + Integer.valueOf(args[2])) {
						
						r.getAccount().pay(b_cost + Integer.valueOf(args[2]), "Bounty Cost");
						p_town(p).getAccount().collect(b_cost, "Bounty Pay");
						
						if(plugin.getConfig().getString("bounty." + args[1] + "." + p.getName()) != null) {
							p.sendMessage(ChatColor.YELLOW + "You Already Have A Bounty On That Player!");
						} else {
							conf_set("bounty." + args[1] + "." + p.getName() + "." + "R", args[2]);
							conf_set("bounty." + args[1] + "." + p.getName() + "." + "T", p_town(p).getName());
							p.sendMessage(ChatColor.YELLOW + "Bounty Set!");
						}
						
						} else {
						if(!is_pl) {p.sendMessage(ChatColor.YELLOW + "That Player Does Not Exist!");}
						if(p_town(p) == null) {p.sendMessage(ChatColor.YELLOW + "You Have To Be In A Town To Set A Bounty!");}
					}
				
    			} catch (NumberFormatException | EconomyException e) {}
    		}
    	}
    	
    	return false;
    	
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		
		Player killed = e.getEntity().getPlayer();
		Player killer;
		
		if(killed.getKiller() != null) {
			if(killed.getKiller() instanceof Player) {
				killer = e.getEntity().getKiller().getPlayer(); 
				
				if(plugin.getConfig().contains("bounty." + killed.getName())) {
					killer.sendMessage(ChatColor.RED + "You Killed A Bounty!");
					ItemStack item = new ItemStack(Material.PAPER, 1);
	                ItemMeta m = item.getItemMeta();
	                m.setDisplayName(ChatColor.YELLOW + "Bounty Certificate");
	                ArrayList<String> lore = new ArrayList<String>();	            
	                
    				Set<String> c = plugin.getConfig().getConfigurationSection("bounty." + killed.getName()).getKeys(false);
    				
        			for(String d : c) {
        				String Ta = killed.getName();
        				String T = plugin.getConfig().getString("bounty." + Ta + "." + d + "." + "T");
        				String R = plugin.getConfig().getString("bounty." + Ta + "." + d + "." + "R");
        				String H = d;
        				
    	                lore.clear();
    	                lore.add(H);
    	                lore.add(Ta);
    	                lore.add(T);
    	                lore.add(R);
    	                
    	                m.setLore(lore);
    	                item.setItemMeta(m);
    	                
    	                killer.getWorld().dropItemNaturally(killed.getLocation(), item);
        			}
	                
				}
			}}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
	    Player p = event.getPlayer();
	 
	    ItemStack item = new ItemStack(Material.PAPER, 1);
	    
	    if(p.getInventory().getItemInMainHand().getType() == item.getType()){
		    item = p.getInventory().getItemInMainHand();
	    	if(item.getItemMeta().hasLore()) {
	    		try {
					Town t_in = TownyAPI.getInstance().getTownBlock(p.getLocation()).getTown();
					Town t_bo = plugin.td().getTown(item.getItemMeta().getLore().get(0));
					
					if(t_in == t_bo) {
					    plugin.cmd("eco give " + p.getName() + " " + item.getItemMeta().getLore().get(2));
						
						conf_set("bounty." + item.getItemMeta().getLore().get(1), item.getItemMeta().getLore().get(0) + " " + item.getItemMeta().getLore().get(2));
						
						plugin.saveConfig();
						p.sendMessage(ChatColor.YELLOW + "You Collected Your Bounty!");
						Integer h = p.getInventory().getItemInMainHand().getAmount();
						p.getInventory().getItemInMainHand().setAmount(h - 1);
						}
					
				} catch (NotRegisteredException e) {}
	    	}}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Bukkit.broadcastMessage("MULTIPLE CLASSES EVENTS NOW WORK");
	}
	
	public void conf_set(String a, String b) {
		
		plugin.getConfig().set(a, b);
		
		plugin.saveConfig();
	}
	
	public Town p_town(Player p) {
		try {
			Town t = TownyAPI.getInstance().getTownBlock(p.getLocation()).getTown();
			return t;
		} catch (NotRegisteredException e) {
			return null;
		}
	}
	
	
	
}
