package devlaunchers.newbies;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Newbies extends JavaPlugin implements Listener {

    private Map<Material, Integer> starterPack = null;

    @Override
    public void onLoad() {
        super.onLoad();
        saveDefaultConfig();
    }

    private void initializeStarterPack() {
        ConfigurationSection section =
                getConfig().getConfigurationSection("newbies.starter-pack");
        if(section != null) {
            this.starterPack = new HashMap<>();
            section.getValues(false).forEach((key, value) -> {
                Material material = Material.getMaterial(key);
                if(material != null) {
                    try {
                        this.starterPack.put(material, (Integer) value);
                    } catch (ClassCastException exc) {
                        getLogger().warning("Invalid material amount: " + value + " ... skipping");
                    }
                } else {
                    getLogger().warning("Invalid material key: " + key + " ... skipping");
                }
            });
        } else if (this.starterPack == null) {
            // Initialize an empty starter pack for a missing configuration section, but don't
            // overwrite a previously successful initialization.
            this.starterPack = new HashMap<>();
        }
    }

    @Override
    public void onEnable() {
        initializeStarterPack();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new EntityInteractListener(), this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(!player.hasPlayedBefore()) {
            getLogger().info("New player detected: " + player.getName());
            this.starterPack.forEach((material, amount) ->
                    player.getInventory().addItem(new ItemStack(material, amount)));
        }
    }

    public final class EntityInteractListener implements Listener {
        @EventHandler
        public void onEntityInteracted(PlayerInteractEntityEvent event) {
            final Player player = event.getPlayer();
            final Entity target = event.getRightClicked();
            if (target instanceof Villager) {
                final Villager villager = (Villager) target;
                player.sendMessage("Targeting villager " + villager);
                ItemStack [] items = villager.getInventory().getContents();
                for (ItemStack item : items) {
                    if (item != null) {
                        player.sendMessage(villager + " has " + item);
                    }
                }
                villager.getInventory().addItem(new ItemStack(Material.BREAD, 64));
            }
        }
    }
}
