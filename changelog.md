# Limit-Mob-Spawn

[![](http://cf.way2muchnoise.eu/versions/449932.svg)][download page]
[![](http://cf.way2muchnoise.eu/full_449932_downloads.svg)][download page]

[![Java CI with Gradle](https://github.com/Kotori316/LimitMobSpawn/actions/workflows/gradle.yml/badge.svg)][action details]

[download page]: https://www.curseforge.com/minecraft/mc-mods/limit-mob-spawn

[action details]: https://github.com/Kotori316/LimitMobSpawn/actions/workflows/gradle.yml

## v20.1.1

* Add a config option `affectBosses`
  * If true, rules can affect spawning of boss monster.

## v20.1.0

* Update for 1.20.1

## v19.3

* Update to 1.19.2

## v19.2

* Update to 1.19.1

## v19.1

* Update for the latest forge.

## v19.0

* Update for 1.19
* Fixed error message shown in loading.

## v18.4

* Added function to change the maximum number of mobs to spawn.
* Add command for MobNumberLimit. `limit-mob-spawn category_limit set <CATEGORY> <COUNT>`

## v18.3

* Update for 1.18.2

## v18.2

* Remove Spawner Mixin as it prevents the client launching.

## v18.1

* Added LightLevelLimit in 1.18.

## v18.0

* Update for 1.18.1

## v17.1

* Added LightLevelLimit.
  * It has 2 properties, `layer`, `level`.
    * Layer is either Block or Sky.
    * Level should be in 0 to 15.
  * This condition outputs `true` if the light level in layer > `level`.
  * Set `layer`=block, `level`=0 to reproduce 1.18 condition.

## v17.0

* Update for 1.17.1
* **Changed mod id to `limitmobspawn`**. (before: `limit-mob-spawn`)
  * Due to limitation of module loading introduced with new Forge.
* Improved command suggestion. ("=" will be inserted.)
* Removed old format compatibility of "And" and "Or".

## v16.8

* Added function to change spawn count of Monster Spawner.
  * Default - OFF
  * Change by command only, `limit-mob-spawn spawner spawnCount <count>`
  * Execute `limit-mob-spawn spawner query` to get current configuration.
  * No way to change this from data packs now.

## v16.7

* Added new rules of Biome and Biome Category.
  * Biome is like `minecraft:ocean`.
  * Category is like NETHER, TAIGA, FOREST and so on.
* Implemented command suggestions for Dimension and Biome.
* Fixed the condition of MobSpawnTypeLimit was wrong.
* Internal - Changed error message of Entity rule.
* Internal - Added log marker.
* Internal - changes for GUI mod.

## v16.6

* Added config option to change required permission level to execute commands of LimitMobSpawn.
  [#1](https://github.com/Kotori316/LimitMobSpawn/issues/1)
  * Config file is saved at `saves/(world name)/serverconfig/limit-mob-spawn-server.toml`, the same dir
    where `forge-server.toml` is saved.
  * The setting is saved for each world, including server world. The config will be synced with client when player logs
    in.
  * I highly recommend the value to be **0 or 2**. Any other values are not tested.
* Fixed the suggestion of condition "all" was wrong.

## v16.5

* Suggests and, or, not condition in command.
* Internal API changes for GUI mod.

## v16.3

* Commands can now add rules of "Add" / "Or" / "Not".
  * All rules can be added by commands.
* Changed default setting of position limit
  * Before, if y-max is omitted, y-max=255
  * After, if y-max is omitted, y-max=256
  * No changes if y-max is presented.

## v16.2

* Added commands to add new rules.
  * Only simple rules.
* Added commands to remove rules.
* Fixed an issue where array of rules can't be parsed correctly.
* Made this mod required only in the server.
  * In the multi-players mode, client **does not need** to install this mod.
  * In the single-player mode, client **needs** to install this mod.
* Internal changes
  * Use JUnit to test commands
    * To test commands can be successfully parsed.
    * To test commands can suggest proper words.
  * Test in CI.

## v16.1

* Serialization was improved to use Dynamic, which can accept both Json and NBT.
* Serialization of And/Or condition was changed to use array, instead of map of "t1", "t2"...
  * ~~The map type object can still be used.~~ Removed in v17.0
* Fixed an issue where deserialization fails in some cases.
* Added "All" and "Spawn Reason" limitations.
* Reduced spams of log.
* Added capability to save limitations in the world.
* Added commands to query rules.

## v16.0

The first release of this mod.

* Setting is loaded from json in datapack.
