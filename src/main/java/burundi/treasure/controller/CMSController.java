package burundi.treasure.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


import burundi.treasure.model.LuckyHistory;
import burundi.treasure.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import burundi.treasure.common.Utils;
import burundi.treasure.model.MPSRequest;
import burundi.treasure.model.User;
import burundi.treasure.model.dto.ChartModel;
import burundi.treasure.model.dto.DateRangeDTO;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
@RequestMapping("/cms")
public class CMSController {
	
	
	@Autowired
	private LuckyService luckyService;
	
	@Autowired
	private MPSService mpsService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private Utils utils;
	
	@Autowired
	private ExcelService excelService;
	
	@GetMapping("/revenue")
	public String cms(@ModelAttribute DateRangeDTO dateRangeDTO, Model model) {
		
		if(dateRangeDTO.getEndDate() == null) {
			dateRangeDTO.setEndDate(new Date());
		}
		
		if(dateRangeDTO.getStartDate() == null) {
			 // Lấy ngày hiện tại
	        Calendar calendar = Calendar.getInstance();

	        // Trừ 30 ngày
	        calendar.add(Calendar.DATE, -30);

	        // Lấy ngày mới
	        dateRangeDTO.setStartDate(calendar.getTime()); 
		}
		
		if(dateRangeDTO.getRowsPerPage() == null) {
			dateRangeDTO.setRowsPerPage(30);
		}
		
		if(dateRangeDTO.getMsisdn() == null) {
			dateRangeDTO.setMsisdn("");
		} else {
			dateRangeDTO.setMsisdn(utils.removePrefixPhoneNumber(dateRangeDTO.getMsisdn()));
		}
		
		Pageable pageable = PageRequest.of(0, dateRangeDTO.getRowsPerPage());
		
        List<Object[]> mpsRequestsObject = 
        		mpsService.groupByDateAndSumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequests = utils.convertToChartModels(mpsRequestsObject);
        mpsRequests = utils.addMoreDate(mpsRequests, dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsSort = utils.sortListChartModelByDate(mpsRequests);
        Long totalAmount = utils.sumCountChartModel(mpsRequests);
        
        
        List<Object[]> mpsRequestsRenewObject = 
        		mpsService.groupByDateAndCountRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequestsRenew = utils.convertToChartModels(mpsRequestsRenewObject);
        mpsRequestsRenew = utils.addMoreDate(mpsRequestsRenew,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsRenewSort = utils.sortListChartModelByDate(mpsRequestsRenew);
        Long totalRenew = utils.sumCountChartModel(mpsRequestsRenew);
        
        
        List<Object[]> mpsRequestsRegisterObject = 
        		mpsService.groupByDateAndCountRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequestsRegister = utils.convertToChartModels(mpsRequestsRegisterObject);
        mpsRequestsRegister = utils.addMoreDate(mpsRequestsRegister,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsRegisterSort = utils.sortListChartModelByDate(mpsRequestsRegister);
        Long totalRegister = utils.sumCountChartModel(mpsRequestsRegister);
        
        
        List<Object[]> mpsRequestsCancelObject = 
        		mpsService.groupByDateAndCountRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "1", Arrays.asList("CANCEL", "CANCEL_WEB"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequestsCancel = utils.convertToChartModels(mpsRequestsCancelObject);
        mpsRequestsCancel = utils.addMoreDate(mpsRequestsCancel,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsCancelSort = utils.sortListChartModelByDate(mpsRequestsCancel);
        Long totalCancel = utils.sumCountChartModel(mpsRequestsCancel);

        List<Object[]> mpsRequestsBuyMoreObject = 
        		mpsService.groupByDateAndCountRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("CHARGE"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequestsBuyMore = utils.convertToChartModels(mpsRequestsBuyMoreObject);
        mpsRequestsBuyMore = utils.addMoreDate(mpsRequestsBuyMore,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsBuyMoreSort = utils.sortListChartModelByDate(mpsRequestsBuyMore);
        Long totalBuyMore = utils.sumCountChartModel(mpsRequestsBuyMore);
        
        
        List<Object[]> luckyHistoriesObject = luckyService.getTotalMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn());
        List<ChartModel> luckyHistories = utils.convertToChartModels(luckyHistoriesObject);
        luckyHistories = utils.addMoreDate(luckyHistories,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> luckyHistoriesSort = utils.sortListChartModelByDate(luckyHistories);
        Long totalNoItem = utils.sumCountChartModel(luckyHistories);
		

        List<Object[]> topUserWithMoneyObjects = luckyService.groupByUserPhoneAndSumMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn(), pageable);
        List<ChartModel> topUserWithMoney = utils.convertToChartModelsUser(topUserWithMoneyObjects);
        Long totalWinMoney = luckyService.sumMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn());
        
        List<Object[]> topUserWithPlayObjects = luckyService.groupByUserPhoneAndCountRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn(), pageable);
        List<ChartModel> topUserWithPlay = utils.convertToChartModelsUser(topUserWithPlayObjects);
        Long totalPlay = luckyService.countRecordByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn());
        
        // List top user mua lượt chơi nhiều
        List<Object[]> topUserChargeObjects = 
        		mpsService.groupByUserPhoneAndSumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn(), pageable);
        List<ChartModel> topUserCharge = utils.convertToChartModelsUser(topUserChargeObjects);
        Long totalCharge = mpsService.sumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn());
        
		model.addAttribute("mpsRequestsRenew", mpsRequestsRenewSort);
		model.addAttribute("totalRenew", totalRenew);
		
		model.addAttribute("mpsRequestsRegister", mpsRequestsRegisterSort);
		model.addAttribute("totalRegister", totalRegister);
		
		model.addAttribute("mpsRequestsCancel", mpsRequestsCancelSort);
		model.addAttribute("totalCancel", totalCancel);
		
		model.addAttribute("mpsRequestsBuyMore", mpsRequestsBuyMoreSort);
		model.addAttribute("totalBuyMore", totalBuyMore);
		
		model.addAttribute("mpsRequests", mpsRequestsSort);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("totalAmountVND", totalAmount * 8.5);

		model.addAttribute("luckyHistories", luckyHistoriesSort);
		model.addAttribute("totalNoItem", totalNoItem);
		model.addAttribute("totalNoItemVND", totalNoItem * 8.5);

		model.addAttribute("topUserWithMoney", topUserWithMoney);
		model.addAttribute("totalWinMoney", totalWinMoney);
		model.addAttribute("totalNoItemVND", totalWinMoney * 8.5);

		model.addAttribute("topUserWithPlay", topUserWithPlay);
		model.addAttribute("totalPlay", totalPlay);
		
		model.addAttribute("topUserCharge", topUserCharge);
		model.addAttribute("totalCharge", totalCharge);
		
		model.addAttribute("dateRangeDTO", dateRangeDTO);

		return "view/cms";
	}
	
	
	@GetMapping("/temp")
	public String cmsSimple(@ModelAttribute DateRangeDTO dateRangeDTO, Model model) {
		
		if(dateRangeDTO.getEndDate() == null) {
			dateRangeDTO.setEndDate(new Date());
		}
		
		if(dateRangeDTO.getStartDate() == null) {
			 // Lấy ngày hiện tại
	        Calendar calendar = Calendar.getInstance();

	        // Trừ 30 ngày
	        calendar.add(Calendar.DATE, -30);

	        // Lấy ngày mới
	        dateRangeDTO.setStartDate(calendar.getTime()); 
		}
		
		if(dateRangeDTO.getRowsPerPage() == null) {
			dateRangeDTO.setRowsPerPage(30);
		}
		
		if(dateRangeDTO.getMsisdn() == null) {
			dateRangeDTO.setMsisdn("");
		} else {
			dateRangeDTO.setMsisdn(utils.removePrefixPhoneNumber(dateRangeDTO.getMsisdn()));
		}
		
		Pageable pageable = PageRequest.of(0, dateRangeDTO.getRowsPerPage());
		
        List<Object[]> mpsRequestsObject = 
        		mpsService.groupByDateAndSumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn());
        List<ChartModel> mpsRequests = utils.convertToChartModels(mpsRequestsObject);
        mpsRequests = utils.addMoreDate(mpsRequests,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> mpsRequestsSort = utils.sortListChartModelByDate(mpsRequests);
        Long totalAmount = utils.sumCountChartModel(mpsRequests);
        
        
        List<Object[]> luckyHistoriesObject = luckyService.getTotalMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn());
        List<ChartModel> luckyHistories = utils.convertToChartModels(luckyHistoriesObject);
        luckyHistories = utils.addMoreDate(luckyHistories,dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate());
        List<ChartModel> luckyHistoriesSort = utils.sortListChartModelByDate(luckyHistories);
        Long totalNoItem = utils.sumCountChartModel(luckyHistories);
		

        List<Object[]> topUserWithMoneyObjects = luckyService.groupByUserPhoneAndSumMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn(), pageable);
        List<ChartModel> topUserWithMoney = utils.convertToChartModelsUser(topUserWithMoneyObjects);
        Long totalWinMoney = luckyService.sumMoneyByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), dateRangeDTO.getMsisdn());

        // List top user mua lượt chơi nhiều
        List<Object[]> topUserChargeObjects = 
        		mpsService.groupByUserPhoneAndSumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn(), pageable);
        List<ChartModel> topUserCharge = utils.convertToChartModelsUser(topUserChargeObjects);
        Long totalCharge = mpsService.sumAmountByDateRange(dateRangeDTO.getStartDate(), dateRangeDTO.getEndDate(), "0", Arrays.asList("RENEW", "CHARGE", "REGISTER", "REGISTER_WEB"), dateRangeDTO.getMsisdn());
		
        
     // List top user mua lượt chơi nhiều
        List<Object[]> topUserHaveTotalPlayObjects = 
        		userService.groupByUserPhoneAndSumTotalPlay(dateRangeDTO.getMsisdn());
        List<ChartModel> topUserHaveTotalPlay = utils.convertToChartModelsUser(topUserHaveTotalPlayObjects);
        Long totalPlay = userService.sumTotalPlayByPhone(dateRangeDTO.getMsisdn());
        if(totalPlay == null) totalPlay = 0L;
		model.addAttribute("mpsRequests", mpsRequestsSort);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("totalAmountVND", totalAmount * 8.5);

		model.addAttribute("luckyHistories", luckyHistoriesSort);
		model.addAttribute("totalNoItem", totalNoItem);
		model.addAttribute("totalNoItemVND", totalNoItem * 8.5);

		model.addAttribute("topUserWithMoney", topUserWithMoney);
		model.addAttribute("totalWinMoney", totalWinMoney);
		model.addAttribute("totalNoItemVND", totalWinMoney * 8.5);

		model.addAttribute("topUserCharge", topUserCharge);
		model.addAttribute("totalCharge", totalCharge);
		
		model.addAttribute("topUserHaveTotalPlay", topUserHaveTotalPlay);
		model.addAttribute("totalPlay", totalPlay);

		model.addAttribute("dateRangeDTO", dateRangeDTO);

		return "view/cms_simple";
	}
	
	
	@GetMapping
	public String history(@RequestParam(required = false)@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, 
						@RequestParam(required = false)@DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
						@RequestParam(defaultValue = "") String phone,
						@RequestParam(defaultValue = "") String giftType,
						@RequestParam(defaultValue = "0") int page,
						@RequestParam(defaultValue = "10") int size,
						Model model) {



		if(giftType.equals("CHARGE")) {
			Page<MPSRequest> mpsRequests = mpsService.findAllByChargetTimeBetweenAndMsisdnContaining(startDate, endDate, phone, PageRequest.of(page, size));
			model.addAttribute("mpsRequests", mpsRequests);
		} else {
			Page<LuckyHistory> luckyHistoriesUssd =
					luckyService.findAllByAddTimeBetweenAndUser_PhoneContainingAndGiftTypeContaining(startDate, endDate, phone, giftType, PageRequest.of(page, size));
			model.addAttribute("luckyHistoriesUssd", luckyHistoriesUssd);

		}


		Long totalPlay = userService.sumTotalPlayByPhone(phone);
		model.addAttribute("startDate", startDate != null ? utils.formatDateYYYYMMDD(startDate) : null);
		model.addAttribute("endDate", endDate != null ? utils.formatDateYYYYMMDD(endDate) : null);
		model.addAttribute("phone", phone);
		model.addAttribute("giftType", giftType);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPlay", totalPlay != null ? totalPlay : 0);

		return "view/history";
	}
	

	
	
	@GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response,
							  @RequestParam(required = false)@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
							  @RequestParam(required = false)@DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
							  @RequestParam(defaultValue = "") String phone,
							  @RequestParam(defaultValue = "") String giftType) throws IOException {



		byte[] excelData;
		if (giftType.equals("STARS")) {
			Page<User> users =
					userService.findAllByTotalStarGreaterThanAndPhoneContainingOrderByTotalStarDesc(0L, phone, null);
			excelData = excelService.exportToExcelUser(users.stream().toList());
		} else {
			// Fetch the data based on the provided filters
			Page<LuckyHistory> luckyHistoriesUssd =
					luckyService.findAllByAddTimeBetweenAndUser_PhoneContainingAndGiftTypeContaining(startDate, endDate, phone, giftType, null);
			excelData = excelService.exportToExcel(luckyHistoriesUssd.toList());
		}


        // Set response content type and headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=lucky_history.xlsx");

        // Write Excel data to the response output stream
        response.getOutputStream().write(excelData);
    }
	
	
//	@GetMapping("/download-pdf")
//    public void downloadPdf(@ModelAttribute DateRangeDTO dateRangeDTOt,
//                             HttpServletResponse response) {
//        try {
//    		if(dateRangeDTOt.getEndDate() == null) {
//    			dateRangeDTOt.setEndDate(new Date());
//    		}
//    		
//    		if(dateRangeDTOt.getStartDate() == null) {
//    			 // Lấy ngày hiện tại
//    	        Calendar calendar = Calendar.getInstance();
//
//    	        // Trừ 30 ngày
//    	        calendar.add(Calendar.DATE, -30);
//
//    	        // Lấy ngày mới
//    	        dateRangeDTOt.setStartDate(calendar.getTime()); 
//    		}
//    		
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ITextRenderer renderer = new ITextRenderer();
//            String htmlContent = thymeleafRenderer.renderPdf(dateRangeDTOt);
//            renderer.setDocumentFromString(htmlContent);
//            renderer.layout();
//            renderer.createPDF(outputStream);
//            
//            // Tên tệp PDF sẽ được tạo dựa trên startDate và endDate
//            String fileName = "report_" + "2024-03-01" + "_to_" + "2024-04-01" + ".pdf";
//
//            response.setContentType("application/pdf");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//            OutputStream responseOutputStream = response.getOutputStream();
//            outputStream.writeTo(responseOutputStream);
//            responseOutputStream.flush();
//            responseOutputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
	
	
	
	
	
	
	private List<MPSRequest> generateSampleData(int numberOfRequests) {
		List<MPSRequest> requests = new ArrayList<>();
        Random random = new Random();
        
        User user = userService.findByPhone("+50987654322");
        for (int i = 0; i < numberOfRequests; i++) {
            MPSRequest request = new MPSRequest();
            request.setId((long) (i + 1));
            request.setTransId("TRANS-" + i);
            request.setMsisdn("MSISDN-" + i);
            request.setChargetTime(randomDate());
            request.setParams(String.valueOf(random.nextInt(2))); // Random 0 hoặc 1
            request.setAmount(random.nextInt(1000)); // Random số nguyên từ 0 đến 999
            int actionIndex = random.nextInt(2);
            if (actionIndex == 0) {
                request.setAction("RENEW");
            } else if (actionIndex == 1) {
                request.setAction("REGISTER");
            }
            request.setUser(user);
            mpsService.add(request);
            requests.add(request);
        }

        System.out.println(requests);
        return requests;
    }
	
	
	private List<LuckyHistory> generateLuckyHistories(int numberOfRequests) {
		List<LuckyHistory> luckyHistories = new ArrayList<>();
        Random random = new Random();
        
        User user = userService.findByPhone("+50987654322");
        for (int i = 0; i < numberOfRequests; i++) {
            LuckyHistory request = new LuckyHistory();
            request.setId((long) (i + 1));
            int actionIndex = random.nextInt(2);
            if (actionIndex == 0) {
                request.setGiftId("HTG");
                request.setGiftType("HTG");
            } else if (actionIndex == 1) {
                request.setGiftId("STAR");
                request.setGiftType("STAR");
            }

            request.setNoItem(random.nextLong(21));
            request.setAddTime(randomDate());
            request.setUser(user);
            luckyService.saveLuckyGiftHistory(request);
            luckyHistories.add(request);
        }

        System.out.println(luckyHistories);
        return luckyHistories;
    }
	
	private Date randomDate() {
        Random random = new Random();
        long millisInDay = 24 * 60 * 60 * 1000;
        long offset = random.nextLong() % (millisInDay * 365); // Random ngày trong vòng một năm
        Date d =  new Date(System.currentTimeMillis() - offset);
        return new Date(d.getYear(), d.getMonth(), d.getDate());
	}
	
	public static void main(String[] args) {
		String xml = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:gwOperationResponse xmlns:ns2=\"http://webservice.bccsgw.viettel.com/\"><Result><error>0</error><description>success</description><return></return><original><?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:saleAnypayCustomerV3Response xmlns:ns2=\"http://ws.payway.viettel.com/\"><return><description>Successful</description><responseCode>0000</responseCode><result><sourceMsisdn>65654877</sourceMsisdn><targetMsisdn>25762640627</targetMsisdn><transAmount>10000</transAmount><transId>230785520</transId></result></return></ns2:saleAnypayCustomerV3Response></S:Body></S:Envelope></original></Result></ns2:gwOperationResponse></S:Body></S:Envelope>";
		System.out.println(new Date().toString());
	}
}
