package com.jouryu.netty;

import com.jouryu.common.CatchData;
import com.jouryu.netty.client.TestTCPClient;
import com.jouryu.netty.server.SocketServer;
import com.jouryu.netty.server.TCPServer;
import com.jouryu.service.MonitorService;
import com.jouryu.service.SimulatorService;
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
    @Qualifier("socketServer")
    private SocketServer socketServer;

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
        new Thread(socketServer).start();
        // 启动模拟的传感器客户端
        testTcpClient.startClient();
    }

    /**
     * 初始化服务执行前所需要的所有数据
     */
    private void init() {
        CatchData.monitorList = monitorService.getMonitors();
        CatchData.simulatorList = simulatorService.getSimulator();
    }
}
