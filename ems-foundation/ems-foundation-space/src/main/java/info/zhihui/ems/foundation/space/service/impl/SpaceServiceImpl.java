package info.zhihui.ems.foundation.space.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.mapstruct.SpaceMapper;
import info.zhihui.ems.foundation.space.qo.SpaceQueryQo;
import info.zhihui.ems.foundation.space.repository.SpaceRepository;
import info.zhihui.ems.foundation.space.service.SpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 空间服务实现类
 *
 * @author jerryxiaosa
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class SpaceServiceImpl implements SpaceService {

    private static final int ROOT_PID = 0;

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    // ==================== 公共接口方法 ====================

    /**
     * 获取空间详情
     *
     * @param id 空间ID
     * @return 空间详情
     */
    @Override
    public SpaceBo getDetail(@NotNull Integer id) {
        SpaceEntity entity = getSpaceEntityById(id);
        SpaceBo spaceBo = spaceMapper.toBo(entity);

        // 填充祖先信息
        fillAncestorInfo(spaceBo);

        return spaceBo;
    }

    /**
     * 查询空间列表
     *
     * @param queryDto 查询条件
     * @return 空间列表
     */
    @Override
    public List<SpaceBo> findSpaceList(@NotNull SpaceQueryDto queryDto) {
        SpaceQueryQo qo = buildQueryQo(queryDto);
        List<SpaceEntity> entities = spaceRepository.selectByQo(qo);

        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }

        List<SpaceBo> result = spaceMapper.toBoList(entities);

        // 批量填充祖先信息
        result.forEach(this::fillAncestorInfo);

        return result;
    }

    /**
     * 新增空间
     *
     * @param createDto 创建参数
     * @return 创建的空间id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addSpace(@Valid @NotNull SpaceCreateDto createDto) {
        log.info("开始创建空间，参数：{}", createDto);

        // 校验和获取父空间
        SpaceEntity parentSpace = validateAndGetParentSpace(createDto.getPid());

        // 校验同级名称唯一性
        validateSiblingNameUniqueness(createDto.getPid(), createDto.getName(), null);

        // 创建空间实体
        SpaceEntity entity = createSpaceEntity(createDto, parentSpace);

        log.info("空间创建成功，ID：{}", entity.getId());
        return entity.getId();
    }

    /**
     * 更新空间
     *
     * @param updateDto 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpace(@Valid @NotNull SpaceUpdateDto updateDto) {
        log.info("开始更新空间，参数：{}", updateDto);

        // 获取现有空间实体
        SpaceEntity existingEntity = getSpaceEntityById(updateDto.getId());

        // 校验父空间变更
        SpaceEntity newParentSpace = validateParentSpaceChange(existingEntity, updateDto.getPid());

        // 校验同级名称唯一性
        validateSiblingNameUniqueness(updateDto.getPid(), updateDto.getName(), existingEntity);

        // 执行更新操作
        performSpaceUpdate(existingEntity, updateDto, newParentSpace, updateDto.getPid());

        log.info("空间更新成功，ID：{}", updateDto.getId());
    }

    /**
     * 删除空间
     *
     * @param id 空间ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(@NotNull Integer id) {
        log.info("开始删除空间，ID：{}", id);

        SpaceEntity entity = spaceRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("空间不存在");
        }

        // 校验是否可以删除
        validateSpaceDeletable(id);

        // 执行删除
        int deleteResult = spaceRepository.deleteById(id);
        if (deleteResult <= 0) {
            throw new BusinessRuntimeException("删除空间失败");
        }

        log.info("空间删除成功，ID：{}", id);
    }

    // ==================== 参数校验方法 ====================

    /**
     * 校验同级空间名称唯一性
     */
    private void validateSiblingNameUniqueness(Integer pid, String name, SpaceEntity existingSpace) {
        int nameCount = spaceRepository.countByParentAndName(pid, name);
        if (nameCount > 0) {
            // 如果是更新操作且名称属于当前空间，则允许
            if (existingSpace != null && Objects.equals(existingSpace.getName(), name)
                    && Objects.equals(existingSpace.getPid(), pid)) {
                return;
            }
            throw new BusinessRuntimeException("同级空间名称不能重复");
        }
    }

    /**
     * 校验空间是否可以删除
     */
    private void validateSpaceDeletable(Integer id) {
        int childrenCount = spaceRepository.countChildrenByPid(id);
        if (childrenCount > 0) {
            throw new BusinessRuntimeException("存在子空间，无法删除");
        }
    }

    // ==================== 业务规则校验方法 ====================

    /**
     * 校验并获取父空间
     */
    private SpaceEntity validateAndGetParentSpace(Integer pid) {
        if (Objects.equals(pid, ROOT_PID)) {
            return null;
        }

        SpaceEntity parentSpace = spaceRepository.selectById(pid);
        if (parentSpace == null) {
            throw new BusinessRuntimeException("父空间不存在");
        }

        return parentSpace;
    }

    /**
     * 校验父空间变更的合法性
     */
    private SpaceEntity validateParentSpaceChange(SpaceEntity existingEntity, Integer targetPid) {
        // 如果父空间没有变更，直接返回null
        if (Objects.equals(existingEntity.getPid(), targetPid)) {
            return null;
        }

        // 校验新父空间存在性
        SpaceEntity newParentSpace = validateAndGetParentSpace(targetPid);

        // 校验循环引用
        if (isCircularReference(existingEntity.getId(), newParentSpace)) {
            throw new BusinessRuntimeException("不能将空间移动到其子空间下");
        }

        return newParentSpace;
    }

    /**
     * 检查是否形成循环引用
     */
    private boolean isCircularReference(Integer spaceId, SpaceEntity targetParent) {
        if (targetParent == null) {
            return false;
        }

        if (Objects.equals(targetParent.getId(), spaceId)) {
            return true;
        }

        String parentFullPath = targetParent.getFullPath();
        if (!StringUtils.hasText(parentFullPath)) {
            return false;
        }

        String currentIdStr = String.valueOf(spaceId);
        return Arrays.stream(parentFullPath.split(","))
                .map(String::trim)
                .anyMatch(segment -> Objects.equals(segment, currentIdStr));
    }

    // ==================== 数据操作方法 ====================

    /**
     * 根据ID获取空间实体
     */
    private SpaceEntity getSpaceEntityById(Integer id) {
        SpaceEntity entity = spaceRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("空间不存在");
        }
        return entity;
    }

    /**
     * 创建空间实体
     */
    private SpaceEntity createSpaceEntity(SpaceCreateDto createDto, SpaceEntity parentSpace) {
        // 转换为实体对象
        SpaceEntity entity = spaceMapper.toEntity(createDto);

        // 保存空间
        int insertResult = spaceRepository.insert(entity);
        if (insertResult != 1) {
            throw new BusinessRuntimeException("创建空间失败");
        }

        // 计算并更新fullPath
        String fullPath = calculateFullPath(parentSpace, entity.getId());
        entity.setFullPath(fullPath);

        // 设置所属主区域
        Integer ownAreaId = resolveOwnAreaId(fullPath, entity.getId(), createDto.getType());
        entity.setOwnAreaId(ownAreaId);

        spaceRepository.updateById(entity);

        return entity;
    }

    /**
     * 执行空间更新操作
     */
    private void performSpaceUpdate(SpaceEntity existingEntity, SpaceUpdateDto updateDto,
                                    SpaceEntity newParentSpace, Integer targetPid) {
        // 更新实体对象
        SpaceEntity updateEntity = spaceMapper.toEntity(updateDto);
        updateEntity.setPid(targetPid);

        // 判断是否需要更新fullPath
        boolean needUpdateFullPath = !Objects.equals(existingEntity.getPid(), targetPid);
        String newFullPath = null;

        if (needUpdateFullPath) {
            newFullPath = calculateFullPath(newParentSpace, existingEntity.getId());
            updateEntity.setFullPath(newFullPath);
        } else {
            updateEntity.setFullPath(existingEntity.getFullPath());
        }

        Integer ownAreaId = resolveOwnAreaId(updateEntity.getFullPath(), updateDto.getId(), updateDto.getType());
        updateEntity.setOwnAreaId(ownAreaId);

        // 执行更新
        int updateResult = spaceRepository.updateById(updateEntity);
        if (updateResult <= 0) {
            throw new BusinessRuntimeException("更新空间失败");
        }

        // 更新子空间的fullPath
        if (needUpdateFullPath) {
            updateDescendantFullPath(existingEntity.getFullPath(), newFullPath);
        }
    }

    /**
     * 构建查询对象
     */
    private SpaceQueryQo buildQueryQo(SpaceQueryDto queryDto) {
        SpaceQueryQo qo = new SpaceQueryQo();

        if (!CollectionUtils.isEmpty(queryDto.getIds())) {
            qo.setIds(queryDto.getIds());
        }

        if (queryDto.getPid() != null) {
            qo.setPid(queryDto.getPid());
        }

        if (StringUtils.hasText(queryDto.getName())) {
            qo.setNameLike(queryDto.getName());
        }

        if (queryDto.getType() != null) {
            qo.setTypes(queryDto.getType().stream().map(SpaceTypeEnum::getCode).toList());
        }

        return qo;
    }

    // ==================== fullPath相关工具方法 ====================

    /**
     * 计算完整路径
     */
    private String calculateFullPath(SpaceEntity parentSpace, Integer currentId) {
        if (parentSpace == null || Objects.equals(parentSpace.getId(), ROOT_PID)) {
            return String.valueOf(currentId);
        }

        String parentFullPath = parentSpace.getFullPath();
        if (!StringUtils.hasText(parentFullPath)) {
            throw new BusinessRuntimeException("父级空间路径数据异常，请修复");
        }
        return parentFullPath + "," + currentId;
    }

    /**
     * 更新子空间的fullPath
     */
    private void updateDescendantFullPath(String oldFullPath, String newFullPath) {
        if (!StringUtils.hasText(oldFullPath) || !StringUtils.hasText(newFullPath)) {
            return;
        }

        // 查询所有子空间
        List<SpaceEntity> descendants = findDescendantSpaces(oldFullPath);
        if (CollectionUtils.isEmpty(descendants)) {
            return;
        }

        // 批量更新fullPath
        List<SpaceEntity> updateList = buildDescendantUpdateList(descendants, oldFullPath, newFullPath);
        if (!updateList.isEmpty()) {
            spaceRepository.updateFullPathBatch(updateList);
        }
    }

    /**
     * 查找所有子空间
     */
    private List<SpaceEntity> findDescendantSpaces(String parentFullPath) {
        SpaceQueryQo qo = new SpaceQueryQo();
        qo.setFullPathPrefix(parentFullPath);
        return spaceRepository.selectByQo(qo);
    }

    /**
     * 构建子空间更新列表
     */
    private List<SpaceEntity> buildDescendantUpdateList(List<SpaceEntity> descendants,
                                                        String oldFullPath, String newFullPath) {
        List<SpaceEntity> updateList = new ArrayList<>(descendants.size());

        for (SpaceEntity descendant : descendants) {
            String originalPath = descendant.getFullPath();
            if (!StringUtils.hasText(originalPath)) {
                continue;
            }

            String rebuildFullPath = rebuildDescendantFullPath(originalPath, oldFullPath, newFullPath);
            descendant.setFullPath(rebuildFullPath);
            updateList.add(descendant);
        }

        return updateList;
    }

    /**
     * 重建子空间的fullPath
     */
    private String rebuildDescendantFullPath(String originalPath, String oldFullPath, String newFullPath) {
        String suffix = originalPath.substring(oldFullPath.length());
        if (suffix.startsWith(",")) {
            suffix = suffix.substring(1);
        }
        return StringUtils.hasText(suffix) ? newFullPath + "," + suffix : newFullPath;
    }

    // ==================== 祖先信息处理方法 ====================

    /**
     * 填充祖先信息
     */
    private void fillAncestorInfo(SpaceBo spaceBo) {
        if (spaceBo == null || !StringUtils.hasText(spaceBo.getFullPath())) {
            return;
        }

        // 解析祖先ID列表
        List<Integer> ancestorIds = parseAncestorIds(spaceBo.getFullPath(), spaceBo.getId());
        if (ancestorIds.isEmpty()) {
            setEmptyAncestorInfo(spaceBo);
            return;
        }

        // 查询祖先实体并填充信息
        fillAncestorDetails(spaceBo, ancestorIds);
    }

    /**
     * 计算所属主区域ID
     */
    private Integer resolveOwnAreaId(String fullPath, Integer currentId, SpaceTypeEnum typeEnum) {
        if (SpaceTypeEnum.MAIN.equals(typeEnum)) {
            return currentId;
        }

        List<Integer> ancestorIds = parseAncestorIds(fullPath, currentId);
        if (CollectionUtils.isEmpty(ancestorIds)) {
            throw new BusinessRuntimeException("未找到关联的主区域，请检查空间层级配置");
        }

        SpaceQueryQo qo = new SpaceQueryQo().setIds(ancestorIds);
        List<SpaceEntity> ancestors = spaceRepository.selectByQo(qo);
        if (CollectionUtils.isEmpty(ancestors)) {
            throw new BusinessRuntimeException("未找到关联的主区域，请检查空间层级配置");
        }

        for (int i = ancestorIds.size() - 1; i >= 0; i--) {
            Integer ancestorId = ancestorIds.get(i);
            SpaceEntity ancestor = findEntityById(ancestors, ancestorId);
            if (ancestor != null && Objects.equals(ancestor.getType(), SpaceTypeEnum.MAIN.getCode())) {
                return ancestorId;
            }
        }

        throw new BusinessRuntimeException("未找到关联的主区域，请检查空间层级配置");
    }

    /**
     * 解析祖先ID列表
     */
    private List<Integer> parseAncestorIds(String fullPath, Integer spaceId) {
        String[] pathSegments = fullPath.split(",");
        if (pathSegments.length <= 1) {
            return new ArrayList<>();
        }

        List<Integer> ancestorIds = new ArrayList<>();
        // 排除最后一个元素（自身ID）
        for (int i = 0; i < pathSegments.length - 1; i++) {
            String segment = pathSegments[i];
            if (!StringUtils.hasText(segment)) {
                continue;
            }

            try {
                ancestorIds.add(Integer.parseInt(segment.trim()));
            } catch (NumberFormatException e) {
                log.warn("空间full_path存在非法片段:{}, spaceId:{}", segment, spaceId);
            }
        }

        return ancestorIds;
    }

    /**
     * 设置空的祖先信息
     */
    private void setEmptyAncestorInfo(SpaceBo spaceBo) {
        spaceBo.setParentsIds(new ArrayList<>());
        spaceBo.setParentsNames(new ArrayList<>());
    }

    /**
     * 填充祖先详细信息
     */
    private void fillAncestorDetails(SpaceBo spaceBo, List<Integer> ancestorIds) {
        // 查询祖先实体
        SpaceQueryQo qo = new SpaceQueryQo().setIds(ancestorIds);
        List<SpaceEntity> ancestorEntities = spaceRepository.selectByQo(qo);

        if (CollectionUtils.isEmpty(ancestorEntities)) {
            setEmptyAncestorInfo(spaceBo);
            return;
        }

        // 按顺序构建祖先信息
        List<Integer> orderedIds = new ArrayList<>(ancestorIds.size());
        List<String> orderedNames = new ArrayList<>(ancestorIds.size());

        for (Integer ancestorId : ancestorIds) {
            SpaceEntity entity = findEntityById(ancestorEntities, ancestorId);
            if (entity != null) {
                orderedIds.add(entity.getId());
                orderedNames.add(entity.getName());
            }
        }

        spaceBo.setParentsIds(orderedIds);
        spaceBo.setParentsNames(orderedNames);
    }

    /**
     * 在实体列表中查找指定ID的实体
     */
    private SpaceEntity findEntityById(List<SpaceEntity> entities, Integer id) {
        return entities.stream()
                .filter(item -> Objects.equals(item.getId(), id))
                .findFirst()
                .orElse(null);
    }
}
