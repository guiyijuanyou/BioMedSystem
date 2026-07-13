import {
  BufferAttribute,
  BufferGeometry,
  Color,
  DoubleSide,
  DynamicDrawUsage,
  Group,
  InstancedMesh,
  LineBasicMaterial,
  LineSegments,
  Mesh,
  MeshBasicMaterial,
  MeshStandardMaterial,
  Object3D,
  Points,
  PointsMaterial,
  SphereGeometry,
} from "three";

export const LEAF_RENDER_BUDGET = Object.freeze({
  longitudinalSegments: 28,
  widthSegments: 10,
  flowNodeCount: 18,
  maxDrawObjects: 5,
});

const BLUE = new Color("#0073e6");
const PURPLE = new Color("#635bff");
const ORANGE = new Color("#ff7a59");
const FLOW_COLORS = [new Color("#68cfff"), new Color("#7a5cff"), new Color("#ff8a4c")];

function leafWidth(t) {
  return Math.pow(Math.sin(Math.PI * t), 0.72) * (1.5 - 0.15 * t);
}

function setLeafPoint(target, t, lateral) {
  const width = leafWidth(t);
  target.set(
    Math.sin(t * Math.PI * 1.3) * 0.12 + lateral * width,
    (t - 0.5) * 5.8,
    Math.sin(t * Math.PI) * 0.18 * (1 - lateral * lateral)
      + lateral * lateral * 0.14
      + Math.sin(t * Math.PI * 2) * 0.08,
  );
  return target;
}

function createSurface() {
  const geometry = new BufferGeometry();
  const positions = [];
  const colors = [];
  const indices = [];
  const color = new Color();
  const point = new Object3D().position;
  const { longitudinalSegments, widthSegments } = LEAF_RENDER_BUDGET;

  for (let row = 0; row <= longitudinalSegments; row += 1) {
    const t = row / longitudinalSegments;
    for (let column = 0; column <= widthSegments; column += 1) {
      const lateral = column / widthSegments * 2 - 1;
      setLeafPoint(point, t, lateral);
      positions.push(point.x, point.y, point.z);

      color.copy(BLUE).lerp(PURPLE, t);
      const edgeMix = Math.max(0, (Math.abs(lateral) - 0.72) / 0.28) * 0.18;
      color.lerp(ORANGE, edgeMix);
      colors.push(color.r, color.g, color.b);
    }
  }

  for (let row = 0; row < longitudinalSegments; row += 1) {
    for (let column = 0; column < widthSegments; column += 1) {
      const current = row * (widthSegments + 1) + column;
      const nextRow = current + widthSegments + 1;
      indices.push(current, nextRow, current + 1, current + 1, nextRow, nextRow + 1);
    }
  }

  geometry.setIndex(indices);
  geometry.setAttribute("position", new BufferAttribute(new Float32Array(positions), 3));
  geometry.setAttribute("color", new BufferAttribute(new Float32Array(colors), 3));
  geometry.computeVertexNormals();

  const material = new MeshStandardMaterial({
    vertexColors: true,
    transparent: true,
    opacity: 0.3,
    depthWrite: false,
    roughness: 0.72,
    metalness: 0.05,
    side: DoubleSide,
  });

  return new Mesh(geometry, material);
}

function addLine(positions, from, to) {
  positions.push(from.x, from.y, from.z + 0.035, to.x, to.y, to.z + 0.035);
}

function createVeins() {
  const positions = [];
  const from = new Object3D().position;
  const to = new Object3D().position;

  for (let index = 0; index < LEAF_RENDER_BUDGET.longitudinalSegments; index += 1) {
    setLeafPoint(from, index / LEAF_RENDER_BUDGET.longitudinalSegments, 0);
    setLeafPoint(to, (index + 1) / LEAF_RENDER_BUDGET.longitudinalSegments, 0);
    addLine(positions, from, to);
  }

  for (let branch = 0; branch < 9; branch += 1) {
    const rootT = 0.12 + branch * 0.09;
    for (const side of [-1, 1]) {
      setLeafPoint(from, rootT, 0);
      setLeafPoint(to, Math.min(0.96, rootT + 0.075), side * 0.84);
      addLine(positions, from, to);
    }
  }

  const geometry = new BufferGeometry();
  geometry.setAttribute("position", new BufferAttribute(new Float32Array(positions), 3));
  const material = new LineBasicMaterial({
    color: "#edf4ff",
    transparent: true,
    opacity: 0.72,
    depthWrite: false,
  });

  return new LineSegments(geometry, material);
}

function createFlowNodes() {
  const geometry = new SphereGeometry(0.055, 8, 6);
  const material = new MeshBasicMaterial({
    vertexColors: true,
    transparent: true,
    opacity: 0.9,
    depthWrite: false,
  });
  const nodes = new InstancedMesh(geometry, material, LEAF_RENDER_BUDGET.flowNodeCount);
  nodes.instanceMatrix.setUsage(DynamicDrawUsage);

  for (let index = 0; index < LEAF_RENDER_BUDGET.flowNodeCount; index += 1) {
    const colorIndex = index % 9 === 0 ? 2 : index % 2;
    nodes.setColorAt(index, FLOW_COLORS[colorIndex]);
  }
  if (nodes.instanceColor) nodes.instanceColor.needsUpdate = true;

  return nodes;
}

function seededRandom(seed) {
  let state = seed >>> 0;
  return () => {
    state += 0x6D2B79F5;
    let value = state;
    value = Math.imul(value ^ value >>> 15, value | 1);
    value ^= value + Math.imul(value ^ value >>> 7, value | 61);
    return ((value ^ value >>> 14) >>> 0) / 4294967296;
  };
}

function createParticles(particleCount) {
  const count = Math.max(0, Math.floor(particleCount));
  const random = seededRandom(20260713);
  const positions = new Float32Array(count * 3);

  for (let index = 0; index < count; index += 1) {
    positions[index * 3] = (random() - 0.5) * 6.6;
    positions[index * 3 + 1] = (random() - 0.5) * 7.2;
    positions[index * 3 + 2] = (random() - 0.5) * 2.4 - 0.4;
  }

  const geometry = new BufferGeometry();
  geometry.setAttribute("position", new BufferAttribute(positions, 3));
  const material = new PointsMaterial({
    color: "#68cfff",
    size: 0.045,
    transparent: true,
    opacity: 0.38,
    depthWrite: false,
    sizeAttenuation: true,
  });

  return new Points(geometry, material);
}

export function createMedicinalLeafModel({ particleCount = 32 } = {}) {
  const group = new Group();
  const surface = createSurface();
  const veins = createVeins();
  const flowNodes = createFlowNodes();
  const particles = createParticles(particleCount);
  const transform = new Object3D();

  group.add(surface, veins, flowNodes, particles);
  group.position.set(1.65, -0.05, 0);
  group.scale.setScalar(0.92);

  const model = { group, surface, veins, flowNodes, particles, transform };
  updateMedicinalLeafModel(model, 0);
  return model;
}

export function updateMedicinalLeafModel(model, elapsed) {
  const time = Number.isFinite(elapsed) ? elapsed : 0;
  model.group.rotation.set(
    -0.08 + Math.sin(time * 0.13) * 0.025,
    -0.18 + Math.sin(time * 0.1) * 0.055,
    -0.22 + Math.cos(time * 0.11) * 0.02,
  );
  model.group.scale.setScalar(0.92 + Math.sin(time * 0.16) * 0.012);

  for (let index = 0; index < LEAF_RENDER_BUDGET.flowNodeCount; index += 1) {
    if (index < 6) {
      const t = (time * 0.035 + index / 6) % 1;
      setLeafPoint(model.transform.position, t, 0);
    } else {
      const branch = Math.floor((index - 6) / 2);
      const side = index % 2 === 0 ? -1 : 1;
      const progress = (time * 0.05 + branch * 0.17) % 1;
      const rootT = 0.16 + branch * 0.12;
      setLeafPoint(model.transform.position, Math.min(0.97, rootT + progress * 0.075), side * progress * 0.84);
    }

    model.transform.position.z += 0.07;
    const pulse = 0.78 + Math.sin(time * 0.8 + index) * 0.18;
    model.transform.scale.setScalar(pulse);
    model.transform.updateMatrix();
    model.flowNodes.setMatrixAt(index, model.transform.matrix);
  }

  model.flowNodes.instanceMatrix.needsUpdate = true;
  model.particles.rotation.z = time * 0.006;
}

function disposeObject(object) {
  object.geometry?.dispose();
  if (Array.isArray(object.material)) object.material.forEach(material => material.dispose());
  else object.material?.dispose();
}

export function disposeMedicinalLeafModel(model) {
  disposeObject(model.surface);
  disposeObject(model.veins);
  disposeObject(model.flowNodes);
  disposeObject(model.particles);
  model.group.clear();
}
