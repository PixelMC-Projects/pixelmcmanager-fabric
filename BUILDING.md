# 构建 / Building

使用 JDK 25 并设置 `JAVA_HOME`，在当前 Minecraft 版本分支中运行：
Use JDK 25 (`JAVA_HOME`) and build from the matching Minecraft version branch:

```sh
./gradlew clean build
```

Windows 使用 `gradlew.bat clean build`。依赖使用大陆镜像与 Fabric 官方 Maven；产物位于 `build/libs/`。
On Windows use `gradlew.bat clean build`. Dependencies use mainland China mirrors and official Fabric Maven. Outputs are in `build/libs/`.
