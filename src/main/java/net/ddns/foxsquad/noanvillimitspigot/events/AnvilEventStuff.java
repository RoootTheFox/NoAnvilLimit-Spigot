package net.ddns.foxsquad.noanvillimitspigot.events;

import org.bukkit.event.*;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import java.util.Objects;

public class AnvilEventStuff implements Listener {
    @EventHandler
    public void onAnvilPrepareEvent(PrepareAnvilEvent event) {
        System.out.println("text: " + event.getInventory().getRenameText());
        System.out.println("length: " + Objects.requireNonNull(event.getInventory().getRenameText()).length());
    }
}
