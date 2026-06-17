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
15. With no active plan, run `/pixelmcmanager stopserver status` and confirm it says `当前没有正在进行的服务器停机计划。`.
16. Run `/pixelmcmanager stopserver 1m`, then `/pixelmcmanager stopserver status`, and confirm it shows `等待维护开始`.
17. Confirm the status maintenance start time is the scheduled kickTime.
18. Confirm the status final stop time is kickTime plus 15 seconds.
19. Confirm the status remaining time decreases with real wall-clock time.
20. Run `/pixelmcmanager stopserver cancel`, then `/pixelmcmanager stopserver status`, and confirm it shows no active plan.
21. Run `/pixelmcmanager stopserver 10s`, wait until players are kicked but before final stop, then run `/pixelmcmanager stopserver status` from console and confirm it shows maintenance in progress.
22. Type `/pixelmcmanager stopserver ` and confirm Tab suggestions include both `status` and `cancel`.
23. Confirm the original `<time>` argument still accepts values such as `/pixelmcmanager stopserver 10s`.
24. Confirm a client without PixelMC Manager can still join normally when no maintenance is active.
