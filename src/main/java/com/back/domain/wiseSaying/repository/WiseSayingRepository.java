package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WiseSayingRepository {
    private final String dbPath = "db/wiseSaying/";
    private final List<WiseSaying> wiseSayings;
    private int lastId = 0;

    // 서비스 시작 시 dbPath로 된 폴더를 생성 (없으면 패스)
    public WiseSayingRepository() {
        new File(dbPath).mkdirs();
        wiseSayings = new ArrayList<>();
    }

    // 만약 수정, 삭제, 등록 등 리스트에 변화가 일어날 수 있는 작업이 발생하면
    // 오류가 생길 수 있으므로 명언의 복사본을 만들어 반환
    public List<WiseSaying> getAllWiseSayings() {
        return new ArrayList<>(wiseSayings);
    }


    // data.json과 lastId.txt 파일을 읽어 각각 wiseSayings와 lastId 변수에 저장
    public void loadAllData() {
        reloadJson();
        lastId = parseLastId();
    }

    // data.json으로부터 값을 불러온다.
    private void reloadJson() {
        File[] jsonData = new File(dbPath).listFiles(
                file -> file.getName().endsWith(".json") && !file.getName().contains("data")
        );

        if (jsonData == null) {
            return;
        }

        for (File data : jsonData) {
            Path path = data.toPath();

            try {
                String json = Files.readString(path)
                        .trim()
                        .replaceAll("[\\n\\r\\t{}\"]", "");

                int id = Integer.parseInt(extractString(json, "id"));
                String author = extractString(json, "author");
                String content = extractString(json, "content");

                WiseSaying wiseSaying = new WiseSaying(author, content);

                wiseSaying.setId(id);

                wiseSayings.add(wiseSaying);

            } catch (IOException | IllegalArgumentException e) { }
        }

        wiseSayings.sort(Comparator.comparingInt(WiseSaying::getId));
    }

    // json 파일로부터 각 키에 맞는 값을 필터링하여 문자열로 반환.
    private String extractString(String json, String key) {
        Matcher matcher = Pattern.compile(key + "\\s*:\\s*([^,]+)(?:,|$)")
                .matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Key not found or invalid format: " + key);
        }

        return matcher.group(1);
    }

    // lastId.txt 파일에서 마지막으로 저장된 id를 반환.
    // 파일이 존재하지 않거나, 오류가 발생하면 0 반환
    private int parseLastId() {
        Path path = Paths.get(dbPath + "lastId.txt");

        if (!Files.exists(path)) {
            return 0;
        }

        try {
            return Integer.parseInt(Files.readString(path));
        } catch (IOException e) {
            return 0;
        }
    }

    // 새 명언을 생성 후, 리스트에 추가하고, 파일에 저장
    // 파일에 저장하는 과정에서 오류가 발생하면 리스트 추가를 취소하고 lastId 수정 반영
    // 생성된 명언 객체 반환. 없으면 null 반환
    public WiseSaying save(WiseSaying wiseSaying) {
        boolean isNew = wiseSaying.isNew();

        if (isNew) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        try {
            saveToJsonFile(wiseSaying);
            return wiseSaying;

        } catch (RuntimeException e) {
            if (isNew) {
                wiseSayings.removeLast();
                lastId--;
            }
            return null;
        }
    }

    // 명언 리스트를 순회하여 id에 맞는 명언 반환. 없으면 null 반환
    public WiseSaying searchById(int id) {
        return wiseSayings
                .stream()
                .filter(w -> w.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // 명언을 수정 후 파일에 저장
    // 업데이트 과정에 오류가 발생하면 기존으로 되돌림
    public WiseSaying update(WiseSaying wiseSaying, String author, String content) {
        String previousAuthor = wiseSaying.getAuthor();
        String previousContent = wiseSaying.getContent();

        wiseSaying.setAuthor(author);
        wiseSaying.setContent(content);

        WiseSaying updateResult = save(wiseSaying);

        if (updateResult == null) {
            wiseSaying.setAuthor(previousAuthor);
            wiseSaying.setContent(previousContent);
        }

        return updateResult;
    }

    // id를 기반으로 삭제하고자 하는 명언의 index를 검색하고
    // 리스트 및 (id).json 파일 삭제
    // 삭제에 실패할 시 기존 index에 명언 다시 추가
    public WiseSaying delete(int id) {
        int index = getIndexById(id);
        if (index == -1) {
            return null;
        }

        WiseSaying removedWiseSaying = wiseSayings.remove(index);

        if (!deleteFile(id + "json")) {
            wiseSayings.add(index, removedWiseSaying);
            return null;
        }
        return removedWiseSaying;
    }

    // data.json 파일 저장
    public void buildDataJson() {
        saveAsFile("data.json", saveToDataJsonFile());
    }

    // lastId.txt 파일 저장
    // 저장 실패 시 RuntimeException 발생
    public void saveLastId() {
        saveAsFile("lastId.txt", String.valueOf(lastId));
    }

    // 파일 저장 시도
    // 실패 시 RuntimeException 반환
    private void saveAsFile(String fileName, String value) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbPath + fileName))){
            writer.write(value);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 명언 파일 삭제
    // 성공 시 true, 실패 시 false 반환
    private boolean deleteFile(String fileName) {
        Path path = Paths.get(dbPath + fileName);

        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // id를 기반으로 index 반환
    // 찾지 못하였을 경우 -1 반환
    private int getIndexById(int id) {
        for (int i = 0; i < wiseSayings.size(); i++) {
            if (id == wiseSayings.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    // 하나의 명언을 파일로 저장함
    // 실패 시 RuntimeException 발생
    private void saveToJsonFile(WiseSaying wiseSaying) {
        String jsonString = toJsonString(wiseSaying);
        saveAsFile(wiseSaying.getId() + ".json", jsonString);
    }

    // data.json 파일을 만들기 위해 명언 리스트를 가공하여
    // String으로 반환. 비어있으면 "[]"로 반환
    private String saveToDataJsonFile() {
        if (wiseSayings.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < wiseSayings.size(); i++) {
            sb.append(toDataJsonString(wiseSayings.get(i)));
            if (i != wiseSayings.size() - 1) {
                sb.append(",\n");
            }
        }

        return sb.append("\n]").toString();
    }

    // wiseSaying을 가공하여 문자열로 반환
    private String toJsonString(WiseSaying wiseSaying) {
        return "{\n" +
                "\t\"id\": " + wiseSaying.getId() + ", \n" +
                "\t\"author\": \"" + escape(wiseSaying.getAuthor()) + "\", \n" +
                "\t\"content\": \"" + escape(wiseSaying.getContent()) + "\"\n" +
                "}";
    }

    // wiseSaying을 가공하여 문자열로 반환하는데,
    // 각 항목들에 대해 띄어쓰기가 적용됨
    private String toDataJsonString(WiseSaying wiseSaying) {
        return "\t{\n" +
                "\t\t\"id\": " + wiseSaying.getId() + ", \n" +
                "\t\t\"author\": \"" + escape(wiseSaying.getAuthor()) + "\", \n" +
                "\t\t\"content\": \"" + escape(wiseSaying.getContent()) + "\"\n" +
                "\t}";
    }

    // 입력된 명언들을 json 형태에 맞게 가공하기 위한 로직
    private String escape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
