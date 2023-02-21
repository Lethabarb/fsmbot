package FSM.services.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import FSM.entities.jobs.NSW;
import net.dv8tion.jda.api.utils.FileUpload;

public class APSservice {

    public APSservice() {
    }

    public static Workbook getJobs() {
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost req = new HttpPost("https://www.apsjobs.gov.au/s/sfsites/aura?r=1&aura.ApexAction.execute=1");
        req.addHeader("message", "{\"actions\":[{\"id\":\"89;a\",\"descriptor\":\"aura://ApexActionController/ACTION$execute\",\"callingDescriptor\":\"UNKNOWN\",\"params\":{\"namespace\":\"\",\"classname\":\"aps_jobSearchController\",\"method\":\"retrieveJobListings\",\"params\":{\"filter\":\"{\"searchString\":\"" + "Officer" +"\",\"salaryFrom\":null,\"salaryTo\":null,\"closingDate\":null,\"positionInitiative\":null,\"classification\":null,\"securityClearance\":null,\"officeArrangement\":null,\"duration\":null,\"department\":null,\"category\":null,\"opportunityType\":null,\"employmentStatus\":null,\"state\":null,\"sortBy\":null,\"offset\":0,\"offsetIsLimit\":false,\"lastVisitedId\":null,\"daysInPast\":null,\"name\":null,\"type\":null,\"notificationsEnabled\":null,\"savedSearchId\":null}&requested=Tue Feb 21 2023 15:54:56 GMT+1100 (Australian Eastern Daylight Time)\"},\"cacheable\":false,\"isContinuation\":false}}]}");
        // req.addHeader("PageSize", "25");
        Workbook wb = new XSSFWorkbook();
        try {
            org.apache.http.HttpResponse res = client.execute(req);
            if (res.getStatusLine().getStatusCode() != 200) {
                System.out.println("http error" + res.getStatusLine());
            } else {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader((res.getEntity().getContent())));

                org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet();
                sheet.setColumnWidth(0, 6000);
                sheet.setColumnWidth(1, 4000);

                // Row header = sheet.createRow(0);

                CellStyle headerStyle = wb.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                XSSFFont font = ((XSSFWorkbook) wb).createFont();
                font.setFontName("Arial");
                font.setFontHeightInPoints((short) 16);
                font.setBold(true);
                headerStyle.setFont(font);

                // Cell headerCell = header.createCell(0);
                // headerCell.setCellValue("Name");
                // headerCell.setCellStyle(headerStyle);

                // headerCell = header.createCell(1);
                // headerCell.setCellValue("Age");
                // headerCell.setCellStyle(headerStyle);

                CellStyle style = wb.createCellStyle();
                style.setWrapText(true);

                // Row row = sheet.createRow(2);
                // Cell cell = row.createCell(0);
                // cell.setCellValue("John Smith");
                // cell.setCellStyle(style);
                // cell = row.createCell(1);
                // cell.setCellValue(20);
                // cell.setCellStyle(style);

                Row head = sheet.createRow(0);
                String[] headers = { "Title", "Location", "Organisation / Company", "link", "Reference",
                        "Job close date", "Salary Range" };
                for (int i = 0; i < 7; i++) {
                    Cell cell = head.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);

                }
                // cell.setCellValue("response");
                int rowCount = 1;
                while ((response = br.readLine()) != null) {
                    Gson gson = new Gson();
                    JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                    responseJson = responseJson.get("Data").getAsJsonObject();
                    responseJson = responseJson.get("Result").getAsJsonObject();
                    String jobsJson = gson.toJson(responseJson.get("ListItem"));
                    // String jobsJson = responseJson.get("Result")
                    NSW[] jobs = gson.fromJson(jobsJson, NSW[].class);
                    // JobAd[] jobs = responseJson.get
                    for (NSW job : jobs) {
                        Row row = sheet.createRow(rowCount);
                        row.setHeight((short) 145);
                        rowCount++;
                        String[] data = { job.getTitle(), job.getJobLocationText(), job.getAgencyName(),
                                job.getJobUrl(), job.getReferenceId(), job.getClosingDate(), "" };
                        for (int i = 0; i < 7; i++) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(data[i]);
                            cell.setCellStyle(style);
                        }
                    }
                    // c.sendMessage(response).queue();
                }
                FileOutputStream outputStream = new FileOutputStream("jobs.xlsx");
                wb.write(outputStream);
                wb.close();
                // c.sendFiles(FileUpload.fromData(new File("jobs.xlsx"))).queue();
            }
            client.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return wb;
        // try {
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
    }
}
