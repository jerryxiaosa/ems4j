package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TreeUtilTest {

    @Test
    @DisplayName("buildTree 默认以 parentId 为 null 为根节点")
    void testBuildTree_DefaultRootByNullParent() {
        List<TestNode> nodes = List.of(
                new TestNode(1, null, "root"),
                new TestNode(2, 1, "child-1"),
                new TestNode(3, 1, "child-2"),
                new TestNode(4, 2, "grandchild-1")
        );

        List<TestNode> roots = TreeUtil.buildTree(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren
        );

        assertEquals(1, roots.size());
        TestNode root = roots.get(0);
        assertEquals("root", root.getName());
        assertEquals(2, root.getChildren().size());

        TestNode child1 = root.getChildren().get(0);
        assertEquals("child-1", child1.getName());
        assertEquals(1, child1.getChildren().size());
        assertEquals("grandchild-1", child1.getChildren().get(0).getName());

        TestNode child2 = root.getChildren().get(1);
        assertEquals("child-2", child2.getName());
        assertTrue(child2.getChildren().isEmpty());
    }

    @Test
    @DisplayName("buildTree 默认根节点同时包含 parentId 为 0")
    void testBuildTree_DefaultRootIncludesZero() {
        List<TestNode> nodes = List.of(
                new TestNode(1, 0, "root-zero"),
                new TestNode(2, 1, "child"),
                new TestNode(3, null, "root-null")
        );

        List<TestNode> roots = TreeUtil.buildTree(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren
        );

        assertEquals(2, roots.size());
        assertTrue(roots.stream().anyMatch(node -> "root-zero".equals(node.getName())));
        assertTrue(roots.stream().anyMatch(node -> "root-null".equals(node.getName())));

        TestNode zeroRoot = roots.stream().filter(node -> "root-zero".equals(node.getName())).findFirst().orElseThrow();
        assertEquals(1, zeroRoot.getChildren().size());
        assertEquals("child", zeroRoot.getChildren().get(0).getName());
    }

    @Test
    @DisplayName("buildTree 自定义根节点父级标识")
    void testBuildTree_WithCustomRootParentId() {
        List<TestNode> nodes = List.of(
                new TestNode(1, 0, "root-0"),
                new TestNode(2, 0, "root-1"),
                new TestNode(3, 1, "child-1"),
                new TestNode(4, 3, "grandchild-1")
        );

        List<TestNode> roots = TreeUtil.buildTreeByParentId(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren,
                0
        );

        assertEquals(2, roots.size());
        assertEquals("root-0", roots.get(0).getName());
        assertEquals(1, roots.get(0).getChildren().size());

        TestNode child = roots.get(0).getChildren().get(0);
        assertEquals("child-1", child.getName());
        assertEquals(1, child.getChildren().size());

        assertEquals("root-1", roots.get(1).getName());
        assertTrue(roots.get(1).getChildren().isEmpty());
    }

    @Test
    @DisplayName("buildTree 使用根节点判断函数 parentId=0")
    void testBuildTree_WithRootPredicateZero() {
        List<TestNode> nodes = List.of(
                new TestNode(100, 0, "A"),
                new TestNode(101, 100, "A-1"),
                new TestNode(102, 100, "A-2"),
                new TestNode(200, 0, "B")
        );

       Predicate<Integer> rootPredicate = parentId -> parentId != null && parentId == 0;

        List<TestNode> roots = TreeUtil.buildTree(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren,
                rootPredicate
        );

        assertEquals(2, roots.size());
        TestNode nodeA = roots.get(0);
        assertEquals("A", nodeA.getName());
        assertEquals(2, nodeA.getChildren().size());
        assertEquals("A-1", nodeA.getChildren().get(0).getName());
        assertEquals("A-2", nodeA.getChildren().get(1).getName());

        TestNode nodeB = roots.get(1);
        assertEquals("B", nodeB.getName());
        assertTrue(nodeB.getChildren().isEmpty());
    }

    @Test
    @DisplayName("buildTree 父节点缺失时自动视为根节点")
    void testBuildTree_MissingParentBecomeRoot() {
        List<TestNode> nodes = List.of(
                new TestNode(10, 100, "orphan-root"),
                new TestNode(11, 10, "child-of-orphan"),
                new TestNode(12, null, "independent-root")
        );

        List<TestNode> roots = TreeUtil.buildTree(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren
        );

        assertEquals(1, roots.size());
        TestNode orphanRoot = roots.get(0);
        assertEquals("independent-root", orphanRoot.getName());
    }

    @Test
    @DisplayName("buildTreeByParentId 根节点不匹配的节点被丢弃")
    void testBuildTreeByParentId_ExcludeInvalidParent() {
        List<TestNode> nodes = List.of(
                new TestNode(1, 0, "valid-root"),
                new TestNode(2, 1, "valid-child"),
                new TestNode(3, 1, "valid-child-2"),
                new TestNode(4, 999, "invalid-root"),
                new TestNode(5, 4, "invalid-child")
        );

        List<TestNode> roots = TreeUtil.buildTreeByParentId(
                nodes,
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren,
                0
        );

        assertEquals(1, roots.size());
        TestNode root = roots.get(0);
        assertEquals("valid-root", root.getName());
        assertEquals(2, root.getChildren().size());

        // 不匹配的节点不会出现在树中
        assertTrue(root.getChildren().stream().noneMatch(child -> "invalid-root".equals(child.getName())));
    }

    @Test
    @DisplayName("buildTree 空列表返回空集合")
    void testBuildTree_WithEmptyList() {
        List<TestNode> roots = TreeUtil.buildTree(
                List.of(),
                TestNode::getId,
                TestNode::getParentId,
                TestNode::setChildren
        );
        assertNotNull(roots);
        assertTrue(roots.isEmpty());
    }

    private static class TestNode {
        private final Integer id;
        private final Integer parentId;
        private final String name;
        private List<TestNode> children = new ArrayList<>();

        TestNode(Integer id, Integer parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        Integer getId() {
            return id;
        }

        Integer getParentId() {
            return parentId;
        }

        String getName() {
            return name;
        }

        List<TestNode> getChildren() {
            return children;
        }

        void setChildren(List<TestNode> children) {
            this.children = new ArrayList<>(children);
        }
    }
}
