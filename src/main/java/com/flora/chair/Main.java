package com.flora.chair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener
{
    public static Map<Player, Entity> chairDataMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("chair"))) return false;
        if (!(sender instanceof Player)) return false;
        if (!(((Entity) sender).isOnGround())) return false;

        Player player = (Player) sender;

        Location location = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY() - 1.7, player.getLocation().getZ());
        location.setDirection(player.getLocation().getDirection());

        /* Spawn Chair */
        ArmorStand chair = (ArmorStand) Objects.requireNonNull(player.getLocation().getWorld()).spawnEntity(location, EntityType.ARMOR_STAND);
        chair.setGravity(false);
        chair.setInvisible(true);

        for (EquipmentSlot slot : EquipmentSlot.values())
            chair.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);

        chair.addScoreboardTag("chair");
        Objects.requireNonNull(chair.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(1.0);


        /* Sitting Player */
        chair.addPassenger(player);
        chairDataMap.put(player, chair);

        return false;
    }



    @EventHandler
    private void onStandUpAction(EntityDismountEvent event)
    {
        if (isStandUpInChair(event.getDismounted(), event.getEntityType())) {
            event.getDismounted().remove();
            chairDataMap.put((Player) event.getEntity(), null);
        }
    }

    private boolean isStandUpInChair(Entity entity, EntityType type)
    {
        if (!(entity instanceof ArmorStand)) return false;
        if (!(entity.getScoreboardTags().contains("chair"))) return false;
        return type.equals(EntityType.PLAYER);
    }



    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        chairDataMap.put(event.getPlayer(), null);
    }

    @EventHandler
    private void onExit(PlayerQuitEvent event)
    {
        if (chairDataMap.get(event.getPlayer()) != null)
            chairDataMap.get(event.getPlayer()).remove();
    }
}
