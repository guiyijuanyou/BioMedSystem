export const modules = {
  herbs: {
    title: "中药材分布地图",
    hint: "品种分布、生态环境、溯源码",
    fields: [
      ["name", "品种名称"], ["district", "分布区县"], ["longitude", "经度"],
      ["latitude", "纬度"], ["scale", "种植规模"], ["environment", "生态环境"], ["traceCode", "溯源码"]
    ]
  },
  "growth-records": {
    title: "生长数据采集",
    hint: "APP、传感器和电脑终端采集记录",
    fields: [
      ["herbName", "药材名称"], ["district", "采集地点"], ["temperature", "温度"],
      ["humidity", "湿度"], ["soilPh", "土壤 PH"], ["growthStage", "生长阶段"], ["collector", "采集来源"], ["recordedAt", "采集时间"]
    ]
  },
  "spectrum-comparisons": {
    title: "图谱比对",
    hint: "中药材图谱上传、参考图谱比对和相似度判定",
    fields: [
      ["herbName", "药材名称"], ["sampleCode", "样本编号"], ["district", "采集区县"],
      ["spectrumType", "图谱类型"], ["referenceName", "参考图谱"], ["similarity", "相似度"],
      ["result", "比对结果"], ["operator", "操作人"], ["comparedAt", "比对时间"], ["remark", "备注"]
    ]
  },
  "growth-analysis": {
    title: "数据对比分析",
    hint: "按批次、区县和生态指标对生长数据进行对比分析",
    fields: [
      ["analysisName", "分析名称"], ["herbName", "药材名称"], ["district", "对比区县"],
      ["indicator", "分析指标"], ["baseline", "基准值"], ["currentValue", "当前值"],
      ["difference", "差异"], ["trend", "趋势判断"], ["conclusion", "分析结论"], ["analyst", "分析人"], ["analyzedAt", "分析时间"]
    ]
  },
  courses: {
    title: "试验课程存储",
    hint: "课程、课件、视频教学资源",
    fields: [["title", "课程名称"], ["teacher", "教师"], ["hours", "学时"], ["materialType", "资料类型"], ["status", "状态"]]
  },
  projects: {
    title: "试验课题研究",
    hint: "课题内容、参与研究、成果转化",
    fields: [["title", "课题名称"], ["leader", "负责人"], ["stage", "研究阶段"], ["transformation", "成果转化"]]
  },
  trainings: {
    title: "培训素材与跟踪",
    hint: "素材制作、培训过程、记录归档",
    fields: [["title", "培训主题"], ["trainer", "培训人"], ["audience", "培训对象"], ["tracking", "过程记录"]]
  },
  evaluations: {
    title: "中药材评价体系",
    hint: "评价过程、结果跟踪、申报素材",
    fields: [["herbName", "药材名称"], ["indicator", "评价指标"], ["score", "评分"], ["result", "评价结果"], ["applicationMaterial", "申报素材"]]
  },
  achievements: {
    title: "工作业绩管理",
    hint: "录入、审核、编辑、分级分类认定",
    fields: [["title", "业绩名称"], ["owner", "所属单位"], ["category", "分类"], ["level", "级别"], ["status", "审核状态"]]
  },
  standards: {
    title: "分类标准动态更新",
    hint: "学校业绩分类认定办法",
    fields: [["name", "标准名称"], ["category", "适用分类"], ["levelRule", "分级规则"], ["effectiveDate", "生效日期"]]
  },
  users: {
    title: "用户层级管理",
    hint: "管理员、教师、学生、科研机构分级授权",
    fields: [["name", "用户名称"], ["role", "角色"], ["department", "部门"], ["level", "权限层级"]]
  }
};

export const navItems = [
  ["dashboard", "总览"],
  ["herbs", "分布地图"],
  ["growth-records", "生长采集"],
  ["spectrum-comparisons", "图谱比对"],
  ["growth-analysis", "数据分析"],
  ["courses", "试验课程"],
  ["projects", "课题研究"],
  ["trainings", "培训素材"],
  ["evaluations", "评价体系"],
  ["achievements", "业绩管理"],
  ["standards", "认定标准"],
  ["users", "用户层级"],
  ["files", "资料文件"]
];
