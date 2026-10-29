# PixelMC Manager · Fabric

[中文](#中文) · [English](#english)

版本 / Versions（全部需要 Fabric API，Manager 只需服务端安装 / Fabric API required, Manager is server-only）:

| Minecraft | Java | Source branch | Download |
| --- | --- | --- | --- |
| 1.20.1 | 17 | [fabric/1.20.1](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/tree/fabric/1.20.1) | [0.6.1 Fabric1.20.1](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/releases/tag/v0.6.1-fabric-1.20.1) |
| 1.21.1 | 21 | [fabric/1.21.1](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/tree/fabric/1.21.1) | [0.6.1 Fabric1.21.1](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/releases/tag/v0.6.1-fabric-1.21.1) |
| 26.1.2 | 25 | [mc-26.1.2](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/tree/mc-26.1.2) | [0.6.1 Fabric26.1.2](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/releases/tag/v0.6.1-fabric-26.1.2) |
| 26.2 | 25 | [mc-26.2](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/tree/mc-26.2) | [0.6.1 Fabric26.2](https://github.com/PixelMC-Projects/pixelmcmanager-fabric/releases/tag/v0.6.1-fabric-26.2) |

Forge1.20.1 / NeoForge1.21.1 位于独立的 [Forge/NeoForge 仓库](https://github.com/PixelMC-Projects/pixelmcmanager-forge)。各加载器共用最新的 [0.6.0.web.2 独立网页服务](https://github.com/PixelMC-Projects/pixelmcmanager-forge/releases/tag/v0.6.0.web.2)。
Forge/NeoForge builds stay in the separate repository. All loaders share the latest independent web service linked above.

## 中文

PixelMC Manager 的官方 Fabric 移植，提供与 Forge 0.6.1 对应的服务器监控、玩家档案与日常管理功能。


### 功能

- 玩家可用 `/killme` 自杀，先以玩家聊天样式显示“自刎归天!!!”，再执行原版死亡流程。

- 实时 TPS、MSPT、内存、玩家和维度数据，可接入常驻网页后台。
- 保存累计在线时长、登录次数与登录历史，排除 Carpet 假人。
- 配置首登/回归问候、轮播公告、颜色、渐变和占位符。
- 预约关服、维护和空服 15 秒后重启；支持模组列表变化后自动重启。
- 重启交由 MCSM 或独立托管脚本执行，沿用原启动命令和 JVM 参数。
- 使用同一个网页后台、全局玩家库和命名空间存储管理整合包归档与可选扩展。

### 使用

适用于 Minecraft **26.1.2 / Fabric**。将本模组和 Fabric API 放入服务端 `mods/`，基础功能无需客户端安装或数据库。指令：`/pixelmcmanager`，配置：`config/pixelmcmanager.json`。

可选的常驻网页按以下流程接入：

1. 按 [Manager Service 部署说明](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/mc-1.20.1/manager-service) 单独运行共享后台和 MariaDB，并登记实例，取得实例 ID 与桥接令牌。
2. 编辑 `config/pixelmcmanager-bridge.json`，填入后台地址 `baseUrl`、`instanceId`、`token`，把 `enabled` 设为 `true`。后台地址必须能从游戏容器访问。
3. 保存后正常重启游戏服；首次连接时，在网页为该实例绑定当前存档/整合包。后台保持独立运行，游戏服关闭后仍可访问。

[构建与接入细节](BUILDING.md) · [Forge/Fabric 移植对照](docs/forge-port-parity.md)。

Manager 本体只需服务端安装。网页、数据库和插件 UI 复用同一份独立服务；MemorialTitles / DailyFortunes 游戏扩展请按各自正式支持表选择相同 Minecraft 版本与加载器的 JAR，不能混用其他端口。

## English

The official Fabric port of PixelMC Manager, with server monitoring, player records and administration matching Forge 0.6.1.


### Features

- Players can use `/killme` to announce “自刎归天!!!” in player-chat style, then die through vanilla's self-kill command.

- Stream live TPS, MSPT, memory, player and dimension data to the independent dashboard.
- Retain playtime totals, login counts and history while excluding Carpet fake players.
- Customize first/returning greetings and rotating announcements with colors, gradients and placeholders.
- Schedule shutdowns, maintenance and restarts after 15 empty seconds, including mod-list changes.
- Hand restarts to MCSM or the standalone supervisor, preserving the original command and JVM arguments.
- Reuse the same web service, global player directory and namespaced storage for archives and extensions.

### Usage

For Minecraft **26.1.2 / Fabric**. Put this mod and Fabric API in the server's `mods/` folder. Basic features need neither a client installation nor a database. Command: `/pixelmcmanager`. Configuration: `config/pixelmcmanager.json`.

To optionally connect the always-on dashboard:

1. Follow the shared [Manager Service setup](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/mc-1.20.1/manager-service) to run the backend with MariaDB and register an instance to obtain its ID and bridge token.
2. Set `baseUrl`, `instanceId`, `token` and `enabled: true` in `config/pixelmcmanager-bridge.json`. The game container must be able to reach the backend URL.
3. Save and restart the game server normally. On first connection, bind that instance to the current world/modpack in the web console. Keep the backend running separately so it remains available while the game is stopped.

See [building and setup details](BUILDING.md) and the [Forge/Fabric parity checklist](docs/forge-port-parity.md).

Manager itself remains server-only. The dashboard, database backend and plugin UI share one independent service. Select MemorialTitles / DailyFortunes game extensions from their official support tables with matching Minecraft and loader versions.

---

原作者 / Original author: [zcpu · PixelMC](https://github.com/zcpu954861).

[AGPL-3.0-only](LICENSE) · [Minecraft linking permission](LICENSE-EXCEPTION.md).
保留版权与署名；分发修改版或使用修改版提供联网服务时，按许可证提供对应源码。
Retain copyright and attribution; provide corresponding source when distributing modifications or offering modified software over a network, as required by the license.
