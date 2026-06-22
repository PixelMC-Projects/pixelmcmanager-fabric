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
- `/pixelmcmanager audit`
- `/pixelmcmanager audit last`
- `/pixelmcmanager audit last <count>`
- `/pixelmcmanager save`
- `/pixelmcmanager stats`
- Configurable command permission levels.
- Automatic chat announcements, disabled by default.

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

`/pixelmcmanager audit` and `/pixelmcmanager audit last [count]` show recent successful management operations in-game. PixelMC Manager records successful `reload`, `stopserver`, `stopserver cancel`, `maintenance <time>`, `maintenance now`, and `maintenance off` operations. Query and preview commands are not recorded. Audit records are stored in the current world save root at `<world-save-root>/pixelmcmanager/audit.jsonl` and a recent in-memory cache is loaded on server start for fast queries.

`/pixelmcmanager save` checkpoints online player stats, saves `player_stats.json`, confirms audit has no buffered writes, saves the current PixelMC Manager config state, requests the server world save flow, and records a successful `save` audit entry.

`/pixelmcmanager stats` shows recorded player count, current online/max players, today active players by system timezone, total accumulated playtime, current server uptime, and maintenance state. It checkpoints online sessions in memory before reading totals and does not write audit.

## Permission Config

```json
"permissions": {
  "reloadLevel": 3,
  "previewLevel": 2,
  "statsLevel": 2,
  "auditLevel": 4,
  "stopserverLevel": 4,
  "maintenanceLevel": 4,
  "saveLevel": 4,
  "serverStatsLevel": 2,
  "announcementLevel": 4
}
```

Permission levels are OP levels from 0 to 4. Missing fields use defaults; invalid or out-of-range values fall back to defaults and are logged. Dangerous commands default to OP level 4. `/pixelmcmanager reload` applies new levels immediately because each command reads the current config at execution time.

## Auto Announcements

Automatic announcements are disabled by default and only send chat messages when enabled. They do not use title, subtitle, actionbar, bossbar, networking, or client-side code.

```json
"announcements": {
  "enabled": false,
  "intervalMinutes": 30,
  "initialDelayMinutes": 5,
  "messages": [
    "&bQQ群：&e768322731",
    "&7文明游玩，禁止恶意破坏。",
    "&7遇到问题请联系管理员。"
  ]
}
```

When enabled, the scheduler waits `initialDelayMinutes` after startup or reload, then sends one configured message every `intervalMinutes`. Messages rotate in order, empty lists are skipped safely, and `{online}`, `{max}`, `{date}`, and `{time}` are resolved when the announcement is sent.

## Release Format

GitHub Release titles must use `PixelMC Manager Fabric <mod_version>`, for example `PixelMC Manager Fabric 0.3.0`. Release notes must include `## 更新内容`, `## 构建验证`, and `## 兼容版本与附件`, with exact jar asset names listed under `兼容版本与附件`.
