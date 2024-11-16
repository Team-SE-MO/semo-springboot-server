package sandbox.semo.application.file.exception;

import lombok.Getter;

@Getter
public class FileBusinessException extends RuntimeException{
    private final FileErrorCode fileErrorCode;

    public FileBusinessException(FileErrorCode fileErrorCode) {
        super(fileErrorCode.getMessage());
        this.fileErrorCode = fileErrorCode;
    }
}
