package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用「名称-数值」结构，用于统计图表的饼图 / 柱状图数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVO implements Serializable {

    private String name;
    private Long value;
}
