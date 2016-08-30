package cn.edu.pku.sei.plde.conqueroverfitting.localization.common.container.various;

import java.util.Map;

import static cn.edu.pku.sei.plde.conqueroverfitting.localization.common.container.classic.MetaMap.newHashMap;

@SuppressWarnings("rawtypes")
public class NullBag<T> extends Bag<T> {

    @SuppressWarnings("unchecked")
    protected static <T> NullBag<T> instance() {
        if (instance == null) {
            Map<?, Integer> empty = newHashMap();
            instance = new NullBag(empty);
        }
        return instance;
    }

    private NullBag(Map<T, Integer> emptyMap) {
        super(emptyMap);
    }

    private static NullBag instance;
}
