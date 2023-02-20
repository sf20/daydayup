package com.ryfast.common.utils.kline;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * 解析Excel文件读取数据
     *
     * @param fullFilePath      文件全路径地址（包含文件名）
     * @param sheetName         要读取的sheet
     * @param colNameAndTypeMap 要读取列的列名以及列数据对应类型的map集合
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> parse(String fullFilePath, String sheetName,
                                                  Map<String, Object> colNameAndTypeMap) throws IOException {
        List<Map<String, Object>> sheetDataList = new ArrayList<Map<String, Object>>();

        InputStream in = new FileInputStream(fullFilePath);
        XSSFWorkbook workBook = new XSSFWorkbook(in);
        XSSFSheet sheet = workBook.getSheet(sheetName);

        Map<String, Integer> colNameAndIndexMap = new HashMap<String, Integer>();
        XSSFRow colNameRow = sheet.getRow(0);
        short firstCellNum = colNameRow.getFirstCellNum();
        short lastCellNum = colNameRow.getLastCellNum();
        for (short i = firstCellNum; i < lastCellNum; i++) {
            XSSFCell colCell = colNameRow.getCell(i);
            if (colCell == null) {
                continue;
            }
            String colName = colCell.getStringCellValue();
            if (colNameAndTypeMap.containsKey(colName)) {
                colNameAndIndexMap.put(colName, colCell.getColumnIndex());
            }
        }

        int totalRowNum = sheet.getLastRowNum();
        for (int rowNum = 1; rowNum <= totalRowNum; rowNum++) {
            XSSFRow nextRow = sheet.getRow(rowNum);

            Map<String, Object> rowMap = new HashMap<String, Object>();
            for (Map.Entry<String, Object> colNameAndType : colNameAndTypeMap.entrySet()) {
                String colName = colNameAndType.getKey();
                String stringCellValue = nextRow.getCell(colNameAndIndexMap.get(colName)).getStringCellValue().replaceAll(",", "");

                Object colType = colNameAndType.getValue();
                if (colType instanceof String) {
                    rowMap.put(colName, stringCellValue);
                } else if (colType instanceof Integer) {
                    rowMap.put(colName, Integer.parseInt(stringCellValue));
                } else if (colType instanceof Long) {
                    rowMap.put(colName, Long.parseLong(stringCellValue));
                } else if (colType instanceof Float) {
                    rowMap.put(colName, Float.parseFloat(stringCellValue));
                }
            }

            sheetDataList.add(rowMap);
        }

        in.close();
        workBook.close();

        return sheetDataList;
    }
}
