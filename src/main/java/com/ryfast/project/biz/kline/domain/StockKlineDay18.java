package com.ryfast.project.biz.kline.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ryfast.framework.aspectj.lang.annotation.Excel;
import com.ryfast.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * kline对象 stock_kline_day_18
 * 
 * @author ruoyi
 * @date 2023-02-16
 */
public class StockKlineDay18 extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 代码 */
    @Excel(name = "代码")
    private String stockCode;

    /** 日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date tradingDate;

    /** 开盘 */
    @Excel(name = "开盘")
    private BigDecimal openPrice;

    /** 最高 */
    @Excel(name = "最高")
    private BigDecimal highPrice;

    /** 最低 */
    @Excel(name = "最低")
    private BigDecimal lowPrice;

    /** 收盘 */
    @Excel(name = "收盘")
    private BigDecimal closePrice;

    /** 成交量 */
    @Excel(name = "成交量")
    private Long volume;

    /** 昨收 */
    @Excel(name = "昨收")
    private BigDecimal closingPricePrevious;

    /** 涨幅 */
    @Excel(name = "涨幅")
    private BigDecimal priceRange;

    /** ma5 */
    @Excel(name = "ma5")
    private Double ma5;

    /** ma10 */
    @Excel(name = "ma10")
    private Double ma10;

    /** ma20 */
    @Excel(name = "ma20")
    private Double ma20;

    /** ma30 */
    @Excel(name = "ma30")
    private Double ma30;

    /** ma60 */
    @Excel(name = "ma60")
    private Double ma60;

    /** ma200 */
    @Excel(name = "ma200")
    private Double ma200;

    /** wr */
    @Excel(name = "wr")
    private Double wr;

    /** ema12 */
    @Excel(name = "ema12")
    private Double ema12;

    /** ema26 */
    @Excel(name = "ema26")
    private Double ema26;

    /** diff */
    @Excel(name = "diff")
    private Double diff;

    /** dea */
    @Excel(name = "dea")
    private Double dea;

    public void setStockCode(String stockCode)
    {
        this.stockCode = stockCode;
    }

    public String getStockCode()
    {
        return stockCode;
    }
    public void setTradingDate(Date tradingDate)
    {
        this.tradingDate = tradingDate;
    }

    public Date getTradingDate()
    {
        return tradingDate;
    }
    public void setOpenPrice(BigDecimal openPrice)
    {
        this.openPrice = openPrice;
    }

    public BigDecimal getOpenPrice()
    {
        return openPrice;
    }
    public void setHighPrice(BigDecimal highPrice)
    {
        this.highPrice = highPrice;
    }

    public BigDecimal getHighPrice()
    {
        return highPrice;
    }
    public void setLowPrice(BigDecimal lowPrice)
    {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getLowPrice()
    {
        return lowPrice;
    }
    public void setClosePrice(BigDecimal closePrice)
    {
        this.closePrice = closePrice;
    }

    public BigDecimal getClosePrice()
    {
        return closePrice;
    }
    public void setVolume(Long volume)
    {
        this.volume = volume;
    }

    public Long getVolume()
    {
        return volume;
    }
    public void setClosingPricePrevious(BigDecimal closingPricePrevious)
    {
        this.closingPricePrevious = closingPricePrevious;
    }

    public BigDecimal getClosingPricePrevious()
    {
        return closingPricePrevious;
    }
    public void setPriceRange(BigDecimal priceRange)
    {
        this.priceRange = priceRange;
    }

    public BigDecimal getPriceRange()
    {
        return priceRange;
    }
    public void setMa5(Double ma5)
    {
        this.ma5 = ma5;
    }

    public Double getMa5()
    {
        return ma5;
    }
    public void setMa10(Double ma10)
    {
        this.ma10 = ma10;
    }

    public Double getMa10()
    {
        return ma10;
    }
    public void setMa20(Double ma20)
    {
        this.ma20 = ma20;
    }

    public Double getMa20()
    {
        return ma20;
    }
    public void setMa30(Double ma30)
    {
        this.ma30 = ma30;
    }

    public Double getMa30()
    {
        return ma30;
    }
    public void setMa60(Double ma60)
    {
        this.ma60 = ma60;
    }

    public Double getMa60()
    {
        return ma60;
    }
    public void setMa200(Double ma200)
    {
        this.ma200 = ma200;
    }

    public Double getMa200()
    {
        return ma200;
    }
    public void setWr(Double wr)
    {
        this.wr = wr;
    }

    public Double getWr()
    {
        return wr;
    }
    public void setEma12(Double ema12)
    {
        this.ema12 = ema12;
    }

    public Double getEma12()
    {
        return ema12;
    }
    public void setEma26(Double ema26)
    {
        this.ema26 = ema26;
    }

    public Double getEma26()
    {
        return ema26;
    }
    public void setDiff(Double diff)
    {
        this.diff = diff;
    }

    public Double getDiff()
    {
        return diff;
    }
    public void setDea(Double dea)
    {
        this.dea = dea;
    }

    public Double getDea()
    {
        return dea;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("stockCode", getStockCode())
            .append("tradingDate", getTradingDate())
            .append("openPrice", getOpenPrice())
            .append("highPrice", getHighPrice())
            .append("lowPrice", getLowPrice())
            .append("closePrice", getClosePrice())
            .append("volume", getVolume())
            .append("closingPricePrevious", getClosingPricePrevious())
            .append("priceRange", getPriceRange())
            .append("ma5", getMa5())
            .append("ma10", getMa10())
            .append("ma20", getMa20())
            .append("ma30", getMa30())
            .append("ma60", getMa60())
            .append("ma200", getMa200())
            .append("wr", getWr())
            .append("ema12", getEma12())
            .append("ema26", getEma26())
            .append("diff", getDiff())
            .append("dea", getDea())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockKlineDay18 that = (StockKlineDay18) o;
        return stockCode.equals(that.stockCode) && tradingDate.equals(that.tradingDate) && openPrice.equals(that.openPrice) && highPrice.equals(that.highPrice) && lowPrice.equals(that.lowPrice) && closePrice.equals(that.closePrice) && volume.equals(that.volume) && priceRange.equals(that.priceRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode, tradingDate, openPrice, highPrice, lowPrice, closePrice, volume, priceRange);
    }
}
