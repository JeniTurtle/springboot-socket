package com.jouryu.service;

import com.jouryu.mapper.MonitorMapper;
import com.jouryu.model.Monitor;
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
