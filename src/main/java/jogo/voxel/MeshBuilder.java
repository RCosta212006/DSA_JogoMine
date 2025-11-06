package jogo.voxel;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {
    private final List<Float> positions = new ArrayList<>();
    private final List<Float> normals = new ArrayList<>();
    private final List<Float> uvs = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    // Optional: enable per-block UV randomization for variety
    private boolean randomizeUV = false;

    public void setRandomizeUV(boolean randomizeUV) {
        this.randomizeUV = randomizeUV;
    }

    public int addVertex(Vector3f p, Vector3f n, Vector2f uv) {
        int idx = positions.size() / 3;
        positions.add(p.x); positions.add(p.y); positions.add(p.z);
        normals.add(n.x); normals.add(n.y); normals.add(n.z);
        uvs.add(uv.x); uvs.add(uv.y);
        return idx;
    }

    public void addQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal) {
        int i0 = addVertex(v0, normal, new Vector2f(0,0));
        int i1 = addVertex(v1, normal, new Vector2f(0,1));
        int i2 = addVertex(v2, normal, new Vector2f(1,1));
        int i3 = addVertex(v3, normal, new Vector2f(1,0));
        // CCW triangles
        indices.add(i0); indices.add(i1); indices.add(i2);
        indices.add(i0); indices.add(i2); indices.add(i3);
    }

    // Add a quad with explicit UVs
    private void addQuadUV(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal,
                           Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3) {
        int i0 = addVertex(v0, normal, uv0);
        int i1 = addVertex(v1, normal, uv1);
        int i2 = addVertex(v2, normal, uv2);
        int i3 = addVertex(v3, normal, uv3);
        indices.add(i0); indices.add(i1); indices.add(i2);
        indices.add(i0); indices.add(i2); indices.add(i3);
    }

    private static Vector2f transformUV(Vector2f uv, int rot, boolean flipU, boolean flipV) {
        float u = uv.x;
        float v = uv.y;
        // Apply flips first
        if (flipU) u = 1f - u;
        if (flipV) v = 1f - v;
        // Apply rotation in 90-degree steps around center (0.5,0.5)
        // To rotate around center, shift to origin, rotate, shift back
        float cu = u - 0.5f;
        float cv = v - 0.5f;
        float ru, rv;
        switch (rot & 3) {
            case 1 -> { // 90 deg
                ru = cv;
                rv = -cu;
            }
            case 2 -> { // 180 deg
                ru = -cu;
                rv = -cv;
            }
            case 3 -> { // 270 deg
                ru = -cv;
                rv = cu;
            }
            default -> { // 0 deg
                ru = cu;
                rv = cv;
            }
        }
        return new Vector2f(ru + 0.5f, rv + 0.5f);
    }

    public void addVoxelFace(int x, int y, int z, Face face) {
        float xf = x, yf = y, zf = z;

        int rot = 0; boolean flipU = false; boolean flipV = false;
        if (randomizeUV) {
            int h = hash3(x, y, z);
            rot = (h) & 3;                 // 0,1,2,3 => 0/90/180/270 deg
            flipU = ((h >>> 2) & 1) != 0;  // boolean
            flipV = ((h >>> 3) & 1) != 0;  // boolean
        }

        // Base UVs for a unit face
        Vector2f uv00 = transformUV(new Vector2f(0f, 0f), rot, flipU, flipV);
        Vector2f uv01 = transformUV(new Vector2f(0f, 1f), rot, flipU, flipV);
        Vector2f uv11 = transformUV(new Vector2f(1f, 1f), rot, flipU, flipV);
        Vector2f uv10 = transformUV(new Vector2f(1f, 0f), rot, flipU, flipV);

        switch (face) {
            case PX -> addQuadUV(
                    new Vector3f(xf+1, yf,   zf  ), new Vector3f(xf+1, yf+1, zf  ), new Vector3f(xf+1, yf+1, zf+1), new Vector3f(xf+1, yf,   zf+1), FaceNormals.PX,
                    uv00, uv01, uv11, uv10
            );
            case NX -> addQuadUV(
                    new Vector3f(xf,   yf,   zf+1), new Vector3f(xf,   yf+1, zf+1), new Vector3f(xf,   yf+1, zf  ), new Vector3f(xf,   yf,   zf  ), FaceNormals.NX,
                    uv00, uv01, uv11, uv10
            );
            case PY -> addQuadUV(
                    new Vector3f(xf,   yf+1, zf  ), new Vector3f(xf,   yf+1, zf+1), new Vector3f(xf+1, yf+1, zf+1), new Vector3f(xf+1, yf+1, zf  ), FaceNormals.PY,
                    uv00, uv01, uv11, uv10
            );
            case NY -> addQuadUV(
                    new Vector3f(xf+1, yf,   zf  ), new Vector3f(xf+1, yf,   zf+1), new Vector3f(xf,   yf,   zf+1), new Vector3f(xf,   yf,   zf  ), FaceNormals.NY,
                    uv00, uv01, uv11, uv10
            );
            case PZ -> addQuadUV(
                    new Vector3f(xf,   yf,   zf+1), new Vector3f(xf+1, yf,   zf+1), new Vector3f(xf+1, yf+1, zf+1), new Vector3f(xf,   yf+1, zf+1), FaceNormals.PZ,
                    uv00, uv01, uv11, uv10
            );
            case NZ -> addQuadUV(
                    new Vector3f(xf+1, yf,   zf  ), new Vector3f(xf,   yf,   zf  ), new Vector3f(xf,   yf+1, zf  ), new Vector3f(xf+1, yf+1, zf  ), FaceNormals.NZ,
                    uv00, uv01, uv11, uv10
            );
        }
    }

    private static int hash3(int x, int y, int z) {
        int h = x * 73856093 ^ y * 19349663 ^ z * 83492791;
        // Finalize mix
        h ^= (h >>> 13);
        h *= 0x5bd1e995;
        h ^= (h >>> 15);
        return h;
    }

    public Mesh build() {
        Mesh mesh = new Mesh();
        float[] pos = toFloatArray(positions);
        float[] nor = toFloatArray(normals);
        float[] tc = toFloatArray(uvs);
        int[] idx = indices.stream().mapToInt(Integer::intValue).toArray();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, pos);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, nor);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, tc);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, idx);
        mesh.updateBound();
        mesh.updateCounts();
        return mesh;
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    public enum Face { PX, NX, PY, NY, PZ, NZ }

    public static class FaceNormals {
        public static final Vector3f PX = new Vector3f(1,0,0);
        public static final Vector3f NX = new Vector3f(-1,0,0);
        public static final Vector3f PY = new Vector3f(0,1,0);
        public static final Vector3f NY = new Vector3f(0,-1,0);
        public static final Vector3f PZ = new Vector3f(0,0,1);
        public static final Vector3f NZ = new Vector3f(0,0,-1);
    }
}
