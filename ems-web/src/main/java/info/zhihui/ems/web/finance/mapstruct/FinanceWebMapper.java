package info.zhihui.ems.web.finance.mapstruct;

import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.finance.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinanceWebMapper {

    AccountConsumeQueryDto toAccountConsumeQueryDto(AccountConsumeQueryVo vo);

    PowerConsumeQueryDto toPowerConsumeQueryDto(PowerConsumeQueryVo vo);

    MeterCorrectionRecordQueryDto toCorrectionRecordQueryDto(CorrectionRecordQueryVo vo);

    @Mapping(target = "correctionType", ignore = true)
    CorrectMeterAmountDto toCorrectAmountDto(CorrectionMeterAmountVo vo);

    AccountConsumeRecordVo toAccountConsumeRecordVo(AccountConsumeRecordDto dto);

    PowerConsumeRecordVo toPowerConsumeRecordVo(PowerConsumeRecordDto dto);

    PowerConsumeDetailVo toPowerConsumeDetailVo(PowerConsumeDetailDto dto);

    CorrectionRecordVo toCorrectionRecordVo(MeterCorrectionRecordDto dto);

    default PageResult<AccountConsumeRecordVo> toAccountConsumeRecordVoPage(PageResult<AccountConsumeRecordDto> dtoPage) {
        if (dtoPage == null) {
            return emptyPage();
        }
        List<AccountConsumeRecordDto> list = dtoPage.getList();
        List<AccountConsumeRecordVo> voList = list == null ? Collections.emptyList() :
                list.stream().map(this::toAccountConsumeRecordVo).collect(Collectors.toList());
        return copyPage(dtoPage, voList);
    }

    default PageResult<PowerConsumeRecordVo> toPowerConsumeRecordVoPage(PageResult<PowerConsumeRecordDto> dtoPage) {
        if (dtoPage == null) {
            return emptyPage();
        }
        List<PowerConsumeRecordVo> voList = dtoPage.getList() == null ? Collections.emptyList() :
                dtoPage.getList().stream().map(this::toPowerConsumeRecordVo).collect(Collectors.toList());
        return copyPage(dtoPage, voList);
    }

    default PageResult<CorrectionRecordVo> toCorrectionRecordVoPage(PageResult<MeterCorrectionRecordDto> dtoPage) {
        if (dtoPage == null) {
            return emptyPage();
        }
        List<CorrectionRecordVo> voList = dtoPage.getList() == null ? Collections.emptyList() :
                dtoPage.getList().stream().map(this::toCorrectionRecordVo).collect(Collectors.toList());
        return copyPage(dtoPage, voList);
    }

    private <T> PageResult<T> copyPage(PageResult<?> source, List<T> targetList) {
        return new PageResult<T>()
                .setPageNum(source.getPageNum())
                .setPageSize(source.getPageSize())
                .setTotal(source.getTotal())
                .setList(targetList);
    }

    private <T> PageResult<T> emptyPage() {
        return new PageResult<T>()
                .setPageNum(1)
                .setPageSize(0)
                .setTotal(0L)
                .setList(Collections.emptyList());
    }
}
