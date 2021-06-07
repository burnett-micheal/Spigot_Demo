package tutorial_pack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class tutorial_class extends JavaPlugin implements Listener{

	public ArrayList<Material> block_list = new ArrayList<Material>();
	//increase on the z axis
	//From start point +1x, +1z - From end point -1x, -1y, -1z
	public Location start_print = new Location(getServer().getWorld("superflat"), 128, 49, 16);
	
	public Location air_save_1 = new Location(getServer().getWorld("superflat"), 134, 49, -31);
	public Location air_save_2 = new Location(getServer().getWorld("superflat"), 165, 80, 0);
	
	public Location cube_save_1 = new Location(getServer().getWorld("superflat"), -80, 49, 32);
	public Location cube_save_2 = new Location(getServer().getWorld("superflat"), -49, 80, 63);
	
	public Location tut_save_1 = new Location(getServer().getWorld("superflat"), -80, 49, 80);
	public Location tut_save_2 = new Location(getServer().getWorld("superflat"), -49, 80, 111);
	
	public Location mob_arena_1 = new Location(getServer().getWorld("superflat"), -128, 49, 32);
	public Location mob_arena_2 = new Location(getServer().getWorld("superflat"), -97, 80, 63);
	
	public ArrayList<Location> used_space = new ArrayList<Location>();
	public ArrayList<Location> unused_space = new ArrayList<Location>();
	public HashMap<Player, Location> tut_loc = new HashMap<Player, Location>();
	public HashMap<Player, Integer> wave = new HashMap<Player, Integer>();
	
	//invite - invitee - invitor
	public HashMap<Player, Player> invite = new HashMap<Player, Player>();
	public HashMap<Player, String> town = new HashMap<Player, String>();
	public HashMap<Player, String> nation = new HashMap<Player, String>();
	public HashMap<Player, Integer> war = new HashMap<Player, Integer>();
	
	public ArrayList<Material> allow_break = new ArrayList<Material>();
	
	public int block_count = 0;
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		allow_break.add(Material.DIAMOND_ORE);
		allow_break.add(Material.GOLD_ORE);
		allow_break.add(Material.EMERALD_ORE);
		allow_break.add(Material.WHEAT);
		
		this.saveDefaultConfig();
	}
	
	@Override
	public void onDisable() {
		for(int a = 0; a < used_space.size(); a++) {
			Location pp = used_space.get(a);
			print(pp, air_save_1, air_save_2, false, true);
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		unprint(p);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		
		Player p = event.getPlayer();
		if(tut_loc.containsKey(p)) {
			event.setDropItems(false);
			event.setExpToDrop(0);
			
			if(!allow_break.contains(event.getBlock().getType())) {
			Block b = event.getBlock();
			Material mat = b.getType();
			String stringData = b.getBlockData().getAsString();
			place_block(b.getLocation(), mat, stringData);}
			
		}
	}
	
	public void place_block(Location loc, Material mat, String data) {        				
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {           				
            	loc.getBlock().setType(mat);
            	Block b = loc.getBlock();
            	b.setBlockData(Bukkit.getServer().createBlockData(data));

            }
        }, 1);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		
		
		if(event.getMessage().equalsIgnoreCase("/tutorial end") && tut_loc.get(p) != null) {
			unprint(p);
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p = (Player) sender;
		
		if(label.equalsIgnoreCase("next") && tut_loc.get(p) != null) {
			Location loc = tut_loc.get(p);
			mob_arena(loc, p);
		}
		
		if(label.equalsIgnoreCase("ma")) {
			
			if(tut_loc.get(p) != null) {
			Location loc = tut_loc.get(p);
			print(loc, mob_arena_1, mob_arena_2, false, false);
			mob_arena(loc, p);
			} else {
			new_area(p, mob_arena_1, mob_arena_2);
			}
			
		}
		
		if(label.equalsIgnoreCase("ts") && tut_loc.get(p) == null) {
			new_area(p, cube_save_1, cube_save_2);
		}
		
		if(label.equalsIgnoreCase("tm") && tut_loc.get(p) != null) {
			Location loc = tut_loc.get(p);
			Location pp = new Location(getServer().getWorld("superflat"), loc.getX(), 49, loc.getZ());
			print(pp, tut_save_1, tut_save_2, false, false);		
		}
		
		return false;		
	}
	
	public void new_area(Player p, Location loc_1, Location loc_2) {
		Location pp = start_print;
		if(unused_space.size() > 0) {pp = unused_space.get(0);}
		used_space.add(pp);
		if(unused_space.contains(pp)) {unused_space.remove(pp);}
		tut_loc.put(p, pp);
		start_print = new Location(getServer().getWorld("superflat"), pp.getX(), pp.getY(), pp.getZ() + 32);
		print(pp, loc_1, loc_2, true, true);
        player_tp(new Location(getServer().getWorld("superflat"), pp.getX() + 16, pp.getY() + 4, pp.getZ() + 16), p);       
        if(loc_1 == mob_arena_1) {mob_arena(pp, p);}
	}
	
	public void player_tp(Location loc, Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {    
		p.teleport(loc);
            }
        }, 5);
	}
	
	public void unprint(Player p) {
		if(tut_loc.get(p) != null) {
		Location loc = tut_loc.get(p);
		tut_loc.remove(p, loc);
		used_space.remove(loc);
		unused_space.add(loc);
		Location pp = loc;
		print(pp, air_save_1, air_save_2, false, true);
		}
	}
	
	public void print(Location print_point, Location start, Location end, Boolean ignore_dupe, Boolean no_wait) {
		Integer x_start = start.getBlockX();
		Integer x_end = end.getBlockX();
		Integer y_start = start.getBlockY();
		Integer y_end = end.getBlockY();
		Integer z_start = start.getBlockZ();
		Integer z_end = end.getBlockZ();
		
		
		
		Integer x_dis = Math.abs(x_start - x_end) + 1;
		Integer y_dis = Math.abs(y_start - y_end) + 1;
		Integer z_dis = Math.abs(z_start - z_end) + 1;
		int block_place_time = 1;
		
		for(int x = 0; x < x_dis; x++) {
			
			int x_mod = 0;
			if(x_end > x_start) {x_mod = 1;} else {x_mod = -1;}
			int x_ = x * x_mod;
			

			for(int z = 0; z < z_dis; z++) {
				
				int z_mod = 0;
				if(z_end > z_start) {z_mod = 1;} else {z_mod = -1;}
				int z_ = z * z_mod;				
				
				for(int y = 0; y < y_dis; y++) {
										
					int y_mod = 0;
					if(y_end > y_start) {y_mod = 1;} else {y_mod = -1;}
					int y_ = y * y_mod;
					
					Block copy_point = getServer().getWorld("superflat").getBlockAt(x_start + x_,y_start + y_,z_start + z_);
					Material mat = copy_point.getType();
					String stringData = copy_point.getBlockData().getAsString();
					
					Location print_loc = new Location(getServer().getWorld("superflat"), print_point.getX() + x_, print_point.getY() + y_, print_point.getZ() + z_);
					
					if(!mat.toString().contains("DOOR") && !mat.toString().contains("LILAC") && !mat.toString().contains("ROSE_BUSH") && !mat.toString().contains("PEONY") || mat.toString().contains("TRAPDOOR")) {
					
					if(mat != print_loc.getBlock().getType() || ignore_dupe == false) {
					
					Boolean fast_print = false;	
					if(print_loc.getBlock().getType() == Material.AIR && mat == Material.AIR) {fast_print = true;}	
						
					if(!no_wait && !fast_print) {
						
					block_count += 1;					
			        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			            public void run() {
			            																					
					print_loc.getBlock().setType(mat);
					print_loc.getBlock().setBlockData(Bukkit.getServer().createBlockData(stringData));
					
			            }
			        }, block_place_time * Math.round(block_count/200));			        
					
					} else {
										
						print_loc.getBlock().setType(mat);
						print_loc.getBlock().setBlockData(Bukkit.getServer().createBlockData(stringData));
						
					}
			        
					}
					
					} else {
					
					if(mat.toString().contains("DOOR") || mat.toString().contains("LILAC") || mat.toString().contains("ROSE_BUSH") || mat.toString().contains("PEONY")) {
						
						print_loc.getBlock().setType(mat);
				        Block doorBottomBlock = print_loc.getBlock();
				        Block doorUpBlock = doorBottomBlock.getRelative(BlockFace.UP);
				        BlockData blockData = getServer().createBlockData(mat);
				        Bisected bisected = (Bisected) blockData;

				        bisected.setHalf(Bisected.Half.BOTTOM);
				        doorBottomBlock.setBlockData(bisected, false);

				        bisected.setHalf(Bisected.Half.TOP);
				        doorUpBlock.setBlockData(bisected, false);
				        
				        print_loc.getBlock().setBlockData(Bukkit.getServer().createBlockData(stringData));
				        y += 1;
					}
					
					}
					
				}}}
		block_count = 0;
	}

	public void mob_arena(Location loc, Player p) {
		Integer _wave = 1;
		Float spawn_speed = 1f;
		Integer mob_count = 0;
		Location spawn_1 = new Location(getServer().getWorld("superflat"), loc.getX() + 1, loc.getY() + 1, loc.getZ() + 1);
		
		if(wave.get(p) != null) {wave.replace(p, wave.get(p) + 1); _wave = wave.get(p);} else {wave.put(p, _wave);}
		
		for(int a = 0; a < _wave * 15; a++) {
		Random r = new Random();
		Integer xr = r.nextInt(29);
		Integer zr = r.nextInt(29);
		
		Location spawn = new Location(getServer().getWorld("superflat"), spawn_1.getX() + xr, spawn_1.getY(), spawn_1.getZ() + zr);
		spawn_scheduler(_wave, spawn_speed, spawn, mob_count);
		mob_count += 1;
		}

		
	}
	
	public void spawn_scheduler(Integer _wave, Float spawn_speed, Location spawn, Integer mob_count) {
		
		EntityType spawning = EntityType.ZOMBIE;		
		
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {           																			
		
		Entity spawned = getServer().getWorld("superflat").spawnEntity(spawn, spawning);
		if(spawning == EntityType.ZOMBIE) {
		Zombie zombie = (Zombie)spawned;
		zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		}
		
            }
        }, (long) ((20 * spawn_speed) * mob_count));
		
		}
	
}
