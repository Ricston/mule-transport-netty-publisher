package com.ricston.nettypublisher.exception;

public class UnsupportedDataTypeException extends Exception
{

    private static final long serialVersionUID = -4159718166243988058L;

    public UnsupportedDataTypeException(Object object){
        super("Unsupported data type: " + object.getClass().getName());
    }

}


