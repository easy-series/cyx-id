package com.caoyixin.cyxid.core.provider;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.exception.CyxIdException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的ID生成器提供者实现
 */
public class DefaultIdGeneratorProvider implements IdGeneratorProvider {
    
    private final Map<String, IdGenerator> generatorMap;
    
    public DefaultIdGeneratorProvider() {
        this.generatorMap = new ConcurrentHashMap<>();
    }
    
    @Override
    public IdGenerator getGenerator(String name) {
        IdGenerator generator = generatorMap.get(name);
        if (generator == null) {
            throw new CyxIdException("找不到名称为[" + name + "]的ID生成器");
        }
        return generator;
    }
    
    @Override
    public void registerGenerator(String name, IdGenerator generator) {
        generatorMap.put(name, generator);
    }
} 