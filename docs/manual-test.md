# Manual Test

1. Build the Fabric jar with `.\gradlew.bat clean build`.
2. Copy the generated jar from `build/libs/` into a Fabric 26.1.2 server `mods/` directory.
3. Verify a vanilla client without this mod can join when maintenance mode is inactive.
4. Join with a new player and verify first-join welcome messages.
5. Rejoin and verify returning welcome messages.
6. Verify stats are written under `<world-save-root>/pixelmcmanager/player_stats.json`.
7. Run `/pixelmcmanager logincount`.
8. Run `/pixelmcmanager logintime`.
9. Verify Tab completion for `<player>` includes recorded offline players.
10. Run `/pixelmcmanager stopserver 10s` and verify countdown messages, subtitle warnings, kick text, and safe server stop.
11. Run `/pixelmcmanager stopserver cancel` before the stop fires and verify the stop is cancelled.
12. During the maintenance window, attempt to join and verify the custom two-line colored rejection message.
13. Verify total playtime accumulates real elapsed wall-clock time and is not tied to server tick speed.
14. Force stop or crash test only in a disposable world and verify at most one checkpoint interval of online time is lost.
