package com.github.exobite.mc.zombifyvillagers.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;


public class BasicEvent implements Listener {

    private static final Class<?>[] ignoredEvents = {
            //Spigot
            VehicleUpdateEvent.class, EntityAirChangeEvent.class, StriderTemperatureChangeEvent.class,
            GenericGameEvent.class, BlockPhysicsEvent.class, BatToggleSleepEvent.class, BlockSpreadEvent.class,
            SheepRegrowWoolEvent.class, EntityChangeBlockEvent.class, PlayerMoveEvent.class, PlayerToggleSprintEvent.class,
            PlayerToggleFlightEvent.class, PlayerToggleSneakEvent.class,
    };

    private static final EntityType[] allowedTypes = {
            EntityType.ZOMBIE_VILLAGER,EntityType.VILLAGER
    };

    public BasicEvent(JavaPlugin main) {
        RegisteredListener registeredListener = new RegisteredListener(
                this,
                (listener, event) -> onEvent(event),
                EventPriority.NORMAL,
                main,
                false);
        for (HandlerList handler : HandlerList.getHandlerLists())
            handler.register(registeredListener);
    }

    @EventHandler
    public void onEvent(Event e) {
        for(Class<?> clazz : ignoredEvents) {
            if(clazz.isInstance(e)) return;
        }
        if(!isAllowedEntityType(e)) return;
        System.out.println(e);
    }

    private boolean isAllowedEntityType(Event e) {
        if(e instanceof EntityEvent ee) {
            return isAllowedEntityType(ee.getEntity());
        }
        //Return true if isn't instanceof EntityEvent
        return true;
    }

    private boolean isAllowedEntityType(Entity e) {
        EntityType type = e.getType();
        for(EntityType t:allowedTypes) {
            if(type==t) return true;
        }
        return false;
    }

}
