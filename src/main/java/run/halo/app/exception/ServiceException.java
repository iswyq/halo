package run.halo.app.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception caused by service.
 * 针对service层中出现错误则抛这个错误
 * @author johnniang
 */
public class ServiceException extends AbstractHaloException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
