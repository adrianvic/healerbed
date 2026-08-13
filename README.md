# HealerBed

<img width="auto" height="100px" align="left" alt="healerbed" src="https://github.com/user-attachments/assets/4992d543-2aee-47a0-a8fd-18320f8452d7" />

This mod aims to provide a simple way for modpacks to heal the player when/after sleeping, with customizability in mind.

It works by hooking into Minecraft's `net.minecraft.world.WorldServer` class and waiting for it to call `EntityPlayer.wakeUpPlayer` inside `this.wakeAllPlayers`. Alternatively it will listen to `PlayerWakeUpEvent` if it's configured to allow incomplete sleeps.

## Configuration
Inside `.minecraft/config/healerbed.cfg` lies all mod configuration. It is self-documented.

### What you can currently change:
- Health amount
- Hunger amount
- Regen/feed even if player leaves bed early

## Supported versions
I made this mod for my personal use on 1.10.2, feel free to contribute code for other game versions.

## AI Disclosure
Not a single line of code was written by AI, however I relied on AI to learn how to inject on game code (ASM).
