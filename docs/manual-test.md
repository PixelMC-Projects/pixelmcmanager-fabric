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
25. With no active maintenance plan, run `/pixelmcmanager maintenance status`.
26. Run `/pixelmcmanager maintenance 1m`, then `/pixelmcmanager maintenance status`, and confirm it shows waiting for maintenance.
27. Confirm `/pixelmcmanager maintenance 1m` sends the 1 minute reminder and the final 10 second countdown.
28. Run `/pixelmcmanager maintenance 10s` and confirm it sends 10 to 1 second chat and subtitle countdown messages.
29. At maintenance time, confirm online players are kicked with two colored lines: `服务器开始执行维护!` and `详细信息见QQ群:768322731`.
30. While maintenance is active, confirm new joins are rejected with two colored lines: `服务器正在维护中!` and `详细信息见QQ群:768322731`.
31. While maintenance is active, run `/pixelmcmanager maintenance now` and confirm it is rejected.
32. While maintenance is active, run `/pixelmcmanager maintenance 1m` and confirm it is rejected.
33. Before scheduled maintenance starts, run `/pixelmcmanager maintenance 5m` again and confirm the old plan is replaced and timing restarts.
34. Before scheduled maintenance starts, run `/pixelmcmanager maintenance off` and confirm the plan is cancelled.
35. While maintenance is active, run `/pixelmcmanager maintenance off` and confirm it says `已解除服务器维护状态，玩家现在可以重新加入。`.
36. Type `/pixelmcmanager maintenance ` and confirm Tab suggestions include `now`, `off`, `status`, and common time values.
37. Confirm `stopserver cancel` does not disable active maintenance, and `maintenance off` does not cancel a stopserver plan.
38. Confirm a client without PixelMC Manager can still join normally when maintenance is inactive.
39. Run a successful `/pixelmcmanager reload`, then `/pixelmcmanager audit`, and confirm a reload record appears.
40. Run `/pixelmcmanager stopserver 1m`, then confirm audit shows a stopserver record.
41. Run `/pixelmcmanager stopserver cancel`, then confirm audit shows a stopserver cancel record.
42. Run `/pixelmcmanager maintenance 1m`, then confirm audit shows a maintenance record.
43. Run `/pixelmcmanager maintenance now`, then confirm audit shows a maintenance now record.
44. Run `/pixelmcmanager maintenance off`, then confirm audit shows a maintenance off record.
45. Confirm `/pixelmcmanager audit` shows the recent 10 records.
46. Confirm `/pixelmcmanager audit last` shows the recent 10 records.
47. Confirm `/pixelmcmanager audit last 20` shows up to 20 recent records.
48. Confirm `/pixelmcmanager audit last 0` and `/pixelmcmanager audit last 51` report the 1 to 50 range error.
49. Confirm status, logincount, logintime, preview, and audit query commands do not create audit records.
50. Confirm audit records are stored at `<world-save-root>/pixelmcmanager/audit.jsonl`.
51. Restart the server and confirm recent audit records can still be queried.
52. Confirm a client without PixelMC Manager can still join normally when maintenance is inactive.
