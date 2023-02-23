package com.ryfast.project.biz.download.service;


import com.ryfast.common.constant.Consts;
import com.ryfast.common.utils.kline.DownloadUtil;
import com.ryfast.common.utils.kline.ExcelUtil;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DownloadKlineDataServiceSZA extends DownloadKlineDataService {
    private static final Logger log = LoggerFactory.getLogger(DownloadKlineDataServiceSZA.class);
    private static final String FILEPATH_DAYDATA = "D:\\Download\\Data\\22\\DATA\\kline\\SZ.A\\daydata\\";
    private static final String FILE_SUFFIX = ".xlsx";
    private static final String SHEET_NAME = "股票行情";
    private static final String[] COL_NAME = {"交易日期", "证券代码", "开盘", "最高", "最低", "今收", "涨跌幅（%）", "成交量(万股)"};
    private static final String URL1 = "http://www.szse.cn/api/report/ShowReport?SHOWTYPE=xlsx&CATALOGID=1815_stock_snapshot&TABKEY=tab1&txtBeginDate=";
    private static final String URL2 = "&txtEndDate=";
    private static final String URL3 = "&radioClass=00%2C20%2C30&txtSite=all&random=0.053655309153661124";
    private static final int MINIMUM_FILE_SIZE = 160000;

    @Resource
    private IStockKlineDay18Service stockKlineDay18Service;

    @Override
    protected Date getMaxTradingDate() {
        StockKlineDay18 queryParam = new StockKlineDay18();
        queryParam.setStockCode("0");
        return stockKlineDay18Service.selectLatestTradingDate(queryParam);
    }

    /**
     * 处理单日数据
     *
     * @param currentDate
     * @throws Exception
     */
    @Override
    protected void processDayData(Date currentDate) throws Exception {
        String currentDateStr = Consts.DATE_FORMAT.format(currentDate);
        log.info("处理单日数据[{}]开始。", currentDateStr);

        // 下载单日数据
        String dayDataFilePath = downloadDayData(currentDateStr);
        // 解析文件
        List<StockKlineDay18> dayDataListFromFile = parseDayData(currentDateStr, dayDataFilePath);
        // 查询数据库数据
        StockKlineDay18 queryParam = new StockKlineDay18();
        queryParam.setTradingDate(currentDate);
        List<StockKlineDay18> dayDataListFromDB = stockKlineDayService.selectStockKlineDay18List(queryParam);
        // 过滤掉SHA数据
        dayDataListFromDB = dayDataListFromDB.stream().filter(k -> !k.getStockCode().startsWith("6")).collect(Collectors.toList());
        // 对比更新
        compareWithDBAndUpdate(dayDataListFromFile, dayDataListFromDB);

        log.info("处理单日数据[{}]结束。", currentDateStr);
    }

    /**
     * 下载指定日期数据
     *
     * @param currentDateStr 指定日期（格式：yyyy-MM-dd）
     * @return
     * @throws IOException
     */
    private String downloadDayData(String currentDateStr) throws IOException {
        String filePath = FILEPATH_DAYDATA + currentDateStr + FILE_SUFFIX;
        File currentDateFile = new File(filePath);
        if (currentDateFile.exists() && currentDateFile.length() > MINIMUM_FILE_SIZE) {
            log.info("提示：数据文件[{}}]已下载。", filePath);
            return filePath;
        }

        String url = URL1 + currentDateStr + URL2 + currentDateStr + URL3;
        String fileName = currentDateStr + FILE_SUFFIX;
        DownloadUtil.downloadFromUrl(url, FILEPATH_DAYDATA, fileName);

        // message
        if (currentDateFile.exists() && currentDateFile.length() > MINIMUM_FILE_SIZE) {
            log.info("数据文件[{}}]下载成功！", filePath);
            return filePath;
        } else {
            throw new RuntimeException("数据文件[" + filePath + "]下载失败，请检查！");
        }
    }

    /**
     * 解析指定日期指定地址的文件
     *
     * @param currentDateStr
     * @param dayDataFilePath
     * @return
     * @throws Exception
     */
    private List<StockKlineDay18> parseDayData(String currentDateStr, String dayDataFilePath) throws Exception {
        Map<String, Object> colNameAndTypeMap = new HashMap<>();
        colNameAndTypeMap.put(COL_NAME[0], new String());
        colNameAndTypeMap.put(COL_NAME[1], new String());
        colNameAndTypeMap.put(COL_NAME[2], new String());
        colNameAndTypeMap.put(COL_NAME[3], new String());
        colNameAndTypeMap.put(COL_NAME[4], new String());
        colNameAndTypeMap.put(COL_NAME[5], new String());
        colNameAndTypeMap.put(COL_NAME[6], new String());
        colNameAndTypeMap.put(COL_NAME[7], new String());
        List<Map<String, Object>> mapList = ExcelUtil.parse(dayDataFilePath, SHEET_NAME, colNameAndTypeMap);

        List<StockKlineDay18> klineList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            String tradingDate = String.valueOf(map.get(COL_NAME[0]));
            if (!tradingDate.equals(currentDateStr)) {
                throw new RuntimeException("指定日期[" + currentDateStr + "]数据获取有误，请检查！");
            }

            StockKlineDay18 klineModel = new StockKlineDay18();
            klineModel.setStockCode(String.valueOf(map.get(COL_NAME[1])));
            klineModel.setTradingDate(Consts.DATE_FORMAT.parse(tradingDate));
            klineModel.setOpenPrice(new BigDecimal(String.valueOf(map.get(COL_NAME[2]))).setScale(2));
            klineModel.setHighPrice(new BigDecimal(String.valueOf(map.get(COL_NAME[3]))).setScale(2));
            klineModel.setLowPrice(new BigDecimal(String.valueOf(map.get(COL_NAME[4]))).setScale(2));
            klineModel.setClosePrice(new BigDecimal(String.valueOf(map.get(COL_NAME[5]))).setScale(2));
            klineModel.setVolume((long) (Float.parseFloat(String.valueOf(map.get(COL_NAME[7]))) * 10000));
            klineModel.setPriceRange(new BigDecimal(String.valueOf(map.get(COL_NAME[6]))).setScale(2));
            klineList.add(klineModel);
        }

        // TODO 过滤1开头的数据

        // 过滤掉code以2开头或者成交量为0的数据
        return klineList.stream().filter(k -> (!k.getStockCode().startsWith("2")) && k.getVolume() > 0).collect(Collectors.toList());
    }

    /**
     * 处理历史数据
     *
     * @param dateList
     * @throws Exception
     */
    @Override
    protected void processHistoryData(List<Date> dateList) throws Exception {
        log.info("处理历史数据开始。");
        for (Date dayDate : dateList) {
            processDayData(dayDate);
        }
        log.info("处理历史数据结束。");
    }
}
