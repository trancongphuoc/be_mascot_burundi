package burundi.treasure.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


@Service
public class ExcelService {
	public byte[] exportToExcel(List<LuckyHistory> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); 
        		ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("History");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No.", "Phone", "Add Time", "Gift"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (LuckyHistory history : data) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(history.getUser().getPhone());
                row.createCell(2).setCellValue(history.getAddTime().toString());
                row.createCell(3).setCellValue(history.getGiftId());
                ++rowNum;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook content to a byte array
            workbook.write(out);

            // Return the byte array containing the Excel data
            return out.toByteArray();
        }
    }

    public byte[] exportToExcelUser(List<User> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("History");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No.", "Phone", "Add Time", "TotalStar"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (User user : data) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(user.getPhone());
                row.createCell(2).setCellValue(user.getAddTime().toString());
                row.createCell(3).setCellValue(user.getTotalStar());
                ++rowNum;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook content to a byte array
            workbook.write(out);

            // Return the byte array containing the Excel data
            return out.toByteArray();
        }
    }
}
