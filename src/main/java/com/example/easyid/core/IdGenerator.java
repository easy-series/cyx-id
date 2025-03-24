package com.example.easyid.core;

/**
 * ID生成器接口，所有ID生成策略的顶层接口
 */
public interface IdGenerator {
    
    /**
     * 生成下一个唯一ID
     *
     * @return 生成的唯一ID
     */
    long nextId();
    
    /**
     * 获取ID生成器实例名称
     *
     * @return ID生成器名称
     */
    String getName();
    
    /**
     * 将long类型的ID转为字符串格式
     *
     * @param id long类型ID
     * @return 字符串格式的ID
     */
    default String convertId(long id) {
        return String.valueOf(id);
    }
    
    /**
     * 生成下一个ID并转为字符串
     *
     * @return 字符串格式的ID
     */
    default String nextStringId() {
        return convertId(nextId());
    }
} 