package com.myapp.learningtube.domain.video;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class UserVideoSpecs {

    private UserVideoSpecs() {}

    public static Specification<UserVideo> ownedBy(Long userId) {
        return (root, q, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<UserVideo> learningStatusEq(LearningStatus status) {
        if (status == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("learningStatus"), status);
    }

    public static Specification<UserVideo> archivedEq(Boolean archived) {
        if (archived == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("archived"), archived);
    }

    public static Specification<UserVideo> titleContainsIgnoreCase(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<UserVideo, Video> video = root.join("video", JoinType.INNER);
            return cb.like(cb.lower(video.get("title")), pattern);
        };
    }
}
