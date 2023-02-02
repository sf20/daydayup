package com.ryfast.project.biz.company.service;

import java.util.List;
import com.ryfast.project.biz.company.domain.StockCompany;

/**
 * 公司Service接口
 * 
 * @author ruoyi
 * @date 2023-02-02
 */
public interface IStockCompanyService 
{
    /**
     * 查询公司
     * 
     * @param stockCode 公司主键
     * @return 公司
     */
    public StockCompany selectStockCompanyByStockCode(String stockCode);

    /**
     * 查询公司列表
     * 
     * @param stockCompany 公司
     * @return 公司集合
     */
    public List<StockCompany> selectStockCompanyList(StockCompany stockCompany);

    /**
     * 新增公司
     * 
     * @param stockCompany 公司
     * @return 结果
     */
    public int insertStockCompany(StockCompany stockCompany);

    /**
     * 修改公司
     * 
     * @param stockCompany 公司
     * @return 结果
     */
    public int updateStockCompany(StockCompany stockCompany);

    /**
     * 批量删除公司
     * 
     * @param stockCodes 需要删除的公司主键集合
     * @return 结果
     */
    public int deleteStockCompanyByStockCodes(String stockCodes);

    /**
     * 删除公司信息
     * 
     * @param stockCode 公司主键
     * @return 结果
     */
    public int deleteStockCompanyByStockCode(String stockCode);
}
