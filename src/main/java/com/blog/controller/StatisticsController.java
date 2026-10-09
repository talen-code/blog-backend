package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.common.Result;
import com.blog.service.StatisticsService;
import com.blog.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计接口（后台面板）。
 *
 * <pre>
 * GET /api/admin/statistics/overview   汇总统计（管理员）
 * </pre>
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @RequireAdmin
    public Result<StatisticsVO> overview() {
        return Result.success(statisticsService.overview());
    }
}
