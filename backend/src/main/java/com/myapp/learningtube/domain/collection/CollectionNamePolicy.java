package com.myapp.learningtube.domain.collection;

import java.util.Locale;

/** 컬렉션 이름: trim 후 보관, 중복 검사는 대소문자 무시(정규화 키 = trim + lower). */
public final class CollectionNamePolicy {

    private CollectionNamePolicy() {}

    public static String trimName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.strip();
    }

    /** DB/JPQL 비교용: trim + 소문자(ROOT). */
    public static String normalizedKey(String trimmedName) {
        return trimmedName.toLowerCase(Locale.ROOT);
    }
}
