package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.Request;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;


public class App {

    void run() {
        Scanner scanner = new Scanner(System.in);

        WiseSayingService wiseSayingService = new WiseSayingService();
        SystemController systemController = new SystemController(wiseSayingService);
        WiseSayingController wsController = new WiseSayingController(scanner, wiseSayingService);

        wiseSayingService.startService();
        System.out.println("== 명령 앱 ==");

        while (!systemController.isTerminated()) {
            System.out.print("명령 : ");
            String command = scanner.nextLine().trim();

            try {
                Request request = new Request(command);

                switch (request.getCommand()) {
                    case 종료 -> systemController.terminate();
                    case 등록 -> wsController.register();
                    case 빌드 -> wsController.build();
                    case 목록 -> wsController.showAllWiseSayings();
                    case 수정 -> wsController.edit(request);
                    case 삭제 -> wsController.remove(request);
                }

            } catch (IllegalArgumentException e) {
                // 유효하지 않은 명령어 입력 처리
                System.out.println("유효하지 않은 명령입니다. 다시 시도해주세요.");

            } catch (RuntimeException e) {
                // 명령어 입력 외에 오류 처리 로직
                System.out.println("오류가 발생하여 작업이 중단되었습니다. 다시 시도해주세요.");
                System.out.println("오류 내용 : " + e.getMessage());
            }
        }
    }
}
