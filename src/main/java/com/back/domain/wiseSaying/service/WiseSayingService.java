package com.back.domain.wiseSaying.service;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;

import java.util.List;


public class WiseSayingService {

    private final WiseSayingRepository repository = new WiseSayingRepository();

    // Repository에서 데이터를 불러옴
    public void startService() {
        repository.loadAllData();
    }

    // 새 명언 모음을 만들어 controller로 반환
    public WiseSaying createNewWiseSaying(String author, String content) {
        WiseSaying wiseSaying = new WiseSaying(author, content);
        return repository.save(wiseSaying);
    }

    // 명언 리스트를 controller로 반환
    public List<WiseSaying> searchAllWiseSayings() {
        return repository.getAllWiseSayings();
    }

    // 찾으려는 ID를 통해 명언을 controller로 반환
    public WiseSaying searchOneWiseSaying(int id) {
        return repository.searchById(id);
    }

    // 명언을 수정함
    // 성공 시 true, 실패 시 false 반환
    public WiseSaying modifyWiseSaying(WiseSaying wiseSaying, String updatedAuthor, String updatedContent) {
        return repository.update(wiseSaying, updatedAuthor, updatedContent);
    }

    // 명언을 찾아 있으면 삭제함
    // 성공 시 true, 실패 시 false 반환
    public WiseSaying removeWiseSaying(int id) {
        return repository.delete(id);
    }

    // 명언 목록에 대한 로그를 작성함
    public void createWiseSayingDataLog() {
        repository.buildDataJson();
    }

    // 서비스 종료
    public void terminateService() {
        repository.saveLastId();
    }
}
