package Currency_Add_Package_01;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Currency_Add_Class_01 extends JavaPlugin implements Listener{
	
	public ArrayList<Location> placed_blocks = new ArrayList<Location>();
	public ArrayList<Material> money_blocks = new ArrayList<Material>();
	
	public File faucetfile;
	public FileConfiguration faucetcfg;
	
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		
		money_blocks.add(Material.STONE);
		money_blocks.add(Material.GRAVEL);
		money_blocks.add(Material.COAL_ORE);
		money_blocks.add(Material.IRON_ORE);
		money_blocks.add(Material.REDSTONE_ORE);
		money_blocks.add(Material.GOLD_ORE);
		money_blocks.add(Material.EMERALD_ORE);
		money_blocks.add(Material.LAPIS_ORE);
		money_blocks.add(Material.DIAMOND_ORE);
		money_blocks.add(Material.CACTUS);
		money_blocks.add(Material.SUGAR_CANE);
		money_blocks.add(Material.WHEAT);
		money_blocks.add(Material.CARROT);
		money_blocks.add(Material.POTATO);
		money_blocks.add(Material.NETHER_WART);
		money_blocks.add(Material.BEETROOT);
		
		int num = 1;
		
		for(int i = 0; i < num; i++) {
			if(getConfig().getString("saved_blocks." + i) != null) {
			String[] parts = getConfig().getString("saved_blocks." + i).split(",");
			Location loc = new Location(Bukkit.getServer().getWorld(parts[3]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
			loc.getBlock().setMetadata("Block_placed", new FixedMetadataValue(this, "true"));
			placed_blocks.add(loc);
			num += 1;
			} else {num = 0;}
		}
		
		create_faucetconfig();
		faucetcfg.addDefault("stone", "0.05");
		faucetcfg.addDefault("gravel", "2.5");
		faucetcfg.addDefault("coal_ore", "5");
		faucetcfg.addDefault("iron_ore", "25");
		faucetcfg.addDefault("redstone_ore", "50");
		faucetcfg.addDefault("gold_ore", "75");
		faucetcfg.addDefault("emerald_ore", "300");
		faucetcfg.addDefault("lapis_ore", "100");
		faucetcfg.addDefault("diamond_ore", "300");
		faucetcfg.addDefault("cactus", "10");
		faucetcfg.addDefault("sugar_cane", "1.25");
		faucetcfg.addDefault("wheat", "5");
		faucetcfg.addDefault("carrot", "5");
		faucetcfg.addDefault("potato", "5");
		faucetcfg.addDefault("nether_wart", "7.5");
		faucetcfg.addDefault("beetroot", "6.25");
		faucetcfg.options().copyDefaults(true);
		save_faucetconfig();
		this.saveDefaultConfig();
	}

	@Override
	public void onDisable() {
		for(int a = 0; a < placed_blocks.size(); a++) 
		{
			Location l = placed_blocks.get(a);
			this.getConfig().set("saved_blocks." + a, l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getWorld().getName());
		}
		
		this.saveConfig();
	}
	
	public void create_faucetconfig() {
		//abcdefghijk
		if(!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}
		
		faucetfile = new File(this.getDataFolder(), "faucets.yml");
		
		if(!faucetfile.exists()) {
			try {
				faucetfile.createNewFile();
			} catch(IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage("couldn't create the file");
			}
		}
		
		faucetcfg = YamlConfiguration.loadConfiguration(faucetfile);
		Bukkit.getServer().getConsoleSender().sendMessage("created the file");
		

	}
	
	public void save_faucetconfig() {
		try {
			faucetcfg.save(faucetfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reload() {
		faucetcfg = YamlConfiguration.loadConfiguration(faucetfile);
	}
	
	
	@EventHandler
	public void blockPlaced(BlockPlaceEvent event) {
	Block b = event.getBlock();
	if(money_blocks.contains(b.getType())) {
		placed_blocks.add(b.getLocation());
		b.setMetadata("Block_placed", new FixedMetadataValue(this, "true"));
	}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		
		
		Player player = (Player) event.getPlayer();
		Block block = event.getBlock();
		Random r = new Random();	
		int randomNum = r.nextInt(10000);
		
		if(money_blocks.contains(block.getType())) {
			
		}
		
		block.getMetadata("Block_placed");
		block.getMetadata("Claimed");
		if (!block.hasMetadata("Block_placed") && !block.hasMetadata("Claimed") && money_blocks.contains(block.getType())) {
			prot_check(block.getLocation(), block.getType(), block, randomNum, player);	
		}		
	}
	
	public void prot_check(Location loc, Material mat, Block block, Integer r, Player p) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {           				
            	
            	if(loc.getBlock().getType() != mat) {
            		
            		Integer m = 10000;
            		float stone = Math.round((Float.valueOf(faucetcfg.getString("stone"))/100) * 10000);
            		float gravel = Math.round((Float.valueOf(faucetcfg.getString("gravel"))/100) * 10000);
            		float coal_ore = Math.round((Float.valueOf(faucetcfg.getString("coal_ore"))/100) * 10000);
            		float iron_ore = Math.round((Float.valueOf(faucetcfg.getString("iron_ore"))/100) * 10000);
            		float redstone_ore = Math.round((Float.valueOf(faucetcfg.getString("redstone_ore"))/100) * 10000);
            		float gold_ore = Math.round((Float.valueOf(faucetcfg.getString("gold_ore"))/100) * 10000);
            		float emerald_ore = Math.round((Float.valueOf(faucetcfg.getString("emerald_ore"))/100) * 10000);
            		float lapis_ore = Math.round((Float.valueOf(faucetcfg.getString("lapis_ore"))/100) * 10000);
            		float diamond_ore = Math.round((Float.valueOf(faucetcfg.getString("diamond_ore"))/100) * 10000);
            		float cactus = Math.round((Float.valueOf(faucetcfg.getString("cactus"))/100) * 10000);
            		float sugar_cane = Math.round((Float.valueOf(faucetcfg.getString("sugar_cane"))/100) * 10000);
            		float wheat = Math.round((Float.valueOf(faucetcfg.getString("wheat"))/100) * 10000);
            		float carrot = Math.round((Float.valueOf(faucetcfg.getString("carrot"))/100) * 10000);
            		float potato = Math.round((Float.valueOf(faucetcfg.getString("potato"))/100) * 10000);
            		float nether_wart = Math.round((Float.valueOf(faucetcfg.getString("nether_wart"))/100) * 10000);
            		float beetroot = Math.round((Float.valueOf(faucetcfg.getString("beetroot"))/100) * 10000);          		
            		
            		float mb = stone;            		
            		if(mat == Material.STONE) {mb = stone; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.GRAVEL) {mb = gravel; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.COAL_ORE) {mb = coal_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.IRON_ORE) {mb = iron_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.REDSTONE_ORE) {mb = redstone_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.GOLD_ORE) {mb = gold_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.EMERALD_ORE) {mb = emerald_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.LAPIS_ORE) {mb = lapis_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.DIAMOND_ORE) {mb = diamond_ore; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.CACTUS) {mb = cactus; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.SUGAR_CANE) {mb = sugar_cane; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.WHEAT) {mb = wheat; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.CARROT) {mb = carrot; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.POTATO) {mb = potato; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.NETHER_WART) {mb = nether_wart; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            		if(mat == Material.BEETROOT) {mb = beetroot; if(mb > m) {add_bal(p, mb/m);} else {if(r <= mb) {add_bal(p, 1f);}}}
            	} else {
            		set_metadata(block);
            	}
            	
            }
        }, 20);
	}
	
	public void set_metadata(Block block) {
		block.setMetadata("Claimed", new FixedMetadataValue(this, "true"));
	}
	
	public void add_bal(Player player, Float amount) {
		getServer().dispatchCommand(getServer().getConsoleSender(), "eco give " + player.getName() + " " + amount );
	}
}