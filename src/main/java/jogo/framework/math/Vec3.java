package jogo.framework.math;

import com.jme3.math.Vector3f;

import java.net.URI;

public class Vec3 {
    public float x;
    public float y;
    public float z;

    public Vec3() { this(0,0,0); }
    public Vec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }

    public Vec3(Vector3f worldTranslation) {
        this.x = worldTranslation.x;
        this.y = worldTranslation.y;
        this.z = worldTranslation.z;
    }

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

    public Vec3 subtract(Vec3 other) {
        return new Vec3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vec3 add(Vec3 other) {
        return new Vec3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vec3 normalize() {
        float length = (float)Math.sqrt(x * x + y * y + z * z);
        if (length != 0) {
            return new Vec3(x / length, y / length, z / length);
        } else {
            return new Vec3(0, 0, 0); // or handle zero-length vector case as needed
        }
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }
}

