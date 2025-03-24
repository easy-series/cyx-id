package com.example.easyid.segment;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 链式号段ID生成器
 * 通过异步预取机制提高性能，避免传统号段模式的"两次获取"问题
 */
public class SegmentChainId implements SegmentId {
    
    /**
     * 默认的预取距离（安全距离）：当号段使用到达该比例时，触发异步获取下一个号段
     */
    private static final double DEFAULT_SAFE_DISTANCE = 0.7;
    
    /**
     * 号段链的头部（当前正在使用的号段）
     */
    private final AtomicReference<SegmentChainNode> head;
    
    /**
     * 号段链的尾部（最后一个预取的号段）
     */
    private final AtomicReference<SegmentChainNode> tail;
    
    /**
     * 号段分发器
     */
    private final SegmentIdDistributor distributor;
    
    /**
     * 安全距离比例
     */
    private final double safeDistance;
    
    /**
     * 预取任务执行器
     */
    private final ScheduledExecutorService prefetchExecutor;
    
    /**
     * 创建链式号段ID生成器
     *
     * @param distributor 号段分发器
     * @param safeDistance 安全距离比例(0.0-1.0)，当号段使用到达该比例时，触发异步获取下一个号段
     * @param prefetchExecutor 预取任务执行器
     */
    public SegmentChainId(SegmentIdDistributor distributor, double safeDistance, 
                         ScheduledExecutorService prefetchExecutor) {
        this.distributor = distributor;
        this.safeDistance = safeDistance;
        this.prefetchExecutor = prefetchExecutor;
        
        // 初始化头尾节点为空，延迟到首次使用时
        this.head = new AtomicReference<>(null);
        this.tail = new AtomicReference<>(null);
    }
    
    /**
     * 创建链式号段ID生成器，使用默认设置
     *
     * @param distributor 号段分发器
     */
    public SegmentChainId(SegmentIdDistributor distributor) {
        this(distributor, DEFAULT_SAFE_DISTANCE, 
            Executors.newScheduledThreadPool(1, new PrefetchThreadFactory(distributor.getName())));
    }
    
    @Override
    public long nextId() {
        SegmentChainNode current = head.get();
        
        // 如果头节点为空，则初始化链表
        if (current == null) {
            synchronized (this) {
                current = head.get();
                if (current == null) {
                    // 获取第一个号段
                    IdSegment segment = distributor.nextSegment();
                    current = new SegmentChainNode(segment);
                    
                    // 设置头尾节点
                    head.set(current);
                    tail.set(current);
                }
            }
        }
        
        try {
            // 尝试从当前节点获取ID
            long id = current.nextId();
            
            // 检查是否需要预取下一个号段
            if (current.isNeedPrefetch(safeDistance)) {
                prefetchNextSegment();
            }
            
            return id;
        } catch (IllegalStateException e) {
            // 当前号段已耗尽，尝试切换到下一个号段
            return tryNextSegment(current);
        }
    }
    
    /**
     * 尝试切换到下一个号段并获取ID
     *
     * @param current 当前节点
     * @return 下一个可用ID
     */
    private synchronized long tryNextSegment(SegmentChainNode current) {
        // 再次检查头节点，可能其他线程已经更新
        if (head.get() != current) {
            return nextId();
        }
        
        // 如果有下一个节点，则切换到下一个节点
        SegmentChainNode next = current.getNext();
        if (next != null) {
            head.set(next);
            return nextId();
        }
        
        // 如果没有预取的下一个节点，则同步获取
        IdSegment segment = distributor.nextSegment();
        next = new SegmentChainNode(segment);
        
        // 连接节点
        current.setNext(next);
        
        // 更新头尾节点
        head.set(next);
        tail.set(next);
        
        return next.nextId();
    }
    
    /**
     * 异步预取下一个号段
     */
    private void prefetchNextSegment() {
        SegmentChainNode currentTail = tail.get();
        
        // 如果尾节点已经有下一个节点，则不需要预取
        if (currentTail.getNext() != null) {
            return;
        }
        
        // 使用CompletableFuture异步预取下一个号段
        CompletableFuture.runAsync(() -> {
            synchronized (this) {
                // 再次检查，避免重复预取
                if (currentTail.getNext() != null || tail.get() != currentTail) {
                    return;
                }
                
                try {
                    // 获取下一个号段
                    IdSegment segment = distributor.nextSegment();
                    SegmentChainNode next = new SegmentChainNode(segment);
                    
                    // 连接节点
                    currentTail.setNext(next);
                    
                    // 更新尾节点
                    tail.set(next);
                } catch (Exception e) {
                    // 预取失败，记录日志（实际项目中应加入日志）
                    System.err.println("预取号段失败: " + e.getMessage());
                }
            }
        }, prefetchExecutor);
    }
    
    @Override
    public String getName() {
        return distributor.getName();
    }
    
    @Override
    public SegmentIdDistributor getDistributor() {
        return distributor;
    }
    
    @Override
    public IdSegment getCurrentSegment() {
        SegmentChainNode current = head.get();
        return current != null ? current.getSegment() : null;
    }
    
    /**
     * 关闭ID生成器，释放资源
     */
    public void shutdown() {
        if (prefetchExecutor != null && !prefetchExecutor.isShutdown()) {
            prefetchExecutor.shutdown();
        }
    }
    
    /**
     * 号段链节点
     * 包含一个号段和指向下一个节点的引用
     */
    private static class SegmentChainNode {
        private final IdSegment segment;
        private volatile SegmentChainNode next;
        private volatile boolean needPrefetch;
        
        public SegmentChainNode(IdSegment segment) {
            this.segment = segment;
            this.next = null;
            this.needPrefetch = false;
        }
        
        /**
         * 获取下一个ID
         *
         * @return 下一个ID
         */
        public long nextId() {
            return segment.nextId();
        }
        
        /**
         * 检查是否需要预取下一个号段
         *
         * @param safeDistance 安全距离比例
         * @return 是否需要预取
         */
        public boolean isNeedPrefetch(double safeDistance) {
            if (needPrefetch) {
                return false;
            }
            
            double usageRate = segment.getUsageRate();
            boolean result = usageRate >= safeDistance;
            
            if (result) {
                needPrefetch = true;
            }
            
            return result;
        }
        
        public IdSegment getSegment() {
            return segment;
        }
        
        public SegmentChainNode getNext() {
            return next;
        }
        
        public void setNext(SegmentChainNode next) {
            this.next = next;
        }
    }
    
    /**
     * 预取线程工厂
     */
    private static class PrefetchThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        public PrefetchThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "easy-id-prefetch-" + name + "-thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
} 