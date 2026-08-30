# Code2Prompt 开发约定

这是 Android Studio / IntelliJ Platform 插件。修改前先阅读 `build.gradle.kts`、`gradle.properties` 与 `src/main/resources/META-INF/plugin.xml`。

## 发布边界

- 默认只生成本地已签名测试包；只有用户明确要求时，才提交 Git、推送远程、创建 GitHub Release 或上传 Marketplace。
- 私钥、证书与密码位于被忽略的 `release-secrets/`；仅可通过下方签名命令使用，禁止输出、提交、共享或替换其中内容。
- 发布包为 `build/distributions/Code2Prompt-<version>-signed.zip`。
- 每次可发布变更必须递增 `pluginVersion`，并更新 `changeNotes`。

## Action 与入口稳定性

- 保持插件 ID `com.wayen.code2prompt` 不变。
- 保持 Action ID `com.wayen.code2prompt.CopySelectionWithContextV2` 不变。
- 保持当前入口注册不变：

  ```xml
  <add-to-group group-id="EditorPopupMenu" anchor="after" relative-to-action="$Copy"/>
  <add-to-group group-id="Floating.CodeToolbar" anchor="after" relative-to-action="CommentByLineComment"/>
  ```

- Action 应始终可见；仅在没有有效代码选区时禁用。不要使用 `isEnabledAndVisible` 隐藏 Action，否则无法在浮动工具栏中发现或配置。
- 浮动工具栏由插件自动注册；文档和测试流程不要求用户手动添加该 Action。
- 若必须调整浮动工具栏 group 或 anchor，先在“全新安装”和“从上一正式版覆盖升级”两种场景中验证，防止 IDE 保留旧 Action 条目而显示重复图标。

## 兼容性与验证

- 最低支持构建为 `253`；不要无验证地提高 `sinceBuild`。
- 签名构建命令：

  ```bash
  PRIVATE_KEY_PASSWORD="$(cat release-secrets/private-key-password.txt)" \
    ./gradlew test buildPlugin verifyPluginStructure signPlugin verifyPluginSignature
  ```

- 在真实 Android Studio 中至少验证：选中 `.kt` 或 `.java` 代码后，浮动栏只显示一个 Code2Prompt 复制入口；右键菜单中的 `Copy Selection with Context` 位于系统 `Copy` 后方；执行后剪贴板内容包含文件、行号和符号上下文。
