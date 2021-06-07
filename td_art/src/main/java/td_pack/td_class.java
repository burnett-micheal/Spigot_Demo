package td_pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.tasks.TownClaim;

import net.md_5.bungee.api.ChatColor;

public class td_class extends JavaPlugin implements Listener{
	
	public static Plugin plugin;
	
	public Integer death_cost = 25;
	public Integer pvp_off_cost = 25;
	public ArrayList<Town> t_pvp_off = new ArrayList<Town>();
	
	public Float death_loss = 0.25f;
	public Integer town_loss = 25;
	public Integer nat_loss = 25;
	
	public Float gain = 0.25f;
	
	public Integer tb_cost = 50;
	public float interest = 1.1f;
	
	public HashMap<Player, Town> tp_data = new HashMap<Player, Town>();
	public HashMap<Player, Double> tp_cost = new HashMap<Player, Double>();
	
	public HashMap<Town, Integer> pvp_time = new HashMap<Town, Integer>();
	public Integer pvp_cost = 15;
	
	public HashMap<Nation, Nation> peace = new HashMap<Nation, Nation>();
	
	public ArrayList<Player> spawn = new ArrayList<Player>();
	
	public HashMap<Player, HashMap<Integer, Integer>> dl_data = new HashMap<Player, HashMap<Integer, Integer>>();
	
	public ArrayList<Player> lobbied = new ArrayList<Player>();
	public Location lobby_spawn = new Location(Bukkit.getWorld("superflat"), 221, 54, -92);
	public HashMap<Player, Location> rtp = new HashMap<Player, Location>();
	
	public HashMap<Player, Location> back = new HashMap<Player, Location>();
	
	@Override
	public void onEnable() {
		plugin = this;
		
		this.getServer().getPluginManager().registerEvents(this, this);
		getCommand("testing").setExecutor(new td_class0(this));
		getCommand("bounty").setExecutor(new td_class0(this));
		getCommand("shop").setExecutor(new td_class1(this));
		this.getServer().getPluginManager().registerEvents(new td_class0(this), this);
		this.getServer().getPluginManager().registerEvents(new td_class1(this), this);
		hour_loop();		
	}
	
	@Override
	public void onDisable() {

		for(Player k : back.keySet()) {
			k.teleport(back.get(k));
		}
		
		plugin = null;
		td().saveAll();
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		if(back.containsKey(e.getPlayer())) {
			e.getPlayer().teleport(back.get(e.getPlayer()));
		}
	}
	
	public static void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
		for (Listener listener : listeners) {
		Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
		}}
	
	public static Plugin get_plugin() {
		return plugin;
	}
	
	public void hour_loop() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	update_hour();
            }      
        }, 72000, 72000);
	}
	
	public void update_hour() {
		Set<String> k = this.getConfig().getConfigurationSection("pvp_timers").getKeys(false);
		
		for(String a : k) {
			Integer timer = Integer.valueOf(this.getConfig().getString("pvp_timers." + a));
			Bukkit.broadcastMessage(a + " " + timer);
			timer -= 1;
			if(timer > 0) {this.getConfig().set("pvp_timers." + a, timer);} else {this.getConfig().set("pvp_timers." + a, null);
			}}
		
		this.saveConfig();
		Bukkit.broadcastMessage(k.toString());
	}
	
	@EventHandler
	public void nte(NewTownEvent e) {
		Town t = e.getTown();
		Resident p = t.getMayor();
		if(t.isPVP()) {
		cmd("sudo " + p.getName() + " t toggle pvp");}
	}
	
	@EventHandler
	public void ode(EntityDamageByEntityEvent e) {
		
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damaged = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			Town dtown = get_town(get_resident(damaged));
			Town town;
			try {
				town = TownyAPI.getInstance().getTownBlock(damaged.getLocation()).getTown();

			if(dtown.equals(town)) {
				
				if(get_nation(get_town(get_resident(damager))) != null && get_nation(get_town(get_resident(damaged))) != null) {
					Nation damaged_n = get_nation(get_town(get_resident(damaged)));
					Nation damager_n = get_nation(get_town(get_resident(damager)));
					if(at_war_with(damaged_n, damager_n)) {
						if(this.getConfig().getString("pvp_timers." + town.getName()) != null) {e.setCancelled(true); damager.sendMessage(ChatColor.YELLOW + damager_n.getName() + ChatColor.YELLOW + "They Have PVP Disabled");}
					}
				}}			
			} catch (NotRegisteredException e1) {
				//there in the wilderness
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(at_war(get_nation(get_town(get_resident(p))))) {p.sendMessage(ChatColor.DARK_RED + "A WAR HAS BEEN DECLARED!"); p.sendMessage(ChatColor.YELLOW + "Do /war list To See Active Wars");}
	}
	
	public TownBlock get_tb(List<TownBlock> tb) {
		
		for(int a = 0; a < tb.size(); a++) {
			if(tb.get(a).isHomeBlock() || tb.get(a).isOutpost()) {} else {return tb.get(a);}
		}
		
		return null;		
	}
	
    @SuppressWarnings("unused")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player) sender;
    	
		Resident r;
		
		r = get_resident(p);
		
		if(label.equalsIgnoreCase("b") && back.containsKey(p)) {
			tp(back.get(p), p, 4);
		}
		
		if(label.equalsIgnoreCase("overworld") && !spawn.contains(p)) {
			spawn.add(p);
			p.teleport(new Location(Bukkit.getWorld("world"), -301, 87, -92));
		}
		
		if(r != null && 1 == 2) {
			if(label.equalsIgnoreCase("test")) {
				Town t = get_town(r);
				Bukkit.broadcastMessage(Boolean.toString(t.isPVP()));
				if(t.isPVP()) {
					p.sendMessage("a");
					t.setPVP(false);
					td().saveTown(t);
				} else {
					p.sendMessage("b");
					t.setPVP(true);
					td().saveTown(t);
				}
			}}	
		
		try {
			r = td().getResident(p.getName());
			Town town = td().getTown(r.getTown().getName());
			Nation nation = td().getNation(town.getNation().getName());
	    	
			if(label.equalsIgnoreCase("war") && args[0].equalsIgnoreCase("pvp") && args[1] != null) {
				Town t = td().getTown(args[1]);
				if(pvp_time.containsKey(t)) {
					p.sendMessage(ChatColor.YELLOW + "That Town Has PVP Disabled");
				} else {
					if(at_war(t.getNation())) {p.sendMessage(ChatColor.YELLOW + "That Town Has PVP Enabled");} else {p.sendMessage(ChatColor.YELLOW + "That Town Is Not At War, And Can Change There PVP At Any Time");}
				}}
			
	    	if(r.isMayor() || r.isKing()) {
	    		
	    		if(r.isKing()) {
	    			if(label.equalsIgnoreCase("declare") && args.length > 0) {
	    				if(args[0].equalsIgnoreCase("war")  && args[1] != nation.getName()){
	    					try {
	    						
	    						Nation enemy = TownyAPI.getInstance().getDataSource().getNation(args[1]);
	    						if(nation.hasEnemy(enemy)) {
	    						String nation_war = this.getConfig().getString("war." + nation.getName());
	    						String enemy_war = this.getConfig().getString("war." + enemy.getName());
	    						
	    						if(nation_war == null) {this.getConfig().set("war." + nation.getName(), enemy.getName());} else {
	    							if(!nation_war.contains(enemy.getName())) {this.getConfig().set("war." + nation.getName(), nation_war + " " + enemy.getName());}
	    						}
	    						
	    						if(enemy_war == null) {this.getConfig().set("war." + enemy.getName(), nation.getName());} else {
	    						if(!enemy_war.contains(nation.getName())) {this.getConfig().set("war." + enemy.getName(), enemy_war + " " + nation.getName());}
	    						}
	    						
	    						nat_pvp(nation, true);
	    						nat_pvp(enemy, true);
	    						
	    						this.saveConfig();
	    						
	    						for(int a = 0; a < nation.getResidents().size(); a++) {Bukkit.getPlayer(nation.getResidents().get(a).getName()).sendMessage(ChatColor.DARK_RED + "A WAR HAS BEEN DECLARED WITH " + enemy.getName());}
	    						for(int a = 0; a < enemy.getResidents().size(); a++) {Bukkit.getPlayer(enemy.getResidents().get(a).getName()).sendMessage(ChatColor.DARK_RED + "A WAR HAS BEEN DECLARED WITH " + nation.getName());}
	    						} else {p.sendMessage(ChatColor.YELLOW + "You Are Not Enemies With That Nation!");}
	    						} catch (NotRegisteredException | NullPointerException e) {p.sendMessage(ChatColor.YELLOW + "That Nation Does Not Exist");}	
	    				}
	    				
	    				if(args[0].equalsIgnoreCase("peace")  && args[1] != nation.getName()) {
	    					if(at_war_with(nation, td().getNation(args[1]))) {
	    						Nation nat2 = td().getNation(args[1]);
	    						if(peace.containsKey(nat2)) {end_war(2, nation, nat2.getName()); peace.remove(nat2);} else {
	    							peace.put(nation, nat2);
	    							Bukkit.broadcastMessage(ChatColor.BLUE + nation.getName() + ChatColor.YELLOW + " Is Offering Peace To " + ChatColor.BLUE + nat2.getName());
	    						}}}
	    			}
	    			

	    		}
	    		
	    		if(r.isMayor()) {
	    			if(label.equalsIgnoreCase("dl") && args.length > 0) {
	    				if(Double.valueOf(args[0]) > 0) {
	    					this.getConfig().set("town_settings." + town, String.valueOf(args[0]));
	    					this.saveConfig();
	    				}}
	    		}
	    	}
	    	
	    	if(label.equalsIgnoreCase("war")) {
	    		if(args[0].equalsIgnoreCase("list")) {
	    			p.sendMessage(wars(p));
	    		}}
	    	
		} catch (NotRegisteredException e) {p.sendMessage(ChatColor.YELLOW + "Your Not In A Nation");}
		
        return false;        
    }
    
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		
		final Player p = e.getPlayer();
		String[] parts = e.getMessage().split(" ");
		if(parts[0].equalsIgnoreCase("/spawn")) {
			if(p.getWorld() != Bukkit.getWorld("superflat")) {
				back.put(p, p.getLocation());
			}
		}
		
		if(parts[0].equalsIgnoreCase("/res") && parts[1].equalsIgnoreCase("spawn")) {e.setCancelled(true);}
		
		if(parts[0].equalsIgnoreCase("/n") || parts[0].equalsIgnoreCase("/nation")) {
			if(get_nation(get_town(get_resident(p))) != null) {
				if(get_resident(p).isKing()) {
					if(parts[1].equalsIgnoreCase("merge")) {
						try {
							Nation n = td().getNation(parts[2]);
							if(at_war_with(get_nation(get_town(get_resident(p))), n)) {
								e.setCancelled(true); p.sendMessage(ChatColor.YELLOW + "You Can't Merge With A Nation Your At War With!");
							}
						} catch (NotRegisteredException e1) {p.sendMessage(ChatColor.YELLOW + "That Nation Does Not Exist");}
					}
				}
			}
		}
		
		if(parts[0].equalsIgnoreCase("/t") || parts[0].equalsIgnoreCase("/town")) {
			if(parts[1].equalsIgnoreCase("spawn")) {
				
				if(parts.length == 2) {e.setCancelled(true);}
				if(get_town(get_resident(p)) == null) {p.sendMessage(ChatColor.YELLOW + "You Must Be A Member Of A Town To Teleport Between Towns"); e.setCancelled(true); return;}
				
				if(parts.length == 3 && !tp_data.containsKey(p)) {
					Boolean enemies = false;
					
					
					//are they enemies?
					try {
						Town t = td().getTown(parts[2]);
						Nation n = get_nation(t);
						Nation na = get_nation(get_town(get_resident(p)));
						
						if(n != null && na != null) {if(n.hasEnemy(na) || na.hasEnemy(n)) {enemies = true;}
						if(at_war_with(n, na)) {enemies = true;}}
						
					} catch (NotRegisteredException e2) {}
					
					if(!enemies) {
						e.setCancelled(true);
						Location loc_1;				
					
						try {
						loc_1 = td().getTown(parts[2]).getSpawn();
					
			            Double dis = loc_1.distance(p.getLocation());
			            long cost = Math.round((dis * 0.05f) * 100);
			            cost = cost/100;
			            p.sendMessage(ChatColor.YELLOW + "It will cost you " + ChatColor.GREEN +"$" + ChatColor.GREEN + cost + ChatColor.YELLOW + " to travel there, do the command again to confirm");
			            tp_cost.put(p, dis * 0.05f);
						} catch (TownyException e2) {Bukkit.broadcastMessage("ERROR");}
						
		            try {tp_data.put(p, td().getTown(parts[2]));} catch (NotRegisteredException e1) {p.sendMessage(ChatColor.YELLOW + "That Town Does Not Exist");}
		            
					} else {p.sendMessage(ChatColor.YELLOW + "You Cannot Teleport To Enemies"); e.setCancelled(true);}
					
				} else {
					if(parts.length == 3) {
					if(tp_data.containsKey(p) && tp_data.get(p).getName().equalsIgnoreCase(parts[2])) {
						if(get_resident(p) != null) {try {
							if(get_resident(p).getAccount().getHoldingBalance() > tp_cost.get(p)) {get_resident(p).getAccount().pay(tp_cost.get(p), "You TP'ed"); tp_cost.remove(p); tp_data.remove(p);} else {e.setCancelled(true); p.sendMessage(ChatColor.BLUE + "You Dont Have Enough Money"); tp_data.remove(p); tp_cost.remove(p);}
						} catch (EconomyException e1) {p.sendMessage(ChatColor.DARK_RED + "ECONOMY ERROR");}
						} else {e.setCancelled(true); p.sendMessage(ChatColor.YELLOW + "You Must Be A Member Of A Town To TP Between Towns");}
					} else {e.setCancelled(true); tp_data.remove(p); tp_cost.remove(p); p.sendMessage(ChatColor.YELLOW + "Thats The Wrong Town, Teleport Cancelled");}
				} else {p.sendMessage(ChatColor.YELLOW + "Improper Command, Use /t spawn TOWN_NAME To Teleport To A Town");}
					}

				}
			
			if(parts[1].equalsIgnoreCase("toggle")) {
				if(parts[2].equalsIgnoreCase("pvp")) {
					if(parts.length == 4) {
						if(p.hasPermission("towny.command.town.toggle.pvp")) {
						try {
							if(Integer.valueOf(parts[3]) > 0 && get_town(get_resident(p)).getAccount().getHoldingBalance() > Integer.valueOf(parts[3]) * pvp_cost && get_nation(get_town(get_resident(p))) != null) {
								if(at_war(get_nation(get_town(get_resident(p))))) {
								
										Integer hours = Integer.valueOf(parts[3]);
										Town t = get_town(get_resident(p));
										try {
											t.getAccount().pay(hours * pvp_cost, "Disabling PVP");
											pvp_time.put(get_town(get_resident(p)), hours);
											getConfig().set("pvp_timers." + t.getName(), hours);
											saveConfig();
										} catch (EconomyException h) {p.sendMessage(ChatColor.YELLOW + "You Cant Afford That");}

								} else {e.setCancelled(true); p.sendMessage(ChatColor.YELLOW + "That Command Is Only For Nations At War");}
							}
						} catch (NumberFormatException | EconomyException e1) {p.sendMessage(ChatColor.YELLOW + "You need to input a number, and be at war, and be in a nation to use that command");} 
					} else {e.setCancelled(true); p.sendMessage(ChatColor.YELLOW + "You dont have permission to do that");}
				}
				}
			}
			
		}
		
	}
	
	public boolean at_war(Nation n) {
		if(this.getConfig().getString("war." + n.getName()) != null) {return true;} else {return false;}
	}
	
	public boolean at_war_with(Nation a, Nation b) {
		if(this.getConfig().getString("war." + a.getName()) != null){
			String[] wars = this.getConfig().getString("war." + a.getName()).split(" ");
			for(String c : wars) {
				if(c.equalsIgnoreCase(b.getName())) {return true;}
			}
		}
		return false;
	}
	
	public String[] wars(Player p) {		
		String[] wars = this.getConfig().getString("war." + get_nation(get_town(get_resident(p))).getName()).split(" ");		
		return wars;	
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Bukkit.broadcastMessage("You Died");
		
		Player killed = e.getEntity().getPlayer();
		
		if(killed.getKiller() != null) {
			if(killed.getKiller() instanceof Player) {
				Player killer = e.getEntity().getKiller().getPlayer();
				
				
				
				if(get_resident(killed) != null) {
				Resident a = get_resident(killed);
				double p_loss;
				
				try {
					p_loss = a.getAccount().getHoldingBalance() * death_loss;
					a.getAccount().setBalance(a.getAccount().getHoldingBalance() - p_loss, "You died!");
					cmd("eco give " + killer.getName() + " " + p_loss * gain);
				} catch (EconomyException e1) {}
				if(get_resident(killer) != null) {
					Resident b = get_resident(killer);
					if(get_town(b) != null) {
						Town bt = get_town(b);
						if(get_nation(bt) != null) {
							Nation bn = get_nation(bt);
							if(get_town(a) != null) {
								Town at = get_town(a);
								if(get_nation(at) != null) {
									Nation an = get_nation(at);
									if(at_war_with(an, bn)) {
										lobbied.add(killed);
										try {
											if(at.getAccount().canPayFromHoldings(town_loss)) {at.getAccount().pay(town_loss, "someone died"); bt.getAccount().collect(town_loss * gain, "Someone died in an enemy town");} else {delete_town(at);}
											if(an.getAccount().canPayFromHoldings(nat_loss)) {an.getAccount().pay(nat_loss, "someone died"); bn.getAccount().collect(nat_loss * gain, "Someone died in an enemy nation");} else {delete_nation(an); end_war(0, an, null);}
										} catch (EconomyException e1) {e1.printStackTrace();}}}}}}}}}}
		Bukkit.broadcastMessage("thingermerbob");
	}
	
	@EventHandler
	public void or(PlayerRespawnEvent e) {
		final Player p = e.getPlayer();
		
		if(lobbied.contains(p)) {
			Bukkit.broadcastMessage("tped");
			tp(lobby_spawn, p, 0);      	
		} else {
        	
        	if(p.getBedSpawnLocation() != null && p.getBedSpawnLocation().getWorld() != Bukkit.getWorld("superflat")) {
        		Location bed = p.getBedSpawnLocation();
        		tp(bed, p, 1);
        		
        	} else {
        		if(get_town(get_resident(p)) != null) {
        			Town t = get_town(get_resident(p));
        			try {tp(t.getSpawn(), p, 2);} catch (TownyException e1) {}
        			
        		} else {
        			if(!rtp.containsKey(p)) {
        			Bukkit.broadcastMessage("RANDOM TELEPORT");
        			Random r = new Random();
        			Integer x = r.nextInt(100000) + -50000;
        			Integer z = r.nextInt(100000) + -50000;
        			Integer y = Bukkit.getWorld("world").getHighestBlockYAt(x, z);
        			Location loc = new Location(Bukkit.getWorld("world"), x, y, z);     			
        			y = Bukkit.getWorld("world").getHighestBlockYAt(x, z);
        			loc = new Location(Bukkit.getWorld("world"), x, y + 32, z);
        			rtp.put(p, loc);	            	
        			tp(loc, p, 3);
        			} else {
        				tp(rtp.get(p), p, 3);
        			}
        			
        		}}}	
		
	}
	
	public void tp(Location location, Player pl, Integer si) {
		
		Bukkit.broadcastMessage("TP " + si);
		
		final Location loc = location;
		final Player p = pl;
		final Integer s = si;
		
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
            	p.teleport(loc);
            	if(s == 0) {lobby_timer(p);}
            	if(s == 3) {p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 999));}
            	if(s == 4) {back.remove(p);}
            }      
        }, 5);
        
	}
	
	public void lobby_timer(Player pl) {
		final Player p = pl;
		
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
            	p.teleport(Bukkit.getWorld("superflat").getSpawnLocation());
            	lobbied.remove(p);
            }      
        }, 6000);
	}

	public Resident get_resident(Player p) {
		try {
			Resident r = td().getResident(p.getName());
			return r;
		} catch (NotRegisteredException e) {e.printStackTrace();}
		return null;
	}
	
	public Town get_town(Resident res) {
		try {
			Town t = td().getTown(res.getTown().getName());
			return t;
		} catch (NotRegisteredException e) {e.printStackTrace();}
		return null;
	}
	
	public Nation get_nation(Town t) {
		try {
			Nation n = td().getNation(t.getName());
			return n;
		} catch (NotRegisteredException e) {e.printStackTrace();}
		return null;
	}
	
	public void nat_pvp(Nation n, Boolean pvp) {
		List<Town> towns = n.getTowns();
		
		for(int a = 0; a < towns.size(); a++) {
			Town t = towns.get(a);
			t.setAdminEnabledPVP(pvp);
			td().saveTown(t);
			}
	}
	
	@EventHandler
	public void pnd(PreNewDayEvent e) {
		
		List<Town> towns = td().getTowns();
		
		for(int a = 0; a < towns.size(); a++) {
			Town town = towns.get(a);
			double taxes = towns.get(a).getTaxes();
			double upkeep = TownySettings.getTownUpkeepCost(towns.get(a));
			double cost = taxes+upkeep;
			
			try {
				if(cost > towns.get(a).getAccount().getHoldingBalance()) {
				List<TownBlock> tb = town.getTownBlocks();
				
				if(get_tb(tb) != null) {
					Double cost_left = cost;
				for(int b = 0; b < tb.size(); b++) {
					//add money to town until its down to only the homeblock, or has enough money to pay cost
					List<WorldCoord> sel = Collections.singletonList(get_tb(tb).getWorldCoord());
					new TownClaim(Towny.getPlugin(), null, town, sel, false, false, true).start();
					double sell_value = tb_cost * (Math.pow(interest, tb.size() - 1));
					double interest = sell_value - tb_cost;
					town.getAccount().collect(interest, "Unclaimed townblock to pay taxes and upkeep");
					cost_left -= sell_value;
					if(cost_left <= 0) {b = tb.size();}
				}
				}}
			} catch (EconomyException e1) {e1.printStackTrace();}
		}}
	
	@EventHandler
	public void nde(NewDayEvent e) {
		for(int a = 0; a < e.getFallenNations().size(); a++) {
			if(this.getConfig().getString("war." + e.getFallenNations().get(a)) != null) {end_war(1, null, e.getFallenNations().get(a));}
		}
	}
	
	@EventHandler
	public void dne(DeleteNationEvent e) {
		if(this.getConfig().getString("war." + e.getNationName()) != null) {end_war(1, null, e.getNationName());}
	}
	
	public void end_war(int s, Nation a, String aa) {
		
		//if s = 0 the nation a has been defeated by a loss of funds via death of players
		//if s = 1 the nation aa ran out of money on a new day, or deleted there nation
		
		if(s == 0) {
			String[] wars_a = this.getConfig().getString("war." + a.getName()).split(" ");
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "The Nation " + ChatColor.BLUE + a.getName() + ChatColor.DARK_RED + " Has Fallen From Battle");
			for(int x = 0; x < wars_a.length; x++) {
				conf_remove(wars_a[x], a.getName());
				Nation nat_2;
				try {
					nat_2 = td().getNation(wars_a[x]);				
					
				if(this.getConfig().getString("war." + wars_a[x]).equalsIgnoreCase("")) {this.getConfig().set("war." + wars_a[x], null);}
				if(!at_war(nat_2)) {nat_pvp(nat_2, false);}
				} catch (NotRegisteredException e) {Bukkit.broadcastMessage("That Nation Does Not Exist ID:0");}
			}
			this.getConfig().set("war." + a.getName(), null);
			
			this.saveConfig();
			delete_nation(a);
		}
		
		if(s == 1) {			
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "The Nation " + ChatColor.BLUE + a.getName() + ChatColor.DARK_RED + " Has Fallen From Battle");
			
			String[] wars_a = this.getConfig().getString("war." + aa).split(" ");
			for(int x = 0; x < wars_a.length; x++) {
				conf_remove(wars_a[x], aa);
				Nation nat_2;
				try {
					nat_2 = td().getNation(wars_a[x]);

					if(this.getConfig().getString("war." + wars_a[x]).equalsIgnoreCase("")) {this.getConfig().set("war." + wars_a[x], null);}
				if(!at_war(nat_2)) {nat_pvp(nat_2, false);}
				} catch (NotRegisteredException e) {Bukkit.broadcastMessage("That Nation Does Not Exist ID:0");}
			}
			this.getConfig().set("war." + aa, null);
			
			this.saveConfig();
		}
		
		if(s == 2) {
			try {
				Nation b = td().getNation(aa);
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Peace Has Been Declared Between " + ChatColor.BLUE + a.getName() + ChatColor.YELLOW + " and " + ChatColor.BLUE + b.getName());
				String wa = this.getConfig().getString("war." + a.getName());
				String wb = this.getConfig().getString("war." + b.getName());
				
				conf_remove(a.getName(), b.getName());
				conf_remove(b.getName(), a.getName());
				
				wa = this.getConfig().getString("war." + a.getName());
				wb = this.getConfig().getString("war." + b.getName());
				
				if(wa.isEmpty()) {this.getConfig().set("war." + a.getName(), null);} else {Bukkit.broadcastMessage(a.getName() + " " + this.getConfig().getString("war." + a.getName()));}
				if(wb.isEmpty()) {this.getConfig().set("war." + b.getName(), null);} else {Bukkit.broadcastMessage(b.getName() + " " + this.getConfig().getString("war." + b.getName()));}
				if(!at_war(a)) {nat_pvp(a, false);}
				if(!at_war(b)) {nat_pvp(b, false);}
				this.saveConfig();
			} catch (NotRegisteredException e) {e.printStackTrace();}
		}
		
		
	}
	
	public void delete_nation(Nation a) {
		cmd("ta nation " + a.getName() + " delete");
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
            	cmd("confirm");
            }
        }, 10);
	}
	
	public void delete_town(Town a) {
		cmd("ta town " + a.getName() + " delete");
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
            	cmd("confirm");
            }
        }, 10);
	}
	
	public void conf_remove(String part, String remove) {	
		
		String[] wars = this.getConfig().getString("war." + part).split(" ");
		String data = "";
		for(int a = 0; a < wars.length; a++) {
			if(!wars[a].equalsIgnoreCase(remove)) {data = wars[a] + " " + data;}
			
			data = data.trim();
			this.getConfig().set("war." + part, data);
			this.saveConfig();
			
			if(this.getConfig().getString("war." + part).split(" ").length == 0) {
				try {nat_pvp(td().getNation(part), false);} catch (NotRegisteredException e) {Bukkit.broadcastMessage("can't disable admin pvp");}}
		}
	}
	
	public void cmd(String _cmd) {
		getServer().dispatchCommand(getServer().getConsoleSender(), _cmd);
	}
	
	public TownyDataSource td() {
		return TownyAPI.getInstance().getDataSource();
	}
}
