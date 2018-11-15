# 基于Netty的socket server

------

## 介绍

使用Netty分别实现了两个Socket server和一个socket client：
> * server1:9099 主要用来跟硬件传感器通信
> * server2:8888/websocket 作为websocket服务端跟网页通信
> * client 作为模拟客户端，跟server1建立连接后，不断给服务端发送假数据

整个项目启动后，主要做了下面几件事:

- [ ] 创建socket server和socket client，并建立连接
- [ ] 执行定时任务，每5秒socket server往所有连接的socket client发送请求数据命令
- [ ] socket client接受到请求数据的命令后，从mysql中读取假数据，伪造成真实设备传输的数据格式，并发送给socket server
- [ ] socket server接收到返回的数据后，分别写入到hbase数据库和kafka队列中
- [ ] 最后调用websocket server，往所有跟它建立的客户端发送接收到的数据