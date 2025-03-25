package com.caoyixin.cyxid.storage.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 机器状态
 * 用于存储机器ID和最近时间戳
 */
@Getter
@AllArgsConstructor
public class MachineState {
    
    /**
     * 未找到的机器状态
     */
    public static final MachineState NOT_FOUND = new MachineState(-1, -1);
    
    /**
     * 机器ID
     */
    private final int machineId;
    
    /**
     * 最近时间戳
     */
    private final long lastTimeStamp;
    
    /**
     * 创建机器状态
     *
     * @param machineId 机器ID
     * @param lastStamp 最近时间戳
     * @return 机器状态
     */
    public static MachineState of(int machineId, long lastStamp) {
        return new MachineState(machineId, lastStamp);
    }
} 