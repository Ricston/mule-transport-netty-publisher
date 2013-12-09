/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.ricston.nettypublisher.exception;

public class UnsupportedDataTypeException extends Exception
{

    private static final long serialVersionUID = -4159718166243988058L;

    public UnsupportedDataTypeException(Object object){
        super("Unsupported data type: " + object.getClass().getName());
    }

}


