package com.ryfast.project.biz.company.controller;

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
import com.ryfast.project.biz.company.domain.StockCompany;
import com.ryfast.project.biz.company.service.IStockCompanyService;
import com.ryfast.framework.web.controller.BaseController;
import com.ryfast.framework.web.domain.AjaxResult;
import com.ryfast.common.utils.poi.ExcelUtil;
import com.ryfast.framework.web.page.TableDataInfo;

/**
 * 公司Controller
 * 
 * @author ruoyi
 * @date 2023-02-02
 */
@Controller
@RequestMapping("/biz/company")
public class StockCompanyController extends BaseController
{
    private String prefix = "biz/company";

    @Autowired
    private IStockCompanyService stockCompanyService;

    @RequiresPermissions("biz:company:view")
    @GetMapping()
    public String company()
    {
        return prefix + "/company";
    }

    /**
     * 查询公司列表
     */
    @RequiresPermissions("biz:company:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StockCompany stockCompany)
    {
        startPage();
        List<StockCompany> list = stockCompanyService.selectStockCompanyList(stockCompany);
        return getDataTable(list);
    }

    /**
     * 导出公司列表
     */
    @RequiresPermissions("biz:company:export")
    @Log(title = "公司", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StockCompany stockCompany)
    {
        List<StockCompany> list = stockCompanyService.selectStockCompanyList(stockCompany);
        ExcelUtil<StockCompany> util = new ExcelUtil<StockCompany>(StockCompany.class);
        return util.exportExcel(list, "公司数据");
    }

    /**
     * 新增公司
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存公司
     */
    @RequiresPermissions("biz:company:add")
    @Log(title = "公司", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StockCompany stockCompany)
    {
        return toAjax(stockCompanyService.insertStockCompany(stockCompany));
    }

    /**
     * 修改公司
     */
    @RequiresPermissions("biz:company:edit")
    @GetMapping("/edit/{stockCode}")
    public String edit(@PathVariable("stockCode") String stockCode, ModelMap mmap)
    {
        StockCompany stockCompany = stockCompanyService.selectStockCompanyByStockCode(stockCode);
        mmap.put("stockCompany", stockCompany);
        return prefix + "/edit";
    }

    /**
     * 修改保存公司
     */
    @RequiresPermissions("biz:company:edit")
    @Log(title = "公司", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StockCompany stockCompany)
    {
        return toAjax(stockCompanyService.updateStockCompany(stockCompany));
    }

    /**
     * 删除公司
     */
    @RequiresPermissions("biz:company:remove")
    @Log(title = "公司", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(stockCompanyService.deleteStockCompanyByStockCodes(ids));
    }
}
