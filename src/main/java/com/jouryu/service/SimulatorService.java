package com.jouryu.service;

import com.jouryu.mapper.SimulatorMapper;
import com.jouryu.model.Simulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tomorrow on 18/11/14.
 */

@Service
public class SimulatorService {
    @Autowired
    private SimulatorMapper simulatorMapper;

    public List<Simulator> getSimulator() {
        return simulatorMapper.getSimulator();
    }
}
