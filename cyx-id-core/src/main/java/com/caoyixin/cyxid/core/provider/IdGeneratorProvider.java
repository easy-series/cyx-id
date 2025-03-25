package com.caoyixin.cyxid.core.provider;

import com.caoyixin.cyxid.core.IdGenerator;

/**
 * ID生成器提供者接口
 * 用于管理和获取不同的ID生成器实例
 */
public interface IdGeneratorProvider {
    
    /**
     * 默认共享的ID生成器名称
     */
    String SHARE = "__share__";
    
    /**
     * 获取ID生成器
     *
     * @param name ID生成器名称
     * @return ID生成器实例
     */
    IdGenerator getGenerator(String name);
    
    /**
     * 获取默认的共享ID生成器
     *
     * @return 默认的共享ID生成器
     */
    default IdGenerator getGenerator() {
        return getGenerator(SHARE);
    }
    
    /**
     * 注册ID生成器
     *
     * @param name ID生成器名称
     * @param generator ID生成器实例
     */
    void registerGenerator(String name, IdGenerator generator);
    
    /**
     * 注册默认的共享ID生成器
     *
     * @param generator ID生成器实例
     */
    default void registerGenerator(IdGenerator generator) {
        registerGenerator(SHARE, generator);
    }
} 