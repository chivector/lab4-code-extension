# 实验四代码-拓展

计算机网络课程设计实验四拓展版，实现了基于 Java Swing 的聊天客户端和 Socket 服务端。

## 功能

- 多客户端连接与在线用户列表
- 现代微信风格聊天界面
- 广播聊天与私聊
- 好友请求与好友通知
- 群聊扩展
- 表情快捷选择与发送
- 图片、语音和普通文件专用发送入口
- 稳定连接：服务端持续监听、客户端连接超时重试和断线清理
- 聊天记录、个人资料和动态等拓展功能
- `screenshots/` 目录包含功能演示截图

## 目录结构

```text
com/cncd/ch04/client/   聊天客户端
com/cncd/ch04/server/   聊天服务端
com/cncd/ch04/          实验演示运行器
screenshots/            演示截图
```

## 运行方式

在项目根目录编译：

```bash
javac -encoding UTF-8 com/cncd/ch04/server/*.java com/cncd/ch04/client/*.java com/cncd/ch04/ChatExperimentRunner.java
```

每次修改 `.java` 后都需要重新编译，否则 `java -cp . ...` 会继续运行旧的 `.class` 文件。

启动服务端：

```bash
java com.cncd.ch04.server.MainServer 3500
```

启动客户端：

```bash
java com.cncd.ch04.client.ChatClient
```

跨电脑连接时，在客户端高级设置中填写服务端电脑的局域网 IP 和端口 `3500`。如果连接超时，请确认服务端已显示 `Server Listening at port: 3500`，并在服务端电脑防火墙中放行 TCP 3500 端口。

也可以运行实验演示器生成/查看演示流程：

```bash
java com.cncd.ch04.ChatExperimentRunner
```
