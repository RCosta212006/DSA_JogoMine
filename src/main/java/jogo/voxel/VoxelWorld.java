package jogo.voxel;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.appstate.PlayerAppState;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.BlockItem;
import jogo.gameobject.item.ItemSlot;
import jogo.util.Hit;
import jogo.util.PerlinNoise;
import jogo.util.ProcTextures;
import jogo.voxel.blocks.Umbreakable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class VoxelWorld {
    private final AssetManager assetManager;
    private final int sizeX, sizeY, sizeZ;
    private final VoxelPalette palette;

    private final Node node = new Node("VoxelWorld");
    private final Map<Byte, Geometry> geoms = new HashMap<>();
    private final Map<Byte, Material> materials = new HashMap<>();

    private boolean lit = true;       // Shading: On by default
    private boolean wireframe = false; // Wireframe: Off by default
    private boolean culling = true;   // Culling: On by default
    private int groundHeight = 20; // baseline Y level

    // Chunked world data
    private final int chunkSize = Chunk.SIZE;
    private final int chunkCountX, chunkCountY, chunkCountZ;
    private final Chunk[][][] chunks;

    public VoxelWorld(AssetManager assetManager, int sizeX, int sizeY, int sizeZ) {
        this.assetManager = assetManager;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palette = VoxelPalette.defaultPalette();
        // Remove old vox array
        // this.vox = new byte[sizeX][sizeY][sizeZ];
        this.chunkCountX = (int)Math.ceil(sizeX / (float)chunkSize);
        this.chunkCountY = (int)Math.ceil(sizeY / (float)chunkSize);
        this.chunkCountZ = (int)Math.ceil(sizeZ / (float)chunkSize);
        this.chunks = new Chunk[chunkCountX][chunkCountY][chunkCountZ];
        for (int cx = 0; cx < chunkCountX; cx++)
            for (int cy = 0; cy < chunkCountY; cy++)
                for (int cz = 0; cz < chunkCountZ; cz++)
                    chunks[cx][cy][cz] = new Chunk(cx, cy, cz);
        initMaterials();
    }

    // Helper to get chunk and local coordinates
    private Chunk getChunk(int x, int y, int z) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;
        int cz = z / chunkSize;
        if (cx < 0 || cy < 0 || cz < 0 || cx >= chunkCountX || cy >= chunkCountY || cz >= chunkCountZ) return null;
        return chunks[cx][cy][cz];
    }
    private int lx(int x) { return x % chunkSize; }
    private int ly(int y) { return y % chunkSize; }
    private int lz(int z) { return z % chunkSize; }

    // Block access
    public byte getBlock(int x, int y, int z) {
        Chunk c = getChunk(x, y, z);
        if (c == null) return VoxelPalette.AIR_ID;
        if (!inBounds(x,y,z)) return VoxelPalette.AIR_ID;
        return c.get(lx(x), ly(y), lz(z));
    }
    public void setBlock(int x, int y, int z, byte id) {
        Chunk c = getChunk(x, y, z);
        if (c != null) {
            c.set(lx(x), ly(y), lz(z), id);
            c.markDirty();
            // If on chunk edge, mark neighbor dirty
            if (lx(x) == 0) markNeighborChunkDirty(x-1, y, z);
            if (lx(x) == chunkSize-1) markNeighborChunkDirty(x+1, y, z);
            if (ly(y) == 0) markNeighborChunkDirty(x, y-1, z);
            if (ly(y) == chunkSize-1) markNeighborChunkDirty(x, y+1, z);
            if (lz(z) == 0) markNeighborChunkDirty(x, y, z-1);
            if (lz(z) == chunkSize-1) markNeighborChunkDirty(x, y, z+1);
        }
    }

    private void markNeighborChunkDirty(int x, int y, int z) {
        Chunk n = getChunk(x, y, z);
        if (n != null) n.markDirty();
    }

    public boolean breakAt(int x, int y, int z, Player player) {
        if (!inBounds(x,y,z)) return false;
        var info = palette.get(getBlock(x,y,z));
        if (info instanceof Umbreakable) return false;
        BlockItem item = new BlockItem(info.getName(), getBlock(x,y,z));
        ItemSlot slot = new ItemSlot(item, 1);
        // adicionar a hotbar do player
        if (player != null) {
            player.addToHotbar(slot);

        }

        setBlock(x, y, z, VoxelPalette.AIR_ID);
        return true;
    }

    public Node getNode() { return node; }

    //TODO this is where you'll generate your world
    public void generateLayers() {
        System.out.println("Generating terrain using Perlin noise...");

        long seed = System.currentTimeMillis();
        PerlinNoise noise = new PerlinNoise(seed);
        PerlinNoise caveNoise = new PerlinNoise(seed + 1000);
        Random surfaceRand = new Random(seed + 500); // para decidir quicksand na superfície
        final float quicksandChance = 0.005f;// 0.5% de hipótese (ajusta aqui)

        float scale = 0.03f; // Controla a frequência das colinas (menor = colinas maiores).
        float amplitude = 13f;   // Quanto o ruído altera a altura em Y.
        int baseHeight = groundHeight; // baseline Y (like sea level)

        // Controlam densidade/escala das cavernas
        // Distância mínima da superfície onde não se cava.
        float caveScale = 0.08f;
        float caveThreshold = 0.05f;
        int caveClearance = 10;

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {

                //Lógica para criar barreiras nas bordas do mundo
                boolean isEdge = (x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1);
                if(isEdge){
                    for(int y =0;y < sizeY;y++){
                        setBlock(x,y,z,VoxelPalette.BARRIER_ID);
                    }
                    continue; // pula o resto da geração para esta posição
                }

                // noise value entre -1 e 1, usado para altura da superfície
                double n = noise.noise(x * scale, 0, z * scale);

                // Altura da superficie
                int height = baseHeight + (int)(n * amplitude);

                // clamp altura para limites do mundo
                if (height < 0) height = 0;
                if (height >= sizeY) height = sizeY - 1;

                // Preencher colunas de blocos até a altura determinada
                for (int y = 0; y < sizeY; y++) {
                    if (y > height) { // acima da superfície
                        setBlock(x, y, z, VoxelPalette.AIR_ID);
                    } else if (y == height) {
                        // pequena chance de QuickSand na superfície (apenas se houver suporte abaixo)
                        int belowY = Math.max(0, y - 1);
                        boolean hasSupportBelow = getBlock(x, belowY, z) != VoxelPalette.AIR_ID;
                        if (hasSupportBelow && surfaceRand.nextFloat() < quicksandChance) {
                            setBlock(x, y, z, VoxelPalette.QUICKSAND_ID);// bloco de quicksand
                        } else {
                            setBlock(x, y, z, VoxelPalette.GRASS_ID); //resto da superficie é relva
                        }
                    } else if (y == height - 1) {
                        // camada diretamente abaixo da superfície
                        setBlock(x, y, z, VoxelPalette.DIRT_ID);
                    } else if(y == 0){
                        // ultima camdada do mundo
                        setBlock(x, y, z, VoxelPalette.BEDROCK_ID);
                    } else if(y < 15){ // abaixo de altura 15 , existe chance de magma
                        if (surfaceRand.nextFloat() < 0.02f) {
                            setBlock(x, y, z, VoxelPalette.MAGMA_ID);
                        } else {
                            //resto de blocos abaixo nivel 15 é pedra
                            setBlock(x, y, z, VoxelPalette.STONE_ID);
                        }
                    }else {
                        // entre terra e nivel 15 é sempre pedra
                        setBlock(x, y, z, VoxelPalette.STONE_ID);
                    }
                }

                // Aplicar cavernas com ruído 3D
                //maxCaveY garante que não escavamos muito perto da superfície
                int maxCaveY = Math.max(0, height - caveClearance);
                // varre de cima para baixo
                for (int y = maxCaveY; y >= 0; y--) {
                    double c = caveNoise.noise(x * caveScale, y * caveScale, z * caveScale);
                    if (c > caveThreshold) {
                        // Não remover se estamos no nível 0 (limite do mundo)
                        if (y == 0) continue;
                        // Mantém chão: só esculpe se o bloco abaixo não for AIR (ou seja, existe suporte)
                        if (getBlock(x, y - 1, z) != VoxelPalette.AIR_ID) {
                            setBlock(x, y, z, VoxelPalette.AIR_ID);
                        }
                    }
                }
            }
        }
        plantTrees(new Random(seed + 2000));// planta árvores na superfície
        populateOres(new Random(seed + 3000));// popula veios de minérios
        System.out.println("Terrain generation complete.");
    }

    private void plantTrees(Random rand) {
        final float treeProbability = 0.02f; // 2% por posição (ajuste)
        final int minTrunk = 4;
        final int maxTrunk = 6;
        final int canopyHeight = 3; // camadas de folhas acima do topo do tronco
        final int canopyRadius = 2; // raio máximo da copa

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                if (rand.nextFloat() >= treeProbability) continue;

                int yTop = getTopSolidY(x, z);// posição da superfície
                if (yTop < 0) continue;

                // só plantar em relva
                if (getBlock(x, yTop, z) != VoxelPalette.GRASS_ID) continue;

                // altura do tronco
                int trunkH = minTrunk + rand.nextInt(maxTrunk - minTrunk + 1);

                // check espaço vertical disponível
                if (yTop + trunkH + canopyHeight >= sizeY) continue;

                // check espaço livre para tronco
                boolean blocked = false;
                for (int ty = yTop + 1; ty <= yTop + trunkH; ty++) {
                    if (!inBounds(x, ty, z) || getBlock(x, ty, z) != VoxelPalette.AIR_ID) {
                        blocked = true; break;
                    }
                }
                if (blocked) continue;

                // planta tronco
                for (int ty = yTop + 1; ty <= yTop + trunkH; ty++) {
                    setBlock(x, ty, z, VoxelPalette.WOODBLOCK_ID);
                }

                int crownBaseY = yTop + trunkH;
                // planta copa em camadas: de crownBaseY até crownBaseY + canopyHeight - 1
                for (int dy = 0; dy < canopyHeight; dy++) {
                    int cy = crownBaseY + dy;//altura absuluta da camada de copa
                    int radius = canopyRadius - dy; // copa mais larga em baixo, menor em cima
                    if (radius < 0) radius = 0;
                    //preenche a camada de copa
                    for (int cx = x - radius; cx <= x + radius; cx++) {
                        for (int cz = z - radius; cz <= z + radius; cz++) {
                            if (!inBounds(cx, cy, cz)) continue;//limites do mundo
                            // evita sobreescrever o tronco ou blocos sólidos (mantém cavernas etc.)
                            byte current = getBlock(cx, cy, cz);
                            if (current == VoxelPalette.AIR_ID) {
                                // opcional: pequena chance de não colocar folha para variação
                                if (rand.nextFloat() < 0.95f) {
                                    setBlock(cx, cy, cz, VoxelPalette.LEAF_ID);
                                }
                            }
                        }
                    }
                }
                // opcional: pular x adjacentes para evitar árvores juntas demais
                x += 1;
            }
        }
    }
    private void populateOres(Random rand) {
        // Probabilidades por bloco exposto (ajuste conforme necessário)
        final float ironChance = 0.10f;    // mais comum
        final float goldChance = 0.012f;    // menos comum que ferro
        final float diamondChance = 0.0015f; // raro

        // Limites de profundidade (valores relativos ao tamanho do mundo)
        final int diamondMaxY = Math.max(3, sizeY / 6);      // só em profundidades baixas
        final int goldMaxY = Math.max(diamondMaxY + 1, sizeY / 3); // médio
        final int ironMaxY = Math.max(goldMaxY + 1, sizeY - 5);    // até próximas camadas de superfície

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int y = 1; y < sizeY - 1; y++) { // evita camada 0 e topo extremo
                    if (!inBounds(x, y, z)) continue;
                    if (getBlock(x, y, z) != VoxelPalette.STONE_ID) continue;
                    // só considerar pedra que esteja exposta a uma caverna (paredes/veias visíveis)
                    if (!isAdjacentToAir(x, y, z)) continue;

                    // diamante: somente em profundidade e muito raro
                    if (y <= diamondMaxY && rand.nextFloat() < diamondChance) {
                        placeOreVein(x, y, z, VoxelPalette.DIAMOND_ID, rand, 3, 6);
                        continue;
                    }

                    // ouro: mais provável que diamante, preferencialmente abaixo de certo Y
                    if (y <= goldMaxY && rand.nextFloat() < goldChance) {
                        placeOreVein(x, y, z, VoxelPalette.GOLD_ID, rand, 2, 5);
                        continue;
                    }

                    // ferro: mais comum e relativamente mais espalhado
                    if (y <= ironMaxY && rand.nextFloat() < ironChance) {
                        placeOreVein(x, y, z, VoxelPalette.IRON_ID, rand, 3, 7);
                    }
                }
            }
        }
    }

    /** Retorna true se alguma face 6-vizinha for ar (caverna). */
    private boolean isAdjacentToAir(int x, int y, int z) {
        if (!inBounds(x, y, z)) return false;
        // 6 vizinhos ortogonais
        int[][] offs = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for (int[] o : offs) {
            int nx = x + o[0], ny = y + o[1], nz = z + o[2];
            if (!inBounds(nx, ny, nz)) continue;
            if (getBlock(nx, ny, nz) == VoxelPalette.AIR_ID) return true;
        }
        return false;
    }

    /**
     * Cria um pequeno veio do tipo oreId a partir de (x,y,z).
     * O tamanho é aleatório entre minSize e maxSize; segue um passeio aleatório.
     */
    private void placeOreVein(int x, int y, int z, byte oreId, Random rand, int minSize, int maxSize) {
        int size = minSize + rand.nextInt(Math.max(1, maxSize - minSize + 1));
        int cx = x, cy = y, cz = z;
        for (int i = 0; i < size; i++) {
            if (!inBounds(cx, cy, cz)) break;
            // Só substituir pedra para não sobreescrever madeira/folhas/air/etc.
            if (getBlock(cx, cy, cz) == VoxelPalette.STONE_ID) {
                setBlock(cx, cy, cz, oreId);
            }
            // passo aleatório pequeno para formar um veio compacto
            cx += rand.nextInt(3) - 1;
            cy += rand.nextInt(3) - 1;
            cz += rand.nextInt(3) - 1;
        }
    }


    public int getTopSolidY(int x, int z) {
        if (x < 0 || z < 0 || x >= sizeX || z >= sizeZ) return -1;
        for (int y = sizeY - 1; y >= 0; y--) {
            if (palette.get(getBlock(x, y, z)).isSolid()) return y;
        }
        return -1;
    }

    public Vector3f getRecommendedSpawn() {
        int cx = sizeX / 2;
        int cz = sizeZ / 2;
        int ty = getTopSolidY(cx, cz);
        if (ty < 0) ty = groundHeight;
        return new Vector3f(cx + 0.5f, ty + 3.0f, cz + 0.5f);
    }

    private void initMaterials() {
        // Single material for STONE blocks
        Texture2D tex = ProcTextures.checker(128, 4, ColorRGBA.Gray, ColorRGBA.DarkGray);
        materials.put(VoxelPalette.STONE_ID, makeLitTex(tex, 0.08f, 16f));
    }

    private Material makeLitTex(Texture2D tex, float spec, float shininess) {
        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture("DiffuseMap", tex);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setColor("Specular", ColorRGBA.White.mult(spec));
        m.setFloat("Shininess", shininess);
        applyRenderFlags(m);
        return m;
    }

    private void applyRenderFlags(Material m) {
        m.getAdditionalRenderState().setFaceCullMode(culling ? RenderState.FaceCullMode.Back : RenderState.FaceCullMode.Off);
        m.getAdditionalRenderState().setWireframe(wireframe);
    }

    public void buildMeshes() {
        node.detachAllChildren();
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    chunk.buildMesh(assetManager, palette);
                    node.attachChild(chunk.getNode());
                }
            }
        }
    }

    public void buildPhysics(PhysicsSpace space) {
        // Build per-chunk static rigid bodies instead of a single world body
        if (space == null) return;
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    chunk.updatePhysics(space);
                }
            }
        }
    }

    public Optional<Hit> pickFirstSolid(Camera cam, float maxDistance) {
        Vector3f origin = cam.getLocation();
        Vector3f dir = cam.getDirection().normalize();

        int x = (int) Math.floor(origin.x);
        int y = (int) Math.floor(origin.y);
        int z = (int) Math.floor(origin.z);

        float tMaxX, tMaxY, tMaxZ;
        float tDeltaX, tDeltaY, tDeltaZ;
        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        float nextVoxelBoundaryX = x + (stepX > 0 ? 1 : 0);
        float nextVoxelBoundaryY = y + (stepY > 0 ? 1 : 0);
        float nextVoxelBoundaryZ = z + (stepZ > 0 ? 1 : 0);

        tMaxX = (dir.x != 0) ? (nextVoxelBoundaryX - origin.x) / dir.x : Float.POSITIVE_INFINITY;
        tMaxY = (dir.y != 0) ? (nextVoxelBoundaryY - origin.y) / dir.y : Float.POSITIVE_INFINITY;
        tMaxZ = (dir.z != 0) ? (nextVoxelBoundaryZ - origin.z) / dir.z : Float.POSITIVE_INFINITY;

        tDeltaX = (dir.x != 0) ? stepX / dir.x : Float.POSITIVE_INFINITY;
        tDeltaY = (dir.y != 0) ? stepY / dir.y : Float.POSITIVE_INFINITY;
        tDeltaZ = (dir.z != 0) ? stepZ / dir.z : Float.POSITIVE_INFINITY;

        float t = 0f;
        // starting inside a solid block
        if (inBounds(x,y,z) && isSolid(x,y,z)) {
            return Optional.of(new Hit(new Vector3i(x,y,z), new Vector3f(0,0,0), 0f));
        }

        Vector3f lastNormal = new Vector3f(0,0,0);

        while (t <= maxDistance) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX; t = tMaxX; tMaxX += tDeltaX;
                    lastNormal.set(-stepX, 0, 0);
                } else {
                    z += stepZ; t = tMaxZ; tMaxZ += tDeltaZ;
                    lastNormal.set(0, 0, -stepZ);
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY; t = tMaxY; tMaxY += tDeltaY;
                    lastNormal.set(0, -stepY, 0);
                } else {
                    z += stepZ; t = tMaxZ; tMaxZ += tDeltaZ;
                    lastNormal.set(0, 0, -stepZ);
                }
            }

            if (!inBounds(x,y,z)) {
                if (t > maxDistance) break;
                continue;
            }
            if (isSolid(x,y,z)) {
                return Optional.of(new Hit(new Vector3i(x,y,z), lastNormal.clone(), t));
            }
        }
        return Optional.empty();
    }

    public boolean isSolid(int x, int y, int z) {
        if (!inBounds(x,y,z)) return false;
        return palette.get(getBlock(x, y, z)).isSolid();
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    public void setLit(boolean lit) {
        if (this.lit == lit) return;
        this.lit = lit;
        for (var e : geoms.entrySet()) {
            Geometry g = e.getValue();
            var oldMat = g.getMaterial();
            Texture tex = oldMat.getTextureParam("DiffuseMap") != null
                    ? oldMat.getTextureParam("DiffuseMap").getTextureValue()
                    : (oldMat.getTextureParam("ColorMap") != null ? oldMat.getTextureParam("ColorMap").getTextureValue() : null);
            Material newMat;
            if (this.lit) {
                newMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                if (tex != null) newMat.setTexture("DiffuseMap", tex);
                newMat.setBoolean("UseMaterialColors", true);
                newMat.setColor("Diffuse", ColorRGBA.White);
                newMat.setColor("Specular", ColorRGBA.White.mult(0.08f));
                newMat.setFloat("Shininess", 16f);
            } else {
                newMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                if (tex != null) newMat.setTexture("ColorMap", tex);
            }
            applyRenderFlags(newMat);
            g.setMaterial(newMat);
        }
    }

    public void setWireframe(boolean wireframe) {
        if (this.wireframe == wireframe) return;
        this.wireframe = wireframe;

        for (Geometry g : geoms.values()) applyRenderFlags(g.getMaterial());
    }

    public void setCulling(boolean culling) {
        if (this.culling == culling) return;
        this.culling = culling;
        for (Geometry g : geoms.values()) applyRenderFlags(g.getMaterial());
    }

    public boolean isLit() { return lit; }
    public boolean isWireframe() { return wireframe; }
    public boolean isCulling() { return culling; }

    public void toggleRenderDebug() {
        System.out.println("Toggled render debug");
        setLit(!isLit());
        setWireframe(!isWireframe());
        setCulling(!isCulling());
    }

    public int getGroundHeight() { return groundHeight; }

    public VoxelPalette getPalette() {
        return palette;
    }

    /**
     * Rebuilds meshes only for dirty chunks. Call this once per frame in your update loop.
     */
    public void rebuildDirtyChunks(PhysicsSpace physicsSpace) {
        int rebuilt = 0;
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    if (chunk.isDirty()) {
                        System.out.println("Rebuilding chunk: " + cx + "," + cy + "," + cz);
                        chunk.buildMesh(assetManager, palette);
                        chunk.updatePhysics(physicsSpace);
                        chunk.clearDirty();
                        rebuilt++;
                    }
                }
            }
        }
        if (rebuilt > 0) System.out.println("Chunks rebuilt this frame: " + rebuilt);
        if (rebuilt > 0 && physicsSpace != null) {
            physicsSpace.update(0); // Force physics space to process changes
            System.out.println("Physics space forced update after chunk physics changes.");
        }
    }

    /**
     * Clears the dirty flag on all chunks. Call after initial buildMeshes().
     */
    public void clearAllDirtyFlags() {
        for (int cx = 0; cx < chunkCountX; cx++)
            for (int cy = 0; cy < chunkCountY; cy++)
                for (int cz = 0; cz < chunkCountZ; cz++)
                    chunks[cx][cy][cz].clearDirty();
    }

    public int getMaxY() {
        return sizeY;
    }

    public int getMinY() {
        return 0;
    }

    // simple int3
    public static class Vector3i {
        public final int x, y, z;
        public Vector3i(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }

        public Vector3i(Vector3f vec3f) {
            this.x = (int) vec3f.x;
            this.y = (int) vec3f.y;
            this.z = (int) vec3f.z;
        }
    }
}
