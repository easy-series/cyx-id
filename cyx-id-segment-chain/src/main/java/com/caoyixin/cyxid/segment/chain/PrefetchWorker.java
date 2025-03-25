package com.caoyixin.cyxid.segment.chain;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 预取工作器
 * 定期检查所有缓存器，为需要预取的缓存器执行预取操作
 */
@Slf4j
public class PrefetchWorker implements Runnable {
    
    /**
     * 链式分段ID缓存管理器
     */
    private final SegmentChainBufferManager bufferManager;
    
    /**
     * 创建预取工作器
     *
     * @param bufferManager 链式分段ID缓存管理器
     */
    public PrefetchWorker(SegmentChainBufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }
    
    @Override
    public void run() {
        try {
            // 获取所有缓存器
            Map<String, SegmentChainBuffer> bufferMap = bufferManager.getBufferMap();
            if (bufferMap.isEmpty()) {
                return;
            }
            
            log.debug("开始检查需要预取的缓存器，共{}个", bufferMap.size());
            
            // 检查每个缓存器是否需要预取
            for (SegmentChainBuffer buffer : bufferMap.values()) {
                checkAndPrefetch(buffer);
            }
            
        } catch (Exception e) {
            log.error("预取工作器执行异常", e);
        }
    }
    
    /**
     * 检查缓存器是否需要预取，如果需要则执行预取
     *
     * @param buffer 缓存器
     */
    private void checkAndPrefetch(SegmentChainBuffer buffer) {
        try {
            if (buffer.shouldPrefetch()) {
                log.debug("缓存器[{}]需要预取", buffer.getName());
                bufferManager.prefetchNextSegment(buffer);
            }
        } catch (Exception e) {
            log.error("检查预取状态异常：{}", buffer.getName(), e);
        }
    }
} 