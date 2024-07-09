package de.weapie.randomtp.config;

import de.weapie.devlok.files.config.annotations.Config;
import de.weapie.randomtp.config.module.MessageModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Config(id = "config")
public class PluginConfig {

    private boolean consoleOutput;
    private String prefix;
    private int cooldown;
    private String bypassPermission;
    private String adminPermission;
    private List<MessageModule> messages;
    private String[] allowedWorlds;
    private Material[] unsafeBlocks;
    private int minRadius;
    private int maxRadius;

}
