package info.zhihui.ems.common.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 列表转树形结构工具类。
 * <p>
 * 通过自定义的 ID/父级 ID 解析函数以及子节点写入逻辑，将平铺的列表构建为树形结构。
 */
public final class TreeUtil {

    private TreeUtil() {
    }

    /**
     * 使用父级 ID 为空（null）作为根节点判断条件，将平铺列表构造成树。
     *
     * @param nodes            数据列表
     * @param idGetter         ID 获取函数
     * @param parentIdGetter   父级 ID 获取函数
     * @param childrenConsumer 子节点写入逻辑
     * @param <T>              节点类型
     * @param <K>              主键类型
     * @return 根节点集合
     */
    public static <T, K> List<T> buildTree(List<T> nodes,
                                           Function<T, K> idGetter,
                                           Function<T, K> parentIdGetter,
                                           BiConsumer<T, List<T>> childrenConsumer) {
        Predicate<K> rootPredicate = parentId -> parentId == null || parentId.equals(0);
        return buildTree(nodes, idGetter, parentIdGetter, childrenConsumer, rootPredicate);
    }

    /**
     * 使用指定根节点父级标识构建树。
     *
     * @param nodes            数据列表
     * @param idGetter         ID 获取函数
     * @param parentIdGetter   父级 ID 获取函数
     * @param childrenConsumer 子节点写入逻辑
     * @param rootParentId     根节点父级标识
     * @param <T>              节点类型
     * @param <K>              主键类型
     * @return 根节点集合
     */
    public static <T, K> List<T> buildTreeByParentId(List<T> nodes,
                                                     Function<T, K> idGetter,
                                                     Function<T, K> parentIdGetter,
                                                     BiConsumer<T, List<T>> childrenConsumer,
                                                     K rootParentId) {
        Predicate<K> rootPredicate = parentId -> Objects.equals(parentId, rootParentId);
        return buildTree(nodes, idGetter, parentIdGetter, childrenConsumer, rootPredicate);
    }

    /**
     * 使用自定义根节点判断条件构建树。
     *
     * @param nodes            数据列表
     * @param idGetter         ID 获取函数
     * @param parentIdGetter   父级 ID 获取函数
     * @param childrenConsumer 子节点写入逻辑
     * @param rootPredicate    根节点判断条件（传入父级 ID）
     * @param <T>              节点类型
     * @param <K>              主键类型
     * @return 根节点集合
     */
    public static <T, K> List<T> buildTree(List<T> nodes,
                                           Function<T, K> idGetter,
                                           Function<T, K> parentIdGetter,
                                           BiConsumer<T, List<T>> childrenConsumer,
                                           Predicate<K> rootPredicate) {
        Objects.requireNonNull(idGetter, "idGetter must not be null");
        Objects.requireNonNull(parentIdGetter, "parentIdGetter must not be null");
        Objects.requireNonNull(childrenConsumer, "childrenConsumer must not be null");
        Objects.requireNonNull(rootPredicate, "rootPredicate must not be null");

        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> orderedNodes = new ArrayList<>(nodes);
        Set<K> availableIds = new LinkedHashSet<>(orderedNodes.size());
        for (T node : orderedNodes) {
            K id = idGetter.apply(node);
            Objects.requireNonNull(id, "node id must not be null");
            availableIds.add(id);
        }

        Map<K, List<T>> childrenBucket = new LinkedHashMap<>();
        List<T> rootNodes = new ArrayList<>();

        for (T node : orderedNodes) {
            K parentId = parentIdGetter.apply(node);
            if (rootPredicate.test(parentId)) {
                rootNodes.add(node);
                continue;
            }

            if (!availableIds.contains(parentId)) {
                continue;
            }

            childrenBucket.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(node);
        }

        for (T node : orderedNodes) {
            K id = idGetter.apply(node);
            List<T> children = childrenBucket.get(id);
            if (children == null) {
                childrenConsumer.accept(node, Collections.emptyList());
            } else {
                childrenConsumer.accept(node, List.copyOf(children));
            }
        }

        return List.copyOf(rootNodes);
    }
}
