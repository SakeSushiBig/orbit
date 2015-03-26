/*
 Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ea.orbit.util;

import com.ea.orbit.exception.UncheckedException;

import java.io.IOException;
import java.net.ServerSocket;

public class NetUtils
{
    private NetUtils()
    {
        // Placating HideUtilityClassConstructorCheck
    }

    public static int findFreePort()
    {
        try (ServerSocket socket1 = new ServerSocket(0))
        {
            return socket1.getLocalPort();
        }
        catch (IOException e)
        {
            throw new UncheckedException("Error trying to find a free port", e);
        }
    }

    public static int findFreePort(int start)
    {
        if (start == 0)
        {
            return findFreePort();
        }
        for (int i = start; i < 0xffff; i += (Math.random() * 50))
        {
            try (ServerSocket socket1 = new ServerSocket())
            {
                return socket1.getLocalPort();
            }
            catch (final IOException e)
            {
                // ignoring
            }
        }
        throw new UncheckedException("No free ports found");
    }
}
