# 会话上下文总结 (Session Context Summary)

> **生成时间**：2026-06-09 14:30:00
> **当前项目/分支**：main — 托管培训中心信息管理系统 (DBACourseProject)

### 1. 核心解决问题与实现方案 (Problems Solved & Core Logic)

---

#### 1.1 项目进度全面审查

对 `DBACourseProject` 进行了全量代码审查，覆盖数据库层（6 张表）、后端（6 Entity + 6 Mapper + 7 Service + 7 Controller）、前端（7 页面 + 7 API 模块 + Router + Layout），输出完整的完成度评估与待办清单。

---

#### 1.2 后端安全漏洞修复

- **问题/需求描述**：`ClassesService.deleteClass` 和 `StudentService.deleteStudent` 在删除前未检查关联表（`student_enrollments`、`accounts`），直接执行 DELETE 将触发 MySQL 外键约束异常，导致 500 错误。
- **核心解决思路**：在 `StudentEnrollmentMapper` 中新增 `countByClassCode()` 和 `countByStudentId()` 两枚计数方法；在 `AccountMapper` 中新增 `countByStudentId()`；在 `ClassesService` 和 `StudentService` 的 delete 方法中先调用计数检查，存在关联记录时返回明确的业务错误提示而非让数据库抛异常。
- **关键代码/配置变更**：

```java
// StudentEnrollmentMapper.java — 新增两枚计数查询
@Select("SELECT COUNT(*) FROM student_enrollments WHERE class_code = #{classCode}")
int countByClassCode(String classCode);

@Select("SELECT COUNT(*) FROM student_enrollments WHERE student_id = #{studentId}")
int countByStudentId(Integer studentId);

// ClassesService.java — 删除前检查
if (studentEnrollmentMapper.countByClassCode(classCode) > 0) {
    return "删除失败：该班级下还有学生报名记录，请先处理报名关系！";
}

// StudentService.java — 删除前双重检查
if (studentEnrollmentMapper.countByStudentId(studentId) > 0) {
    return "删除失败：该学生还有选课报名记录，请先处理报名关系！";
}
if (accountMapper.countByStudentId(studentId) > 0) {
    return "删除失败：该学生还有账目流水记录，请先处理账目！";
}
```

---

#### 1.3 enrolledCount 字段被前端表单任意覆盖的漏洞

- **问题/需求描述**：`ClassManage.vue` 编辑班级弹窗中，"已报名人数"输入框开放编辑。`ClassesService.updateClass` 直接透传前端提交的 `enrolledCount` 值到 `UPDATE` SQL，意味着用户可以通过编辑班级页面**任意篡改已报名人数**，破坏数据一致性。
- **核心解决思路**：**双重防御**——(1) 后端 `ClassesService.updateClass` 在更新前先读取数据库中现有的 `enrolledCount`，强制覆盖前端传入的值；(2) 前端 `ClassManage.vue` 编辑模式下将 `enrolledCount` 输入框 `disabled`，并标注灰色提示"由系统自动维护，不可手动修改"。
- **关键代码/配置变更**：

```java
// ClassesService.java updateClass — 后端兜底
Classes existing = classesMapper.getClassByCode(classes.getClassCode());
// ... 外键校验 ...
classes.setEnrolledCount(existing.getEnrolledCount()); // 强制覆盖，忽略前端值
return classesMapper.updateClass(classes) > 0 ? "更新成功" : "更新失败";
```

```html
<!-- ClassManage.vue — 前端禁用 -->
<el-input-number v-model="form.enrolledCount" :min="0" :disabled="isEdit" />
<span v-if="isEdit" style="color:#909399">（由系统自动维护，不可手动修改）</span>
```

---

#### 1.4 前端"假删除成功"核心 Bug 修复

- **问题/需求描述**：所有删除操作均能弹出确认框，点击确认后始终显示绿色"删除成功"，但数据库中的数据纹丝不动。用户无法区分操作的真实成败。
- **核心解决思路**：
  - **根因**：后端所有操作（成功/失败）均返回 HTTP 200 + 纯字符串（如 `"删除成功"` 或 `"删除失败：该科目下还有班级"`）。Axios 响应拦截器对 HTTP 200 一律按成功处理，前端各组件在 `await` 后**无条件**执行 `ElMessage.success('删除成功')` 和 `loadData()`，完全无视后端实际返回的字符串内容。
  - **修复**：在 `request.js` 响应拦截器的成功分支中，对返回值做**字符串语义分析**——若包含 `"失败"`、`"已存在"`、`"不能"` 关键词，自动弹出 `ElMessage.error` 并 `Promise.reject()`，使组件侧的 `catch` 块接管控制流，阻止后续的虚假成功提示和无效数据刷新。
  - **防御加固**：为 `SubjectManage.vue` 的 `saveSubject()` 补充缺失的 try/catch（原先裸 `await` 在拦截器 reject 后会产生未处理的 Promise rejection）。
- **关键代码/配置变更**：

```js
// request.js — 响应拦截器成功分支：字符串语义分析
request.interceptors.response.use(
  response => {
    const data = response.data
    if (typeof data === 'string' && (data.includes('失败') || data.includes('已存在') || data.includes('不能'))) {
      ElMessage.error(data)
      return Promise.reject(new Error(data))
    }
    return data
  },
  // ... 错误分支（已同步优化：支持 string / {message} / {error} / JSON 四种格式）
)
```

---

#### 1.5 前端页面细节修复（4 项）

| 修复项 | 文件 | 变更 |
|--------|------|------|
| 学生课表"教师ID"列只显示数字 | `ScheduleQuery.vue` | 加载教师列表，添加 `getTeacherName()` 映射函数 |
| 缴费金额默认值 0 可空提交 | `FeeManage.vue` | 拆分校验逻辑，金额独立判断 `> 0` |
| 报名成功后班级名额不刷新 | `StudentEnrollment.vue` | 报名成功后保留科目选择，自动重新拉取班级列表 |
| 错误拦截器对 Spring Boot JSON 对象处理不当 | `request.js` | 智能识别 `string` / `{message}` / `{error}` / 纯对象四种格式 |

---

#### 1.6 后端工程质量提升

- **Entity 层 Lombok 化**：6 个 Entity 全部由手写 getter/setter（~280 行）替换为 `@Data` + `@NoArgsConstructor`，pom.xml 已引入 Lombok 依赖。
- **全局异常处理器**：新增 `GlobalExceptionHandler.java`（`@RestControllerAdvice`），统一捕获 `DataIntegrityViolationException`（FK 冲突/重复键/CHECK 约束）、`MissingServletRequestParameterException`、`HttpMessageNotReadableException`、`NumberFormatException` 及兜底 `Exception`，向前端返回结构化错误信息。
- **统一响应格式**：新增 `dto/Result<T>` 类（`{code, message, data}`），为后续 Controller 层重构提供标准响应模板。

---

### 2. 问答对历史沉淀 (Q&A History)

- **Q1**：结合课设要求与项目内容，分析课程设计进度如何？
  - **A1**：逐文件审查了整个项目，从数据库（100%）、后端（95%）、前端（90%）、日志文档（80%）、课设报告（0%）、加分项（17%）六个维度给出细化评分。核心功能基本完成，主要差距在课设报告未撰写、加分项仅完成事务管理一项、前后端联调待验证。

- **Q2**：前后端开发只列了已完成部分，未列出待完成事项，要求逐步列举并完善。
  - **A2**：逐文件审查后识别出 11 个问题（3 严重 Bug + 4 中等改进 + 4 工程质量提升），逐一修复并编译验证。修改 14 个文件，新增 `dto/Result.java` 和 `handler/GlobalExceptionHandler.java`。

- **Q3**：网页端删除操作弹出"删除成功"但数据没变；以及编辑后的数据不知道存在哪里。
  - **A3**：
    - **数据存储**：纠正了概念误解——`insert_test_data.sql` 是一次性初始化脚本，数据修改发生在 MySQL 数据库 `tutoring_center` 中，需用 `SELECT` 语句查询实际表数据而非查看 SQL 文件。
    - **假删除成功**：定位到 `request.js` 响应拦截器对 HTTP 200 字符串不做内容甄别的架构缺陷，通过语义关键词检测（`失败`/`已存在`/`不能`）自动将业务失败转为 Promise rejection，修复了全部 CRUD 操作的真假成功混淆问题。

---

### 3. 遗留问题与下一步计划 (Next Steps / Pending Items)

- **前后端联调**：代码已修改完毕且前后端独立编译均通过，但需启动 MySQL + 后端（`./mvnw spring-boot:run`）+ 前端（`npm run dev`），在浏览器中完整走通全部业务流程（科目 CRUD → 班级创建 → 学生建档 → 报名 → 缴费 → 课表查询 → 催费列表），验证修复效果。
- **课设报告**：[课设报告模版.docx](../课设说明/课设报告模版.docx) 已就绪，尚未开始正文撰写。建议优先完成报告，占总评比重通常最高。
- **加分项**：6 项仅完成事务管理（`@Transactional`），建议再选做 Redis 缓存（对科目/教师查询做缓存，实现复杂度最低）或敏感数据加解密。
- **安全项**：`application.properties` 中数据库密码 `Root@123456` 以明文硬编码并已提交至公开 GitHub 仓库，建议使用环境变量或 Spring 加密方案处理。
- **`StudentEnrollmentMapper.selectByStudentAndClass` 使用 `SELECT *`**：当前 MyBatis `map-underscore-to-camel-case=true` 配置下可正常工作，但建议将 `*` 替换为显式列名+别名以增强可维护性。

---
