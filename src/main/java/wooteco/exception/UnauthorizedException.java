package wooteco.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("인증 실패");
    }
}