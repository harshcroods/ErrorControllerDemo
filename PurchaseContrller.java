package com.croods.vyaparerp.controller.purchase;

import com.croods.vyaparerp.config.ImageResize;
import com.croods.vyaparerp.config.MenuPermission;
import com.croods.vyaparerp.config.SecurityValidation;
import com.croods.vyaparerp.constant.*;
import com.croods.vyaparerp.controller.merchanttype.MerchantTypeController;
import com.croods.vyaparerp.controller.whatsapp.WhatsappController;
import com.croods.vyaparerp.dto.account.AccountCustomDTO;
import com.croods.vyaparerp.dto.einvoice.IrnDTO;
import com.croods.vyaparerp.dto.ewaybill.EwayDTO;
import com.croods.vyaparerp.dto.file.FileValidationResponse;
import com.croods.vyaparerp.dto.purchase.*;
import com.croods.vyaparerp.dto.sales.DeliveryTaxDTO;
import com.croods.vyaparerp.dto.sales.TaxDTO;
import com.croods.vyaparerp.dto.tax.TaxFormDTO;
import com.croods.vyaparerp.dto.user.BranchDTO;
import com.croods.vyaparerp.dto.vyapar.product.StockMasterDTOForDebitNote;
import com.croods.vyaparerp.dto.vyapar.purchase.PurchaseItemPdfDTO;
import com.croods.vyaparerp.dto.vyapar.purchase.PurchaseReturnItemDTO;
import com.croods.vyaparerp.exception.CustomRateLimitExceedException;
import com.croods.vyaparerp.global.CurrentDateTime;
import com.croods.vyaparerp.global.GetFileExtension;
import com.croods.vyaparerp.global.NumberToWord;
import com.croods.vyaparerp.repository.barcodemaster.BarcodeMasterRepository;
import com.croods.vyaparerp.repository.barcodemaster.BarcodeMasterSettingRepository;
import com.croods.vyaparerp.repository.contact.ContactRepository;
import com.croods.vyaparerp.repository.product.ProductTypeRepository;
import com.croods.vyaparerp.repository.purchase.PurchaseAdditionalChargeRepository;
import com.croods.vyaparerp.repository.purchase.PurchaseRepository;
import com.croods.vyaparerp.repository.rilproduct.RILPurchaseBillInfoRepository;
import com.croods.vyaparerp.repository.sales.SalesRepository;
import com.croods.vyaparerp.repository.stock.StockMasterRepository;
import com.croods.vyaparerp.repository.stocktransfer.StockTransferRepository;
import com.croods.vyaparerp.repository.systemactivitylog.SystemActivityLogRepository;
import com.croods.vyaparerp.repository.user.UserRepository;
import com.croods.vyaparerp.service.account.AccountCustomService;
import com.croods.vyaparerp.service.additionalcharge.AdditionalChargeService;
import com.croods.vyaparerp.service.aws.AwsService;
import com.croods.vyaparerp.service.azure.AzureBlobService;
import com.croods.vyaparerp.service.brand.BrandService;
import com.croods.vyaparerp.service.category.CategoryService;
import com.croods.vyaparerp.service.city.CityService;
import com.croods.vyaparerp.service.contact.ContactService;
import com.croods.vyaparerp.service.country.CountryService;
import com.croods.vyaparerp.service.department.DepartmentService;
import com.croods.vyaparerp.service.email.EmailService;
import com.croods.vyaparerp.service.employee.EmployeeService;
import com.croods.vyaparerp.service.expense.ExpenseService;
import com.croods.vyaparerp.service.financial.FinancialService;
import com.croods.vyaparerp.service.messageService.MessageService;
import com.croods.vyaparerp.service.messagesetting.GlobalMessageService;
import com.croods.vyaparerp.service.messagesetting.VasyMessageSettingService;
import com.croods.vyaparerp.service.notification.NotificationService;
import com.croods.vyaparerp.service.payment.PaymentService;
import com.croods.vyaparerp.service.paymentterm.PaymentTermService;
import com.croods.vyaparerp.service.prefix.PrefixService;
import com.croods.vyaparerp.service.product.HsnTaxMasterService;
import com.croods.vyaparerp.service.product.ProductService;
import com.croods.vyaparerp.service.profile.ProfileService;
import com.croods.vyaparerp.service.purchase.PurchaseService;
import com.croods.vyaparerp.service.ratelimit.RateLimitService;
import com.croods.vyaparerp.service.report.ReportService;
import com.croods.vyaparerp.service.sales.SalesService;
import com.croods.vyaparerp.service.setting.CompanySettingService;
import com.croods.vyaparerp.service.setting.DateFormatMasterService;
import com.croods.vyaparerp.service.shopify.ShopifyServiceNew;
import com.croods.vyaparerp.service.shorturl.ShortenUrlService;
import com.croods.vyaparerp.service.state.StateService;
import com.croods.vyaparerp.service.stock.StockMasterService;
import com.croods.vyaparerp.service.stock.StockTransactionService;
import com.croods.vyaparerp.service.stocktransfer.StockTransferService;
import com.croods.vyaparerp.service.tax.TaxService;
import com.croods.vyaparerp.service.termsandcondition.PurchaseTermsAndConditionService;
import com.croods.vyaparerp.service.termsandcondition.TermsAndConditionService;
import com.croods.vyaparerp.service.transaction.TransactionService;
import com.croods.vyaparerp.service.typesense.TypesenseService;
import com.croods.vyaparerp.service.unitofmeasurement.UnitOfMeasurementService;
import com.croods.vyaparerp.service.userfront.UserService;
import com.croods.vyaparerp.service.whatsapp.WhatsappService;
import com.croods.vyaparerp.service.woocommerce.WooService;
import com.croods.vyaparerp.util.Number;
import com.croods.vyaparerp.util.*;
import com.croods.vyaparerp.vo.account.AccountCustomVo;
import com.croods.vyaparerp.vo.bankcash.BankVo;
import com.croods.vyaparerp.vo.barcodemaster.BarcodeMasterDTO;
import com.croods.vyaparerp.vo.barcodemaster.BarcodeMasterVo;
import com.croods.vyaparerp.vo.contact.ContactAddressVo;
import com.croods.vyaparerp.vo.contact.ContactProductVo;
import com.croods.vyaparerp.vo.contact.ContactVo;
import com.croods.vyaparerp.vo.datatable.DataTableMetaDTO;
import com.croods.vyaparerp.vo.datatable.purchase.DataTablePurchaseDTO;
import com.croods.vyaparerp.vo.datatable.purchase.DataTablePurchaseResponceDTO;
import com.croods.vyaparerp.vo.messagesetting.VasyMessageSettingVo;
import com.croods.vyaparerp.vo.payment.PaymentBillVo;
import com.croods.vyaparerp.vo.payment.PaymentVo;
import com.croods.vyaparerp.vo.product.ProductVarientsVo;
import com.croods.vyaparerp.vo.product.ProductVo;
import com.croods.vyaparerp.vo.purchase.PurchaseAdditionalCharge;
import com.croods.vyaparerp.vo.purchase.PurchaseItemVo;
import com.croods.vyaparerp.vo.purchase.PurchaseVo;
import com.croods.vyaparerp.vo.reportSetting.ReportSettingVo;
import com.croods.vyaparerp.vo.rilpurchasebillinfo.RILPurchaseBillInfo;
import com.croods.vyaparerp.vo.sales.SalesAdditionalChargeVo;
import com.croods.vyaparerp.vo.sales.SalesItemVo;
import com.croods.vyaparerp.vo.sales.SalesVo;
import com.croods.vyaparerp.vo.setting.CompanySettingVo;
import com.croods.vyaparerp.vo.stock.StockMasterVo;
import com.croods.vyaparerp.vo.stocktransfer.StockTransferItemVo;
import com.croods.vyaparerp.vo.stocktransfer.StockTransferVo;
import com.croods.vyaparerp.vo.systemactivitylog.SystemActivityLogVo;
import com.croods.vyaparerp.vo.tax.TaxVo;
import com.croods.vyaparerp.vo.userfront.UserFrontVo;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
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

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Log
@Controller
@RequestMapping("/purchase/{type}")
public class PurchaseController {

    @Autowired
    AwsService awsService;

    @Autowired
    UserService userService;

    @Autowired
    private RILPurchaseBillInfoRepository rILPurchaseBillInfoRepository;

    @Autowired
	VasyMessageSettingService vasyMessageSettingService;

	@Autowired
	GlobalMessageService globalMessageService;

    @Autowired
    DateFormatMasterService dateFormatMasterService;

    @Autowired
    JasperExporter jasperExporter;

    @Autowired
    AccountCustomService accountCustomService;

    @Lazy
    @Autowired
    BarcodeMasterSettingRepository barcodeMasterSettingRepository;

    @Autowired
    SecurityValidation securityValidation;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PaymentService paymentService;

    @Lazy
    @Autowired
    ReportService reportService;

    @Autowired
    ContactService contactService;

    @Autowired
    WhatsappController whatsappController;

    @Autowired
    WhatsappService whatsappService;

    @Autowired
    MessageService messageService;

    @Autowired
    EmailService sendGridEmailService;

    @Autowired
    ProductService productService;
    @Autowired
    PrefixService prefixService;

    @Autowired
    SalesService salesService;

    @Autowired
    TermsAndConditionService termsAndConditionService;

    @Autowired
    StockMasterRepository stockMasterRepository;

    @Autowired
    TransactionService transactionService;

    @Autowired
    StockTransactionService stockTransactionService;

    @Autowired
    StockTransferRepository stockTransferRepository;

    @Autowired
    BarcodeMasterRepository barcodeMasterRepository;

    @Autowired
    StockTransferService stockTransferService;

    @Autowired
    PurchaseService purchaseService;

    @Autowired
    PaymentTermService paymentTermService;

    @Autowired
    CountryService countryService;

    @Autowired
    RateLimitService rateLimitService;

    @Autowired
    Number numberUtil;

    @Autowired
    StateService stateService;

    @Autowired
    CityService cityService;

    @Autowired
    TaxService taxService;

    @Autowired
    UnitOfMeasurementService unitOfMeasurementService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    AdditionalChargeService additionalChargeService;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    ProfileService profileService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CompanySettingService companySettingService;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    StockMasterService stockMasterService;

    @Autowired
    PurchaseTermsAndConditionService purchaseTermsAndConditionService;

    @Autowired
    BrandService brandService;

    @Autowired
    ShortenUrlService urlService;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WooService wooService;

    @Autowired
    AzureBlobService azureBlobService;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ExpenseService expenseService;

    @Autowired
    PurchaseAdditionalChargeRepository purchaseAdditionalChargeRepository;

    @Autowired
    YearEndingPrefixAlertMsg yearEndingPrefixAlertMsg;

    @Autowired
    NotificationService notificationService;

    @Autowired
    TypesenseService typesenseService;

    @Autowired
    private ShopifyServiceNew shopifyServiceNew;

    @Autowired
    private HsnTaxMasterService hsnTaxMasterService;

    @Autowired
    FinancialService financialService;

    @Autowired
    SalesRepository salesRepository;

    @Value("${autoGrowCollectionLimit}")
    private int autoGrowCollectionLimit;

    @Value("${from.to}")
    private String from;

    @Value("${END_POINT_URL}")
    private String END_POINT_URL;

    @Value("${BUCKET}")
    private String BUCKET;

    @Value("${PURCHASE_ATTACHMENT_LOCATION}")
    private String PURCHASE_ATTACHMENT_LOCATION;

    @Value("${base.url}")
    private String BASEURL;

    @Value("${FILE_UPLOAD_SERVER}")
    private String FILE_UPLOAD_SERVER;

    @Value("${JASPER_REPORT_PATH}")
	private String JASPER_REPORT_PATH;

    @Autowired
    SystemActivityLogRepository systemActivityLogRepository;

    private static DecimalFormat df2 = new DecimalFormat("#.###");

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

    @RequestMapping("")
    public ModelAndView purchaseList(HttpSession session, @PathVariable(value = "type") String type) {

        String rateLimitType;
        switch(type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_LIST;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_LIST;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_LIST;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_LIST;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_LIST;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

       ModelAndView view = new ModelAndView("purchase/purchase");
        long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
        String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
        if (MenuPermission.havePermission(session, type, Constant.VIEW) == 1) {
            // Check if valid using the existing method
            boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
            view.addObject("isValidMerchantType", isValidMerchantType);
            view.addObject("type", type);
// Check if the user has permission for E-Waybill for the current module (e.g., Invoice)

            if(type.equals(Constant.PURCHASE_BILL)) {
                view.addObject("displayType", "Supplier Bill");
            } else if (type.equals(Constant.PURCHASE_ORDER)) {
                view.addObject("displayType", "Purchase Order");
            } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                view.addObject("displayType", "Debit Note");
            } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                view.addObject("displayType", "Material Inward");
            }

//			view.addObject("ContactList", contactService.contactList(
//					Long.parseLong(session.getAttribute("branchId").toString()), Constant.CONTACT_SUPPLIER));
            if (session.getAttribute("userType").toString().equals("2") || session.getAttribute("parentUserType").toString().equals("2")) {
                view.addObject("branchList", profileService.getCustomListOfBranch(Long.parseLong(session.getAttribute("companyId").toString())));
              }
            view.addObject(Constant.POAPPROVAL, companySettingService
   					.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.POAPPROVAL));
            view.addObject("isInsert", MenuPermission.havePermission(session, type, Constant.INSERT));
            view.addObject("isEdit", MenuPermission.havePermission(session, type, Constant.EDIT));
            view.addObject("isDelete", MenuPermission.havePermission(session, type, Constant.DELETE));
            view.addObject("isExport", MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT));
//            view.addObject(Constant.STOCKBYMI, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYMI).getValue());
//            view.addObject(Constant.STOCKBYBILL, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYBILL).getValue());
            view.addObject(Constant.ADDQTYBY, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.ADDQTYBY).getValue());
            view.addObject("isSupplierBillNew",
            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_BILL, Constant.INSERT));
//            view.addObject("isCanceled",
//            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.CANCELED));
            view.addObject("isCanceled",1);

            try{
                if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE)!=null?session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE):"0").toString()) == 1
                        && (merchantTypeId == 0 || merchantTypeId == 1)) {

                    int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());

                    int userType = Integer.parseInt(session.getAttribute("userType").toString());

                    if (userType > Constant.URID_USER){
                        userType = Integer.parseInt(session.getAttribute("parentUserType").toString());
                    }

                    String typesenseCollectionName = typesenseService.getCollectionName(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), accountingType, userType, Constant.CONTACT_SUPPLIER);
                    log.warning("typesenseCollectionName : "+typesenseCollectionName);
                    view.addObject("typesenseCollectionName", typesenseCollectionName);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            view.setViewName(Constant.ACCESSDENIED);
        }
        return view;
    }

//    @RequestMapping("/datatable")
//    @ResponseBody
//    public DataTablesOutput<PurchaseVo> purchaseDatatable(@PathVariable String type, @Valid DataTablesInput input,
//                                                          @RequestParam Map<String, String> allRequestParams, HttpSession session)
//            throws NumberFormatException, ParseException {
//
//        //long branchId = Long.parseLong(session.getAttribute("branchId").toString());
//
//
//
//
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//
//        Specification<PurchaseVo> specification = new Specification<PurchaseVo>() {
//
//
//            @Override
//            public Predicate toPredicate(Root<PurchaseVo> root, CriteriaQuery<?> query,
//                                         CriteriaBuilder criteriaBuilder) {
//
//                List<Long> branchList =new ArrayList<Long>();
//
//                if(StringUtils.isNotBlank(allRequestParams.get("branch"))) {
//                    branchList = Arrays.asList(allRequestParams.get("branch").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
//                }else {
//                    branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
//                }
//
//                List<Predicate> predicates = new ArrayList<Predicate>();
//                predicates.add(criteriaBuilder.equal(root.get("type"), type));
//                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
//                predicates.add(root.get("branchId").in(branchList));
//                query.orderBy(criteriaBuilder.desc(root.get("purchaseDate")));
//                query.orderBy(criteriaBuilder.desc(root.get("purchaseId")));
//
////                              if (Integer.parseInt(session.getAttribute("userType").toString()) > 3) {
////                                      predicates.add(criteriaBuilder.equal(root.get("createdBy"), Long.parseLong(session.getAttribute("userId").toString())));
////                              }
//                if (allRequestParams.get("paidType")!=null && allRequestParams.get("paidType")!="") {
//                    if(allRequestParams.get("paidType").equals("unpaid")){
//
//                        if (type.equalsIgnoreCase(Constant.PURCHASE_DEBIT_NOTE)) {
//                            predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("status"), "open"), criteriaBuilder.equal(root.get("status"), "due")));
//                        } else {
//                            predicates.add(criteriaBuilder.notEqual(root.get("total"), root.get("paidAmount")));
//                        }
//
//                    }
//                }
//                if (allRequestParams.get("status")!=null && allRequestParams.get("status")!="") {
//                    if(allRequestParams.get("status").equals("paid")){
//                        predicates.add(criteriaBuilder.equal(root.get("total"), root.get("paidAmount")));
//                    }else if(allRequestParams.get("status").equals("overdue")){
//                        try {
//                            predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), dateFormat.parse(CurrentDateTime.getTodayDate())));
//                            predicates.add(criteriaBuilder.notEqual(root.get("total"), root.get("paidAmount")));
//                        } catch (ParseException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }else if(allRequestParams.get("status").equals("due")){
//                        predicates.add(criteriaBuilder.notEqual(root.get("total"), root.get("paidAmount")));
//                    }else if(allRequestParams.get("status").equals("close")){
//                        List<Long> list = new ArrayList<Long>();
//                        list=purchaseRepository.findByListParentIdIsavailableAndtype(branchList);
//                        predicates.add(root.get("purchaseId").in(list));
//                    }else if(allRequestParams.get("status").equals(Constant.DRAFT)){
//                        List<Long> list = new ArrayList<Long>();
//                        list=purchaseRepository.findByListParentIdIsavailableAndtype(branchList);
//                        predicates.add(root.get("purchaseId").in(list).not());
//                    }
//                }
//                if (!allRequestParams.get("contactId").equals("")) {
//                    ContactVo contactVo = new ContactVo();
//                    contactVo.setContactId(Long.parseLong(allRequestParams.get("contactId")));
//                    predicates.add(criteriaBuilder.equal(root.get("contactVo"), contactVo));
//                }
//                String daterange = allRequestParams.get("daterange");
//                Date startDate= null;
//                Date endDate= null;
//
//                Calendar calendar = Calendar.getInstance();
//
//                try {
//                    if (daterange.equals("currentYear")) {
//                        calendar.setTime(dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()));
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        calendar.setTime(dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    } else if (daterange.equals("lastMonth")) {
//                        calendar.set(Calendar.DAY_OF_MONTH, -1);
//                        calendar.add(Calendar.DATE, 1);
//                        int min =calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//                        calendar.set(Calendar.DAY_OF_MONTH, min);
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//                        calendar.set(Calendar.DAY_OF_MONTH, max);
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    }else if (daterange.equals("thisMonth")) {
//                        calendar.set(Calendar.DAY_OF_MONTH, 1);
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//                        calendar.set(Calendar.DAY_OF_MONTH, max);
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    } else if (daterange.equals("thisWeek")) {
//                        calendar.add(Calendar.DATE, -7);
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        calendar.add(Calendar.DATE, 7);
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    } else if (daterange.equals("lastWeek")) {
//                        calendar.add(Calendar.DATE, -14);
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        calendar.add(Calendar.DATE, 7);
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    }else if (daterange.equals("today")) {
//                        startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                        endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
//                    }else if (daterange.equals("customrange")) {
//                        if(!allRequestParams.get("from").equals("")) {
//                            String[] Daterange = allRequestParams.get("from").split("-");
//                            startDate= dateFormat.parse(Daterange[0]);
//                            endDate=dateFormat.parse(Daterange[1]);
//                        }
//                    }
//                    predicates.add(criteriaBuilder.between(root.get("purchaseDate"),
//                            startDate,
//                            endDate));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                if(StringUtils.isNotBlank(allRequestParams.get("dateofsupply"))) {
//                    String[] Daterange = allRequestParams.get("dateofsupply").split("-");
//                    try {
//                        predicates.add(criteriaBuilder.between(root.get("dateOfSupply"),
//                                dateFormat.parse(Daterange[0]),
//                                dateFormat.parse(Daterange[1])));
//                    } catch (ParseException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//                if(StringUtils.isNotBlank(allRequestParams.get("dueDate"))) {
//                    String[] Daterange = allRequestParams.get("dueDate").split("-");
//                    try {
//                        predicates.add(criteriaBuilder.between(root.get("dueDate"),
//                                dateFormat.parse(Daterange[0]),
//                                dateFormat.parse(Daterange[1])));
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
//            }
//        };
//
//        DataTablesOutput<PurchaseVo> a = purchaseService.findAll(input, null, specification);
//        a.getData().forEach(x -> {
//            x.setCreatedbyname(profileService.getName(x.getCreatedBy()));
//            x.setBranchName(profileService.getName(x.getBranchId()));
//        });
//
//        a.getData().forEach(y -> {
//            y.setPurchaseAdditionalChargeVos(null);
//        });
//
//        a.getData().forEach(z -> {
//            z.getPurchaseItemVos().forEach(f -> {
//                z.setTaxAmount(z.getTaxAmount() + f.getTaxAmount());
//            });
//        });
//        if (type.equals(Constant.PURCHASE_ORDER)) {
//            a.getData().forEach(z -> {
//
//                z.setIschildCreated(purchaseService.findByParentIdIsavailableAndtype(z.getPurchaseId(),Constant.PURCHASE_BILL));
//            });
//        }
//
//
////        a.getData().forEach(x -> {
////            if (x.getContactVo() != null) {
////                x.getContactVo().setContactAddressVos(null);
////
////            }
////            x.setPurchaseItemVos(null);
////            x.setPurchaseVo(null);
////
////        });
//        a.getData().forEach(x -> {
//            if (x.getContactVo() != null) {
//                x.getContactVo().setContactAddressVos(null);
//                x.getContactVo().setContactProductVos(null);
//            }
//            x.setPurchaseItemVos(null);
//            x.setSupplierbillgenerated(0);
//
//            if (x.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
//                if(x.getPurchaseVo()!=null) {
//                    x.getPurchaseVo().setPurchaseItemVos(null);
//                    List<Long> parent = purchaseService.checkBillgeneratedornot(x.getPurchaseVo().getPurchaseId(), Constant.PURCHASE_BILL, Long.parseLong(session.getAttribute("branchId").toString()));
//                    //System.err.println("HERE parent size is :"+parent.size());
//                    if(parent.size() == 0) {
//                        x.setSupplierbillgenerated(0);
//                    }else {
//                        x.setSupplierbillgenerated(1);
//                    }
//                    if (x.getPurchaseVo().getContactVo() != null) {
//                        x.getPurchaseVo().getContactVo().setContactAddressVos(null);
//                        x.getPurchaseVo().getContactVo().setContactProductVos(null);
//                    }
//                }
//            }else {
//                x.setPurchaseVo(null);
//            }
//            //x.setPurchaseVo(null);
//
//        });
//        return a;
//    }


    @RequestMapping("/datatable")
    @ResponseBody
    public JSONObject UnpaidData(HttpSession session, @PathVariable String type, @RequestParam Map<String, String> allRequestParams) throws ParseException {

        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DETAIL_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DETAIL_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DETAIL_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        List<Long> branchList = StringUtils.isNotBlank(allRequestParams.get(Constant.BRANCH))
                ? Arrays.stream(allRequestParams.get(Constant.BRANCH).split(",")).map(Long::parseLong).collect(Collectors.toList())
                : Collections.singletonList(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));

        String status = allRequestParams.get(Constant.STATUS);

        long itemId = StringUtils.isNotBlank(allRequestParams.get("itemname"))
                ? Long.parseLong(allRequestParams.get("itemname"))
                : 0L;


        String paidType = allRequestParams.get("paidType");
        String searchValue = StringUtils.isNotBlank(allRequestParams.get("search.value"))
                ? "%" + allRequestParams.get("search.value") + "%"
                : "";

        long supplierId = StringUtils.isNotBlank(allRequestParams.get(Constant.CONTACT_ID))
                ? Long.parseLong(allRequestParams.get(Constant.CONTACT_ID))
                : 0L;

        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        Date todayDate = dateFormat.parse(CurrentDateTime.getTodayDate());

        String daterange = allRequestParams.get("daterange");
        Date startDate= null;
        Date endDate= null;

        Calendar calendar = Calendar.getInstance();

        try {
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
                if (StringUtils.isNotBlank(allRequestParams.get("from"))) {
                    String[] customDateRange = allRequestParams.get("from").split("-");
                    startDate = dateFormat.parse(customDateRange[0]);
                    endDate = dateFormat.parse(customDateRange[1]);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date formDateOfSupplier = null;
        Date toDateOfSupplier = null;
        Boolean supplierFlag = null;
        if (StringUtils.isNotBlank(allRequestParams.get("dateofsupply"))) {
            String[] dateRangeForSupply = allRequestParams.get("dateofsupply").split("-");
            try {
                supplierFlag = true;
                formDateOfSupplier = dateFormat.parse(dateRangeForSupply[0]);
                toDateOfSupplier = dateFormat.parse(dateRangeForSupply[1]);
            } catch (ParseException e) {

						e.printStackTrace();
					}

                }else {
            supplierFlag = false;
            formDateOfSupplier = dateFormat.parse(CurrentDateTime.getTodayDate());
            toDateOfSupplier = dateFormat.parse(CurrentDateTime.getTodayDate());
        }

        Date fromDueDate = null;
        Date toDueDate = null;
        Boolean dueFlag = null;
        if (StringUtils.isNotBlank(allRequestParams.get("dueDate"))) {
            String[] Daterange = allRequestParams.get("dueDate").split("-");
            try {
                dueFlag = true;
                fromDueDate = dateFormat.parse(Daterange[0]);
                toDueDate = dateFormat.parse(Daterange[1]);
					} catch (ParseException e) {
						e.printStackTrace();

            }
        } else {
            dueFlag = false;
            fromDueDate = dateFormat.parse(CurrentDateTime.getTodayDate());
            toDueDate = dateFormat.parse(CurrentDateTime.getTodayDate());
        }

        int totalRecord = purchaseService.findTotalUnpaidData(branchList, type, supplierId, status, paidType, todayDate, startDate, endDate, supplierFlag, dueFlag, formDateOfSupplier, toDateOfSupplier, fromDueDate, toDueDate, itemId, searchValue);

                    int start = Integer.parseInt(allRequestParams.get(Constant.START));
        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");
        int length, page = 0, offset;

            if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
            page = start / length; // Calculate page number
            offset = page * length;
        			}else {
        				length = totalRecord;
            offset = 0;
        }

        List<Map<String, Object>> list = totalRecord > 0
            ? purchaseService.findUnpaidData(branchList, type, supplierId, status, paidType, todayDate, startDate, endDate, supplierFlag, dueFlag, formDateOfSupplier, toDateOfSupplier, fromDueDate, toDueDate, itemId, length, offset, searchValue)
                : Collections.emptyList();

        JSONObject jsonMainObject = new JSONObject();
        JSONObject jsonMataObject = new JSONObject();
        double totalRecords = totalRecord;
        jsonMainObject.put(Constant.DRAW, Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        jsonMainObject.put(Constant.RECORDS_FILTERED, totalRecords);
        jsonMainObject.put(Constant.RECORDS_TOTAL, totalRecords);
        jsonMainObject.put(Constant.DATA, list);

        jsonMataObject.put(Constant.PAGE, page);
        jsonMataObject.put(Constant.PAGES, (int) Math.ceil((totalRecords) / length));
        jsonMataObject.put(Constant.PERPAGE, length);
        jsonMataObject.put(Constant.TOTAL, totalRecords);

        jsonMainObject.put(Constant.META, jsonMataObject);

        return jsonMainObject;

    }

    @RequestMapping("/unpaidcustomdatatable")
    @ResponseBody
    public DataTablePurchaseResponceDTO purchaseUnpaidCustomDatatable(@PathVariable String type, @Valid DataTablesInput input,
                                                          @RequestParam Map<String, String> allRequestParams, HttpSession session)
            throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_UNPAID_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_UNPAID_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_UNPAID_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_UNPAID_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_UNPAID_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	long companyId = Long.parseLong(session.getAttribute("companyId").toString());
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        List<Long> productlist=new ArrayList<>();

        long contactId = (StringUtils.isNotBlank(allRequestParams.get("contactId"))) ? Long.parseLong(allRequestParams.get("contactId")) : 0L;
        Date fromDate = StringUtils.isNotBlank(allRequestParams.get(Constant.FROM_DATE)) ?
                dateFormat.parse(allRequestParams.get(Constant.FROM_DATE))
                :dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString());
        Date toDate = StringUtils.isNotBlank(allRequestParams.get(Constant.TO_DATE)) ?
                dateFormat.parse(allRequestParams.get(Constant.TO_DATE)) :
                dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString());
        String status = StringUtils.isNotBlank(allRequestParams.get(Constant.STATUS)) ?
                allRequestParams.get(Constant.STATUS) : "";
        String searchValue = StringUtils.isNotBlank(allRequestParams.get(Constant.SEARCHVALUE)) ?
                allRequestParams.get(Constant.SEARCHVALUE) : "";
        String paidType = StringUtils.isNotBlank(allRequestParams.get("paidType")) ?
                allRequestParams.get("paidType") : "";
        //System.err.println("search value"+serachValue);
        Integer totallength=purchaseService.countByPurchaseUnpaidDatatable(branchId,type,contactId,fromDate,toDate,status,paidType,searchValue);
        //System.err.println("count IS :"+totallength);
        int start = Integer.parseInt(allRequestParams.get("start"));
   	 	String pageLength=allRequestParams.get("length");
 	   	int length =0,page=0,offset=0;

 	   	 if(!pageLength.equals("-1")) {
 	   		 length = Integer.parseInt(allRequestParams.get("length"));
 	    	    page = start / length; //Calculate page number
 	    	    offset= page*length;

 	   	 }else {
 	   		 length=totallength;
 	   		 offset=0;
 	   	 }
        List<DataTablePurchaseDTO>	list=purchaseService.findByPurchaseUnpaidDatatable(branchId,type,contactId,fromDate,toDate,status,paidType,searchValue,length,offset);
        //System.err.println("LIST SIZE IS :"+list.size());
 	   double totalRecords=totallength;
		DataTablePurchaseResponceDTO dto=new DataTablePurchaseResponceDTO();
		dto.setData(list);
		dto.setDraw(Integer.parseInt(allRequestParams.get("draw")));
		dto.setError(null);
		dto.setRecordsFiltered((int) totalRecords);
		dto.setRecordsTotal((int) totalRecords);
		dto.setDataTableMetaDTO(new DataTableMetaDTO(page,(int)Math.ceil((totalRecords) / length), length, (int)totalRecords));

		return dto;

    }

    @RequestMapping("/customdatatable")
    @ResponseBody
    @Transactional(readOnly = true)
    public DataTablePurchaseResponceDTO purchaseCustomDatatable(@PathVariable String type, @Valid DataTablesInput input,
                                                          @RequestParam Map<String, String> allRequestParams, HttpSession session)
            throws NumberFormatException, ParseException {

        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DATATABLE;
        }

        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	String daterange = allRequestParams.get("daterange");

        List<Long> branchList =StringUtils.isNotBlank(allRequestParams.get(Constant.BRANCH))
                ? Arrays.stream(allRequestParams.get(Constant.BRANCH).split(",")).map(Long::parseLong).collect(Collectors.toList())
                : Collections.singletonList(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));

        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        Date startDate=null;
        Date endDate=null;

        if (daterange.equals("currentYear")) {
            calendar.setTime(dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString()));
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            calendar.setTime(dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString()));
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        } else if (daterange.equals("lastMonth")) {
	       	 calendar.set(Calendar.DAY_OF_MONTH, -1);
	         calendar.add(Calendar.DATE, 1);
	         int min =calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
	         calendar.set(Calendar.DAY_OF_MONTH, min);
	         startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
	         int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	         calendar.set(Calendar.DAY_OF_MONTH, max);
	         endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
	    }else if (daterange.equals("thisMonth")) {
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
        }else if (daterange.equals("today")) {
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        }else if (daterange.equals("customrange")) {
        	if(StringUtils.isNotBlank(allRequestParams.get("from"))) {
        	String[] Daterange = allRequestParams.get("from").split("-");
        	startDate= dateFormat.parse(Daterange[0]);
        	endDate=dateFormat.parse(Daterange[1]);
        	}
        }
        Date fromdateofsupply = startDate, todateofsupply = startDate, fromDueDate = startDate, toDueDate = startDate;

        long contactId = StringUtils.isNotBlank(allRequestParams.get(Constant.CONTACT_ID))
                ? Long.parseLong(allRequestParams.get(Constant.CONTACT_ID))
                : 0L;

        String status = StringUtils.isNotBlank(allRequestParams.get(Constant.STATUS))
                ? allRequestParams.get(Constant.STATUS)
                : "";

        String serachValue = StringUtils.isNotBlank(allRequestParams.get("search.value"))
                ? "%" + allRequestParams.get("search.value") + "%"
                : "";

        String paidType = StringUtils.isNotBlank(allRequestParams.get("paidType"))
                ? allRequestParams.get("paidType")
                : "";

        int isdateofsupply = 0, isDueDate = 0;
        if(StringUtils.isNotBlank(allRequestParams.get("dateofsupply"))) {
        	String[] Daterange = allRequestParams.get("dateofsupply").split("-");
        	fromdateofsupply=dateFormat.parse(Daterange[0]);
        	todateofsupply=dateFormat.parse(Daterange[1]);
        	isdateofsupply=1;
        }
        if(StringUtils.isNotBlank(allRequestParams.get("dueDate"))) {
        	String[] Daterange = allRequestParams.get("dueDate").split("-");
        	fromDueDate=dateFormat.parse(Daterange[0]);
        	toDueDate=dateFormat.parse(Daterange[1]);
        	isDueDate=1;
        }

        long productVarientId = StringUtils.isNotBlank(allRequestParams.get("itemname"))
                ? Long.parseLong(allRequestParams.get("itemname"))
                : 0;

        // When click on supplier count in notification then filter data by supplier ids
        List<Long> contactList;
        int isContactList = 0;
        if(StringUtils.isNotBlank(allRequestParams.get("supplierIds"))){
            contactList = Arrays.stream(allRequestParams.get("supplierIds").split(",")).map(Long::parseLong).collect(Collectors.toList());
            isContactList = 1;
        } else {
            contactList = Collections.singletonList(0L);
        }

        int isGenerateFromStockTransfer = StringUtils.isNotBlank(allRequestParams.get("isGenerateFromStockTransfer"))
                ? Integer.parseInt(allRequestParams.get("isGenerateFromStockTransfer"))
                : 0;

        int totalLength = purchaseService.countByPurchaseAllDatatable(branchList, type, contactId, startDate, endDate, status, paidType, serachValue, productVarientId, isdateofsupply, fromdateofsupply, todateofsupply, isDueDate, fromDueDate, toDueDate, isContactList, contactList, isGenerateFromStockTransfer);

        int start = StringUtils.isNotBlank(allRequestParams.get(Constant.START))
                ? Integer.parseInt(allRequestParams.get(Constant.START))
                : 0;
        String pageLength = StringUtils.defaultIfBlank(allRequestParams.get(Constant.LENGTH), "10");
        int length, page = 0, offset;

        if (!StringUtils.equals(pageLength, "-1")) {
            length = Integer.parseInt(pageLength);
 	        page = start / length; //Calculate page number
 	    	offset= page*length;

 	   	}else {
 	   		length=totalLength;
            offset = 0;
        }

        List<DataTablePurchaseDTO> list = totalLength > 0
                ? purchaseService.findByPurchaseAllDatatable(branchList, type, contactId, startDate, endDate, status,
                    paidType, serachValue, length, offset, productVarientId, isdateofsupply, fromdateofsupply, todateofsupply, isDueDate, fromDueDate, toDueDate, isContactList, contactList, isGenerateFromStockTransfer)
                : Collections.emptyList();

		DataTablePurchaseResponceDTO dto=new DataTablePurchaseResponceDTO();
		dto.setData(list);
		dto.setDraw(Integer.parseInt(allRequestParams.get(Constant.DRAW)));
		dto.setError(null);
		dto.setRecordsFiltered(totalLength);
		dto.setRecordsTotal(totalLength);
        dto.setDataTableMetaDTO(new DataTableMetaDTO(page, (int) Math.ceil((double) (totalLength) / length), length, totalLength));
		return dto;

    }

    @RequestMapping("/getpurchaseno")
    @ResponseBody
    public Map<String, String> getPurchaseNo(HttpSession session, @PathVariable(value = "type") String type) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_FIND_PURCHASE_NO;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_FIND_PURCHASE_NO;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_FIND_PURCHASE_NO;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_FIND_PURCHASE_NO;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_FIND_PURCHASE_NO;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	Map<String, String> map = new HashMap<>();
    	String purchaseno = "0";
    	String prefix = "0";
    	try {
    		prefix = prefixService
                    .getPrefixByPrefixTypeAndBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), type, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
    		long newPurchaseNo = purchaseService.getNewPurchaseNo(type,
                    Long.parseLong(session.getAttribute("branchId").toString()),
                    Long.parseLong(session.getAttribute("userId").toString()), prefix, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
    		purchaseno = String.valueOf(newPurchaseNo);

//    		map.put("NewPurchaseNo", String.valueOf(newPurchaseNo));
//        	map.put("purchasePrefix", prefixService
//                    .findByBranchIdAndprefixType(Long.parseLong(session.getAttribute("branchId").toString()), type)
//                    .get(0).getPrefix());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	map.put("NewPurchaseNo", purchaseno);
    	map.put("purchasePrefix", prefix);

    	return map;

    }

    @RequestMapping("/new")
    public ModelAndView newPurchase(HttpSession session, @PathVariable(value = "type") String type,
                                    @Param(value = "contactId") String contactId,
                                    @RequestParam(value = "parentId", defaultValue = "0") String parentId) {

        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_NEW;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_NEW;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_NEW;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_NEW;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_NEW;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        ModelAndView view = new ModelAndView();
        view.addObject("isPercentageDiscount", companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.BATCHDISCOUNTVALUE).getValue());
        view.addObject(Constant.STOPUMOWISEDECIMAL, companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.STOPUMOWISEDECIMAL));
        if (MenuPermission.havePermission(session, type, Constant.INSERT) == 1) {
        	 long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
             long userId = Long.parseLong(session.getAttribute(Constant.USERID).toString());
        	 String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
             long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
             long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
             long userType = Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString());
             int taxVal = 0;
            float roundoffAmount = 0;
            int allowSupplierMappingPrice = 1; // Used for Not fetching Supplier Mapping Price in PO By Sale Qty Page
            // Check if valid using the existing method
            boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
            view.addObject("isValidMerchantType", isValidMerchantType);
             if(session.getAttribute("governmentTaxType").toString().equals(Constant.VAT)) {
     			taxVal = 1;
     		 }
             view.addObject(Constant.EXPIRY,
     				companySettingService.findByCompanyIdAndType(companyId, Constant.EXPIRY));
             String purchaseKey = userId + type + "_"+ Calendar.getInstance().getTimeInMillis();
             purchaseKey = purchaseKey.concat(generateRandomString(5));
             view.addObject("purchaseKey", purchaseKey);
             view.addObject(Constant.PURCHASEALLOWFOCUSON,
 					companySettingService.findByBranchIdAndType(branchId, Constant.PURCHASEALLOWFOCUSON));
             view.addObject(Constant.ALLOWPURCHASEMERGEITEM,
 					companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWPURCHASEMERGEITEM));
             view.addObject(Constant.IS_OCR_ENABLED,0);
             view.addObject(Constant.FATOORAHQRCODE,
                     companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.FATOORAHQRCODE));
             if(type.equals(Constant.PURCHASE_BILL)) {
                 view.addObject(Constant.IS_OCR_ENABLED, companySettingService.getvalueByCompanyIdAndType(companyId, Constant.IS_OCR_ENABLED));
                 // Prefix-Alert After Year Ending Process:-
                 yearEndingPrefixAlertMsg.prefixAlertAfterYearEnding(session,Constant.PURCHASE_BILL,view);
              	CompanySettingVo setting = companySettingService.findByBranchIdAndType(companyId, Constant.SUPPLIER_BILL);
              		if(setting!=null) {
              			if(setting.getValue()==1) {
              				view.setViewName("purchase/purchase-new");
              			}else {
              				CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(companyId, Constant.PURCHASECONVERSATION);
                  			if(conversation!=null) {
                  				if(conversation.getValue()==1) {
                  					view.setViewName("purchase/purchase-new-conversation");
                      			}else {
                      				view.setViewName("purchase/purchase-new");
                      			}
                  			}else {
                  				view.setViewName("purchase/purchase-new");
                  			}
              			}
              		}else {
              			CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(companyId, Constant.PURCHASECONVERSATION);
              			if(conversation!=null) {

              				if(conversation.getValue()==1) {

                  				view.setViewName("purchase/purchase-new-conversation");
                  			}else {
                  				view.setViewName("purchase/purchase-new");
                  			}
              			}else {
              				view.setViewName("purchase/purchase-new");
              			}
              		}

              } else  if(type.equals(Constant.PURCHASE_ORDER)) {
            	  CompanySettingVo pobysalesqty = companySettingService.findByCompanyIdAndType(companyId, Constant.POBYSALESQTY);
                  view.addObject("pobysalesqty", pobysalesqty);
                  if(pobysalesqty!=null) {
        				if(pobysalesqty.getValue()==1) {
                            allowSupplierMappingPrice = 0;
        					view.addObject("CategoryList",categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
        					view.addObject("BrandList",brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
        					view.setViewName("purchase/purchase-new-pobysalesqty");
            			}else {
            				view.setViewName("purchase/purchase-new");
            			}
        			}else {
        				view.setViewName("purchase/purchase-new");
        			}

              }else {
              	view.setViewName("purchase/purchase-new");
              }

            CompanySettingVo pobysalesqty = companySettingService.findByCompanyIdAndType(companyId, Constant.POBYSALESQTY);
            view.addObject("pobysalesqty", pobysalesqty);
            view.addObject("allowSupplierMappingPrice", allowSupplierMappingPrice);
            view.addObject("type", type);
            String prefixtype = "BIL";
            if (type.equals(Constant.PURCHASE_BILL)) {
            	prefixtype = "BIL";
                view.addObject("displayType", "Supplier Bill");
            } else if (type.equals(Constant.PURCHASE_ORDER)) {
            	prefixtype = "BIL";
                view.addObject("displayType", "Purchase Order");
            } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
            	prefixtype = "BIL";
                // Prefix-Alert After Year Ending Process:-
                yearEndingPrefixAlertMsg.prefixAlertAfterYearEnding(session,Constant.PURCHASE_DEBIT_NOTE,view);
                view.addObject("displayType", "Debit Note");
            } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
            	prefixtype = "MI";
                view.addObject("displayType", "Material Inward");
//                if (!parentId.equals("0")) {
//                	List<Long> parent = purchaseService.checkBillgeneratedornot(Long.parseLong(parentId), Constant.PURCHASE_MATERIALINWARD, Long.parseLong(session.getAttribute("branchId").toString()));
//        			//System.err.println("HERE parent size in PURCHASE_MATERIALINWARD new is :"+parent.size());
//        			if(parent.size() == 0) {
//        				view.addObject("inwardgenerated", 0);
//        			}else {
//        				view.addObject("inwardgenerated", 1);
//        			}
//                }
            }

//			view.addObject("ContactList", contactService.contactList(
//					Long.parseLong(session.getAttribute("branchId").toString()), Constant.CONTACT_SUPPLIER));

            // view.addObject("ProductList",productService.findByCompanyIdAndIsDeleted(Long.parseLong(session.getAttribute("companyId").toString()),
            // 0));
            long newPurchaseNo = purchaseService.getNewPurchaseNo(type, branchId, userId, prefixtype, companyId);
            String prefix = prefixService
                    .getPrefixByPrefixTypeAndBranchId(branchId, type, companyId);

            view.addObject(Constant.ALLOWATTACHMENTVALIDATION,companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWATTACHMENTVALIDATION).getValue());


            view.addObject("NewPurchaseNo", newPurchaseNo);
            view.addObject("purchasePrefix", prefix);
            view.addObject("paymentTermList", paymentTermService.findBybranchId(branchId, 0, companyId));
            // view.addObject("category",categoryService.findByCompanyId(Long.parseLong(session.getAttribute("companyId").toString())));
            // view.addObject("brand",brandService.findByCompanyId(Long.parseLong(session.getAttribute("companyId").toString())));
            view.addObject(Constant.MULTIBARCODE,companySettingService.findByCompanyIdAndType(companyId, Constant.MULTIBARCODE));
            view.addObject(Constant.MULTIDUPLICATEBARCODE,companySettingService.findByCompanyIdAndType(companyId, Constant.MULTIDUPLICATEBARCODE));

            view.addObject("supplierInsert",
                    MenuPermission.havePermission(session, Constant.CONTACT_SUPPLIER, Constant.INSERT));
            view.addObject("categoryBrandPermissions",
                    MenuPermission.havePermission(session, Constant.CATEGORY_BRANDS, Constant.EDIT));
            view.addObject(Constant.Taxdefault, companySettingService.findByBranchIdAndType(
					branchId, Constant.Taxdefault));
            view.addObject("UomList", unitOfMeasurementService
                    .findByCompanyIdAndIsDeleted(companyId, 0,merchantTypeId,clusterId));
            view.addObject("CategoryList",
                    categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
            view.addObject("BrandList", brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
             view.addObject("contactlist", contactService.findByType(Constant.CONTACT_TRANSPORT,
			branchId));
//            view.addObject("TaxList",
//                    taxService.findByCompanyId(Long.parseLong(session.getAttribute("companyId").toString())));
//            view.addObject("isItemCodeProduct",
//            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.ITEMCODE_PRODUCT));
            view.addObject("isItemCodeProduct",1);
            view.addObject("isQty", userType<5?1:MenuPermission.havePermission(session, type, Constant.QUANTITY));
            view.addObject("isFreeQty", userType<5?1:MenuPermission.havePermission(session, type, Constant.FREE_QTY));
            view.addObject("isUnitCostMrpSpMarginTaxType", userType<5?1:MenuPermission.havePermission(session, type, Constant.UNITCOST_MRP_SP_MARGIN_TAXTYPE));
            view.addObject("isDiscount1", userType<5?1:MenuPermission.havePermission(session, type, Constant.DISCOUNT_1));
            view.addObject("isDiscount2", userType<5?1:MenuPermission.havePermission(session, type, Constant.DISCOUNT_2));
            view.addObject("isFlatDiscount", userType<5?1:MenuPermission.havePermission(session, type, Constant.FLAT_DISCOUNT));
            view.addObject("isRoundOff", userType<5?1:MenuPermission.havePermission(session, type, Constant.ROUND_OFF));
            view.addObject("isAdditionalChargeValue", userType<5?1:MenuPermission.havePermission(session, type, Constant.ADDITIONAL_CHARGES_VALUE));
            view.addObject("isProductEdit", userType<5?1:MenuPermission.havePermission(session, Constant.PRODUCT, Constant.EDIT));
            view.addObject("isContactEdit", userType<5?1:MenuPermission.havePermission(session, Constant.CONTACT, Constant.EDIT));
            view.addObject("isAdditionalChargeAdd", userType<5?1:MenuPermission.havePermission(session, Constant.ADDITIONALCHARGE, Constant.INSERT));
            view.addObject("isSaveAndPayment", userType<5?1:MenuPermission.havePermission(session, Constant.PAYMENT, Constant.INSERT));
            view.addObject("isSaveAndPrint", userType<5?1:MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT));
            view.addObject("isDebitNoteNew", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.INSERT));
            view.addObject("isUnitCostMrpSpMarginTaxTypeDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.UNITCOST_MRP_SP_MARGIN_TAXTYPE));
            view.addObject("isDiscount1DebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.DISCOUNT_1));
            view.addObject("isDiscount2DebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.DISCOUNT_2));
            view.addObject("isRoundOffDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.ROUND_OFF));
            view.addObject("isQtyDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.QUANTITY));
//            view.addObject("isCleared",
//            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.CLEARED));
//            view.addObject("isUploadExcel",
//            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.UPLOAD_EXCEL));
            view.addObject("isCleared",1);
            view.addObject("isUploadExcel",1);
            List<TaxVo> taxVos=taxService.findByCompanyId(companyId,merchantTypeId,clusterId,taxVal);
            view.addObject("TaxList",taxVos);
            view.addObject("tax",taxVos);
            view.addObject("ProductOption",productService.getProductOption(companyId, 0));
            view.addObject("department",departmentService.findByCompanyId(companyId,merchantTypeId,clusterId));
            view.addObject("EmployeeList",employeeService.getEmployeeByAssignedBranchId(branchId));

            view.addObject("category",categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
            view.addObject("brand",brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
            int garmentIndustryTaxType=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTINDUSTRYTAXTYPE);
            int garmentIndustryTaxTypeMethod=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTTAX_CALCULATION_METHOD);
            int hsnTypeWiseCalculation= companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATION);
            int hsnTypeWiseCalculationMethod = companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATIONMETHOD);
            view.addObject("garmentIndustryTaxType", garmentIndustryTaxType);
            view.addObject("garmentIndustryTaxTypeMethod",garmentIndustryTaxTypeMethod);
            view.addObject("hsnTypeWiseCalculation",hsnTypeWiseCalculation);
            view.addObject("hsnTypeWiseCalculationMethod",hsnTypeWiseCalculationMethod);
            view.addObject("TermsAndCondition", purchaseTermsAndConditionService.findByBranchIdAndIsDefaultAndIsDeleted(branchId, 1, 0, companyId));
            view.addObject("termsAndCondition", purchaseTermsAndConditionService.getTermsAndConditionIdByCompanyId(companyId, 1, 0));
            String gstType= userRepository.getTaxTypeByUserFrontId(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
            if((garmentIndustryTaxType==1 || hsnTypeWiseCalculation==1) && !gstType.equals(Constant.VAT)) {
        		int taxType = Constant.TAX_TYPE_GST;
        		try {
        			Map<String, String> gstMap = userRepository.getgstDetails(companyId);
        			if(gstMap!=null && !gstMap.isEmpty()) {
        				if(StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"),Constant.VAT)) {
        					taxType = Constant.TAX_TYPE_VAT;
        				}
        			}
        		}catch (Exception e) {
        			e.printStackTrace();
        		}

	            TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(5, companyId,taxType,"");
	            view.addObject("tax5name", taxVo.getTaxName());
	            view.addObject("tax5rate", taxVo.getTaxRate());
	            view.addObject("tax5id", taxVo.getTaxId());

	            taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(12, companyId,taxType,"");
	            view.addObject("tax12name", taxVo.getTaxName());
	            view.addObject("tax12rate", taxVo.getTaxRate());
	            view.addObject("tax12id", taxVo.getTaxId());

                taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(18, companyId,taxType,"");
                view.addObject("tax18name", taxVo.getTaxName());
                view.addObject("tax18rate", taxVo.getTaxRate());
                view.addObject("tax18id", taxVo.getTaxId());
            }
            view.addObject(Constant.ALLPRICESHOW,companySettingService.findByCompanyIdAndType(companyId, Constant.ALLPRICESHOW));
            view.addObject(Constant.REATILERMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.REATILERMARGIN));
            view.addObject(Constant.WHOLESALERMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.WHOLESALERMARGIN));
            view.addObject(Constant.SELLINGMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.SELLINGMARGIN));

            view.addObject(Constant.PRODUCTTYPE, companySettingService.findByCompanyIdAndType(companyId, Constant.PRODUCTTYPE));
            view.addObject("productType",productTypeRepository.findAll());
            CompanySettingVo setting2 = companySettingService.findByBranchIdAndType(branchId, "taxIncluded");
            view.addObject("taxIncluded",setting2.getValue());
            CompanySettingVo setting1 = companySettingService.findByBranchIdAndType(branchId, "purchaseTaxIncluded");
            view.addObject("purchaseTaxIncluded",setting1.getValue());
            view.addObject(Constant.ALLOWROUNDOFF, companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWROUNDOFF));
            view.addObject("allowNegativeStock", companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWNEGATIVESTOCK));
            if (!parentId.equals("0")) {
                List<PurchaseItemVo> purchaseItemVo = purchaseService.findbyPurchaseVoPurchaseId(Long.parseLong(parentId));
                if(type.equals(Constant.PURCHASE_MATERIALINWARD) || type.equals(Constant.PURCHASE_BILL)){
                    purchaseItemVo.removeIf(x-> x.getReceiveQty()>=x.getQty());
                 }

                 if(purchaseItemVo.size()>0) {
                     if(type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                         for (PurchaseItemVo itemVo : purchaseItemVo) {
                             BigDecimal miQty = BigDecimal.ZERO;
                             BigDecimal qty = new BigDecimal(String.valueOf(itemVo.getQty()));
                             BigDecimal receivedQty = new BigDecimal(String.valueOf(itemVo.getReceiveQty()));
                             miQty = qty.subtract(receivedQty);
                             itemVo.setMiQty(miQty.doubleValue());
                         }
                     }
	                view.addObject("purchaseItemVo", purchaseItemVo);
	                view.addObject("parentId", parentId);
	                view.addObject("parentType", purchaseItemVo.get(0).getPurchaseVo().getType());
	                contactId = String.valueOf(purchaseItemVo.get(0).getPurchaseVo().getContactVo().getContactId());
	                view.addObject("ContactVo", purchaseItemVo.get(0).getPurchaseVo().getContactVo());
	                if(purchaseItemVo.get(0).getFlatDiscount()>0) {
	                	view.addObject("FLATDISCOUNT",true);
	                }else {
	                	view.addObject("FLATDISCOUNT",false);
	                }
                    if(purchaseItemVo.get(0).getPurchaseVo().getRoundoff() != 0){
                        roundoffAmount = purchaseItemVo.get(0).getPurchaseVo().getRoundoff();
                    }
	                view.addObject("flatDiscount",purchaseItemVo.get(0).getPurchaseVo().getFlatDiscount());
                 }else {
                	 view.addObject("FLATDISCOUNT",false);
                 }
            }else {
            	view.addObject("FLATDISCOUNT",false);
            }
            view.addObject("roundoffAmount",roundoffAmount);
            view.addObject(Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING,companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING).getValue()!=1?0:1);
            view.addObject("NEWPRODUCTPERMISSION", MenuPermission.havePermission(session, Constant.PRODUCT, Constant.INSERT));
            view.addObject("insert_supplier", MenuPermission.havePermission(session, Constant.CONTACT_SUPPLIER, Constant.INSERT));
            if (contactId != null) {

                view.addObject("contactId", contactId);
                view.addObject("ContactVo", contactService.findByContactId(Long.parseLong(contactId)));

            }
            view.addObject(Constant.BARCODESERIES, 0);
            if (companyId == 202) {
                CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
            }
            if (companyId == 415) {
                CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(
                        Long.parseLong(session.getAttribute("companyId").toString()), Constant.BARCODESERIES);

                view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
            }

            if (companyId == 436) {
                CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
            }

            CompanySettingVo barcodegenrateserieswise = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEGENRATESERIESWISE);
            if(barcodegenrateserieswise!=null) {
            	if(barcodegenrateserieswise.getValue()==1){
            		CompanySettingVo barcodemanage = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEMANAGE);
            		if(barcodemanage!=null && barcodemanage.getValue()==2) {
            			try {
            				CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
               			 	CompanySettingVo barcodeprefix = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEPREFIX);
               			 CompanySettingVo barcodelength = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODELENGTH);
               			 view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
               			 view.addObject(Constant.BARCODEPREFIX, barcodeprefix.getAddValue());
               			view.addObject(Constant.BARCODEGENRATESERIESWISE, 1);
               			view.addObject(Constant.BARCODELENGTH, barcodelength.getValue());
            			} catch (Exception e) {
							// TODO: handle exception
						}

            		}else if(barcodemanage!=null && barcodemanage.getValue()==3) {
            			try {
            				CompanySettingVo barcodeSeries = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODESERIES);
               			 	CompanySettingVo barcodeprefix = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODEPREFIX);
               			 CompanySettingVo barcodelength = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODELENGTH);
	               			 view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
	               			 view.addObject(Constant.BARCODEPREFIX, barcodeprefix.getAddValue());
	               			 view.addObject(Constant.BARCODEGENRATESERIESWISE, 1);
	               			view.addObject(Constant.BARCODELENGTH, barcodelength.getValue());
            			} catch (Exception e) {
							// TODO: handle exception
						}
            		}
            	}
            }

            List<String> groupNature = new  ArrayList<String>();
            if (type.equals(Constant.PURCHASE_BILL)) {
            	groupNature.add(Constant.ACCOUNT_PURCHASE);
            	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(companyId, branchId,groupNature);
            	view.addObject("accountCustomDTO", accountCustomDTO);
            } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
            	groupNature.add(Constant.ACCOUNT_PURCHASE_RETURN);
            	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(companyId, branchId,groupNature);
            	view.addObject("accountCustomDTO", accountCustomDTO);
            }
             DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
	         Date date = new Date();
	         view.addObject("serverdate", dateFormat2.format(date));
            view.addObject(Constant.PRODUCTTYPE, companySettingService.findByCompanyIdAndType(companyId, Constant.PRODUCTTYPE));
            view.addObject(Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING, companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING));
            String tanNo = "";
            if ((Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_COMPANY) ||
                    (Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_FRANCHISE) || (Integer.parseInt(session.getAttribute("parentUserType").toString()) == Constant.URID_FRANCHISE)) {
                tanNo = profileService.getTanNo(branchId);
                view.addObject("tanNo", tanNo);
            } else {
                tanNo = profileService.getTanNo(companyId);
                view.addObject("tanNo", tanNo);
            }
            if (StringUtils.isNotBlank(tanNo)) {
                List<Map<String, String>> tcsLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TCS);
                List<Map<String, String>> tdsLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TDS);
                view.addObject("tdsLedgerlist", tdsLedgerlist);
                view.addObject("tcsLedgerlist", tcsLedgerlist);
            }
        } else {
            view.setViewName(Constant.ACCESSDENIED);
        }
        try{
            if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE)!=null?session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE):"0").toString()) == 1) {

                int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());

                int userType = Integer.parseInt(session.getAttribute("userType").toString());

                if (userType > Constant.URID_USER){
                    userType = Integer.parseInt(session.getAttribute("parentUserType").toString());
                }

                String typesenseCollectionName = typesenseService.getCollectionName(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), accountingType, userType, Constant.CONTACT_SUPPLIER);
                log.warning("typesenseCollectionName : "+typesenseCollectionName);
                view.addObject("typesenseCollectionName", typesenseCollectionName);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @PostMapping("/create")
    public String insertPurchase(@RequestParam Map<String, String> allRequestParams,@PathVariable(value = "type") String type,@RequestParam(value =  "image_logo",required = false) MultipartFile file,
    								@ModelAttribute("purchaseVo") PurchaseVo purchaseVo,HttpSession session, HttpServletRequest request) throws IOException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CREATE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CREATE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CREATE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CREATE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CREATE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        } else {
            long idd = purchaseVo.getPurchaseId();
            ContactAddressVo contactAddressVo;
            DecimalFormat df2 = new DecimalFormat("#.##");
//        double miqty;
//        double listmiqty=0.0;
            BigDecimal miqty = BigDecimal.ZERO;
            BigDecimal listmiqty = BigDecimal.ZERO;
            String postatus = "";
            long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
            long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
            long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
            String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
            StockTransferVo stockTransferVo = stockTransferService
                    .findByStockTransferIdAndIsDeleted(purchaseVo.getStockTransferId(), 0);
            String stockApproveRejectFromBranchId = session.getAttribute(Constant.USERID).toString();
            String stockApproveRejectFromBranchName = session.getAttribute("Name").toString();
            int decimalNumber = 2;
            int roundOff = companySettingService.getvalueByCompanyIdAndType(companyId, Constant.ALLOWROUNDOFF);
            int garmentIndustryTaxType=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTINDUSTRYTAXTYPE);
            int garmentIndustryTaxTypeMethod=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTTAX_CALCULATION_METHOD);
            int hsnTypeWiseCalculation= companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATION);
            int hsnTypeWiseCalculationMethod = companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATIONMETHOD);
            TaxVo tax5 = null;
            TaxVo tax12 = null;
            TaxVo tax18 = null;
            String gstType= userRepository.getTaxTypeByUserFrontId(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
            if ((garmentIndustryTaxType == 1 || hsnTypeWiseCalculation == 1) && !gstType.equals(Constant.VAT)) {
                int taxType = Constant.TAX_TYPE_GST;
                try {
                    Map<String, String> gstMap = userRepository.getgstDetails(Long.parseLong(session.getAttribute("companyId").toString()));
                    if (gstMap != null && !gstMap.isEmpty()) {
                        if (StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"), Constant.VAT)) {
                            taxType = Constant.TAX_TYPE_VAT;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tax5 = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(Constant.TAX_RATE_5, companyId, taxType, "");
                tax12 = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(Constant.TAX_RATE_12, companyId, taxType, "");
                tax18 = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(Constant.TAX_RATE_18, companyId, taxType, "");
            }
            try {
                decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (purchaseVo.getPurchaseId() == 0) {
                long newPurchaseNo = purchaseService.getNewPurchaseNo(type,branchId,
                        Long.parseLong(session.getAttribute(Constant.USERID).toString()), purchaseVo.getPrefix(), companyId);
                purchaseVo.setPurchaseNo(newPurchaseNo);
                if (StringUtils.isBlank(clusterId)) {
                    purchaseVo.setBillNo(purchaseVo.getPrefix() + purchaseVo.getPurchaseNo());
                }
            }

            purchaseVo.setType(type);
            int isedit = 1;
            if (purchaseVo.getPurchaseId() == 0) {


                isedit = 0;
                if (type.equals(Constant.PURCHASE_BILL)) {
                    purchaseVo.setStatus("due");
                } else if (type.equals(Constant.PURCHASE_ORDER)) {
                    CompanySettingVo companySettingVo = companySettingService.findByBranchIdAndType(branchId, Constant.POAPPROVAL);
                    if (companySettingVo.getValue() == 1) {
                        purchaseVo.setStatus(Constant.TOAPPROVE);
                    } else {
                        purchaseVo.setStatus(Constant.DRAFT);
                    }
                }
            }

            if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                purchaseVo.setStatus("open");
                //edit delete receive  qty first

                if (isedit == 1) {
                    PurchaseVo purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(idd, branchId);
                    purchaseService.deletepurchasereceiveQty(purchaseVo2);
                    //purchaseVo.setStockByMi(purchaseVo2.getStockByMi());
                }

            } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                purchaseVo.setStatus("open");

                //edit delete receive  qty first
                if (isedit == 1) {
                    PurchaseVo purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(idd, Long.parseLong(session.getAttribute("branchId").toString()));
                    purchaseService.deletepurchasereceiveQty(purchaseVo2);
                    purchaseVo.setStockByMi(purchaseVo2.getStockByMi());
                }
                //update status for po
                if (purchaseVo.getPurchaseVo() != null) {
                    long poId = purchaseVo.getPurchaseVo().getPurchaseId();
                    double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                    List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(poId);
                    if (isedit == 0) {
                        for (int i = 0; i < mIVo.size(); i++) {
                            if (mIVo.get(i).getPurchaseItemVos() != null) {
                                BigDecimal qty = mIVo.get(i).getPurchaseItemVos()
                                        .stream()
                                        .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                listmiqty = listmiqty.add(qty);
                                //listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                //System.err.println("HERE listmiqty is :"+listmiqty);
                            }
                        }
                    } else if (mIVo.size() > 1) {
                        for (int i = 0; i < mIVo.size(); i++) {
                            if (mIVo.get(i).getPurchaseItemVos() != null) {
                                if (mIVo.get(i).getPurchaseId() != purchaseVo.getPurchaseId()) {
                                    BigDecimal qty = mIVo.get(i).getPurchaseItemVos()
                                            .stream()
                                            .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    listmiqty = listmiqty.add(qty);
                                    //listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                    //System.err.println("HERE listmiqty is :"+listmiqty);
                                }
                            }
                        }
                    }
                    if (purchaseVo.getPurchaseItemVos() != null) {
                        BigDecimal qty = purchaseVo.getPurchaseItemVos()
                                .stream()
                                .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        miqty = listmiqty.add(qty);
                        BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                        if (miqty.compareTo(poqtyBigDecimal) == 0) {
                            postatus = "delivered";
                        } else if (miqty.compareTo(poqtyBigDecimal) < 0) {
                            postatus = "partiallydelivered";
                        } else if (miqty.compareTo(poqtyBigDecimal) > 0) {
                            postatus = "exceed";
                        }
//                    if (miqty == poqty) {
//						postatus = "delivered";
//					} else if (miqty < poqty) {
//						postatus = "partiallydelivered";
//					} else if (miqty > poqty) {
//						postatus = "exceed";
//					}
                        purchaseService.updatePurchaseStatus(poId, postatus);

                    } else {
                        purchaseVo.setStatus("mi created");
                    }
                }
            }


            if (type.equals(Constant.PURCHASE_BILL)) {
                //edit delete receive  qty first
                if (isedit == 1) {
                    PurchaseVo purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(idd, branchId);
                    purchaseService.deletepurchasereceiveQty(purchaseVo2);
                }
                if (purchaseVo.getPurchaseVo() != null) {
                    long poId = purchaseVo.getPurchaseVo().getPurchaseId();
                    PurchaseVo purchasevo1 = purchaseService.findByPurchaseIdAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                    if (purchasevo1 != null) {
                        double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                        miqty = purchaseVo.getPurchaseItemVos().stream()
                                .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        //miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                        //update receive qty into-> MI
                        BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                        if (miqty.compareTo(poqtyBigDecimal) <= 0) {
                            purchaseService.updatePurchaseReceivedqty(poId, miqty.doubleValue());
                        } else {
                            purchaseService.updatePurchaseReceivedqty(poId, poqty);
                        }
//                        if(miqty <=poqty) {
//	            			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
//	            		}else {
//	            			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
//	            		}
                        if (purchasevo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {

                            //update status of PO
                            if (purchasevo1.getPurchaseVo() != null) {
                                if (purchasevo1.getPurchaseVo().getStatus().equals("delivered") || purchasevo1.getPurchaseVo().getStatus().equals("exceed")) {
                                    purchaseService.updatePurchaseStatus(purchasevo1.getPurchaseVo().getPurchaseId(), "close");
                                }
                            }

                        } else if (purchasevo1.getType().equals(Constant.PURCHASE_ORDER)) {
                            purchaseService.updatePurchaseStatus(poId, "close");
                        }
                    }
                } else {
                    if (purchaseVo.getMaterialInwardIds() != null) {
                        String[] billNo = purchaseVo.getMaterialInwardIds().split(",");
                        for (int i = 0; i < billNo.length; i++) {
                            PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(Long.parseLong(billNo[i]), Long.parseLong(session.getAttribute("branchId").toString()));
                            if (purchaseVo1 != null) {
                                if (purchaseVo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                                    double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(purchaseVo1.getPurchaseId(), Long.parseLong(session.getAttribute("branchId").toString()));
                                    //System.out.println("her call ::"+purchaseVo1.getPurchaseId()+":::"+purchaseVo.getPurchaseId());
                                    purchaseService.updateQtyOfMi(purchaseVo1.getPurchaseId(), purchaseVo.getPurchaseId());
                                    //update status of MI
                                    //purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(),"completed");

                                    //update status of PO
                                    if (purchaseVo1.getPurchaseVo() != null) {
                                        if (purchaseVo1.getPurchaseVo().getStatus().equals("delivered") || purchaseVo1.getPurchaseVo().getStatus().equals("exceed")) {
                                            List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(purchaseVo1.getPurchaseVo().getPurchaseId());
                                            int update = 0;
                                            for (int j = 0; j < mIVo.size(); j++) {
                                                //System.err.println("staus of mi"+mIVo.get(j).getStatus());
                                                if (mIVo.get(j).getStatus().equals("completed")) {
                                                    update = 1;
                                                } else {
                                                    update = 0;
                                                    break;
                                                }
                                            }
                                            if (update == 1) {
                                                purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseVo().getPurchaseId(), "close");
                                            }
                                        }
                                    }
                                } else if (purchaseVo1.getType().equals(Constant.PURCHASE_ORDER)) {
                                    purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(), "close");
                                }
                            }
                        }
                    }
                }
            } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                if (purchaseVo.getPurchaseVo() != null) {
                    long poId = purchaseVo.getPurchaseVo().getPurchaseId();
                    double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                    BigDecimal qty = purchaseVo.getPurchaseItemVos()
                            .stream()
                            .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    miqty = listmiqty.add(qty);
                    BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                    if (miqty.compareTo(poqtyBigDecimal) <= 0) {
                        purchaseService.updatePurchaseReceivedqty(poId, miqty.doubleValue());
                    } else {
                        purchaseService.updatePurchaseReceivedqty(poId, poqty);
                    }
//                miqty = listmiqty + purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//                if (miqty <= poqty) {
//                    purchaseService.updatePurchaseReceivedqty(poId, miqty);
//                } else {
//                    purchaseService.updatePurchaseReceivedqty(poId, poqty);
//                }
                }

            }
            double purchaseTotalTaxAmount = 0.0;
            try {
                if (purchaseVo.getPurchaseItemVos() != null) {
                    purchaseTotalTaxAmount = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getTaxAmount()).sum();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            purchaseVo.setNotes(allRequestParams.get("notes"));
            purchaseVo.setPurchaseTotalTaxAmount(purchaseTotalTaxAmount);
            purchaseVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
            purchaseVo.setModifiedOn(CurrentDateTime.getCurrentDate());
            purchaseVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
            purchaseVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
                purchaseService.updateDebitNoteAmount(purchaseVo.getPurchaseVo().getPurchaseId(), purchaseVo.getPurchaseId(), purchaseVo.getTotal());
            }
            try {
                purchaseVo.setPurchaseDate(dateFormat.parse(allRequestParams.get("purchaseDate")));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                if (!purchaseVo.getTermsAndConditionIds().equals("")) {
                    purchaseVo.setTermsAndConditionIds(
                            purchaseVo.getTermsAndConditionIds());
                }
            } catch (Exception e) {
            }
            if (type.equals(Constant.PURCHASE_BILL)) {
                try {
                    purchaseVo.setDueDate(dateFormat.parse(allRequestParams.get("dueDate")));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (!type.equals(Constant.PURCHASE_DEBIT_NOTE) && !type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                try {
                    purchaseVo.setDateOfSupply(dateFormat.parse(allRequestParams.get("dateOfSupply")));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                purchaseVo.setShippingDate(dateFormat.parse(allRequestParams.get("shippingDate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                purchaseVo.setTransportDate(dateFormat.parse(allRequestParams.get("transportDate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            PurchaseVo purchaseVo2 = null;

            if (allRequestParams.get("billingAddressId").equals("0")
                    || allRequestParams.get("shippingAddressId").equals("0")) {
                purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(purchaseVo.getPurchaseId(),
                        purchaseVo.getBranchId());
            }

            try {
                // --------------Set Billing Address Details ------------------------
                if (!allRequestParams.get("billingAddressId").equals("0")) {
                    contactAddressVo = contactService
                            .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));
                    purchaseVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
                    purchaseVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
                    purchaseVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
                    purchaseVo.setBillingCompanyName(allRequestParams.get("billingcompanyname"));
                    purchaseVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
                    purchaseVo.setBillingFirstName(allRequestParams.get("billingfirstname"));
                    purchaseVo.setBillingLastName(allRequestParams.get("billinglastname"));
                    purchaseVo.setBillingPinCode(allRequestParams.get("billingpincode"));
                    purchaseVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
                    purchaseVo.setBillingGstin(contactAddressVo.getGstin());
                } else if (purchaseVo2 != null) {
                    contactAddressVo = contactService
                            .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));
                    try {
                        purchaseVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
                        purchaseVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
                        purchaseVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
                        purchaseVo.setBillingCompanyName(allRequestParams.get("billingcompanyname"));
                        purchaseVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
                        purchaseVo.setBillingFirstName(allRequestParams.get("billingfirstname"));
                        purchaseVo.setBillingLastName(allRequestParams.get("billinglastname"));
                        purchaseVo.setBillingPinCode(allRequestParams.get("billingpincode"));
                        purchaseVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
                        purchaseVo.setBillingPhoneNo(allRequestParams.get("billingPhoneNo"));
                        purchaseVo.setBillingGstin(contactAddressVo.getGstin());
                    } catch (Exception e) {
                        e.printStackTrace();
                        purchaseVo.setBillingAddressLine1(purchaseVo2.getBillingAddressLine1());
                        purchaseVo.setBillingAddressLine2(purchaseVo2.getBillingAddressLine2());
                        purchaseVo.setBillingCityCode(purchaseVo2.getBillingCityCode());
                        purchaseVo.setBillingCompanyName(purchaseVo2.getBillingCompanyName());
                        purchaseVo.setBillingCountriesCode(purchaseVo2.getBillingCountriesCode());
                        purchaseVo.setBillingFirstName(purchaseVo2.getBillingFirstName());
                        purchaseVo.setBillingLastName(purchaseVo2.getBillingLastName());
                        purchaseVo.setBillingPinCode(purchaseVo2.getBillingPinCode());
                        purchaseVo.setBillingStateCode(purchaseVo2.getBillingStateCode());
                        purchaseVo.setBillingGstin(purchaseVo2.getBillingGstin());
                    }

                }

                // --------------Set Shipping Address Details ------------------------
                if (!allRequestParams.get("shippingAddressId").equals("0")) {
                    contactAddressVo = contactService
                            .findByContactAddressId(Long.parseLong(allRequestParams.get("shippingAddressId")));

                    purchaseVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
                    purchaseVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
                    purchaseVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
                    purchaseVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
                    purchaseVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
                    purchaseVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
                    purchaseVo.setShippingLastName(allRequestParams.get("shippinglastname"));
                    purchaseVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
                    purchaseVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
                    purchaseVo.setShippingPhoneNo(allRequestParams.get("shippingphone"));
                } else if (purchaseVo2 != null) {
                    try {
                        purchaseVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
                        purchaseVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
                        purchaseVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
                        purchaseVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
                        purchaseVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
                        purchaseVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
                        purchaseVo.setShippingLastName(allRequestParams.get("shippinglastname"));
                        purchaseVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
                        purchaseVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
                        purchaseVo.setShippingPhoneNo(allRequestParams.get("shippingphone"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        purchaseVo.setShippingAddressLine1(purchaseVo2.getShippingAddressLine1());
                        purchaseVo.setShippingAddressLine2(purchaseVo2.getShippingAddressLine2());
                        purchaseVo.setShippingCityCode(purchaseVo2.getShippingCityCode());
                        purchaseVo.setShippingCompanyName(purchaseVo2.getShippingCompanyName());
                        purchaseVo.setShippingCountriesCode(purchaseVo2.getShippingCountriesCode());
                        purchaseVo.setShippingFirstName(purchaseVo2.getShippingFirstName());
                        purchaseVo.setShippingLastName(purchaseVo2.getShippingLastName());
                        purchaseVo.setShippingPinCode(purchaseVo2.getShippingPinCode());
                        purchaseVo.setShippingStateCode(purchaseVo2.getShippingStateCode());
                        purchaseVo.setShippingPhoneNo(purchaseVo2.getShippingPhoneNo());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ////System.err.println("updateaddress is :"+allRequestParams.get("updateaddress"));
                if (StringUtils.isNotBlank(allRequestParams.get("updateaddress"))) {
                    //to update address
                    contactAddressVo = contactService
                            .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));
                    contactAddressVo.setAddressLine1(allRequestParams.get("billingaddressline1"));
                    contactAddressVo.setAddressLine2(allRequestParams.get("billingaddressline2"));
                    contactAddressVo.setCompanyName(allRequestParams.get("billingcompanyname"));
                    contactAddressVo.setPhoneNo(allRequestParams.get("billingphone"));
                    contactAddressVo.setCountriesCode(allRequestParams.get("billingcountrycode"));
                    contactAddressVo.setStateCode(allRequestParams.get("billingstatecode"));
                    contactAddressVo.setCityCode(allRequestParams.get("billingcitycode"));
                    contactAddressVo.setPinCode(allRequestParams.get("billingpincode"));
                    //contactAddressVo.getContact()
                    //contactAddressVo.setContact(contact);
                    contactService.saveAddress(contactAddressVo);
                    //System.err.println("updateaddress*************"+allRequestParams.get("updateaddress"));
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (purchaseVo.getPurchaseId() == 0) {
                purchaseVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
                purchaseVo.setCreatedOn(CurrentDateTime.getCurrentDate());
                purchaseVo.setPaidAmount(0.0);
            }
            if (purchaseVo.getPurchaseAdditionalChargeVos() != null) {
                purchaseVo.getPurchaseAdditionalChargeVos().removeIf(rm -> rm.getAdditionalChargeVo() == null);
                purchaseVo.getPurchaseAdditionalChargeVos().forEach(item1 -> item1.setPurchaseVo(purchaseVo));
            }

            if (purchaseVo.getPurchaseItemVos() != null) {
                purchaseVo.getPurchaseItemVos().removeIf(rm -> rm.getProduct() == null);
                purchaseVo.getPurchaseItemVos().forEach(item -> item.setPurchaseVo(purchaseVo));

            }

            if (allRequestParams.get("deletePurchaseItemIds") != null
                    && !allRequestParams.get("deletePurchaseItemIds").equals("")) {
                String address = allRequestParams.get("deletePurchaseItemIds").substring(0,
                        allRequestParams.get("deletePurchaseItemIds").length() - 1);
                List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());

                purchaseService.deletePurchaseItem(l);
            }

            if (allRequestParams.get("deleteAdditionalChargeIds") != null
                    && !allRequestParams.get("deleteAdditionalChargeIds").equals("")) {

                String address = allRequestParams.get("deleteAdditionalChargeIds").substring(0,
                        allRequestParams.get("deleteAdditionalChargeIds").length() - 1);
                List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());

                purchaseService.deletePurchaseAdditionalItem(l);
            }
            // sheet data upload
            if (allRequestParams.get("addproductby") != null)
                if (allRequestParams.get("addproductby").equals("2")) {
                    try (InputStream is = new FileInputStream((String) session.getAttribute(Constant.FILE_PATH));
                         ReadableWorkbook wb = new ReadableWorkbook(is)) {
                        org.dhatim.fastexcel.reader.Sheet sheet = wb.getFirstSheet();
                        List<org.dhatim.fastexcel.reader.Row> rows = sheet.read();
                        List<PurchaseItemVo> itemVos = new ArrayList<>();
                        DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
                        double mainTotal = 0.0;
                        for (int count = 1; count < rows.size(); count++) {
                            org.dhatim.fastexcel.reader.Row row = rows.get(count);
                            double qty;
                            double rate;
                            double discount, discount2;
                            double taxRate = 0.0;
                            double taxAmount = 0.0, taxableValue = 0.0;
                            double total = 0.0;
                            PurchaseItemVo purchaseItemVo = new PurchaseItemVo();

                            ProductVarientsVo productVarientsVo = productService
                                    .getProductVarientsVobyItemCodeCompanyIdMerchantTypeIdActive(
                                            securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(0).trim()),
                                            companyId, merchantTypeId, clusterId);

                            if (productVarientsVo != null) {
                                purchaseItemVo.setProductVarientsVo(productVarientsVo);
                                purchaseItemVo.setItemCode(row.getCellText(0).trim());
                                purchaseItemVo.setProduct(productVarientsVo.getProductVo());
                                purchaseItemVo.setTaxVo(productVarientsVo.getProductVo().getPurchaseTaxVo());

                                if (allRequestParams.get("gstTaxType").equals("outofscope")) {
                                    purchaseItemVo.setTaxRate(taxRate);
                                } else {
                                    purchaseItemVo.setTaxRate(productVarientsVo.getProductVo().getPurchaseTaxVo().getTaxRate());
                                }
                                try {
                                    purchaseItemVo.setDiscountType(securityValidation.checkAndReplaceCsvInjectionCharacters(StringUtils.lowerCase(row.getCellText(5).trim())));
                                } catch (Exception e) {
                                    purchaseItemVo.setDiscountType("percentage");
                                }
                                try {
                                    purchaseItemVo.setDiscount(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(6).trim())));
                                } catch (Exception e) {
                                    purchaseItemVo.setDiscount(0);
                                }
                                try {
                                    purchaseItemVo.setDiscountType2(securityValidation.checkAndReplaceCsvInjectionCharacters(StringUtils.lowerCase(row.getCellText(7).trim())));
                                } catch (Exception e) {
                                    purchaseItemVo.setDiscountType2("percentage");
                                }
                                try {
                                    purchaseItemVo.setDiscount2(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(8).trim())));
                                } catch (Exception e) {
                                    purchaseItemVo.setDiscount2(0);
                                }
                                try {
                                    if (productVarientsVo.getProductVo().getIsExpiryManage() == 1) {
                                        Calendar c = Calendar.getInstance();
                                        String cellValue = "";
                                        if (type.equals(Constant.PURCHASE_ORDER)) {
                                            cellValue = row.getCellText(4).trim();
                                        } else {
                                            cellValue = row.getCellText(9).trim();
                                        }
                                        int dateCell = 10;
                                        if (type.equals(Constant.PURCHASE_ORDER)) {
                                            dateCell = 5;
                                        }
                                        Date dob = dateFormat2.parse(row.getCellText(dateCell).trim().replace("-", "/"));
                                        c.setTime(dob);
                                        if (cellValue.equals("MFG")) {
                                            purchaseItemVo.setBatchCreationDate(dateFormat2.format(dob));
                                            c.add(Calendar.DATE, Integer.parseInt(productVarientsVo.getProductVo().getExpirationdays()));
                                            dob = c.getTime();
                                            purchaseItemVo.setBatchExpiryDate(dateFormat2.format(dob));
                                        } else {
                                            purchaseItemVo.setBatchExpiryDate(dateFormat2.format(dob));
                                            c.add(Calendar.DATE, -Integer.parseInt(productVarientsVo.getProductVo().getExpirationdays()));
                                            dob = c.getTime();
                                            purchaseItemVo.setBatchCreationDate(dateFormat2.format(dob));
                                        }
                                        purchaseItemVo.setExpiryManage(1);
                                        purchaseItemVo.setExpdays(Integer.parseInt(productVarientsVo.getProductVo().getExpirationdays()));

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    purchaseItemVo.setQty(Float.parseFloat(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(1).trim())));
                                } catch (Exception e) {
                                    purchaseItemVo.setQty(0);
                                }
                                try {
                                    purchaseItemVo.setMrp(round(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(3).trim())), decimalNumber));
                                } catch (Exception e) {
                                    purchaseItemVo.setMrp(0);
                                }
                                try {
                                    purchaseItemVo.setSellingPrice(round(Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(4).trim())), decimalNumber));
                                } catch (Exception e) {
                                    purchaseItemVo.setSellingPrice(0);
                                }
                                try {
                                    int taxincluded = 0;
                                    if (allRequestParams.get("gstTaxType").equals("taxinclusive")) {
                                        taxincluded = 1;
                                    } else if (allRequestParams.get("gstTaxType").equals("taxdefault")) {
                                        taxincluded = productVarientsVo.getProductVo().getPurchaseTaxIncluded();
                                    }
                                    if (taxincluded == 1) {
                                        purchaseItemVo.setPrice(
                                                Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(
                                                        row.getCellText(2).trim())) / ((purchaseItemVo.getTaxRate() / 100) + 1));
                                    } else {
                                        purchaseItemVo.setPrice(
                                                Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(2).trim())));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    purchaseItemVo.setPrice(0);
                                }
                                double landingCost = 0.0;
                                landingCost = round(purchaseItemVo.getPrice() + ((purchaseItemVo.getPrice() * purchaseItemVo.getTaxRate()) / 100), decimalNumber);
                                purchaseItemVo.setLandingCost(landingCost);
                                if (purchaseItemVo.getSellingPrice() == 0) {
                                    purchaseItemVo.setSellingPrice(purchaseItemVo.getMrp());
                                }
                                qty = purchaseItemVo.getQty();
                                taxRate = purchaseItemVo.getTaxRate();
                                rate = purchaseItemVo.getPrice();
                                discount = purchaseItemVo.getDiscount();
                                discount2 = purchaseItemVo.getDiscount2();
                                taxableValue = (rate * qty);
                                if (purchaseItemVo.getDiscountType().equals("percentage")) {
                                    try {
                                        discount = ((rate * qty) * discount) / 100;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        discount = 0.0;
                                    }
                                }
                                taxableValue = taxableValue - discount;
                                if (purchaseItemVo.getDiscountType2().equals("percentage")) {
                                    try {
                                        discount2 = ((taxableValue) * discount2) / 100;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        discount2 = 0.0;
                                    }
                                }
                                taxableValue = taxableValue - discount2;
                                if (garmentIndustryTaxType == 1 || hsnTypeWiseCalculation == 1) {
                                    int taxincluded = 0;
                                    if (allRequestParams.get(Constant.GST_TAX_TYPE).equals("taxinclusive")) {
                                        taxincluded = 1;
                                    } else if (allRequestParams.get(Constant.GST_TAX_TYPE).equals("taxdefault")) {
                                        taxincluded = productVarientsVo.getProductVo().getPurchaseTaxIncluded();
                                    }

                                    double calculationOnMrp = (taxableValue + (taxableValue * (taxRate / 100))) / qty;
                                    double calculationOnTaxable = taxableValue / qty;
                                    double unitPrice = Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(2).trim()));
                                    int taxMethod = garmentIndustryTaxType == 1 ? garmentIndustryTaxTypeMethod : hsnTypeWiseCalculationMethod;
//                                    double pricePerUnit = (taxMethod == 0 ? unitPrice : taxableValue) / qty;
                                    double pricePerUnit = taxMethod == 0 ? calculationOnMrp : calculationOnTaxable;
                                    int hsnType = StringUtils.isNotBlank(productVarientsVo.getProductVo().getHsnCode()) ?
                                            hsnTaxMasterService.getHsnTypeByHsnCode(productVarientsVo.getProductVo().getHsnCode()) : 0;
                                    if(garmentIndustryTaxType == 1){
                                        purchaseItemVo = purchaseService.setGarmentHsnSwitchWiseTax(purchaseItemVo,pricePerUnit,tax12,tax5);
                                    }else if(hsnTypeWiseCalculation == 1){
                                        if(hsnType == 1){
                                            purchaseItemVo = purchaseService.setGarmentHsnSwitchWiseTax(purchaseItemVo,pricePerUnit,tax12,tax5);
                                        }else if(hsnType == 2){
                                            purchaseItemVo = purchaseService.setGarmentHsnSwitchWiseTax(purchaseItemVo,pricePerUnit,tax18,tax12);
                                        }
                                    }
                                    purchaseItemVo.setTaxRate(allRequestParams.get(Constant.GST_TAX_TYPE).equals(Constant.OUT_OF_SCOPE)?0.0:purchaseItemVo.getTaxVo().getTaxRate());
                                    taxRate = purchaseItemVo.getTaxRate();
                                    if (taxincluded == 1) {
                                        purchaseItemVo.setPrice(unitPrice / ((purchaseItemVo.getTaxRate() / 100) + 1));
                                        rate = purchaseItemVo.getPrice();
                                    } else {
                                        purchaseItemVo.setPrice(unitPrice);
                                        rate = purchaseItemVo.getPrice();
                                    }
                                    discount = purchaseItemVo.getDiscount();
                                    discount2 = purchaseItemVo.getDiscount2();
                                    taxableValue = (rate * qty);
                                    if (purchaseItemVo.getDiscountType().equals("percentage")) {
                                        try {
                                            discount = ((rate * qty) * discount) / 100;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            discount = 0.0;
                                        }
                                    }
                                    taxableValue = taxableValue - discount;
                                    if (purchaseItemVo.getDiscountType2().equals("percentage")) {
                                        try {
                                            discount2 = ((taxableValue) * discount2) / 100;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            discount2 = 0.0;
                                        }
                                    }
                                }
                                if (!allRequestParams.get("gstTaxType").equals("outofscope")) {
                                    taxAmount = (taxableValue * taxRate) / 100;
                                }
                                try {
                                    total = Double.parseDouble(new DecimalFormat("#.##").format(taxableValue + taxAmount));
                                } catch (Exception e) {
                                    total = taxableValue + taxAmount;
                                }
                                mainTotal += total;
                                purchaseItemVo.setLandingCost(round(total / qty, decimalNumber));
                                purchaseItemVo.setTaxAmount(round(taxAmount, decimalNumber));
                                purchaseItemVo.setPrice(round(purchaseItemVo.getPrice(), decimalNumber));
                                purchaseItemVo.setPurchaseVo(purchaseVo);
                                purchaseItemVo.setNetAmount(total);
                                itemVos.add(purchaseItemVo);
                            }
                        }
                        purchaseVo.setPurchaseItemVos(itemVos);
                        purchaseVo.setRoundoff(roundOff == 1 ? 0.0f : (float) (Math.round(mainTotal) - mainTotal));

                        purchaseVo.setTotal(Math.round(mainTotal));
                        try {
                            if (purchaseVo.getPurchaseItemVos() != null) {
                                purchaseTotalTaxAmount = purchaseVo.getPurchaseItemVos().stream().mapToDouble(PurchaseItemVo::getTaxAmount).sum();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        purchaseVo.setPurchaseTotalTaxAmount(purchaseTotalTaxAmount);
                    }
                }
            // end sheet data
            if (StringUtils.isNotBlank(allRequestParams.get(Constant.TCS_APPLICABLE_NEW)) && (allRequestParams.get(Constant.TCS_APPLICABLE_NEW)).equals("1")) {
                Map<String, Object> tcsJsonData = new HashMap<>(5);
                String tcsAccountCustomId = allRequestParams.get(Constant.TCS_ACCOUNT_CUSTOM_ID);
                String tcsRate = allRequestParams.get(Constant.TCS_RATE_NEW);
                String tcsAmount = allRequestParams.get(Constant.TCS_AMOUNT_NEW);
                String tcsType = allRequestParams.get(Constant.TCS_TYPE_NEW);
                String tcsApplicable = allRequestParams.get(Constant.TCS_APPLICABLE_NEW);
                if (StringUtils.isNotBlank(tcsApplicable)) {
                    tcsJsonData.put(Constant.TCS_APPLICABLE, Integer.parseInt(tcsApplicable));
                }
                if (StringUtils.isNotBlank(tcsAccountCustomId)) {
                    tcsJsonData.put(Constant.TCS_ACCOUNT_ID, Long.parseLong(tcsAccountCustomId.trim()));
                }
                if (StringUtils.isNotBlank(tcsRate)) {
                    tcsJsonData.put(Constant.TCS_RATE, Double.parseDouble(tcsRate.trim()));
                }
                if (StringUtils.isNotBlank(tcsAmount)) {
                    tcsJsonData.put(Constant.TCS_AMOUNT, Double.parseDouble(tcsAmount.trim()));
                }
                if (StringUtils.isNotBlank(tcsType)) {
                    tcsJsonData.put(Constant.TCS_TYPE, tcsType.trim());
                }
                purchaseVo.setTcsJson(tcsJsonData);
            }
            /*Below code for TDS JSON data*/
            double tdsAmount = 0;
            if (StringUtils.isNotBlank(allRequestParams.get(Constant.TDS_APPLICABLE_NEW)) && (allRequestParams.get(Constant.TDS_APPLICABLE_NEW)).equals("1")) {
                Map<String, Object> tdsJsonData = new HashMap<>(6);
                String tdsAccountCustomId = allRequestParams.get(Constant.TDS_ACCOUNT_CUSTOM_ID);
                String tdsRate = allRequestParams.get(Constant.TDS_RATE_NEW);
                String tdsType = allRequestParams.get(Constant.TDS_TYPE_NEW);
                String tdsPercent = allRequestParams.get(Constant.TDS_percent);
                String tdsApplicable = allRequestParams.get(Constant.TDS_APPLICABLE_NEW);
                String tdsRoundoff = allRequestParams.get(Constant.TDS_ROUND_OFF);
                if (StringUtils.isNotBlank(tdsApplicable)) {
                    tdsJsonData.put(Constant.TDS_APPLICABLE, Integer.parseInt(tdsApplicable));
                }
                if (StringUtils.isNotBlank(tdsAccountCustomId)) {
                    tdsJsonData.put(Constant.TDS_ACCOUNT_ID, Long.parseLong(tdsAccountCustomId.trim()));
                }
                if (StringUtils.isNotBlank(tdsRate)) {
                    if (tdsType.equals("percentage")) {
                        tdsJsonData.put(Constant.TDS_RATE, Double.parseDouble(tdsRate.trim()));
                    } else {
                        tdsJsonData.put(Constant.TDS_RATE, Double.parseDouble(tdsPercent.trim()));
                    }
                }
                if (StringUtils.isNotBlank(allRequestParams.get(Constant.TDS_AMOUNT_NEW))) {
                    tdsJsonData.put(Constant.TDS_AMOUNT, Double.parseDouble((allRequestParams.get(Constant.TDS_AMOUNT_NEW)).trim()));
                    tdsAmount = Double.parseDouble((allRequestParams.get(Constant.TDS_AMOUNT_NEW)).trim());
                }
                if (StringUtils.isNotBlank(tdsType)) {
                    tdsJsonData.put(Constant.TDS_TYPE, tdsType.trim());
                }
                if(StringUtils.isNotBlank(tdsRoundoff)){
                    tdsJsonData.put(Constant.TDS_ROUND_OFF,Integer.parseInt(tdsRoundoff));
                }
                purchaseVo.setTdsJson(tdsJsonData);
            }

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
            if (allRequestParams.get("gstApply") == null) {
                purchaseVo.setGstApply(1);
                //System.err.println("GSTAPPLAYEEEE*************");
                TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(0, Long.parseLong(session.getAttribute("companyId").toString()), taxType, tax_code);
                if (taxVo != null)
                    purchaseVo.getPurchaseItemVos().forEach(p -> {
                        p.setTaxVo(taxVo);
                        p.setTaxRate(0);
                    });
            } else {
                if (type.equals(Constant.PURCHASE_DEBIT_NOTE)
                        && (StringUtils.isNotBlank(allRequestParams.get("gstTaxType")))) {
                    if (allRequestParams.get("gstTaxType").equals("outofscope")) {
                        purchaseVo.setGstApply(1);
                        TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(0, Long.parseLong(session.getAttribute("companyId").toString()), taxType, tax_code);
                        if (taxVo != null)
                            purchaseVo.getPurchaseItemVos().forEach(p -> {
                                p.setTaxVo(taxVo);
                                p.setTaxRate(0);
                            });
                    }
                }
            }
            if (StringUtils.isNotBlank(allRequestParams.get("parentType"))) {
                if (StringUtils.equalsIgnoreCase(Constant.PURCHASE_MATERIALINWARD, allRequestParams.get("parentType"))) {
                    if (purchaseVo.getPurchaseVo() != null) {
                        if (purchaseVo.getPurchaseId() == 0) {
                            purchaseVo.setMaterialInwardIds(purchaseVo.getPurchaseVo().getPurchaseId() + ",");
                            purchaseVo.setPurchaseVo(null);
                        }
                    }
                }
            }

            // add tds amount as paid amount
            purchaseVo.setPaidAmount(tdsAmount);

            PurchaseVo purchaseVo3 = purchaseService.save(purchaseVo);
            if (MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId)) {
                if (type.equals(Constant.PURCHASE_BILL)) {
                    if (purchaseVo.getPurchaseVo() != null && (!Objects.isNull(purchaseVo.getPurchaseVo().getPurchaseId()))) {
                        RILPurchaseBillInfo rilPurchaseBillInfo = rILPurchaseBillInfoRepository.getPurchaseBillInfoByPurchaseId(purchaseVo.getPurchaseVo().getPurchaseId());
                        if (rilPurchaseBillInfo != null) {
                            updateRILPurchaseBillInfo(rilPurchaseBillInfo, purchaseVo3.getPurchaseId());
                        }
                    } else {
                        if (StringUtils.isNotBlank(purchaseVo.getMaterialInwardIds())) {
                            String[] billNo = purchaseVo.getMaterialInwardIds().split(",");
                            for (String billNoStr : billNo) {
                                Long purchaseId = Long.parseLong(billNoStr);
                                // Fetch PurchaseVo based on purchaseId and branchId
                                PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(purchaseId, branchId);
                                if (purchaseVo1 == null || !purchaseVo1.getType().equals(Constant.PURCHASE_MATERIALINWARD) || purchaseVo1.getPurchaseVo() == null) {
                                    continue;  // Skip if the PurchaseVo is not valid or the type doesn't match
                                }
                                // Fetch and update RILPurchaseBillInfo
                                RILPurchaseBillInfo rilPurchaseBillInfo = rILPurchaseBillInfoRepository.getPurchaseBillInfoByPurchaseId(purchaseVo1.getPurchaseVo().getPurchaseId());
                                if (rilPurchaseBillInfo != null) {
                                    updateRILPurchaseBillInfo(rilPurchaseBillInfo, purchaseVo3.getPurchaseId());
                                    break;  // Exit loop after processing the first valid entry
                                }
                            }
                        }
                    }
                }
            }
            CompanySettingVo companySettingVo = companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWATTACHMENTVALIDATION);
            if (companySettingVo.getValue() == 1) {

                if (type.equals(Constant.PURCHASE_ORDER) || type.equals(Constant.PURCHASE_MATERIALINWARD)) {

                    try {
                        String fileName = "";


                        if (!file.isEmpty()) {


                            String fileExtension = "";
                            File fb =
                                    ImageResize.convert(file);
                            Calendar calendar = Calendar.getInstance();
                            fileExtension = GetFileExtension.get(fb);
                            fileName = purchaseVo3.getPurchaseId() + "." +
                                    fileExtension;
                            String filePath = PURCHASE_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + purchaseVo3.getPurchaseId() + "/" + fileName;
                            String uploadStatus = "500";
                            if (FILE_UPLOAD_SERVER.equals(Constant.FILE_UPLOAD_SERVER_AZURE)) {
                                uploadStatus = azureBlobService.sendPurchaseAttachmentFileToAZURE(fb, filePath);
                            } else {
                                uploadStatus = awsService.saveAttachmentToS3(fileExtension, fb, filePath);
                            }
                            if (uploadStatus == "200") {
                                purchaseService.updateAttachmentfile(purchaseVo3.getPurchaseId(), fileName); //
                            }

                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

            }
            //update receive qty
            purchaseService.updatereceiveQty(purchaseVo3);


            try {
                if (purchaseVo3.getType().equals(Constant.PURCHASE_BILL)) {
                    if (purchaseVo.getPurchaseVo() != null) {
                        //means purchase bill generated po status update
                        if (StringUtils.isNotBlank(allRequestParams.get("parentType"))) {
                            if (StringUtils.equalsIgnoreCase(Constant.PURCHASE_ORDER, allRequestParams.get("parentType"))) {
                                purchaseService.updatePurchaseStatus(purchaseVo.getPurchaseVo().getPurchaseId(), "close");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


//        purchaseVo3.getPurchaseItemVos().forEach((p ->
//        {
//            shopifySetupService.updateStock(p.getQty(), p.getProductVarientsVo().getProductVarientId(), Long.parseLong(session.getAttribute("companyId").toString()));
//        }));


            if (purchaseVo.getType().equals(Constant.PURCHASE_BILL) || purchaseVo.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                purchaseService.insertPurchaseTransaction(purchaseVo3, session.getAttribute("financialYear").toString());
            } else if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
                purchaseService.insertPurchaseDebitNote(purchaseVo3, session.getAttribute("financialYear").toString());
            }

            if (purchaseVo3.getType().equals(Constant.PURCHASE_BILL) && isedit == 0) {
                generatedebitnote(purchaseVo3, allRequestParams, session);
            }
            if (purchaseVo3.getType().equals(Constant.PURCHASE_BILL) && isedit == 1) {
                purchaseVo3.setPurchaseReturnItemDTO(purchaseVo.getPurchaseReturnItemDTO());
                updatedebitnote(purchaseVo3, allRequestParams, session);
            }
            if (type.equals(Constant.PURCHASE_BILL)) {

                if (purchaseVo.getMaterialInwardIds() != null) {
                    String[] billNo = purchaseVo.getMaterialInwardIds().split(",");
                    for (int i = 0; i < billNo.length; i++) {
                        // log.severe("billNo["+i+"]---->"+billNo[i]);
                        PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(Long.parseLong(billNo[i]), Long.parseLong(session.getAttribute("branchId").toString()));
                        if (purchaseVo1 != null) {
                            if (purchaseVo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                                double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(Long.parseLong(billNo[i]), Long.parseLong(session.getAttribute("branchId").toString()));
                                double totalbillQty = purchaseService.findByTotalQtyOfPurchases(Long.parseLong(billNo[i]),
                                        Long.parseLong(session.getAttribute("branchId").toString()), Constant.PURCHASE_BILL, "bymultipleinwardids");
                                ////System.err.println("here calll updaet updside   status"+poqty+":::::"+totalbillQty);
                                if (poqty <= totalbillQty) {
                                    //update status of MI
                                    purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(), "completed");
                                    ////System.err.println("here calll updaet status"+poqty+":::::"+totalbillQty);
                                }
                                ////System.err.println("her call111 ::"+purchaseVo3.getPurchaseItemVos().get(0).getPurchaseItemId());
                                ////System.err.println("her call ::"+purchaseVo1.getPurchaseId()+":::"+purchaseVo3.getPurchaseId());
                                purchaseService.updateQtyOfMi(purchaseVo1.getPurchaseId(), purchaseVo3.getPurchaseId());
                                //update status of MI
                                //purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(),"completed");
                                if (purchaseVo1.getPurchaseVo() != null) {
                                    //update status of PO
                                    if (purchaseVo1.getPurchaseVo().getStatus().equals("delivered") || purchaseVo1.getPurchaseVo().getStatus().equals("exceed")) {
                                        List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(purchaseVo1.getPurchaseVo().getPurchaseId());
                                        int update = 0;
                                        for (int j = 0; j < mIVo.size(); j++) {
                                            //System.err.println("staus of mi"+mIVo.get(j).getStatus());
                                            if (mIVo.get(j).getStatus().equals("completed")) {
                                                update = 1;
                                            } else {
                                                update = 0;
                                                break;
                                            }
                                        }
                                        if (update == 1) {
                                            purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseVo().getPurchaseId(), "close");
                                        }
                                    }
                                }
                            } else if (purchaseVo1.getType().equals(Constant.PURCHASE_ORDER)) {
                                purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(), "close");
                            }
                        }

                    }
                } else if (purchaseVo.getPurchaseVo() != null) {
                    try {
                        //means purchase bill generated From Direct Material Inward
                        if (StringUtils.isNotBlank(allRequestParams.get("parentType"))) {
                            if (StringUtils.equalsIgnoreCase(Constant.PURCHASE_MATERIALINWARD, allRequestParams.get("parentType"))) {
                                // log.info("START========parentType is PURCHASE_MATERIALINWARD=========START");
                                long miId = purchaseVo.getPurchaseVo().getPurchaseId();
                                // log.severe("miId---->"+miId);
                                PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(miId, Long.parseLong(session.getAttribute("branchId").toString()));
                                if (purchaseVo1 != null) {
                                    if (purchaseVo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                                        double inwardqty = purchaseService.getTotalQtyByPurchaseAndBranchId(miId, Long.parseLong(session.getAttribute("branchId").toString()));
                                        double totalbillQty = purchaseService.findByTotalQtyOfPurchases(miId, Long.parseLong(session.getAttribute("branchId").toString()),
                                                Constant.PURCHASE_BILL, "byparent");
                                        // log.warning("here calll updaet updside status inwardqty--->"+inwardqty+" totalbillQty---->"+totalbillQty);
                                        if (inwardqty <= totalbillQty) {
                                            //update status of MI
                                            purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(), "completed");
                                            // log.warning("here calll updaet status inwardqty----->"+inwardqty+" totalbillQty-->"+totalbillQty);
                                        }
                                        // log.warning("her call111 ::"+purchaseVo3.getPurchaseItemVos().get(0).getPurchaseItemId());
                                        // log.warning("her call ::"+purchaseVo1.getPurchaseId()+":::"+purchaseVo3.getPurchaseId());
                                        purchaseService.updateQtyOfMi(purchaseVo1.getPurchaseId(), purchaseVo3.getPurchaseId());
                                        //update status of MI
                                        //purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(),"completed");
                                        if (purchaseVo1.getPurchaseVo() != null) {
                                            //update status of PO
                                            if (purchaseVo1.getPurchaseVo().getStatus().equals("delivered") || purchaseVo1.getPurchaseVo().getStatus().equals("exceed")) {
                                                List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(purchaseVo1.getPurchaseVo().getPurchaseId());
                                                int update = 0;
                                                for (int j = 0; j < mIVo.size(); j++) {
                                                    //System.err.println("staus of mi"+mIVo.get(j).getStatus());
                                                    if (mIVo.get(j).getStatus().equals("completed")) {
                                                        update = 1;
                                                    } else {
                                                        update = 0;
                                                        break;
                                                    }
                                                }
                                                if (update == 1) {
                                                    purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseVo().getPurchaseId(), "close");
                                                }
                                            }
                                        }
                                    } else if (purchaseVo1.getType().equals(Constant.PURCHASE_ORDER)) {
                                        purchaseService.updatePurchaseStatus(purchaseVo1.getPurchaseId(), "close");
                                    }
                                }
                                // log.info("START========parentType is PURCHASE_MATERIALINWARD=========START");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // log.severe("ERROR---->"+e.getMessage());
                    }


                }

            }
            //payment accept
            if (allRequestParams.get("saveandpayment") != null && Integer.parseInt(allRequestParams.get("saveandpayment")) == 1) {
                if (allRequestParams.get("typePayment") != null && allRequestParams.get("typePayment").equals("1")) {
//                List<ReceiptVo> receiptVos = receiptService.findByBranchIdAndIsDeletedAndContactVoContactIdAndType(
//                        Long.parseLong(session.getAttribute("branchId").toString()), 0,
//                        salesVo2.getContactVo().getContactId(), Constant.PAYMENT_TYPE_ADVANCE);
                    String advancePaymentBill = allRequestParams.get("advancePaymentBillId");
                    List<PaymentVo> paymentVos = new ArrayList<PaymentVo>();
                    if (advancePaymentBill != null && !advancePaymentBill.equals("")) {
                        List<Long> l = Arrays.asList(advancePaymentBill.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
                        paymentVos = paymentService.findByBranchidandisDeletedAndTypeAndPaymentId(Long.parseLong(session.getAttribute("branchId").toString()), 0, Constant.PAYMENT_TYPE_ADVANCE, l);
                    }

                    double salesTotal = 0.0, totalPayment = 0;
                    salesTotal = purchaseVo3.getTotal() - tdsAmount;
                    for (PaymentVo paymentVo4 : paymentVos) {

                        if (salesTotal != 0) {

                            double receiptbilltotal = paymentVo4.getPaymentBillVos().stream()
                                    .mapToDouble(p -> p.getTotalPayment()).sum();


                            if (receiptbilltotal != paymentVo4.getTotalPayment()) {

                                double receiptAmount = paymentVo4.getTotalPayment() - receiptbilltotal;
                                PaymentBillVo billVo = new PaymentBillVo();
                                if (salesTotal <= receiptAmount) {

                                    billVo.setKasar(0);
                                    billVo.setOldPyament(0);
                                    billVo.setPaymentVo(paymentVo4);
                                    billVo.setVoucherId(purchaseVo3.getPurchaseId());
                                    billVo.setVoucherType(Constant.PURCHASE);
                                    billVo.setTotalPayment(salesTotal);
                                    salesTotal = 0;
                                } else {

                                    billVo.setKasar(0);
                                    billVo.setOldPyament(0);
                                    billVo.setPaymentVo(paymentVo4);
                                    billVo.setVoucherType(Constant.PURCHASE);
                                    billVo.setVoucherId(purchaseVo3.getPurchaseId());
                                    billVo.setTotalPayment(receiptAmount);
                                    salesTotal -= receiptAmount;
                                }

                                totalPayment += billVo.getTotalPayment();

                                paymentService.saveBill(billVo);
                                paymentService.transation(paymentVo4, 0);
                            }

                        }
                    }
                    //salesService.updatePaidAmountPlus(salesVo2.getSalesId(), totalPayment);
                    purchaseService.updatePaidAmountPlus(purchaseVo3.getPurchaseId(), totalPayment);

                }

                if (!allRequestParams.get("totalPayment").equals("0")) {

                    PaymentVo paymentVo = new PaymentVo();

                    paymentVo.setPaymentNo(paymentService.getNewPaymentNo(Constant.PAYMENT,
                            Long.parseLong(session.getAttribute("branchId").toString()),
                            Long.parseLong(session.getAttribute("userId").toString()), "PAY", session));
                    paymentVo.setPrefix(prefixService
                            .getPrefixByPrefixTypeAndBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
                                    Constant.PAYMENT, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())));
                    paymentVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
                    paymentVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
                    paymentVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
                    paymentVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
                    paymentVo.setCreatedOn(CurrentDateTime.getCurrentDate());
                    paymentVo.setModifiedOn(CurrentDateTime.getCurrentDate());
                    paymentVo.setAmount(Double.parseDouble(allRequestParams.get("totalPayment")));
                    paymentVo.setPaymentDate(purchaseVo3.getPurchaseDate());
                    paymentVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                    paymentVo.setPaymentMode("Cash");
                    paymentVo.setType(Constant.PAYMENT_TYPE_AGAINSTBILL);
                    long accountCustomId = contactService.findAccountCustomIdByContactId(purchaseVo3.getContactVo().getContactId());
                    AccountCustomVo accountCustomVo = new AccountCustomVo();
                    accountCustomVo.setAccountCustomId(accountCustomId);
                    paymentVo.setPartyAccountVo(accountCustomVo);
//                paymentVo.setContactVo(purchaseVo3.getContactVo());
                    // DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    try {
                        paymentVo.setDescription(allRequestParams.get("description"));
                    } catch (Exception e) {
                        // TODO: handle exception

                    }

                    if (allRequestParams.get("paymentMode").equals("bank")) {

                        BankVo bankVo = new BankVo();
                        bankVo.setBankId(Long.parseLong(allRequestParams.get("bankVoId")));
                        paymentVo.setBankVo(bankVo);
                        paymentVo.setPaymentMode("bank");
                        paymentVo.setAccountNo(allRequestParams.get("accountNo"));
                        paymentVo.setBankTransactionType(allRequestParams.get("bankpaymentmode"));
                        paymentVo.setStatus("cleared");
                        paymentVo.setAcceptedBankReceipt(1);
                        try {
                            paymentVo.setChequeDate(dateFormat.parse(allRequestParams.get("chequeDate")));
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {

                        if (allRequestParams.get("paymentMode").equalsIgnoreCase("cash")) {
                            paymentVo.setStatus("cleared");
                        }

                        AccountCustomVo cashaccountCustomVo = new AccountCustomVo();
                        cashaccountCustomVo.setAccountCustomId(Long.parseLong(allRequestParams.get("accountCustomVo.accountCustomId")));
                        paymentVo.setCashAccountCustomVo(cashaccountCustomVo);
                        try {
                            paymentVo.setChequeDate(paymentVo.getPaymentDate());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    PaymentBillVo paymentBillVo = new PaymentBillVo();
                    List<PaymentBillVo> paymentBillVos = new ArrayList<>();
                    if (StringUtils.isNotBlank(allRequestParams.get("Kasar"))) {
                        paymentBillVo.setKasar(Double.parseDouble(allRequestParams.get("Kasar")));
                    } else {
                        paymentBillVo.setKasar(0);
                    }
                    paymentBillVo.setOldPyament(0);
                    paymentBillVo.setPaymentVo(paymentVo);
                    paymentBillVo.setVoucherId(purchaseVo3.getPurchaseId());
                    paymentBillVo.setVoucherType(Constant.PURCHASE);
                    paymentBillVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
                    paymentBillVos.add(paymentBillVo);
                    paymentVo.setPaymentBillVos(paymentBillVos);
                    PaymentVo paymentVo2 = paymentService.save(paymentVo);

                    paymentService.transation(paymentVo2, paymentBillVo.getKasar());

                    purchaseService.updatePaidAmountPlus(purchaseVo3.getPurchaseId(), Double.parseDouble(allRequestParams.get("totalPayment")) + paymentBillVo.getKasar());
                }
            }
            // end

            //supplier bill update status
            purchaseService.updatestatusbypurchaseID(purchaseVo3.getPurchaseId(), Constant.PURCHASE_BILL);
            if (type.equals(Constant.PURCHASE_BILL)) {
                purchaseService.updatecategoryAndbrand(purchaseVo3);
            }
            // Send Message
            sendMessageToCutomer(purchaseVo3, session);
            CompanySettingVo settingVo = companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWPURCHASEORDERSMS);

            if (type.equals(Constant.PURCHASE_ORDER) && idd == 0 && settingVo != null && settingVo.getValue() == 1) {

                if (purchaseVo3.getPdfToken() == null) {
                    String pdfToken = EncryptMessage.getSecureMessage(purchaseVo3.getPurchaseId()
                            + purchaseVo3.getBillingCompanyName() + CurrentDateTime.getCurrentDate());

                    // log.warning("Pdf Token : "+pdfToken);
//				salesService.updateToken(purchaseVo3.getPurchaseId(), pdfToken);
                    purchaseService.updateToken(purchaseVo3.getPurchaseId(), pdfToken);
                    purchaseVo3.setPdfToken(pdfToken);
                }
                //sendMessageToSupplier(purchaseVo3, session);
            }

            //case for new or edit
            if (idd == 0) {
                //System.err.println("HERE StocktransferId is :"+purchaseVo3.getStockTransferId());
                if (purchaseVo3.getStockTransferId() != 0) {
                    stockTransferService.updatestatusDateBystocktransferId(purchaseVo3.getStockTransferId(), CurrentDateTime.getCurrentDate());
                    stockTransferService.updatestatusBystocktransferId(purchaseVo3.getStockTransferId(), Constant.STOCK_TRANSFER_APPROVE);
                    if (salesService.findByStockTransferId(purchaseVo3.getStockTransferId(), Long.parseLong(session.getAttribute("companyId").toString()),Constant.SALES_INVOICE) == null) {
                        List<StockTransferItemVo> stockTransferItemVos = stockTransferService.findBystocktransferId(purchaseVo3.getStockTransferId());
                        if (stockTransferItemVos.size() > 0) {
                            for (int i = 0; i < purchaseVo3.getPurchaseItemVos().size(); i++) {
                                for (int j = 0; j < stockTransferItemVos.size(); j++) {
                                    if (purchaseVo3.getPurchaseItemVos().size() > 0) {
                                        //   //System.err.println("productvarient check"+purchaseVo3.getPurchaseItemVos().get(i).getProductVarientsVo().getProductVarientId());
                                        //  //System.err.println("stocktransfer productvarient check"+stockTransferItemVos.get(j).getProductVarientsVo().getProductVarientId());
                                        if (purchaseVo3.getPurchaseItemVos().get(i).getProductVarientsVo().getProductVarientId() == stockTransferItemVos.get(j).getProductVarientsVo().getProductVarientId()) {
                                            purchaseVo3.getPurchaseItemVos().get(i).setTempBatchId(stockTransferItemVos.get(j).getBatchId());
                                        }
                                    } else {
                                        //System.err.println("purchase item nulll---");
                                    }
                                }
                            }
                        }
                        saveSalesForStockTransferInCompany(purchaseVo3, session, request);
                        notificationService.createStocktransfernotificationfranchise(stockTransferVo, stockApproveRejectFromBranchId, stockApproveRejectFromBranchName, session);
                    }
                }
            }

            VasyMessageSettingVo messageSettingVo = vasyMessageSettingService
                    .getVasyMessageSettingPurchaseTypeWise(purchaseVo3, idd);
            if (messageSettingVo != null && messageSettingVo.getIsActive() == 1) {
                if ((purchaseVo3.getContactVo() != null)
                        && ((StringUtils.isNotBlank(purchaseVo3.getContactVo().getWhatsappNo())
                        || StringUtils.isNotBlank(purchaseVo3.getContactVo().getMobNo())))) {
                    if (type.equals(Constant.PURCHASE_ORDER)) {
                        globalMessageService.sendMerchantToCustomerPurchaseOrderMessage(purchaseVo3,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                request.getServletContext().getRealPath("/"), session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(), purchaseVo3.getContactVo().getWhatsappNo(),
                                purchaseVo3.getContactVo().getMobNo(),
                                (idd == 0) ? MessageConstant.MERCHANT_EVENT_PURCHASE_NEW_ORDER
                                        : MessageConstant.MERCHANT_EVENT_PURCHASE_EDIT_ORDER);
                    } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                        globalMessageService.sendMerchantToCustomerMaterialInwardMessage(purchaseVo3,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                request.getServletContext().getRealPath("/"), session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(), purchaseVo3.getContactVo().getWhatsappNo(),
                                purchaseVo3.getContactVo().getMobNo(),
                                (idd == 0) ? MessageConstant.MERCHANT_EVENT_PURCHASE_NEW_MATERIAL_INWARD
                                        : MessageConstant.MERCHANT_EVENT_PURCHASE_EDIT_MATERIAL_INWARD);
                    } else if (type.equals(Constant.PURCHASE_BILL)) {
                        globalMessageService.sendMerchantToCustomerPurchaseBillMessage(purchaseVo3,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                request.getServletContext().getRealPath("/"), session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(), purchaseVo3.getContactVo().getWhatsappNo(),
                                purchaseVo3.getContactVo().getMobNo(),
                                (idd == 0) ? MessageConstant.MERCHANT_EVENT_PURCHASE_NEW_BILL
                                        : MessageConstant.MERCHANT_EVENT_PURCHASE_EDIT_BILL);
                    } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                        globalMessageService.sendMerchantToCustomerDebitNoteMessage(purchaseVo3,
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                Long.parseLong(session.getAttribute("userId").toString()),
                                request.getServletContext().getRealPath("/"), session.getAttribute("realPath").toString(),
                                session.getAttribute("currencyCode").toString(),
                                session.getAttribute("currencyName").toString(),
                                session.getAttribute("decimalPoint").toString(), purchaseVo3.getContactVo().getWhatsappNo(),
                                purchaseVo3.getContactVo().getMobNo(),
                                (idd == 0) ? MessageConstant.MERCHANT_EVENT_PURCHASE_NEW_DEBIT_NOTE
                                        : MessageConstant.MERCHANT_EVENT_PURCHASE_EDIT_DEBIT_NOTE);
                    }
                }
            } else {
                // new Purchase Order send PDF ON mail And Whatsapp
                if (type.equals(Constant.PURCHASE_ORDER)) {
                    try {
                        sendOrderPdfToSuppliers(purchaseVo3, session, request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (isedit == 1) {

                SystemActivityLogVo systemActivityLogVo = new SystemActivityLogVo();
                systemActivityLogVo.setBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
                systemActivityLogVo.setType(type);
                systemActivityLogVo.setFromyear(CurrentDateTime.getFinancialYearInterval(financialService.findByMonthInterval(session.getAttribute("monthInterval").toString())));
                systemActivityLogVo.setCreatedOn(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(CurrentDateTime.getCurrentDate()));
                systemActivityLogVo.setCompletedOn(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(CurrentDateTime.getCurrentDate()));
                systemActivityLogVo.setStatus(Constant.SUCCESS);
                systemActivityLogVo.setCompanyId(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                systemActivityLogVo.setDescription("Purchase "+type+"  "+purchaseVo.getPrefix()+purchaseVo.getPurchaseNo()+" Edited");
                systemActivityLogRepository.save(systemActivityLogVo);
            }

            if (allRequestParams.get("saveandnew") != null && Integer.parseInt(allRequestParams.get("saveandnew")) == 1) {
                return "redirect:/purchase/" + type + "/new";
            } else if (allRequestParams.get("saveandprint") != null && Integer.parseInt(allRequestParams.get("saveandprint")) == 1) {
                if (type.equals(Constant.PURCHASE_BILL)) {
                    return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId() + "?print=true";
                }
                return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId() + "?print=true";
            } else {
                if (type.equals(Constant.PURCHASE_BILL)) {
                    return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId();
                }
                return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId();
            }
        }

    }

    @RequestMapping("/{id}/status/{status}")
	public ModelAndView purchaseRequestStatus(HttpSession session, @PathVariable(value = "id") long id,
			@PathVariable(value = "type") String type, @PathVariable(value = "status") String status,
			@RequestParam Map<String, String> allRequestParams) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REQUEST_STATUS;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_REQUEST_STATUS;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_REQUEST_STATUS;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_REQUEST_STATUS;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REQUEST_STATUS;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		ModelAndView view = new ModelAndView();
		if (MenuPermission.havePermission(session, type, Constant.APPROVE_REJECT_ACTION) == 1) {

			int response = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(id,
	                Long.parseLong(session.getAttribute("branchId").toString()), 0);
	        if (response == 0) {
	        	 view.setViewName(Constant.ERROR_PAGE_404);
	        } else {
	        	purchaseService.updatePurchaseStatus(id, status);
	        	view.setViewName("redirect:/purchase/" + type + "/" + id);
	        }
	    } else {
	        view.setViewName(Constant.ACCESSDENIED);
	    }

		return view;
	}

//    @PostMapping("/create")
//    public String insertPurchase(@RequestParam Map<String, String> allRequestParams,@PathVariable(value = "type") String type,
//    								@ModelAttribute("purchaseVo") PurchaseVo purchaseVo,HttpSession session, HttpServletRequest request) throws IOException {
//    	long idd = purchaseVo.getPurchaseId();
//        ContactAddressVo contactAddressVo;
//        DecimalFormat df2 = new DecimalFormat("#.##");
//        double miqty;
//        String postatus = "";
//        purchaseVo.setType(type);
//        int isedit=1;
//        if(purchaseVo.getPurchaseId()==0) {
//        	isedit=0;
//        	if (type.equals(Constant.PURCHASE_BILL)) {
//            	purchaseVo.setStatus("due");
//            } else if (type.equals(Constant.PURCHASE_ORDER)) {
//            	purchaseVo.setStatus(Constant.DRAFT);
//            }
//        }
//
//        if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
//        	purchaseVo.setStatus("open");
//        } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
//        	purchaseVo.setStatus("open");
//        	//System.err.println("HERE type is :"+Constant.PURCHASE_MATERIALINWARD);
//        	long poId = purchaseVo.getPurchaseVo().getPurchaseId();
//        	//System.err.println("HERE purchase order id is :"+poId);
//
//        	double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
//        	//System.err.println("HERE poqty is :"+poqty);
//        	if (purchaseVo.getPurchaseItemVos() != null) {
//                miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//                //System.err.println("HERE miqty is :"+miqty);
//                if(miqty == poqty) {
//                	postatus = "delivered";
//                }else if (miqty < poqty) {
//                	postatus = "partiallydelivered";
//				}else if (miqty > poqty) {
//                	postatus = "exceed";
//				}
//                //System.err.println("HERE postatus is :"+postatus);
//                purchaseService.updatePurchaseStatus(poId, postatus);
//
//                /*.mapToInt(Integer::intValue)
//                .sum();*/
//            }else {
//            	purchaseVo.setStatus("mi created");
//            }
//
//        }
//
//        CompanySettingVo quantityupdatebymi = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.QUANTITYUPDATEBYMI);
//
//        //System.err.println("quantityupdatebymi"+quantityupdatebymi.getValue());
//        if(type.equals(Constant.PURCHASE_BILL)) {
//        	if(quantityupdatebymi.getValue()==1) { //stock update to MI no need to update receive qty
//        		if(purchaseVo.getPurchaseVo()!= null) {
//	        		long poId = purchaseVo.getPurchaseVo().getPurchaseId();
//	        		PurchaseVo purchasevo1 = purchaseService.findByPurchaseIdAndBranchId(poId,Long.parseLong(session.getAttribute("branchId").toString())) ;
//	        		//System.err.println("poId"+poId);
//	        		////System.err.println("vooooo"+purchasevo1);
//	        		if(purchasevo1 != null && purchasevo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
//
//	            		double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
//	            		miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//	            		if(miqty <=poqty) {
//	            			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
//	            		}else {
//	            			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
//	            		}
//	            		purchaseService.updatePurchaseStatus(poId, "completed");
//	            		purchaseService.updatePurchaseStatus(purchasevo1.getPurchaseVo().getPurchaseId(), "close");
//	        		}
//        		}
//        	}else {
//        		if(purchaseVo.getPurchaseVo()!= null) {
//	        		long poId = purchaseVo.getPurchaseVo().getPurchaseId();
//	        		PurchaseVo purchasevo1 = purchaseService.findByPurchaseIdAndBranchId(poId,Long.parseLong(session.getAttribute("branchId").toString())) ;
//	        		if(purchasevo1 != null && purchasevo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
//
//		        		double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
//		        		miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//		        		if(miqty <=poqty) {
//		        			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
//		        		}else {
//		        			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
//		        		}
//		        		purchaseService.updatePurchaseStatus(poId, "completed");
//	            		purchaseService.updatePurchaseStatus(purchasevo1.getPurchaseVo().getPurchaseId(), "close");
//	        		}
//        		}
//        	}
//        }else if(type.equals(Constant.PURCHASE_MATERIALINWARD)) {
//        	if(quantityupdatebymi.getValue()==1) {
//        		long poId = purchaseVo.getPurchaseVo().getPurchaseId();
//        		double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
//        		miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//        		if(miqty <=poqty) {
//        			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
//        		}else {
//        			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
//        		}
//        	}
//        }
//        double purchaseTotalTaxAmount = 0.0;
//        try {
//        	if (purchaseVo.getPurchaseItemVos() != null) {
//        		purchaseTotalTaxAmount = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getTaxAmount()).sum();
//        	}else {
//
//        	}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        purchaseVo.setPurchaseTotalTaxAmount(purchaseTotalTaxAmount);
//
//        purchaseVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
//        purchaseVo.setModifiedOn(CurrentDateTime.getCurrentDate());
//        purchaseVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
//        purchaseVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
//
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
//        	purchaseService.updateDebitNoteAmount(purchaseVo.getPurchaseVo().getPurchaseId(),purchaseVo.getPurchaseId(),purchaseVo.getTotal());
//        }
//        try {
//            purchaseVo.setPurchaseDate(dateFormat.parse(allRequestParams.get("purchaseDate")));
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        try {
//            if (!purchaseVo.getTermsAndConditionIds().equals("")) {
//                purchaseVo.setTermsAndConditionIds(
//                        purchaseVo.getTermsAndConditionIds().substring(0, purchaseVo.getTermsAndConditionIds().length() - 1));
//            } else {
//
//            }
//        } catch (Exception e) {
//        }
//        if (type.equals(Constant.PURCHASE_BILL) ) {
//            try {
//                purchaseVo.setDueDate(dateFormat.parse(allRequestParams.get("dueDate")));
//            } catch (ParseException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        if (!type.equals(Constant.PURCHASE_DEBIT_NOTE) && !type.equals(Constant.PURCHASE_MATERIALINWARD)) {
//            try {
//                purchaseVo.setDateOfSupply(dateFormat.parse(allRequestParams.get("dateOfSupply")));
//            } catch (ParseException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        PurchaseVo purchaseVo2 = null;
//
//        if (allRequestParams.get("billingAddressId").equals("0")
//                || allRequestParams.get("shippingAddressId").equals("0")) {
//            purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(purchaseVo.getPurchaseId(),
//                    purchaseVo.getBranchId());
//        }
//
//        try {
//        	// --------------Set Billing Address Details ------------------------
//            if (!allRequestParams.get("billingAddressId").equals("0")) {
//                purchaseVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
//                purchaseVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
//                purchaseVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
//                purchaseVo.setBillingCompanyName(allRequestParams.get("billingcompanyname"));
//                purchaseVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
//                purchaseVo.setBillingFirstName(allRequestParams.get("billingfirstname"));
//                purchaseVo.setBillingLastName(allRequestParams.get("billinglastname"));
//                purchaseVo.setBillingPinCode(allRequestParams.get("billingpincode"));
//                purchaseVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
//            } else if (purchaseVo2 != null) {
//            	try {
//            		purchaseVo.setBillingAddressLine1(allRequestParams.get("billingaddressline1"));
//                    purchaseVo.setBillingAddressLine2(allRequestParams.get("billingaddressline2"));
//                    purchaseVo.setBillingCityCode(allRequestParams.get("billingcitycode"));
//                    purchaseVo.setBillingCompanyName(allRequestParams.get("billingcompanyname"));
//                    purchaseVo.setBillingCountriesCode(allRequestParams.get("billingcountrycode"));
//                    purchaseVo.setBillingFirstName(allRequestParams.get("billingfirstname"));
//                    purchaseVo.setBillingLastName(allRequestParams.get("billinglastname"));
//                    purchaseVo.setBillingPinCode(allRequestParams.get("billingpincode"));
//                    purchaseVo.setBillingStateCode(allRequestParams.get("billingstatecode"));
//				} catch (Exception e) {
//					e.printStackTrace();
//					purchaseVo.setBillingAddressLine1(purchaseVo2.getBillingAddressLine1());
//	                purchaseVo.setBillingAddressLine2(purchaseVo2.getBillingAddressLine2());
//	                purchaseVo.setBillingCityCode(purchaseVo2.getBillingCityCode());
//	                purchaseVo.setBillingCompanyName(purchaseVo2.getBillingCompanyName());
//	                purchaseVo.setBillingCountriesCode(purchaseVo2.getBillingCountriesCode());
//	                purchaseVo.setBillingFirstName(purchaseVo2.getBillingFirstName());
//	                purchaseVo.setBillingLastName(purchaseVo2.getBillingLastName());
//	                purchaseVo.setBillingPinCode(purchaseVo2.getBillingPinCode());
//	                purchaseVo.setBillingStateCode(purchaseVo2.getBillingStateCode());
//				}
//
//            }
//
//            // --------------Set Shipping Address Details ------------------------
//            if (!allRequestParams.get("shippingAddressId").equals("0")) {
//                contactAddressVo = contactService
//                        .findByContactAddressId(Long.parseLong(allRequestParams.get("shippingAddressId")));
//
//                purchaseVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
//                purchaseVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
//                purchaseVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
//                purchaseVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
//                purchaseVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
//                purchaseVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
//                purchaseVo.setShippingLastName(allRequestParams.get("shippinglastname"));
//                purchaseVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
//                purchaseVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
//            } else if (purchaseVo2 != null) {
//            	try {
//            		purchaseVo.setShippingAddressLine1(allRequestParams.get("shippingaddressline1"));
//                    purchaseVo.setShippingAddressLine2(allRequestParams.get("shippingaddressline2"));
//                    purchaseVo.setShippingCityCode(allRequestParams.get("shippingcitycode"));
//                    purchaseVo.setShippingCompanyName(allRequestParams.get("shippingcompanyname"));
//                    purchaseVo.setShippingCountriesCode(allRequestParams.get("shippingcountrycode"));
//                    purchaseVo.setShippingFirstName(allRequestParams.get("shippingfirstname"));
//                    purchaseVo.setShippingLastName(allRequestParams.get("shippinglastname"));
//                    purchaseVo.setShippingPinCode(allRequestParams.get("shippingpincode"));
//                    purchaseVo.setShippingStateCode(allRequestParams.get("shippingstatecode"));
//				} catch (Exception e) {
//					e.printStackTrace();
//					purchaseVo.setShippingAddressLine1(purchaseVo2.getShippingAddressLine1());
//	                purchaseVo.setShippingAddressLine2(purchaseVo2.getShippingAddressLine2());
//	                purchaseVo.setShippingCityCode(purchaseVo2.getShippingCityCode());
//	                purchaseVo.setShippingCompanyName(purchaseVo2.getShippingCompanyName());
//	                purchaseVo.setShippingCountriesCode(purchaseVo2.getShippingCountriesCode());
//	                purchaseVo.setShippingFirstName(purchaseVo2.getShippingFirstName());
//	                purchaseVo.setShippingLastName(purchaseVo2.getShippingLastName());
//	                purchaseVo.setShippingPinCode(purchaseVo2.getShippingPinCode());
//	                purchaseVo.setShippingStateCode(purchaseVo2.getShippingStateCode());
//				}
//            }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        try {
//        	////System.err.println("updateaddress is :"+allRequestParams.get("updateaddress"));
//        	if (StringUtils.isNotBlank(allRequestParams.get("updateaddress"))) {
//        		//to update address
//        		contactAddressVo = contactService
//                      .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));
//        		contactAddressVo.setAddressLine1(allRequestParams.get("billingaddressline1"));
//        		contactAddressVo.setAddressLine2(allRequestParams.get("billingaddressline2"));
//            	contactAddressVo.setCompanyName(allRequestParams.get("billingcompanyname"));
//            	contactAddressVo.setPhoneNo(allRequestParams.get("billingphone"));
//            	contactAddressVo.setCountriesCode(allRequestParams.get("billingcountrycode"));
//            	contactAddressVo.setStateCode(allRequestParams.get("billingstatecode"));
//            	contactAddressVo.setCityCode(allRequestParams.get("billingcitycode"));
//            	contactAddressVo.setPinCode(allRequestParams.get("billingpincode"));
//            	//contactAddressVo.getContact()
//            	//contactAddressVo.setContact(contact);
//            	contactService.saveAddress(contactAddressVo);
//                //System.err.println("updateaddress*************"+allRequestParams.get("updateaddress"));
//        	}else {
//
//        	}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
////        // --------------Set Billing Address Details ------------------------
////        if (!allRequestParams.get("billingAddressId").equals("0")) {
////            contactAddressVo = contactService
////                    .findByContactAddressId(Long.parseLong(allRequestParams.get("billingAddressId")));
////
////            purchaseVo.setBillingAddressLine1(contactAddressVo.getAddressLine1());
////            purchaseVo.setBillingAddressLine2(contactAddressVo.getAddressLine2());
////            purchaseVo.setBillingCityCode(contactAddressVo.getCityCode());
////            purchaseVo.setBillingCompanyName(contactAddressVo.getCompanyName());
////            purchaseVo.setBillingCountriesCode(contactAddressVo.getCountriesCode());
////            purchaseVo.setBillingFirstName(contactAddressVo.getFirstName());
////            purchaseVo.setBillingLastName(contactAddressVo.getLastName());
////            purchaseVo.setBillingPinCode(contactAddressVo.getPinCode());
////            purchaseVo.setBillingStateCode(contactAddressVo.getStateCode());
////        } else if (purchaseVo2 != null) {
////            purchaseVo.setBillingAddressLine1(purchaseVo2.getBillingAddressLine1());
////            purchaseVo.setBillingAddressLine2(purchaseVo2.getBillingAddressLine2());
////            purchaseVo.setBillingCityCode(purchaseVo2.getBillingCityCode());
////            purchaseVo.setBillingCompanyName(purchaseVo2.getBillingCompanyName());
////            purchaseVo.setBillingCountriesCode(purchaseVo2.getBillingCountriesCode());
////            purchaseVo.setBillingFirstName(purchaseVo2.getBillingFirstName());
////            purchaseVo.setBillingLastName(purchaseVo2.getBillingLastName());
////            purchaseVo.setBillingPinCode(purchaseVo2.getBillingPinCode());
////            purchaseVo.setBillingStateCode(purchaseVo2.getBillingStateCode());
////        }
////
////        // --------------Set Shipping Address Details ------------------------
////        if (!allRequestParams.get("shippingAddressId").equals("0")) {
////            contactAddressVo = contactService
////                    .findByContactAddressId(Long.parseLong(allRequestParams.get("shippingAddressId")));
////
////            purchaseVo.setShippingAddressLine1(contactAddressVo.getAddressLine1());
////            purchaseVo.setShippingAddressLine2(contactAddressVo.getAddressLine2());
////            purchaseVo.setShippingCityCode(contactAddressVo.getCityCode());
////            purchaseVo.setShippingCompanyName(contactAddressVo.getCompanyName());
////            purchaseVo.setShippingCountriesCode(contactAddressVo.getCountriesCode());
////            purchaseVo.setShippingFirstName(contactAddressVo.getFirstName());
////            purchaseVo.setShippingLastName(contactAddressVo.getLastName());
////            purchaseVo.setShippingPinCode(contactAddressVo.getPinCode());
////            purchaseVo.setShippingStateCode(contactAddressVo.getStateCode());
////        } else if (purchaseVo2 != null) {
////            purchaseVo.setShippingAddressLine1(purchaseVo2.getShippingAddressLine1());
////            purchaseVo.setShippingAddressLine2(purchaseVo2.getShippingAddressLine2());
////            purchaseVo.setShippingCityCode(purchaseVo2.getShippingCityCode());
////            purchaseVo.setShippingCompanyName(purchaseVo2.getShippingCompanyName());
////            purchaseVo.setShippingCountriesCode(purchaseVo2.getShippingCountriesCode());
////            purchaseVo.setShippingFirstName(purchaseVo2.getShippingFirstName());
////            purchaseVo.setShippingLastName(purchaseVo2.getShippingLastName());
////            purchaseVo.setShippingPinCode(purchaseVo2.getShippingPinCode());
////            purchaseVo.setShippingStateCode(purchaseVo2.getShippingStateCode());
////        }
//
//        if (purchaseVo.getPurchaseId() == 0) {
//            purchaseVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
//            purchaseVo.setCreatedOn(CurrentDateTime.getCurrentDate());
//            purchaseVo.setPaidAmount(0.0);
//        }
//        if (purchaseVo.getPurchaseAdditionalChargeVos() != null) {
//            purchaseVo.getPurchaseAdditionalChargeVos().removeIf(rm -> rm.getAdditionalChargeVo() == null);
//            purchaseVo.getPurchaseAdditionalChargeVos().forEach(item1 -> item1.setPurchaseVo(purchaseVo));
//        }
//
//        if (purchaseVo.getPurchaseItemVos() != null) {
//            purchaseVo.getPurchaseItemVos().removeIf(rm -> rm.getProduct() == null);
//            purchaseVo.getPurchaseItemVos().forEach(item -> item.setPurchaseVo(purchaseVo));
//
//        }
//
////        //System.err.println("xxxxxxxxxxxxdto size---"+purchaseVo.getPurchaseReturnItemDTO().size());
//
//        if (allRequestParams.get("deletePurchaseItemIds") != null
//                && !allRequestParams.get("deletePurchaseItemIds").equals("")) {
//            String address = allRequestParams.get("deletePurchaseItemIds").substring(0,
//                    allRequestParams.get("deletePurchaseItemIds").length() - 1);
//            List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
//
//            purchaseService.deletePurchaseItem(l);
//        }
//
//        if (allRequestParams.get("deleteAdditionalChargeIds") != null
//                && !allRequestParams.get("deleteAdditionalChargeIds").equals("")) {
//
//            String address = allRequestParams.get("deleteAdditionalChargeIds").substring(0,
//                    allRequestParams.get("deleteAdditionalChargeIds").length() - 1);
//            List<Long> l = Arrays.asList(address.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
//
//            purchaseService.deletePurchaseAdditionalItem(l);
//        }
//        ////////////////////////
//        if (allRequestParams.get("addproductby") != null)
//            if (allRequestParams.get("addproductby").equals("2")) {
//                double mainTotal = 0.0;
//
//                List<PurchaseItemVo> itemVos = new ArrayList<>();
//                String filepath = (String) session.getAttribute("filepath");
//                File fb = new File(filepath);
//                InputStream in = new FileInputStream(fb);
//
//                // Create Workbook instance holding reference to .xlsx file
//                XSSFWorkbook workbook = new XSSFWorkbook(in);
//                // Get first/desired sheet from the workbook
//                XSSFSheet sheet = workbook.getSheetAt(0);
//
//                // Iterate through each rows one by one
//                Iterator<Row> rowIterator = sheet.iterator();
//                rowIterator.next();
//                while (rowIterator.hasNext()) {
//                    Row row = rowIterator.next();
//
//                    // For each row, iterate through all the columns
//                    Iterator<Cell> cellIterator = row.cellIterator();
//
//                    while (cellIterator.hasNext()) {
//                        Cell cell = cellIterator.next();
//                        // Check the cell type and format accordingly
//                        cell.setCellType(Cell.CELL_TYPE_STRING);
//                        switch (cell.getCellType()) {
//                            case Cell.CELL_TYPE_BOOLEAN:
//
//                                System.out.println("boolean===>>>" + cell.getBooleanCellValue() + "\t");
//                                break;
//                            case Cell.CELL_TYPE_NUMERIC:
//
//                                break;
//                            case Cell.CELL_TYPE_STRING:
//
//                                // list.add(cell.getStringCellValue().trim());
//                                break;
//
//                        }
//
//                    }
//                    ////////
//                    double qty, totalQty = 0;
//                    double rate;
//                    double discount;
//                    double taxRate = 0.0;
//                    double taxableAmount, taxAmount = 0.0, totalTaxAmount = 0.0, taxableValue = 0.0;
//                    double total = 0.0, totalAmount = 0.0;
//                    double discountType, totalDiscount = 0.0;
//                    /////////
//
//                    System.out.println("Row No= " + (row.getRowNum() + 1));
//                    DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
//
//                    PurchaseItemVo purchaseItemVo = new PurchaseItemVo();
//                    System.out.println("BARCODE_____-----" + row.getCell(0).getStringCellValue().trim());
//                    ProductVarientsVo productVarientsVo = productService
//                            .findByitemCodeIgnoreCaseAndCompanyIdAndIsDeleted(
//                                    row.getCell(0).getStringCellValue().trim(),
//                                    Long.parseLong(session.getAttribute("companyId").toString()), 0);
//                    if(productVarientsVo!=null) {
//                    purchaseItemVo.setProductVarientsVo(productVarientsVo);
//
//                    purchaseItemVo.setProduct(productVarientsVo.getProductVo());
//                    purchaseItemVo.setTaxVo(productVarientsVo.getProductVo().getPurchaseTaxVo());
//                    purchaseItemVo.setTaxRate(productVarientsVo.getProductVo().getPurchaseTaxVo().getTaxRate());
//                    //	if (row.getCell(5) != null && row.getCell(5).getStringCellValue().trim() != "") {
//                    //		try {
//                    //			purchaseItemVo.setDiscount(Double.parseDouble(row.getCell(5).getStringCellValue().trim()));
//                    //		} catch (Exception e) {
//                    purchaseItemVo.setDiscount(0);
//                    //		}
//
//                    //	} else {
//                    purchaseItemVo.setDiscount(0);
//                    //	}
//                    // emne pu
////					if (row.getCell(4) != null && row.getCell(4).getStringCellValue().trim() != "") {
////						try {
////							purchaseItemVo.setDiscountType(row.getCell(4).getStringCellValue().trim().toLowerCase());
////						} catch (Exception e) {
//                    purchaseItemVo.setDiscountType("percentage");
//                    //	}
//
//                    //	} else {
//                    //		purchaseItemVo.setDiscountType("percentage");
//                    //	}
//                    // emne pu
////					if (row.getCell(7) != null && row.getCell(7).getStringCellValue().trim() != "") {
////						try {
////							purchaseItemVo.setDiscountType2(row.getCell(7).getStringCellValue().trim().toLowerCase());
////						} catch (Exception e) {
////							purchaseItemVo.setDiscountType2("percentage");
////						}
////
////					} else {
//                    purchaseItemVo.setDiscountType2("percentage");
//                    //	}
////					if (row.getCell(8) != null && row.getCell(8).getStringCellValue().trim() != "") {
////						try {
////							purchaseItemVo.setDiscount2(Double.parseDouble(row.getCell(8).getStringCellValue().trim()));
////						} catch (Exception e) {
////							purchaseItemVo.setDiscount2(0);
////						}
////
////					} else {
//                    purchaseItemVo.setDiscount2(0);
//                    //}
//
////					if (row.getCell(6) != null && row.getCell(6).getStringCellValue().trim() != "") {
////						purchaseItemVo.setProductDescription("");
////					} else {
////						try {
////							purchaseItemVo.setProductDescription(row.getCell(6).getStringCellValue().trim());
////						} catch (Exception e) {
////							purchaseItemVo.setProductDescription("");
////						}
////					}
//
//                    try {
//                        purchaseItemVo.setQty(Float.parseFloat(row.getCell(1).getStringCellValue().trim()));
//                    } catch (Exception e) {
//                        purchaseItemVo.setQty(0);
//                    }
//
//                    try {
//                        purchaseItemVo.setMrp(round(Double.parseDouble(row.getCell(3).getStringCellValue().trim()), 2));
//                    } catch (Exception e) {
//                        purchaseItemVo.setMrp(0);
//                    }
//
//                    try {
//                    	int taxincluded = productVarientsVo.getProductVo().getPurchaseTaxIncluded();
//                    	if (taxincluded == 1) {
//                    		//System.err.println("----------------------------------------------");
//                    		//System.err.println("---: tax including :"+productVarientsVo.getProductVo().getPurchaseTaxIncluded());
//                    		double taxrate = productVarientsVo.getProductVo().getTaxVo().getTaxRate();
//                    		System.out.println("taxrate is :"+taxrate);
//                    		System.out.println("actual sheet price is :"+row.getCell(2).getStringCellValue().trim());
//                            double price = Double.parseDouble(row.getCell(2).getStringCellValue().trim()) / ((taxrate / 100) + 1);
//                            System.out.println("taxinclueded price is :"+price);
//                            purchaseItemVo.setPrice(round(price, 2));
//                            //System.err.println("----------------------------------------------");
//                    	}else {
//                    		//System.err.println("not tax included:"+productVarientsVo.getProductVo().getPurchaseTaxIncluded());
//                    		purchaseItemVo
//                            .setPrice(round(Double.parseDouble(row.getCell(2).getStringCellValue().trim()), 2));
//                    	}
//
//                    } catch (Exception e) {
//                    	e.printStackTrace();
//                        purchaseItemVo.setPrice(0);
//                    }
//
//                    //System.err.println("---------------YASh----------------");
//                    //System.err.println("------"+purchaseItemVo.getPrice());
//                    //System.err.println("---------------YASh----------------");
//
//                    qty = purchaseItemVo.getQty();
//                    taxRate = purchaseItemVo.getTaxRate();
//                    rate = purchaseItemVo.getPrice();
//                    discount = purchaseItemVo.getDiscount();
//
//                    if (purchaseItemVo.getDiscountType().equals("percentage")) {
//                        discount = (rate * discount) / 100;
//                    }
//
//                    totalDiscount += (discount * 1) * (qty * 1);
//
//                    taxableValue = (rate * qty) - (discount * qty);
//
//                    taxAmount = (taxableValue * taxRate) / 100;
//                    total = taxableValue + taxAmount;
//
//
//                    totalAmount += total;
//                    totalTaxAmount += taxAmount;
//                    totalQty += qty;
//
//                    mainTotal += totalAmount;
//                    purchaseItemVo.setLandingCost(total / qty);
//                    purchaseItemVo.setTaxAmount(taxAmount);
//                    purchaseItemVo.setPurchaseVo(purchaseVo);
//
//                    itemVos.add(purchaseItemVo);
//                    }
//                }
//
//                purchaseVo.setPurchaseItemVos(itemVos);
//                System.out.println("mainTotal-========================----------::" + mainTotal);
//                purchaseVo.setTotal(Math.round(mainTotal * 100.0) / 100.0);
//                in.close();
//            }
//        ////////////////////////
//        if (allRequestParams.get("gstApply") == null) {
//            purchaseVo.setGstApply(1);
//            //System.err.println("GSTAPPLAYEEEE*************");
//            List<TaxVo> tax = taxService.findByTaxRate(0.0);
//            if (tax.size() > 0)
//                purchaseVo.getPurchaseItemVos().forEach(p -> {
//                    p.setTaxVo(tax.get(0));
//                    p.setTaxRate(0);
//                });
//        }
//
//        PurchaseVo purchaseVo3 = purchaseService.save(purchaseVo);
//
//        purchaseService.updatestatusbypurchaseID(purchaseVo3.getPurchaseId());
//
//        try {
//        	if(purchaseVo3.getType().equals(Constant.PURCHASE_BILL)) {
//            	if(purchaseVo.getPurchaseVo() != null) {
//            		//means purchase bill generated
//            		if(StringUtils.isNotBlank(allRequestParams.get("parentType"))) {
//            			if(StringUtils.equalsIgnoreCase(Constant.PURCHASE_ORDER, allRequestParams.get("parentType"))) {
//            				purchaseService.updatePurchaseStatus(purchaseVo.getPurchaseVo().getPurchaseId(), "close");
//                		}
//            		}
//
//            	}
//            }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//
//
//        purchaseVo3.getPurchaseItemVos().forEach((p ->
//        {
//            shopifySetupService.updateStock(p.getQty(), p.getProductVarientsVo().getProductVarientId(), Long.parseLong(session.getAttribute("companyId").toString()));
//        }));
//
//        // purchase price updation //off to purchase tax including features add 11/01/20
////		if(purchaseVo.getType().equals(Constant.PURCHASE_BILL)) {
////			for (PurchaseItemVo vo:purchaseVo.getPurchaseItemVos()) {
////				productService.updatepurchasePrice(vo.getProductVarientsVo().getProductVarientId(),vo.getPrice());
////			}
////		}
//        /////////////////////////
//
//
//
//        if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
//        	purchaseService.insertPurchaseDebitNote(purchaseVo3, session.getAttribute("financialYear").toString(),session);
//        }
//
//        if (purchaseVo.getType().equals(Constant.PURCHASE_BILL) || purchaseVo.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
//            purchaseService.insertPurchaseTransaction(purchaseVo3, session.getAttribute("financialYear").toString());
//        } else if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
//        	purchaseService.insertPurchaseDebitNote(purchaseVo3, session.getAttribute("financialYear").toString(),session);
//        }
//
//        if(purchaseVo3.getType().equals(Constant.PURCHASE_BILL) && isedit==0) {
//        	generatedebitnote(purchaseVo3,allRequestParams,session);
//        }
//        if(purchaseVo3.getType().equals(Constant.PURCHASE_BILL) && isedit==1) {
//        	purchaseVo3.setPurchaseReturnItemDTO(purchaseVo.getPurchaseReturnItemDTO());
//        //	//System.err.println("dto size---"+purchaseVo3.getPurchaseReturnItemDTO().size());
//        	updatedebitnote(purchaseVo3,allRequestParams,session);
//        }
//        if (allRequestParams.get("saveandpayment") != null && Integer.parseInt(allRequestParams.get("saveandpayment")) == 1) {
//
//
//            if (allRequestParams.get("typePayment") != null && allRequestParams.get("typePayment").equals("1")) {
////                List<ReceiptVo> receiptVos = receiptService.findByBranchIdAndIsDeletedAndContactVoContactIdAndType(
////                        Long.parseLong(session.getAttribute("branchId").toString()), 0,
////                        salesVo2.getContactVo().getContactId(), Constant.PAYMENT_TYPE_ADVANCE);
//            	String advancePaymentBill = allRequestParams.get("advancePaymentBillId");
//            	List<PaymentVo> paymentVos=new ArrayList<PaymentVo>();
//            	if(advancePaymentBill!=null&& !advancePaymentBill.equals("")) {
//            	List<Long> l = Arrays.asList(advancePaymentBill.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
//            	paymentVos = paymentService.findByBranchidandisDeletedAndTypeAndPaymentId(  Long.parseLong(session.getAttribute("branchId").toString()), 0,Constant.PAYMENT_TYPE_ADVANCE,l);
//                }
//
//            	double salesTotal = purchaseVo3.getTotal(), totalPayment = 0;
//                for (PaymentVo paymentVo4 : paymentVos) {
//
//                    if (salesTotal != 0) {
//
//                        double receiptbilltotal = paymentVo4.getPaymentBillVos().stream()
//                                .mapToDouble(p -> p.getTotalPayment()).sum();
//
//
//                        if (receiptbilltotal != paymentVo4.getTotalPayment()) {
//
//                            double receiptAmount = paymentVo4.getTotalPayment() - receiptbilltotal;
//                            PaymentBillVo billVo = new PaymentBillVo();
//                            if (salesTotal <= receiptAmount) {
//
//                                billVo.setKasar(0);
//                                billVo.setOldPyament(0);
//                                billVo.setPaymentVo(paymentVo4);
//                                billVo.setPurchaseVo(purchaseVo3);
//                                billVo.setTotalPayment(salesTotal);
//                                salesTotal = 0;
//                            } else {
//
//                                billVo.setKasar(0);
//                                billVo.setOldPyament(0);
//                                billVo.setPaymentVo(paymentVo4);
//                                billVo.setPurchaseVo(purchaseVo3);
//                                billVo.setTotalPayment(receiptAmount);
//                                salesTotal -= receiptAmount;
//                            }
//
//                            totalPayment += billVo.getTotalPayment();
//
//                            paymentService.saveBill(billVo);
//                            paymentService.transation(paymentVo4, 0);
//                        }
//
//                    }
//                }
//                //salesService.updatePaidAmountPlus(salesVo2.getSalesId(), totalPayment);
//                purchaseService.updatePaidAmountPlus(purchaseVo3.getPurchaseId(), totalPayment);
//
//            }
//
//            if (!allRequestParams.get("totalPayment").equals("0")) {
//
//            	PaymentVo paymentVo = new PaymentVo();
//
//                paymentVo.setPaymentNo(paymentService.getNewPaymentNo(Constant.PAYMENT,
//                        Long.parseLong(session.getAttribute("branchId").toString()),
//                        Long.parseLong(session.getAttribute("userId").toString()), "PAY", session));
//                paymentVo.setPrefix(prefixService
//                        .findByBranchIdAndprefixType(Long.parseLong(session.getAttribute("branchId").toString()),
//                                Constant.PAYMENT)
//                        .get(0).getPrefix());
//                paymentVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
//                paymentVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
//                paymentVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));
//                paymentVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
//                paymentVo.setCreatedOn(CurrentDateTime.getCurrentDate());
//                paymentVo.setModifiedOn(CurrentDateTime.getCurrentDate());
//                paymentVo.setAmount(Double.parseDouble(allRequestParams.get("totalPayment")));
//                paymentVo.setPaymentDate(purchaseVo3.getPurchaseDate());
//                paymentVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
//                paymentVo.setPaymentMode("Cash");
//                paymentVo.setType(Constant.PAYMENT_TYPE_AGAINSTBILL);
//                paymentVo.setContactVo(purchaseVo3.getContactVo());
//               // DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//
//                try {
//                	paymentVo.setDescription(allRequestParams.get("description"));
//                } catch (Exception e) {
//                    // TODO: handle exception
//
//                }
//
//                if (allRequestParams.get("paymentMode").equals("bank")) {
//
//                    BankVo bankVo = new BankVo();
//                    bankVo.setBankId(Long.parseLong(allRequestParams.get("bankVoId")));
//                    paymentVo.setBankVo(bankVo);
//                    paymentVo.setPaymentMode("bank");
//                    paymentVo.setAccountNo(allRequestParams.get("accountNo"));
//                    paymentVo.setBankTransactionType(allRequestParams.get("bankpaymentmode"));
//                    paymentVo.setStatus("cleared");
//                    try {
//                    	paymentVo.setChequeDate(dateFormat.parse(allRequestParams.get("chequeDate")));
//                    } catch (ParseException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }else {
//                	AccountCustomVo accountCustomVo=new AccountCustomVo();
//                	accountCustomVo.setAccountCustomId(Long.parseLong(allRequestParams.get("accountCustomVo.accountCustomId")));
//                	paymentVo.setAccountCustomVo(accountCustomVo);
//                	try {
//                    	paymentVo.setChequeDate(paymentVo.getPaymentDate());
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//
//                PaymentBillVo paymentBillVo=new PaymentBillVo();
//                List<PaymentBillVo> paymentBillVos = new ArrayList<>();
//                paymentBillVo.setKasar(Double.parseDouble(allRequestParams.get("Kasar")));
//                paymentBillVo.setOldPyament(0);
//                paymentBillVo.setPaymentVo(paymentVo);
//                paymentBillVo.setPurchaseVo(purchaseVo3);
//                paymentBillVo.setTotalPayment(Double.parseDouble(allRequestParams.get("totalPayment")));
//                paymentBillVos.add(paymentBillVo);
//                paymentVo.setPaymentBillVos(paymentBillVos);
//                PaymentVo paymentVo2=paymentService.save(paymentVo);
//
//                paymentService.transation(paymentVo2, paymentBillVo.getKasar());
//
//                purchaseService.updatePaidAmountPlus(purchaseVo3.getPurchaseId(), Double.parseDouble(allRequestParams.get("totalPayment")));
//            }
//        }
//
//        // Send Message
//        sendMessageToCutomer(purchaseVo3, session);
//
//        //case for new or edit
//        if(idd == 0) {
//	        //System.err.println("HERE StocktransferId is :"+purchaseVo3.getStockTransferId());
//	        if(purchaseVo3.getStockTransferId() != 0) {
//	        	saveSalesForStockTransferInCompany(purchaseVo3,session,request);
//	        }
//        }
//
//        if (allRequestParams.get("saveandnew") != null
//                && Integer.parseInt(allRequestParams.get("saveandnew")) == 1) {
//            return "redirect:/purchase/" + type + "/new";
//        } else if (allRequestParams.get("saveandprint") != null
//                && Integer.parseInt(allRequestParams.get("saveandprint")) == 1) {
//            if (type.equals(Constant.PURCHASE_BILL)) {
//                return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId() + "?print=true&barcode=true";
//            }
//            return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId() + "?print=true";
//        } else {
//            if (type.equals(Constant.PURCHASE_BILL)) {
//                return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId() + "?barcode=true";
//            }
//            return "redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId();
//        }
//
//    }



	@Async
    protected void saveSalesForStockTransferInCompany(PurchaseVo purchaseVo3, HttpSession session, HttpServletRequest servletRequest) {

    	SalesVo salesVo = new SalesVo();
    	String type = Constant.SALES_INVOICE;

    	UserFrontVo frontVo = profileService.findByUserFrontId(purchaseVo3.getCompanyId());
    	long customerId = profileService.getCustomerId(purchaseVo3.getBranchId());

    	salesVo.setType(type);
    	salesVo.setStatus(Constant.INVOICED);
    	salesVo.setAlterBy(purchaseVo3.getCompanyId());
    	salesVo.setCreatedOn(CurrentDateTime.getCurrentDate());
        salesVo.setModifiedOn(CurrentDateTime.getCurrentDate());
        salesVo.setBranchId(purchaseVo3.getCompanyId());
        salesVo.setCompanyId(purchaseVo3.getCompanyId());
        if(customerId!=0) {
        	ContactVo contact = new ContactVo();
            contact.setContactId(customerId);
            salesVo.setContactVo(contact);

        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            salesVo.setSalesDate(purchaseVo3.getPurchaseDate());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
                salesVo.setDueDate(purchaseVo3.getPurchaseDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String defaultPrefix = "INV";
        String prefix = prefixService
                .getPrefixByPrefixTypeAndBranchId(purchaseVo3.getCompanyId(), type, purchaseVo3.getCompanyId());

        if (StringUtils.isNotBlank(prefix))
            defaultPrefix = prefix;
        long newSalesNo = salesService.findMaxSalesNo(purchaseVo3.getCompanyId(),
        		type, defaultPrefix, purchaseVo3.getCompanyId());
        salesVo.setPrefix(defaultPrefix);
        salesVo.setSalesNo(newSalesNo);
        String termsandcondition = "";
        try {
        	List<String> termnsandconditionlist = termsAndConditionService.getTermsAndConditionByBranchIdIsDefaultAndIsDeleted(purchaseVo3.getBranchId(), 1, 0, purchaseVo3.getCompanyId()).stream()
                    .map(p -> String.valueOf(p.getTermandconditionId()))
                    .collect(Collectors.toList());
            for(String t : termnsandconditionlist) {
            	// log.warning("HERE terms id is :"+t);
            	termsandcondition = t+",";
            }
            salesVo.setTermsAndConditionIds(termsandcondition.substring(0, termsandcondition.length() - 1));
		} catch (Exception e) {
			salesVo.setTermsAndConditionIds("");
			e.printStackTrace();
		}
        SalesVo salesVo2 = null;
        ContactAddressVo contactAddressVo = contactService.findAddressByContactIdDefault(customerId);
        try {

        	salesVo.setBillingAddressLine1(contactAddressVo.getAddressLine1());
            salesVo.setBillingAddressLine2(contactAddressVo.getAddressLine2());
            salesVo.setBillingCityCode(contactAddressVo.getCityCode());
            salesVo.setBillingCompanyName(contactAddressVo.getCompanyName());
            salesVo.setBillingCountriesCode(contactAddressVo.getCountriesCode());
            salesVo.setBillingFirstName(contactAddressVo.getFirstName());
            salesVo.setBillingLastName(contactAddressVo.getLastName());
            salesVo.setBillingPinCode(contactAddressVo.getPinCode());
            salesVo.setBillingStateCode(contactAddressVo.getStateCode());
            salesVo.setBillingGstin(contactAddressVo.getGstin());
		} catch (Exception e) {
			e.printStackTrace();
		}
        try {
        	salesVo.setShippingAddressLine1(contactAddressVo.getAddressLine1());
            salesVo.setShippingAddressLine2(contactAddressVo.getAddressLine2());
            salesVo.setShippingCityCode(contactAddressVo.getCityCode());
            salesVo.setShippingCompanyName(contactAddressVo.getCompanyName());
            salesVo.setShippingCountriesCode(contactAddressVo.getCountriesCode());
            salesVo.setShippingFirstName(contactAddressVo.getFirstName());
            salesVo.setShippingLastName(contactAddressVo.getLastName());
            salesVo.setShippingPinCode(contactAddressVo.getPinCode());
            salesVo.setShippingStateCode(contactAddressVo.getStateCode());
            salesVo.setShippingGstin(contactAddressVo.getGstin());
            if(!StringUtils.equals(frontVo.getStateCode(), salesVo.getShippingStateCode())) {
            	salesVo.setTaxType(1);
            	// log.warning("HERE Tax Type = 1");
            }else {
            	// log.warning("HERE Tax Type = 0");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
        // log.warning("HERE out Tax Type = "+salesVo.getTaxType());
        salesVo.setPaidAmount(0.0);

        if(!purchaseVo3.getPurchaseAdditionalChargeVos().isEmpty()) {
        	List<SalesAdditionalChargeVo> additionalChargeVos = new ArrayList<SalesAdditionalChargeVo>();
        	for(PurchaseAdditionalCharge purchaseAdditionalCharge : purchaseVo3.getPurchaseAdditionalChargeVos()) {
        		SalesAdditionalChargeVo salesAdditionalCharge = new SalesAdditionalChargeVo();
        		salesAdditionalCharge.setAdditionalChargeVo(purchaseAdditionalCharge.getAdditionalChargeVo());
        		salesAdditionalCharge.setAmount(purchaseAdditionalCharge.getAmount());
        		salesAdditionalCharge.setSalesVo(salesVo);
        		salesAdditionalCharge.setTaxAmount(purchaseAdditionalCharge.getTaxAmount());
        		salesAdditionalCharge.setTaxRate(purchaseAdditionalCharge.getTaxRate());
        		salesAdditionalCharge.setTaxVo(purchaseAdditionalCharge.getTaxVo());
        		additionalChargeVos.add(salesAdditionalCharge);
        	}
			salesVo.setSalesAdditionalChargeVos(additionalChargeVos);
        }
        salesVo.setStockTransferId(purchaseVo3.getStockTransferId());
        salesVo.setFlatDiscount(purchaseVo3.getFlatDiscount());
        salesVo.setReverseCharge(0);
        salesVo.setSez(0);
        salesVo.setCreatedBy(purchaseVo3.getCompanyId());
        double finalNetAmount = 0;
        if(!purchaseVo3.getPurchaseItemVos().isEmpty()) {
        	List<SalesItemVo> salesItemVos = new ArrayList<SalesItemVo>();
        	for(PurchaseItemVo purchaseItemVo : purchaseVo3.getPurchaseItemVos()) {
        		SalesItemVo salesItemVo = new SalesItemVo();
        		List<StockMasterVo> stockMasterVos = stockMasterRepository
                        .findByProductVarientsVoProductVarientIdAndBranchIdAndYearIntervalOnlyOneBatch2(purchaseItemVo.getProductVarientsVo().getProductVarientId()
                                ,purchaseVo3.getCompanyId(), frontVo.getDefaultYearInterval());
        		double mrp = 0;
                double originalprice = 0;
        		 double discount;
                 double discount2;
                 double flatDisc;
                 double amount = 0.0;
                 double taxableValue = 0.0;
                 double taxAmount = 0.0;
                 double total = 0.0;

         		String mrpToDiscountType = "percentage";
                 double mrpToDiscount = 0;
                 ProductVarientsVo varientsVo = productService.findByProductVarientId(purchaseItemVo.getProductVarientsVo().getProductVarientId());
                //ProductVarientsVo varientsVo = new ProductVarientsVo();
                //varientsVo.setProductVarientId(purchaseItemVo.getProductVarientsVo().getProductVarientId());


            	double landingcost =purchaseItemVo.getLandingCost();
	            double sellingPrice=purchaseItemVo.getSellingPrice();
	            salesItemVo.setProductVarientsVo(varientsVo);
	            salesItemVo.setItemCode(varientsVo.getItemCode());
                // itemVo.setProduct(ProductVarientsVo.getProductVo());

	            salesItemVo.setTaxRate(purchaseItemVo.getTaxRate());
        		salesItemVo.setTaxVo(purchaseItemVo.getTaxVo());

                mrp = purchaseItemVo.getMrp();
                //barchmrp = purchaseItemVo.getMrp();

                if(!stockMasterVos.isEmpty()){
	                try {
	                	salesItemVo.setBatchNo(stockMasterVos.get(0).getBatchNo());
	                	//landingcost = stockMasterVos.get(0).getLandingCost();
                  	  	//sellingPrice = stockMasterVos.get(0).getSellingPrice();
                  	  	//barchmrp = stockMasterVos.get(0).getMrp();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
                }else {
                	try {
                		salesItemVo.setBatchNo("B" + purchaseItemVo.getProductVarientsVo().getProductVarientId()
                                + new DecimalFormat("#.###").format(purchaseItemVo.getPrice()));
					} catch (Exception e) {
						e.printStackTrace();
					}

                }
                salesItemVo.setProductDescription(purchaseItemVo.getProductDescription());
        		salesItemVo.setPrice(purchaseItemVo.getPrice());
        		salesItemVo.setQty(purchaseItemVo.getQty());
        		salesItemVo.setSalesmanId(0);

                salesItemVo.setLandingCost(landingcost);
                salesItemVo.setSellingPrice(sellingPrice);
                salesItemVo.setProfit(sellingPrice-landingcost);
                originalprice = mrp;
                mrpToDiscount  = purchaseItemVo.getDiscount();
                mrpToDiscountType = purchaseItemVo.getDiscountType();
                flatDisc=purchaseItemVo.getFlatDiscount();

                salesItemVo.setMrpToDiscount(mrpToDiscount);
                salesItemVo.setMrpToDiscountType(mrpToDiscountType);

                salesItemVo.setMrpTodiscountAdditional(0);
                salesItemVo.setMrpToDiscountTypeAdditional("percentage");
                salesItemVo.setDiscountType(purchaseItemVo.getDiscountType());
                salesItemVo.setDiscountType2("percentage");
                salesItemVo.setFlatDiscount(flatDisc);
                double mrptodiscount = 0;

//            	if (!mrpToDiscountType.equals("percentage")) {
//                	mrptodiscount = ((mrpToDiscount * 100) / originalprice);
//                }
            	// log.warning("setDiscount---------->"+mrptodiscount);
            	salesItemVo.setDiscount(purchaseItemVo.getDiscount());

//          	double mrptodiscountadditional = 0;
//            	if (mrpToDiscountTypeAdditional.equals("percentage")) {
//            		mrptodiscountadditional = mrpToDiscountAdditional;
//                	// log.warning("setDiscount2---------->"+mrptodiscountadditional);
//                	salesItemVo.setDiscount2(mrptodiscountadditional);
//                } else {
//                	double total_value = 0;
//                    if (mrpToDiscountType.equals("percentage")) {
//                    	total_value = mrp - ((mrp * mrpToDiscount) / 100);
//                    } else {
//                    	total_value = mrp - mrpToDiscount;
//                    }
//
//                    mrptodiscountadditional = (((total_value - mrpToDiscountAdditional) * 100) / total_value);
//                    // log.warning("setDiscount2---------->"+mrptodiscountadditional);
//                    salesItemVo.setDiscount2(round((100 - mrptodiscountadditional),2));
//                }

        		salesItemVo.setCessAmount(0);
        		salesItemVo.setCessRate(0);
        		salesItemVo.setDesignNo(purchaseItemVo.getDesignNo());

//        		salesItemVo.setDiscount(purchaseItemVo.getDiscount());
//        		salesItemVo.setDiscount2(purchaseItemVo.getDiscount2());
//
//        		salesItemVo.setDiscountType(purchaseItemVo.getDiscountType());
//        		salesItemVo.setDiscountType2(purchaseItemVo.getDiscountType2());

//        		salesItemVo.setDiscountAdditional(0);
//        		salesItemVo.setDiscountTypeAdditional("amount");
        		salesItemVo.setFreeQty(purchaseItemVo.getFreeQty());
        		salesItemVo.setMrp(mrp);
//        		salesItemVo.setMrpToDiscount(0);
//        		salesItemVo.setMrpToDiscountType("amount");
        		salesItemVo.setMrpTodiscountAdditional(0);
        		salesItemVo.setMrpToDiscountTypeAdditional("amount");
        		double qty = salesItemVo.getQty();
        		double taxRate = salesItemVo.getTaxRate();
        		double rate = salesItemVo.getPrice();
                amount = rate * qty;
double totalDiscountAmount = 0.0;
                discount = salesItemVo.getDiscount();
                discount2 = salesItemVo.getDiscount2();

//                if (salesItemVo.getDiscountType().equals("percentage")) {
//                	taxableValue = amount - ((amount * discount) / 100);
//        		} else {
        			taxableValue = purchaseItemVo.getNetAmount() - purchaseItemVo.getTaxAmount() -purchaseItemVo.getCessAmount();
//        		}
                // log.warning("taxableValue---------->"+taxableValue);

//        		if (salesItemVo.getDiscountType2().equals("percentage")) {
//        			taxableValue = taxableValue - ((taxableValue * discount2) / 100);
//        		} else {
//        			taxableValue = taxableValue - discount2;
//        		}
//        		taxableValue = amount -  flatDisc ;
        		// log.warning("After taxableValue---------->"+taxableValue);


                taxAmount = purchaseItemVo.getTaxAmount();
                // log.warning("taxAmount---------->"+taxAmount);

                total = taxableValue + taxAmount;
                // log.warning("HERE salesitem netamount is-->"+total);
totalDiscountAmount += amount - taxableValue;
        		salesItemVo.setNetAmount(Double.valueOf(df2.format(total)));
        		salesItemVo.setTaxAmount(taxAmount);

        		finalNetAmount += total;

        		salesItemVo.setSalesVo(salesVo);
        		salesItemVo.setTaxAmount(purchaseItemVo.getTaxAmount());

        		salesItemVo.setProductVarientsVo(varientsVo);

                ////System.err.println("batchId---"+purchaseItemVo.getTempBatchId());
                salesItemVo.setBatchId(purchaseItemVo.getTempBatchId());

                salesItemVos.add(salesItemVo);
                salesVo.setTotalDiscountInAmount(totalDiscountAmount);
        	}
			salesVo.setSalesItemVos(salesItemVos);

			double roundoff = Math.round(finalNetAmount)-finalNetAmount;
			Double actualTotal = Double.valueOf(df2.format(roundoff));
			float roundOff = actualTotal.floatValue();
			salesVo.setRoundoff(roundOff);
			salesVo.setTotal(purchaseVo3.getTotal());
        }


        salesVo2 = salesService.save(salesVo);
        StockTransferVo stockTransferVo = stockTransferService.findByStockTransferIdAndIsDeleted(salesVo2.getStockTransferId(),0);

        stockTransactionService.deleteStockTransactionSales(stockTransferVo.getBranchId(), stockTransferVo.getStockTransferId(), "stock_transfer");

        salesService.insertSalesTransaction(salesVo2, session.getAttribute("financialYear").toString(),session);

        String pdfToken = EncryptMessage.getSecureMessage(
                salesVo.getSalesId() + salesVo.getBillingCompanyName() + CurrentDateTime.getCurrentDate());
        salesService.updateToken(salesVo2.getSalesId(), pdfToken);
        salesVo2.setPdfToken(pdfToken);
        //sendSMS(salesVo2, session);
        try {
        	sendSMS(salesVo2, salesVo2.getCompanyId(), session.getAttribute("whatsappToken").toString(), frontVo.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (salesVo2.getContactVo() != null) {
            if (salesVo2.getContactVo().getEmail() != null) {
                CompanySettingVo allowInvoiceEmail = companySettingService.findByCompanyIdAndType(salesVo2.getCompanyId(),
                        Constant.ALLOWINVOICEEMAIL);
                if (allowInvoiceEmail != null && allowInvoiceEmail.getValue() == 1) {
                    String body = Converter.convertToHtml(servletRequest,
                            "/media/download/sales/" + salesVo2.getPdfToken() + "/mail");
                    sendGridEmailService.sendHTML(from, salesVo2.getContactVo().getEmail(), "Invoice", body, salesVo2.getCompanyId());
                }
            }
        }


	}

    @Async
    private void sendSMS(SalesVo salesVo,long companyId,String whatsappToken,String name) {

        String message = "";

        if (salesVo.getType().equals(Constant.SALES_INVOICE) || salesVo.getType().equals(Constant.SALES_CREDIT_NOTE)) {
            //long companyId = Long.parseLong(session.getAttribute("companyId").toString());
            CompanySettingVo allowInvoicePOSSMS = companySettingService.findByCompanyIdAndType(companyId,
                    Constant.ALLOWINVOICESMS);

            CompanySettingVo allowInvoiceWhatsapp = companySettingService.findByCompanyIdAndType(companyId,
                    Constant.ALLOWINVOICEWHATSAPP);
            message +="Thank you for your purchase. Check your invoice\n"
               		+ ""+urlService.shortenUrl(BASEURL + "media/download/sales/" + salesVo.getPdfToken() + "/pdf")+" We welcome you again!! Regards\n"
               		+ ""+ name+" \n"
               		+ "system developed by vasyerp.com";

//            message += "Thank you for your purchase. Check your invoice \n";
//            message += urlService.shortenUrl(BASEURL + "media/download/sales/" + salesVo.getPdfToken() + "/pdf");
//            message += " We welcome you again!! Regards \n" + name;
            //String token = (String) session.getAttribute("whatsappToken");
            String token = whatsappToken;
            if (salesVo.getContactVo().getMobNo() != null || salesVo.getContactVo().getWhatsappNo() != null) {
            	Map<String, String> map = messageService.generateMessageForSales(salesVo,Constant.SMSFOR_USER);
            	String senderId = "VSYERP";

                if (map.isEmpty()) {
                	String branchSenderId = userRepository.findSenderIdByuserFrontId(companyId);
					if(StringUtils.isNotBlank(branchSenderId)){
						senderId = branchSenderId;
					}
                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), message, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), message,
//                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),
//                            		SMSCONSTANT.POS_ORDER_MESSAGE,senderId,"defaultMSG",4);
                            messageService.sendMsgWithCountryDialCodeAsync(salesVo.getContactVo().getMobNo(), message,
                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),
                            		SMSCONSTANT.POS_ORDER_MESSAGE,senderId,"defaultMSG",4,salesVo.getContactVo().getCountryDialCodePrefix());

                        }
                    }
                    if (salesVo.getContactVo().getWhatsappNo() != null
                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(),
//                                    message, token, companyId, companyId);
                            whatsappController.sendTextMessageToCustomer((salesVo.getContactVo().getCountryDialCodePrefixWhatsapp()==0?91:salesVo.getContactVo().getCountryDialCodePrefixWhatsapp())+ salesVo.getContactVo().getWhatsappNo(),
                                    message, token, companyId, companyId);
                        }
                    }

                } else {
                	if(StringUtils.isNotBlank(map.get("senderId"))){
						senderId = map.get("senderId");
					}
                    if (salesVo.getContactVo().getMobNo() != null && !salesVo.getContactVo().getMobNo().equals("")) {
                        if (allowInvoicePOSSMS != null && allowInvoicePOSSMS.getValue() == 1) {
                            //messageService.sendMsg(salesVo.getContactVo().getMobNo(), m, session);
//                            messageService.sendMsgAsync(salesVo.getContactVo().getMobNo(), map.get("textMessage"),
//                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),map.get("templateId"),senderId,"templateMSG",Integer.parseInt(map.get("route")));
                            messageService.sendMsgWithCountryDialCodeAsync(salesVo.getContactVo().getMobNo(), map.get("textMessage"),
                            		salesVo.getContactVo().getCompanyId(), salesVo.getContactVo().getCompanyId(),map.get("templateId"),senderId,"templateMSG",Integer.parseInt(map.get("route")),salesVo.getContactVo().getCountryDialCodePrefix());
                        }
                    }
                    if (salesVo.getContactVo().getWhatsappNo() != null
                            && !salesVo.getContactVo().getWhatsappNo().equals("")) {
                        if (allowInvoiceWhatsapp != null && allowInvoiceWhatsapp.getValue() == 1) {
//                            whatsappController.sendTextMessageToCustomer(salesVo.getContactVo().getWhatsappNo(), map.get("whatsappMessage"),
//                                    token, companyId, companyId);
                            whatsappController.sendTextMessageToCustomer((salesVo.getContactVo().getCountryDialCodePrefixWhatsapp()==0?91:salesVo.getContactVo().getCountryDialCodePrefixWhatsapp())+ salesVo.getContactVo().getWhatsappNo(),
                            		map.get("whatsappMessage"),token, companyId, companyId);
                        }
                    }
                }
            }

        }

    }

    private void sendOrderPdfToSuppliers(PurchaseVo purchaseVo, HttpSession session, HttpServletRequest request) throws IOException {
    	try {


        if (purchaseVo.getContactVo() != null && (purchaseVo.getType().equals(Constant.PURCHASE_ORDER))) {
            long companyId = Long.parseLong(session.getAttribute("companyId").toString());
            CompanySettingVo settingVo = null;
            String absolutepath=getPurchasePDFpath(purchaseVo.getPurchaseId(), purchaseVo.getType(), session, request);
          String message="Thank you For purchase";
          if (purchaseVo.getContactVo().getEmail() != null) {
              settingVo = companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWSUPPLIERORDEREMAIL);
              if (settingVo != null && settingVo.getValue() == 1) {

//					String body = Converter.convertToHtml(servletRequest,
//							"/media/download/sales/" + salesvo.getPdfToken() + "/mail");
                  //sendGridEmailService.sendHTML(from, purchaseVo.getContactVo().getEmail(), "Supplier Bill", message, companyId);
            	  try {
            		  if (purchaseVo.getContactVo().getEmail() != null
                              && !purchaseVo.getContactVo().getEmail().equals("")) {
                  	sendGridEmailService.sendMessageWithAttachmentCUSTOM(from, purchaseVo.getContactVo().getEmail(),"Purchase Order ", message, absolutepath,companyId);
                	  }
				} catch (Exception e) {
					e.printStackTrace();
					}

              }
          }
            if (purchaseVo.getContactVo().getWhatsappNo() != null) {
                String token = (String) session.getAttribute("whatsappToken");
                settingVo = companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWSUPPLIERORDERWHATSAPP);
                if (settingVo != null && settingVo.getValue() == 1) {
                	//String absolutepath=getPurchasePDFpath(purchaseVo.getPurchaseId(), purchaseVo.getType(), session, request);
					//System.err.println("-------------send whtasapp--------");
					try {
						 if (purchaseVo.getContactVo().getWhatsappNo() != null
		                            && !purchaseVo.getContactVo().getWhatsappNo().equals("")) {
							  	//whatsappService.SendDocument(purchaseVo.getContactVo().getMobNo(), message,  token, absolutepath);
							  	whatsappService.SendDocument((purchaseVo.getContactVo().getCountryDialCodePrefix()==0?91:purchaseVo.getContactVo().getCountryDialCodePrefix())+ purchaseVo.getContactVo().getMobNo(),
							  			message, token, absolutepath);
						  }
					} catch (Exception e) {
						e.printStackTrace();
					}

                    //(purchaseVo.getContactVo().getWhatsappNo(), message,
                      //      token, companyId, companyId);
                }
            }


        }
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    @SuppressWarnings("unchecked")
	public String getPurchasePDFpath(long id,String type, HttpSession session,
			HttpServletRequest request) throws IOException
	{	String absolutepath="";
    	 PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                 Long.parseLong(session.getAttribute("branchId").toString()));

         ReportSettingVo setting = reportService.findByTypeAndBranchId(type,
                 Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

         HashMap jasperParameter = new HashMap();
         jasperParameter.put("purchase_id", id);
         jasperParameter.put("realPath", session.getAttribute("realPath").toString());

         jasperParameter.put("user_front_id", Long.parseLong(session.getAttribute("branchId").toString()));
         jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(purchaseVo.getTotal(),session.getAttribute("currencyName").toString()));
         jasperParameter.put("contact_id", purchaseVo.getContactVo().getContactId());
         jasperParameter.put("contact_type", purchaseVo.getContactVo().getType());
         jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());

         if (type.equals(Constant.PURCHASE_BILL)) {
             jasperParameter.put("display_title", "Purchase Bill");
         } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
             jasperParameter.put("display_title", "Debitnote");
         } else if (type.equals(Constant.PURCHASE_ORDER)) {
             jasperParameter.put("display_title", "Purchase Order");
         } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
             jasperParameter.put("display_title", "Material Inward");
         }
         int decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());


         String decimalFormate = numberUtil.getFormateOnDecimal(decimalNumber);
         //System.out.println("decimalFormate:::"+decimalFormate);
         //System.out.println("report"+setting.getReportVo().getReport());
         jasperParameter.put("decimalFormate", decimalFormate);

         jasperParameter.put("path",JASPER_REPORT_PATH + File.separator);
         try {
             if (type.equals(Constant.PURCHASE_ORDER)) {
             	if (setting != null) {
             		if(StringUtils.isNotBlank(setting.getReportVo().getReportFormateType()) && setting.getReportVo().getReportFormateType().equals(Constant.HTML)){
             			absolutepath=jasperExporter.jasperExporterPDFabsolutepath(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/purchase-order-1.jrxml");
            		}else {
             			 absolutepath=jasperExporter.jasperExporterPDFabsolutepath(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/"
                                         + setting.getReportVo().getReport() + ".jrxml");
            		}
                 } else {
                	 absolutepath=jasperExporter.jasperExporterPDFabsolutepath(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/purchase-order-1.jrxml");
                 }


             }
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             //System.err.println(e.toString());
         }finally {
//         	System.out.println("path-----"+absolutepath);
				return absolutepath;

			}
	}

    private void sendMessageToSupplier(PurchaseVo purchaseVo, HttpSession session) {
		if (purchaseVo.getContactVo() != null && (purchaseVo.getType().equals(Constant.PURCHASE_ORDER))) {
			long companyId = Long.parseLong(session.getAttribute("companyId").toString());
			String senderId = "VSYERP";
			String branchSenderId = userRepository.findSenderIdByuserFrontId(companyId);
			if (StringUtils.isNotBlank(branchSenderId)) {
				senderId = branchSenderId;
			}

			String companyName=purchaseVo.getContactVo().getCompanyName();
			String purchaseOrder=purchaseVo.getPrefix()+purchaseVo.getPurchaseNo();
			Date purchaseDate=purchaseVo.getPurchaseDate();
			double amount=purchaseVo.getTotal();
//			String link="https://app.vasyerp.com/vasy/bpwc";
//			System.err.println("Token Is  :"+purchaseVo.getPdfToken());
			String link=urlService.shortenUrlP(BASEURL + "media/download/purchase/" + purchaseVo.getPdfToken() + "/pdf");

			String message = companyName+": You have received a PO - "+purchaseOrder +" on "+purchaseDate +" of amount "+amount +". Check your PO here - "+link;
			if (purchaseVo.getContactVo().getMobNo() != null) {
			String mobileNumber=purchaseVo.getContactVo().getMobNo();
//					messageService.sendMsgAsync(mobileNumber, message,
//							purchaseVo.getContactVo().getCompanyId(), purchaseVo.getContactVo().getCompanyId(),
//							SMSCONSTANT.PURCHASE_ORDER_MESSAGE, senderId, "defaultMSG", 4);
			messageService.sendMsgWithCountryDialCodeAsync(mobileNumber, message,
					purchaseVo.getContactVo().getCompanyId(), purchaseVo.getContactVo().getCompanyId(),
					SMSCONSTANT.PURCHASE_ORDER_MESSAGE, senderId, "defaultMSG", 4,purchaseVo.getContactVo().getCountryDialCodePrefix());
			}

		}
	}


	private void sendMessageToCutomer(PurchaseVo purchaseVo, HttpSession session) {
        if (purchaseVo.getContactVo() != null && (purchaseVo.getType().equals(Constant.PURCHASE_BILL)
                || purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE))) {
            long companyId = Long.parseLong(session.getAttribute("companyId").toString());
            String senderId = "VSYERP";
            String branchSenderId = userRepository.findSenderIdByuserFrontId(companyId);
			if(StringUtils.isNotBlank(branchSenderId)){
				senderId = branchSenderId;
			}
            CompanySettingVo settingVo = null;
            String message = "Thanks for sales.";
            if (purchaseVo.getContactVo().getMobNo() != null) {
				/*
				 * settingVo = companySettingService.findByCompanyIdAndType(companyId,
				 * Constant.ALLOWSUPPLIERBILLSMS); if (settingVo != null && settingVo.getValue()
				 * == 1) { //messageService.sendMsg(purchaseVo.getContactVo().getMobNo(),
				 * message, session);
				 * messageService.sendMsgAsync(purchaseVo.getContactVo().getMobNo(), message,
				 * purchaseVo.getContactVo().getCompanyId(),
				 * purchaseVo.getContactVo().getCompanyId(),SMSCONSTANT.POS_ORDER_MESSAGE,
				 * senderId,"defaultMSG",4); }
				 */
            }
            if (purchaseVo.getContactVo().getWhatsappNo() != null) {
				/*
				 * String token = (String) session.getAttribute("whatsappToken"); settingVo =
				 * companySettingService.findByCompanyIdAndType(companyId,
				 * Constant.ALLOWSUPPLIERBILLWHATSAPP); if (settingVo != null &&
				 * settingVo.getValue() == 1) {
				 * whatsappController.sendTextMessageToCustomer(purchaseVo.getContactVo().
				 * getWhatsappNo(), message, token, companyId, companyId); }
				 */
            }
            if (purchaseVo.getContactVo().getEmail() != null) {
				/*
				 * settingVo = companySettingService.findByCompanyIdAndType(companyId,
				 * Constant.ALLOWSUPPLIERBILLEMAIL); if (settingVo != null &&
				 * settingVo.getValue() == 1) {
				 *
				 * // String body = Converter.convertToHtml(servletRequest, //
				 * "/media/download/sales/" + salesvo.getPdfToken() + "/mail");
				 * sendGridEmailService.sendHTML(from, purchaseVo.getContactVo().getEmail(),
				 * "Supplier Bill", message, companyId); }
				 */
            }

        }
    }


	@GetMapping("{id}")
	public ModelAndView purchaseDetails(@PathVariable String type, @PathVariable long id, HttpSession session) {

        String rateLimitType;
        boolean hasEwaybillPermission = false;
        boolean hasEInvoicePermission = false;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_VIEW;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_VIEW;
                break;
            case Constant.PURCHASE_BILL:
               rateLimitType = RateLimitConstant.PURCHASE_BILL_VIEW;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_VIEW;
                hasEwaybillPermission = (MenuPermission.havePermission(session,type, Constant.EWAY_ACTION)==1);
                hasEInvoicePermission = (MenuPermission.havePermission(session,type, Constant.EINVOICE_ACTION)==1);
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_VIEW;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		ModelAndView view = new ModelAndView();
        view.addObject("ewaybillflag", hasEwaybillPermission);
        view.addObject("einvoiceflag", hasEInvoicePermission);

		int response = 0;
		if (Integer.parseInt(session.getAttribute(Constant.USER_TYPE).toString()) == Constant.URID_COMPANY) {
			// log.warning("Inside Contact Vos usertype == 2 >>>>>");
			response = purchaseService.countByPurchaseIdAndCompanyIdAndIsDeleted(id,
					Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
			// log.warning("response is >>>>" + response);
		} else {
			// log.warning("Inside Contact Vos else >>>>>>");
			if (Integer.parseInt(session.getAttribute(Constant.USER_TYPE).toString()) > Constant.URID_USER) {
				// log.warning("Inside Contact Vos usertype > 3 >>>");
				UserFrontVo userFrontVo = userService
						.findByUserFrontId(Long.parseLong(session.getAttribute(Constant.USERID).toString()));
				if ((userFrontVo.getUserFrontVo().getRoles().get(0).getUserRoleId()) == Constant.URID_COMPANY) {
					// log.warning("Inside Contact Vos usertype == 2 >>>");
					response = purchaseService.countByPurchaseIdAndCompanyIdAndIsDeleted(id,
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
				} else {
					// log.warning("Inside Contact Vos branch else >>>");
					response = purchaseService.countByPurchaseIdAndCompanyIdAndIsDeleted(id,
                            Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
				}
			} else {
				// log.warning("Inside Contact Vos branch else >>><<<<");
				response = purchaseService.countByPurchaseIdAndCompanyIdAndIsDeleted(id,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
			}
		}
		if (response == 0) {
			view.setViewName(Constant.ERROR_PAGE_404);
		} else {
			if (MenuPermission.havePermission(session, type, Constant.VIEW) == 1) {
				long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
				String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
                // Check if valid using the existing method
                boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
                view.addObject("isValidMerchantType", isValidMerchantType);
				PurchaseVo purchaseVo = new PurchaseVo();
				if (Integer.parseInt(session.getAttribute(Constant.USER_TYPE).toString()) == Constant.URID_COMPANY) {
					// log.warning("Inside Purchase Vo usertype == 2 ");
					purchaseVo = purchaseService.findByPurchaseIdAndCompanyId(id,
							Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

				} else {
					// log.warning("Inside Contact Vos else");
					if (Integer.parseInt(session.getAttribute(Constant.USER_TYPE).toString()) > Constant.URID_USER) {
						// log.warning("Inside Contact Vos usertype >= 3 ");
						UserFrontVo userFrontVo = userService
								.findByUserFrontId(Long.parseLong(session.getAttribute(Constant.USERID).toString()));
						if ((userFrontVo.getUserFrontVo().getRoles().get(0).getUserRoleId()) == Constant.URID_COMPANY) {
							// log.warning("Inside Contact Vos userfrontVo == 2 ");
							purchaseVo = purchaseService.findByPurchaseIdAndCompanyId(id,
									Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
						} else {
							// log.warning("Inside Contact Vos branch else >>>");
							purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
									Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
						}
					} else {
						// log.warning("Inside Contact Vos branch else >>><<<<");
						purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
								Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
					}
				}
				if (type.equals(Constant.PURCHASE_BILL)) {
					view.addObject("displayType", "Supplier Bill");
				} else if (type.equals(Constant.PURCHASE_ORDER)) {
					view.addObject("displayType", "Purchase Order");
				} else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
					view.addObject("displayType", "Debit Note");
				} else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
					view.addObject("displayType", "Material Inward");
				}
                Map<String,String> result = rILPurchaseBillInfoRepository.findCustomerSapCodeAndShipToCodeByPurchaseId(id);
                boolean isVatUser = false;
                if(userRepository.getTaxType(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())).equals(Constant.VAT)){
                    isVatUser = true;
                }
                view.addObject("isVatUser",isVatUser);
                view.addObject("customerCode",result.get("customerCode"));
                view.addObject("shipToCode",result.get("shipToCode"));
                view.addObject("isSapBill", rILPurchaseBillInfoRepository.countById(id) == 0 ? 0 : 1);
                view.addObject("rilPurchaseBillInfo", rILPurchaseBillInfoRepository.getPurchaseBillInfoByPurchaseId(id));
                view.addObject("isExport", MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT));
				view.addObject(Constant.EWAYBILL, companySettingService.findByBranchIdAndType(
						Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.EWAYBILL));
				view.addObject(Constant.ALLOWEINVOICE, companySettingService.findByBranchIdAndType(
						Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.ALLOWEINVOICE));
                if(MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) && type.equals(Constant.PURCHASE_BILL)){
                            Long stockTransferId = purchaseVo.getStockTransferId();
                            String stockTransferNo = "-";
                            if(stockTransferId != null && stockTransferId != 0){
                                stockTransferNo = stockTransferRepository.getStockTransferNoByStockTransferId(stockTransferId);
                            }
                            view.addObject("stockTransferNo",stockTransferNo);
                }

				if (purchaseVo == null || purchaseVo.getIsDeleted() == 1) {
					view.setViewName("accessdenied/datanotavailbal");
				} else {
//		            	view.addObject(Constant.STOCKBYMI, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYMI));
//		                view.addObject(Constant.STOCKBYBILL, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYBILL));
					view.addObject(Constant.ADDQTYBY, companySettingService.findByBranchIdAndType(
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.ADDQTYBY).getValue());
					purchaseVo.setCreatedbyname(userRepository.getName(purchaseVo.getCreatedBy()));
					view.addObject("accountLedger",purchaseVo.getAccountCustomId()!=0?accountCustomService.getAccountCustomNameByAccountCustomId(purchaseVo.getAccountCustomId()):"");
					if (type.equals(Constant.PURCHASE_BILL)) {

						CompanySettingVo setting = companySettingService.findByBranchIdAndType(
								Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.SUPPLIER_BILL);
						if (setting != null) {
							if (setting.getValue() == 1) {
								view.setViewName("purchase/purchase-view");
							} else {
								CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(
										Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()),
										Constant.PURCHASECONVERSATION);
								if (conversation != null) {
									if (conversation.getValue() == 1) {
										view.setViewName("purchase/purchase-view-conversation");
									} else {
										view.setViewName("purchase/purchase-view");
									}
								} else {
									view.setViewName("purchase/purchase-view");
								}
							}
						} else {
							CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(
									Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()),
									Constant.PURCHASECONVERSATION);
							if (conversation != null) {

								if (conversation.getValue() == 1) {

									view.setViewName("purchase/purchase-view-conversation");
								} else {
									view.setViewName("purchase/purchase-view");
								}
							} else {
								view.setViewName("purchase/purchase-view");
							}
						}

					} else if (type.equals(Constant.PURCHASE_ORDER)) {
						CompanySettingVo pobysalesqty = companySettingService.findByCompanyIdAndType(
								Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), Constant.POBYSALESQTY);
						view.addObject("pobysalesqty", pobysalesqty);
						view.addObject(Constant.POAPPROVAL, companySettingService.findByBranchIdAndType(
								Long.parseLong(session.getAttribute("branchId").toString()), Constant.POAPPROVAL));
						view.addObject(Constant.PURCHASE_ORDER + "IsApproveReject",
								MenuPermission.havePermission(session, type, Constant.APPROVE_REJECT_ACTION));

						// log.severe("pobysalesqty : " + pobysalesqty);
						if (pobysalesqty != null) {
							if (pobysalesqty.getValue() == 1) {
								view.addObject("CategoryList",
										categoryService.findByCompanyId(
												Long.parseLong(session.getAttribute("companyId").toString()),
												merchantTypeId, clusterId));
								view.addObject("BrandList",
										brandService.findByCompanyId(
												Long.parseLong(session.getAttribute("companyId").toString()),
												merchantTypeId, clusterId));
								view.setViewName("purchase/purchase-view-pobysalesqty");
							} else {
								view.setViewName("purchase/purchase-view");
							}
						} else {
							view.setViewName("purchase/purchase-view");
						}

					} else {
						view.setViewName("purchase/purchase-view");
					}
//		                CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.PURCHASECONVERSATION);
//		                  if(conversation!=null) {
//		                      if(conversation.getValue()==1) {
//		                          view.setViewName("purchase/purchase-view-conversation");
//		                      }else {
//		                          view.setViewName("purchase/purchase-view");
//		                      }
//		                  }else {
//		                      view.setViewName("purchase/purchase-view");
//		                  }
					try {
						purchaseVo.setShippingCountriesName(countryService
								.findByCountriesCode(purchaseVo.getShippingCountriesCode()).getCountriesName());
						purchaseVo.setShippingStateName(
								stateService.findByStateCode(purchaseVo.getShippingStateCode()).getStateName());
						purchaseVo.setShippingCityName(
								cityService.findByCityCode(purchaseVo.getShippingCityCode()).getCityName());

						purchaseVo.setBillingCountriesName(countryService
								.findByCountriesCode(purchaseVo.getBillingCountriesCode()).getCountriesName());
						purchaseVo.setBillingStateName(
								stateService.findByStateCode(purchaseVo.getBillingStateCode()).getStateName());
						purchaseVo.setBillingCityName(
								cityService.findByCityCode(purchaseVo.getBillingCityCode()).getCityName());
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (type.equals(Constant.PURCHASE_ORDER)) {
						if (purchaseVo != null) {
							List<Long> parent = purchaseService.checkBillgeneratedornot(purchaseVo.getPurchaseId(),
									Constant.PURCHASE_MATERIALINWARD,
									Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
							// System.err.println("HERE parent size in PURCHASE_ORDER is :"+parent.size());
							if (parent.size() == 0) {
								view.addObject("inwardgenerated", 0);
							} else {
								view.addObject("inwardgenerated", 1);
							}
						}
					}
					if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
						if (purchaseVo.getPurchaseVo() != null) {
							List<Long> parent = purchaseService.checkBillgeneratedornot(
									purchaseVo.getPurchaseVo().getPurchaseId(), Constant.PURCHASE_BILL,
									Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
							// System.err.println("HERE parent size is :"+parent.size());
							if (parent.size() == 0) {
								view.addObject("supplierbillgenerated", 0);
							} else {
								view.addObject("supplierbillgenerated", 1);
							}
						}
					}
                    if (type.equals(Constant.PURCHASE_BILL) || type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                        if (MapUtils.isNotEmpty(purchaseVo.getTdsJson())) {
                            view.addObject("tdsAccountLedger", (purchaseVo.getTdsJson().get("tds_account_id") != null ?
                                    accountCustomService.getAccountCustomNameByAccountCustomId((Integer.parseInt(purchaseVo.getTdsJson().get("tds_account_id").toString()))) : ""));
                            view.addObject("tdsAmount", (StringUtils.isNotBlank(purchaseVo.getTdsJson().get("tds_amount").toString())
                                    ? Double.parseDouble(purchaseVo.getTdsJson().get("tds_amount").toString()) : 0));

                        }
                        if (MapUtils.isNotEmpty(purchaseVo.getTcsJson())) {
                            view.addObject("tcsAccountLedger", (purchaseVo.getTcsJson().get("tcs_account_id") != null ?
                                    accountCustomService.getAccountCustomNameByAccountCustomId(Integer.parseInt(purchaseVo.getTcsJson().get("tcs_account_id").toString())) : ""));
                        }
                    }
					view.addObject(Constant.ALLOWATTACHMENTVALIDATION,
							companySettingService
									.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),
											Constant.ALLOWATTACHMENTVALIDATION)
									.getValue());

					// view.addObject(Constant.QUANTITYUPDATEBYMI,
					// companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()),
					// Constant.QUANTITYUPDATEBYMI));

//		              Map<String, String> map2 = purchaseVo.getPurchaseItemVos().stream()
//		                        .map(entry -> String.valueOf(entry.getProduct().getProductId()+"="+entry.getProduct().getName()).split("="))
//		                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
//		              //System.err.println("map2 is :"+map2.size());
//		              //System.err.println("map2 is :"+map2.toString());
					/*
					 * try { view.addObject("ProductSet", purchaseVo.getPurchaseItemVos().stream()
					 * .map(p-> p.getProduct().getProductId()).collect(Collectors.toSet())); } catch
					 * (Exception e) { e.printStackTrace(); }
					 */
					String mobile = "Mobile no. is not provided";
					try {
						if (!purchaseVo.getContactVo().getContactAddressVos().isEmpty()) {
							if (StringUtils
									.isNotBlank(purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo())) {
								//mobile = purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo();
								int prefix = purchaseVo.getContactVo().getContactAddressVos().get(0).getCountryDialCodePrefix();
								mobile = "+"+(prefix == 0 ? 91 : prefix)+"-" +purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo();
							}

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					purchaseVo.setBillingPhone(mobile);

//		                List<BarcodeMasterSettingVo> barcodesetting = barcodeMasterSettingRepository.findByCompanyIdOrderByBarcodeMasterVoBarcodeIdAsc(Long.parseLong(session.getAttribute("companyId").toString()));

					List<BarcodeMasterDTO> barcodesetting = barcodeMasterSettingRepository
							.findBarcodeDetailByCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));

					// log.warning("Size: " + barcodesetting.size());
                    if (StringUtils.isNotBlank(purchaseVo.getFlatDiscountType()) && StringUtils.equals(purchaseVo.getFlatDiscountType(), Constant.AMOUNT)) {
                        Map<String, Object> purchaseFlatDiscountData = purchaseService.calculateFlatDiscountPercentageFromAmount(purchaseVo.getPurchaseId());
                        if(!purchaseFlatDiscountData.isEmpty()){
                            view.addObject("flatDiscountInPercentage", Double.parseDouble(purchaseFlatDiscountData.get("flatDiscountPercentage").toString()));
                        }
                    }else{
                        view.addObject("flatDiscountInPercentage", 0.0);
                    }
					view.addObject("purchaseVo", purchaseVo);
					view.addObject("type", type);
					view.addObject("barcodesetting", barcodesetting);
//					view.addObject("isCanceled",
//		            		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.CANCELED));
					//view.addObject("isInsert", MenuPermission.havePermission(session, type, Constant.INSERT));
					view.addObject("isCanceled",1);
					view.addObject("isInsert", 1);

					if (type.equals(Constant.PURCHASE_BILL)) {
						if (purchaseVo != null && purchaseVo.getMaterialInwardIds() != null) {
							String billstring = "";
							String[] billNo = purchaseVo.getMaterialInwardIds().split(",");
							for (int i = 0; i < billNo.length; i++) {
								PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndCompanyId(
										Long.parseLong(billNo[i]),
                                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
								billstring = billstring + purchaseVo1.getBillNo() + ",";
							}
							// System.out.println("bill string-------"+billstring);
							if (billstring.endsWith(",")) {
								billstring = billstring.substring(0, billstring.length() - 1);
							}

							purchaseVo.setBillNumber(billstring);
						}
						// log.info(purchaseVo.getPurchaseId()+"==================");
						List<Map<String, String>> returnpurchaseItem = purchaseService
								.getpurchaseitemfromparentpurchase(purchaseVo.getPurchaseId(),session.getAttribute(Constant.FINANCIAL_YEAR).toString());
						// System.err.println("list"+returnpurchaseItem.size());
						view.addObject("returnpurchaseItem", returnpurchaseItem);
					}

				}
				DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
	   	         Date date = new Date();
	   	         view.addObject("serverdate", dateFormat2.format(date));
			} else {
				view.setViewName(Constant.ACCESSDENIED);
			}
		}
		/*
		 * it is done by Harshvardhan at Jan 17 2023, by this code issue occur that
		 * loading issue in view page loading so i comment out this code
		 */
//		try {
//			WoocommerceSetupVo wooCommerceVo = woocommerceSetupService.getWoocommerceDetail(Long.parseLong(session.getAttribute("companyId").toString()));
//			CompanySettingVo updatewooCommercepricefromsupplierbill = companySettingService.findByCompanyIdAndType(
//					Long.parseLong(session.getAttribute("companyId").toString()), Constant.UPDATEWOOCOMMERCEPRICEFROMSHUPPLIERBILL);
//			if (wooCommerceVo != null && wooCommerceVo.getAllowWooCommerce() != 0
//					&& updatewooCommercepricefromsupplierbill != null && updatewooCommercepricefromsupplierbill.getValue()==1){
//				List<ProductWooPriceDTO> productWooPriceDTOs = productService.findProductstoUpdatePriceInWooCommerce(
//						id,Long.parseLong(session.getAttribute("companyId").toString()));
//				if(!productWooPriceDTOs.isEmpty()) {
//					for(int i=0; i<productWooPriceDTOs.size();i++) {
//						ProductWooPriceDTO productWooPriceDTO = productWooPriceDTOs.get(i);
//
//						productService.updateShopifyPriceInProductVariant(productWooPriceDTO.getProductVarientId(),
//								Long.parseLong(session.getAttribute("companyId").toString()),productWooPriceDTO.getSellingPrice(),0);
//
//						wooService.updatePriceinProduct(id,Long.parseLong(session.getAttribute("companyId").toString()),
//								wooCommerceVo, productWooPriceDTO);
//					}
//				}
//
//			}
//		} catch(Exception e) {
//
//		}


		return view;
	}


    @RequestMapping("{id}/getproductset")
    @ResponseBody
    public Map<Long, String> getproductset(@PathVariable String type, @PathVariable long id, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PRODUCT_SET;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_PRODUCT_SET;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_PRODUCT_SET;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_PRODUCT_SET;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PRODUCT_SET;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                Long.parseLong(session.getAttribute("branchId").toString()));
    	Map<Long, String> map2 = new HashMap<>();
    	if(purchaseVo != null) {
    		for(int i=0;i<purchaseVo.getPurchaseItemVos().size();i++) {
        		try {
        			map2.put(purchaseVo.getPurchaseItemVos().get(i).getProduct().getProductId(), purchaseVo.getPurchaseItemVos().get(i).getProduct().getName());
        		}catch (Exception e) {
    				e.printStackTrace();			}

        	}
    	}

    	return map2;
    }

    @GetMapping("{id}/edit")
    public ModelAndView purchaseEdit(@PathVariable String type, @PathVariable long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EDIT;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_EDIT;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_EDIT;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_EDIT;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EDIT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	ModelAndView view = new ModelAndView();
    	int response = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        view.addObject("isPercentageDiscount", companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Constant.BATCHDISCOUNTVALUE).getValue());
    	if(response == 0) {
    	        view.setViewName(Constant.ERROR_PAGE_404);
    	} else {
    		if (MenuPermission.havePermission(session, type, Constant.EDIT) == 1) {
                boolean isPurchaseEditable = true;
                PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                        Long.parseLong(session.getAttribute("branchId").toString()));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate purchaseLocalDate = LocalDate.parse(purchaseVo.getPurchaseDate().toString());
                LocalDate firstDateFinancialYear = LocalDate.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString(), formatter);

                if (!purchaseLocalDate.isBefore(firstDateFinancialYear)) {
                if (type.equals(Constant.PURCHASE_ORDER)) {
                    int isPOApproval = companySettingService.findValueByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.POAPPROVAL);

                    if (isPOApproval == 1 && !purchaseVo.getStatus().equals("toapprove")) {
                        isPurchaseEditable = false;
                    } else if (isPOApproval == 0 && !purchaseVo.getStatus().equals(Constant.DRAFT) && !purchaseVo.getStatus().equals("partiallydelivered")) {
                        isPurchaseEditable = false;
                    }
                } else if (type.equals(Constant.PURCHASE_MATERIALINWARD) && purchaseVo.getStatus().equals("completed")) {
                    isPurchaseEditable = false;
                } else if (type.equals(Constant.PURCHASE_BILL) && purchaseVo.getStatus().equals("paid")) {
                    isPurchaseEditable = false;
                } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE) && !purchaseVo.getStatus().equals("open")) {
                    isPurchaseEditable = false;
                }

                if(isPurchaseEditable) {
                    long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
                    long userId = Long.parseLong(session.getAttribute(Constant.USERID).toString());
                    String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
                    long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
                    long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
                    long userType = Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString());
                    int taxVal = 0;
                    double tdsAmount = 0.0;
                    int allowSupplierMappingPrice = 1; // Used for Not fetching Supplier Mapping Price in PO By Sale Qty Page
                    // Check if valid using the existing method
                    boolean isValidMerchantType = MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId);
                    view.addObject("isValidMerchantType", isValidMerchantType);
                    if (session.getAttribute("governmentTaxType").toString().equals(Constant.VAT)) {
                        taxVal = 1;
                    }
                    view.addObject(Constant.STOPUMOWISEDECIMAL, companySettingService.findByCompanyIdAndType(companyId, Constant.STOPUMOWISEDECIMAL));
                    view.addObject(Constant.EXPIRY,
                            companySettingService.findByCompanyIdAndType(companyId, Constant.EXPIRY));
                    view.addObject(Constant.PRODUCTTYPE,
                            companySettingService.findByCompanyIdAndType(companyId, Constant.PRODUCTTYPE));
                    view.addObject(Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING,
                            companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING));
                    view.addObject(Constant.PURCHASEALLOWFOCUSON,
                            companySettingService.findByBranchIdAndType(branchId, Constant.PURCHASEALLOWFOCUSON));
                    view.addObject(Constant.ALLOWPURCHASEMERGEITEM,
                            companySettingService.findByBranchIdAndType(branchId, Constant.ALLOWPURCHASEMERGEITEM));
                    view.addObject(Constant.FATOORAHQRCODE,
                            companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.FATOORAHQRCODE));
                    if (type.equals(Constant.PURCHASE_BILL)) {
                        view.addObject("displayType", "Supplier Bill");
                    } else if (type.equals(Constant.PURCHASE_ORDER)) {
                        view.addObject("displayType", "Purchase Order");
                    } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                        view.addObject("displayType", "Debit Note");
                    } else if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                        view.addObject("displayType", "Material Inward");
                    }


                if (purchaseVo.getTdsJson() != null && !purchaseVo.getTdsJson().isEmpty()) {
                    int tds_applicable = (purchaseVo.getTdsJson().get(Constant.TDS_APPLICABLE) != null ? Integer.parseInt(purchaseVo.getTdsJson().get(Constant.TDS_APPLICABLE).toString()) : 0);
                    if (tds_applicable == 1) {
                        tdsAmount = (purchaseVo.getTdsJson().get(Constant.TDS_AMOUNT) != null ? Double.parseDouble(purchaseVo.getTdsJson().get(Constant.TDS_AMOUNT).toString()) : 0.0);
                    }
                }
                if (purchaseVo.getIsDeleted() == 1) {
                    view.setViewName("accessdenied/datanotavailbal");
                }else if (ewayBillOrEInvoiceGenerated(purchaseVo,redirectAttributes,Constant.EDIT_ACTION)){
                    return new ModelAndView("redirect:/purchase/" + type + "/" + id);
                }
                else if((purchaseVo.getPaidAmount() - tdsAmount)>0) {
                    view.setViewName("redirect:" + request.getHeader("Referer"));
                } else {
                     if(type.equals(Constant.PURCHASE_BILL)) {

                                CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(companyId, Constant.PURCHASECONVERSATION);
                                if(conversation!=null) {

                                    if(conversation.getValue()==1) {

                                        view.setViewName("purchase/purchase-edit-conversation");
                                    }else {
                                        view.setViewName("purchase/purchase-edit");
                                    }
                                }else {
                                    view.setViewName("purchase/purchase-edit");
                                }


                        }else  if(type.equals(Constant.PURCHASE_ORDER)) {
                          CompanySettingVo pobysalesqty = companySettingService.findByCompanyIdAndType(companyId, Constant.POBYSALESQTY);
                            view.addObject("pobysalesqty", pobysalesqty);
                            if(pobysalesqty!=null) {
                                if(pobysalesqty.getValue()==1) {
                                    allowSupplierMappingPrice = 0;
                                    view.addObject("CategoryList",categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
                                    view.addObject("BrandList",brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
                                    view.setViewName("purchase/purchase-edit-pobysalesqty");
                                }else {
                                    view.setViewName("purchase/purchase-edit");
                                }
                            }else {
                                view.setViewName("purchase/purchase-edit");
                            }

                        }else {
                            view.setViewName("purchase/purchase-edit");
                        }
//                  CompanySettingVo conversation = companySettingService.findByCompanyIdAndType(Long.parseLong(session.getAttribute("companyId").toString()), Constant.PURCHASECONVERSATION);
//                      if(conversation!=null) {
//                          if(conversation.getValue()==1) {
//                              view.setViewName("purchase/purchase-edit-conversation");
//                          }else {
//                              view.setViewName("purchase/purchase-edit");
//                          }
//                      }else {
//                          view.setViewName("purchase/purchase-edit");
//                      }


                     try {
 						purchaseVo.setShippingCountriesName(countryService
 								.findByCountriesCode(purchaseVo.getShippingCountriesCode()).getCountriesName());
 						purchaseVo.setShippingStateName(
 								stateService.findByStateCode(purchaseVo.getShippingStateCode()).getStateName());
 						purchaseVo.setShippingCityName(
 								cityService.findByCityCode(purchaseVo.getShippingCityCode()).getCityName());

 						purchaseVo.setBillingCountriesName(countryService
 								.findByCountriesCode(purchaseVo.getBillingCountriesCode()).getCountriesName());
 						purchaseVo.setBillingStateName(
 								stateService.findByStateCode(purchaseVo.getBillingStateCode()).getStateName());
 						purchaseVo.setBillingCityName(
 								cityService.findByCityCode(purchaseVo.getBillingCityCode()).getCityName());
 					} catch (Exception e) {
 						e.printStackTrace();
 					}

                    String mobile = "Mobile no. is not provided";
                    long addressId = 0;
                    try {
                        if(!purchaseVo.getContactVo().getContactAddressVos().isEmpty()) {
                            addressId = purchaseVo.getContactVo().getContactAddressVos().get(0).getContactAddressId();

                            if(StringUtils.isNotBlank(purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo())) {
                                //mobile = purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo();
                                int prefix = purchaseVo.getContactVo().getContactAddressVos().get(0).getCountryDialCodePrefix();
								mobile = "+"+(prefix == 0 ? 91 : prefix)+"-" +purchaseVo.getContactVo().getContactAddressVos().get(0).getPhoneNo();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    purchaseVo.setAddressId(addressId);
                    purchaseVo.setBillingPhone(mobile);
//                     view.addObject("ProductSet", purchaseVo.getPurchaseItemVos().stream()
//                           .map(p-> p.getProduct().getProductId()).collect(Collectors.toSet()));
                    try {
                        //view.addObject(Constant.QUANTITYUPDATEBYMI, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.QUANTITYUPDATEBYMI));
//                    	view.addObject(Constant.STOCKBYMI, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYMI).getValue());
//    	                view.addObject(Constant.STOCKBYBILL, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYBILL).getValue());
                    	view.addObject(Constant.ADDQTYBY, companySettingService.findByBranchIdAndType(branchId, Constant.ADDQTYBY).getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CompanySettingVo pobysalesqty = companySettingService.findByCompanyIdAndType(companyId, Constant.POBYSALESQTY);
                    view.addObject("pobysalesqty", pobysalesqty);
                    view.addObject("allowSupplierMappingPrice", allowSupplierMappingPrice);
                    //debit note regarding calculation
                    if(Constant.PURCHASE_DEBIT_NOTE.equalsIgnoreCase(type)){
                        double debitNoteAmount = 0,supplierBillAmount =0;
                        if(!purchaseVo.getPurchaseVo().getPurchaseItemVos().isEmpty()){
                            supplierBillAmount = purchaseVo.getPurchaseVo().getPurchaseItemVos().stream().mapToDouble(PurchaseItemVo::getNetAmount).sum();
                        }
                        List<PurchaseVo> purchaseVoList = purchaseRepository.findDebitNoteForSupplierBill(purchaseVo.getPurchaseVo().getPurchaseId(),purchaseVo.getPurchaseId());
                        if(!purchaseVoList.isEmpty()){
                            for(PurchaseVo previousPurchaseVo : purchaseVoList) {
                                debitNoteAmount = previousPurchaseVo.getPurchaseItemVos().stream().mapToDouble(PurchaseItemVo::getNetAmount).sum();
                            }
                        }
                        purchaseVo.setDebitNoteAmountSupplierBill(supplierBillAmount-debitNoteAmount);
                    }
                    //debit note regarding calculation
                    view.addObject("purchaseVo", purchaseVo);
                    view.addObject("type", type);
                    if (StringUtils.isNotBlank(purchaseVo.getTermsAndConditionIds())) {
//                        List<Long> termandconditionIds = Arrays.asList(purchaseVo.getTermsAndConditionIds().split("\\s*,\\s*"))
//                                .stream().map(Long::parseLong).collect(Collectors.toList());
//                        view.addObject("TermsAndCondition",
//                                purchaseTermsAndConditionService.getTermAndConditionList(termandconditionIds));
                        view.addObject("termsAndCondition", purchaseTermsAndConditionService.getTermsAndConditionIdByCompanyId(companyId, 1, 0));
                    }
//                  view.addObject("ContactList", contactService.contactList(
//                          Long.parseLong(session.getAttribute("companyId").toString()), Constant.CONTACT_SUPPLIER));

                    // view.addObject("ProductList",productService.findByCompanyIdAndIsDeleted(Long.parseLong(session.getAttribute("companyId").toString()),
                    // 0));
                    view.addObject(Constant.PRODUCTTYPE, companySettingService.findByCompanyIdAndType(companyId, Constant.PRODUCTTYPE));
                    view.addObject(Constant.MULTIBARCODE,companySettingService.findByCompanyIdAndType(companyId, Constant.MULTIBARCODE));
                    view.addObject(Constant.MULTIDUPLICATEBARCODE,companySettingService.findByCompanyIdAndType(companyId, Constant.MULTIDUPLICATEBARCODE));
                    view.addObject("paymentTermList", paymentTermService.findBybranchId(branchId, 0, companyId));
                    view.addObject("insert_supplier", MenuPermission.havePermission(session, Constant.CONTACT_SUPPLIER, Constant.INSERT));
                    view.addObject("NEWPRODUCTPERMISSION", MenuPermission.havePermission(session, Constant.PRODUCT, Constant.INSERT));
                    view.addObject("category", categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
                    view.addObject("brand", brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
                    view.addObject("EmployeeList",employeeService.getEmployeeByAssignedBranchId(branchId));
                    view.addObject("UomList", unitOfMeasurementService.findByCompanyIdAndIsDeleted(companyId, 0,merchantTypeId,clusterId));
                    view.addObject("CategoryList", categoryService.findByCompanyId(companyId,merchantTypeId,clusterId));
                    view.addObject("BrandList", brandService.findByCompanyId(companyId,merchantTypeId,clusterId));
                    view.addObject("contactlist", contactService.findByType(Constant.CONTACT_TRANSPORT,
			        branchId));
//                    view.addObject("TaxList",
//                            taxService.findByCompanyId(Long.parseLong(session.getAttribute("companyId").toString())));
//                    view.addObject("isItemCodeProduct",
//                    		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.ITEMCODE_PRODUCT));
                    view.addObject("isItemCodeProduct",1);
                    view.addObject("isQty", userType<5?1:MenuPermission.havePermission(session, type, Constant.QUANTITY));
                    view.addObject("isFreeQty", userType<5?1:MenuPermission.havePermission(session, type, Constant.FREE_QTY));
                    view.addObject("isUnitCostMrpSpMarginTaxType", userType<5?1:MenuPermission.havePermission(session, type, Constant.UNITCOST_MRP_SP_MARGIN_TAXTYPE));
                    view.addObject("isDiscount1", userType<5?1:MenuPermission.havePermission(session, type, Constant.DISCOUNT_1));
                    view.addObject("isDiscount2", userType<5?1:MenuPermission.havePermission(session, type, Constant.DISCOUNT_2));
                    view.addObject("isFlatDiscount", userType<5?1:MenuPermission.havePermission(session, type, Constant.FLAT_DISCOUNT));
                    view.addObject("isRoundOff", userType<5?1:MenuPermission.havePermission(session, type, Constant.ROUND_OFF));
                    view.addObject("isAdditionalChargeValue", userType<5?1:MenuPermission.havePermission(session, type, Constant.ADDITIONAL_CHARGES_VALUE));
                    view.addObject("isProductEdit", userType<5?1:MenuPermission.havePermission(session, Constant.PRODUCT, Constant.EDIT));
                    view.addObject("isContactEdit", userType<5?1:MenuPermission.havePermission(session, Constant.CONTACT, Constant.EDIT));
                    view.addObject("isAdditionalChargeAdd", userType<5?1:MenuPermission.havePermission(session, Constant.ADDITIONALCHARGE, Constant.INSERT));
                    view.addObject("isDebitNoteNew", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.INSERT));
                    view.addObject("isUnitCostMrpSpMarginTaxTypeDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.UNITCOST_MRP_SP_MARGIN_TAXTYPE));
                    view.addObject("isDiscount1DebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.DISCOUNT_1));
                    view.addObject("isDiscount2DebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.DISCOUNT_2));
                    view.addObject("isRoundOffDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.ROUND_OFF));
                    view.addObject("isQtyDebitNote", userType<5?1:MenuPermission.havePermission(session, Constant.PURCHASE_DEBIT_NOTE, Constant.QUANTITY));

//                    view.addObject("isCleared",
//                    		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.CLEARED));
//                    view.addObject("isUploadExcel",
//                    		Long.parseLong(session.getAttribute("userType").toString())<5?1:MenuPermission.havePermission(session, type, Constant.UPLOAD_EXCEL));
                    view.addObject("isCleared",1);
                    view.addObject("isUploadExcel",1);
                    List<TaxVo> taxVos=taxService.findByCompanyId(companyId,merchantTypeId,clusterId,taxVal);
                    view.addObject("TaxList",taxVos);
                    view.addObject("tax",taxVos);
                    int garmentIndustryTaxType=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTINDUSTRYTAXTYPE);
                    int garmentIndustryTaxTypeMethod=companySettingService.getvalueByCompanyIdAndType(companyId, Constant.GARMENTTAX_CALCULATION_METHOD);
                    int hsnTypeWiseCalculation= companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATION);
                    int hsnTypeWiseCalculationMethod = companySettingService.getvalueByCompanyIdAndType(companyId, Constant.HSNTYPEWISECALCULATIONMETHOD);
                    view.addObject("garmentIndustryTaxType", garmentIndustryTaxType);
                    view.addObject("garmentIndustryTaxTypeMethod",garmentIndustryTaxTypeMethod);
                    view.addObject("hsnTypeWiseCalculation",hsnTypeWiseCalculation);
                    view.addObject("hsnTypeWiseCalculationMethod",hsnTypeWiseCalculationMethod);
                    view.addObject("allowNegativeStock", companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWNEGATIVESTOCK));
                    String gstType= userRepository.getTaxTypeByUserFrontId(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                    if((garmentIndustryTaxType==1 || hsnTypeWiseCalculation == 1) && !gstType.equals(Constant.VAT)) {
                		int taxType = Constant.TAX_TYPE_GST;
                		try {
                			Map<String, String> gstMap = userRepository.getgstDetails(companyId);
                			if(gstMap!=null && !gstMap.isEmpty()) {
                				if(StringUtils.isNotBlank(gstMap.get("tax_type")) && StringUtils.equalsIgnoreCase(gstMap.get("tax_type"),Constant.VAT)) {
                					taxType = Constant.TAX_TYPE_VAT;
                				}
                			}
                		}catch (Exception e) {
                			e.printStackTrace();
                		}

        	            TaxVo taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(5, companyId,taxType,"");
        	            view.addObject("tax5name", taxVo.getTaxName());
        	            view.addObject("tax5rate", taxVo.getTaxRate());
        	            view.addObject("tax5id", taxVo.getTaxId());

        	            taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(12, companyId,taxType,"");
        	            view.addObject("tax12name", taxVo.getTaxName());
        	            view.addObject("tax12rate", taxVo.getTaxRate());
        	            view.addObject("tax12id", taxVo.getTaxId());

                        taxVo = taxService.findByTaxRateAndIsGlobalOrCompanyIdAndTaxTypeAndTaxCode(18, companyId,taxType,"");
                        view.addObject("tax18name", taxVo.getTaxName());
                        view.addObject("tax18rate", taxVo.getTaxRate());
                        view.addObject("tax18id", taxVo.getTaxId());
                    }
                    CompanySettingVo setting = companySettingService.findByBranchIdAndType(branchId, "taxIncluded");
                    view.addObject("taxIncluded",setting.getValue());
                    CompanySettingVo setting1 = companySettingService.findByBranchIdAndType(branchId, "purchaseTaxIncluded");
                    view.addObject("purchaseTaxIncluded",setting1.getValue());
                    view.addObject("productType",productTypeRepository.findAll());
                    view.addObject(Constant.ALLPRICESHOW,companySettingService.findByCompanyIdAndType(companyId, Constant.ALLPRICESHOW));
                    view.addObject(Constant.REATILERMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.REATILERMARGIN));
                    view.addObject(Constant.WHOLESALERMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.WHOLESALERMARGIN));
                    view.addObject(Constant.SELLINGMARGIN,companySettingService.findByCompanyIdAndType(companyId, Constant.SELLINGMARGIN));

                    List<String> types = new ArrayList<>();
                    types.add(Constant.REPORT_PURCHASE);
                    view.addObject(Constant.ALLOWROUNDOFF, companySettingService.findByCompanyIdAndType(companyId, Constant.ALLOWROUNDOFF));
                    if (!purchaseVo.getPurchaseAdditionalChargeVos().isEmpty()) {
                        view.addObject("AdditionalChargeVos", additionalChargeService
                                .findBycompanyId(companyId,branchId, 0,types));
                    }
                    view.addObject(Constant.BARCODESERIES, 0);
                    if (companyId == 202) {
                        CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                        view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
                    }
                    if (companyId == 415) {
                        CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                        view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
                    }

                    if (companyId == 436) {
                        CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                        view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
                    }


                    CompanySettingVo barcodegenrateserieswise = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEGENRATESERIESWISE);
                    if(barcodegenrateserieswise!=null) {
                        if(barcodegenrateserieswise.getValue()==1){
                            CompanySettingVo barcodemanage = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEMANAGE);
                            if(barcodemanage!=null && barcodemanage.getValue()==2) {
                                try {
                                    CompanySettingVo barcodeSeries = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODESERIES);
                                    CompanySettingVo barcodeprefix = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODEPREFIX);
                                 CompanySettingVo barcodelength = companySettingService.findByCompanyIdAndType(companyId, Constant.BARCODELENGTH);
                                 view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
                                 view.addObject(Constant.BARCODEPREFIX, barcodeprefix.getAddValue());
                                view.addObject(Constant.BARCODEGENRATESERIESWISE, 1);
                                view.addObject(Constant.BARCODELENGTH, barcodelength.getValue());
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }

                            }else if(barcodemanage!=null && barcodemanage.getValue()==3) {
                                try {
                                    CompanySettingVo barcodeSeries = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODESERIES);
                                    CompanySettingVo barcodeprefix = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODEPREFIX);
                                 CompanySettingVo barcodelength = companySettingService.findByBranchIdAndType(branchId, Constant.BARCODELENGTH);
                                     view.addObject(Constant.BARCODESERIES, barcodeSeries.getAddValue());
                                     view.addObject(Constant.BARCODEPREFIX, barcodeprefix.getAddValue());
                                     view.addObject(Constant.BARCODEGENRATESERIESWISE, 1);
                                    view.addObject(Constant.BARCODELENGTH, barcodelength.getValue());
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            }
                        }
                    }
                    if(type.equals(Constant.PURCHASE_BILL)) {
                        if(purchaseVo.getMaterialInwardIds() !=null) {
                            String billstring ="";
                            String[] billNo =  purchaseVo.getMaterialInwardIds().split(",");
                            for (int i = 0; i < billNo.length; i++) {
                                PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(Long.parseLong(billNo[i]), branchId);
                                billstring = billstring + purchaseVo1.getBillNo()+",";
                            }

                            if (billstring.endsWith(",")) {
                                billstring = billstring.substring(0, billstring.length() - 1);
                                }

                            purchaseVo.setBillNumber(billstring);
                        }
                        List<Map<String, String>> returnpurchaseItem = purchaseService.getpurchaseitemfromparentpurchase(purchaseVo.getPurchaseId(),session.getAttribute(Constant.FINANCIAL_YEAR).toString());

                        view.addObject("returnpurchaseItem",returnpurchaseItem);
                        for (int i = 0; i < returnpurchaseItem.size(); i++) {

                        }
                    }
                    List<String> groupNature = new  ArrayList<String>();
                    if (type.equals(Constant.PURCHASE_BILL)) {
                    	groupNature.add(Constant.ACCOUNT_PURCHASE);
                    	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(companyId, branchId,groupNature);
                    	view.addObject("accountCustomDTO", accountCustomDTO);
                    } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                    	groupNature.add(Constant.ACCOUNT_PURCHASE_RETURN);
                    	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(companyId, branchId,groupNature);
                    	view.addObject("accountCustomDTO", accountCustomDTO);
                    }
                    if (type.equals(Constant.PURCHASE_BILL) || type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                        String tanNo = "";
                        if ((Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_COMPANY) ||
                                (Integer.parseInt(session.getAttribute("userType").toString()) == Constant.URID_FRANCHISE) || (Integer.parseInt(session.getAttribute("parentUserType").toString()) == Constant.URID_FRANCHISE)) {
                            tanNo = profileService.getTanNo(branchId);
                            view.addObject("tanNo", tanNo);
                        } else {
                            tanNo = profileService.getTanNo(companyId);
                            view.addObject("tanNo", tanNo);
                        }
                        if (StringUtils.isNotBlank(tanNo) ) {
                            List<Map<String, String>> tcsLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TCS);
                            List<Map<String, String>> tdsLedgerlist = accountCustomService.findTDSTCSLedgers(companyId, branchId, Constant.ACCOUNT_GROUP_TDS);
                            view.addObject("tdsLedgerlist", tdsLedgerlist);
                            view.addObject("tcsLedgerlist", tcsLedgerlist);
                        }
                    }
                    if(purchaseVo.getFlatDiscount()>0) {
	                	view.addObject("FLATDISCOUNT",true);
	                }else {
	                	view.addObject("FLATDISCOUNT",false);
	                }


                    if (StringUtils.isBlank(purchaseVo.getFlatDiscountType())){
                        view.addObject("flatDiscountType", Constant.PERCENTAGE);
                    }else{
                        view.addObject("flatDiscountType", purchaseVo.getFlatDiscountType());
                    }
                }

             DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
   	         Date date = new Date();
   	         view.addObject("serverdate", dateFormat2.format(date));

                try{
                    if (Long.parseLong((session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE)!=null?session.getAttribute(Constant.ALLOW_CONTACT_TYPESENSE):"0").toString()) == 1
                            && (merchantTypeId == 0 || merchantTypeId == 1)) {

                        int accountingType = Integer.parseInt(session.getAttribute("accountingType").toString());

                        int userTypeInt = Integer.parseInt(session.getAttribute("userType").toString());

                        if (userTypeInt > Constant.URID_USER){
                            userTypeInt = Integer.parseInt(session.getAttribute("parentUserType").toString());
                        }

                        String typesenseCollectionName = typesenseService.getCollectionName(branchId, companyId, accountingType, userTypeInt, Constant.CONTACT_SUPPLIER);
                        log.warning("typesenseCollectionName : "+typesenseCollectionName);
                        view.addObject("typesenseCollectionName", typesenseCollectionName);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
           } else {
                redirectAttributes.addFlashAttribute("isPurchaseEditable", 0);
                view.setViewName("redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId());
           }
                }else{
                    redirectAttributes.addFlashAttribute("isPurchaseEditableInCurrentYear", 0);
                    view.setViewName("redirect:/purchase/" + type + "/" + purchaseVo.getPurchaseId());
                }
            } else {
                view.setViewName(Constant.ACCESSDENIED);
            }
    	}

        return view;
    }

    private boolean ewayBillOrEInvoiceGenerated(PurchaseVo purchaseVo, RedirectAttributes model,String operation) {
        String orderNo = StringUtils.defaultIfEmpty(purchaseVo.getBillNo(), purchaseVo.getPrefix() + purchaseVo.getPurchaseNo()).trim();
        String action =  Constant.EDIT_ACTION.equals(operation) ? Constant.EDIT_ACTION : Constant.ISDELETE;

        if (purchaseVo.getEwayBillNo() != 0) {
            model.addFlashAttribute("ewaybilloreinvoicemsg", "Please cancel E-way bill to " + action + ": " + orderNo);
            return true;
        } else if (StringUtils.isNotBlank(purchaseVo.getIrnNo())) {
            model.addFlashAttribute("ewaybilloreinvoicemsg", "Please cancel E-Invoice bill to " + action + ": " + orderNo);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("{id}/delete")
    public ModelAndView purchaseDelete(@PathVariable String type, @PathVariable long id, HttpSession session,
                                       HttpServletRequest request,RedirectAttributes model) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DELETE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DELETE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DELETE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DELETE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DELETE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        ModelAndView view = new ModelAndView();
        int response = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if(response == 0) {
                view.setViewName(Constant.ERROR_PAGE_404);
        } else {
//        	double miqty;
//            double listmiqty=0;
            BigDecimal miqty = BigDecimal.ZERO;
            BigDecimal listmiqty = BigDecimal.ZERO;
            String postatus="";
            if (MenuPermission.havePermission(session, type, Constant.DELETE) == 1) {
                PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                        Long.parseLong(session.getAttribute("branchId").toString()));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate purchaseLocalDate = LocalDate.parse(purchaseVo.getPurchaseDate().toString());
                LocalDate firstDateFinancialYear = LocalDate.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString(), formatter);

                if (!purchaseLocalDate.isBefore(firstDateFinancialYear)) {
                if (ewayBillOrEInvoiceGenerated(purchaseVo,model,Constant.ISDELETE)){
                    view.setViewName("redirect:/purchase/" + type + "/" + id);
                    return view;
                }
                if (purchaseVo.getType().equals(Constant.PURCHASE_BILL)) {
                    //delete po item received qty
                    purchaseService.deletepurchasereceiveQty(purchaseVo);
                    if (purchaseVo.getStockTransferId() != 0) {
                        int count = purchaseService.countByStockTransferIdAndIsDeletedAndBranchId(purchaseVo.getStockTransferId(), 0, Long.parseLong(session.getAttribute("branchId").toString()));
                        // log.info("count :::::::::::::::::::"+count);
                        if (count == 1) {
                            stockTransferService.updatestatusBystocktransferId(purchaseVo.getStockTransferId(), Constant.STOCK_TRANSFER_OPEN);
                            stockTransferService.updateStockTransferMappingForPurchaseId(0, purchaseVo.getStockTransferId());

                            if (purchaseVo.getPurchaseVo() != null) {
                                if (purchaseVo.getPurchaseVo().getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                                    purchaseService.cancelPurchaseDetails(purchaseVo.getPurchaseVo());
                                }
                            }
                        }
                    } else{
                        //System.err.println("in billasa---> ");

                        //woocommerce stock update

                        //End woocommerce stock update

                        if (purchaseVo.getPurchaseVo() != null) {
                            long mIId = purchaseVo.getPurchaseVo().getPurchaseId();
                            if (purchaseVo.getPurchaseVo().getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                                //MI status update
                                purchaseService.updatePurchaseStatus(mIId, "open");
                                //System.err.println("update calll---> ");

                                //MI status receive qty update
                                PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(mIId, Long.parseLong(session.getAttribute("branchId").toString()));
                                if (purchaseVo1.getPurchaseItemVos() != null) {
                                    for (int i = 0; i < purchaseVo1.getPurchaseItemVos().size(); i++) {
                                        ////System.err.println("itemid ---> "+purchaseVo1.getPurchaseItemVos().get(i).getPurchaseItemId());
                                        ////System.err.println("qty --->"+purchaseVo1.getPurchaseItemVos().get(i).getQty());
                                        purchaseService.updatePurchaseReceivedqtyInPurchaseItem(purchaseVo1.getPurchaseItemVos().get(i).getPurchaseItemId(), purchaseVo1.getPurchaseItemVos().get(i).getQty());
                                    }
                                }

                                //po status update
                                if (purchaseVo.getPurchaseVo().getPurchaseVo() != null) {
                                    long poId = purchaseVo.getPurchaseVo().getPurchaseVo().getPurchaseId();
                                    double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                                    PurchaseVo po = purchaseService.findByPurchaseIdAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                                    List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(po.getPurchaseId());
                                    for (int i = 0; i < mIVo.size(); i++) {
                                        if (mIVo.get(i).getPurchaseItemVos() != null) {
                                            BigDecimal qty = mIVo.get(i).getPurchaseItemVos().stream()
                                                    .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            listmiqty = listmiqty.add(qty);
//                                        listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                        }
                                    }
                                    miqty = listmiqty;
                                    BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                                    if (miqty.compareTo(poqtyBigDecimal) == 0) {
                                        postatus = "delivered";
                                    } else if (miqty.compareTo(poqtyBigDecimal) < 0) {
                                        if (miqty.compareTo(BigDecimal.ZERO) == 0) {
                                            postatus = Constant.DRAFT;
                                        } else {
                                            postatus = "partiallydelivered";
                                        }
                                    } else if (miqty.compareTo(poqtyBigDecimal) > 0) {
                                        postatus = "exceed";
                                    } else {
                                        postatus = Constant.DRAFT;
                                    }

//                         		if(miqty == poqty) {
//                                 	postatus = "delivered";
//                                 }else if (miqty< poqty) {
//                                	 if(miqty==0) {
//                                		 postatus = Constant.DRAFT;
//                                	 }else {
//                                		 postatus = "partiallydelivered";
//                                	 }
//                 				}else if (miqty> poqty) {
//                                 	postatus = "exceed";
//                 				}else {
//                 					postatus = Constant.DRAFT;
//                 				}
                                    purchaseService.updatePurchaseStatus(poId, postatus);

                                }

                            } else if (purchaseVo.getPurchaseVo().getType().equals(Constant.PURCHASE_ORDER)) {
                                purchaseService.updatePurchaseReceivedqty(purchaseVo.getPurchaseVo().getPurchaseId(), 0);
                                purchaseService.updatePurchaseStatus(purchaseVo.getPurchaseVo().getPurchaseId(), Constant.DRAFT);
                            }
                        } else {
                            if (purchaseVo.getMaterialInwardIds() != null) {
                                String[] billNo = purchaseVo.getMaterialInwardIds().split(",");
                                for (int j = 0; j < billNo.length; j++) {
                                    PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(Long.parseLong(billNo[j]), Long.parseLong(session.getAttribute("branchId").toString()));
                                    long mIId = purchaseVo1.getPurchaseId();

                                    if (purchaseVo1.getPurchaseItemVos() != null) {
                                        for (int k = 0; k < purchaseVo1.getPurchaseItemVos().size(); k++) {
                                            //System.err.println("update calll---> ");
                                            purchaseService.updatePurchaseReceivedqtyInPurchaseItem(purchaseVo1.getPurchaseItemVos().get(k).getPurchaseItemId(), purchaseVo1.getPurchaseItemVos().get(k).getReceiveQty());
                                        }
                                    }


                                    if (purchaseVo1.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {

                                        //MI status update

                                        double materialInwardQty = purchaseService.getTotalQtyByPurchaseAndBranchId(mIId, Long.parseLong(session.getAttribute("branchId").toString()));
                                        double totalBillQty = purchaseService.findTotalQtyOfPurchasesByIsDeleted(mIId,
                                                Long.parseLong(session.getAttribute(Constant.BRANCH_ID).toString()), Constant.PURCHASE_BILL, "bymultipleinwardids");
                                        ////System.err.println("here calll updaet updside   status"+poqty+":::::"+totalbillQty);
                                        if (totalBillQty <= materialInwardQty) {
                                            //update status of MI
                                            purchaseService.updatePurchaseStatus(mIId, "open");
                                        }
                                        //po status update
                                        if (purchaseVo1.getPurchaseVo() != null) {

                                            long poId = purchaseVo1.getPurchaseVo().getPurchaseId();

                                            double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                                            PurchaseVo po = purchaseService.findByPurchaseIdAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
                                            List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(po.getPurchaseId());

                                            for (int i = 0; i < mIVo.size(); i++) {
                                                if (mIVo.get(i).getPurchaseItemVos() != null) {
                                                    BigDecimal qty = mIVo.get(i).getPurchaseItemVos().stream()
                                                            .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                    listmiqty = listmiqty.add(qty);
                                                    //listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                                }
                                            }
                                            miqty = listmiqty;
                                            BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                                            if (miqty.compareTo(poqtyBigDecimal) == 0) {
                                                postatus = "delivered";
                                            } else if (miqty.compareTo(poqtyBigDecimal) < 0) {
                                                if (miqty.compareTo(BigDecimal.ZERO) == 0) {
                                                    postatus = Constant.DRAFT;
                                                } else {
                                                    postatus = "partiallydelivered";
                                                }
                                            } else if (miqty.compareTo(poqtyBigDecimal) > 0) {
                                                postatus = "exceed";
                                            } else {
                                                postatus = Constant.DRAFT;
                                            }
//    	                         		if(miqty == poqty) {
//    	                                 	postatus = "delivered";
//    	                                 }else if (miqty< poqty) {
//    	                                	 if(miqty==0) {
//    	                                		 postatus = Constant.DRAFT;
//    	                                	 }else {
//    	                                		 postatus = "partiallydelivered";
//    	                                	 }
//    	                 				}else if (miqty> poqty) {
//    	                                 	postatus = "exceed";
//    	                 				}else {
//    	                 					postatus = Constant.DRAFT;
//    	                 				}
                                            purchaseService.updatePurchaseStatus(poId, postatus);
                                        }
                                    }
                                }
                            }
                        }
                    stockTransactionService.deleteStockTransactionPurchase(purchaseVo.getBranchId(),
                            purchaseVo.getPurchaseId(), "purchase");
                    transactionService.deleteTransaction(purchaseVo.getBranchId(), purchaseVo.getPurchaseId(),
                            purchaseVo.getType());
                    if (purchaseVo.getDebitNoteId() != 0) {
                        PurchaseVo purchasevo1 = purchaseService.findByPurchaseIdAndBranchId(purchaseVo.getDebitNoteId(), purchaseVo.getBranchId());
                        // log.info("type of before vo check purchasevo1---"+purchasevo1.getType());
                        if (purchasevo1 != null) {
                            // log.info("type of in supplier bill---"+purchasevo1.getType());
                            ////System.err.println("ids----"+purchasevo1.getPurchaseId());
                            transactionService.deleteTransaction(purchaseVo.getBranchId(), purchasevo1.getPurchaseId(), purchasevo1.getType());
                            stockTransactionService.deleteStockTransactionPurchase(purchaseVo.getBranchId(), purchasevo1.getPurchaseId(), purchasevo1.getType());
                            purchaseService.deletePurchase(purchaseVo.getBranchId(), purchasevo1.getPurchaseId(), purchasevo1.getType());
                            purchaseService.updatedebitNoteId(purchaseVo.getPurchaseId(), 0);

                        }
                    }

                    try {
                        if (purchaseVo.getPurchaseVo() != null) {
                            long poId = purchaseVo.getPurchaseVo().getPurchaseId();
                            double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));

                            List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(poId);
                            for (int i = 0; i < mIVo.size(); i++) {
                                if (mIVo.get(i).getPurchaseItemVos() != null) {
                                    BigDecimal qty = mIVo.get(i).getPurchaseItemVos().stream()
                                            .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    listmiqty = listmiqty.add(qty);
                                    // listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();

                                }
                            }
                            if (purchaseVo.getPurchaseItemVos() != null) {
                                BigDecimal qty = purchaseVo.getPurchaseItemVos().stream()
                                        .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                miqty = listmiqty.subtract(qty);
                                BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                                if (miqty.compareTo(poqtyBigDecimal) == 0) {
                                    postatus = "delivered";
                                } else if (miqty.compareTo(poqtyBigDecimal) < 0) {
                                    if (miqty.compareTo(BigDecimal.ZERO) == 0) {
                                        postatus = Constant.DRAFT;
                                    } else {
                                        postatus = "partiallydelivered";
                                    }
                                } else if (miqty.compareTo(poqtyBigDecimal) > 0) {
                                    postatus = "exceed";
                                } else {
                                    postatus = Constant.DRAFT;
                                }
                                //miqty = listmiqty- purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
//                        		if(miqty == poqty) {
//                                	postatus = "delivered";
//                                }else if (miqty < poqty) {
//                               	 if(miqty==0) {
//                               		 postatus = Constant.DRAFT;
//                               	 }else {
//                               		 postatus = "partiallydelivered";
//                               	 }
//                				}else if (miqty > poqty) {
//                                	postatus = "exceed";
//                				}else {
//                					postatus = Constant.DRAFT;
//                				}
                                purchaseService.updatePurchaseStatus(poId, postatus);
                                if (miqty.compareTo(poqtyBigDecimal) <= 0) {
                                    purchaseService.updatePurchaseReceivedqty(poId, miqty.doubleValue());
                                } else {
                                    purchaseService.updatePurchaseReceivedqty(poId, poqty);
                                }
//                                if (miqty <= poqty) {
//                                    purchaseService.updatePurchaseReceivedqty(poId, miqty);
//                                } else {
//                                    purchaseService.updatePurchaseReceivedqty(poId, poqty);
//                                }
                            } else {
                                purchaseVo.setStatus("mi created");
                            }
                            //purchaseService.updatePurchaseStatus(purchaseVo.getPurchaseVo().getPurchaseId(), "in progress");
                        }
                        //woocommerce & shopify stock update
//                   	 try {
//                     	for (PurchaseItemVo PurchaseItemVo : purchaseVo.getPurchaseItemVos()) {
//                     		ProductVo product =  PurchaseItemVo.getProductVarientsVo().getProductVo();
//                     		if(product != null) {
//                     			wooService.syncAllProductStockInWooCommerce(session,product.getProductId());
//                             }
//
//                     		try {
//         						if(PurchaseItemVo.getProductVarientsVo()!=null) {
//         			                if (shopifySetupVo != null && responce!=null&& responce.isStatus()) {
//         			                	log.info("in purchase bill");
//         			                    shopifyService.updateStockadjustment(PurchaseItemVo.getProductVarientsVo().getProductVarientId(), purchaseVo.getCompanyId());
//         			                }
//         						}
//         					} catch (Exception e) {
//         						e.printStackTrace();
//         					}
//                     	}
//                         }catch (Exception e) {
//             				// TODO: handle exception
//             			}
                        //End woocommerce & shopify stock update
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                } else if (purchaseVo.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
                	//delete po item received qty
                	// log.info("dlt transaction started");
                	purchaseService.deletepurchasereceiveQty(purchaseVo);
                	//purchaseService.updatePurchaseReceivedqty(purchaseVo.getPurchaseVo().getPurchaseId() , 0);
                	stockTransactionService.deleteStockTransactionPurchase(purchaseVo.getBranchId(),
                            purchaseVo.getPurchaseId(), purchaseVo.getType());
                    transactionService.deleteTransaction(purchaseVo.getBranchId(), purchaseVo.getPurchaseId(),
                            purchaseVo.getType());

                    //woocommerce stock update

                  //End woocommerce stock update
                    try {
                    	 if(purchaseVo.getPurchaseVo() != null) {
                    		 long poId = purchaseVo.getPurchaseVo().getPurchaseId();
                         	double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));

                         	List<PurchaseVo> mIVo = purchaseService.findByParentpurchaseVo(poId);
                             for (int i = 0; i < mIVo.size(); i++) {
                                 if (mIVo.get(i).getPurchaseItemVos() != null) {
                                     BigDecimal qty = mIVo.get(i).getPurchaseItemVos().stream()
                                             .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                             .reduce(BigDecimal.ZERO, BigDecimal::add);
                                     listmiqty = listmiqty.add(qty);
                                     //listmiqty = listmiqty + mIVo.get(i).getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                 }
                             }
                         	if (purchaseVo.getPurchaseItemVos() != null) {
                         		 // miqty = listmiqty- purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
                                BigDecimal qty = purchaseVo.getPurchaseItemVos().stream()
                                        .map(q -> new BigDecimal(String.valueOf(q.getQty())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal poqtyBigDecimal = new BigDecimal(String.valueOf(poqty));
                                if (miqty.compareTo(poqtyBigDecimal) == 0) {
                                    postatus = "delivered";
                                } else if (miqty.compareTo(poqtyBigDecimal) < 0) {
                                    if (miqty.compareTo(BigDecimal.ZERO) == 0) {
                                        postatus = Constant.DRAFT;
                                    } else {
                                        postatus = "partiallydelivered";
                                    }
                                } else if (miqty.compareTo(poqtyBigDecimal) > 0) {
                                    postatus = "exceed";
                                } else {
                                    postatus = Constant.DRAFT;
                                }
//                                 if(miqty == poqty) {
//                                 	postatus = "delivered";
//                                 }else if (miqty < poqty) {
//                                	 if(miqty==0) {
//                                		 postatus = Constant.DRAFT;
//                                	 }else {
//                                		 postatus = "partiallydelivered";
//                                	 }
//                 				}else if (miqty > poqty) {
//                                 	postatus = "exceed";
//                 				}else {
//                 					postatus = Constant.DRAFT;
//                 				}
                                 purchaseService.updatePurchaseStatus(poId, postatus);
                                if(miqty.compareTo(poqtyBigDecimal) <= 0) {
                                    purchaseService.updatePurchaseReceivedqty(poId, miqty.doubleValue());
                                } else {
                                    purchaseService.updatePurchaseReceivedqty(poId, poqty);
                                }
//                                 if(miqty <=poqty) {
//                        			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
//                        		}else {
//                        			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
//                        		}
                             }else {
                             	purchaseVo.setStatus("mi created");
                             }
                         	//purchaseService.updatePurchaseStatus(purchaseVo.getPurchaseVo().getPurchaseId(), "in progress");
                         }


                    	//woocommerce & Shopify stock update
//                    	 try {
//                         	for (PurchaseItemVo PurchaseItemVo : purchaseVo.getPurchaseItemVos()) {
//                         		ProductVo product =  PurchaseItemVo.getProductVarientsVo().getProductVo();
//                         		if(product != null) {
//                         			//wooCommerceService.updateProductStockInWooCommerce(product.getCompanyId(), product);
//                         			wooService.syncAllProductStockInWooCommerce(session,product.getProductId());
//                                     }
//
//                         		try {
//             						if(PurchaseItemVo.getProductVarientsVo()!=null) {
//             			                if (shopifySetupVo != null && responce!=null&& responce.isStatus()) {
//             			                	log.info("in material inward bill");
//             			                    shopifyService.updateStockadjustment(PurchaseItemVo.getProductVarientsVo().getProductVarientId(), purchaseVo.getCompanyId());
//             			                }
//             						}
//             					} catch (Exception e) {
//             						e.printStackTrace();
//             					}
//                         	}
//                             }catch (Exception e) {
//                 				// TODO: handle exception
//                 			}
                    	//END woocommerce & Shopify stock update
    				} catch (Exception e) {
    					e.printStackTrace();
    				}

                }else if (purchaseVo.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
                	if(purchaseVo.getPurchaseVo()!=null) {
                		//delete bill item received qty
                    	purchaseService.deletepurchasereceiveQty(purchaseVo);
                		long parentpurchaseId = purchaseVo.getPurchaseVo().getPurchaseId();
                		double total = purchaseVo.getTotal();
            			purchaseService.UpdateoldDebitnoteAmount(total, parentpurchaseId);
                        try {
                            if (purchaseVo.getIsDNbyBill() == 1 && purchaseVo.getPurchaseVo().getDebitNoteId() != 0) {
                                purchaseService.updatedebitNoteId(parentpurchaseId, 0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                	}

                    transactionService.deleteTransaction(purchaseVo.getBranchId(), purchaseVo.getPurchaseId(),
                            purchaseVo.getType());
                    stockTransactionService.deleteStockTransactionPurchase(purchaseVo.getBranchId(),
                            purchaseVo.getPurchaseId(), purchaseVo.getType());

                    //woocommerce stock update
//                    try {
//                    	for (PurchaseItemVo PurchaseItemVo : purchaseVo.getPurchaseItemVos()) {
//                    		ProductVo product =  PurchaseItemVo.getProductVarientsVo().getProductVo();
//                    		if(product != null) {
//                    			//wooCommerceService.updateProductStockInWooCommerce(product.getCompanyId(), product);
//                    			wooService.syncAllProductStockInWooCommerce(session,product.getProductId());
//                            }
//
//                    		try {
//        						if(PurchaseItemVo.getProductVarientsVo()!=null) {
//        			                if (shopifySetupVo != null && responce!=null&& responce.isStatus()) {
//        			                	log.info("in debit note");
//        			                    shopifyService.updateStockadjustment(PurchaseItemVo.getProductVarientsVo().getProductVarientId(), purchaseVo.getCompanyId());
//        			                }
//        						}
//        					} catch (Exception e) {
//        						e.printStackTrace();
//        					}
//                    	}
//                        }catch (Exception e) {
//            				// TODO: handle exception
//            			}
                  //End woocommerce stock update
                }
                purchaseService.deletePurchase(Long.parseLong(session.getAttribute("branchId").toString()), id, type);
                String referer = request.getHeader("Referer");
                if (referer != null && referer.contains("/purchase/")) {
                    view.setViewName("redirect:/purchase/" + type);
                } else {
                    view.setViewName("redirect:" + request.getHeader("Referer"));
                }
                //woocommerce & shopify stock update
              	 try {
                	for (PurchaseItemVo PurchaseItemVo : purchaseVo.getPurchaseItemVos()) {
                		ProductVo product =  PurchaseItemVo.getProductVarientsVo().getProductVo();
                		if(product != null) {
                			wooService.syncAllProductStockInWooCommerce(session,product.getProductId());
                        }

                		try {
                            if (purchaseVo.getBranchId() == purchaseVo.getCompanyId()) {
                                if (PurchaseItemVo.getProductVarientsVo() != null) {
                                    shopifyServiceNew.updateStockAdjustmentByProductVariantId(new ArrayList<Long>() {{
                                        add(PurchaseItemVo.getProductVarientsVo().getProductVarientId());
                                    }}, purchaseVo.getCompanyId());
                                }
                            }
        				} catch (Exception e) {
        					e.printStackTrace();
        				}
                	}
                    }catch (Exception e) {
        				e.printStackTrace();
        			}
              //End woocommerce & shopify stock update
                }else {
                    model.addFlashAttribute("isPurchaseDeletableInCurrentYear", 0);
                    view.setViewName("redirect:/purchase/" + type + "/" + id);
                }
            } else {
                view.setViewName(Constant.ACCESSDENIED);
            }
        }
        return view;

        // return "redirect:"+request.getHeader("Referer");
    }

    @PostMapping("{id}/barcode/{size}")
    public void purchaseBarcode(@PathVariable String type, @PathVariable String size, @PathVariable long id,
            @RequestParam String productId, @RequestParam Map<String, String> allRequestParams, HttpSession session,
            HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_BARCODE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_BARCODE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_BARCODE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_BARCODE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_BARCODE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	 long merchantTypeId = Long.parseLong(session.getAttribute("merchantTypeId").toString());
         String clusterId = session.getAttribute("clusterId").toString();


        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()));
        if(purchaseVo == null) {
            response.sendRedirect("/404");
        } else {
        	List<String> productVariantids = Collections.emptyList();
            if (productId != null && !productId.isEmpty()) {
                String[] productIds = productId.split(",");
                List<String> list = Arrays.asList(productIds);
                productVariantids = purchaseVo.getPurchaseItemVos().stream()
                        .filter(x -> list.contains(String.valueOf(x.getProduct().getProductId())))
                        .map(p -> String.valueOf(p.getProductVarientsVo().getProductVarientId()))
                        .collect(Collectors.toList());
            }
            List<ProductVo> productVos = productService.productDetail(id,
                    Long.parseLong(session.getAttribute("companyId").toString()),merchantTypeId,clusterId);

            HashMap jasperParameter = new HashMap();
            jasperParameter.put("logoserver", FILE_UPLOAD_SERVER);
            jasperParameter.put("company_id", Long.parseLong(session.getAttribute("companyId").toString()));
            jasperParameter.put("branch_id", purchaseVo.getBranchId());
            jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
            jasperParameter.put("purchase_id", id);
            //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


            String ss = CurrentDateTime.getTodayDate();
            jasperParameter.put("pkgdate", ss);
            jasperParameter.put("qty", 0);
            long blankNo = 0;
            if(StringUtils.isNotBlank(allRequestParams.get("blankNo"))) {
            	try {
            		blankNo = Long.parseLong(allRequestParams.get("blankNo"));
    			} catch (NumberFormatException ne) {
    				ne.printStackTrace();
    				// log.severe("NumberFormatException parsing error :"+ne.getMessage());
    			}

            }
            jasperParameter.put("blank_no", blankNo);
            jasperParameter.put("product_id", productVariantids);
            jasperParameter.put("from_barcode_no", "");
            jasperParameter.put("to_barcode_no", "");
            jasperParameter.put("price_type", allRequestParams.get("contactType"));
            jasperParameter.put("path",JASPER_REPORT_PATH + File.separator);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat dateFormatmonth = new SimpleDateFormat("MM/yyyy");

            jasperParameter.put("mfg", dateFormat.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
            String days ="";
            if (productVos.size() > 0) {
                days = productVos.get(0).getExpirationdays();
            }
           // //System.err.println("exp--- "+days);

            if (StringUtils.isNotBlank(days)) {

            	Calendar c = Calendar.getInstance();
                c.setTime(dateFormat.parse(allRequestParams.get("expirationDays")));
                c.add(Calendar.DATE, Integer.parseInt(days));

                if (allRequestParams.get("customDate").equals("date")) {
                    jasperParameter.put("exp", dateFormat.format(c.getTime()));
                } else if (allRequestParams.get("customDate").equals("month")) {
                    jasperParameter.put("mfg", dateFormatmonth.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
                    jasperParameter.put("exp", dateFormatmonth.format(c.getTime()));
                }

                //System.err.println("exp--- "+dateFormat.format(c.getTime()));
            }else {
            	 if (allRequestParams.get("customDate").equals("date")) {
                     jasperParameter.put("exp", dateFormat.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
                 } else if (allRequestParams.get("customDate").equals("month")) {
                     jasperParameter.put("mfg", dateFormatmonth.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
                     jasperParameter.put("exp",dateFormatmonth.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
                 }
            	// //System.err.println("qwq exp--- "+dateFormat.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
            }

            if (StringUtils.isNotBlank(allRequestParams.get("batchNo"))) {
                jasperParameter.put("batch_no", allRequestParams.get("batchNo"));
            } else {
                jasperParameter.put("batch_no", "");
            }

            if (session.getAttribute("branchId") != null && !session.getAttribute("branchId").toString().equals("2")) {
                jasperParameter.put("realPath", session.getAttribute("realPath").toString());
            }

            jasperParameter.put("packingDate", dateFormat.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
           // //System.err.println("ccc----"+dateFormat.format(dateFormat.parse(allRequestParams.get("expirationDays"))));
            BarcodeMasterVo barcodeMasterVo = barcodeMasterRepository.findBybarcodeSize(size);

            try {
            	 jasperExporter.jasperExporterPDF(
                         jasperParameter,JASPER_REPORT_PATH + File.separator + "/barcode/"+barcodeMasterVo.getBarcodePath(),
                         "barcode_a4_40l.pdf", response);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    @PostMapping("{id}/pending/json")
    @ResponseBody
    public List<PurchaseVo> purchasePendingListJson(@PathVariable String type, @PathVariable long id,
                                                    HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PENDING_JSON;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_PENDING_JSON;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_PENDING_JSON;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_PENDING_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PENDING_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<PurchaseVo> purchasevos = purchaseService
                .findByTypeAndBranchIdAndIsDeletedAndPurchaseDateBetweenAndContactVo(type,
                        Long.parseLong(session.getAttribute("branchId").toString()), 0,
                        dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
                        dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()), id);
        if(CollectionUtils.isNotEmpty(purchasevos)) {
        	purchasevos.forEach(pr -> pr.setPurchaseItemVos(null));
            purchasevos.forEach(prc -> prc.setPurchaseAdditionalChargeVos(null));
            purchasevos.forEach(p -> p.setContactVo(null));
            purchasevos.forEach(p -> p.setPurchaseVo(null));
        }


        return purchasevos;
    }
    @PostMapping("expanse/{accountCustomId}/pending/json")
    @ResponseBody
    public List<Map<String, String>> purchaseAndExpensePendingList(@PathVariable String type, @PathVariable long accountCustomId,
                                                    HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EXPENSE_PENDING_JSON;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_EXPENSE_PENDING_JSON;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_EXPENSE_PENDING_JSON;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_EXPENSE_PENDING_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EXPENSE_PENDING_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	long branchId = Long.parseLong(session.getAttribute("branchId").toString());

    	List<Long> contactIds = contactService.findContactIdByaccountCustomId(accountCustomId);

    	 List<Map<String, String>> pendingBillList = new ArrayList<Map<String,String>>();

//    	 log.info("contactIds :"+contactIds);
    	if(contactIds.size()>0) {
    		pendingBillList = purchaseService.findByTypeAndBranchIdAndIsDeletedAndPurchaseDateBetweenAndContactVogetmap(type,branchId, 0, contactIds.get(0));
    	}
    	 // log.info("pendingBillList :"+pendingBillList.size());

    	 List<Map<String, String>> pendingexpenseList = expenseService.findPendingExpenseList(branchId,accountCustomId,Constant.REJECTED);

    	 // log.info("pendingexpenseList :"+pendingexpenseList.size());
	pendingBillList.addAll(pendingexpenseList);
	 // log.info("pendingBillList :"+pendingBillList.size());

        return pendingBillList;
    }

    @PostMapping("{accountCustomId}/pendingnew/json")
    @ResponseBody
    public List<Map<String, String>> purchasePendingListJsonnew(@PathVariable String type, @PathVariable long accountCustomId,
                                                    HttpSession session) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PENDING_JSON_NEW;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_PENDING_JSON_NEW;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_PENDING_JSON_NEW;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_PENDING_JSON_NEW;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PENDING_JSON_NEW;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

    	List<Long> contactIds = contactService.findContactIdByaccountCustomId(accountCustomId);
//    	 log.info("contactIds :"+contactIds);

    	 List<Map<String, String>> purchasevos =new ArrayList<Map<String,String>>();
     	if(contactIds.size()>0) {

         purchasevos = purchaseService.findByTypeAndBranchIdAndIsDeletedAndPurchaseDateBetweenAndContactVogetmap(type,
                        Long.parseLong(session.getAttribute("branchId").toString()), 0, contactIds.get(0));

     	}

        return purchasevos;
    }

    @PostMapping("{id}/json")
    @ResponseBody
    public PurchaseVo purchaseDetailsJson(@PathVariable long id, HttpSession session)
            throws NumberFormatException, ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DETAIL_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
                Long.parseLong(session.getAttribute("branchId").toString()));
        if(purchaseVo != null) {
        	purchaseVo.getContactVo().getContactAddressVos().forEach(ad -> ad.setContact(null));
            purchaseVo.setPurchaseVo(null);
            purchaseVo.setPurchaseItemVos(null);
            purchaseVo.setPurchaseAdditionalChargeVos(null);
            purchaseVo.getContactVo().setContactProductVos(null);
        }

        return purchaseVo;
    }

//    @RequestMapping("{id}/debitnote/json")
//    @ResponseBody
//    public DebitNoteDTO purchaseDebitNoteDetailsJson(@PathVariable long id, HttpSession session)
//            throws NumberFormatException, ParseException {
//
//      DebitNoteDTO debitNoteDTO = purchaseService.findDebitNoteData(Long.parseLong(session.getAttribute("branchId").toString()),id);
//           return debitNoteDTO;
//    }

    @RequestMapping("/{contactId}/contact/datatable")
    @ResponseBody
    public JSONObject purchaseByContactDatatable(@PathVariable String type, @RequestParam Map<String, String> allRequestParams,
                                                                   @PathVariable long contactId, HttpSession session)
            throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CONTACT_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CONTACT_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CONTACT_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
        long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
        DateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        Date startDate = dateFormat.parse(session.getAttribute(Constant.FIRST_DATE_FINANCIAL_YEAR).toString());
        Date endDate = dateFormat.parse(session.getAttribute(Constant.LAST_DATE_FINANCIAL_YEAR).toString());

        int userType = (session.getAttribute(Constant.USER_TYPE).toString().equals("2") ||
                (session.getAttribute("parentUserType").toString().equals("2") &&
                        Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString()) > 4)
                ? 1
                : 0);

        int totalRecords = purchaseService.findTotalPurchaseDataForContactDebitNoteAndBill(contactId, type, userType,companyId, branchId, startDate, endDate);

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

        List<Map<String, Object>> purchaseData = (totalRecords > 0)
                ? purchaseService.findPurchaseDataForContactDebitNoteAndBill(contactId, type, userType, companyId, branchId, startDate, endDate, length, offset)
                : Collections.emptyList();

        JSONObject jsonMainObject = new JSONObject();
        JSONObject jsonMetaObject = new JSONObject();
        jsonMainObject.put(Constant.DRAW, Integer.parseInt(allRequestParams.get(Constant.DRAW)));
        jsonMainObject.put(Constant.RECORDS_FILTERED, totalRecords);
        jsonMainObject.put(Constant.RECORDS_TOTAL, totalRecords);
        jsonMainObject.put(Constant.DATA, purchaseData);

        jsonMetaObject.put(Constant.PAGE, page);
        jsonMetaObject.put(Constant.PAGES, (int) Math.ceil((double) (totalRecords) / length));
        jsonMetaObject.put(Constant.PERPAGE, length);
        jsonMetaObject.put(Constant.TOTAL, totalRecords);

        jsonMainObject.put(Constant.META, jsonMetaObject);

        return jsonMainObject;
    }

    @RequestMapping("/report")
    public ModelAndView invoiceReport(@PathVariable String type,HttpSession session,HttpServletRequest request) throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REPORT;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_REPORT;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_REPORT;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_REPORT;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REPORT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        ModelAndView view = new ModelAndView();
        if (MenuPermission.haveReportPermission(session, request.getRequestURI()) == 1) {
        List<String> purchaseTypes = new ArrayList<String>();
        if (type.equals(Constant.PURCHASE_BILL)) {
        	view = new ModelAndView("report/purchase/purchase");
        	purchaseTypes.add(Constant.PURCHASE_BILL);
        } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
        	view = new ModelAndView("report/purchase/debitnote");
        	purchaseTypes.add(Constant.PURCHASE_DEBIT_NOTE);
        }
        //salesTypes.add(Constant.PURCHASE_BILL);

        view.addObject("displayType", "Supplier Bill");
        view.addObject("type", type);
        view.addObject("ContactList", contactService
                .contactList(Long.parseLong(session.getAttribute("branchId").toString()), Constant.CONTACT_SUPPLIER));
        view.addObject("PaidAndTotalAmount",
                purchaseService.getPaidAndTotalAmountAll(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()),
                        dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
                        dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString())));

        if (Long.parseLong(session.getAttribute("companyId").toString())==Long.parseLong(session.getAttribute("branchId").toString())) {
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
			view= new ModelAndView();
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
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PAID_AND_TOTAL_AMOUNT_JSON;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_PAID_AND_TOTAL_AMOUNT_JSON;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_PAID_AND_TOTAL_AMOUNT_JSON;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_PAID_AND_TOTAL_AMOUNT_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PAID_AND_TOTAL_AMOUNT_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<String> purchaseTypes = new ArrayList<String>();
        if (type.equals(Constant.PURCHASE_BILL)) {
        	purchaseTypes.add(Constant.PURCHASE_BILL);
        } else if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
        	purchaseTypes.add(Constant.PURCHASE_DEBIT_NOTE);
        }

        String[] Daterange = allRequestParams.get("dateRange").split("-");
        String[] DueDaterange = allRequestParams.get("dueDaterange").split("-");
        long customerId = 0;
        String countriesCode = "0", stateCode = "0", cityCode = "0";
        if(StringUtils.isNotBlank(allRequestParams.get("customerId"))) {
        	customerId = Long.parseLong(allRequestParams.get("customerId"));
        }


//        if (allRequestParams.get("countriesCode") != null && !allRequestParams.get("countriesCode").equals("")) {
//            countriesCode = allRequestParams.get("countriesCode");
//        }
//
//        if (allRequestParams.get("stateCode") != null && !allRequestParams.get("stateCode").equals("")) {
//            stateCode = allRequestParams.get("stateCode");
//        }
//
//        if (allRequestParams.get("cityCode") != null && !allRequestParams.get("cityCode").equals("")) {
//            cityCode = allRequestParams.get("cityCode");
//        }

//        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
//        if(StringUtils.isNotBlank(allRequestParams.get("branchId"))) {
//        	branchId = Long.parseLong(allRequestParams.get("branchId"));
//        }

        List<Long> branchList =new ArrayList<Long>();
    	if(StringUtils.isNotBlank(allRequestParams.get("branchId"))) {
    		branchList = Arrays.asList(allRequestParams.get("branchId").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
        }else {
        	branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
        }
//        purchaseService.getPaidAndTotalAmountAll(salesTypes,
//                Long.parseLong(session.getAttribute("branchId").toString()),
//                dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
//                dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
        return purchaseService.getPaidAndTotalAmountJson(purchaseTypes,
        		branchList, dateFormat.parse(Daterange[0]),
                dateFormat.parse(Daterange[1]), dateFormat.parse(DueDaterange[0]), dateFormat.parse(DueDaterange[1]),
                customerId);

    }

    @RequestMapping("/report/datatable")
    @ResponseBody
    @Transactional(readOnly = true)
    public DataTablesOutput<PurchaseVo> reportSalesdatatable(@PathVariable String type,
                                                             @RequestParam Map<String, String> allRequestParams, @Valid DataTablesInput input, HttpSession session)
            throws NumberFormatException, ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REPORT_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_REPORT_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_REPORT_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_REPORT_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_REPORT_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String[] Daterange = allRequestParams.get("dateRange").split("-");
        String[] DueDaterange = allRequestParams.get("dueDaterange").split("-");


        Specification<PurchaseVo> specification = new Specification<PurchaseVo>() {

            @Override
            public Predicate toPredicate(Root<PurchaseVo> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates.add(criteriaBuilder.equal(root.get("salesVo"), salesVo));

               // predicates.add(criteriaBuilder.equal(root.get("branchId"), branchId));
                List<Long> branchList =new ArrayList<Long>();
            	if(StringUtils.isNotBlank(allRequestParams.get("branchId"))) {
            		branchList = Arrays.asList(allRequestParams.get("branchId").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
                }else {
                	branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
                }
            	predicates.add(root.get("branchId").in(branchList));
                predicates.add(criteriaBuilder.equal(root.get("type"), Constant.PURCHASE_BILL));
                query.orderBy(criteriaBuilder.desc(root.get("purchaseDate")));
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
                query.orderBy(criteriaBuilder.desc(root.get("purchaseId")));

                try {
                    predicates.add(criteriaBuilder.between(root.get("purchaseDate"), dateFormat.parse(Daterange[0]),
                            dateFormat.parse(Daterange[1])));
                } catch (ParseException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                try {
                    if (dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString())
                            .compareTo(dateFormat.parse(DueDaterange[1])) != 0) {
                        //System.err.println("DueDaterange::" + DueDaterange[0] + "::" + DueDaterange[1]);
                        try {
                            predicates.add(criteriaBuilder.between(root.get("dueDate"),
                                    dateFormat.parse(DueDaterange[0]), dateFormat.parse(DueDaterange[1])));
                        } catch (ParseException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!allRequestParams.get("customerId").equals("")) {
                    //System.err.println("customerId::" + allRequestParams.get("customerId"));
                    ContactVo contactVo = new ContactVo();
                    contactVo.setContactId(Long.parseLong(allRequestParams.get("customerId")));
                    predicates.add(criteriaBuilder.equal(root.get("contactVo"), contactVo));
                }

                if (!allRequestParams.get("gst").equals("2")) {
                    predicates.add(criteriaBuilder.equal(root.get("taxType"), allRequestParams.get("gst")));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };

        DataTablesOutput<PurchaseVo> a = purchaseService.findAll(input, null, specification);

        // a.getData().forEach(v->{v.getContactVo().setContactAddressVos(null);v.setSalesItemVos(null);v});
        a.getData().forEach(x -> {
            if (x.getContactVo() != null) {
                x.getContactVo().setContactAddressVos(null);

            }
            x.setPurchaseVo(null);
            x.setPurchaseItemVos(null);
        });

        a.getData().forEach(x -> {
            x.setPurchaseAdditionalChargeVos(null);
        });

        a.getData().forEach(y -> {
        	try {
        		y.setBranchName(profileService.getName(y.getBranchId()));
			} catch (Exception e) {
				e.printStackTrace();
			}

        });

        return a;
    }

    @GetMapping("{id}/pdf") // Purchase PDF
    public void salesPDF(@PathVariable(value = "type") String type, @PathVariable long id, HttpSession session,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PDF;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_PDF;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_PDF;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_PDF;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_PDF;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }else {
            if (MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT) == 1) {
                PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndCompanyId(id,
                        Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                if (purchaseVo != null) {
                    ReportSettingVo setting = reportService.findByTypeAndBranchId(type,
                            Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                    String currencyName = Constant.RUPEES;
                    if (session.getAttribute("currencyName") != null) {
                        currencyName = session.getAttribute("currencyName").toString();
                    }
                    HashMap jasperParameter = new HashMap();
                    jasperParameter.put(Constant.LOGO_SERVER, FILE_UPLOAD_SERVER);
                    jasperParameter.put("purchase_id", id);
                    jasperParameter.put(Constant.REAL_PATH, session.getAttribute("realPath").toString());
                jasperParameter.put("printDateFormat", dateFormatMasterService.getDateFormatMasterByBranchId(purchaseVo.getBranchId()).getJavaPattern());
	            jasperParameter.put("user_front_id", purchaseVo.getBranchId());
	            jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(purchaseVo.getTotal(),currencyName));
	            jasperParameter.put("contact_id", purchaseVo.getContactVo().getContactId());
	            jasperParameter.put("contact_type", purchaseVo.getContactVo().getType());
	            jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
                jasperParameter.put(Constant.PRINT_DATE_FORMAT, dateFormatMasterService.getDateFormatMasterByBranchId(purchaseVo.getBranchId()).getJavaPattern());
	            if (StringUtils.equals(type,Constant.PURCHASE_BILL)) {
	                jasperParameter.put("display_title", "Purchase Bill");
	            } else if (StringUtils.equals(type,Constant.PURCHASE_DEBIT_NOTE)) {
	                jasperParameter.put("display_title", "Debitnote");
	            } else if (StringUtils.equals(type, Constant.PURCHASE_ORDER)) {
	                jasperParameter.put("display_title", "Purchase Order");
	            } else if (StringUtils.equals(type,Constant.PURCHASE_MATERIALINWARD)) {
	                jasperParameter.put("display_title", "Material Inward");
	            }
	            int decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());


	            jasperParameter.put("decimalFormate", numberUtil.getFormateOnDecimal(decimalNumber));

	            jasperParameter.put("path",JASPER_REPORT_PATH + File.separator);
	            try {
	                if (StringUtils.equals(type,Constant.PURCHASE_BILL)) {
	                    if (setting != null) {
	                        jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/"
	                                        + setting.getReportVo().getReport() + ".jrxml",
	                                purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                    } else {
	                        jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/Purchase-1.jrxml",
	                                purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                    }
	                } else if (StringUtils.equals(type,Constant.PURCHASE_ORDER)) {
	                	if (setting != null) {
	                		if(StringUtils.equals(Constant.HTML, setting.getReportVo().getReportFormateType())){
	                			response.sendRedirect("/purchase/"+Constant.PURCHASE_ORDER+"/htmlpdf/"+id);
	                		}else {
	                			jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/"
	                                            + setting.getReportVo().getReport() + ".jrxml",
	                                    purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                		}

	                    } else {
	                    	jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + File.separator + "/purchase/purchase-order-1.jrxml",
	                                purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                    }


	                } else if (StringUtils.equals(type, Constant.PURCHASE_MATERIALINWARD)) {
	                	if (setting != null) {
	                        jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + FileSystems.getDefault().getSeparator() + "/material-inward/"  + setting.getReportVo().getReport() + ".jrxml",
	                                        purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                    } else {
		                	jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + File.separator + "/material-inward/material-inward-1.jrxml",
		                                    purchaseVo.getPrefix()+purchaseVo.getPurchaseNo(), response);
	                    }


	                } else if (StringUtils.equals(type, Constant.PURCHASE_DEBIT_NOTE)) {
                            jasperExporter.jasperExporterPDF(jasperParameter, JASPER_REPORT_PATH + File.separator + "/debitnote/debitnote-1.jrxml",
                                    purchaseVo.getPrefix() + purchaseVo.getPurchaseNo(), response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

	            }
            }else {
            	response.sendRedirect("/404");
            }

        } else {
            response.sendRedirect("/accessdenied");}
        }


    }

    @GetMapping("{id}/excel")
    @ResponseBody
    public String downloadPurchaseOrderExcel(@PathVariable("type") String type, HttpSession session, @PathVariable("id") long purchaseId, HttpServletResponse response) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EXCEL;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_EXCEL;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_EXCEL;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_EXCEL;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_EXCEL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int count = 0;
        long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
        String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
        clusterId = clusterId.equals(RILProductConstant.LEE_COOPER) ? "LC" : clusterId.equals(RILProductConstant.JOHN_PLAYERS) ? "JP" : clusterId.equals(RILProductConstant.FASHION_WORLD) ? "FW" : "0";
        // log.warning("usertype is " + userType);
        if (Integer.parseInt(session.getAttribute(Constant.USER_TYPE).toString()) == 2) {
            count = purchaseService.countByPurchaseIdAndCompanyIdAndIsDeleted(purchaseId, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()), 0);
        } else {
            count = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(purchaseId, Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), 0);
        }
        // log.warning("count is >>>>" + count);

        if (count == 0) {
            return "redirect:/" + Constant.ERROR_PAGE_404;
        } else {
            try {
                boolean isAJioClient = (MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) || merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER);
                String[] columns = (isAJioClient) ?
                        (type.equals(Constant.PURCHASE_BILL) ?
                        new String[] {"SR. NO", "Itemcode", "Ean No", "Description", "HSN", "Qty", "UOM", "Price", "MRP", "Discount 1 Type","Discount 1 ","Discount 2 Type","Discount 2","Taxable", "Tax Rate", "Landing Cost", "Total", "Stock Transfer No"}:
                        new String[] {"SR. NO", "Itemcode", "Ean No", "Description", "HSN", "Qty", "UOM", "Price", "MRP", "Discount 1 Type","Discount 1 ","Discount 2 Type","Discount 2","Taxable","Tax Rate", "Landing Cost", "Total"}):
                        (type.equals(Constant.PURCHASE_ORDER) ?
                        new String[]{"SR. NO", "Itemcode", "Description", "HSN", "Qty", "UOM", "Price", "MRP", "Discount 1 Type","Discount 1 ","Discount 2 Type","Discount 2","Taxable", "Tax Rate", "Landing Cost", "Total"}:
                        new String[]{"SR. NO", "Itemcode", "Description", "HSN", "Qty", "UOM", "Price", "MRP", "Selling Price", "Discount 1 Type","Discount 1 ","Discount 2 Type","Discount 2","Taxable", "Tax Rate", "Landing Cost", "Total"});
                List<Object[]> details = purchaseService.getListPurchaseOrderDetails(purchaseId,merchantTypeId,clusterId);
                boolean hasVariantColumn = details.stream()
                        .anyMatch(row -> row.length > 3 && row[3] != null && !row[3].toString().trim().isEmpty());

                if (hasVariantColumn) {
                    List<String> columnsList = new ArrayList<>(Arrays.asList(columns));
                    int index = columnsList.indexOf("Description") + 1;
                    columnsList.add(index, "Variant Name");
                    columns = columnsList.toArray(new String[0]);
                }
                if (CollectionUtils.isNotEmpty(details)) {
                    String filename;
                    switch (type) {
                        case Constant.PURCHASE_BILL:
                            filename = "PURCHASE BILL(" + String.valueOf(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())) + ")";
                            break;
                        case Constant.PURCHASE_DEBIT_NOTE:
                            filename = "DEBIT NOTE(" + String.valueOf(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())) + ")";
                            break;
                        case Constant.PURCHASE_ORDER:
                            filename = "PURCHASE ORDER(" + String.valueOf(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())) + ")";
                            break;
                        case Constant.PURCHASE_MATERIALINWARD:
                            filename = "MATERIALINWARD(" + String.valueOf(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())) + ")";
                            break;
                        default:
                            filename = type + "(" + String.valueOf(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString())) + ")";
                            break;
                    }
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
                    try (OutputStream os = response.getOutputStream()) {
                        org.dhatim.fastexcel.Workbook wb = new org.dhatim.fastexcel.Workbook(os, filename, "1.0");
                        Worksheet ws = wb.newWorksheet(filename);
                        String stockTransferNo = "-";
                        if(isAJioClient && type.equals(Constant.PURCHASE_BILL)){
                            Long stockTransferId = purchaseRepository.findStockTransferIdByPurchaseId(purchaseId);
                            if(stockTransferId != null && stockTransferId != 0){
                                stockTransferNo = stockTransferRepository.getStockTransferNoByStockTransferId(stockTransferId);
                            }
                        }

                        for (int i = 0; i < columns.length; i++) {
                            ws.value(0, i, columns[i]);
                        }
                        ws.range(0, 0, 0, columns.length-1).style().bold().fontSize(12).fillColor("008000").set();
                        double totalQty = 0.0, totalAmount = 0.0;
                        int rowNum = 1;
                        double taxable;
                        double discount2 ;
                        String discountType2 ;
                        for (Object[] obj : details) {
                            double rate = Double.parseDouble(String.valueOf(obj[5]));
                            double price = Double.parseDouble(String.valueOf(obj[7]));
                            double flatDiscount = Double.parseDouble(String.valueOf(obj[14]));
                            double discount1Value = Double.parseDouble(String.valueOf(obj[11]));
                            double discount2Value = Double.parseDouble(String.valueOf(obj[13]));
                            taxable = rate * price;  // taxable = rate * price
                            if (flatDiscount > 0) {
                                taxable -= (Constant.AMOUNT.equals(String.valueOf(obj[10]))) ? discount1Value : (taxable * discount1Value) / 100;
                                if (Constant.AMOUNT.equals(String.valueOf(obj[12]))) {
                                    discount2 = discount2Value + flatDiscount;
                                } else {
                                    discount2 = (taxable * discount2Value) / 100 + flatDiscount;
                                }
                                discountType2 = "amount";
                                taxable -= discount2;
                            } else {
                                taxable -= (Constant.AMOUNT.equals(String.valueOf(obj[10]))) ? discount1Value : (taxable * discount1Value) / 100;
                                taxable -= (Constant.AMOUNT.equals(String.valueOf(obj[12]))) ? discount2Value : (taxable * discount2Value) / 100;
                                discountType2 = String.valueOf(obj[12]);
                                discount2 = discount2Value;
                            }

                            int colNum = 0;
                            ws.value(rowNum, colNum++, String.valueOf(rowNum));// sr no
                            ws.value(rowNum, colNum++, String.valueOf(obj[0]));// item code
                            if (isAJioClient) {
                                ws.value(rowNum, colNum++, String.valueOf(obj[1]));// ean no
                            }
                            ws.value(rowNum, colNum++, String.valueOf(obj[2]));// description
                            if (hasVariantColumn) {
                                String variantName = (obj[3] != null && !obj[3].toString().trim().isEmpty())
                                        ? obj[3].toString() : "-";
                                ws.value(rowNum, colNum++, variantName);// VM
                            }
                            ws.value(rowNum, colNum++, String.valueOf(obj[4]));// hsn
                            ws.value(rowNum, colNum++, String.valueOf(obj[5]));// qty
                            ws.value(rowNum, colNum++, String.valueOf(obj[6]));// uom
                            ws.value(rowNum, colNum++, String.valueOf(obj[7]));// price
                            ws.value(rowNum, colNum++, String.valueOf(obj[8]));// mrp
                            if (!isAJioClient && !type.equals(Constant.PURCHASE_ORDER)) {
                                ws.value(rowNum, colNum++, String.valueOf(obj[9]));//selling price
                            }
                            ws.value(rowNum, colNum++, String.valueOf(obj[10]));// discount 1 Type
                            ws.value(rowNum, colNum++, String.valueOf(obj[11]));// discount 1
                            ws.value(rowNum, colNum++, String.valueOf(discountType2));// discount 2 Type
                            ws.value(rowNum, colNum++, String.valueOf(discount2));// discount 2
                            ws.value(rowNum, colNum++, String.valueOf(taxable));// taxable
                            ws.value(rowNum, colNum++, String.valueOf(obj[15]));// tax rate
                            ws.value(rowNum, colNum++, String.valueOf(obj[16]));// landing cost
                            ws.value(rowNum, colNum++, String.valueOf(obj[17]));// total
                            if(isAJioClient && type.equals(Constant.PURCHASE_BILL)){
                                ws.value(rowNum, colNum++, stockTransferNo);// Stock Transfer No. for LC/JP client in Supplier Bill Only
                            }
                            totalQty += Double.parseDouble(String.valueOf(obj[5]));
                            totalAmount += Double.parseDouble(String.valueOf(obj[17]));
                            rowNum++;
                        }
                        if(isAJioClient){
                            int qtyColumn = hasVariantColumn ? 6 : 5;
                            int amountColumn = (hasVariantColumn ? 17 : 16);
                            ws.style(rowNum, 3).bold().fontSize(12).set();
                            ws.style(rowNum, qtyColumn).bold().fontSize(12).set();
                            ws.style(rowNum, amountColumn).bold().fontSize(12).set();
                            ws.value(rowNum, 3, "Total");// total
                            ws.value(rowNum, qtyColumn, String.valueOf(totalQty));// total qty
                            ws.value(rowNum, amountColumn, String.valueOf(totalAmount));// amount
                        }else{
                            int qtyColumn = hasVariantColumn ? 5 : 4;
                            int amountColumn = type.equals(Constant.PURCHASE_ORDER) ? (hasVariantColumn ? 16 : 15) : (hasVariantColumn ? 17 : 16);
                            ws.style(rowNum, 2).bold().fontSize(12).set();
                            ws.style(rowNum, qtyColumn).bold().fontSize(12).set();
                            ws.style(rowNum, amountColumn).bold().fontSize(12).set();
                            ws.value(rowNum, 2, "Total");// total
                            ws.value(rowNum, qtyColumn, String.valueOf(totalQty));// total qty
                            ws.value(rowNum, amountColumn, String.valueOf(totalAmount));// amount
                        }
                        if(Constant.PURCHASE_DEBIT_NOTE.equalsIgnoreCase(type)) {
                            List<Map> purchaseAdditionalChargeMap = purchaseAdditionalChargeRepository.findPurchaseAdditionalChargesByPurchaseId(purchaseId);
                            if(!purchaseAdditionalChargeMap.isEmpty()) {
                                rowNum += 2;
                                for(Map obj : purchaseAdditionalChargeMap){
                                    ws.value(rowNum, 2, obj.get("name").toString());
                                    ws.value(rowNum, 7, obj.get("amount").toString());
                                    ws.value(rowNum, 14, obj.get("rate").toString());
                                    ws.value(rowNum, 16, obj.get("totalAmount").toString());
                                    totalAmount += Double.parseDouble(obj.get("totalAmount").toString());
                                    ws.range(rowNum, 2, rowNum, columns.length).style().bold().fontSize(12).fillColor("FFC115").set();
                                    rowNum++;
                                }
                                rowNum += 2;
                                ws.style(rowNum, 16).bold().fontSize(12).set();
                                ws.value(rowNum, 16, String.valueOf(totalAmount));
                            }
                        }
                        wb.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Record Not Found";
    }

    @PostMapping("/variantforDebitnote/select/json")
    @ResponseBody
    public String variantforDebitnoteeMultiSelectJSON(@RequestParam Map<String, String> allRequestParams,
                                                      HttpSession session) throws ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DEBITNOTE_VERIANT_MULTI_SELECT_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        List<PurchaseItemVo> PurchaseItemVo = purchaseService.findProductVariantsWithPackage(allRequestParams.get("q"),
                Long.parseLong(session.getAttribute("branchId").toString()),
                Long.parseLong(allRequestParams.get("id")));


        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Date startDate, endDate;

        startDate = dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString());
        endDate = dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString());
        PurchaseItemVo.forEach(p -> {
            JSONObject json1 = new JSONObject();
            try {
                json1.put("id", p.getProductVarientsVo().getProductVarientId() + "," + p.getPrice());
                json1.put("text", p.getProductVarientsVo().getProductVo().getName() + " "
                        + p.getProductVarientsVo().getVarientName());
                json1.put("name",
                        p.getProductVarientsVo().getProductVo().getName() + " "
                                + (p.getProductVarientsVo().getVarientName() == null ? ""
                                : p.getProductVarientsVo().getVarientName()));
                json1.put("mrp", p.getMrp());
                json1.put("price", p.getPrice());
                json1.put("qty",
                        stockTransactionService.getVariantQty(
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                p.getProductVarientsVo().getProductVarientId(), startDate, endDate,
                                session.getAttribute("financialYear").toString()));
            } catch (Exception e) {

            }
            jsonArray.add(json1);
        });
        try {
            jsonObject.put("total_count", PurchaseItemVo.size());
            jsonObject.put("incomplete_results", true);
            jsonObject.put("items", jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @PostMapping("/{id}/varientId/{varientId}/json")
    @ResponseBody
    public ProductVarientsVo viewVariantBydebitnoteJSON(@PathVariable(value = "type") String type,
                                                        @PathVariable("id") long id, @PathVariable("varientId") String varientIdAndPrice, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_VERIANT_DEBITNOTE_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        String[] array = varientIdAndPrice.split(",");
        long productVarientID = Long.parseLong(array[0]);
        Double purchasePrice = Double.parseDouble(array[1]);

        PurchaseItemVo purchaseItemVo = purchaseService.findByPurchaseIdAndBrancIdAndProductVArientIdAndPurchasePrice(
                id, Long.parseLong(session.getAttribute("branchId").toString()), productVarientID, purchasePrice);


        if (purchaseItemVo == null) {
            return null;
        } else {
            purchaseItemVo.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
            purchaseItemVo.getProductVarientsVo().setRetailerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setWholesalerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setMrp(purchaseItemVo.getMrp());
            purchaseItemVo.getProductVarientsVo().setQty(purchaseItemVo.getQty());
            if (purchaseItemVo.getProductVarientsVo().getProductVo().getPurchaseTaxIncluded() == 1) {
                purchaseItemVo.getProductVarientsVo()
                        .setPurchasePrice(purchaseItemVo.getPrice() + purchaseItemVo.getTaxAmount());
            } else {
                purchaseItemVo.getProductVarientsVo().setPurchasePrice(purchaseItemVo.getPrice());
            }

            // purchaseItemVo.getProductVarientsVo().get
            return purchaseItemVo.getProductVarientsVo();
        }

    }

    @PostMapping("/{id}/barcode/{barcode}/json")
    @ResponseBody
    public ProductVarientsVo viewVariantByBarcodeForDebitnoteJSON(@PathVariable("id") long id,
                                                                  @PathVariable("barcode") String barcode, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_VERIANT_BARCODE_DEBITNOTE_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        PurchaseItemVo purchaseItemVo = purchaseService.findByPurchaseIdAndBrancIdAndItemcode(id,
                Long.parseLong(session.getAttribute("branchId").toString()), barcode);

        if (purchaseItemVo == null) {
            return null;
        } else {
            purchaseItemVo.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
            purchaseItemVo.getProductVarientsVo().setRetailerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setWholesalerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setPurchasePrice(purchaseItemVo.getPrice());
            // purchaseItemVo.getProductVarientsVo().get
            return purchaseItemVo.getProductVarientsVo();
        }

    }

    @PostMapping("/contact/{id}/json")
    @ResponseBody
    public List<PurchaseVo> PurchaseListJSON(HttpSession session, @PathVariable(value = "type") String type,
                                             @PathVariable(value = "id") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_LIST_JSON;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_LIST_JSON;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_LIST_JSON;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_LIST_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_LIST_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        List<PurchaseVo> purchaseVos = new ArrayList<PurchaseVo>();

        try {

            List<String> purchaseTypes = new ArrayList<String>();
            purchaseTypes.add(type);
            List<String> status = new ArrayList<>();
            if(type.equals(Constant.PURCHASE_ORDER)) {

                status.add(Constant.DRAFT);
                status.add("partiallydelivered");
            	purchaseVos = purchaseService.findByTypesAndContactAndBranchIdAndSalesDateBetweenAndStatus(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId,status);

            }else if(type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                status.add("open");
                status.add("inprogress");
            	purchaseVos = purchaseService.findByTypesAndContactAndBranchIdAndSalesDateBetweenAndStatus(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId,status);

            }
            else {
            purchaseVos = purchaseService.findByTypesAndContactAndBranchIdAndSalesDateBetween(purchaseTypes,
                    Long.parseLong(session.getAttribute("branchId").toString()), contactId);
            }
            Collections.reverse(purchaseVos);

            purchaseVos.forEach(purchaseItem -> {
                if (type.equals(Constant.SALES_CREDIT_NOTE)) {
                    purchaseItem.getPurchaseItemVos().forEach(q -> {
                        q.getProductVarientsVo().setProductVo(null);
                        q.setPurchaseVo(null);
                        q.setTaxVo(null);
                    });
                } else {
                    purchaseItem.setPurchaseItemVos(null);
                }
                purchaseItem.setPurchaseVo(null);
                purchaseItem.setPurchaseAdditionalChargeVos(null);
                purchaseItem.setContactVo(null);
                purchaseItem.setEmployeeVo(null);
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return purchaseVos;
    }
    @PostMapping("/contact/{id}/list")
    @ResponseBody
    public List<Map<String, String>> purchaseNoAndDateJson(HttpSession session, @PathVariable(value = "type") String type,
                                             @PathVariable(value = "id") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_LIST_JSON;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CONTACT_LIST_JSON;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CONTACT_LIST_JSON;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CONTACT_LIST_JSON;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_LIST_JSON;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        List<Map<String,String>> purchaseVos = new ArrayList<>();

        try {
            List<String> purchaseTypes = new ArrayList<String>();
            purchaseTypes.add(type);
            List<String> status = new ArrayList<>();
            if(type.equals(Constant.PURCHASE_ORDER)) {
                status.add(Constant.DRAFT);
                status.add("partiallydelivered");
            	purchaseVos = purchaseService.getPurchaseNoAndDateAndDebotnoteAmountByTypesAndContactAndBranchIdAndStatus(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId,status);

            }else if(type.equals(Constant.PURCHASE_MATERIALINWARD)) {
                status.add("open");
                status.add("inprogress");
            	purchaseVos = purchaseService.getPurchaseNoAndDateAndDebotnoteAmountByTypesAndContactAndBranchIdAndStatus(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId,status);

            }
            else {
            	purchaseVos = purchaseService.getPurchaseNoAndDateAndDebotnoteAmountByTypesAndContactAndBranchId(purchaseTypes,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return purchaseVos;
    }
    @PostMapping("/lastpurchase/{id}/json")
    @ResponseBody
    public List<Map<String, String>> lastpurchaseJSON(@RequestParam Map<String, String> allRequestParams,
                                                      @PathVariable long id, @RequestParam(value = "contactId", defaultValue = "0") long contactId,
                                                      HttpSession session) throws ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.LAST_PURCHASE_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }


        List<Map<String, String>> itemVos = null;
        if (contactId == 0) {
            itemVos = purchaseService
                    .findProductVarientsIdForLastFivew(Long.parseLong(session.getAttribute("branchId").toString()), id);
        } else {
            itemVos = purchaseService.findProductVarientsIdForLastFivewithcontactid(
                    Long.parseLong(session.getAttribute("branchId").toString()), id, contactId);
        }

        return itemVos;
    }

    @PostMapping(value = "/check/billno")
    @ResponseBody
    public Map<String, String> checkSupplierBillNo(@RequestParam("contactId") long contactId,@RequestParam(name =  "purchaseId", defaultValue = "0") long purchaseId,
    		@RequestParam(name="billNo", defaultValue="", required=false) String billNo, HttpSession session,@PathVariable("type") String type) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CHECK_SUPPLIER_BILL_NO;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CHECK_SUPPLIER_BILL_NO;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CHECK_SUPPLIER_BILL_NO;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CHECK_SUPPLIER_BILL_NO;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CHECK_SUPPLIER_BILL_NO;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        Map<String, String> map = new HashMap<String, String>();

        if(StringUtils.isNotBlank(billNo)) {
        	List<String> list = new ArrayList<String>();
        	if(purchaseId==0) {
        		list = purchaseRepository.checkBillNo(Long.parseLong(session.getAttribute("branchId").toString()), type, contactId, billNo);
        	}else {
        		list = purchaseRepository.checkBillNoAndPurchaseId(Long.parseLong(session.getAttribute("branchId").toString()), type, contactId, billNo,purchaseId);
        	}

	        if(list.size() > 0) {
	        	map.put("msg", "false");
	        }else {
	        	map.put("msg", "true");
	        }
        }else {
        	map.put("msg", "false");
        }

        return map;
    }

    private boolean cellExistFastExcel(org.dhatim.fastexcel.reader.Row row, int cellIndex) {
        return StringUtils.isNotBlank(row.getCellText(cellIndex));
    }

    @PostMapping(value = "/check/excel")
    @ResponseBody
    public Map<String, Object> ImportCustomer(@RequestParam("excelFile") MultipartFile file, @RequestParam("contactId") long contactId,
                                              @RequestParam("allowProduct") String allowedProduct,
                                              @PathVariable(value = "type") String type, HttpSession session) throws IOException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String rateLimitType = switch (type) {
            case Constant.PURCHASE_ORDER -> RateLimitConstant.PURCHASE_ORDER_CHECKEXCEL;
            case Constant.PURCHASE_MATERIALINWARD -> RateLimitConstant.PURCHASE_MATERIALINWARD_CHECKEXCEL;
            case Constant.PURCHASE_BILL -> RateLimitConstant.PURCHASE_BILL_CHECKEXCEL;
            case Constant.PURCHASE_DEBIT_NOTE -> RateLimitConstant.PURCHASE_DEBITNOTE_CHECKEXCEL;
            default -> RateLimitConstant.PURCHASE_ORDER_CHECKEXCEL;
        };
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        FileValidationResponse fileValidationResponse = securityValidation.validateFile(file, Constant.FILE_EXCEL);
        if(!fileValidationResponse.isValid()) {
            map.put("msg", fileValidationResponse.getMessage());
            return map;
        } else {
            File fb = ImageResize.convert(file);
            session.setAttribute(Constant.FILE_PATH, fb.getAbsolutePath());
            boolean allowProduct = StringUtils.isNotBlank(allowedProduct) ? Boolean.parseBoolean(allowedProduct) : false;
            Map<String, Object> sheetResult = checkSheet(session, type, contactId, allowProduct);
            if ((Boolean) sheetResult.get("result")) {
                map.put("msg", "success");
            } else {
                map.put("msg", "There are some errors in the following cell numbers: " + sheetResult.get("reason").toString());
            }
        }
        return map;
    }

    public Map<String, Object> checkSheet( HttpSession session, String type, long contactId, boolean allowProduct) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        try (InputStream is = Files.newInputStream(Paths.get((String) session.getAttribute(Constant.FILE_PATH)));
             ReadableWorkbook wb = new ReadableWorkbook(is)) {
            org.dhatim.fastexcel.reader.Sheet sheet = wb.getFirstSheet();
            List<org.dhatim.fastexcel.reader.Row> rows = sheet.read();
            if (rows.size() <= 1) {
                map.put("result", false);
                map.put("reason", "Sheet is empty. Please enter some data");
                return map;
            }
            int limit = companySettingService.getvalueByCompanyIdAndType(0, Constant.SHEETLIMIT);
            if ((rows.size() - 1) > limit) {
                map.put("result", false);
                map.put("reason", "Only " + limit + " product can be upload at a time");
                return map;
            }
            org.dhatim.fastexcel.reader.Row headerRow = rows.get(0);
            if (cellExistFastExcel(headerRow, 0)) {
                if (!headerRow.getCellText(0).trim().equalsIgnoreCase("Item Code *")
                        && !headerRow.getCellText(0).trim().equalsIgnoreCase("Item Code")) {
                    map.put("result", false);
                    map.put("reason", "Column Not Find Wrong Sheet");
                    return map;
                }
            } else {
                map.put("result", false);
                map.put("reason", "Column Not Find Wrong Sheet");
                return map;
            }
            boolean result = true;
            Multimap<String, String> priceAndItemCodeList = ArrayListMultimap.create();
            Map<String, Boolean> sheetItemCodeMap = new HashMap<>();
            long branchId = Long.parseLong(session.getAttribute(Constant.BRANCHID).toString());
            long companyId = Long.parseLong(session.getAttribute(Constant.COMPANYID).toString());
            long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
            boolean supplierWiseMapping = companySettingService.findValueByBranchIdAndType(branchId,Constant.ALLOWSUPPLIERWISEPRODUCTMAPPING) == 1;
            String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
            boolean hasPermissionForUcMrpSp = Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString()) <= 4 || MenuPermission.havePermission(session, type, Constant.UNITCOST_MRP_SP_MARGIN_TAXTYPE) != 0;
            boolean hasPermissionForDiscount1 = Long.parseLong(session.getAttribute(Constant.USER_TYPE).toString()) <= 4 || MenuPermission.havePermission(session, type, Constant.DISCOUNT_1) != 0;
            int isUmoWiseDecimalRestrictionStopped = companySettingService.getvalueByCompanyIdAndType(companyId, Constant.STOPUMOWISEDECIMAL);
            String yearInterval = session.getAttribute(Constant.FINANCIAL_YEAR).toString();
            Map<String, List<Map<String, Object>>> itemCodeDetailsMap = new HashMap<>();
            for (Map<String, Object> results : productService.getItemCodeProductVarientIdAndExpiryManageByCompanyIdMerchantTypeId(companyId, merchantTypeId, clusterId)) {
                itemCodeDetailsMap.computeIfAbsent(String.valueOf(results.get("itemCode")), k -> new ArrayList<>()).add(results);
            }
            int expType = 4;
            int expDate = 5;
            StringBuilder reasonBuilder = new StringBuilder();
            int decimalNumber = 2;
            try {
                decimalNumber = Integer.parseInt(session.getAttribute(Constant.DECIMAL_POINT).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int count = 1; count < rows.size(); count++) {
                org.dhatim.fastexcel.reader.Row row = rows.get(count);
                boolean existItemCode = false;
                // -----------------------------cell 0------------------Item Code----------------------------------
                long productVarientId = 0;
                int isExpiry = 0;
                try {
                    if (cellExistFastExcel(row, 0)) {
                        if (itemCodeDetailsMap.containsKey(row.getCellText(0).trim())) {
                            productVarientId = Long.parseLong(itemCodeDetailsMap.get(row.getCellText(0).trim()).get(0).get("productVarientId").toString());
                            isExpiry = Integer.parseInt((itemCodeDetailsMap.get(row.getCellText(0).trim()).get(0).get("isExpiryManage").toString()));
                        } else {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",A)-Item Code Not Found");
                        }
                        if(supplierWiseMapping && allowProduct){
                            ContactProductVo contactProductVo = contactService.findByProductVarientIdAndContactId(productVarientId,contactId);
                            if(contactProductVo == null){
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",A)-Product is not Mapped against the party");
                            }
                        }
                        existItemCode = sheetItemCodeMap.containsKey(row.getCellText(0).trim());
                        sheetItemCodeMap.put(row.getCellText(0).trim(), true);
                    } else {
                        result = false;
                        reasonBuilder.append("(").append(row.getRowNum()).append(",A)-Item Code Is Required");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // -----------------------------cell 1------------------Qty----------------------------------
                double sheetQty = 0;
                try {
                    if (cellExistFastExcel(row, 1)) {
                        if (RegexTest.validateDouble(row.getCellText(1).trim())) {
                            boolean isValidDecimal = productService.isValidDecimalQty
                                    (productVarientId, row.getCellText(1).trim(),
                                            isUmoWiseDecimalRestrictionStopped, 0);
                            sheetQty = Double.parseDouble(row.getCellText(1).trim());
                            if (!isValidDecimal) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",B)- The number of decimal places in qty has been reached ");
                            }
                        } else {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",B)- Qty Is Only In Double");
                        }

                    } else {
                        result = false;
                        reasonBuilder.append("(").append(row.getRowNum()).append(",B)- Qty Is Required");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // -----------------------------cell 3------------------Mrp----------------------------------
                try {
                    if (cellExistFastExcel(row, 3)) {
                        if (!RegexTest.validateDouble(row.getCellText(3).trim(), decimalNumber)) {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",D)- Mrp Is InValid ");
                        }
                    } else {
                        result = false;
                        reasonBuilder.append("(").append(row.getRowNum()).append(",D)- Mrp Is Required ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // -----------------------------cell 2------------------Price----------------------------------
                try {
                    if (cellExistFastExcel(row, 2)) {
                        if (RegexTest.validateDouble(row.getCellText(2).trim(), decimalNumber)) {
                            double sheetPrice = Double.parseDouble(row.getCellText(2).trim());
                            if (existItemCode) {
                                if (!hasPermissionForUcMrpSp) {
                                    int batchCountWithoutExpiry = stockMasterService.getBatchPriceCountWithoutExpiryDetails(productVarientId, branchId, yearInterval, Double.parseDouble(row.getCellText(3).trim()), Double.parseDouble(row.getCellText(2).trim()), type.equals(Constant.PURCHASE_ORDER) ? 0 : 1, type.equals(Constant.PURCHASE_ORDER) ? 0 : Double.parseDouble(row.getCellText(4).trim()));
                                    if (batchCountWithoutExpiry == 0) {
                                        result = false;
                                        if (type.equals(Constant.PURCHASE_ORDER)) {
                                            reasonBuilder.append("(").append(row.getRowNum()).append(",C)- (You don't have permission to change the Price, Mrp)");
                                        } else {
                                            reasonBuilder.append("(").append(row.getRowNum()).append(",C)- (You don't have permission to change the Price, Mrp & Selling Price)");
                                        }

                                    }
                                }
                                // log.info("=============================================="+priceAndItemCodeList.size());
                                if (!priceAndItemCodeList.isEmpty()) {
                                    List<String> itemCodes = priceAndItemCodeList.entries().stream().filter(e -> e.getKey().equalsIgnoreCase(row.getCellText(0).trim())).map(Map.Entry::getValue).collect(Collectors.toList());
                                    if (itemCodes.contains(row.getCellText(2).trim())) {
                                        result = false;
                                        reasonBuilder.append("(").append(row.getRowNum()).append(",C)- prices can not be duplicate.Please merge qunatity.");
                                    }
                                }
                            }
                            priceAndItemCodeList.put(row.getCellText(0).trim(), row.getCellText(2).trim());
                            List<Map<String, Object>> data = productService.getProductTaxAndIncudingExcluding(row.getCellText(0).trim(), companyId, merchantTypeId, clusterId);
                            try {
                                if (!data.isEmpty()) {
                                    int taxIncluded = (int) data.get(0).get("purchaseTaxIncluded");
                                    double taxRate = (double) data.get(0).get("taxRate");
                                    double landingCost = 0;
                                    double taxablePrice = 0;
                                    String discountType = "amount";
                                    double discount = 0;
                                    String discountType2 = "amount";
                                    double discount2 = 0;

                                    if (!type.equals(Constant.PURCHASE_ORDER)) {
                                        if (cellExistFastExcel(row, 8) && RegexTest.validateDouble(row.getCellText(8).trim(), decimalNumber)) {
                                            if (row.getCellText(7).trim().equalsIgnoreCase("Percentage")) {
                                                discountType2 = "percentage";
                                            }
                                            discount2 = Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(8).trim()));
                                        }
                                        if (cellExistFastExcel(row, 6) && RegexTest.validateDouble(row.getCellText(6).trim(), decimalNumber)) {
                                            if (row.getCellText(5).trim().equalsIgnoreCase("Percentage")) {
                                                discountType = "percentage";
                                            }
                                            discount = Double.parseDouble(securityValidation.checkAndReplaceCsvInjectionCharacters(row.getCellText(6).trim()));
                                        }

                                    }
                                    // Here we are calculating the landingCost based on the value of tax included, discount1 and discount2
                                    landingCost = sheetPrice * sheetQty;

                                    double taxCalculation = taxIncluded == 1 ? (landingCost * taxRate / 100) : 0;
                                    landingCost -= taxCalculation;

                                    double discount1Calculation = discountType.equalsIgnoreCase("percentage") ? (landingCost * discount / 100) : discount;
                                    landingCost -= discount1Calculation;

                                    double discount2Calculation = discountType2.equalsIgnoreCase("percentage") ? (landingCost * discount2 / 100) : discount2;
                                    landingCost -= discount2Calculation;

                                    landingCost = landingCost + ((landingCost * taxRate) / 100);
                                    landingCost = round(landingCost / sheetQty, decimalNumber);
                                    try {
                                        if (cellExistFastExcel(row, 3) && RegexTest.validateDouble(row.getCellText(3).trim(), decimalNumber)) {
                                            double parsedMrp = Double.parseDouble(row.getCellText(3).trim());
                                            if (taxablePrice > parsedMrp || parsedMrp < landingCost) {
                                                result = false;
                                                reasonBuilder.append("(").append(row.getRowNum()).append(")");
                                                if (taxablePrice > parsedMrp) {
                                                    reasonBuilder.append(",C)- MRP is Less than Price");
                                                } else {
                                                    reasonBuilder.append(",D)- MRP is smaller than landingcost ").append(landingCost).append(" ");
                                                }
                                            }
                                            if (cellExistFastExcel(row, 4) && RegexTest.validateDouble(row.getCellText(4).trim(), decimalNumber)) {
                                                double parsedSellingPrice = Double.parseDouble(row.getCellText(4).trim());
                                                if (parsedSellingPrice > parsedMrp || parsedSellingPrice < landingCost) {
                                                    result = false;
                                                    reasonBuilder.append("(").append(row.getRowNum()).append(",E)");
                                                    if (parsedSellingPrice > parsedMrp) {
                                                        reasonBuilder.append("- Selling Price Should not greater than MRP ");
                                                    } else {
                                                        reasonBuilder.append("- Selling Price Should not less than Landing Cost ").append(landingCost).append(" ");
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",C)- Price Is InValid ");
                        }
                    } else {
                        result = false;
                        reasonBuilder.append("(").append(row.getRowNum()).append(",C)- Price Is Required ");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!type.equals(Constant.PURCHASE_ORDER)) {
                    try {
                        if (cellExistFastExcel(row, 4)) {
                            if (!RegexTest.validateDouble(row.getCellText(4).trim(), decimalNumber)) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",E)- Selling Price Is InValid ");
                            }
                        } else {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",E)- Selling Price Is Required ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (cellExistFastExcel(row, 5)) {
                            if (!row.getCellText(5).trim().equalsIgnoreCase("Amount")
                                    && !row.getCellText(5).trim().equalsIgnoreCase("Percentage")) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",F)- Discount Type Only Amount Or Percentage");
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (cellExistFastExcel(row, 6) && !row.getCellText(6).trim().equals("0")) {
                            if (RegexTest.validateDouble(String.valueOf(Double.valueOf(row.getCellText(6).trim())), decimalNumber)) {
                                if (row.getCellText(5).trim().equalsIgnoreCase("Percentage") || row.getCellText(5).trim().equalsIgnoreCase("Amount")) {
                                    if (!hasPermissionForDiscount1 && !row.getCellText(6).trim().equals("0")) {
                                        result = false;
                                        reasonBuilder.append("(").append(row.getRowNum()).append(",G)- (You don't have permission to change the Discount 1)");
                                    }
                                    if (row.getCellText(5).trim().equalsIgnoreCase("Percentage") && Double.parseDouble(row.getCellText(6).trim()) > Double.parseDouble("100")) {
                                        result = false;
                                        reasonBuilder.append("(").append(row.getRowNum()).append(",G)- Discount cannot be greater than 100%. ");
                                    }
                                }
                            } else {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",G)- Discount Is InValid ");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (cellExistFastExcel(row, 7) && !row.getCellText(7).trim().equalsIgnoreCase("Amount")
                                && !row.getCellText(7).trim().equalsIgnoreCase("Percentage")) {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",H)- Discount Type Only Amount Or Percentage");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // ---------------------------------------------------------------------------------

                    try {
                        if (cellExistFastExcel(row, 8)) {
                            if (RegexTest.validateDouble(String.valueOf(Double.valueOf(row.getCellText(8).trim())), decimalNumber)) {
                                if (row.getCellText(7).trim().equalsIgnoreCase("Percentage")
                                        && Double.parseDouble(row.getCellText(8).trim()) > Double.parseDouble("100")) {
                                    result = false;
                                    reasonBuilder.append("(").append(row.getRowNum()).append(",I)- Discount cannot be greater than 100%. ");
                                }
                            } else {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",I)- Discount Is InValid ");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    expType = 9;
                    expDate = 10;

                }
                    //------------------------------Checking if discount is less than selling or not -----------------
                    try {
                        double sellingPrice = Double.parseDouble(row.getCellText(4).trim());
                        if (StringUtils.isNotBlank(row.getCellText(6)) && Double.parseDouble(row.getCellText(6))>sellingPrice){
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum() + ",I)- Discount cannot be greater than selling price ");
                        }else if(StringUtils.isNotBlank(row.getCellText(8)) && Double.parseDouble(row.getCellText(8))>sellingPrice) {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum() + ",j)- Discount cannot be greater than selling price ");
                        }
                        else if(StringUtils.isNotBlank(row.getCellText(6)) || StringUtils.isNotBlank(row.getCellText(8))) {
                            double discount1 = Double.parseDouble(row.getCellText(6).trim());
                            double discount2 = Double.parseDouble(row.getCellText(8).trim());
                            if((discount1+discount2 > sellingPrice)) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum() + ",I,J)- Discount cannot be greater than selling price ");
                            }
                        }
//                        else{
//                            result= true;
//                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
//                 // -----------------------------cell 4 & 5------------------Exp/Mfg Date & Type----------------------------------
                try {
                    if (isExpiry == 1) {
                        if (cellExistFastExcel(row, expType)) {
                            if (!row.getCellText(expType).trim().equalsIgnoreCase("MFG") && !row.getCellText(expType).trim().equalsIgnoreCase("EXP")) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",J)- Expiry Type Is InValid");
                            }
                            if (cellExistFastExcel(row, expDate)) {
                                try {
                                    String value = row.getCellText(expDate).trim();
                                    if (!value.matches("\\d{2}-\\d{2}-\\d{4}") || Integer.parseInt(value.substring(0, 2)) > 31 || Integer.parseInt(value.substring(3, 5)) > 12) {
                                        reasonBuilder.append("(").append(row.getRowNum()).append(", K )-(Date is not valid)");
                                        result = false;
                                    }
                                    //this code was commented becuase 2023<2024 year conditioin was not pass if exp was in future so
                                    /*09/10/2023
                                     * if(currentYear<Integer.parseInt(value.substring(6, value.length()))) {
                                     * result=true; rowNumber +="(" + i + ", K )-(Date is not valid)"; result =
                                     * false; }
                                     */
                                } catch (Exception e) {
                                    result = false;
                                    reasonBuilder.append("(").append(row.getRowNum()).append(",K)- please check date format also check date in excel write as Text");
                                    e.printStackTrace();
                                }

                            } else {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",K)- MFG/EXP Date is Required Or Invalid Please Correct it");
                            }
                        } else {
                            result = false;
                            reasonBuilder.append("(").append(row.getRowNum()).append(",J)- Expiry Type Is Required For Expiry Manage Product ");

                            if (!cellExistFastExcel(row, expDate)) {
                                result = false;
                                reasonBuilder.append("(").append(row.getRowNum()).append(",K)- Expiry Date Is Required For Expiry Manage Product ");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            map.put("result", result);
            map.put("reason", reasonBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
            map.put("result", false);
            map.put("reason", "Error: Unable to retrieve sheet information.");
        }
        return map;
    }

    @PostMapping("/bypurchaseid/{purchaseId}")
    @ResponseBody
    public Map<String,Object> getSalesItemBySaleId(HttpSession session, @PathVariable(value = "type") String type,
                                                        @PathVariable(value = "purchaseId") long purchaseId,@RequestParam(value = "batchrequired",required = false,defaultValue = "0")String batchrequired) throws CloneNotSupportedException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DETAIL;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DETAIL;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DETAIL;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(purchaseId, branchId);
        List<ProductVarientsVo> productVarientsVos = new ArrayList<ProductVarientsVo>();
        List<PurchaseItemVo> purchaseItemVos = null;
        Map<String,Object> responseMap = new HashMap<>();

        if (purchaseVo != null) {

            purchaseItemVos = purchaseVo.getPurchaseItemVos();
            if (purchaseItemVos != null) {
                final int[] count = {0};
                for (PurchaseItemVo purchaseItemVo : purchaseItemVos) {

                    ProductVarientsVo productVarientsVo = new ProductVarientsVo();
                    productVarientsVo.setSku("Count" + count[0]++);
                    productVarientsVo = (ProductVarientsVo) purchaseItemVo.getProductVarientsVo().clone();
                    productVarientsVo.getProductVo().setProductVarientsVos(null);

                    productVarientsVo.setRetailerPrice(purchaseItemVo.getPrice());
                    productVarientsVo.setWholesalerPrice(purchaseItemVo.getPrice());
                    productVarientsVo.setQty(new BigDecimal(String.valueOf(purchaseItemVo.getQty())).doubleValue());
                    productVarientsVo.setReceiveqty(new BigDecimal(String.valueOf(purchaseItemVo.getReceiveQty())).doubleValue());
                    productVarientsVo.setMrp(purchaseItemVo.getMrp());
                    productVarientsVo.setDiscount(purchaseItemVo.getDiscount());
                    productVarientsVo.setDiscountType(purchaseItemVo.getDiscountType());

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    productVarientsVo.setPurchaseDate(dateFormat.format(purchaseItemVo.getPurchaseVo().getPurchaseDate()));
                    productVarientsVo.setBatchCreationDate(purchaseItemVo.getBatchCreationDate());
                    productVarientsVo.setBatchExpiryDate((purchaseItemVo.getBatchExpiryDate()));
                        productVarientsVo.setPurchasePrice(purchaseItemVo.getPrice());
                    productVarientsVo.setExpdays(purchaseItemVo.getExpdays());

                    productVarientsVo.setExpiryManage(purchaseItemVo.getExpiryManage());
                    if(batchrequired.equals("1")) {
                    	List<StockMasterDTOForDebitNote>  masterVos= stockMasterRepository.findByProductBatch(productVarientsVo.getProductVarientId(),Long.parseLong(session.getAttribute("branchId").toString()),session.getAttribute("financialYear").toString());
                    	productVarientsVo.setStockMasterDTOForDebitNote(masterVos);
                    }

                    String itemCode = productVarientsVo.getItemCode();
                    if(StringUtils.isNotEmpty(purchaseItemVo.getItemCode())) {
                    	itemCode = purchaseItemVo.getItemCode();
                    }
                    productVarientsVo.setPurchaseItemCode(itemCode);
                    productVarientsVo.setGstTaxType(purchaseItemVo.getPurchaseVo().getGstTaxType());

                    productVarientsVo.setFlatDiscount(purchaseVo.getFlatDiscount());
                    productVarientsVo.getProductVo().setProductAttributeVos(null);
                    productVarientsVos.add(productVarientsVo);
                }
                if(!productVarientsVos.isEmpty()) {
                    responseMap.put("productVariant",productVarientsVos);
                }
                if(!purchaseVo.getPurchaseAdditionalChargeVos().isEmpty()){
                    purchaseVo.getPurchaseAdditionalChargeVos().forEach(obj->{
                        obj.setPurchaseVo(null);
                    });
                    responseMap.put("additionaCharge", purchaseVo.getPurchaseAdditionalChargeVos());
                }
                if (MapUtils.isNotEmpty(purchaseVo.getTdsJson())) {
                    responseMap.put("tdsApplicable", (purchaseVo.getTdsJson().get(Constant.TDS_APPLICABLE) != null ? Integer.parseInt(purchaseVo.getTdsJson().get(Constant.TDS_APPLICABLE).toString()) : 0));
                }
                if (MapUtils.isNotEmpty(purchaseVo.getTcsJson())) {
                    responseMap.put("tcsApplicable", (purchaseVo.getTcsJson().get(Constant.TCS_APPLICABLE) != null ? Integer.parseInt(purchaseVo.getTcsJson().get(Constant.TCS_APPLICABLE).toString()) : 0));
                }
            }
        }

        return responseMap;
    }

    @PostMapping("/attachmentUpload")
    @ResponseBody
    public String imageupload(@RequestParam("id") long
                                      id, @RequestParam("image_logo") MultipartFile file, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_ATTACHMENT_UPLOAD, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int response = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (response == 0) return "404";
        String fileName = "";
        try {


            if (!file.isEmpty()) {

                long companyId = Long.parseLong(String.valueOf(session.getAttribute("companyId").toString()));
                String fileExtension = "";
                File fb =
                        ImageResize.convert(file);
                Calendar calendar = Calendar.getInstance();

                fileExtension = GetFileExtension.get(fb);
                fileName = id + "." +
                        fileExtension;
                String filePath = PURCHASE_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + fileName;
                String uploadStatus = "500";
                if (FILE_UPLOAD_SERVER.equals(Constant.FILE_UPLOAD_SERVER_AZURE)) {
                    uploadStatus = azureBlobService.sendPurchaseAttachmentFileToAZURE(fb, filePath);
                } else {
                    uploadStatus = awsService.saveAttachmentToS3(fileExtension, fb, filePath);
                }
                if (uploadStatus == "200") {
                    purchaseService.updateAttachmentfile(id, fileName); //
                }
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
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DOWNLOAD_DOCUMENT, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id, Long.parseLong(session.getAttribute("branchId").toString()));
        long companyId = Long.parseLong(String.valueOf(session.getAttribute("companyId").toString()));
        String fileLocation = PURCHASE_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + purchaseVo.getImgLocation();

        if(FILE_UPLOAD_SERVER.equals(Constant.FILE_UPLOAD_SERVER_AZURE)) {
        	return azureBlobService.getPurchaseAttachmentFileFromAZURE(fileLocation, purchaseVo.getImgLocation());
        }else {
        	String file_path = END_POINT_URL + "/" + BUCKET + "/" + fileLocation;//PURCHASE_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + purchaseVo.getImgLocation();
//	        String file_path = END_POINT_URL + "/" + BUCKET + "/" + PURCHASE_ATTACHMENT_LOCATION + "/" + companyId + "/" + "/" + id + "/" + purchaseVo.getImgLocation();
	        String file_name = purchaseVo.getImgLocation();
	        BufferedInputStream inputStream = new BufferedInputStream(new URL(file_path).openStream());

	        Files.copy(inputStream, Paths.get(System.getProperty("java.io.tmpdir") + File.separator + file_name), StandardCopyOption.REPLACE_EXISTING);
	        Path filePath = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + file_name);
	        Resource resource = new UrlResource(filePath.toUri());

	        // Try to determine file's content type
	        String contentType = null;
	        try {
	            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	        } catch (IOException ex) {

	        }
	        if (contentType == null) {
	            contentType = "application/octet-stream";
	        }

	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType("application/octet-stream"))
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	                .body(resource);
        }

    }
    @PostMapping(value = "/items/{id}")
    @ResponseBody
    public List<Map<String, String>> itemsRetrive (@PathVariable(value = "type") String type,HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_ITEMS_DETAILS;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ITEMS_DETAILS;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView("purchase/purchase-view");
        PurchaseVo purchaseVo=purchaseService.findByPurchaseIdAndBranchId(id,Long.parseLong(session.getAttribute("branchId").toString()));
        List<Map<String, String>> data=purchaseService.findBypurchaseItems(id,Long.parseLong(session.getAttribute("companyId").toString()),Long.parseLong(session.getAttribute("branchId").toString()),session.getAttribute("financialYear").toString());
        view.addObject("DebitNoteList", purchaseVo);
        // log.info(purchaseVo.getShippingDate()+"date for debitnoteeeee*");
        return data;

    }

    @PostMapping(value = "/normal/items/{id}")
    @ResponseBody
    public List<Map<String, String>> itemsRetrivePurchase (@PathVariable(value = "type") String type,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_ITEMS_DETAILS;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_ITEMS_DETAILS;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ITEMS_DETAILS;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        ModelAndView view = new ModelAndView("purchase/purchase-view");
        int offset = page * size;

        List<Map<String, String>> data=purchaseService.findBypurchaseItems(id,Long.parseLong(session.getAttribute("companyId").toString()),Long.parseLong(session.getAttribute("branchId").toString()),session.getAttribute("financialYear").toString(),size,offset);


        return data;

    }

    @PostMapping(value = "/getitems/{id}")
    @ResponseBody
    public List<Map<String, String>> getPurchaseItemList (@PathVariable(value = "type") String type,HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.GET_PURCHASE_ITEMS_LIST, session))
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        return purchaseService.getPurchaseItemByPurchaseId(id,Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()),session.getAttribute(Constant.FINANCIAL_YEAR).toString());

    }

    @PostMapping(value = "/additionalCharges/{id}")
    @ResponseBody
    public List<PurchaseAdditionalChargeDTO> getAdditionalCharges (@PathVariable(value = "type") String type, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, @RequestParam(value = "parentType", required = false, defaultValue = "0") int parentType,HttpSession session) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_ADDITIONAL_CHARGES, session))
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        List<Map> purAdditionalCharges = purchaseAdditionalChargeRepository.findPurchaseAdditionalChargeByPurId(id);
        List<PurchaseAdditionalChargeDTO> purchaseAdditionalChargeDTO  = new ArrayList<>();
        String additionalChargeType;
        if(parentType == 1){
            additionalChargeType = Constant.REPORT_PURCHASE;
        }else{
            additionalChargeType = Constant.REPORT_SALES;
        }
        if(purAdditionalCharges !=null){
            for(Map<String,Object> additionalChargeData : purAdditionalCharges) {
                PurchaseAdditionalChargeDTO purchaseAdditionalChargesdto = new PurchaseAdditionalChargeDTO();
                if (!additionalChargeData.isEmpty() && additionalChargeData != null) {
                    long existingAdditionalChargeId = additionalChargeService.getAdditionalChargeIdByBranchIdAndNameAndType(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), additionalChargeData.get("additionalCharge").toString(),additionalChargeType);
                    if (existingAdditionalChargeId == 0) {
                        Long newAdditionalChargeid = purchaseService.createNewAdditionalCharge(additionalChargeData, Long.parseLong(session.getAttribute(Constant.USERID).toString()), Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
                        purchaseAdditionalChargesdto.setAdditionalChargeId(newAdditionalChargeid);
                    }else{
                        purchaseAdditionalChargesdto.setAdditionalChargeId(existingAdditionalChargeId);
                    }
                    purchaseAdditionalChargesdto.setAdditionalChargeValue(Double.parseDouble(additionalChargeData.get("additionalChargeValue").toString()));
                    purchaseAdditionalChargesdto.setAdditionalChargesType(additionalChargeData.get("additionalChargesType").toString());
                    purchaseAdditionalChargesdto.setPurchaseAdditionalChargeName(additionalChargeData.get("additionalCharge").toString());
                    purchaseAdditionalChargesdto.setAmount(Double.parseDouble(additionalChargeData.get("amount").toString()));
                    purchaseAdditionalChargesdto.setTaxAmount(Double.parseDouble(additionalChargeData.get("taxAmount").toString()));
                    purchaseAdditionalChargesdto.setTaxId(Long.parseLong(additionalChargeData.get("taxId").toString()));
                    purchaseAdditionalChargesdto.setTaxRate(Double.parseDouble(additionalChargeData.get("taxRate").toString()));
                }
                purchaseAdditionalChargeDTO.add(purchaseAdditionalChargesdto);
            }
        }
        return purchaseAdditionalChargeDTO;
    }

    @RequestMapping(value = "/attachmentDelete/{id}")
    @ResponseBody
    public String deleteDocument(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") long id, HttpSession session) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DELETE_ATTACHMENT, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	int result = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(id, Long.parseLong(session.getAttribute("branchId").toString()), 0);
    	if(result == 0) {
    	    return "404";
    	} else {
    	    PurchaseVo purchaseVo = purchaseService.deleteAttachment(id, Long.parseLong(session.getAttribute("branchId").toString()));
    	}
        return "";
    }
    @PostMapping("/{id}/varientId/debitnote/{varientId}/json")
    @ResponseBody
    public ProductVarientsVo viewVariantByOnlydebitnoteJSON(@PathVariable(value = "type") String type,
                                                        @PathVariable("id") long id, @PathVariable("varientId") String varientId, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PRODUCT_VERIANT_DEBITNOTE_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
       PurchaseItemVo purchaseItemVo = purchaseService.findByPurchaseIdAndBrancIdAndProductVArientId(
                id, Long.parseLong(session.getAttribute("branchId").toString()),Long.parseLong(varientId));


        if (purchaseItemVo == null) {

                return null;
        } else {

            purchaseItemVo.getProductVarientsVo().getProductVo().setProductVarientsVos(null);
            purchaseItemVo.getProductVarientsVo().setRetailerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setWholesalerPrice(purchaseItemVo.getPrice());
            purchaseItemVo.getProductVarientsVo().setMrp(purchaseItemVo.getMrp());
            purchaseItemVo.getProductVarientsVo().setQty(purchaseItemVo.getQty());
            if (purchaseItemVo.getProductVarientsVo().getProductVo().getPurchaseTaxIncluded() == 1) {
                purchaseItemVo.getProductVarientsVo()
                        .setPurchasePrice(purchaseItemVo.getPrice() + purchaseItemVo.getTaxAmount());
            } else {
                purchaseItemVo.getProductVarientsVo().setPurchasePrice(purchaseItemVo.getPrice());
            }

            // purchaseItemVo.getProductVarientsVo().get
            return purchaseItemVo.getProductVarientsVo();
        }

    }


    @PostMapping("/receiveallitem/{id}")
	public String receiveAllItemAndGenrateMI(
			 @PathVariable(value = "id") long purchaseId,
			HttpSession session, HttpServletRequest request) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_ITEM_GENRATE_MI, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	String type =Constant.PURCHASE_MATERIALINWARD;
    	int  value= companySettingService.findValueByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.ADDQTYBY);
    	if(value !=1) {
    		return "redirect:/accessdenied";
    	}
    	PurchaseVo purchaseVo1 = purchaseService.findByPurchaseIdAndBranchId(purchaseId,Long.parseLong(session.getAttribute("branchId").toString()));
    	if(purchaseVo1 == null) {
    	    return "redirect:/404";
    	} else {
    		PurchaseVo purchaseVo = new PurchaseVo();
    		long idd = purchaseVo.getPurchaseId();
    		ContactAddressVo contactAddressVo;
    		DecimalFormat df2 = new DecimalFormat("#.##");
    		double miqty;
    		String postatus = "";
    		purchaseVo.setType(type);
    		String prefixtype = "MI";

    		long newPurchaseNo = purchaseService.getNewPurchaseNo(type,
    				Long.parseLong(session.getAttribute("branchId").toString()),
    				Long.parseLong(session.getAttribute("userId").toString()), prefixtype, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

    		purchaseVo.setPurchaseNo(newPurchaseNo);
    		purchaseVo.setPrefix(prefixtype);
    		purchaseVo.setBillNo(prefixtype+newPurchaseNo);
    		if (type.equals(Constant.PURCHASE_MATERIALINWARD)) {
    			purchaseVo.setStatus("open");
    			//System.err.println("HERE type is :" + Constant.PURCHASE_MATERIALINWARD);
    			long poId = purchaseId;
    			//System.err.println("HERE purchase order id is :" + poId);

    			double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId,
    					Long.parseLong(session.getAttribute("branchId").toString()));
    			//System.err.println("HERE poqty is :" + poqty);
    			if (purchaseVo1.getPurchaseItemVos() != null) {
    				miqty = purchaseVo1.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
    				//System.err.println("HERE miqty is :" + miqty);
    				if (miqty == poqty) {
    					postatus = "delivered";
    				} else if (miqty < poqty) {
    					postatus = "partiallydelivered";
    				} else if (miqty > poqty) {
    					postatus = "exceed";
    				}
    				//System.err.println("HERE postatus is :" + postatus);
    				purchaseService.updatePurchaseStatus(poId, postatus);

    				/*
    				 * .mapToInt(Integer::intValue) .sum();
    				 */
    			} else {
    				purchaseVo.setStatus("mi created");
    			}

    		}
    		double purchaseTotalTaxAmount = 0.0;
    		try {
    			if (purchaseVo1.getPurchaseItemVos() != null) {
    				purchaseTotalTaxAmount = purchaseVo1.getPurchaseItemVos().stream().mapToDouble(q -> q.getTaxAmount())
    						.sum();
    			} else {

    			}

    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		purchaseVo.setPurchaseTotalTaxAmount(purchaseTotalTaxAmount);

    		purchaseVo.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
    		purchaseVo.setModifiedOn(CurrentDateTime.getCurrentDate());
    		purchaseVo.setBranchId(Long.parseLong(session.getAttribute("branchId").toString()));
    		purchaseVo.setCompanyId(Long.parseLong(session.getAttribute("companyId").toString()));

    		purchaseVo.setContactVo(purchaseVo1.getContactVo());
    		purchaseVo.setPurchaseVo(purchaseVo1);
    		purchaseVo.setSez(purchaseVo1.getSez());
    		purchaseVo.setTaxType(purchaseVo1.getTaxType());
    		purchaseVo.setTotal(purchaseVo1.getTotal());
    		purchaseVo.setRoundoff(purchaseVo1.getRoundoff());
    		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    		try {
    			purchaseVo.setPurchaseDate(dateFormat.parse(CurrentDateTime.getTodayDate()));
    		} catch (ParseException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		try {
    			if (!purchaseVo1.getTermsAndConditionIds().equals("")) {
    				purchaseVo.setTermsAndConditionIds(purchaseVo1.getTermsAndConditionIds().substring(0,
    						purchaseVo1.getTermsAndConditionIds().length() - 1));
    			} else {

    			}
    		} catch (Exception e) {
    		}


    		PurchaseVo purchaseVo2 = null;


    			purchaseVo2 = purchaseService.findByPurchaseIdAndBranchId(purchaseVo.getPurchaseId(),
    					purchaseVo.getBranchId());


    		try {
    			// --------------Set Billing Address Details ------------------------

    				purchaseVo.setBillingAddressLine1(purchaseVo1.getBillingAddressLine1());
    				purchaseVo.setBillingAddressLine2(purchaseVo1.getBillingAddressLine2());
    				purchaseVo.setBillingCityCode(purchaseVo1.getBillingCityCode());
    				purchaseVo.setBillingCompanyName(purchaseVo1.getBillingCompanyName());
    				purchaseVo.setBillingCountriesCode(purchaseVo1.getBillingCountriesCode());
    				purchaseVo.setBillingFirstName(purchaseVo1.getBillingFirstName());
    				purchaseVo.setBillingLastName(purchaseVo1.getBillingLastName());
    				purchaseVo.setBillingPinCode(purchaseVo1.getBillingPinCode());
    				purchaseVo.setBillingStateCode(purchaseVo1.getBillingStateCode());


    			// --------------Set Shipping Address Details ------------------------


    				purchaseVo.setShippingAddressLine1(purchaseVo1.getShippingAddressLine1());
    				purchaseVo.setShippingAddressLine2(purchaseVo1.getShippingAddressLine2());
    				purchaseVo.setShippingCityCode(purchaseVo1.getShippingCityCode());
    				purchaseVo.setShippingCompanyName(purchaseVo1.getShippingCompanyName());
    				purchaseVo.setShippingCountriesCode(purchaseVo1.getShippingCountriesCode());
    				purchaseVo.setShippingFirstName(purchaseVo1.getShippingFirstName());
    				purchaseVo.setShippingLastName(purchaseVo1.getShippingLastName());
    				purchaseVo.setShippingPinCode(purchaseVo1.getShippingPinCode());
    				purchaseVo.setShippingStateCode(purchaseVo1.getShippingStateCode());

    		} catch (Exception e) {
    			e.printStackTrace();
    		}


    		if (purchaseVo.getPurchaseId() == 0) {
    			purchaseVo.setCreatedBy(Long.parseLong(session.getAttribute("userId").toString()));
    			purchaseVo.setCreatedOn(CurrentDateTime.getCurrentDate());
    			purchaseVo.setPaidAmount(0.0);
    		}

    		List<PurchaseItemVo> itemlist = new ArrayList<PurchaseItemVo>();
    		for (int i = 0; i < purchaseVo1.getPurchaseItemVos().size(); i++) {
    			PurchaseItemVo item = new PurchaseItemVo();
    			item.setDesignNo(purchaseVo1.getPurchaseItemVos().get(i).getDesignNo());
    			item.setDiscount(purchaseVo1.getPurchaseItemVos().get(i).getDiscount());
    			item.setDiscount2(purchaseVo1.getPurchaseItemVos().get(i).getDiscount2());
    			item.setDiscountType(purchaseVo1.getPurchaseItemVos().get(i).getDiscountType());
    			item.setDiscountType2(purchaseVo1.getPurchaseItemVos().get(i).getDiscountType2());
    			item.setFreeQty(purchaseVo1.getPurchaseItemVos().get(i).getFreeQty());
    			item.setLandingCost(purchaseVo1.getPurchaseItemVos().get(i).getLandingCost());
    			item.setMrp(purchaseVo1.getPurchaseItemVos().get(i).getMrp());
    			item.setPrice(purchaseVo1.getPurchaseItemVos().get(i).getPrice());
    			item.setProduct(purchaseVo1.getPurchaseItemVos().get(i).getProduct());
    			item.setProductDescription(purchaseVo1.getPurchaseItemVos().get(i).getProductDescription());
    			item.setProductVarientsVo(purchaseVo1.getPurchaseItemVos().get(i).getProductVarientsVo());
    			item.setPurchaseVo(purchaseVo);
    			item.setQty(purchaseVo1.getPurchaseItemVos().get(i).getQty());
    			item.setPoqty(purchaseVo1.getPurchaseItemVos().get(i).getQty());
    			item.setTaxAmount(purchaseVo1.getPurchaseItemVos().get(i).getTaxAmount());
    			item.setTaxRate(purchaseVo1.getPurchaseItemVos().get(i).getTaxRate());
    			item.setTaxVo(purchaseVo1.getPurchaseItemVos().get(i).getTaxVo());
    			item.setTotal(purchaseVo1.getPurchaseItemVos().get(i).getTotal());
    			itemlist.add(item);
    		}

    		purchaseVo.setPurchaseItemVos(itemlist);

    		purchaseVo.setGstApply(purchaseVo1.getGstApply());


    		PurchaseVo purchaseVo3 = purchaseService.save(purchaseVo);

    		  //update receive qty
            purchaseService.updatereceiveQty(purchaseVo3);
            //view.addObject(Constant.STOCKBYMI, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYMI).getValue());
            //view.addObject(Constant.STOCKBYBILL, companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.STOCKBYBILL).getValue());
    		CompanySettingVo addQtyBy = companySettingService.findByBranchIdAndType(Long.parseLong(session.getAttribute("branchId").toString()), Constant.ADDQTYBY);
    		if(addQtyBy.getValue()==1) {//1  means it is by Material Inward
        		long poId = purchaseVo.getPurchaseVo().getPurchaseId();
        		double poqty = purchaseService.getTotalQtyByPurchaseAndBranchId(poId, Long.parseLong(session.getAttribute("branchId").toString()));
        		miqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(q -> q.getQty()).sum();
        		if(miqty <=poqty) {
        			 purchaseService.updatePurchaseReceivedqty(poId, miqty);
        		}else {
        			 purchaseService.updatePurchaseReceivedqty(poId, poqty);
        		}
        	}

    		purchaseVo3.getPurchaseItemVos().forEach((p -> {
    			shopifyServiceNew.updateStockAdjustmentByProductVariantId(new ArrayList<Long>(){{add(p.getProductVarientsVo().getProductVarientId());}},
                        Long.parseLong(session.getAttribute("companyId").toString()));
    		}));

    		if (purchaseVo.getType().equals(Constant.PURCHASE_BILL)
    				|| purchaseVo.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
    			purchaseService.insertPurchaseTransaction(purchaseVo3, session.getAttribute("financialYear").toString());
    		}


    		// Send Message
    		sendMessageToCutomer(purchaseVo3, session);



    		return "redirect:/purchase/" + type ;
    	}


	}


    @PostMapping("/duplicate/{id}")
	public String generateDuplicatePO(
			 @PathVariable(value = "id") long purchaseId,
			HttpSession session, HttpServletRequest request) throws IOException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DUPLICATE_PO, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	String type =Constant.PURCHASE_ORDER;

    	PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(purchaseId,Long.parseLong(session.getAttribute("branchId").toString()));
    	if(purchaseVo == null) {
    	    return "redirect:/404";
    	} else {
    		long newPurchaseNo = purchaseService.getNewPurchaseNo(type,
                    Long.parseLong(session.getAttribute("branchId").toString()),
                    Long.parseLong(session.getAttribute("userId").toString()), purchaseVo.getPrefix(), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

       	try {
       		purchaseVo.getContactVo().setContactProductVos(null);
           	purchaseVo.getPurchaseItemVos().forEach(pi->{
           		pi.getProductVarientsVo().getProductVo().setProductAttributeVos(null);
           	});
		} catch (Exception e) {
			// TODO: handle exception
		}

       	//create clone purchase vo
            PurchaseVo purchaseVo1 =  new PurchaseVo();
            BeanUtils.copyProperties(purchaseVo,purchaseVo1);
            purchaseVo1.setPurchaseId(0);


       	//System.err.println("clone purchase "+purchaseVo1.getPurchaseId());
       	purchaseVo1.setPurchaseNo(newPurchaseNo);
       	purchaseVo1.setBillNo(purchaseVo1.getPrefix()+newPurchaseNo);
       	purchaseVo1.setContactVo(purchaseVo.getContactVo());
       	purchaseVo1.setAlterBy(Long.parseLong(session.getAttribute("userId").toString()));
           purchaseVo1.setModifiedOn(CurrentDateTime.getCurrentDate());


       	List<PurchaseItemVo> items = new ArrayList<PurchaseItemVo>();
       	purchaseVo.getPurchaseItemVos().forEach(x->{

       		PurchaseItemVo item = new PurchaseItemVo();
               BeanUtils.copyProperties(x,item);
               item.setPurchaseItemId(0);
       		//System.err.println("clone purchaseitem "+item.getPurchaseItemId());
       		item.setPurchaseVo(purchaseVo1);
       		item.setProduct(x.getProduct());
       		item.setTaxVo(x.getTaxVo());
       		item.setProductVarientsVo(x.getProductVarientsVo());
       		item.setBrandVo(x.getBrandVo());
       		item.setCategoryVo(x.getCategoryVo());
       		items.add(item);
       		});
       	purchaseVo1.setPurchaseItemVos(items);
       	//System.err.println("before save");
       	PurchaseVo purchaseVo3 = purchaseService.save(purchaseVo1);
   		return "redirect:/purchase/" + type ;
    	}


	}

    @ResponseBody
    @RequestMapping("/statistic")
    @Transactional(readOnly = true)
    public StatisticDTO getstatisticData(@RequestParam("daterange") String daterange,@PathVariable(value = "type") String type,
    		@RequestParam Map<String, String> allRequestParams,HttpSession session) throws ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_STATISTIC;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_STATISTIC;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_STATISTIC;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_STATISTIC;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_STATISTIC;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	StatisticDTO dto = new StatisticDTO();
    	//long branchId =  Long.parseLong(session.getAttribute("branchId").toString());

    	 List<Long> branchList =new ArrayList<Long>();

     	if(StringUtils.isNotBlank(allRequestParams.get("branch"))) {
     		branchList = Arrays.asList(allRequestParams.get("branch").split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
         }else {
         	branchList.add(Long.parseLong(session.getAttribute("branchId").toString()));
         }


    	Date startDate= null;
    	Date endDate =null;
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    	Calendar calendar = Calendar.getInstance();
    	//System.err.println("daterange---"+daterange);
    	if (daterange.equals("currentYear")) {
            calendar.setTime(dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()));
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
            calendar.setTime(dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
        } else if (daterange.equals("lastMonth")) {
	       	 calendar.set(Calendar.DAY_OF_MONTH, -1);
	         calendar.add(Calendar.DATE, 1);
	         int min =calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
	         calendar.set(Calendar.DAY_OF_MONTH, min);
	         startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
	         int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	         calendar.set(Calendar.DAY_OF_MONTH, max);
	         endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
	    }else if (daterange.equals("thisMonth")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, max);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
        } else if (daterange.equals("thisWeek")) {
            calendar.add(Calendar.DATE, -7);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
            calendar.add(Calendar.DATE, 7);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
        } else if (daterange.equals("lastWeek")) {
            calendar.add(Calendar.DATE, -14);
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
            calendar.add(Calendar.DATE, 7);
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
        }else if (daterange.equals("today")) {
            startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
            endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
        }else if (daterange.equals("customrange")) {
        	if(!allRequestParams.get("from").equals("") ) {
        	String[] Daterange = allRequestParams.get("from").split("-");
        	startDate= dateFormat.parse(Daterange[0]);
        	endDate=dateFormat.parse(Daterange[1]);
        	}
        }
        DecimalFormat df = new DecimalFormat("#." + "0".repeat(Integer.parseInt(session.getAttribute("decimalPoint").toString())));

    	try {
			dto.setOrdersString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "0")));
			dto.setIsToApproveString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, Constant.TOAPPROVE)));
			dto.setIsRejectedString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, Constant.REJECTED)));

			dto.setIsdraftString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, Constant.DRAFT)));
	    	dto.setIscloseString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "close")));
	    	dto.setIsdeliveredString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "delivered")));
	    	dto.setIspartiallyDeliveredString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "partiallydelivered")));
	    	dto.setIsexccedString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "exceed")));
	    	dto.setIsopenString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "open")));
	    	dto.setIscompletedString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "completed")));
	    	dto.setIscancelString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscCount( branchList, startDate, endDate, type, "cancel")));
	    	if(type.equals(Constant.PURCHASE_BILL)) {
                String totalRaw = purchaseService.purchaseStatiscAmount(branchList, startDate, endDate, type, "total");
                String paidRaw = purchaseService.purchaseStatiscAmount(branchList, startDate, endDate, type, "paid");
                String unpaidRaw = purchaseService.purchaseStatiscAmount(branchList, startDate, endDate, type, "unpaid");

                dto.setTotalamountRaw(df.format(Double.parseDouble(totalRaw)));
                dto.setPaidamountRaw(df.format(Double.parseDouble(paidRaw)));
                dto.setUnpaidamountRaw(df.format(Double.parseDouble(unpaidRaw)));

                dto.setTotalamountString(NumberToWord.getCheckData(totalRaw));
                dto.setPaidamountString(NumberToWord.getCheckData(paidRaw));
                dto.setUnpaidamountString(NumberToWord.getCheckData(unpaidRaw));
            }else if(type.equals(Constant.PURCHASE_DEBIT_NOTE))
	    	{
	    		dto.setTotalamountString(NumberToWord.getCheckData("" +purchaseService.purchaseStatiscAmount( branchList, startDate, endDate, type, "total")));
	    	}else {

	    	}
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return dto;
    }

    @Async
    public void generatedebitnote(PurchaseVo purchaseVo,Map<String, String> allRequestParams,HttpSession session) {

    	if(purchaseVo.getPurchaseReturnItemDTO()!= null && purchaseVo.getPurchaseReturnItemDTO().size()>0) {
    		////System.err.println("getPurchaseReturnItemDTO "+purchaseVo.getPurchaseReturnItemDTO().size());
        	////System.err.println("return_total "+allRequestParams.get("return_total"));

    		String type =Constant.PURCHASE_DEBIT_NOTE;
            String prefix = prefixService.getPrefixByPrefixTypeAndBranchId(Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), type, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));


    	 long newPurchaseNo = purchaseService.getNewPurchaseNo(type,
                 Long.parseLong(session.getAttribute("branchId").toString()),
                 Long.parseLong(session.getAttribute("userId").toString()),prefix, Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

    	try {
       		purchaseVo.getContactVo().setContactProductVos(null);
           	purchaseVo.getPurchaseItemVos().forEach(pi->{
           		pi.getProductVarientsVo().getProductVo().setProductAttributeVos(null);
           	});
		} catch (Exception e) {
			// TODO: handle exception
		}
    	//create clone purchase vo

    	PurchaseVo purchaseVo1= new PurchaseVo();
        BeanUtils.copyProperties(purchaseVo,purchaseVo1);
        purchaseVo1.setPurchaseId(0);
    	//System.err.println("clone purchase "+purchaseVo1.getPurchaseId());
    	purchaseVo1.setPurchaseNo(newPurchaseNo);
        purchaseVo1.setPrefix(prefix);
    	purchaseVo1.setBillNo(purchaseVo1.getPrefix()+newPurchaseNo);
    	purchaseVo1.setType(type);
    	purchaseVo1.setContactVo(purchaseVo.getContactVo());
    	purchaseVo1.setAlterBy(Long.parseLong(session.getAttribute(Constant.USERID).toString()));
        purchaseVo1.setModifiedOn(CurrentDateTime.getCurrentDate());
        purchaseVo1.setTotal(Double.parseDouble(allRequestParams.get("return_total")));
        purchaseVo1.setRoundoff(Float.parseFloat(allRequestParams.get("return_roundoff")));
        purchaseVo1.setPurchaseVo(purchaseVo);
        purchaseVo1.setStatus("open");
        purchaseVo1.setIsDNbyBill(1);
        purchaseVo1.setPaymentTermsVo(null);
        purchaseVo1.setTcsJson(null);
        purchaseVo1.setTdsJson(null);
        purchaseVo1.setPaidAmount(0.0); // Paid amount set as 0.0 because tds amount applied in bill showing as paid amount in debit note.
        List<PurchaseItemVo> items = new ArrayList<PurchaseItemVo>();

    	List<PurchaseReturnItemDTO> dto = purchaseVo.getPurchaseReturnItemDTO();
    	dto.forEach(x->{
    		PurchaseItemVo item = new PurchaseItemVo();
            BeanUtils.copyProperties(x,item);
            item.setPurchaseItemId(0);
    		item.setItemCode(x.getItemCode());
			item.setDesignNo(x.getDesignNo());
			item.setDiscount(x.getDiscount());
			item.setDiscount2(x.getDiscount2());
			item.setDiscountType(x.getDiscountType());
			item.setDiscountType2(x.getDiscountType2());
			item.setFreeQty(x.getFreeQty());
			item.setLandingCost(x.getLandingCost());
			item.setMrp(x.getMrp());
			item.setPrice(x.getPrice());
			item.setProduct(x.getProduct());
			item.setProductDescription(x.getProductDescription());
			item.setProductVarientsVo(x.getProductVarientsVo());
			item.setPurchaseVo(purchaseVo1);
			item.setQty(x.getQty());
            item.setBatchId(x.getBatchId());
            item.setSellingPrice(x.getSellingPrice());
			item.setTaxAmount(x.getTaxAmount());
			item.setTaxRate(x.getTaxRate());
			item.setTaxVo(x.getTaxVo());
			item.setTotal(x.getTotal());
            item.setNetAmount(x.getNetAmount());
            item.setOrderBy(x.getOrderBy());
            item.setExpiryManage(x.getIsExpirymange());
            //System.err.println("clone purchaseitem "+item.getPurchaseItemId());

    		items.add(item);
    		});
    	purchaseVo1.setPurchaseItemVos(items);
    	purchaseVo1.getPurchaseItemVos().removeIf(rm -> rm.getProduct() == null);
    	purchaseVo1.setFlatDiscount(0L);
//    	List<PurchaseAdditionalCharge> additionlisAdditionalCharges = new ArrayList<>();
//    	for (int j = 0; j < purchaseVo.getPurchaseAdditionalChargeVos().size(); j++) {
//    		PurchaseAdditionalCharge additioncharge = new PurchaseAdditionalCharge();
//    		PurchaseAdditionalCharge additional = purchaseVo.getPurchaseAdditionalChargeVos().get(j);
//    		additioncharge.setAdditionalChargeVo(additional.getAdditionalChargeVo());
//    		additioncharge.setAmount(additional.getAmount());
//    		additioncharge.setPurchaseVo(purchaseVo1);
//    		additioncharge.setTaxAmount(additional.getTaxAmount());
//    		additioncharge.setTaxRate(additional.getTaxRate());
//    		additioncharge.setTaxVo(additional.getTaxVo());;
//    		additionlisAdditionalCharges.add(additioncharge);
//
//    	}
    	purchaseVo1.setPurchaseAdditionalChargeVos(null);
    	if (purchaseVo1.getPurchaseAdditionalChargeVos() != null) {
    		purchaseVo1.getPurchaseAdditionalChargeVos().removeIf(rm -> rm.getAdditionalChargeVo() == null);
    		purchaseVo1.getPurchaseAdditionalChargeVos().forEach(item1 -> item1.setPurchaseVo(purchaseVo1));
        }

    	double purchaseTotalTaxAmount = 0.0;
        try {
        	if (purchaseVo1.getPurchaseItemVos() != null) {
        		purchaseTotalTaxAmount += purchaseVo1.getPurchaseItemVos().stream().mapToDouble(q -> q.getTaxAmount()).sum();
//        		purchaseTotalTaxAmount += purchaseVo1.getPurchaseAdditionalChargeVos().stream().mapToDouble(q -> q.getTaxAmount()).sum();
        	}else {

        	}

		} catch (Exception e) {
			e.printStackTrace();
		}
        purchaseVo1.setPurchaseTotalTaxAmount(purchaseTotalTaxAmount);

        // log.warning("additionalcharge - SIZE---------->"+purchaseVo1.getPurchaseAdditionalChargeVos().size());;

    	//System.err.println("before save");
        List<String> groupNature = new  ArrayList<String>();
        groupNature.add(Constant.ACCOUNT_PURCHASE_RETURN);
    	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(purchaseVo1.getCompanyId(),purchaseVo1.getBranchId(),groupNature);
    	if(accountCustomDTO.size()>0) {
    		purchaseVo1.setAccountCustomId(accountCustomDTO.get(0).getAccountCustomId());
    	}



    	PurchaseVo purchaseVo3 = purchaseService.save(purchaseVo1);

    	purchaseService.insertPurchaseDebitNote(purchaseVo3, session.getAttribute("financialYear").toString());
    	purchaseService.updatedebitNoteId(purchaseVo.getPurchaseId(),purchaseVo3.getPurchaseId());
    	}
    }

    @Async
    public void updatedebitnote(PurchaseVo purchaseVo,Map<String, String> allRequestParams,HttpSession session) {
    	//System.err.println("debit note id----"+purchaseVo.getDebitNoteId());
    	////System.err.println("getPurchaseReturnItemDTO "+purchaseVo.getPurchaseReturnItemDTO().size());
    	if(purchaseVo.getDebitNoteId() !=0) {
    	PurchaseVo purchasevo1 = purchaseService.findByPurchaseIdAndBranchId(purchaseVo.getDebitNoteId(), purchaseVo.getBranchId());
    	if(purchasevo1  != null) {
    	//System.err.println("ids----"+purchasevo1.getPurchaseId());

  //  	List<Map<String, String>> data=purchaseService.findBypurchaseItems(purchaseVo.getDebitNoteId());
    	List<Long> l = new ArrayList<>();

//    	for (int i = 0; i < data.size(); i++) {
//		l.add(Long.parseLong( data.get(i).get("purchase_item_id")));
//	}
    	purchasevo1.getPurchaseItemVos().forEach(x->{

    		l.add(x.getPurchaseItemId());
    		});
    	purchaseService.deletePurchaseItem(l);


    	purchasevo1.setTotal(Double.parseDouble(allRequestParams.get("return_total")));
    	purchasevo1.setRoundoff(Float.parseFloat(allRequestParams.get("return_roundoff")));

    	 List<String> groupNature = new  ArrayList<String>();
         groupNature.add(Constant.ACCOUNT_PURCHASE_RETURN);
     	List<AccountCustomDTO> accountCustomDTO = accountCustomService.findAccountCustomByBranchIdAndGroupNature(purchasevo1.getCompanyId(),purchasevo1.getBranchId(),groupNature);
     	if(accountCustomDTO.size()>0) {
     		purchasevo1.setAccountCustomId(accountCustomDTO.get(0).getAccountCustomId());
     	}
         if(purchaseVo.getPurchaseReturnItemDTO() !=null) { //edit purchase return items
    List<PurchaseItemVo> items = new ArrayList<PurchaseItemVo>();

	List<PurchaseReturnItemDTO> dto = purchaseVo.getPurchaseReturnItemDTO();
	dto.forEach(x->{
		PurchaseItemVo item = new PurchaseItemVo();
		item.setItemCode(x.getItemCode());
		item.setDesignNo(x.getDesignNo());
		item.setDiscount(x.getDiscount());
		item.setDiscount2(x.getDiscount2());
		item.setDiscountType(x.getDiscountType());
		item.setDiscountType2(x.getDiscountType2());
		item.setFreeQty(x.getFreeQty());
		item.setLandingCost(x.getLandingCost());
		item.setMrp(x.getMrp());
		item.setPrice(x.getPrice());
		item.setProduct(x.getProduct());
		item.setProductDescription(x.getProductDescription());
		item.setProductVarientsVo(x.getProductVarientsVo());
		item.setPurchaseVo(purchasevo1);
        item.setBatchId(x.getBatchId());
		item.setQty(x.getQty());
		item.setTaxAmount(x.getTaxAmount());
		item.setTaxRate(x.getTaxRate());
		item.setTaxVo(x.getTaxVo());
		item.setTotal(x.getTotal());
        item.setSellingPrice(x.getSellingPrice());
        item.setNetAmount(x.getNetAmount());

		//System.err.println("clone purchaseitem "+item.getPurchaseItemId());

		items.add(item);
		});
	purchasevo1.setPurchaseItemVos(items);
	purchasevo1.getPurchaseItemVos().removeIf(rm -> rm.getProduct() == null);
    purchasevo1.setTcsJson(null);
    purchasevo1.setTdsJson(null);
	PurchaseVo purchaseVo3 =	purchaseService.save(purchasevo1);
		purchaseService.insertPurchaseDebitNote(purchaseVo3, session.getAttribute("financialYear").toString());

         }else { // delete debit note
        	 //System.err.println("delete transaction-----"+purchasevo1.getPurchaseId());
        	 transactionService.deleteTransaction(purchaseVo.getBranchId(), purchasevo1.getPurchaseId(),
        			 purchasevo1.getType());
        	 stockTransactionService.deleteStockTransactionPurchase(purchaseVo.getBranchId(), purchasevo1.getPurchaseId(), purchasevo1.getType());
        	 purchaseService.deletePurchase(Long.parseLong(session.getAttribute("branchId").toString()), purchasevo1.getPurchaseId(), purchasevo1.getType());
        	 purchaseService.updatedebitNoteId(purchaseVo.getPurchaseId(),0);
         }
         	}
    	}else if(purchaseVo.getPurchaseReturnItemDTO()!=null)//new debit note add in edit page
    	{
    		if(purchaseVo.getPurchaseReturnItemDTO().size()>0) {
    			generatedebitnote(purchaseVo,allRequestParams,session);
    		}
    	}
   }


    @PostMapping("/{purchaseId}/datatable")
    @ResponseBody
    public  DataTablesOutput<MaterialInwardDTO> purchaseDatatableBillWise(
            @PathVariable(value = "purchaseId") long purchaseId,@PathVariable(value = "type") String type, HttpSession session, @Valid DataTablesInput input)
            throws ParseException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_BILL_WISE_DATATABLE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_BILL_WISE_DATATABLE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_WISE_DATATABLE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_BILL_WISE_DATATABLE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_BILL_WISE_DATATABLE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	  int totalRecord = 0;


    	 if (input.getLength() != -1) {
             if (input.getStart() == 0) {
                 input.setLength(input.getLength() - 1);
             } else {
                 input.setStart(input.getStart() - 1);
             }

         }else {
         	input.setLength(totalRecord);
         }

    	  DataTablesOutput<MaterialInwardDTO> a = new DataTablesOutput<>();


    	 List<MaterialInwardDTO> list = purchaseService.findparentMIdata(Constant.PURCHASE_MATERIALINWARD,purchaseId);

    	 a.setRecordsFiltered(list.size());
         //a.setDraw(draw);
         a.setRecordsTotal(list.size());
         a.setData(list);


        return a;
    }

    @PostMapping("/data/{id}/json")
    @ResponseBody
    public  List<PurchaseDTO> newpurchaseDetailsJson(@PathVariable long id, HttpSession session) throws NumberFormatException, ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DETAIL_DATA_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	List<PurchaseDTO> purchaseDTOs=new ArrayList<PurchaseDTO>();
    	PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id, Long.parseLong(session.getAttribute("branchId").toString()));
        if(purchaseVo != null) {
        	purchaseVo.getContactVo().getContactAddressVos().forEach(ad -> ad.setContact(null));
            purchaseVo.setPurchaseVo(null);
//            purchaseVo.setPurchaseItemVos(null);

            for(PurchaseItemVo purchaseItemVo : purchaseVo.getPurchaseItemVos()) {

            	PurchaseDTO dto=new PurchaseDTO();

            	dto.setDescription(purchaseItemVo.getProductDescription());
            	dto.setDiscount(purchaseItemVo.getDiscount());
            	dto.setDiscount2(purchaseItemVo.getDiscount2());
            	dto.setDiscountType(purchaseItemVo.getDiscountType());
            	dto.setDiscountType2(purchaseItemVo.getDiscountType2());
            	dto.setItemCode(purchaseItemVo.getProductVarientsVo().getItemCode());

            	if(StringUtils.isNotBlank(purchaseItemVo.getBatchCreationDate())){
            		dto.setBatchCreationDate(purchaseItemVo.getBatchCreationDate().toString());
            	}
            	if(StringUtils.isNotBlank(purchaseItemVo.getBatchExpiryDate())){
            		dto.setBatchExpiryDate(purchaseItemVo.getBatchExpiryDate().toString());
            	}
            	dto.setExpiryManage(purchaseItemVo.getExpiryManage());
            	dto.setExpdays(purchaseItemVo.getExpdays());
            	try {
            		if(StringUtils.isNotEmpty(purchaseItemVo.getItemCode())) {
                    	dto.setItemCode(purchaseItemVo.getItemCode());
                    }
    			} catch (Exception e) {
    				e.printStackTrace();
    			}


            	//dto.setPrice(purchaseItemVo.getPrice()+(purchaseItemVo.getTaxAmount() / purchaseItemVo.getQty()));
            	dto.setPrice(purchaseItemVo.getPrice());
            	dto.setMrp(purchaseItemVo.getMrp());
            	dto.setSellingPrice(purchaseItemVo.getSellingPrice());
            	try {
            		 if (purchaseItemVo.getProductVarientsVo().getProductVo().getPurchaseTaxIncluded() == 1) {
                		 dto.setPurchasePrice(purchaseItemVo.getPrice() + (purchaseItemVo.getTaxAmount() / purchaseItemVo.getQty()));
                     } else {
                    	 dto.setPurchasePrice(purchaseItemVo.getPrice());
                     }
    			} catch (Exception e) {
    				e.printStackTrace();
    				dto.setPurchasePrice(purchaseItemVo.getPrice()+(purchaseItemVo.getTaxAmount() / purchaseItemVo.getQty()));
    			}

            	dto.setVarientName(purchaseItemVo.getProductVarientsVo().getVarientName());
            	dto.setProductId(purchaseItemVo.getProduct().getProductId());
            	dto.setProductVarientId(purchaseItemVo.getProductVarientsVo().getProductVarientId());
            	dto.setPurchaseTaxIncluded(purchaseItemVo.getProduct().getPurchaseTaxIncluded());
            	dto.setQty(purchaseItemVo.getQty()-purchaseItemVo.getReceiveQty());
            	dto.setReceiveqty(purchaseItemVo.getReceiveQty());
            	dto.setTaxName(purchaseItemVo.getTaxVo().getTaxName());
            	dto.setTaxAmount(purchaseItemVo.getTaxAmount());
            	dto.setTaxId(purchaseItemVo.getTaxVo().getTaxId());
            	dto.setTaxRate(purchaseItemVo.getTaxRate());
            	dto.setUom(purchaseItemVo.getProduct().getUnitOfMeasurementVo().getMeasurementCode());
            	dto.setProductName(purchaseItemVo.getProduct().getName());
            	dto.setCategoryId(purchaseItemVo.getProduct().getCategoryVo().getCategoryId());
            	dto.setBrandId(purchaseItemVo.getProduct().getBrandVo().getBrandId());;

            	purchaseDTOs.add(dto);
            }
            purchaseDTOs.get(0).setFlatDiscount(purchaseVo.getFlatDiscount());
        }
        return purchaseDTOs;
    }

    @PostMapping("/normal/data/{id}/json")
    @ResponseBody
    public List<PurchaseDTO> purchaseDetails(@PathVariable long id,
                                                      @RequestParam(value = "limit", defaultValue = "10") int limit,
                                                      @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                      HttpSession session) throws NumberFormatException, ParseException {

        // Rate limit check
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DETAIL_DATA_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        // Prepare response map

        List<PurchaseDTO> purchaseDTOs = new ArrayList<>();

        // Fetch purchase data
        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id, Long.parseLong(session.getAttribute("branchId").toString()));

        if (purchaseVo != null) {
            // Process PurchaseVo details
            purchaseVo.getContactVo().getContactAddressVos().forEach(ad -> ad.setContact(null));
            purchaseVo.setPurchaseVo(null);

            // Get list of items from the PurchaseVo
            List<PurchaseItemVo> purchaseItems = purchaseVo.getPurchaseItemVos();

            // Apply limit and offset
            int totalItems = purchaseItems.size();  // Total number of items
            int end = Math.min(offset + limit, totalItems);  // Calculate end index

            // Extract the requested subset of purchase items
            List<PurchaseItemVo> paginatedItems = purchaseItems.subList(offset, end);

            // Convert items to DTOs
            for (PurchaseItemVo purchaseItemVo : paginatedItems) {
                PurchaseDTO dto = new PurchaseDTO();
                dto.setDescription(purchaseItemVo.getProductDescription());
                dto.setDiscount(purchaseItemVo.getDiscount());
                dto.setDiscount2(purchaseItemVo.getDiscount2());
                dto.setDiscountType(purchaseItemVo.getDiscountType());
                dto.setDiscountType2(purchaseItemVo.getDiscountType2());
                dto.setItemCode(purchaseItemVo.getProductVarientsVo().getItemCode());
                if (StringUtils.isNotBlank(purchaseItemVo.getBatchCreationDate())) {
                    dto.setBatchCreationDate(purchaseItemVo.getBatchCreationDate().toString());
                }
                if (StringUtils.isNotBlank(purchaseItemVo.getBatchExpiryDate())) {
                    dto.setBatchExpiryDate(purchaseItemVo.getBatchExpiryDate().toString());
                }
                dto.setExpiryManage(purchaseItemVo.getExpiryManage());
                dto.setExpdays(purchaseItemVo.getExpdays());

                dto.setPrice(purchaseItemVo.getPrice());
                dto.setMrp(purchaseItemVo.getMrp());
                dto.setSellingPrice(purchaseItemVo.getSellingPrice());

                try {
                    if (purchaseItemVo.getProductVarientsVo().getProductVo().getPurchaseTaxIncluded() == 1) {
                        dto.setPurchasePrice(purchaseItemVo.getPrice() + (purchaseItemVo.getTaxAmount() / purchaseItemVo.getQty()));
                    } else {
                        dto.setPurchasePrice(purchaseItemVo.getPrice());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setPurchasePrice(purchaseItemVo.getPrice() + (purchaseItemVo.getTaxAmount() / purchaseItemVo.getQty()));
                }

                dto.setVarientName(purchaseItemVo.getProductVarientsVo().getVarientName());
                dto.setProductId(purchaseItemVo.getProduct().getProductId());
                dto.setProductVarientId(purchaseItemVo.getProductVarientsVo().getProductVarientId());
                dto.setPurchaseTaxIncluded(purchaseItemVo.getProduct().getPurchaseTaxIncluded());
                dto.setQty(purchaseItemVo.getQty() - purchaseItemVo.getReceiveQty());
                dto.setReceiveqty(purchaseItemVo.getReceiveQty());
                dto.setTaxName(purchaseItemVo.getTaxVo().getTaxName());
                dto.setTaxAmount(purchaseItemVo.getTaxAmount());
                dto.setTaxId(purchaseItemVo.getTaxVo().getTaxId());
                dto.setTaxRate(purchaseItemVo.getTaxRate());
                dto.setUom(purchaseItemVo.getProduct().getUnitOfMeasurementVo().getMeasurementCode());
                dto.setProductName(purchaseItemVo.getProduct().getName());
                dto.setCategoryId(purchaseItemVo.getProduct().getCategoryVo().getCategoryId());
                dto.setBrandId(purchaseItemVo.getProduct().getBrandVo().getBrandId());

                purchaseDTOs.add(dto);
            }

            // Set flat discount for the first DTO
            if (!purchaseDTOs.isEmpty()) {
                purchaseDTOs.get(0).setFlatDiscount(purchaseVo.getFlatDiscount());
            }
        }



        return purchaseDTOs;
    }


    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable(value = "id") long purchaseId, @PathVariable(value = "type") String type,
            HttpSession session, HttpServletRequest request) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CANCEL_ORDER;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CANCEL_ORDER;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        int response = purchaseService.countByPurchaseIdAndBranchIdAndIsDeleted(purchaseId,
                Long.parseLong(session.getAttribute("branchId").toString()), 0);
        if (response == 0) {
            return "redirect:/404";
        } else {
            purchaseService.updatePurchaseStatus(purchaseId, "cancel");
            return "redirect:/purchase/" + type;
        }

    }

    @RequestMapping(value = "/htmlpdf/{id}")
    public ModelAndView htmlpdf(@PathVariable("id") String salesId, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_HTML_PDF, session)) {
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
		PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(id,
				Long.parseLong(session.getAttribute("branchId").toString()));
		if (purchaseVo == null || purchaseVo.getIsDeleted() == 1) {
			view.setViewName(Constant.ERROR_PAGE_404);
		} else {
			ReportSettingVo setting = reportService.findByTypeAndBranchId(Constant.PURCHASE_ORDER,
					Long.parseLong(session.getAttribute(Constant.BRANCHID).toString()), Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));

			UserFrontVo companyVo = userRepository
					.findByUserFrontId(purchaseVo.getBranchId());
			companyVo.setCityName(cityService.findByCityCode(companyVo.getCityCode()).getCityName());
			companyVo.setStateName(stateService.findByStateCode(companyVo.getStateCode()).getStateName());
			companyVo.setCountriesName(
					countryService.findByCountriesCode(companyVo.getCountriesCode()).getCountriesName());

			if (purchaseVo.getPurchaseItemVos() != null) {
				if (purchaseVo.getPurchaseItemVos().size() > 0) {
					double totalqty = purchaseVo.getPurchaseItemVos().stream().mapToDouble(x -> x.getQty()).sum();
					view.addObject("totalqty", totalqty);
				}
			}
			ContactVo contactVo = contactRepository.findByContactId(purchaseVo.getContactVo().getContactId());
			if (StringUtils.isNotBlank(purchaseVo.getShippingCityCode())) {
				purchaseVo.setShippingCityName(
						cityService.findByCityCode(purchaseVo.getShippingCityCode()).getCityName());
			}
			if (StringUtils.isNotBlank(purchaseVo.getShippingStateCode())) {
				purchaseVo.setShippingStateName(
						stateService.findByStateCode(purchaseVo.getShippingStateCode()).getStateName());
			}
			if (purchaseVo.getShippingCountriesCode() != null && !purchaseVo.getShippingCountriesCode().isEmpty()) {
				purchaseVo.setShippingCountriesName(
						countryService.findByCountriesCode(purchaseVo.getShippingCountriesCode()).getCountriesName());
			}
			String placeOfSupplyCode = session.getAttribute("stateCode").toString();

			placeOfSupplyCode = purchaseVo.getShippingStateCode();
			List<PurchaseItemPdfDTO> purchaseItemDTO = new ArrayList<PurchaseItemPdfDTO>();
			for (int i = 0; i < purchaseVo.getPurchaseItemVos().size(); i++) { /// sales Item
				PurchaseItemVo purchaseItemVo = purchaseVo.getPurchaseItemVos().get(i);
				PurchaseItemPdfDTO itemPdfDTO = new PurchaseItemPdfDTO();

				itemPdfDTO.setDiscount(purchaseItemVo.getDiscount());
				itemPdfDTO.setDiscountType(purchaseItemVo.getDiscountType());
				itemPdfDTO.setHsnCode(purchaseItemVo.getProductVarientsVo().getProductVo().getHsnCode());
				itemPdfDTO.setPrice(purchaseItemVo.getPrice());
				itemPdfDTO.setQty(purchaseItemVo.getQty());
				itemPdfDTO.setTaxAmount(purchaseItemVo.getTaxAmount());
				itemPdfDTO.setTaxRate(purchaseItemVo.getTaxRate());

				purchaseItemDTO.add(itemPdfDTO);
			}
			for (int i = 0; i < purchaseVo.getPurchaseAdditionalChargeVos().size(); i++) { // sales ledger account
				PurchaseAdditionalCharge additionalcharge = purchaseVo.getPurchaseAdditionalChargeVos().get(i);
				PurchaseItemPdfDTO itemPdfDTO = new PurchaseItemPdfDTO();

				itemPdfDTO.setDiscount(0);
				itemPdfDTO.setDiscountType("amount");
				itemPdfDTO.setHsnCode("-");
				itemPdfDTO.setPrice(additionalcharge.getAmount());
				itemPdfDTO.setQty(1);
				itemPdfDTO.setTaxAmount(additionalcharge.getTaxAmount());
				itemPdfDTO.setTaxRate(additionalcharge.getTaxRate());

				purchaseItemDTO.add(itemPdfDTO);
			}
			List<DeliveryTaxDTO> deliveryTaxDTOs = new ArrayList<DeliveryTaxDTO>();
			for (int i = 0; i < purchaseItemDTO.size(); i++) {
				PurchaseItemPdfDTO itemPdfDTO = purchaseItemDTO.get(i);
				int is_same = 0;
				if (i == 0) {
					if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {// CGST -SGST
//						System.err.println("CGST -SGST 1");
						double taxableamount = 0;
						if (itemPdfDTO.getDiscountType().equals("amount")) {
							taxableamount = round(
									(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(), 2);
						} else {
							taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
									- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount()) / 100),
									2);
						}

						DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
						deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
						deliveryTaxDTO2.setTaxableAmount(taxableamount);
						deliveryTaxDTO2.setCgstAmount(itemPdfDTO.getTaxAmount() / 2);
						deliveryTaxDTO2.setCgstRate(itemPdfDTO.getTaxRate() / 2);
						deliveryTaxDTO2.setSgstAmount(itemPdfDTO.getTaxAmount() / 2);
						deliveryTaxDTO2.setSgstRate(itemPdfDTO.getTaxRate() / 2);
						deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
						deliveryTaxDTOs.add(deliveryTaxDTO2);

					} else {// IGST
//						System.err.println("IGST 1");
						double taxableamount = 0;
						if (itemPdfDTO.getDiscountType().equals("amount")) {
							taxableamount = round(
									(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(), 2);
						} else {
							taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
									- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount()) / 100),
									2);
						}

						DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
						deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
						deliveryTaxDTO2.setTaxableAmount(taxableamount);
						deliveryTaxDTO2.setIgstAmount(itemPdfDTO.getTaxAmount());
						deliveryTaxDTO2.setIgstRate(itemPdfDTO.getTaxRate());
						deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
						deliveryTaxDTOs.add(deliveryTaxDTO2);
					}
				} else {
					for (int j = 0; j < deliveryTaxDTOs.size(); j++) {

						DeliveryTaxDTO deliveryTaxDTO = deliveryTaxDTOs.get(j);
						if (itemPdfDTO.getTaxRate() == deliveryTaxDTO.getTaxRate()) {// SAME
							is_same = 1;
//							System.err.println("both same");
							double taxableamount = 0.00;
							double amount = 0.00;
							double cgst = 0.00;
							double sgst = 0.00;
							double igst = 0.00;
							double taxamount = 0.0;
							String hsn = "";
							String newhsn = "";
							if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {// CGST -SGST
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
								if (itemPdfDTO.getDiscountType().equals("amount")) {
									taxableamount = round(
											(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(),
											2);
								} else {
									taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
											- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount())
													/ 100),
											2);
								}
								amount = taxableamount + deliveryTaxDTO.getTaxableAmount();

								deliveryTaxDTO.setCgstAmount(cgst);
								deliveryTaxDTO.setSgstAmount(sgst);
								deliveryTaxDTO.setTaxAmount(taxamount);
								deliveryTaxDTO.setTaxableAmount(amount);

								break;
							} else {
//								System.err.println("IGST 2");
								taxamount = deliveryTaxDTO.getTaxAmount() + (itemPdfDTO.getTaxAmount());
								igst = deliveryTaxDTO.getIgstAmount() + itemPdfDTO.getTaxAmount();
								if (itemPdfDTO.getDiscountType().equals("amount")) {
									taxableamount = round(
											(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(),
											2);
								} else {
									taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
											- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount())
													/ 100),
											2);
								}
								amount = taxableamount + deliveryTaxDTO.getTaxableAmount();

								deliveryTaxDTO.setTaxAmount(taxamount);
								deliveryTaxDTO.setIgstAmount(igst);
								deliveryTaxDTO.setTaxableAmount(amount);
								break;
							}
						}
					}

					if (is_same == 0) {
						if (placeOfSupplyCode.equals(session.getAttribute("stateCode").toString())) {// CGST -SGST
//							System.err.println("CGST -SGST 3");
							double taxableamount = 0;
							if (itemPdfDTO.getDiscountType().equals("amount")) {
								taxableamount = round(
										(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(), 2);
							} else {
								taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
										- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount())
												/ 100),
										2);
							}

							DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
							deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
							deliveryTaxDTO2.setTaxableAmount(taxableamount);
							deliveryTaxDTO2.setCgstAmount(itemPdfDTO.getTaxAmount() / 2);
							deliveryTaxDTO2.setCgstRate(itemPdfDTO.getTaxRate() / 2);
							deliveryTaxDTO2.setSgstAmount(itemPdfDTO.getTaxAmount() / 2);
							deliveryTaxDTO2.setSgstRate(itemPdfDTO.getTaxRate() / 2);
							deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
							deliveryTaxDTOs.add(deliveryTaxDTO2);

						} else {// IGST
//							System.err.println("IGST 3");
							double taxableamount = 0;
							if (itemPdfDTO.getDiscountType().equals("amount")) {
								taxableamount = round(
										(itemPdfDTO.getPrice() * itemPdfDTO.getQty()) - itemPdfDTO.getDiscount(), 2);
							} else {
								taxableamount = round((itemPdfDTO.getPrice() * itemPdfDTO.getQty())
										- ((itemPdfDTO.getPrice() * itemPdfDTO.getQty() * itemPdfDTO.getDiscount())
												/ 100),
										2);
							}

							DeliveryTaxDTO deliveryTaxDTO2 = new DeliveryTaxDTO();
							deliveryTaxDTO2.setTaxRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setTaxAmount(round(itemPdfDTO.getTaxAmount(), 2));
							deliveryTaxDTO2.setTaxableAmount(taxableamount);
							deliveryTaxDTO2.setIgstAmount(itemPdfDTO.getTaxAmount());
							deliveryTaxDTO2.setIgstRate(itemPdfDTO.getTaxRate());
							deliveryTaxDTO2.setHsnCode(itemPdfDTO.getHsnCode());
							deliveryTaxDTOs.add(deliveryTaxDTO2);
						}
					}
				}
			}

			List<TaxDTO> taxDTOs = new ArrayList<TaxDTO>();

			for (int i = 0; i < deliveryTaxDTOs.size(); i++) {
				DeliveryTaxDTO deliveryTaxDTO5 = deliveryTaxDTOs.get(i);
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
					taxDTOs.add(taxDTO);
				} else {
					for (int j = 0; j < taxDTOs.size(); j++) {
						TaxDTO taxDTO = taxDTOs.get(j);
						if (deliveryTaxDTO5.getTaxRate() == taxDTO.getTaxRate()) {
							same_rate = 1;
							double cgstAmount = 0.00;
							double sgstAmount = 0.00;
							double igstAmount = 0.00;
							cgstAmount = taxDTO.getCgstAmount() + deliveryTaxDTO5.getCgstAmount();
							sgstAmount = taxDTO.getSgstAmount() + deliveryTaxDTO5.getSgstAmount();
							igstAmount = taxDTO.getIgstAmount() + deliveryTaxDTO5.getIgstAmount();
							taxDTO.setCgstAmount(cgstAmount);
							taxDTO.setSgstAmount(sgstAmount);
							taxDTO.setIgstAmount(igstAmount);

							break;
						}
					}
					if (same_rate == 0) {
						TaxDTO taxDTO = new TaxDTO();
						taxDTO.setCgstAmount(deliveryTaxDTO5.getCgstAmount());
						taxDTO.setCgstRate(deliveryTaxDTO5.getCgstRate());
						taxDTO.setSgstAmount(deliveryTaxDTO5.getSgstAmount());
						taxDTO.setSgstRate(deliveryTaxDTO5.getSgstRate());
						taxDTO.setIgstAmount(deliveryTaxDTO5.getIgstAmount());
						taxDTO.setIgstRate(deliveryTaxDTO5.getIgstRate());
						taxDTO.setTaxRate(deliveryTaxDTO5.getTaxRate());
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
			double totaldiscount = 0.00;
			double taxable = 0.00;
			double dis = 0.0, discount = 0.0;
			for (int j = 0; j < purchaseVo.getPurchaseItemVos().size(); j++) {
				dis = 0.0;
				discount = 0.0;
				PurchaseItemVo purchaseItemVo1 = purchaseVo.getPurchaseItemVos().get(j);
				taxable = purchaseItemVo1.getPrice() * purchaseItemVo1.getQty();
				if (purchaseItemVo1.getDiscountType() != null
						&& StringUtils.isNotBlank(purchaseItemVo1.getDiscountType())
						&& purchaseItemVo1.getDiscountType().equals("amount")) {
					discount = purchaseItemVo1.getDiscount();
					totaldiscount = totaldiscount + discount;
					dis = dis + discount;
					taxable = taxable - discount;
   				purchaseItemVo1.setDiscountAmount(discount);
   				purchaseItemVo1.setDiscountPer((discount*100)/taxable);

				} else {
					discount = ((taxable * purchaseItemVo1.getDiscount()) / 100);
					totaldiscount = totaldiscount + discount;
					dis = dis + discount;
					taxable = taxable - discount;
   				purchaseItemVo1.setDiscountAmount(discount);
   				purchaseItemVo1.setDiscountPer(purchaseItemVo1.getDiscount());

				}

				if (purchaseItemVo1.getDiscountType2() != null
						&& StringUtils.isNotBlank(purchaseItemVo1.getDiscountType2())
						&& purchaseItemVo1.getDiscountType2().equals("amount")) {
					discount = purchaseItemVo1.getDiscount2();
					totaldiscount = totaldiscount + discount;
					dis = dis + discount;
					taxable = taxable - discount;
   				purchaseItemVo1.setDiscountAmount2(discount);
   				purchaseItemVo1.setDiscountPer2((discount*100)/taxable);

				} else {
					discount = ((taxable * purchaseItemVo1.getDiscount2()) / 100);
					totaldiscount = totaldiscount + discount;
					dis = dis + discount;
					taxable = taxable - discount;
   				purchaseItemVo1.setDiscountAmount2(discount);
   				purchaseItemVo1.setDiscountPer2(purchaseItemVo1.getDiscount2());

				}

//				System.err.println("taxable" + taxable);

				subTotal = subTotal + taxable;
   	   	purchaseItemVo1.setTotaldiscount(dis);
   	 purchaseItemVo1.setTaxable(taxable);
			}

			for (int j = 0; j < purchaseVo.getPurchaseAdditionalChargeVos().size(); j++) {
				PurchaseAdditionalCharge accountVo = purchaseVo.getPurchaseAdditionalChargeVos().get(j);
				subTotal += round(accountVo.getAmount(), 2);
			}

			for (int i = 0; i < deliveryTaxDTOs.size(); i++) {
				totaltaxableAmount = totaltaxableAmount + deliveryTaxDTOs.get(i).getTaxableAmount();
				totalcgst = totalcgst + deliveryTaxDTOs.get(i).getCgstAmount();
				totalsgst = totalsgst + deliveryTaxDTOs.get(i).getSgstAmount();
				totaligst = totaligst + deliveryTaxDTOs.get(i).getIgstAmount();
				totaltaxAmount = round(totaltaxAmount + deliveryTaxDTOs.get(i).getTaxAmount(), 2);
			}

			Total = subTotal + totaltaxAmount;
			netAmount = Total + purchaseVo.getRoundoff();
			if (!purchaseVo.getTermsAndConditionIds().equals("")) {
				List<Long> termandconditionIds = Arrays.asList(purchaseVo.getTermsAndConditionIds().split("\\s*,\\s*"))
						.stream().map(Long::parseLong).collect(Collectors.toList());

				view.addObject("TermsAndCondition",
						purchaseTermsAndConditionService.getTermAndConditionList(termandconditionIds));
			}

			view.addObject("totalsgst", round(totalsgst, 2));
			view.addObject("totalcgst", round(totalcgst, 2));
			view.addObject("totaligst", round(totaligst, 2));
			view.addObject("totaltaxableAmount", round(totaltaxableAmount, 2));
			view.addObject("totaltax", round(totaltax, 2));
			view.addObject("totaltaxAmount", round(totaltaxAmount, 2));
			view.addObject("deliveryTaxDTOs", deliveryTaxDTOs);
			view.addObject("taxDTOs", taxDTOs);
			view.addObject("subTotal", subTotal);
			view.addObject("totaldiscount", totaldiscount);
			view.addObject("Total", Total);
			view.addObject("netAmount", netAmount);

			view.addObject("companyVo", companyVo);
			view.addObject("contactVo", contactVo);
			view.addObject("purchaseVo", purchaseVo);
			view.addObject("GST", placeOfSupplyCode.equals(session.getAttribute("stateCode").toString()));
			view.addObject("wordtotal", NumberToWord.getNumberToWord(purchaseVo.getTotal(),
					session.getAttribute("currencyName").toString()));
			view.addObject("preparedBy",
					profileService.getName(Long.parseLong(session.getAttribute("userId").toString())));
			view.addObject("FILE_UPLOAD_SERVER", FILE_UPLOAD_SERVER);
            view.addObject("printDateFormat", dateFormatMasterService.getDateFormatMasterByBranchId(Long.parseLong(session.getAttribute("branchId").toString())));
			view.addObject("signatureLogo",
					userRepository.getSignatureLogoSignedSrc(
							Long.parseLong(session.getAttribute("branchId").toString()),
							Constant.FILE_UPLOAD_SERVER_AZURE, FILE_UPLOAD_SERVER));
			view.setViewName("purchase/" + setting.getReportVo().getReport());
		}

		return view;
	}
    @GetMapping("{id}/ackno/pdf") // Purchase PDF
    public void acknoledgePDF(@PathVariable(value = "type") String type, @PathVariable long id, HttpSession session,
                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ACK_PDF;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_ACK_PDF;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_ACK_PDF;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_ACK_PDF;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_ACK_PDF;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        if (MenuPermission.havePermission(session, type, Constant.PDF_EXCEL_PRINT) == 1) {
            // Purc =salesService.findBySalesIdAndBranchId(id,
            // Long.parseLong(session.getAttribute("branchId").toString()));
            PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndCompanyId(id,
                    Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()));
			if(purchaseVo!=null) {
                HashMap jasperParameter = new HashMap();
	            jasperParameter.put("purchase_id", id);
	            jasperParameter.put("realPath", session.getAttribute("realPath").toString());
	            jasperParameter.put("currency_code", session.getAttribute("currencyCode").toString());
	            jasperParameter.put("user_front_id", Long.parseLong(session.getAttribute("branchId").toString()));
	            jasperParameter.put("amount_in_word", NumberToWord.getNumberToWord(purchaseVo.getTotal(),session.getAttribute("currencyName").toString()));
	            jasperParameter.put("contact_id", purchaseVo.getContactVo().getContactId());
	            jasperParameter.put("contact_type", purchaseVo.getContactVo().getType());
	            jasperParameter.put("logoserver", FILE_UPLOAD_SERVER);
	            jasperParameter.put("display_title", "ACKNOWLEDGEMENT");
	            int decimalNumber = Integer.parseInt(session.getAttribute("decimalPoint").toString());

	            //System.out.println("Decimal Number iss" + decimalNumber);
	            String decimalFormate = numberUtil.getFormateOnDecimal(decimalNumber);

	            //System.out.println("Decimal Formate is" + decimalFormate);
	            jasperParameter.put("decimalFormate", decimalFormate);

	            jasperParameter.put("path",JASPER_REPORT_PATH + File.separator);
	            try {

	                //System.err.println("I AM HERE  MAN ");
	                jasperExporter.jasperExporterPDF(jasperParameter,JASPER_REPORT_PATH + "/purchase/acknowledgement.jrxml",
	                        "acknowledgement-1", response);

	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	                //System.err.println(e.toString());
	            }
            }else {
            	response.sendRedirect("/404");
            }


        } else {
            response.sendRedirect("/accessdenied");
        }


    }

    @PostMapping("/getunpaiddebitnote/{id}")
    @ResponseBody
   	public List<Map<String, String>> getunpaiddebitnote(@PathVariable(value = "id") long contactId, @PathVariable(value = "type") String type,
   			HttpSession session, HttpServletRequest request,HttpSession session1) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_UNPAID_DEBIT_NOTE;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_UNPAID_DEBIT_NOTE;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_UNPAID_DEBIT_NOTE;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_UNPAID_DEBIT_NOTE;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_UNPAID_DEBIT_NOTE;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
    	long companyId = Long.parseLong(session1.getAttribute("companyId").toString());
    	long branchId = Long.parseLong(session1.getAttribute("branchId").toString());
    	List<String> statusList = new ArrayList<>();
    	statusList.add("open");
    	statusList.add("due");
    	List<Map<String, String>>  list = null;

    	try {
    		list = purchaseService.getUnpaidDebitNotDataOfSupplier(type, statusList, contactId, companyId, branchId);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return list;
    }

	@PostMapping("/get/list")
	@ResponseBody
	public List<PurchaseListDTO> getPurchaseByContactIdAndType(@PathVariable(value = "type") String type,
			HttpSession session,
			@RequestParam(value = "contactId", required = false, defaultValue = "0") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_DETAIL_LIST;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CONTACT_DETAIL_LIST;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CONTACT_DETAIL_LIST;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CONTACT_DETAIL_LIST;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CONTACT_DETAIL_LIST;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		long branchId = Long.parseLong(session.getAttribute("branchId").toString());
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()));
			Date startDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
			calendar.setTime(dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()));
			Date endDate = dateFormat.parse(dateFormat.format(calendar.getTime()).toString());
			List<PurchaseListDTO> purchaseVos = purchaseService.findByTypeAndContactAndBranchAndDateAndIsDeleted(type,
					contactId, branchId, startDate, endDate, 0);
			return purchaseVos;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	@PostMapping("/history")
	@ResponseBody
	public List<Map<String, String>> getPurchaseHistoryByProductVarientIdAndType(@PathVariable(value = "type") String type,
			HttpSession session,
			@RequestParam(required = false, defaultValue = "0") long productVarientId) {
        String rateLimitType = switch (type) {
            case Constant.PURCHASE_ORDER -> RateLimitConstant.PURCHASE_ORDER_HISTORY;
            case Constant.PURCHASE_MATERIALINWARD -> RateLimitConstant.PURCHASE_MATERIALINWARD_HISTORY;
            case Constant.PURCHASE_BILL -> RateLimitConstant.PURCHASE_BILL_HISTORY;
            case Constant.PURCHASE_DEBIT_NOTE -> RateLimitConstant.PURCHASE_DEBITNOTE_HISTORY;
            default -> RateLimitConstant.PURCHASE_ORDER_HISTORY;
        };
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		try {
			return purchaseService.getPurchaseHistoryByTypeAndProductVarientId(Long.parseLong(session.getAttribute(Constant.COMPANYID).toString()),type,productVarientId);
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	@PostMapping("/cancel")
	@ResponseBody
	public String cancelOrders(@PathVariable(value = "type") String type,@RequestParam String ids, HttpSession session, HttpServletRequest request) throws IOException {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CANCEL_ORDER;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CANCEL_ORDER;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CANCEL_ORDER;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		List<Long> l = Arrays.asList(ids.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
	    int response = purchaseService.countByPurchaseIdsAndBranchIdAndIsDeleted(l,
	            Long.parseLong(session.getAttribute("branchId").toString()), 0);
	    if (response == 0) {
	        return "redirect:/404";
	    } else {
	        purchaseService.updatePurchaseStatusByPurchaseIds(l, "cancel");
	        return "redirect:/purchase/" + type;
	    }
	}
	@RequestMapping("/update/transaction/list/{company}")
	@ResponseBody
	public String updateTransaction(HttpSession session,@PathVariable String type,@PathVariable long company) throws IOException, ParseException {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_UPDATE_TRANSACTION, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		 List<PurchaseVo> purchaseList= purchaseService.getAllpurchaseByDateBetweenAndCompanyId(dateFormat.parse(session.getAttribute("firstDateFinancialYear").toString()),
                 dateFormat.parse(session.getAttribute("lastDateFinancialYear").toString()),company);
		 if(purchaseList!=null && purchaseList.size()>0) {
			 for (PurchaseVo p : purchaseList) {
				 if (p.getType().equals(Constant.PURCHASE_BILL) || p.getType().equals(Constant.PURCHASE_MATERIALINWARD)) {
        	            purchaseService.insertPurchaseTransactionOnly(p, session.getAttribute("financialYear").toString());
			        } else if (p.getType().equals(Constant.PURCHASE_DEBIT_NOTE)) {
			        	purchaseService.insertPurchaseDebitNoteOnly(p, session.getAttribute("financialYear").toString(),session);
			        }
			}
		 }


		return "success";
	}
	@RequestMapping("/checkstatus")
    @ResponseBody
    public String checkstatus(@RequestParam(defaultValue = "0") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_CHECK_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		if (paymentService.countForPaymentStatus(id,"purchase")==0) {
			return (purchaseService.countByPurchaseIdAndStatusAndIsDeleted(id,Constant.PAID,0)>0?2:0)+"";
		}
		else {
			return "1";
		}

    }


	@PostMapping("/checkunpaid/json")
    @ResponseBody
    public String unpaidpurchasebillcheck( @RequestParam("purchaseIds") String purchaseIds, @RequestParam("expenseids") String expenseids, @RequestParam("paymentIds") String paymentIds, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_CHECK_UNPAID_BILL_JSON, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		log.info("purchaseIds--->>>"+purchaseIds+"    expenseids--->>>"+expenseids+"    paymentIds--->>>"+paymentIds);
		JSONObject jsonObject = new JSONObject();
		List<Map<String, String>> unpaidpurchasebills =new ArrayList<Map<String,String>>();

		if(StringUtils.isNotBlank(purchaseIds)) {

			List<Long> list = Stream.of(purchaseIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
			List<String> payments = Arrays.asList(paymentIds.split(","));
			List<Long> paymentList = new ArrayList();
			if (payments.toString().equals("[]")) {

				paymentList.add(0L);

			} else {

				for (String string : payments) {
					paymentList.add(Long.valueOf(string));
				}
			}
			unpaidpurchasebills.addAll(purchaseService.checkunpaidpurchasebills(list,Long.parseLong(session.getAttribute("branchId").toString()),paymentList));
    		//log.info("unpaidpurchasebills"+unpaidpurchasebills.size());

		}

		if(StringUtils.isNotBlank(expenseids)) {

			List<Long> list = Stream.of(expenseids.split(",")).map(Long::parseLong).collect(Collectors.toList());
			unpaidpurchasebills.addAll(expenseService.checkunpaidexpensebills(list,Long.parseLong(session.getAttribute("branchId").toString())));
    		// log.info("unpaidexpensebills"+unpaidpurchasebills.size());
        }
		if(unpaidpurchasebills.size()>0) {
    		jsonObject.put("status", true);
    		jsonObject.put("data",unpaidpurchasebills);
    	}else {
    		jsonObject.put("status", false);
    	}

		return jsonObject.toString();

    }
	public static String generateRandomString( int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

	@RequestMapping("/{id}/{vouchertype}/checkPaymentStatus")
    @ResponseBody
    public Map<String, Object> checkStatusCount(@PathVariable("id") long id,@PathVariable("vouchertype") String voucherType, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_CHECK_PAYMENT_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        HashMap<String, Object> result = new HashMap<>();
		int b = paymentService.countForPaymentStatus(id,voucherType);
        if (b == 0) {
            long merchantTypeId = Long.parseLong(session.getAttribute(Constant.MERCHANTTYPEID).toString());
            String clusterId = session.getAttribute(Constant.CLUSTERID).toString();
            if(MerchantTypeController.MerchantClusterEnum.isMerchantTypeValid(merchantTypeId, clusterId) || merchantTypeId == Constant.MERCHANTTYPE_AJIO_WHOLESALER){
                long isSapBill =rILPurchaseBillInfoRepository.countById(id);
                if(isSapBill == 1) {
                    result.put("status", 1);
                    result.put("msg", "Supplier bills generated through automation cannot be edited.");
                }
            } else{
                result.put("status",0);
                result.put("msg","true");
            }
        } else {
            result.put("status",1);
            result.put("msg","The payment is generated against this bill, please delete that to delete this bill.");
        }
        return result;
    }

	@RequestMapping("/{id}/getDebitNoteStatus")
    @ResponseBody
    public String getDebitNoteStatus(@PathVariable("id") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DEBIT_NOTE_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
		int b = purchaseService.getDebitNoteStatus(id);
        if (b == 0) {
            return "0";
        } else {
            return "1";
        }
    }

	@RequestMapping("/{id}/getDebitNoteAvailabiltyStatus")
	@ResponseBody
	public String getDebitNoteAvailabiltyStatus(@PathVariable("id") long id, HttpSession session) {
        if (!rateLimitService.allowRequest(RateLimitConstant.PURCHASE_DEBIT_NOTE_AVAILABILITY_STATUS, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        // Fetch the sales VO to check e-way bill and IRN

        EwayDTO purchaseEway = purchaseRepository.findEwayBillNoById(id);
        IrnDTO purchaseIrn = purchaseRepository.findIrnById(id);

        if (purchaseEway.getEwayBillNo() != 0) {
            return "3";
        }

        if (StringUtils.isNotBlank(purchaseIrn.getIrnNo())) {
            return "2";
        }

		int b = purchaseService.getDebitNoteAvailabiltyStatus(id);
		if (b == 0) {
			return "0";
		} else {
			return "1";
		}
	}
	@PostMapping("/{id}/data")
    @ResponseBody
    public List<Map<String, String>> PurchaseListJSONForDebitnote(HttpSession session, @PathVariable(value = "type") String type,
                                             @PathVariable(value = "id") long contactId) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_JSON_LIST;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_JSON_LIST;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_JSON_LIST;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_JSON_LIST;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_JSON_LIST;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        List<Map<String,String>> purchaseVos = new ArrayList<>();
        try {
            if(Constant.PURCHASE_BILL.equalsIgnoreCase(type)) {
                purchaseVos = purchaseService.getPurchaseBillByTypeAndContactAndBranchIdAndIsDeletedForBill(type,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId, 0);
            }else{
                purchaseVos = purchaseService.getPurchaseBillByTypeAndContactAndBranchIdAndIsDeleted(type,
                        Long.parseLong(session.getAttribute("branchId").toString()), contactId, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return purchaseVos;
    }

    @RequestMapping("/checktdstcsamount")
    @ResponseBody
    public JSONObject checkTotalTdsTcsAmount(@PathVariable String type, @RequestParam Map<String, String> allRequestParams, HttpSession session) {
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CHECK_TOTAL_TDS_AMOUNT;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_CHECK_TOTAL_TDS_AMOUNT;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_CHECK_TOTAL_TDS_AMOUNT;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_CHECK_TOTAL_TDS_AMOUNT;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_CHECK_TOTAL_TDS_AMOUNT;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        long salesId = StringUtils.isNotBlank(allRequestParams.get("purchaseId")) ? Long.parseLong(allRequestParams.get("purchaseId")) : 0;
        long parentSalesId = StringUtils.isNotBlank(allRequestParams.get("parentPurchaseId")) ? Long.parseLong(allRequestParams.get("parentPurchaseId")) : 0;
        int isEdit = StringUtils.isNotBlank(allRequestParams.get("isEdit")) ? Integer.parseInt(allRequestParams.get("isEdit")) : 0;
        JSONObject jsonObject = new JSONObject();
        double tcsAmount = 0.0, tdsAmount = 0.0;
        if (parentSalesId != 0 && StringUtils.isNotBlank(type)) {
            try {
                if (type.equals(Constant.PURCHASE_DEBIT_NOTE)) {
                    Map<String, Double> tdsTcsAmount = new HashMap<>(2);
                    tdsTcsAmount = purchaseService.getTotalTdsTcsAmountUsedInDebitNoteByIdAndBranchIdAndType(parentSalesId, salesId,
                            Long.parseLong(session.getAttribute("branchId").toString()), type, isEdit);
                    tdsAmount = tdsTcsAmount.get("tds_amount");
                    tcsAmount = tdsTcsAmount.get("tcs_amount");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jsonObject.put("tcsAmount", tcsAmount);
        jsonObject.put("tdsAmount", tdsAmount);
        return jsonObject;
    }

    @PostMapping("/getDataForDebiteNote/{purchaseId}")
    @ResponseBody
    public Map<String,Object> getPurchaseItemByPurchaseId(HttpSession session, @PathVariable(value = "type") String type,
                                                   @PathVariable(value = "purchaseId") long purchaseId,
                                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size) throws CloneNotSupportedException {
        System.out.println(page+"==="+size);
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DETAIL;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DETAIL;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DETAIL;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }
        Map<String, Object> responseMap = new HashMap<>();
        int offset = page * size;

        List<Map<String, Object>> list = purchaseService.findPurchaseItemforDebiteNote(purchaseId,size,offset, Long.parseLong(session.getAttribute("branchId").toString()),
                session.getAttribute("financialYear").toString(),Long.parseLong(session.getAttribute("companyId").toString()));

        for (int i = 0; i < list.size(); i++) {
            // Convert immutable map to mutable HashMap
            Map<String, Object> mutableMap = new HashMap<>(list.get(i));

            // Fetch the stock master data
            List<StockMasterDTOForDebitNote> masterVos = stockMasterRepository.findByProductBatch(
                    Long.parseLong(mutableMap.get("productvariantid").toString()),
                    Long.parseLong(session.getAttribute("branchId").toString()),
                    session.getAttribute("financialYear").toString()
            );
            // Add StockMasterDTOForDebitNote data to the mutable map
            mutableMap.put("stockMasterDTOForDebitNote", masterVos);
            // Replace the original map with the modified one
            list.set(i, mutableMap);
        }
        responseMap.put("productVariant", list);
        // Handle additional charges
        List<PurchaseAdditionalCharge> purchaseAdditionalChargeVo = purchaseService.findByPurchaseAdditionalCharges(purchaseId);
        if (!purchaseAdditionalChargeVo.isEmpty()) {
            purchaseAdditionalChargeVo.forEach(obj -> obj.setPurchaseVo(null)); // Avoid circular reference or unnecessary data
            responseMap.put("additionalCharge", purchaseAdditionalChargeVo);
        }
        return responseMap;
    }

    private void updateRILPurchaseBillInfo(RILPurchaseBillInfo rilPurchaseBillInfo, Long newPurchaseId) {
        RILPurchaseBillInfo newRilPurchaseBillInfo = new RILPurchaseBillInfo();
        newRilPurchaseBillInfo.setPurchaseId(newPurchaseId);
        newRilPurchaseBillInfo.setDc(rilPurchaseBillInfo.getDc());
        newRilPurchaseBillInfo.setRilPurchaseBillData(rilPurchaseBillInfo.getRilPurchaseBillData());
        newRilPurchaseBillInfo.setLrNo(rilPurchaseBillInfo.getLrNo());
        newRilPurchaseBillInfo.setLrDt(rilPurchaseBillInfo.getLrDt());
        newRilPurchaseBillInfo.setCourierPartner(rilPurchaseBillInfo.getCourierPartner());
        newRilPurchaseBillInfo.setCustomerSAPCode(rilPurchaseBillInfo.getCustomerSAPCode());
        newRilPurchaseBillInfo.setDeliveryNo(rilPurchaseBillInfo.getDeliveryNo());
        newRilPurchaseBillInfo.setShipToCode(rilPurchaseBillInfo.getShipToCode());
        rILPurchaseBillInfoRepository.save(newRilPurchaseBillInfo);
    }

    @PostMapping("/normal/bypurchaseid/{purchaseId}")
    @ResponseBody
    public Map<String, Object> getSalesItemBySaleId(
            HttpSession session,
            @PathVariable(value = "type") String type,
            @PathVariable(value = "purchaseId") long purchaseId,
            @RequestParam(value = "batchrequired", required = false, defaultValue = "0") String batchrequired,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) throws CloneNotSupportedException {

        // Check rate limit
        String rateLimitType;
        switch (type) {
            case Constant.PURCHASE_ORDER:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
                break;
            case Constant.PURCHASE_MATERIALINWARD:
                rateLimitType = RateLimitConstant.PURCHASE_MATERIALINWARD_DETAIL;
                break;
            case Constant.PURCHASE_BILL:
                rateLimitType = RateLimitConstant.PURCHASE_BILL_DETAIL;
                break;
            case Constant.PURCHASE_DEBIT_NOTE:
                rateLimitType = RateLimitConstant.PURCHASE_DEBITNOTE_DETAIL;
                break;
            default:
                rateLimitType = RateLimitConstant.PURCHASE_ORDER_DETAIL;
        }
        if (!rateLimitService.allowRequest(rateLimitType, session)) {
            throw new CustomRateLimitExceedException(RateLimitConstant.RATE_LIMIT_EXCEEDS_MESSAGE);
        }

        long branchId = Long.parseLong(session.getAttribute("branchId").toString());
        PurchaseVo purchaseVo = purchaseService.findByPurchaseIdAndBranchId(purchaseId, branchId);
        List<ProductVarientsVo> productVarientsVos = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(offset, limit); // `offset` is page number, `limit` is page size
        List<PurchaseItemVo> purchaseItemVos = null;
        Map<String, Object> responseMap = new HashMap<>();

        if (purchaseVo != null) {
            purchaseItemVos = purchaseService.findbyPurchaseVoPurchaseId(purchaseId,pageRequest);
            System.out.println(purchaseItemVos.size()+"======");
            if (purchaseItemVos != null) {
                final int[] count = {0};
                double productSubTotal = 0.0;
                // Apply offset and limit
                if(Constant.AMOUNT.equalsIgnoreCase(purchaseVo.getFlatDiscountType())) {
                        Map<String, Object> purchaseFlatDiscountData = purchaseService.calculateFlatDiscountPercentageFromAmount(purchaseVo.getPurchaseId());
                        if(!purchaseFlatDiscountData.isEmpty()){
                            productSubTotal = Double.parseDouble(purchaseFlatDiscountData.get("productSubTotal").toString());
                        }
                }
                for (PurchaseItemVo purchaseItemVo:purchaseItemVos) {
                    ProductVarientsVo productVarientsVo = new ProductVarientsVo();
                    productVarientsVo.setSku("Count" + count[0]++);
                    productVarientsVo = (ProductVarientsVo) purchaseItemVo.getProductVarientsVo().clone();
                    productVarientsVo.getProductVo().setProductVarientsVos(null);

                    if (productVarientsVo == null) {
                        //return null;
                    } else {
                        String s = stockMasterRepository.findproductVariantQty(
                                Long.parseLong(session.getAttribute("companyId").toString()),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                productVarientsVo.getProductVarientId(), session.getAttribute("financialYear").toString());
                        productVarientsVo.setAvailableQty(s);
                    }
                    productVarientsVo.setRetailerPrice(purchaseItemVo.getPrice());
                    productVarientsVo.setWholesalerPrice(purchaseItemVo.getPrice());
                    productVarientsVo.setQty(new BigDecimal(String.valueOf(purchaseItemVo.getQty())).doubleValue());
                    productVarientsVo.setReceiveqty(new BigDecimal(String.valueOf(purchaseItemVo.getReceiveQty())).doubleValue());
                    productVarientsVo.setMrp(purchaseItemVo.getMrp());
                    productVarientsVo.setDiscount(purchaseItemVo.getDiscount());
                    productVarientsVo.setDiscountType(purchaseItemVo.getDiscountType());
                    productVarientsVo.setDiscount2(purchaseItemVo.getDiscount2());
                    productVarientsVo.setDiscountType2(purchaseItemVo.getDiscountType2());

                    productVarientsVo.setSellingPrice(purchaseItemVo.getSellingPrice());

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    productVarientsVo.setPurchaseDate(dateFormat.format(purchaseItemVo.getPurchaseVo().getPurchaseDate()));
                    productVarientsVo.setBatchCreationDate(purchaseItemVo.getBatchCreationDate());
                    productVarientsVo.setBatchExpiryDate(purchaseItemVo.getBatchExpiryDate());
                    productVarientsVo.setPurchasePrice(purchaseItemVo.getPrice());
                    productVarientsVo.setExpdays(purchaseItemVo.getExpdays());
                    productVarientsVo.setExpiryManage(purchaseItemVo.getExpiryManage());

                    if (batchrequired.equals("1")) {
                        List<StockMasterDTOForDebitNote> masterVos = stockMasterRepository.findByProductBatch(
                                productVarientsVo.getProductVarientId(),
                                Long.parseLong(session.getAttribute("branchId").toString()),
                                session.getAttribute("financialYear").toString());
                        productVarientsVo.setStockMasterDTOForDebitNote(masterVos);
                    }

                    String itemCode = productVarientsVo.getItemCode();
                    if (StringUtils.isNotEmpty(purchaseItemVo.getItemCode())) {
                        itemCode = purchaseItemVo.getItemCode();
                    }
                    productVarientsVo.setPurchaseItemCode(itemCode);
                    productVarientsVo.setGstTaxType(purchaseItemVo.getPurchaseVo().getGstTaxType());
                    productVarientsVo.setFlatDiscount(purchaseVo.getFlatDiscount());
                    productVarientsVo.setFlatDiscountType(purchaseVo.getFlatDiscountType());
                    if(Constant.AMOUNT.equalsIgnoreCase(purchaseVo.getFlatDiscountType())) {
                        productVarientsVo.setProductSubTotal(productSubTotal);
                    }
                    productVarientsVo.getProductVo().setProductAttributeVos(null);
                    if(Objects.nonNull(purchaseItemVo.getTaxVo())) {
                        productVarientsVo.setPurchaseTaxDto(new TaxFormDTO(purchaseItemVo.getTaxVo().getTaxId(), purchaseItemVo.getTaxVo().getTaxName(), purchaseItemVo.getTaxRate()));
                    }
                    if (StringUtils.isNotBlank(productVarientsVo.getProductVo().getHsnCode())) {
                        productVarientsVo.getProductVo().setHsnType(hsnTaxMasterService.getHsnTypeByHsnCode(productVarientsVo.getProductVo().getHsnCode()));
                    }
                    productVarientsVos.add(productVarientsVo);
                }

                if (!productVarientsVos.isEmpty()) {
                    responseMap.put("productVariant", productVarientsVos);
                }
                if (!purchaseVo.getPurchaseAdditionalChargeVos().isEmpty()) {
                    purchaseVo.getPurchaseAdditionalChargeVos().forEach(obj -> obj.setPurchaseVo(null));
                    responseMap.put("additionaCharge", purchaseVo.getPurchaseAdditionalChargeVos());
                }
                if (MapUtils.isNotEmpty(purchaseVo.getTdsJson())) {
                    responseMap.put("tdsApplicable", purchaseVo.getTdsJson().getOrDefault(Constant.TDS_APPLICABLE, 0));
                }
                if (MapUtils.isNotEmpty(purchaseVo.getTcsJson())) {
                    responseMap.put("tcsApplicable", purchaseVo.getTcsJson().getOrDefault(Constant.TCS_APPLICABLE, 0));
                }
            }
        }

        return responseMap;
    }


}
