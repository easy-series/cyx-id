package com.caoyixin.cyxid.core;

import java.util.ArrayList;
import java.util.List;

/**
 * ID生成器接口
 * 所有ID生成器都必须实现此接口
 */
public interface IdGenerator {
    
    /**
     * 生成唯一ID
     *
     * @return 生成的唯一ID
     */
    long generate();
    
    /**
     * 批量生成唯一ID
     *
     * @param size 批量大小
     * @return 生成的唯一ID列表
     */
    default List<Long> batchGenerate(int size) {
        List<Long> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(generate());
        }
        return ids;
    }
    
    /**
     * 获取ID生成器名称
     *
     * @return ID生成器名称
     */
    String getName();

    String getType();
}