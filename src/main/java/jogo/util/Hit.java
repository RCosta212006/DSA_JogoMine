package jogo.util;

import com.jme3.math.Vector3f;
import jogo.voxel.VoxelWorld.Vector3i;

public class Hit {
    public final Vector3i cell;
    public final Vector3f normal;
    public final float distance;
    public Hit(Vector3i cell, Vector3f normal, float distance) {
        this.cell = cell;
        this.normal = normal;
        this.distance = distance;
    }
}
