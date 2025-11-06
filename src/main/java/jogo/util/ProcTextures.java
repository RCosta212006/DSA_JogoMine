package jogo.util;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public final class ProcTextures {
    private ProcTextures() {}

    public static Texture2D checker(int size, int squares, ColorRGBA a, ColorRGBA b) {
        int w = size;
        int h = size;
        Image image = new Image(Image.Format.RGBA8, w, h, ByteBuffer.allocateDirect(w * h * 4));
        ImageRaster ras = ImageRaster.create(image);
        int sq = Math.max(1, squares);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cx = x * sq / w;
                int cy = y * sq / h;
                boolean useA = ((cx + cy) & 1) == 0;
                ras.setPixel(x, y, useA ? a : b);
            }
        }
        Texture2D tex = new Texture2D(image);
        tex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
        tex.setMinFilter(com.jme3.texture.Texture.MinFilter.Trilinear);
        tex.setAnisotropicFilter(4);
        tex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
        return tex;
    }

    public static Texture2D random(int width, int height, double randomness, int seed, ColorRGBA a, ColorRGBA b) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        Image image = new Image(Image.Format.RGBA8, w, h, ByteBuffer.allocateDirect(w * h * 4));
        ImageRaster ras = ImageRaster.create(image);
        double thresh = Math.min(1.0, Math.max(0.0, randomness));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean useA = ((Math.abs((x * 73856093) ^ (y * 19349663) ^ seed) % 1000) / 1000.0) >= thresh;
                ras.setPixel(x, y, useA ? a : b);
            }
        }
        Texture2D tex = new Texture2D(image);
        // Use crisp filtering for high-frequency noise to avoid blur
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setAnisotropicFilter(1);
        tex.setWrap(Texture.WrapMode.Repeat);
        return tex;
    }

    public static Texture2D randomBlocks(int width, int height, int cellSize, double randomness, int seed, ColorRGBA a, ColorRGBA b) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int cs = Math.max(1, cellSize);
        Image image = new Image(Image.Format.RGBA8, w, h, ByteBuffer.allocateDirect(w * h * 4));
        ImageRaster ras = ImageRaster.create(image);
        double thresh = Math.min(1.0, Math.max(0.0, randomness));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cx = (x / cs);
                int cy = (y / cs);
                int hash = (cx * 73856093) ^ (cy * 19349663) ^ seed;
                boolean useA = ((Math.abs(hash) % 1000) / 1000.0) >= thresh;
                ras.setPixel(x, y, useA ? a : b);
            }
        }
        Texture2D tex = new Texture2D(image);
        // Crisp filtering for procedural blocky noise
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setAnisotropicFilter(1);
        tex.setWrap(Texture.WrapMode.Repeat);
        return tex;
    }

    // New: list-based palette version
    public static Texture2D randomBlocks(int width, int height, int cellSize, int seed, List<ColorRGBA> colors) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int cs = Math.max(1, cellSize);
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("randomBlocks: colors list must not be empty");
        }
        Image image = new Image(Image.Format.RGBA8, w, h, ByteBuffer.allocateDirect(w * h * 4));
        ImageRaster ras = ImageRaster.create(image);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cx = (x / cs);
                int cy = (y / cs);
                int hash = hash2i(cx, cy, seed);
                int idx = fastRangeUnsigned(hash, colors.size());
                ras.setPixel(x, y, colors.get(idx));
            }
        }
        Texture2D tex = new Texture2D(image);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setAnisotropicFilter(1);
        tex.setWrap(Texture.WrapMode.Repeat);
        return tex;
    }

    // Convenience varargs version
    public static Texture2D randomBlocks(int width, int height, int cellSize, int seed, ColorRGBA... colors) {
        return randomBlocks(width, height, cellSize, seed, Arrays.asList(colors));
    }

    public static Texture2D stripes(int width, int height, int bands, ColorRGBA a, ColorRGBA b, boolean vertical) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        Image image = new Image(Image.Format.RGBA8, w, h, ByteBuffer.allocateDirect(w * h * 4));
        ImageRaster ras = ImageRaster.create(image);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pos = vertical ? x : y;
                int bandIndex = (pos * bands) / (vertical ? w : h);
                boolean useA = (bandIndex & 1) == 0;
                ras.setPixel(x, y, useA ? a : b);
            }
        }
        Texture2D tex = new Texture2D(image);
        tex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
        tex.setMinFilter(com.jme3.texture.Texture.MinFilter.Trilinear);
        tex.setAnisotropicFilter(4);
        tex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
        return tex;
    }

    // 32-bit integer hash mixer for (x,y,seed)
    private static int hash2i(int x, int y, int seed) {
        int h = seed;
        h ^= 0x9E3779B9; // golden ratio
        h = mix32(h + x * 0x85EBCA6B);
        h = mix32(h + y * 0xC2B2AE35);
        return h;
    }

    private static int mix32(int z) {
        z ^= (z >>> 16);
        z *= 0x7feb352d;
        z ^= (z >>> 15);
        z *= 0x846ca68b;
        z ^= (z >>> 16);
        return z;
    }

    // Unbiased range reduction: maps unsigned 32-bit to [0,n)
    private static int fastRangeUnsigned(int x, int n) {
        long ux = x & 0xFFFFFFFFL;
        return (int)((ux * (long)n) >>> 32);
    }
}
