package com.ryfast;

import com.ryfast.common.constant.Consts;
import com.ryfast.project.biz.download.service.DownloadCompanyService;
import com.ryfast.project.biz.download.service.DownloadKlineDataService;
import com.ryfast.project.biz.download.service.DownloadKlineDataServiceSHA;
import com.ryfast.project.biz.download.service.DownloadKlineDataServiceSZA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RyfastApplication {
    private static final Logger log = LoggerFactory.getLogger(RyfastApplication.class);

    public static void main(String[] args) throws Exception {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RyfastApplication.class, args);

//        DownloadCompanyService downloadCompanyService = applicationContext.getBean(DownloadCompanyService.class);
//        downloadCompanyService.updateCompanySHA();
//        downloadCompanyService.updateCompanySZA();

//        DownloadKlineDataServiceSHA serviceSHA = applicationContext.getBean(DownloadKlineDataServiceSHA.class);
//        log.info("----------SHA Kline 数据处理开始。----------");
//        serviceSHA.doProcess();
//        log.info("----------SHA Kline 数据处理结束。----------");
//        DownloadKlineDataServiceSZA serviceSZA = applicationContext.getBean(DownloadKlineDataServiceSZA.class);
//        log.info("----------SZA Kline 数据处理开始。----------");
//        serviceSZA.doProcess();
//        log.info("----------SZA Kline 数据处理结束。----------");
//        // 数据库已更新ma值的最大日期
//        serviceSHA.doUpdate(Consts.DATE_FORMAT.parse("2023-02-24"));

        DownloadKlineDataServiceSHA serviceSHA = applicationContext.getBean(DownloadKlineDataServiceSHA.class);
        List<String> v3List = serviceSHA.findV3(Consts.DATE_FORMAT.parse("2023-02-27"));
        v3List.stream().filter(k -> !k.startsWith("3")).forEach(System.out::println);

    }
}