<div align="center" id="toc">
  <a href="https://github.com/Flummidill/SimpleGraves/releases">
    <img src="https://github.com/Flummidill/SimpleGraves/blob/HEAD/icons/SimpleGraves-250x250.png?raw=true" alt="SimpleGraves-Icon">
  </a>

  <ul style="list-style: none">
    <summary>
      <h1>SimpleGraves</h1>
    </summary>
  </ul>
  
  <a href="https://github.com/Flummidill/SimpleGraves/releases">
    <img src="https://img.shields.io/github/downloads/Flummidill/SimpleGraves/v1.2.1/SimpleGraves-1.2.1.jar?style=for-the-badge&label=Downloads&color=29A100"</img>
  </a>
</div>


<hr/>


## 🎯 Features
- **Grave System for Players**
When players die, their items and XP are safely stored in a Grave so they don't despawn.

- **Grave-Info Command**
Players can easily check the location of their graves, helping them recover items without wandering aimlessly.

- **Admin Grave Management**
Admins can list, inspect, teleport to, or remove graves. This makes it easy to help players or keep the world clean from abandoned graves.

- **Configurable Grave Behavior**
Decide whether Graves can be looted by other players, how much XP gets stored in them, and more.


<hr/>


### Commands:
```
/graveinfo <number> - Shows the Location of your Grave
```

### Admin Commands:
```
/graveadmin go <player> <number> - Teleports you to a Player's Grave
/graveadmin list <player> - Lists a Player's Graves
/graveadmin info <player> <number> - Shows the Location of a Player's Grave
/graveadmin remove <player> <number> - Removes a Player's Grave
```

### Permissions:
```
simplegraves.use (Default: true) - Allow use of Player Commands
simplegraves.admin (Default: false) - Allow use of Admin Commands
```

### Config:
```
grave-stealing - Allows breaking other Player's Graves
max-stored-xp - Maximum Amount of XP-Levels that will be stord in Graves
delete-vanishing-items - Deletes Items with Curse of Vanishing
```
