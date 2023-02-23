package com.ryfast.project.biz.download.service;


import com.google.gson.Gson;
import com.ryfast.common.constant.Consts;
import com.ryfast.common.utils.StringUtils;
import com.ryfast.common.utils.kline.HttpClientUtil;
import com.ryfast.project.biz.company.domain.StockCompany;
import com.ryfast.project.biz.company.service.IStockCompanyService;
import com.ryfast.project.biz.download.domain.ResDataModel;
import com.ryfast.project.biz.download.domain.ResDayDataModel;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DownloadKlineDataServiceSHA extends DownloadKlineDataService {
    private static final Logger log = LoggerFactory.getLogger(DownloadKlineDataServiceSHA.class);
    private final String FILEPATH_BASE_DIRECTORY = "D:\\Download\\Data\\22\\DATA\\kline\\SH.A\\daydata\\";
    private final String FILEPATH_HISTORY = "D:\\Download\\Data\\22\\DATA\\kline\\SH.A\\history\\";
    private final String FILE_SUFFIX = ".txt";
    private final String[] CODE_ARRAY = {"600004", "600006"};
    @Resource
    private IStockCompanyService stockCompanyService;
    @Resource
    private IStockKlineDay18Service stockKlineDay18Service;

    @Override
    protected Date getMaxTradingDate() {
//        StockKlineDay18 queryParam = new StockKlineDay18();
//        queryParam.setStockCode("6");
//        return stockKlineDay18Service.selectLatestTradingDate(queryParam);
        Date date = null;
        try {
            date = Consts.DATE_FORMAT.parse("2023-02-20");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 处理单日数据
     *
     * @param currentDate
     * @throws Exception
     */
    @Override
    protected void processDayData(Date currentDate) throws Exception {
        String currentDateStr = Consts.DATE_FORMAT_YYYYMMDD.format(currentDate);
        log.info("处理单日数据[{}]开始。", currentDateStr);

        // 下载单日数据
        String dayDataFilePath = downloadDayData(currentDateStr);
        // 解析文件
        List<StockKlineDay18> dayDataListFromFile = parseDayData(currentDateStr, dayDataFilePath);
        // 查询数据库数据
        StockKlineDay18 queryParam = new StockKlineDay18();
        queryParam.setTradingDate(currentDate);
        List<StockKlineDay18> dayDataListFromDB = stockKlineDayService.selectStockKlineDay18List(queryParam);
        // 过滤掉SZA数据
        dayDataListFromDB = dayDataListFromDB.stream().filter(k -> k.getStockCode().startsWith("6")).collect(Collectors.toList());
        // 对比更新
        compareWithDBAndUpdate(dayDataListFromFile, dayDataListFromDB);

        log.info("处理单日数据[{}]结束。", currentDateStr);
    }

    /**
     * 下载指定日期数据
     *
     * @param currentDateStr
     * @return
     * @throws IOException
     */
    private String downloadDayData(String currentDateStr) throws IOException {
        String filePath = FILEPATH_BASE_DIRECTORY + currentDateStr + FILE_SUFFIX;
        File currentDateFile = new File(filePath);
        if (currentDateFile.exists()) {
            log.info("提示：数据文件[{}}]已下载。", filePath);
            return filePath;
        }

        String url = "http://yunhq.sse.com.cn:32041/v1/sh1/list/exchange/ashare?callback=jQuery1124024753230256038972_1629275223250&select=code%2Cname%2Copen%2Chigh%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2Ccpxxprodusta&order=&begin=0&end=2000&_=1629275223253";
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "yunhq.sse.com.cn:32041");
        headers.put("Connection", "keep-alive");
        headers.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        headers.put("Accept", "*/*");
        headers.put("Referer", "http://www.sse.com.cn/");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Cookie",
                "yfx_c_g_u_id_10000042=_ck21081814041818569039793965160; yfx_mr_f_10000042=%3A%3Amarket_type_free_search%3A%3A%3A%3Abaidu%3A%3A%3A%3A%3A%3A%3A%3Awww.baidu.com%3A%3A%3A%3Apmf_from_free_search; yfx_mr_10000042=%3A%3Amarket_type_free_search%3A%3A%3A%3Abaidu%3A%3A%3A%3A%3A%3A%3A%3Awww.baidu.com%3A%3A%3A%3Apmf_from_free_search; yfx_key_10000042=; VISITED_COMPANY_CODE=%5B%22600000%22%2C%22600004%22%5D; VISITED_STOCK_CODE=%5B%22600000%22%2C%22605580%22%2C%22600004%22%5D; seecookie=%5B600000%5D%3A%u6D66%u53D1%u94F6%u884C%2C%5B600004%5D%3A%u767D%u4E91%u673A%u573A; VISITED_MENU=%5B%228530%22%2C%228529%22%2C%229056%22%2C%229057%22%2C%229061%22%2C%229662%22%2C%228528%22%2C%229062%22%2C%229055%22%2C%228466%22%2C%228454%22%5D; yfx_f_l_v_t_10000042=f_t_1629266658846__r_t_1629266658846__v_t_1629275222297__r_c_0");
        headers.put("Cache-Control", "no-cache");
        String result = HttpClientUtil.doGet(url, null, headers);

        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(result);
        }

        // message
        if (currentDateFile.exists()) {
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
        Path path = Paths.get(dayDataFilePath);

        ResDayDataModel resData = null;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String data = reader.readLine();
            resData = parseDayDataJson(data);
            if (!currentDateStr.equals(resData.getDate())
                    || (resData.getEnd() - resData.getBegin() != resData.getList().size())) {
                throw new RuntimeException("指定日期[" + currentDateStr + "]数据获取有误，请检查！");
            }
        }

        List<StockKlineDay18> klineList = new ArrayList<>();
        String tradingDate = resData.getDate();
        for (String[] arr : resData.getList()) {
            StockKlineDay18 klineModel = new StockKlineDay18();
            klineModel.setStockCode(arr[0]);
            klineModel.setTradingDate(Consts.DATE_FORMAT_YYYYMMDD.parse(tradingDate));
            klineModel.setOpenPrice(new BigDecimal(arr[2]).setScale(2));
            klineModel.setHighPrice(new BigDecimal(arr[3]).setScale(2));
            klineModel.setLowPrice(new BigDecimal(arr[4]).setScale(2));
            klineModel.setClosePrice(new BigDecimal(arr[5]).setScale(2));
            klineModel.setVolume(Long.parseLong(arr[8]));
            klineModel.setPriceRange(new BigDecimal(arr[7]).setScale(2));
            klineList.add(klineModel);
        }

        // 过滤掉成交量为0的数据
        return klineList.stream().filter(k -> k.getVolume() > 0).collect(Collectors.toList());
    }

    /**
     * 解析单日数据JSON
     *
     * @param data
     * @return
     */
    private ResDayDataModel parseDayDataJson(String data) {
        ResDayDataModel resDayDataModel = null;
        if (StringUtils.isNotBlank(data)) {
            data = data.replace(")", "").replaceFirst(".+\\(", "");
            resDayDataModel = new Gson().fromJson(data, ResDayDataModel.class);
        }
        return resDayDataModel;
    }

    /**
     * 处理所有历史数据
     *
     * @throws Exception
     */
    public void processAllHistoryData() throws Exception {
        List<String> stockCodeList = null;
        if (CODE_ARRAY.length > 0) {
            stockCodeList = Arrays.asList(CODE_ARRAY);
        } else {
            // 查询所有SHA公司
            StockCompany queryParam = new StockCompany();
            queryParam.setStockCode("6");
            List<StockCompany> companyList = stockCompanyService.selectStockCompanyList(queryParam);
            stockCodeList = companyList.stream().map(StockCompany::getStockCode).collect(Collectors.toList());
        }

        // 下载历史数据天数
        int days = 10000;
        for (String stockCode : stockCodeList) {
            log.info("处理历史数据[{}]开始。", stockCode);

            // 下载公司指定天数历史数据
            String historyDataFilePath = downloadHistoryData(FILEPATH_HISTORY, stockCode, days);
            // 解析文件
            List<StockKlineDay18> historyDataListFromFile = parseHistoryData(stockCode, historyDataFilePath);

            // 查询数据库指定公司code数据
            StockKlineDay18 queryParam = new StockKlineDay18();
            queryParam.setStockCode(stockCode);
            List<StockKlineDay18> historyDataListFromDB = stockKlineDayService.selectStockKlineDay18List(queryParam);
            // 对比更新
            compareWithDBAndUpdate(historyDataListFromFile, historyDataListFromDB);

            log.info("处理历史数据[{}]结束。", stockCode);
        }
    }

    /**
     * 处理历史数据
     *
     * @param notProcessedDateList
     * @throws Exception
     */
    @Override
    protected void processHistoryData(List<Date> notProcessedDateList) throws Exception {
        Date startDate = notProcessedDateList.get(0);
        Date endDate = notProcessedDateList.get(notProcessedDateList.size() - 1);
        // 创建历史数据保存目录
        String startDateStr = Consts.DATE_FORMAT_YYYYMMDD.format(startDate);
        String endDateStr = Consts.DATE_FORMAT_YYYYMMDD.format(endDate);
        String historySaveDir = FILEPATH_BASE_DIRECTORY + startDateStr + Consts.CONNECTOR + endDateStr + File.separator;
        File historySaveDirFile = new File(historySaveDir);
        if (!historySaveDirFile.exists()) {
            historySaveDirFile.mkdir();
        }

        // 查询所有SHA公司
        StockCompany queryParam = new StockCompany();
        queryParam.setStockCode("6");
        List<StockCompany> companyList = stockCompanyService.selectStockCompanyList(queryParam);

        // 下载历史数据天数
        int days = notProcessedDateList.size() + 1;
        // 循环处理
        for (StockCompany stockCompany : companyList) {
            String stockCode = stockCompany.getStockCode();
            log.info("处理历史数据[{}]开始。", stockCode);
            // 下载历史数据
            String historyDataFilePath = downloadHistoryData(historySaveDir, stockCode, days);
            // 解析文件
            List<StockKlineDay18> historyDataListFromFile = parseHistoryData(stockCode, historyDataFilePath);
            // 过滤指定日期范围内数据
            historyDataListFromFile = historyDataListFromFile.stream().filter(k -> k.getTradingDate().compareTo(startDate) >= 0 && k.getTradingDate().compareTo(endDate) <= 0)
                    .collect(Collectors.toList());

            // 查询数据库数据
            StockKlineDay18 tempQueryParam = new StockKlineDay18();
            tempQueryParam.setStockCode(stockCode);
            List<StockKlineDay18> historyDataListFromDB = stockKlineDayService.selectStockKlineDay18List(tempQueryParam);
            // 对比更新
            compareWithDBAndUpdate(historyDataListFromFile, historyDataListFromDB);
            log.info("处理历史数据[{}]结束。", stockCode);
        }
    }

    /**
     * 现在指定公司指定天数历史数据，保存到指定目录
     *
     * @param historySaveDir 指定目录
     * @param stockCode      指定公司code
     * @param days           指定天数
     * @return
     * @throws Exception
     */
    private String downloadHistoryData(String historySaveDir, String stockCode, int days) throws Exception {
        String filePath = historySaveDir + stockCode + FILE_SUFFIX;
        if (new File(filePath).exists()) {
            log.info("提示：数据文件[" + filePath + "]已下载。");
            return filePath;
        }

        String url1 = "http://yunhq.sse.com.cn:32041/v1/sh1/dayk/";
        // end从-1开始
        int begin = -1 - days;
        String url2 = "?callback=jQuery112403337589365544511_1629359726620&select=date%2Copen%2Chigh%2Clow%2Cclose%2Cvolume%2Cchg_rate&begin=" + begin + "&end=-1&_=1629359726680";
        String union = url1 + stockCode + url2;
        String result = HttpClientUtil.doGet(union);

        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(result);
        }
        log.info("数据文件[" + filePath + "]下载成功.");

        Thread.sleep(100);

        return filePath;
    }

    /**
     * 解析指定公司指定地址的文件
     *
     * @param stockCode
     * @param historyDataFilePath
     * @return
     * @throws Exception
     */
    private List<StockKlineDay18> parseHistoryData(String stockCode, String historyDataFilePath) throws Exception {
        Path path = Paths.get(historyDataFilePath);

        ResDataModel resData = null;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String data = reader.readLine();
            resData = parseHistoryDataJson(data);
            if (!stockCode.equals(resData.getCode())
                    || (resData.getEnd() - resData.getBegin() != resData.getKline().size())) {
                throw new RuntimeException("指定公司[" + stockCode + "]数据获取有误，请检查！");
            }
        }

        List<StockKlineDay18> klineList = new ArrayList<>();
        for (String[] arr : resData.getKline()) {
            StockKlineDay18 klineModel = new StockKlineDay18();
            klineModel.setStockCode(stockCode);
            klineModel.setTradingDate(Consts.DATE_FORMAT_YYYYMMDD.parse(arr[0]));
            klineModel.setOpenPrice(new BigDecimal(arr[1]).setScale(2));
            klineModel.setHighPrice(new BigDecimal(arr[2]).setScale(2));
            klineModel.setLowPrice(new BigDecimal(arr[3]).setScale(2));
            klineModel.setClosePrice(new BigDecimal(arr[4]).setScale(2));
            klineModel.setVolume(Long.parseLong(arr[5]));
            klineModel.setPriceRange(new BigDecimal(arr[6]).setScale(2));
            klineList.add(klineModel);
        }

        // 过滤掉成交量为0的数据
        return klineList.stream().filter(k -> k.getVolume() > 0).collect(Collectors.toList());
    }

    /**
     * 解析历史数据JSON
     *
     * @param data
     * @return
     */
    private ResDataModel parseHistoryDataJson(String data) {
        ResDataModel resDataModel = null;
        if (StringUtils.isNotBlank(data)) {
            data = data.replace(")", "").replaceFirst(".+\\(", "");
            resDataModel = new Gson().fromJson(data, ResDataModel.class);
        }
        return resDataModel;
    }
}
