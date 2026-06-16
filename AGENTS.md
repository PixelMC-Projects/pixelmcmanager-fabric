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
- Use Gradle/Maven Aliyun mirrors where applicable, while preserving Fabric official Maven.
- Always run a clean Gradle build before claiming success.
- Push to git@github.com:zcpu954861/pixelmcmanager-fabric.git only after build succeeds.
