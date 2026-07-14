export const resourceTabs = [
  {
    key: "herb-batches",
    label: "批次",
    titleField: "batchName",
    meta: ["batchCode", "herbName", "district", "currentStage"]
  },
  {
    key: "growth-records",
    label: "生长",
    titleField: "herbName",
    meta: ["district", "growthStage", "temperature", "recordedAt"]
  },
  {
    key: "trace-events",
    label: "溯源",
    titleField: "eventType",
    meta: ["herbName", "traceCode", "operatorName", "eventTime"]
  },
  {
    key: "courses",
    label: "课程",
    titleField: "title",
    meta: ["teacher", "hours", "materialType", "status"]
  },
  {
    key: "teaching-resources",
    label: "资料",
    titleField: "title",
    meta: ["courseTitle", "resourceType", "uploader", "status"]
  },
  {
    key: "projects",
    label: "课题",
    titleField: "title",
    meta: ["leader", "stage", "status"]
  }
];

export function displayValue(item, field) {
  const value = item?.[field];
  if (value === undefined || value === null || value === "") return "";
  return String(value);
}

export function summarize(item, fields) {
  return fields.map(field => displayValue(item, field)).filter(Boolean).join(" · ");
}
