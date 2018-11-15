package com.jouryu.hbase;

import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import org.apache.hadoop.hbase.client.Mutation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tomorrow on 18/11/14.
 */

@Service
public class HbaseService {
    @Autowired
    private HbaseTemplate hbaseTemplate;

    public List<Mutation> saveOrUpdate(String tableName, List<Mutation> datas) {
        hbaseTemplate.saveOrUpdates(tableName, datas);
        return datas;
    }
}
