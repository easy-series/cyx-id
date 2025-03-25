package com.caoyixin.cyxid.segment.buffer;

import com.caoyixin.cyxid.storage.api.IdSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分段ID缓存器
 * 用于存储和管理一个ID段的使用情况
 */
@Getter
public class SegmentBuffer {
    
    /**
     * ID段名称
     */
    private final String name;
    
    /**
     * 当前正在使用的ID段
     */
    private volatile IdSegment currentSegment;
    
    /**
     * 下一个预加载的ID段
     */
    private volatile IdSegment nextSegment;
    
    /**
     * 当前ID段的当前值
     */
    private final AtomicLong currentValue = new AtomicLong(0);
    
    /**
     * 是否处于下一个ID段的加载状态
     */
    private volatile boolean isLoadingNext = false;
    
    /**
     * 是否初始化完成
     */
    @Setter
    private volatile boolean initialized = false;
    
    /**
     * 最大步长
     */
    @Setter
    private volatile int step = 1000;
    
    /**
     * 安全阈值百分比，当消耗到此百分比时开始异步加载下一个ID段
     */
    @Setter
    private volatile int safeDistancePercent = 50;
    
    /**
     * 创建分段ID缓存器
     *
     * @param name ID段名称
     */
    public SegmentBuffer(String name) {
        this.name = name;
    }
    
    /**
     * 设置当前ID段
     *
     * @param currentSegment 当前ID段
     */
    public void setCurrentSegment(IdSegment currentSegment) {
        this.currentSegment = currentSegment;
    }
    
    /**
     * 设置是否正在加载下一个ID段
     *
     * @param isLoadingNext 是否正在加载
     */
    public void setIsLoadingNext(boolean isLoadingNext) {
        this.isLoadingNext = isLoadingNext;
    }
    
    /**
     * 获取下一个ID
     *
     * @return 下一个ID
     */
    public long nextId() {
        return currentValue.incrementAndGet();
    }
    
    /**
     * 获取ID段的起始值
     *
     * @return ID段的起始值
     */
    public long getCurrentSegmentMinId() {
        IdSegment segment = currentSegment;
        return segment == null ? 0 : segment.getMinId();
    }
    
    /**
     * 当前ID段的剩余可用数量
     *
     * @return 剩余可用数量
     */
    public long remainingIdsCount() {
        IdSegment segment = currentSegment;
        if (segment == null) {
            return 0;
        }
        return segment.getMaxId() - currentValue.get();
    }
    
    /**
     * 是否需要加载下一个ID段
     *
     * @return 是否需要加载
     */
    public boolean shouldLoadNext() {
        if (currentSegment == null) {
            return true;
        }
        
        long threshold = (long)(currentSegment.getStep() * (safeDistancePercent / 100.0));
        return !isLoadingNext && remainingIdsCount() < threshold;
    }
    
    /**
     * 是否耗尽当前ID段
     *
     * @return 是否耗尽
     */
    public boolean isExhausted() {
        IdSegment segment = currentSegment;
        if (segment == null) {
            return true;
        }
        return currentValue.get() > segment.getMaxId();
    }
    
    /**
     * 切换到下一个ID段
     */
    public synchronized void switchToNextSegment() {
        if (nextSegment == null) {
            return;
        }
        
        currentSegment = nextSegment;
        currentValue.set(currentSegment.getMinId());
        nextSegment = null;
        isLoadingNext = false;
    }
    
    /**
     * 内部设置下一个ID段，由加载器调用
     *
     * @param nextSegment 下一个ID段
     */
    public synchronized void setNextSegment(IdSegment nextSegment) {
        this.nextSegment = nextSegment;
        this.isLoadingNext = false;
    }
    
    /**
     * 开始加载下一个ID段，设置加载状态
     *
     * @return 如果已经在加载中，返回false；否则设置加载状态并返回true
     */
    public synchronized boolean startLoadingNextSegment() {
        if (isLoadingNext) {
            return false;
        }
        isLoadingNext = true;
        return true;
    }
} 