package com.back.domain.system.controller;

import com.back.domain.wiseSaying.service.WiseSayingService;

public class SystemController {

    private final WiseSayingService service;
    private static boolean terminated = false;

    public SystemController(WiseSayingService service) {
        this.service = service;
    }

    public void terminate() {
        service.terminateService();
        terminated = true;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
