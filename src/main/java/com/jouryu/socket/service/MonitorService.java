package com.jouryu.socket.service;

import com.jouryu.socket.mapper.MonitorMapper;
import com.jouryu.socket.model.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorService {

    @Autowired
    private MonitorMapper monitorMapper;

    public List<Monitor> getMonitors() {
        return monitorMapper.getMonitors();
    }

}
