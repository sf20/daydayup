package com.ryfast.project.biz.download.service;

import com.ryfast.common.constant.Consts;
import com.ryfast.project.biz.company.domain.StockCompany;
import com.ryfast.project.biz.company.service.IStockCompanyService;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class DownloadKlineDataService {
    private static final Logger log = LoggerFactory.getLogger(DownloadKlineDataService.class);

    protected final String[] HOLIDAYS = {"2022-01-01", "2022-01-02", "2022-01-03", "2022-01-31", "2022-02-01", "2022-02-02", "2022-02-03", "2022-02-04", "2022-02-05", "2022-02-06", "2022-04-03", "2022-04-04", "2022-04-05", "2022-04-30", "2022-05-01", "2022-05-02", "2022-05-03", "2022-05-04", "2022-06-03", "2022-06-04", "2022-06-05", "2022-09-10", "2022-09-11", "2022-09-12", "2022-10-01", "2022-10-02", "2022-10-03", "2022-10-04", "2022-10-05", "2022-10-06", "2022-10-07",
            "2022-12-31", "2023-01-01", "2023-01-02", "2023-01-21", "2023-01-22", "2023-01-23", "2023-01-24", "2023-01-25", "2023-01-26", "2023-01-27", "2023-04-05", "2023-04-29", "2023-04-30", "2023-05-01", "2023-05-02", "2023-05-03", "2023-06-22", "2023-06-23", "2023-06-24", "2023-09-29", "2023-09-30", "2023-10-01", "2023-10-02", "2023-10-03", "2023-10-04", "2023-10-05", "2023-10-06"};
    @Resource
    protected IStockKlineDay18Service stockKlineDayService;
    @Resource
    protected IStockCompanyService stockCompanyService;

    /**
     * 每日更新主处理程序
     *
     * @throws Exception
     */
    public void doProcess() throws Exception {
        boolean useTodayDate = true;
        // 执行时间判断
        Calendar nowDate = Calendar.getInstance();
        // 执行时间<17:00 进行提示
        if (nowDate.get(Calendar.HOUR_OF_DAY) < 17) {
            nowDate.add(Calendar.DAY_OF_MONTH, -1);
            log.info("提示：当前时间在线数据还未生成，获取数据截止到前一天。");

            useTodayDate = false;
        }

        // 查询数据库最新更新日期 日期格式：yyyy-MM-dd 00:00:00
        Date startDate = getMaxTradingDate();
        // 执行时间进行日期格式化 统一日期格式为：yyyy-MM-dd 00:00:00
        Date endDate = Consts.DATE_FORMAT.parse(Consts.DATE_FORMAT.format(nowDate.getTime()));
        log.info("数据库最新更新日期: {}", Consts.DATE_FORMAT_FULL.format(startDate));
        log.info("当前格式化执行日期: {}", Consts.DATE_FORMAT_FULL.format(endDate));

        // 日期比较
        // 数据库最新更新日期>格式化后执行日期 报错
        if (startDate.after(endDate)) {
            throw new RuntimeException("日期比较错误，请检查！");
        }
        // 数据库最新更新日期=格式化后执行日期 提示数据已是最新状态。
        if (startDate.equals(endDate)) {
            log.info("提示：当前数据库数据已是最新状态。");
            return;
        }
        // 数据库最新更新日期<格式化后执行日期 程序继续执行
        // 未处理日期集合
        List<Date> notProcessedDateList = getTradingDaysFromDatePeriod(startDate, endDate);
        List<String> notProcessedDateStrList = notProcessedDateList.stream().map(d -> Consts.DATE_FORMAT.format(d)).collect(Collectors.toList());
        log.info("数据库数据未更新日期：" + notProcessedDateStrList);

        // 未处理天数
        int notProcessedDays = notProcessedDateList.size();
        if (notProcessedDays > 0) {
            if (notProcessedDays == 1 && useTodayDate) {
                // 执行单日数据处理
                processDayData(notProcessedDateList.get(0));
            } else {
                // 执行历史数据处理
                processHistoryData(notProcessedDateList);
            }
        }
    }

    private List<Date> getTradingDaysFromDatePeriod(Date startDate, Date endDate) {
        List<Date> tradingDays = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date loopDate = startDate;
        while (loopDate.before(endDate)) {
            calendar.setTime(loopDate);
            // 日期往后加一天
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            loopDate = calendar.getTime();
            if (isTradingDay(loopDate)) {
                tradingDays.add(loopDate);
            }
        }
        return tradingDays;
    }

    /**
     * 判断给定日期是否为交易日
     *
     * @param givenDate
     * @return
     */
    private boolean isTradingDay(Date givenDate) {
        // 判断日期是否是周六或者周日
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(givenDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false;
        }

        // 判断日期是否是假日
        String givenDateStr = Consts.DATE_FORMAT.format(givenDate);
        boolean isHoliday = Arrays.asList(HOLIDAYS).stream().anyMatch(d -> d.equals(givenDateStr));
        if (isHoliday) {
            return false;
        }

        return true;
    }

    /**
     * 将文件解析数据和数据库数据比较 进行新增或更新数据
     *
     * @param klineDayListFromFile 文件解析数据集合
     * @param klineDayListFromDB   数据库数据集合
     */
    protected void compareWithDBAndUpdate
    (List<StockKlineDay18> klineDayListFromFile, List<StockKlineDay18> klineDayListFromDB) {

        if (!klineDayListFromFile.equals(klineDayListFromDB)) {
            log.info("文件数据和数据库数据比对不一致，开始逐条进行比较。");

            for (StockKlineDay18 klineDayFromFile : klineDayListFromFile) {
                // 根据主键字段查询
                Predicate<StockKlineDay18> klineDayPredicate = klineDayFromDB -> klineDayFromDB.getStockCode().equals(klineDayFromFile.getStockCode())
                        && klineDayFromDB.getTradingDate().equals(klineDayFromFile.getTradingDate());
                boolean recordExists = klineDayListFromDB.stream().anyMatch(klineDayPredicate);
                if (recordExists) {
                    // 数据库查找到了
                    Optional<StockKlineDay18> klineDayOptional = klineDayListFromDB.stream().filter(klineDayPredicate).findFirst();
                    if (klineDayOptional.isPresent()) {
                        StockKlineDay18 dataFromDB = klineDayOptional.get();
                        if (!klineDayFromFile.equals(dataFromDB)) {
                            // 比对不一致 更新记录
                            stockKlineDayService.updateStockKlineDay18(klineDayFromFile);
                            log.info("数据库原记录：" + dataFromDB);
                            log.info("更新后新记录：" + klineDayFromFile);
                        }
                    }
                } else {
                    // 数据库查找不到 新增记录
                    stockKlineDayService.insertStockKlineDay18(klineDayFromFile);
                    log.info("新增记录：" + klineDayFromFile);
                }
            }
            log.info("文件数据和数据库数据比对不一致，逐条进行比较结束。");
        } else {
            log.info("提示：数据比对一致，无更新操作。");
        }

//        for (StockKlineDay18 stockKline : stockKlineDayList) {
//            LambdaQueryWrapper<StockKlineDay18> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(StockKlineDay18::getStockCode, stockKline.getStockCode()).eq(StockKlineDay18::getTradingDate, stockKline.getTradingDate());
//            // 根据主键查询一条记录
//            StockKlineDay18 dataFromDB = stockKlineDayService.getOne(queryWrapper);
//            if (dataFromDB != null) {
//                // 存在且比对不一致 更新记录
//                if (!dataFromDB.equals(stockKline)) {
//                    stockKlineDayService.update(stockKline, queryWrapper);
//                    log.info("数据库原记录：" + dataFromDB);
//                    log.info("更新后新记录：" + stockKline);
//                }
//            } else {
//                // 数据库查找不到 新增记录
//                stockKlineDayService.save(stockKline);
//                log.info("新增记录：" + stockKline);
//            }
//        }
    }

    /**
     * 更新给定日期后需计算的数据
     *
     * @throws Exception
     */
    public void doUpdate(Date startDate) {
        log.info("-------------doUpdate 开始-------------");
        Date latestTradingDate = stockKlineDayService.selectLatestTradingDate(null);
        List<Date> toUpdateDateList = getTradingDaysFromDatePeriod(startDate, latestTradingDate);
        List<StockCompany> companyList = stockCompanyService.selectStockCompanyList(null);
        for (StockCompany company : companyList) {
            StockKlineDay18 queryParam = new StockKlineDay18();
            queryParam.setStockCode(company.getStockCode());
            List<StockKlineDay18> companyKlineList = stockKlineDayService.selectStockKlineDay18List(queryParam);
            if (companyKlineList.size() > 0) {
                // 按日期倒序排序
                List<StockKlineDay18> sortedKlineList = companyKlineList.stream().sorted(Comparator.comparing(StockKlineDay18::getTradingDate).reversed()).collect(Collectors.toList());
                for (Date toUpdateDate : toUpdateDateList) {
                    // 查找指定日期前的所有数据
                    List<StockKlineDay18> beforeKlineList = sortedKlineList.stream().filter(s -> s.getTradingDate().compareTo(toUpdateDate) <= 0).collect(Collectors.toList());
                    // 至少有两天数据才更新
                    if (beforeKlineList.size() >= 2) {
                        BigDecimal closingPricePrevious = null;
                        Double ma5 = null, ma10 = null, ma20 = null, ma30 = null, ma60 = null, ma200 = null;
                        Double wr = null, ema12 = null, ema26 = null, diff = null, dea = null;

                        // 计算macd
                        List<StockKlineDay18> limit2List = beforeKlineList.stream().limit(2).collect(Collectors.toList());
                        StockKlineDay18 todayData = limit2List.get(0);
                        StockKlineDay18 yesterdayData = limit2List.get(1);
                        closingPricePrevious = yesterdayData.getClosePrice();
                        ema12 = (yesterdayData.getEma12() * 11 + todayData.getClosePrice().doubleValue() * 2) / 13;
                        ema26 = (yesterdayData.getEma26() * 25 + todayData.getClosePrice().doubleValue() * 2) / 27;
                        diff = ema12 - ema26;
                        dea = (yesterdayData.getDea() * 8 + diff * 2) / 10;
                        // 更新今日相关值（重要）
                        todayData.setEma12(ema12);
                        todayData.setEma26(ema26);
                        todayData.setDiff(diff);
                        todayData.setDea(dea);

                        // 计算ma
                        ma5 = calculateMaX(beforeKlineList, 5);
                        ma10 = calculateMaX(beforeKlineList, 10);
                        ma20 = calculateMaX(beforeKlineList, 20);
                        ma30 = calculateMaX(beforeKlineList, 30);
                        ma60 = calculateMaX(beforeKlineList, 60);
                        ma200 = calculateMaX(beforeKlineList, 200);

                        // 计算wr
                        int period10 = 10;
                        List<StockKlineDay18> limit10List = beforeKlineList.stream().limit(period10).collect(Collectors.toList());
                        if (limit10List.size() == period10) {
                            BigDecimal todayClosePrice = limit10List.get(0).getClosePrice();
                            BigDecimal maxHighPrice = limit10List.stream().map(StockKlineDay18::getHighPrice).max(BigDecimal::compareTo).get();
                            BigDecimal minLowPrice = limit10List.stream().map(StockKlineDay18::getLowPrice).min(BigDecimal::compareTo).get();
                            wr = (maxHighPrice.subtract(todayClosePrice)).multiply(BigDecimal.valueOf(100)).divide(maxHighPrice.subtract(minLowPrice), 2, RoundingMode.HALF_UP).doubleValue();
                        }

                        StockKlineDay18 toUpdateKline = new StockKlineDay18();
                        toUpdateKline.setStockCode(company.getStockCode());
                        toUpdateKline.setTradingDate(toUpdateDate);
                        toUpdateKline.setClosingPricePrevious(closingPricePrevious);
                        toUpdateKline.setMa5(ma5);
                        toUpdateKline.setMa10(ma10);
                        toUpdateKline.setMa20(ma20);
                        toUpdateKline.setMa30(ma30);
                        toUpdateKline.setMa60(ma60);
                        toUpdateKline.setMa200(ma200);
                        toUpdateKline.setWr(wr);
                        toUpdateKline.setEma12(ema12);
                        toUpdateKline.setEma26(ema26);
                        toUpdateKline.setDiff(diff);
                        toUpdateKline.setDea(dea);
                        stockKlineDayService.updateStockKlineDay18(toUpdateKline);
                        log.info("当前更新数据：" + toUpdateKline);
                    }
                }
            }
        }
        log.info("-------------doUpdate 结束-------------");
    }

    /**
     * 计算给定数据均值
     *
     * @param beforeKlineList
     * @param period      均值计算周期
     * @return
     */
    private Double calculateMaX(List<StockKlineDay18> beforeKlineList, int period) {
        Double maX = null;
        List<StockKlineDay18> limitPeriodList = beforeKlineList.stream().limit(period).collect(Collectors.toList());
        if (limitPeriodList.size() == period) {
            maX = limitPeriodList.stream().map(StockKlineDay18::getClosePrice).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP).doubleValue();
        }
        return maX;
    }

    public List<String> findV3(Date givenDate) {
        List<String> v3List = new ArrayList<>();
        List<StockCompany> companyList = stockCompanyService.selectStockCompanyList(null);
        for (StockCompany company : companyList) {
            String stockCode = company.getStockCode();
            StockKlineDay18 queryParam = new StockKlineDay18();
            queryParam.setStockCode(stockCode);
            List<StockKlineDay18> companyKlineList = stockKlineDayService.selectStockKlineDay18List(queryParam);
            if (companyKlineList.size() > 10) {
                List<StockKlineDay18> limit3List = companyKlineList.stream()
                        .filter(s -> s.getTradingDate().compareTo(givenDate) <= 0)
                        .sorted(Comparator.comparing(StockKlineDay18::getTradingDate).reversed())
                        .limit(3)
                        .collect(Collectors.toList());
                if (limit3List.size() == 3) {
                    StockKlineDay18 day1Kline = limit3List.get(2);
                    StockKlineDay18 day2Kline = limit3List.get(1);
                    StockKlineDay18 day3Kline = limit3List.get(0);
                    BigDecimal day1HighPrice = day1Kline.getHighPrice();
                    BigDecimal day1LowPrice = day1Kline.getLowPrice();
                    BigDecimal day2HighPrice = day2Kline.getHighPrice();
                    BigDecimal day2LowPrice = day2Kline.getLowPrice();
                    BigDecimal day3HighPrice = day3Kline.getHighPrice();
                    BigDecimal day3LowPrice = day3Kline.getLowPrice();
                    Double day1KlineWr = day1Kline.getWr();
                    Double day2KlineWr = day2Kline.getWr();
                    Double day3KlineWr = day3Kline.getWr();

                    if ((day1HighPrice.compareTo(day2HighPrice) > 0 && day1LowPrice.compareTo(day2LowPrice) > 0)
                            && (day3HighPrice.compareTo(day2HighPrice) > 0 && day3LowPrice.compareTo(day2LowPrice) >= 0)
                            && ((day1KlineWr > 90 || day2KlineWr > 90 || day3KlineWr > 90) && (day2KlineWr > day3KlineWr))) {
                        v3List.add(stockCode);
                    }
                }
            }
        }
        return v3List;
    }

    protected abstract Date getMaxTradingDate();

    protected abstract void processDayData(Date date) throws Exception;

    protected abstract void processHistoryData(List<Date> notProcessedDateList) throws Exception;

}
