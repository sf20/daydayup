package com.ryfast.project.biz.kline.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ryfast.framework.aspectj.lang.annotation.Log;
import com.ryfast.framework.aspectj.lang.enums.BusinessType;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import com.ryfast.framework.web.controller.BaseController;
import com.ryfast.framework.web.domain.AjaxResult;
import com.ryfast.common.utils.poi.ExcelUtil;
import com.ryfast.framework.web.page.TableDataInfo;

/**
 * klineController
 * 
 * @author ruoyi
 * @date 2023-02-16
 */
@Controller
@RequestMapping("/biz/kline")
public class StockKlineDay18Controller extends BaseController
{
    private String prefix = "biz/kline";

    @Autowired
    private IStockKlineDay18Service stockKlineDay18Service;

    @RequiresPermissions("biz:kline:view")
    @GetMapping()
    public String kline()
    {
        return prefix + "/kline";
    }

    /**
     * 查询kline列表
     */
    @RequiresPermissions("biz:kline:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StockKlineDay18 stockKlineDay18)
    {
        startPage();
        List<StockKlineDay18> list = stockKlineDay18Service.selectStockKlineDay18List(stockKlineDay18);
        return getDataTable(list);
    }

    /**
     * 导出kline列表
     */
    @RequiresPermissions("biz:kline:export")
    @Log(title = "kline", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StockKlineDay18 stockKlineDay18)
    {
        List<StockKlineDay18> list = stockKlineDay18Service.selectStockKlineDay18List(stockKlineDay18);
        ExcelUtil<StockKlineDay18> util = new ExcelUtil<StockKlineDay18>(StockKlineDay18.class);
        return util.exportExcel(list, "kline数据");
    }

    /**
     * 新增kline
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存kline
     */
    @RequiresPermissions("biz:kline:add")
    @Log(title = "kline", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StockKlineDay18 stockKlineDay18)
    {
        return toAjax(stockKlineDay18Service.insertStockKlineDay18(stockKlineDay18));
    }

    /**
     * 修改kline
     */
    @RequiresPermissions("biz:kline:edit")
    @GetMapping("/edit/{stockCode}")
    public String edit(@PathVariable("stockCode") String stockCode, ModelMap mmap)
    {
        StockKlineDay18 stockKlineDay18 = stockKlineDay18Service.selectStockKlineDay18ByStockCode(stockCode);
        mmap.put("stockKlineDay18", stockKlineDay18);
        return prefix + "/edit";
    }

    /**
     * 修改保存kline
     */
    @RequiresPermissions("biz:kline:edit")
    @Log(title = "kline", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StockKlineDay18 stockKlineDay18)
    {
        return toAjax(stockKlineDay18Service.updateStockKlineDay18(stockKlineDay18));
    }

    /**
     * 删除kline
     */
    @RequiresPermissions("biz:kline:remove")
    @Log(title = "kline", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(stockKlineDay18Service.deleteStockKlineDay18ByStockCodes(ids));
    }
}
