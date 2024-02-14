package jpabook.jpashop.exception;

public class NotEnoughStockException extends RuntimeException { // alt+insert > override method

    public NotEnoughStockException() {
        super();
    }
    // 메세지랑 넘겨주고
    public NotEnoughStockException(String message) {
        super(message);
    }
    // 메세지랑 예외가 발생한 근원적인 인셉션을 넣어서 트레이스가 나오게 할 수 있음
    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

}
