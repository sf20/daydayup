package com.ryfast.project.biz.kline.mapper;

import java.util.Date;
import java.util.List;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;

/**
 * klineMapper接口
 * 
 * @author ruoyi
 * @date 2023-02-16
 */
public interface StockKlineDay18Mapper 
{
    /**
     * 查询kline
     * 
     * @param stockCode kline主键
     * @return kline
     */
    public StockKlineDay18 selectStockKlineDay18ByStockCode(String stockCode);

    /**
     * 查询kline列表
     * 
     * @param stockKlineDay18 kline
     * @return kline集合
     */
    public List<StockKlineDay18> selectStockKlineDay18List(StockKlineDay18 stockKlineDay18);

    /**
     * 查询kline列表
     *
     * @param stockKlineDay18 kline
     * @return kline集合
     */
    List<StockKlineDay18> selectStockKlineDay18ListLimit(StockKlineDay18 stockKlineDay18);

    /**
     * 新增kline
     * 
     * @param stockKlineDay18 kline
     * @return 结果
     */
    public int insertStockKlineDay18(StockKlineDay18 stockKlineDay18);

    /**
     * 修改kline
     * 
     * @param stockKlineDay18 kline
     * @return 结果
     */
    public int updateStockKlineDay18(StockKlineDay18 stockKlineDay18);

    /**
     * 删除kline
     * 
     * @param stockCode kline主键
     * @return 结果
     */
    public int deleteStockKlineDay18ByStockCode(String stockCode);

    /**
     * 批量删除kline
     * 
     * @param stockCodes 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStockKlineDay18ByStockCodes(String[] stockCodes);

    Date selectLatestTradingDate(StockKlineDay18 stockKlineDay18);
}
