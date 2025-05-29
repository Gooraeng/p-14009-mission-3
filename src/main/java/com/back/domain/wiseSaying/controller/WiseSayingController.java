package com.back.domain.wiseSaying.controller;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;


public class WiseSayingController {

    private final WiseSayingService service;
    private final Scanner scanner;

    public WiseSayingController(Scanner scanner, WiseSayingService service) {
        this.scanner = scanner;
        this.service = service;
    }

    // 명언 등록
    // 필드 별 입력으로 완성된 값들을 통해 새 명언 생성
    public void register() {
        String content = fillField("명언");
        String author = fillField("작가");

        WiseSaying newWiseSaying = service.createNewWiseSaying(author, content);

        if (newWiseSaying == null) {
            System.out.println("명언 등록에 실패했습니다.");
            return;
        }
        System.out.printf("%d번 명언이 등록되었습니다.\n", newWiseSaying.getId());
    }

    // 명언 수정
    // "id" 필드로부터 값을 검색 후 알 수 없다면 -1 반환
    public void edit(final Request request) {
        int id = request.getIntValueByKey("id", -1);
        if (id == -1) {
            System.out.println("유효하지 않은 입력입니다. 다시 입력해주세요. 예) 수정?id=1");
            return;
        }

        WiseSaying foundWiseSaying = service.searchOneWiseSaying(id);
        if (foundWiseSaying == null) {
            System.out.printf("%d번 명언은 존재하지 않습니다.%n", id);
            return;
        }

        System.out.printf("명언(기존) : %s\n", foundWiseSaying.getContent());
        String newContent = fillField("명언");

        System.out.printf("작가(기존) : %s\n", foundWiseSaying.getAuthor());
        String newAuthor = fillField("작가");

        if (service.modifyWiseSaying(foundWiseSaying, newAuthor, newContent) == null) {
            System.out.println("수정에 실패하였습니다. 다시 시도해주세요.");
        }
    }

    // 입력된 item을 토대로 사용자에게 입력을 받음
    // 특수문자 여부 확인 후 성공 시 반환
    private String fillField(String item) {
        String finalItem = "";
        boolean passed = false;

        while (!passed) {
            System.out.printf("%s : ", item);
            finalItem = scanner.nextLine().trim();

            passed = isValidInput(finalItem);

            if (!passed) {
                System.out.println("\".\"을 제외한 특수문자는 입력하실 수 없습니다.");
            }
        }

        return finalItem;
    }

    // .을 제외한 특수문자 입력이 확인될 시 false를 반환
    private boolean isValidInput(String string) {
        return string.matches("^[가-힣A-Za-z0-9.\\s]+$");
    }

    // 명언 삭제 로직
    public void remove(final Request request) {
        int id = request.getIntValueByKey("id", -1);
        if (id == -1) {
            System.out.println("유효하지 않은 입력입니다. 다시 입력해주세요. 예) 삭제?id=1");
            return;
        }

        WiseSaying removedWiseSaying = service.removeWiseSaying(id);

        if (removedWiseSaying == null) {
            System.out.printf("%d번 명언은 존재하지 않습니다.\n", id);
            return;
        }

        System.out.printf("%d번 명언이 삭제되었습니다.\n", id);
    }

    // 명언 목록 출력
    public void showAllWiseSayings() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("--------------------");

        service.searchAllWiseSayings()
                .reversed()
                .forEach(System.out::println);
    }

    // data.json 생성(수정)을 진행함
    // 실패 시 RuntimeException이 App으로 전달됨
    public void build() {
        service.createWiseSayingDataLog();
        System.out.println("data.json 파일의 내용이 갱신되었습니다.");
    }
}
