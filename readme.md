# DiamondRushV2

DiamondRushV2 is the rewritten plugin developed by Hironak and heavily inspired by the plugin DiamondRush developed by
aumgn and noogotte.

## DiamondRush mini-game

DiamondRush is a mini-game on Minecraft where teams of players must destroy all enemy totems to win the game.
The totems are made of obsidian and players rush to find diamond, so they can quickly create a diamond pickaxe and
mine the enemy totems.

After placing their totem and their spawn, teams find themselves in an exploration phase where PvP is disabled.
During this phase they can however gather resources to prepare for the combat phase and try to find the enemy totems.
Mining a totem can be done during any phase. If an enemy is close to your totem during an exploration phase, you can
send him back to his spawn with an attack.
During this phase messages in chat can only be seen by players on the same team. If you're using a voice chat system
it is recommended to have your teams in separate channels.

The combat phase follows the exploration one. During this phase, PvP is enabled and encouraged. A player killing an
enemy player will receive a configurable reward. Messages in chat can be seen by everyone. In voice chat, it is
recommended to have everyone in the same channel.

Exploration and combat phases follow each other until all totems but one have been destroyed.

## Warning

This plugin is intended to be used on a server and a world dedicated to this mini-game. Using this plugin on your main
server can lead to unexpected behaviors and will damage your world.

## Compatibility

This plugin has been developed for PaperMC servers and Minecraft 26.1.2. No other Minecraft version has been tested.
As recommended by the PaperMC team, this plugin requires at least Java 25.

## Commands

### /diamondrush create

Creates a new DiamondRush game and a platform at the position of the player. This platform will be the starting point
of all players when starting the game.

### /diamondrush team add \<teamName> \<teamColor>

Adds a team to the current DiamondRush game with the specified name and represented by the specified color
(colored wool).

### /diamondrush team remove \<teamName>

Removes the team with the specified name from the current DiamondRush game.

### /diamondrush join \<teamName>

Joins the team with the provided name. Must be executed before the start of the game.

### /diamondrush start

Starts the current DiamondRush game. Every player is placed on their side of the starting platform and their leader is
given an obsidian block which can be placed to select the team's totem location.

### /diamondrush pause

Pauses the current DiamondRush game. Players cannot move or interact during the pause.

### /diamondrush resume

Resumes the current paused DiamondRush game.

### /diamondrush reload

Reloads the DiamondRush configuration files. Must be executed before the game starts. 
