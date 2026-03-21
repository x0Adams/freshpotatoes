package hu.notkulonme.DataTransferer.entity.dto;

import java.util.Collections;
import java.util.List;

public interface DumpDocument {
    String qid();

    default int getIdFromQid() {
        return parseQid(qid());
    }

    default int parseQid(String value) {
        if (value == null || value.length() < 2) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    default String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "None";
        }
        return value.trim();
    }

    default List<String> safeList(List<String> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    default List<String> splitDate(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("-"));
    }

    default int toIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
