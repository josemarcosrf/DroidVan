package project.van.fionaremote;


public abstract class BTResult<T> {
    private BTResult() {
    }

    public static final class Success<T> extends BTResult<T> {
        public T data;

        public Success(T data) {
            this.data = data;
        }
    }

    public static final class Error<T> extends BTResult<T> {
        public Exception exception;

        public Error(Exception exception) {
            this.exception = exception;
        }
    }
}
