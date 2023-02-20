package com.ryfast.project.biz.download.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ryfast.common.constant.Consts;
import com.ryfast.common.utils.kline.DownloadUtil;
import com.ryfast.common.utils.kline.ExcelUtil;
import com.ryfast.common.utils.kline.HttpClientUtil;
import com.ryfast.project.biz.company.domain.StockCompany;
import com.ryfast.project.biz.company.service.IStockCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class DownloadCompanyService {
    private static final Logger log = LoggerFactory.getLogger(DownloadCompanyService.class);
    private static final String FILEPATH = "D:\\Download\\Data\\22\\DATA\\company\\SZ.A\\";
    private static final String FILE_SUFFIX = ".xlsx";
    private static final String SHEET_NAME = "A股列表";
    private static final String[] COL_NAME = {"A股代码", "A股简称", "A股上市日期", "公司全称", "所属行业"};

    @Resource
    IStockCompanyService stockCompanyService;

    public void updateCompanySZA() throws Exception {
        log.info("-----update SZA company start-----");
        List<StockCompany> dataListFromFile = getCompanyFromFileSZA();
        List<StockCompany> dataListFromDB = getDataFromDB();

        for (StockCompany cmpFromFile : dataListFromFile) {
            if (!dataListFromDB.contains(cmpFromFile)) {
                String cmpCode = cmpFromFile.getStockCode();
                StockCompany cmpFromDB = stockCompanyService.selectStockCompanyByStockCode(cmpCode);
                if (cmpFromDB == null) {
                    // new company
//                    cmpFromFile.setCreateTime(new Date());
//                    cmpFromFile.setUpdateTime(new Date());
                    stockCompanyService.insertStockCompany(cmpFromFile);
                    log.info("insert SZA company: " + cmpFromFile);
                } else {
                    // company info changed
//                    cmpFromFile.setUpdateTime(new Date());
                    stockCompanyService.updateStockCompany(cmpFromFile);
                    log.info("update SZA company before: " + cmpFromDB);
                    log.info("update SZA company after: " + cmpFromFile);
                }
            }
        }
        log.info("-----update SZA company done-----");
    }

    private List<StockCompany> getCompanyFromFileSZA() throws Exception {
        String fullFilePath = FILEPATH + Consts.DATE_FORMAT.format(new Date()) + FILE_SUFFIX;
        File excelFile = new File(fullFilePath);
        if (!excelFile.exists()) {
            downloadCompanySZA();
            log.info("download SZA company finish.");
        }

        Map<String, Object> colNameAndTypeMap = new HashMap<String, Object>();
        colNameAndTypeMap.put(COL_NAME[0], new String());
        colNameAndTypeMap.put(COL_NAME[1], new String());
        colNameAndTypeMap.put(COL_NAME[2], new String());
        colNameAndTypeMap.put(COL_NAME[3], new String());
        colNameAndTypeMap.put(COL_NAME[4], new String());

        List<StockCompany> companyList = new ArrayList<StockCompany>();
        List<Map<String, Object>> mapList = ExcelUtil.parse(fullFilePath, SHEET_NAME, colNameAndTypeMap);
        for (Map<String, Object> map : mapList) {
            StockCompany company = new StockCompany();
            company.setStockCode(String.valueOf(map.get(COL_NAME[0])));
            company.setShortName(String.valueOf(map.get(COL_NAME[1])));
            company.setListingDate(Consts.DATE_FORMAT.parse(String.valueOf(map.get(COL_NAME[2]))));
            company.setFullName(String.valueOf(map.get(COL_NAME[3])));
            company.setIndustry(String.valueOf(map.get(COL_NAME[4])));
            String code = company.getStockCode();
            company.setMarket(String.valueOf(code.charAt(0)));

            companyList.add(company);
        }
        log.info("download SZA company size: " + companyList.size());
        return companyList;
    }

    private void downloadCompanySZA() throws IOException {
        String url = "http://www.szse.cn/api/report/ShowReport?SHOWTYPE=xlsx&CATALOGID=1110&TABKEY=tab1&random=0.1388435131806085";
        String currentDate = Consts.DATE_FORMAT.format(new Date());
        String fileName = currentDate + FILE_SUFFIX;
        DownloadUtil.downloadFromUrl(url, FILEPATH, fileName);
    }

    public void updateCompanySHA() throws Exception {
        log.info("-----update SHA company start-----");
        List<StockCompany> dataListOnline = downloadBasicInfoSHA();
        List<StockCompany> dataListFromDB = getDataFromDB();

        log.info("update SHA company...");
        for (StockCompany cmpOnline : dataListOnline) {
            String cmpCode = cmpOnline.getStockCode();
            JsonObject fullInfoJson = getCompanyFullInfoSHA(cmpCode);
            cmpOnline.setMarket("6");
            cmpOnline.setFullName(fullInfoJson.get("FULLNAME").getAsString());
            cmpOnline.setIndustry(
                    fullInfoJson.get("SSE_CODE_DESC").getAsString() + "/" + fullInfoJson.get("CSRC_CODE_DESC").getAsString() + "/"
                            + fullInfoJson.get("CSRC_GREAT_CODE_DESC").getAsString());

            if (!dataListFromDB.contains(cmpOnline)) {
                StockCompany cmpFromDB = stockCompanyService.selectStockCompanyByStockCode(cmpCode);
                if (cmpFromDB == null) {
                    // new company
//                    cmpOnline.setCreateTime(new Date());
//                    cmpOnline.setUpdateTime(new Date());
                    stockCompanyService.insertStockCompany(cmpOnline);
                    log.info("insert SHA company: " + cmpOnline);
                } else {
                    // company info changed
//                    cmpOnline.setUpdateTime(new Date());
                    stockCompanyService.updateStockCompany(cmpOnline);
                    log.info("update SHA company before: " + cmpFromDB);
                    log.info("update SHA company after: " + cmpOnline);
                }
            }
        }
        log.info("-----update SHA company done-----");
    }

    private List<StockCompany> getDataFromDB() {
        List<StockCompany> list = stockCompanyService.selectStockCompanyList(null);
        log.info("DB company size: " + list.size());
        return list;
    }

    private List<StockCompany> downloadBasicInfoSHA() throws Exception {
        List<StockCompany> list = new ArrayList<>();

        String url = "http://query.sse.com.cn/security/stock/downloadStockListFile.do?csrcCode=&stockCode=&areaName=&stockType=1";
        Map<String, String> params = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "http://www.sse.com.cn/");
        String result = HttpClientUtil.doGet(url, params, headers);

        String[] strArr = result.split("\n");
        // skip line 1
        for (int i = 1; i < strArr.length; i++) {
            String[] lineArr = strArr[i].split("	  ");
            StockCompany cmp = new StockCompany();
            cmp.setStockCode(lineArr[0]);
            cmp.setShortName(lineArr[1]);
            cmp.setListingDate(Consts.DATE_FORMAT.parse(lineArr[4]));
            list.add(cmp);
        }
        log.info("download SHA company size: " + list.size());
        return list;
    }

    private JsonObject getCompanyFullInfoSHA(String code) throws Exception {
        String url = "http://query.sse.com.cn/commonQuery.do?jsonCallBack=jsonpCallback51286&isPagination=false&sqlId=COMMON_SSE_ZQPZ_GP_GPLB_C&productid="
                + code + "&_=1636611399813";
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "http://www.sse.com.cn/");
        String result = HttpClientUtil.doGet(url, null, headers);
        result = result.replace("jsonpCallback51286(", "").replaceAll("\\)$", "");

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("result");

        Thread.sleep(100);

        return jsonArray.get(0).getAsJsonObject();
    }

    public void test() {
        log.info("测试ok");
    }
}

