package com.caoyixin.cyxid.segment.chain;

import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 链式分段ID缓存管理器
 * 负责管理多个链式分段ID缓存器，并提供异步预取功能
 */
@Slf4j
public class SegmentChainBufferManager {
    
    /**
     * 缓存器映射，key为ID段名称
     */
    private final Map<String, SegmentChainBuffer> bufferMap = new ConcurrentHashMap<>();
    
    /**
     * ID段分配器
     */
    private final SegmentAllocator segmentAllocator;
    
    /**
     * 异步预取线程池
     */
    private final ScheduledExecutorService prefetchExecutor;
    
    /**
     * 预取任务
     */
    private final PrefetchWorker prefetchWorker;
    
    /**
     * 预取检查周期（毫秒）
     */
    private final long prefetchPeriod;
    
    /**
     * 创建链式分段ID缓存管理器
     *
     * @param segmentAllocator ID段分配器
     * @param prefetchPeriod 预取检查周期（毫秒）
     */
    public SegmentChainBufferManager(SegmentAllocator segmentAllocator, long prefetchPeriod) {
        this.segmentAllocator = segmentAllocator;
        this.prefetchPeriod = prefetchPeriod;
        
        // 创建预取线程池
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(0);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("segment-chain-prefetcher-" + threadCount.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        };
        
        this.prefetchExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.prefetchWorker = new PrefetchWorker(this);
        
        // 启动预取线程
        startPrefetchWorker();
        
        log.info("初始化链式分段ID缓存管理器，预取周期：{}ms", prefetchPeriod);
    }
    
    /**
     * 启动预取工作线程
     */
    private void startPrefetchWorker() {
        prefetchExecutor.scheduleWithFixedDelay(
                prefetchWorker,
                prefetchPeriod,
                prefetchPeriod,
                TimeUnit.MILLISECONDS
        );
        log.debug("启动预取工作线程，周期：{}ms", prefetchPeriod);
    }
    
    /**
     * 获取或创建链式分段ID缓存器
     *
     * @param name ID生成器名称
     * @return 链式分段ID缓存器
     */
    public SegmentChainBuffer getBuffer(String name) {
        return bufferMap.computeIfAbsent(name, this::createBuffer);
    }
    
    /**
     * 创建新的链式分段ID缓存器
     *
     * @param name ID生成器名称
     * @return 新创建的链式分段ID缓存器
     */
    private SegmentChainBuffer createBuffer(String name) {
        log.info("创建链式分段ID缓存器：{}", name);
        SegmentChainBuffer buffer = new SegmentChainBuffer(name);
        
        // 初始化缓存器
        initBuffer(buffer);
        
        return buffer;
    }
    
    /**
     * 初始化缓存器，加载第一个ID段
     *
     * @param buffer 缓存器
     */
    private void initBuffer(SegmentChainBuffer buffer) {
        if (buffer.isInitialized()) {
            return;
        }
        
        synchronized (buffer) {
            if (buffer.isInitialized()) {
                return;
            }
            
            // 获取第一个ID段
            IdSegment segment = segmentAllocator.nextSegment(buffer.getName());
            buffer.initialize(segment);
            
            log.info("初始化链式分段ID缓存器[{}]完成", buffer.getName());
        }
    }
    
    /**
     * 为指定缓存器预取下一个ID段
     *
     * @param buffer 缓存器
     */
    public void prefetchNextSegment(SegmentChainBuffer buffer) {
        if (!buffer.startPrefetching()) {
            // 已经有预取任务在执行
            return;
        }
        
        try {
            String name = buffer.getName();
            log.debug("为缓存器[{}]预取下一个ID段", name);
            
            // 获取下一个ID段
            IdSegment segment = segmentAllocator.nextSegment(name);
            
            // 添加到链尾
            boolean added = buffer.appendSegment(segment);
            if (added) {
                log.debug("为缓存器[{}]预取ID段成功：{}", name, segment);
            } else {
                log.debug("缓存器[{}]链长已达到最大值，不再添加预取的ID段", name);
            }
            
        } catch (Exception e) {
            log.error("预取ID段失败", e);
        } finally {
            buffer.endPrefetching();
        }
    }
    
    /**
     * 获取所有缓存器
     *
     * @return 缓存器映射
     */
    Map<String, SegmentChainBuffer> getBufferMap() {
        return bufferMap;
    }
    
    /**
     * 关闭管理器，释放资源
     */
    public void shutdown() {
        if (prefetchExecutor != null) {
            prefetchExecutor.shutdown();
        }
    }
} 