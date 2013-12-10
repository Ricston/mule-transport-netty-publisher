package com.ricston.nettypublisher.exception;

public class UnknownServerTypeException extends Exception
{

    private static final long serialVersionUID = 5380138267166095980L;
    
    public UnknownServerTypeException(String serverType){
        super("Unknown server type: " + serverType);
    }

}


