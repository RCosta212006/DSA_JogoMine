package jogo.framework.math;

public class Vec3 {
    public float x, y, z;

    public Vec3() { this(0,0,0); }
    public Vec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }

    public Vec3 set(float x, float y, float z) { this.x = x; this.y = y; this.z = z; return this; }
    public Vec3 set(Vec3 other) { return set(other.x, other.y, other.z); }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Float.floatToIntBits(this.x);
        hash = 31 * hash + Float.floatToIntBits(this.y);
        hash = 31 * hash + Float.floatToIntBits(this.z);
        return hash;
    }
}

