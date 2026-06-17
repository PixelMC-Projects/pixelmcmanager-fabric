# AGENTS.md

This repository is the official Fabric port of PixelMC Manager.

Hard rules:
- Keep mod id as pixelmcmanager.
- Keep this project Fabric-only.
- Use Minecraft-version branches; this version lives on mc-26.1.2.
- Do not add Forge, NeoForge, Connector, or Architectury.
- Do not require client installation.
- Do not add gameplay content, blocks, items, entities, GUI, Mixin, or networking unless explicitly requested.
- Store player stats under the current world save root, not global config.
- Track playtime using real wall-clock milliseconds, not ticks.
- Keep stopserver and maintenance states independent; stopserver join rejection has priority.
- Use Gradle/Maven Aliyun mirrors where applicable, while preserving Fabric official Maven.
- Always run a clean Gradle build before claiming success.
- Push to git@github.com:zcpu954861/pixelmcmanager-fabric.git only after build succeeds.

GitHub Release title format:
- Forge: PixelMC Manager Forge <mod_version>
- Fabric: PixelMC Manager Fabric <mod_version>

Examples:
- PixelMC Manager Forge 0.2.2
- PixelMC Manager Fabric 0.2.2

Do not include Minecraft version or a leading "v" in the release title.
Tags may use the v-prefix, such as v0.2.2.
Jar filenames may include loader, Minecraft version, and mod version.

Release notes must include a "兼容版本与附件" section.
List each supported Minecraft version and the exact jar asset name there.
Do not use a single "适用环境" section as the only compatibility description, because the same mod version may later include multiple Minecraft versions.
