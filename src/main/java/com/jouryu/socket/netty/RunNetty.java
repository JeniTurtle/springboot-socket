package com.jouryu.socket.netty;

import com.jouryu.socket.common.CacheData;
import com.jouryu.socket.netty.client.TestTCPClient;
import com.jouryu.socket.netty.server.StormSocketServer;
import com.jouryu.socket.netty.server.WebSocketServer;
import com.jouryu.socket.netty.server.TCPServer;
import com.jouryu.socket.service.MonitorService;
import com.jouryu.socket.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by tomorrow on 18/11/8.
 */

@Component
@Qualifier("runNetty")
public class RunNetty {
    @Autowired
    @Qualifier("webSocketServer")
    private WebSocketServer webSocketServer;

    @Autowired
    @Qualifier("stormSocketServer")
    private StormSocketServer stormSocketServer;

    @Autowired
    @Qualifier("tcpServer")
    private TCPServer tcpServer;

    @Autowired
    @Qualifier("testTcpClient")
    private TestTCPClient testTcpClient;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private SimulatorService simulatorService;

    public void run() throws Exception {
        init();
        new Thread(tcpServer).start();
        new Thread(stormSocketServer).start();
        new Thread(webSocketServer).start();
        // 启动模拟的传感器客户端
        testTcpClient.startClient();
    }

    /**
     * 初始化服务执行前所需要的所有数据
     */
    private void init() {
        CacheData.monitorList = monitorService.getMonitors();
        CacheData.simulatorList = simulatorService.getSimulator();
    }
}
