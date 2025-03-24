package com.example.easyid.snowflake;

import com.example.easyid.core.IdGenerator;

/**
 * 雪花算法ID生成器接口
 */
public interface SnowflakeId extends IdGenerator {
    
    /**
     * 获取时间戳位数
     *
     * @return 时间戳位数
     */
    int getTimestampBits();
    
    /**
     * 获取机器号位数
     *
     * @return 机器号位数
     */
    int getMachineBits();
    
    /**
     * 获取序列号位数
     *
     * @return 序列号位数
     */
    int getSequenceBits();
    
    /**
     * 获取机器号
     *
     * @return 机器号
     */
    int getMachineId();
    
    /**
     * 将ID解析为可读的格式
     *
     * @param id 雪花算法生成的ID
     * @return 可读的格式化字符串
     */
    String parseId(long id);
} 