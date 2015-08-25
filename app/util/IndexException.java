package util;

public class IndexException extends Exception {

    private static final long serialVersionUID = 1L;

    private int status;

    public IndexException(int status, String message) {
        super(message);
        this.status = status;
    }

    public IndexException(Throwable cause) {
        super(cause);
        this.status = 500;
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
        this.status = 500;
    }

    public int getStatus() {
        return status;
    }
}
