package su.pmchahawoman.russkayarybalka;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.List;
import java.util.Random;

public final class RusskayaRybalka extends JavaPlugin implements Listener {

    YamlConfiguration lootTable = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        loadConfig();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        //List list1 = lootTable.getList("Fish")
        String name = lootTable.getList("FishList").get(1).toString();
        //Bukkit.getLogger().info(String.valueOf(name));
    }

    public void loadConfig(){
        File configFile = new File(getDataFolder(), "loot.yml");
        if(!configFile.exists())
            saveResource("loot.yml", false);
        this.lootTable = YamlConfiguration.loadConfiguration(configFile);

        Bukkit.getLogger().info("[RusskayaRybalka] Лут загружен.");
    }

    public ItemStack getLootedItem(String ItemName, String playerName){
        ItemStack item = new ItemStack(Material.getMaterial(lootTable.getString(ItemName + ".material").toUpperCase()));

        Random r = new Random();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(setMessageColorCode(lootTable.getString(ItemName + ".display-name")));

        List a = lootTable.getList(ItemName + ".lore");
        a.add(setMessageColorCode("&fПоймал: " + playerName));

        if(lootTable.getString(ItemName + ".min-weight") != null
                || lootTable.getString(ItemName + ".max-weight") != null){
            double randomWeight = r.nextDouble(lootTable.getDouble(ItemName + ".min-weight"), lootTable.getDouble(ItemName + ".max-weight"));
            a.add(setMessageColorCode(String.format("&fВес: %.2f", randomWeight)));
        }

        if(lootTable.getString(ItemName + ".min-height") != null
                || lootTable.getString(ItemName + ".max-height") != null){
            double randomHeight = r.nextDouble(lootTable.getDouble(ItemName + ".min-weight"), lootTable.getDouble(ItemName + ".max-weight"));
            a.add(setMessageColorCode(String.format("&fДлинна: %.2f", randomHeight)));
        }

        a = a.stream().map(b -> setMessageColorCode((String)b)).toList();


        itemMeta.setLore(a);
        item.setItemMeta(itemMeta);

        return item;
    }

    public String chooseItemToDrop(){
        if(lootTable == null){
            Bukkit.getLogger().severe("[RusskayaRybalka] Файл с лутом пуст.");
            return null;
        }
        Random r = new Random();
        int itemID = r.nextInt(0, lootTable.getList("FishList").size()); //get item id
        return lootTable.getList("FishList").get(itemID).toString(); //get item name
    }

    public boolean itemDropChance(String itemName){
        double itemDropChance = lootTable.getDouble(itemName + ".chance-to-drop");
        Random r = new Random();
        if(r.nextDouble(0, 1.0) < itemDropChance){ //Если зарандомленный шанс меньше шанса выпадения, то дропай
            return true;
        }
        else
            return false;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e){
        //https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerFishEvent.State.html
        if(e.getState() == PlayerFishEvent.State.CAUGHT_FISH){
            if(e.getCaught() != null){

                String itemName = chooseItemToDrop();

                if(itemDropChance(itemName)){
                    Item d = (Item) e.getCaught();
                    d.setItemStack(getLootedItem(itemName, e.getPlayer().getName()));
                    e.getPlayer().sendMessage("Ты поймал: " + e.getCaught().getName());
                }
            }
        }
    }

    public String setMessageColorCode(String unformatedString){
        return ChatColor.translateAlternateColorCodes('&', unformatedString);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
