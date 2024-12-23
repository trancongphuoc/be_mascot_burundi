package burundi.treasure.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import burundi.treasure.model.dto.ChartModel;
import burundi.treasure.payload.Response;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class Utils {

	public String formatPhoneBurundi(String phoneNumber) {
		phoneNumber = phoneNumber.trim();
		if (phoneNumber.startsWith("257")) {
			phoneNumber = "+" + phoneNumber;
		} else if (phoneNumber.startsWith("0")) {
			phoneNumber = "+257" + phoneNumber.substring(1);
		} else if (!phoneNumber.startsWith("+257")) {
			phoneNumber = "+257" + phoneNumber;
		}

		return phoneNumber.replace("+", "");
	}
	
	public String callApi(String targetUrl, String method, String data, Map<String, String> headers) {
		try {
			log.info("call API");
			log.info("METHOD: " + method);
			log.info("URL: " + targetUrl);
			URL url = new URL(targetUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			con.setRequestMethod(method);
			con.setDoOutput(true);
			con.setDoInput(true);

			if (headers != null) {
				headers.forEach((key, value) -> {
					//System.out.println(value);
					con.setRequestProperty(key, value);
				});
			}

			con.setDoOutput(true);

			if (data != null) {
				log.info("DATA: " + data);
				OutputStream os = con.getOutputStream();
				byte[] input = data.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

			StringBuilder response = new StringBuilder();
			String responseLine = null;

			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			log.info("RESPONSE: " + response);
			return response.toString();
		} catch (Exception e) {
			log.warn("BUGS", e);
			return "";
		}
	}
	
	public List<ChartModel> convertToChartModels(List<Object[]> data) {
        List<ChartModel> chartModels = new ArrayList();
        for (Object[] row : data) {
            String date = formatDate((String) row[0]);
            Long count = (Long) row[1];
            Date dateSort = formatDateDDMMYYYY(date);
            chartModels.add(new ChartModel(date, count, dateSort));
        }
        return chartModels;
    }


	public List<ChartModel> convertToChartModelsUser(List<Object[]> data) {
        List<ChartModel> chartModels = new ArrayList();
        for (Object[] row : data) {
            String date = (String) row[0];
            Long count = (Long) row[1];
            chartModels.add(new ChartModel(date, count, null));
        }
        return chartModels;
    }
	
	public long sumCountChartModel(List<ChartModel> list) {
		try {
			return list.stream().mapToLong(ChartModel::getCount).sum();
		} catch (Exception e) {
			log.warn(e);
			return 0;
		}
	}
	
	public List<ChartModel> sortListChartModelByDate(List<ChartModel> list) {
		return list.stream().sorted(Comparator.comparing(ChartModel::getDateSort)).collect(Collectors.toList());
	}
	
	public String generateTransactionID() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		final String SESS = "998" + dateFormat.format(new Date());
		return SESS;
	}
	
	public String formatDate(String date) {
		String[] dates = date.split("-");
	    return String.format("%02d-%02d-%s", Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), dates[2]);
	}
	
	public Date formatDateDDMMYYYY(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			return new Date(0);
		}
	}
	
	public String formatDateYYYYmmdd(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
	}
	
	public String formatDate(Date date, SimpleDateFormat sdf) {
        return sdf.format(date);
	}
	
	public String formatDateDDMMYYYY(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date);
    }
	public String formatDateYYYYMMDD(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
//	public List<ChartModel> addMoreDate(List<ChartModel> list, Date endDate) {
//		if(!list.isEmpty()) {
//			Date date = list.get(0).getDateSort();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(endDate);
//            calendar.add(Calendar.DAY_OF_MONTH, -1); 
//            endDate = calendar.getTime();
//			
//			while (date.before(endDate) || date.equals(endDate)) {
//				
//	            String dateToCheck = formatDateDDMMYYYY(date);
//				boolean dateExists = list.stream()
//		                .anyMatch(chartModel -> chartModel.getDate().equals(dateToCheck));
//				
//                calendar.setTime(date);
//                calendar.add(Calendar.DAY_OF_MONTH, 1); // Thêm 1 ngày
//                
//                if(!dateExists) {
//                    ChartModel newChartModel = new ChartModel();
//                    newChartModel.setDateSort(date);
//                    newChartModel.setDate(formatDateDDMMYYYY(date));
//                    newChartModel.setCount(0L);
//                    list.add(newChartModel);
//                }
//                // Cập nhật lại ngày cuối cùng trong danh sách
//                date = calendar.getTime();
//			}
//		}
//		
//		return list;
//	}
	
	
	public List<ChartModel> addMoreDate(List<ChartModel> list, Date startDate, Date endDate) {
		if(!list.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1); 
            endDate = calendar.getTime();
			
			while (startDate.before(endDate) || startDate.equals(endDate)) {
				
	            String dateToCheck = formatDateDDMMYYYY(startDate);
				boolean dateExists = list.stream()
		                .anyMatch(chartModel -> chartModel.getDate().equals(dateToCheck));
				
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Thêm 1 ngày
                
                if(!dateExists) {
                    ChartModel newChartModel = new ChartModel();
                    newChartModel.setDateSort(startDate);
                    newChartModel.setDate(formatDateDDMMYYYY(startDate));
                    newChartModel.setCount(0L);
                    list.add(newChartModel);
                }
                // Cập nhật lại ngày cuối cùng trong danh sách
                startDate = calendar.getTime();
			}
		}
		
		return list;
	}
	
	public Response getResponseOK() {
		return new Response("OK", "OK");
	}
	
	public Response getResponseFailed() {
		return new Response("FAILED", "FAILED");
	}
	
	public Response getResponseOK(String message) {
		return new Response("OK", message);
	}
	
	public Response getResponseFailed(String message) {
		return new Response("FAILED", message);
	}
	
	public boolean isSameDay(Date date1, Date date2) {
	    Calendar cal1 = Calendar.getInstance();
	    Calendar cal2 = Calendar.getInstance();
	    cal1.setTime(date1);
	    cal2.setTime(date2);

	    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	public String removePrefixPhoneNumber(String phone) {
		return phone.replace("+257", "");
	}

	public static long getLong(Object property, long i) {
		// TODO Auto-generated method stub
		if (property == null)
			return i;
		if (property instanceof Integer)
			return Long.valueOf(((Integer) property).longValue());
		else if (property instanceof Long)
			return ((Long) property);
		else if (property instanceof Double)
			return ((Double) property).longValue();
		else
			return i;
	}

	public Date getStartOfDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static void main(String[] args) {
		String dateString = "01-01-2000";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate date = LocalDate.parse(dateString, formatter);
		System.out.println(date);
		System.out.println(new Date("01-01-2000"));
	}

	public String getText(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}

}
