# 实验四代码-拓展

计算机网络课程设计实验四拓展版，实现了基于 Java Swing 的聊天客户端和 Socket 服务端。

## 功能

- 多客户端连接与在线用户列表
- 广播聊天与私聊
- 好友请求与好友通知
- 群聊扩展
- 表情快捷选择与发送
- 文件发送
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

启动服务端：

```bash
java com.cncd.ch04.server.MainServer 3500
```

启动客户端：

```bash
java com.cncd.ch04.client.ChatClient
```

也可以运行实验演示器生成/查看演示流程：

```bash
java com.cncd.ch04.ChatExperimentRunner
```
