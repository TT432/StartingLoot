# Starting Loot

The Starting Loot Mod is a custom modification designed to provide players with an enhanced and personalized starting experience by allowing them to set their initial gear through the use of LootTables. This feature empowers players to tailor the contents of their inventory at the beginning of a new game, ensuring that they can begin their adventure with customized equipment and resources.

## Getting Started

To utilize this mod, follow these simple steps:

1. Mod Installation: First, ensure that you have properly installed the Starting Loot Mod in your game. Depending on the game platform and mod loader, this may involve downloading the mod file from a repository and placing it in the appropriate mods folder.
2. Configuration File: The mod requires a configuration file named `starting_loot.json` to specify the starter loot settings. Create or download a pre-configured version of this JSON file and place it in your game's configuration directory. The exact path might vary depending on the game; common locations include a "config" folder within the root directory of the game installation.
3. LootTable Configuration: Open the `starting_loot.json` file with a text editor to define the items and chances of receiving them when a player starts a new game. A `LootTable` typically contains entries for different item types, each with its own weight or chance to spawn, quality, and potentially additional attributes like enchantments.
4. JSON Structure: The JSON should adhere to the format specified by _Minecraft_. It will likely contain key-value pairs representing item IDs/names and their associated properties. For example:

```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:stone"
        }
      ]
    }
  ]
}
```

5. Save and Launch: Once you've configured your `starting_loot.json` file to your liking, save it and launch your game. The mod will read this file during startup and apply the custom starter loot to all new player characters.

By following these steps, you'll be able to create a unique and exciting starting point for every player, adding depth and replayability to your gaming experience.