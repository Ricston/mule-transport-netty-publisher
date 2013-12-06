/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.ricston.nettypublisher;

public class UnknownServerTypeException extends Exception
{

    private static final long serialVersionUID = 5380138267166095980L;
    
    public UnknownServerTypeException(String serverType){
        super("Unknown server type: " + serverType);
    }

}


