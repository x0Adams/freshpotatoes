package hu.notkulonme.DataTransferer.entity.dto;

public interface DumpDocument {
    String qid();
    default int getIdFromQid() {
        return Integer.parseInt(qid().substring(1));
    }
}
