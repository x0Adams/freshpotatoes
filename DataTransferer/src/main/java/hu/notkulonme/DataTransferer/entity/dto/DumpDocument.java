package hu.notkulonme.DataTransferer.entity.dto;

public interface DumpDocument {
    String qid();
    default int getIdFromQid() {
        try {
            return Integer.parseInt(qid().substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }

    }
}
