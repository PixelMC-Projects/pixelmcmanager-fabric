# PixelMC Manager Fabric

Minecraft 26.1.2
Fabric Loader
Fabric API
Server-side only
Mod ID: pixelmcmanager
Branch: mc-26.1.2

This is the official Fabric port of PixelMC Manager. It does not require client installation and does not support Forge, NeoForge, Connector, or Architectury.

## Features

- Configurable first-join and returning welcome messages.
- Placeholder support for player, online count, login count, playtime, ping, and dates.
- `&` color codes, hex colors, and simple `<gradient:#RRGGBB:#RRGGBB>text</gradient>` gradients.
- `/pixelmcmanager reload`
- `/pixelmcmanager preview`
- `/pixelmcmanager preview first`
- `/pixelmcmanager preview returning`
- Player login count statistics.
- Real wall-clock playtime statistics with periodic checkpoints.
- `/pixelmcmanager logincount`
- `/pixelmcmanager logincount <player>`
- `/pixelmcmanager logintime`
- `/pixelmcmanager logintime <player>`
- `/pixelmcmanager stopserver <time>`
- `/pixelmcmanager stopserver cancel`
- `/pixelmcmanager stopserver status`
- `/pixelmcmanager maintenance <time>`
- `/pixelmcmanager maintenance now`
- `/pixelmcmanager maintenance off`
- `/pixelmcmanager maintenance status`

Player statistics are stored in:

```text
<world-save-root>/pixelmcmanager/player_stats.json
```

Configuration is stored in:

```text
config/pixelmcmanager.json
```

Gradle and Maven resolution prefer Aliyun mirrors for public repositories while preserving the official Fabric Maven repository for Fabric artifacts.

## Branch Strategy

This repository uses Minecraft-version branches.
Current branch: mc-26.1.2
Future branches should use names like mc-1.21.x or mc-1.22.x.

## Maintenance Limitation

This build uses Fabric API server play connection events and no Mixin. During the 15-second maintenance window after players are kicked, new joins are disconnected immediately after Fabric exposes the joined player, with the configured two-line maintenance message.

`/pixelmcmanager stopserver status` displays the current stop plan without changing it. It shows whether the server is waiting for maintenance, already in the 15-second maintenance window, or past the final stop time, plus the remaining real wall-clock time, maintenance start time, and final stop time.

`/pixelmcmanager maintenance <time>` schedules maintenance mode without stopping the server. At the scheduled time all online players are kicked, new joins are rejected with a custom two-line maintenance message, and the server keeps running until `/pixelmcmanager maintenance off`.

`/pixelmcmanager maintenance now` enters maintenance immediately. `/pixelmcmanager maintenance off` cancels a pending maintenance plan or disables active maintenance. `/pixelmcmanager maintenance status` shows the current maintenance plan or active maintenance state. Maintenance plans and active maintenance state are in memory only and are not persisted across server restarts.
