package com.blog.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>前端分页组件（el-pagination）所需的 total / page / size 全部返回，
 * 避免前端二次计算。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页数据 */
    private List<T> records = new ArrayList<>();

    /** 总记录数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private int page;

    /** 每页条数 */
    private int size;

    /** 总页数（由 total / size 计算得出） */
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records == null ? new ArrayList<>() : records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
    }

    /**
     * 基于「全量列表」在内存中做分页，这是本工程 Mock 场景最常用的方式。
     *
     * @param all  已排好序的全量数据
     * @param page 页码，从 1 开始（小于 1 自动纠正为 1）
     * @param size 每页条数（小于 1 自动纠正为 10）
     */
    public static <T> PageResult<T> of(List<T> all, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        List<T> source = all == null ? List.of() : all;
        int from = Math.min((page - 1) * size, source.size());
        int to = Math.min(from + size, source.size());
        // 用新集合承载，避免返回视图后原集合被并发修改
        List<T> records = new ArrayList<>(source.subList(from, to));
        return new PageResult<>(records, source.size(), page, size);
    }
}
