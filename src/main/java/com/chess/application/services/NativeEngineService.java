package com.chess.application.services;


import com.chess.application.model.NativePayload;
import com.chess.application.model.ReturnPayload;

import org.springframework.stereotype.Service;

@Service
public class NativeEngineService {

    static {
        System.loadLibrary("chess"); // libchess.dylib
    }

    public native ReturnPayload test_java_interface(NativePayload nativePayload);
}
