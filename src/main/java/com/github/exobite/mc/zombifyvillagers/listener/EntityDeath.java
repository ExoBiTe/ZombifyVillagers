package com.github.exobite.mc.zombifyvillagers.listener;

import com.github.exobite.mc.zombifyvillagers.utils.Config;
import com.github.exobite.mc.zombifyvillagers.utils.VersionHelper;
import de.tr7zw.nbtapi.*;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class EntityDeath implements Listener {

    private final Random rdmInst = new Random();
    private record VillagerNBTData(NBTCompoundList gossips,
                                   NBTCompound offers,
                                   NBTCompound vData,
                                   int xp,
                                   boolean isAdult,
                                   Entity vehicle,
                                   String customName) {}

    @EventHandler
    public void onEntityByEntityDeath(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof LivingEntity ent)) return;
        if((e.getDamager().getType() != EntityType.ZOMBIE && e.getDamager().getType() != EntityType.ZOMBIE_VILLAGER)
                || e.getEntityType() != EntityType.VILLAGER) return;
        if(ent.getHealth() - e.getFinalDamage() > 0) return;
        double rdm = rdmInst.nextDouble(0.0, 1.0);
        if(rdm > Config.getInstance().getInfectionChance()) return;
        tryVillagerZombiefication((Villager) e.getEntity());
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if(e.getEntityType() != EntityType.ZOMBIE_VILLAGER) return;
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.INFECTION) {
            e.setCancelled(true);
        }
    }

    private void tryVillagerZombiefication(Villager v) {
        NBTEntity nbtv = new NBTEntity(v);
        NBTCompoundList gossips = nbtv.getCompoundList("Gossips");
        NBTCompound offers = nbtv.getCompound("Offers");
        NBTCompound vData = nbtv.getCompound("VillagerData");
        int xp = nbtv.getInteger("Xp");
        VillagerNBTData dat = new VillagerNBTData(gossips, offers, vData, xp, v.isAdult(), v.getVehicle(), v.getCustomName());
        spawnCustomZombieVillager(v.getLocation(), dat);
    }

    private void spawnCustomZombieVillager(Location l, VillagerNBTData data) {
        assert l.getWorld() != null;
        ZombieVillager ze = l.getWorld().spawn(l, ZombieVillager.class);
        if(data.isAdult()) {
            ze.setAdult();
        }else{
            ze.setBaby();
        }
        if(data.vehicle()!=null) data.vehicle().addPassenger(ze);
        if(data.customName()!=null) ze.setCustomName(data.customName());
        NBTEntity zeNbt = new NBTEntity(ze);
        addDataToEntity(zeNbt, setNameToCompound("Gossips", data.gossips()));
        addDataToEntity(zeNbt, setNameToCompound("Offers", data.offers()));
        addDataToEntity(zeNbt, setNameToCompound("VillagerData", data.vData()));
        addDataToEntity(zeNbt, setNameToCompound("Xp", data.xp()));
        if(VersionHelper.isPaper()) zeNbt.mergeCompound(new NBTContainer("{Paper.SpawnReason: \"INFECTION\"}"));
    }

    private void addDataToEntity(NBTEntity target, NBTCompound data) {
        if(data==null || target==null) return;
        target.mergeCompound(data);
    }

    private NBTCompound setNameToCompound(String name, Object data) {
        return new NBTContainer("{"+name+": "+data+"}");
    }

}
