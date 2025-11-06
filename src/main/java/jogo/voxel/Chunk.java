package jogo.voxel;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import jogo.framework.math.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a chunk of the voxel world (e.g., 16x16x16 blocks).
 */
public class Chunk {
    public static final int SIZE = 16;
    private final int chunkX, chunkY, chunkZ;
    private final byte[][][] vox;
    private final Node node;

    private boolean dirty = true;

    private RigidBodyControl rigidBody;

    public Chunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.vox = new byte[SIZE][SIZE][SIZE];
        this.node = new Node("Chunk_" + chunkX + "," + chunkY + "," + chunkZ);
    }

    public Node getNode() { return node; }
    public byte get(int x, int y, int z) { return vox[x][y][z]; }
    public void set(int x, int y, int z, byte id) { vox[x][y][z] = id; }
    public int getChunkX() { return chunkX; }
    public int getChunkY() { return chunkY; }
    public int getChunkZ() { return chunkZ; }

    public void markDirty() { dirty = true; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }

    // Build and attach mesh for this chunk
    public void buildMesh(AssetManager assetManager, VoxelPalette palette) {
        long start = System.nanoTime();
        node.detachAllChildren();
        Map<Byte, MeshBuilder> builders = new HashMap<>();
        for (int i = 0; i < palette.size(); i++) {
            if (i == VoxelPalette.AIR_ID) continue;
            MeshBuilder mb = new MeshBuilder();
            // Randomize UVs only for dirt to add variation without per-block materials
            mb.setRandomizeUV(true);
            builders.put((byte)i, mb);
        }
        // Track first block position for each type
        Map<Byte, Vec3> firstBlockPos = new HashMap<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte id = vox[x][y][z];
                    if (id == VoxelPalette.AIR_ID) continue;
                    if (!palette.get(id).isSolid()) continue;
                    MeshBuilder builder = builders.get(id);
                    int wx = chunkX * SIZE + x;
                    int wy = chunkY * SIZE + y;
                    int wz = chunkZ * SIZE + z;
                    // Only add faces if neighbor is air or out of bounds
                    if (!isSolid(wx+1,wy,wz,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.PX);
                    if (!isSolid(wx-1,wy,wz,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.NX);
                    if (!isSolid(wx,wy+1,wz,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.PY);
                    if (!isSolid(wx,wy-1,wz,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.NY);
                    if (!isSolid(wx,wy,wz+1,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.PZ);
                    if (!isSolid(wx,wy,wz-1,palette)) builder.addVoxelFace(wx,wy,wz, MeshBuilder.Face.NZ);
                    if (!firstBlockPos.containsKey(id)) firstBlockPos.put(id, new Vec3(wx, wy, wz));
                }
            }
        }
        int geomCount = 0;
        for (Map.Entry<Byte, MeshBuilder> entry : builders.entrySet()) {
            MeshBuilder meshBuilder = entry.getValue();
            Mesh mesh = meshBuilder.build();
            if (mesh.getTriangleCount() > 0) {
                byte id = entry.getKey();
                Geometry g = new Geometry("chunk_"+chunkX+"_"+chunkY+"_"+chunkZ+"_"+id, mesh);
                Vec3 blockPos = firstBlockPos.getOrDefault(id, new Vec3(chunkX*SIZE, chunkY*SIZE, chunkZ*SIZE));
                Material mat = palette.get(id).getMaterial(assetManager, blockPos);
                g.setMaterial(mat);
                node.attachChild(g);
                geomCount++;
            }
        }
        long end = System.nanoTime();
        System.out.println("Chunk ["+chunkX+","+chunkY+","+chunkZ+"] mesh built in " + ((end-start)/1_000_000.0) + " ms, geometries: " + geomCount);
    }

    /**
     * Updates the physics control for this chunk. Call after mesh rebuild.
     */
    public void updatePhysics(PhysicsSpace space) {
        if (rigidBody != null) {
            System.out.println("Removing old RigidBodyControl for chunk ["+chunkX+","+chunkY+","+chunkZ+"]");
            space.remove(rigidBody);
            node.removeControl(rigidBody);
            rigidBody = null;
        }
        if (node.getQuantity() > 0) { // Only add if chunk has geometry
            // Ensure node is attached to a parent (world node)
            if (node.getParent() == null) {
                System.out.println("Warning: Chunk node ["+chunkX+","+chunkY+","+chunkZ+"] not attached to world node before physics update!");
            }
            // Clone meshes for collision shape to avoid Bullet caching issues
            Node tempNode = node.clone(false); // shallow clone
            for (int i = 0; i < node.getQuantity(); i++) {
                if (node.getChild(i) instanceof Geometry) {
                    Geometry g = (Geometry) node.getChild(i);
                    Geometry gClone = g.clone(false);
                    gClone.setMesh(g.getMesh().deepClone());
                    tempNode.attachChild(gClone);
                }
            }
            CollisionShape shape = CollisionShapeFactory.createMeshShape(tempNode);
            rigidBody = new RigidBodyControl(shape, 0f);
            node.addControl(rigidBody);
            space.add(rigidBody);
            System.out.println("Added new RigidBodyControl for chunk ["+chunkX+","+chunkY+","+chunkZ+"]");
        }
    }

    // Helper for solid check in world coordinates
    private boolean isSolid(int wx, int wy, int wz, VoxelPalette palette) {
        if (wx < 0 || wy < 0 || wz < 0) return false;
        int max = SIZE * 1000; // arbitrary large world limit
        if (wx >= max || wy >= max || wz >= max) return false;
        int cx = wx / SIZE, cy = wy / SIZE, cz = wz / SIZE;
        int lx = wx % SIZE, ly = wy % SIZE, lz = wz % SIZE;
        if (cx != chunkX || cy != chunkY || cz != chunkZ) return false; // only check within this chunk
        if (lx < 0 || ly < 0 || lz < 0 || lx >= SIZE || ly >= SIZE || lz >= SIZE) return false;
        byte id = vox[lx][ly][lz];
        return id != VoxelPalette.AIR_ID && palette.get(id).isSolid();
    }
}
