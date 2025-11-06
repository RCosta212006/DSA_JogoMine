package jogo.engine;

import com.jme3.scene.Spatial;
import jogo.gameobject.GameObject;

import java.util.Map;
import java.util.WeakHashMap;

public class RenderIndex {
    private final Map<Spatial, GameObject> bySpatial = new WeakHashMap<>();

    public synchronized void register(Spatial spatial, GameObject obj) {
        if (spatial != null && obj != null) bySpatial.put(spatial, obj);
    }

    public synchronized void unregister(Spatial spatial) {
        if (spatial != null) bySpatial.remove(spatial);
    }

    public synchronized GameObject lookup(Spatial spatial) {
        return bySpatial.get(spatial);
    }
}

