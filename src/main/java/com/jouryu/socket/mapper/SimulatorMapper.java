package com.jouryu.socket.mapper;

import com.jouryu.socket.model.Simulator;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by tomorrow on 18/11/14.
 */

@Mapper
@Component
public interface SimulatorMapper {
    List<Simulator> getSimulator();
}
