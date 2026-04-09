package info.zhihui.ems.web.report;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.business.report.bo.ElectricBillReportDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportAccountDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportPageItemBo;
import info.zhihui.ems.business.report.dto.ElectricBillReportQueryDto;
import info.zhihui.ems.business.report.service.query.ElectricBillReportQueryService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.annotation.TranslateChild;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.report.biz.ElectricBillReportBiz;
import info.zhihui.ems.web.report.controller.ElectricBillReportController;
import info.zhihui.ems.web.report.vo.ElectricBillReportAccountDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportMeterDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportPageVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportQueryVo;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricBillReportControllerTest {

    @Test
    @DisplayName("列表接口应委托业务层并返回成功结果")
    void testFindPage_ShouldDelegateToBiz() {
        FakeElectricBillReportQueryService service = new FakeElectricBillReportQueryService();
        ElectricBillReportController controller = new ElectricBillReportController(new ElectricBillReportBiz(service));
        ElectricBillReportQueryVo queryVo = new ElectricBillReportQueryVo()
                .setAccountNameLike("企业A")
                .setStartDate(LocalDate.now().minusDays(1))
                .setEndDate(LocalDate.now().minusDays(1));

        RestResult<PageResult<ElectricBillReportPageVo>> result = controller.findPage(queryVo, 2, 20);

        assertTrue(result.getSuccess());
        assertNotNull(service.pageQueryBo);
        assertEquals("企业A", service.pageQueryBo.getAccountNameLike());
        assertEquals(queryVo.getStartDate(), service.pageQueryBo.getStartDate());
        assertEquals(2, service.pageParam.getPageNum());
        assertEquals(20, service.pageParam.getPageSize());
    }

    @Test
    @DisplayName("详情接口应委托业务层并返回成功结果")
    void testGetDetail_ShouldDelegateToBiz() {
        FakeElectricBillReportQueryService service = new FakeElectricBillReportQueryService();
        ElectricBillReportController controller = new ElectricBillReportController(new ElectricBillReportBiz(service));
        ElectricBillReportQueryVo queryVo = new ElectricBillReportQueryVo()
                .setStartDate(LocalDate.now().minusDays(2))
                .setEndDate(LocalDate.now().minusDays(1));

        RestResult<ElectricBillReportDetailVo> result = controller.getDetail(101, queryVo);

        assertTrue(result.getSuccess());
        assertEquals(101, service.detailAccountId);
        assertNotNull(service.detailQueryBo);
        assertEquals(queryVo.getEndDate(), service.detailQueryBo.getEndDate());
    }

    @Test
    @DisplayName("控制器路由与参数校验应符合约定")
    void testAnnotations_ShouldMatchExpectedContract() throws NoSuchMethodException, NoSuchFieldException {
        RequestMapping classRequestMapping = ElectricBillReportController.class.getAnnotation(RequestMapping.class);
        Method pageMethod = ElectricBillReportController.class.getMethod(
                "findPage", ElectricBillReportQueryVo.class, Integer.class, Integer.class
        );
        Method detailMethod = ElectricBillReportController.class.getMethod(
                "getDetail", Integer.class, ElectricBillReportQueryVo.class
        );
        Field startDateField = ElectricBillReportQueryVo.class.getDeclaredField("startDate");
        Field endDateField = ElectricBillReportQueryVo.class.getDeclaredField("endDate");

        assertNotNull(classRequestMapping);
        assertEquals(ApiPathConstant.V1 + "/report/electric-bill", classRequestMapping.value()[0]);
        assertEquals("/page", pageMethod.getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/{accountId}/detail", detailMethod.getAnnotation(GetMapping.class).value()[0]);
        assertEquals("reports:electric-bill:page", pageMethod.getAnnotation(SaCheckPermission.class).value()[0]);
        assertEquals("reports:electric-bill:detail", detailMethod.getAnnotation(SaCheckPermission.class).value()[0]);
        assertNotNull(startDateField.getAnnotation(NotNull.class));
        assertNotNull(endDateField.getAnnotation(NotNull.class));
    }

    @Test
    @DisplayName("报表VO应使用格式化注解和子节点递归注解")
    void testVoAnnotations_ShouldUseFormatTextAndTranslateChild() throws NoSuchFieldException {
        Field pagePowerTextField = ElectricBillReportPageVo.class.getDeclaredField("periodConsumePowerText");
        Field pageAccountTypeNameField = ElectricBillReportPageVo.class.getDeclaredField("electricAccountTypeName");
        Field accountBalanceTextField = ElectricBillReportAccountDetailVo.class.getDeclaredField("accountBalanceText");
        Field accountTypeNameField = ElectricBillReportAccountDetailVo.class.getDeclaredField("electricAccountTypeName");
        Field meterPriceTextField = ElectricBillReportMeterDetailVo.class.getDeclaredField("displayPriceHighText");
        Field detailAccountInfoField = ElectricBillReportDetailVo.class.getDeclaredField("accountInfo");
        Field detailMeterListField = ElectricBillReportDetailVo.class.getDeclaredField("meterList");

        FormatText pagePowerTextFormat = pagePowerTextField.getAnnotation(FormatText.class);
        EnumLabel pageAccountTypeEnumLabel = pageAccountTypeNameField.getAnnotation(EnumLabel.class);
        FormatText accountBalanceTextFormat = accountBalanceTextField.getAnnotation(FormatText.class);
        EnumLabel accountTypeEnumLabel = accountTypeNameField.getAnnotation(EnumLabel.class);
        FormatText meterPriceTextFormat = meterPriceTextField.getAnnotation(FormatText.class);

        assertNotNull(pagePowerTextFormat);
        assertEquals("periodConsumePower", pagePowerTextFormat.source());
        assertEquals("info.zhihui.ems.web.common.formatter.PowerScale2TextFormatter",
                pagePowerTextFormat.formatter().getName());

        assertNotNull(pageAccountTypeEnumLabel);
        assertEquals("electricAccountType", pageAccountTypeEnumLabel.source());

        assertNotNull(accountBalanceTextFormat);
        assertEquals("accountBalance", accountBalanceTextFormat.source());

        assertNotNull(accountTypeEnumLabel);
        assertEquals("electricAccountType", accountTypeEnumLabel.source());

        assertNotNull(meterPriceTextFormat);
        assertEquals("displayPriceHigh", meterPriceTextFormat.source());
        assertEquals("info.zhihui.ems.web.common.formatter.PriceScale4TextFormatter",
                meterPriceTextFormat.formatter().getName());

        assertNotNull(detailAccountInfoField.getAnnotation(TranslateChild.class));
        assertNotNull(detailMeterListField.getAnnotation(TranslateChild.class));
    }

    private static class FakeElectricBillReportQueryService implements ElectricBillReportQueryService {

        private ElectricBillReportQueryDto pageQueryBo;
        private PageParam pageParam;
        private Integer detailAccountId;
        private ElectricBillReportQueryDto detailQueryBo;

        @Override
        public PageResult<ElectricBillReportPageItemBo> findPage(ElectricBillReportQueryDto query, PageParam pageParam) {
            this.pageQueryBo = query;
            this.pageParam = pageParam;
            return new PageResult<ElectricBillReportPageItemBo>()
                    .setPageNum(pageParam.getPageNum())
                    .setPageSize(pageParam.getPageSize())
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }

        @Override
        public ElectricBillReportDetailBo getDetail(Integer accountId, ElectricBillReportQueryDto query) {
            this.detailAccountId = accountId;
            this.detailQueryBo = query;
            return new ElectricBillReportDetailBo()
                    .setAccountInfo(new ElectricBillReportAccountDetailBo().setAccountId(accountId))
                    .setMeterList(Collections.emptyList());
        }
    }
}
