package bugBust.transitgo.exception;

public class BusIdAlreadyExistException extends Exception{
    public BusIdAlreadyExistException(String message){
        super(message);
    }
}
