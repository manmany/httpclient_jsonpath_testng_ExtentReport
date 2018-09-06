package com.man.api.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * 使用POI批量导入Excel数据 - POI提供API给Java程序对Microsoft Office格式档案读和写的功能
 * 1. 读取出filepath中的所有数据
 *      InputStream inputStream = new FileInputStream(filepath);
 * 2. 得到工作簿
 *      Workbook xssWorkbook;
 *      if(filepath.endsWith(".xls")){
 *          xssWorkbook = new HSSFWorkbook(inputStream);
 *      }else{
 *          xssWorkbook = new XSSFWorkbook(inputStream);
 *      }
 *
 * 3. 得到工作表Sheet
 *      int sheetNum = xssWorkbook.getNumberOfSheets();
 *      Sheet xssSheet = xssWorkbook.getSheet(sheetName);
 *
 * 4. 获得数据的总行数
 *      int lastRowNum = xssSheet.getLastRowNum();
 *
 * 5. 获得Row
 *      for(int rowNum = 1; rowNum < lastRowNum; rowNum++){
 *          Row xssRow = xssSheet.getRow(rowNum);
 *      }
 *
 * 6. 获取数据总列cell数
 *      int lastCellNum = xssRow.getLastCellNum();
 *
 * 7. 获取cell
 *      for(short celNum = 0; celNum < lastCellNum; cellNum++){
 *          Cell xssCell = xssRow.getCell(cellNum);
 *      }
 *
 * 7. 获取一个数据类型的数据
 *      if(null == cell){
 *          return "";
 *      }else if(cell.getCellTypeEnum == CellType.BOOLEAN){
 *          return String.valueOf(xssCell.getBooleanCelValue());
 *      }else if(cell.getCellTypeEnum == CellType.NUMERIC){
 *          return String.valueOf(xssCell.getNumericCellValue());
 *      }else{
 *          return String.valueOf(xssCell.getStringCellValue());
 *      }
 *
 *
 *
 * 封装实现方法顺序：
 * 1. 获取单元格的值 - 根据cell
 * 2. 获取cell    - cell <- row <- sheet
 * 3. 获取表头，转换成bean中对应的属性 -> 获取所有属性的setter方法集
 * 4.
 * 5. 转换Excel里的数据成List   - 根据Workbook，sheetName
 * 6. 获取Excel里的数据   - 根据Excel文件，sheetName
 * 7.
 */
public class ExcelUtil {
    /**
     * 7. 获取Excle 表里的所有数据
     * @param clz
     * @param path
     * @param <T>
     * @return
     */
    public static <T> List<T> readExcel(Class<T> clz, String path){
        System.out.println(path);
        if(null == path || "".equals(path)){
            return null;
        }

        InputStream inputStream;
        Workbook xssWorkbook;

        try{
            inputStream = new FileInputStream(path);
            //2003版本的excel，用.xls结尾
            if(path.endsWith(".xls")){
                xssWorkbook = new HSSFWorkbook(inputStream);
            }else{
                xssWorkbook = new XSSFWorkbook(inputStream);
            }
            inputStream.close();

            int sheetNumber = xssWorkbook.getNumberOfSheets();
            List<T> allData = new ArrayList<T>();
            for (int i  = 0; i < sheetNumber; i++){
                allData.addAll(transToObject(clz, xssWorkbook, xssWorkbook.getSheetName(i));
            }
            return allData;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("转换excel文件失败：" + e.getMessage());
        }

    }

    /**
     * 6. 获取Excel表指定的sheet表数据
     * @param clz
     * @param path
     * @param sheetName
     * @param <T>
     * @return
     */
    public static <T> List<T> readExcel(Class<T> clz, String path, String sheetName){
        if(null == path || "".equals(path)){
            return null;
        }

        InputStream inputStream;
        Workbook xssWorkbook;
        try{
            inputStream = new FileInputStream(path);
            if(path.endsWith(".xls")){
                xssWorkbook = new HSSFWorkbook(inputStream);
            }else{
                xssWorkbook = new XSSFWorkbook(inputStream);
            }
            inputStream.close();
            //转换成List<T>
            return transToObject(clz, xssWorkbook, sheetName);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("转换excel文件失败：" + e.getMessage());
        }
    }

    /**
     * 5. 使用Excel中的数据, 实例化该类，并添加到列表中List<T>
     * @param clz
     * @param xssWorkbook
     * @param sheetName
     * @param <T>
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static <T> List<T> transToObject(Class<T> clz, Workbook xssWorkbook, String sheetName)
            throws InstantiationException, IllegalAccessException, InvocationTargetException{
        /**
         * 1. 根据工作簿得到工作表sheet
         * 2. 根据工作表得到第一行Row，并根据这个Row得到第一行的数据
         * 3. 第一行数据添加到列表里，形成表头列表
         * 4. 根据表头属性，获取相应的setter方法的集合
         * 5. 获取第rowNum行
         * 6. 获取第rowNum行的所有数据
         * 7. 实例该类
         * 8. 设置该类中属性的值
         * 9. 将该类的实例信息，添加到列表中
         * 10. 返回列表
         */
        List<T> list = new ArrayList<T>();

        //得到工作表Sheet
        Sheet xssSheet = xssWorkbook.getSheet(sheetName);

        //获取第一行：表头
        Row firstRow = xssSheet.getRow(0);
        if(null == firstRow){
            return list;
        }

        //获取第一行表头数据
        List<Object> heads = getRow(firstRow);

        //添加sheetName字段，用于封装至bean中，与bean中的字段相匹配。
        heads.add("sheetName");

        //根据表头中对应bean中的属性，获取所有属性的setter方法集
        Map<String, Method> headMethod = getSetMethod(clz, heads);

        //获取sheet中，其他行的数据
        for (int rowNum = 1; rowNum <= xssSheet.getLastRowNum(); rowNum++){
            try{
                //获取第rowNum行
                Row xssRow = xssSheet.getRow(rowNum);
                if(xssRow == null){
                    continue;
                }

                T t = clz.newInstance();
                //获取xssRow的所有数据
                List<Object> data = getRow(xssRow);
                //如果发现表数据的列数小于表头的列数，则自动填充为null，最后一位不动，用于添加sheetName数据
                while (data.size()+1 < heads.size()){
                    data.add("");
                }

                data.add(sheetName);
                //回调方法，设置属性的值
                setValue(t, data, heads, headMethod);

                list.add(t);

            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }

        return list;

    }

    /**
     * 3. 获取所有属性的setter方法集
     * @param clz
     * @param heads
     * @return
     */
    private static Map<String, Method> getSetMethod(Class<T> clz, List<Object> heads){
        Map<String, Method> map = new HashMap<String, Method>();
        Method[] methods = clz.getMethods();
        for (Object head : heads){
            for (Method method : methods){
                if(method.getName().toLowerCase().equals("set" + head.toString().toLowerCase()) && method.getParameterTypes().length == 1){
                    map.put(head.toString(), method);
                    break;
                }
            }
        }
        return map;
    }

    /**
     * 4. 根据数据，表头信息，所有setter方法集 -> 根据反射回调方法，设置属性的值
     * @param obj
     * @param data
     * @param heads
     * @param methods
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static void setValue(Object obj, List<Object> data, List<Object> heads, Map<String, Method> methods)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        for (Map.Entry<String, Method> entry : methods.entrySet()){
            Object value = "";
            int dataIndex = heads.indexOf(entry.getKey());
            if(dataIndex < data.size()){
                value = data.get(heads.indexOf(entry.getKey()));
            }

            Method method = entry.getValue();
            //获取方法的参数类型，返回一个Class对象数组
            Class<?> param = method.getParameterTypes()[0];

            //根据不同参数类型，回调方法
            if(String.class.equals(param)){
                method.invoke(obj, value);
            }
            else if(Integer.class.equals(param) || int.class.equals(param)){
                if(value.toString() == ""){
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).intValue());
            }
            else if(Long.class.equals(param) || long.class.equals(param)){
                if(value.toString() == ""){
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).longValue());
            }
            else if(Short.class.equals(param) || short.class.equals(param)){
                if(value.toString() == ""){
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).shortValue());
            }
            else if(Boolean.class.equals(param) || boolean.class.equals(param)){
                method.invoke(obj, Boolean.valueOf(value.toString()) || value.toString().toLowerCase().equals("y"));
            }
            else if(JSONObject.class.equals(param) || JSONObject.class.equals(param)){
                method.invoke(obj, JSONObject.parseObject(value.toString()));
            }
            else{
                method.invoke(obj, value);
            }
        }
    }

    /**
     * 2. 获取Excel中的单元格 列
     * @param xssRow    行
     * @return
     */

    private static List<Object> getRow(Row xssRow){
        List<Object> cells = new ArrayList<Object>();
        if(xssRow != null){
            for(short cellNum = 0; cellNum < xssRow.getLastCellNum(); cellNum++){
                Cell xssCell = xssRow.getCell(cellNum);
                cells.add(xssCell);
            }
        }
        return cells;
    }

    /**
     * 1. 获取单元格中的值
     * @param cell  列
     * @return
     */
    public static String getValue(Cell cell){
        if(null == cell){
            return "";
        }
        else if(cell.getCellTypeEnum() == CellType.BOOLEAN){
            return String.valueOf(cell.getBooleanCellValue());
        }
        else if(cell.getCellTypeEnum() == CellType.NUMERIC){
            return String.valueOf(cell.getNumericCellValue());
        }
        else{
            return String.valueOf(cell.getStringCellValue());
        }
    }
}
