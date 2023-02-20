package com.ryfast.project.biz.download.service;

import com.ryfast.common.constant.Consts;
import com.ryfast.project.biz.kline.domain.StockKlineDay18;
import com.ryfast.project.biz.kline.service.IStockKlineDay18Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class DownloadKlineDataService {
    private static final Logger log = LoggerFactory.getLogger(DownloadKlineDataService.class);

    protected final String[] HOLIDAYS = {"2022-01-01", "2022-01-02", "2022-01-03", "2022-01-31", "2022-02-01", "2022-02-02", "2022-02-03", "2022-02-04", "2022-02-05", "2022-02-06", "2022-04-03", "2022-04-04", "2022-04-05", "2022-04-30", "2022-05-01", "2022-05-02", "2022-05-03", "2022-05-04", "2022-06-03", "2022-06-04", "2022-06-05", "2022-09-10", "2022-09-11", "2022-09-12", "2022-10-01", "2022-10-02", "2022-10-03", "2022-10-04", "2022-10-05", "2022-10-06", "2022-10-07",
            "2022-12-31", "2023-01-01", "2023-01-02", "2023-01-21", "2023-01-22", "2023-01-23", "2023-01-24", "2023-01-25", "2023-01-26", "2023-01-27", "2023-04-05", "2023-04-29", "2023-04-30", "2023-05-01", "2023-05-02", "2023-05-03", "2023-06-22", "2023-06-23", "2023-06-24", "2023-09-29", "2023-09-30", "2023-10-01", "2023-10-02", "2023-10-03", "2023-10-04", "2023-10-05", "2023-10-06"};
    @Resource
    protected IStockKlineDay18Service stockKlineDayService;

    /**
     * 每日更新主处理程序
     *
     * @throws Exception
     */
    public void doProcess() throws Exception {
        // 执行时间判断
        Calendar nowDate = Calendar.getInstance();
        // 执行时间<17:00 进行提示
        if (nowDate.get(Calendar.HOUR_OF_DAY) < 17) {
            nowDate.add(Calendar.DAY_OF_MONTH, -1);
            log.info("提示：当前时间在线数据还未生成，获取数据截止到前一天。");
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
        List<Date> notProcessedDateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date loopDate = startDate;
        while (loopDate.before(endDate)) {
            calendar.setTime(loopDate);
            // 日期往后加一天
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            loopDate = calendar.getTime();
            if (isTradingDay(loopDate)) {
                notProcessedDateList.add(loopDate);
            }
        }

        List<String> notProcessedDateStrList = notProcessedDateList.stream().map(d -> Consts.DATE_FORMAT.format(d)).collect(Collectors.toList());
        log.info("数据库数据未更新日期：" + notProcessedDateStrList);

        // 未处理天数
        int notProcessedDays = notProcessedDateList.size();
        if (notProcessedDays == 1) {
            // 执行单日数据处理
            processDayData(notProcessedDateList.get(0));
        } else if (notProcessedDays > 1) {
            // 执行历史数据处理
            processHistoryData(notProcessedDateList);
        }
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
//                            klineDayFromFile.setUpdateTime(new Date());
                            stockKlineDayService.updateStockKlineDay18(klineDayFromFile);
                            log.info("数据库原记录：" + dataFromDB);
                            log.info("更新后新记录：" + klineDayFromFile);
                        }
                    }
                } else {
                    // 数据库查找不到 新增记录
//                    klineDayFromFile.setCreateTime(new Date());
//                    klineDayFromFile.setUpdateTime(new Date());
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

    protected abstract Date getMaxTradingDate();

    protected abstract void processDayData(Date date) throws Exception;

    protected abstract void processHistoryData(List<Date> notProcessedDateList) throws Exception;

}
