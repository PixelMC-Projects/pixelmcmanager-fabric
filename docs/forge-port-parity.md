# Forge / Fabric 正式移植对照

本文件记录 Fabric 的移植归属与版本差异。网页原型的存在不代表游戏扩展已移植，也不代表生产环境已部署。

逐文件证据位于 `forge-source-parity.json`：游戏内 38 个 Java 文件全部覆盖，其中 27 个完全同源、11 个为明确记录的 Fabric / Minecraft 26 API 适配；另逐路径记录从 Forge 0.5.1 到本轮源提交的全部变更及其归属。`tools/check_forge_parity.py` 会同时验证完整文件集合、两侧规范化 SHA-256 和变更清单，无文件可静默遗漏。

## 源码归属

| 部分 | 正式源码归属 | Fabric 处理 |
| --- | --- | --- |
| 游戏内 Manager | 本仓对应 Minecraft 版本分支 | 保留纯服务端 Fabric 入口与 Minecraft 26 / Java 25 API 适配 |
| Manager Service / 数据库 / 网页 | `PixelMC-Projects/pixelmcmanager-forge` 的共享服务 | Forge / Fabric 连接同一实现，不复制成两套后端 |
| MemorialTitles 网页和共享业务 | `PixelMC-Projects/MemorialTitles` | 按共享服务构建说明提供对应源码；游戏扩展需要独立 Fabric 移植 |
| DailyFortunes 网页和共享业务 | DailyFortunes 工程与 Manager 共享服务 | 网页接入不代表 Forge 游戏扩展 JAR 可用于 Fabric |
| 重启托管程序 | 本仓 `tools/restart_host.py` | 与 Forge 使用相同 IPC 协议，保留原启动命令 |

## 游戏内行为对照

| Forge 行为 | Fabric 实现/差异 | 验证方式 |
| --- | --- | --- |
| 当前存档统计与审计、墙钟累计在线时长 | `PlayerStatsStore` / `AuditService`；仅替换 MC26 玩家名及 Fabric 版本来源 API | `clean build`、源码对照；真实登录待独立测试服验收 |
| 首登/回归问候、颜色/渐变、公告 | 相同配置与文本逻辑；MC26 `Component` API；保留旧 Fabric `{first_join}` / `{last_join}` 别名 | `textParityTest`；真实聊天显示待验收 |
| 可配置 OP 0–4、save/stats/reload | 适配 MC26 权限集与指令接口；危险指令默认权限不变 | 编译与源码对照；实服权限待验收 |
| 停服维护与独立维护 | 通过 Fabric END_SERVER_TICK 驱动原有状态机，停服拒绝登录优先 | 源码对照；真实踢人/倒计时待验收 |
| 连续空服 15 秒后正常重启 | Fabric 生命周期驱动同一状态机；无就绪托管程序时拒绝重启 | `restartLogicTest` / Python 托管测试；实际 MCSM / 独立重启待验收 |
| 轻量模组变更检测 | 同一顶层 JAR 文件事件 + 定期元数据核对，不新增持续 JAR 哈希或递归扫描 | `restartLogicTest` |
| TPS/MSPT、长 tick 与维度采样 | Fabric START/END 事件阶段顺序；MC26 Identifier、ChunkPos、维度时间、强制区块和 latency API | 编译、阶段顺序与源码对照；真实采样待验收 |
| 真人登记与假人排除 | 保留 Carpet/Curtain 类继承链识别；无 Forge FakePlayer 依赖 | 源码对照；对应版本假人模组待验收 |
| 异步有界桥接、登录补传、性能 spool | 同一协议与实现；配置目录改用 FabricLoader；版本元数据来自 FabricLoader | `bridgeLogicTest`；真实网络重连待验收 |
| 可选扩展注册/回调 | 相同无加载器类型的 `ManagerBridgeApi` 公共签名；DailyFortunes RPC 与既有称号登录一样验证真人入服证明 | `bridgeAdmissionTest`；不宣称 Forge 扩展二进制兼容 |
| 停服后的可选 MCA executor 处理 | 保留按存在性反射、正常 shutdown、排队任务完成、无强制退出的行为 | `lifecycleShutdownTest`；对应 Fabric MCA 版本未实测 |

## 两个分支的边界

- `mc-26.1.2`：Minecraft 26.1.2，Fabric Loader 0.19.3，Fabric API 0.152.1+26.1.2，Java 25。
- `mc-26.2`：Minecraft 26.2，Fabric Loader 0.19.3，Fabric API 0.153.0+26.2，Java 25。
- 两版各使用独立 checkout；不要把一个版本的 JAR 改名用于另一个版本。
- 更新前备份现有配置及当前存档中的 `pixelmcmanager/`。正常停服替换对应 JAR，保留配置、统计和桥接身份；回滚时替换为对应 Minecraft 版本的旧 JAR并保留数据备份。
- 本轮没有部署、reload或重启生产服。GitHub 0.5.2 发布包由两个 Manager 版本及共用的可选服务/托管包组成；不附带新的 DailyFortunes 游戏模组 JAR。

## 验证记录

- 2026-10-26：两分支分别使用 Oracle JDK 25.0.1 执行 `gradlew.bat --no-daemon clean build`，全部通过。
- 每个版本通过 73 项 Java 检查：入服证明 12、磁盘补传 9、停服生命周期 7、重启/轻量模组监测 35、MC26 文本 10。入服证明测试调用真实 `BridgeAgent.rpc`，只使用临时 loopback HTTP 端点，不启动 Minecraft。
- 共用托管脚本通过 8 项 Python 检查，包括真实临时子进程重启、相同启动参数、在线/过期/未满15秒/重放拒绝与 MCSM 实例绑定；未连接真实 MCSM。
- 两版 JAR 已验证 ZIP 完整性、许可证与署名、`fabric.mod.json` 的版本/MC版本/纯服务端入口、Java25字节码。sources JAR保留源码中的 Gradle 资源模板是预期行为。
- 本机首次构建遇到 Windows Unix-domain 临时目录导致的 `Unable to establish loopback connection`。仅本次命令设置 `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=D:\Gradle\.tmp` 后干净构建通过；未修改全局环境，也不要求其他工作站使用此目录。
- 两个全新隔离 `run/` 目录分别执行 `runServer --args=nogui`：真实 Minecraft 26.1.2 / 26.2 进程均确认 `PixelMC Manager Fabric 0.5.2 loaded.`，Fabric Loader 0.19.3 与各自 Fabric API 正常解析。EULA 保持默认 false，进程均自行正常退出；这只验证实际主程序与模组入口/事件注册初始化，不代表可游玩世界已启动。26.2 日志含本机 Windows Perflib009/OSHI 计数器告警，模组入口仍正常完成；本轮未修改系统计数器。
- 世界加载、无模组客户端登录、玩家权限、聊天、假人、维度采样、桥接重连、实际重启与可选游戏扩展均**未实机验证**。现有检查不能替代这部分验收。

本地 0.5.2 产物（发布时使用这些文件，位于各 checkout 的 `build/libs/`）：

| 产物 | SHA-256 |
| --- | --- |
| `pixelmcmanager-fabric-26.1.2-0.5.2.jar` | `ffae68b08ea7e69d1f55c0a4bd096a8f96b0d1715e644deead13ba1789380216` |
| `pixelmcmanager-fabric-26.2-0.5.2.jar` | `685b60038e06f866f307ac6dfbd3940e7895acf425aa407ebd110f9b8f2751b0` |
| 两个 `0.5.2-sources.jar`（当前游戏内源码一致） | `f75586db4f2afe35155bfed5793de3c413ced708a763059e08fed4d4c224231f` |

## 0.5.2 原始共享源版本与服务包

下列正式源提交已锁定；两版 Fabric 使用同一个共享服务包，不重编为不同变体。最后的字段中文标签、带版本戳导入预览、抽签备份时间、设置草稿 baseline/日历总数合并及 HTTP 环境 ID 兼容等修正均由此共享服务提供。

| 源码 | 精确提交 |
| --- | --- |
| [Forge / Manager Service / 正式网页](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/961cde16accdbe72601a4662ad99afcaf8e995f5) | `961cde16accdbe72601a4662ad99afcaf8e995f5` |
| [MemorialTitles 共享业务](https://github.com/PixelMC-Projects/MemorialTitles/tree/0bc729f3030c5d0a0df6c8442a561c3a9cc8685a) | `0bc729f3030c5d0a0df6c8442a561c3a9cc8685a` |
| [DailyFortunes 共享业务](https://github.com/PixelMC-Projects/DailyFortunes/tree/9e5b6e6c14fa4cede433eadc126b3abe172333f0) | `9e5b6e6c14fa4cede433eadc126b3abe172333f0` |

原 `v0.5.2` 标签内的 `forge-source-parity.json` 按当时的最终 Forge 提交验证全部 38 个 Java 源文件及从 0.5.1 起的 68 个变更路径。服务包内的 README 包含三个仓库的完整源链接；构建参数见本仓 `BUILDING.md`。这些源码链接不表示 DailyFortunes 游戏模组已发布或已适配 Fabric。

| 共用附件 | SHA-256 |
| --- | --- |
| `pixelmc-manager-service-0.5.2.zip` | `bf7d99ac0c9fe47ad914ebaf5a4cbf3323ee96fc47d6144a0d1f1df031e87122` |
| `pixelmcmanager-restart-supervisor-0.5.2.zip` | `e0abdcf74106b8db643271c3761b9356197f59db37f3254295fdcff98a348ec8` |

## 0.5.2-web.1 服务头像补丁

- 目的：纠正 DailyFortunes 玩家头像过小，列表恢复 32×32px，详情恢复 40×40px；Vite 开发扫描限定为正式 `index.html` 入口。网页源固定为 [e1ccabd17aa53137361ff8e000f06fe53ce1934d](https://github.com/PixelMC-Projects/pixelmcmanager-forge/tree/e1ccabd17aa53137361ff8e000f06fe53ce1934d)。当前 `forge-source-parity.json` 已更新到此提交，覆盖同一 38 个游戏内 Java 文件及 80 个 Forge 变更路径。新增 Forge 实例运维协调脚本按归属登记，不移植或执行为 Fabric 部署。
- 范围：在已发布原服务 JAR 上仅替换 `web/` 与 manifest 版本。619 个 `.class` 及所有其他后端/默认资源条目逐字节不变；原 MemorialTitles / DailyFortunes 后端源码 pin 沿用上表。两个 Fabric 游戏 JAR 和 sources JAR 的原 SHA-256 均保持不变。本轮不重新编译 Minecraft 模组。
- 兼容：现有 Minecraft 26.1.2 / 26.2 的 Fabric Manager 0.5.2；独立服务继续使用 Java 17 + MariaDB。补丁只发布服务 ZIP 和 `SHA256SUMS`，保留原 `v0.5.2` 标签及七个附件，并继续将完整 0.5.2 标为 Latest。
- 维护方验收：最终补丁 JAR 使用真实本地 SQL/API 浏览器检查，列表 32×32、详情 40×40；深浅主题截图保存于共享 Forge 工程的 `web-console/screenshots/dailyfortunes-production-20261026/calendar-web-patch-{dark,light}.png`。这是隔离本地 UI 证据，不是生产游戏联调。
- 升级/回滚：正常停止独立网页服务，保留配置与数据后替换 `service.jar`，按原命令启动并刷新网页；回滚仅恢复原 0.5.2 的 `service.jar`。本轮未部署 Fabric 或操作其游戏进程。

| 附件/内容 | SHA-256 |
| --- | --- |
| `pixelmc-manager-service-0.5.2-web.1.zip` | `462442b24c48ccde9493011a84e117b8dc81a12d550824c8a52a85204d935b2c` |
| ZIP 内 `service.jar` | `d13c8c8e4817f9b55e56f53eb5070eee63b1d71a6a28a26c4e8399a584b1d1e6` |
