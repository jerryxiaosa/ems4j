# 单元测试编写规范

## 1. 测试类命名和结构规范

### 命名规范
- 测试类命名：`{被测试类名}Test`
- 示例：`ElectricMeterServiceImplTest`

### 类结构
```java
@ExtendWith(MockitoExtension.class)
class ElectricMeterServiceImplTest {
    
    // Mock对象声明
    @Mock
    private ElectricMeterRepository repository;
    
    @Mock
    private ElectricMeterMapper mapper;
    
    // 被测试对象
    @InjectMocks
    private ElectricMeterServiceImpl electricMeterService;
    
    // 测试数据准备
    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }
    
    // 测试方法
}
```

## 2. Mock对象使用规范

### 【强制】Mock对象声明
- 使用`@Mock`注解声明依赖对象
- 使用`@InjectMocks`注解声明被测试对象
- 使用`@ExtendWith(MockitoExtension.class)`启用Mockito

### 【强制】Mock行为定义
```java
// 正例：使用具体参数
when(repository.selectById(1L)).thenReturn(mockEntity);
when(mapper.toVo(mockEntity)).thenReturn(mockVo);

// 反例：过度使用any()
when(repository.selectById(any())).thenReturn(mockEntity);
```

## 3. 测试方法命名规范

### 命名模式
- 格式：`test{方法名}_{场景}_{期望结果}`
- 示例：
  - `testGetDetail_Success()` - 成功场景
  - `testGetDetail_NotFound()` - 未找到场景
  - `testAdd_DeviceModelNotFound()` - 设备型号不存在场景

### 测试类和每个测试方法加上@DisplayName，用中文说明。含义与类名/方法名保持一致

### 【推荐】测试场景覆盖
- 正常流程测试
- 边界条件测试
- 异常情况测试
- 业务规则验证测试

## 4. 测试数据准备规范

### 【推荐】在@BeforeEach中初始化通用测试数据
```java
@BeforeEach
void setUp() {
    mockEntity = new ElectricMeterEntity();
    mockEntity.setId(1L);
    mockEntity.setMeterName("测试电表");
    
    mockVo = new ElectricMeterVo();
    mockVo.setId(1L);
    mockVo.setMeterName("测试电表");
}
```

## 5. 断言和验证规范

### 【强制】使用具体断言
```java
// 正例：具体断言
assertThat(result.getId()).isEqualTo(1L);
assertThat(result.getMeterName()).isEqualTo("测试电表");

// 反例：模糊断言
assertThat(result).isNotNull();
```

### 【强制】验证Mock对象交互
```java
// 验证方法调用
verify(repository).selectById(1L);
verify(mapper).toVo(mockEntity);

// 验证方法未被调用
verify(repository, never()).updateById(any(ElectricMeterEntity.class));
```

## 6. ArgumentCaptor使用规范

### 【推荐】捕获复杂参数进行验证
```java
@Captor
private ArgumentCaptor<ElectricMeterEntity> entityCaptor;

@Test
void testUpdate_Success() {
    // 执行测试
    electricMeterService.update(updateBo);
    
    // 捕获参数
    verify(repository).updateById(entityCaptor.capture());
    ElectricMeterEntity capturedEntity = entityCaptor.getValue();
    
    // 验证捕获的参数
    assertThat(capturedEntity.getId()).isEqualTo(1L);
    assertThat(capturedEntity.getMeterName()).isEqualTo("更新后的名称");
}
```

### 【注意】ArgumentCaptor使用场景
- 需要验证传入参数的具体内容时
- 参数是复杂对象且需要验证多个字段时
- 简单参数验证直接使用具体值
- 如果调用时使用了any，可以在verify时使用ArgumentCaptor具体校验

## 7. 异常测试规范

### 【强制】异常场景测试
```java
@Test
void testGetDetail_NotFound() {
    // 准备数据
    when(repository.selectById(999L)).thenReturn(null);
    
    // 执行并验证异常
    assertThatThrownBy(() -> electricMeterService.getDetail(999L))
        .isInstanceOf(BusinessException.class)
        .hasMessage("电表不存在");
}
```

### 【推荐】异常测试覆盖
- 业务异常
- 参数校验异常
- 系统异常处理

## 8. 测试覆盖率要求

### 【强制】覆盖率指标
- 行覆盖率：≥ 95%
- 分支覆盖率：≥ 85%
- 方法覆盖率：≥ 100%

### 【推荐】重点测试场景
- 所有public方法
- 核心业务逻辑
- 异常处理逻辑
- 边界条件

## 9. 常见反模式和最佳实践

### 【禁止】常见反模式
```java
// 反例1：过度使用any()
when(repository.selectById(any())).thenReturn(mockEntity);

// 反例2：测试方法过长
@Test
void testComplexScenario() {
    // 100+ 行代码
}

// 反例3：测试数据硬编码
assertThat(result.getCreateTime()).isEqualTo("2024-01-01 10:00:00");
```

### 【推荐】最佳实践
```java
// 正例1：使用具体参数
when(repository.selectById(1L)).thenReturn(mockEntity);

// 正例2：单一职责测试
@Test
void testAdd_Success() {
    // 专注测试添加成功场景
}

// 正例3：使用相对时间断言
assertThat(result.getCreateTime()).isAfter(testStartTime);
```

### 【强制】代码质量要求
- 测试方法保持简洁，单个方法不超过50行
- 每个测试方法只测试一个场景
- 测试代码要有良好的可读性
- 避免测试之间的相互依赖

### 【推荐】性能考虑
- 合理使用@BeforeEach和@BeforeAll
- 避免重复创建大量测试数据
- 使用@MockBean时注意Spring上下文重用

## 10. 示例模板

```java
@ExtendWith(MockitoExtension.class)
class ServiceImplTest {
    
    @Mock
    private Repository repository;
    
    @Mock
    private Mapper mapper;
    
    @InjectMocks
    private ServiceImpl service;
    
    @Captor
    private ArgumentCaptor<Entity> entityCaptor;
    
    private Entity mockEntity;
    private Vo mockVo;
    
    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }
    
    @Test
    void testMethod_Success() {
        // Given
        when(repository.method(param)).thenReturn(result);
        
        // When
        Result actual = service.method(param);
        
        // Then
        assertThat(actual).isNotNull();
        verify(repository).method(param);
    }
    
    @Test
    void testMethod_Exception() {
        // Given
        when(repository.method(param)).thenThrow(new RuntimeException());
        
        // When & Then
        assertThatThrownBy(() -> service.method(param))
            .isInstanceOf(BusinessException.class);
    }
}
```

### 11. 运行方式
```bash
# 例如运行ems-business-device模块的测试用例
export JAVA_HOME=YOUR_JAVA_HOME && mvn clean compile -DskipTests && mvn test -pl ems-business/ems-business-device -am
```

---

**注意事项：**
1. 本规范基于Mockito和JUnit 5框架
2. 遵循项目现有的代码风格和命名规范
3. 定期review测试代码，确保质量和维护性
4. 测试代码同样需要遵循SOLID原则和KISS原则
