package devlaunchers.newbies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
        if(!player.hasPlayedBefore() || true) { // TODO: Change
            getLogger().info("New player detected: " + player.getName());
            //this.starterPack.forEach((item) -> player.getInventory().addItem(new ItemStack(item)));
            for(ItemStack i : starterPack){
                ItemStack it = new ItemStack(i);
                if(it.getType() == Material.WRITTEN_BOOK){
                    i.setItemMeta(extractActionsAndParse(it));
                }
                player.getInventory().addItem(it);
            }
        }
    }

    // Function which parse for events in book pages, Conventional minecraft serializer doesnt support events
    // so this function can be used to have actions for you! To add an event just edit the page text and in the
    // start of line write in this format (<Command Name>__<Command Value>)<Your Text Here>...
    // Command Name is simply name of Enums in ClickEvent Action class like: ClickEvent.Action.<Command Name>,
    // List of valid commands: OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD
    // Mind the opening and closing brackets and always write this markup in start of line and see config.yml
    // for sample!!
    Pattern pattern = Pattern.compile("^\\((.*?)__(.*?)\\)");
    private BookMeta extractActionsAndParse(ItemStack it) {
        BookMeta bm = (BookMeta) it.getItemMeta();
        int pageNo = 1;
        for(String page : bm.getPages()){
            ComponentBuilder p;
            Matcher matcher = pattern.matcher(page);
            if (matcher.find())
            {
                String c = page.replace(matcher.group(), "");
                p = new ComponentBuilder(c).event(new ClickEvent(ClickEvent.Action.valueOf(matcher.group(1)), matcher.group(2)));;
            }
            else{
                p = new ComponentBuilder(page);
            }
            bm.spigot().setPage(pageNo, p.create());
            pageNo++;
        }
        return bm;
    }
}
