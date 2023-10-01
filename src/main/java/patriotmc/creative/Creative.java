package patriotmc.creative;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Date;

import net.minecraft.server.v1_12_R1.PacketPlayInBlockPlace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.WorldCreator;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Level;

class Var implements java.io.Serializable {
    Object value = null;

    public Var() {
    }

    public Var(Object value) {
        this.value = value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        if (value instanceof Var) {
            Var v = (Var) value;
            return ((Var) value).getValue();
        } else {
            return value;
        }
    }
}

class GameCode implements java.io.Serializable {
    String code = "";
    int worldID = 0;
    HashMap<String, Var> variable = new HashMap<>();

    public GameCode(String code, int worldID) {
        this.code = code;
        this.worldID = worldID;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setWorldID(int worldID) {
        this.worldID = worldID;
    }


}

class GameWorld implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    public int id;
    public String name;
    public String owner;

    public int mode;
    // 0 - build; 1 - play;

    public int status;
    // 0 - Closed to everyone except the creator.
    // 1 - Closed to everyone except the creator and whitelisted players.
    // 2 - open

    public List<String> unique_visitors;
    public List<String> likes;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public double spawnYaw;
    public double spawnPitch;
    public String spawnName;

    public List<String> whitelistedPlayers = new ArrayList<>();
    public List<String> blacklistedPlayers = new ArrayList<>();
    public GameCode code;

    public GameWorld(int id, String name, String owner, int mode, int status, Location spawn) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.mode = mode;
        this.status = status;
        this.spawnX = spawn.getX();
        this.spawnY = spawn.getY();
        this.spawnZ = spawn.getZ();
        this.spawnYaw = spawn.getYaw();
        this.spawnPitch = spawn.getPitch();
        this.spawnName = spawn.getWorld().getName();
        this.unique_visitors = new ArrayList<>();
        this.likes = new ArrayList<>();
        code = new GameCode("", id);

    }

    public String toString() {
        return "id: " + id + " name: " + name + " owner: " + owner + " mode: " + mode + " status: " + status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}

class BannedPlayer {
    public String name;
    public long time;
    public String reason;

    public BannedPlayer(String name, long time, String reason) {
        this.name = name;
        this.time = time;
        this.reason = reason;
    }
}

class MutedPlayer {
    public String name;
    public long time;
    public String reason;

    public MutedPlayer(String name, long time, String reason) {
        this.name = name;
        this.time = time;
        this.reason = reason;
    }
}

class BannedIp {
    public String ip;
    public long time;
    public String reason;

    public BannedIp(String ip, long time, String reason) {
        this.ip = ip;
        this.time = time;
        this.reason = reason;
    }
}

public class Main extends JavaPlugin implements Listener {

    public static boolean isNumeric(String string) {
        int intValue;


        if (string == null || string.equals("")) {
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    HashMap<String, Location> playerDeathLocations = new HashMap<>();
    HashMap<String, Integer> playerCurGamesPage = new HashMap<>();

    HashMap<String, GameWorld> playerCurGameWorld = new HashMap<>();
    HashMap<String, Boolean> playerMustEnterNameOfWorld = new HashMap<>();
    HashMap<String, Location> playerCodeCurClickedSign = new HashMap<>();

    HashMap<Integer, GameWorld> worlds = new HashMap<>();
    List<GameWorld> sortedWorlds = new ArrayList<>();
    World spawn_world;

    ItemStack menu_item = new ItemStack(Material.COMPASS);
    ItemMeta menu_item_meta = menu_item.getItemMeta();

    ItemStack diamond_item = new ItemStack(Material.DIAMOND);
    ItemMeta diamond_inv_item_meta = diamond_item.getItemMeta();

    Inventory menu_inv;
    Inventory creative_inv;

    ItemStack myGames_item = new ItemStack(Material.COMMAND);
    ItemMeta myGames_item_meta = myGames_item.getItemMeta();

    ItemStack myGames2_item = new ItemStack(Material.COMMAND_CHAIN);
    ItemMeta myGames2_item_meta = myGames_item.getItemMeta();

    ItemStack createWorld_item = new ItemStack(Material.GLASS);
    ItemMeta createWorld_item_meta = createWorld_item.getItemMeta();

    ItemStack settingsItem = new ItemStack(Material.BOOK);

    ItemStack settingsItem_status0 = new ItemStack(Material.BARRIER);
    ItemStack settingsItem_status1 = new ItemStack(Material.STRUCTURE_VOID);
    ItemStack settingsItem_status2 = new ItemStack(Material.DIAMOND);
    ItemStack settingsItem_mode0 = new ItemStack(Material.BRICK);
    ItemStack settingsItem_mode1 = new ItemStack(Material.EMERALD);
    ItemStack settingsItem_spawn = new ItemStack(Material.ENDER_PEARL);
    ItemStack settingsItem_whitelist = new ItemStack(Material.WOOL);
    ItemStack settingsItem_tpToWorld = new ItemStack(Material.GOLD_BLOCK);
    ItemStack settingsItem_tpToCode = new ItemStack(Material.COMMAND);
    ItemStack settingsItem_name = new ItemStack(Material.NAME_TAG);
    ItemStack settingsItem_blacklist = new ItemStack(Material.COAL_BLOCK);
    ItemStack settingsItem_gamerule = new ItemStack(Material.FLINT);
    ItemMeta settingsItem_status0_meta = settingsItem_status0.getItemMeta();
    ItemMeta settingsItem_status1_meta = settingsItem_status0.getItemMeta();
    ItemMeta settingsItem_status2_meta = settingsItem_status0.getItemMeta();
    ItemMeta settingsItem_spawn_meta = settingsItem_spawn.getItemMeta();
    ItemMeta settingsItem_mode0_meta = settingsItem_mode0.getItemMeta();
    ItemMeta settingsItem_mode1_meta = settingsItem_mode1.getItemMeta();
    ItemMeta settingsItem_whitelist_meta = settingsItem_whitelist.getItemMeta();
    ItemMeta settingsItem_tpToWorld_meta = settingsItem_tpToWorld.getItemMeta();
    ItemMeta settingsItem_tpToCode_meta = settingsItem_tpToCode.getItemMeta();
    ItemMeta settingsItem_name_meta = settingsItem_name.getItemMeta();
    ItemMeta settingsItem_blacklist_meta = settingsItem_blacklist.getItemMeta();
    ItemMeta settingsItem_gamerule_meta = settingsItem_gamerule.getItemMeta();

    ItemStack codeItem_event = new ItemStack(Material.DIAMOND_BLOCK);
    ItemMeta codeItem_event_meta = codeItem_event.getItemMeta();
    ItemStack codeItem_player = new ItemStack(Material.COBBLESTONE);
    ItemMeta codeItem_player_meta = codeItem_player.getItemMeta();
    ItemStack codeItem_If = new ItemStack(Material.OBSIDIAN);
    ItemMeta codeItem_If_meta = codeItem_If.getItemMeta();
    ItemStack codeItem_var = new ItemStack(Material.IRON_BLOCK);
    ItemMeta codeItem_var_meta = codeItem_var.getItemMeta();
    ItemStack codeItem_eventControl = new ItemStack(Material.COAL_BLOCK);
    ItemMeta codeItem_eventControl_meta = codeItem_eventControl.getItemMeta();
    ItemStack codeItem_varChooser = new ItemStack(Material.IRON_INGOT);
    ItemMeta codeItem_varChooser_meta = codeItem_varChooser.getItemMeta();
    ItemStack codeItem_varText = new ItemStack(Material.BOOK);
    ItemMeta codeItem_varText_meta = codeItem_varText.getItemMeta();
    ItemStack codeItem_varInt = new ItemStack(Material.SLIME_BALL);
    ItemMeta codeItem_varInt_meta = codeItem_varInt.getItemMeta();
    ItemStack codeItem_varVar = new ItemStack(Material.MAGMA_CREAM);
    ItemMeta codeItem_varVar_meta = codeItem_varVar.getItemMeta();
    ItemStack codeItem_varBool = new ItemStack(Material.WATCH);
    ItemMeta codeItem_varBool_meta = codeItem_varBool.getItemMeta();
    ItemStack codeItem_varEvent = new ItemStack(Material.APPLE);
    ItemMeta codeItem_varEvent_meta = codeItem_varEvent.getItemMeta();

    ItemStack codeItem_EventJoin = new ItemStack(Material.EMERALD);
    ItemMeta codeItem_EventJoin_meta = codeItem_EventJoin.getItemMeta();
    ItemStack codeItem_EventExit = new ItemStack(Material.REDSTONE);
    ItemMeta codeItem_EventExit_meta = codeItem_EventExit.getItemMeta();
    ItemStack codeItem_EventDamage = new ItemStack(Material.IRON_SWORD);
    ItemMeta codeItem_EventDamage_meta = codeItem_EventDamage.getItemMeta();
    ItemStack codeItem_EventBlockBreak = new ItemStack(Material.COBBLESTONE);
    ItemMeta codeItem_EventBlockBreak_meta = codeItem_EventBlockBreak.getItemMeta();
    ItemStack codeItem_EventBlockPlace = new ItemStack(Material.STONE);
    ItemMeta codeItem_EventBlockPlace_meta = codeItem_EventBlockPlace.getItemMeta();

    ItemStack codeItem_PlayerControlGamemode = new ItemStack(Material.WORKBENCH);
    ItemMeta codeItem_PlayerControlGamemode_meta = codeItem_PlayerControlGamemode.getItemMeta();
    ItemStack codeItem_PlayerControlSendmessage = new ItemStack(Material.BOOK);
    ItemMeta codeItem_PlayerControlSendmessage_meta = codeItem_PlayerControlSendmessage.getItemMeta();
    ItemStack codeItem_VarControlSet = new ItemStack(Material.GOLD_INGOT);
    ItemMeta codeItem_VarControlSet_meta = codeItem_VarControlSet.getItemMeta();
    ItemStack codeItem_VarControlGetPlayerName = new ItemStack(Material.NAME_TAG);
    ItemMeta codeItem_VarControlGetPlayerName_meta = codeItem_VarControlGetPlayerName.getItemMeta();

    ItemStack codeItem_getPlayer = new ItemStack(Material.APPLE);
    ItemMeta codeItem_getPlayer_meta = codeItem_getPlayer.getItemMeta();
    ItemStack codeItem_getEntity = new ItemStack(Material.APPLE);
    ItemMeta codeItem_getEntity_meta = codeItem_getPlayer.getItemMeta();
    ItemStack codeItem_getDamage = new ItemStack(Material.APPLE);
    ItemMeta codeItem_getDamage_meta = codeItem_getDamage.getItemMeta();
    ItemStack codeItem_getFinalDamage = new ItemStack(Material.APPLE);
    ItemMeta codeItem_getFinalDamage_meta = codeItem_getFinalDamage.getItemMeta();

    ItemStack codeItem_varGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 1);
    ItemMeta codeItem_varGlass_meta = codeItem_varGlass.getItemMeta();
    ItemStack codeItem_emptyGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 7);
    ItemMeta codeItem_emptyGlass_meta = codeItem_emptyGlass.getItemMeta();
    ItemStack codeItem_valueGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 0);
    ItemMeta codeItem_valueGlass_meta = codeItem_valueGlass.getItemMeta();
    ItemStack codeItem_varPlayerGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0, (byte) 1);
    ItemMeta codeItem_varPlayerGlass_meta = codeItem_varGlass.getItemMeta();

    HashMap<String, String[]> suffixes = new HashMap<>();
    HashMap<String, String> curSuffix = new HashMap<>();

    List<BannedPlayer> bannedPlayers = new ArrayList<>();
    List<BannedIp> bannedIps = new ArrayList<>();
    List<MutedPlayer> mutedPlayers = new ArrayList<>();
    HashMap<String, String> ranks = new HashMap<>();
    public int count_of_pl_worlds = 0;

    @EventHandler
    public void preLogin(AsyncPlayerPreLoginEvent e) {
        for (int i = 0; i < bannedPlayers.size(); i++) {
            BannedPlayer b = bannedPlayers.get(i);
            if (b.name.equals(e.getName())) {
                Date date = new Date();
                double time = b.time - date.getTime() / 1000;
                if (time < 0) {
                    bannedPlayers.remove(b);
                    break;
                }
                int d = (int) Math.floor(time / 86400);
                time -= d * 86400;
                int h = (int) Math.floor(time / 3600) % 24;
                time -= h * 3600;
                int m = (int) Math.floor(time / 60) % 60;
                time -= m * 60;
                int s = (int) time % 60;

                e.setKickMessage("§6Вы забанены\n§fПричина: §6" + b.reason + "\n§fОсталось: §a" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд\n§6Если вы считаете, что наказание выдано неверно,\n§6оставьте заявку на разбан:\n§9https://discord.gg/UJk7GxN59w");
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            }
        }
        for (int i = 0; i < bannedIps.size(); i++) {
            BannedIp b = bannedIps.get(i);
            if (b.ip.equals(e.getAddress().getHostAddress())) {
                Date date = new Date();
                double time = b.time - date.getTime() / 1000;
                if (time < 0) {
                    bannedIps.remove(b);
                    break;
                }
                int d = (int) Math.floor(time / 86400);
                time -= d * 86400;
                int h = (int) Math.floor(time / 3600) % 24;
                time -= h * 3600;
                int m = (int) Math.floor(time / 60) % 60;
                time -= m * 60;
                int s = (int) time % 60;

                e.setKickMessage("§6Вы забанены\n§fПричина: §6" + b.reason + "\n§fОсталось: §a" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд\n§6Если вы считаете, что наказание выдано неверно,\n§6оставьте заявку на разбан:\n§9https://discord.gg/UJk7GxN59w");
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            }
        }
    }

    @Override
    public void onEnable() {


        File creativeData_file = new File("creativeData.txt");
        try {
            creativeData_file.createNewFile();
            Scanner scanner = new Scanner(creativeData_file);
            String next;
            String key;
            String value;
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                key = next.split(": ")[0];
                value = next.split(": ")[1];
                if (key.equals("count")) {
                    count_of_pl_worlds = Integer.parseInt(value);
                }
            }
        } catch (IOException ex) {
            //
        }
        try {
            for (int i = 0; i < count_of_pl_worlds; i++) {
                File worldData_file = new File("" + i + "worldData.ser");
                FileInputStream fis = new FileInputStream(worldData_file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                GameWorld gw = (GameWorld) ois.readObject();
                if (gw.unique_visitors == null) {
                    gw.unique_visitors = new ArrayList<>();
                }
                if (gw.likes == null) {
                    gw.likes = new ArrayList<>();
                }
                worlds.put(i, gw);

                fis.close();
                ois.close();

            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        try {
            File newFile = new File("suffixes.mmap");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            Scanner scanner = new Scanner(newFile);
            String next;
            String texts[];
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                texts = next.split(" ");
                String name = texts[0];
                String texts2[] = new String[texts.length - 2];
                for (int i = 2; i < texts.length; i++) {
                    texts2[i - 2] = texts[i];
                }
                curSuffix.put(name, texts[1]);
                suffixes.put(name, texts2);
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("banned.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            Scanner scanner = new Scanner(newFile);
            String next;
            String texts[];
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                texts = next.split(" ");
                bannedPlayers.add(new BannedPlayer(texts[0], new Long(texts[1]), texts[2]));
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("muted.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            Scanner scanner = new Scanner(newFile);
            String next;
            String texts[];
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                texts = next.split(" ");
                mutedPlayers.add(new MutedPlayer(texts[0], new Long(texts[1]), texts[2]));
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("bannedIp.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            Scanner scanner = new Scanner(newFile);
            String next;
            String texts[];
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                texts = next.split(" ");
                bannedIps.add(new BannedIp(texts[0], new Long(texts[1]), texts[2]));
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("ranks.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            Scanner scanner = new Scanner(newFile);
            String next;
            String texts[];
            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                texts = next.split(" ");
                ranks.put(texts[0], texts[1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        spawn_world = getServer().getWorld("spawn");


        menu_item_meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Меню");
        menu_item.setItemMeta(menu_item_meta);

        menu_inv = Bukkit.createInventory(null, 45, "Меню");


        creative_inv = Bukkit.createInventory(null, (int) (54), "§d§lCreative§b§l+");

        diamond_inv_item_meta.setDisplayName("§d§lCreative+");
        diamond_inv_item_meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        diamond_inv_item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        diamond_inv_item_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        diamond_item.setItemMeta(diamond_inv_item_meta);
        menu_inv.setItem(22, diamond_item);


        myGames_item_meta.setDisplayName("§2§lМои игры");
        myGames_item_meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        myGames_item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        myGames_item_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        myGames_item.setItemMeta(myGames_item_meta);
        creative_inv.setItem(49, myGames_item);

        myGames2_item_meta.setDisplayName("§2§lМои игры");
        myGames2_item_meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        myGames2_item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        myGames2_item_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        myGames2_item.setItemMeta(myGames2_item_meta);


        createWorld_item_meta.setDisplayName("§e§lСоздать мир");
        createWorld_item_meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        createWorld_item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        createWorld_item_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        createWorld_item.setItemMeta(createWorld_item_meta);


        getServer().getPluginManager().registerEvents(this, this);

        spawn_world.setSpawnLocation(new Location(spawn_world, 23.5, 13, -22.5, -180, 0));
        spawn_world.setKeepSpawnInMemory(true);
        this.getCommand("ad").setExecutor(new CommandAd());
        this.getCommand("games").setExecutor(new CommandGames());
        this.getCommand("donate").setExecutor(new CommandDonate());
        this.getCommand("tp").setExecutor(new CommandTp());
        this.getCommand("spawn").setExecutor(new CommandSpawn());
        this.getCommand("m").setExecutor(new CommandMsg());
        this.getCommand("suffixes").setExecutor(new CommandSuffix());
        this.getCommand("setWorld").setExecutor(new CommandSetWorld());
        this.getCommand("like").setExecutor(new CommandLike());
        this.getCommand("ban").setExecutor(new CommandBan());
        this.getCommand("pardon").setExecutor(new CommandPardon());
        this.getCommand("ban-ip").setExecutor(new CommandBanIp());
        this.getCommand("mute").setExecutor(new CommandMute());
        this.getCommand("unmute").setExecutor(new CommandUnmute());
        this.getCommand("rank").setExecutor(new CommandRank());
        this.getCommand("ttc").setExecutor(new CommandTTC());
        this.getCommand("suffixes").setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
                List<String> args = new ArrayList<>();
                args.add("");
                args.add("give <player> <suffix>");
                args.add("remove <player> <suffix>");
                args.add("list <player>");
                return args;
            }
        });
        ItemMeta im = settingsItem.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Настройки");
        settingsItem.setItemMeta(im);

        List<String> lore = new ArrayList<>();

        settingsItem_status0_meta.setDisplayName(ChatColor.GREEN + "Статус");
        lore.add("");
        lore.add("§6§l ♦ §fЗакрыт для всех кроме создателя.");
        lore.add("§6§l ◊ §fЗакрыт для всех кроме созателя и игроков из белого списка.");
        lore.add("§6§l ◊ §fОткрыт.");
        lore.add("");
        settingsItem_status0_meta.setLore(lore);
        settingsItem_status0.setItemMeta(settingsItem_status0_meta);

        lore = new ArrayList<>();
        settingsItem_status1_meta.setDisplayName(ChatColor.GREEN + "Status");
        lore.add("");
        lore.add("§6§l ◊ §fЗакрыт для всех кроме создателя.");
        lore.add("§6§l ♦ §fЗакрыт для всех кроме созателя и игроков из белого списка.");
        lore.add("§6§l ◊ §fОткрыт.");
        lore.add("");
        settingsItem_status1_meta.setLore(lore);
        settingsItem_status1.setItemMeta(settingsItem_status1_meta);

        lore = new ArrayList<>();
        settingsItem_status2_meta.setDisplayName(ChatColor.GREEN + "Status");
        lore.add("");
        lore.add("§6§l ◊ §fЗакрыт для всех кроме создателя.");
        lore.add("§6§l ◊ §fЗакрыт для всех кроме созателя и игроков из белого списка.");
        lore.add("§6§l ♦ §fОткрыт.");
        lore.add("");
        settingsItem_status2_meta.setLore(lore);
        settingsItem_status2.setItemMeta(settingsItem_status2_meta);

        lore = new ArrayList<>();
        settingsItem_spawn_meta.setDisplayName("§a§lМестоположение спавна");
        lore.add("");
        lore.add("§f Првавый клик чтобы установить текущее местоположение     ");
        lore.add("§f Левый клик чтобы установить местоположение по-умолчанию      ");
        lore.add("");
        settingsItem_spawn_meta.setLore(lore);
        settingsItem_spawn.setItemMeta(settingsItem_spawn_meta);

        lore = new ArrayList<>();
        settingsItem_mode0_meta.setDisplayName("§e§lРежим игры");
        lore.add("");
        lore.add("§6 ♦ Строительство");
        lore.add("§e ◊ Игра");
        lore.add("");
        settingsItem_mode0_meta.setLore(lore);
        settingsItem_mode0.setItemMeta(settingsItem_mode0_meta);

        lore = new ArrayList<>();
        settingsItem_mode1_meta.setDisplayName("§e§lРежим игры");
        lore.add("");
        lore.add("§6 ◊ Строительство");
        lore.add("§e ♦ Игра");
        lore.add("");
        settingsItem_mode1_meta.setLore(lore);
        settingsItem_mode1.setItemMeta(settingsItem_mode1_meta);

        lore = new ArrayList<>();
        settingsItem_whitelist_meta.setDisplayName("§f§lВайтлист");
        lore.add("");
        lore.add(" §bЛКМ §f- Добавить в белый список");
        lore.add(" §bПКМ §f- Удалить из белого списка");
        lore.add("");
        settingsItem_whitelist_meta.setLore(lore);
        settingsItem_whitelist.setItemMeta(settingsItem_whitelist_meta);

        lore = new ArrayList<>();
        settingsItem_tpToWorld_meta.setDisplayName("§6§lТелепорт в мир");
        lore.add("§7После нажатия на кнопку,");
        lore.add("§7код будет сохранён");
        lore.add("");
        settingsItem_tpToWorld_meta.setLore(lore);
        settingsItem_tpToWorld.setItemMeta(settingsItem_tpToWorld_meta);

        lore = new ArrayList<>();
        settingsItem_tpToCode_meta.setDisplayName("§6§lТелепорт в код");
        lore.add("§7Телепорт в код");
        lore.add("");
        settingsItem_tpToCode_meta.setLore(lore);
        settingsItem_tpToCode.setItemMeta(settingsItem_tpToCode_meta);

        lore = new ArrayList<>();
        settingsItem_name_meta.setDisplayName("§f§lНазвание мира");
        lore.add("§7");
        lore.add("§7Отображается на предмете в меню игр");
        lore.add("");
        settingsItem_name_meta.setLore(lore);
        settingsItem_name.setItemMeta(settingsItem_name_meta);

        lore = new ArrayList<>();
        settingsItem_blacklist_meta.setDisplayName("§f§lБлэклист");
        lore.add("");
        lore.add(" §bЛКМ §f- Добавить в блэклист");
        lore.add(" §bПКМ §f- Удалить из блэклиста");
        lore.add("");
        settingsItem_blacklist_meta.setLore(lore);
        settingsItem_blacklist.setItemMeta(settingsItem_blacklist_meta);

        lore = new ArrayList<>();
        settingsItem_gamerule_meta.setDisplayName("§9§lПравила игры");
        lore.add("§7PS: команда /gamerule для мира");
        lore.add("");
        settingsItem_blacklist_meta.setLore(lore);
        settingsItem_blacklist.setItemMeta(settingsItem_blacklist_meta);

        lore = new ArrayList<>();
        codeItem_event_meta.setDisplayName("§b§lСобытие");
        lore.add("§7Один из блоков, которые начинают строку.");
        lore.add("");
        codeItem_event_meta.setLore(lore);
        codeItem_event.setItemMeta(codeItem_event_meta);

        lore = new ArrayList<>();
        codeItem_player_meta.setDisplayName("§a§lИгрок");
        lore.add("§7Управление игроком.");
        lore.add("");
        codeItem_player_meta.setLore(lore);
        codeItem_player.setItemMeta(codeItem_player_meta);

        lore = new ArrayList<>();
        codeItem_var_meta.setDisplayName("§6§lПеременная");
        lore.add("§7Управление переменной.");
        lore.add("");
        codeItem_var_meta.setLore(lore);
        codeItem_var.setItemMeta(codeItem_var_meta);

        lore = new ArrayList<>();
        codeItem_eventControl_meta.setDisplayName("§8§lУправление событием");
        lore.add("§7Управление событием.");
        lore.add("");
        codeItem_eventControl_meta.setLore(lore);
        codeItem_eventControl.setItemMeta(codeItem_eventControl_meta);

        lore = new ArrayList<>();
        codeItem_If_meta.setDisplayName("§6§lЕсли");
        lore.add("§7Если.");
        lore.add("");
        codeItem_If_meta.setLore(lore);
        codeItem_If.setItemMeta(codeItem_If_meta);

        lore = new ArrayList<>();
        codeItem_varChooser_meta.setDisplayName("§f§lПеременные");
        lore.add("§7Клик.");
        lore.add("");
        codeItem_varChooser_meta.setLore(lore);
        codeItem_varChooser.setItemMeta(codeItem_varChooser_meta);

        codeItem_varText_meta.setDisplayName("Текст");
        codeItem_varText.setItemMeta(codeItem_varText_meta);

        codeItem_varInt_meta.setDisplayName("Число");
        codeItem_varInt.setItemMeta(codeItem_varInt_meta);

        codeItem_varVar_meta.setDisplayName("Переменная");
        codeItem_varVar.setItemMeta(codeItem_varVar_meta);

        codeItem_varEvent_meta.setDisplayName("Значение события");
        codeItem_varEvent.setItemMeta(codeItem_varEvent_meta);

        codeItem_varBool_meta.setDisplayName("Логический тип");
        codeItem_varBool.setItemMeta(codeItem_varBool_meta);

        codeItem_varGlass_meta.setDisplayName("Переменная");
        codeItem_varGlass.setItemMeta(codeItem_varGlass_meta);
        codeItem_varPlayerGlass_meta.setDisplayName("Переменная, хранящая игрока");
        codeItem_varPlayerGlass.setItemMeta(codeItem_varPlayerGlass_meta);
        codeItem_emptyGlass_meta.setDisplayName("§r");
        codeItem_emptyGlass.setItemMeta(codeItem_emptyGlass_meta);
        codeItem_valueGlass_meta.setDisplayName("Значение");
        codeItem_valueGlass.setItemMeta(codeItem_valueGlass_meta);

        lore = new ArrayList<>();
        codeItem_EventJoin_meta.setDisplayName("Событие входа игрока");
        lore.add("§7Вызывается, когда игрок заходит в игру.");
        lore.add("");
        codeItem_EventJoin_meta.setLore(lore);
        codeItem_EventJoin.setItemMeta(codeItem_EventJoin_meta);

        lore = new ArrayList<>();
        codeItem_EventExit_meta.setDisplayName("Событие выхода игрока");
        lore.add("§7Вызывается, когда игрок выходит из игры.");
        lore.add("");
        codeItem_EventExit_meta.setLore(lore);
        codeItem_EventExit.setItemMeta(codeItem_EventExit_meta);

        lore = new ArrayList<>();
        codeItem_EventDamage_meta.setDisplayName("Событие получения урона сущностью");
        lore.add("§7Вызывается, когда сущность получает урон.");
        lore.add("");
        codeItem_EventDamage_meta.setLore(lore);
        codeItem_EventDamage_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_EventDamage.setItemMeta(codeItem_EventDamage_meta);

        lore = new ArrayList<>();
        codeItem_EventBlockBreak_meta.setDisplayName("Событие ломания блока");
        lore.add("§7Вызывается, когда игрок ломает блок.");
        lore.add("");
        codeItem_EventBlockBreak_meta.setLore(lore);
        codeItem_EventBlockBreak_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_EventBlockBreak.setItemMeta(codeItem_EventBlockBreak_meta);

        lore = new ArrayList<>();
        codeItem_EventBlockPlace_meta.setDisplayName("Событие установки блока");
        lore.add("§7Вызывается, когда игрок ставит блок.");
        lore.add("");
        codeItem_EventBlockPlace_meta.setLore(lore);
        codeItem_EventBlockPlace_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_EventBlockPlace.setItemMeta(codeItem_EventBlockPlace_meta);

        lore = new ArrayList<>();
        codeItem_PlayerControlGamemode_meta.setDisplayName("Режим игры");
        lore.add("§7Управление режимом игры игрока.");
        lore.add("§7Выживание/Креатив/Приключение/Наблюдатель");
        lore.add("");
        codeItem_PlayerControlGamemode_meta.setLore(lore);
        codeItem_PlayerControlGamemode_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_PlayerControlGamemode.setItemMeta(codeItem_PlayerControlGamemode_meta);

        lore = new ArrayList<>();
        codeItem_PlayerControlSendmessage_meta.setDisplayName("Сообщение");
        lore.add("§7Отправляет сообщение игроку");
        lore.add("");
        codeItem_PlayerControlSendmessage_meta.setLore(lore);
        codeItem_PlayerControlSendmessage_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_PlayerControlSendmessage.setItemMeta(codeItem_PlayerControlSendmessage_meta);

        lore = new ArrayList<>();
        codeItem_VarControlSet_meta.setDisplayName("Установить");
        lore.add("§7Устанавливает значение переменной");
        lore.add("");
        codeItem_VarControlSet_meta.setLore(lore);
        codeItem_VarControlSet_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_VarControlSet.setItemMeta(codeItem_VarControlSet_meta);

        lore = new ArrayList<>();
        codeItem_VarControlGetPlayerName_meta.setDisplayName("Получить имя игрока");
        lore.add("§7Устанавливает имя игрока в переменную");
        lore.add("");
        codeItem_VarControlGetPlayerName_meta.setLore(lore);
        codeItem_VarControlGetPlayerName_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        codeItem_VarControlGetPlayerName.setItemMeta(codeItem_VarControlGetPlayerName_meta);

        lore = new ArrayList<>();
        codeItem_getPlayer_meta.setDisplayName("Игрок");
        lore.add("§7Игрок, участвующий в событии");
        lore.add("");
        codeItem_getPlayer_meta.setLore(lore);
        codeItem_getPlayer_meta.addItemFlags();
        codeItem_getPlayer.setItemMeta(codeItem_getPlayer_meta);

        lore = new ArrayList<>();
        codeItem_getEntity_meta.setDisplayName("Сущность");
        lore.add("§7Сущность, участвующая в событии");
        lore.add("");
        codeItem_getEntity_meta.setLore(lore);
        codeItem_getEntity_meta.addItemFlags();
        codeItem_getEntity.setItemMeta(codeItem_getEntity_meta);

        lore = new ArrayList<>();
        codeItem_getDamage_meta.setDisplayName("Урон");
        lore.add("§7Полученный урон");
        lore.add("");
        codeItem_getDamage_meta.setLore(lore);
        codeItem_getDamage_meta.addItemFlags();
        codeItem_getDamage.setItemMeta(codeItem_getDamage_meta);

        lore = new ArrayList<>();
        codeItem_getFinalDamage_meta.setDisplayName("Окончательный урон");
        lore.add("§7Урон, который получила сущность после");
        lore.add("всех требуемых вычислений");
        lore.add("");
        codeItem_getFinalDamage_meta.setLore(lore);
        codeItem_getFinalDamage_meta.addItemFlags();
        codeItem_getFinalDamage.setItemMeta(codeItem_getFinalDamage_meta);


    }

    @Override
    public void onDisable() {
        try {
            FileWriter myWriter = new FileWriter("creativeData.txt");
            myWriter.write("count: " + Integer.toString(count_of_pl_worlds) + "\n");
            myWriter.close();
            try {
                FileWriter myWriter3 = new FileWriter("suffixes.mmap");

                for (Map.Entry<String, String[]> entry : suffixes.entrySet()) {
                    String key = entry.getKey();
                    String[] value = entry.getValue();
                    myWriter3.write(key + " ");
                    myWriter3.write(curSuffix.get(key) + " ");
                    for (int i = 0; i < value.length; i++) {
                        myWriter3.write(value[i] + (i != value.length - 1 ? " " : ""));
                        myWriter3.flush();
                    }
                    myWriter3.write("\n");
                    myWriter3.flush();
                }

                myWriter3.close();
            } catch (IOException ex) {
                //
            }
        } catch (IOException ex) {
            //
        }
        try {
            File newFile = new File("banned.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            FileWriter myWriter3 = new FileWriter(newFile);

            for (BannedPlayer b : bannedPlayers) {
                myWriter3.write(b.name + " " + b.time + " " + b.reason);
                myWriter3.write("\n");
                myWriter3.flush();
            }

            myWriter3.close();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("muted.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            FileWriter myWriter3 = new FileWriter(newFile);

            for (MutedPlayer b : mutedPlayers) {
                myWriter3.write(b.name + " " + b.time + " " + b.reason);
                myWriter3.write("\n");
                myWriter3.flush();
            }

            myWriter3.close();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            File newFile = new File("bannedIps.txt");
            if (newFile.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
            FileWriter myWriter3 = new FileWriter(newFile);

            for (BannedIp b : bannedIps) {
                myWriter3.write(b.ip + " " + b.time + " " + b.reason);
                myWriter3.write("\n");
                myWriter3.flush();
            }

            myWriter3.close();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        try {
            FileWriter myWriter3 = new FileWriter("ranks.txt");

            for (Map.Entry<String, String> entry : ranks.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                myWriter3.write(key + " " + value);
                myWriter3.write("\n");
                myWriter3.flush();
            }

            myWriter3.close();
        } catch (IOException ex) {
            //
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {

        e.setJoinMessage("");
        playerCurGamesPage.put(e.getPlayer().getName(), 0);

    }

    @EventHandler
    public void FoodLevelChangeEvent(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getWorld().equals(spawn_world)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void PlayerDropItemEvent(PlayerDropItemEvent e) {
        if (e.getPlayer().getWorld() == spawn_world) {
            if (e.getItemDrop().getItemStack().equals(menu_item)) {

                e.setCancelled(true);
            } else {
                e.getItemDrop().remove();
            }
        }
    }
//Integer.parseInt(e.getRemover().getWorld().getName().split("world")[0])

    @EventHandler
    public void HangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if (!e.getRemover().getWorld().equals(spawn_world)) {
            if (worlds.get(Integer.parseInt(e.getRemover().getWorld().getName().split("world")[0])).mode == 0 && (!(worlds.get(Integer.parseInt(e.getRemover().getWorld().getName().split("world")[0])).owner.equals(e.getRemover().getName()) || !worlds.get(Integer.parseInt(e.getRemover().getWorld().getName().split("world")[0])).whitelistedPlayers.contains(e.getRemover().getName())))) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void PlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {
        if (!e.getPlayer().getWorld().equals(spawn_world)) {
            if (worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 0 && (!(worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).owner.equals(e.getPlayer().getName()) || !worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).whitelistedPlayers.contains(e.getPlayer().getName())))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void PlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        if (!e.getPlayer().getWorld().equals(spawn_world)) {
            if (worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 0 && (!(worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).owner.equals(e.getPlayer().getName()) || !worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).whitelistedPlayers.contains(e.getPlayer().getName())))) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().equals(Action.PHYSICAL) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

            if (!e.getPlayer().getWorld().equals(spawn_world)) {
                if (worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 0 && (!(worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).owner.equals(e.getPlayer().getName()) || !worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).whitelistedPlayers.contains(e.getPlayer().getName())))) {
                    e.setCancelled(true);
                }
            }


        }
        if (!p.getWorld().equals(getServer().getWorld("reg"))) {
            if (e.getItem() != null) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (e.getItem().equals(menu_item)) {
                        e.setCancelled(true);
                        p.openInventory(menu_inv);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BELL, 1, 1);

                    }
                    if (e.getItem().equals(settingsItem)) {
                        Inventory settings_inv;
                        settings_inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "" + ChatColor.BOLD + "Настройки");
                        ItemStack stGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
                        ItemMeta stGlass_meta = stGlass.getItemMeta();
                        stGlass_meta.setDisplayName("");
                        stGlass.setItemMeta(stGlass_meta);
                        for (int i = 0; i < 54; i++) {

                            settings_inv.setItem(i, new ItemStack(stGlass));
                            i += 8;
                            settings_inv.setItem(i, new ItemStack(stGlass));
                        }
                        for (int i = 1; i < 8; i++) {
                            settings_inv.setItem(i, new ItemStack(stGlass));
                        }
                        for (int i = 45; i < 53; i++) {
                            settings_inv.setItem(i, new ItemStack(stGlass));
                        }
                        if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).owner.equals(p.getName()) || p.getName().equals("bulat5280") || worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).whitelistedPlayers.contains(p.getName())) {
                            if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).status == 0) {
                                settings_inv.setItem(16, settingsItem_status0);
                            } else if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).status == 1) {
                                settings_inv.setItem(16, settingsItem_status1);
                            } else if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).status == 2) {
                                settings_inv.setItem(16, settingsItem_status2);
                            }

                            if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).mode == 0) {
                                settings_inv.setItem(22, settingsItem_mode0);
                            } else if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).mode == 1) {
                                settings_inv.setItem(22, settingsItem_mode1);
                            }

                            ItemStack infoItem = new ItemStack(Material.PAPER);
                            ItemMeta infoItemMeta = infoItem.getItemMeta();
                            infoItemMeta.setDisplayName(worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).name);
                            List<String> lore = new ArrayList<>();
                            lore.add("");
                            lore.add(" §eАйди: §r§f" + Integer.parseInt(p.getWorld().getName().split("world")[0]));
                            lore.add(" §eВладелец: §r§f" + worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).owner);
                            lore.add("");
                            infoItemMeta.setLore(lore);
                            infoItem.setItemMeta(infoItemMeta);
                            settings_inv.setItem(40, infoItem);
                            settings_inv.setItem(10, settingsItem_spawn);
                            settings_inv.setItem(29, settingsItem_whitelist);
                            settings_inv.setItem(33, settingsItem_tpToWorld);
                            settings_inv.setItem(34, settingsItem_tpToCode);
                            settings_inv.setItem(11, settingsItem_name);
                            settings_inv.setItem(28, settingsItem_blacklist);
                        }
                        p.openInventory(settings_inv);
                    }
                }

            }
            if (e.getPlayer().getWorld().getName().contains("Code")) {
                if (e.getItem() != null && !e.getItem().equals(settingsItem)) {
                    if (e.getItem().equals(codeItem_varChooser)) {
                        Inventory varChooserInv = Bukkit.createInventory(null, 27, "Переменные");
                        varChooserInv.setItem(11, codeItem_varText);
                        varChooserInv.setItem(12, codeItem_varInt);
                        varChooserInv.setItem(13, codeItem_varVar);
                        varChooserInv.setItem(14, codeItem_varBool);
                        varChooserInv.setItem(15, codeItem_varEvent);
                        e.getPlayer().openInventory(varChooserInv);
                    } else if (e.getItem().getType().equals(Material.BOOK) || e.getItem().getType().equals(Material.MAGMA_CREAM) || e.getItem().getType().equals(Material.SLIME_BALL) || e.getItem().getType().equals(Material.WATCH)) {
                        e.getPlayer().sendMessage(e.getItem().getItemMeta().getDisplayName());
                        e.getPlayer().sendMessage("Введите имя переменной:");
                    } else if (e.getItem().equals(codeItem_varEvent)) {
                        Inventory varChooserInv = Bukkit.createInventory(null, 54, "Переменные событий");
                        varChooserInv.addItem(codeItem_EventJoin);
                        varChooserInv.addItem(codeItem_EventExit);
                        varChooserInv.addItem(codeItem_EventDamage);
                        varChooserInv.addItem(codeItem_EventBlockBreak);
                        varChooserInv.addItem(codeItem_EventBlockPlace);
                        e.getPlayer().openInventory(varChooserInv);
                    }
                }
                if (e.getClickedBlock() != null) {
                    if (e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
                        if (e.getClickedBlock().getLocation().add(0, 0, -1).getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                            Inventory inv = Bukkit.createInventory(null, 27, "События");
                            playerCodeCurClickedSign.put(e.getPlayer().getName(), e.getClickedBlock().getLocation());
                            inv.addItem(codeItem_EventJoin);
                            inv.addItem(codeItem_EventExit);
                            inv.addItem(codeItem_EventDamage);
                            inv.addItem(codeItem_EventBlockBreak);
                            inv.addItem(codeItem_EventBlockPlace);
                            e.getPlayer().openInventory(inv);
                        } else if (e.getClickedBlock().getLocation().add(0, 0, -1).getBlock().getType().equals(Material.COBBLESTONE)) {
                            Inventory inv = Bukkit.createInventory(null, 27, "Управление игроком");
                            playerCodeCurClickedSign.put(e.getPlayer().getName(), e.getClickedBlock().getLocation());
                            inv.addItem(codeItem_PlayerControlGamemode);
                            inv.addItem(codeItem_PlayerControlSendmessage);
                            e.getPlayer().openInventory(inv);
                        } else if (e.getClickedBlock().getLocation().add(0, 0, -1).getBlock().getType().equals(Material.IRON_BLOCK)) {
                            Inventory inv = Bukkit.createInventory(null, 27, "Управление переменными");
                            playerCodeCurClickedSign.put(e.getPlayer().getName(), e.getClickedBlock().getLocation());
                            inv.addItem(codeItem_VarControlSet);
                            inv.addItem(codeItem_VarControlGetPlayerName);
                            e.getPlayer().openInventory(inv);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent e) {

        try {
            if (e.getEntity() instanceof Player) {
                Player player = (Player) e.getEntity();
                if (player.getWorld().equals(spawn_world) || player.getWorld().equals(getServer().getWorld("reg")) || worlds.get(Integer.parseInt(player.getName().split("world")[0])).mode == 0) {
                    e.setCancelled(true);
                    if (e.getEntity().getLocation().getY() <= 0) {
                        e.getEntity().teleport(e.getEntity().getLocation().add(0, 100, 0));
                    }
                }

            }
            if (e.getEntity().getWorld().getName().endsWith("world") && worlds.get(Integer.parseInt(e.getEntity().getWorld().getName().split("world")[0])).mode == 1) {
                entityDamageEvent(e.getEntity(), Integer.parseInt(e.getEntity().getWorld().getName().split("world")[0]), e, e);
            }
        } catch (Exception ex) {
        }
    }


    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        event.setDeathMessage("");
        playerDeathLocations.put(event.getEntity().getPlayer().getName(), event.getEntity().getPlayer().getWorld().getSpawnLocation());
        event.getEntity().getPlayer().teleport(event.getEntity().getPlayer().getLocation());
    }

    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent event) {
        new BukkitRunnable() {
            public void run() {
                event.getPlayer().teleport(playerDeathLocations.get(event.getPlayer().getName()));
                event.getPlayer().getInventory().setItem(4, menu_item);
                if (worlds.get(Integer.parseInt(event.getPlayer().getWorld().getName().split("world")[0])).owner.equals(event.getPlayer().getName())) {
                    event.getPlayer().getInventory().setItem(8, settingsItem);
                }
                playerDeathLocations.remove(event.getPlayer().getName());
            }
        }.runTaskLater(this, 1);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.setQuitMessage("");
        if (playerCurGameWorld.get(e.getPlayer().getName()) != null) {
            if (worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 1) {
                playerExitEvent(e.getPlayer(), Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0]), null, e);
            }
            playerCurGameWorld.remove(e.getPlayer().getName());
        }
        playerCurGamesPage.remove(e.getPlayer().getName());

        for (Player p : getServer().getOnlinePlayers()) {
            p.sendMessage("§c§l❰ §f§l" + e.getPlayer().getDisplayName());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getCurrentItem() != null) {

            if (e.getClickedInventory().getName().equals("Меню")) {
                e.setCancelled(true);
                if (e.getCurrentItem().equals(diamond_item)) {
                    creative_inv = getGamesInventory((Player) e.getWhoClicked(), creative_inv);
                    myGames_item_meta.setDisplayName("§2§lМои игры");
                    myGames_item_meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    myGames_item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    myGames_item_meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    myGames_item.setItemMeta(myGames_item_meta);
                    creative_inv.setItem(49, myGames_item);
                    e.getWhoClicked().openInventory(creative_inv);
                }
            }

            if (e.getClickedInventory().getName().equals("§d§lCreative§b§l+")) {
                e.setCancelled(true);
                if (e.getCurrentItem().getType().equals(Material.COMMAND)) {
                    e.getWhoClicked().getName();
                    Inventory inv = Bukkit.createInventory(null, 54, "§2§lМои игры");
                    try {
                        for (int i = 0; i < count_of_pl_worlds; i++) {
                            if (worlds.get(i).owner.equals(e.getWhoClicked().getName()) || worlds.get(i).whitelistedPlayers.contains(e.getWhoClicked().getName())) {
                                ItemStack ginv_item = new ItemStack(Material.DIAMOND);
                                if (worlds.get(i).mode == 0) {
                                    ginv_item = new ItemStack(Material.BRICK);
                                }

                                ItemMeta ginv_item_meta = ginv_item.getItemMeta();
                                ginv_item_meta.setDisplayName(worlds.get(i).name);
                                List<String> ginv_lore = new ArrayList<>();
                                ginv_lore.add("§8Айди: " + Integer.toString(worlds.get(i).id));
                                ginv_lore.add("§fУникальные посетители: §f" + Integer.toString(worlds.get(i).unique_visitors.size()));
                                ginv_lore.add("§cЛайки: §f" + Integer.toString(worlds.get(i).likes.size()));
                                ginv_lore.add("");
                                ginv_lore.add("§f§oНажми, чтобы войти.");
                                ginv_item_meta.setLore(ginv_lore);
                                ginv_item.setItemMeta(ginv_item_meta);

                                inv.addItem(ginv_item);
                            }
                        }
                    } catch (Exception ex) {
//
                    }

                    inv.addItem(createWorld_item);
                    e.getWhoClicked().openInventory(inv);


                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§f<-") && e.getCurrentItem().getType().equals(Material.ARROW)) {
                    playerCurGamesPage.put(e.getWhoClicked().getName(), playerCurGamesPage.get(e.getWhoClicked().getName()) - 1);
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().openInventory(getGamesInventory((Player) e.getWhoClicked(), creative_inv));
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§f->") && e.getCurrentItem().getType().equals(Material.ARROW)) {
                    playerCurGamesPage.put(e.getWhoClicked().getName(), playerCurGamesPage.get(e.getWhoClicked().getName()) + 1);
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().openInventory(getGamesInventory((Player) e.getWhoClicked(), creative_inv));
                } else if (e.getCurrentItem().getType().equals(Material.DIAMOND) || e.getCurrentItem().getType().equals(Material.BRICK)) {
                    e.setCancelled(true);
                    teleportToWorld(Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(2).split("§f")[1]), (Player) e.getWhoClicked());
                }
            }
        }
        if (e.getClickedInventory() != null) {
            if (e.getClickedInventory().getName().equals("§2§lМои игры")) {
                e.setCancelled(true);
                if (e.getCurrentItem().getType().equals(Material.COMMAND_CHAIN)) {
                    e.getWhoClicked().openInventory(creative_inv);

                }
                if (e.getCurrentItem().getType().equals(Material.GLASS)) {
                    getServer().getScheduler().runTask(this, new Runnable() {
                        @Override
                        public void run() {

                            e.getWhoClicked().closeInventory();
                            Player p = Bukkit.getPlayer(e.getWhoClicked().getName());

                            p.sendTitle("", "Мир создаётся...");
                            try {
                                int plc = count_of_pl_worlds;
                                count_of_pl_worlds++;
                                WorldCreator wc = new WorldCreator("" + Integer.toString(plc) + "world");
                                wc.generator(new CustomChunkGenerator());
                                WorldCreator wcC = new WorldCreator("" + Integer.toString(plc) + "worldCode");
                                wcC.generator(new CustomChunkGeneratorCode());

                                World w = wc.createWorld();
                                World wC = wcC.createWorld();
                                GameWorld gw = new GameWorld(plc, e.getWhoClicked().getName() + "'s world", e.getWhoClicked().getName(), 0, 0, new Location(w, 0, 100, 0, 0, 0));
                                worlds.put(plc, gw);

                                File creativeData_file = new File("" + Integer.toString(plc) + "worldData.ser");
                                FileOutputStream fos = new FileOutputStream(creativeData_file);
                                ObjectOutputStream oos = new ObjectOutputStream(fos);
                                oos.writeObject(gw);
                                oos.close();
                                fos.close();
                            } catch (IOException ex) {
//
                            }


                            p.sendTitle("§a§lМир создан.", "", 2, 10, 2);
                        }
                    });


                }
                if (e.getCurrentItem().getType().equals(Material.DIAMOND) || e.getCurrentItem().getType().equals(Material.BRICK)) {
                    Player p = (Player) e.getWhoClicked();
                    p.performCommand("ad " + worlds.get(Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0).split(": ")[1])).id);
                    //teleportToWorld(worlds.get(Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0).split(": ")[1])).id, p);
                }
            }

            if (e.getClickedInventory().getName().equals(ChatColor.BLUE + "" + ChatColor.BOLD + "Настройки")) {
                e.setCancelled(true);
                if (e.getCurrentItem().equals(settingsItem_status0)) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).setStatus(1);
                    e.getClickedInventory().setItem(16, settingsItem_status1);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).setStatus(1);
                            }
                        } catch (Exception ex) {
                        }
                    }
                } else if (e.getCurrentItem().equals(settingsItem_status1)) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).setStatus(2);
                    e.getClickedInventory().setItem(16, settingsItem_status2);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).setStatus(2);
                            }
                        } catch (Exception ex) {
                        }
                    }
                } else if (e.getCurrentItem().equals(settingsItem_status2)) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).setStatus(0);
                    e.getClickedInventory().setItem(16, settingsItem_status0);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).setStatus(0);
                            }
                        } catch (Exception ex) {
                        }
                    }
                }

                if (e.getCurrentItem().equals(settingsItem_mode0)) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).setMode(1);

                    e.getClickedInventory().setItem(22, settingsItem_mode1);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).setMode(1);
                                teleportToWorld(Integer.parseInt(p.getWorld().getName().split("world")[0]), p);
                            }
                        } catch (Exception ex) {
                        }
                    }

                } else if (e.getCurrentItem().equals(settingsItem_mode1)) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).setMode(0);

                    e.getClickedInventory().setItem(22, settingsItem_mode0);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).setMode(0);
                                teleportToWorld(Integer.parseInt(p.getWorld().getName().split("world")[0]), p);
                            }
                        } catch (Exception ex) {
                        }
                    }
                }

                if (e.getCurrentItem().equals(settingsItem_spawn)) {
                    if (e.getClick().isLeftClick()) {

                        worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnX = 0;
                        worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnY = 100;
                        worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnZ = 0;
                        worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnYaw = 0;
                        worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnPitch = 0;
                        for (Player p : getServer().getOnlinePlayers()) {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                playerCurGameWorld.get(e.getWhoClicked().getName()).spawnX = 0;
                                playerCurGameWorld.get(e.getWhoClicked().getName()).spawnY = 100;
                                playerCurGameWorld.get(e.getWhoClicked().getName()).spawnZ = 0;
                                playerCurGameWorld.get(e.getWhoClicked().getName()).spawnYaw = 0;
                                playerCurGameWorld.get(e.getWhoClicked().getName()).spawnPitch = 0;
                            }
                        }

                    } else if (e.getClick().isRightClick()) {
                        if (!e.getWhoClicked().getLocation().getWorld().getName().contains("Code")) {
                            worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnX = e.getWhoClicked().getLocation().getX();
                            worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnY = e.getWhoClicked().getLocation().getY();
                            worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnZ = e.getWhoClicked().getLocation().getZ();
                            worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnYaw = e.getWhoClicked().getLocation().getYaw();
                            worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).spawnPitch = e.getWhoClicked().getLocation().getPitch();
                            for (Player p : getServer().getOnlinePlayers()) {
                                if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                    playerCurGameWorld.get(e.getWhoClicked().getName()).spawnX = e.getWhoClicked().getLocation().getX();
                                    playerCurGameWorld.get(e.getWhoClicked().getName()).spawnY = e.getWhoClicked().getLocation().getY();
                                    playerCurGameWorld.get(e.getWhoClicked().getName()).spawnZ = e.getWhoClicked().getLocation().getZ();
                                    playerCurGameWorld.get(e.getWhoClicked().getName()).spawnYaw = e.getWhoClicked().getLocation().getYaw();
                                    playerCurGameWorld.get(e.getWhoClicked().getName()).spawnPitch = e.getWhoClicked().getLocation().getPitch();
                                }
                            }
                        } else {
                            e.getWhoClicked().sendMessage("§cВы не можете поставить точку спавна в мире кода.");
                        }
                    }
                }

                if (e.getCurrentItem().equals(settingsItem_whitelist)) {
                    //открытие меню добавить в вайтлист
                    if (e.getClick().isLeftClick()) {
                        Inventory wlAddInv = Bukkit.createInventory(null, 54, "§f§lДобавить в вайтлист:");
                        for (Player p : getServer().getOnlinePlayers()) {
                            try {
                                if ((!worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.contains(p.getName())) && (!p.getName().equals(worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).owner))) {
                                    wlAddInv.addItem(getHead(p.getName()));
                                }
                            } catch (Exception ex) {
                            }
                        }
                        e.getWhoClicked().openInventory(wlAddInv);
                    } else if (e.getClick().isRightClick()) {
                        //открытие меню удалить из вайтлиста
                        Inventory wlRemoveInv = Bukkit.createInventory(null, 54, "§f§lУдалить из вайтлиста:");
                        for (String p : worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers) {
                            if (worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.contains(p)) {
                                wlRemoveInv.addItem(getHead(p));
                            }

                        }
                        e.getWhoClicked().openInventory(wlRemoveInv);
                    }
                }
                if (e.getCurrentItem().equals(settingsItem_blacklist)) {
                    //открытие меню добавить в блаклист
                    if (e.getClick().isLeftClick()) {
                        Inventory blAddInv = Bukkit.createInventory(null, 54, "§f§lДобавить в блэклист:");
                        for (Player p : getServer().getOnlinePlayers()) {
                            try {
                                if ((!worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.contains(p.getName())) && (!p.getName().equals(worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).owner))) {
                                    blAddInv.addItem(getHead(p.getName()));
                                }
                            } catch (Exception ex) {
                            }
                        }
                        e.getWhoClicked().openInventory(blAddInv);
                    } else if (e.getClick().isRightClick()) {
                        //открытие меню удалить из блаклиста
                        Inventory blRemoveInv = Bukkit.createInventory(null, 54, "§f§lУдалить из блэклиста:");
                        for (String p : worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers) {
                            if (worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.contains(p)) {
                                blRemoveInv.addItem(getHead(p));
                            }

                        }
                        e.getWhoClicked().openInventory(blRemoveInv);
                    }
                }
                reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);
            } else if (e.getClickedInventory().getName().equals("§f§lДобавить в вайтлист:")) {
                //Клик по меню добавить в вайтлист
                if (e.getCurrentItem() != null) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.add(e.getCurrentItem().getItemMeta().getDisplayName());
                    e.getWhoClicked().closeInventory();
                    Inventory wlAddInv = Bukkit.createInventory(null, 54, "§f§lДобавить в вайтлист:");
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])) != null) {
                                if ((!worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.contains(p.getName())) && (!p.getName().equals(worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).owner))) {
                                    wlAddInv.addItem(getHead(p.getName()));
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }
                    e.getWhoClicked().openInventory(wlAddInv);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                playerCurGameWorld.get(e.getWhoClicked().getName()).whitelistedPlayers.add(e.getCurrentItem().getItemMeta().getDisplayName());
                            }
                        } catch (Exception ex) {
                        }
                    }
                    reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);

                }
            } else if (e.getClickedInventory().getName().equals("§f§lУдалить из вайтлиста:")) {
                //Клик по меню удалить из вайтлиста
                worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.remove(e.getCurrentItem().getItemMeta().getDisplayName());
                e.getWhoClicked().closeInventory();
                Inventory wlRemoveInv = Bukkit.createInventory(null, 54, "§f§lУдалить из вайтлиста:");
                for (String p : worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers) {
                    if (playerCurGameWorld.get(p) != null) {
                        if (worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).whitelistedPlayers.contains(p)) {
                            wlRemoveInv.addItem(getHead(p));
                        }
                    }
                }
                e.getWhoClicked().openInventory(wlRemoveInv);
                for (Player p : getServer().getOnlinePlayers()) {
                    try {
                        if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                            playerCurGameWorld.get(e.getWhoClicked().getName()).whitelistedPlayers.remove(e.getCurrentItem().getItemMeta().getDisplayName());
                        }
                    } catch (Exception ex) {
                    }
                }
                reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);
            } else if (e.getClickedInventory().getName().equals("§f§lДобавить в блэклист:")) {
                //Клик по меню добавить в блаклист
                if (e.getCurrentItem() != null) {
                    worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.add(e.getCurrentItem().getItemMeta().getDisplayName());
                    if (worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).mode == 1) {
                        playerExitEvent((Player) e.getWhoClicked(), playerCurGameWorld.get(e.getWhoClicked().getName()).id, e, e);
                    }
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setFoodLevel(20);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setExp(0);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setExhaustion(0);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setHealthScale(20);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setHealth(20);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setSaturation(20);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setTotalExperience(0);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setGlowing(false);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).getInventory().clear();
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).getInventory().setItem(4, menu_item);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).setGameMode(GameMode.ADVENTURE);
                    Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName()).teleport(spawn_world.getSpawnLocation());

                    playerCurGameWorld.remove(e.getWhoClicked().getName());
                    e.getWhoClicked().closeInventory();
                    Inventory blAddInv = Bukkit.createInventory(null, 54, "§f§lДобавить в блэклист:");
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])) != null) {
                                if (!worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.contains(p.getName())) {
                                    blAddInv.addItem(getHead(p.getName()));
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }
                    e.getWhoClicked().openInventory(blAddInv);
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                                playerCurGameWorld.get(e.getWhoClicked().getName()).blacklistedPlayers.add(e.getCurrentItem().getItemMeta().getDisplayName());
                            }
                        } catch (Exception ex) {
                        }
                    }
                    reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);

                }
            } else if (e.getClickedInventory().getName().equals("§f§lУдалить из блэклиста:")) {
                //Клик по меню удалить из блаклиста
                worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.remove(e.getCurrentItem().getItemMeta().getDisplayName());
                e.getWhoClicked().closeInventory();
                Inventory blRemoveInv = Bukkit.createInventory(null, 54, "§f§lУдалить из блэклиста:");
                for (String p : worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers) {
                    if (playerCurGameWorld.get(p) != null) {
                        if (worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).blacklistedPlayers.contains(p)) {
                            blRemoveInv.addItem(getHead(p));
                        }
                    }
                }

                e.getWhoClicked().openInventory(blRemoveInv);
                for (Player p : getServer().getOnlinePlayers()) {
                    try {
                        if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == playerCurGameWorld.get(e.getWhoClicked().getName()).id) {
                            playerCurGameWorld.get(e.getWhoClicked().getName()).blacklistedPlayers.remove(e.getCurrentItem().getItemMeta().getDisplayName());
                        }
                    } catch (Exception ex) {
                    }
                }
                reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);
            } else if (e.getClickedInventory().getName().equals("Суффиксы")) {
                e.setCancelled(true);
                if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
                    if (curSuffix.containsKey(e.getWhoClicked().getName())) {
                        curSuffix.remove(e.getWhoClicked().getName());
                        ((Player) e.getWhoClicked()).performCommand("suffixes");
                    }
                } else {
                    if (e.getCurrentItem() != null) {
                        curSuffix.put(e.getWhoClicked().getName(), e.getCurrentItem().getItemMeta().getDisplayName());
                        ((Player) e.getWhoClicked()).performCommand("suffixes");
                    }
                }
            }
            if (e.getCurrentItem().equals(settingsItem_tpToWorld)) {

                GameWorld gw = worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id);
                e.getWhoClicked().sendMessage("§e§lКомпиляция кода...");
                worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).code.setCode(compileCode(worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).id));
                //e.getWhoClicked().sendMessage(worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id).code.code);

                teleportToWorld(playerCurGameWorld.get(e.getWhoClicked().getName()).id, (Player) e.getWhoClicked());
                reloadWorldData(playerCurGameWorld.get(e.getWhoClicked().getName()).id);
            }
            if (e.getCurrentItem().equals(settingsItem_tpToCode)) {

                GameWorld gw = worlds.get(playerCurGameWorld.get(e.getWhoClicked().getName()).id);

                e.getWhoClicked().teleport(Bukkit.getWorld(gw.spawnName + "Code").getSpawnLocation());
                e.getWhoClicked().getInventory().clear();
                e.getWhoClicked().getInventory().setItem(8, settingsItem);
                e.getWhoClicked().getInventory().setItem(4, menu_item);
                e.getWhoClicked().getInventory().setItem(0, codeItem_event);
                e.getWhoClicked().getInventory().setItem(1, codeItem_player);
                e.getWhoClicked().getInventory().setItem(2, codeItem_var);
                e.getWhoClicked().getInventory().setItem(9, codeItem_eventControl);
                e.getWhoClicked().getInventory().setItem(3, codeItem_If);
                e.getWhoClicked().getInventory().setItem(6, codeItem_varChooser);
                e.getWhoClicked().setGameMode(GameMode.CREATIVE);
            }
            if (e.getCurrentItem().equals(settingsItem_name)) {
                playerMustEnterNameOfWorld.put(e.getWhoClicked().getName(), true);
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().sendMessage("§a§lВведите текст:");

            }
            if (e.getCurrentItem().equals(codeItem_emptyGlass) || e.getCurrentItem().equals(codeItem_varGlass) || e.getCurrentItem().equals(codeItem_valueGlass) || e.getCurrentItem().equals(codeItem_varPlayerGlass)) {
                e.setCancelled(true);
            }
            if (e.getCurrentItem() != null) {
                if (e.getWhoClicked().getWorld().getName().contains("Code")) {
                    if (e.getClickedInventory().getName().equals("События")) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().equals(codeItem_EventJoin)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "ВходИгрока");
                            s.update();
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        } else if (e.getCurrentItem().equals(codeItem_EventExit)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "ВыходИгрока");
                            s.update();
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        } else if (e.getCurrentItem().equals(codeItem_EventDamage)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "СущностьПолучаетУрон");
                            s.update();
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        } else if (e.getCurrentItem().equals(codeItem_EventBlockBreak)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "ЛоманиеБлокаИгроком");
                            s.update();
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        } else if (e.getCurrentItem().equals(codeItem_EventBlockPlace)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "УстановкаБлокаИгроком");
                            s.update();
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        }
                    } else if (e.getClickedInventory().getName().equals("Управление игроком")) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().equals(codeItem_PlayerControlGamemode)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "РежимИгры");
                            s.update();
                            CraftChest chest = (CraftChest) b.getLocation().clone().add(0, 1, -1).getBlock().getState();
                            for (int i = 0; i < 27; i++) {
                                chest.getInventory().setItem(i, codeItem_emptyGlass);
                            }
                            chest.getInventory().setItem(2, codeItem_varPlayerGlass);
                            chest.getInventory().setItem(4, codeItem_valueGlass);
                            chest.getInventory().setItem(11, null);
                            chest.getInventory().setItem(13, null);
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());

                        } else if (e.getCurrentItem().equals(codeItem_PlayerControlSendmessage)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "Сообщение");
                            s.update();
                            CraftChest chest = (CraftChest) b.getLocation().clone().add(0, 1, -1).getBlock().getState();
                            for (int i = 0; i < 27; i++) {
                                chest.getInventory().setItem(i, null);
                            }
                            chest.getInventory().setItem(25, codeItem_varPlayerGlass);
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());
                        }
                    } else if (e.getClickedInventory().getName().equals("Управление переменными")) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().equals(codeItem_VarControlSet)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "Установить значение");
                            s.update();
                            CraftChest chest = (CraftChest) b.getLocation().clone().add(0, 1, -1).getBlock().getState();
                            for (int i = 0; i < 27; i++) {
                                chest.getInventory().setItem(i, codeItem_emptyGlass);
                            }
                            chest.getInventory().setItem(2, codeItem_varGlass);
                            chest.getInventory().setItem(6, codeItem_valueGlass);
                            chest.getInventory().setItem(11, null);
                            chest.getInventory().setItem(15, null);
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());

                        } else if (e.getCurrentItem().equals(codeItem_VarControlGetPlayerName)) {
                            e.getWhoClicked().closeInventory();
                            Block b = playerCodeCurClickedSign.get(e.getWhoClicked().getName()).getBlock();
                            Sign s = (Sign) b.getState();
                            s.setLine(1, "Получить имя игрока");
                            s.update();
                            CraftChest chest = (CraftChest) b.getLocation().clone().add(0, 1, -1).getBlock().getState();
                            for (int i = 0; i < 27; i++) {
                                chest.getInventory().setItem(i, codeItem_emptyGlass);
                            }
                            chest.getInventory().setItem(2, codeItem_varGlass);
                            chest.getInventory().setItem(6, codeItem_varPlayerGlass);
                            chest.getInventory().setItem(11, null);
                            chest.getInventory().setItem(15, null);
                            playerCodeCurClickedSign.remove(e.getWhoClicked().getName());

                        }
                    } else if (e.getClickedInventory().getName().equals("Переменные событий")) {
                        if (e.getCurrentItem().equals(codeItem_EventJoin)) {
                            e.setCancelled(true);
                            for (int i = 36; i < 45; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, codeItem_emptyGlass);
                            }
                            for (int i = 45; i < 54; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, null);
                            }
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(45, codeItem_getPlayer);
                        } else if (e.getCurrentItem().equals(codeItem_EventExit)) {
                            e.setCancelled(true);
                            for (int i = 36; i < 45; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, codeItem_emptyGlass);
                            }
                            for (int i = 45; i < 54; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, null);
                            }
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(45, codeItem_getPlayer);
                        } else if (e.getCurrentItem().equals(codeItem_EventDamage)) {
                            e.setCancelled(true);
                            for (int i = 36; i < 45; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, codeItem_emptyGlass);
                            }
                            for (int i = 45; i < 54; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, null);
                            }
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(45, codeItem_getEntity);
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(46, codeItem_getDamage);
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(47, codeItem_getFinalDamage);

                        } else if (e.getCurrentItem().equals(codeItem_EventBlockBreak)) {
                            e.setCancelled(true);
                            for (int i = 36; i < 45; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, codeItem_emptyGlass);
                            }
                            for (int i = 45; i < 54; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, null);
                            }
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(45, codeItem_getPlayer);
                        } else if (e.getCurrentItem().equals(codeItem_EventBlockPlace)) {
                            e.setCancelled(true);
                            for (int i = 36; i < 45; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, codeItem_emptyGlass);
                            }
                            for (int i = 45; i < 54; i++) {
                                e.getWhoClicked().getOpenInventory().getTopInventory().setItem(i, null);
                            }
                            e.getWhoClicked().getOpenInventory().getTopInventory().setItem(45, codeItem_getPlayer);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        e.setMessage(e.getMessage().replace("&", "§"));
        try {
            if (playerMustEnterNameOfWorld.containsKey(e.getPlayer().getName())) {
                if (playerMustEnterNameOfWorld.get(e.getPlayer().getName())) {
                    worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).name = e.getMessage();
                    for (Player p : getServer().getOnlinePlayers()) {
                        try {
                            if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])) {

                                worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).name = e.getMessage();
                            }
                        } catch (Exception ex) {
                        }
                    }
                    playerMustEnterNameOfWorld.remove(e.getPlayer().getName());
                    e.getPlayer().sendMessage("Название мира изменено");
                    reloadWorldData(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0]));

                }
            }
            if (!playerMustEnterNameOfWorld.containsKey(e.getPlayer().getName())) {

                if (e.getPlayer().getWorld().getName().contains("Code") && e.getPlayer().getItemInHand().getType().equals(Material.BOOK)) {
                    if (!e.getMessage().contains("/n") && !e.getMessage().contains("~") && !e.getMessage().contains("@") && !e.getMessage().contains("#") && !e.getMessage().contains("∝") && !e.getPlayer().getItemInHand().equals(settingsItem)) {
                        ItemStack newText = codeItem_varText.clone();
                        ItemMeta newTextMeta = newText.getItemMeta();
                        newTextMeta.setDisplayName(e.getMessage());
                        newText.setItemMeta(newTextMeta);
                        e.getPlayer().setItemInHand(newText);
                    }
                } else if (e.getPlayer().getWorld().getName().contains("Code") && e.getPlayer().getItemInHand().getType().equals(Material.MAGMA_CREAM)) {
                    if (!e.getMessage().contains("/n") && !e.getMessage().contains("~") && !e.getMessage().contains("@") && !e.getMessage().contains("#") && !e.getMessage().contains("∝")) {
                        ItemStack newVar = codeItem_varVar.clone();
                        ItemMeta newVarMeta = newVar.getItemMeta();
                        newVarMeta.setDisplayName(e.getMessage());
                        newVar.setItemMeta(newVarMeta);
                        e.getPlayer().setItemInHand(newVar);
                    }
                } else if (e.getPlayer().getWorld().getName().contains("Code") && e.getPlayer().getItemInHand().getType().equals(Material.SLIME_BALL)) {
                    if (!e.getMessage().contains("/n") && !e.getMessage().contains("~") && !e.getMessage().contains("@") && !e.getMessage().contains("#") && !e.getMessage().contains("∝")) {
                        if (isNumeric(e.getMessage())) {
                            ItemStack newInt = codeItem_varInt.clone();
                            ItemMeta newIntMeta = newInt.getItemMeta();
                            newIntMeta.setDisplayName(e.getMessage());
                            newInt.setItemMeta(newIntMeta);
                            e.getPlayer().setItemInHand(newInt);
                        } else {
                            e.getPlayer().sendMessage("Надо ввести число");
                        }
                    }
                } else if (e.getPlayer().getWorld().getName().contains("Code") && e.getPlayer().getItemInHand().getType().equals(Material.WATCH)) {
                    if (!e.getMessage().contains("/n") && !e.getMessage().contains("~") && !e.getMessage().contains("@") && !e.getMessage().contains("#") && !e.getMessage().contains("∝")) {
                        if (e.getMessage().equals("true") || e.getMessage().equals("false")) {
                            ItemStack newInt = codeItem_varBool.clone();
                            ItemMeta newIntMeta = newInt.getItemMeta();
                            newIntMeta.setDisplayName(e.getMessage());
                            newInt.setItemMeta(newIntMeta);
                            e.getPlayer().setItemInHand(newInt);
                        } else {
                            e.getPlayer().sendMessage("Надо ввести логическое значение(true/false)");
                        }
                    }
                } else {
                    boolean muted = false;
                    for (int i = 0; i < mutedPlayers.size(); i++) {
                        MutedPlayer b = mutedPlayers.get(i);
                        if (b.name.equals(e.getPlayer().getName())) {
                            muted = true;
                            Date date = new Date();
                            double time = b.time - date.getTime() / 1000;
                            if (time < 0) {
                                mutedPlayers.remove(b);
                                muted = false;
                                break;
                            }
                            int d = (int) Math.floor(time / 86400);
                            time -= d * 86400;
                            int h = (int) Math.floor(time / 3600) % 24;
                            time -= h * 3600;
                            int m = (int) Math.floor(time / 60) % 60;
                            time -= m * 60;
                            int s = (int) time % 60;
                            e.getPlayer().sendMessage("§6Вы замучены! §fРазмут через §b" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд");
                            break;
                        }
                    }
                    if ((!e.getPlayer().getDisplayName().contains("not registered")) && (!muted)) {
                        String prefix = "§7§lPlayer";
                        if (e.getPlayer().isOp()) {
                            prefix = "§6§lAdmin";
                        }
                        if (ranks.containsKey(e.getPlayer().getName())) {
                            if (ranks.get(e.getPlayer().getName()).equals("Moder")) {
                                prefix = "§9§lModer";
                            }
                            if (ranks.get(e.getPlayer().getName()).equals("Builder")) {
                                prefix = "§c§lBuilder";
                            }
                            if (ranks.get(e.getPlayer().getName()).equals("Tester")) {
                                prefix = "§b§lTester";
                            }
                            if (ranks.get(e.getPlayer().getName()).equals("Curator")) {
                                prefix = "§e§lCurator";
                            }
                        }
                        String suffix = curSuffix.containsKey(e.getPlayer().getName()) ? curSuffix.get(e.getPlayer().getName()) : "";
                        if (e.getMessage().startsWith("!")) {
                            if (e.getPlayer().isOp()) {

                                for (Player p : getServer().getOnlinePlayers()) {
                                    p.sendMessage("§f[§6!§f] " + prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage().substring(1));
                                }
                            } else {
                                for (Player p : getServer().getOnlinePlayers()) {
                                    p.sendMessage("§f[§6!§f] " + prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage().substring(1));
                                }
                            }
                        } else if (e.getPlayer().getWorld().equals(spawn_world)) {
                            if (e.getPlayer().isOp()) {

                                for (Player p : getServer().getOnlinePlayers()) {
                                    p.sendMessage("§f[§6!§f] " + prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage());
                                }
                            } else {
                                for (Player p : getServer().getOnlinePlayers()) {
                                    p.sendMessage("§f[§6!§f] " + prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage());
                                }
                            }
                        } else {
                            if (e.getPlayer().isOp()) {

                                for (Player p : getServer().getOnlinePlayers()) {
                                    try {
                                        if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])) {
                                            p.sendMessage(prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage());
                                        }
                                    } catch (Exception ex) {
                                    }
                                }
                            } else {
                                for (Player p : getServer().getOnlinePlayers()) {
                                    try {
                                        if (Integer.parseInt(p.getWorld().getName().split("world")[0]) == Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])) {
                                            p.sendMessage(prefix + " " + e.getPlayer().getName() + " §f" + suffix + "§f: " + e.getMessage());
                                        }
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {

        }
    }

    public static ItemStack getHead(String player) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();

        skull.setDisplayName(player);
        skull.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        item.setItemMeta(skull);
        return item;
    }

    void teleportToWorld(int id, Player p) {
        if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])) != null) {
            if (worlds.get(id).mode == 1) {
                playerExitEvent(p, id, null, new PlayerQuitEvent(p, ""));
            }
            for (Player p1 : getServer().getOnlinePlayers()) {
                try {
                    if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])) != null) {
                        if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                            p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвышел из игры.");
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }

        if (worlds.get(id).status == 2) {
            if (!worlds.get(id).blacklistedPlayers.contains(p.getName())) {


                GameWorld gw = worlds.get(id);

                if (Bukkit.getWorld(gw.spawnName) == null) {
                    getServer().getScheduler().runTask(this, new Runnable() {
                        @Override
                        public void run() {

                            p.sendMessage(ChatColor.YELLOW + "Загрузка... Пожалуйста, подождите.");
                            WorldCreator wc = new WorldCreator("" + Integer.toString(id) + "world");
                            WorldCreator wcC = new WorldCreator("" + Integer.toString(id) + "worldCode");
                            getServer().createWorld(wc);
                            getServer().createWorld(wcC);
                            TextComponent tc = new TextComponent("§aМир загружен!\n§6-> [Нажми, чтобы телепортнуться] <-");
                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad " + id);
                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("*Клик*").create());
                            tc.setClickEvent(ce);
                            tc.setHoverEvent(he);
                            p.spigot().sendMessage(tc);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);
                        }
                    });


                } else {
                    playerCurGameWorld.put(p.getName(), worlds.get(id));
                    p.setGameMode(GameMode.ADVENTURE);
                    p.sendMessage(ChatColor.GREEN + "Телепортация...");
                    p.teleport(new Location(Bukkit.getWorld(gw.spawnName), gw.spawnX, gw.spawnY, gw.spawnZ, (float) gw.spawnYaw, (float) gw.spawnPitch));
                    for (Player p1 : getServer().getOnlinePlayers()) {
                        try {
                            if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                                p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвошёл в игру.");
                            }
                        } catch (Exception ex) {
                        }
                    }
                    p.setFoodLevel(20);
                    p.setExp(0);
                    p.setExhaustion(0);
                    p.setHealthScale(20);
                    p.setHealth(20);
                    p.setSaturation(20);
                    p.setTotalExperience(0);
                    p.setGlowing(false);
                    p.getInventory().clear();
                    p.getInventory().setItem(4, menu_item);
                    if (worlds.get(id).owner.equals(p.getName()) || worlds.get(id).whitelistedPlayers.contains(p.getName()) || p.getName().equals("bulat5280")) {

                        p.getInventory().setItem(8, settingsItem);

                        if (worlds.get(id).mode == 0) {
                            p.setGameMode(GameMode.CREATIVE);
                        }
                    }
                    if (worlds.get(id).mode == 1) {
                        playerJoinEvent(p, id, null, new PlayerJoinEvent(p, ""));
                    }
                }
            }
        } else if (worlds.get(id).status == 1) {
            if (worlds.get(id).whitelistedPlayers.contains(p.getName()) || !worlds.get(id).blacklistedPlayers.contains(p.getName()) || p.getName().equals("bulat5280")) {

                GameWorld gw = worlds.get(id);

                if (Bukkit.getWorld(gw.spawnName) == null) {

                    getServer().getScheduler().runTask(this, new Runnable() {
                        @Override
                        public void run() {
                            p.sendMessage(ChatColor.YELLOW + "Загрузка... Пожалуйста, подождите.");
                            WorldCreator wc = new WorldCreator("" + Integer.toString(id) + "world");
                            WorldCreator wcC = new WorldCreator("" + Integer.toString(id) + "worldCode");
                            getServer().createWorld(wc);
                            getServer().createWorld(wcC);

                            TextComponent tc = new TextComponent("§aМир загружен!\n§6-> [Нажми, чтобы телепортнуться] <-");
                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad " + id);
                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("*Клик*").create());
                            tc.setClickEvent(ce);
                            tc.setHoverEvent(he);
                            p.spigot().sendMessage(tc);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);
                        }
                    });

                } else {
                    playerCurGameWorld.put(p.getName(), worlds.get(id));
                    p.sendMessage(ChatColor.GREEN + "Телепортация...");
                    p.teleport(new Location(Bukkit.getWorld(gw.spawnName), gw.spawnX, gw.spawnY, gw.spawnZ, (float) gw.spawnYaw, (float) gw.spawnPitch));
                    for (Player p1 : getServer().getOnlinePlayers()) {
                        try {
                            if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                                p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвошёл в игру.");
                            }
                        } catch (Exception ex) {
                        }
                    }
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setFoodLevel(20);
                    p.setExp(0);
                    p.setExhaustion(0);
                    p.setHealthScale(20);
                    p.setHealth(20);
                    p.setSaturation(20);
                    p.setTotalExperience(0);
                    p.setGlowing(false);
                    p.getInventory().clear();
                    p.getInventory().setItem(8, settingsItem);
                    p.getInventory().setItem(4, menu_item);
                    if (worlds.get(id).mode == 0) {
                        p.setGameMode(GameMode.CREATIVE);
                    }

                    if (worlds.get(id).mode == 1) {
                        playerJoinEvent(p, id, null, new PlayerJoinEvent(p, ""));
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "Мир закрыт для обычных игроков.");
            }
        } else {
            if (worlds.get(id).owner.equals(p.getName()) || p.getName().equals("bulat5280")) {

                GameWorld gw = worlds.get(id);


                if (Bukkit.getWorld(gw.spawnName) == null) {


                    getServer().getScheduler().runTask(this, new Runnable() {
                        @Override
                        public void run() {
                            p.sendMessage(ChatColor.YELLOW + "Загрузка... Пожалуйста, подождите.");
                            WorldCreator wc = new WorldCreator("" + Integer.toString(id) + "world");
                            WorldCreator wcC = new WorldCreator("" + Integer.toString(id) + "worldCode");
                            getServer().createWorld(wc);
                            getServer().createWorld(wcC);

                            TextComponent tc = new TextComponent("§aМир загружен!\n§6-> [Нажми, чтобы телепортнуться] <-");
                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ad " + id);
                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("*Клик*").create());
                            tc.setClickEvent(ce);
                            tc.setHoverEvent(he);
                            p.spigot().sendMessage(tc);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);
                        }
                    });


                } else {
                    playerCurGameWorld.put(p.getName(), worlds.get(id));
                    p.sendMessage(ChatColor.GREEN + "Телепортация...");
                    p.teleport(new Location(Bukkit.getWorld(gw.spawnName), gw.spawnX, gw.spawnY, gw.spawnZ, (float) gw.spawnYaw, (float) gw.spawnPitch));


                    for (Player p1 : getServer().getOnlinePlayers()) {
                        try {
                            if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                                p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвошёл в игру.");
                            }
                        } catch (Exception ex) {
                        }
                    }
                    p.setFoodLevel(20);
                    p.setExp(0);
                    p.setExhaustion(0);
                    p.setHealthScale(20);
                    p.setHealth(20);
                    p.setSaturation(20);
                    p.setTotalExperience(0);
                    p.setGlowing(false);
                    p.getInventory().clear();
                    p.getInventory().setItem(8, settingsItem);
                    p.getInventory().setItem(4, menu_item);
                    if (worlds.get(id).mode == 0) {
                        p.setGameMode(GameMode.CREATIVE);
                    } else {
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    if (worlds.get(id).mode == 1) {
                        playerJoinEvent(p, id, null, new PlayerJoinEvent(p, ""));
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "Мир закрыт.");
            }
        }
        if (!worlds.get(id).unique_visitors.contains(p.getName())) {
            worlds.get(id).unique_visitors.add(p.getName());
            reloadWorldData(id);
        }
    }

    @EventHandler
    public void achievment(PlayerAdvancementDoneEvent e) {
        e.getPlayer().getWorld().setGameRuleValue("announceAdvancements", "false");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getWorld().getName().contains("Code")) {
            if (e.getItemInHand().equals(codeItem_event) && e.getBlockAgainst().getData() == 3) {
                //Location chestLoc = e.getBlockPlaced().getLocation().add(0, 1, 0);
                //chestLoc.getBlock().setType(Material.CHEST);
                //chestLoc.getBlock().setData((byte) 3);
                Location secBlockLoc = e.getBlockPlaced().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.DIAMOND_ORE);
                Location signLoc = e.getBlockPlaced().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.WALL_SIGN);
                signLoc.getBlock().setData((byte) 3);
                Sign sign = (Sign) signLoc.getBlock().getState();
                sign.setLine(0, "Событие");
                sign.setLine(1, "ВходИгрока");
                sign.update();
            } else if (e.getItemInHand().equals(codeItem_player) && e.getBlockAgainst().getData() == 8) {
                Location chestLoc = e.getBlockPlaced().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.TRAPPED_CHEST);
                chestLoc.getBlock().setData((byte) 3);
                CraftChest chest = (CraftChest) chestLoc.getBlock().getState();
                chest.setCustomName("Сообщение");
                chest.update();
                chest.getInventory().setItem(25, codeItem_varPlayerGlass);

                Location secBlockLoc = e.getBlockPlaced().getLocation().add(1, 0, 0);
                if (secBlockLoc.getBlock().getType().equals(Material.PISTON_BASE)) {
                    e.getPlayer().sendMessage("§c§lСкобка потеряна. Поставьте её на место. §a§l:)");
                    if (!e.getPlayer().getInventory().contains(Material.PISTON_BASE)) {
                        e.getPlayer().getInventory().addItem(new ItemStack(Material.PISTON_BASE));
                    }
                }
                secBlockLoc.getBlock().setType(Material.STONE);
                Location signLoc = e.getBlockPlaced().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.WALL_SIGN);
                signLoc.getBlock().setData((byte) 3);
                Sign sign = (Sign) signLoc.getBlock().getState();
                sign.setLine(0, "Управление игроком");
                sign.setLine(1, "Сообщение");
                sign.update();
            } else if (e.getItemInHand().equals(codeItem_var) && e.getBlockAgainst().getData() == 8) {
                Location chestLoc = e.getBlockPlaced().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.TRAPPED_CHEST);
                chestLoc.getBlock().setData((byte) 3);
                CraftChest chest = (CraftChest) chestLoc.getBlock().getState();
                chest.setCustomName("Установить значение");

                chest.update();
                for (int i = 0; i < 27; i++) {
                    chest.getInventory().setItem(i, codeItem_emptyGlass);
                }
                chest.getInventory().setItem(2, codeItem_varGlass);
                chest.getInventory().setItem(6, codeItem_valueGlass);
                chest.getInventory().setItem(11, null);
                chest.getInventory().setItem(15, null);
                Location secBlockLoc = e.getBlockPlaced().getLocation().add(1, 0, 0);
                if (secBlockLoc.getBlock().getType().equals(Material.PISTON_BASE)) {
                    e.getPlayer().sendMessage("§c§lСкобка потеряна. Поставьте её на место. §a§l:)");
                    if (!e.getPlayer().getInventory().contains(Material.PISTON_BASE)) {
                        e.getPlayer().getInventory().addItem(new ItemStack(Material.PISTON_BASE));
                    }
                }
                secBlockLoc.getBlock().setType(Material.IRON_ORE);
                Location signLoc = e.getBlockPlaced().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.WALL_SIGN);
                signLoc.getBlock().setData((byte) 3);
                Sign sign = (Sign) signLoc.getBlock().getState();
                sign.setLine(0, "Управление переменными");
                sign.setLine(1, "Установить значение");
                sign.update();
            } else if (e.getItemInHand().equals(codeItem_If) && e.getBlockAgainst().getData() == 8) {
                Location chestLoc = e.getBlockPlaced().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.TRAPPED_CHEST);
                chestLoc.getBlock().setData((byte) 3);
                CraftChest chest = (CraftChest) chestLoc.getBlock().getState();
                chest.setCustomName("Если");

                chest.update();
                for (int i = 0; i < 27; i++) {
                    chest.getInventory().setItem(i, codeItem_emptyGlass);
                }
                chest.getInventory().setItem(4, codeItem_valueGlass);
                chest.getInventory().setItem(13, null);
                Location secBlockLoc = e.getBlockPlaced().getLocation().add(1, 0, 0);
                if (secBlockLoc.getBlock().getType().equals(Material.PISTON_BASE)) {
                    e.getPlayer().sendMessage("§c§lСкобка потеряна. Поставьте её на место. §a§l:)");
                    if (!e.getPlayer().getInventory().contains(Material.PISTON_BASE)) {
                        e.getPlayer().getInventory().addItem(new ItemStack(Material.PISTON_BASE));
                    }
                }
                secBlockLoc.getBlock().setType(Material.PISTON_BASE);
                secBlockLoc.getBlock().setData((byte) 5);
                Location secBlockLoc2 = e.getBlockPlaced().getLocation().add(3, 0, 0);
                secBlockLoc2.getBlock().setType(Material.PISTON_BASE);
                secBlockLoc2.getBlock().setData((byte) 4);
                Location signLoc = e.getBlockPlaced().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.WALL_SIGN);
                signLoc.getBlock().setData((byte) 3);
                Sign sign = (Sign) signLoc.getBlock().getState();
                sign.setLine(0, "Если");
                sign.setLine(1, "Выражение истинно");
                sign.update();
            } else if (e.getItemInHand().equals(codeItem_eventControl) && e.getBlockAgainst().getData() == 8) {
                /*Location chestLoc = e.getBlockPlaced().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.TRAPPED_CHEST);
                chestLoc.getBlock().setData((byte) 3);
                CraftChest chest = (CraftChest) chestLoc.getBlock().getState();
                chest.setCustomName("Управление событием");

                chest.update();
                for (int i = 0; i < 27; i++) {
                    chest.getInventory().setItem(i, codeItem_emptyGlass);
                }
                chest.getInventory().setItem(2, codeItem_varGlass);
                chest.getInventory().setItem(6, codeItem_valueGlass);
                chest.getInventory().setItem(11, null);
                chest.getInventory().setItem(15, null);*/
                Location secBlockLoc = e.getBlockPlaced().getLocation().add(1, 0, 0);
                if (secBlockLoc.getBlock().getType().equals(Material.PISTON_BASE)) {
                    e.getPlayer().sendMessage("§c§lСкобка потеряна. Поставьте её на место. §a§l:)");
                    if (!e.getPlayer().getInventory().contains(Material.PISTON_BASE)) {
                        e.getPlayer().getInventory().addItem(new ItemStack(Material.PISTON_BASE));
                    }
                }
                secBlockLoc.getBlock().setType(Material.COAL_ORE);
                Location signLoc = e.getBlockPlaced().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.WALL_SIGN);
                signLoc.getBlock().setData((byte) 3);
                Sign sign = (Sign) signLoc.getBlock().getState();
                sign.setLine(0, "Управление событием");
                sign.setLine(1, "отмена");
                sign.update();
            } else if (e.getBlockPlaced().getType().equals(Material.PISTON_BASE)) {
            } else {
                e.setCancelled(true);
            }
        }
        if (e.getPlayer().getWorld().getName().endsWith("world") && worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 1) {
            blockPlaceEvent((Entity) e.getPlayer(), Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0]), e, e);
        }
    }

    public void moveCodeRight(Location loc) {
        Block block = loc.getBlock();
        block.getLocation().add(1, 0, 0);
    }

    public Location findClosingBracket(Location firstBracket) {
        Location result = firstBracket.clone();
        try {
            int j = 0;
            for (int i = firstBracket.getBlockX(); i < 256; i++) {
                result = result.add(1, 0, 0);
                if (result.getBlock().getType().equals(Material.PISTON_BASE)) {
                    if (result.getBlock().getData() == (byte) 5) {
                        j++;
                    } else if (result.getBlock().getData() == (byte) 4) {
                        j--;
                    }

                }
                if (j < 0) {
                    break;
                }
            }
        } catch (Exception ex) {

        }
        return result;
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getWorld().getName().contains("Code")) {
            if (e.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                //Location chestLoc = e.getBlock().getLocation().add(0, 1, 0);
                //chestLoc.getBlock().setType(Material.AIR);
                Location secBlockLoc = e.getBlock().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.AIR);
                Location signLoc = e.getBlock().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.AIR);
            } else if (e.getBlock().getType().equals(Material.COBBLESTONE)) {
                Location chestLoc = e.getBlock().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.AIR);
                Location secBlockLoc = e.getBlock().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.AIR);
                Location signLoc = e.getBlock().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.AIR);
            } else if (e.getBlock().getType().equals(Material.IRON_BLOCK)) {
                Location chestLoc = e.getBlock().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.AIR);
                Location secBlockLoc = e.getBlock().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.AIR);
                Location signLoc = e.getBlock().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.AIR);
            } else if (e.getBlock().getType().equals(Material.OBSIDIAN)) {
                Location chestLoc = e.getBlock().getLocation().add(0, 1, 0);
                chestLoc.getBlock().setType(Material.AIR);
                Location secBlockLoc = e.getBlock().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.AIR);
                Location closBr = findClosingBracket(secBlockLoc);
                Location curBlock = secBlockLoc.clone();
                for (int i = secBlockLoc.getBlockX(); i <= closBr.getBlockX(); i++) {
                    curBlock.setX(i);
                    curBlock.getBlock().setType(Material.AIR);
                    Location chestLoc2 = curBlock.clone().add(0, 1, 0);
                    chestLoc2.getBlock().setType(Material.AIR);
                    Location signLoc2 = curBlock.clone().add(0, 0, 1);
                    signLoc2.getBlock().setType(Material.AIR);
                }
                Location signLoc = e.getBlock().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.AIR);
            } else if (e.getBlock().getType().equals(Material.COAL_BLOCK)) {
                Location secBlockLoc = e.getBlock().getLocation().add(1, 0, 0);
                secBlockLoc.getBlock().setType(Material.AIR);
                Location signLoc = e.getBlock().getLocation().add(0, 0, 1);
                signLoc.getBlock().setType(Material.AIR);
            } else if (e.getBlock().getType().equals(Material.PISTON_BASE)) {
            } else {
                e.setCancelled(true);
            }
        }
        if (e.getPlayer().getWorld().getName().endsWith("world") && worlds.get(Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0])).mode == 1) {
            blockBreakEvent((Entity) e.getPlayer(), Integer.parseInt(e.getPlayer().getWorld().getName().split("world")[0]), e, e);
        }
    }

    @EventHandler
    public void pistonExtendEvent(BlockPistonExtendEvent e) {
        if (e.getBlock().getLocation().getWorld().getName().contains("Code")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pistonRetractEvent(BlockPistonRetractEvent e) {
        if (e.getBlock().getLocation().getWorld().getName().contains("Code")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void playerTab(PlayerChatTabCompleteEvent e) {
        if (e.getChatMessage().contains("suffixes")) {
            e.getPlayer().sendMessage("§f /suffixes give [nickname] [suffix] - выдать суффикс игроку");
            e.getPlayer().sendMessage("§f /suffixes remove [nickname] [suffix] - Забрать суффикс у игрока");
            e.getPlayer().sendMessage("§f /suffixes list [nickname] - Посмотреть суффиксы игроков");
        }
    }

    @EventHandler
    public void itemSpawnEvent(ItemSpawnEvent e) {
        if (e.getLocation().getWorld().getName().contains("Code")) {
            e.setCancelled(true);
        }
    }

    void reloadWorldData(int id) {
        File creativeData_file = new File("" + Integer.toString(id) + "worldData.ser");
        try {
            boolean delete = creativeData_file.delete();
            if (delete) {
            }
            boolean newFile = creativeData_file.createNewFile();
            if (newFile) {
            }
            FileOutputStream fos = new FileOutputStream(creativeData_file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(worlds.get(id));
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    Inventory getGamesInventory(Player p, Inventory inv) {
        sortedWorlds = bubbleSort();
        Inventory gamesInv = Bukkit.createInventory(inv.getHolder(), inv.getSize(), inv.getTitle());
        try {
            for (int i = playerCurGamesPage.get(p.getName()) * 45; i < 45; i++) {
                if (sortedWorlds.size() > i) {
                    if (sortedWorlds.get(i).status == 2) {
                        ItemStack item = new ItemStack(Material.DIAMOND);
                        ItemMeta itemM = item.getItemMeta();
                        itemM.setDisplayName(sortedWorlds.get(i).name);
                        List<String> lore = new ArrayList<>();
                        lore.add("");
                        if (sortedWorlds.get(i).mode == 0) {
                            lore.add("§eРежим: §6строительство");
                            item.setType(Material.BRICK);
                        } else if (sortedWorlds.get(i).mode == 1) {
                            lore.add("§eРежим: §aигра");
                        }
                        lore.add("§9Айди: §f" + sortedWorlds.get(i).id);
                        lore.add("§bВладелец: §f" + sortedWorlds.get(i).owner);
                        lore.add("§fУникальные посетители: §f" + Integer.toString(sortedWorlds.get(i).unique_visitors.size()));
                        lore.add("§cЛайки: §f" + Integer.toString(sortedWorlds.get(i).likes.size()));
                        lore.add("");
                        itemM.setLore(lore);
                        item.setItemMeta(itemM);
                        gamesInv.addItem(item);
                    } else if (sortedWorlds.get(i).status == 1) {
                        if (sortedWorlds.get(i).whitelistedPlayers.contains(p.getName())) {
                            ItemStack item = new ItemStack(Material.DIAMOND);
                            ItemMeta itemM = item.getItemMeta();
                            itemM.setDisplayName(sortedWorlds.get(i).name);
                            List<String> lore = new ArrayList<>();
                            lore.add("");
                            if (sortedWorlds.get(i).mode == 0) {
                                lore.add("§eРежим: §6строительство");
                                item.setType(Material.BRICK);
                            } else if (sortedWorlds.get(i).mode == 1) {
                                lore.add("§eРежим: §aигра");
                            }
                            lore.add("§9Айди: §f" + sortedWorlds.get(i).id);
                            lore.add("§bВладелец: §f" + sortedWorlds.get(i).owner);
                            lore.add("§fУникальные посетители: §f" + Integer.toString(sortedWorlds.get(i).unique_visitors.size()));
                            lore.add("§cЛайки: §f" + Integer.toString(sortedWorlds.get(i).likes.size()));
                            lore.add("");
                            itemM.setLore(lore);
                            item.setItemMeta(itemM);
                            gamesInv.addItem(item);
                        }
                    } else if (sortedWorlds.get(i).status == 0) {
                        if (sortedWorlds.get(i).owner.equals(p.getName())) {
                            ItemStack item = new ItemStack(Material.DIAMOND);
                            ItemMeta itemM = item.getItemMeta();
                            itemM.setDisplayName(sortedWorlds.get(i).name);
                            List<String> lore = new ArrayList<>();
                            lore.add("");
                            if (sortedWorlds.get(i).mode == 0) {
                                lore.add("§eРежим: §6строительство");
                                item.setType(Material.BRICK);
                            } else if (sortedWorlds.get(i).mode == 1) {
                                lore.add("§eРежим: §aигра");
                            }
                            lore.add("§9Айди: §f" + sortedWorlds.get(i).id);
                            lore.add("§bВладелец: §f" + sortedWorlds.get(i).owner);
                            lore.add("§fУникальные посетители: §f" + Integer.toString(sortedWorlds.get(i).unique_visitors.size()));
                            lore.add("§cЛайки: §f" + Integer.toString(sortedWorlds.get(i).likes.size()));
                            lore.add("");
                            itemM.setLore(lore);
                            item.setItemMeta(itemM);
                            gamesInv.addItem(item);
                        }
                    }
                } else {
                    break;
                }
            }

            if (playerCurGamesPage.get(p.getName()) > 0) {
                ItemStack lastArrowItem = new ItemStack(Material.ARROW);
                ItemMeta lastArrowItemMeta = lastArrowItem.getItemMeta();
                lastArrowItemMeta.setDisplayName("§f<-");
                lastArrowItem.setItemMeta(lastArrowItemMeta);
                gamesInv.setItem(45, lastArrowItem);
            }
            if (gamesInv.getItem(44) != null) {
                ItemStack ArrowItem = new ItemStack(Material.ARROW);
                ItemMeta ArrowItemMeta = ArrowItem.getItemMeta();
                ArrowItemMeta.setDisplayName("§f->");
                ArrowItem.setItemMeta(ArrowItemMeta);
                gamesInv.setItem(53, ArrowItem);
            }
        } catch (Exception ex) {
            p.sendMessage("§cПроизошла ошибка:\n" + ex.getMessage());
        }
        return gamesInv;
    }

    /*public boolean processLine(String line, Player p, int firstId, HashMap<String, Var> variable) {
        try {
            String function = line.split("~")[firstId];
            String functionName = function.split("#")[0];
            if (functionName.equals("Сообщение")) {
                for (int i = 1; i < function.split("#").length; i++) {
                    if (function.split("#")[i].contains("@")) {
                        p.sendMessage(variable.get(function.split("#")[i].split("@")[1]).getValue().toString());
                    } else {
                        p.sendMessage(function.split("#")[i]);
                    }
                }
            } else if (functionName.equals("Установить значение")) {
                if (function.split("#")[2].contains("@")) {
                    variable.put(function.split("#")[1].split("@")[1], new Var(variable.get(function.split("#")[2].split("@")[1])));
                } else {
                    variable.put(function.split("#")[1].split("@")[1], new Var(function.split("#")[2]));
                }
            }
            if (line.split("~").length > firstId + 1) {
                processLine(line, p, firstId + 1, variable);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public boolean playerJoinEvent(Player p, String code, HashMap<String, Var> variable) {
        try {
            String[] lines = code.split("/n");
            for (String line : lines) {
                String event = line.split("~")[0];
                if (event.equals("ВходИгрока")) {
                    processLine(line, p, 1, variable);
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    public boolean playerExitEvent(Player p, String code, HashMap<String, Var> variable) {
        try {
            String[] lines = code.split("/n");
            for (String line : lines) {
                String event = line.split("~")[0];
                if (event.equals("ВыходИгрока")) {
                    processLine(line, p, 1, variable);
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }*/
    /*public void procLine(String e, Player player, int id) { // process lines with event e, from world id

        GameWorld world = worlds.get(Integer.parseInt(player.getName().split("world")[0]));
        String[] events = world.code.code.split("/n"); // split into lines (in block code)

        for (String event : events) {
            String[] funcs = event.split("~"); // split into block (in block code)

            if (funcs[0].equals(e)) { // if block (in block code) == event e

                // delete event from funcs array
                List<String> convertlist1 = new ArrayList<>(Arrays.asList(funcs));
                convertlist1.remove(e);

                funcs = convertlist1.toArray(new String[convertlist1.size()]);

                // process funcs
                for (String func : funcs) {
                    String[] args = func.split("#"); // split into args (in block code)
                    String f = args[0]; // set f = func name

                    List<String> listargs = new ArrayList<>(Arrays.asList(args));
                    listargs.remove(f);

                    args = listargs.toArray(new String[listargs.size()]);


                    // func sendMessage
                    if (f.equals("Сообщение")) {
                        String message = "";

                        for (int i = 0; i < args.length; i++) {
                            message += convertArg(args[i], world).toString();
                        }
                        player.sendMessage(message);

                    } else if (f.equals("Установить значение")) {
                        if (args.length == 2) {
                            world.code.variable.put(args[0].substring(1), new Var(convertArg(args[1], world)));

                        } else throw new ArrayIndexOutOfBoundsException("Exception: args is not equals 2!");
                    } else if (f.equals("Выражение истинно")) {

                    }
                }
            }
        }
    }*/

    public void procLine2(String e, Entity entity, int id, int curFunc, Cancellable c, Event ev) { // process lines with event e, from world id

        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
        }
        GameWorld world = worlds.get(Integer.parseInt(player.getName().split("world")[0]));
        String[] events = world.code.code.split("/n"); // split into lines (in block code)

        for (String event : events) {
            String[] funcs = event.split("~"); // split into block (in block code)

            if (funcs[0].equals(e)) { // if block (in block code) == event e

                // process funcs
                String[] args = funcs[curFunc].split("#"); // split into args (in block code)
                String f = args[0]; // set f = func name

                List<String> listargs = new ArrayList<>(Arrays.asList(args));
                listargs.remove(f);

                args = listargs.toArray(new String[listargs.size()]);
                boolean continue_ = true;

                // func sendMessage
                if (f.equals("Сообщение")) {
                    String message = "";

                    for (int i = 1; i < args.length; i++) {
                        message += convertArg(args[i], world, ev).toString();
                    }
                    ((Player) convertArg(args[0], world, ev)).sendMessage(message);


                } else if (f.equals("Установить значение")) {
                    if (args.length == 2) {
                        world.code.variable.put(args[0].substring(1), new Var(convertArg(args[1], world, ev)));
                    } else throw new ArrayIndexOutOfBoundsException("Exception: args is not equals 2!");
                } else if (f.equals("Выражение истинно")) {

                    continue_ = false;
                    int nextBracket = findLastBracket(curFunc + 2, event, player);
                    String IfCode = getCodeBetween(curFunc, nextBracket, event, player);
                    executeIf(IfCode, id, e, player, curFunc, nextBracket, c, ev);
                } else if (f.equals("отмена")) {
                    c.setCancelled(true);
                } else if (f.equals("РежимИгры")) {
                    if (convertArg(args[1], world, ev).equals(0) || args[1].equals("survival"))
                        ((Player) convertArg(args[0], world, ev)).setGameMode(GameMode.SURVIVAL);
                    if (convertArg(args[1], world, ev).equals(1) || args[1].equals("creative"))
                        ((Player) convertArg(args[0], world, ev)).setGameMode(GameMode.CREATIVE);
                    if (convertArg(args[1], world, ev).equals(2) || args[1].equals("adventure"))
                        ((Player) convertArg(args[0], world, ev)).setGameMode(GameMode.SURVIVAL);
                    if (convertArg(args[1], world, ev).equals(3) || args[1].equals("spectator"))
                        ((Player) convertArg(args[0], world, ev)).setGameMode(GameMode.SPECTATOR);
                } else if (f.equals("Получить имя игрока")) {
                    if (args.length == 2) {
                        world.code.variable.put(args[0].substring(1), new Var(((Player) convertArg(args[1], world, ev)).getName()));
                    } else throw new ArrayIndexOutOfBoundsException("Exception: args is not equals 2!");
                }

                if (continue_ && curFunc < funcs.length - 1) {
                    procLine2(e, (Entity) player, id, curFunc + 1, c, ev);
                }
            }
        }
    }

    public int findLastBracket(int id, String code, Player p) {
        int counter = 0;
        int result = id;
        for (int i = id; i < code.split("~").length; i++) {

            String f = code.split("~")[i];
            if (f.equals("{")) {
                counter++;
            }
            if (f.equals("}")) {
                counter--;
            }
            if (counter < 0) {
                result = i;
                break;
            }
        }
        return result;
    }

    public String getCodeBetween(int first, int last, String code, Player p) {
        String result = "";
        for (int i = first; i < last; i++) {
            result += code.split("~")[i];
            result += "~";
        }
        return result;
    }

    public void executeIf(String code, int id, String e, Player player, int before, int after, Cancellable c, Event ev) {
        String[] funcs = code.split("~");
        String[] args = funcs[0].split("#");
        if ((Boolean) convertArg(args[1], worlds.get(id), ev)) {
            procLine2(e, (Entity) player, id, before + 1, c, ev);
        } else {
            procLine2(e, (Entity) player, id, after, c, ev);
        }
    }

    public Object[] convertArgs(String[] args, GameWorld world, Event e) {
        // convert arguments
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i].toString();
            result[i] = convertArg(arg, world, e);
        }

        return result;
    }

    public Object convertArg(String arg, GameWorld world, Event e) {
        if (arg.startsWith("@")) {
            return world.code.variable.get(arg.substring(1)).getValue();
        } else if (isNumeric(arg)) {
            return Integer.parseInt(arg);
        } else if (arg.equals("true")) {
            return true;
        } else if (arg.equals("false")) {
            return false;
        } else if (arg.startsWith("∝")) {
            if (arg.substring(1).equals("Игрок")) {
                return ((PlayerEvent) e).getPlayer();
            } else if (arg.substring(1).equals("Сущность")) {
                return ((EntityEvent) e).getEntity();
            } else if (arg.substring(1).equals("Урон")) {
                return ((EntityDamageEvent) e).getDamage();
            } else if (arg.substring(1).equals("Окончательный урон")) {
                return ((EntityDamageEvent) e).getFinalDamage();
            } else {
                return null;
            }
        } else {
            return arg;
        }
    }

    public static String toString(Object[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public void playerJoinEvent(Player p, int id, Cancellable c, Event ev) {
        procLine2("ВходИгрока", (Entity) p, id, 1, c, ev);
    }

    public void playerExitEvent(Player p, int id, Cancellable c, Event ev) {
        procLine2("ВыходИгрока", (Entity) p, id, 1, c, ev);
    }

    public void entityDamageEvent(Entity e, int id, Cancellable c, Event ev) {
        procLine2("СущностьПолучаетУрон", e, id, 1, c, ev);
    }

    public void blockBreakEvent(Entity e, int id, Cancellable c, Event ev) {
        procLine2("ЛоманиеБлокаИгроком", e, id, 1, c, ev);
    }

    public void blockPlaceEvent(Entity e, int id, Cancellable c, Event ev) {
        procLine2("УстановкаБлокаИгроком", e, id, 1, c, ev);
    }


    public String compileArg (Chest ch, int[] nums) {

        for (int num : nums) {
            if (ch.getBlockInventory().getItem(26) != null) {

                if (ch.getBlockInventory().getItem(13).getType().equals(Material.MAGMA_CREAM)) {
                    return "#@" + ch.getBlockInventory().getItem(13).getItemMeta().getDisplayName();
                } else if (ch.getBlockInventory().getItem(13).getType().equals(Material.APPLE)) {
                    return "#∝" + ch.getBlockInventory().getItem(13).getItemMeta().getDisplayName();
                } else {
                    return "#" + ch.getBlockInventory().getItem(13).getItemMeta().getDisplayName();
                }
            }
        }
        return "";
    }

    public String compileCode(int worldID) {
        String code = "";
        World world = Bukkit.getWorld(Integer.toString(worldID) + "worldCode");
        for (int z = 2; z < 256; z++) {
            Location curBlock = new Location(world, 2, 1, z);
            if (curBlock.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                for (int i = 2; i < 254; i++) {
                    Location curBlockI = new Location(world, i, 1, z);
                    Location signLoc = curBlockI.clone().add(0, 0, 1);
                    if (curBlockI.getBlock().getType().equals(Material.PISTON_BASE)) {
                        if (curBlockI.getBlock().getData() == (byte) 5) code += "{~";
                        if (curBlockI.getBlock().getData() == (byte) 4) code += "}~";
                    }
                    try {
                        if (signLoc.getBlock().getType().equals(Material.WALL_SIGN)) {


                            Sign sign = (Sign) signLoc.getBlock().getState();
                            String command = sign.getLine(1);
                            code += command;

                            if (curBlockI.getBlock().getType().equals(Material.COBBLESTONE)) {
                                Chest ch = (Chest) curBlockI.add(0, 1, 0).getBlock().getState();
                                if (command.equals("Сообщение")) {
                                    code += compileArg(ch, new int[] {26});
                                    for (int j = 0; j < 25; j++) {
                                        code += compileArg(ch, new int[] {j} );
                                    }
                                } else if (command.equals("РежимИгры")) {
                                    code += compileArg(ch, new int[] {11, 13 });
                                }
                            } else if (curBlockI.getBlock().getType().equals(Material.IRON_BLOCK)) {
                                Chest ch = (Chest) curBlockI.add(0, 1, 0).getBlock().getState();
                                if (command.equals("Установить значение")) {
                                    code += compileArg(ch, new int[] {11, 15});

                                } else if (command.equals("Получить имя игрока")) {
                                    code += compileArg(ch, new int[] {11, 15});
                                }
                            } else if (curBlockI.getBlock().getType().equals(Material.OBSIDIAN)) {
                                Chest ch = (Chest) curBlockI.add(0, 1, 0).getBlock().getState();
                                code += compileArg(ch, new int[] {13});

                            } else if (curBlockI.getBlock().getType().equals(Material.COAL_BLOCK)) {}

                            code += "~";

                        }
                    } catch (Exception ex) {
                    }

                }
                code += "/n";
            }
        }
        return code;
    }



    public List<GameWorld> bubbleSort() {
        int stepsCount = worlds.size() - 1;
        // Объявляем переменную swapped, значение которой показывает был ли
        // совершен обмен элементов во время перебора массива
        boolean swapped;
        // do..while цикл. Работает почти идентично while
        // Разница в проверке. Тут она делается не до выполнения тела, а после
        // Такой цикл полезен там, где надо выполнить тело хотя бы раз в любом случае
        List<GameWorld> result = new ArrayList<>();
        for (Map.Entry<Integer, GameWorld> entry : worlds.entrySet()) {
            int key = entry.getKey();
            GameWorld value = entry.getValue();
            result.add(value);

            // do what you have to do here
            // In your case, another loop.
        }
        do {
            swapped = false;
            // Перебираем массив и меняем местами элементы, если предыдущий
            // больше, чем следующий
            for (int i = 0; i < stepsCount; i++) {
                GameWorld gw = result.get(i);
                GameWorld gw1 = result.get(i + 1);
                if (gw.unique_visitors.size() < gw1.unique_visitors.size()) {
                    // temp – временная константа для хранения текущего элемента
                    GameWorld temp = gw;
                    result.set(i, gw1);
                    result.set(i + 1, temp);
                    // Если сработал if и была совершена перестановка,
                    // присваиваем swapped значение true
                    swapped = true;
                }
            }
            // Уменьшаем счетчик на 1, т.к. самый большой элемент уже находится
            // в конце массива
            stepsCount--;
        } while (swapped); // продолжаем, пока swapped == true
        return result;
    }

    public void textToCode(Player player, String code) throws Exception {
        World world = player.getWorld(); // мир
        Location pos = player.getTargetBlock(null, 10).getLocation(); // стартовая точка

        if (pos.getBlock().getType() != Material.GLASS)
            throw new Exception("The player is not looking at the blue glass"); // тута именно синее стекло но я хз как
        if (!world.getName().contains("worldCode")) throw new Exception("World is not code"); // проверка на мир

        String[] events = code.split("/n"); // ивенты

        if (events.length > 0) { // если не 0

            for (String event : events) {

                String[] funcs = event.split("~"); // действия
                if (funcs.length > 0) { // если не 0

                    String e = funcs[0]; // e - название ивента
                    System.arraycopy(funcs, 1, funcs, 0, funcs.length - 1); // удалить e из funcs

                    if (getConfig().getStringList("events").contains(e)) { // если ивент существует

                        // поставка ивента
                        pos.getBlock().setType(Material.DIAMOND_BLOCK);
                        pos.add(0, 0, 1).getBlock().setType(Material.SIGN);
                        Sign esign = (Sign) pos.add(0, 0, 1).getBlock().getState();
                        esign.setLine(0, "Событие");
                        esign.setLine(1, e);
                        pos.add(1, 0, 0).getBlock().setType(Material.DIAMOND_ORE);

                        for (String func : funcs) {

                            String[] args = func.split("#");
                            if (args.length > 0) { // если не 0

                                String f = args[0]; // f - название действия
                                System.arraycopy(args, 1, args, 0, funcs.length - 1); // удалить f из args

                                if (getConfig().getStringList("player").contains(f)) { // если действие игрока

                                    // поставка действия игрока
                                    pos.getBlock().setType(Material.STONE);
                                    pos.add(0, 0, 1).getBlock().setType(Material.SIGN);
                                    Sign fsign = (Sign) pos.add(0, 0, 1).getBlock().getState();
                                    fsign.setLine(0, "Управление игроком");
                                    fsign.setLine(1, f);
                                    pos.add(1, 0, 0).getBlock().setType(Material.COBBLESTONE);

                                } else if (getConfig().getStringList("var").contains(f)) { // если переменная

                                    // поставка действия над переменной
                                    pos.getBlock().setType(Material.IRON_BLOCK);
                                    pos.add(0, 0, 1).getBlock().setType(Material.SIGN);
                                    Sign fsign = (Sign) pos.add(0, 0, 1).getBlock().getState();
                                    fsign.setLine(0, "Управление переменными");
                                    fsign.setLine(1, f);
                                    pos.add(1, 0, 0).getBlock().setType(Material.IRON_ORE);

                                    // тут можно на условие проверку

                                } else if (!f.equals("{")) {
                                    throw new Exception("Action not exist"); // если не действие
                                }

                                pos.setX(pos.getX() + 2); // движение дальше по действиям

                            }
                            throw new Exception("Args is 0"); // если аргументов нет
                        }
                    } else throw new Exception("Event not exist"); // если события нет
                } else throw new Exception("Funcs is 0"); // если действий нет
            }
            pos.setX(2); // возврощение на синее стекло
            pos.setZ(pos.getZ() + 3); // тут не 3, а чередование 3 и 4, но это сам сделаешь))))
        } else throw new Exception("Events is 0");

    }

    public class CommandTTC implements CommandExecutor { // text to code

        @Override

        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            try {
                textToCode((Player) sender, String.join(" ", args));
                return true;
            } catch (Exception ex) {
                sender.sendMessage(ex.toString());
                return false;
            }
        }
    }

    public class CommandAd implements CommandExecutor {

        @Override

        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length > 0) {
                    if (isNumeric(args[0])) {
                        if (worlds.containsKey(Integer.parseInt(args[0]))) {
                            teleportToWorld(Integer.parseInt(args[0]), p);
                        } else {
                            p.sendMessage(ChatColor.RED + "Мир не найден.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Пожалуйста, введите число: " + ChatColor.WHITE + "/ad [айди]");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Пожалуйста, введите число: " + ChatColor.WHITE + "/ad [айди]");
                }
            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandDonate implements CommandExecutor {

        @Override

        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                TextComponent tc = new TextComponent("§a§lДонат §e§l§n(клик)");
                ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://qiwi.com/p/79867150064");
                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Пожертвование на развитие сервера :)").create());

                tc.setClickEvent(ce);
                tc.setHoverEvent(he);
                p.spigot().sendMessage(tc);
            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandGames implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.openInventory(getGamesInventory(p, creative_inv));


            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandTp implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.isOp()) {
                    if (args.length > 0) {
                        try {
                            Location to = Bukkit.getPlayer(args[0]).getLocation();
                            if (to.getWorld().equals(spawn_world)) {
                                if (p.getWorld().equals(spawn_world)) {
                                    p.teleport(to);
                                    p.sendMessage("Оба игрока на спавне, телепортирую :)");
                                }
                            }
                            if (!to.getWorld().equals(spawn_world)) {
                                teleportToWorld(Integer.parseInt(getServer().getPlayer(args[0]).getWorld().getName().split("world")[0]), p);
                                p.teleport(to);
                                p.sendMessage("Цель находится в какой-то игре, телепортирую :)");
                            } else if (!p.getWorld().equals(spawn_world)) {
                                p.sendMessage("Цель находится на спавне, телепортирую :)");
                                p.setGameMode(GameMode.ADVENTURE);
                                p.setFoodLevel(20);
                                p.setExp(0);
                                p.setExhaustion(0);
                                p.setHealthScale(20);
                                p.setHealth(20);
                                p.setSaturation(20);
                                p.setTotalExperience(0);
                                p.setGlowing(false);
                                p.getInventory().clear();
                                p.getInventory().setItem(4, menu_item);
                                try {
                                    if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])) != null) {
                                        if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).mode == 1) {
                                            playerExitEvent(p, Integer.parseInt(p.getWorld().getName().split("world")[0]), null, null);
                                        }
                                        for (Player p1 : getServer().getOnlinePlayers()) {

                                            if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])) != null) {
                                                if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                                                    p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвышел из игры.");
                                                }
                                            }

                                        }
                                    }
                                } catch (Exception ex) {
                                }

                                p.teleport(to);
                            }

                        } catch (Exception ex) {
                            p.sendMessage("Произошла ошибка((( Скорее всего прост не существует цели...");
                        }
                    }
                }
            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandSpawn implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.setGameMode(GameMode.ADVENTURE);
                p.setFoodLevel(20);
                p.setExp(0);
                p.setExhaustion(0);
                p.setHealthScale(20);
                p.setHealth(20);
                p.setSaturation(20);
                p.setTotalExperience(0);
                p.setGlowing(false);
                p.getInventory().clear();
                p.getInventory().setItem(4, menu_item);
                p.teleport(new Location(spawn_world, 23.5, 13, -22.5, -180, 0));
                try {
                    if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])) != null) {

                        if (worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).mode == 1) {
                            playerExitEvent(p, Integer.parseInt(p.getWorld().getName().split("world")[0]), null, null);
                        }
                        for (Player p1 : getServer().getOnlinePlayers()) {
                            try {
                                if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])) != null) {
                                    if (worlds.get(Integer.parseInt(p1.getWorld().getName().split("world")[0])).id == Integer.parseInt(p.getWorld().getName().split("world")[0])) {
                                        p1.sendMessage("§6 ♦ §a" + p.getName() + " §fвышел из игры.");
                                    }
                                }
                            } catch (Exception ex) {
                            }
                        }
                        playerCurGameWorld.remove(p.getName());
                    }

                } catch (Exception ex) {
                }
            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandMsg implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                try {
                    Player to = Bukkit.getPlayer(args[0]);
                    String msg = "";
                    for (int i = 1; i < args.length; i++) {
                        msg += args[i] + " ";
                    }
                    to.sendMessage("§6 * §f§n" + p.getName() + "§f-> тебе: §e" + msg);
                    p.sendMessage("§6 * §fты -> §f§n" + to.getName() + "§f: §e" + msg);
                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandSuffix implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;

                if (args.length == 0 || args[0].equals("")) {
                    Inventory inv = Bukkit.createInventory(null, 54, "Суффиксы");
                    ItemStack resetItem = new ItemStack(Material.BARRIER);
                    ItemMeta resetItemMeta = resetItem.getItemMeta();
                    resetItemMeta.setDisplayName("§fНажми, чтобы очистить");
                    resetItem.setItemMeta(resetItemMeta);
                    inv.setItem(0, resetItem);
                    if (suffixes.containsKey(p.getName())) {
                        for (int i = 0; i < suffixes.get(p.getName()).length; i++) {

                            if (i < 53) {
                                ItemStack item = new ItemStack(Material.PAPER);
                                ItemMeta itemM = item.getItemMeta();
                                itemM.setDisplayName(suffixes.get(p.getName())[i]);
                                if (curSuffix.containsKey(sender.getName())) {
                                    if (suffixes.get(p.getName())[i].equals(curSuffix.get(sender.getName()))) {
                                        itemM.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                                        itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    }
                                }
                                List<String> lore = new ArrayList<>();
                                lore.add("");
                                lore.add(" §fЛКМ, чтобы выбрать.");
                                lore.add("");
                                itemM.setLore(lore);
                                item.setItemMeta(itemM);
                                inv.setItem(i + 1, item);
                            }

                        }
                        p.openInventory(inv);
                    }
                } else {
                    if (p.isOp()) {
                        if (args[0].equals("give")) {
                            if (args.length == 3) {
                                if (suffixes.containsKey(args[1])) {
                                    List<String> array = new ArrayList<>(Arrays.asList(suffixes.get(args[1])));
                                    args[2] = args[2].replace("&", "§");
                                    array.add(args[2]);
                                    suffixes.put(args[1], (String[]) array.toArray(new String[array.size()]));
                                } else {
                                    List<String> array = new ArrayList<>();
                                    args[2] = args[2].replace("&", "§");
                                    array.add(args[2]);
                                    suffixes.put(args[1], (String[]) array.toArray(new String[array.size()]));
                                }
                            }
                        }
                        if (args[0].equals("remove")) {
                            if (args.length == 3) {
                                if (suffixes.containsKey(args[1])) {
                                    List<String> array = new ArrayList<>(Arrays.asList(suffixes.get(args[1])));
                                    args[2] = args[2].replace("&", "§");
                                    array.remove(args[2]);
                                    suffixes.put(args[1], (String[]) array.toArray(new String[array.size()]));
                                    if (curSuffix.containsKey(args[1])) {
                                        if (curSuffix.get(args[1]).equals(args[2])) {
                                            curSuffix.remove(args[1]);
                                        }
                                    }
                                }
                            }
                        }
                        if (args[0].equals("list")) {
                            if (args.length == 2) {
                                if (suffixes.containsKey(args[1])) {
                                    for (int i = 0; i < suffixes.get(args[1]).length; i++) {
                                        p.sendMessage(suffixes.get(args[1])[i]);
                                    }

                                }
                            }
                        }
                    }
                    if (args[0].equals("help")) {
                        p.sendMessage("§f /suffixes - открыть меню");
                        p.sendMessage("§f /suffixes give [nickname] [suffix] - выдать суффикс игроку");
                        p.sendMessage("§f /suffixes remove [nickname] [suffix] - забрать суффикс у игрока");
                        p.sendMessage("§f /suffixes list [nickname] - посмотреть суффиксы игрока");

                    }
                }


            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandBan implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                try {

                    int d = new Integer(args[1].split("d")[0]);
                    int h = new Integer(args[1].split("d")[1].split("h")[0]);
                    int m = new Integer(args[1].split("d")[1].split("h")[1].split("m")[0]);
                    int s = new Integer(args[1].split("d")[1].split("h")[1].split("m")[1].split("s")[0]);
                    Date date = new Date();
                    int time = d * 24 * 60 * 60 + h * 60 * 60 + m * 60 + s;
                    String reason = "";
                    for (int i = 2; i < args.length; i++) {
                        reason += args[i] + " ";
                    }

                    if (!bannedPlayers.contains(new BannedPlayer(args[0], (long) date.getTime() / 1000 + time, reason))) {
                        bannedPlayers.add(new BannedPlayer(args[0], (long) date.getTime() / 1000 + time, reason));
                    }
                    for (Player player : getServer().getOnlinePlayers()) {
                        player.sendMessage("§6Наказания §8>> §9" + name + " §fзабанил §7" + args[0] + " §fна §b" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд. §fПричина: §6" + reason);
                        if (player.getName().equals(args[0])) {
                            player.kickPlayer("§6Вы забанены\n§fПричина: §6" + reason + "\n§fОсталось: §a" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд\n§6Если вы считаете, что наказание выдано неверно,\n§6оставьте заявку на разбан:\n§9https://discord.gg/UJk7GxN59w");

                        }
                    }


                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandRank implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                try {
                    if (args[0].equals("set")) {
                        ranks.put(args[1], args[2]);
                        for (Player p : getServer().getOnlinePlayers()) {
                            p.sendMessage("§6Ранги §8>> §9" + name + " §fустановил ранг \"" + args[2] + "\" игроку §7" + args[1]);
                        }
                    } else if (args[0].equals("remove")) {
                        ranks.remove(args[1]);
                        for (Player p : getServer().getOnlinePlayers()) {
                            p.sendMessage("§6Ранги §8>> §9" + name + " §fудалил ранг игроку §7" + args[1]);
                        }
                    }
                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandMute implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                try {

                    int d = new Integer(args[1].split("d")[0]);
                    int h = new Integer(args[1].split("d")[1].split("h")[0]);
                    int m = new Integer(args[1].split("d")[1].split("h")[1].split("m")[0]);
                    int s = new Integer(args[1].split("d")[1].split("h")[1].split("m")[1].split("s")[0]);
                    Date date = new Date();
                    int time = d * 24 * 60 * 60 + h * 60 * 60 + m * 60 + s;
                    String reason = "";
                    for (int i = 2; i < args.length; i++) {
                        reason += args[i] + " ";
                    }

                    if (!mutedPlayers.contains(new MutedPlayer(args[0], (long) date.getTime() / 1000 + time, reason))) {
                        mutedPlayers.add(new MutedPlayer(args[0], (long) date.getTime() / 1000 + time, reason));
                    }
                    for (Player player : getServer().getOnlinePlayers()) {
                        player.sendMessage("§6Наказания §8>> §9" + name + " §fзамутил §7" + args[0] + " §fна §b" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд. §fПричина: §6" + reason);
                    }


                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandBanIp implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                try {

                    int d = new Integer(args[1].split("d")[0]);
                    int h = new Integer(args[1].split("d")[1].split("h")[0]);
                    int m = new Integer(args[1].split("d")[1].split("h")[1].split("m")[0]);
                    int s = new Integer(args[1].split("d")[1].split("h")[1].split("m")[1].split("s")[0]);
                    Date date = new Date();
                    int time = d * 24 * 60 * 60 + h * 60 * 60 + m * 60 + s;
                    String reason = "";
                    for (int i = 2; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                    for (Player player : getServer().getOnlinePlayers()) {
                        player.sendMessage("§6Наказания §8>> §9" + name + " §fзабанил по айпи §7" + args[0] + " §fна §b" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд. §fПричина: §6" + reason);
                        if (player.getName().equals(args[0]) || player.getAddress().getHostName().equals(args[0])) {
                            args[0] = player.getAddress().getHostName();
                            player.kickPlayer("§6Вы забанены\n§fПричина: §6" + reason + "\n§fОсталось: §a" + d + " дней, " + h + " часов, " + m + " минут, " + s + " секунд\n§6Если вы считаете, что наказание выдано неверно,\n§6оставьте заявку на разбан:\n§9https://discord.gg/UJk7GxN59w");
                        }

                    }
                    if (args[0].contains(".")) {
                        if (!bannedIps.contains(new BannedIp(args[0], (long) date.getTime() / 1000 + time, reason))) {
                            bannedIps.add(new BannedIp(args[0], (long) date.getTime() / 1000 + time, reason));
                        }
                    }

                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandPardon implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                for (int i = 0; i < bannedPlayers.size(); i++) {
                    if (args[0].equals(bannedPlayers.get(i).name)) {
                        bannedPlayers.remove(i);
                    }
                }
                for (int i = 0; i < bannedIps.size(); i++) {
                    if (args[0].equals(bannedIps.get(i).ip)) {
                        bannedIps.remove(i);
                    }
                }
                for (Player player : getServer().getOnlinePlayers()) {
                    player.sendMessage("§6Наказания §8>> §9" + name + " §fразбанил §7" + args[0]);
                }

            }
            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;

        }
    }

    public class CommandUnmute implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player && ((Player) sender).isOp() || sender instanceof ConsoleCommandSender) {
                String name = "§9CONSOLE";
                if (sender instanceof Player) {
                    name = ((Player) sender).getName();
                }
                for (int i = 0; i < mutedPlayers.size(); i++) {
                    if (args[0].equals(mutedPlayers.get(i).name)) {
                        mutedPlayers.remove(i);
                    }
                }
                for (Player player : getServer().getOnlinePlayers()) {
                    player.sendMessage("§6Наказания §8>> §9" + name + " §fразмутил §7" + args[0]);
                }

            }
            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;

        }
    }

    public class CommandLike implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                try {
                    if (playerCurGameWorld.containsKey(p.getName())) {
                        if (!worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).likes.contains(p.getName())) {
                            worlds.get(Integer.parseInt(p.getWorld().getName().split("world")[0])).likes.add(p.getName());
                            reloadWorldData(Integer.parseInt(p.getWorld().getName().split("world")[0]));
                            sender.sendMessage("Успешно.");
                        } else {
                            sender.sendMessage("Тебе не жирно? 1 лайк максимум.");
                        }
                    } else {
                        sender.sendMessage("Согласен, мир спавна самый лучший.");
                    }

                } catch (Exception ex) {

                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }

    public class CommandSetWorld implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length == 3) {
                    args[2] = args[2].replace("&", "§");
                    switch (args[1]) {
                        case "name":
                            worlds.get(Integer.parseInt(args[0])).name = args[2];
                            break;
                        case "owner":
                            worlds.get(Integer.parseInt(args[0])).owner = args[2];
                            break;
                        case "mode":
                            worlds.get(Integer.parseInt(args[0])).mode = Integer.parseInt(args[2]);
                            break;
                        case "status":
                            worlds.get(Integer.parseInt(args[0])).status = Integer.parseInt(args[2]);
                            break;
                        case "spawnX":
                            worlds.get(Integer.parseInt(args[0])).spawnX = Integer.parseInt(args[2]);
                            break;
                        case "spawnY":
                            worlds.get(Integer.parseInt(args[0])).spawnY = Integer.parseInt(args[2]);
                            break;
                        case "spawnZ":
                            worlds.get(Integer.parseInt(args[0])).spawnZ = Integer.parseInt(args[2]);
                            break;
                        case "spawnYaw":
                            worlds.get(Integer.parseInt(args[0])).spawnYaw = Integer.parseInt(args[2]);
                            break;
                        case "spawnPitch":
                            worlds.get(Integer.parseInt(args[0])).spawnPitch = Integer.parseInt(args[2]);
                            break;
                    }
                    reloadWorldData(worlds.get(Integer.parseInt(args[0])).id);
                } else {
                    sender.sendMessage("/sw <id> <name/owner/mode/status/spawn[X/Y/Z/Yaw/Pitch]> <value>");
                }

            }

            // Если игрок (или консоль) правильно использует нашу команду, мы можем возвращать истину
            return true;
        }
    }
}