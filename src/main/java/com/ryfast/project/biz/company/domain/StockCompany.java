package com.ryfast.project.biz.company.domain;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ryfast.framework.aspectj.lang.annotation.Excel;
import com.ryfast.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 公司对象 stock_company
 * 
 * @author ruoyi
 * @date 2023-02-02
 */
public class StockCompany extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公司代码 */
    private String stockCode;

    /** 公司名 */
    @Excel(name = "公司名")
    private String shortName;

    /** 日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date listingDate;

    /** 公司全名 */
    @Excel(name = "公司全名")
    private String fullName;

    /** 市场 */
    @Excel(name = "市场")
    private String market;

    /** 行业 */
    @Excel(name = "行业")
    private String industry;

    public void setStockCode(String stockCode)
    {
        this.stockCode = stockCode;
    }

    public String getStockCode()
    {
        return stockCode;
    }
    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    public String getShortName()
    {
        return shortName;
    }
    public void setListingDate(Date listingDate)
    {
        this.listingDate = listingDate;
    }

    public Date getListingDate()
    {
        return listingDate;
    }
    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullName()
    {
        return fullName;
    }
    public void setMarket(String market)
    {
        this.market = market;
    }

    public String getMarket()
    {
        return market;
    }
    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getIndustry()
    {
        return industry;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("stockCode", getStockCode())
            .append("shortName", getShortName())
            .append("listingDate", getListingDate())
            .append("fullName", getFullName())
            .append("market", getMarket())
            .append("industry", getIndustry())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockCompany that = (StockCompany) o;
        return stockCode.equals(that.stockCode) && shortName.equals(that.shortName) && listingDate.equals(that.listingDate) && fullName.equals(that.fullName) && market.equals(that.market) && industry.equals(that.industry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode, shortName, listingDate, fullName, market, industry);
    }
}
