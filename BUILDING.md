# 构建与接入 / Building and setup

使用 JDK 25 并设置 `JAVA_HOME`，在对应 Minecraft 版本分支中运行：
Use JDK 25 (`JAVA_HOME`) on the matching Minecraft-version branch:

```sh
./gradlew clean build
```

Windows 使用 `gradlew.bat clean build`；产物位于 `build/libs/`。依赖优先使用大陆镜像，保留 Fabric 官方 Maven。不需要本机专用缓存或客户端模组。
On Windows use `gradlew.bat clean build`. Outputs are in `build/libs/`. Mainland mirrors and official Fabric Maven are configured; no workstation-specific cache or client mod is required.

## 常驻网页 / Independent web service

网页服务、MariaDB 数据库和 UI 与 Forge 共用，避免维护两份后端。使用 [共享服务的构建说明](https://github.com/PixelMC-Projects/pixelmcmanager-forge/blob/mc-1.20.1/BUILDING.md) 单独构建/运行 Manager Service。Java 17 的后端可接收本 Fabric 模组（Java 25）的同一桥接协议。
The dashboard, MariaDB backend and UI are shared with Forge. Build/run Manager Service using [its instructions](https://github.com/PixelMC-Projects/pixelmcmanager-forge/blob/mc-1.20.1/BUILDING.md). The Java 17 service accepts this Java 25 Fabric mod through the same bridge protocol.

模组首次启动生成 `config/pixelmcmanager-bridge.json`，默认关闭。填入后台分配的实例 ID 和独立桥接令牌后开启；地址使用游戏容器能访问的后台地址。不要把真实令牌提交至 Git。
The first start creates `config/pixelmcmanager-bridge.json`, disabled by default. Set the service-assigned instance ID, a dedicated bridge token, and a backend URL reachable from the game container before enabling it. Never commit real credentials.

```json
{
  "enabled": false,
  "baseUrl": "http://127.0.0.1:18800",
  "instanceId": "minecraft",
  "token": "",
  "publicUrl": ""
}
```

玩家统计/登录补传保存在当前存档的 `pixelmcmanager/`；网页持久资料在后台数据库。性能数据按当前运行批次和原有 12/24 小时保留规则处理，重连不会冒充新一轮开服。
Local player checkpoints and pending login uploads stay in the active world's `pixelmcmanager/` directory. Durable web records use the backend database. Performance follows the existing current-run 12/24-hour retention policy; reconnecting is not a new server run.

扩展使用 `com.pixelmc.pixelmcmanager.api.ManagerBridgeApi` 注册，不绑定 Forge API。游戏内扩展本身仍需对应加载器版本；不能直接把 Forge 扩展 JAR 放进 Fabric。
Extensions register through the loader-independent `ManagerBridgeApi`. An in-game extension still needs a Fabric-compatible build; a Forge extension JAR does not become loadable on Fabric merely by sharing the backend.

### 0.5.2 共享源码 / Shared sources

Manager Service 0.5.2 的构建同时需要 `MemorialTitles` 和 `DailyFortunes` 源码。把三仓放在同一父目录，或在共享 Forge 工程构建时传入 `-PmemorialTitlesDir=/path/to/MemorialTitles -PdailyFortunesDir=/path/to/DailyFortunes`。后端仍使用 Java 17；Fabric 模组使用 Java 25，两者不需要使用同一个 Java 进程。
Building Manager Service 0.5.2 requires the `MemorialTitles` and `DailyFortunes` sources. Clone the three projects side by side, or pass `-PmemorialTitlesDir=/path/to/MemorialTitles -PdailyFortunesDir=/path/to/DailyFortunes` when building the shared Forge project. The backend stays on Java 17 and the Fabric mod stays on Java 25; they run independently.

本轮精确源版本、全文件移植对照和验证边界见 [正式移植记录](docs/forge-port-parity.md)。`docs/forge-source-parity.json` 含完整游戏内 Java 清单、两侧规范化 SHA-256，以及自 Forge 0.5.1 起每个变更路径的归属；本地取得清单指定的 Forge 提交与 v0.5.1 基线后，可用 Python 3.9+ 只读验证：
See the [formal port record](docs/forge-port-parity.md) for the exact source revisions, ownership and validation limits. `docs/forge-source-parity.json` covers every in-game Java source, its normalized SHA-256 on both loaders, and the ownership of every Forge change since 0.5.1. With its pinned Forge commit and v0.5.1 baseline available locally, verify read-only using Python 3.9+:

```sh
python tools/check_forge_parity.py --forge-repo /path/to/pixelmcmanager-forge
```

网页服务中的抽签页面及独立数据库支持不等于 Fabric 版抽签游戏扩展。DailyFortunes 当前游戏端只适用于 Forge 1.20.1；本次仅移植 Manager 桥接证明，不添加客户端依赖或抽签玩法。
The shared fortune pages and database support do not supply a Fabric in-game fortune extension. DailyFortunes currently targets Forge 1.20.1. This port adds its Manager admission proof while keeping Manager server-only.

### 0.5.2-web.1 网页服务补丁 / Web service patch

此补丁恢复抽签列表/详情头像的 32px / 40px 尺寸，适配现有两个版本的 Fabric Manager 0.5.2。下载补丁 ZIP 后，正常停止独立网页服务，备份并替换其中的 `service.jar`，保留现有 `manager.json`、`data/`、凭据、头像与备份，再按原命令启动并刷新网页。回滚时恢复原 0.5.2 的 `service.jar`，按相同步骤启动。游戏 JAR、数据库结构与 Java 业务字节码保持不变，游戏服和客户端不需要为此补丁更新或重启。
This patch restores the fortune list/detail avatars to 32px / 40px and works with both existing Fabric Manager 0.5.2 versions. Stop only the independent web service normally, back up and replace `service.jar`, retain `manager.json`, `data/`, credentials, avatars and backups, then restart it with the original command and refresh the browser. Roll back by restoring the original 0.5.2 `service.jar`. Game JARs, database structure and Java business bytecode stay unchanged; this patch needs no game or client update/restart.

补丁网页源固定为 [Forge e1ccabd](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/e1ccabd17aa53137361ff8e000f06fe53ce1934d)。维护方从经过校验的原 0.5.2 服务 JAR 仅替换 `web/` 和 manifest；619 个 `.class` 及全部其他条目逐字节不变。复刻时在该 Forge 提交的 `web-console/` 执行 `npm ci`、`npm run build`，再回到工程根运行：
The web source is pinned to [Forge e1ccabd](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/e1ccabd17aa53137361ff8e000f06fe53ce1934d). The patch replaces only `web/` and the manifest in the verified original service JAR; all 619 `.class` files and every other entry remain byte-identical. To reproduce, run `npm ci` and `npm run build` in that Forge commit's `web-console/`, then run from its project root:

```sh
python tools/package_service_web_patch.py /path/to/pixelmc-manager-service-0.5.2.zip
```

输入包取自原 [0.5.2 完整发行](https://github.com/PixelMC-Projects/pixelmcmanager-forge/releases/tag/v0.5.2)，工具会核验其中原始服务 JAR 的 SHA-256。Fabric 直接复用 Forge 的同字节服务补丁；新补丁校验和见移植记录及补丁 Release。原 0.5.2 标签、七附件与完整发行的 Latest 标记保持不变。
Use the input package from the original [complete 0.5.2 release](https://github.com/PixelMC-Projects/pixelmcmanager-forge/releases/tag/v0.5.2); the tool verifies its original service JAR SHA-256. Fabric reuses the exact Forge patch bytes. Checksums are in the port record and patch release. The original 0.5.2 tag, seven assets and Latest status are preserved.

## 重启交接 / Restart supervision

`/pixelmcmanager restart whenempty` 预约空服 15 秒后重启；`cancel` / `status` 取消或查询。`restart onmodchange on|off|status` 控制轻量模组变更检测，配置为 `restart.restartOnModChange`，默认关闭。监听 mods 顶层 JAR 文件事件，定期只核对文件元数据，不持续读 JAR 或递归扫盘。
Use `/pixelmcmanager restart whenempty`, `cancel`, or `status`. `restart onmodchange on|off|status` controls lightweight mod-change restarts (`restart.restartOnModChange`, off by default). Top-level JAR events and periodic metadata checks avoid continuous content reads or recursive scanning.

必须先运行 `tools/restart_host.py`。未发现可用托管程序时，模组拒绝提交重启，避免只停服却无法拉起。首次启动及重启使用托管程序收到的同一原始命令。
Run `tools/restart_host.py` first. Without a ready supervisor the mod refuses restart requests rather than stopping a server it cannot relaunch. First start and subsequent restarts use the same original command.

独立模式使用 `standalone --game-dir SERVER_DIRECTORY -- ORIGINAL_STARTUP_COMMAND`；MCSM 模式使用 `mcsm --game-dir SERVER_DIRECTORY --instance INSTANCE_UUID --daemon-config DAEMON_CONFIG --state-file STATE_FILE`。
Use `standalone --game-dir SERVER_DIRECTORY -- ORIGINAL_STARTUP_COMMAND` for independent startup, or `mcsm --game-dir SERVER_DIRECTORY --instance INSTANCE_UUID --daemon-config DAEMON_CONFIG --state-file STATE_FILE` for MCSM. Replace the uppercase placeholders with the actual paths and original command.

MCSM 桥在宿主机运行，只授权所选实例；游戏容器与桥需共享该实例的 `pixelmcmanager/restart` 目录。Daemon 凭据只留在宿主机，不发给游戏客户端。MCSM 模式由 MCSM 使用该实例原启动设置完成重启。
Run the MCSM bridge on the host for the selected instance only. Share that instance's `pixelmcmanager/restart` directory with the game container. Keep daemon credentials on the host. MCSM performs restarts using the instance's original startup settings.

## 更新兼容 / Upgrade compatibility

0.3.0 的配置与存档内统计会保留；旧 Fabric 占位符 `{first_join}` / `{last_join}` 继续有效，同时支持 Forge 的完整占位符集合。新配置项使用与 Forge 相同默认值。不要用旧包覆盖当前数据目录。
Existing 0.3.0 configuration and world-local statistics are retained. Legacy Fabric `{first_join}` / `{last_join}` placeholders remain supported alongside the full Forge placeholder set. New options use the same defaults as Forge. Do not replace live data directories with an old package.
