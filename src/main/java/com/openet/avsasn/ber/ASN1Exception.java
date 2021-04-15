package avsasn.ber;

public class ASN1Exception extends Exception
{
    private static final long serialVersionUID = 2462733685628597741L;

    protected Exception originalCause;
    
    public ASN1Exception(Exception exception)
    {
        originalCause = exception;
    }

    public ASN1Exception(String s)
    {
        super(s);
    }

    public Throwable getCause()
    {
        return originalCause;
    }

    public String getMessage()
    {
        if(originalCause != null)
            return originalCause.getMessage();
        else
            return super.getMessage();
    }

   
}
