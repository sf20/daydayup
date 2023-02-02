package com.ryfast.project.biz.company.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ryfast.project.biz.company.mapper.StockCompanyMapper;
import com.ryfast.project.biz.company.domain.StockCompany;
import com.ryfast.project.biz.company.service.IStockCompanyService;
import com.ryfast.common.utils.text.Convert;

/**
 * 公司Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-02-02
 */
@Service
public class StockCompanyServiceImpl implements IStockCompanyService 
{
    @Autowired
    private StockCompanyMapper stockCompanyMapper;

    /**
     * 查询公司
     * 
     * @param stockCode 公司主键
     * @return 公司
     */
    @Override
    public StockCompany selectStockCompanyByStockCode(String stockCode)
    {
        return stockCompanyMapper.selectStockCompanyByStockCode(stockCode);
    }

    /**
     * 查询公司列表
     * 
     * @param stockCompany 公司
     * @return 公司
     */
    @Override
    public List<StockCompany> selectStockCompanyList(StockCompany stockCompany)
    {
        return stockCompanyMapper.selectStockCompanyList(stockCompany);
    }

    /**
     * 新增公司
     * 
     * @param stockCompany 公司
     * @return 结果
     */
    @Override
    public int insertStockCompany(StockCompany stockCompany)
    {
        return stockCompanyMapper.insertStockCompany(stockCompany);
    }

    /**
     * 修改公司
     * 
     * @param stockCompany 公司
     * @return 结果
     */
    @Override
    public int updateStockCompany(StockCompany stockCompany)
    {
        return stockCompanyMapper.updateStockCompany(stockCompany);
    }

    /**
     * 批量删除公司
     * 
     * @param stockCodes 需要删除的公司主键
     * @return 结果
     */
    @Override
    public int deleteStockCompanyByStockCodes(String stockCodes)
    {
        return stockCompanyMapper.deleteStockCompanyByStockCodes(Convert.toStrArray(stockCodes));
    }

    /**
     * 删除公司信息
     * 
     * @param stockCode 公司主键
     * @return 结果
     */
    @Override
    public int deleteStockCompanyByStockCode(String stockCode)
    {
        return stockCompanyMapper.deleteStockCompanyByStockCode(stockCode);
    }
}
