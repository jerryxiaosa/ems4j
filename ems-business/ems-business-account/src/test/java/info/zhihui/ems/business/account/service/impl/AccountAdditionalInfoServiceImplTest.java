package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.repository.OwnerSpaceRelRepository;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAdditionalInfoServiceImplTest {

    @Mock
    private OwnerSpaceRelRepository ownerSpaceRelRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private AccountAdditionalInfoServiceImpl accountAdditionalInfoService;

    @Test
    void testFindCandidateMeterList_NoRentedSpace_ReturnEmpty() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of());

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        assertThat(result).isEmpty();
        verify(organizationService).getDetail(1001);
    }

    @Test
    void testFindCandidateMeterList_FilterBySpaceNameAndUnopened() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(
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
                        .setDeviceNo("M1001")
                        .setIsPrepay(true)
                        .setIsOnline(false)
                        .setAccountId(null),
                new ElectricMeterBo()
                        .setId(1002)
                        .setSpaceId(102)
                        .setMeterName("已开户表")
                        .setDeviceNo("M1002")
                        .setIsPrepay(true)
                        .setIsOnline(true)
                        .setAccountId(9)
        ));

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
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
    void testFindCandidateMeterList_PersonalOwner_ShouldSkipOrganizationValidation() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.PERSONAL.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of());

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1001)
        );

        assertThat(result).isEmpty();
        verifyNoInteractions(organizationService);
    }

    @Test
    void testFindCandidateMeterList_WhenSpaceServiceReturnsEmpty_ShouldReturnEmpty() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101)
        ));
        when(spaceService.findSpaceList(any(SpaceQueryDto.class))).thenReturn(List.of());

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        assertThat(result).isEmpty();
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testFindCandidateMeterList_WhenMatchedSpaceIdIsEmpty_ShouldReturnEmpty() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101)
        ));
        when(spaceService.findSpaceList(any(SpaceQueryDto.class))).thenReturn(Arrays.asList(
                null,
                new SpaceBo().setName("无ID空间")
        ));

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        assertThat(result).isEmpty();
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testFindCandidateMeterList_WhenSpaceInfoMissing_ShouldReturnCandidateWithNullSpaceInfo() {
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101)
        ));
        when(spaceService.findSpaceList(any(SpaceQueryDto.class))).thenReturn(List.of(
                new SpaceBo().setId(101).setName("一层101").setParentsNames(List.of("A栋", "一层"))
        ));
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(Arrays.asList(
                null,
                new ElectricMeterBo()
                        .setId(2001)
                        .setSpaceId(999)
                        .setMeterName("候选无空间")
                        .setDeviceNo("M2001")
                        .setIsPrepay(true)
                        .setIsOnline(false)
                        .setAccountId(null),
                new ElectricMeterBo()
                        .setId(2002)
                        .setSpaceId(101)
                        .setMeterName("已开户表")
                        .setDeviceNo("M2002")
                        .setIsPrepay(true)
                        .setIsOnline(true)
                        .setAccountId(1)
        ));

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto().setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2001);
        assertThat(result.get(0).getSpaceName()).isNull();
        assertThat(result.get(0).getSpaceParentNames()).isNull();
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

        List<OwnerSpaceRelEntity> relList = Arrays.asList(
                enterpriseRelList.get(0),
                enterpriseRelList.get(1),
                enterpriseRelList.get(2),
                personalRelList.get(0),
                personalRelList.get(1),
                personalRelList.get(2)
        );
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.containsAll(List.of(
                        OwnerTypeEnum.ENTERPRISE.getCode(), OwnerTypeEnum.PERSONAL.getCode()
                ))),
                argThat(ownerIds -> ownerIds != null && ownerIds.containsAll(List.of(1001, 1002, 1003)))
        )).thenReturn(relList);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(meterBoList);

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 3);
        assertThat(result).containsEntry(2, 3);
        assertThat(result).containsEntry(3, 0);
        verify(electricMeterInfoService).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenOwnerInfoInvalid_ShouldReturnZeroMap() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(null).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(null),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(null)
        );

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 0).containsEntry(2, 0);
        verifyNoInteractions(ownerSpaceRelRepository);
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenContainsNullItem_ShouldIgnoreNull() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                null
        );
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(new OwnerSpaceRelEntity()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(1001)
                .setSpaceId(101)));
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(List.of(
                new ElectricMeterBo().setSpaceId(101)
        ));

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 1);
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenAccountHasNoOwnerMapping_ShouldReturnZero() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(null).setOwnerId(1002)
        );
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.contains(OwnerTypeEnum.ENTERPRISE.getCode())),
                argThat(ownerIds -> ownerIds != null && ownerIds.contains(1001))
        )).thenReturn(List.of(new OwnerSpaceRelEntity()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(1001)
                .setSpaceId(101)));
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(List.of(
                new ElectricMeterBo().setSpaceId(101),
                new ElectricMeterBo().setSpaceId(101)
        ));

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 2).containsEntry(2, 0);
    }

    @Test
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WhenNoSpaceRel_ShouldReturnZeroMap() {
        List<AccountOwnerInfoDto> inputAccountOwnerInfoDtoList = List.of(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1002)
        );
        when(ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                argThat(ownerTypes -> ownerTypes != null && ownerTypes.containsAll(List.of(
                        OwnerTypeEnum.ENTERPRISE.getCode(), OwnerTypeEnum.PERSONAL.getCode()
                ))),
                argThat(ownerIds -> ownerIds != null && ownerIds.containsAll(List.of(1001, 1002)))
        )).thenReturn(List.of());

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(inputAccountOwnerInfoDtoList);

        assertThat(result).containsEntry(1, 0).containsEntry(2, 0);
        verify(ownerSpaceRelRepository).findListByOwnerTypesAndOwnerIds(any(), any());
        verify(electricMeterInfoService, never()).findList(any(ElectricMeterQueryDto.class));
    }

    @Test
    void testFindElectricBalanceAmountMap_Normal() {
        when(balanceService.findListByAccountIds(List.of(1, 2, 3))).thenReturn(List.of(
                new BalanceBo().setAccountId(1).setBalanceType(BalanceTypeEnum.ACCOUNT).setBalance(new BigDecimal("100.00")),
                new BalanceBo().setAccountId(1).setBalanceType(BalanceTypeEnum.ELECTRIC_METER).setBalance(new BigDecimal("10.00")),
                new BalanceBo().setAccountId(1).setBalanceType(BalanceTypeEnum.ELECTRIC_METER).setBalance(new BigDecimal("5.50")),
                new BalanceBo().setAccountId(2).setBalanceType(BalanceTypeEnum.ACCOUNT).setBalance(new BigDecimal("88.00"))
        ));

        Map<Integer, BigDecimal> result = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto().setAccountId(1).setElectricAccountType(ElectricAccountTypeEnum.QUANTITY),
                new AccountElectricBalanceAggregateItemDto().setAccountId(2).setElectricAccountType(ElectricAccountTypeEnum.MONTHLY),
                new AccountElectricBalanceAggregateItemDto().setAccountId(3).setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
        ));

        assertThat(result.get(1)).isEqualByComparingTo("15.50");
        assertThat(result.get(2)).isEqualByComparingTo("88.00");
        assertThat(result.get(3)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testFindElectricBalanceAmountMap_MergedType_ShouldUseAccountBalance() {
        when(balanceService.findListByAccountIds(List.of(2))).thenReturn(List.of(
                new BalanceBo().setAccountId(2).setBalanceType(BalanceTypeEnum.ACCOUNT).setBalance(new BigDecimal("66.00")),
                new BalanceBo().setAccountId(2).setBalanceType(BalanceTypeEnum.ELECTRIC_METER).setBalance(new BigDecimal("11.00"))
        ));

        Map<Integer, BigDecimal> result = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto().setAccountId(2).setElectricAccountType(ElectricAccountTypeEnum.MERGED)
        ));

        assertThat(result.get(2)).isEqualByComparingTo("66.00");
    }

    @Test
    void testFindElectricBalanceAmountMap_DuplicateAccountWithFirstNullType_ShouldKeepFirst() {
        when(balanceService.findListByAccountIds(List.of(1))).thenReturn(List.of(
                new BalanceBo().setAccountId(1).setBalanceType(BalanceTypeEnum.ACCOUNT).setBalance(new BigDecimal("99.00"))
        ));

        Map<Integer, BigDecimal> result = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto().setAccountId(1).setElectricAccountType(null),
                new AccountElectricBalanceAggregateItemDto().setAccountId(1).setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
        ));

        assertThat(result.get(1)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
