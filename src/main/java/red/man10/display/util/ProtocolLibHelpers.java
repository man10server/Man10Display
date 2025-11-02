package red.man10.display.util;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import java.util.Collection;
import java.util.Optional;

public final class ProtocolLibHelpers {
    private ProtocolLibHelpers() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static StructureModifier<Collection<?>> getCollections(PacketContainer packet) {
        return (StructureModifier) packet.getModifier().withType(Collection.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeEmptyCollection(PacketContainer packet, int index) {
        StructureModifier<Collection> mod = (StructureModifier) packet.getModifier().withType(Collection.class);
        if (mod != null && mod.size() > 0) {
            mod.write(index, java.util.Collections.emptyList());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeEmptyOptional(PacketContainer packet, int index) {
        StructureModifier<Optional> mod = (StructureModifier) packet.getModifier().withType(Optional.class);
        if (mod != null && mod.size() > 0) {
            mod.write(index, Optional.empty());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static StructureModifier<Collection> getCollectionModifier(PacketContainer packet) {
        return (StructureModifier) packet.getModifier().withType(Collection.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeCollection(PacketContainer packet, int index, java.util.Collection<?> collection) {
        StructureModifier<Collection> mod = (StructureModifier) packet.getModifier().withType(Collection.class);
        if (mod != null && mod.size() > index) {
            mod.write(index, (Collection) collection);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static StructureModifier<Optional> getOptionalModifier(PacketContainer packet) {
        return (StructureModifier) packet.getModifier().withType(Optional.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeOptionalCollection(PacketContainer packet, int index, java.util.Collection<?> collection) {
        StructureModifier<Optional> mod = (StructureModifier) packet.getModifier().withType(Optional.class);
        if (mod != null && mod.size() > index) {
            // Collectionを明示的にキャストしてOptionalにラップ
            Collection<?> castCollection = (Collection<?>) collection;
            Optional<?> optionalValue = Optional.of(castCollection);
            mod.write(index, (Optional) optionalValue);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeOptionalCollectionToField(PacketContainer packet, int fieldIndex, java.util.Collection<?> collection) {
        // 指定されたフィールドに直接書き込む（modifierから直接）
        // 重要: CollectionをArrayListに変換して型情報を保持
        java.util.ArrayList<Object> typedList = new java.util.ArrayList<>();
        for (Object item : collection) {
            typedList.add(item);
        }
        
        try {
            StructureModifier<Object> baseModifier = packet.getModifier();
            if (baseModifier.size() > fieldIndex) {
                // Optional.of()でラップ（型情報を保持）
                Optional<java.util.Collection<Object>> optionalValue = Optional.of(typedList);
                // フィールドインデックスを指定して直接書き込む
                baseModifier.write(fieldIndex, optionalValue);
                return;
            }
        } catch (Exception e) {
            // エラーは無視してフォールバックへ
        }
        
        // フォールバック: Optional modifierを使用
        try {
            StructureModifier<Optional> mod = (StructureModifier) packet.getModifier().withType(Optional.class);
            if (mod != null && mod.size() > fieldIndex) {
                // ArrayListを使用して型情報を保持
                Optional<java.util.Collection> optionalValue = Optional.of((java.util.Collection) typedList);
                mod.write(fieldIndex, (Optional) optionalValue);
            }
        } catch (Exception e) {
            // エラーは無視
        }
    }
}
