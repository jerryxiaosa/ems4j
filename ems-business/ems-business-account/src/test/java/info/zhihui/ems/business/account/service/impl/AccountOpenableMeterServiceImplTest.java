package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.repository.OwnerSpaceRelRepository;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountOpenableMeterServiceImplTest {

    @Mock
    private OwnerSpaceRelRepository ownerSpaceRelRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @InjectMocks
    private AccountOpenableMeterServiceImpl accountOpenableMeterService;

    @Test
    void testFindCandidateMeterList_NoRentedSpace_ReturnEmpty() {
        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(OwnerTypeEnum.ENTERPRISE.getCode(), List.of(1001))).thenReturn(List.of());

        List<AccountCandidateMeterDto> result = accountOpenableMeterService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void testFindCandidateMeterList_FilterBySpaceNameAndUnopened() {
        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(OwnerTypeEnum.ENTERPRISE.getCode(), List.of(1001))).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(102),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(102)
        ));

        SpaceBo spaceBo = new SpaceBo()
                .setId(102)
                .setName("二层101")
                .setParentsNames(List.of("A栋", "二层"));
        when(spaceService.findSpaceList(any(SpaceQueryDto.class))).thenReturn(List.of(spaceBo));

        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(List.of(
                new ElectricMeterBo()
                        .setId(1001)
                        .setSpaceId(102)
                        .setMeterName("候选表1")
                        .setMeterNo("M1001")
                        .setIsPrepay(true)
                        .setIsOnline(false)
                        .setAccountId(null),
                new ElectricMeterBo()
                        .setId(1002)
                        .setSpaceId(102)
                        .setMeterName("已开户表")
                        .setMeterNo("M1002")
                        .setIsPrepay(true)
                        .setIsOnline(true)
                        .setAccountId(9)
        ));

        List<AccountCandidateMeterDto> result = accountOpenableMeterService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto()
                        .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                        .setOwnerId(1001)
                        .setSpaceNameLike("二层")
        );

        assertThat(result).hasSize(1);
        AccountCandidateMeterDto candidateMeterDto = result.get(0);
        assertThat(candidateMeterDto.getId()).isEqualTo(1001);
        assertThat(candidateMeterDto.getMeterName()).isEqualTo("候选表1");
        assertThat(candidateMeterDto.getIsOnline()).isFalse();
        assertThat(candidateMeterDto.getIsPrepay()).isTrue();
        assertThat(candidateMeterDto.getSpaceName()).isEqualTo("二层101");
        assertThat(candidateMeterDto.getSpaceParentNames()).containsExactly("A栋", "二层");

        ArgumentCaptor<SpaceQueryDto> spaceQueryCaptor = ArgumentCaptor.forClass(SpaceQueryDto.class);
        verify(spaceService).findSpaceList(spaceQueryCaptor.capture());
        assertThat(spaceQueryCaptor.getValue().getIds()).containsExactlyInAnyOrder(101, 102);
        assertThat(spaceQueryCaptor.getValue().getName()).isEqualTo("二层");

        ArgumentCaptor<ElectricMeterQueryDto> meterQueryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(meterQueryCaptor.capture());
        assertThat(meterQueryCaptor.getValue().getSpaceIds()).containsExactly(102);
        assertThat(meterQueryCaptor.getValue().getIsPrepay()).isTrue();
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_Success() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1002),
                new AccountOwnerInfoDto().setAccountId(3).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1003),
                new AccountOwnerInfoDto().setAccountId(null).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1004)
        );
        List<OwnerSpaceRelEntity> enterpriseRelList = List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(102),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(102)
        );
        List<OwnerSpaceRelEntity> personalRelList = List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.PERSONAL.getCode()).setOwnerId(1002).setSpaceId(201),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.PERSONAL.getCode()).setOwnerId(null).setSpaceId(301),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.PERSONAL.getCode()).setOwnerId(1003).setSpaceId(null)
        );
        List<ElectricMeterBo> meterBoList = List.of(
                new ElectricMeterBo().setSpaceId(101),
                new ElectricMeterBo().setSpaceId(101),
                new ElectricMeterBo().setSpaceId(102),
                new ElectricMeterBo().setSpaceId(201),
                new ElectricMeterBo().setSpaceId(201),
                new ElectricMeterBo().setSpaceId(201),
                new ElectricMeterBo().setSpaceId(null)
        );

        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.ENTERPRISE.getCode()), argThat(ownerIds ->
                ownerIds != null && ownerIds.equals(List.of(1001))
        ))).thenReturn(enterpriseRelList);
        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.PERSONAL.getCode()), argThat(ownerIds ->
                ownerIds != null && ownerIds.containsAll(List.of(1002, 1003))
        ))).thenReturn(personalRelList);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(meterBoList);

        Map<Integer, Integer> result = accountOpenableMeterService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 3);
        assertThat(result).containsEntry(2, 3);
        assertThat(result).containsEntry(3, 0);
        verify(electricMeterInfoService).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenNoValidAccountId_ShouldReturnEmptyMap() {
        Map<Integer, Integer> result = accountOpenableMeterService.countTotalOpenableMeterByAccountOwnerInfoList(Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(null).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                null
        ));

        assertThat(result).isEmpty();
        verifyNoInteractions(ownerSpaceRelRepository);
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenNoSpaceRel_ShouldReturnZeroMap() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = List.of(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1002)
        );
        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.ENTERPRISE.getCode()), eq(List.of(1001)))).thenReturn(List.of());
        when(ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.PERSONAL.getCode()), eq(List.of(1002)))).thenReturn(List.of());

        Map<Integer, Integer> result = accountOpenableMeterService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 0).containsEntry(2, 0);
        verify(ownerSpaceRelRepository).findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.ENTERPRISE.getCode()), eq(List.of(1001)));
        verify(ownerSpaceRelRepository).findListByOwnerTypeAndOwnerIds(eq(OwnerTypeEnum.PERSONAL.getCode()), eq(List.of(1002)));
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }
}
