package com.croods.vyaparerp.controller.sales;

import com.croods.vyaparerp.config.ApiResponse;
import com.croods.vyaparerp.config.ImageResize;
import com.croods.vyaparerp.config.MenuPermission;
import com.croods.vyaparerp.config.SecurityValidation;
import com.croods.vyaparerp.constant.Constant;
import com.croods.vyaparerp.constant.MessageConstant;
import com.croods.vyaparerp.constant.RateLimitConstant;
import com.croods.vyaparerp.constant.SMSCONSTANT;
import com.croods.vyaparerp.controller.merchanttype.MerchantTypeController;
import com.croods.vyaparerp.controller.whatsapp.WhatsappController;
import com.croods.vyaparerp.dto.account.AccountCustomDTO;
import com.croods.vyaparerp.dto.einvoice.IrnDTO;
import com.croods.vyaparerp.dto.ewaybill.EwayDTO;
import com.croods.vyaparerp.dto.file.FileValidationResponse;
import com.croods.vyaparerp.dto.sales.*;
import com.croods.vyaparerp.dto.user.BranchDTO;
import com.croods.vyaparerp.dto.userFront.BasicUserFrontDTO;
import com.croods.vyaparerp.exception.CustomRateLimitExceedException;
import com.croods.vyaparerp.global.CurrentDateTime;
import com.croods.vyaparerp.global.GetFileExtension;
import com.croods.vyaparerp.global.NumberToWord;
import com.croods.vyaparerp.repository.contact.ContactRepository;
import com.croods.vyaparerp.repository.employee.EmployeeRepository;
import com.croods.vyaparerp.repository.feedBack.FeedBackRepository;
import com.croods.vyaparerp.repository.product.HsnTaxMasterRepository;
import com.croods.vyaparerp.repository.product.ProductRepository;
import com.croods.vyaparerp.repository.product.ProductTypeRepository;
import com.croods.vyaparerp.repository.receipt.ReceiptBillRepository;
import com.croods.vyaparerp.repository.sales.SalesHistoryRepository;
import com.croods.vyaparerp.repository.sales.SalesItemRepository;
import com.croods.vyaparerp.repository.sales.SalesMappingRepository;
import com.croods.vyaparerp.repository.sales.SalesRepository;
import com.croods.vyaparerp.repository.stock.StockMasterRepository;
import com.croods.vyaparerp.repository.user.UserRepository;
import com.croods.vyaparerp.service.account.AccountCustomService;
import com.croods.vyaparerp.service.additionalcharge.AdditionalChargeService;
import com.croods.vyaparerp.service.alternativenumber.AlternativeNumberService;
import com.croods.vyaparerp.service.aws.AwsService;
import com.croods.vyaparerp.service.azure.AzureBlobService;
import com.croods.vyaparerp.service.city.CityService;
import com.croods.vyaparerp.service.contact.ContactService;
import com.croods.vyaparerp.service.country.CountryService;
import com.croods.vyaparerp.service.email.EmailService;
import com.croods.vyaparerp.service.feedBackService.FeedBackService;
import com.croods.vyaparerp.service.messageService.MessageService;
import com.croods.vyaparerp.service.messagesetting.GlobalMessageService;
import com.croods.vyaparerp.service.messagesetting.MessageBodyService;
import com.croods.vyaparerp.service.messagesetting.VasyMessageSettingService;
import com.croods.vyaparerp.service.payment.PaymentService;
import com.croods.vyaparerp.service.paymentterm.PaymentTermService;
import com.croods.vyaparerp.service.prefix.PrefixService;
import com.croods.vyaparerp.service.product.HsnTaxMasterService;
import com.croods.vyaparerp.service.product.ProductService;
import com.croods.vyaparerp.service.profile.ProfileService;
import com.croods.vyaparerp.service.ratelimit.RateLimitService;
import com.croods.vyaparerp.service.receipt.ReceiptService;
import com.croods.vyaparerp.service.report.PrintLogService;
import com.croods.vyaparerp.service.report.ReportService;
import com.croods.vyaparerp.service.sales.SalesService;
import com.croods.vyaparerp.service.setting.CompanySettingService;
import com.croods.vyaparerp.service.setting.DateFormatMasterService;
import com.croods.vyaparerp.service.shiprocketmaster.ShiprocketService;
import com.croods.vyaparerp.service.shopify.ShopifyServiceNew;
import com.croods.vyaparerp.service.shorturl.ShortenUrlService;
import com.croods.vyaparerp.service.state.StateService;
import com.croods.vyaparerp.service.stock.StockMasterService;
import com.croods.vyaparerp.service.stock.StockTransactionService;
import com.croods.vyaparerp.service.tax.TaxService;
import com.croods.vyaparerp.service.termsandcondition.TermsAndConditionService;
import com.croods.vyaparerp.service.typesense.TypesenseService;
import com.croods.vyaparerp.service.userfront.UserService;
import com.croods.vyaparerp.service.whatsapp.WhatsappService;
import com.croods.vyaparerp.service.woocommerce.WooCommerceService;
import com.croods.vyaparerp.service.woocommerce.WooService;
import com.croods.vyaparerp.util.Number;
import com.croods.vyaparerp.util.*;
import com.croods.vyaparerp.util.einvoice.qrbarcode.QRBarcodeEncoder;
import com.croods.vyaparerp.util.einvoice.qrbarcode.tag.*;
import com.croods.vyaparerp.vo.account.AccountCustomVo;
import com.croods.vyaparerp.vo.bankcash.BankVo;
import com.croods.vyaparerp.vo.contact.ContactAddressVo;
import com.croods.vyaparerp.vo.contact.ContactVo;
import com.croods.vyaparerp.vo.datatable.DataTableMetaDTO;
import com.croods.vyaparerp.vo.datatable.sales.DataTableSalesResponceDTO;
import com.croods.vyaparerp.vo.feedback.FeedBackVo;
import com.croods.vyaparerp.vo.messagesetting.VasyMessageSettingVo;
import com.croods.vyaparerp.vo.product.ProductVarientsVo;
import com.croods.vyaparerp.vo.product.ProductVo;
import com.croods.vyaparerp.vo.receipt.ReceiptBillVo;
import com.croods.vyaparerp.vo.receipt.ReceiptVo;
import com.croods.vyaparerp.vo.reportSetting.ReportSettingVo;
import com.croods.vyaparerp.vo.sales.*;
import com.croods.vyaparerp.vo.setting.CompanySettingVo;
import com.croods.vyaparerp.vo.shiprocketmaster.ShiprocketVo;
import com.croods.vyaparerp.vo.stock.StockMasterVo;
import com.croods.vyaparerp.vo.tax.TaxVo;
import com.croods.vyaparerp.vo.userfront.UserFrontVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//♥♥♥
@Log
@Controller
@RequestMapping("/sales/{type}")
public class SalesController {

    @Autowired
    AwsService awsService;

    @Autowired
    UserService userService;

    @Autowired
    private MessageBodyService messageBodyService;

    @Autowired
    DateFormatMasterService dateFormatMasterService;

    @Autowired
    VasyMessageSettingService vasyMessageSettingService;

    @Autowired
    GlobalMessageService globalMessageService;

    @Autowired
    JasperExporter jasperExporter;

    @Autowired
    AccountCustomService accountCustomService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    StockMasterService stockMasterService;

    @Autowired
    SecurityValidation securityValidation;

    @Autowired
    StockMasterRepository stockMasterRepository;

    @Autowired
    WhatsappController whatsappController;

    @Autowired
    CompanySettingService companySettingService;

    @Autowired
    StockTransactionService stockTransactionService;
    @Autowired
    ContactService contactService;

    @Autowired
    ProductService productService;
    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    SalesService salesService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WhatsappService whatsappService;

    @Autowired
    ShortenUrlService urlService;

    @Autowired
    PrefixService prefixService;
    @Autowired
    SalesItemRepository salesItemRepository;
    @Autowired
    PaymentTermService paymentTermService;
    @Autowired
    CountryService countryService;

    @Autowired
    StateService stateService;

    @Autowired
    SalesHistoryRepository salesHistoryRepository;

    @Autowired
    Number numberUtil;

    @Autowired
    CityService cityService;
    @Autowired
    TaxService taxService;

    @Autowired
    AdditionalChargeService additionalChargeService;

    @Autowired
    TermsAndConditionService termsAndConditionService;

    @Autowired
    SalesRepository salesRepository;

    @Autowired
    ReceiptService receiptService;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    SalesMappingRepository salesMappingRepository;

    @Autowired
    ProfileService profileService;

    @Lazy
    @Autowired
    ReportService reportService;

    @Autowired
    WooCommerceService wooCommerceService;

    @Autowired
    MessageService messageService;

    @Autowired
    EmailService sendGridEmailService;

    @Autowired
    ShiprocketService shiprocketService;

    @Autowired
    AzureBlobService azureBlobService;

    @Autowired
    WooService wooService;

    @Autowired
    ReceiptBillRepository receiptBillRepository;

    @Autowired
    PaymentService paymentService;

	@Autowired
	private ShopifyServiceNew shopifyServiceNew;

    @Autowired
    private AlternativeNumberService alternativeNumberService;

    @Autowired
    RateLimitService rateLimitService;

    @Autowired
    PrintLogService printLogService;

    @Autowired
    HsnTaxMasterService hsnTaxMasterService;

    @Autowired
    HsnTaxMasterRepository hsnTaxMasterRepository;

    @Value("${END_POINT_URL}")
    private String END_POINT_URL;

    @Value("${BUCKET}")
    private String BUCKET;

    @Value("${SALES_ATTACHMENT_LOCATION}")
    private String SALES_ATTACHMENT_LOCATION;

    @Value("${FILE_UPLOAD_SERVER}")
    private String FILE_UPLOAD_SERVER;

    String rowNumber = "";


    @Value("${autoGrowCollectionLimit}")
    private int autoGrowCollectionLimit;

    @Value("${base.url}")
    private String BASEURL;

    @Value("${from.to}")
    private String from;

    @Value("${JASPER_REPORT_PATH}")
    private String JASPER_REPORT_PATH;

    @Autowired
    FeedBackRepository feedBackRepository;

    @Autowired
    FeedBackService feedBackService;

    @Autowired
    ProductRepository productRepository;

	@Autowired
	YearEndingPrefixAlertMsg yearEndingPrefixAlertMsg;

    @Autowired
    TypesenseService typesenseService;

    private static final DecimalFormat df2 = new DecimalFormat("#.###");

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(autoGrowCollectionLimit);
    }

    @GetMapping("")
    public ModelAndView salesList(HttpSession session, @PathVariable(value = "type") String type) {
        String rateLimitType ;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_LIST;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_LIST;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_LIST;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_LIST;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_LIST;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_LIST;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView("sales/sales");
        long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
        String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
        // Check if valid using the existing method
        boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
        view.addObject("isValidMerchantType", isValidMerchantType);
        String salestype = type;
        if (type.equals(Constant.SALES_ORDER)) {
            salestype = "salesorder";
        }
        if (MenuPermission.havePermission(session, salestype, Constant.VIEW) == 1) {
            view.addObject("type", type);

            if (type.equals(Constant.SALES_INVOICE)) {
                view.addObject("displayType", "Invoice");
                CompanySettingVo allowshopify = companySettingService.findByCompanyIdAndType(
                        Long.parseLong(session.getAttribute("companyId").toString()), Constant.SHOPIFY);
                if (allowshopify != null) {
                    if (allowshopify.getValue() == 1) {
                        view.addObject("allowShopify", 1);
                    } else {
                        view.addObject("allowShopify", 0);
                    }
                }
            } else if (type.equals(Constant.SALES_ESTIMATE)) {
                view.addObject("displayType", "Estimate");
            } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
                view.addObject("displayType", "Bill Of Supply");
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                view.addObject("displayType", "Credit Note");
            } else if (type.equals(Constant.SALES_ORDER)) {
                view.addObject("displayType", "Sales Order");
            } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {

                view.addObject("displayType", "Delivery Challan");
            }
            view.addObject(Constant.ALLOWFEEDBACK, companySettingService.findByCompanyIdAndType(
                    Long.valueOf(session.getAttribute("companyId").toString()), Constant.ALLOWFEEDBACK).getValue());
            view.addObject("feedBackPermission",
                    MenuPermission.havePermission(session, Constant.FEEDBACK, Constant.VIEW));
            view.addObject("isInsert", MenuPermission.havePermission(session, salestype, Constant.INSERT));
            view.addObject("isEdit", MenuPermission.havePermission(session, salestype, Constant.EDIT));
            view.addObject("isDelete", MenuPermission.havePermission(session, salestype, Constant.DELETE));
            view.addObject("isPdfExcelPrint",
                    MenuPermission.havePermission(session, salestype, Constant.PDF_EXCEL_PRINT));

            if (session.getAttribute("userType").toString().equals("2") || session.getAttribute("parentUserType").toString().equals("2")) {
                view.addObject("branchList", profileService
                        .getCustomListOfBranch(Long.parseLong(session.getAttribute("companyId").toString())));
            }
            view.addObject(Constant.CUSTOMERNAME, companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERNAME).getValue());
            view.addObject(Constant.ALLOWINVOICEWHATSAPP, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWINVOICEWHATSAPP));
            view.addObject(Constant.ALLOWINVOICESMS, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWINVOICESMS));

            try {
                if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE)!=null?session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE):"0").toString()) == 1) {

                    int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());
                    int userType = Integer.parseInt(session.getAttribute("userType").toString());
                    if (userType > Constant.URID_USER) {
                        userType = Integer.parseInt(session.getAttribute("parentUserType").toString());
                    }

                    String typesenseCollectionName = typesenseService.getCollectionName(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), accountingType, userType, Constant.CONTACT_CUSTOMER);
                    log.warning("typesenseCollectionName : "+typesenseCollectionName);
                    view.addObject("typesenseCollectionName", typesenseCollectionName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//			view.addObject("ContactList", contactService.contactList(
//					Long.parseLong(session.getAttribute("branchId").toString()), Constant.CONTACT_CUSTOMER));

            /*
             * DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
             *
             * try { List<SalesVo> salesVos =
             * salesService.findByTypeAndBranchIdAndIsDeletedAndSalesDateBetween(type,
             * Long.parseLong(session.getAttribute("branchId").toString()), 0,
             * dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
             * dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
             *
             * Collections.reverse(salesVos);
             *
             * view.addObject("salesVos", salesVos); } catch (NumberFormatException |
             * ParseException e) { // TODO Auto-generated catch block e.printStackTrace(); }
             */
        } else {
            view.setViewName(Constant.ACCESSDENIED);
        }
        return view;
    }

    @RequestMapping("/list/datatable")
    @ResponseBody
    @Transactional(readOnly = true)
    public DataTableSalesResponceDTO ListProduct(HttpSession session, @PathVariable String type,
                                                 @RequestParam Map<String, String> allRequestParams) throws NumberFormatException, JsonProcessingException {

        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_DATATABLE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_DATATABLE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_DATATABLE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_DATATABLE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        List<String> salesType = Collections.singletonList(type);

        List<Long> branchList = StringUtils.isNotBlank(allRequestParams.get(Constant.BRANCH)) ? Arrays.stream(allRequestParams.get(Constant.BRANCH).split(",")).map(Long::parseLong)
                .collect(Collectors.toList()) :
                Collections.singletonList(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));


        long contactId = 0L;
        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        String serachValue = "";
        int isInvoiceList = 0;
        if (StringUtils.isNotBlank(allRequestParams.get("contactId"))) {
            contactId = Long.parseLong(allRequestParams.get("contactId"));
        }
        String status = "";
        if (StringUtils.isNotBlank(allRequestParams.get("status"))) {
            status = allRequestParams.get("status");
        }
        String paymentStatus = "";
        if (StringUtils.isNotBlank(allRequestParams.get("paymentStatus"))) {
            paymentStatus = allRequestParams.get("paymentStatus");
        }

        if (StringUtils.isNotBlank(allRequestParams.get("search.value"))) {
            serachValue = "%" + allRequestParams.get("search.value") + "%";
        }

        String dateRange = allRequestParams.get("daterange");
        Date startDate = null;
        Date endDate = null;
        Calendar calendar = Calendar.getInstance();

        try {
            if (StringUtils.equals(dateRange, "currentYear")) {
                calendar.setTime(dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString()));
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                calendar.setTime(dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString()));
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange, "lastMonth")) {
                calendar.set(Calendar.DAY_OF_MONTH, -1);
                calendar.add(Calendar.DATE, 1);
                int min = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, min);
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, max);
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange,"thisMonth")) {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, max);
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange, "thisWeek")) {
                calendar.add(Calendar.DATE, -7);
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 7);
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange,"lastWeek")) {
                calendar.add(Calendar.DATE, -14);
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 7);
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange,"today")) {
                startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            } else if (StringUtils.equals(dateRange,"customrange")) {
                if (StringUtils.isNotBlank(allRequestParams.get(Constant.FROM_DATE))) {
                    startDate = dateFormat.parse(allRequestParams.get(Constant.FROM_DATE));
                    endDate = dateFormat.parse(allRequestParams.get(Constant.TO_DATE));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<Long> invoiceIdList;
        try {
            if (StringUtils.isNotBlank(allRequestParams.get("invoiceIds"))) {
                isInvoiceList = 1;
                invoiceIdList = Arrays.stream(allRequestParams.get("invoiceIds").split(",")).map(Long::parseLong).collect(Collectors.toList());
            }else{
                invoiceIdList =  Collections.singletonList(0L);
            }
        } catch (Exception e) {
            e.printStackTrace();
            invoiceIdList = Collections.singletonList(0L);
        }
        int totalLength = salesService.countByDatatable(branchList, contactId, startDate, endDate, salesType, status,
                serachValue, paymentStatus, isInvoiceList, invoiceIdList);

        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");

        int length, page = 0, offset = 0;

        if (!StringUtils.equals(pageLength,"-1")) {
            length = Integer.parseInt(pageLength);
            page = Integer.parseInt(allRequestParams.get(Constant.START)) / length;
            offset = page * length;
        } else {
            length = totalLength;
        }

        DataTableSalesResponceDTO dto = new DataTableSalesResponceDTO();
        dto.setData(salesService.findByProductDatatable(branchList, contactId, startDate, endDate, salesType, status,
                serachValue, length, offset, paymentStatus, isInvoiceList, invoiceIdList));
        dto.setDraw(Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        dto.setError(null);
        dto.setRecordsFiltered(totalLength);
        dto.setRecordsTotal(totalLength);
        dto.setDataTableMetaDTO(
                new DataTableMetaDTO(page, (int) ((double) (totalLength) / length), length, totalLength));

        return dto;

    }

    @RequestMapping("/new")
    public ModelAndView newSales(HttpSession session, @PathVariable(value = "type") String type,
                                 HttpServletRequest request, @Param(value = "contactId") String contactId,
                                 @RequestParam(value = "parentId", defaultValue = "0") String parentId) {
        String rateLimitType;

        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_NEW;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_NEW;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_NEW;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_NEW;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_NEW;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_NEW;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		ModelAndView view = new ModelAndView();
        view.addObject("isPercentageDiscount", companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.BATCHDISCOUNTVALUE).getValue());
		String salestype = type;
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
		if (type.equals(Constant.SALES_ORDER)) {
			salestype = "salesorder";
		}
		if (MenuPermission.havePermission(session, salestype, Constant.INSERT) == 1) {

			view.addObject("paymentTermInsertPermission",
					MenuPermission.havePermission(session, Constant.PAYMENTTERMS, Constant.INSERT));
			long merchantTypeId = Long.parseLong(session.getAttribute("merchantTypeId").toString());
			String clusterId = session.getAttribute("clusterId").toString();
            // Check if valid using the existing method
            boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
            view.addObject("isValidMerchantType", isValidMerchantType);
			int taxVal = 0;
            if(session.getAttribute("governmentTaxType").toString().equals(Constant.VAT)) {
    			taxVal = 1;
    		 }
			String defaultPrefix = "";
            view.addObject(Constant.SALESALLOWFOCUSON,
                    companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.SALESALLOWFOCUSON));
            if (type.equals(Constant.SALES_INVOICE)) {
				// Prefix-Alert After Year Ending Process:-
				yearEndingPrefixAlertMsg.prefixAlertAfterYearEnding(session,Constant.SALES_INVOICE,view);
                view.addObject("displayType", "Invoice");
                defaultPrefix = "INV";
            } else if (type.equals(Constant.SALES_ESTIMATE)) {
                view.addObject("displayType", "Estimate");
                defaultPrefix = "EST";
            } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
                view.addObject("displayType", "Bill Of Supply");
                defaultPrefix = "BOS";
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
				// Prefix-Alert After Year Ending Process:-
				yearEndingPrefixAlertMsg.prefixAlertAfterYearEnding(session,Constant.SALES_CREDIT_NOTE,view);
                view.addObject("displayType", "Credit Note");
                defaultPrefix = "CRD";
            } else if (type.equals(Constant.SALES_ORDER)) {
				// Prefix-Alert After Year Ending Process:-
				yearEndingPrefixAlertMsg.prefixAlertAfterYearEnding(session,Constant.SALES_ORDER,view);
				view.addObject("displayType", "Sales Order");
				defaultPrefix = "ORD";
			} else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
				view.addObject("displayType", "Delivery Challan");
				defaultPrefix = "DC";
			}
//			System.err.println("type-----------------" + type);
            view.addObject("type", type);
//			view.addObject("ContactList", contactService.contactList(
//					Long.parseLong(session.getAttribute("branchId").toString()), Constant.CONTACT_CUSTOMER));

            // view.addObject("ProductList",productService.findByCompanyIdAndProductVoIsDeleted(Long.parseLong(session.getAttribute("companyId").toString()),
            // 0));
            String prefix = prefixService
                    .getPrefixByPrefixTypeAndBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), salestype, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

            if (StringUtils.isNotBlank(prefix))
                defaultPrefix = prefix;

            view.addObject("salesPrefix", defaultPrefix);

            long newSalesNo = salesService.findMaxSalesNo(Long.parseLong(session.getAttribute("branchId").toString()),
                    type, defaultPrefix, Long.parseLong(session.getAttribute("userId").toString()));
            // log.info("newSalesNo:::" + newSalesNo);
            view.addObject("NewSalesNo", newSalesNo);

            view.addObject("allowNegativeStock", companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWNEGATIVESTOCK));

            view.addObject(Constant.CUSTOMERTYPEWISECALCULATION, companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERTYPEWISECALCULATION));
            Boolean billRestriction = false;
            String value = companySettingService.getValueByTypeAndBranchId(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.BILLRESTRICTIONANDWARNING);
            if (StringUtils.isNotBlank(value) && value.equals("1")) {
                value = companySettingService.getValueByTypeAndBranchId(
                        Long.parseLong(session.getAttribute("branchId").toString()), Constant.FORUSERTYPE);
                if (StringUtils.isNotBlank(value) && !value.equals("2")) {
                    billRestriction = true;
                }
            }
            DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            view.addObject("serverdate", dateFormat2.format(date));
            if (Integer.parseInt(session.getAttribute("jioType").toString()) != Constant.JIO_TYPE_AJIO_WHOLESALER) {
                CompanySettingVo flatDiscountLimit = companySettingService.findByBranchIdAndType(
                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.SALESFLATDISCOUNTLIMIT);
                CompanySettingVo flatDiscountLimitInAmount = companySettingService.findByBranchIdAndType(
                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.SALESFLATDISCOUNTLIMITINAMOUNT);
                if(StringUtils.isNotBlank(flatDiscountLimitInAmount.getAddValue())){
                    view.addObject(Constant.SALESFLATDISCOUNTLIMITINAMOUNT, flatDiscountLimitInAmount.getAddValue().trim());
                }else{
                    view.addObject(Constant.SALESFLATDISCOUNTLIMITINAMOUNT, "");
                }
                if (StringUtils.isNoneEmpty(flatDiscountLimit.getAddValue())) {
                    try {
                        view.addObject(Constant.FLATDISCOUNTLIMIT,
                                Double.parseDouble(flatDiscountLimit.getAddValue().trim()));
                        view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                    } catch (Exception e) {
                        view.addObject(Constant.FLATDISCOUNTLIMIT, 100);
                        view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                    }

                } else {
                    view.addObject(Constant.FLATDISCOUNTLIMIT, 100);
                    view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                }
            } else {
                CompanySettingVo flatDiscountLimit = companySettingService
                        .findByType(Constant.AJIOWHOLSELLERDISCOUNTLIMIT);
                view.addObject(Constant.FLATDISCOUNTLIMIT, Double.parseDouble(flatDiscountLimit.getAddValue().trim()));
                CompanySettingVo ajioWholseller = companySettingService.findByType(Constant.AJIOWHOLSELLERCASHDISCOUNT);
                view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT,
                        Double.parseDouble(ajioWholseller.getAddValue().trim()));
            }
            view.addObject("billRestriction", billRestriction);
            view.addObject("paymentTermList",
                    paymentTermService.findBybranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), 0, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
            view.addObject("TermsAndCondition", termsAndConditionService.findByBranchIdAndIsDefaultAndIsDeleted(
                    Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), 1, 0, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
            view.addObject("customerInsert",
                    MenuPermission.havePermission(session, Constant.CONTACT_CUSTOMER, Constant.INSERT));
            view.addObject("contactlist", contactService.findByType(Constant.CONTACT_TRANSPORT,
                    Long.parseLong(session.getAttribute("branchId").toString())));

            String tanNo;
            if ((Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_COMPANY) ||
                    (Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_FRANCHISE) || (Integer.parseInt(session.getAttribute("parentUserType").toString()) == Constant.URID_FRANCHISE)) {
                tanNo = profileService.getTanNo(branchId);
                view.addObject("tanNo", tanNo);
            } else {
                tanNo = profileService.getTanNo(companyId);
                view.addObject("tanNo", tanNo);
            }

            if (StringUtils.isNotBlank(tanNo)) {
                List<Map<String, String>> tscLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TCS);
                view.addObject("TCSLedgerlist", tscLedgerlist);
            }
            view.addObject(Constant.FINISHPRODUCTMAPPING,
                    (companySettingService
                            .findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()),
                                    Constant.FINISHPRODUCTMAPPING)
                            .getValue()));
            view.addObject(Constant.MAPPINGSTOCKMINUS,
                    (companySettingService
                            .findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()),
                                    Constant.MAPPINGSTOCKMINUS)
                            .getValue()));

            ShiprocketVo shiprocketVo = shiprocketService
                    .getDetails(Long.parseLong(session.getAttribute("companyId").toString()));
            if (shiprocketVo != null) {
                view.addObject("status", shiprocketVo.getStatus());
                view.addObject("defaultSyncInvoice", shiprocketVo.getDefaultSyncInvoice());
            } else {
                view.addObject("status", 0);
                view.addObject("defaultSyncInvoice", 0);
            }
            log.info("Sales-Id::::>" + parentId);
            double roundOffAmount = 0.0;
            if (!parentId.equals("0")) {
                boolean fromParentFlag = false;
                SalesVo salesVo = salesService.findBySalesId(Long.parseLong(parentId));
                view.addObject("paymentTermId", (salesVo.getPaymentTermsVo() != null ? salesVo.getPaymentTermsVo().getPaymentTermId() : 0));
                if(salesVo.getTcsJson()!=null && !salesVo.getTcsJson().isEmpty() && salesVo.getTcsJson().get(Constant.TCS_APPLICABLE)!=null){
                    int tcsApply = Integer.parseInt(salesVo.getTcsJson().get(Constant.TCS_APPLICABLE).toString());
                    if (StringUtils.isNotBlank(tanNo)) {
                        view.addObject("tcsAllow", tcsApply);
                    }
                }
                if(salesVo.getRoundoff() != 0){
                    roundOffAmount = salesVo.getRoundoff() ;
                }
                String parentMsg = null;
                for (int i = 0; i < salesVo.getSalesItemVos().size(); i++) {
                    salesVo.getSalesItemVos().get(i).setExpirySee(productRepository.findExpiryProductVarient(salesVo.getSalesItemVos().get(i).getSalesItemId()));
//					System.out.println("->>>>>>>exppiryseeeee>>>>"+salesVo.getSalesItemVos().get(i).getExpirySee());
                    if (salesVo.getSalesItemVos().get(i).getProductVarientsVo().getProductVo() != null && StringUtils.isNotBlank(salesVo.getSalesItemVos().get(i).getProductVarientsVo().getProductVo().getHsnCode())) {
                        salesVo.getSalesItemVos().get(i).getProductVarientsVo().getProductVo().setHsnType(hsnTaxMasterService.getHsnTypeByHsnCode(salesVo.getSalesItemVos().get(i).getProductVarientsVo().getProductVo().getHsnCode()));
                    }
                }
                if (StringUtils.isNotBlank(salesVo.getFlatDiscountType()) && !StringUtils.equals(salesVo.getFlatDiscountType(), Constant.PERCENTAGE)) {
                    Map<String, Object> salesFlatDiscountData = salesService.calculateFlatDiscountPercentageFromAmount(salesVo.getSalesId());
                    if (!salesFlatDiscountData.isEmpty()) {
                        salesVo.setFlatDiscountInPercentage(Double.parseDouble(salesFlatDiscountData.get("flatDiscountPercentage").toString()));
                    }
                }else {
                    salesVo.setFlatDiscountInPercentage(salesVo.getFlatDiscount());
                    salesVo.setFlatDiscountType(Constant.PERCENTAGE);
                }

                if (type.equals(Constant.SALES_INVOICE)) {
                    if (StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER) && (StringUtils.equals(salesVo.getStatus(), Constant.PENDING) || StringUtils.equals(salesVo.getStatus(), Constant.PARTIAL_INVOICE_CREATED))) {
                        fromParentFlag = true;
                    } else if (salesVo != null && !StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER)) {
                        fromParentFlag = true;
                    }
                    if (salesVo != null && StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER)
                            && (StringUtils.equals(salesVo.getStatus(), Constant.DC_CREATED)
                            || StringUtils.equals(salesVo.getStatus(), Constant.PARTIAL_DC_CREATED))) {
                        parentMsg = "Please refresh Order Page - Already Invoice is Created for this Sales Order : "
                                + (salesVo.getType() + salesVo.getSalesNo());
                    }
                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    if (salesVo != null && StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER)
                            && (StringUtils.equals(salesVo.getStatus(), Constant.PENDING)
                            || StringUtils.equals(salesVo.getStatus(), Constant.PARTIAL_DC_CREATED))) {
                        fromParentFlag = true;
                    } else if (salesVo != null && !StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER)) {
                        fromParentFlag = true;
                    }
                    if (salesVo != null && StringUtils.equals(salesVo.getType(), Constant.SALES_ORDER)
                            && (StringUtils.equals(salesVo.getStatus(), Constant.INVOICE_CREATED)
                            || StringUtils.equals(salesVo.getStatus(), Constant.PARTIAL_INVOICE_CREATED))) {
                        parentMsg = "Please refresh Order Page - Already Delivery Challan is Created for this Sales Order : "
                                + (salesVo.getType() + salesVo.getSalesNo());
                    }
                } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    if (salesVo != null && ((salesVo.getTotal() - salesVo.getCreditNoteAmount()) <= 0)) {
                        parentMsg = "Please refresh Invoice Page - Already Credit Note is Created for this Sales Invoice : "
                                + (salesVo.getPrefix() + salesVo.getSalesNo());
                    } else {
                        fromParentFlag = true;
                    }
                } else {
                    fromParentFlag = true;
                }
                // log.warning("parentMsg---->" + parentMsg);
                if (fromParentFlag) {
                    view.addObject("transportlist", salesVo);

                    salesVo = salesService.findBySalesId(Long.parseLong(parentId));

                    //for shipping address country name, state name, city name
                    salesVo.setShippingCountriesName(
                            countryService.findByCountriesCode(salesVo.getShippingCountriesCode()).getCountriesName());
                    salesVo.setShippingStateName(
                            stateService.findByStateCode(salesVo.getShippingStateCode()).getStateName());
                    salesVo.setShippingCityName(cityService.findByCityCode(salesVo.getShippingCityCode()).getCityName());

                    //for billing address country name, state name, city name
                    salesVo.setBillingCountriesName(
                            countryService.findByCountriesCode(salesVo.getBillingCountriesCode()).getCountriesName());
                    salesVo.setBillingStateName(
                            stateService.findByStateCode(salesVo.getBillingStateCode()).getStateName());
                    salesVo.setBillingCityName(cityService.findByCityCode(salesVo.getBillingCityCode()).getCityName());

                    view.addObject("transportlist", salesVo);
                    if (salesVo.getFlatDiscount() > 0) {
                        view.addObject("FLATDISCOUNT", true);
                    } else {
                        view.addObject("FLATDISCOUNT", false);
                    }
                    List<SalesItemVo> itemVos = salesService.findItemBySalesId(Long.parseLong(parentId));
                    Collections.sort(itemVos, Comparator.comparingInt(SalesItemVo::getOrderBy));
                    List<SalesItemVo> salesItemVos = new ArrayList<>();
                    if (type.equals(Constant.SALES_INVOICE)) {
//						salesItemVos = itemVos.stream()
//								.filter(p -> (p.getInvoiceQty() + p.getPartialQty()) != p.getQty())
//								.collect(Collectors.toList());
                        salesItemVos = itemVos.stream()
                                .filter(p -> {
                                    BigDecimal invoiceQty = new BigDecimal(String.valueOf(p.getInvoiceQty()+p.getInvoiceFreeQty()));
                                    BigDecimal partialQty = new BigDecimal(String.valueOf(p.getPartialQty()+p.getPartialFreeQty()));
                                    BigDecimal qty = new BigDecimal(String.valueOf(p.getQty()+p.getFreeQty()));
                                    BigDecimal sum = invoiceQty.add(partialQty);
                                    return sum.compareTo(qty) != 0;
                                }).collect(Collectors.toList());
                        for (SalesItemVo salesItemVo : salesItemVos) {
                            BigDecimal qty = new BigDecimal(String.valueOf(salesItemVo.getQty()));
                            BigDecimal invoiceQty = new BigDecimal(String.valueOf(salesItemVo.getInvoiceQty()));
                            BigDecimal partialQty = new BigDecimal(String.valueOf(salesItemVo.getPartialQty()));
                            BigDecimal salesQty = qty.subtract(invoiceQty.add(partialQty));
                            salesItemVo.setSalesQty(salesQty.doubleValue());

                            //set free qty
                            BigDecimal salesFreeQty = new BigDecimal(String.valueOf(salesItemVo.getFreeQty())).subtract(new BigDecimal(String.valueOf(salesItemVo.getInvoiceFreeQty())).add(new BigDecimal(String.valueOf(salesItemVo.getPartialFreeQty()))));
                            salesItemVo.setSalesFreeQty(salesFreeQty.doubleValue());
                        }
                    } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
//						salesItemVos = itemVos.stream().filter(p -> (p.getDcQty() + p.getPartialQty()) != p.getQty())
//								.collect(Collectors.toList());
                        salesItemVos = itemVos.stream()
                                .filter(p -> {
                                    BigDecimal dcQty = new BigDecimal(String.valueOf(p.getDcQty()+p.getDcFreeQty()));
                                    BigDecimal partialQty = new BigDecimal(String.valueOf(p.getPartialQty()+p.getPartialFreeQty()));
                                    BigDecimal qty = new BigDecimal(String.valueOf(p.getQty()+p.getFreeQty()));
                                    BigDecimal sum = dcQty.add(partialQty);
                                    return sum.compareTo(qty) != 0;
                                }).collect(Collectors.toList());
                        for (SalesItemVo salesItemVo : salesItemVos) {
                            BigDecimal qty = new BigDecimal(String.valueOf(salesItemVo.getQty()));
                            BigDecimal dcQty = new BigDecimal(String.valueOf(salesItemVo.getDcQty()));
                            BigDecimal partialQty = new BigDecimal(String.valueOf(salesItemVo.getPartialQty()));
                            BigDecimal salesQty = qty.subtract(dcQty.add(partialQty));
                            salesItemVo.setSalesQty(salesQty.doubleValue());

                            //set free qty
                            BigDecimal salesFreeQty = new BigDecimal(String.valueOf(salesItemVo.getFreeQty())).subtract(new BigDecimal(String.valueOf(salesItemVo.getDcFreeQty())).add(new BigDecimal(String.valueOf(salesItemVo.getPartialFreeQty()))));
                            salesItemVo.setSalesFreeQty(salesFreeQty.doubleValue());
                        }
                    } else {
                        salesItemVos = itemVos;
                    }

                    salesItemVos.forEach(s -> {
                        try {
                            s.setSalesmanName(employeeRepository.findEmployeeName(s.getSalesmanId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        StockMasterVo stockMasterVo = stockMasterService
                                    .findByProductVarientIdAndBranchIdAndYearIntervalAndBatchNo(
                                        s.getProductVarientsVo().getProductVarientId(),
                                        Long.parseLong(session.getAttribute("branchId").toString()),
                                        session.getAttribute("financialYear").toString(), s.getBatchNo());

                        if (stockMasterVo != null) {
                            s.setAvailableQty(stockMasterVo.getQuantity());
                        }


                        s.setExpirySee(productRepository.findExpiryProductVarient(s.getSalesItemId()));
                        // log.info("->>>>>>>exppiryseeeee>>>>"+s.getExpirySee());
                        // Set HsnType : For HSN Type wise calculation
                        if (s.getProductVarientsVo().getProductVo() != null && StringUtils.isNotBlank(s.getProductVarientsVo().getProductVo().getHsnCode())) {
                            s.getProductVarientsVo().getProductVo().setHsnType(hsnTaxMasterService.getHsnTypeByHsnCode(s.getProductVarientsVo().getProductVo().getHsnCode()));
                        }
                    });

                    try {
                        if (salesVo.getType().equals(Constant.SALES_ORDER)) {
                            view.addObject("orderAmount", salesVo.getTotal());
                        } else {
                            view.addObject("orderAmount", 0);
                        }
                    } catch (Exception e) {
                    }

                    view.addObject("salesItemVos", salesItemVos);
                    view.addObject("parentId", parentId);
//					System.err.println("8888888888888888    " + Long.parseLong(parentId));
//					System.err.println("555    " + salesItemVos);
                    if (salesVo.getSalesAdditionalChargeVos().size() > 0) {
                        view.addObject("additionalChargeVos", salesVo.getSalesAdditionalChargeVos());
                    }
                    List<String> types = new ArrayList<>();
                    types.add(Constant.REPORT_SALES);
                    if (salesVo.getSalesAdditionalChargeVos().size() > 0) {
                        view.addObject("AdditionalChargeVos",additionalChargeService.
                                getAdditionalChargesByBranchIdAndCompanyIdAndTypesAndIsFranchise(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()),types,1));
                    }
                    if (salesItemVos.size() > 0) {

                        view.addObject("parentType", salesItemVos.get(0).getSalesVo().getType());

                        view.addObject("ContactVo", salesItemVos.get(0).getSalesVo().getContactVo());
                        contactId = String.valueOf(salesItemVos.get(0).getSalesVo().getContactVo().getContactId());
//						System.err.println("555    " + salesItemVos.get(0).getSalesVo().getContactVo().getFirstName()
//								+ salesItemVos.get(0).getSalesVo().getContactVo().getLastName());
                    }

                } else {
                    view.addObject("FLATDISCOUNT", false);
                    view.addObject("parentMsg", parentMsg);
                }
            }
            if (contactId != null) {
            int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());
            long userId = Long.parseLong(session.getAttribute("userId").toString());
            int userType = Integer.parseInt(session.getAttribute("userType").toString());
            ContactVo currentContact = contactService.findByContactId(Long.parseLong(contactId));
            String currentContactType = currentContact.getType();
            String flag = contactService.checkBranchOrCompany(accountingType, userId, userType, currentContactType);
            int result = StringUtils.equals(Constant.BRANCH,flag) ?
                    contactService.countByContactIdAndTypeAndBranchIdAndIsDeleted(Long.parseLong(contactId), currentContactType, branchId, 0) :
                    contactService.countByContactIdAndTypeAndCompanyIdAndIsDeleted(Long.parseLong(contactId), currentContactType, companyId, 0) ;
            if ( result != 0) {
                view.addObject("contactId", contactId);
                view.addObject("ContactVo", currentContact);
            }
            }
            List<TaxVo> taxVos = taxService.findByCompanyId(
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), merchantTypeId, clusterId, taxVal);
            view.addObject("tax", taxVos);
            CompanySettingVo garmentIndustryTaxType = companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.GARMENTINDUSTRYTAXTYPE);
            CompanySettingVo garmentTaxCalculationMethod = companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.GARMENTTAX_CALCULATION_METHOD);

            CompanySettingVo hsntypewisecalculation = companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.HSNTYPEWISECALCULATION);
            CompanySettingVo hsntypewisecalculationmethod = companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.HSNTYPEWISECALCULATIONMETHOD);
            view.addObject("hsntypewisecalculation", hsntypewisecalculation);
            view.addObject("hsntypewisecalculationmethod", hsntypewisecalculationmethod);
            view.addObject("garmentIndustryTaxType", garmentIndustryTaxType);
            view.addObject("garmentTaxCalculationMethod", garmentTaxCalculationMethod);

            view.addObject(Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING,
                    companySettingService
                            .findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()),
                                    Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING)
                            .getValue() != 1 ? 0 : 1);
            view.addObject(Constant.ADDNEWLINEINSALES,
                    companySettingService
                            .findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()),
                                    Constant.ADDNEWLINEINSALES)
                            .getValue());
            view.addObject(Constant.ALLOWINVOICEWHATSAPP, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWINVOICEWHATSAPP));
            view.addObject(Constant.ALLOWINVOICESMS, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWINVOICESMS));

            CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESGARMENTDESIGN);
//			System.err.println("value" + settingVo.getValue());
            String gstType= userRepository.getTaxTypeByUserFrontId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
            if (((garmentIndustryTaxType != null && garmentIndustryTaxType.getValue() == 1 )|| (hsntypewisecalculation != null && hsntypewisecalculation.getValue() == 1)) && !gstType.equals(Constant.VAT)) {
                String taxCode = Constant.GST;
                int taxType = Constant.TAX_TYPE_GST;
                try {
                    Map<String, String> gstMap = userRepository
                            .getgstDetails(Long.parseLong(session.getAttribute("companyId").toString()));
                    if (gstMap != null && !gstMap.isEmpty()) {
                        if (StringUtils.isNotBlank(gstMap.get("tax_type"))
                                && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"), Constant.VAT)) {
                            taxCode = Constant.VAT;
                            taxType = Constant.TAX_TYPE_VAT;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String tax_code = "";
                TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(5,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);

                view.addObject("tax5name", taxVo.getTaxName());
                view.addObject("tax5rate", taxVo.getTaxRate());
                view.addObject("tax5id", taxVo.getTaxId());

                taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(12,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);
                view.addObject("tax12name", taxVo.getTaxName());
                view.addObject("tax12rate", taxVo.getTaxRate());
                view.addObject("tax12id", taxVo.getTaxId());

                taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(18,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);
                view.addObject("tax18name", taxVo.getTaxName());
                view.addObject("tax18rate", taxVo.getTaxRate());
                view.addObject("tax18id", taxVo.getTaxId());
            }
            view.addObject(Constant.ROUNDOFF, roundOffAmount);
            view.addObject(Constant.ALLOWDUPLICATEPRODUCTINSALES,
                    companySettingService
                            .findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()),
                                    Constant.ALLOWDUPLICATEPRODUCTINSALES)
                            .getValue());
            view.addObject(Constant.ALLOWROUNDOFF, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWROUNDOFF));
            view.addObject(Constant.MULTIBARCODE, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.MULTIBARCODE));
            view.addObject(Constant.MULTIDUPLICATEBARCODE, companySettingService.findByCompanyIdAndType(
                    Long.parseLong(session.getAttribute("companyId").toString()), Constant.MULTIDUPLICATEBARCODE));
            view.addObject(Constant.CUSTOMERNAME, companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERNAME).getValue());
            view.addObject(Constant.DEFAULTSALESQTYBLANK,
                    companySettingService
                            .findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()),
                                    Constant.DEFAULTSALESQTYBLANK)
                            .getValue());
            view.addObject(Constant.LASTSALESMRP, companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.LASTSALESMRP));
            CompanySettingVo b2bStockOutBy = companySettingService.findByBranchIdAndType(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.B2BSTOCKOUT);
            view.addObject(Constant.B2BSTOCKOUT, b2bStockOutBy);
            if (settingVo.getValue() == 1
                    && (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_DELIVERY_CHALLAN))) {
                view.setViewName("sales/sales-new");
            } else {
                view.setViewName("sales/sales-new");
            }
            List<String> groupNature = new ArrayList<String>();
            if (type.equals(Constant.SALES_INVOICE)) {
                groupNature.add(Constant.ACCOUNT_SALES);
                List<AccountCustomDTO> accountCustomDTO = accountCustomService
                        .findAccountCustomByBranchIdAndGroupNature(
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()), groupNature);
                view.addObject("accountCustomDTO", accountCustomDTO);
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                groupNature.add(Constant.ACCOUNT_SALES_RETURN);
                List<AccountCustomDTO> accountCustomDTO = accountCustomService
                        .findAccountCustomByBranchIdAndGroupNature(
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()), groupNature);
                view.addObject("accountCustomDTO", accountCustomDTO);
            }

            view.addObject(Constant.EXPIRY,
                    companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.EXPIRY));
			view.addObject(Constant.PRODUCTTYPE,
					companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.PRODUCTTYPE));
			view.addObject(Constant.SELLRAWPRODUCTS,
					companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLRAWPRODUCTS));
			view.addObject(Constant.SELLSEMIFINISHEDPRODUCTS,
					companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLSEMIFINISHEDPRODUCTS));
			view.addObject(Constant.SELLPACKAGINGPRODUCTS,
					companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLPACKAGINGPRODUCTS));
			view.addObject(Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING,
					companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()),Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING));
        } else {
            view.setViewName(Constant.ACCESSDENIED);
        }

        try {
            if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE)!=null?session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE):"0").toString()) == 1) {

                int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());
                int userType = Integer.parseInt(session.getAttribute("userType").toString());
                if (userType > Constant.URID_USER) {
                    userType = Integer.parseInt(session.getAttribute("parentUserType").toString());
                }

                String typesenseCollectionName = typesenseService.getCollectionName(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), accountingType, userType, Constant.CONTACT_CUSTOMER);
                log.warning("typesenseCollectionName : "+typesenseCollectionName);
                view.addObject("typesenseCollectionName", typesenseCollectionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @PostMapping("/contact/{id}/json")
    @ResponseBody
    public List<SalesVo> salesListJSON(HttpSession session, @PathVariable(value = "type") String type,
                                       @PathVariable(value = "id") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        List<SalesVo> salesVos = new ArrayList<SalesVo>();

        try {

            List<String> salesTypes = new ArrayList<String>();
            salesTypes.add(Constant.SALES_BILL_OF_SUPPLY);
            salesTypes.add(Constant.SALES_INVOICE);
            // salesTypes.add(Constant.SALES_POS);

            salesVos = salesService.findByTypesAndContactAndBranchIdAndSalesDateBetween(salesTypes,
                    Long.parseLong(session.getAttribute("branchId").toString()), contactId);
//			System.err.println("Sales Vos Size:" + salesVos.size());
            Collections.reverse(salesVos);

            salesVos.forEach(salesItem -> {
                if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    // log.info("sales");
                    salesItem.getSalesItemVos().forEach(q -> {
                        q.getProductVarientsVo().setProductVo(null);
                        q.setSalesVo(null);
                        q.setTaxVo(null);
                    });
                } else {
                    salesItem.setSalesItemVos(null);
                }
                salesItem.setSalesVo(null);
                salesItem.setSalesAdditionalChargeVos(null);
                salesItem.setContactVo(null);

            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return salesVos;
    }

    /*
     * @GetMapping("/{id}/barcode/{barcode}/json")
     *
     * @ResponseBody public List<SalesVo> salesProductDetailsJSON(HttpSession
     * session, @PathVariable(value="type")String type, @PathVariable(value="type")
     * String barcode, @PathVariable(value="id") long contactId) {
     *
     * DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     *
     * List<SalesVo> salesVos = new ArrayList<SalesVo>();
     *
     * try {
     *
     * List<String> salesTypes = new ArrayList<String>();
     * salesTypes.add(Constant.SALES_BILL_OF_SUPPLY);
     * salesTypes.add(Constant.SALES_INVOICE); salesTypes.add(Constant.SALES_POS);
     *
     * salesVos =
     * salesService.findByTypesAndContactAndBranchIdAndSalesDateBetween(salesTypes,
     * Long.parseLong(session.getAttribute("branchId").toString()),
     * dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
     * dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()),
     * contactId);
     *
     * Collections.reverse(salesVos);
     *
     * salesVos.forEach( salesItem -> { salesItem.setSalesItemVos(null);
     * salesItem.setSalesAdditionalChargeVos(null); salesItem.setContactVo(null);
     * }); } catch (NumberFormatException | ParseException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); }
     *
     * return salesVos; }
     */

    @GetMapping("/{id}")
    public ModelAndView salesDetails(HttpSession session, @PathVariable(value = "type") String type,
                                     @PathVariable(value = "id") long id,
                                     @RequestParam(name = "errorMessage", required = false, defaultValue = "") String errorMessage)
            throws Exception {
        String rateLimitType ;
        // Check if the user has permission for E-Waybill for the current module (e.g., Invoice)
        boolean hasEwaybillPermission = false;
        boolean hasEInvoicePermission = false;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_VIEW;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_VIEW;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_VIEW;
                hasEwaybillPermission = (MenuPermission.havePermission(session,type, Constant.EWAY_ACTION)==1);
                hasEInvoicePermission = (MenuPermission.havePermission(session,type, Constant.EINVOICE_ACTION)==1);
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_VIEW;
                hasEwaybillPermission = (MenuPermission.havePermission(session,type, Constant.EWAY_ACTION)==1);
                hasEInvoicePermission = (MenuPermission.havePermission(session,type, Constant.EINVOICE_ACTION)==1);
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_VIEW;
                hasEwaybillPermission = (MenuPermission.havePermission(session,type, Constant.EWAY_ACTION)==1);
                hasEInvoicePermission = (MenuPermission.havePermission(session,type, Constant.EINVOICE_ACTION)==1);
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_VIEW;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView();
        long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
        String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
        // Check if valid using the existing method
        boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
        view.addObject("isValidMerchantType", isValidMerchantType);
        int result = 0;
        boolean isVatUser = false;
        if(userRepository.getTaxType(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())).equals(Constant.VAT)){
            isVatUser = true;
        }
        view.addObject("isVatUser",isVatUser);

        view.addObject("ewaybillflag", hasEwaybillPermission);
        view.addObject("einvoiceflag", hasEInvoicePermission);

        CompanySettingVo allowFeedBackResponse = companySettingService.findByCompanyIdAndType(Long.valueOf(session.getAttribute("companyId").toString()),
                Constant.ALLOWFEEDBACK);
        view.addObject(Constant.ALLOWFEEDBACK, allowFeedBackResponse!=null?allowFeedBackResponse.getValue():new CompanySettingVo());
        view.addObject("feedBackPermission", MenuPermission.havePermission(session, Constant.FEEDBACK, Constant.VIEW));

        List<Map<String, String>> feedBackDetails = feedBackService.findBySalesId(id);
        view.addObject("feedbackid", feedBackDetails.get(0).get("feedbackid"));
        view.addObject("rating", feedBackDetails.get(0).get("rating"));
        view.addObject("comment", feedBackDetails.get(0).get("comment"));
        view.addObject("recomended", feedBackDetails.get(0).get("recomended"));
        if (Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_COMPANY) {
            // log.info("Inside Contact Vos usertype == 2 >>>>>");
            result = salesService.countBySalesIdAndCompanyIdAndIsDeleted(id,
                    Long.parseLong(session.getAttribute("companyId").toString()), 0);
            // log.warning("result is >>>>" + result);

        } else {

            // log.info("Inside Contact Vos else >>>>>>");
            if (Integer.parseInt(session.getAttribute("userType").toString()) > Constant.URID_USER) {
                // log.info("Inside Contact Vos usertype > 3 >>>");
                UserFrontVo userFrontVo = userService
                        .findByUserFrontId(Long.parseLong(session.getAttribute("userId").toString()));
                if ((userFrontVo.getUserFrontVo().getRoles().get(0).getUserRoleId()) == Constant.URID_COMPANY) {
                    // log.info("Inside Contact Vos usertype == 2 >>>");
                    result = salesService.countBySalesIdAndCompanyIdAndIsDeleted(id,
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
                } else {
                    // log.info("Inside Contact Vos branch else >>>");
                    result = salesService.countBySalesIdAndCompanyIdAndIsDeleted(id,
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
                }
            } else {
                // log.info("Inside Contact Vos branch else >>><<<<");
                result = salesService.countBySalesIdAndCompanyIdAndIsDeleted(id,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
            }
        }
        if (result == 0) {
            view.setViewName(Constant.ERROR_PAGE_404);
        } else {
            String salestype = type;
            if (type.equals(Constant.SALES_ORDER)) {
                salestype = "salesorder";
            }
            if (MenuPermission.havePermission(session, salestype, Constant.VIEW) == 1) {
                SalesViewDTO salesVo = salesService.getBySalesIdAndCompanyId(id,
                        Long.parseLong(session.getAttribute("companyId").toString()));
                view.addObject("type", type);
                String typeForPrint = "";
                if (type.equals(Constant.SALES_INVOICE)) {
                    view.addObject("displayType", "Invoice");
                    view.addObject("errorMessage", errorMessage);
                    typeForPrint = Constant.SALES_INVOICE;
                } else if (type.equals(Constant.SALES_ESTIMATE)) {
                    view.addObject("displayType", "Estimate");
                    typeForPrint = Constant.SALES_ESTIMATE;
                } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
                    view.addObject("displayType", "Bill Of Supply");
                    typeForPrint = Constant.SALES_BILL_OF_SUPPLY;
                } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    view.addObject("displayType", "Credit Note");
                    typeForPrint = Constant.SALES_CREDIT_NOTE;
                } else if (type.equals(Constant.SALES_ORDER)) {
                    view.addObject("displayType", "Sales Order");
                    typeForPrint = Constant.SALES_ORDER;
                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    view.addObject("displayType", "Delivery Challan");
                    typeForPrint = Constant.SALES_DELIVERY_CHALLAN;
                }
                view.addObject(Constant.EWAYBILL, companySettingService.findByBranchIdAndType(
                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.EWAYBILL));
                view.addObject(Constant.ALLOWEINVOICE, companySettingService.findByBranchIdAndType(
                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.ALLOWEINVOICE));
                view.addObject("isExport", MenuPermission.havePermission(session, salestype, Constant.PDF_EXCEL_PRINT));
                if (salesVo == null || salesVo.getIsDeleted() == 1) {
                    view.setViewName("accessdenied/datanotavailbal");
                } else {
                    // Initialize these variables outside the if block
                    boolean hasEwayBill = false;
                    boolean hasEInvoice = false;
                    int invoiceCount = 0;
                    boolean dcHasEwayBill = false; // New variable
                    boolean isDCLinkedToInvoice = false; // New variable for checking if linked invoice has e-way bill
                    boolean cnHasEInvoice = false;
                    boolean invoiceHasCNWithEInvoice = false;
                    // Only process if it's a valid sales document type
                    if (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_ORDER)
                            || type.equals(Constant.SALES_DELIVERY_CHALLAN) || type.equals(Constant.SALES_CREDIT_NOTE)) {

                        List<SalesMappingVo> salesMappingVos = salesMappingRepository.findByMainSalesId(id);
                        view.addObject("salesMappingVos", salesMappingVos);

                        switch (type) {
                            case Constant.SALES_DELIVERY_CHALLAN:
                                // Check if DC is linked to invoice with e-way bill
                                List<SalesMappingVo> dcLinkedInvoices = salesMappingRepository
                                        .findByParentSalesIdAndMainSalesType(id, Constant.SALES_INVOICE);

                                for (SalesMappingVo mapping : dcLinkedInvoices) {
//                                    SalesVo invoice = salesRepository.findById(mapping.getParentSalesId()).orElse(null);
                                    EwayDTO invoice = salesRepository.findEwayBillNoById(mapping.getParentSalesId());
                                    if (invoice != null && invoice.getEwayBillNo() != 0) {
                                        isDCLinkedToInvoice = true;
                                        break;
                                    }
                                }

                                // Count invoices and check for e-way bills
                                for (SalesMappingVo mapping : salesMappingVos) {
                                    if (Constant.SALES_INVOICE.equals(mapping.getParentSalesType())) {
                                        invoiceCount++;
//                                        SalesVo invoice = salesRepository.findById(mapping.getParentSalesId()).orElse(null);
                                        EwayDTO invoice = salesRepository.findEwayBillNoById(mapping.getParentSalesId());
                                        if (invoice != null && invoice.getEwayBillNo() != 0) {
                                            hasEwayBill = true;
                                            break;
                                        }
                                    }
                                }
                                break;

                            case Constant.SALES_CREDIT_NOTE:
                                // Check if CN is linked to invoice with e-invoice
                                List<SalesMappingVo> cnLinkedInvoices = salesMappingRepository
                                        .findByParentSalesIdAndMainSalesType(id, Constant.SALES_INVOICE);

                                for (SalesMappingVo mapping : cnLinkedInvoices) {
//                                    SalesVo invoice = salesRepository.findById(mapping.getParentSalesId()).orElse(null);
                                    IrnDTO invoice = salesRepository.findIrnById(mapping.getParentSalesId());
                                    if (invoice != null && !StringUtils.isEmpty(invoice.getIrnNo())) {
                                        cnHasEInvoice = true;
                                        break;
                                    }
                                }

                                // Check for e-invoices in parent sales
                                for (SalesMappingVo mapping : salesMappingVos) {
                                    if (Constant.SALES_INVOICE.equals(mapping.getParentSalesType())) {
//                                        SalesVo invoice = salesRepository.findById(mapping.getParentSalesId()).orElse(null);
                                        IrnDTO invoice = salesRepository.findIrnById(mapping.getParentSalesId());
                                        if (invoice != null && invoice.getIrnNo() != null) {
                                            hasEInvoice = true;
                                            break;
                                        }
                                    }
                                }
                                break;

                            case Constant.SALES_INVOICE:
                                // Check linked documents
                                List<SalesMappingVo> linkedDocs = salesMappingRepository.findByParentSalesId(id);

                                // Check DCs for e-way bills
                                for (SalesMappingVo doc : linkedDocs) {
                                    if (Constant.SALES_DELIVERY_CHALLAN.equals(doc.getMainSalesType())) {
//                                        SalesVo dc = salesRepository.findById(doc.getMainSalesId()).orElse(null);
                                        EwayDTO dc = salesRepository.findEwayBillNoById(doc.getMainSalesId());
                                        if (dc != null && dc.getEwayBillNo() != 0) {
                                            dcHasEwayBill = true;
                                            break;
                                        }
                                    }
                                }

                                // Check CNs for e-invoices
                                for (SalesMappingVo doc : linkedDocs) {
                                    if (Constant.SALES_CREDIT_NOTE.equals(doc.getMainSalesType())) {
//                                        SalesVo cn = salesRepository.findById(doc.getMainSalesId()).orElse(null);
                                        IrnDTO cn = salesRepository.findIrnById(doc.getMainSalesId());
                                        if (cn != null && !StringUtils.isEmpty(cn.getIrnNo())) {
                                            invoiceHasCNWithEInvoice = true;
                                            break;
                                        }
                                    }
                                }
                                break;
                        }
                    }
                    // Add these objects to the view outside the if block
                    view.addObject("hasEwayBill", hasEwayBill);
                    view.addObject("invoiceCount", invoiceCount);
                    view.addObject("dcHasEwayBill", dcHasEwayBill); // Add this to view
                    view.addObject("isDCLinkedToInvoice",isDCLinkedToInvoice);
                    view.addObject("cnHasEInvoice", cnHasEInvoice);
                    view.addObject("hasEInvoice", hasEInvoice);
                    view.addObject("invoiceHasCNWithEInvoice", invoiceHasCNWithEInvoice);
                    int approvalFlag = salesRepository.getStockTransferApprovalFlag(id, Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), type);
//                    if(type.equals(Constant.SALES_INVOICE)|| type.equals(Constant.SALES_DELIVERY_CHALLAN)){
                    view.addObject("approvalFlag", approvalFlag);
//                    }
                    view.addObject("salesAdditionalChargeVos", salesService.findSalesAdditionalChargeDetails(id));
                    double remainingQty = 0;
                    try {
                        remainingQty = salesService.findRemainingQtyBySalesId(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    view.addObject("remainingQty", remainingQty);
                    CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(
                            Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESGARMENTDESIGN);

                    view.setViewName("sales/sales-view");

                    if (!salesVo.getTermsAndConditionIds().equals("")) {
                        List<Long> termandconditionIds = Arrays
                                .asList(salesVo.getTermsAndConditionIds().split("\\s*,\\s*")).stream()
                                .map(Long::parseLong).collect(Collectors.toList());

                        view.addObject("TermsAndCondition",
                                termsAndConditionService.getTermAndConditionList(termandconditionIds));
                    }
                    ReportSettingVo setting = reportService.findByTypeAndBranchId(typeForPrint,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                    if (setting != null && setting.getReportVo() != null)
                        view.addObject("reportPdfType", setting.getReportVo().getReportFormateType());
                    view.addObject(Constant.CUSTOMERNAME,
                            companySettingService
                                    .findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()),
                                            Constant.CUSTOMERNAME)
                                    .getValue());

                    double totalTaxAmount = salesService.getTotalTaxAmountBySalesId(id);
                    double productSubTotal = salesService.getProductSubTotalBySalesId(id);
                    Map totalQty = salesService.getActualQtyAndFreeQtyBySalesId(id);
                    double additionalSubTotal = salesService.getAdditionalSubTotalBySalesId(id);
                    double totalAmount = productSubTotal + additionalSubTotal;
                    double netAmount = totalAmount + salesVo.getRoundoff();

                    view.addObject("netAmount", netAmount);
                    view.addObject("totalAmount", totalAmount);
                    view.addObject("totalQty",totalQty.get("totalQuantity"));
                    view.addObject("totalFreeQty",totalQty.get("totalFreeQuantity"));
                    view.addObject("invoiceFreeQty",totalQty.get("invoiceFreeQuantity"));
                    view.addObject("invoiceQty",totalQty.get("invoiceQuantity"));
                    view.addObject("dcFreeQty",totalQty.get("dcFreeQuantity"));
                    view.addObject("dcQty",totalQty.get("dcQuantity"));
                    view.addObject("partialQty",totalQty.get("partialQuantity"));
                    view.addObject("partialFreeQty",totalQty.get("partialFreeQuantity"));
                    view.addObject("productSubTotal", productSubTotal);
                    view.addObject("totalTaxAmount", totalTaxAmount);
                    view.addObject("salesVo", salesVo);
                    view.addObject("total", salesRepository.getProductTotalBySalesId(salesVo.getSalesId()));
                    view.setViewName("sales/sales-view");

                    boolean isEditableFlag = true;
                    if (StringUtils.isNotBlank(salesVo.getStatus())){
                        if(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()) == Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()) &&
                                (merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER || MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId)) &&
                                salesVo.getStatus().equals(Constant.INVOICED) && (StringUtils.isBlank(salesVo.getParentType())) && salesVo.getStockTransferId() == 0){
                            isEditableFlag = false;
                        }
                    }
                    view.addObject("isEditableFlag", isEditableFlag);
                }
            } else {
                view.setViewName(Constant.ACCESSDENIED);
            }
        }
        view.addObject("contactlist", contactService.findByType(Constant.CONTACT_TRANSPORT,
                Long.parseLong(session.getAttribute("branchId").toString())));

        return view;
    }

    @PostMapping("/checksalesno")
    @ResponseBody
    public String checksalesno(@RequestParam Map<String, String> allRequestParam,
                               @RequestParam(defaultValue = "0", value = "salesId") long salesId,
                               @PathVariable(value = "type") String type, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CHECK_NO;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECK_NO;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CHECK_NO;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CHECK_NO;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CHECK_NO;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECK_NO;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long salesNo = 0;
        try {
            salesNo = Long.parseLong(allRequestParam.get("salesNo"));
            // log.warning("salesNo-------->"+salesNo);
        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"valid\": false }";
        }

        List<SalesVo> b = new ArrayList<>();
        String defaultPrefix = "";
        if (type.equals(Constant.SALES_INVOICE)) {
            defaultPrefix = "INV";
        } else if (type.equals(Constant.SALES_ESTIMATE)) {
            defaultPrefix = "EST";
        } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
            defaultPrefix = "BOS";
        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
            defaultPrefix = "CRD";
        } else if (type.equals(Constant.SALES_ORDER)) {
            defaultPrefix = "ORD";
        }
        String salestype = type;
        if (type.equals(Constant.SALES_ORDER)) {
            salestype = "salesorder";
        }
        String prefix = prefixService
                .getPrefixByPrefixTypeAndBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), salestype, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

        if (StringUtils.isNotBlank(prefix)) {
            defaultPrefix = prefix;
        }

        if (salesId == 0) {
            // System.err.println("-----*******checkitemcode
            // NEWWW**********------------------"+itemcode);
            // b=productService.findByitemCodeIgnoreCaseAndCompanyId(itemcode,Long.parseLong(session.getAttribute("companyId").toString()));
            b = salesService.checkSalesNoExist(salesNo,
                    Long.parseLong(session.getAttribute("branchId").toString()), type, defaultPrefix);
        } else {
            // System.err.println("-----*******checkitemcode
            // UPADETEEE**********------------------"+itemcode);
            b = salesService.checkSalesNoExistWithSalesId(salesNo, salesId,
                    Long.parseLong(session.getAttribute("branchId").toString()), type, defaultPrefix);
        }

        if (b.size() == 0) {
            return "{ \"valid\": true }";
        } else {
            return "{ \"valid\": false }";
        }
    }

    @PostMapping("/{id}/barcodedetails")
    @ResponseBody
    public List<SalesItemVo> salesBarcodeDeatils(HttpSession session, @PathVariable(value = "type") String type,
                                                 @PathVariable(value = "id") long id) throws Exception {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_BARCODE_DETAILS;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_BARCODE_DETAILS;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_BARCODE_DETAILS;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_BARCODE_DETAILS;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_BARCODE_DETAILS;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_BARCODE_DETAILS;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(id,
                Long.parseLong(session.getAttribute("branchId").toString()));
        // if(salesVo.getSalesItemVos().)
        if (salesVo != null) {
            salesVo.getSalesItemVos().forEach(p -> {

                // p.getBarcodeVo().setProductVarientsVo(null);
                // p.getBarcodeVo().setStockTransactionVo(null);
                p.setProductVarientsVo(null);
                p.setSalesVo(null);
                p.setTaxVo(null);
            });
            return salesVo.getSalesItemVos();
        } else {
            return new ArrayList<>();
        }

    }

    @GetMapping("/{id}/edit")
    public ModelAndView salesEditOld(HttpSession session, @PathVariable(value = "type") String type,
                                     @PathVariable(value = "id") long id, RedirectAttributes model) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_EDIT;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_EDIT;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_EDIT;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_EDIT;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_EDIT;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_EDIT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView("sales/sales-edit");
        view.addObject("isPercentageDiscount", companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.BATCHDISCOUNTVALUE).getValue());
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        int result = salesService.countBySalesIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (result == 0) {
            view.setViewName(Constant.ERROR_PAGE_404);
        } else {
            String salestype = type;
            if (type.equals(Constant.SALES_ORDER)) {
                salestype = "salesorder";
            }
            DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            view.addObject("serverdate", dateFormat2.format(date));
            if (MenuPermission.havePermission(session, salestype, Constant.EDIT) == 1) {
                view.addObject("paymentTermInsertPermission", MenuPermission.havePermission(session, Constant.PAYMENTTERMS, Constant.INSERT));
                view.addObject("type", type);
                long merchantTypeId = Long.parseLong(session.getAttribute("merchantTypeId").toString());
                String clusterId = session.getAttribute("clusterId").toString();
                // Check if valid using the existing method
                boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
                view.addObject("isValidMerchantType", isValidMerchantType);
                int taxVal = 0;
                if (session.getAttribute("governmentTaxType").toString().equals(Constant.VAT)) {
                    taxVal = 1;
                }
                if (type.equals(Constant.SALES_INVOICE)) {
                    view.addObject("displayType", "Invoice");
                } else if (type.equals(Constant.SALES_ESTIMATE)) {
                    view.addObject("displayType", "Estimate");
                } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
                    view.addObject("displayType", "Bill Of Supply");
                } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    view.addObject("displayType", "Credit Note");
                } else if (type.equals(Constant.SALES_ORDER)) {
                    view.addObject("displayType", "Sales Order");
                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    view.addObject("displayType", "Delivery Challan");
                }
                view.addObject(Constant.SALESALLOWFOCUSON,
                        companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.SALESALLOWFOCUSON));
                log.info("BEFORE SALES FIND");
                SalesVo salesVo = salesService.findBySalesIdAndBranchId(id,
                        Long.parseLong(session.getAttribute("branchId").toString()));
                if (salesVo.getIsDeleted() == 1) {
                    view.setViewName("accessdenied/datanotavailbal");
                }else if (ewayBillOrEInvoiceGenerated(salesVo,model,Constant.EDIT_ACTION,type)){
                    return new ModelAndView("redirect:/sales/" + type + "/" + id);
                } else {
                    if (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_ORDER) || type.equals(Constant.SALES_DELIVERY_CHALLAN) || type.equals(Constant.SALES_ESTIMATE)) {
                        List<SalesMappingVo> salesMappingVos = salesMappingRepository.findByMainSalesId(id);
                        salesVo.setSalesMappingVos(salesMappingVos);
                    }
                    try {
                        salesVo.getSalesItemVos().forEach(s -> {
                            s.setSalesmanName(employeeRepository.findEmployeeName(s.getSalesmanId()));
                            int isExpirySellable = productRepository.findExpiryProductVarient(s.getSalesItemId());
//	            			System.out.println(isExpirySellable+"=========");
                            s.setExpirySee(isExpirySellable);
                            String parentSalesItemIds = s.getParentSalesItemIds();
                            if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                        .map(Long::parseLong).collect(Collectors.toList());
                                if (!parentItemIds.isEmpty()) {
                                    BigDecimal orderQty = BigDecimal.ZERO;
                                    BigDecimal orderFreeQty = BigDecimal.ZERO;
                                    for (int i = 0; i < parentItemIds.size(); i++) {
                                        long parentSalesItemId = parentItemIds.get(i);
                                        // log.warning("parentSalesItemId--->"+parentSalesItemId);
                                        SalesItemQtyDTO salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                                        if (salesItemQtyDTO != null) {
                                            orderQty = orderQty.add(new BigDecimal(String.valueOf(salesItemQtyDTO.getQty())));
                                            orderFreeQty = orderFreeQty.add(new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty())));
                                        }
                                    }
                                    s.setOrderQty(orderQty.doubleValue());
                                    s.setOrderFreeQty(orderFreeQty.doubleValue());
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    salesVo.setShippingCountriesName(
                            countryService.findByCountriesCode(salesVo.getShippingCountriesCode()).getCountriesName());
                    salesVo.setShippingStateName(
                            stateService.findByStateCode(salesVo.getShippingStateCode()).getStateName());
                    salesVo.setShippingCityName(cityService.findByCityCode(salesVo.getShippingCityCode()).getCityName());

                    salesVo.setBillingCountriesName(
                            countryService.findByCountriesCode(salesVo.getBillingCountriesCode()).getCountriesName());
                    salesVo.setBillingStateName(stateService.findByStateCode(salesVo.getBillingStateCode()).getStateName());
                    salesVo.setBillingCityName(cityService.findByCityCode(salesVo.getBillingCityCode()).getCityName());

                    if (!salesVo.getTermsAndConditionIds().equals("")) {
                        List<Long> termandconditionIds = Arrays.asList(salesVo.getTermsAndConditionIds().split("\\s*,\\s*"))
                                .stream().map(Long::parseLong).collect(Collectors.toList());

                        view.addObject("TermsAndCondition",
                                termsAndConditionService.getTermAndConditionList(termandconditionIds));
                    }
                    salesVo.getSalesItemVos().forEach(s -> {
                        StockMasterVo stockMasterVo = stockMasterService
                                .findByProductVarientIdAndBranchIdAndYearIntervalAndBatchNo(
                                        s.getProductVarientsVo().getProductVarientId(),
                                        Long.parseLong(session.getAttribute("branchId").toString()),
                                        session.getAttribute("financialYear").toString(), s.getBatchNo());

                        if (stockMasterVo != null) {
                            s.setAvailableQty(stockMasterVo.getQuantity() + s.getQty()+s.getFreeQty());
                        }
                        String qty = stockMasterRepository.findproductVariantQty(
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                s.getProductVarientsVo().getProductVarientId(), session.getAttribute("financialYear").toString());
                        if (qty == null) {
                            qty = "0";
                        }
                        s.setQuantity(qty);

                        if (s.getProductVarientsVo().getProductVo() != null && StringUtils.isNotBlank(s.getProductVarientsVo().getProductVo().getHsnCode())) {
                            s.getProductVarientsVo().getProductVo().setHsnType(hsnTaxMasterService.getHsnTypeByHsnCode(s.getProductVarientsVo().getProductVo().getHsnCode()));
                        }
                    });

                    view.addObject("salesVo", salesVo);
                    String tanNo="";
                    if ((Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_COMPANY) ||
                            (Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_FRANCHISE) || (Integer.parseInt(session.getAttribute("parentUserType").toString()) == Constant.URID_FRANCHISE)) {
                        tanNo = profileService.getTanNo(branchId);
                        view.addObject("tanNo",tanNo);
                    } else {
                        tanNo = profileService.getTanNo(companyId);
                        view.addObject("tanNo",tanNo);
                    }
                    if(StringUtils.isNotBlank(tanNo) && tanNo!=null) {
                        List<Map<String, String>> TCSLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TCS);
                        view.addObject("TCSLedgerlist", TCSLedgerlist);
                    }

                    List<SalesItemVo> reversedList = new ArrayList<>(salesVo.getSalesItemVos());
                    Collections.reverse(reversedList);
                    salesVo.setSalesItemVos(reversedList);
                    view.addObject("salesVo", salesVo);

                    CompanySettingVo garmentIndustryTaxType = companySettingService.findByCompanyIdAndType(
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.GARMENTINDUSTRYTAXTYPE);
                    CompanySettingVo garmentTaxCalculationMethod = companySettingService.findByCompanyIdAndType(
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.GARMENTTAX_CALCULATION_METHOD);

                    CompanySettingVo hsntypewisecalculation = companySettingService.findByCompanyIdAndType(
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.HSNTYPEWISECALCULATION);
                    CompanySettingVo hsntypewisecalculationmethod = companySettingService.findByCompanyIdAndType(
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.HSNTYPEWISECALCULATIONMETHOD);
                    view.addObject("hsntypewisecalculation", hsntypewisecalculation);
                    view.addObject("hsntypewisecalculationmethod", hsntypewisecalculationmethod);
                    view.addObject("garmentIndustryTaxType", garmentIndustryTaxType);
                    view.addObject("garmentTaxCalculationMethod", garmentTaxCalculationMethod);

                    String gstType= userRepository.getTaxTypeByUserFrontId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
                    if (((garmentIndustryTaxType != null && garmentIndustryTaxType.getValue() == 1 )|| (hsntypewisecalculation != null && hsntypewisecalculation.getValue() == 1)) && !gstType.equals(Constant.VAT)) {
                        String taxCode = Constant.GST;
                        int taxType = Constant.TAX_TYPE_GST;
                        try {
                            Map<String, String> gstMap = userRepository.getgstDetails(Long.parseLong(session.getAttribute("companyId").toString()));
                            if (gstMap != null && !gstMap.isEmpty()) {
                                if (StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"), Constant.VAT)) {
                                    taxCode = Constant.VAT;
                                    taxType = Constant.TAX_TYPE_VAT;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String tax_code = "";
                        TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(5, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);
                        view.addObject("tax5name", taxVo.getTaxName());
                        view.addObject("tax5rate", taxVo.getTaxRate());
                        view.addObject("tax5id", taxVo.getTaxId());

                        taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(12, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);
                        view.addObject("tax12name", taxVo.getTaxName());
                        view.addObject("tax12rate", taxVo.getTaxRate());
                        view.addObject("tax12id", taxVo.getTaxId());

                        taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(18, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), taxType, tax_code);
                        view.addObject("tax18name", taxVo.getTaxName());
                        view.addObject("tax18rate", taxVo.getTaxRate());
                        view.addObject("tax18id", taxVo.getTaxId());
                    }
                    view.addObject("contactlist", contactService.findByType(Constant.CONTACT_TRANSPORT, Long.parseLong(session.getAttribute("branchId").toString())));
                    // view.addObject("ProductList",productService.findByCompanyIdAndProductVoIsDeleted(Long.parseLong(session.getAttribute("companyId").toString()),
                    // 0));

                    view.addObject("allowNegativeStock", companySettingService.findByCompanyIdAndType(
                            Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWNEGATIVESTOCK));
                    view.addObject(Constant.CUSTOMERTYPEWISECALCULATION, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERTYPEWISECALCULATION));
                    view.addObject("paymentTermList", paymentTermService
                            .findBybranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), 0,Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
                    List<String> types = new ArrayList<>();
                    types.add(Constant.REPORT_SALES);
                    if (salesVo.getSalesAdditionalChargeVos().size() > 0) {
                        view.addObject("AdditionalChargeVos", additionalChargeService.
                                getAdditionalChargesByBranchIdAndCompanyIdAndTypesAndIsFranchise(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()),types,1));

                    }
                    view.addObject(Constant.ADDNEWLINEINSALES, companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.ADDNEWLINEINSALES).getValue());
                }

                CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESGARMENTDESIGN);
                view.addObject(Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.ALLOWCUSTOMERWISEPRODUCTMAPPING).getValue() != 1 ? 0 : 1);
                view.addObject(Constant.ALLOWDUPLICATEPRODUCTINSALES, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.ALLOWDUPLICATEPRODUCTINSALES).getValue());
                view.addObject(Constant.ALLOWROUNDOFF, companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWROUNDOFF));
                view.addObject(Constant.MULTIBARCODE, companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.MULTIBARCODE));
                view.addObject(Constant.MULTIDUPLICATEBARCODE, companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.MULTIDUPLICATEBARCODE));
                view.addObject(Constant.CUSTOMERNAME, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERNAME).getValue());
                view.addObject(Constant.LASTSALESMRP, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.LASTSALESMRP));
                CompanySettingVo b2bStockOutBy = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.B2BSTOCKOUT);
                view.addObject(Constant.B2BSTOCKOUT, b2bStockOutBy);
                view.addObject(Constant.DEFAULTSALESQTYBLANK,
                        companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.DEFAULTSALESQTYBLANK).getValue());
                List<TaxVo> taxVos = taxService.findByCompanyId(Long.parseLong(session.getAttribute("companyId").toString()), merchantTypeId, clusterId, taxVal);
                view.addObject("tax", taxVos);
                Boolean billRestriction = false;
                String value = companySettingService.getValueByTypeAndBranchId(Long.parseLong(session.getAttribute("branchId").toString()), Constant.BILLRESTRICTIONANDWARNING);
                if (StringUtils.isNotBlank(value) && value.equals("1")) {
                    value = companySettingService.getValueByTypeAndBranchId(Long.parseLong(session.getAttribute("branchId").toString()), Constant.FORUSERTYPE);
                    if (StringUtils.isNotBlank(value) && !value.equals("2")) {
                        billRestriction = true;
                    }
                }
                view.addObject("billRestriction", billRestriction);
                if (settingVo.getValue() == 1 && (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_DELIVERY_CHALLAN))) {
                    view.setViewName("sales/sales-edit");
                } else {
                    view.setViewName("sales/sales-edit");
                }
                if (StringUtils.isNotBlank(salesVo.getFlatDiscountType()) && !StringUtils.equals(salesVo.getFlatDiscountType(), Constant.PERCENTAGE)) {
                    Map<String, Object> salesFlatDiscountData = salesService.calculateFlatDiscountPercentageFromAmount(salesVo.getSalesId());
                    if(!salesFlatDiscountData.isEmpty()){
                        view.addObject("flatDiscountInPercentage", Double.parseDouble(salesFlatDiscountData.get("flatDiscountPercentage").toString()));
                    }
                } else {
                    view.addObject("flatDiscountInPercentage", salesVo.getFlatDiscount());
                }
                if (Integer.parseInt(session.getAttribute("jioType").toString()) != Constant.JIO_TYPE_AJIO_WHOLESALER) {
                    CompanySettingVo flatDiscountLimit = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESFLATDISCOUNTLIMIT);
                    CompanySettingVo flatDiscountLimitInAmount = companySettingService.findByBranchIdAndType(
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.SALESFLATDISCOUNTLIMITINAMOUNT);
                    if(StringUtils.isNotBlank(flatDiscountLimitInAmount.getAddValue())){
                        view.addObject(Constant.SALESFLATDISCOUNTLIMITINAMOUNT, flatDiscountLimitInAmount.getAddValue().trim());
                    }else{
                        view.addObject(Constant.SALESFLATDISCOUNTLIMITINAMOUNT, "");
                    }
                    if (StringUtils.isNoneEmpty(flatDiscountLimit.getAddValue())) {
                        try {
                            view.addObject(Constant.FLATDISCOUNTLIMIT,
                                    Double.parseDouble(flatDiscountLimit.getAddValue().trim()));
                            view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                        } catch (Exception e) {
                            view.addObject(Constant.FLATDISCOUNTLIMIT, 100);
                            view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                        }

                    } else {
                        view.addObject(Constant.FLATDISCOUNTLIMIT, 100);
                        view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, 0);
                    }
                } else {
                    CompanySettingVo flatDiscountLimit = companySettingService.findByType(Constant.AJIOWHOLSELLERDISCOUNTLIMIT);
                    view.addObject(Constant.FLATDISCOUNTLIMIT, Double.parseDouble(flatDiscountLimit.getAddValue().trim()));
                    CompanySettingVo ajioWholseller = companySettingService.findByType(Constant.AJIOWHOLSELLERCASHDISCOUNT);
                    view.addObject(Constant.AJIOWHOLSELLERCASHDISCOUNT, Double.parseDouble(ajioWholseller.getAddValue().trim()));
                }

                List<String> groupNature = new ArrayList<String>();
                if (type.equals(Constant.SALES_INVOICE)) {
                    groupNature.add(Constant.ACCOUNT_SALES);
                    List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(
                            Long.parseLong(session.getAttribute("companyId").toString()),
                            Long.parseLong(session.getAttribute("branchId").toString()), groupNature);
                    view.addObject("accountCustomDTO", accountCustomDTO);
                } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    groupNature.add(Constant.ACCOUNT_SALES_RETURN);
                    List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(
                            Long.parseLong(session.getAttribute("companyId").toString()),
                            Long.parseLong(session.getAttribute("branchId").toString()), groupNature);
                    view.addObject("accountCustomDTO", accountCustomDTO);
                }
            } else {
                view.setViewName(Constant.ACCESSDENIED);
            }
        }
		view.addObject(Constant.PRODUCTTYPE,
				companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.PRODUCTTYPE));
		view.addObject(Constant.SELLRAWPRODUCTS,
				companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLRAWPRODUCTS).getValue());
		view.addObject(Constant.SELLSEMIFINISHEDPRODUCTS,
				companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLSEMIFINISHEDPRODUCTS).getValue());
		view.addObject(Constant.SELLPACKAGINGPRODUCTS,
				companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SELLPACKAGINGPRODUCTS).getValue());

        try {
            if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE) != null ? session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE) : "0").toString()) == 1) {

                int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());
                int userType = Integer.parseInt(session.getAttribute("userType").toString());
                if (userType > Constant.URID_USER) {
                    userType = Integer.parseInt(session.getAttribute("parentUserType").toString());
                }

                String typesenseCollectionName = typesenseService.getCollectionName(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), accountingType, userType, Constant.CONTACT_CUSTOMER);
                log.warning("typesenseCollectionName : " + typesenseCollectionName);
                view.addObject("typesenseCollectionName", typesenseCollectionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private boolean ewayBillOrEInvoiceGenerated(SalesVo salesVo, RedirectAttributes model,String operation,String type) {
        String orderNo = StringUtils.defaultIfEmpty(salesVo.getOrderNo(), salesVo.getPrefix() + salesVo.getSalesNo()).trim();
        String action =  Constant.EDIT_ACTION.equals(operation) ? Constant.EDIT_ACTION : Constant.ISDELETE;

        if (salesVo.getEwayBillNo() != 0) {
            model.addFlashAttribute("ewaybilloreinvoicemsg", "Please cancel E-way bill to " + action + ": " + orderNo);
            return true;
        } else if (StringUtils.isNotBlank(salesVo.getIrnNo())) {
            if (Constant.EDIT_ACTION.equals(action)) {
//                model.addFlashAttribute("ewaybilloreinvoicemsg", "Editing of " + orderNo + " is restricted since the e-invoice has been generated. You can only edit the shipping details.");
                if (Constant.SALES_CREDIT_NOTE.equalsIgnoreCase(type)) {
                    model.addFlashAttribute("ewaybilloreinvoicemsg",
                            "Editing of " + orderNo + " is restricted since the e-invoice has been generated.");
                } else {
                    model.addFlashAttribute("ewaybilloreinvoicemsg",
                            "Editing of " + orderNo + " is restricted since the e-invoice has been generated. You can only edit the shipping details.");
                }
            } else {
                model.addFlashAttribute("ewaybilloreinvoicemsg", "The invoice " + orderNo + " cannot be deleted once the E-invoice has been issued");
            }
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/creategarment")
    public String insertSalesForGarment(@RequestParam Map<String, String> allRequestParams,
                                        @PathVariable(value = "type") String type, @ModelAttribute("salesVo") SalesVo salesVo, HttpSession session,
                                        HttpServletRequest servletRequest) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_GARMENT_CREATE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_GARMENT_CREATE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_GARMENT_CREATE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_GARMENT_CREATE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_GARMENT_CREATE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_GARMENT_CREATE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        long merchantTypeId = Long.parseLong(session.getAttribute("merchantTypeId").toString());
        String clusterId = session.getAttribute("clusterId").toString();
        int isEdit = (salesVo.getSalesId() == 0) ? 0 : 1;
        ContactAddressVo contactAddressVo;

        salesVo.setType(type);
        if (type.equals(Constant.SALES_ORDER)) {
            if (salesVo.getSalesId() == 0L) {
                salesVo.setStatus("In Progress");
            }
        } else {
            salesVo.setStatus("open");
        }

        if (salesVo.getSalesVo() != null && salesVo.getSalesVo().getSalesId() != 0) {
            if (type.equals(Constant.SALES_ORDER)) {
                salesService.updateStatusBySalesId(Constant.ORDER_CREATED, salesVo.getSalesVo().getSalesId(),
                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
            } else if (type.equals(Constant.SALES_INVOICE)) {
//                System.err.println("type-----------------"+type);
                salesService.updateStatusBySalesId(Constant.INVOICE_CREATED, salesVo.getSalesVo().getSalesId(),
                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
            }
        }


        salesVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
        salesVo.setModifiedOn(CurrentDateTime.getCurrentDate());
        salesVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
        salesVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            salesVo.setSalesDate(dateFormat.parse(allRequestParams.get("salesDate")));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            salesVo.setShippingDate(dateFormat.parse(allRequestParams.get("shippingDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            salesVo.setTransportDate(dateFormat.parse(allRequestParams.get("transportDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            if (allRequestParams.get("dueDate") != null)
                salesVo.setDueDate(dateFormat.parse(allRequestParams.get("dueDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            if (!salesVo.getTermsAndConditionIds().equals("")) {
                salesVo.setTermsAndConditionIds(
                        salesVo.getTermsAndConditionIds().substring(0, salesVo.getTermsAndConditionIds().length() - 1));
            } else {

            }
        } catch (Exception e) {
        }

        SalesVo salesVo2 = null;

        if (allRequestParams.get("billingAddressId").equals("0")
                || allRequestParams.get("shippingAddressId").equals("0")) {
            salesVo2 = salesService.findBySalesIdAndBranchId(salesVo.getSalesId(), salesVo.getBranchId());
        }

        // --------------Set Billing Address Details ------------------------
        if (!allRequestParams.get("billingAddressId").equals("0")) {
            contactAddressVo = contactService
                    .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));

            salesVo.setBillingAddressLine1(contactAddressVo.getAddressLine1());
            salesVo.setBillingAddressLine2(contactAddressVo.getAddressLine2());
            salesVo.setBillingCityCode(contactAddressVo.getCityCode());
            salesVo.setBillingCompanyName(contactAddressVo.getCompanyName());
            salesVo.setBillingCountriesCode(contactAddressVo.getCountriesCode());
            salesVo.setBillingFirstName(contactAddressVo.getFirstName());
            salesVo.setBillingLastName(contactAddressVo.getLastName());
            salesVo.setBillingPinCode(contactAddressVo.getPinCode());
            salesVo.setBillingStateCode(contactAddressVo.getStateCode());
        } else if (salesVo2 != null) {
            salesVo.setBillingAddressLine1(salesVo2.getBillingAddressLine1());
            salesVo.setBillingAddressLine2(salesVo2.getBillingAddressLine2());
            salesVo.setBillingCityCode(salesVo2.getBillingCityCode());
            salesVo.setBillingCompanyName(salesVo2.getBillingCompanyName());
            salesVo.setBillingCountriesCode(salesVo2.getBillingCountriesCode());
            salesVo.setBillingFirstName(salesVo2.getBillingFirstName());
            salesVo.setBillingLastName(salesVo2.getBillingLastName());
            salesVo.setBillingPinCode(salesVo2.getBillingPinCode());
            salesVo.setBillingStateCode(salesVo2.getBillingStateCode());
        }

        // --------------Set Shipping Address Details ------------------------
        if (!allRequestParams.get("shippingAddressId").equals("0")) {
            contactAddressVo = contactService
                    .findByContactAddressId(Long.parseLong(allRequestParams.get("shippingAddressId")));

//            salesVo.setShippingAddressLine1(contactAddressVo.getAddressLine1());
//            salesVo.setShippingAddressLine2(contactAddressVo.getAddressLine2());
//            salesVo.setShippingCityCode(contactAddressVo.getCityCode());
//            salesVo.setShippingCompanyName(contactAddressVo.getCompanyName());
//            salesVo.setShippingCountriesCode(contactAddressVo.getCountriesCode());
//            salesVo.setShippingFirstName(contactAddressVo.getFirstName());
//            salesVo.setShippingLastName(contactAddressVo.getLastName());
//            salesVo.setShippingPinCode(contactAddressVo.getPinCode());
//            salesVo.setShippingStateCode(contactAddressVo.getStateCode());


            //data set via parameter, for edited shipping address
            salesVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
            salesVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
            salesVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
            salesVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
            salesVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
//            salesVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
//            salesVo.setShippingLastName(allRequestParams.get("shippinglastname"));
            salesVo.setShippingFirstName(StringUtils.isNotBlank(allRequestParams.get("shippingfirstname")) ? allRequestParams.get("shippingfirstname") : (StringUtils.isNotBlank(contactAddressVo.getFirstName()) ? contactAddressVo.getFirstName() : ""));
            salesVo.setShippingLastName(StringUtils.isNotBlank(allRequestParams.get("shippinglastname")) ? allRequestParams.get("shippinglastname") : (StringUtils.isNotBlank(contactAddressVo.getLastName()) ? contactAddressVo.getLastName() : ""));
            salesVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
            salesVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
        } else if (salesVo2 != null) {
            try {
                salesVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
                salesVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
                salesVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
                salesVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
                salesVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
//                salesVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
//                salesVo.setShippingLastName(allRequestParams.get("shippinglastname"));
                salesVo.setShippingFirstName(StringUtils.isNotBlank(allRequestParams.get("shippingfirstname")) ? allRequestParams.get("shippingfirstname") : (StringUtils.isNotBlank(salesVo2.getShippingFirstName()) ? salesVo2.getShippingFirstName() : ""));
                salesVo.setShippingLastName(StringUtils.isNotBlank(allRequestParams.get("shippinglastname")) ? allRequestParams.get("shippinglastname") : (StringUtils.isNotBlank(salesVo2.getShippingLastName()) ? salesVo2.getShippingLastName() : ""));
                salesVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
                salesVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
            } catch (Exception e) {
                e.printStackTrace();
                salesVo.setShippingAddressLine1(salesVo2.getShippingAddressLine1());
                salesVo.setShippingAddressLine2(salesVo2.getShippingAddressLine2());
                salesVo.setShippingCityCode(salesVo2.getShippingCityCode());
                salesVo.setShippingCompanyName(salesVo2.getShippingCompanyName());
                salesVo.setShippingCountriesCode(salesVo2.getShippingCountriesCode());
                salesVo.setShippingFirstName(salesVo2.getShippingFirstName());
                salesVo.setShippingLastName(salesVo2.getShippingLastName());
                salesVo.setShippingPinCode(salesVo2.getShippingPinCode());
                salesVo.setShippingStateCode(salesVo2.getShippingStateCode());
            }
        }

        if (salesVo.getSalesId() == 0) {
            salesVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
            salesVo.setCreatedOn(CurrentDateTime.getCurrentDate());
            salesVo.setPaidAmount(0.0);
        }
        if (salesVo.getSalesAdditionalChargeVos() != null) {
            salesVo.getSalesAdditionalChargeVos().removeIf(rm -> rm.getAdditionalChargeVo() == null);
            salesVo.getSalesAdditionalChargeVos().forEach(item1 -> item1.setSalesVo(salesVo));
        }

        List<SalesItemVo> salesItemVoList = new ArrayList<>();
        if (salesVo.getSalesProductDTOs() != null) {

            if (salesVo.getSalesItemVos() != null) {

                salesVo.getSalesProductDTOs().removeIf(rm -> rm.getProductVarientId() == 0);

                for (int i = 0; i < salesVo.getSalesProductDTOs().size(); i++) {

                    long productId = salesVo.getSalesProductDTOs().get(i).getProductId();
                    long productVarientId = salesVo.getSalesProductDTOs().get(i).getProductVarientId();
                    double qty = salesVo.getSalesProductDTOs().get(i).getQty();
                    String batchNo = salesVo.getSalesProductDTOs().get(i).getBatchNo();
                    double landingCost = salesVo.getSalesProductDTOs().get(i).getLandingCost();
                    double sellingPrice = salesVo.getSalesProductDTOs().get(i).getSellingPrice();
                    long salesItemId = salesVo.getSalesProductDTOs().get(i).getSalesItemId();
                    long batchId = salesVo.getSalesProductDTOs().get(i).getBatchId();
                    double mrp = salesVo.getSalesProductDTOs().get(i).getMrp();

                    int orderBY = salesVo.getSalesProductDTOs().get(i).getOrderBy();
                    SalesItemVo salesItemVo = null;

                    for (int j = 0; j < salesVo.getSalesItemVos().size(); j++) {
                        if (salesVo.getSalesItemVos().get(j).getProductId() == productId && orderBY == j) {
                            salesItemVo = salesVo.getSalesItemVos().get(j);
                            // log.warning("itemindex---->"+j);
                            break;
                        }
                    }

                    if (salesItemVo != null) {
                        // log.warning("productId---->"+productId);
                        // log.warning("productVarientId---->"+productVarientId);
                        // log.warning("qty---->"+qty);
                        // log.warning("batchno---->"+batchNo);
                        // log.warning("orderBY---->"+orderBY);
                        // log.warning("sellingPrice---->"+sellingPrice);

                        // log.warning("landingCost---->"+landingCost);
                        // log.warning("mrp---->"+mrp);
                        // log.warning("batchId---->"+batchId);

                        SalesItemVo item = new SalesItemVo();

                        item.setQty((float) qty);
                        item.setProductId(productId);
                        item.setBatchNo(batchNo);
                        item.setBatchId(batchId);
                        item.setLandingCost(landingCost);
//                        System.err.println("landingCost::::::;;"+landingCost);
                        item.setSellingPrice(sellingPrice);
                        ProductVarientsVo productVarientVo = new ProductVarientsVo();
                        productVarientVo.setProductVarientId(productVarientId);
                        item.setProductVarientsVo(productVarientVo);
                        item.setSalesVo(salesVo);
                        item.setDiscount(salesItemVo.getDiscount());
                        item.setDiscountType(salesItemVo.getDiscountType());
                        item.setPrice(salesItemVo.getPrice());
//                    	System.err.println("xprice-----------"+salesItemVo.getPrice());
                        item.setMrp(mrp);
                        item.setProductDescription(salesItemVo.getProductDescription());
//                    	System.err.println("tax amount ---> "+salesItemVo.getTaxAmount());
//                        System.err.println("Qty ---> "+salesItemVo.getQty());
//                        System.err.println("final tax amount ---> "+salesItemVo.getTaxAmount()/salesItemVo.getQty()* qty);
                        item.setTaxAmount(salesItemVo.getTaxAmount() / salesItemVo.getQty() * qty);
                        item.setTaxRate(salesItemVo.getTaxRate());
                        item.setTaxVo(salesItemVo.getTaxVo());
                        item.setDiscountAdditional(salesItemVo.getDiscountAdditional());
                        item.setDiscountTypeAdditional(salesItemVo.getDiscountTypeAdditional());
                        item.setSalesMan(salesItemVo.getSalesMan());
                        item.setMrpToDiscount(salesItemVo.getMrpToDiscount());
                        item.setMrpTodiscountAdditional(salesItemVo.getMrpTodiscountAdditional());
                        item.setMrpToDiscountType(salesItemVo.getMrpToDiscountType());
                        item.setMrpToDiscountTypeAdditional(salesItemVo.getMrpToDiscountTypeAdditional());
                        item.setDiscount2(salesItemVo.getDiscount2());
                        item.setDiscountType2(salesItemVo.getDiscountType2());
                        item.setFreeQty(salesItemVo.getFreeQty());
                        item.setCessAmount(salesItemVo.getCessAmount());
                        item.setCessRate(salesItemVo.getCessRate());
                        item.setIsReturn(salesItemVo.getIsReturn());
                        item.setNetAmount(salesItemVo.getNetAmount());
                        item.setSalesItemType(salesItemVo.getSalesItemType());
                        item.setSalesType(salesItemVo.getSalesType());
                        //item.setLandingCost(salesItemVo.getLandingCost());
                        item.setProfit(salesItemVo.getProfit());
                        //item.setSellingPrice(salesItemVo.getSellingPrice());
                        item.setSalesItemId(salesItemId);
                        item.setOrderBy(orderBY);
                        salesItemVoList.add(item);
                    }

                }
            }
        }

        // log.warning("size------>"+salesItemVoList.size());
        //salesVo.setSalesItemVos(null);
        salesVo.setSalesItemVos(salesItemVoList);
        // log.warning("salesitem size------>"+salesVo.getSalesItemVos().size());
//        if (salesVo.getSalesItemVos() != null) {
//            salesVo.getSalesItemVos().removeIf(rm -> rm.getProductVarientsVo() == null);
//            salesVo.getSalesItemVos().forEach(item -> item.setSalesVo(salesVo));
//        }

        if (allRequestParams.get("deleteSalesItemIds") != null
                && !allRequestParams.get("deleteSalesItemIds").equals("")) {
            String address = allRequestParams.get("deleteSalesItemIds").substring(0,
                    allRequestParams.get("deleteSalesItemIds").length() - 1);
            List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());

            if (salesVo != null && salesVo.getSalesId() != 0) {
                salesService.deleteSalesItem(l, salesVo.getSalesId());
            }
        }

        if (allRequestParams.get("deleteAdditionalChargeIds") != null
                && !allRequestParams.get("deleteAdditionalChargeIds").equals("")) {

            String address = allRequestParams.get("deleteAdditionalChargeIds").substring(0,
                    allRequestParams.get("deleteAdditionalChargeIds").length() - 1);
            List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());

            salesService.deleteSalesAdditionalItem(l);
        }
        if (allRequestParams.get("addproductby") != null)
            if (allRequestParams.get("addproductby").equals("2")) {
                double mainTotal = 0.0;

                List<SalesItemVo> itemVos = new ArrayList<>();
                String filepath = (String) session.getAttribute("filepath");
                File fb = new File(filepath);
                InputStream in = new FileInputStream(fb);

                // Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(in);
                // Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);

                // Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                rowIterator.next();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    // For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        // Check the cell type and format accordingly
                        cell.setCellType(CellType.STRING);
                        switch (cell.getCellType()) {
                            case BOOLEAN:
                                break;
                            case NUMERIC:
                                break;
                            case STRING:
                                break;
                        }

                    }
                    ////////
                    double qty, totalQty = 0;
                    double rate;
                    double discount;
                    double taxRate = 0.0;
                    double taxableAmount, taxAmount = 0.0, totalTaxAmount = 0.0, taxableValue = 0.0;
                    double total = 0.0, totalAmount = 0.0;
                    double discountType, totalDiscount = 0.0;
                    /////////


                    DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");

                    // PurchaseItemVo purchaseItemVo = new PurchaseItemVo();
                    SalesItemVo itemVo = new SalesItemVo();
                    List<String> producttypelist = new ArrayList<>();
                    producttypelist = productTypeRepository.findListOfData();

                    ProductVarientsVo productVarientsVo = productService
                            .findByitemCodeIgnoreCaseAndCompanyIdAndIsDeleted(
                                    securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(0).getStringCellValue().trim()),
                                    Long.parseLong(session.getAttribute("companyId").toString()), 0, merchantTypeId, clusterId, producttypelist);
                    if (productVarientsVo != null) {
                        itemVo.setProductVarientsVo(productVarientsVo);

                        // itemVo.setProduct(ProductVarientsVo.getProductVo());
                        itemVo.setTaxVo(productVarientsVo.getProductVo().getTaxVo());
                        itemVo.setTaxRate(productVarientsVo.getProductVo().getTaxVo().getTaxRate());
                        if (row.getCell(4) != null && row.getCell(4).getStringCellValue().trim() != "") {
                            try {
                                itemVo.setDiscount(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(4).getStringCellValue().trim())));
                            } catch (Exception e) {
                                itemVo.setDiscount(0);
                            }

                        } else {
                            itemVo.setDiscount(0);
                        }
                        // emne pu
                        if (row.getCell(3) != null && row.getCell(3).getStringCellValue().trim() != "") {
                            try {
                                itemVo.setDiscountType(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(3).getStringCellValue().trim().toLowerCase()));
                            } catch (Exception e) {
                                itemVo.setDiscountType("percentage");
                            }

                        } else {
                            itemVo.setDiscountType("percentage");
                        }
                        if (row.getCell(7) != null && row.getCell(7).getStringCellValue().trim() != "") {
                            try {
                                itemVo.setDiscount2(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(7).getStringCellValue().trim())));
                            } catch (Exception e) {
                                itemVo.setDiscount2(0);
                            }

                        } else {
                            itemVo.setDiscount2(0);
                        }
                        // emne pu
                        if (row.getCell(6) != null && row.getCell(6).getStringCellValue().trim() != "") {
                            try {
                                itemVo.setDiscountType2(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(3).getStringCellValue().trim().toLowerCase()));
                            } catch (Exception e) {
                                itemVo.setDiscountType2("percentage");
                            }

                        } else {
                            itemVo.setDiscountType2("percentage");
                        }

                        if (row.getCell(5) != null && row.getCell(5).getStringCellValue().trim() != "") {
                            itemVo.setProductDescription("");
                        } else {
                            try {
                                itemVo.setProductDescription(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(5).getStringCellValue().trim()));
                            } catch (Exception e) {
                                itemVo.setProductDescription("");
                            }
                        }
                        if (row.getCell(5) != null && row.getCell(5).getStringCellValue().trim() != "") {
                            itemVo.setProductDescription("");
                        } else {
                            try {
                                itemVo.setProductDescription(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(5).getStringCellValue().trim()));
                            } catch (Exception e) {
                                itemVo.setProductDescription("");
                            }
                        }
                        if (row.getCell(8) != null && row.getCell(8).getStringCellValue().trim() != "") {
                            itemVo.setBatchNo(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(8).getStringCellValue().trim()));
                        }

                        try {
                            itemVo.setQty(Float.parseFloat(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(1).getStringCellValue().trim())));
                        } catch (Exception e) {
                            itemVo.setQty(0);
                        }

                        try {
                            itemVo.setMrp(productVarientsVo.getMrp());
                        } catch (Exception e) {
                            itemVo.setMrp(0);
                        }

                        try {
                            int taxincluded = productVarientsVo.getProductVo().getTaxIncluded();
                            if (taxincluded == 1) {
                                double taxrate = productVarientsVo.getProductVo().getTaxVo().getTaxRate();
                                double price = Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(2).getStringCellValue().trim())) / ((taxrate / 100) + 1);
                                itemVo.setPrice(round(price, 2));
                            } else {
                                itemVo.setPrice(round(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCell(2).getStringCellValue().trim())), 2));
                            }

                        } catch (Exception e) {
                            itemVo.setPrice(0);
                        }


                        qty = itemVo.getQty();
                        taxRate = itemVo.getTaxRate();
                        rate = itemVo.getPrice();
                        discount = itemVo.getDiscount();
                        if (itemVo.getDiscountType().equals("percentage")) {
                            discount = (rate * discount) / 100;
                        }
                        totalDiscount += (discount * 1) * (qty * 1);

                        taxableValue = (rate * qty) - (discount * qty);

                        taxAmount = (taxableValue * taxRate) / 100;
                        total = taxableValue + taxAmount;

                        totalAmount += total;
                        totalTaxAmount += taxAmount;
                        totalQty += qty;

                        mainTotal += totalAmount;
                        itemVo.setTaxAmount(taxAmount);
                        itemVo.setSalesVo(salesVo);

                        itemVos.add(itemVo);
                    }
                }
                salesVo.setSalesItemVos(itemVos);
                salesVo.setTotal(Math.round(mainTotal * 100.0) / 100.0);
                in.close();
            }
//            System.err.println("gst apply @@@@@@@@@@@@@@@@@@@ "+allRequestParams.get("gstApply") );
        if (allRequestParams.get("gstApply") == null) {
            salesVo.setGstApply(1);
            String taxCode = Constant.GST;
            int taxType = Constant.TAX_TYPE_GST;
            try {
                Map<String, String> gstMap = userRepository.getgstDetails(Long.parseLong(session.getAttribute("companyId").toString()));
                if (gstMap != null && !gstMap.isEmpty()) {
                    if (StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"), Constant.VAT)) {
                        taxCode = Constant.VAT;
                        taxType = Constant.TAX_TYPE_VAT;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String tax_code = "";
            TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(0, Long.parseLong(session.getAttribute("companyId").toString()), taxType, tax_code);
            if (taxVo != null)
                salesVo.getSalesItemVos().forEach(p -> {
                    p.setTaxVo(taxVo);
                    p.setTaxRate(0);
                });
        }
        salesVo2 = salesService.save(salesVo);
//        if(salesVo2.getSalesItemVos().get(0).getProductVarientsVo().getProductVo().getTaxVo()!=null) {
//        System.out.println("tax fB"+salesVo2.getSalesItemVos().get(0).getProductVarientsVo().getProductVo().getTaxVo().getTaxId());
//        }
        if (salesVo2.getType().equals(Constant.SALES_INVOICE)) {

            ShiprocketVo shiprocketVo = shiprocketService.getDetails(Long.parseLong(session.getAttribute("companyId").toString()));

            if (shiprocketVo != null) {
                if (shiprocketVo.getDefaultSyncInvoice() == 1 && salesVo2.getDefaultSyncInvoice() == 1) {
                    try {
                        boolean flag = false;
                        String token = (String) session.getAttribute("shiprocketauthtoken");
                        if (token == null || token == "") {
                            flag = false;
                        } else {
                            flag = shiprocketService.getAllOrder(token);
                        }
                        if (!flag || token == null) {
                            shiprocketService.createShipRocketAuthToken(servletRequest, session);
                            // log.info("4");
                            token = (String) session.getAttribute("shiprocketauthtoken");
//                            log.info("5" + token);
                            shiprocketService.shipRocketCreateOrder(salesVo2, token, servletRequest);
                            // log.info("6");

                        } else {
                            shiprocketService.shipRocketCreateOrder(salesVo, token, servletRequest);

                        }
                    } catch (Exception e) {
                        // log.info("Exception at Shiprocket service:------" + e);
                    }
                    // xyz=shiprocket.placeorder(sales);
                    // sales.shiprocketStatus= xyz.status
                }
            }
            salesService.insertSalesTransaction(salesVo2, session.getAttribute("financialYear").toString(), session);
        } else if (salesVo2.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
            CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(salesVo2.getBranchId(), Constant.SALESDELIVERYCHALANSTOCK);
//        	System.out.println("here delivery chalan:::::::::::::::::::::::::::::::::::::::::"+settingVo.getValue());
            if (settingVo.getValue() == 1) {
                stockTransactionService.deleteStockTransactionSales(salesVo2.getBranchId(), salesVo2.getSalesId(), Constant.SALES_DELIVERY_CHALLAN);
                stockTransactionService.saveStockFromDeliveryChalan(salesVo2.getSalesItemVos(), session.getAttribute("financialYear").toString());
            }
        } else {
            if (salesVo2.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                salesService.insertSalesReturnTransaction(salesVo2, session.getAttribute("financialYear").toString(), session);
                returnUpdateInParent(salesVo2.getSalesId(), salesVo2.getSalesVo().getSalesId());
            }
        }

        if (salesVo2.getSalesVo() != null && salesVo2.getSalesVo().getType().equals(Constant.SALES_ORDER)) {
            if (salesVo2.getType().equals(Constant.SALES_INVOICE)) {
//               System.err.println("type-----------------"+salesVo2.getType());
                salesService.updateStatusBySalesId(Constant.INVOICE_CREATED, salesVo2.getSalesVo().getSalesId(),
                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
            } else if (salesVo2.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                salesService.updateStatusBySalesId("DC Created", salesVo2.getSalesVo().getSalesId(),
                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
            }

        }

        if (allRequestParams.get("saveandpayment") != null && Integer.parseInt(allRequestParams.get("saveandpayment")) == 1) {


            if (allRequestParams.get("typePayment") != null && allRequestParams.get("typePayment").equals("1")) {
//                List<ReceiptVo> receiptVos = receiptService.findByBranchIdAndIsDeletedAndContactVoContactIdAndType(
//                        Long.parseLong(session.getAttribute("branchId").toString()), 0,
//                        salesVo2.getContactVo().getContactId(), Constant.PAYMENT_TYPE_ADVANCE);
                String advancePaymentBill = allRequestParams.get("advancePaymentBillId");
                List<ReceiptVo> receiptVos = new ArrayList<ReceiptVo>();
                if (advancePaymentBill != null && !advancePaymentBill.equals("")) {
                    List<Long> l = Arrays.asList(advancePaymentBill.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
                    receiptVos = receiptService.findByBranchidandisDeletedAndTypeAndReceiptId(Long.parseLong(session.getAttribute("branchId").toString()), 0, Constant.PAYMENT_TYPE_ADVANCE, l);
                }

                double salesTotal = salesVo2.getTotal(), totalPayment = 0;
                for (ReceiptVo receiptVo2 : receiptVos) {

                    if (salesTotal != 0) {

                        double receiptbilltotal = receiptVo2.getReceiptBillVos().stream()
                                .mapToDouble(p -> p.getTotalPayment()).sum();


                        if (receiptbilltotal != receiptVo2.getTotalPayment()) {

                            double receiptAmount = receiptVo2.getTotalPayment() - receiptbilltotal;
                            ReceiptBillVo billVo = new ReceiptBillVo();
                            if (salesTotal <= receiptAmount) {

                                billVo.setKasar(0);
                                billVo.setOldPyament(0);
                                billVo.setReceiptVo(receiptVo2);
                                billVo.setSalesVo(salesVo2);
                                billVo.setTotalPayment(salesTotal);
                                salesTotal = 0;
                            } else {

                                billVo.setKasar(0);
                                billVo.setOldPyament(0);
                                billVo.setReceiptVo(receiptVo2);
                                billVo.setSalesVo(salesVo2);
                                billVo.setTotalPayment(receiptAmount);
                                salesTotal -= receiptAmount;
                            }

                            totalPayment += billVo.getTotalPayment();
                            receiptService.saveBill(billVo);
                            receiptService.transation(receiptVo2, 0);
                        }

                    }
                }
                salesService.updatePaidAmountPlus(salesVo2.getSalesId(), totalPayment);

            }

            if (!allRequestParams.get("totalPayment").equals("0")) {

                ReceiptVo receiptVo = new ReceiptVo();
                receiptVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
                receiptVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
                receiptVo.setReceiptNo(receiptService.getNewPaymentNo(Constant.RECEIPT,
                        Long.parseLong(session.getAttribute("branchId").toString()),
                        Long.parseLong(session.getAttribute("userId").toString()), "PAY",
                        Long.parseLong(session.getAttribute("companyId").toString())));
                receiptVo
                        .setPrefix(prefixService.getPrefixByPrefixTypeAndBranchId(
                                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.RECEIPT, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
                receiptVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
                receiptVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
                receiptVo.setCreatedOn(CurrentDateTime.getCurrentDate());
                receiptVo.setModifiedOn(CurrentDateTime.getCurrentDate());
                receiptVo.setAmount(Double.parseDouble(allRequestParams.get("totalPayment")));
                receiptVo.setReceiptDate(salesVo2.getSalesDate());
                receiptVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                receiptVo.setReceiptMode("Cash");
                receiptVo.setPaymentType("cash");
                receiptVo.setType(Constant.PAYMENT_TYPE_AGAINSTBILL);
                //receiptVo.setContactVo(salesVo2.getContactVo());
                receiptVo.setPartyAccountVo(salesVo2.getContactVo().getAccountCustomVo());
                try {
                    receiptVo.setDescription(allRequestParams.get("description"));
                } catch (Exception e) {
                    // TODO: handle exception
                    // log.severe(String.valueOf(e));
                }

                if (allRequestParams.get("receiptMode").equals("bank")) {

                    BankVo bankVo = new BankVo();
                    bankVo.setBankId(Long.parseLong(allRequestParams.get("bankVoId")));
                    receiptVo.setBankVo(bankVo);
                    receiptVo.setReceiptMode("bank");
                    receiptVo.setPaymentType("bank");
                    receiptVo.setBankAccountNo(allRequestParams.get("accountNo"));
                    receiptVo.setBankTransactionType(allRequestParams.get("bankpaymentmode"));

                    try {
                        receiptVo.setChequeDate(dateFormat.parse(allRequestParams.get("chequeDate")));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    AccountCustomVo cashAccountCustomVo = new AccountCustomVo();
                    cashAccountCustomVo.setAccountCustomId(Long.parseLong(allRequestParams.get("accountCustomVo.accountCustomId")));
                    receiptVo.setCashAccountCustomVo(cashAccountCustomVo);
                }

                ReceiptBillVo receiptBillVo = new ReceiptBillVo();
                List<ReceiptBillVo> receiptBillVos = new ArrayList<>();
                receiptBillVo.setKasar(Double.parseDouble(allRequestParams.get("Kasar")));
                receiptBillVo.setOldPyament(0);
                receiptBillVo.setReceiptVo(receiptVo);
                receiptBillVo.setSalesVo(salesVo2);
                receiptBillVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                receiptBillVos.add(receiptBillVo);
                receiptVo.setReceiptBillVos(receiptBillVos);
                ReceiptVo receiptVo3 = receiptService.save(receiptVo);
                receiptService.transation(receiptVo3, receiptBillVo.getKasar());
                salesService.updatePaidAmountPlus(salesVo2.getSalesId(),
                        Double.parseDouble(allRequestParams.get("totalPayment")));
            }

        }

        // PDF TOKEN CODE
        if (allRequestParams.get("salesId") == null) {
            // new case
            String pdfToken = EncryptMessage.getSecureMessage(
                    salesVo.getSalesId() + salesVo.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
            salesService.updateToken(salesVo2.getSalesId(), pdfToken);
            salesVo2.setPdfToken(pdfToken);
        } else if (salesVo2.getPdfToken() == null) {
            String pdfToken = EncryptMessage.getSecureMessage(
                    salesVo2.getSalesId() + salesVo2.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
            salesService.updateToken(salesVo2.getSalesId(), pdfToken);
            salesVo2.setPdfToken(pdfToken);
        }

        // MESSAGE FLOW
        VasyMessageSettingVo messageSettingVo = vasyMessageSettingService.getVasyMessageSettingSaleTypeWise(salesVo2, isEdit);
        if (messageSettingVo != null && messageSettingVo.getIsActive() == 1) {
            if ((salesVo2.getContactVo() != null) && ((StringUtils.isNotBlank(salesVo2.getContactVo().getWhatsappNo())
                    || StringUtils.isNotBlank(salesVo2.getContactVo().getMobNo())))) {
                if (isEdit == 0) {
                    // new cases
                    if (type.equals(Constant.SALES_INVOICE)) {
                        globalMessageService.sendMerchantToCustomerNewInvoiceMessage(salesVo2,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                servletRequest.getServletContext().getRealPath("/"),
                                session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(),
                                salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                    } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                        globalMessageService.sendMerchantToCustomerNewCreditNoteMessage(salesVo2,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                servletRequest.getServletContext().getRealPath("/"),
                                session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(),
                                salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                    }
                } else {
                    if (type.equals(Constant.SALES_INVOICE)) {
                        globalMessageService.sendMerchantToCustomerEditInvoiceMessage(salesVo2,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                servletRequest.getServletContext().getRealPath("/"),
                                session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(),
                                salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                    } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                        globalMessageService.sendMerchantToCustomerEditCreditNoteMessage(salesVo2,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                servletRequest.getServletContext().getRealPath("/"),
                                session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(),
                                salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                    }

                }

            }
        }

        if (messageSettingVo == null || messageSettingVo.getIsActive() == 0) {
            if (salesVo2.getContactVo() != null && salesVo2.getContactVo().getMobNo() != null) {
                try {
                    String whatsappToken = session.getAttribute("whatsappToken") != null
                            && StringUtils.isNotBlank(session.getAttribute("whatsappToken").toString())
                            ? session.getAttribute("whatsappToken").toString()
                            : "";
                    sendSMS(salesVo2, companyId, whatsappToken, session.getAttribute("name").toString(), session,
                            servletRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (salesVo2.getContactVo() != null) {
                if (salesVo2.getContactVo().getEmail() != null) {
                    CompanySettingVo allowInvoiceEmail = companySettingService.findByCompanyIdAndType(companyId,
                            Constant.ALLOWINVOICEEMAIL);
                    if (allowInvoiceEmail != null && allowInvoiceEmail.getValue() == 1) {
                        String body = Converter.convertToHtml(servletRequest,
                                "/media/download/sales/" + salesVo2.getPdfToken() + "/mail");
                        sendGridEmailService.sendHTML(from, salesVo2.getContactVo().getEmail(), "Invoice", body,
                                Long.parseLong(session.getAttribute("companyId").toString()));
                    }
                }
            }
        }

        // EDIT INVOICE VASY TO MERCHANT
        if (type.equals(Constant.SALES_INVOICE) && (isEdit != 0)) {
            VasyMessageSettingVo vasyMessageSettingVo = vasyMessageSettingService.findByTypeAndEvent(
                    MessageConstant.TYPE_VASY_TO_MERCHANT, MessageConstant.SYSTEM_EVENT_EDIT_INVOICE);
            if (vasyMessageSettingVo != null && vasyMessageSettingVo.getIsActive() == 1) {
                globalMessageService.sendVasyToMerchantEditInvoiceMessage(salesVo2,
                        Long.parseLong(session.getAttribute("companyId").toString()),
                        Long.parseLong(session.getAttribute("branchId").toString()),
                        Long.parseLong(session.getAttribute("userId").toString()),
                        servletRequest.getServletContext().getRealPath("/"),
                        session.getAttribute("realPath").toString(), session.getAttribute("currencyCode").toString(),
                        session.getAttribute("currencyName").toString(),
                        session.getAttribute("decimalPoint").toString());
            }
        }


//        if (allRequestParams.get("salesId") == null) {
//            String pdfToken = EncryptMessage.getSecureMessage(
//                    salesVo.getSalesId() + salesVo.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
//            salesService.updateToken(salesVo2.getSalesId(), pdfToken);
//            salesVo2.setPdfToken(pdfToken);
//            //sendSMS(salesVo2, session);
//            try {
//            	sendSMS(salesVo2, companyId, session.getAttribute("whatsappToken").toString(), session.getAttribute("name").toString(),session,servletRequest);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//        } else {
//            if (salesVo2.getPdfToken() == null) {
//                String pdfToken = EncryptMessage.getSecureMessage(
//                        salesVo2.getSalesId() + salesVo2.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
//                salesService.updateToken(salesVo2.getSalesId(), pdfToken);
//                salesVo2.setPdfToken(pdfToken);
//                if (salesVo2.getContactVo() != null) {
//                    if (salesVo2.getContactVo().getMobNo() != null) {
//                        //sendSMS(salesVo2, session);
//                    	 try {
//                         	sendSMS(salesVo2, companyId, session.getAttribute("whatsappToken").toString(), session.getAttribute("name").toString(),session,servletRequest);
//             			} catch (Exception e) {
//             				e.printStackTrace();
//             			}
//                    }
//                }
//
//            } else {
//                if (salesVo2.getContactVo() != null) {
//                    if (salesVo2.getContactVo().getMobNo() != null) {
//                        //sendSMS(salesVo2, session);
//                    	 try {
//                         	sendSMS(salesVo2, companyId, session.getAttribute("whatsappToken").toString(), session.getAttribute("name").toString(),session,servletRequest);
//             			} catch (Exception e) {
//             				e.printStackTrace();
//             			}
//                    }
//                }
//            }
//        }
//
//        // send mail code for invoice//
//        if (salesVo2.getContactVo() != null) {
//            if (salesVo2.getContactVo().getEmail() != null) {
//                CompanySettingVo allowInvoiceEmail = companySettingService.findByCompanyIdAndType(companyId,
//                        Constant.ALLOWINVOICEEMAIL);
//                if (allowInvoiceEmail != null && allowInvoiceEmail.getValue() == 1) {
//                    String body = Converter.convertToHtml(servletRequest,
//                            "/media/download/sales/" + salesVo2.getPdfToken() + "/mail");
//                    sendGridEmailService.sendHTML(from, salesVo2.getContactVo().getEmail(), "Invoice",
//                            body,
//                            Long.parseLong(session.getAttribute("companyId").toString()));
//                }
//            }
//        }


        // send mail code complete

        if (allRequestParams.get("saveandnew") != null
                && Integer.parseInt(allRequestParams.get("saveandnew")) == 1) {
            return "redirect:/sales/" + type + "/new";
        } else if (allRequestParams.get("saveandprint") != null
                && Integer.parseInt(allRequestParams.get("saveandprint")) == 1) {
            return "redirect:/sales/" + type + "/" + salesVo2.getSalesId() + "?print=true";
        } else {
            return "redirect:/sales/" + type + "/" + salesVo2.getSalesId();
        }

    }


    @Async
    protected void returnUpdateInParent(long creditnoteId, long salesId) {
        salesService.returnUpdateInParent(creditnoteId, salesId);
    }

    @PostMapping("{id}/delete")
    public ModelAndView salesDelete(@PathVariable String type, @PathVariable long id, HttpSession session,
                                    HttpServletRequest request,RedirectAttributes model) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_DELETE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_DELETE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_DELETE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_DELETE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_DELETE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_DELETE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView();
        int resultCount = salesService.countBySalesIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (resultCount == 0) {
            view.setViewName(Constant.ERROR_PAGE_404);
        } else {
            String salestype = type;
            if (type.equals(Constant.SALES_ORDER)) {
                salestype = "salesorder";
            }
            String pdfAbsolutePath = "";
            UserFrontVo branch = userRepository.findByUserFrontId(Long.parseLong(session.getAttribute("branchId").toString()));
            if (MenuPermission.havePermission(session, salestype, Constant.DELETE) == 1) {
                SalesVo salesVo = salesService.findBySalesIdAndBranchId(id, Long.parseLong(session.getAttribute("branchId").toString()));
                 if (ewayBillOrEInvoiceGenerated(salesVo,model,Constant.ISDELETE,type)){
                    view.setViewName("redirect:/sales/" + type + "/" + id);
                    return view;
                }

                if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                    List<SalesItemQtyDTO> salesItemQtyDTOs = salesRepository.findSalesItemQtyDetailsBySalesId(id);
                    if (!salesItemQtyDTOs.isEmpty()) {
                        for (int j = 0; j < salesItemQtyDTOs.size(); j++) {
                            SalesItemQtyDTO salesItemQtyDTO = salesItemQtyDTOs.get(j);
                            if (salesItemQtyDTO != null) {
                                String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                    List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                            .map(Long::parseLong).collect(Collectors.toList());
                                    if (!parentItemIds.isEmpty()) {
                                        long parentSalesItemId = parentItemIds.get(0);
                                        BigDecimal updateQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                        // log.warning("updateQty-->"+updateQty);
                                        int result = salesService.updateSalesItemCreditNoteQtyMinus(parentSalesItemId, updateQty.doubleValue(),Double.parseDouble(String.valueOf(salesItemQtyDTO.getFreeQty())));
                                        // log.warning("result-->"+result);
                                    }
                                }
                            }
                        }
                    }
                }
                if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    try {
                        BasicUserFrontDTO salesBranchVo = userRepository.getBasicUserDetailsById(salesVo.getBranchId());
                        pdfAbsolutePath = messageBodyService.getsalesPDFpath(salesVo, salesBranchVo,
                                request.getServletContext().getRealPath("/"), session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(), Constant.DELETE);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    List<SalesItemQtyDTO> salesItemQtyDTOs = salesRepository.findSalesItemQtyDetailsBySalesId(id);
                    if (!salesItemQtyDTOs.isEmpty()) {
                        for (int j = 0; j < salesItemQtyDTOs.size(); j++) {
                            SalesItemQtyDTO salesItemQtyDTO = salesItemQtyDTOs.get(j);
                            if (salesItemQtyDTO != null) {
                                String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                    List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                            .map(Long::parseLong).collect(Collectors.toList());
                                    if (!parentItemIds.isEmpty()) {
                                        if (parentItemIds.size() > 1) {//Here Multiple Parent Sales Items
                                            // log.warning("=======Here Multiple Parent Sales Items=======");
                                            BigDecimal qty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                            BigDecimal freeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty()));
                                            // log.warning("qty--->"+qty);
                                            for (int i = 0; i < parentItemIds.size(); i++) {
                                                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                                                    long parentSalesItemId = parentItemIds.get(i);
                                                    // log.warning("parentSalesItemId--->"+parentSalesItemId);
                                                    SalesItemQtyDTO itemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                                                    if (itemQtyDTO != null) {
                                                        BigDecimal orderQty = qty; //150 //50
                                                        BigDecimal updateQty = BigDecimal.ZERO;
                                                        BigDecimal updateFreeQty = BigDecimal.ZERO;
                                                        // log.warning("updateQty-->"+updateQty);
                                                        if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                            BigDecimal actualOrderQty = new BigDecimal(String.valueOf(itemQtyDTO.getInvoiceQty()));
                                                            BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(itemQtyDTO.getInvoiceFreeQty()));
                                                            updateQty = orderQty.compareTo(actualOrderQty) >= 0 ? actualOrderQty : orderQty;
                                                            updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty:freeQty;
                                                            salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                                        } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                            BigDecimal actualOrderQty = new BigDecimal(String.valueOf(itemQtyDTO.getDcQty()));
                                                            BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(itemQtyDTO.getDcFreeQty()));
                                                            updateQty = orderQty.compareTo(actualOrderQty) >= 0 ? actualOrderQty : orderQty;
                                                            updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty:freeQty;
                                                            salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());//100
                                                        }
                                                        qty = qty.subtract(updateQty);//150-100=50
                                                        freeQty = freeQty.subtract(updateFreeQty);
                                                    }

                                                }

                                            }
                                            // log.warning("=======Here Multiple Parent Sales Items DELETE=======");
                                        } else {
                                            long parentSalesItemId = parentItemIds.get(0);
                                            BigDecimal qty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                            BigDecimal freeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty()));
                                            SalesItemQtyDTO itemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                                            if(itemQtyDTO != null) {
                                                if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                        || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                    BigDecimal actualOrderQty = new BigDecimal(String.valueOf(itemQtyDTO.getInvoiceQty()));
                                                    BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(itemQtyDTO.getInvoiceFreeQty()));
                                                    BigDecimal updateQty = qty.compareTo(actualOrderQty) >= 0 ? actualOrderQty : qty;
                                                    BigDecimal updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty:freeQty;
                                                    salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty.doubleValue(), updateFreeQty.doubleValue());
                                                } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                        || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                    BigDecimal actualOrderQty = new BigDecimal(String.valueOf(itemQtyDTO.getDcQty()));
                                                    BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(itemQtyDTO.getDcFreeQty()));
                                                    BigDecimal updateQty = qty.compareTo(actualOrderQty) >= 0 ? actualOrderQty : qty;
                                                    BigDecimal updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty:freeQty;
                                                    salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty.doubleValue(), updateFreeQty.doubleValue());//100
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }


                salesService.deleteSales(Long.parseLong(session.getAttribute("branchId").toString()), id, type, Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                salesService.saveSalesHistory(Constant.HISTORY_TYPE_DELETED, salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()),
                        0, "", salesVo.getStatus(), 0, "", "", 0, session);


                try {

                    List<SalesMappingVo> mappingVos = salesMappingRepository.findByMainSalesId(id);
                    if (!mappingVos.isEmpty()) {
                        for (int i = 0; i < mappingVos.size(); i++) {

                            Long parentSalesId = mappingVos.get(i).getParentSalesId();
                            //find sales packing
                            // log.warning("parentSalesId---->" + parentSalesId);
                            if (parentSalesId != null && parentSalesId != 0) {
                                SalesDTO salesDTO = salesRepository.findCustomSalesBySalesId(parentSalesId);
                                String fromStatus = "";
                                if (salesDTO != null) {
                                    fromStatus = salesDTO.getStatus();
                                }
                                String parentType = mappingVos.get(i).getParentSalesType();
                                String salesNo = mappingVos.get(i).getParentSalesNo();


                                //for estimate and order status of that parent is PENDING
                                String parentStatus = Constant.PENDING;
                                if (salesVo.getType().equals(Constant.SALES_INVOICE)
                                        && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN))) {//HERE ORDER/DC STATUS
                                    salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_INVOICE);
                                    List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                    List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() < p.getQty()).collect(Collectors.toList());
                                    List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() == p.getQty()).collect(Collectors.toList());
                                    // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                    // log.warning("partiallyList length--------->"+partiallyList.size());
                                    // log.warning("completeList length--------->"+completeList.size());
                                    if (StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN)) {
                                        parentStatus = Constant.DELIVERED;
                                    }
                                    if (partiallyList.size() == salesItemQtyDTO.size()) {
										/* double totalInvoiceQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getInvoiceQty()).sum();
										double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
										if (totalInvoiceQty > 0) {
											if (totalInvoiceQty < totalQty) {
												parentStatus = Constant.PARTIAL_INVOICE_CREATED;
											}
										} */
                                        BigDecimal totalQty = salesItemQtyDTO
                                                .stream()
                                                .map(dto -> new BigDecimal(String.valueOf(dto.getQty())))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal totalInvoiceQty = salesItemQtyDTO
                                                .stream()
                                                .map(dto -> new BigDecimal(String.valueOf(dto.getInvoiceQty())))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        if (totalInvoiceQty.compareTo(BigDecimal.ZERO) > 0) {
                                            if (totalInvoiceQty.compareTo(totalQty) < 0) {
                                                parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                            }
                                        }
                                    } else if (completeList.size() == salesItemQtyDTO.size()) {
                                        parentStatus = Constant.INVOICE_CREATED;
                                    } else {
                                        parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                    }
                                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)
                                        && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_INVOICE))) {//HERE ORDER/INVOICE STATUS
                                    salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_DC);
                                    List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                    List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() < p.getQty()).collect(Collectors.toList());
                                    List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() == p.getQty()).collect(Collectors.toList());
                                    // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                    // log.warning("partiallyList length--------->"+partiallyList.size());
                                    // log.warning("completeList length--------->"+completeList.size());
                                    if (StringUtils.equals(parentType, Constant.SALES_INVOICE)) {
                                        parentStatus = Constant.INVOICED;
                                    }
                                    if (partiallyList.size() == salesItemQtyDTO.size()) {
										/*double totalDcQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getDcQty()).sum();
										double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
										if(totalDcQty>0) {
											if(totalDcQty<totalQty) {
												parentStatus = Constant.PARTIAL_DC_CREATED;
											}
										}*/
                                        BigDecimal totalQty = salesItemQtyDTO
                                                .stream()
                                                .map(dto -> new BigDecimal(String.valueOf(dto.getQty())))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal totalDcQty = salesItemQtyDTO
                                                .stream()
                                                .map(dto -> new BigDecimal(String.valueOf(dto.getDcQty())))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        if (totalDcQty.compareTo(BigDecimal.ZERO) > 0) {
                                            if (totalDcQty.compareTo(totalQty) < 0) {
                                                parentStatus = Constant.PARTIAL_DC_CREATED;
                                            }
                                        }
                                    } else if (completeList.size() == salesItemQtyDTO.size()) {
                                        parentStatus = Constant.DC_CREATED;
                                    } else {
                                        parentStatus = Constant.PARTIAL_DC_CREATED;
                                    }
                                }
                                // log.warning("Parent Status---->"+parentStatus+" parentSalesId--->"+parentSalesId);
                                //here update status in sales Parent
                                if (StringUtils.equals(parentStatus, Constant.PENDING) || StringUtils.equals(parentStatus, Constant.DELIVERED)
                                        || StringUtils.equals(parentStatus, Constant.INVOICED)) {
                                    salesService.updateChildType(parentSalesId, 0);
                                }
                                salesService.updateStatusBySalesId(parentStatus, parentSalesId,
                                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                                salesService.saveSalesHistory(Constant.HISTORY_TYPE_CHILDDELETED, parentSalesId, parentType, salesNo,
                                        0, fromStatus, parentStatus, salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()), 0, session);
                                salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, parentSalesId, parentType, salesNo,
                                        0, fromStatus, parentStatus, 0, "", "", 0, session);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
				try {
					for (SalesItemVo salesItemVo : salesVo.getSalesItemVos()) {
						ProductVo product = salesItemVo.getProductVarientsVo().getProductVo();
						if (product != null) {
							//wooCommerceService.updateProductStockInWooCommerce(product.getCompanyId(), product);
							wooService.syncAllProductStockInWooCommerce(session,product.getProductId());
						}
						try {
                            if (salesVo.getCompanyId() == salesVo.getBranchId())
                                if (salesItemVo.getProductVarientsVo() != null) {
                                    shopifyServiceNew.updateStockAdjustmentByProductVariantId(
                                            new ArrayList<Long>() {{
                                                add(salesItemVo.getProductVarientsVo().getProductVarientId());
                                            }},
                                            salesVo.getCompanyId());

                                }
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

                if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    // log.info("here callllllllllllllllllll");
                    if (salesVo.getSalesVo() != null) {
                        long salesId;
                        double total;
                        salesId = salesVo.getSalesVo().getSalesId();
                        total = salesVo.getTotal();
                        salesService.updateCreditNoteAmountMinus(salesId, total);
                    }
                    salesService.revertreturn(id);
                }
                try {
                    int wooCommerceOrderId = 0;
                    try {
                        // log.severe("wooCommerceOrderId :" + wooCommerceOrderId);
                        wooCommerceOrderId = salesService.findWooCommerceOrderId(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    wooCommerceService.deleteOrderFromWooCommerce(session, wooCommerceOrderId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String referer = request.getHeader("Referer");
                if (referer != null && referer.contains("/sales/" + type)) {
                    view.setViewName("redirect:/sales/" + type);
                } else {
                    view.setViewName("redirect:" + request.getHeader("Referer"));
                }

                try {
                    UserFrontVo userMain = userRepository.findByCompanyId(salesVo.getCompanyId());
                    UserFrontVo user = userRepository
                            .findByUserFrontId(Long.parseLong(session.getAttribute("userId").toString()));
                    if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                        VasyMessageSettingVo vasyMessageSettingVo = vasyMessageSettingService.findByTypeAndEvent(
                                MessageConstant.TYPE_VASY_TO_MERCHANT, MessageConstant.SYSTEM_EVENT_DELETE_INVOICE);
                        if (vasyMessageSettingVo != null && vasyMessageSettingVo.getIsActive() == 1) {
                            // vasyMessageSettingVo found and setting is active
                            globalMessageService.sendVasyToMerchantDeleteInvoiceMessage(salesVo, userMain, branch, user,
                                    pdfAbsolutePath);
                        } else {
                            // vasyMessageSettingVo not found or setting is not active

//								String message = " " + salesVo.getPrefix() + "" + salesVo.getSalesNo() + " is Deleted By "
//										+ user.getName() + " FROM "+userMain.getName()+" ";
                            String message = salesVo.getPrefix() + salesVo.getSalesNo() + " is Deleted By " + user.getName() + " FROM " + branch.getName() + "\n"
                                    + "system developed by vasyerp.com";
                            //senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), message,SMSCONSTANT.POS_ORDER_DELERE_MESSAGE);
                            String senderId = "VSYERP";
                            Map<String, String> map = messageService.generateMessageForDeletedSales(salesVo, Constant.SMSFOR_ADMIN, Constant.SMS_POS_ORDER_DELETE_MESSAGE);
                            if (map.isEmpty()) {
                                if (StringUtils.isNotBlank(userMain.getSenderId())) {
                                    senderId = userMain.getSenderId();
                                }
                                senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), message, message,
                                        SMSCONSTANT.POS_ORDER_DELERE_MESSAGE, senderId, 4);
                            } else {
                                if (StringUtils.isNotBlank(map.get("senderId"))) {
                                    senderId = map.get("senderId");
                                }
                                senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), map.get("textMessage"),
                                        map.get("whatsappMessage"), map.get("templateId"), senderId, Integer.parseInt(map.get("route")));
                            }
                        }
                    } else {
                        String message = salesVo.getPrefix() + salesVo.getSalesNo() + " is Deleted By " + user.getName() + " FROM " + branch.getName() + "\n"
                                + "system developed by vasyerp.com";
                        //senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), message,SMSCONSTANT.POS_ORDER_DELERE_MESSAGE);
                        String senderId = "VSYERP";
                        Map<String, String> map = messageService.generateMessageForDeletedSales(salesVo, Constant.SMSFOR_ADMIN, Constant.SMS_POS_ORDER_DELETE_MESSAGE);
                        if (map.isEmpty()) {
                            if (StringUtils.isNotBlank(userMain.getSenderId())) {
                                senderId = userMain.getSenderId();
                            }
                            senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), message, message,
                                    SMSCONSTANT.POS_ORDER_DELERE_MESSAGE, senderId, 4);
                        } else {
                            if (StringUtils.isNotBlank(map.get("senderId"))) {
                                senderId = map.get("senderId");
                            }
                            senssmsforDeleteBill(Long.parseLong(session.getAttribute("companyId").toString()), map.get("textMessage"),
                                    map.get("whatsappMessage"), map.get("templateId"), senderId, Integer.parseInt(map.get("route")));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                view.setViewName(Constant.ACCESSDENIED);
            }
        }

        return view;
        // return "redirect:/sales/"+type;
    }

//    @Async
//    private void senssmsforDeleteBill(long companyId, String message,String templeteId) {
//        // System.out.println("amount:------------"+amount);
//        SendSmsToBranchForInvoiceDeleteWhatsappBody(companyId, message,templeteId);
//    }

    @Async
    protected void senssmsforDeleteBill(long companyId, String textMessage, String whatsappMessage, String templeteId, String senderId, int route) {
        // System.out.println("amount:------------"+amount);
        SendSmsToBranchForInvoiceDeleteWhatsappBody(companyId, textMessage, whatsappMessage, templeteId, senderId, route);
    }

    synchronized void SendSmsToBranchForInvoiceDeleteWhatsappBody(long companyId, String textMessage, String whatsappMessage, String templeteId, String senderId, int route) {

        UserFrontVo frontVo = userRepository.findByUserFrontId(companyId);
        List<Map<String, String>> ownerNumberList = alternativeNumberService.findByUserFrontIdAndType(companyId, Constant.OWNERNUMBER);
        SimpleDateFormat DateFor = new SimpleDateFormat("EEEE, dd MMM yyyy-HH:mm aaa");
        String date = DateFor.format(new Date());

        // log.info("sales invoice delete message is:--------------" + whatsappMessage);
        if (CollectionUtils.isNotEmpty(ownerNumberList)) {
            for (Map<String, String> mapOwner : ownerNumberList) {
                String ownernumberPrefix = mapOwner.get("country_dial_code_prefix");
                int ownerCountryDialCodePrefix = Integer.parseInt(StringUtils.isBlank(ownernumberPrefix) || ownernumberPrefix.equals("0") ? "91" : ownernumberPrefix);
                String ownerNumber = mapOwner.get("mobile_no");

                if (frontVo.getWhatsappToken() != null) {
                    // log.info("call whatsapp service------");
                    whatsappService.SendMessageWithCountryDialCode(ownerNumber, whatsappMessage, textMessage,
                            frontVo.getWhatsappToken(), frontVo.getCompanyId(), frontVo.getUserFrontId(), templeteId, senderId, route, ownerCountryDialCodePrefix);
                } else {
                    // log.info("call simple text messageService------");
                    messageService.sendMsgWithCountryDialCode(ownerNumber, textMessage, frontVo.getCompanyId(), companyId, templeteId, senderId, route, ownerCountryDialCodePrefix);
                }
            }
        }
//        if (StringUtils.isNotBlank(frontVo.getOwnerNo())) {
//            String CSV = frontVo.getOwnerNo();
//            String[] values = CSV.split(",");
//            if (frontVo.getWhatsappToken() != null) {
//                System.out.println("call whatsapp service------");
//                for (int i = 0; i < values.length; i++) {
//                    String ownernumber = values[i];
//                    whatsappService.SendMessage(ownernumber, whatsappMessage,textMessage,
//                            frontVo.getWhatsappToken(), frontVo.getCompanyId(), frontVo.getUserFrontId(),templeteId,senderId,route);
//                }
//            } else {
//                System.out.println("call simple text messageService ------");
//                for (int i = 0; i < values.length; i++) {
//                    String ownernumberfortextmsg = values[i];
//                    messageService.sendMsg(ownernumberfortextmsg, textMessage, frontVo.getCompanyId(), companyId,templeteId,senderId,route);
//                }
//
//            }
//        }
//		if (frontVo.getContactNo() != null) {
//			if (frontVo.getWhatsappToken() != null) {
//				System.out.println("call whatsapp service------");
//				whatsappService.SendMessage(frontVo.getContactNo(), message, frontVo.getWhatsappToken(),
//						frontVo.getCompanyId(), frontVo.getUserFrontId());
//			} else {
//				System.out.println("call simple text messageService ------");
//				messageService.sendMsg(frontVo.getContactNo(), message, frontVo.getCompanyId(),	branchId);
//
//			}
//
//		}
//			if (salesVo.getContactVo().getMobNo() != null) {
//				messageService.sendMsg(salesVo.getContactVo().getMobNo(), message, salesVo.getCompanyId(),
//						salesVo.getCreatedBy());
//
//			}

    }

//    @PostMapping("{id}/pending/json")
//    @ResponseBody
//    public List<SalesVo> salesPendingListJson(@PathVariable String type, @PathVariable long id, HttpSession session)
//            throws NumberFormatException, ParseException {
//
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        System.out.println(type+",pos");
//        List<String> types=new  ArrayList<String>();
//        types.add("invoice");
//        types.add("pos");
//        List<SalesVo> salesvos = salesService.getListOfAllUnpaidBill(types,
//                Long.parseLong(session.getAttribute("branchId").toString()), 0, id);
//        if(CollectionUtils.isNotEmpty(salesvos)) {
//        	salesvos.forEach(p -> p.setContactVo(null));
//            salesvos.forEach(pr -> pr.setSalesItemVos(null));
//            salesvos.forEach(pr -> pr.setSalesVo(null));
//            salesvos.forEach(prc -> prc.setSalesAdditionalChargeVos(null));
//        }
//        return salesvos;
//    }

    @PostMapping("{accountCustomId}/pending/json")
    @ResponseBody
    public List<Map<String, String>> salesPendingList(@PathVariable String type, @PathVariable long accountCustomId,
                                                      HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_PENDING_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_PENDING_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_PENDING_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_PENDING_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_PENDING_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_PENDING_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());

        List<Long> contactIds = contactService.findContactIdByaccountCustomId(accountCustomId);

        List<Map<String, String>> pendingBillList = new ArrayList<Map<String, String>>();

//    	 log.info("contactIds :"+contactIds);
        if (contactIds.size() > 0) {
            List<String> salesTypes = new ArrayList<String>();
            if (type.equals(Constant.SALES_INVOICE)) {
                salesTypes.add(Constant.SALES_INVOICE);
                salesTypes.add(Constant.SALES_POS);
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                salesTypes.add(Constant.SALES_CREDIT_NOTE);
                salesTypes.add(Constant.SALES_POS_RETURN);
            }

            pendingBillList = salesService.getListofUnpaidSalesByConctactId(branchId, salesTypes, contactIds.get(0));
        }
        // log.info("pendingBillList :"+pendingBillList.size());

        return pendingBillList;
    }

    @PostMapping("{id}/json")
    @ResponseBody
    public SalesVo salesDetailsJson(@PathVariable long id, HttpSession session)
            throws NumberFormatException, ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_JSON_DETAIL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(id,
                Long.parseLong(session.getAttribute("branchId").toString()));

        if (salesVo != null) {
            double totalGrossAmount = 0.0;
            if (salesVo.getSalesItemVos() != null) {
                for (SalesItemVo salesItemVo : salesVo.getSalesItemVos()) {
                    totalGrossAmount += (salesItemVo.getNetAmount() - salesItemVo.getTaxAmount());
                }
                salesVo.setTotalGrossAmount(totalGrossAmount);
            }
            if (StringUtils.isNotBlank(salesVo.getFlatDiscountType()) && !StringUtils.equals(salesVo.getFlatDiscountType(), Constant.PERCENTAGE)) {
                Map<String, Object> salesFlatDiscountData = salesService.calculateFlatDiscountPercentageFromAmount(salesVo.getSalesId());
                if (!salesFlatDiscountData.isEmpty()) {
                    salesVo.setFlatDiscountInPercentage(Double.parseDouble(salesFlatDiscountData.get("flatDiscountPercentage").toString()));
                }
            }else {
                salesVo.setFlatDiscountInPercentage(salesVo.getFlatDiscount());
                salesVo.setFlatDiscountType(Constant.PERCENTAGE);
            }
            salesVo.setSalesVo(null);
            salesVo.setSalesItemVos(null);
            salesVo.setSalesAdditionalChargeVos(null);
        }
        return salesVo;
    }

    @PostMapping("{id}/data/json")
    @ResponseBody
    public Map<String, String> salesDATAJson(@PathVariable long id, HttpSession session)
            throws NumberFormatException, ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_DATA_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        Map<String, String> salesVo = salesService.getSalesDataBySalesId(id, Long.parseLong(session.getAttribute("branchId").toString()));

        return salesVo;
    }


    @RequestMapping("/{contactId}/datatable")
    @ResponseBody
    public JSONObject salesByContactDatatable(@PathVariable String type, @PathVariable long contactId, @RequestParam Map<String, String> allRequestParams,
                                              HttpSession session) throws NumberFormatException, ParseException {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CONTACT_CREDITNOTE_DATATABLE, session))
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);

        long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());

        List<String> salesTypes = new ArrayList<>(2);
        if (StringUtils.equals(type, Constant.SALES_INVOICE)) {
            salesTypes.add(Constant.SALES_INVOICE);
            salesTypes.add(Constant.SALES_POS);
        } else if (StringUtils.equals(type, Constant.SALES_CREDIT_NOTE)) {
            salesTypes.add(Constant.SALES_CREDIT_NOTE);
            salesTypes.add(Constant.SALES_POS_RETURN);
        }
        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        Date fromDate = dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString());
        Date toDate = dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString());

        int userType = StringUtils.equals(session.getAttribute(Constant.USER_TYPE).toString(), "2") ||
                (StringUtils.equals(session.getAttribute("parentUserType").toString(), "2") &&
                        Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString()) > 4)
                ? 1
                : 0;

        int totalRecord = salesService.countSalesDataForContactCreditNoteDatatable(companyId, branchId, userType, salesTypes, contactId, fromDate, toDate);

        int start = StringUtils.isNotBlank(allRequestParams.get(Constant.START))
                ? Integer.parseInt(allRequestParams.get(Constant.START))
                : 0;
        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");
        int length, page = 0, offset;

        if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
            page = start / length; // Calculate page number
            offset = page * length;
        } else {
            length = totalRecord;
            offset = 0;
        }

        List<Map<String, Object>> list = (totalRecord > 0)
                ? salesService.findSalesDataForContactCreditNoteDatatable(companyId, branchId, userType, salesTypes, contactId, fromDate, toDate, length, offset)
                : Collections.emptyList();

        JSONObject jsonMainObject = new JSONObject();
        JSONObject jsonMetaObject = new JSONObject();
        jsonMainObject.put(Constant.DRAW, Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        jsonMainObject.put(Constant.RECORDS_FILTERED, totalRecord);
        jsonMainObject.put(Constant.RECORDS_TOTAL, totalRecord);
        jsonMainObject.put(Constant.DATA, list);

        jsonMetaObject.put(Constant.PAGE, page);
        jsonMetaObject.put(Constant.PAGES, (int) Math.ceil((double) (totalRecord) / length));
        jsonMetaObject.put(Constant.PERPAGE, length);
        jsonMetaObject.put(Constant.TOTAL, totalRecord);

        jsonMainObject.put(Constant.META, jsonMetaObject);

        return jsonMainObject;

    }

    @RequestMapping("/item/{salesId}/datatable")
    @ResponseBody
    public DataTablesOutput<SalesItemVo> salesByItemDatatable(@PathVariable String type, @PathVariable long salesId,
                                                              @Valid DataTablesInput input, HttpSession session) throws NumberFormatException, ParseException {

        long branchId = Long.parseLong(session.getAttribute("branchId").toString());

        Specification<SalesItemVo> specification = new Specification<SalesItemVo>() {

            @Override
            public Predicate toPredicate(Root<SalesItemVo> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates.add(criteriaBuilder.equal(root.get("type"), type));
                predicates.add(criteriaBuilder.equal(root.get("salesVo").get("isDeleted"), 0));
                predicates.add(criteriaBuilder.equal(root.get("salesVo").get("branchId"), branchId));
                predicates.add(criteriaBuilder.equal(root.get("salesVo").get("salesId"), salesId));

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };

        DataTablesOutput<SalesItemVo> a = salesItemRepository.findAll(input, null, specification);

        a.getData().forEach(x -> {
            x.setSalesVo(null);
            x.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
        });

        return a;
    }

    @RequestMapping("/creditnote/{salesId}/datatable")
    @ResponseBody
    public JSONObject creditNoteSalesDatatable(@PathVariable String type, @PathVariable long salesId,
                                               @RequestParam Map<String, String> allRequestParams, HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CREDITNOTE_DATATABLE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREDITNOTE_DATATABLE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CREDITNOTE_DATATABLE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CREDITNOTE_DATATABLE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_DETAIL_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREDITNOTE_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());

        int totalRecords = salesService.getCountForSalesCreditNoteDatatable(salesId, branchId);

        int start = StringUtils.isNotBlank(allRequestParams.get(Constant.START))
                ? Integer.parseInt(allRequestParams.get(Constant.START))
                : 0;
        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");
        int length, page = 0, offset;

        if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
            page = start / length; // Calculate page number
            offset = page * length;
        } else {
            length = totalRecords;
            offset = 0;
        }

        List<Map<String, Object>> creditNoteData = (totalRecords > 0)
                ? salesService.findSalesDataForSalesCreditNoteDatatable(salesId, branchId, length, offset)
                : Collections.emptyList();

        JSONObject jsonMainObject = new JSONObject();
        JSONObject jsonMetaObject = new JSONObject();
        jsonMainObject.put(Constant.DRAW, Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        jsonMainObject.put(Constant.RECORDS_FILTERED, totalRecords);
        jsonMainObject.put(Constant.RECORDS_TOTAL, totalRecords);
        jsonMainObject.put(Constant.DATA, creditNoteData);

        jsonMetaObject.put(Constant.PAGE, page);
        jsonMetaObject.put(Constant.PAGES, (int) Math.ceil((double) (totalRecords) / length));
        jsonMetaObject.put(Constant.PERPAGE, length);
        jsonMetaObject.put(Constant.TOTAL, totalRecords);

        jsonMainObject.put(Constant.META, jsonMetaObject);

        return jsonMainObject;

    }

    @RequestMapping("/report")
    public ModelAndView invoiceReport(@PathVariable String type, HttpSession session, HttpServletRequest request)
            throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_REPORT;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_REPORT;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_REPORT;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_REPORT;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_REPORT;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_REPORT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
//      System.err.println("url---->"+ request.getRequestURI());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<String> salesTypes = new ArrayList<String>();

        ModelAndView view = null;
        if (MenuPermission.haveReportPermission(session, request.getRequestURI()) == 1) {
            if (type.equals(Constant.SALES_INVOICE)) {
                view = new ModelAndView("report/sales/invoice");
                salesTypes.add(Constant.SALES_INVOICE);
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                view = new ModelAndView("report/sales/creditnote");
                salesTypes.add(Constant.SALES_CREDIT_NOTE);
            }
            // view.addObject("ContactList", contactService
            // .contactList(Long.parseLong(session.getAttribute("branchId").toString()),
            // Constant.CONTACT_CUSTOMER));
            view.addObject("PaidAndTotalAmount",
                    salesService.getPaidAndTotalAmountAll(salesTypes,
                            Long.parseLong(session.getAttribute("branchId").toString()),
                            dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
                            dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString())));

            if (Long.parseLong(session.getAttribute("companyId").toString()) == Long.parseLong(session.getAttribute("branchId").toString())) {
                List<BranchDTO> branchDTOs = new ArrayList<>();
                branchDTOs = profileService
                        .getCustomListOfBranch(Long.parseLong(session.getAttribute("companyId").toString()));
                view.addObject("branchList", branchDTOs);
            } else {
                List<BranchDTO> branchDTOs = new ArrayList<>();
                branchDTOs = profileService
                        .getCustomBranchDetails(Long.parseLong(session.getAttribute("branchId").toString()));
                view.addObject("branchList", branchDTOs);
            }

        } else {
            view = new ModelAndView();
            view.setViewName(Constant.ACCESSDENIED);
        }
        return view;
    }

    @PostMapping("/report/paidandtotalamount/json")
    @ResponseBody
    public List<Map<Double, Double>> paidandtotalamount(@PathVariable String type,
                                                        @RequestParam Map<String, String> allRequestParams, HttpSession session)
            throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_PAID_TOTAL_AMOUNT_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_PAID_TOTAL_AMOUNT_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_PAID_TOTAL_AMOUNT_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_PAID_TOTAL_AMOUNT_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_PAID_TOTAL_AMOUNT_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_PAID_TOTAL_AMOUNT_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<String> salesTypes = new ArrayList<String>();
        if (type.equals(Constant.SALES_INVOICE)) {
            salesTypes.add(Constant.SALES_INVOICE);
        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
            salesTypes.add(Constant.SALES_CREDIT_NOTE);
        }

        String[] Daterange = allRequestParams.get("dateRange").split("-");
        String[] DueDaterange = allRequestParams.get("dueDaterange").split("-");
        long Customerid = 0;
        String countriesCode = "0", stateCode = "0", cityCode = "0";
        if (!allRequestParams.get("customerId").equals("")) {

            Customerid = Long.parseLong(allRequestParams.get("customerId"));
        }

        if (allRequestParams.get("countriesCode") != null && !allRequestParams.get("countriesCode").equals("")) {
            countriesCode = allRequestParams.get("countriesCode");
        }

        if (allRequestParams.get("stateCode") != null && !allRequestParams.get("stateCode").equals("")) {
            stateCode = allRequestParams.get("stateCode");
        }

        if (allRequestParams.get("cityCode") != null && !allRequestParams.get("cityCode").equals("")) {
            cityCode = allRequestParams.get("cityCode");
        }

        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        List<Long> branchList = new ArrayList<Long>();

        if (StringUtils.isNotBlank(allRequestParams.get("branchId"))) {
            branchList = Arrays.asList(allRequestParams.get("branchId").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
        } else {
            branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
        }

        return salesService.getPaidAndTotalAmountJson(salesTypes,
                branchList, dateFormat.parse(Daterange[0]),
                dateFormat.parse(Daterange[1]), dateFormat.parse(DueDaterange[0]), dateFormat.parse(DueDaterange[1]),
                Customerid, countriesCode, stateCode, cityCode);

    }

    @RequestMapping("/report/datatable")
    @ResponseBody
    public JSONObject reportSalesdatatable(@RequestParam Map<String, String> allRequestParams, HttpSession session)
            throws NumberFormatException, ParseException, IOException {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_INVOICE_LIST_REPORT, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        String[] dueDateRange = StringUtils.isNotBlank(allRequestParams.get("dueDaterange"))
                ? allRequestParams.get("dueDaterange").split("-")
                : new String[]{dateFormat.format(new Date()), dateFormat.format(new Date())};
        String[] dateRange = StringUtils.isNotBlank(allRequestParams.get(Constant.DATE_RANGE))
                ? allRequestParams.get(Constant.DATE_RANGE).split("-")
                : new String[]{dateFormat.format(new Date()), dateFormat.format(new Date())};

        Date startDate = dateFormat.parse(dateRange[0]);
        Date endDate = dateFormat.parse(dateRange[1]);

        Date dueStartDate = dateFormat.parse(dueDateRange[0]);
        Date dueEndDate = dateFormat.parse(dueDateRange[1]);

        List<Long> branchList = StringUtils.isNotBlank(allRequestParams.get(Constant.BRANCHID))
                ? Arrays.stream(allRequestParams.get(Constant.BRANCHID).split(",")).map(Long::parseLong).collect(Collectors.toList())
                : Collections.singletonList(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
        long customerId = StringUtils.isNotBlank(allRequestParams.get("customerId"))
                ? Long.parseLong(allRequestParams.get("customerId"))
                : 0;
        int taxType = StringUtils.isNotBlank(allRequestParams.get(Constant.GST))
                ? Integer.parseInt(allRequestParams.get(Constant.GST))
                : 0;
        String searchValue = StringUtils.isNotBlank(allRequestParams.get(Constant.SEARCH_VALUE))
                ? "%" + allRequestParams.get(Constant.SEARCH_VALUE) + "%"
                : "";

        int totalRecords = salesService.invoiceListDataCount(branchList, startDate, endDate, dueStartDate, dueEndDate,
                customerId, taxType, searchValue);

        int start = StringUtils.isNotBlank(allRequestParams.get(Constant.START))
                ? Integer.parseInt(allRequestParams.get(Constant.START))
                : 0;
        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");
        int length, page = 0, offset;

        if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
            page = start / length; // Calculate page number
            offset = page * length;
        } else {
            length = totalRecords;
            offset = 0;
        }

        List<Map<String, Object>> invoiceList = (totalRecords > 0)
                ? salesService.invoiceListData(branchList, startDate, endDate, dueStartDate, dueEndDate, customerId, taxType, searchValue, length, offset)
                : Collections.emptyList();

        JSONObject jsonMainObject = new JSONObject();
        JSONObject jsonMetaObject = new JSONObject();
        jsonMainObject.put(Constant.DRAW, Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        jsonMainObject.put(Constant.RECORDS_FILTERED, totalRecords);
        jsonMainObject.put(Constant.RECORDS_TOTAL, totalRecords);
        jsonMainObject.put(Constant.DATA, invoiceList);

        jsonMetaObject.put(Constant.PAGE, page);
        jsonMetaObject.put(Constant.PAGES, (int) Math.ceil((double)(totalRecords) / length));
        jsonMetaObject.put(Constant.PERPAGE, length);
        jsonMetaObject.put(Constant.TOTAL, totalRecords);

        jsonMainObject.put(Constant.META, jsonMetaObject);

        return jsonMainObject;

    }

    @GetMapping(value = {"{id}/pdf", "{id}/einvoicepdf"}) // Invoice PDF
    public void salesPDF(@PathVariable(value = "type") String type, @PathVariable long id, HttpSession session,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
//        System.err.println("in controller");
        //System.out.println("-->>>>>>>PERMSIOn" + MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT));
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_PDF;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_PDF;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_PDF;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_PDF;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_PDF;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_PDF;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int einvoicepdf = 0;
        String referer = request.getRequestURI();
        if (referer.contains("einvoicepdf")) {
            einvoicepdf = 1;
        }
        String salestype = type;
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        if (type.equals(Constant.SALES_ORDER)) {
            salestype = "salesorder";
        }
        if (MenuPermission.havePermission(session, salestype, Constant.PDF_EXCEL_PRINT) == 0) {
            response.sendRedirect("/accessdenied");
        }
//        int result = salesService.countBySalesIdAndBranchIdAndIsDeleted(id,Long.parseLong(session.getAttribute("branchId").toString()), 0);
//		if (result == 0) {
//			 response.sendRedirect("/404");
//		}

        int customernametype = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERNAME).getValue();
        SalesVo salesVo = salesService.findBySalesIdAndCompanyId(id,
                Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
        if (salesVo != null) {


            ReportSettingVo setting;
            //UpiQrCode

//            String qrBase64String = "";
//            CompanySettingVo fatoorahSettingVo = companySettingService.findByCompanyIdAndType(companyId, Constant.FATOORAHQRCODE);
//            if(fatoorahSettingVo.getValue()==1) {
//            	log.warning("HERE--FATOORAHQRCODE------is--ON");
//            	qrBase64String = getBase64ForQRCode(salesVo);
//            }else {
//            	log.warning("HERE--FATOORAHQRCODE------is--OFF");
//            }


            HashMap jasperParameter = new HashMap();
            jasperParameter.put("logoserver", FILE_UPLOAD_SERVER);
            jasperParameter.put("customernametype", customernametype);
            jasperParameter.put("sales_id", id);
            jasperParameter.put("realPath", session.getAttribute("realPath").toString());
            jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
            jasperParameter.put("user_front_id", salesVo.getBranchId());
            jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(salesVo.getTotal(), session.getAttribute("currencyName").toString()));
            jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
            jasperParameter.put("printDateFormat", dateFormatMasterService.getDateFormatMasterByBranchId(Long.parseLong(session.getAttribute("branchId").toString())).getJavaPattern());
            //jasperParameter.put("qrBase64String", qrBase64String);
            try {
            	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            	jasperParameter.put("fromdate",dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString()));
				jasperParameter.put("todate",dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString()));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
            	jasperParameter.put("year_interval", session.getAttribute(Constant.FINANCIAL_YEAR).toString());
            try {
                //	String companyUpi = userRepository.findCompanyUpiBYcompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
                UserFrontVo user = userRepository.findByCompanyId(Long.parseLong(session.getAttribute("branchId").toString()));
                String pa = user.getCompanyUpi();
                String UPI = "";
                if (StringUtils.isNotBlank(pa)) {
                    try {
                        Double amount = receiptService.findUpiReceiptAmountBySalesId(salesVo.getSalesId(),
                                Constant.UPI);
                        if (amount != null && amount > 0) {
                            UPI = Converter.getUPIString(pa, user.getName(), String.valueOf(id),
                                    salesVo.getPrefix() + salesVo.getSalesNo(), String.valueOf(amount), "INR");
                        } else {
                            if (salesVo.getPaidAmount() == 0) {
                                // log.warning("Paylater mode>>>");
                                UPI = Converter.getUPIString(pa, user.getName(), String.valueOf(id),
                                        salesVo.getPrefix() + salesVo.getSalesNo(), String.valueOf(salesVo.getTotal()), "INR");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // log.warning("PAYMENT TYPE NOT UPI AND ERROR RISE");
                    }
                }
                jasperParameter.put("UpiQrCode", UPI);
//                	System.err.println("upi is :"+UPI);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (type.equalsIgnoreCase(Constant.SALES_ESTIMATE)) {
                setting = reportService.findByTypeAndBranchId(Constant.SALES_ESTIMATE,
                        Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                if (setting == null) {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_INVOICE,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                }
                double total = 0;
//                for (SalesItemVo item : salesVo.getSalesItemVos()) {
//					total += item.getQty()*item.getPrice();
//				}
                //  jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(total,session.getAttribute("currencyName").toString()));
            } else {
                if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_DELIVERY_CHALLAN,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                } else {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_INVOICE,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                }
            }
            int printCount = printLogService.countByTypeIdAndType(id,Constant.SALES_INVOICE);
            jasperParameter.put("printCount",printCount);
            if (type.equals(Constant.SALES_INVOICE)) {
                jasperParameter.put("display_title", "Tax Invoice");
            } else if (type.equals(Constant.SALES_ESTIMATE)) {
                jasperParameter.put("display_title", "Estimate");
            } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
                jasperParameter.put("display_title", "Bill Of Supply");
            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                jasperParameter.put("display_title", "Credit Note");
            } else if (type.equals(Constant.SALES_ORDER)) {
                jasperParameter.put("display_title", "Sales Order");
            } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESGARMENTDESIGN);
                if (settingVo.getValue() == 1) {
                    jasperParameter.put("display_title", "Estimate");
                } else {
                    jasperParameter.put("display_title", "Delivery Challan");
                }
            }

            jasperParameter.put("path", JASPER_REPORT_PATH + File.separator);

            int decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());


            String decimalFormate = numberUtil.getFormateOnDecimal(decimalNumber);
            jasperParameter.put("decimalFormate", decimalFormate);
//        System.err.println("calllll-------------");

            jasperParameter.put("einvoice", einvoicepdf);
            if (setting != null) {
                // log.info("--------JS----------------------------");
//                System.err.println("setting not null");
                if (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_ORDER)
                        || type.equals(Constant.SALES_ESTIMATE) || type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    if (setting.getReportVo().getReportFormateType() != null) {
                        if (setting.getReportVo().getReportFormateType().equals("html")) {
                            response.sendRedirect("/sales/" + type + "/htmlpdf/" + id);
                        } else {
                            try {
                                if(type.equals(Constant.SALES_ORDER) && companyId == 32797){
                                    jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                                    + "Thermal_80mm_SO-1" + ".jrxml",
                                            "Thermal_80mm_SO-1", response);
                                } else {
                                jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                                + setting.getReportVo().getReport() + ".jrxml",
                                            salesVo.getPrefix() + salesVo.getSalesNo(), response);
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                            + setting.getReportVo().getReport() + ".jrxml",
                                    salesVo.getPrefix() + salesVo.getSalesNo(), response);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
//                else if (type.equals(Constant.SALES_ESTIMATE)) {
//					if (setting.getReportVo().getReportFormateType() != null) {
//						if (setting.getReportVo().getReportFormateType().equals("html")) {
//							response.sendRedirect("/sales/" + type + "/htmlpdf/" + id);
//						}
//					}
//				}
                else {
//                    System.err.println("delivery challan");

                    try {


                        jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                        + setting.getReportVo().getReport() + ".jrxml",
                                salesVo.getPrefix() + salesVo.getSalesNo(), response);


                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                try {
                    if (type.equals(Constant.SALES_CREDIT_NOTE)) {
//                        System.err.println("I AM HERE  MAN ");
                        jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/creditnote/creditnote-1.jrxml",
                                salesVo.getPrefix() + salesVo.getSalesNo(), response);
                    } else {
                        if (!type.equals(Constant.SALES_INVOICE)) {

                            if(type.equals(Constant.SALES_ORDER) && companyId == 32797){
                                jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                                + "Thermal_80mm_SO-1" + ".jrxml",
                                        "Thermal_80mm_SO-1", response);
                            } else {
                            jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                            + setting.getReportVo().getReport() + ".jrxml",
                                    salesVo.getPrefix() + salesVo.getSalesNo(), response);
                            }
                        } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                            jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                            + "deliverychallan.jrxml",
                                    salesVo.getPrefix() + salesVo.getSalesNo(), response);
                        }
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
//                System.err.println("setting is null");
                try {
                    if (type.equals(Constant.SALES_CREDIT_NOTE)) {
//                        System.err.println("I AM HERE  MAN ");
                        jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/creditnote/creditnote-1.jrxml",
                                salesVo.getPrefix() + salesVo.getSalesNo(), response);
                    } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                        jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/"
                                        + "deliverychallan.jrxml",
                                salesVo.getPrefix() + salesVo.getSalesNo(), response);
                    } else {

                        jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/sales/invoice-1.jrxml",
                                salesVo.getPrefix() + salesVo.getSalesNo(), response);
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
//        System.err.println("setting is null");
//        System.err.println("in controller");
            if (printLogService.verifyPrintLogCondition(setting) && (type.equals(Constant.SALES_INVOICE)||type.equals(Constant.SALES_ORDER))) {
                printLogService.savePrintLog(id,type.equals(Constant.SALES_INVOICE)? Constant.SALES_INVOICE : Constant.SALES_ORDER_NEW,"/sales/{id}/pdf");
            }
        } else {
            response.sendRedirect("/404");
        }

    }

    public String getBase64ForQRCode(SalesVo salesVo) {
        // log.severe("START=============getBase64ForQRCode===============START");
        String qrBarcodeHash = "";
        long userFrontId = salesVo.getBranchId();
        String sellerName = userRepository.findNameByUserFrontId(userFrontId);
        String vatNumber = userRepository.findVatNoByUserFrontId(userFrontId);
        String totalAmount = String.valueOf(salesVo.getTotal());
        String totalTaxAmount = String.valueOf(salesVo.getSalesItemVos().stream().mapToDouble(p -> p.getTaxAmount()).sum());

        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(salesVo.getCreatedOn());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String invoiceDate = CurrentDateTime.getTimeStampDate(date, "Asia/Kolkata");
        // log.warning("sellerName------------>"+sellerName);
        // log.warning("vatNumber------------>"+vatNumber);
        // log.warning("invoiceDate------------>"+invoiceDate);
        // log.warning("totalAmount------------>"+totalAmount);
        // log.warning("totalTaxAmount------------>"+totalTaxAmount);

        if(StringUtils.isNotBlank(sellerName) && StringUtils.isNotBlank(vatNumber) && StringUtils.isNotBlank(invoiceDate) &&
                StringUtils.isNotBlank(totalAmount) && StringUtils.isNotBlank(totalTaxAmount)) {
            qrBarcodeHash = QRBarcodeEncoder.encode(new Seller(sellerName),
                    new TaxNumber(vatNumber),
                    new InvoiceDate(invoiceDate),
                    new InvoiceTotalAmount(totalAmount), new InvoiceTaxAmount(totalTaxAmount));
        }
//        log.info(qrBarcodeHash);
        // log.severe("END=============getBase64ForQRCode===============END");
        return qrBarcodeHash;
    }


    @PostMapping("{id}/update/status")
    @ResponseBody
    public String updateStatus(@PathVariable("id") long salesId, @PathVariable("type") String type,
                               @RequestParam("status") String status,
                               @RequestParam(name = "reason", defaultValue = "", required = false) String reason, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_STATUS_UPDATE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_STATUS_UPDATE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_STATUS_UPDATE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_STATUS_UPDATE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_STATUS_UPDATE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_STATUS_UPDATE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int result = salesService.countBySalesIdAndBranchIdAndIsDeleted(salesId, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (result == 0) {
            return "404";
        } else {
            SalesDTO salesDTO = salesRepository.findCustomSalesBySalesId(salesId);

            if (StringUtils.isNotBlank(status) && StringUtils.equals(status, Constant.PARTIALLY_CANCELLED)) {
                List<SalesItemQtyDTO> salesItemQtyDTOs = salesRepository.findSalesItemQtyDetailsBySalesId(salesId);
                if (!salesItemQtyDTOs.isEmpty()) {
                    for (int j = 0; j < salesItemQtyDTOs.size(); j++) {
                        SalesItemQtyDTO salesItemQtyDTO = salesItemQtyDTOs.get(j);
                        if (salesItemQtyDTO != null) {
                            double partialQty = salesItemQtyDTO.getQty() - (salesItemQtyDTO.getInvoiceQty() + salesItemQtyDTO.getDcQty());
                            double partialFreeQty = salesItemQtyDTO.getFreeQty() - (salesItemQtyDTO.getInvoiceFreeQty() + salesItemQtyDTO.getDcFreeQty());
                            salesService.updateSalesItemPartialQtyPlus(salesItemQtyDTO.getSalesItemId(), partialQty,partialFreeQty);
                            salesService.updateSalesItemPartialCancel(salesItemQtyDTO.getSalesItemId(), 1);
                        }
                    }
                }
                salesService.updatePartiallyStatusAndReasonBySalesId(status, salesId, Long.parseLong(session.getAttribute("userId").toString()),
                        CurrentDateTime.getCurrentDate(), reason);
            } else if (StringUtils.isNotBlank(status) && StringUtils.equals(status, Constant.CANCELLED)) {
                salesService.updateStatusAndReasonBySalesId(status, salesId, Long.parseLong(session.getAttribute("branchId").toString()), type,
                        Long.parseLong(session.getAttribute("userId").toString()),
                        CurrentDateTime.getCurrentDate(), reason);
            } else {
                salesService.updateStatusAndReasonBySalesId(status, salesId, Long.parseLong(session.getAttribute("branchId").toString()), type,
                        Long.parseLong(session.getAttribute("userId").toString()),
                        CurrentDateTime.getCurrentDate(), reason);
            }
            String salesType = "";
            String salesNo = "";
            String fromStatus = "";
            if (salesDTO != null) {
                salesType = salesDTO.getType();
                salesNo = salesDTO.getPrefix() + salesDTO.getSalesNo();
                fromStatus = salesDTO.getStatus();
            }
            salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, salesId, salesType, salesNo,
                    0, fromStatus, status, 0, "", "", 0, session);
//			salesService.updateStatusAndStockDeleteBySalesId(type, status, salesId,
//	                Long.parseLong(session.getAttribute("branchId").toString()),
//	                Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());

            return "success";
        }

    }

    @PostMapping("/{id}/barcodedetailsforposReturn")
    @ResponseBody
    public SalesVo barcodedetailsforposReturn(HttpSession session, @PathVariable(value = "type") String type,
                                              @PathVariable(value = "id") long id) throws Exception {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_POS_RETURN_BARCODE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_POS_RETURN_BARCODE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_POS_RETURN_BARCODE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_POS_RETURN_BARCODE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_POS_RETURN_BARCODE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_POS_RETURN_BARCODE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(id,
                Long.parseLong(session.getAttribute("branchId").toString()));
        if (salesVo != null) {
            // if(salesVo.getSalesItemVos().)
            salesVo.getSalesItemVos().forEach(p -> {
                p.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
                p.setSalesVo(null);
            });
            if (salesVo.getContactVo() != null) {
                salesVo.getContactVo().setContactAddressVos(null);
            }
        }
        return salesVo;
    }

//    @Async
//    private void sendSMS(SalesVo salesVo,long companyId,String whatsappToken,String name) {
//
//        String message = "";
//
//        if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
//            //long companyId = Long.parseLong(session.getAttribute("companyId").toString());
//            CompanySettingVo allowInvoicePOSSMS = companySettingService.findByCompanyIdAndType(companyId,
//                    Constant.ALLOWINVOICESMS);
//
//            CompanySettingVo allowInvoiceWhatsapp = companySettingService.findByCompanyIdAndType(companyId,
//                    Constant.ALLOWINVOICEWHATSAPP);
//            message +="Thank you for your purchase. Check your invoice\n"
//               		+ ""+urlService.shortenUrl(BASEURL + "media/download/sales/" + salesVo.getPdfToken() + "/pdf")+" We welcome you again!! Regards\n"
//               		+ ""+ name+" \n"
//               		+ "system developed by vasyerp.com";
//
////            message += "Thank you for your purchase. Check your invoice \n";
////            message += urlService.shortenUrl(BASEURL + "media/download/sales/" + salesVo.getPdfToken() + "/pdf");
////            message += " We welcome you again!! Regards \n" + name;
//            //String token = (String) session.getAttribute("whatsappToken");
//            String token = whatsappToken;
//            if (salesVo.getContactVo().getMobNo() != null || salesVo.getContactVo().getWhatsappNo() != null) {
//                String m = messageService.generateMessage(salesVo);
//                if (m.equals("")) {
//
//                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
//                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
//                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), message, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), message, salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE);
//                        }
//                    }
//                    if (salesVo.getContactVo().getWhatsappNo() != null
//                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
//                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(),
//                                    message, token, companyId, companyId);
//                        }
//                    }
//
//                } else {
//                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
//                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
//                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), m, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), message, salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE);
//                        }
//                    }
//                    if (salesVo.getContactVo().getWhatsappNo() != null
//                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
//                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(), m,
//                                    token, companyId, companyId);
//                        }
//                    }
//                }
//            }
//
//        }
//
//    }

    @Async
    protected void sendSMS(SalesVo salesVo, long companyId, String whatsappToken, String name, HttpSession session, HttpServletRequest request) {

        String message = "";

        if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
            //long companyId = Long.parseLong(session.getAttribute("companyId").toString());
            CompanySettingVo allowInvoicePOSSMS = companySettingService.findByCompanyIdAndType(companyId,
                    Constant.ALLOWINVOICESMS);
            long branchId = salesVo.getBranchId();
            UserFrontVo branchVo = userRepository.findByCompanyId(branchId);
            CompanySettingVo allowInvoiceWhatsapp = companySettingService.findByCompanyIdAndType(companyId,
                    Constant.ALLOWINVOICEWHATSAPP);
            // log.warning("frontName--------->"+name);
            String userFrontName = TemplateDefault.getSubStringByCharLength(name, 29);
            // log.warning("userFrontName--------->"+userFrontName);
            message += "Thank you for your purchase. Check your invoice\n"
                    + urlService.shortenUrl(BASEURL + "feedback/" + salesVo.getPdfToken()) + " We welcome you again!! Regards\n"
                    + userFrontName + " \n"
                    + "system developed by vasyerp.com";

//            message += "Thank you for your purchase. Check your invoice \n";
//            message += urlService.shortenUrl(BASEURL + "media/download/sales/" + salesVo.getPdfToken() + "/pdf");
//            message += " We welcome you again!! Regards \n" + name;
            //String token = (String) session.getAttribute("whatsappToken");
            String token = whatsappToken;
            if (salesVo.getContactVo().getMobNo() != null || salesVo.getContactVo().getWhatsappNo() != null) {
                Map<String, String> map = messageService.generateMessageForSales(salesVo, Constant.SMSFOR_USER);
                String senderId = "VSYERP";
                if (map.isEmpty()) {
                    String branchSenderId = userRepository.findSenderIdByuserFrontId(companyId);
                    if (StringUtils.isNotBlank(branchSenderId)) {
                        senderId = branchSenderId;
                    }
                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), message, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), message,
//                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE,senderId,"defaultMSG",4);
                            messageService.sendMsgWithCountryDialCodeAsync(salesVo.getContactVo().getMobNo(), message,
                                    salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(), SMSCONSTANT.POS_ORDER_MESSAGE, senderId, "defaultMSG", 4, salesVo.getContactVo().getCountryDialCodePrefix());
                        }
                    }
                    if (salesVo.getContactVo().getWhatsappNo() != null
                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(),
//                                    message, token, companyId, companyId);
                            whatsappController.sendTextMessageToCustomer((salesVo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesVo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesVo.getContactVo().getWhatsappNo(),
                                    message, token, companyId, companyId);

                            try {
                                String absolutepath = getsalesPDFpath(salesVo.getSalesId(), salesVo.getType(), session, request);
//								System.err.println("-------------send whtasapp--------"+absolutepath);
//								whatsappService.SendDocument(salesVo.getContactVo().getWhatsappNo(), message,
//										branchVo.getWhatsappToken(), absolutepath);
                                whatsappService.SendDocument((salesVo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesVo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesVo.getContactVo().getWhatsappNo(), message,
                                        branchVo.getWhatsappToken(), absolutepath);
                            } catch (IOException e) {
//								System.err.println("eeee" + e);
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }

                } else {
                    if (StringUtils.isNotBlank(map.get("senderId"))) {
                        senderId = map.get("senderId");
                    }
                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), m, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), map.get("textMessage"),
//                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),
//                            		map.get("templateId"),senderId,"templateMSG",Integer.parseInt(map.get("route")));
                            messageService.sendMsgWithCountryDialCodeAsync(salesVo.getContactVo().getMobNo(), map.get("textMessage"),
                                    salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),
                                    map.get("templateId"), senderId, "templateMSG", Integer.parseInt(map.get("route")), salesVo.getContactVo().getCountryDialCodePrefix());
                        }
                    }
                    if (salesVo.getContactVo().getWhatsappNo() != null
                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(), map.get("whatsappMessage"),
//                                    token, companyId, companyId);
                            whatsappController.sendTextMessageToCustomer((salesVo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesVo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesVo.getContactVo().getWhatsappNo(), map.get("whatsappMessage"),
                                    token, companyId, companyId);
                        }
                    }

                }
            }

        }

    }

    public String getsalesPDFpath(long salesId, String type, HttpSession session, HttpServletRequest request)
            throws IOException {
        ReportSettingVo setting = reportService.findByTypeAndBranchId(Constant.SALES_INVOICE,
                Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
        HashMap jasperParameter;
        String absolutepath = "";

        SalesVo salesVo = salesService.findBySalesIdAndBranchId(salesId, Long.parseLong(session.getAttribute("branchId").toString()));
        // System.out.println("-----------=" + salesVo.getSalesId());
        jasperParameter = new HashMap();
        jasperParameter.put("sales_id", salesId);
        jasperParameter.put("realPath", session.getAttribute("realPath").toString());
        jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
        jasperParameter.put("user_front_id", Long.parseLong(session.getAttribute("branchId").toString()));
        jasperParameter.put("amount_in_word",
                NumberToWord.getNumberToWord(salesVo.getTotal(), session.getAttribute("currencyName").toString()));

        if (type.equals(Constant.SALES_INVOICE)) {
            jasperParameter.put("display_title", "Invoice");
        }

        jasperParameter.put("path", jasperParameter + File.separator);
///

        int customernametype = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.CUSTOMERNAME).getValue();


        jasperParameter = new HashMap();
        jasperParameter.put("logoserver", FILE_UPLOAD_SERVER);
        jasperParameter.put("customernametype", customernametype);
        jasperParameter.put("sales_id", salesId);
        jasperParameter.put("realPath", session.getAttribute("realPath").toString());
        jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
        jasperParameter.put("user_front_id", Long.parseLong(session.getAttribute("branchId").toString()));
        jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(salesVo.getTotal(), session.getAttribute("currencyName").toString()));
        jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
        //jasperParameter.put("qrBase64String", qrBase64String);
        try {
            //	String companyUpi = userRepository.findCompanyUpiBYcompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
            UserFrontVo user = userRepository.findByCompanyId(Long.parseLong(session.getAttribute("branchId").toString()));
            String pa = user.getCompanyUpi();
            String UPI = "";
            if (StringUtils.isNotBlank(pa)) {
                try {
                    Double amount = receiptService.findUpiReceiptAmountBySalesId(salesVo.getSalesId(),
                            Constant.UPI);
                    if (amount != null && amount > 0) {
                        UPI = Converter.getUPIString(pa, user.getName(), String.valueOf(salesId),
                                salesVo.getPrefix() + salesVo.getSalesNo(), String.valueOf(amount), "INR");
                    } else {
                        if (salesVo.getPaidAmount() == 0) {
                            // log.warning("Paylater mode>>>");
                            UPI = Converter.getUPIString(pa, user.getName(), String.valueOf(salesId),
                                    salesVo.getPrefix() + salesVo.getSalesNo(), String.valueOf(salesVo.getTotal()), "INR");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // log.warning("PAYMENT TYPE NOT UPI AND ERROR RISE");
                }
            }
            jasperParameter.put("UpiQrCode", UPI);
//                	System.err.println("upi is :"+UPI);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (type.equals(Constant.SALES_INVOICE)) {
            jasperParameter.put("display_title", "Tax Invoice");
        } else if (type.equals(Constant.SALES_ESTIMATE)) {
            jasperParameter.put("display_title", "Estimate");
        } else if (type.equals(Constant.SALES_BILL_OF_SUPPLY)) {
            jasperParameter.put("display_title", "Bill Of Supply");
        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
            jasperParameter.put("display_title", "Credit Note");
        } else if (type.equals(Constant.SALES_ORDER)) {
            jasperParameter.put("display_title", "Sales Order");
        } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
            CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALESGARMENTDESIGN);
            if (settingVo.getValue() == 1) {
                jasperParameter.put("display_title", "Estimate");
            } else {
                jasperParameter.put("display_title", "Delivery Challan");
            }
        }

        jasperParameter.put("path", JASPER_REPORT_PATH + File.separator);

        int decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());


        String decimalFormate = numberUtil.getFormateOnDecimal(decimalNumber);
        jasperParameter.put("decimalFormate", decimalFormate);
        jasperParameter.put("printDateFormat",
                dateFormatMasterService.getDateFormatMasterByBranchId(salesVo.getBranchId()).getJavaPattern());

        if (setting != null) {
            // log.info("--------JS----------------------------");
//                System.err.println("setting not null");
            if (type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_ORDER)) {
                if (setting.getReportVo().getReportFormateType() != null) {
                    if (setting.getReportVo().getReportFormateType().equals("html")) {
                        // response.sendRedirect("/sales/"+type+"/htmlpdf/"+id);
                    } else {
                        absolutepath = jasperExporter.jasperExporterPDFabsolutepath(jasperParameter, JASPER_REPORT_PATH + File.separator
                                + "/sales/" + setting.getReportVo().getReport() + ".jrxml", request, salesVo.getPrefix() + salesVo.getSalesNo());
                    }
                } else {
                    absolutepath = jasperExporter.jasperExporterPDFabsolutepath(jasperParameter, JASPER_REPORT_PATH + File.separator
                            + "/sales/" + setting.getReportVo().getReport() + ".jrxml", request, salesVo.getPrefix() + salesVo.getSalesNo());
                }
            }
        } else {

            absolutepath = jasperExporter.jasperExporterPDFabsolutepath(jasperParameter, JASPER_REPORT_PATH + File.separator
                    + "/sales/invoice-1.jrxml", request, salesVo.getPrefix() + salesVo.getSalesNo());

        }
        return absolutepath;


    }

    @PostMapping("/checkmailid")
    @ResponseBody
    public String checkmailid(@RequestParam("salesids") String salesids, HttpServletRequest servletRequest) {


        String[] idlist = salesids.split("\\s*,\\s*");

        String result = "";

        for (String s : idlist) {
            SalesVo salesvo = salesService.findBySalesId(Long.parseLong(s));

            if (salesvo != null) {
                if (salesvo.getContactVo().getEmail() == null || salesvo.getContactVo().getEmail().equals("")) {
//                    System.err.println(salesvo.getSalesNo() + " Mail not send");
                    result = result + salesvo.getContactVo().getFirstName() + " " + salesvo.getContactVo().getLastName()
                            + " , ";
                }
//				else {
//                    System.err.println(salesvo.getContactVo().getFirstName() + " Mail send");
//                }
            }
        }
        if (result.length() != 0) {
            result = result.substring(0, result.length() - 1) + " - This sale's contact doesn't have email address";
        } else {
            result = "success";
        }
        return result;
    }

    @PostMapping("/sendmail")
    @ResponseBody
    public String sendmail(@RequestParam("salesids") String salesids, HttpServletRequest servletRequest,
                           HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_SEND_MAIL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        String[] idlist = salesids.split("\\s*,\\s*");

        String result = "";

        for (String s : idlist) {
            //SalesVo salesvo = salesService.findBySalesId(Long.parseLong(s));
            SalesVo salesvo = salesService.findBySalesIdAndBranchId(Long.parseLong(s), Long.parseLong(session.getAttribute("branchId").toString()));

            if (salesvo != null && salesvo.getIsDeleted() == 0) {
                if (salesvo.getContactVo().getEmail() == null || salesvo.getContactVo().getEmail().equals("")) {
//                    System.err.println(salesvo.getPrefix() + salesvo.getSalesNo() + " Mail not send");
                    result = result + salesvo.getPrefix() + salesvo.getSalesNo() + ",";
                } else {
//                    System.err.println(salesvo.getPrefix() + salesvo.getSalesNo() + " Mail send");

                    if (salesvo.getContactVo().getEmail() != null) {


                        String body = Converter.convertToHtml(servletRequest,
                                "/media/download/sales/" + salesvo.getPdfToken() + "/mail");
                        sendGridEmailService.sendHTML(from, salesvo.getContactVo().getEmail(), "Invoice", body,
                                Long.parseLong(session.getAttribute("companyId").toString()));

                    }
                }
            }
        }
        if (result.length() != 0) {
            result = result.substring(0, result.length() - 1) + " - This sale's contact doesn't have email address";
        } else {
            result = "success";
        }
        return result;
    }

    @PostMapping("/checkcontactno")
    @ResponseBody
    public String checkContactNo(@RequestParam("salesids") String salesids, HttpServletRequest servletRequest, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CHECK_CONTACT_NO, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        String[] idlist = salesids.split("\\s*,\\s*");

        String result = "";

        for (String s : idlist) {
            //SalesVo salesvo = salesService.findBySalesId(Long.parseLong(s));
            SalesVo salesvo = salesService.findBySalesIdAndBranchId(Long.parseLong(s), Long.parseLong(session.getAttribute("branchId").toString()));
            if (salesvo != null && salesvo.getIsDeleted() == 0) {
                if (salesvo.getContactVo().getMobNo() == null || salesvo.getContactVo().getMobNo().equals("")) {
//                    System.err.println(salesvo.getSalesNo() + " sms not send");
                    result = result + salesvo.getContactVo().getFirstName() + " " + salesvo.getContactVo().getLastName()
                            + " , ";
                }
//				else {
//                    System.err.println(salesvo.getContactVo().getFirstName() + " Mail send");
//                }
            }
        }
        if (result.length() != 0) {
            result = result.substring(0, result.length() - 1) + " - This sale's contact doesn't have Contact Number";
        } else {
            result = "success";
        }
        return result;
    }

    @PostMapping("/sendsms")
    @ResponseBody
    public String sendSms(@RequestParam("salesids") String salesids, HttpServletRequest request, @RequestParam("type") String type,
                          HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_SEND_SMS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long companyId = Long.parseLong(session.getAttribute("branchId").toString());
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        CompanySettingVo allowInvoicePOSSMS = companySettingService.findByCompanyIdAndType(companyId,
                Constant.ALLOWINVOICESMS);
        String whatsappToken = "";
        if (session.getAttribute("whatsappToken") != null && StringUtils.isNotBlank(session.getAttribute("whatsappToken").toString())) {
            whatsappToken = session.getAttribute("whatsappToken").toString();
        }
        UserFrontVo branchVo = userRepository.findByCompanyId(branchId);
        CompanySettingVo allowInvoiceWhatsapp = companySettingService.findByCompanyIdAndType(companyId,
                Constant.ALLOWINVOICEWHATSAPP);
        String[] idlist = salesids.split("\\s*,\\s*");

        String result = "";

        for (String s : idlist) {
            SalesVo salesvo = salesService.findBySalesIdAndBranchId(Long.parseLong(s), Long.parseLong(session.getAttribute("branchId").toString()));
            if (salesvo != null && salesvo.getIsDeleted() == 0) {
                if (salesvo.getContactVo().getMobNo() == null || salesvo.getContactVo().getMobNo().equals("")) {
//                    System.err.println(salesvo.getSalesNo() + " sms not send");
                    // result=result+salesvo.getContactVo().getFirstName()+"
                    // "+salesvo.getContactVo().getLastName()+" , " ;
                    result = "fail";
                } else {
//                    System.err.println(salesvo.getContactVo().getFirstName() + " sms send");
                    String message = "";

                    if (salesvo.getType().equals(Constant.SALES_INVOICE)) {
                        // Thank you for doing shopping with us. Click here for paperless invoice
                        // "invoice pdf link". We welcome you again!!
                        String name = session.getAttribute("Name").toString();
                        // log.warning("frontName--------->"+name);
                        String userFrontName = TemplateDefault.getSubStringByCharLength(name, 29);
                        // log.warning("userFrontName--------->"+userFrontName);


                        message += "Thank you for your purchase. Check your invoice\n"
                                + urlService.shortenUrl(BASEURL + "media/download/sales/" + salesvo.getPdfToken() + "/pdf") + " We welcome you again!! Regards\n"
                                + userFrontName + " \n"
                                + "system developed by vasyerp.com";
                        if (salesvo.getContactVo().getMobNo() != null || salesvo.getContactVo().getWhatsappNo() != null) {
                            Map<String, String> map = messageService.generateMessageForSales(salesvo, Constant.SMSFOR_USER);
                            String senderId = "VSYERP";
                            if (map.isEmpty()) {
                                String branchSenderId = userRepository.findSenderIdByuserFrontId(salesvo.getCompanyId());
                                if (StringUtils.isNotBlank(branchSenderId)) {
                                    senderId = branchSenderId;
                                }
                                if (salesvo.getContactVo().getMobNo() != null && !salesvo.getContactVo().getMobNo().equals("")) {
                                    if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                                        if (type.equalsIgnoreCase("sms")) {
//		         						messageService.sendMsgAsync(salesvo.getContactVo().getMobNo(), message, salesvo.getContactVo().getCompanyId(),
//		                        			 salesvo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE,senderId,"defaultMSG",4);
                                            messageService.sendMsgWithCountryDialCodeAsync(salesvo.getContactVo().getMobNo(), message, salesvo.getContactVo().getCompanyId(),
                                                    salesvo.getContactVo().getCompanyId(), SMSCONSTANT.POS_ORDER_MESSAGE, senderId, "defaultMSG", 4, salesvo.getContactVo().getCountryDialCodePrefix());
                                        }
                                    }
                                }
                                if (salesvo.getContactVo().getWhatsappNo() != null
                                        && !salesvo.getContactVo().getWhatsappNo().equals("")) {
                                    if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
                                        if (type.equalsIgnoreCase("whatsapp")) {
//	                                     whatsappController.sendTextMessageToCustomer(salesvo.getContactVo().getWhatsappNo(),
//	                                             message, whatsappToken, companyId, companyId);
                                            whatsappController.sendTextMessageToCustomer((salesvo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesvo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesvo.getContactVo().getWhatsappNo(),
                                                    message, whatsappToken, companyId, companyId);

                                            try {
                                                String absolutepath = getsalesPDFpath(salesvo.getSalesId(), salesvo.getType(), session, request);
//	         								System.err.println("-------------send whtasapp--------"+absolutepath);
//	         								whatsappService.SendDocument(salesvo.getContactVo().getWhatsappNo(), message,
//	         										branchVo.getWhatsappToken(), absolutepath);
                                                whatsappService.SendDocument((salesvo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesvo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesvo.getContactVo().getWhatsappNo(), message,
                                                        branchVo.getWhatsappToken(), absolutepath);
                                            } catch (IOException e) {
//	         								System.err.println("eeee" + e);
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                            } else {
                                if (StringUtils.isNotBlank(map.get("senderId"))) {
                                    senderId = map.get("senderId");
                                }
                                if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                                    if (type.equalsIgnoreCase("sms")) {
//		                        	 messageService.sendMsgAsync(salesvo.getContactVo().getMobNo(), map.get("textMessage"), salesvo.getContactVo().getCompanyId(),
//		                        			 salesvo.getContactVo().getCompanyId(),map.get("templateId"),senderId,"templateMSG",Integer.parseInt(map.get("route")));
                                        messageService.sendMsgWithCountryDialCodeAsync(salesvo.getContactVo().getMobNo(), map.get("textMessage"), salesvo.getContactVo().getCompanyId(),
                                                salesvo.getContactVo().getCompanyId(), map.get("templateId"), senderId, "templateMSG", Integer.parseInt(map.get("route")), salesvo.getContactVo().getCountryDialCodePrefix());
                                    }
                                }

                                if (salesvo.getContactVo().getWhatsappNo() != null
                                        && !salesvo.getContactVo().getWhatsappNo().equals("")) {
                                    if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
                                        if (type.equalsIgnoreCase("whatsapp")) {
//	                                      whatsappController.sendTextMessageToCustomer(salesvo.getContactVo().getWhatsappNo(), map.get("whatsappMessage"),
//	                                    		  whatsappToken, companyId, companyId);
                                            whatsappController.sendTextMessageToCustomer((salesvo.getContactVo().getCountryDialCodePrefixWhatsapp() == 0 ? 91 : salesvo.getContactVo().getCountryDialCodePrefixWhatsapp()) + salesvo.getContactVo().getWhatsappNo(),
                                                    map.get("whatsappMessage"), whatsappToken, companyId, companyId);
                                        }
                                    }
                                }

                            }
                        } else {

                        }
//                        if (salesvo.getContactVo().getMobNo() != null) {
//                            String m = messageService.generateMessage(salesvo);
//                            if (m.equals("")) {
//                                messageService.sendMsgAsync(salesvo.getContactVo().getMobNo(), message, salesvo.getContactVo().getCompanyId(), salesvo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE);
//
//                                //messageService.sendMsg(salesvo.getContactVo().getMobNo(), message, session);
//                            } else {
//                                //messageService.sendMsg(salesvo.getContactVo().getMobNo(), m, session);
//                                messageService.sendMsgAsync(salesvo.getContactVo().getMobNo(), m, salesvo.getContactVo().getCompanyId(), salesvo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE);
//
//                            }
//                        }
                    }

                    result = "success";
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/{id}/exportexcel")
    public void exportSalesInvoiceExcel(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                                        @PathVariable(value = "id") long id) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_EXPORT_EXCEL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        SalesVo salesVo = salesService.findBySalesIdAndCompanyId(id,
                Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
        if (salesVo != null && salesVo.getIsDeleted() == 0) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                JasperPrint jasperPrint;
                HashMap jasperParameter = new HashMap();
                String filename = salesVo.getPrefix() + salesVo.getSalesNo() +".xls";
                List<JasperPrint> sheets = new ArrayList<JasperPrint>();
                String requestURL = request.getRequestURL().toString();
                if (StringUtils.containsIgnoreCase(requestURL, Constant.SALES_CREDIT_NOTE)) {
                    jasperParameter.put("report_name", "Credit No.");
                    filename= salesVo.getPrefix() + salesVo.getSalesNo() +".xls";
                }else {
                    jasperParameter.put("report_name", "Invoice No.");
                }
                jasperParameter.put("branch_id", salesVo.getBranchId());
                jasperParameter.put("sales_id", id);
                long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
                String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
                if(MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) || merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER){
                    jasperPrint = JasperFillManager
                            .fillReport(
                                    JasperCompileManager
                                            .compileReport(JASPER_REPORT_PATH + File.separator
                                                    + "sales_export/sales_bill_wise_report_for_lcjp.jrxml"),
                                    jasperParameter, connection);
                }else {
                    jasperPrint = JasperFillManager.fillReport(
                            JasperCompileManager.compileReport(JASPER_REPORT_PATH + File.separator + "sales_export/sales_bill_wise_report.jrxml"),
                            jasperParameter, connection);
                }
                sheets.add(jasperPrint);

                File excel = File.createTempFile("output.", ".xls");
                response.setContentType("application/vnd.ms-excel");

                String[] sheetNames = {salesVo.getPrefix() + salesVo.getSalesNo()};

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(SimpleExporterInput.getInstance(sheets));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));

                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setSheetNames(sheetNames);
                // sheets names is an array of the different names.
                configuration.setOnePagePerSheet(false); // remove that it break on new page
                configuration.setDetectCellType(true);
                configuration.setWhitePageBackground(false);
                configuration.setRemoveEmptySpaceBetweenRows(true);

                exporter.setConfiguration(configuration);
                exporter.exportReport();

                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename="+filename);
                response.getOutputStream().write(os.toByteArray());
                response.flushBuffer();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    connection = null;
                }
            }
        } else {
            response.sendRedirect("/404");
        }
    }

    @PostMapping(value = "/check/excel")
    @ResponseBody
    public Map<String, Object> ImportCustomer(@RequestParam("excelFile") MultipartFile file,
                                              @PathVariable(value = "type") String type, HttpSession session, HttpServletRequest request,
                                              HttpServletResponse response) throws IOException {
        HashMap<String, Object> map = new HashMap<String, Object>();

        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CHECKEXCEL;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECKEXCEL;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CHECKEXCEL;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CHECKEXCEL;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CHECKEXCEL;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECKEXCEL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        File fb = ImageResize.convert(file);


        System.out
                .println(
                        "********************************************************************************************");
        String filepath = fb.getAbsolutePath();
        session.setAttribute("filepath", filepath);

        rowNumber = "";

        Map<String, String> responseData = checkSheet(request, session, type);
        if (StringUtils.equals(responseData.get("result"), "true") && StringUtils.equals(responseData.get("expiry"), "true")) {
//        	log.info("result-------->>>" +responseData.get("result"));
//        	log.info("filePath-------->>>" +responseData.get("filePath"));

            map.put("msg", "success");
            map.put("filePath", responseData.get("filePath"));
            return map;
        } else {
            String limitErrorMessage = responseData.get("reason"); // Get the limit error message if present
            if (limitErrorMessage != null) {
                map.put("msg", limitErrorMessage);
            } else {
                map.put("msg", "There are some error in following Cell Number --> " + rowNumber);
            }
            map.put("expiry", StringUtils.equals(responseData.get("expiry"), "true"));
            map.put("result", StringUtils.equals(responseData.get("result"), "true"));
            map.put("filePath", responseData.get("filePath"));
            return map;
        }
    }

    public Map<String, String> checkSheet(HttpServletRequest request, HttpSession session, String type)
            throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String result = "true";
        String expiry = "true";
        long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
        String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
        // log.severe("TYPE :::: InCheckSheet :::: >>>>>>>  :"+type);
        CompanySettingVo negativestock = companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.ALLOWNEGATIVESTOCK);
        CompanySettingVo isUmoWiseDecimalRestrictionStopped = companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.STOPUMOWISEDECIMAL);

        Map<String, String> responseData = new HashMap<String, String>();

        String filepath = (String) session.getAttribute("filepath");
        File fb = new File(filepath);
        InputStream in = new FileInputStream(fb);
        // Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        // Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);
        // Iterate through each rows one by one
        int rowCount = sheet.getPhysicalNumberOfRows();
        int limit = companySettingService.getvalueByCompanyIdAndType(0, Constant.SHEETLIMIT);

        // Check if the number of rows exceeds the limit
        if ((rowCount - 1) > limit) { // Assuming the first row is the header
            responseData.put("result", "false");
            responseData.put("reason", "Only " + limit + " products can be uploaded at a time");
            return responseData;
        }
        DataFormatter formatter = new DataFormatter();
        Set<String> itemcodelist = productService.findAllItemcodeNew(Long.parseLong(session.getAttribute("companyId").toString()), merchantTypeId, clusterId);

// 		Create bugfile Start
        File errorFile = new File(filepath);

        String[] columns = {"Item Code", "Qty","Free Qty", "Price", "Discount Type (Default Percentage)", "Discount (default 0)", "Description",
                "Discount Type (Default Percentage)", "Discount (default 0)", "Batch No", "Reason"};
        Workbook workbooksheet = new XSSFWorkbook();
        Instant instant = Instant.now();
        String filename = "Bug_Product(" + session.getAttribute("companyId").toString() + ")" + instant.getEpochSecond();
        Sheet sheetwrong = workbooksheet.createSheet(filename);

        Font headerFont = workbooksheet.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());
        CellStyle headerCellStyle = workbooksheet.createCellStyle();
        headerCellStyle.setFont(headerFont);

        CellStyle erroCellStyle = workbooksheet.createCellStyle();
        erroCellStyle.setFont(headerFont);

        Font headerBlackFont = workbooksheet.createFont();
        headerBlackFont.setFontHeightInPoints((short) 14);
        headerBlackFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle nonMandatoryCellStyle = workbooksheet.createCellStyle();
        nonMandatoryCellStyle.setFont(headerBlackFont);

        Row headerRow = sheetwrong.createRow(0);
        // log.info("Sheet--------" + headerRow);

        for (int j = 0; j < 3; j++) {
            Cell cell = headerRow.createCell(j);
            cell.setCellValue(columns[j]);
            cell.setCellStyle(headerCellStyle);
        }
        for (int k = 3; k < columns.length - 1; k++) {
            Cell cell = headerRow.createCell(k);
            cell.setCellValue(columns[k]);
            cell.setCellStyle(nonMandatoryCellStyle);
        }
        Cell cellReason = headerRow.createCell(10);
        cellReason.setCellValue(columns[10]);
        cellReason.setCellStyle(headerCellStyle);
// 		Create bugfile End

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        int i = 2;
        boolean sheetEmpty = true;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Row wrongSheetRow = sheetwrong.createRow((i - 1));
            StringBuilder errorMsgBuilder = new StringBuilder();
            // For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                cell.setCellValue(formatter.formatCellValue(cell));
                cell.setCellType(CellType.STRING);
            }

            /*---------------------------------Employee Code-------------------------*/

            // ---------------ITEM CODE CHECK-------------
			long id = 0;
            try {
                if (row.getCell(0) != null && !StringUtils.equals(row.getCell(0).getStringCellValue().trim(), "")) {
                    id = productService.findProductVarientIdByItemCode(
                            Long.parseLong(session.getAttribute("companyId").toString()),
                            row.getCell(0).getStringCellValue().trim(), merchantTypeId, clusterId);
                    boolean exist = itemcodelist.contains(row.getCell(0).getStringCellValue().trim());
                    if (!exist) {
                        result = "false";
                        stringBuilder.append("(" + i + ",A)-Item Code Not Found " + row.getCell(0).getStringCellValue().trim());
                        errorMsgBuilder.append("Item Code Not Found,");
                    }
                    wrongSheetRow.createCell(0).setCellValue(row.getCell(0).getStringCellValue().trim());
                    // itemcodelist.add(row.getCell(5).getStringCellValue().trim());

                } else {
                    result = "false";
                    stringBuilder.append("(" + i + ",A)-Item Code Is Required");
                    errorMsgBuilder.append("Item Code Is Required,");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            // -----------------------------------------------
            double qty = 0;
            try {
                if (row.getCell(1) != null && !StringUtils.equals(row.getCell(1).getStringCellValue().trim(), "")) {
                    if (RegexTest.validateDouble(row.getCell(1).getStringCellValue().trim())) {
                        qty = Double.parseDouble(row.getCell(1).getStringCellValue().trim());
                        wrongSheetRow.createCell(1).setCellValue(row.getCell(1).getStringCellValue().trim());
                        boolean isValidDecimal = productService.isValidDecimalQty
								(id, row.getCell(1).getStringCellValue().trim(),
                                        isUmoWiseDecimalRestrictionStopped.getValue(), 0);
                        if (!isValidDecimal) {
                            result = "false";
                            stringBuilder.append("(" + i + ",B)- The number of decimal places in qty has been reached ");
                            errorMsgBuilder.append("The number of decimal places in qty has been reached,");
                        }
                    } else {
                        result = "false";
                        stringBuilder.append("(" + i + ",B)- Qty Is Only In Double");
                        errorMsgBuilder.append("Qty Is Only In Double,");
                    }

                } else {
                    result = "false";
                    stringBuilder.append("(" + i + ",B)- Qty Is Required");
                    errorMsgBuilder.append("Qty Is Required,");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            double freeQty = 0;
            try {
                if (row.getCell(2) != null && !StringUtils.equals(row.getCell(2).getStringCellValue().trim(), "")) {
                    if (RegexTest.validateDouble(row.getCell(2).getStringCellValue().trim())) {
                        if (Double.parseDouble(row.getCell(2).getStringCellValue().trim()) <= 1000000) {
                            freeQty = Double.parseDouble(row.getCell(2).getStringCellValue().trim());
                            wrongSheetRow.createCell(2).setCellValue(row.getCell(2).getStringCellValue().trim());
                            boolean isValidDecimal = productService.isValidDecimalQty
                                    (id, row.getCell(2).getStringCellValue().trim(),
                                            isUmoWiseDecimalRestrictionStopped.getValue(), 0);
                            if (!isValidDecimal) {
                                result = "false";
                                stringBuilder.append("(" + i + ",B)- The number of decimal places in free qty has been reached ");
                                errorMsgBuilder.append("The number of decimal places in free qty has been reached,");
                            }
                        } else {
                            result = "false";
                            stringBuilder.append("(" + i + ",B)- The Free Qty must be less than 1000000");
                            errorMsgBuilder.append("The Free Qty must be less than 1000000");
                        }
                    } else {
                        result = "false";
                        stringBuilder.append("(" + i + ",B)- Free Qty Is Only In Double");
                        errorMsgBuilder.append("Free Qty Is Only In Double,");
                    }

                } else {
                    result = "false";
                    stringBuilder.append("(" + i + ",B)- Free Qty Is Required");
                    errorMsgBuilder.append("Free Qty Is Required,");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            // ---------------------------------------------------------------------------------

            try {
                if (row.getCell(3) != null && !StringUtils.equals(row.getCell(3).getStringCellValue().trim(), "")) {
                    if (RegexTest.validateDouble(row.getCell(3).getStringCellValue().trim())) {
                        wrongSheetRow.createCell(3).setCellValue(row.getCell(3).getStringCellValue().trim());
                    } else {
                        result = "false";
                        stringBuilder.append("(" + i + ",C)- Price In Double ");
                        errorMsgBuilder.append("Price In Double,");
                    }

                } else {
                    result = "false";
                    stringBuilder.append("(" + i + ",C)- Price Is Required ");
                    errorMsgBuilder.append("Price Is Required,");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            try {
//                if (row.getCell(3) != null && row.getCell(3).getStringCellValue().trim() != "") {
//                    if (RegexTest.validateDouble(row.getCell(3).getStringCellValue().trim())) {
//
//                    } else {
//                        result = false;
//                        rowNumber += "(" + i + ",D)- Mrp In Double ";
//                    }
//
//                } else {
//                    result = false;
//                    rowNumber += "(" + i + ",D)- Mrp Is Required ";
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            try {
                if (row.getCell(4) != null && !StringUtils.equals(row.getCell(4).getStringCellValue().trim(), "")) {
                    // log.info(""+(!row.getCell(3).getStringCellValue().trim().equalsIgnoreCase(
//							"Amount")
//							|| !row.getCell(3).getStringCellValue().trim().equalsIgnoreCase("Percentage")));
                    wrongSheetRow.createCell(4).setCellValue(row.getCell(4).getStringCellValue().trim());

                    if (!row.getCell(4).getStringCellValue().trim().equalsIgnoreCase("Amount")
                            && !row.getCell(4).getStringCellValue().trim().equalsIgnoreCase("Percentage")) {
                        result = "false";
                        stringBuilder.append("(" + i + ",E)- Discount Type Only Amount Or Percentage");
                        errorMsgBuilder.append("Discount Type Only Amount Or Percentage,");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // ---------------------------------------------------------------------------------

            try {
                if (row.getCell(5) != null && !StringUtils.equals(row.getCell(5).getStringCellValue().trim(), "")) {
                    if (RegexTest.validateDouble(row.getCell(5).getStringCellValue().trim())) {
                        wrongSheetRow.createCell(5).setCellValue(row.getCell(5).getStringCellValue().trim());
                    } else {
                        result = "false";
                        stringBuilder.append("(" + i + ",F)- Discount In Double ");
                        errorMsgBuilder.append("Discount In Double,");
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (row.getCell(6) != null && !StringUtils.equals(row.getCell(6).getStringCellValue().trim(), "")) {
                    wrongSheetRow.createCell(6).setCellValue(row.getCell(6).getStringCellValue().trim());
                }
                if (row.getCell(7) != null && !StringUtils.equals(row.getCell(7).getStringCellValue().trim(), "")) {
                    wrongSheetRow.createCell(7).setCellValue(row.getCell(7).getStringCellValue().trim());
                }
                if (row.getCell(8) != null && !StringUtils.equals(row.getCell(8).getStringCellValue().trim(), "")) {
                    wrongSheetRow.createCell(8).setCellValue(row.getCell(8).getStringCellValue().trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String batchNo = "";
                String message = "";
                // log.warning("itemcode---------------->" + row.getCell(0).getStringCellValue());
                // log.warning("id---------------->" + id);

				if (id != 0) {
                    // log.warning("id not null---------------->" + id);
                    if (row.getCell(9) != null && !StringUtils.equals(row.getCell(9).getStringCellValue().trim(), "")) {
                        batchNo = row.getCell(9).getStringCellValue().trim();
                        wrongSheetRow.createCell(9).setCellValue(row.getCell(9).getStringCellValue());
//						log.info(Long.parseLong(id) + ":::"
//								+ Long.parseLong(session.getAttribute("branchId").toString()));
                        List<StockMasterVo> list = stockMasterService
                                .findByProductVarientsVoProductVarientIdAndBranchIdAndYearIntervalAndBatchNo(
										id, Long.parseLong(session.getAttribute("branchId").toString()),
                                        session.getAttribute("financialYear").toString(), batchNo);
                        // log.warning("list.size()---------------->" + list.size());
                        // log.warning("negativestock---------------->" + negativestock.getValue());
//						row.getCell(9).setCellValue(message);
                        if (!list.isEmpty()) {
                            int count = stockMasterService
									.getByProductVarientIdAndBranchIdAndYearIntervalAndBatchNoAndisDisable(id,
                                            Long.parseLong(session.getAttribute("branchId").toString()),
                                            session.getAttribute("financialYear").toString(), batchNo, 1);
                            if (count > 0) {
                                result = "false";
                                stringBuilder.append("(" + i + ",G)-  batch is disabled ");
                                errorMsgBuilder.append("batch is disabled,");

                            } else {
                                if (!StringUtils.equals(type, Constant.SALES_ORDER) && (negativestock.getValue() == 1)) {
                                    // log.warning("qty---------------->" + list.get(0).getQuantity());
                                    if (list.get(0).getQuantity() < qty+freeQty) {
                                        result = "false";
                                        stringBuilder.append("(" + i + ",I)- stock not available");
                                        errorMsgBuilder.append("stock not available,");
                                    }

                                }
                            }
                        } else {
                            result = "false";
                            stringBuilder.append("(" + i + ",I)- Batch No Invalid");
                            errorMsgBuilder.append("Batch No Invalid,");
                        }
                    } else {
                        String errMessage = "", errAlert = "", errResult = "";

//						result = false;
//						rowNumber += "(" + i + ",I)- Batch No Must Required ";
//						batchNo=stockMasterService.findLatestBatchNo(Long.parseLong(id),
//								Long.parseLong(session.getAttribute("branchId").toString()),
//								session.getAttribute("financialYear").toString());

                        List<StockMasterVo> batchesVo = stockMasterRepository
								.findByProductVarientIdAndBranchIdAndYearIntervalOrderByStockIdDsc(id,
                                        Long.parseLong(session.getAttribute("branchId").toString()),
                                        session.getAttribute("financialYear").toString());
                        if (!batchesVo.isEmpty()) {
                            for (int batch = 0; batch < batchesVo.size(); batch++) {
                                // log.warning("System qty---------------->" + batchesVo.get(0).getQuantity());
                                // log.warning("Order qty---------------->" + qty);

                                errResult = "true";
                                errMessage = "";
                                errAlert = "";

                                if (!StringUtils.equals(type, Constant.SALES_ORDER) && negativestock.getValue() == 1) {
                                    // log.warning("Negative selling Switch OFF---------------->");
                                    // log.warning("BATCH qty---------------->" + batchesVo.get(batch).getQuantity());
                                    // log.warning("SHEET qty---------------->" + qty);
                                    if (batchesVo.get(batch).getQuantity() >= qty+freeQty) {

                                        batchNo = batchesVo.get(batch).getBatchNo();

                                        int checkDisable = stockMasterService
												.getByProductVarientIdAndBranchIdAndYearIntervalAndBatchNoAndisDisable(id,
                                                        Long.parseLong(session.getAttribute("branchId").toString()),
                                                        session.getAttribute("financialYear").toString(), batchNo, 1);
                                        if (checkDisable == 0) {
                                            // log.warning("Batch Not Disabled---------------->" + batchesVo.get(batch).getBatchNo());
                                            break;
                                        } else {
                                            batchNo = "";
                                            // log.warning("Batch is Disabled---------------->" + batchesVo.get(batch).getBatchNo());
                                            errResult = "false";
                                            errAlert += "(" + i + ",G)-  batch is disabled ";
                                            errMessage += "batch is disabled";
                                        }
                                    } else {
                                        // log.warning("Batch QTY is Less---------------->" + batchesVo.get(batch).getBatchNo());
                                        errResult = "false";
                                        errAlert += "(" + i + ",I)- stock not available";
                                        errMessage += "stock not available";
                                    }
                                } else {
                                    // log.warning("Negative selling Switch ON---------------->");

                                    batchNo = batchesVo.get(batch).getBatchNo();
                                    int checkDisable = stockMasterService
											.getByProductVarientIdAndBranchIdAndYearIntervalAndBatchNoAndisDisable(id,
                                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                                    session.getAttribute("financialYear").toString(), batchNo, 1);
                                    if (checkDisable == 0) {
                                        // log.warning("Batch Not Disabled---------------->" + batchesVo.get(batch).getBatchNo());
                                        break;
                                    } else {
                                        batchNo = "";
                                        // log.warning("Batch is Disabled---------------->" + batchesVo.get(batch).getBatchNo());
                                        errResult = "false";
                                        errAlert += "(" + i + ",G)-  batch is disabled ";
                                        errMessage += "batch is disabled";
                                    }
                                }
                            }
                        } else {
                            errResult = "false";
                            errAlert = "(" + i + ",I)- Batch Not found";
                            errMessage += "Batch Not found";
                        }
                        // log.warning("id    >>>>>>>>>>>>  " +id);
                        // log.warning("batchNo    >>>>>>>>>>>>  " +batchNo);
                        // log.warning("errResult    >>>>>>>>>>>>  " +errResult);
                        // log.warning("errAlert  >>>>>>>>>>>>>>>  " +errAlert);
                        // log.warning("errMessage   >>>>>>>>>>>>  " +errMessage);

                        if (StringUtils.equals(errResult, "false")) {
                            message += errAlert;
                            errorMsgBuilder.append(errMessage);
                            result = "false";
                        } else {
                            errMessage = "";
                            errAlert = "";
                        }

                    }

                    // log.warning("batchNo  >>> " + batchNo);

//                    if (!StringUtils.isNotBlank(batchNo)) {
//                        result = "false";
//                        stringBuilder.append(message);
//                    } else {
						Map<String, Object> productDetails = productRepository.findExpiryDetailssOnbatchAndVarientIdAndBranchId(batchNo, id, Long.parseLong(session.getAttribute("branchId").toString()), session.getAttribute(Constant.FINANCIAL_YEAR).toString());

                        Integer expiryManageInteger = (Integer) productDetails.get("expirymanage");
                        try {
                            if (expiryManageInteger == 1) {
                                // System.out.println(productDetails.get("expirysee")+"====Done===="+productDetails.get("date"));
                                Date dateString = (Date) productDetails.get("date");

                                if (ObjectUtils.isNotEmpty(productDetails.get("date"))) {

                                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                                    LocalDate date = LocalDate.parse(String.valueOf(dateString), dateFormatter);

                                    LocalDate currentDate = LocalDate.now();

                                    int comparisonResult = date.compareTo(currentDate);
                                    // System.out.println(comparisonResult+"======Comper Result");
                                    if (comparisonResult == 0) {
                                        expiry = "true";
                                    } else if (comparisonResult > 0) {
                                        expiry = "true";
                                    } else {
                                        Integer isExpiryVisible = (Integer) productDetails.get("expirysee");
                                        if (isExpiryVisible == 1) {

                                            expiry = "false";
                                            String errAlert = "(" + i + ",I)- This product batch has been expired";
                                            String errMessage = "This product batch has been expired ";
                                            message += errAlert;
                                            stringBuilder.append("(" + i + ",I)- This product batch has been expired");
                                            errorMsgBuilder.append(errMessage);
                                        } else {
                                            result = "false";
                                            String errAlert = "(" + i + ",I)- product are expired and non-saleable";
                                            String errMessage = "Batch is expired and Product is Non-Saleable";
                                            message += errAlert;
                                            stringBuilder.append("(" + i + ",I)- product are expired and non-saleable");
                                            errorMsgBuilder.append(errMessage);
                                        }
                                        // System.out.println("The date is before today.");
                                    }
                                } else {
                                    result = "false";
                                    stringBuilder.append("(" + i + ",I)- product expiry date not found");
                                    errorMsgBuilder.append("product expiry date not found,");
                                }
                            }
                        } catch (Exception ex) {
                        }


//                    }
//                	String id=productService.findProductVarientIdByItemCode(Long.parseLong(session.getAttribute("companyId").toString()),
//                			row.getCell(0).getStringCellValue().trim());
//					if(id!=null) {

//					}else {
//						result = false;
//	                    rowNumber += "(" + i + ",I)- product not found by item code ";
//					}

//                    boolean isExist = stockMasterService
//                            .isExistByProductVarientsVoItemCodeAndCompanyIdAndYearIntervalAndBatchNo(
//                                    row.getCell(0).getStringCellValue().trim(),
//                                    Long.parseLong(session.getAttribute("companyId").toString()),
//                                    session.getAttribute("financialYear").toString(),
//                                    row.getCell(8).getStringCellValue().trim());
//                    if (!isExist) {
//                        result = false;
//                        rowNumber += "(" + i + ",I)- Batch No Invalid";
//                    }

                } else {
                    result = "false";
                    stringBuilder.append("(" + i + ",I)- product not found by item code");
                    errorMsgBuilder.append("product not found by item code,");
                }

            } catch (Exception e) {
                result = "false";
                stringBuilder.append("(" + i + ",I)- Stock check Internal Server Error");
                errorMsgBuilder.append("Stock check Internal Server Error,");
                e.printStackTrace();
            }

            sheetEmpty = false;
            i++;
//            wrongSheetRow.createCell(9).setCellValue(errorMsg);
//            wrongSheetRow.createCell(9).setCellStyle(erroCellStyle);
            Cell cell = wrongSheetRow.createCell(10);
            cell.setCellValue(errorMsgBuilder.toString());
            cell.setCellStyle(erroCellStyle);
        }

        rowNumber = stringBuilder.toString();
        String filepath1 = "";
        //FileOutputStream fileOut = new FileOutputStream(tmpdir + "/" + filename + ".xlsx");


        FileOutputStream fileOut = new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + filename + ".xlsx");
        workbooksheet.write(fileOut);

        // log.info("Excel complete-----");
        String filePathToBeServed = System.getProperty("java.io.tmpdir") + File.separator + filename + ".xlsx"; // complete file name with path;
        filepath1 = filename + ".xlsx";
        File fileToDownload = new File(filePathToBeServed);
        InputStream inputStream = new FileInputStream(fileToDownload);
//		log.info("Sheet--------" +fileToDownload.getAbsolutePath());
        inputStream.close();
        fileOut.close();
        workbooksheet.close();

        workbook.close();
        in.close();
        if (sheetEmpty) {
            result = "false";
            rowNumber += "Empty Sheet, Please Add minimum one product";
        }
        responseData.put("expiry", expiry);
        responseData.put("result", result);
        responseData.put("filePath", filepath1);
//		log.info("filePath--------" +filepath1);
        return responseData;
    }

    @PostMapping("/bysaleid/{saleId}")
    @ResponseBody
    public List<SalesItemVo> getSalesItemBySaleId(HttpSession session, @PathVariable(value = "type") String type,
                                                  @PathVariable(value = "saleId") long saleId) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_ITEMS_DETAIL;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_ITEMS_DETAIL;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_ITEMS_DETAIL;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_ITEMS_DETAIL;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_ITEMS_DETAIL;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_ITEMS_DETAIL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(saleId, branchId);
        List<SalesItemVo> salesItemVos = null;
        if (salesVo != null) {
            salesVo.getSalesItemVos().removeIf(z -> z.getIsReturn() == 1);
            salesItemVos = salesVo.getSalesItemVos();

            if (salesItemVos != null) {
                salesItemVos.forEach(salesItemVo -> {
                    salesItemVo.setSalesVo(null);
                    salesItemVo.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
                    salesItemVo.getProductVarientsVo().getProductVo().setProductAttributeVos(null);
                    if (StringUtils.isNotBlank(salesItemVo.getProductVarientsVo().getProductVo().getHsnCode())) {
                        salesItemVo.getProductVarientsVo().getProductVo().setHsnType(hsnTaxMasterRepository.getHsnTypeByHsnCode(salesItemVo.getProductVarientsVo().getProductVo().getHsnCode()));
                    }
                    if (salesItemVo.getProductVarientsVo().getProductVo().getIsExpiryManage() == 1) {
                        salesItemVo.setExpDate(stockMasterRepository.findExpDateByStockId(salesItemVo.getBatchId(), session.getAttribute(Constant.FINANCIAL_YEAR).toString()));
                    } else {
                        salesItemVo.setExpDate("");
                    }
                    List<StockMasterVo> stockMasterVos = stockMasterService
                            .findByProductVarientsVoProductVarientIdAndCompanyIdAndYearIntervalWithBatchCheck(
                                    salesItemVo.getProductVarientsVo().getProductVarientId(),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    session.getAttribute("financialYear").toString(),
                                    Long.parseLong(session.getAttribute("companyId").toString()),type, 0, 0);
                    double totalAvailableQTY = stockMasterVos.stream().mapToDouble(StockMasterVo::getQuantity).sum();
                    log.info(stockMasterVos+"stockMasterVos");
                    if (stockMasterVos != null) {
                        salesItemVo.getProductVarientsVo().setStockMasterVos(stockMasterVos);
                        salesItemVo.setAvailableQty(totalAvailableQTY);
                    }

                });

            }
        }
        return salesItemVos;
    }

    @PostMapping("/itemcode/{saleId}")
    @ResponseBody
    public Map<String, Object> getSalesItemBySaleId(HttpSession session,
                                                    @PathVariable(value = "saleId") long saleId,
                                                    @RequestParam(value = "itemCode") String itemCode) {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CREDITNOTE_SALES_ITEMS_DETAIL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
        return salesService.getSalesItemBySaleId(saleId, branchId, itemCode);
    }


    @PostMapping("/deleteInvoice")
    @ResponseBody
    public String deleteInvoice(@RequestParam("salesids") String salesids, HttpServletRequest request,
                                HttpSession session, @PathVariable String type) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_DELETE_INVOICE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_DELETE_INVOICE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_DELETE_INVOICE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_DELETE_INVOICE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_DELETE_INVOICE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_DELETE_INVOICE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        String salestype = type;
        long idSize = 0;
        if (type.equals(Constant.SALES_ORDER)) {
            salestype = "salesorder";
        }
        if (MenuPermission.havePermission(session, salestype, Constant.DELETE) == 1) {
            List<String> idlist = Arrays.asList(salesids.split("\\s*,\\s*"));
            idSize = idlist.size();
            // System.out.println("result:---" + idlist.size());
            // New: List to store invoice numbers with e-way bills
            List<String> ewayBillInvoices = new ArrayList<>();
            List<Long> salesIds = new ArrayList<>();
            for (String s : idlist) {
                if (StringUtils.isNotBlank(s)) {
                    SalesVo salesVo = salesService.findBySalesIdAndBranchId(Long.parseLong(s), Long.parseLong(session.getAttribute("branchId").toString()));
                    if (salesVo != null && salesVo.getIsDeleted() == 0) {
                        // System.out.println("sales id----------"+salesVo.getSalesId());
                        boolean deleteFlag = false;
                        if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) || salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                            // Check for e-way bill and e-invoice
                            if (salesVo.getEwayBillNo() != 0 || StringUtils.isNotBlank(salesVo.getIrnNo())) {
                                // Add invoice number to the list
                                ewayBillInvoices.add(salesVo.getPrefix() + salesVo.getSalesNo());
                                continue; // Skip to next invoice
                            }
                        }
                        if (salesVo.getType().equals(Constant.SALES_ESTIMATE) && StringUtils.equals(salesVo.getStatus(), Constant.PENDING)) {
                            deleteFlag = true;
                        } else if (salesVo.getType().equals(Constant.SALES_ORDER)
                                && (StringUtils.equals(salesVo.getStatus(), Constant.INPROGRESS) || StringUtils.equals(salesVo.getStatus(), Constant.PENDING)
                                || StringUtils.equals(salesVo.getStatus(), Constant.OPEN))) {
                            deleteFlag = true;
                        } else if (salesVo.getType().equals(Constant.SALES_INVOICE)
                                && (salesVo.getChildType() == 0 && salesVo.getTotal() > salesVo.getPaidAmount() && salesVo.getCreditNoteAmount() == 0
                                && !StringUtils.equals(salesVo.getPartiallyStatus(), Constant.PARTIALLY_CANCELLED)
                                && !StringUtils.equals(salesVo.getStatus(), Constant.CANCELLED))) {
                            if (salesVo.getPaidAmount() == 0) {
                                deleteFlag = true;
                            }
                        } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)
                                && (!StringUtils.equals(salesVo.getPartiallyStatus(), Constant.PARTIALLY_CANCELLED)
                                && StringUtils.equals(salesVo.getStatus(), Constant.DELIVERED))) {
                            deleteFlag = true;
                        } else if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                            deleteFlag = true;
                        }
                        if (deleteFlag) {
                            idSize--;
                            if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                                List<SalesItemQtyDTO> salesItemQtyDTOs = salesRepository.findSalesItemQtyDetailsBySalesId(salesVo.getSalesId());
                                if (!salesItemQtyDTOs.isEmpty()) {
                                    for (int j = 0; j < salesItemQtyDTOs.size(); j++) {
                                        SalesItemQtyDTO salesItemQtyDTO = salesItemQtyDTOs.get(j);
                                        if (salesItemQtyDTO != null) {
                                            String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                            // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                            // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                            if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                                List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                                        .map(Long::parseLong).collect(Collectors.toList());
                                                if (!parentItemIds.isEmpty()) {
                                                    long parentSalesItemId = parentItemIds.get(0);
                                                    float updateQty = salesItemQtyDTO.getQty();
                                                    // log.warning("updateQty-->"+updateQty);
                                                    int result = salesService.updateSalesItemCreditNoteQtyMinus(parentSalesItemId, updateQty,Double.parseDouble(String.valueOf(salesItemQtyDTO.getFreeQty())));//100
                                                    // log.warning("result-->"+result);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                                List<SalesItemQtyDTO> salesItemQtyDTOs = salesRepository.findSalesItemQtyDetailsBySalesId(salesVo.getSalesId());
                                if (!salesItemQtyDTOs.isEmpty()) {
                                    for (int j = 0; j < salesItemQtyDTOs.size(); j++) {
                                        SalesItemQtyDTO salesItemQtyDTO = salesItemQtyDTOs.get(j);
                                        if (salesItemQtyDTO != null) {
                                            String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                            // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                            // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                            if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                                List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                                        .map(Long::parseLong).collect(Collectors.toList());
                                                if (!parentItemIds.isEmpty()) {
                                                    if (parentItemIds.size() > 1) {//Here Multiple Parent Sales Items
                                                        // log.warning("=======Here Multiple Parent Sales Items=======");
                                                        double qty = salesItemQtyDTO.getQty(); //150
                                                        double freeQty = salesItemQtyDTO.getFreeQty();
                                                        // log.warning("qty--->"+qty);
                                                        for (int i = 0; i < parentItemIds.size(); i++) {
                                                            if (qty+freeQty > 0) {
                                                                long parentSalesItemId = parentItemIds.get(i);
                                                                // log.warning("parentSalesItemId--->"+parentSalesItemId);
                                                                SalesItemQtyDTO itemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                                                                if (itemQtyDTO != null) {
                                                                    double actualOrderQty = itemQtyDTO.getQty();//100 //100
                                                                    double actualOrderFreeQty = itemQtyDTO.getFreeQty();

                                                                    double orderQty = qty;//150 //50
                                                                    double updateQty = 0;
                                                                    double updateFreeQty = 0;
                                                                    // log.warning("actualOrderQty--->"+actualOrderQty);
                                                                    // log.warning("orderQty--->"+orderQty);

                                                                    if (orderQty >= actualOrderQty) {//150>=100 //50>=100
                                                                        updateQty = actualOrderQty;//100
                                                                    } else {
                                                                        updateQty = orderQty;//50
                                                                    }
                                                                    updateFreeQty = freeQty >= actualOrderFreeQty ? actualOrderFreeQty:freeQty;

                                                                    // log.warning("updateQty-->"+updateQty);
                                                                    if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                            || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                                        int result = salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty,updateFreeQty);
                                                                        // log.warning("result-->"+result);
                                                                    } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                            || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                                        int result = salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty,updateFreeQty);//100
                                                                        // log.warning("result-->"+result);
                                                                    }
                                                                    qty = qty - updateQty;//150-100=50
                                                                }

                                                            }

                                                        }
                                                        // log.warning("=======Here Multiple Parent Sales Items DELETE=======");
                                                    } else {
                                                        long parentSalesItemId = parentItemIds.get(0);
                                                        //salesService.updateSalesItemDcQtyMinus(parentSalesItemId, 0);
                                                        float updateQty = salesItemQtyDTO.getQty();
                                                        float updateFreeQty = salesItemQtyDTO.getFreeQty();
                                                        // log.warning("updateQty-->"+updateQty);
                                                        if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                            int result = salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty,updateFreeQty);
                                                            // log.warning("result-->"+result);
                                                        } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                            int result = salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty,updateFreeQty);//100
                                                            // log.warning("result-->"+result);
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            salesService.deleteSales(Long.parseLong(session.getAttribute("branchId").toString()), Long.parseLong(s), type, Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());

                            salesService.saveSalesHistory(Constant.HISTORY_TYPE_DELETED, salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()),
                                    0, "", salesVo.getStatus(), 0, "", "", 0, session);
                            try {

                                List<SalesMappingVo> mappingVos = salesMappingRepository.findByMainSalesId(salesVo.getSalesId());
                                if (!mappingVos.isEmpty()) {
                                    for (int i = 0; i < mappingVos.size(); i++) {

                                        Long parentSalesId = mappingVos.get(i).getParentSalesId();
                                        //find sales packing
                                        // log.warning("parentSalesId---->" + parentSalesId);
                                        if (parentSalesId != null && parentSalesId != 0) {
                                            SalesDTO salesDTO = salesRepository.findCustomSalesBySalesId(parentSalesId);
                                            String fromStatus = "";
                                            if (salesDTO != null) {
                                                fromStatus = salesDTO.getStatus();
                                            }
                                            String parentType = mappingVos.get(i).getParentSalesType();
                                            String salesNo = mappingVos.get(i).getParentSalesNo();


                                            //for estimate and order status of that parent is PENDING
                                            String parentStatus = Constant.PENDING;
                                            if (salesVo.getType().equals(Constant.SALES_INVOICE)
                                                    && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN))) {//HERE ORDER/DC STATUS
                                                salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_INVOICE);
                                                List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                                List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() < p.getQty()).collect(Collectors.toList());
                                                List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() == p.getQty()).collect(Collectors.toList());
                                                // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                                // log.warning("partiallyList length--------->"+partiallyList.size());
                                                // log.warning("completeList length--------->"+completeList.size());
                                                if (StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN)) {
                                                    parentStatus = Constant.DELIVERED;
                                                }
                                                if (partiallyList.size() == salesItemQtyDTO.size()) {
                                                    double totalInvoiceQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getInvoiceQty()).sum();
                                                    double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
                                                    if (totalInvoiceQty > 0) {
                                                        if (totalInvoiceQty < totalQty) {
                                                            parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                                        }
                                                    }
                                                } else if (completeList.size() == salesItemQtyDTO.size()) {
                                                    parentStatus = Constant.INVOICE_CREATED;
                                                } else {
                                                    parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                                }
                                            } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)
                                                    && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_INVOICE))) {//HERE ORDER/INVOICE STATUS
                                                salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_DC);
                                                List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                                List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() < p.getQty()).collect(Collectors.toList());
                                                List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() == p.getQty()).collect(Collectors.toList());
                                                // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                                // log.warning("partiallyList length--------->"+partiallyList.size());
                                                // log.warning("completeList length--------->"+completeList.size());
                                                if (StringUtils.equals(parentType, Constant.SALES_INVOICE)) {
                                                    parentStatus = Constant.INVOICED;
                                                }
                                                if (partiallyList.size() == salesItemQtyDTO.size()) {
                                                    double totalDcQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getDcQty()).sum();
                                                    double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
                                                    if (totalDcQty > 0) {
                                                        if (totalDcQty < totalQty) {
                                                            parentStatus = Constant.PARTIAL_DC_CREATED;
                                                        }
                                                    }

                                                } else if (completeList.size() == salesItemQtyDTO.size()) {
                                                    parentStatus = Constant.DC_CREATED;
                                                } else {
                                                    parentStatus = Constant.PARTIAL_DC_CREATED;
                                                }
                                            }
                                            // log.warning("Parent Status---->"+parentStatus+" parentSalesId--->"+parentSalesId);
                                            //here update status in sales Parent
                                            if (StringUtils.equals(parentStatus, Constant.PENDING) || StringUtils.equals(parentStatus, Constant.DELIVERED)
                                                    || StringUtils.equals(parentStatus, Constant.INVOICED)) {
                                                salesService.updateChildType(parentSalesId, 0);
                                            }
                                            salesService.updateStatusBySalesId(parentStatus, parentSalesId,
                                                    Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                                            salesService.saveSalesHistory(Constant.HISTORY_TYPE_CHILDDELETED, parentSalesId, parentType, salesNo,
                                                    0, fromStatus, parentStatus, salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()), 0, session);
                                            salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, parentSalesId, parentType, salesNo,
                                                    0, fromStatus, parentStatus, 0, "", "", 0, session);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }

            }
            // If we found any invoices with e-way bills, return them in the message
            if (!ewayBillInvoices.isEmpty()) {
                String invoiceList = String.join(", ", ewayBillInvoices);
                return "E-way bill or E-invoice is generated for " + invoiceList + ". These invoices cannot be deleted.";
            }

        } else {
            return Constant.ACCESSDENIED;
        }
        // System.out.println("result:---" + true);
        if (idSize != 0 && type.equals(Constant.SALES_INVOICE)) {
            return "" + idSize;
        } else {
            return "true";
        }
    }

    @PostMapping("/attachmentUpload")
    @ResponseBody
    public String attachmentUpload(@RequestParam("id") long
                                           id, @RequestParam("image_logo") MultipartFile file, HttpSession session) {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_UPLOAD_ATTACHMENT, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int result = salesService.countBySalesIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (result == 0) return "404";
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // System.out.println("in controller:::::" + id);
        String fileName = "";
        try {


            if (!file.isEmpty()) {
                FileValidationResponse fileValidationResponse = securityValidation.validateFile(file, Constant.FILE_IMAGE_AND_PDF);
                if (!fileValidationResponse.isValid()) return fileValidationResponse.getMessage();
                long companyId = Long.parseLong(String.valueOf(session.getAttribute("companyId").toString()));
                String fileExtension = "";
                File fb =
                        ImageResize.convert(file);
                Calendar calendar = Calendar.getInstance();

                fileExtension = GetFileExtension.get(fb);
                fileName = id + "." +
                        fileExtension;
                String uploadStatus = "500";
                if (FILE_UPLOAD_SERVER.equals(Constant.FILE_UPLOAD_SERVER_AZURE)) {
                    uploadStatus = azureBlobService.sendSalesAttachmentFileToAZURE(fb, fileName, fileExtension, companyId, id);
                } else {
                    uploadStatus = awsService.saveAttachmentToS3(fileExtension, fb, SALES_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + fileName);
                }
                if (uploadStatus == "200") {
                    salesService.updateAttachmentfile(id, fileName);
                }


                // System.out.println("Image upload sucessfully");
                return "success";

            }
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }

        return "";
    }

    @RequestMapping(value = "/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> retrieveDocument(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        SalesVo salesVo = salesService.findBySalesId(id);
        long companyId = Long.parseLong(String.valueOf(session.getAttribute("companyId").toString()));
        if (FILE_UPLOAD_SERVER.equals(Constant.FILE_UPLOAD_SERVER_AZURE)) {
            return azureBlobService.getSalesAttachmentFileFromAZURE(salesVo.getImgLocation(), companyId, id);
        } else {
            String file_path = END_POINT_URL + "/" + BUCKET + "/" + SALES_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + salesVo.getImgLocation();
            String file_name = salesVo.getImgLocation();
            BufferedInputStream inputStream = new BufferedInputStream(new URL(file_path).openStream());

            Files.copy(inputStream, Paths.get(System.getProperty("java.io.tmpdir") + File.separator + file_name), StandardCopyOption.REPLACE_EXISTING);
            Path filePath = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + file_name);
            Resource resource = new UrlResource(filePath.toUri());

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {

            }

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }

    }

    @PostMapping(value = "/attachmentDelete/{id}")
    @ResponseBody
    public String deleteDocument(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        String rateLimitType;
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_DELETE_ATTACHMENT, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int result = salesService.countBySalesIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (result == 0) {
            return "404";
        } else {
            salesService.deleteAttachment(id, Long.parseLong(session.getAttribute("branchId").toString()));
        }

        return "";
    }

    @RequestMapping(value = "/htmlpdf/{id}")
    public ModelAndView htmlpdf(@PathVariable("id") String salesId, HttpSession session) {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_HTML_PDF, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView();
        long id = 0;
        try {
            id = Long.parseLong(salesId);
        } catch (Exception e) {
            e.printStackTrace();
            view.setViewName("accessdenied/datanotavailbal");
            return view;
        }
        // log.warning("Inside Model And View");
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(id, Long.parseLong(session.getAttribute("branchId").toString()));
        if (salesVo != null && salesVo.getIsDeleted() == 0) {
            // log.warning("Inside Sales Vo");
            UserFrontVo companyVo = userRepository.findByUserFrontId(salesVo.getBranchId());
            companyVo.setCityName(cityService.findByCityCode(companyVo.getCityCode()).getCityName());
            companyVo.setStateName(stateService.findByStateCode(companyVo.getStateCode()).getStateName());
            companyVo.setCountriesName(countryService.findByCountriesCode(companyVo.getCountriesCode()).getCountriesName());
            try {
                if (StringUtils.isNotBlank(companyVo.getBankAcno())) {
                    companyVo.setAcNo(companyVo.getBankAcno());
                    if (companyVo.getBankAcno().length() > 4) {
                        String bankAcNo = "**********" + companyVo.getBankAcno().substring(companyVo.getBankAcno().length() - 4);
                        companyVo.setAcNo(bankAcNo);
                    }

                }
                if (StringUtils.isNotBlank(companyVo.getPanNo())) {
                    companyVo.setPan(companyVo.getPanNo());
                    if (companyVo.getPanNo().length() > 4) {
                        String panNo = "******" + companyVo.getPanNo()
                                .substring(companyVo.getPanNo().length() - 4);
                        companyVo.setPan(panNo);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ContactVo contactVo = contactRepository.findByContactId(salesVo.getContactVo().getContactId());
            if (salesVo.getShippingCityCode() != null && salesVo.getShippingCityCode() != "") {
                salesVo.setShippingCityName(cityService.findByCityCode(salesVo.getShippingCityCode()).getCityName());
            }
            if (salesVo.getShippingStateCode() != null && salesVo.getShippingStateCode() != "") {
                salesVo.setShippingStateName(stateService.findByStateCode(salesVo.getShippingStateCode()).getStateName());
            }
            if (salesVo.getShippingCountriesCode() != null && !salesVo.getShippingCountriesCode().isEmpty()) {
                salesVo.setShippingCountriesName(countryService.findByCountriesCode(salesVo.getShippingCountriesCode()).getCountriesName());
            }
            if (salesVo.getBillingStateCode() != null && !salesVo.getBillingStateCode().isEmpty()) {
                salesVo.setBillingStateName(stateService.findByStateCode(salesVo.getBillingStateCode()).getStateName());
            }
            if (salesVo.getBillingCityCode() != null && salesVo.getBillingCityCode() != "") {
                salesVo.setBillingCityName(cityService.findByCityCode(salesVo.getBillingCityCode()).getCityName());
            }

            String placeOfSupplyCode = session.getAttribute("stateCode").toString();


            placeOfSupplyCode = salesVo.getBillingStateCode();


            //salesVo.setPlaceOfSupplyName(stateService.findByStateCode(placeOfSupplyCode).getStateName());

            List<SalesItemPdfDTO> itemPdfDTOs = new ArrayList<SalesItemPdfDTO>();
            for (int i = 0; i < salesVo.getSalesItemVos().size(); i++) { /// sales Item
                SalesItemVo salesItemVo = salesVo.getSalesItemVos().get(i);
                SalesItemPdfDTO itemPdfDTO = new SalesItemPdfDTO();

				itemPdfDTO.setDiscount(salesItemVo.getDiscount());
				itemPdfDTO.setDiscountType(salesItemVo.getDiscountType());
				itemPdfDTO.setDiscount2(salesItemVo.getDiscount2());
				itemPdfDTO.setDiscountType2(salesItemVo.getDiscountType2());
				itemPdfDTO.setHsnCode(salesItemVo.getProductVarientsVo().getProductVo().getHsnCode());
				itemPdfDTO.setPrice(salesItemVo.getPrice());
				itemPdfDTO.setQty(salesItemVo.getQty());
				itemPdfDTO.setTaxAmount(salesItemVo.getTaxAmount());
				itemPdfDTO.setTaxRate(salesItemVo.getTaxRate());
				itemPdfDTO.setCessAmount(salesItemVo.getCessAmount());
				itemPdfDTO.setCessRate(salesItemVo.getCessRate());
				itemPdfDTO.setItemNetAmount(salesItemVo.getNetAmount());
				double discountAmount1 = 0.0;
				double discountAdditionalAmount1 = 0.0;
				double flatdiscount = 0;
				double discount1 = salesItemVo.getDiscount();
				if (salesItemVo.getDiscountType().equals("percentage")) {
					double discountPercent = discount1 / 100;
					double itemTotal = salesItemVo.getQty() * salesItemVo.getPrice();
					double discountAmount = itemTotal * discountPercent;
					discountAmount1 += discountAmount;
				} else {
					discountAmount1 += discount1 * salesItemVo.getQty();
				}

				double discount2 = salesItemVo.getDiscount2();
				if (salesItemVo.getDiscountType2().equals("percentage")) {
					double discountPercent2 = discount2 / 100;
					double itemTotalWithDiscount1 = salesItemVo.getQty() * salesItemVo.getPrice() - discountAmount1;
					double discountAdditionalAmount = itemTotalWithDiscount1 * discountPercent2;
					discountAdditionalAmount1 += discountAdditionalAmount;
				} else {
					discountAdditionalAmount1 += discount2 * salesItemVo.getQty();
				}

				itemPdfDTO.setFlatDiscount(salesItemVo.getFlatDiscount());
				salesVo.getSalesItemVos().get(i).setDiscountAmount2(discountAdditionalAmount1);
				salesVo.getSalesItemVos().get(i).setMainDiscount(discountAmount1);
				itemPdfDTOs.add(itemPdfDTO);
			}

			for (int i = 0; i < salesVo.getSalesAdditionalChargeVos().size(); i++) {  // sales ledger account
				SalesAdditionalChargeVo additionalcharge = salesVo.getSalesAdditionalChargeVos().get(i);
				SalesItemPdfDTO itemPdfDTO = new SalesItemPdfDTO();

				itemPdfDTO.setDiscount(0);
				itemPdfDTO.setDiscountType("amount");
				itemPdfDTO.setDiscount2(0);
				itemPdfDTO.setDiscountType2("amount");
				itemPdfDTO.setHsnCode("-");
				itemPdfDTO.setPrice(additionalcharge.getAmount());
				itemPdfDTO.setQty(1);
				itemPdfDTO.setTaxAmount(additionalcharge.getTaxAmount());
				itemPdfDTO.setTaxRate(additionalcharge.getTaxRate());

				itemPdfDTOs.add(itemPdfDTO);
			}


			List<DeliveryTaxDTO> deliveryTaxDTOs = new ArrayList<DeliveryTaxDTO>();

			for (int i = 0; i < itemPdfDTOs.size(); i++) {
				SalesItemPdfDTO itemPdfDTO = itemPdfDTOs.get(i);

				int is_same = 0;
				if (i == 0) {
					if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {//CGST -SGST
//						System.err.println("CGST -SGST 1");
                        double taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                :round(itemPdfDTO.getPrice(),2);

						DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
						deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
						deliveryTaxDTO2.setTaxableAmount(taxableamount);
						deliveryTaxDTO2.setCgstAmount(itemPdfDTO.getTaxAmount() / 2);
						deliveryTaxDTO2.setCgstRate(itemPdfDTO.getTaxRate() / 2);
						deliveryTaxDTO2.setSgstAmount(itemPdfDTO.getTaxAmount() / 2);
						deliveryTaxDTO2.setSgstRate(itemPdfDTO.getTaxRate() / 2);
						deliveryTaxDTO2.setCessAmount(itemPdfDTO.getCessAmount());
						deliveryTaxDTO2.setCessRate(itemPdfDTO.getCessRate());
						deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
						deliveryTaxDTOs.add(deliveryTaxDTO2);

					} else {
//						System.err.println("IGST 1");
                        double taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                :round(itemPdfDTO.getPrice(),2);

						DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
						deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
						deliveryTaxDTO2.setTaxableAmount(taxableamount);
						deliveryTaxDTO2.setIgstAmount(itemPdfDTO.getTaxAmount());
						deliveryTaxDTO2.setIgstRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setCessAmount(itemPdfDTO.getCessAmount());
						deliveryTaxDTO2.setCessRate(itemPdfDTO.getCessRate());
						deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
						deliveryTaxDTOs.add(deliveryTaxDTO2);

					}

				} else {

					for (int j = 0; j < deliveryTaxDTOs.size(); j++) {

						DeliveryTaxDTO deliveryTaxDTO = deliveryTaxDTOs.get(j);
						if (itemPdfDTO.getTaxRate() == deliveryTaxDTO.getTaxRate()) {//SAME
							is_same = 1;
//							System.err.println("both same");
							double taxableamount = 0.0;
							double amount = 0.0;
							double cgst = 0.0;
							double sgst = 0.0;
							double igst = 0.0;
							double taxamount = 0.0;
							String hsn = "";
							String newhsn = "";
							if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {//CGST -SGST
//								System.err.println("CGST -SGST 2");
								cgst = deliveryTaxDTO.getCgstAmount() + (itemPdfDTO.getTaxAmount() / 2);
								sgst = deliveryTaxDTO.getSgstAmount() + (itemPdfDTO.getTaxAmount() / 2);
								taxamount = deliveryTaxDTO.getTaxAmount() + (itemPdfDTO.getTaxAmount());
                                hsn = StringUtils.isBlank(deliveryTaxDTO.getHsnCode()) ? "" : deliveryTaxDTO.getHsnCode();
                                newhsn = StringUtils.isBlank(itemPdfDTO.getHsnCode()) ? "" : itemPdfDTO.getHsnCode();
								int ishsn = 0;
								String[] myName = hsn.split(",");
								for (int p = 0; p < myName.length; p++) {
									String s = myName[p];
									if (s.equals(newhsn)) {
										ishsn = 1;
										break;
									} else {
										ishsn = 0;
									}
								}
								if (ishsn == 0) {
									deliveryTaxDTO.setHsnCode(deliveryTaxDTO.getHsnCode() + "," + newhsn);
								}
                                taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                        :round(itemPdfDTO.getPrice(),2);
								amount = taxableamount + deliveryTaxDTO.getTaxableAmount();
								// log.warning("amount is >>>>>" + amount);
								deliveryTaxDTO.setCgstAmount(cgst);
								deliveryTaxDTO.setSgstAmount(sgst);
								deliveryTaxDTO.setTaxAmount(taxamount);
								deliveryTaxDTO.setTaxableAmount(amount);

								break;
							} else {
//								System.err.println("IGST 2");
                                hsn = StringUtils.isBlank(deliveryTaxDTO.getHsnCode()) ? "" : deliveryTaxDTO.getHsnCode();
                                newhsn = StringUtils.isBlank(itemPdfDTO.getHsnCode()) ? "" : itemPdfDTO.getHsnCode();
                                int ishsn = 0;
                                String[] myName = hsn.split(",");
                                for (int p = 0; p < myName.length; p++) {
                                    String s = myName[p];
                                    if (s.equals(newhsn)) {
                                        ishsn = 1;
                                        break;
                                    } else {
                                        ishsn = 0;
                                    }
                                }
                                if (ishsn == 0) {
                                    deliveryTaxDTO.setHsnCode(deliveryTaxDTO.getHsnCode() + "," + newhsn);
                                }
                                taxamount = deliveryTaxDTO.getTaxAmount() + (itemPdfDTO.getTaxAmount());
                                igst = deliveryTaxDTO.getIgstAmount() + itemPdfDTO.getTaxAmount();
                                taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                        :round(itemPdfDTO.getPrice(),2);

                                amount = taxableamount + deliveryTaxDTO.getTaxableAmount();

                                deliveryTaxDTO.setTaxAmount(taxamount);
                                deliveryTaxDTO.setIgstAmount(igst);
                                deliveryTaxDTO.setTaxableAmount(amount);
                                break;
                            }
                        }
                    }

					if (is_same == 0) {
						if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {//CGST -SGST
//							System.err.println("CGST -SGST 3");
                            double taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                    :round(itemPdfDTO.getPrice(),2);
							DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
							deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
							deliveryTaxDTO2.setTaxableAmount(taxableamount);
							deliveryTaxDTO2.setCgstAmount(itemPdfDTO.getTaxAmount() / 2);
							deliveryTaxDTO2.setCgstRate(itemPdfDTO.getTaxRate() / 2);
							deliveryTaxDTO2.setSgstAmount(itemPdfDTO.getTaxAmount() / 2);
							deliveryTaxDTO2.setSgstRate(itemPdfDTO.getTaxRate() / 2);
							deliveryTaxDTO2.setCessAmount(itemPdfDTO.getCessAmount());
							deliveryTaxDTO2.setCessRate(itemPdfDTO.getCessRate());
							deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
							deliveryTaxDTOs.add(deliveryTaxDTO2);

						} else {//IGST
//							System.err.println("IGST 3");
                            double taxableamount = (itemPdfDTO.getItemNetAmount()!=0) ? round((itemPdfDTO.getItemNetAmount() - itemPdfDTO.getTaxAmount()), 2)
                                    :round(itemPdfDTO.getPrice(),2);
							DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
							deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
							deliveryTaxDTO2.setTaxableAmount(taxableamount);
							deliveryTaxDTO2.setIgstAmount(itemPdfDTO.getTaxAmount());
							deliveryTaxDTO2.setIgstRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setCessAmount(itemPdfDTO.getCessAmount());
							deliveryTaxDTO2.setCessRate(itemPdfDTO.getCessRate());
							deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
							deliveryTaxDTOs.add(deliveryTaxDTO2);
						}
					}
				}
			}

            List<TaxDTO> taxDTOs = new ArrayList<TaxDTO>();

            for (int i = 0; i < deliveryTaxDTOs.size(); i++) {
                DeliveryTaxDTO deliveryTaxDTO5 = deliveryTaxDTOs.get(i);
                //	System.err.println("- "+i+" rate "+deliveryTaxDTO5.getTaxRate()+"amount "+deliveryTaxDTO5.getCgstAmount());
                int same_rate = 0;
                if (i == 0) {
                    TaxDTO taxDTO = new TaxDTO();
                    taxDTO.setCgstAmount(deliveryTaxDTO5.getCgstAmount());
                    taxDTO.setCgstRate(deliveryTaxDTO5.getCgstRate());
                    taxDTO.setSgstAmount(deliveryTaxDTO5.getSgstAmount());
                    taxDTO.setSgstRate(deliveryTaxDTO5.getSgstRate());
                    taxDTO.setIgstAmount(deliveryTaxDTO5.getIgstAmount());
                    taxDTO.setIgstRate(deliveryTaxDTO5.getIgstRate());
                    taxDTO.setTaxRate(deliveryTaxDTO5.getTaxRate());
                    //	System.err.println("first loop");
                    taxDTOs.add(taxDTO);
                    //	System.err.println("size "+taxDTOs.size());
                } else {
                    //	System.err.println("not first "+taxDTOs.size());
                    for (int j = 0; j < taxDTOs.size(); j++) {
                        TaxDTO taxDTO = taxDTOs.get(j);
                        if (deliveryTaxDTO5.getTaxRate() == taxDTO.getTaxRate()) {
                            same_rate = 1;
                            double cgstAmount = 0.0;
                            double sgstAmount = 0.0;
                            double igstAmount = 0.0;
                            //	System.err.println("same "+taxDTO.getTaxRate());
                            cgstAmount = taxDTO.getCgstAmount() + deliveryTaxDTO5.getCgstAmount();
                            sgstAmount = taxDTO.getSgstAmount() + deliveryTaxDTO5.getSgstAmount();
                            igstAmount = taxDTO.getIgstAmount() + deliveryTaxDTO5.getIgstAmount();
                            //	System.err.println("sgst "+sgstAmount +" deliveryTax"+deliveryTaxDTO5.getSgstAmount()+" taxDTO "+taxDTO.getSgstAmount());
                            taxDTO.setCgstAmount(cgstAmount);
                            taxDTO.setSgstAmount(sgstAmount);
                            taxDTO.setIgstAmount(igstAmount);

                            break;
                        }
                    }
                    if (same_rate == 0) {
                        //	System.err.println("not same ");
                        TaxDTO taxDTO = new TaxDTO();
                        taxDTO.setCgstAmount(deliveryTaxDTO5.getCgstAmount());
                        taxDTO.setCgstRate(deliveryTaxDTO5.getCgstRate());
                        taxDTO.setSgstAmount(deliveryTaxDTO5.getSgstAmount());
                        taxDTO.setSgstRate(deliveryTaxDTO5.getSgstRate());
                        taxDTO.setIgstAmount(deliveryTaxDTO5.getIgstAmount());
                        taxDTO.setIgstRate(deliveryTaxDTO5.getIgstRate());
                        taxDTO.setTaxRate(deliveryTaxDTO5.getTaxRate());
                        //System.err.println("first loop");
                        taxDTOs.add(taxDTO);
                    }
                }

            }
            deliveryTaxDTOs.sort((d1, d2) -> (int) d1.getTaxRate() - (int) d2.getTaxRate());
            taxDTOs.sort((t1, t2) -> (int) t1.getTaxRate() - (int) t2.getTaxRate());
            double totaltaxableAmount = 0.00;
            double totaligst = 0.00;
            double totalcgst = 0.00;
            double totalsgst = 0.00;
            double totaltax = 0.00;
            double totaltaxAmount = 0.00;
            double subTotal = 0.00;
            double Total = 0.00;
            double netAmount = 0.00;
            double flatdiscount = 0;
            double totaldiscount = 0.00;
            double taxable = 0.00;
            double dis = 0.0, discount = 0.0;
            double totalCessAmount = 0.0;
            double tcsAmount = 0.0;
            for (int j = 0; j < salesVo.getSalesItemVos().size(); j++) {
                dis = 0.0;
                discount = 0.0;
                SalesItemVo salesItemVo1 = salesVo.getSalesItemVos().get(j);
                taxable = salesItemVo1.getPrice() * salesItemVo1.getQty();
                if (salesItemVo1.getDiscountType() != null && StringUtils.isNotBlank(salesItemVo1.getDiscountType()) && salesItemVo1.getDiscountType().equals("amount")) {
                    discount = salesItemVo1.getDiscount() * salesItemVo1.getQty();
                    totaldiscount = totaldiscount + discount;
                    dis = dis + discount;
                    taxable = taxable - discount;
                    salesItemVo1.setDiscountAmount(discount);
                    salesItemVo1.setDiscountPer((discount * 100) / taxable);

                } else {
                    discount = ((taxable * salesItemVo1.getDiscount()) / 100);
                    totaldiscount = totaldiscount + discount;
                    dis = dis + discount;
                    taxable = taxable - discount;
                    salesItemVo1.setDiscountAmount(discount);
                    salesItemVo1.setDiscountPer(salesItemVo1.getDiscount());

                }

                if (salesItemVo1.getDiscountType2() != null && StringUtils.isNotBlank(salesItemVo1.getDiscountType2()) && salesItemVo1.getDiscountType2().equals("amount")) {
                    discount = salesItemVo1.getDiscount2() * salesItemVo1.getQty();
                    totaldiscount = totaldiscount + discount;
                    dis = dis + discount;
                    taxable = taxable - discount;
                    salesItemVo1.setDiscountAmount2(discount);
                    salesItemVo1.setDiscountPer2((discount * 100) / taxable);

                } else {
                    discount = ((taxable * salesItemVo1.getDiscount2()) / 100);
                    totaldiscount = totaldiscount + discount;
                    dis = dis + discount;
                    taxable = taxable - discount;
                    salesItemVo1.setDiscountAmount2(discount);
                    salesItemVo1.setDiscountPer2(salesItemVo1.getDiscount2());

                }
                totaldiscount = totaldiscount + salesItemVo1.getFlatDiscount();
                flatdiscount = salesItemVo1.getFlatDiscount();
                taxable = taxable - flatdiscount;

//				System.err.println("taxable" + taxable);

                subTotal = subTotal + taxable;
                salesItemVo1.setTotaldiscount(dis);
                salesItemVo1.setTaxable(taxable);
            }
            try {
                List<Map<String, Double>> cess = salesItemRepository.findCessRateBySalesId(id);
                for (Map<String, Double> map : cess) {
                    totalCessAmount += map.get("CessAmount");
                }
                view.addObject("cess", cess);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // log.info("totalCessAmount" + totalCessAmount);
            for (int j = 0; j < salesVo.getSalesAdditionalChargeVos().size(); j++) {
                SalesAdditionalChargeVo accountVo = salesVo.getSalesAdditionalChargeVos().get(j);
                subTotal += round(accountVo.getAmount(), 2);
            }

			for (int i = 0; i < deliveryTaxDTOs.size(); i++) {
				//	System.err.println("- "+i+" rate "+deliveryTaxDTOs.get(i).getTaxRate()+"amount "+deliveryTaxDTOs.get(i).getCgstAmount());
				totaltaxableAmount = totaltaxableAmount + deliveryTaxDTOs.get(i).getTaxableAmount()-deliveryTaxDTOs.get(i).getCessAmount();
				totalcgst = totalcgst + deliveryTaxDTOs.get(i).getCgstAmount();
				totalsgst = totalsgst + deliveryTaxDTOs.get(i).getSgstAmount();
				totaligst = totaligst + deliveryTaxDTOs.get(i).getIgstAmount();
				//	System.err.println("totAL IGST = "+totaligst);
				totaltaxAmount = round(totaltaxAmount + deliveryTaxDTOs.get(i).getTaxAmount(), 2);
			}
            if (salesVo.getTcsJson() != null && !salesVo.getTcsJson().isEmpty() && salesVo.getTcsJson().get(Constant.TCS_APPLICABLE) != null) {
                int tcsApply = Integer.parseInt(salesVo.getTcsJson().get(Constant.TCS_APPLICABLE).toString());
                if (tcsApply == 1)
                    tcsAmount = Double.parseDouble(salesVo.getTcsJson().get(Constant.TCS_AMOUNT).toString());
            }
            Total = subTotal + totaltaxAmount + totalCessAmount + tcsAmount;
            netAmount = Total + salesVo.getRoundoff();

            if (!salesVo.getTermsAndConditionIds().equals("")) {
                List<Long> termandconditionIds = Arrays.asList(salesVo.getTermsAndConditionIds().split("\\s*,\\s*"))
                        .stream().map(Long::parseLong).collect(Collectors.toList());

                view.addObject("TermsAndCondition",
                        termsAndConditionService.getTermAndConditionList(termandconditionIds));
            }

			view.addObject("totalsgst", round(totalsgst, 2));
			view.addObject("totalCessAmount", round(totalCessAmount, 2));
			view.addObject("totalcgst", round(totalcgst, 2));
			view.addObject("totaligst", round(totaligst, 2));
			view.addObject("totaltaxableAmount", round(totaltaxableAmount, 2));
			view.addObject("totaltax", round(totaltax, 2));
			view.addObject("totaltaxAmount", round(totaltaxAmount, 2));
			view.addObject("wordtotal", NumberToWord.getNumberToWord(salesVo.getTotal(), session.getAttribute("currencyName").toString()));
			view.addObject("deliveryTaxDTOs", deliveryTaxDTOs);
			view.addObject("taxDTOs", taxDTOs);
			view.addObject("subTotal", subTotal);
			view.addObject("totaldiscount", totaldiscount);
			view.addObject("Total", Total);
			view.addObject("netAmount", netAmount);
			view.addObject("amount_in_word", NumberToWord.getNumberToWord(salesVo.getTotal(), session.getAttribute("currencyName").toString()));
            List<SalesItemVo> reversedList = new ArrayList<>(salesVo.getSalesItemVos());
            Collections.reverse(reversedList);
            salesVo.setSalesItemVos(reversedList);
			view.addObject("salesVo", salesVo);
			view.addObject("companyVo", companyVo);
            view.addObject("printCount", printLogService.countByTypeIdAndType(salesVo.getSalesId(),Constant.SALES_INVOICE));
			view.addObject("contactVo", contactVo);
			view.addObject("paymentMode", receiptService.getPaymentModeBySalesId(salesVo.getSalesId()));
			if (salesVo.getContactVos() != null) {
//				log.info("transport id " + salesVo.getContactVos().getContactId());
                view.addObject("transporterName", contactService.getContactNameByContactId(salesVo.getContactVos().getContactId()));
            }
            view.addObject("preparedBy", profileService.getName(Long.parseLong(session.getAttribute("userId").toString())));
            view.addObject("FILE_UPLOAD_SERVER", FILE_UPLOAD_SERVER);
            view.addObject("printDateFormat", dateFormatMasterService.getDateFormatMasterByBranchId(Long.parseLong(session.getAttribute("branchId").toString())));
            view.addObject("signatureLogo", userRepository.getSignatureLogoSignedSrc(Long.parseLong(session.getAttribute("branchId").toString()), Constant.FILE_UPLOAD_SERVER_AZURE, FILE_UPLOAD_SERVER));

//             view.addObject("deliveryVo", deliveryVo);
            view.addObject("GST", placeOfSupplyCode.equals(session.getAttribute("stateCode").toString()));
            if (StringUtils.isNotBlank(salesVo.getType())) {
                ReportSettingVo setting = null;
                if (salesVo.getType().equals(Constant.SALES_ESTIMATE)) {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_ESTIMATE,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_DELIVERY_CHALLAN,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                } else {
                    setting = reportService.findByTypeAndBranchId(Constant.SALES_INVOICE,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                }
                view.setViewName((setting != null && setting.getReportVo() != null) ? "sales/" + setting.getReportVo().getReport() : Constant.ERROR_PAGE_404);
                if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                    printLogService.savePrintLog(id, Constant.SALES_INVOICE,"/sales/htmlpdf/{id}");
                }
            }
//             ReportSettingVo setting = reportService.findByTypeAndBranchId(Constant.SALES_INVOICE,
//                     Long.parseLong(session.getAttribute("branchId").toString()));
//             System.err.println("report????????"+salesVo.getType());
//             System.err.println("Report ............"+setting.getReportVo().getReport());
//             view.setViewName("sales/"+setting.getReportVo().getReport());
        } else {
            view.setViewName(Constant.ERROR_PAGE_404);
        }
        return view;
    }

    @ResponseBody
    @RequestMapping("/statistic")
    @Transactional(readOnly = true)
    public SalesStatisticDTO getstatisticData(@RequestParam("daterange") String daterange, @PathVariable(value = "type") String type,
                                              @RequestParam Map<String, String> allRequestParams, HttpSession session) throws ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_STATISTIC;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_STATISTIC;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_STATISTIC;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_STATISTIC;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_STATISTIC;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_STATISTIC;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        SalesStatisticDTO dto = new SalesStatisticDTO();
//    	long branchId =  Long.parseLong(session.getAttribute("branchId").toString());
        List<Long> branchList = new ArrayList<Long>();

        if (StringUtils.isNotBlank(allRequestParams.get("branch"))) {
            branchList = Arrays.asList(allRequestParams.get("branch").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
        } else {
            branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
        }
        Date startDate = null;
        Date endDate = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
//    	System.err.println("daterange---"+daterange);
        if (daterange.equals("currentYear")) {
            calendar.setTime(dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()));
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            calendar.setTime(dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("lastMonth")) {
            calendar.set(Calendar.DAY_OF_MONTH, -1);
            calendar.add(Calendar.DATE, 1);
            int min = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, min);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, max);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("thisMonth")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, max);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("thisWeek")) {
            calendar.add(Calendar.DATE, -7);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DATE, 7);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("lastWeek")) {
            calendar.add(Calendar.DATE, -14);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DATE, 7);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("today")) {
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("customrange")) {
            if (!allRequestParams.get("from").equals("")) {
                String[] Daterange = allRequestParams.get("from").split("-");
                startDate = dateFormat.parse(Daterange[0]);
                endDate = dateFormat.parse(Daterange[1]);
            }
        }

        // System.out.println("branchId "+branchList);
        // System.out.println("startDate "+startDate);
        // System.out.println("endDate "+endDate);
        // System.out.println("type "+type);
        try {

            dto.setAllString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, "0"))));
            dto.setPendingString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.PENDING))));
            dto.setOrderCreatedString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.ORDER_CREATED))));
            dto.setPartiallyCancelString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.PARTIALLY_CANCELLED))));
            dto.setCancelString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.CANCELLED))));
            dto.setInvoiceCreatedString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.INVOICE_CREATED))));
            dto.setInvoicedString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.INVOICED))));
            dto.setPartialInvoiceString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.PARTIAL_INVOICE_CREATED))));
            dto.setDcCreatedString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.DC_CREATED))));
            dto.setDeliverString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.DELIVERED))));
            dto.setPartialDeliverString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscCount(branchList, startDate, endDate, type, Constant.PARTIAL_DC_CREATED))));

            if (type.equals(Constant.SALES_INVOICE)) {
                dto.setTotalamountString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscAmount(branchList, startDate, endDate, type, "total"))));
                dto.setPaidamountString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscAmount(branchList, startDate, endDate, type, "paid"))));
                dto.setUnpaidamountString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscAmount(branchList, startDate, endDate, type, "unpaid"))));

            } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                dto.setTotalamountString(NumberToWord.getCheckData(String.valueOf(salesService.salesStatiscAmount(branchList, startDate, endDate, type, "total"))));
            } else {

            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }

        return dto;
    }

    @PostMapping(value = "/items/{id}")
    @ResponseBody
    public List<Map<String, String>> itemsRetrive(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_ITEMS_DATA, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<Map<String, String>> data = salesService.findBysalesItems(id);

        return data;

    }


    @PostMapping(value = "/items/{id}/{productid}/json/{mainindex}")
    @ResponseBody
    public List<Map<String, String>> salesItemlist(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, @PathVariable("productid") long productId, @PathVariable("mainindex") int mainindex, HttpSession session) throws IOException {
        List<Map<String, String>> data = salesService.findBysalesItemslist(id, productId, mainindex);
//        System.err.println("function callll ");
        return data;

    }

    @PostMapping(value = "/items/orderByupdate")
    @ResponseBody
    public String updateorderBy(HttpServletRequest request, HttpServletResponse response, @RequestParam("salesId") long salesId, @RequestParam("productId") long productId,
                                @RequestParam("orderBy") int orderBy, HttpSession session) throws IOException {
        salesService.updateorderBy(salesId, productId, orderBy);


        return "success";

    }


    @PostMapping("/planning/items/json")
    @ResponseBody
    public List<SalesItemProdPlanningDTO> findSalesItemListForProductionPlanning(@RequestParam(name = "salesId", defaultValue = "0", required = false) long salesId, HttpSession session) {
        List<SalesItemProdPlanningDTO> salesItemProdPlanningDTOs = new ArrayList<>();
        if (salesId != 0) {
            //long branchId = Long.parseLong(session.getAttribute("branchId").toString());
            long companyId = Long.parseLong(session.getAttribute("companyId").toString());
            //String yearInterval = session.getAttribute("financialYear").toString();
            salesItemProdPlanningDTOs = salesService.findSalesItemListForProductionPlanning(salesId);
        } else {
            // log.warning("here productVarientId is 0");
        }
        return salesItemProdPlanningDTOs;

    }

    @PostMapping("/get/contact/json")
    @ResponseBody
    public List<SalesDTO> getContactSalesJson(@RequestParam(name = "contactId", defaultValue = "0", required = false) long contactId,
                                              @PathVariable(value = "type") String type, HttpSession session) {
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        String yearInterval = session.getAttribute("financialYear").toString();
        List<SalesDTO> salesList = new ArrayList<>();
        try {
            List<String> statusList = new ArrayList<>();
            statusList.add(Constant.INPROGRESS);
            salesList = salesService.findCustomSalesListByStatusAndContact(
                    Long.parseLong(session.getAttribute("branchId").toString()), Constant.SALES_ORDER, statusList, contactId);
            // log.warning("HERE-salesList is :" + salesList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesList;
    }

    @PostMapping("/billrestricted/contact")
    @ResponseBody
    public String findTotalPendingBills(@RequestParam(name = "contactId", defaultValue = "0", required = false) long contactId, @PathVariable(value = "type") String type, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CONTACT_BILL_RESTRICTED;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CONTACT_BILL_RESTRICTED;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CONTACT_BILL_RESTRICTED;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CONTACT_BILL_RESTRICTED;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CONTACT_BILL_RESTRICTED;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CONTACT_BILL_RESTRICTED;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<String> typeList = new ArrayList<>();
        String msg = "0";
        try {
            String value = companySettingService.getValueByTypeAndBranchId(Long.parseLong(session.getAttribute("branchId").toString()), Constant.FORUSERTYPE);
            if (StringUtils.isNotBlank(value)) {
                if (value.equals("1")) {
                    typeList.add("invoice");
                } else if (value.equals("2")) {
                    typeList.add("pos");
                } else {
                    typeList.add("invoice");
                    typeList.add("pos");
                }
            } else {
                typeList.add("invoice");
                typeList.add("pos");
            }
            int count = salesService.checkTotalPendingBillsByTypeAndBranchIdAndIsDeletedAndPaidAmountIsZeroAndContactId(typeList, Long.parseLong(session.getAttribute("branchId").toString()), 0, contactId);
            int noOfBills = 1;
            value = companySettingService.getAddValueByTypeAndBranchId(Constant.NOOFPREVIOUSBILL, Long.parseLong(session.getAttribute("branchId").toString()));
            if (StringUtils.isNotBlank(value)) {
                noOfBills = Integer.parseInt(value);
            }
            if (count > 0 && noOfBills <= count) {
                value = companySettingService.getValueByTypeAndBranchId(Long.parseLong(session.getAttribute("branchId").toString()), Constant.LASTBILLDUE);
                msg = count + "|" + value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    @PostMapping("{id}/gettotalreceipt/{receiptId}")
    @ResponseBody
    public String gettotalreceipt(@PathVariable("id") long id, @PathVariable("receiptId") long receiptId, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_RECEIPT_TOTAL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            String s = receiptService.gettotalreceipt(id, Long.parseLong(session.getAttribute("branchId").toString()), receiptId);
            jsonObject.put("status", "200");
            jsonObject.put("total", s);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("status", "500");
            jsonObject.put("total", "0");
        }


        return jsonObject.toString();
    }

    @PostMapping("/checkunpaid/json")
    @ResponseBody
    public String unpaidinvoicecheck(@RequestParam("salesIds") String id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CHECK_UNPAID, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        if (StringUtils.isNotBlank(id)) {
            List<Long> list = Stream.of(id.split(",")).map(Long::parseLong).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            List<Map<String, String>> unpaidsales = salesService.checkunpaidsales(list, Long.parseLong(session.getAttribute("branchId").toString()));
            // log.info("unpaidsales"+unpaidsales.size());
            if (unpaidsales.size() > 0) {
                jsonObject.put("status", true);
                jsonObject.put("data", unpaidsales);
            } else {
                jsonObject.put("status", false);
            }
            return jsonObject.toString();
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", false);
            return jsonObject.toString();
        }

    }

    //=======new sales flow changes============//
    @PostMapping("/contact/json")
    @ResponseBody
    public List<SalesDTO> getSalesJsonByContact(@RequestParam(name = "contactId", defaultValue = "0", required = false) long contactId,
                                                @RequestParam(name = "salesFor", defaultValue = "", required = false) String salesFor,
                                                @PathVariable(value = "type") String type, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CONTACT_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CONTACT_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CONTACT_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CONTACT_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CONTACT_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CONTACT_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
//    	long companyId=Long.parseLong(session.getAttribute("companyId").toString());
//    	String yearInterval = session.getAttribute("financialYear").toString();
        List<SalesDTO> salesList = new ArrayList<>();
        List<String> partiallyStatus = new ArrayList<>();
        partiallyStatus.add(Constant.PARTIALLY_CANCELLED);
        try {
            List<String> statusList = new ArrayList<>();
            List<String> parentType = new ArrayList<>();
            parentType.add("default");
            if (type.equals(Constant.SALES_ESTIMATE)) {
                statusList.add(Constant.PENDING);
            } else if (type.equals(Constant.SALES_ORDER) && salesFor.equals(Constant.SALES_INVOICE)) {
                statusList.add(Constant.PENDING);
                statusList.add(Constant.PARTIAL_INVOICE_CREATED);
            } else if (type.equals(Constant.SALES_ORDER) && salesFor.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                statusList.add(Constant.PENDING);
                statusList.add(Constant.PARTIAL_DC_CREATED);
            } else if (type.equals(Constant.SALES_INVOICE)) {
                statusList.add(Constant.INVOICED);
                statusList.add(Constant.PARTIAL_DC_CREATED);

                parentType.add(Constant.SALES_DELIVERY_CHALLAN);
            } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                statusList.add(Constant.DELIVERED);
                statusList.add(Constant.PARTIAL_INVOICE_CREATED);

                parentType.add(Constant.SALES_INVOICE);
                salesList = salesService.findCustomSalesListByStatusAndContactAndParentTypeNotAndEwayBillNo(branchId, type, statusList, contactId, parentType);
                return salesList;
            }
            salesList = salesService.findCustomSalesListByStatusAndContactAndParentTypeNot(branchId, type, statusList, contactId, parentType);
            // log.warning("HERE-salesList is :" + salesList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesList;
    }

    @RequestMapping("/{salesId}/salesitem/json")
    @ResponseBody
    public List<Map<String, Object>> getSalesItemJson(@PathVariable(value = "type") String type, @PathVariable(value = "salesId") long salesId,
                                                      @RequestParam(value = "page", required = false, defaultValue = "") String page,
                                                      HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_ITEMS_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_ITEMS_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_ITEMS_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_ITEMS_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_ITEMS_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_ITEMS_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        String financialYear = session.getAttribute(Constant.FINANCIAL_YEAR).toString();
        long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
        List<SalesItemCUSTOMDTO> salesItemData = salesService.findSalesItemDetails(salesId, financialYear);
        Map<String,Object> salesFlatDiscountData = salesService.calculateFlatDiscountPercentageFromAmount(salesId);
        double flatDiscount, flatDiscountInPercentage;
        String taxType, flatDiscountType;
        if (!salesFlatDiscountData.isEmpty()) {
            flatDiscount = Double.parseDouble(salesFlatDiscountData.get("flatDiscount").toString());
            flatDiscountInPercentage = Double.parseDouble(salesFlatDiscountData.get("flatDiscountPercentage").toString());
            flatDiscountType = StringUtils.isNotBlank(salesFlatDiscountData.get("flatDiscountType").toString()) ? salesFlatDiscountData.get("flatDiscountType").toString() : Constant.PERCENTAGE;
        } else {
            flatDiscountType = Constant.PERCENTAGE;
            flatDiscountInPercentage = 0.0;
            flatDiscount = 0.0;
        }
        taxType = salesRepository.findTaxType(salesId);
        // log.warning("SIZE---->"+salesItemData.size());
        List<Map<String, Object>> salesItemList = new ArrayList<>();
        CompanySettingVo b2bStockOutBy = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.B2BSTOCKOUT);
        salesItemData.forEach(si -> {
            String salesType = type;
            Map<String, Object> salesItem = new HashMap<>();
            if (StringUtils.isNotBlank(page) && page.equals("edit")) {
                float orderQty = 0;
                String parentSalesItemIds = si.getParentSalesItemIds();
                if (StringUtils.isNotBlank(parentSalesItemIds)) {
                    List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                            .map(Long::parseLong).collect(Collectors.toList());
                    if (!parentItemIds.isEmpty()) {
                        for (int i = 0; i < parentItemIds.size(); i++) {
                            long parentSalesItemId = parentItemIds.get(i);
                            // log.warning("parentSalesItemId--->"+parentSalesItemId);
                            SalesItemQtyDTO salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                            if (salesItemQtyDTO != null) {
                                orderQty += salesItemQtyDTO.getQty();
                            }
                        }
                    }
                }
                salesItem.put("parentOrderQty", orderQty);
            }
            salesItem.put("product", si);
            long batchId = si.getBatchId();
            String batchNo = si.getBatchNo();

            if (type.equals(Constant.SALES_DELIVERY_CHALLAN) && b2bStockOutBy.getValue() == 0) {
                salesType = "all";
            } else if (type.equals(Constant.SALES_INVOICE) && b2bStockOutBy.getValue() == 1) {
                salesType = "all";
            }
            if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                if (si.getStockByInvoice() == 1) {
                    salesType = "all";
                }
            } else if (type.equals(Constant.SALES_INVOICE)) {
                if (si.getStockByDc() == 1) {
                    salesType = "all";
                }
            }
            List<StockMasterVo> stockMasterVos = stockMasterService
                    .findByProductVarientsVoProductVarientIdAndCompanyIdAndYearIntervalWithBatchCheck(
                            si.getProductVarientId(), branchId, financialYear, companyId, salesType, 0, 0);
            stockMasterVos.forEach(stockMasterVo -> {
                String uomValue = stockMasterVo.getProductVarientsVo().getProductVo().getUnitOfMeasurementVo().getMeasurementCode();
                stockMasterVo.setUom(uomValue);
            });

            salesItem.put("flatDiscount", flatDiscount);
            salesItem.put("flatDiscountInPercentage", flatDiscountInPercentage);
            salesItem.put("flatDiscountType", flatDiscountType);
            salesItem.put("taxType", taxType);
            salesItem.put("batchId", batchId);
            salesItem.put("batchNo", batchNo);
            salesItem.put("stockMasterVos", stockMasterVos);
            salesItemList.add(salesItem);
        });
        return salesItemList;

    }

    @RequestMapping("/{salesId}/additionalcharge/json")
    @ResponseBody
    public List<SalesAdditionalChargeDTO> getSalesAdditionalchargeJson(@PathVariable(value = "salesId") long salesId,
                                                                       HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_ADDITIONAL_CHARGE_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<SalesAdditionalChargeDTO> salesAdditionalChargeDTOList = salesService.findSalesAdditionalChargeDetails(salesId);
        // log.warning("SIZE---->"+salesAdditionalChargeDTOList.size());
        return salesAdditionalChargeDTOList;

    }

    @RequestMapping("/{salesId}/history/json")
    @ResponseBody
    public List<SalesHistoryVo> getSalesHistoryJson(@PathVariable(value = "salesId") long salesId,
                                                    HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_HISTORY_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<SalesHistoryVo> salesHistoryVos = salesHistoryRepository.findByMainSalesIdOrderBySalesHistoryIdDesc(salesId);
        try {
            salesHistoryVos.forEach(sh -> sh.setCreatedByName(userRepository.getName(sh.getUserId())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesHistoryVos;
    }

    @RequestMapping("/items")
    @ResponseBody
    public List<SalesViewItemDTO> getSalesItems(@RequestParam(value = "id") long id,
                                                @RequestParam(name = "length", required = false, defaultValue = "10") int length,
                                                @RequestParam(name = "offset", required = false, defaultValue = "0") int offset, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_ITEMS_DETAILS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        try {
            return salesService.getItemsBySalesId(id, length, offset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping("/productwisestock")
    @ResponseBody
    public ApiResponse getProductWiseStock(@RequestParam Map<String, String> allRequestParams, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_PRODUCT_STOCK, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ApiResponse apiResponse = new ApiResponse();
        List<Map<String, String>> list = salesRepository.getBatchWiseData(Long.valueOf(allRequestParams.get("varientId")), Long.valueOf(allRequestParams.get("branchId")), session.getAttribute(Constant.FINANCIAL_YEAR).toString());
        apiResponse.setResponse(list);
        apiResponse.setStatus(true);
        return apiResponse;
    }


    @RequestMapping("/{contactId}/datatableCutomerWise")
    @ResponseBody
    public DataTableContacWiseSalesDto getContactWiseSalesDetails(@PathVariable String type, @PathVariable long contactId, @RequestParam Map<String, String> allRequestParams, HttpSession session) throws ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CUSTOMER_DATATABLE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CUSTOMER_DATATABLE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CUSTOMER_DATATABLE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CUSTOMER_DATATABLE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CUSTOMER_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CUSTOMER_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DataTableContacWiseSalesDto dto = new DataTableContacWiseSalesDto();
        List<String> salesTypes = new ArrayList<>(2);
        if (StringUtils.equals(type,Constant.SALES_INVOICE)) {
            salesTypes.add(Constant.SALES_INVOICE);
            salesTypes.add(Constant.SALES_POS);
        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
            salesTypes.add(Constant.SALES_CREDIT_NOTE);
            salesTypes.add(Constant.SALES_POS_RETURN);
        }

        long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
        int userType = (session.getAttribute(Constant.USER_TYPE).toString().equals("2") || (session.getAttribute("parentUserType").toString().equals("2") && Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString()) > 4) ? 1 : 0);

        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);

        Calendar calendar = Calendar.getInstance();
        Date startDate;
        Date endDate;

        calendar.setTime(dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString()));
        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        calendar.setTime(dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString()));
        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));

        int totalLength = salesService.getCountInvoiceDetailsUsingContactId(contactId, salesTypes, startDate, endDate, userType, companyId, branchId);
        int length, page = 0, offset = 0;

        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");

        if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
            page = Integer.parseInt(allRequestParams.get(Constant.START)) / length;
            offset = page * length;
        } else {
            length = totalLength;
        }

        dto.setData(salesService.getInvoiceDetailsUsingContactId(contactId, length, offset, salesTypes, startDate, endDate, userType, companyId, branchId));
        dto.setDraw(Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        dto.setError(null);
        dto.setRecordsFiltered(totalLength);
        dto.setRecordsTotal(totalLength);
        dto.setDataTableMetaDTO(new DataTableMetaDTO(page, (int) ((double) (totalLength) / length), length, totalLength));
        return dto;
    }

    @RequestMapping("/checkreceipt/{id}")
    @ResponseBody
    public String checkReceiptAlreadyGeneratedOrNot(HttpSession session, @PathVariable(value = "id") long salesId) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CHECK_RECEIPT_DETAIL, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        try {
            long branchId = Long.parseLong(session.getAttribute("branchId").toString());
            int receiptCount = receiptBillRepository.getReceiptCountBySalesIdAndBranchId(salesId, branchId);
            if (receiptCount > 0) {
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/create")
    public ModelAndView insertSales(@RequestParam Map<String, String> allRequestParams,
                                    @RequestParam(name = "parentSalesIds", required = false) long[] parentSalesIds,
                                    @PathVariable(value = "type") String type, @ModelAttribute("salesVo") SalesVo salesVo, HttpSession session,
                                    HttpServletRequest servletRequest) throws IOException {

        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CREATE;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREATE;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CREATE;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CREATE;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CREATE;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREATE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        long merchantTypeId = Long.parseLong(session.getAttribute("merchantTypeId").toString());
        String clusterId = session.getAttribute("clusterId").toString();
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());

        ModelAndView view = new ModelAndView();
        ContactAddressVo contactAddressVo;
        long isEdit = salesVo.getSalesId();
        int isReceiptCreated = 0;
        int isInvoiceCreatedAlready = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date firstDateFinacialYear = null, lastDateFinacialYear = null;
        int isFinacialYear = 0;
        String salesNumber = salesVo.getPrefix() + salesVo.getSalesNo();
        boolean isAllowSalesTransaction = true;
        try {
            if (StringUtils.isNotBlank(type)) {
                if (type.equals(Constant.SALES_INVOICE) && isEdit != 0) {
                    int receiptCount = receiptBillRepository.getReceiptCountBySalesIdAndBranchId(isEdit, branchId);
                    if (receiptCount > 0) {
                        isReceiptCreated = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (StringUtils.isNotBlank(session.getAttribute("firstDateFinancialYear").toString())
                    && StringUtils.isNotBlank(session.getAttribute("lastDateFinancialYear").toString())) {
                isFinacialYear = 1;
                firstDateFinacialYear = dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString());
                lastDateFinacialYear = dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString());
                // log.info("firstDateFinancialYear >>>>" + firstDateFinacialYear);
                // log.info("lastDateFinacialYear >>>>" + lastDateFinacialYear);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (StringUtils.isNotBlank(type)) {
                if (type.equals(Constant.SALES_INVOICE) && isEdit == 0) {
                    // log.info("sales Number ::: " + salesNumber);
                    int count = salesRepository.findBySalesPrefixAndSalesNoAndBranchId(salesNumber, branchId,
                            Constant.SALES_INVOICE, 0, isFinacialYear, firstDateFinacialYear, lastDateFinacialYear);
                    // log.info("count :: " + count);
//							log.info("branchId :: " + branchId);
                    if (count != 0) {
                        isInvoiceCreatedAlready = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // log.info("isInvoiceCreatedAlready :: " + isInvoiceCreatedAlready);
        if (isReceiptCreated == 1) {
            view.addObject("errorMessage", "editissue");
            view.setViewName("redirect:/sales/" + type + "/" + isEdit);
        } else if (isInvoiceCreatedAlready == 1) {
            long salesId = salesRepository.findSalesIdBySalesNoAndBranchId(salesNumber, branchId,
                    Constant.SALES_INVOICE, 0, isFinacialYear, firstDateFinacialYear, lastDateFinacialYear);
            view.setViewName("redirect:/sales/" + type + "/" + salesId);
        } else {
            salesVo.setType(type);
            if (salesVo.getSalesId() == 0L) {
                if (type.equals(Constant.SALES_ORDER) || type.equals(Constant.SALES_ESTIMATE)) {
                    salesVo.setStatus(Constant.PENDING);
                } else if (type.equals(Constant.SALES_INVOICE)) {
                    if(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()) == Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()) &&
                            (MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) || merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER) &&
                            StringUtils.isBlank(salesVo.getParentType())){
                                salesVo.setStatus(Constant.HOLD);
                                isAllowSalesTransaction = false;
                    }else{
                        salesVo.setStatus(Constant.INVOICED);
                    }
                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    salesVo.setStatus(Constant.DELIVERED);
                }
            }
            if (salesVo.getParentSalesType() == Constant.CHILD_PARENT_TYPE_ORDER) {
                salesVo.setParentType(Constant.SALES_ORDER);
            } else if (salesVo.getParentSalesType() == Constant.CHILD_PARENT_TYPE_INVOICE) {
                salesVo.setParentType(Constant.SALES_INVOICE);
            } else if (salesVo.getParentSalesType() == Constant.CHILD_PARENT_TYPE_DC) {
                salesVo.setParentType(Constant.SALES_DELIVERY_CHALLAN);
            }
            try {
                if (type.equals(Constant.SALES_INVOICE)) {
                    if (salesVo.getSalesId() != 0L) {
                        Map<String, String> einvoiceMap = salesRepository.getEInvoiceDetails(salesVo.getSalesId());
                        if (!einvoiceMap.isEmpty()) {
                            salesVo.setIrnNo(einvoiceMap.get("irn_no"));
                            salesVo.setAckDate(einvoiceMap.get("ack_date"));
                            salesVo.setAckNo(einvoiceMap.get("ack_no"));
                            salesVo.setEinvoiceQRCode(einvoiceMap.get("einvoice_qr_code"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//		        if (salesVo.getSalesVo() != null && salesVo.getSalesVo().getSalesId() != 0) {
//		            if (type.equals(Constant.SALES_ORDER)) {
//		                salesService.updateStatusBySalesId(Constant.ORDER_CREATED, salesVo.getSalesVo().getSalesId(),
//		                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
//		            } else if (type.equals(Constant.SALES_INVOICE) && StringUtils.isNotBlank(allRequestParams.get("parentType"))
//		            		&& StringUtils.equals(allRequestParams.get("parentType"), Constant.SALES_ESTIMATE)) {
//		                log.warning("parentType-----------------"+allRequestParams.get("parentType"));
//		                salesService.updateStatusBySalesId(Constant.INVOICE_CREATED, salesVo.getSalesVo().getSalesId(),
//		                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
//		            }
//		        }
            salesVo.setNote(allRequestParams.get("note"));
            salesVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
            salesVo.setModifiedOn(CurrentDateTime.getCurrentDate());
            salesVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
            salesVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
            if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                salesService.updateCreditNoteAmount(salesVo.getSalesVo().getSalesId(), salesVo.getSalesId(), salesVo.getTotal());
            }

            try {
                salesVo.setSalesDate(dateFormat.parse(allRequestParams.get("salesDate")));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                salesVo.setShippingDate(dateFormat.parse(allRequestParams.get("shippingDate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                salesVo.setTransportDate(dateFormat.parse(allRequestParams.get("transportDate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                if (allRequestParams.get("dueDate") != null)
                    salesVo.setDueDate(dateFormat.parse(allRequestParams.get("dueDate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                if (!salesVo.getTermsAndConditionIds().equals("")) {
                    salesVo.setTermsAndConditionIds(
                            salesVo.getTermsAndConditionIds().substring(0, salesVo.getTermsAndConditionIds().length() - 1));
                } else {

                }
            } catch (Exception e) {
            }

            SalesVo salesVo2 = null;

            if (allRequestParams.get("billingAddressId").equals("0")
                    || allRequestParams.get("shippingAddressId").equals("0")) {
                salesVo2 = salesService.findBySalesIdAndBranchId(salesVo.getSalesId(), salesVo.getBranchId());
            }

            // --------------Set Billing Address Details ------------------------
            if (!allRequestParams.get("billingAddressId").equals("0")) {
                contactAddressVo = contactService
                        .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));

                salesVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
                salesVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
                salesVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
                salesVo.setBillingCompanyName(StringUtils.isNotBlank(allRequestParams.get("billingcompanyname")) ?
                        allRequestParams.get("billingcompanyname") : contactAddressVo.getCompanyName());
                salesVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
                salesVo.setBillingFirstName(StringUtils.isNotBlank(allRequestParams.get("billingfirstname")) ? allRequestParams.get("billingfirstname") : (StringUtils.isNotBlank(contactAddressVo.getFirstName()) ? contactAddressVo.getFirstName() : ""));
                salesVo.setBillingLastName(StringUtils.isNotBlank(allRequestParams.get("billinglastname")) ? allRequestParams.get("billinglastname") : (StringUtils.isNotBlank(contactAddressVo.getLastName()) ? contactAddressVo.getLastName() : ""));
                salesVo.setBillingPinCode(allRequestParams.get("billingpincode"));
                salesVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
                salesVo.setBillingPhoneNo(allRequestParams.get("billingphone"));
                salesVo.setBillingGstin(StringUtils.isNotBlank(salesVo.getBillingGstin()) ? salesVo.getBillingGstin() : contactAddressVo.getGstin());
            } else if (salesVo2 != null) {
                try {
                    salesVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
                    salesVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
                    salesVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
                    salesVo.setBillingCompanyName(allRequestParams.get("billingcompanyname"));
                    salesVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
                    salesVo.setBillingFirstName(StringUtils.isNotBlank(allRequestParams.get("billingfirstname")) ? allRequestParams.get("billingfirstname") : (StringUtils.isNotBlank(salesVo2.getShippingFirstName()) ? salesVo2.getShippingFirstName() : ""));
                    salesVo.setBillingLastName(StringUtils.isNotBlank(allRequestParams.get("billinglastname")) ? allRequestParams.get("billinglastname") : (StringUtils.isNotBlank(salesVo2.getShippingLastName()) ? salesVo2.getShippingLastName() : ""));
                    salesVo.setBillingPinCode(allRequestParams.get("billingpincode"));
                    salesVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
                    salesVo.setBillingPhoneNo(allRequestParams.get("billingphone"));
                    salesVo.setBillingGstin(salesVo2.getBillingGstin());
                } catch (Exception e) {
                    e.printStackTrace();
                    salesVo.setBillingAddressLine1(salesVo2.getBillingAddressLine1());
                    salesVo.setBillingAddressLine2(salesVo2.getBillingAddressLine2());
                    salesVo.setBillingCityCode(salesVo2.getBillingCityCode());
                    salesVo.setBillingCompanyName(salesVo2.getBillingCompanyName());
                    salesVo.setBillingCountriesCode(salesVo2.getBillingCountriesCode());
                    salesVo.setBillingFirstName(salesVo2.getBillingFirstName());
                    salesVo.setBillingLastName(salesVo2.getBillingLastName());
                    salesVo.setBillingPinCode(salesVo2.getBillingPinCode());
                    salesVo.setBillingStateCode(salesVo2.getBillingStateCode());
                    salesVo.setBillingPhoneNo(salesVo2.getBillingPhoneNo());
                    salesVo.setBillingGstin(salesVo2.getBillingGstin());
                }
            }

            // --------------Set Shipping Address Details ------------------------
            if (!allRequestParams.get("shippingAddressId").equals("0")) {
                contactAddressVo = contactService
                        .findByContactAddressId(Long.parseLong(allRequestParams.get("shippingAddressId")));
                if (contactAddressVo != null) {
                    String name = StringUtils.isNotBlank(contactAddressVo.getFirstName()) ? contactAddressVo.getFirstName() : (StringUtils.isNotBlank(contactAddressVo.getFirstName()) ? contactAddressVo.getFirstName() : "");
                    //		            salesVo.setShippingAddressLine1(contactAddressVo.getAddressLine1());
                    //		            salesVo.setShippingAddressLine2(contactAddressVo.getAddressLine2());
                    //		            salesVo.setShippingCityCode(contactAddressVo.getCityCode());
                    //		            salesVo.setShippingCompanyName(contactAddressVo.getCompanyName());
                    //		            salesVo.setShippingCountriesCode(contactAddressVo.getCountriesCode());
                    //		            salesVo.setShippingFirstName(contactAddressVo.getFirstName());
                    //		            salesVo.setShippingLastName(contactAddressVo.getLastName());
                    //		            salesVo.setShippingPinCode(contactAddressVo.getPinCode());
                    //		            salesVo.setShippingStateCode(contactAddressVo.getStateCode());
                    //		            salesVo.setShippingPhoneNo(contactAddressVo.getPhoneNo());
                    //		            salesVo.setShippingGstin(contactAddressVo.getGstin());

                    //data set via parameter, for edited shipping address
                    salesVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
                    salesVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
                    salesVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
                    salesVo.setShippingCompanyName(StringUtils.isNotBlank(allRequestParams.get("shippingcompanyname")) ?
                            allRequestParams.get("shippingcompanyname") : contactAddressVo.getCompanyName());
                    salesVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
                    salesVo.setShippingFirstName(StringUtils.isNotBlank(allRequestParams.get("shippingfirstname")) ? allRequestParams.get("shippingfirstname") : (StringUtils.isNotBlank(contactAddressVo.getFirstName()) ? contactAddressVo.getFirstName() : ""));
                    salesVo.setShippingLastName(StringUtils.isNotBlank(allRequestParams.get("shippinglastname")) ? allRequestParams.get("shippinglastname") : (StringUtils.isNotBlank(contactAddressVo.getLastName()) ? contactAddressVo.getLastName() : ""));
                    salesVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
                    salesVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
                    salesVo.setShippingPhoneNo(allRequestParams.get("shippingphone"));
                    salesVo.setShippingGstin(StringUtils.isNotBlank(salesVo.getShippingGstin()) ? salesVo.getShippingGstin() : contactAddressVo.getGstin());
                }
            } else if (salesVo2 != null) {
                try {
                    salesVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
                    salesVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
                    salesVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
                    salesVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
                    salesVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
                    salesVo.setShippingFirstName(StringUtils.isNotBlank(allRequestParams.get("shippingfirstname")) ? allRequestParams.get("shippingfirstname") : (StringUtils.isNotBlank(salesVo2.getShippingFirstName()) ? salesVo2.getShippingFirstName() : ""));
                    salesVo.setShippingLastName(StringUtils.isNotBlank(allRequestParams.get("shippinglastname")) ? allRequestParams.get("shippinglastname") : (StringUtils.isNotBlank(salesVo2.getShippingLastName()) ? salesVo2.getShippingLastName() : ""));
                    salesVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
                    salesVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
                    salesVo.setShippingPhoneNo(allRequestParams.get("shippingphone"));
                    salesVo.setShippingGstin(salesVo2.getShippingGstin());
                } catch (Exception e) {
                    e.printStackTrace();
                    salesVo.setShippingAddressLine1(salesVo2.getShippingAddressLine1());
                    salesVo.setShippingAddressLine2(salesVo2.getShippingAddressLine2());
                    salesVo.setShippingCityCode(salesVo2.getShippingCityCode());
                    salesVo.setShippingCompanyName(salesVo2.getShippingCompanyName());
                    salesVo.setShippingCountriesCode(salesVo2.getShippingCountriesCode());
                    salesVo.setShippingFirstName(salesVo2.getShippingFirstName());
                    salesVo.setShippingLastName(salesVo2.getShippingLastName());
                    salesVo.setShippingPinCode(salesVo2.getShippingPinCode());
                    salesVo.setShippingStateCode(salesVo2.getShippingStateCode());
                    salesVo.setShippingPhoneNo(salesVo2.getShippingPhoneNo());
                    salesVo.setShippingGstin(salesVo2.getShippingGstin());
                }
            }

            if (salesVo.getSalesId() == 0) {
                salesVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
                salesVo.setCreatedOn(CurrentDateTime.getCurrentDate());
                salesVo.setPaidAmount(0.0);
            }
            if (salesVo.getSalesAdditionalChargeVos() != null) {
                salesVo.getSalesAdditionalChargeVos().removeIf(rm -> rm.getAdditionalChargeVo() == null);
                salesVo.getSalesAdditionalChargeVos().forEach(item1 -> item1.setSalesVo(salesVo));
            }

            if (salesVo.getSalesItemVos() != null) {
                salesVo.getSalesItemVos().removeIf(rm -> rm.getProductVarientsVo() == null);
                salesVo.getSalesItemVos().forEach(item -> item.setSalesVo(salesVo));
            }
            if (salesVo.getSalesItemVos() != null) {
                if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                    salesVo.getSalesItemVos().forEach(s -> {
                        String parentSalesItemIds = s.getParentSalesItemIds();
                        // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                        // log.warning("s.getQty()-->"+s.getQty());
                        if (StringUtils.isNotBlank(parentSalesItemIds)) {
                            List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                    .map(Long::parseLong).collect(Collectors.toList());
                            if (!parentItemIds.isEmpty()) {
                                long parentSalesItemId = parentItemIds.get(0);
                                BigDecimal qty = new BigDecimal(String.valueOf(s.getQty()));
                                BigDecimal freeQty = new BigDecimal(String.valueOf(s.getFreeQty()));
                                if (s.getSalesItemId() != 0) {
                                    BigDecimal originalQty = new BigDecimal(String.valueOf(s.getOriginalQty()));
                                    qty = qty.subtract(originalQty);
                                    freeQty = freeQty.subtract(new BigDecimal(String.valueOf(s.getOriginalFreeQty())));
                                }
                                int result = salesService.updateSalesItemCreditNoteQtyPlus(parentSalesItemId, qty.doubleValue(),freeQty.doubleValue());
                                // log.warning("result-->"+result);
                            }
                        }
                    });
                }
                String parentType = "";
                if (StringUtils.isNotBlank(allRequestParams.get("parentType")) && StringUtils.equals(allRequestParams.get("parentType"), Constant.SALES_ESTIMATE)) {
                    parentType = Constant.SALES_ESTIMATE;
                }
                if (!StringUtils.equals(parentType, Constant.SALES_ESTIMATE) &&
                        (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                    salesVo.getSalesItemVos().forEach(s -> {
                        String parentSalesItemIds = s.getParentSalesItemIds();
                        // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                        // log.warning("s.getQty()-->"+s.getQty());
                        if (StringUtils.isNotBlank(parentSalesItemIds)) {
                            List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                    .map(Long::parseLong).collect(Collectors.toList());
                            if (!parentItemIds.isEmpty()) {
                                if (parentItemIds.size() > 1) {
                                    // Here Multiple Parent Sales Items
                                    // System.out.println("START=======Here Multiple Parent Sales Items=======START");
                                    BigDecimal qty = new BigDecimal(String.valueOf(s.getQty())); // 25
                                    BigDecimal freeQty = new BigDecimal(String.valueOf(s.getFreeQty()));
                                    if (s.getSalesItemId() != 0) {
                                        Collections.reverse(parentItemIds);
                                        BigDecimal originalQty = new BigDecimal(String.valueOf(s.getOriginalQty()));
                                        qty = qty.subtract(originalQty); // 25 - 30 = -5
                                        freeQty = freeQty.subtract(new BigDecimal(String.valueOf(s.getOriginalFreeQty())));
                                    }
                                    // System.out.println("qty--->" + qty);
                                    for (int i = 0; i < parentItemIds.size(); i++) {
                                        long parentSalesItemId = parentItemIds.get(i);
                                        // System.out.println("parentSalesItemId--->" + parentSalesItemId);
                                        SalesItemQtyDTO salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);

                                        if (qty.compareTo(BigDecimal.ZERO) != 0  || freeQty.compareTo(BigDecimal.ZERO) != 0) {
                                            if (salesItemQtyDTO != null) {
                                                BigDecimal actualOrderQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                                BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty()));
                                                BigDecimal updateQty = BigDecimal.ZERO;
                                                BigDecimal updateFreeQty = BigDecimal.ZERO;

                                                if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                                                    BigDecimal invoiceQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getInvoiceQty()));
                                                    BigDecimal invoiceFreeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getInvoiceFreeQty()));
                                                    actualOrderQty = actualOrderQty.subtract(invoiceQty);
                                                    actualOrderFreeQty = actualOrderFreeQty.subtract(invoiceFreeQty);
                                                } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                                                    BigDecimal dcQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getDcQty()));
                                                    BigDecimal dcFreeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getDcFreeQty()));
                                                    actualOrderQty = actualOrderQty.subtract(dcQty);
                                                    actualOrderFreeQty = actualOrderFreeQty.subtract(dcFreeQty);
                                                }

                                                BigDecimal orderQty = qty;

                                                if (orderQty.compareTo(actualOrderQty) >= 0) {
                                                    updateQty = actualOrderQty;
                                                } else {
                                                    updateQty = orderQty;
                                                }
                                                    updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty: freeQty;
                                                // System.out.println("updateQty-->" + updateQty);
                                                if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                                                    int result = salesService.updateSalesItemInvoiceQtyPlus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                                    // System.out.println("result-->" + result);
                                                } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                                                    int result = salesService.updateSalesItemDcQtyPlus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                                    // System.out.println("result-->" + result);
                                                }
                                                qty = qty.subtract(updateQty);
                                                freeQty = freeQty.subtract(updateFreeQty);
                                            }
                                        }
                                    }
                                    // System.out.println("END=======Here Multiple Parent Sales Items=======END");
                                } else {
                                    // log.warning("START=======Here Single Parent Sales Items=======START");
                                    long parentSalesItemId = parentItemIds.get(0);
                                    //float updateQty = 0;
                                    BigDecimal updateQty = BigDecimal.ZERO;
                                    BigDecimal updateFreeQty = BigDecimal.ZERO;
                                    if (s.getSalesItemId() == 0) {
                                        //updateQty = s.getQty();
                                        updateQty = new BigDecimal(String.valueOf(s.getQty()));
                                        updateFreeQty = new BigDecimal(String.valueOf(s.getFreeQty()));
                                    } else {
                                        // log.warning("s.getOriginalQty()-->"+s.getOriginalQty());
                                        //updateQty = (s.getQty()-s.getOriginalQty());
                                        BigDecimal originalQty = new BigDecimal(String.valueOf(s.getOriginalQty()));
                                        BigDecimal originalFreeQty = new BigDecimal(String.valueOf(s.getOriginalFreeQty()));
                                        BigDecimal qty = new BigDecimal(String.valueOf(s.getQty()));
                                        BigDecimal freeQty = new BigDecimal(String.valueOf(s.getFreeQty()));
                                        updateQty = qty.subtract(originalQty);
                                        updateFreeQty = freeQty.subtract(originalFreeQty);
                                    }

                                    // log.warning("updateQty-->"+updateQty);
                                    if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                                        int result = salesService.updateSalesItemInvoiceQtyPlus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                        // log.warning("result-->"+result);
                                    } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                                        int result = salesService.updateSalesItemDcQtyPlus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                        // log.warning("result-->"+result);
                                    }
                                    // log.warning("END=======Here Single Parent Sales Items=======END");
                                }
                            }
                        }
                    });
                }
            }

            if (allRequestParams.get("deleteSalesItemIds") != null
                    && !allRequestParams.get("deleteSalesItemIds").equals("")) {
                String address = allRequestParams.get("deleteSalesItemIds").substring(0,
                        allRequestParams.get("deleteSalesItemIds").length() - 1);
                List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
                if (salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                    if (!l.isEmpty()) {
                        for (int j = 0; j < l.size(); j++) {
                            long salesItemId = l.get(j);
                            SalesItemQtyDTO salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(salesItemId);
                            if (salesItemQtyDTO != null) {
                                String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                    List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                            .map(Long::parseLong).collect(Collectors.toList());
                                    if (!parentItemIds.isEmpty()) {
                                        long parentSalesItemId = parentItemIds.get(0);
                                        BigDecimal updateQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                        // log.warning("updateQty-->"+updateQty);
                                        int result = salesService.updateSalesItemCreditNoteQtyMinus(parentSalesItemId, updateQty.doubleValue(),Double.parseDouble(String.valueOf(salesItemQtyDTO.getFreeQty())));
                                        // log.warning("result-->"+result);
                                    }
                                }
                            }
                        }
                    }
                }
                if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    if (!l.isEmpty()) {
                        for (int j = 0; j < l.size(); j++) {
                            long salesItemId = l.get(j);
                            SalesItemQtyDTO salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(salesItemId);
                            if (salesItemQtyDTO != null) {
                                String parentSalesItemIds = salesItemQtyDTO.getParentSalesItemIds();
                                // log.warning("parentSalesItemIds-->"+parentSalesItemIds);
                                // log.warning("salesItemQtyDTO.getQty()-->"+salesItemQtyDTO.getQty());
                                if (StringUtils.isNotBlank(parentSalesItemIds)) {
                                    List<Long> parentItemIds = Arrays.asList(parentSalesItemIds.split(",")).stream()
                                            .map(Long::parseLong).collect(Collectors.toList());
                                    if (!parentItemIds.isEmpty()) {
                                        if (parentItemIds.size() > 1) {//Here Multiple Parent Sales Items
                                            // log.warning("START=======Here Multiple Parent Sales Items DELETE=======START");
                                            BigDecimal qty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                            BigDecimal freeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty()));
                                            //double qty = salesItemQtyDTO.getQty(); //25
                                            // log.warning("qty--->"+qty);
                                            for (int i = 0; i < parentItemIds.size(); i++) {
                                                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                                                    long parentSalesItemId = parentItemIds.get(i);
                                                    // log.warning("parentSalesItemId--->"+parentSalesItemId);
                                                    SalesItemQtyDTO itemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesItemId(parentSalesItemId);
                                                    if (itemQtyDTO != null) {
                                                        BigDecimal updateQty = BigDecimal.ZERO;
                                                        BigDecimal updateFreeQty = BigDecimal.ZERO;
                                                        BigDecimal actualOrderQty = new BigDecimal(String.valueOf(itemQtyDTO.getQty()));
                                                        BigDecimal actualOrderFreeQty = new BigDecimal(String.valueOf(itemQtyDTO.getFreeQty()));
                                                        BigDecimal orderQty = qty;//25
                                                        // log.warning("actualOrderQty--->" + actualOrderQty);
                                                        // log.warning("orderQty--->" + orderQty);

                                                        if (orderQty.compareTo(actualOrderQty) >= 0) {//150>=100 //50>=100
                                                            updateQty = actualOrderQty;//100
                                                        } else {
                                                            updateQty = orderQty;//50
                                                        }
                                                            updateFreeQty = freeQty.compareTo(actualOrderFreeQty) >= 0 ? actualOrderFreeQty:freeQty;

                                                        // log.warning("updateQty-->" + updateQty);
                                                        if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                            int result = salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                                            // log.warning("result-->" + result);
                                                        } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                                || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                            int result = salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());//100
                                                            // log.warning("result-->" + result);
                                                        }
                                                        qty = qty.subtract(updateQty);//150-100=50
                                                    }

                                                }

                                            }
                                            // log.warning("END=======Here Multiple Parent Sales Items DELETE=======END");
                                        } else {
                                            long parentSalesItemId = parentItemIds.get(0);
                                            //salesService.updateSalesItemDcQtyMinus(parentSalesItemId, 0);
                                            BigDecimal updateQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getQty()));
                                            BigDecimal updateFreeQty = new BigDecimal(String.valueOf(salesItemQtyDTO.getFreeQty()));
                                            // log.warning("updateQty-->"+updateQty);
                                            if (salesVo.getType().equals(Constant.SALES_INVOICE) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                    || salesVo.getParentType().equals(Constant.SALES_DELIVERY_CHALLAN))) {
                                                int result = salesService.updateSalesItemInvoiceQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());
                                                // log.warning("result-->"+result);
                                            } else if (salesVo.getType().equals(Constant.SALES_DELIVERY_CHALLAN) && (salesVo.getParentType().equals(Constant.SALES_ORDER)
                                                    || salesVo.getParentType().equals(Constant.SALES_INVOICE))) {
                                                int result = salesService.updateSalesItemDcQtyMinus(parentSalesItemId, updateQty.doubleValue(),updateFreeQty.doubleValue());//100
                                                // log.warning("result-->"+result);
                                            }
                                        }
                                    }
                                }

                            }
                        }

                    }
                }
                if (salesVo != null && salesVo.getSalesId() != 0) {
                    salesService.deleteSalesItem(l, salesVo.getSalesId());
                }
            }

            if (allRequestParams.get("deleteAdditionalChargeIds") != null
                    && !allRequestParams.get("deleteAdditionalChargeIds").equals("")) {

                String address = allRequestParams.get("deleteAdditionalChargeIds").substring(0,
                        allRequestParams.get("deleteAdditionalChargeIds").length() - 1);
                List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());

                salesService.deleteSalesAdditionalItem(l);
            }
            if (allRequestParams.get("addproductby") != null && (allRequestParams.get("addproductby").equals("2"))) {
                salesService.createSalesForUploadExcelSheet(type, companyId, branchId, merchantTypeId, clusterId, session.getAttribute(Constant.FINANCIAL_YEAR).toString(),
                        Integer.parseInt(session.getAttribute(Constant.DECIMAL_POINT).toString()), (String) session.getAttribute(Constant.FILE_PATH), allRequestParams.get("gstTaxType"), salesVo);
            }

            String tcsApplicable = allRequestParams.get(Constant.TCS_APPLICABLE_NEW);

            if (StringUtils.isNotBlank(tcsApplicable) && tcsApplicable != null) {
                if(Integer.parseInt(tcsApplicable)==1) {
                    Map<String, Object> jsonData = new HashMap<>();
                    jsonData.put(Constant.TCS_APPLICABLE, Integer.parseInt(tcsApplicable));
                    String tcsId = allRequestParams.get(Constant.TCS_ACCOUNT_CUSTOM_ID);
                    String tcsRate = allRequestParams.get(Constant.TCS_RATE_NEW);
                    String tcsAmount = allRequestParams.get(Constant.TCS_AMOUNT_NEW);
                    String tcsType = allRequestParams.get(Constant.TCS_TYPE_NEW);

                    if (StringUtils.isNotBlank(tcsId) && tcsId != null) {
                        jsonData.put(Constant.TCS_ACCOUNT_ID, Long.parseLong(tcsId.trim()));
                    }
                    if (StringUtils.isNotBlank(tcsRate) && tcsRate != null) {
                        jsonData.put(Constant.TCS_RATE, Double.parseDouble(tcsRate.trim()));
                    }
                    if (StringUtils.isNotBlank(tcsAmount) && tcsAmount != null) {
                        jsonData.put(Constant.TCS_AMOUNT, Double.parseDouble(tcsAmount.trim()));
                    }
                    if (StringUtils.isNotBlank(tcsType) && tcsType != null) {
                        jsonData.put(Constant.TCS_TYPE, tcsType.trim());
                    }
                    salesVo.setTcsJson(jsonData);
                }
            }
            if (allRequestParams.get("gstApply") == null) {
                salesVo.setGstApply(1);
                String taxCode = Constant.GST;
                int taxType = Constant.TAX_TYPE_GST;
                try {
                    Map<String, String> gstMap = userRepository.getgstDetails(Long.parseLong(session.getAttribute("companyId").toString()));
                    if (gstMap != null && !gstMap.isEmpty()) {
                        if (StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"), Constant.VAT)) {
                            taxCode = Constant.VAT;
                            taxType = Constant.TAX_TYPE_VAT;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String tax_code = "";
                TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(0, Long.parseLong(session.getAttribute("companyId").toString()), taxType, tax_code);
                if (taxVo != null)
                    salesVo.getSalesItemVos().forEach(p -> {
                        p.setTaxVo(taxVo);
                        p.setTaxRate(0);
                    });
            }
            salesVo2 = salesService.save(salesVo);

            if (isEdit == 0) {
                try {
                    long newSalesNo = salesService.findMaxSalesNo(Long.parseLong(session.getAttribute("branchId").toString()), type,
                            salesVo2.getPrefix(), Long.parseLong(session.getAttribute("userId").toString()));
                    // log.warning("send newSalesNo:" + salesVo.getBranchId());
//		        		messagingTemplate.convertAndSendToUser(""+salesVo.getBranchId(), "/queue/sales/"+type,newSalesNo);
                } catch (Exception e) {
                    // log.severe("errorrrrrrrrrr at send newsalesno");
                    e.printStackTrace();
                }
            }

            try {
                if (isEdit == 0) {
                    salesService.saveSalesHistory(Constant.HISTORY_TYPE_NEW, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()),
                            salesVo2.getTotal(), "", salesVo2.getStatus(), 0, "", "", 0, session);
                } else {
                    salesService.saveSalesHistory(Constant.HISTORY_TYPE_EDITED, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()),
                            salesVo2.getTotal(), "", salesVo2.getStatus(), 0, "", "", 0, session);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(StringUtils.isNotBlank(salesVo2.getStatus()) && salesVo2.getStatus().equals(Constant.HOLD)){
                isAllowSalesTransaction = false;
            }

            if (isEdit == 0) {
                // log.info("isEdit-------------------is 0---New Case");
                if (StringUtils.isNotBlank(allRequestParams.get("parentSalesIds"))) {
                    // log.warning("parentSalesIds------>"+allRequestParams.get("parentSalesIds"));
//						List<Long> parentSalesIds  = Arrays.asList(allRequestParams.get("parentSalesIds").split(",")).stream()
//								.map(Long::parseLong).collect(Collectors.toList());
                    int result = salesMappingRepository.deleteSalesMappingByMainSalesId(salesVo2.getSalesId());
                    // log.warning("deleteSalesMappingByMainSalesId---->"+result);
                    // log.warning("parentPurchaseIds is not null");
                    for (long parentSalesId : parentSalesIds) {
                        // log.warning("parent Sales Id-->"+parentSalesId);
                        SalesDTO salesDTO = salesRepository.findCustomSalesBySalesId(parentSalesId);
                        String salesNo = "";
                        String parentType = "";
                        String fromStatus = "";
                        SalesMappingVo salesMappingVo = new SalesMappingVo();
                        if (salesDTO != null) {
                            salesNo = salesDTO.getPrefix() + salesDTO.getSalesNo();
                            parentType = salesDTO.getType();
                            fromStatus = salesDTO.getStatus();
                        }
                        salesMappingVo.setParentSalesId(parentSalesId);
                        salesMappingVo.setParentSalesType(parentType);
                        salesMappingVo.setParentSalesNo(salesNo);
                        salesMappingVo.setMainSalesId(salesVo2.getSalesId());
                        salesMappingVo.setMainSalesType(salesVo2.getType());
                        salesMappingVo.setMainSalesNo(salesVo2.getPrefix() + salesVo2.getSalesNo());
                        salesMappingRepository.save(salesMappingVo);

                        String parentStatus = Constant.PENDING;
                        if (type.equals(Constant.SALES_ORDER)) {//HERE ESTIMATE STATUS
                            parentStatus = Constant.ORDER_CREATED;
                            salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_ORDER);
                        } else if (type.equals(Constant.SALES_INVOICE) && StringUtils.equals(parentType, Constant.SALES_ESTIMATE)) {//HERE ESTIMATE STATUS
                            parentStatus = Constant.INVOICE_CREATED;
                            salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_INVOICE);
                        } else if (type.equals(Constant.SALES_INVOICE)
                                && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN))) {//HERE ORDER/DC STATUS
                            salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_INVOICE);
                            List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                            List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty()) < (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            List<SalesItemQtyDTO> exceedList = salesItemQtyDTO.stream().filter(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty()) > (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty()) == (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                            // log.warning("partiallyList length--------->"+partiallyList.size());
                            // log.warning("completeList length--------->"+completeList.size());
                            if (StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN)) {
                                parentStatus = Constant.DELIVERED;
                            }
                            if (partiallyList.isEmpty()) {
                                if (completeList.size() == salesItemQtyDTO.size() || exceedList.size() > 0) {
                                    parentStatus = Constant.INVOICE_CREATED;
                                }
                            } else {
                                parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                            }
                        } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)
                                && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_INVOICE))) {//HERE ORDER/INVOICE STATUS
                            salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_DC);
                            List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                            List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> (p.getDcQty()+p.getDcFreeQty()) <(p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            List<SalesItemQtyDTO> exceedList = salesItemQtyDTO.stream().filter(p -> (p.getDcQty()+p.getDcFreeQty()) > (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> (p.getDcQty()+p.getDcFreeQty()) == (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                            // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                            // log.warning("partiallyList length--------->"+partiallyList.size());
                            // log.warning("completeList length--------->"+completeList.size());
                            if (StringUtils.equals(parentType, Constant.SALES_INVOICE)) {
                                parentStatus = Constant.INVOICED;
                            }
                            if (partiallyList.isEmpty()) {
                                if (completeList.size() == salesItemQtyDTO.size() || exceedList.size() > 0) {
                                    parentStatus = Constant.DC_CREATED;
                                }
                            } else {
                                parentStatus = Constant.PARTIAL_DC_CREATED;
                            }
                        }
                        // log.warning("Parent Status---->"+parentStatus+" parentSalesId--->"+parentSalesId);
                        //here update status in sales Parent
                        salesService.updateStatusBySalesId(parentStatus, parentSalesId,
                                Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                        salesService.saveSalesHistory(Constant.HISTORY_TYPE_CHILDCREATED, parentSalesId, parentType, salesNo,
                                0, "", parentStatus, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()), 0, session);
                        salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, parentSalesId, parentType, salesNo,
                                0, fromStatus, parentStatus, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()), 0, session);
                    }
                } else {
                    // log.warning("parentSalesIds is null");
                }

            } else {
                // log.info("isEdit-------------------is "+isEdit+"---Edit Case");
                if (salesVo2.getChildType() != 0 && (type.equals(Constant.SALES_ORDER) || type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_DELIVERY_CHALLAN))) {
                    String parentStatus = Constant.PENDING;
                    long parentSalesId = salesVo2.getSalesId();
                    if (salesVo2.getChildType() == Constant.CHILD_PARENT_TYPE_INVOICE) {//ORDER and DC have child as invoice
                        List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                        List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() < p.getQty()).collect(Collectors.toList());
                        List<SalesItemQtyDTO> exceedList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() > p.getQty()).collect(Collectors.toList());
                        List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getInvoiceQty() == p.getQty()).collect(Collectors.toList());
                        // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                        // log.warning("partiallyList length--------->"+partiallyList.size());
                        // log.warning("completeList length--------->"+completeList.size());
                        if (type.equals(Constant.SALES_INVOICE)) {
                            parentStatus = Constant.INVOICED;
                        }
                        if (partiallyList.isEmpty()) {
                            if (completeList.size() == salesItemQtyDTO.size() || exceedList.size() > 0) {
                                parentStatus = Constant.INVOICE_CREATED;
                            }
                        } else {
                            double totalInvoiceQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getInvoiceQty()).sum();
                            double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
                            if (totalInvoiceQty > 0) {
                                if (totalInvoiceQty < totalQty) {
                                    parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                }
                            }
                        }
                    } else if (salesVo2.getChildType() == Constant.CHILD_PARENT_TYPE_DC) {//ORDER and INVOICE has child as DC
                        List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                        List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() < p.getQty()).collect(Collectors.toList());
                        List<SalesItemQtyDTO> exceedList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() > p.getQty()).collect(Collectors.toList());
                        List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> p.getDcQty() == p.getQty()).collect(Collectors.toList());
                        // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                        // log.warning("partiallyList length--------->"+partiallyList.size());
                        // log.warning("completeList length--------->"+completeList.size());
                        if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                            parentStatus = Constant.DELIVERED;
                        }
                        if (partiallyList.isEmpty()) {
                            if (completeList.size() == salesItemQtyDTO.size() || exceedList.size() > 0) {
                                parentStatus = Constant.DC_CREATED;
                            }
                        } else {
                            double totalDcQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getDcQty()).sum();
                            double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> p.getQty()).sum();
                            if (totalDcQty > 0) {
                                if (totalDcQty < totalQty) {
                                    parentStatus = Constant.PARTIAL_DC_CREATED;
                                }
                            }
                        }
                    }
                    // log.warning("Parent Status---->"+parentStatus+" parentSalesId--->"+parentSalesId);
                    //here update status in sales Parent
                    salesService.updateStatusBySalesId(parentStatus, parentSalesId,
                            Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                    salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()),
                            0, salesVo2.getStatus(), parentStatus, 0, "", "", 0, session);
                }

                try {

                    List<SalesMappingVo> mappingVos = salesMappingRepository.findByMainSalesId(salesVo2.getSalesId());
                    String fromStatus = salesVo2.getStatus();
                    if (!mappingVos.isEmpty()) {
                        for (int i = 0; i < mappingVos.size(); i++) {
                            Long mainSalesId = mappingVos.get(i).getMainSalesId();
                            String mainSalesNo = mappingVos.get(i).getMainSalesNo();
                            String mainSalesType = mappingVos.get(i).getMainSalesType();
                            Long parentSalesId = mappingVos.get(i).getParentSalesId();
                            String salesNo = mappingVos.get(i).getParentSalesNo();
                            //find sales packing
                            // log.warning("parentSalesId---->" + parentSalesId);
                            if (parentSalesId != null && parentSalesId != 0) {
                                String parentType = mappingVos.get(i).getParentSalesType();
                                //for estimate and order status of that parent is PENDING
                                String parentStatus = Constant.PENDING;
                                if (salesVo.getType().equals(Constant.SALES_ORDER)) {
                                    parentStatus = Constant.ORDER_CREATED;
                                } else if (salesVo.getType().equals(Constant.SALES_INVOICE) && StringUtils.equals(parentType, Constant.SALES_ESTIMATE)) {
                                    parentStatus = Constant.INVOICE_CREATED;
                                }
                                if (salesVo.getType().equals(Constant.SALES_INVOICE)
                                        && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN))) {//HERE ORDER/DC STATUS
                                    salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_INVOICE);
                                    List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                    List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty()) < (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                                    List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty()) == (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                                    // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                    // log.warning("partiallyList length--------->"+partiallyList.size());
                                    // log.warning("completeList length--------->"+completeList.size());
                                    if (StringUtils.equals(parentType, Constant.SALES_DELIVERY_CHALLAN)) {
                                        parentStatus = Constant.DELIVERED;
                                    }
                                    if (partiallyList.size() == salesItemQtyDTO.size()) {
                                        double totalInvoiceQty = salesItemQtyDTO.stream().mapToDouble(p -> (p.getInvoiceQty()+p.getInvoiceFreeQty())).sum();
                                        double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> (p.getQty()+p.getQty())).sum();
                                        if (totalInvoiceQty > 0) {
                                            if (totalInvoiceQty < totalQty) {
                                                parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                            }
                                        }
                                    } else if (completeList.size() == salesItemQtyDTO.size()) {
                                        parentStatus = Constant.INVOICE_CREATED;
                                    } else {
                                        parentStatus = Constant.PARTIAL_INVOICE_CREATED;
                                    }
                                } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)
                                        && (StringUtils.equals(parentType, Constant.SALES_ORDER) || StringUtils.equals(parentType, Constant.SALES_INVOICE))) {//HERE ORDER/INVOICE STATUS
                                    salesService.updateChildType(parentSalesId, Constant.CHILD_PARENT_TYPE_DC);
                                    List<SalesItemQtyDTO> salesItemQtyDTO = salesRepository.findSalesItemQtyDetailsBySalesId(parentSalesId);
                                    List<SalesItemQtyDTO> partiallyList = salesItemQtyDTO.stream().filter(p -> (p.getDcQty()+p.getDcFreeQty()) < (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                                    List<SalesItemQtyDTO> completeList = salesItemQtyDTO.stream().filter(p -> (p.getDcQty()+p.getDcFreeQty()) == (p.getQty()+p.getFreeQty())).collect(Collectors.toList());
                                    // log.warning("salesItemQtyDTO length--------->"+salesItemQtyDTO.size());
                                    // log.warning("partiallyList length--------->"+partiallyList.size());
                                    // log.warning("completeList length--------->"+completeList.size());
                                    if (StringUtils.equals(parentType, Constant.SALES_INVOICE)) {
                                        parentStatus = Constant.INVOICED;
                                    }
                                    if (partiallyList.size() == salesItemQtyDTO.size()) {
                                        double totalDcQty = salesItemQtyDTO.stream().mapToDouble(p -> (p.getDcQty()+p.getDcFreeQty())).sum();
                                        double totalQty = salesItemQtyDTO.stream().mapToDouble(p -> (p.getQty()+p.getFreeQty())).sum();
                                        if (totalDcQty > 0) {
                                            if (totalDcQty < totalQty) {
                                                parentStatus = Constant.PARTIAL_DC_CREATED;
                                            }
                                        }

                                    } else if (completeList.size() == salesItemQtyDTO.size()) {
                                        parentStatus = Constant.DC_CREATED;
                                    } else {
                                        parentStatus = Constant.PARTIAL_DC_CREATED;
                                    }
                                }
                                // log.warning("Parent Status---->"+parentStatus+" parentSalesId--->"+parentSalesId);
                                //here update status in sales Parent
                                if (StringUtils.equals(parentStatus, Constant.PENDING) || StringUtils.equals(parentStatus, Constant.DELIVERED)
                                        || StringUtils.equals(parentStatus, Constant.INVOICED)) {
                                    salesService.updateChildType(parentSalesId, 0);
                                }
                                salesService.updateStatusBySalesId(parentStatus, parentSalesId,
                                        Long.parseLong(session.getAttribute("userId").toString()), CurrentDateTime.getCurrentDate());
                                salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, parentSalesId, parentType, salesNo,
                                        0, fromStatus, parentStatus, mainSalesId, mainSalesType, mainSalesNo, 0, session);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


            if (salesVo2.getType().equals(Constant.SALES_INVOICE)) {

                ShiprocketVo shiprocketVo = shiprocketService.getDetails(Long.parseLong(session.getAttribute("companyId").toString()));

                if (shiprocketVo != null) {
                    if (shiprocketVo.getDefaultSyncInvoice() == 1 && salesVo2.getDefaultSyncInvoice() == 1) {
                        try {
                            boolean flag = false;
                            String token = (String) session.getAttribute("shiprocketauthtoken");
                            if (token == null || token == "") {
                                flag = false;
                            } else {
                                flag = shiprocketService.getAllOrder(token);
                            }
                            if (!flag || token == null) {
                                shiprocketService.createShipRocketAuthToken(servletRequest, session);
                                // System.out.println("4");
                                token = (String) session.getAttribute("shiprocketauthtoken");
                                // System.out.println("5" + token);
                                shiprocketService.shipRocketCreateOrder(salesVo2, token, servletRequest);
                                // System.out.println("6");

                            } else {
                                shiprocketService.shipRocketCreateOrder(salesVo, token, servletRequest);

                            }
                        } catch (Exception e) {
                            // System.out.println("Exception at Shiprocket service:------" + e);
                        }
                        // xyz=shiprocket.placeorder(sales);
                        // sales.shiprocketStatus= xyz.status
                    }
                }

            } else {
                if (salesVo2.getType().equals(Constant.SALES_CREDIT_NOTE)) {
                    salesService.insertSalesReturnTransaction(salesVo2, session.getAttribute("financialYear").toString(), session);
                    returnUpdateInParent(salesVo2.getSalesId(), salesVo2.getSalesVo().getSalesId());
                }

            }

            //sales stock save
            if(isAllowSalesTransaction){
                if (salesVo2.getType().equals(Constant.SALES_INVOICE) || salesVo2.getType().equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    salesService.insertSalesTransaction(salesVo2, session.getAttribute(Constant.FINANCIAL_YEAR).toString(), session);
                }
            }

            if (allRequestParams.get("saveandpayment") != null && Integer.parseInt(allRequestParams.get("saveandpayment")) == 1) {


                if (allRequestParams.get("typePayment") != null && allRequestParams.get("typePayment").equals("1")) {
//		                List<ReceiptVo> receiptVos = receiptService.findByBranchIdAndIsDeletedAndContactVoContactIdAndType(
//		                        Long.parseLong(session.getAttribute("branchId").toString()), 0,
//		                        salesVo2.getContactVo().getContactId(), Constant.PAYMENT_TYPE_ADVANCE);
                    String advancePaymentBill = allRequestParams.get("advancePaymentBillId");
                    List<ReceiptVo> receiptVos = new ArrayList<ReceiptVo>();
                    if (advancePaymentBill != null && !advancePaymentBill.equals("")) {
                        List<Long> l = Arrays.asList(advancePaymentBill.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
                        receiptVos = receiptService.findByBranchidandisDeletedAndTypeAndReceiptId(Long.parseLong(session.getAttribute("branchId").toString()), 0, Constant.PAYMENT_TYPE_ADVANCE, l);
                    }

                    double salesTotal = salesVo2.getTotal(), totalPayment = 0;
                    for (ReceiptVo receiptVo2 : receiptVos) {

                        if (salesTotal != 0) {

                            double receiptbilltotal = receiptVo2.getReceiptBillVos().stream()
                                    .mapToDouble(p -> p.getTotalPayment()).sum();


                            if (receiptbilltotal != receiptVo2.getTotalPayment()) {

                                double receiptAmount = receiptVo2.getTotalPayment() - receiptbilltotal;
                                ReceiptBillVo billVo = new ReceiptBillVo();
                                if (salesTotal <= receiptAmount) {

                                    billVo.setKasar(0);
                                    billVo.setOldPyament(0);
                                    billVo.setReceiptVo(receiptVo2);
                                    billVo.setSalesVo(salesVo2);
                                    billVo.setTotalPayment(salesTotal);
                                    salesTotal = 0;
                                } else {

                                    billVo.setKasar(0);
                                    billVo.setOldPyament(0);
                                    billVo.setReceiptVo(receiptVo2);
                                    billVo.setSalesVo(salesVo2);
                                    billVo.setTotalPayment(receiptAmount);
                                    salesTotal -= receiptAmount;
                                }

                                totalPayment += billVo.getTotalPayment();
                                receiptService.saveBill(billVo);
                                receiptService.transation(receiptVo2, 0);
                            }

                        }
                    }
                    salesService.updatePaidAmountPlus(salesVo2.getSalesId(), totalPayment);

                }

                if (allRequestParams.get("totalPayment") != null && !allRequestParams.get("totalPayment").equals("0")) {

                    ReceiptVo receiptVo = new ReceiptVo();
                    receiptVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
                    receiptVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
                    receiptVo.setReceiptNo(receiptService.getNewPaymentNo(Constant.RECEIPT,
                            Long.parseLong(session.getAttribute("branchId").toString()),
                            Long.parseLong(session.getAttribute("userId").toString()), "PAY",
                            Long.parseLong(session.getAttribute("companyId").toString())));
                    receiptVo.setPrefix(prefixService.getPrefixByPrefixTypeAndBranchId(
                                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.RECEIPT, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
                    receiptVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
                    receiptVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
                    receiptVo.setCreatedOn(CurrentDateTime.getCurrentDate());
                    receiptVo.setModifiedOn(CurrentDateTime.getCurrentDate());
                    receiptVo.setAmount(Double.parseDouble(allRequestParams.get("totalPayment")));
                    receiptVo.setReceiptDate(salesVo2.getSalesDate());
                    receiptVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                    receiptVo.setReceiptMode("Cash");
                    receiptVo.setPaymentType("cash");
                    receiptVo.setType(Constant.PAYMENT_TYPE_AGAINSTBILL);
                    //receiptVo.setContactVo(salesVo2.getContactVo());
                    receiptVo.setPartyAccountVo(salesVo2.getContactVo().getAccountCustomVo());
                    receiptVo.setStatus("cleared");
                    try {
                        receiptVo.setDescription(allRequestParams.get("description"));
                    } catch (Exception e) {
                        // TODO: handle exception
                        // log.severe(String.valueOf(e));
                    }

                    if (allRequestParams.get("receiptMode").equals("bank")) {

                        BankVo bankVo = new BankVo();
                        bankVo.setBankId(Long.parseLong(allRequestParams.get("bankVoId")));
                        receiptVo.setBankVo(bankVo);
                        receiptVo.setReceiptMode("bank");
                        receiptVo.setPaymentType("bank");
                        receiptVo.setBankAccountNo(allRequestParams.get("accountNo"));
                        receiptVo.setBankTransactionType(allRequestParams.get("bankpaymentmode"));
                        receiptVo.setAcceptedBankReceipt(1);
                        try {
                            receiptVo.setChequeDate(dateFormat.parse(allRequestParams.get("chequeDate")));
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        AccountCustomVo cashAccountCustomVo = new AccountCustomVo();
                        cashAccountCustomVo.setAccountCustomId(Long.parseLong(allRequestParams.get("accountCustomVo.accountCustomId")));
                        receiptVo.setCashAccountCustomVo(cashAccountCustomVo);
                    }

                    ReceiptBillVo receiptBillVo = new ReceiptBillVo();
                    List<ReceiptBillVo> receiptBillVos = new ArrayList<>();
                    receiptBillVo.setKasar(Double.parseDouble(allRequestParams.get("Kasar")));
                    receiptBillVo.setOldPyament(0);
                    receiptBillVo.setReceiptVo(receiptVo);
                    receiptBillVo.setSalesVo(salesVo2);
                    receiptBillVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                    receiptBillVos.add(receiptBillVo);
                    receiptVo.setReceiptBillVos(receiptBillVos);
                    ReceiptVo receiptVo3 = receiptService.save(receiptVo);
                    receiptService.transation(receiptVo3, receiptBillVo.getKasar());
                    salesService.updatePaidAmountPlus(salesVo2.getSalesId(),
                            Double.parseDouble(allRequestParams.get("totalPayment")) + receiptBillVo.getKasar());
                    salesService.saveSalesHistory(Constant.HISTORY_TYPE_RECEIPT_NEW, salesVo2.getSalesId(), salesVo2.getType(), (salesVo2.getPrefix() + salesVo2.getSalesNo()),
                            salesVo2.getTotal(), "", salesVo2.getStatus(), receiptVo3.getReceiptId(), Constant.RECEIPT, (receiptVo3.getPrefix() + receiptVo3.getReceiptNo()), receiptVo.getTotalPayment(), session);

                }

            }

            // PDF TOKEN CODE
            if (allRequestParams.get("salesId") == null) {
                // new case
                String pdfToken = EncryptMessage.getSecureMessage(
                        salesVo.getSalesId() + salesVo.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
                salesService.updateToken(salesVo2.getSalesId(), pdfToken);
                salesVo2.setPdfToken(pdfToken);
            } else if (salesVo2.getPdfToken() == null) {
                String pdfToken = EncryptMessage.getSecureMessage(
                        salesVo2.getSalesId() + salesVo2.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
                salesService.updateToken(salesVo2.getSalesId(), pdfToken);
                salesVo2.setPdfToken(pdfToken);
            }


            // EDIT INVOICE MERCHANT TO CUSTOMER
            VasyMessageSettingVo messageSettingVo = vasyMessageSettingService.getVasyMessageSettingSaleTypeWise(salesVo2, isEdit);

            if (messageSettingVo != null && messageSettingVo.getIsActive() == 1) {
                if ((salesVo2.getContactVo() != null) && ((StringUtils.isNotBlank(salesVo2.getContactVo().getWhatsappNo())
                        || StringUtils.isNotBlank(salesVo2.getContactVo().getMobNo())))) {
                    if (isEdit == 0) {
                        // new cases
                        if (type.equals(Constant.SALES_INVOICE)) {
                            globalMessageService.sendMerchantToCustomerNewInvoiceMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                            globalMessageService.sendMerchantToCustomerNewCreditNoteMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                        } else if (type.equals(Constant.SALES_ESTIMATE)) {
                            globalMessageService.sendMerchantToCustomerNewEditSalesEstimateMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_NEW_ESTIMATE);
                        } else if (type.equals(Constant.SALES_ORDER)) {
                            globalMessageService.sendMerchantToCustomerNewEditSalesOrderMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_NEW_ORDER);

                        } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                            globalMessageService.sendMerchantToCustomerNewEditDeliveryChallanMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_NEW_DELIVERY_CHALLAN);
                        }
                    } else {
                        if (type.equals(Constant.SALES_INVOICE)) {
                            globalMessageService.sendMerchantToCustomerEditInvoiceMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                        } else if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                            globalMessageService.sendMerchantToCustomerEditCreditNoteMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo());
                        } else if (type.equals(Constant.SALES_ESTIMATE)) {
                            globalMessageService.sendMerchantToCustomerNewEditSalesEstimateMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_EDIT_ESTIMATE);

                        } else if (type.equals(Constant.SALES_ORDER)) {
                            globalMessageService.sendMerchantToCustomerNewEditSalesOrderMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_EDIT_ORDER);

                        } else if (type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                            globalMessageService.sendMerchantToCustomerNewEditDeliveryChallanMessage(salesVo2,
                                    Long.parseLong(session.getAttribute("companyId").toString()),
                                    Long.parseLong(session.getAttribute("branchId").toString()),
                                    Long.parseLong(session.getAttribute("userId").toString()),
                                    servletRequest.getServletContext().getRealPath("/"),
                                    session.getAttribute("realPath").toString(),
                                    session.getAttribute("currencyCode").toString(),
                                    session.getAttribute("currencyName").toString(),
                                    session.getAttribute("decimalPoint").toString(),
                                    salesVo2.getContactVo().getWhatsappNo(), salesVo2.getContactVo().getMobNo(),
                                    MessageConstant.MERCHANT_EVENT_SALES_EDIT_DELIVERY_CHALLAN);

                        }

                    }
                }
            }


            if (messageSettingVo == null || messageSettingVo.getIsActive() == 0) {
                if (salesVo2.getContactVo() != null && salesVo2.getContactVo().getMobNo() != null) {
                    try {
                        String whatsappToken = session.getAttribute("whatsappToken") != null
                                && StringUtils.isNotBlank(session.getAttribute("whatsappToken").toString())
                                ? session.getAttribute("whatsappToken").toString()
                                : "";
                        sendSMS(salesVo2, companyId, whatsappToken, session.getAttribute("name").toString(), session,
                                servletRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (salesVo2.getContactVo() != null) {
                    if (salesVo2.getContactVo().getEmail() != null) {
                        CompanySettingVo allowInvoiceEmail = companySettingService.findByCompanyIdAndType(companyId,
                                Constant.ALLOWINVOICEEMAIL);
                        if (allowInvoiceEmail != null && allowInvoiceEmail.getValue() == 1) {
                            String body = Converter.convertToHtml(servletRequest,
                                    "/media/download/sales/" + salesVo2.getPdfToken() + "/mail");
                            sendGridEmailService.sendHTML(from, salesVo2.getContactVo().getEmail(), "Invoice", body,
                                    Long.parseLong(session.getAttribute("companyId").toString()));
                        }
                    }
                }
            }

            // EDIT INVOICE VASY TO MERCHANT
            if (type.equals(Constant.SALES_INVOICE) && (isEdit != 0)) {
                VasyMessageSettingVo vasyMessageSettingVo = vasyMessageSettingService.findByTypeAndEvent(
                        MessageConstant.TYPE_VASY_TO_MERCHANT, MessageConstant.SYSTEM_EVENT_EDIT_INVOICE);
                if (vasyMessageSettingVo != null && vasyMessageSettingVo.getIsActive() == 1) {
                    globalMessageService.sendVasyToMerchantEditInvoiceMessage(salesVo2,
                            Long.parseLong(session.getAttribute("companyId").toString()),
                            Long.parseLong(session.getAttribute("branchId").toString()),
                            Long.parseLong(session.getAttribute("userId").toString()),
                            servletRequest.getServletContext().getRealPath("/"),
                            session.getAttribute("realPath").toString(), session.getAttribute("currencyCode").toString(),
                            session.getAttribute("currencyName").toString(),
                            session.getAttribute("decimalPoint").toString());
                }
            }


            // send mail code for invoice//
            if (salesVo2.getContactVo() != null) {
                if (salesVo2.getContactVo().getEmail() != null) {
                    CompanySettingVo allowInvoiceEmail = companySettingService.findByCompanyIdAndType(companyId,
                            Constant.ALLOWINVOICEEMAIL);
                    if (allowInvoiceEmail != null && allowInvoiceEmail.getValue() == 1) {
                        String body = Converter.convertToHtml(servletRequest,
                                "/media/download/sales/" + salesVo2.getPdfToken() + "/mail");
                        sendGridEmailService.sendHTML(from, salesVo2.getContactVo().getEmail(), "Invoice",
                                body,
                                Long.parseLong(session.getAttribute("companyId").toString()));
                    }
                }
            }
            // send mail code for invoice//

            if (isEdit == 0 && salesVo.getContactVo() != null && salesVo.getSalesId() != 0L && companySettingService.findByCompanyIdAndType(Long.valueOf(session.getAttribute("companyId").toString()), Constant.ALLOWFEEDBACK).getValue() == 0 && type.equals(Constant.SALES_INVOICE)) {
                FeedBackVo feedBackVo = new FeedBackVo();
                feedBackVo.setCustomerId(salesVo.getContactVo().getContactId());
                feedBackVo.setSalesId(salesVo.getSalesId());
                feedBackVo.setFeedbackStatus("pending");
                feedBackVo.setSendAt(CurrentDateTime.getCurrentDate());
                feedBackRepository.save(feedBackVo);
            }


            // send mail code complete

            if (allRequestParams.get("saveandnew") != null
                    && Integer.parseInt(allRequestParams.get("saveandnew")) == 1) {
                view.setViewName("redirect:/sales/" + type + "/new");
            } else if (allRequestParams.get("saveandprint") != null
                    && Integer.parseInt(allRequestParams.get("saveandprint")) == 1) {
                view.setViewName("redirect:/sales/" + type + "/" + salesVo2.getSalesId() + "?print=true");
            } else {
                view.setViewName("redirect:/sales/" + type + "/" + salesVo2.getSalesId());
            }
        }
        return view;

    }

    @RequestMapping("/{id}/checkReceiptStatus")
    @ResponseBody
    public Map<String, Object> checkReceiptStatusCount(@PathVariable("id") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CHECK_RECEIPT_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        // Fetch the sales VO to check e-way bill and IRN
        EwayDTO salesEway = salesRepository.findEwayBillNoById(id);
        IrnDTO salesIrn = salesRepository.findIrnById(id);

        HashMap<String, Object> result = new HashMap<>();
        int b = salesService.countForReceiptStatus(id);

        // Check e-way bill and IRN
        // Check for E-way bill and E-invoice status
        if (salesEway.getEwayBillNo() != 0) {
            result.put("status", 3);
            result.put("msg", "Please cancel E-way bill to edit/delete this document");
            return result;
        }

        // Check for E-way bill and E-invoice status
        if (StringUtils.isNotBlank(salesIrn.getIrnNo())) {
            result.put("status", 4);
            result.put("msg", "This Document cannot be edited/deleted once the E-invoice has been issued.");
            return result;
        }


        if (b == 0) {
            int stockTransferApproved = salesService.checkStockTransferApproved(id);
            if(stockTransferApproved!=0){
                result.put("status",1);
                result.put("msg","Stock Transfer for this invoice is not approved.");
            }else{
                result.put("status",0);
                result.put("msg","true");
            }
        } else {
            result.put("status",1);
            result.put("msg","The receipt is generated against this bill, please delete that to delete this bill.");
        }
        return result;
    }

    @RequestMapping("/{id}/getCreditNoteStatus")
    @ResponseBody
    public String getCreditNoteStatus(@PathVariable("id") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CREDITNOTE_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int b = salesService.getCreditNoteStatus(id);
        if (b == 0) {
            return "0";
        } else {
            return "1";
        }
    }

    @RequestMapping("/{id}/getCreditNoteAvailabiltyStatus")
    @ResponseBody
    public String getCreditNoteAvailabiltyStatus(@PathVariable("id") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CREDITNOTE_AVAILABLITY_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        // Fetch the sales VO to check e-way bill and IRN
        IrnDTO salesIrn = salesRepository.findIrnById(id);

        if (StringUtils.isNotBlank(salesIrn.getIrnNo())) {
            return "2";
        }

        int b = salesService.getCreditNoteAvailabiltyStatus(id);
        if (b == 0) {
            return "0";
        } else {
            return "1";
        }
    }

    @PostMapping("/{id}/data")
    @ResponseBody
    public List<Map<String, String>> salesListJSONForcreditnote(HttpSession session, @PathVariable(value = "type") String type, @PathVariable(value = "id") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CREDITNOTE_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREDITNOTE_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CREDITNOTE_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CREDITNOTE_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_JSON_DATA;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CREDITNOTE_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<Map<String, String>> purchaseVos = new ArrayList<>();
        try {
            purchaseVos = salesService.getSalesByTypeAndContactAndBranchIdAndIsDeleted(type, Long.parseLong(session.getAttribute("branchId").toString()), contactId, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return purchaseVos;
    }

    @PostMapping(value = {"/creditnote/{id}/cashback"})
    @ResponseBody
    public String creditNoteCashbackInB2B(@RequestParam Map<String, String> allRequestParams, HttpSession session, @PathVariable(value = "id") long salesId,
                                          HttpServletRequest request) throws Exception {
        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_CREDITNOTE_CASHBACK, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        String strDate = sdf.format(cal.getTime());
        String referer = request.getRequestURI();
        SimpleDateFormat sdf1 = new SimpleDateFormat();
        sdf1.applyPattern("dd/MM/yyyy HH:mm:ss.SS");

        SalesVo salesVo = salesService.findBySalesIdAndBranchId(salesId,
                Long.parseLong(session.getAttribute("branchId").toString()));
        try {
//					CreditnoteCashReturnVo cashReturnVo = new CreditnoteCashReturnVo();
//					double cashReturnAmount  = (salesVo.getTotal() - salesVo.getPaidAmount());
//					cashReturnVo.setAmount((salesVo.getTotal() - salesVo.getPaidAmount()));
//					cashReturnVo.setSalesVo(salesVo);
//					creditNoteCashReturnRepository.save(cashReturnVo);

            salesVo.setPaidAmount((salesVo.getTotal() - salesVo.getPaidAmount()));
            paymentService.createPaymentForOrderRefund(allRequestParams, salesVo, request, session, Constant.CREDITNOTE, 0);

            String returnAmtStr = allRequestParams.getOrDefault("returnCashAmount", "");
            if (StringUtils.isNotBlank(returnAmtStr) && Double.parseDouble(returnAmtStr) != 0) {
                salesService.saveOrderHistory(Constant.HISTORY_TYPE_CASH_RETURN, salesVo.getSalesId(), salesVo.getSalesVo().getType(), (salesVo.getPrefix() + salesVo.getSalesNo()),
                        Double.parseDouble(allRequestParams.get("returnCashAmount")), "", salesVo.getStatus(), salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()), Double.parseDouble(allRequestParams.get("returnCashAmount")), Long.parseLong(session.getAttribute("userId").toString()));
            }

            returnAmtStr = allRequestParams.getOrDefault("returnBankAmount", "");
            if (StringUtils.isNotBlank(returnAmtStr) && Double.parseDouble(returnAmtStr) != 0) {
                salesService.saveOrderHistory(Constant.HISTORY_TYPE_BANK_RETURN, salesVo.getSalesId(), salesVo.getSalesVo().getType(), (salesVo.getPrefix() + salesVo.getSalesNo()),
                        Double.parseDouble(allRequestParams.get("returnBankAmount")), "", salesVo.getStatus(), salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()), Double.parseDouble(allRequestParams.get("returnBankAmount")), Long.parseLong(session.getAttribute("userId").toString()));
            }
            salesVo.setPaidAmount((salesVo.getTotal()));
            salesService.save(salesVo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:" + request.getHeader("Referer");
    }


    @PostMapping("{accountCustomId}/pendingnew/json")
    @ResponseBody
    public List<Map<String, String>> salesPendingListJsonnew(@PathVariable String type, @PathVariable long accountCustomId,
                                                             HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_PENDING_JSON;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_PENDING_JSON;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_PENDING_JSON;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_PENDING_JSON;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_PENDING_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_PENDING_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<Long> contactIds = contactService.findContactIdByaccountCustomId(accountCustomId);

        List<Map<String, String>> purchasevos = new ArrayList<Map<String, String>>();

        purchasevos = salesService.findByTypeAndBranchIdAndIsDeletedAndSalesDateBetweenAndContactVogetmap(type,
                Long.parseLong(session.getAttribute("branchId").toString()), 0, contactIds.size() > 0 ? contactIds.get(0) : 0);


        return purchasevos;
    }

    @RequestMapping("/checktcsamount")
    @ResponseBody
    public JSONObject checkTotalTcsAmount(@PathVariable String type, @RequestParam Map<String, String> allRequestParams, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.SALES_ESTIMATE:
                rateLimitType = RateLimitConstant.SALES_ESTIMATE_CHECK_TCS_AMOUNT;
                break;
            case Constant.SALES_ORDER:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECK_TCS_AMOUNT;
                break;
            case Constant.SALES_INVOICE:
                rateLimitType = RateLimitConstant.SALES_INVOICE_CHECK_TCS_AMOUNT;
                break;
            case Constant.SALES_DELIVERY_CHALLAN:
                rateLimitType = RateLimitConstant.SALES_DC_CHECK_TCS_AMOUNT;
                break;
            case Constant.SALES_CREDIT_NOTE:
                rateLimitType = RateLimitConstant.SALES_CREDITNOTE_CHECK_TCS_AMOUNT;
                break;
            default:
                rateLimitType = RateLimitConstant.SALES_ORDER_CHECK_TCS_AMOUNT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long salesId = StringUtils.isNotBlank(allRequestParams.get("salesId")) ? Long.parseLong(allRequestParams.get("salesId")) : 0;
        long parentSalesId = StringUtils.isNotBlank(allRequestParams.get("parentSalesId")) ? Long.parseLong(allRequestParams.get("parentSalesId")) : 0;
        int isEdit = StringUtils.isNotBlank(allRequestParams.get("isEdit")) ? Integer.parseInt(allRequestParams.get("isEdit")) : 0;
        JSONObject jsonObject = new JSONObject();
        double tcs_amount = 0.0;
        if (parentSalesId != 0 && StringUtils.isNotBlank(type)) {
            try {
                if (type.equals(Constant.SALES_CREDIT_NOTE) || type.equals(Constant.SALES_DELIVERY_CHALLAN)) {
                    tcs_amount = salesService.getTotalTcsAmountUsedInCreditNoteByIdAndBranchIdAndType(parentSalesId, salesId,
                            Long.parseLong(session.getAttribute("branchId").toString()), type, isEdit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jsonObject.put("tcsAmount", tcs_amount);
        return jsonObject;
    }

    @PostMapping("/updateSalesForEwayForOrderdetail")
    @ResponseBody
    public String updateSalesForEwaybillForOrderdetail(@RequestParam(value = "salesId") long salesId,
                                                       @RequestParam(value = "shippingDate") String shippingDate,
                                                       @RequestParam(value = "transportDate") String transportDate,
                                                       @RequestParam(value = "transportname", required = false) long transportname,
                                                       @RequestParam(value = "vehicleno", required = false) String vehicleno,
                                                       @RequestParam(value = "refno", required = false) String refno,
                                                       @RequestParam(value = "modeoftransport", required = false) String modeoftransport,
                                                       @RequestParam(value = "weight", required = false) String weight,
                                                       @RequestParam(value = "shippingType", required = false) int shippingType,
                                                       @RequestParam(value = "lrNo", required = false) String lrNo,
                                                       @RequestParam(value = "noOfBoxes", required = false) String noOfBoxes
            , HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.POS_E_WAY_ORDER_UPDATE, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // log.info("API called");
        try {
            log.info("DATA" + salesId + "" + transportname + "" + vehicleno + "" + refno + "" + modeoftransport + "" + weight + "" + shippingType);
            log.info("YASHVI shippingDate " + shippingDate);
            int isUpdated = 0;
            long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
            String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
            // Check if valid using the existing method
            boolean isValidMerchantType = (MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) || merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER);
            if(!isValidMerchantType) {
                isUpdated = salesService.updateSalesForEwaybillForOrderdetail(salesId, dateFormat.parse(shippingDate), dateFormat.parse(transportDate), transportname, vehicleno, refno, modeoftransport, weight, shippingType);
            }else {
                isUpdated = salesService.updateSalesForEwaybillForOrderdetailForRIL(salesId, dateFormat.parse(shippingDate), dateFormat.parse(transportDate), transportname, vehicleno, refno, modeoftransport, weight, shippingType, lrNo, noOfBoxes);
            }
            log.info(isUpdated + "updated ewaybill");
            if (isUpdated > 0) {
                return "success";
            } else {
                return "failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";


    }

    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, Boolean> getInvoiceStatus(@PathVariable("id") long id, HttpSession session) {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_INVOICE_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        SalesVo salesVo = salesService.findBySalesIdAndBranchId(id, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

        Map<String, Boolean> status = new HashMap<>();

        if (salesVo != null) {
            status.put("isEInvoiceGenerated", StringUtils.isNotBlank(salesVo.getIrnNo()));
            status.put("isEWayBillGenerated", salesVo.getEwayBillNo() != 0);
        } else {
            status.put("isEInvoiceGenerated", false);
            status.put("isEWayBillGenerated", false);
        }

        return status;
    }

    @PostMapping("/{id}/checkactions")
    @ResponseBody
    public Map<String, String> checkEwayBillOrEInvoice(@PathVariable("id") long salesId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        SalesVo salesVo = salesService.findBySalesIdAndBranchId(salesId, Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
        String orderNo = StringUtils.defaultIfEmpty(salesVo.getOrderNo(), salesVo.getPrefix() + salesVo.getSalesNo()).trim();

        if (salesVo != null) {
            if (salesVo.getEwayBillNo() != 0) {
                response.put("message", "Please cancel E-way bill first to cancel " + orderNo);
            } else if (StringUtils.isNotBlank(salesVo.getIrnNo())) {
                response.put("message", "Please cancel E-invoice first to cancel " + orderNo);
            } else {
                SalesViewDTO childSales = salesService.getBySalesIdAndBranchId(salesId, Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));

                if (childSales != null && StringUtils.isNotBlank(childSales.getChildEinvoiceNo())) {
                    response.put("message", "Reference document" + orderNo + " already generated. You cannot cancel invoice ");

                } else {
                    response.put(Constant.STATUS, Constant.SUCCESS_LOWER);
                }
            }
        } else {
            response.put("message", "Sales record not found.");
        }

        return response;
    }

    @PostMapping("/{id}/status/{status}")
    @ResponseBody
    public String changeSalesInvoiceStatusForReturnFlow(HttpSession session, @PathVariable(value = "id") long id,
                                                          @PathVariable(value = "status") String status, HttpServletRequest servletRequest) {

        if (!rateLimitService.allowRequest(RateLimitConstant.SALES_INVOICE_STATUS, session))
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);

        SalesVo salesVo = salesService.findBySalesId(id);

        if (salesVo != null && StringUtils.equals(salesVo.getStatus(), Constant.HOLD)) {
            salesVo.setStatus(Constant.INVOICED);
            salesService.save(salesVo);

            if (salesVo.getType().equals(Constant.SALES_INVOICE)) {
                salesService.saveSalesHistory(Constant.HISTORY_TYPE_STATUSCHANGE, salesVo.getSalesId(), salesVo.getType(), (salesVo.getPrefix() + salesVo.getSalesNo()),
                        salesVo.getTotal(), Constant.HOLD, salesVo.getStatus(), 0, "", "", 0, session);

                salesService.insertSalesTransaction(salesVo, session.getAttribute(Constant.FINANCIAL_YEAR).toString(), session);
            }
        }
        return "success";
    }

    @PostMapping("/history")
    @ResponseBody
    public List<Map<String, String>> getSalesHistoryByProductVarientIdAndType(@RequestParam(required = false, defaultValue = "0") long productVarientId,
                                                                              @RequestParam(required = false, defaultValue = "0") long contactId) {

        try {
            List<Map<String,String>> salesVos = salesService.getSalesHistoryByTypeAndProductVarientId(productVarientId,contactId);
            return salesVos;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @GetMapping("/checkSalesType/{id}")
    public ModelAndView checkSalesType(HttpSession session, @PathVariable(value = "id") long id) throws Exception {
        String type = salesRepository.findTypeByTypeId(id);
        if(type.equals(Constant.SALES_INVOICE) || type.equals(Constant.SALES_CREDIT_NOTE)) {
            return new ModelAndView("redirect:/sales/" + type + "/" + id);
        } else if (type.equals(Constant.SALES_POS)) {
            return new ModelAndView("redirect:/pos/" + id);
        } else {
            return new ModelAndView("redirect:/pos/creditnote/" + id);
        }
    }
}
