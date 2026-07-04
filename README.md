# 实验四代码-拓展

计算机网络课程设计实验四拓展版，实现了 Java Socket 聊天服务端、Swing 备用客户端，以及一个现代 Web 前端 + Java 本地桥接客户端。

## 功能

- 多客户端连接与在线用户列表
- 现代 Web 聊天界面，仿微信/QQ 桌面端三栏布局
- 保留 Java Swing 备用客户端
- 广播聊天与私聊
- 私聊离线待发送、上线自动补发与已读/未读状态
- 好友请求与好友通知
- 群聊扩展
- 表情快捷选择与发送
- 图片、语音文件、浏览器录音和普通文件专用发送入口
- Web 端支持拖拽文件、粘贴图片、未读提示、会话筛选和 toast 状态反馈
- 稳定连接：服务端持续监听、客户端连接超时重试和断线清理
- 聊天记录、个人资料和动态等拓展功能
- `screenshots/` 目录包含功能演示截图

## 目录结构

```text
com/cncd/ch04/client/   聊天客户端
com/cncd/ch04/web/      Web 前端本地桥接后端
com/cncd/ch04/server/   聊天服务端
com/cncd/ch04/          实验演示运行器
web/                    浏览器前端页面
screenshots/            演示截图
```

## 运行方式

在项目根目录编译：

```bash
javac -encoding UTF-8 com/cncd/ch04/server/*.java com/cncd/ch04/client/*.java com/cncd/ch04/web/*.java com/cncd/ch04/ChatExperimentRunner.java
```

每次修改 `.java` 后都需要重新编译，否则 `java -cp . ...` 会继续运行旧的 `.class` 文件。

启动服务端：

```bash
java com.cncd.ch04.server.MainServer 3500
```

启动默认 Swing 客户端：

```bash
java com.cncd.ch04.client.ChatClient
```

启动现代 Web 客户端：

```bash
java com.cncd.ch04.web.WebChatLauncher 8088
```

浏览器会打开 `http://127.0.0.1:8088/`。在页面左侧填写聊天服务端 IP、端口和昵称后连接即可。跨电脑连接时，服务端仍然填写对方电脑的局域网 IP，例如 `172.20.10.2`，端口通常是 `3500`。

也可以通过原客户端入口显式启动 Web 版：

```bash
java com.cncd.ch04.client.ChatClient --web 8088
```

如果只想启动 Web 桥接而不自动打开浏览器：

```bash
java com.cncd.ch04.web.WebChatLauncher 8088 --no-open
```

使用一键脚本启动 Web 版：

```bash
start-web-client.bat
```

跨电脑连接时，在客户端高级设置中填写服务端电脑的局域网 IP 和端口 `3500`。如果连接超时，请确认服务端已显示 `Server Listening at port: 3500`，并在服务端电脑防火墙中放行 TCP 3500 端口。

也可以运行实验演示器生成/查看演示流程：

```bash
java com.cncd.ch04.ChatExperimentRunner
```
