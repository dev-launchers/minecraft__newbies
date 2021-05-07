package devlaunchers.newbies;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Newbies extends JavaPlugin implements Listener {

    private List<ItemStack> starterPack = null;

    private void initializeStarterPack() {
        ConfigurationSection section =
                getConfig().getConfigurationSection("newbies.starter-pack");
        if(section != null) {
            this.starterPack = new ArrayList<>();
            section.getValues(false).forEach((key, value) -> {
                if (value instanceof ItemStack) {
                    this.starterPack.add((ItemStack) value);
                } else {
                    getLogger().warning("Invalid material: " + key + " ... skipping");
                }
            });
        } else if (this.starterPack == null) {
            // Initialize an empty starter pack for a missing configuration section, but don't
            // overwrite a previously successful initialization.
            this.starterPack = new ArrayList<>();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        initializeStarterPack();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(!player.hasPlayedBefore()) {
            getLogger().info("New player detected: " + player.getName());
            this.starterPack.forEach((item) -> player.getInventory().addItem(new ItemStack(item)));
        }
    }
}
