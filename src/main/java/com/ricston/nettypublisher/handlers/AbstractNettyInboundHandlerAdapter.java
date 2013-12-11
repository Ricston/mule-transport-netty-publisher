/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.ricston.nettypublisher.handlers;

import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractNettyInboundHandlerAdapter extends ChannelInboundHandlerAdapter
{
    public abstract void close();
}


