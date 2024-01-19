# WorldTP

A Paper Plugin for Minecraft Servers which remembers Players Locations in Worlds and teleports them back there upon entering a World. Written for a small SMP Server.

The Plugin stores its data in the storage.yml File in its directory. This solution is not meant for big Servers with many worlds or players. 

## Permissions:

**worldtp.use**<br>
Usage Permission (The Plugin will only affect these players)

**worldtp.switchworlds**<br>
Permission for the /switchworld Command (if activated)

**worldtp.admin**<br>
Permission for administrative functions

## Commands

**/worldtp**<br>
Administrative Commands, requires `worldtp.admin` Permission

**/switchworld**<br>
Command for switching worlds, requires `worldtp.switchworlds` Permission, can be disabled in the config.yml (needs Server restart).
