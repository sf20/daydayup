package com.ryfast.project.biz.kline.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ryfast.project.biz.kline.mapper.StockKlineDay18Mapper;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import com.ryfast.common.utils.text.Convert;

/**
 * klineService业务层处理
 * 
 * @author ruoyi
 * @date 2023-02-16
 */
@Service
public class StockKlineDay18ServiceImpl implements IStockKlineDay18Service 
{
    @Autowired
    private StockKlineDay18Mapper stockKlineDay18Mapper;

    /**
     * 查询kline
     * 
     * @param stockCode kline主键
     * @return kline
     */
    @Override
    public StockKlineDay18 selectStockKlineDay18ByStockCode(String stockCode)
    {
        return stockKlineDay18Mapper.selectStockKlineDay18ByStockCode(stockCode);
    }

    /**
     * 查询kline列表
     * 
     * @param stockKlineDay18 kline
     * @return kline
     */
    @Override
    public List<StockKlineDay18> selectStockKlineDay18List(StockKlineDay18 stockKlineDay18)
    {
        return stockKlineDay18Mapper.selectStockKlineDay18List(stockKlineDay18);
    }

    /**
     * 查询kline列表
     *
     * @param stockKlineDay18 kline
     * @return kline
     */
    @Override
    public List<StockKlineDay18> selectStockKlineDay18ListLimit(StockKlineDay18 stockKlineDay18)
    {
        return stockKlineDay18Mapper.selectStockKlineDay18ListLimit(stockKlineDay18);
    }

    /**
     * 新增kline
     * 
     * @param stockKlineDay18 kline
     * @return 结果
     */
    @Override
    public int insertStockKlineDay18(StockKlineDay18 stockKlineDay18)
    {
        return stockKlineDay18Mapper.insertStockKlineDay18(stockKlineDay18);
    }

    /**
     * 修改kline
     * 
     * @param stockKlineDay18 kline
     * @return 结果
     */
    @Override
    public int updateStockKlineDay18(StockKlineDay18 stockKlineDay18)
    {
        return stockKlineDay18Mapper.updateStockKlineDay18(stockKlineDay18);
    }

    /**
     * 批量删除kline
     * 
     * @param stockCodes 需要删除的kline主键
     * @return 结果
     */
    @Override
    public int deleteStockKlineDay18ByStockCodes(String stockCodes)
    {
        return stockKlineDay18Mapper.deleteStockKlineDay18ByStockCodes(Convert.toStrArray(stockCodes));
    }

    /**
     * 删除kline信息
     * 
     * @param stockCode kline主键
     * @return 结果
     */
    @Override
    public int deleteStockKlineDay18ByStockCode(String stockCode)
    {
        return stockKlineDay18Mapper.deleteStockKlineDay18ByStockCode(stockCode);
    }

    @Override
    public Date selectLatestTradingDate(StockKlineDay18 stockKlineDay18) {
        return stockKlineDay18Mapper.selectLatestTradingDate(stockKlineDay18);
    }
}
