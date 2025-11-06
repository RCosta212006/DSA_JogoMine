package jogo.voxel;

import java.util.Objects;

public class VoxelBlock {
    private final String name;
    private final boolean solid;

    VoxelBlock(String name, boolean solid) {
        this.name = name;
        this.solid = solid;
    }

    public String getName() { return name; }
    public boolean isSolid() { return solid; }

    @Override
    public String toString() { return "VoxelBlock{" + name + ", solid=" + solid + '}'; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoxelBlock)) return false;
        VoxelBlock that = (VoxelBlock) o;
        return solid == that.solid && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, solid);
    }

    // Factory helpers
    public static VoxelBlock air() { return new VoxelBlock("air", false); }
    public static VoxelBlock solid() { return new VoxelBlock("solid", true); }
}

