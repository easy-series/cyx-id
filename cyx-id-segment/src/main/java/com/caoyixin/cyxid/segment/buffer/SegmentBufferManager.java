package com.caoyixin.cyxid.segment.buffer;

import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分段ID缓存管理器
 * 负责管理多个分段ID缓存器，并提供异步加载下一个ID段的能力
 */
@Slf4j
public class SegmentBufferManager {
    
    /**
     * 缓存器映射，key为ID段名称
     */
    private final Map<String, SegmentBuffer> bufferMap = new ConcurrentHashMap<>();
    
    /**
     * ID段分配器
     */
    private final SegmentAllocator segmentAllocator;
    
    /**
     * 异步加载线程池
     */
    private final ExecutorService loadingExecutor;
    
    /**
     * 创建分段ID缓存管理器
     *
     * @param segmentAllocator ID段分配器
     */
    public SegmentBufferManager(SegmentAllocator segmentAllocator) {
        this.segmentAllocator = segmentAllocator;
        
        // 创建加载线程池，使用自定义线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(0);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("segment-loader-" + threadCount.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        };
        
        this.loadingExecutor = Executors.newFixedThreadPool(2, threadFactory);
    }
    
    /**
     * 获取或创建分段ID缓存器
     *
     * @param name ID段名称
     * @return 分段ID缓存器
     */
    public SegmentBuffer getBuffer(String name) {
        return bufferMap.computeIfAbsent(name, this::createBuffer);
    }
    
    /**
     * 创建新的分段ID缓存器
     *
     * @param name ID段名称
     * @return 新创建的分段ID缓存器
     */
    private SegmentBuffer createBuffer(String name) {
        log.info("创建分段ID缓存器：{}", name);
        SegmentBuffer buffer = new SegmentBuffer(name);
        
        // 初始化缓存器
        initBuffer(buffer);
        
        return buffer;
    }
    
    /**
     * 初始化缓存器，加载第一个ID段
     *
     * @param buffer 缓存器
     */
    private void initBuffer(SegmentBuffer buffer) {
        if (buffer.isInitialized()) {
            return;
        }
        
        synchronized (buffer) {
            if (buffer.isInitialized()) {
                return;
            }
            
            // 加载第一个ID段
            IdSegment segment = segmentAllocator.nextSegment(buffer.getName());
            buffer.setCurrentSegment(segment);
            buffer.getCurrentValue().set(segment.getMinId() - 1);  // 预设为最小值-1，首次nextId会得到最小值
            buffer.setInitialized(true);
            
            log.info("初始化缓存器[{}]完成，初始ID段：{}", buffer.getName(), segment);
        }
    }
    
    /**
     * 异步加载下一个ID段
     *
     * @param buffer 缓存器
     */
    public void loadNextSegmentAsync(SegmentBuffer buffer) {
        if (!buffer.startLoadingNextSegment()) {
            // 已经有加载任务在执行
            return;
        }
        
        String name = buffer.getName();
        loadingExecutor.execute(() -> {
            try {
                log.debug("异步加载下一个ID段：{}", name);
                IdSegment nextSegment = segmentAllocator.nextSegment(name);
                buffer.setNextSegment(nextSegment);
                log.debug("加载下一个ID段成功：{} -> {}", name, nextSegment);
            } catch (Exception e) {
                log.error("加载下一个ID段失败：{}", name, e);
                // 重置加载状态，允许下次重试
                buffer.setIsLoadingNext(false);
            }
        });
    }
    
    /**
     * 关闭管理器，释放资源
     */
    public void shutdown() {
        if (loadingExecutor != null) {
            loadingExecutor.shutdown();
        }
    }
} 