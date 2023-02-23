package FSM.services.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import FSM.entities.jobs.HeadPair;
import FSM.entities.jobs.NSW;
import net.dv8tion.jda.api.utils.FileUpload;

public class JobsService<T extends ToDataService> {
    private String[] SearchTerms = { "Crisis Management", "Governance", "Goverment Investigations", "HR grieviance",
            "Human Resources Grieviance", "HR Disciplinary", "Human Resources Disiplinary", "Inspect",
            "People Management", "policy" };
    private String uri = "";
    private String[] JSONpath;
    private String headerName;
    private String listName = "";
    // private Class<T> jobType;
    private static Workbook wb = null;

    private static CellStyle headerStyle;
    private static CellStyle style;
    private static XSSFFont font;

    public JobsService(String uri, String headerName, String listName, String... JSONpath) {
        this.uri = uri;
        this.JSONpath = JSONpath;
        this.headerName = headerName;
        this.listName = listName;

        if (wb == null) {
            wb = new XSSFWorkbook();
            headerStyle = wb.createCellStyle();
            style = wb.createCellStyle();
            font = ((XSSFWorkbook) wb).createFont();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            headerStyle.setFont(font);
            style.setWrapText(true);
        }
    }

    public void getJobs(String name, Class<?> typeClass, HeadPair... extraHeaders) {
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost req = new HttpPost(uri);
        org.apache.poi.ss.usermodel.Sheet sheet;
        int rowCount = 1;
        try {
            sheet = wb.createSheet(name);
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);
        } catch (Exception e) {
            sheet = wb.getSheet(name);
            Row r = sheet.getRow(rowCount);
            while (r.getCell(0) != null && !r.getCell(0).getStringCellValue().equalsIgnoreCase("")) {
                rowCount++;
                r = sheet.getRow(rowCount);
            }
        }

        for (String search : SearchTerms) {
            ArrayList<NameValuePair> payload = new ArrayList<>();
            payload.add(new BasicNameValuePair(headerName, search));

            // req. addHeader(headerName, search);
            for (HeadPair header : extraHeaders) {
                payload.add(new BasicNameValuePair(header.HeaderName, header.HeaderValue));
            }
            try {
                req.setEntity(new UrlEncodedFormEntity(payload, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // req.addHeader("SearchKey", "Inspect");
            try {
                org.apache.http.HttpResponse res = client.execute(req);
                if (res.getStatusLine().getStatusCode() != 200) {
                    System.out.println("http error" + res.getStatusLine());
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader((res.getEntity().getContent())));

                    Row head = sheet.createRow(0);
                    String[] headers = { "Title", "Location", "Organisation / Company", "link", "Reference",
                            "Job close date", "Salary Range" };
                    for (int i = 0; i < 7; i++) {
                        Cell cell = head.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    while ((response = br.readLine()) != null) {
                        Gson gson = new Gson();
                        JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                        for (String path : JSONpath) {
                            responseJson = responseJson.get(path).getAsJsonObject();
                        }
                        String jobsJson = gson.toJson(responseJson.get(listName));
                        // TypeToken<T[]> type = TypeToken.of(T[].class);
                        // System.out.println(clazz.getSimpleName());
                        T[] jobs = (T[]) gson.fromJson(jobsJson, typeClass);

                        for (T job : jobs) {
                            if (!job.putinHash()) {
                                Row row = sheet.createRow(rowCount);
                                row.setHeight((short) 1450);
                                String[] data = job.toData();
                                // if (search.equalsIgnoreCase("inspect")) {
                                // System.out.println(data[0]);
                                // }
                                if (data[0].toLowerCase().contains(search.toLowerCase())
                                        || data[0].toLowerCase().contains("advisor")
                                        || data[0].toLowerCase().contains("quality")) {
                                    rowCount++;
                                    // String[] data = { job.getTitle(), job.getJobLocationText(),
                                    // job.getAgencyName(),
                                    // job.getJobUrl(), job.getReferenceId(), job.getClosingDate(), "" };
                                    for (int i = 0; i < 7; i++) {
                                        Cell cell = row.createCell(i);
                                        cell.setCellValue(data[i]);
                                        cell.setCellStyle(style);
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println(rowCount);
                client.getConnectionManager().closeExpiredConnections();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // try {
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
    }

    public void getJobsWithCustomSearch(String name, String SearchParam, Class<?> typeClass, HeadPair... extraHeaders) {
        //Replace <> with search term
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost req = new HttpPost(uri);
        org.apache.poi.ss.usermodel.Sheet sheet;
        int rowCount = 1;
        try {
            sheet = wb.createSheet(name);
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);
        } catch (Exception e) {
            sheet = wb.getSheet(name);
            Row r = sheet.getRow(rowCount);
            while (r.getCell(0) != null && !r.getCell(0).getStringCellValue().equalsIgnoreCase("")) {
                rowCount++;
                r = sheet.getRow(rowCount);
            }
        }
        for (String search : SearchTerms) {
            ArrayList<NameValuePair> payload = new ArrayList<>();
            payload.add(new BasicNameValuePair(headerName, SearchParam.replace("<>", search)));

            // req. addHeader(headerName, search);
            for (HeadPair header : extraHeaders) {
                payload.add(new BasicNameValuePair(header.HeaderName, header.HeaderValue));
            }
            try {
                req.setEntity(new UrlEncodedFormEntity(payload, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // req.addHeader("SearchKey", "Inspect");
            try {
                org.apache.http.HttpResponse res = client.execute(req);
                if (res.getStatusLine().getStatusCode() != 200) {
                    System.out.println("http error" + res.getStatusLine());
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader((res.getEntity().getContent())));

                    Row head = sheet.createRow(0);
                    String[] headers = { "Title", "Location", "Organisation / Company", "link", "Reference",
                            "Job close date", "Salary Range" };
                    for (int i = 0; i < 7; i++) {
                        Cell cell = head.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    while ((response = br.readLine()) != null) {
                        Gson gson = new Gson();
                        JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                        for (String path : JSONpath) {
                            JsonElement ele = responseJson.get(path);
                            if (ele.isJsonArray()) {
                                JsonArray arr = ele.getAsJsonArray();
                                responseJson = arr.get(0).getAsJsonObject();
                            } else {
                                responseJson = responseJson.get(path).getAsJsonObject();
                            }
                        }
                        String jobsJson = gson.toJson(responseJson.get(listName));
                        // TypeToken<T[]> type = TypeToken.of(T[].class);
                        // System.out.println(clazz.getSimpleName());
                        T[] jobs = (T[]) gson.fromJson(jobsJson, typeClass);

                        for (T job : jobs) {
                            if (!job.putinHash()) {
                                Row row = sheet.createRow(rowCount);
                                row.setHeight((short) 1450);
                                String[] data = job.toData();
                                // if (search.equalsIgnoreCase("inspect")) {
                                // System.out.println(data[0]);
                                // }
                                if (data[0].toLowerCase().contains(search.toLowerCase())
                                        || data[0].toLowerCase().contains("advisor")
                                        || data[0].toLowerCase().contains("quality")) {
                                    rowCount++;
                                    // String[] data = { job.getTitle(), job.getJobLocationText(),
                                    // job.getAgencyName(),
                                    // job.getJobUrl(), job.getReferenceId(), job.getClosingDate(), "" };
                                    for (int i = 0; i < 7; i++) {
                                        Cell cell = row.createCell(i);
                                        cell.setCellValue(data[i]);
                                        cell.setCellStyle(style);
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println(rowCount);
                client.getConnectionManager().closeExpiredConnections();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // try {
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
    }

    public static void sendSheet() {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream("jobs.xlsx");
            wb.write(outputStream);
            wb.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
