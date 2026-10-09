package com.blog.service;

import com.blog.vo.StatisticsVO;

/**
 * 数据统计服务（后台面板）。
 */
public interface StatisticsService {

    /** 汇总各项统计数据 */
    StatisticsVO overview();
}
