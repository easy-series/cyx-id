package com.caoyixin.cyxid.segment;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.exception.CyxIdException;
import com.caoyixin.cyxid.segment.buffer.SegmentBuffer;
import com.caoyixin.cyxid.segment.buffer.SegmentBufferManager;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

/**
 * 分段ID生成器
 * 基于分段方式的ID生成器实现，使用双缓冲机制提高性能
 */
@Slf4j
public class SegmentIdGenerator implements IdGenerator {
    
    /**
     * 生成器名称
     */
    private final String name;
    
    /**
     * 分段缓冲器管理器
     */
    private final SegmentBufferManager bufferManager;
    
    /**
     * 创建分段ID生成器
     *
     * @param name 生成器名称
     * @param segmentAllocator ID段分配器
     */
    public SegmentIdGenerator(String name, SegmentAllocator segmentAllocator) {
        this.name = name;
        this.bufferManager = new SegmentBufferManager(segmentAllocator);
        
        // 预初始化缓冲器
        getBuffer();
        
        log.info("初始化分段ID生成器：{}", name);
    }
    
    @Override
    public long generate() {
        // 获取当前生成器的ID缓冲区
        SegmentBuffer buffer = getBuffer();
        
        // 如果发现ID缓冲区快用完，异步加载下一段
        if (buffer.shouldLoadNext()) {
            bufferManager.loadNextSegmentAsync(buffer);
        }
        
        // 获取下一个ID
        long id = buffer.nextId();
        
        // 如果当前段用完，切换到下一段
        if (buffer.isExhausted()) {
            waitAndSwitchBuffer(buffer);
            return generate();
        }
        
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * 获取ID生成器类型
     *
     * @return ID生成器类型
     */
    @Override
    public String getType() {
        return "segment";
    }
    
    /**
     * 获取当前生成器的ID缓冲器
     *
     * @return ID缓冲器
     */
    private SegmentBuffer getBuffer() {
        return bufferManager.getBuffer(name);
    }
    
    /**
     * 等待下一个ID段加载完成并切换
     *
     * @param buffer ID缓冲器
     */
    private void waitAndSwitchBuffer(SegmentBuffer buffer) {
        synchronized (buffer) {
            // 如果下一个段已经加载好，直接切换
            if (buffer.getNextSegment() != null) {
                buffer.switchToNextSegment();
                return;
            }
            
            // 如果没有在加载，触发加载
            if (!buffer.isLoadingNext()) {
                bufferManager.loadNextSegmentAsync(buffer);
            }
            
            // 等待加载完成
            try {
                long start = System.currentTimeMillis();
                // 最多等待10秒
                long waitTimeout = 10000;
                
                while (buffer.getNextSegment() == null) {
                    if (System.currentTimeMillis() - start > waitTimeout) {
                        throw new CyxIdException("等待下一个ID段超时，生成器：" + name);
                    }
                    
                    log.info("等待下一个ID段加载完成，生成器：{}", name);
                    // 等待500ms后重新检查
                    buffer.wait(500);
                }
                
                // 切换到下一个段
                buffer.switchToNextSegment();
                
            } catch (InterruptedException e) {
                log.warn("等待ID段时被中断", e);
                Thread.currentThread().interrupt();
                throw new CyxIdException("等待ID段时被中断", e);
            }
        }
    }
} 