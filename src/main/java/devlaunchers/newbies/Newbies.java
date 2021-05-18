package devlaunchers.newbies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devlaunchers.config.DevLauncherConfiguration;
import devlaunchers.items.DevLauncherItem;
import devlaunchers.plugin.DevLaunchersPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class Newbies extends DevLaunchersPlugin implements Listener {

  private List<ItemStack> starterPack = null;

  private void initializeStarterPack() {
    DevLauncherConfiguration section = getConfig().getConfigurationSection("newbies.starter-pack");
    if (section != null) {
      this.starterPack = new ArrayList<>();
      section
          .getValues(false)
          .forEach(
              (key, value) -> {
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
    // ItemStack testItem = getItem(DevLauncherItem.TEST_ITEM);
    saveDefaultConfig();
    initializeStarterPack();
    getServer().getPluginManager().registerEvents(this, this);
    getLogger().info("Starting Newbie's Plugin!");
  }

  @Override
  public void onDisable() {}

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    if (!player.hasPlayedBefore()) {
      getLogger().info("New player detected: " + player.getName());
      // this.starterPack.forEach((item) -> player.getInventory().addItem(new ItemStack(item)));
      for (ItemStack i : starterPack) {
        ItemStack it = new ItemStack(i);
        if (it.getType() == Material.WRITTEN_BOOK) {
          i.setItemMeta(extractActionsAndParse(it));
        }
        player.getInventory().addItem(it);
      }
    }
  }

  Pattern pattern = Pattern.compile("^\\((.*?)__(.*?)\\)");

  /**
   * Uses special format of text on pages to attach click events in book meta Start of line write in
   * this format (<Command Name>__<Command Value>)<Your Text Here>... List of valid commands:
   * OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD
   * Check config.yml for all commands and their sample use.
   *
   * @param it Given book ItemStack
   * @return Modified BookMeta containing events
   */
  private BookMeta extractActionsAndParse(ItemStack it) {
    BookMeta bm = (BookMeta) it.getItemMeta();
    int pageNo = 1;
    for (String page : bm.getPages()) {
      ComponentBuilder p;
      Matcher matcher = pattern.matcher(page);
      if (matcher.find()) {
        String c = page.replace(matcher.group(), "");
        p =
            new ComponentBuilder(c)
                .event(
                    new ClickEvent(ClickEvent.Action.valueOf(matcher.group(1)), matcher.group(2)));
        ;
      } else {
        p = new ComponentBuilder(page);
      }
      bm.spigot().setPage(pageNo, p.create());
      pageNo++;
    }
    return bm;
  }
}
