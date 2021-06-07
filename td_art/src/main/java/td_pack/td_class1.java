package td_pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import net.md_5.bungee.api.ChatColor;

public class td_class1 implements Listener, CommandExecutor {
	
	public HashMap<Player, Integer> sc = new HashMap<Player, Integer>();
	public HashMap<Player, HashMap<String, String>> shop_data = new HashMap<Player, HashMap<String, String>>();
	public HashMap<Player, HashMap<String, Location>> shop_loc = new HashMap<Player, HashMap<String, Location>>();
	
	static HashMap<Player, HashMap<Integer, Inventory>> shop_invs = new HashMap<Player, HashMap<Integer, Inventory>>();
	static HashMap<Player, Integer> shop_index = new HashMap<Player, Integer>();
	
	public HashMap<Inventory, Integer> bs_amount = new HashMap<Inventory, Integer>();
	public HashMap<Player, String> shop_id = new HashMap<Player, String>();
	
	static HashMap<Player, Integer> inv_id = new HashMap<Player, Integer>();
	
	public ArrayList<Inventory> shop_inventories = new ArrayList<Inventory>();
	
	public float shops_nearby = 50;
	
	static td_class plugin;
	public td_class1(td_class instance) {
		plugin = instance;
	}

	@EventHandler
	public void enable(PluginEnableEvent E) {
		Bukkit.broadcastMessage("stuffa");
		if(!E.getPlugin().getConfig().equals(plugin.getConfig())) {return;}
		Bukkit.broadcastMessage("stuffb");
	}
	
	public Entity get_frame(Location loc) {
		for(Entity e : loc.getWorld().getNearbyEntities(loc, 0.05, 0.05, 0.05)) {
			if(!(e instanceof ItemFrame)) {}else {
		Location eloc = e.getLocation();
		if(eloc.distance(loc) < 0.01) {return e;}
		Bukkit.broadcastMessage(eloc.getWorld().toString() + " " + eloc.getX() + " " + eloc.getY() + " " + eloc.getZ());	
		}
	}
		return null;
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent e){
		Entity a = e.getRightClicked();
		Player p = e.getPlayer();
		
		if(a instanceof ItemFrame) {
			ItemFrame b = (ItemFrame) a;
			ItemStack c = b.getItem();
			if(c.getType().equals(Material.AIR)) {cs(b, p);}
		}
	   }
	
	public void cs(ItemFrame b, Player c) {
		final ItemFrame a = b;
		final Player p = c;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
    			ItemStack i = ((ItemFrame) a).getItem();
    			
    			Block bl = a.getLocation().getBlock().getRelative(a.getFacing().getOppositeFace()).getLocation().getBlock();
    			
    			if(bl.getType() != Material.CHEST) {return;}
    			
    			p.sendMessage(ChatColor.YELLOW + "Are You Creating A Shop?");
    			p.sendMessage(ChatColor.YELLOW + "Input Y for yes N for no");
    			sc.put(p, 0);
    			
    			HashMap<String, Location> d = new HashMap<String, Location>();
    			d.put("LC", a.getLocation().getBlock().getRelative(a.getFacing().getOppositeFace()).getLocation());
    			d.put("LI", a.getLocation());
    			
    			shop_loc.put(p, d);
   			
    			plugin.saveConfig();
            }      
        }, 1);
	}
	
	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(sc.containsKey(p)) {
			if(sc.get(p) == 0) {
			if(e.getMessage().equalsIgnoreCase("y")) {p.sendMessage(ChatColor.YELLOW + "Are You Buying or Selling?"); p.sendMessage(ChatColor.YELLOW + "Input S for selling B for buying"); sc.put(p, 1); return;}
			if(e.getMessage().equalsIgnoreCase("n")) {sc.remove(p); p.sendMessage(ChatColor.YELLOW + "Shop Creation Cancelled"); return;}
			p.sendMessage(ChatColor.YELLOW + "Invalid message, Input Y or N");
			return;
			}
			
			if(sc.get(p) == 1) {
			if(e.getMessage().equalsIgnoreCase("s")) {
				p.sendMessage(ChatColor.YELLOW + "At What Price Are You Selling?");
				shop_data.put(p, hm_make("S", "0"));
				sc.put(p, 2); return;}
			
			if(e.getMessage().equalsIgnoreCase("b")) {
				p.sendMessage(ChatColor.YELLOW + "What Are You Willing To Pay?");
				shop_data.put(p, hm_make("B", "0"));
				sc.put(p, 2); return;}
			
			p.sendMessage(ChatColor.YELLOW + "Invalid message, Input S or B");
			return;
			}
			
			if(sc.get(p) == 2) {
    			try {
    				  Integer num = Integer.parseInt(e.getMessage());
    	  				if(shop_data.get(p).containsKey("S")) {p.sendMessage(ChatColor.YELLOW + "How Many Are You Selling At This Price?"); shop_data.get(p).put("S", e.getMessage()); sc.put(p, 3); return;}
    	  				if(shop_data.get(p).containsKey("B")) {p.sendMessage(ChatColor.YELLOW + "How Many Are You Buying At This Price?"); shop_data.get(p).put("B", e.getMessage()); sc.put(p, 3); return;}
    	  				} catch (NumberFormatException ex){ p.sendMessage(ChatColor.YELLOW + "Invalid message, Input A Number");}
			}
			
			if(sc.get(p) == 3) {
				try {
					  Integer num = Integer.parseInt(e.getMessage());
						shop_data.get(p).put("A", e.getMessage());
						create_shop(p);
					} catch (NumberFormatException ex){ p.sendMessage(ChatColor.YELLOW + "Invalid message, Input A Number");}
			}
		}
	}
	
	public void create_shop(Player p) {
		
		Location LC = shop_loc.get(p).get("LC");
		String lc = LC.getWorld().getName() + " " + LC.getBlockX() + " " + LC.getBlockY() + " " + LC.getBlockZ();
		Location LI = shop_loc.get(p).get("LI");
		String li = LI.getWorld().getName() + " " + LI.getX() + " " + LI.getY() + " " + LI.getZ();
		
		li = li.replace(".", "a");
		
		String shop = "shop." + li + ".";
		
		Set<String> v = shop_data.get(p).keySet();
		
		for(String c : v) {
			Bukkit.broadcastMessage(c);
		}
		
		if(shop_data.get(p).containsKey("B")) {plugin.getConfig().set(shop + "B", shop_data.get(p).get("B")); Bukkit.broadcastMessage("Buy price " + shop_data.get(p).get("B"));}
		
		if(shop_data.get(p).containsKey("S")) {plugin.getConfig().set(shop + "S", shop_data.get(p).get("S")); Bukkit.broadcastMessage("Sell price " + shop_data.get(p).get("S"));}
		
		plugin.getConfig().set(shop + "A", shop_data.get(p).get("A"));
		
		Bukkit.broadcastMessage(lc);
		Bukkit.broadcastMessage(li);
		
		plugin.getConfig().set(shop + "LC", lc);
		plugin.getConfig().set(shop + "LI", li);
		plugin.getConfig().set(shop + "P", p.getName());
		
		plugin.saveConfig();
		
		sc.remove(p);
		shop_data.remove(p);
		p.sendMessage(ChatColor.YELLOW + "Shop Created!");
		
	}
	
	@EventHandler
	public void onPlayerUse(PlayerInteractEntityEvent e){
		Player p = e.getPlayer();
		Entity E = e.getRightClicked();
		
		if(E instanceof ItemFrame) {
			
			if(plugin.get_resident(p) == null) {p.sendMessage(ChatColor.YELLOW + "You Must Be A Member Of A Town To Use Shops!");return;}
			
			p.sendMessage("You Right Clicked The Frame!");
			
			String li = get_id(E);
			
			Block bl = E.getLocation().getBlock().getRelative(E.getFacing().getOppositeFace()).getLocation().getBlock();
			
			if(bl.getType() == Material.CHEST) {			
			p.sendMessage(plugin.getConfig().getString("shop." + li + "." + "S"));
			p.sendMessage(plugin.getConfig().getString("shop." + li + "." + "A"));
			p.sendMessage(plugin.getConfig().getString("shop." + li + "." + "LC"));
			p.sendMessage(plugin.getConfig().getString("shop." + li + "." + "LI"));
			ItemStack item = ((ItemFrame) E).getItem();
			if(item != null) {
				if(plugin.getConfig().getString("shop." + li) != null) {
					shop_id.put(p, li);
					Inventory I = Bukkit.createInventory(p, 9);
					
					shop_inventories.add(I);
					
					new_shop_gui(E.getLocation(), item, I, li, p);
					p.openInventory(I);
					e.setCancelled(true);
				}
			}
			}
		}
		
	}
	
	public String get_id(Entity E) {
		Location LI = E.getLocation();
		String li = LI.getWorld().getName() + " " + LI.getX() + " " + LI.getY() + " " + LI.getZ();
		li = li.replace(".", "a");
		return li;
	}
	
	@EventHandler
	public void onPlayerdestroy(EntityDamageByEntityEvent e) {
		Bukkit.broadcastMessage("bop");
		
		if(e.getEntity() instanceof ItemFrame) {
			ItemFrame c = (ItemFrame) e.getEntity();
			String li = get_id(c);	
			String shop = "shop." + li;
			plugin.getConfig().set(shop, null);
			plugin.saveConfig();
			Bukkit.broadcastMessage("Broke ItemFrame");
			}
	}
	
	public void new_shop_gui(Location if_loc, ItemStack item, Inventory I, String li, Player p) {
		
		Boolean buying = false;
		Boolean selling = false;
		
		if(plugin.getConfig().getString("shop." + li + "." + "S") != null) {selling = true;}
		if(plugin.getConfig().getString("shop." + li + "." + "B") != null) {buying = true;}
		
		String bs = null;
		if(selling) {bs = "SELLING";}else {bs = "BUYING";}
		Integer price = null;
		
		if(selling) {
			String a = plugin.getConfig().getString("shop." + li + "." + "S");
			price = Integer.valueOf(a);
			}
		
		if(buying) {
			String a = plugin.getConfig().getString("shop." + li + "." + "B");
			price = Integer.valueOf(a);
			}
		
		String[] str = plugin.getConfig().getString("shop." + li + "." + "LC").split(" ");
		
		Block bl = Bukkit.getWorld(str[0]).getBlockAt(new Location(Bukkit.getWorld(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]), Integer.parseInt(str[3])));
		Chest shop = null;
		if(bl.getType() == Material.CHEST) { shop = (Chest) bl.getState();}
		if(shop == null) {return;}
		ItemStack[] shop_contents = shop.getInventory().getContents();
		
		Integer maxstack = item.getMaxStackSize();
		
		Integer space = shop.getInventory().getSize() * maxstack;
		Integer stock = 0;
		
		for(int a = 0; a < shop_contents.length; a++) {
			
			if(shop_contents[a] == null || shop_contents[a].getType() == Material.AIR) {} else {
			
			if(shop_contents[a].isSimilar(item)) {
				Integer amount = shop_contents[a].getAmount();
				stock += amount;
				space -= amount;
			} else {
			space -= maxstack;
			}}}
		
		Bukkit.broadcastMessage(ChatColor.YELLOW + "Space = " + String.valueOf(space));
		Bukkit.broadcastMessage(ChatColor.YELLOW + "Stock = " + String.valueOf(stock));
		
		for(int a = 0; a < 9; a++) {
			if(a == 0) {I.setItem(a, IC(Material.SUNFLOWER, ChatColor.GREEN + "BALANCE:", plugin.get_resident(p).getAccount().getHoldingFormattedBalance()));}
			if(a == 1) {I.setItem(a, item);}
			if(a == 2) {I.setItem(a, IC(Material.RED_STAINED_GLASS_PANE, ChatColor.DARK_RED + "-10", "null"));}
			if(a == 3) {I.setItem(a, IC(Material.BROWN_STAINED_GLASS_PANE, ChatColor.RED + "-1", "null"));}
			
			if(a == 4) {
				if(buying) {I.setItem(a, IC(Material.PAPER, "AMOUNT:", "0/" + space, "0"));}
				if(selling) {I.setItem(a, IC(Material.PAPER, "AMOUNT:", "0/" + stock, "0"));}
			}
			
			if(a == 5) {I.setItem(a, IC(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+1", "null"));}
			if(a == 6) {I.setItem(a, IC(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "+10", "null"));}
			
			if(a == 7) {I.setItem(a, IC(Material.CHEST, ChatColor.YELLOW + plugin.getConfig().getString("shop." + li + "." + "P"), ChatColor.YELLOW + bs + ": " + price));}
			
			if(a == 8) {I.setItem(a, IC(Material.RED_WOOL, ChatColor.RED + "EXIT", "null"));}
		}
	}
	
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e)
    {
		Integer isize = e.getInventory().getSize();
		Player p = (Player) e.getWhoClicked();
		
		Bukkit.broadcastMessage(String.valueOf(isize));
		if(isize == 9) {
		e.setCancelled(true);		
		
		Bukkit.broadcastMessage(String.valueOf(e.getRawSlot()));		
		
		Inventory i = e.getInventory();
		ItemStack item = i.getItem(1);
		if(!bs_amount.containsKey(i)) {bs_amount.put(i, 0);}
		
		Integer bs_cost = 0;
		String sid = "shop."+shop_id.get(p)+".";
		String[] ch_loc = plugin.getConfig().getString(sid + "LC").split(" ");
		Block ch = Bukkit.getWorld(ch_loc[0]).getBlockAt(new Location(Bukkit.getWorld(ch_loc[0]), Integer.parseInt(ch_loc[1]), Integer.parseInt(ch_loc[2]), Integer.parseInt(ch_loc[3])));
		Chest shop = (Chest) ch.getState();
		
		Boolean tb_fs = false;
		if(plugin.getConfig().getString(sid+"B") != null) {tb_fs = true; bs_cost=Integer.valueOf(plugin.getConfig().getString(sid+"B"));}
		if(plugin.getConfig().getString(sid+"S") != null) {tb_fs = false; bs_cost=Integer.valueOf(plugin.getConfig().getString(sid+"S"));}
		
		Integer st_sp = 2;
		if(tb_fs == true) {st_sp = 1;} else {st_sp = 0;}
		
		Bukkit.broadcastMessage("a " + String.valueOf(get_inv_data_0(item, shop.getInventory()).get(0)));
		Bukkit.broadcastMessage("b " + String.valueOf(get_inv_data_0(item, shop.getInventory()).get(1)));
		
		String temp = String.valueOf(bs_amount.get(i))+"/"+String.valueOf(get_inv_data_0(item, shop.getInventory()).get(st_sp));
		i.setItem(4, IC(Material.PAPER, "AMOUNT:", temp, String.valueOf(0)));
		String str = temp;
		Integer bs = bs_amount.get(i);
		String str0 = String.valueOf(bs);
		String str1 = str.split("/")[1];
		Integer int1 = Integer.valueOf(str1);
		
		if(e.getRawSlot() == 2) {if(bs >= 10) {bs -= 10;} else {bs = 0;} Bukkit.broadcastMessage("-10");}
		if(e.getRawSlot() == 3) {if(bs >= 1) {bs -= 1;} else {bs = 0;} Bukkit.broadcastMessage("-1");}
		if(e.getRawSlot() == 5) {if(bs <= int1 - 1) {bs += 1;} else {bs = int1;} Bukkit.broadcastMessage("+1");}
		if(e.getRawSlot() == 6) {if(bs <= int1 - 10) {bs += 10;} else {bs = int1;} Bukkit.broadcastMessage("+10");}
		if(e.getRawSlot() == 8) {p.closeInventory(); Bukkit.broadcastMessage("EXIT");}
		
		Integer cost = bs * bs_cost;
		
		bs_amount.put(i, bs);
		str0 = String.valueOf(bs);
		str = str0+"/"+str1;
		Bukkit.broadcastMessage(str);
		
		i.setItem(4, IC(Material.PAPER, "AMOUNT:", str, String.valueOf(cost)));
		
		
		
		if(e.getRawSlot() == 4) {
			
			Integer item_total = item.getAmount() * bs;
			
			ItemStack it = item.clone();
			it.setAmount(item_total);
			
			if(tb_fs == false) {
				Bukkit.broadcastMessage("Selling");
			try {
				if(plugin.get_resident(p).getAccount().getHoldingBalance() >= cost) {
					if(get_inv_data(item, p.getInventory()).get(1) >= item_total) {
						p.sendMessage("Enough space and money");
						plugin.get_resident(p).getAccount().pay(cost, "Shop Cost");
						p.getInventory().addItem(it);
						shop.getInventory().removeItem(it);
						p.closeInventory();
					} else {p.sendMessage("Not enough space and money");}
				} else {p.sendMessage("Not enough space and money");}
			} catch (EconomyException e1) {}
			
			Bukkit.broadcastMessage("COMPLETE TRANSACTION");
			} else {
				Bukkit.broadcastMessage("Buying");
				try {
				//check if player has all the items required
				Integer a = get_inv_data(item, p.getInventory()).get(0);
				if(a >= item_total) {} else {return;}
				//check if chest has enough space
				Integer b = get_inv_data_0(item, i).get(1);
				if(b >= item_total) {} else {return;}
				//put items in chest remove items from player
				p.getInventory().removeItem(it);
				shop.getInventory().addItem(it);
				//add money to players account
				plugin.get_resident(p).getAccount().collect(bs_cost * item_total, "payment");
				p.closeInventory();
				} catch (EconomyException e1) {}
			}
		}
		}
		
		if(isize == 54) {
			Bukkit.broadcastMessage(String.valueOf(e.getRawSlot()));
			
			Integer cs = e.getRawSlot();
			Boolean s = false;
			
			if(e.getRawSlot() == 50 && shop_invs.get(p).get(shop_index.get(p) + 1) != null) {
				Integer index = shop_index.get(p) + 1;
				p.openInventory(shop_invs.get(p).get(index));
				shop_index.put(p, index);
			}
			
			if(e.getRawSlot() == 48 && shop_invs.get(p).get(shop_index.get(p) - 1) != null) {
				Integer index = shop_index.get(p) - 1;
				p.openInventory(shop_invs.get(p).get(index));
				shop_index.put(p, index);
			}
			
			if(cs >= 10 && cs <= 16) {s = true;}
			if(cs >= 19 && cs <= 25) {s = true;}
			if(cs >= 28 && cs <= 34) {s = true;}
			if(cs >= 37 && cs <= 43) {s = true;}
			
			if(s && e.getInventory().getItem(cs) != null && e.getInventory().getItem(cs).getType() != Material.AIR) {
				Bukkit.broadcastMessage(String.valueOf(inv_id.get(p)));
				if(inv_id.get(p) == 1) {
				Bukkit.broadcastMessage("You Clicked A Shop");
				ItemStack i = e.getInventory().getItem(cs);
				Location if_loc = null;
				
				for(int a = 0; a < i.getItemMeta().getLore().size(); a++) {
					Bukkit.broadcastMessage(i.getItemMeta().getLore().get(a));
					if(i.getItemMeta().getLore().get(a).contains("Location: ")) {
						Bukkit.broadcastMessage("a");
						String loc = i.getItemMeta().getLore().get(a).split("Location: ")[1];
						Bukkit.broadcastMessage(loc);
						if_loc = get_loc(loc);
					}}
				
				Inventory I = Bukkit.createInventory(p, 9);
				i = ((ItemFrame) get_frame(if_loc)).getItem();
				String id = get_id(get_frame(if_loc));
				
				new_shop_gui(if_loc, i, I, id, p);
				shop_id.put(p, id);
				
				p.openInventory(I);
				}
				
				if(inv_id.get(p) == 2) {
					Bukkit.broadcastMessage("a");
					//Use Create Shops GUI and create an arraylist of all itemframes with that item material
					ArrayList<ItemFrame> a = new ArrayList<ItemFrame>();
					Material m = e.getInventory().getItem(cs).getType();
					for(String str : plugin.getConfig().getConfigurationSection("shop").getKeys(false)) {
						Location ploc = p.getLocation();
						Location loc = get_loc(str);
						Material mat = ((ItemFrame) get_frame(loc)).getItem().getType();
						
						if(mat == m) {a.add((ItemFrame) get_frame(get_loc(str)));}						
				}
					create_shops_gui(p, a);
					Bukkit.broadcastMessage("b");
			}
			}
    }
	}
	
	
	@EventHandler
	public void InvClose(InventoryCloseEvent e) {
		if(shop_inventories.contains(e.getInventory())) {
			if(bs_amount.containsKey(e.getInventory())) {
				shop_inventories.remove(e.getInventory());
				bs_amount.remove(e.getInventory());
			}}
		if(shop_invs.containsKey(e.getPlayer())) {shop_invs.remove(e.getPlayer()); shop_index.remove(e.getPlayer());}
		inv_id.remove(e.getPlayer());
	}
	
	public HashMap<String, String> hm_make(String b, String c) {
		HashMap<String, String> a = new HashMap<String, String>();
		a.put(b, c);
		return a;
	}
	
	public ItemStack IC(Material M, String N, String... L) {
		ItemStack item = new ItemStack(M, 1);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(N);
		if(!Arrays.asList(L).contains("null")) {meta.setLore(Arrays.asList(L));}
		item.setItemMeta(meta);
		
		return item;
	}
	
	public ItemStack I_C(ItemStack item, String N, String... L) {
		
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(N);
		if(!Arrays.asList(L).contains("null")) {meta.setLore(Arrays.asList(L));}
		item.setItemMeta(meta);
		
		return item;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		
		if(label.equalsIgnoreCase("shop")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("nearby")) {
					Bukkit.broadcastMessage("shop nearby");
					
					ArrayList<Location> close_shops = new ArrayList<Location>();
					ArrayList<ItemFrame> shops = new ArrayList<ItemFrame>();
					
					for(String str : plugin.getConfig().getConfigurationSection("shop").getKeys(false)) {
						Bukkit.broadcastMessage(str);
						Location ploc = p.getLocation();
						Location loc = get_loc(str);
						if(ploc.distance(loc) <= shops_nearby) {Bukkit.broadcastMessage(str + " " + "is close enough"); close_shops.add(loc);} else {Bukkit.broadcastMessage(str + " " + "isn't close enough");}
					}
					
					for(int a = 0; a < close_shops.size(); a++) {
						shops.add((ItemFrame)get_frame(close_shops.get(a)));
					}
					create_shops_gui(p, shops);
				}
				
				if(args[0].equalsIgnoreCase("all")) {
					HashMap<Material, ArrayList<String>> hm = new HashMap<Material, ArrayList<String>>();
					for(String str : plugin.getConfig().getConfigurationSection("shop").getKeys(false)) {
						Location ploc = p.getLocation();
						Location loc = get_loc(str);
						Material mat = ((ItemFrame) get_frame(loc)).getItem().getType();
						
						if(hm.containsKey(mat)) {
							hm.get(mat).add(str);
						} else {
							ArrayList<String> a = new ArrayList<String>();
							a.add(str);
							hm.put(mat, a);							
						}}
					ArrayList<ItemStack> items = new ArrayList<ItemStack>();
					for(Map.Entry<Material, ArrayList<String>> entry : hm.entrySet()) {
						ItemStack i = IC(entry.getKey(), entry.getKey().toString(), "null");
						items.add(i);
					}
					
					Inventory I = Bukkit.createInventory(p, 54);
					ArrayList<Inventory> I_list = new ArrayList<Inventory>();
					Integer loop = 1;
					
					for(int a = 0; a < loop; a++) {
						Bukkit.broadcastMessage(String.valueOf(items.size()));
						I_list.add(shop_inv_create(I, items, p));
						I = Bukkit.createInventory(p, 54);
						Bukkit.broadcastMessage(String.valueOf(items.size()));
						if(items.size() > 0) {loop += 1;} else {loop = 0;}
					}
					
					HashMap<Integer, Inventory> temp = new HashMap<Integer, Inventory>();
					for(int a = 0; a < I_list.size(); a++) {
						temp.put(a, I_list.get(a));
					}
					
					shop_invs.put(p, temp);
					shop_index.put(p, 0);
					
					p.openInventory(shop_invs.get(p).get(shop_index.get(p)));
					inv_id.put(p, 2);
				}
			}
		}
		
		return false;
	}
	
	public void create_shops_gui(Player p, ArrayList<ItemFrame> shops) {
		Inventory I = Bukkit.createInventory(p, 54);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		ArrayList<Inventory> I_list = new ArrayList<Inventory>();
		
		for(int a = 0; a < shops.size(); a++) {
			String id = get_id(shops.get(a));
			String P = plugin.getConfig().getString("shop." + id + ".P");
			String bs = null;
			String price = null;
			
			if(plugin.getConfig().getString("shop." + id + ".B") != null) 
			{bs = "BUYING"; price = plugin.getConfig().getString("shop." + id + ".B");}
			else 
			{bs = "SELLING";price = plugin.getConfig().getString("shop." + id + ".S");}
			
			String amount = plugin.getConfig().getString("shop." + id + ".A");
			
			String[] str = id.split(" ");
			id = str[0] + " " + (str[1] + " " + str[2] + " " + str[3]).replace("a", ".");
			items.add(I_C(shops.get(a).getItem(), P, bs, "Amount: " + amount, "Price: " + price, "Location: " + id));
		}
		
		Integer loop = 1;
		for(int a = 0; a < loop; a++) {
			Bukkit.broadcastMessage(String.valueOf(items.size()));
			I_list.add(shop_inv_create(I, items, p));
			I = Bukkit.createInventory(p, 54);
			Bukkit.broadcastMessage(String.valueOf(items.size()));
			if(items.size() > 0) {loop += 1;} else {loop = 0;}
		}
		
		HashMap<Integer, Inventory> temp = new HashMap<Integer, Inventory>();
		for(int a = 0; a < I_list.size(); a++) {
			temp.put(a, I_list.get(a));
		}
		
		shop_invs.put(p, temp);
		shop_index.put(p, 0);
		
		p.openInventory(shop_invs.get(p).get(shop_index.get(p)));
		inv_id.put(p, 1);
	}
	
	public Inventory shop_inv_create(Inventory I, ArrayList<ItemStack> items, Player p) {
		for(int a = 0; a < 54; a++) {
			if(a >= 0 && a <= 7) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a == 8) {I.setItem(a, IC(Material.RED_WOOL, ChatColor.RED + "EXIT", "null"));}
			if(a == 9) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a >= 10 && a <= 16) {if(items.size() > 0) {I.setItem(a, items.get(0)); items.remove(0);}}			
			if(a == 17 || a == 18) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a >= 19 && a <= 25) {if(items.size() > 0) {I.setItem(a, items.get(0)); items.remove(0);}}
			if(a == 26 || a == 27) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a >= 28 && a <= 34) {if(items.size() > 0) {I.setItem(a, items.get(0)); items.remove(0);}}
			if(a == 35 || a == 36) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a >= 37 && a <= 43) {if(items.size() > 0) {I.setItem(a, items.get(0)); items.remove(0);}}
			if(a >= 44 && a <= 47) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
			if(a == 48) {I.setItem(a, IC(Material.PAPER, ChatColor.YELLOW + "Previous Page"));}
			if(a == 49) {I.setItem(a, IC(Material.SUNFLOWER, ChatColor.GREEN + "BALANCE:", plugin.get_resident(p).getAccount().getHoldingFormattedBalance()));}
			if(a == 50) {I.setItem(a, IC(Material.PAPER, ChatColor.YELLOW + "Next Page"));}
			if(a >= 51 && a <= 53) {I.setItem(a, IC(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "|", "null"));}
		}
		return I;
	}
	
	public ArrayList<Integer> get_inv_data(ItemStack item, Inventory i) {
		ItemStack[] shop_contents = i.getStorageContents();
		
		Integer maxstack = item.getMaxStackSize();
		
		Integer space = shop_contents.length * maxstack;
		Integer stock = 0;
		
		for(int a = 0; a < shop_contents.length; a++) {
			
			if(shop_contents[a] == null || shop_contents[a].getType() == Material.AIR) {} else {
			
			if(shop_contents[a].isSimilar(item)) {
				Integer amount = shop_contents[a].getAmount();
				stock += amount;
				space -= amount;
			} else {
			space -= maxstack;
			}}}
		
		ArrayList<Integer> id = new ArrayList<Integer>();
		id.add(stock);
		id.add(space);
		return id;
	}
	
	public ArrayList<Integer> get_inv_data_0(ItemStack item, Inventory i) {
		ItemStack[] shop_contents = i.getContents();
		
		Integer maxstack = item.getMaxStackSize();
		
		Integer space = shop_contents.length * maxstack;
		Integer stock = 0;
		
		for(int a = 0; a < shop_contents.length; a++) {
			
			if(shop_contents[a] == null || shop_contents[a].getType() == Material.AIR) {} else {
			
			if(shop_contents[a].isSimilar(item)) {
				Integer amount = shop_contents[a].getAmount();
				stock += amount;
				space -= amount;
			} else {
			space -= maxstack;
			}}}
		
		ArrayList<Integer> id = new ArrayList<Integer>();
		id.add(stock);
		id.add(space);
		return id;
	}
	
	public Location get_loc(String str) {
		String[] S = str.split(" ");
		String ST = S[1] + " " + S[2] + " " + S[3];
		ST = ST.replace("a", ".");
		String STR = S[0] + " " + ST;
		String[] s = STR.split(" ");
		Location loc = new Location(Bukkit.getWorld(s[0]), Float.valueOf(s[1]), Float.valueOf(s[2]), Float.valueOf(s[3]));
		return loc;
	}
	
}
